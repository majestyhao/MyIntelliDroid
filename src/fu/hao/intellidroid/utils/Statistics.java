package fu.hao.intellidroid.utils;

import fu.hao.intellidroid.core.IntelliDroidAppAnalysis;

import java.util.Date;

/**
 * Created by majes on 7/11/2016.
 */
public class Statistics {
    private static Date startTime = null;

    public static void startAnalysis() {
        if (IntelliDroidAppAnalysis.Config.GenerateStats) {
            startTime = new Date();
        }
    }

}
