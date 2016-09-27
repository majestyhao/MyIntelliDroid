package fu.hao.intellidroid.utils;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.util.collections.Pair;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class: Statistics
 * Description:
 * Authors: Hao Fu(haofu@ucdavis.edu)
 * Date: 7/14/2016 6:00 PM
 */
public class Statistics {
    private static Date startTime = null;
    private static Date callGraphStartTime = null;
    private static Date callGraphEndTime = null;
    private static Date constraintStartTime = null;
    private static Date constraintEndTime = null;

    private static long numberOfNodes = 0;
    private static long numberOfEdges = 0;

    public static void startAnalysis() {
        if (Settings.getGenerateStats()) {
            startTime = new Date();
        }
    }

    public static void startCallGraph() {
        if (Settings.getGenerateStats()) {
            callGraphStartTime = new Date();
        }
    }

    public static void endCallGraph() {
        if (Settings.getGenerateStats()) {
            callGraphEndTime = new Date();
        }
    }

    public static void setNumberOfNodes(long numberOfNodes) {
        Statistics.numberOfNodes = numberOfNodes;
    }

    public static void setNumberOfEdges(long numberOfEdges) {
        Statistics.numberOfEdges = numberOfEdges;
    }

    public static void startConstraintAnalysis() {
        if (Settings.getGenerateStats()) {
            constraintStartTime = new Date();
        }
    }

    public static void endConstraintAnalysis() {
        if (Settings.getGenerateStats()) {
            constraintEndTime = new Date();
        }
    }

    private static Set<CGNode> pathNodes;
    private static Set<Pair<IMethod, IMethod>> pathEdges;
    public static void trackPath(List<CGNode> path, IMethod targetMethod) {
        if (Settings.getGenerateStats()) {
            pathNodes = new HashSet<>();
            pathEdges = new HashSet<>();

            pathNodes.addAll(path);
            for (int i = 0; i < path.size() - 1; i++) {
                pathEdges.add(Pair.make(path.get(i).getMethod(), path.get(i + 1).getMethod()));
            }

            if (targetMethod != null) {
                pathEdges.add(Pair.make(path.get(path.size() - 1).getMethod(), targetMethod));
            }

        }
    }

}
