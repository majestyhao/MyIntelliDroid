package fu.hao.intellidroid.core;

import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.types.TypeReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Description: Add ability to attach listener to call graph builder to get information about heap stores and invoked nodes.
 * Common utilities for CFA-style call graph builders.
 * This abstract base class provides the general algorithm for a call graph builder that relies on propagation through an iterative
 * dataflow solver, and constraints generated by statements in SSA form.
 * Authors: Hao Fu(haofu@ucdavis.edu)
 * Date: 2016/8/29
 */
public class CallGraphInfoListener implements SSAPropagationCallGraphBuilder.BuilderListener {
    private IClassHierarchy classHierarchy;
    private Map<String, Set<CGNode>> targetMethodInvokes = new HashMap<String, Set<CGNode>>();
    private Map<PointerKey, Set<CGNode>> heapStores = new HashMap<PointerKey, Set<CGNode>>();
    private Map<String, Set<CGNode>> sharedPrefStores = new HashMap<String, Set<CGNode>>();
    private Map<String, CGNode> sharedPrefUIStores = new HashMap<String, CGNode>();
    private Map<TypeReference, Set<CGNode>> callbackRegistrations = new HashMap<TypeReference, Set<CGNode>>();


    public CallGraphInfoListener(IClassHierarchy cha) {
        classHierarchy = cha;
    }

    @Override
    public void onPut(CGNode cgNode, IField iField, PointerKey[] pointerKeys) {

    }

    @Override
    public void onInvoke(CGNode node, SSAAbstractInvokeInstruction invokeInstr) {

    }

    public void clear() {
        targetMethodInvokes.clear();
        heapStores.clear();
        sharedPrefStores.clear();
        sharedPrefUIStores.clear();
        callbackRegistrations.clear();
    }
}