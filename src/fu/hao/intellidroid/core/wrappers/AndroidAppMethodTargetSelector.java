package fu.hao.intellidroid.core.wrappers;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.MethodTargetSelector;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;
import fu.hao.intellidroid.core.ManifestAnalysis;

/**
 * Description: represents policies for selecting a method to call at a given invocation site.
 * Authors: Hao Fu(haofu@ucdavis.edu)
 * Date: 2016/8/30
 */
public class AndroidAppMethodTargetSelector implements MethodTargetSelector {
    private ManifestAnalysis manifestAnalysis;
    private MethodTargetSelector childSelector;
    private IClassHierarchy classHierarchy;

    private IClass handlerClass;
    private IClass contextClass;
    private IClass serviceClass;
    private IClass intentServiceClass;
    private IClass threadClass;
    private IClass runnableClass;
    private IClass asyncTaskClass;
    private IClass timerClass;
    private IClass methodClass;
    private IClass executorServiceClass;
    private IClass dialogFragmentClass;

    public AndroidAppMethodTargetSelector(MethodTargetSelector childSelector, IClassHierarchy classHierarchy, ManifestAnalysis manifestAnalysis) {
        this.childSelector = childSelector;
        this.classHierarchy = classHierarchy;
        this.manifestAnalysis = manifestAnalysis;

        handlerClass = classHierarchy.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Extension, "Landroid/os/Handler"));
        contextClass = classHierarchy.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Extension, "Landroid/content/Context"));
        serviceClass = classHierarchy.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Extension, "Landroid/app/Service"));
        intentServiceClass = classHierarchy.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Extension, "Landroid/app/IntentService"));
        threadClass = classHierarchy.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Extension, "Ljava/lang/Thread"));
        runnableClass = classHierarchy.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Extension, "Ljava/lang/Runnable"));
        asyncTaskClass = classHierarchy.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Extension, "Landroid/os/AsyncTask"));
        timerClass = classHierarchy.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Extension, "Ljava/util/Timer"));
        methodClass = classHierarchy.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Extension, "Ljava/lang/reflect/Method"));
        executorServiceClass = classHierarchy.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Extension, "Ljava/util/concurrent/ExecutorService"));
        dialogFragmentClass = classHierarchy.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Extension, "Landroid/app/DialogFragment"));
        //preferenceActivityClass = cha.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Extension, "Landroid/preference/PreferenceActivity"));
        //broadcastReceiverClass = cha.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Extension, "Landroid/content/BroadcastReceiver"));
    }

    @Override
    public IMethod getCalleeTarget(CGNode caller, CallSiteReference site, IClass receiver) {
        return null;
    }
}
