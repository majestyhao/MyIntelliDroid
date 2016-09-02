package fu.hao.intellidroid.core.analysis;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import fu.hao.intellidroid.core.wrappers.CallGraphInfoListener;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import java.util.Collection;


/**
 * Description:
 * Authors: Hao Fu(haofu@ucdavis.edu)
 * Date: 2016/9/1
 */
public class TargetedPathsAnalysis {
    private final String TAG = this.getClass().getSimpleName();

    private EntrypointAnalysis entrypointAnalysis;
    private CallGraph callGraph;
    private PointerAnalysis pointerAnalysis;
    private ManifestAnalysis manifestAnalysis;
    private UIActivityMapping uiActivityMapping;
    private CallGraphInfoListener callGraphInfoListener;

    public TargetedPathsAnalysis(EntrypointAnalysis entrypointAnalysis, ManifestAnalysis manifestAnalysis, UIActivityMapping uiActivityMapping, CallGraphInfoListener callGraphInfoListener) {
        this.entrypointAnalysis = entrypointAnalysis;
        this.manifestAnalysis = manifestAnalysis;
        this.pointerAnalysis = entrypointAnalysis.getPointerAnalysis();
        this.callGraph = entrypointAnalysis.getCallGraph();
        this.callGraphInfoListener = callGraphInfoListener;
        this.uiActivityMapping = uiActivityMapping;
    }

    public void analyze() {
        JsonObject targetedPathsJson = new JsonObject();

        Collection<IMethod> entrypoints = entrypointAnalysis.getTrueEntrypoints();
    }

}
