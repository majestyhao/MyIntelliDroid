package fu.hao.intellidroid.core;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.ClassHierarchyMethodTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.DefaultContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXCFABuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import fu.hao.intellidroid.core.wrappers.AndroidAppMethodTargetSelector;
import fu.hao.intellidroid.core.wrappers.AndroidClassesAndMethods;
import fu.hao.intellidroid.core.wrappers.AndroidEntrypoint;
import fu.hao.intellidroid.core.wrappers.UIActivityMapping;
import fu.hao.intellidroid.utils.Log;

import java.util.*;

/**
 * Description: Look for the entry points and generate the call graph
 * Authors: Hao Fu(haofu@ucdavis.edu)
 * Date: 2016/8/30
 */
public class EntrypointAnalysis {
    private final String TAG = getClass().getSimpleName();

    private final IClassHierarchy classHierarchy;
    private final ManifestAnalysis manifestAnalysis;
    private final UIActivityMapping uiActivityMapping;
    private final CallGraphInfoListener callGraphInfoListener;

    private Map<IMethod, MethodReference> entrypointFrameworkMethodMap = new HashMap<>();

    private SSAPropagationCallGraphBuilder callGraphBuilder = null;
    private CallGraph callGraph = null;
    private PointerAnalysis pointerAnalysis = null;

    private Set<TypeReference> activities = new HashSet<>();
    private Set<TypeReference> fragments = new HashSet<>();

    public EntrypointAnalysis(IClassHierarchy classHierarchy, ManifestAnalysis manifestAnalysis, UIActivityMapping uiActivityMapping,
                              CallGraphInfoListener callGraphInfoListener) {
        this.classHierarchy = classHierarchy;
        this.manifestAnalysis = manifestAnalysis;
        this.uiActivityMapping = uiActivityMapping;
        this.callGraphInfoListener = callGraphInfoListener;

        // Get the entry points for the components listed in the manifest
        entrypointFrameworkMethodMap = getComponentEntries();
        List<Entrypoint> incrementalEntrypoints = new ArrayList<>();

        for (IMethod entrypoint : entrypointFrameworkMethodMap.keySet()) {
            incrementalEntrypoints.add(new AndroidEntrypoint(entrypoint, classHierarchy));
        }

        /* For all callback listener types, find subclasses/implementors and look for their constructors in the call graph.
        * If found, add overriden methods to entrypoints.
        * Do this instead of looking for callback registrations, since the class type may not be immediately obvious
        * and it can be costly to get this information precisely*/
        Log.debug(TAG, "Callback entrypoints:");
        boolean changed = true;
        while (changed) {
            changed = false;

        }

    }

    private String convertClassNameToWALA(String className) {
        String walaName = "L" + className.replace(".", "/");
        return walaName;
    }

