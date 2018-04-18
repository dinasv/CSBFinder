package Main;
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

    public class PositiveInteger implements IParameterValidator {
        public void validate(String name, String value) throws ParameterException {
            int n = Integer.parseInt(value);
            if (n < 0) {
                throw new ParameterException("Parameter " + name + " should be positive (found " + value +")");
            }
        }
    }

    public class PositiveInteger2 implements IParameterValidator {
        public void validate(String name, String value) throws ParameterException {
            int n = Integer.parseInt(value);
            if (n < 2) {
                throw new ParameterException("Parameter " + name + " value should be at least 2 (found " + value +")");
            }
        }
    }

    @Parameter(names={"-in"}, description = "Input file name", required = true)
    public static String input_file_name = "";

    @Parameter(names={"-q"}, description = "Instance quorum with insertions", required = true,
            validateWith = PositiveInteger.class)
    public static int quorum2;

    @Parameter(names={"-qexact"}, description = "Instance quorum without insertions",
            validateWith = PositiveInteger.class)
    public static int quorum1 = 1;

    @Parameter(names={"-ins"}, description = "Maximal number of insertions allowed",
            validateWith = PositiveInteger.class)
    public static int max_insertion = 0;

    @Parameter(names={"-l"}, description = "Minimal cluster length", validateWith = PositiveInteger2.class)
    public static int min_pattern_length = -1;

    @Parameter(names={"-lmax"}, description = "Maximal cluster length", validateWith = PositiveInteger2.class)
    public static int max_pattern_length = Integer.MAX_VALUE;

    @Parameter(names={"-bcount"}, description = "If true, count one instance per input string",
            arity = 1)
    public static boolean bool_count = true;

    @Parameter(names={"--datasetname", "-ds"}, description = "Dataset name")
    public static String dataset_name = "dataset1";

    @Parameter(names={"--patterns", "-p"}, description = "Input patterns file name")
    public static String input_patterns_file_name = null;

    @Parameter(names={"--cog-info"}, description = "gene families info file name")
    public static String cog_info_file_name = null;

    @Parameter(names={"--threshold", "-t"}, description = "Threshold for family clustering")
    public static double threshold = 0.8;

    @Parameter(names = "-mem", description = "Memory Saving Mode")
    public static boolean memory_saving_mode = false;

    @Parameter(names={"-out"}, description = "Output file type")
    public static OutputType output_file_type = OutputType.XLSX;

    @Parameter(names={"-clust-by"}, description = "Cluster by: 'score' or 'length'. Default 'length'")
    public static ClusterBy cluster_by = ClusterBy.SCORE;


    @Parameter(names={"--mismatch", "-err"}, description = "Maximal number of mismatches allowed", hidden = true)
    public static int max_error = 0;
    @Parameter(names={"--deletion", "-del"}, description = "Maximal number of deletions allowed", hidden = true)
    public static int max_deletion = 0;
    @Parameter(names={"--wildcard", "-wc"}, description = "Maximal number of wildcards allowed", hidden = true)
    public static int max_wildcards = 0;


    @Parameter(names = "-debug", description = "Debug mode", hidden = true)
    public static boolean debug = false;



    //@Parameter(names = "--help", help = true)
    //public static boolean help;
}

