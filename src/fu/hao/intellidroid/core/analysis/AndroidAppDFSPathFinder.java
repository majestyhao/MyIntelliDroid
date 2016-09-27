package fu.hao.intellidroid.core.analysis;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.traverse.DFSPathFinder;

/**
 * Description: Perform dfs for node that matches certain criteria
 * If found, it reports a path to the first node found.
 * Authors: Hao Fu(haofu@ucdavis.edu)
 * Date: 2016/9/2
 */
public class AndroidAppDFSPathFinder extends DFSPathFinder<CGNode> {
    public AndroidAppDFSPathFinder(Graph<CGNode> graph, CGNode node, Filter<CGNode> filter) {
        super(graph, node, filter);
    }

}
