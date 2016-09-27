package fu.hao.intellidroid.core.analysis;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.graph.traverse.DFSPathFinder;
import fu.hao.intellidroid.core.wrappers.CallGraphInfoListener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import fu.hao.intellidroid.core.wrappers.CallPath;
import fu.hao.intellidroid.utils.Statistics;

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

            //for (CGNode pathNode : pathFinder) {
            // Iteracte over the accepted cg nodes
            while (pathFinder.hasNext()) {
                // The first path found from a root to a node accepted by the filter.
                List<CGNode> callPath = pathFinder.find();

                if (callPath == null) {
                    continue;
                }

                CGNode pathEndNode = callPath.get(0);
                Collections.reverse(callPath);

                SSAInstruction[] instructions = pathEndNode.getIR().getInstructions();
                // Iterate over the instructions and locate the target invocations
                for (int instrIndex = 0; instrIndex < instructions.length; instrIndex++) {
                    SSAInstruction instruction = instructions[instrIndex];
                    if (instruction == null) {
                        continue;
                    }

                    // If it is not an invokation instruction
                    if (!(instruction instanceof SSAAbstractInvokeInstruction)) {
                        continue;
                    }

                    SSAAbstractInvokeInstruction invokeInstruction = (SSAAbstractInvokeInstruction) instruction;
                    CallSiteReference callsite = invokeInstruction.getCallSite();
                    IMethod invokedMethod = classHierarchy.resolveMethod(callsite.getDeclaredTarget());

                    if (invokedMethod != null
                            && targetMethod.equals(invokedMethod.getSignature())) {
                        Statistics.trackPath(callPath, invokedMethod);

                        CallPath newPath = new CallPath(callPath, callsite, instrIndex, callGraph, pointerAnalysis);
                        callPaths.add(newPath);
                    }
                }

            }
        }

        return callPaths;
    }

    private Map<Integer, JsonObject> analyzePathsFromEntrypoint(IMethod entrypoint) {
        Map<Integer, JsonObject> entrypointPathsJsonMap = new HashMap<>();
        Set<CGNode> entrypointNodes = callGraph.getNodes(entrypoint.getReference());


        for (CGNode entrypointNode : entrypointNodes) {
            Set<CallPath> callPaths = findCallPathsToTargets(entrypointNode);

            int callPathId = 0;
            for (CallPath callPath : callPaths) {
                JsonObject targetedPath = analyzeTargetedPath(callPath, callPathId);

            }

            break;
        }

        return entrypointPathsJsonMap;
    }

    /* Extract constraints thau govrtn path's execution     */
    private JsonObject analyzeTargetedPath(CallPath callPath, int callPathId) {


        return null;

    }

    public void analyze() {
        JsonObject targetedPathsJson = new JsonObject();

        Collection<IMethod> entrypoints = entrypointAnalysis.getTrueEntrypoints();

        for (IMethod entrypoint : entrypoints) {

        }
    }

}
