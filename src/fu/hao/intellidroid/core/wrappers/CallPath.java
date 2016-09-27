package fu.hao.intellidroid.core.wrappers;

import com.ibm.wala.classLoader.ProgramCounter;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Description: A path in the call graph
 * Authors: Hao Fu(haofu@ucdavis.edu)
 * Date: 2016/9/1
 */
public class CallPath {
    private final List<CGNode> callPath;
    private final ProgramCounter target; // an instruction
    private final int targetIndex;
    private final CallGraph callGraph;
    private final PointerAnalysis pointerAnalysis;
    private final Set<CGNode> callNodes;

    public CallPath(List<CGNode> callPath, ProgramCounter target, int targetIndex, CallGraph callGraph, PointerAnalysis pointerAnalysis) {
        this.callPath = callPath;
        this.target = target;
        this.targetIndex = targetIndex;
        this.callGraph = callGraph;
        this.pointerAnalysis = pointerAnalysis;

        callNodes = new HashSet<>();
        callNodes.addAll(callPath);
    }

    public CallGraph getCallGraph() {
        return callGraph;
    }

    public List<CGNode> getPath() {
        return callPath;
    }

    public int getTargetIndex() {
        return targetIndex;
    }
}
