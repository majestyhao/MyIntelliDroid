package fu.hao.intellidroid.core;

import com.ibm.wala.classLoader.JarFileModule;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.AnalysisScopeReader;
import fu.hao.intellidroid.core.wrappers.UIActivityMapping;
import fu.hao.intellidroid.utils.Log;
import fu.hao.intellidroid.utils.Settings;
import fu.hao.intellidroid.utils.Statistics;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;
import java.util.jar.JarFile;

/**
 * Class: IntelliDroidAppAnalysis
 * Description: The main class for analysis.
 * Authors: Hao Fu(haofu@ucdavis.edu)
 * Date: 7/14/2016 6:00 PM
 */
public class IntelliDroidAppAnalysis {

    private final static String TAG = IntelliDroidAppAnalysis.class.getSimpleName();

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption(
                Option.builder("h").longOpt("help")
                        .required(false).hasArg(false)
                        .desc("Print help")
                        .build()
        );

        options.addOption(
                Option.builder("o").longOpt("output")
                        .required(false).hasArg(true)
                        .desc("Output directory for extracted paths and constraints (default: \"./pathOutput\")")
                        .build()
        );

        CommandLineParser commandLineParser = new DefaultParser();

        try {
            CommandLine commandLine = commandLineParser.parse(options, args, true);
            if (commandLine.hasOption("h")) {
                throw new java.text.ParseException("Print help", 0);
            };

            List<String> operands = commandLine.getArgList();
            if (operands.size() != 1) {
                throw new ParseException("Missing target apk directory");
            }

            Settings.setAppDirectory(operands.get(0));
            Settings.setAppName(commandLine.getOptionValue("n", null));
            Settings.setOutputDirectory(commandLine.getOptionValue("o", "./pathOutput"));

            // Clean output directory
            try {
                File outputDirFile = new File(Settings.getOutputDirectory());
                outputDirFile.mkdirs();

                FileUtils.cleanDirectory(outputDirFile);
            } catch (Exception e) {
                //Output.error(e.toString());
                e.printStackTrace();
            }

            Log.msg(TAG, "Starting fu.hao.intellidroid.IntelliDroidAppAnalysis for " + (Settings.getAppName() == null ? Settings.getAppDirectory(): Settings.getAppName()));
            IntelliDroidAppAnalysis analysis = new IntelliDroidAppAnalysis();
            analysis.analyze();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void analyze() throws Exception {
        Statistics.startAnalysis();

        String appPath = null;
        String manifestPath = null;

        String extractedApkPath = Settings.getAppDirectory() + "/apk";
        File extractedApkDir = new File(extractedApkPath);

        if (extractedApkDir.isDirectory()) {
            appPath = extractedApkPath + "/classes.jar";
            manifestPath = extractedApkPath + "/AndroidManifest.xml";
        } else {
            Log.err(TAG, "Missing AndroidManifest.xml and/or classes.jar files in target APK directory.");
        }

        // Extract info from AndroidManifest.xml
        ManifestAnalysis manifestAnalysis = new ManifestAnalysis(manifestPath);
        Log.msg(TAG, manifestAnalysis.getPackageName());

        // Represents a set of files to be analyzed: to construct from classpath:
        AnalysisScope analysisScope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(appPath, null);
        Module androidMod = new JarFileModule(new JarFile(Settings.getAndroidLib()));
        analysisScope.addToScope(ClassLoaderReference.Extension, androidMod);

        // Get the class hierarchies for name resolution, etc
        IClassHierarchy classHierarchy = ClassHierarchy.make(analysisScope);

        /* Search for entry points and build the call graph */
        Statistics.startCallGraph();

        CallGraphInfoListener callGraphInfoListener = new CallGraphInfoListener(classHierarchy);
        UIActivityMapping uiActivityMapping = new UIActivityMapping(classHierarchy);

        // Look for the entry points and generate the call graph


    }

}
