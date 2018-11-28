package Core;
import Core.Genomes.Pattern;
import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import java.util.Comparator;

public class Parameters {

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

    @Parameter(names={"-in"}, description = "Input file relative or absolute path", required = true, order = 0)
    public String inputFilePath = "";

    @Parameter(names={"-q"}, description = "Instance quorum with insertions", required = true,
            validateWith = PositiveInteger.class, order = 1)
    public int quorum2 = 1;

    @Parameter(names={"-qexact"}, description = "Instance quorum without insertions"
                , validateWith = Parameters.PositiveInteger.class, order = 3)
    public int quorum1 = 1;

    @Parameter(names={"-ins"}, description = "Maximal number of insertions allowed"
            ,validateWith = Parameters.PositiveInteger.class, order = 2)
    public int maxInsertion = 0;

    @Parameter(names={"-lmin"}, description = "Minimal length of a CSB"
            , validateWith = Parameters.PositiveInteger2.class, order = 4)
    public int minPatternLength = 2;

    @Parameter(names={"-lmax"}, description = "Maximal length of a CSB"
            , validateWith = Parameters.PositiveInteger2.class, order = 5)
    public int maxPatternLength = Integer.MAX_VALUE;

    @Parameter(names={"-mult-count"}, description = "Count multiple instances per input string, not just one",
            order = 9)
    public boolean multCount = false;

    @Parameter(names={"--datasetname", "-ds"}, description = "Dataset name", order = 6)
    public String datasetName = "dataset1";

    @Parameter(names={"--patterns", "-p"}, description = "Input patterns file relative or absolute path", order = 7)
    public String inputPatternsFilePath = null;

    @Parameter(names={"-cog-info"}, description = "Gene families info file relative or absolute path", order = 8)
    public String cogInfoFilePath = null;

    @Parameter(names={"--threshold", "-t"}, description = "Threshold for family clustering", order = 10)
    public double threshold = 0.8;

    @Parameter(names={"-out"}, description = "Output file type", order = 11)
    public OutputType outputFileType = OutputType.XLSX;

    @Parameter(names={"-clust-by"}, description = "Cluster CSBs to families by: 'score' or 'length'", order = 12)
    public ClusterBy clusterBy = ClusterBy.SCORE;

    @Parameter(names={"-non-directons"}, description = "If true, segment input sequences directons", order = 13)
    public boolean nonDirectons = false;

    @Parameter(names={"-out-dir"}, description = "Path to output directory", order = 14)
    public String outputDir = "output";

    @Parameter(names={"--mismatch", "-err"}, description = "Maximal number of mismatches allowed", hidden = true)
    public int maxError = 0;
    @Parameter(names={"--deletion", "-del"}, description = "Maximal number of deletions allowed", hidden = true)
    public int maxDeletion = 0;
    @Parameter(names={"--wildcard", "-wc"}, description = "Maximal number of wildcards allowed", hidden = true)
    public int maxWildcards = 0;

    @Parameter(names = "-debug", description = "Debug mode", hidden = true)
    public boolean debug = false;

    @Parameter(names = {"-h","--help"}, description = "Show usage", help = true)
    public boolean help;
}

