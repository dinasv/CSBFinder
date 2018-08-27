package CLI;
import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class CommandLineArgs {
    public enum OutputType {
        TXT,
        XLSX;
    }

    public enum ClusterBy {
        LENGTH,
        SCORE
    }

    public static class PositiveInteger implements IParameterValidator {
        public void validate(String name, String value) throws ParameterException {
            try {
                int n = Integer.parseInt(value);
                if (n < 0) {
                    throw new ParameterException("Parameter " + name + " should be positive (found " + value + ")");
                }
            }catch (NumberFormatException e){
                throw new ParameterException("Parameter " + name + " should be an integer (found " + value + ")");

            }
        }
    }

    public static class PositiveInteger2 implements IParameterValidator {
        public void validate(String name, String value) throws ParameterException {
            try {
                int n = Integer.parseInt(value);
                if (n < 2) {
                    throw new ParameterException("Parameter " + name + " value should be at least 2 (found " + value + ")");
                }
            }catch (NumberFormatException e){
                throw new ParameterException("Parameter " + name + " should be an integer (found " + value + ")");

            }
        }
    }

    @Parameter(names={"-in"}, description = "Input file name", required = true, order = 0)
    public static String input_file_name = "";

    @Parameter(names={"-q"}, description = "Instance quorum with insertions", required = true,
            validateWith = PositiveInteger.class, order = 1)
    public static int quorum2 = 1;

    @Parameter(names={"-qexact"}, description = "Instance quorum without insertions"
                , validateWith = CommandLineArgs.PositiveInteger.class, order = 3)
    public static int quorum1 = 1;

    @Parameter(names={"-ins"}, description = "Maximal number of insertions allowed"
            ,validateWith = CommandLineArgs.PositiveInteger.class, order = 2)
    public static int max_insertion = 0;

    @Parameter(names={"-lmin"}, description = "Minimal length of a CSB"
            , validateWith = CommandLineArgs.PositiveInteger2.class, order = 4)
    public static int min_pattern_length = 2;

    @Parameter(names={"-lmax"}, description = "Maximal length of a CSB"
            , validateWith = CommandLineArgs.PositiveInteger2.class, order = 5)
    public static int max_pattern_length = Integer.MAX_VALUE;

    @Parameter(names={"-mult-count"}, description = "Count multiple instances per input string, not just one",
            order = 9)
    public static boolean mult_count = false;

    @Parameter(names={"--datasetname", "-ds"}, description = "Dataset name", order = 6)
    public static String dataset_name = "dataset1";

    @Parameter(names={"--patterns", "-p"}, description = "Input patterns file name", order = 7)
    public static String input_patterns_file_name = null;

    @Parameter(names={"-cog-info"}, description = "Gene families info file name", order = 8)
    public static String cog_info_file_name = null;

    @Parameter(names={"--threshold", "-t"}, description = "Threshold for family clustering", order = 10)
    public static double threshold = 0.8;


    @Parameter(names={"-out"}, description = "Output file type", order = 11)
    public static OutputType output_file_type = OutputType.XLSX;

    @Parameter(names={"-clust-by"}, description = "Cluster CSBs to families by: 'score' or 'length'", order = 12)
    public static ClusterBy cluster_by = ClusterBy.SCORE;

    @Parameter(names={"-non-directons"}, description = "If true, segment input sequences directons", order = 13)
    public static boolean non_directons = false;

    @Parameter(names={"--mismatch", "-err"}, description = "Maximal number of mismatches allowed", hidden = true)
    public static int max_error = 0;
    @Parameter(names={"--deletion", "-del"}, description = "Maximal number of deletions allowed", hidden = true)
    public static int max_deletion = 0;
    @Parameter(names={"--wildcard", "-wc"}, description = "Maximal number of wildcards allowed", hidden = true)
    public static int max_wildcards = 0;

    //TODO: check how much memory consumption is improved
    @Parameter(names = "-mem", description = "Memory Saving Mode", hidden = true)
    public static boolean memory_saving_mode = false;


    @Parameter(names = "-debug", description = "Debug mode", hidden = true)
    public static boolean debug = false;

    @Parameter(names = {"-h","--help"}, description = "Show usage", help = true)
    public static boolean help;
}

