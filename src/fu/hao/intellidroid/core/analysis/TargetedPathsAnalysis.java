package fu.hao.intellidroid.core.analysis;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.graph.traverse.DFSPathFinder;
import fu.hao.intellidroid.core.wrappers.CallGraphInfoListener;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import fu.hao.intellidroid.core.wrappers.CallPath;

import java.util.*;


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

    private Set<CallPath> findCallPathsToTargets(final CGNode rootNode) {
        Set<CallPath> callPaths = new HashSet<>();
        final IClassHierarchy classHierarchy = callGraph.getClassHierarchy();
        //final HeapModel heapModel = pointerAnalysis.getHeapModel();

        for (String targetMethod : callGraphInfoListener.getTargetMethods()) {
            final Set<CGNode> targetNodes = callGraphInfoListener.getTargetNodes(targetMethod);
            Filter<CGNode> targetMethodFilter = new Filter<CGNode>() {
                @Override
                public boolean accepts(CGNode cgNode) {
                    if (targetNodes.contains(cgNode)) {
                        return true;
                    }

                    return false;
                }

            };

            DFSPathFinder<CGNode> pathFinder = new AndroidAppDFSPathFinder(callGraph, rootNode, targetMethodFilter);

            for (CGNode pathNode : pathFinder) {
                // The first path found from a root to a node accepted by the filter.
                List<CGNode> callPath = pathFinder.find();
                if (callPath == null) {
                    continue;
                }

                CGNode pathEndNode = callPath.get(0);
                Collections.reverse(callPath);

                SSAInstruction[] instructions = pathEndNode.getIR().getInstructions();
                

            }
        }

    }

    private Map<Integer, JsonObject> analyzePathsFromEntrypoint(IMethod entrypoint) {
        Map<Integer, JsonObject> entrypointPathsJsonMap = new HashMap<>();
        Set<CGNode> entrypointNodes = callGraph.getNodes(entrypoint.getReference());

        for (CGNode entrypointNode : entrypointNodes) {


            break;
        }

        return entrypointPathsJsonMap;
    }

    public void analyze() {
        JsonObject targetedPathsJson = new JsonObject();

        Collection<IMethod> entrypoints = entrypointAnalysis.getTrueEntrypoints();

        for (IMethod entrypoint : entrypoints) {

        }
    }

}
