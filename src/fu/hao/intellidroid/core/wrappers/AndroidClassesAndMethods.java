package fu.hao.intellidroid.core.wrappers;

import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;

import java.util.HashSet;
import java.util.Set;

/**
 * Description: Store method ref for official methods in Android
 * Authors: Hao Fu(haofu@ucdavis.edu)
 * Date: 2016/8/30
 */
public class AndroidClassesAndMethods {
    private static final Set<Selector> applicationLifecycleMethods;
    private static final Set<Selector> serviceLifecycleMethods;
    private static final Selector receiverLifecycleMethod;
    private static final Selector providerLifecycleMethod;
    private static final Set<Selector> fragmentLifecycleMethods;

    private static final TypeReference applicationClass;
    private static final TypeReference activityClass;
    private static final TypeReference serviceClass;
    private static final TypeReference receiverClass;
    private static final TypeReference providerClass;
    private static final TypeReference fragmentClass;

    static private MethodReference onClickMethod;

    static {
        applicationClass = TypeReference.findOrCreate(ClassLoaderReference.Extension, "Landroid/app/Application");
        activityClass = TypeReference.findOrCreate(ClassLoaderReference.Extension, "Landroid/app/Activity");
        serviceClass = TypeReference.findOrCreate(ClassLoaderReference.Extension, "Landroid/app/Service");
        receiverClass = TypeReference.findOrCreate(ClassLoaderReference.Extension, "Landroid/content/BroadcastReceiver");
        providerClass = TypeReference.findOrCreate(ClassLoaderReference.Extension, "Landroid/content/ContentProvider");
        fragmentClass = TypeReference.findOrCreate(ClassLoaderReference.Extension, "Landroid/app/Fragment");

        applicationLifecycleMethods = new HashSet<>();
        applicationLifecycleMethods.add(Selector.make("onCreate()V"));
        applicationLifecycleMethods.add(Selector.make("onTerminate()V"));
        applicationLifecycleMethods.add(Selector.make("onConfigurationChanged(Landroid/content/res/Configuration;)V"));
        applicationLifecycleMethods.add(Selector.make("onLowMemory()V"));
        applicationLifecycleMethods.add(Selector.make("onTrimMemory()V"));

        serviceLifecycleMethods = new HashSet<Selector>();
        serviceLifecycleMethods.add(Selector.make("onCreate()V"));
        serviceLifecycleMethods.add(Selector.make("onStart(Landroid/content/Intent;I)V"));
        serviceLifecycleMethods.add(Selector.make("onStartCommand(Landroid/content/Intent;II)I"));
        serviceLifecycleMethods.add(Selector.make("onDestroy()V"));

        TypeReference onClickListenerClass = TypeReference.findOrCreate(ClassLoaderReference.Extension, "Landroid/view/View$OnClickListener");
        onClickMethod = MethodReference.findOrCreate(onClickListenerClass, Selector.make("onClick(Landroid/view/View;)V"));

        receiverLifecycleMethod = Selector.make("onReceive(Landroid/content/Context;Landroid/content/Intent;)V");

        providerLifecycleMethod = Selector.make("onCreate()Z");

        fragmentLifecycleMethods = new HashSet<Selector>();
        fragmentLifecycleMethods.add(Selector.make("onAttach(Landroid/app/Activity;)V"));
        fragmentLifecycleMethods.add(Selector.make("onCreate(Landroid/os/Bundle;)V"));
        fragmentLifecycleMethods.add(Selector.make("onCreateView(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;"));
        fragmentLifecycleMethods.add(Selector.make("onActivityCreated(Landroid/os/Bundle;)V"));
        fragmentLifecycleMethods.add(Selector.make("onViewStateRestored(Landroid/os/Bundle;)V"));
        fragmentLifecycleMethods.add(Selector.make("onStart()V"));
        fragmentLifecycleMethods.add(Selector.make("onResume()V"));
        fragmentLifecycleMethods.add(Selector.make("onPause()V"));
        fragmentLifecycleMethods.add(Selector.make("onStop()V"));
        fragmentLifecycleMethods.add(Selector.make("onDestroyView()V"));
        fragmentLifecycleMethods.add(Selector.make("onDestroy()V"));
        fragmentLifecycleMethods.add(Selector.make("onDetach()V"));
    }

    public static TypeReference getApplicationClass() {
        return applicationClass;
    }

    public static TypeReference getActivityClass() {
        return activityClass;
    }

    public static TypeReference getServiceClass() {
        return serviceClass;
    }

    public static TypeReference getReceiverClass() {
        return receiverClass;
    }

    public static TypeReference getProviderClass() {
        return providerClass;
    }

    public static TypeReference getFragmentClass() {
        return fragmentClass;
    }

    public static Set<Selector> getAppLifecycleMethods() {
        return applicationLifecycleMethods;
    }

    public static Set<Selector> getServiceLifecycleMethods() {
        return serviceLifecycleMethods;
    }

    public static MethodReference getOnClickMethod() {
        return onClickMethod;
    }

    public static Selector getReceiverLifecycleMethod() {
        return receiverLifecycleMethod;
    }

    public static Selector getProviderLifecycleMethod() {
        return providerLifecycleMethod;
    }

    public static Set<Selector> getFragmentLifecycleMethods() {
        return fragmentLifecycleMethods;
    }


}
