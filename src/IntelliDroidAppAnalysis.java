import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by majes on 7/11/2016.
 */
public class IntelliDroidAppAnalysis {

    public static class Configuration {
        static public Set<String> TargetMethods = new HashSet<String>();

        static public String AppDirectory = null;
        static public String AppName = null;
        static public String OutputDirectory = null;

        static public boolean PrintOutput = true;
        static public boolean PrintConstraints = false;
        static public boolean GenerateStats = false;
    }

    public static Configuration Config = new Configuration();

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

            Config.AppDirectory = operands.get(0);
            Config.AppName = commandLine.getOptionValue("n", null);
            Config.OutputDirectory = commandLine.getOptionValue("o", "./pathOutput");

            // Clean output directory
            try {
                File outputDirFile = new File(Config.OutputDirectory);
                outputDirFile.mkdirs();

                FileUtils.cleanDirectory(outputDirFile);
            } catch (Exception e) {
                //Output.error(e.toString());
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
