package fu.hao.intellidroid.core;

import com.ibm.wala.classLoader.JarFileModule;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
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
        Settings.setLogLevel(Log.MODE_VERBOSE);

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

        options.addOption(
                Option.builder("l").longOpt("lib")
                        .required(false).hasArg(true)
                        .desc("Android SDK android.jar location")
                        .build()
        );

        CommandLineParser commandLineParser = new DefaultParser();

        try {
            CommandLine commandLine = commandLineParser.parse(options, args, true);
            if (commandLine.hasOption("h")) {
                throw new java.text.ParseException("Print help", 0);
            }
            ;

            List<String> operands = commandLine.getArgList();
            if (operands.size() != 1) {
                Log.err(TAG, "Missing target apk directory: " + operands.size());
            }

            Settings.setAppDirectory(operands.get(0));
            Settings.setAppName(commandLine.getOptionValue("n", null));
            Settings.setAndroidLib(commandLine.getOptionValue("l", "./android/android-4.3/android.jar"));
            Settings.setOutputDirectory(commandLine.getOptionValue("o", Settings.getAppDirectory() + "/Output"));
            Log.bb(TAG, Settings.getAndroidLib());
            Log.bb(TAG, Settings.getOutputDirectory());

            // Clean output directory
            try {
                File outputDirFile = new File(Settings.getOutputDirectory());
                outputDirFile.mkdirs();

                FileUtils.cleanDirectory(outputDirFile);
            } catch (Exception e) {
                //Output.error(e.toString());
                e.printStackTrace();
            }

            Log.msg(TAG, "Starting IntelliDroidAppAnalysis for " + (Settings.getAppName() == null ? Settings.getAppDirectory() : Settings.getAppName()));
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

        String extractedApkPath = Settings.getAppDirectory(); // + "/apk";
        File extractedApkDir = new File(extractedApkPath);

        Log.bb(TAG, extractedApkDir);

        if (extractedApkDir.isDirectory()) {
            appPath = extractedApkPath + "/classes-dex2jar.jar";
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
        EntrypointAnalysis entrypointAnalysis = new EntrypointAnalysis(classHierarchy, manifestAnalysis, uiActivityMapping, callGraphInfoListener);

        Statistics.endCallGraph();

        CallGraph callGraph = entrypointAnalysis.getCallGraph();
        PointerAnalysis pointerAnalysis = entrypointAnalysis.getPointerAnalysis();



    }

}
