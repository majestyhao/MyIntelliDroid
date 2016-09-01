package fu.hao.intellidroid.utils;

import java.util.Date;

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

}