    private Map<IMethod, MethodReference> getApplicationEntries() {
        Map<IMethod, MethodReference> applicationEntries = new HashMap<>();
        String appName = convertClassNameToWALA(manifestAnalysis.getAppName());

        if (!appName.isEmpty()) {
            IClass appClass = classHierarchy.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Application, appName));
            if (appClass != null) {
                for (IMethod appMethod : appClass.getDeclaredMethods()) {
                    if (AndroidClassesAndMethods.getAppLifecycleMethods().contains(appMethod.getSelector())) {
                        applicationEntries.put(appMethod, MethodReference.findOrCreate(AndroidClassesAndMethods.getApplicationClass(), appMethod.getSelector()));
                    }
                }
            } else {
                Log.err(TAG, "App classes cannot be identified!");
            }
        } else {
            Log.err(TAG, "App name is incorrect!");
        }

        return applicationEntries;
    }

    private Map<IMethod, MethodReference> getActivityEntries() {
        Map<IMethod, MethodReference> entries = new HashMap<>();
        List<String> activities = manifestAnalysis.getActivities();
        Set<String> uiDefinedHandlers = uiActivityMapping.getUILayoutDefinedHandlers();

        for (String activityName : activities) {
            String className = convertClassNameToWALA(activityName);
            IClass activityClass = classHierarchy.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Application, className));
            if (activityClass != null) {
                this.activities.add(activityClass.getReference());

                for (IMethod method : activityClass.getDeclaredMethods()) {
                    if (AndroidClassesAndMethods.getAppLifecycleMethods().contains(method.getSelector())) {
                        entries.put(method, MethodReference.findOrCreate(AndroidClassesAndMethods.getActivityClass(), method.getSelector()));
                    } else if (uiDefinedHandlers.contains(method.getSelector().getName().toString())) {
                        entries.put(method, AndroidClassesAndMethods.getOnClickMethod());
                    }
                }
            } else {
                Log.err(TAG, "Missing Activity class!");
            }
        }

        return entries;
    }

    private Map<IMethod, MethodReference> getServiceEntries() {
        Map<IMethod, MethodReference> entries = new HashMap<>();
        List<String> services = manifestAnalysis.getServices();

        for (String serviceName : services) {
            String className = convertClassNameToWALA(serviceName);
            IClass serviceClass = classHierarchy.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Application, className));

            if (serviceClass != null) {
                for (IMethod method : serviceClass.getDeclaredMethods()) {
                    if (AndroidClassesAndMethods.getServiceLifecycleMethods().contains(method.getSelector())) {
                        entries.put(method, MethodReference.findOrCreate(AndroidClassesAndMethods.getServiceClass(), method.getSelector()));
                    }
                }
            } else {
                Log.err(TAG, "Missing Service Class " + serviceName);
            }
        }

        return entries;
    }

    private Map<IMethod, MethodReference> getBroadcastReceiverEntries() {
        Map<IMethod, MethodReference> receiverEntrypoints = new HashMap<IMethod, MethodReference>();
        List<String> receivers = manifestAnalysis.getBroadcastReceivers();

        for (String receiverName : receivers) {
            String className = convertClassNameToWALA(receiverName);
            IClass receiverClass = classHierarchy.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Application, className));

            if (receiverClass == null) {
                Log.warn(TAG, "Missing manifest component: " + className);
                continue;
            }

            IMethod onReceiveMethod = receiverClass.getMethod(AndroidClassesAndMethods.getReceiverLifecycleMethod());

            if (onReceiveMethod != null) {
                receiverEntrypoints.put(onReceiveMethod, MethodReference.findOrCreate(AndroidClassesAndMethods.getReceiverClass(), AndroidClassesAndMethods.getReceiverLifecycleMethod()));
            }
        }

        return receiverEntrypoints;
    }

    private Map<IMethod, MethodReference> getContentProviderEntries() {
        Map<IMethod, MethodReference> providerEntrypoints = new HashMap<IMethod, MethodReference>();
        List<String> providers = manifestAnalysis.getContentProviders();

        for (String providerName : providers) {
            String className = convertClassNameToWALA(providerName);
            IClass providerClass = classHierarchy.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Application, className));

            if (providerClass == null) {
                Log.warn(TAG, "Missing manifest component: " + className);
                continue;
            }

            IMethod onCreateMethod = providerClass.getMethod(AndroidClassesAndMethods.getProviderLifecycleMethod());

            if (onCreateMethod != null) {
                providerEntrypoints.put(onCreateMethod, MethodReference.findOrCreate(AndroidClassesAndMethods.getProviderClass(), AndroidClassesAndMethods.getProviderLifecycleMethod()));
            }
        }

        return providerEntrypoints;
    }

    private Map<IMethod, MethodReference> getFragmentEntries() {
        Map<IMethod, MethodReference> fragmentEntrypoints = new HashMap<IMethod, MethodReference>();

        Collection<IClass> fragmentClasses = classHierarchy.computeSubClasses(TypeReference.findOrCreate(ClassLoaderReference.Extension, "Landroid/app/Fragment"));

        for (IClass fragmentClass : fragmentClasses) {
            fragments.add(fragmentClass.getReference());

            for (IMethod fragmentMethod : fragmentClass.getDeclaredMethods()) {
                if (AndroidClassesAndMethods.getFragmentLifecycleMethods().contains(fragmentMethod.getSelector())) {
                    fragmentEntrypoints.put(fragmentMethod, MethodReference.findOrCreate(AndroidClassesAndMethods.getFragmentClass(), fragmentMethod.getSelector()));
                }
            }
        }

        return fragmentEntrypoints;
    }

    /**
     * Method: getComponentEntries
     * Description: Get the entries of the components in the app.
     * Authors：Hao Fu(haofu@ucdavis.edu)
     * Date: 2016/8/30 13:13
     */
    private Map<IMethod, MethodReference> getComponentEntries() {
        Map<IMethod, MethodReference> componentEntries = new HashMap<>();

        componentEntries.putAll(getApplicationEntries());
        componentEntries.putAll(getActivityEntries());
        componentEntries.putAll(getServiceEntries());
        componentEntries.putAll(getBroadcastReceiverEntries());
        componentEntries.putAll(getContentProviderEntries());
        componentEntries.putAll(getFragmentEntries());

        return componentEntries;
    }

    private void makeCallgraphIncremental(Iterable<Entrypoint> entrypoints, AnalysisScope scope, IClassHierarchy classHierarchy) {
        AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
        options.setSelector(new AndroidAppMethodTargetSelector(new ClassHierarchyMethodTargetSelector(classHierarchy), classHierarchy, manifestAnalysis));
        options.setSelector(new ClassHierarchyMethodTargetSelector(classHierarchy));

        // 0-1-CFA Call graph builder
        callGraphBuilder = ZeroXCFABuilder.make(classHierarchy, options, new AnalysisCache(), new DefaultContextSelector(options, classHierarchy), null, ZeroXInstanceKeys.NONE);
        callGraphInfoListener.clear();
        callGraphBuilder.setBuilderListener(callGraphInfoListener);

        try {
            callGraph = callGraphBuilder.makeCallGraph(options, null);
            pointerAnalysis = callGraphBuilder.getPointerAnalysis();
            Log.debug(TAG, "< Call graph created >");
        } catch (Exception e) {
            Log.err(TAG, e.getMessage());
        }

    }
}
