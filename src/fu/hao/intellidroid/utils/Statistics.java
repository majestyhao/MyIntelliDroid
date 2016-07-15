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

    public static void startAnalysis() {
        if (Settings.getGenerateStats()) {
            startTime = new Date();
        }
    }

}
