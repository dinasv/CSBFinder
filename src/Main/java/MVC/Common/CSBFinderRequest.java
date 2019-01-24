package MVC.Common;

import Core.AlgorithmType;
import Core.ClusterBy;
import MVC.View.Requests.Request;

import java.util.LinkedList;
import java.util.List;

public class CSBFinderRequest implements Request{

    private String inputGenomeFilesPath = "";
    private int quorum = 1;
    private int numberOfInsertions = 0;
    private int quorumWithoutInsertions = 1;
    private int minimalCSBLength = 2;
    private int maximumCSBLength = Integer.MAX_VALUE;
    private String csbPatternFilePath = null;
    private String geneInfoFilePath = null;
    private boolean multCount = true;
    private boolean nonDirectons = false;
    private float familyClusterThreshold = 0.8f;
    private ClusterBy clusterType = ClusterBy.SCORE;
    private AlgorithmType algorithmType = AlgorithmType.SUFFIX_TREE;

    public int getQuorum() {
        return quorum;
    }

    public void setQuorum(int quorum) {
        this.quorum = quorum;
    }

    public int getNumberOfInsertions() {
        return numberOfInsertions;
    }

    public void setNumberOfInsertions(int numberOfInsertions) {
        this.numberOfInsertions = numberOfInsertions;
    }

    public int getQuorumWithoutInsertions() {
        return quorumWithoutInsertions;
    }

    public void setQuorumWithoutInsertions(int quorumWithoutInsertions) {
        this.quorumWithoutInsertions = quorumWithoutInsertions;
    }

    public int getMinimalCSBLength() {
        return minimalCSBLength;
    }

    public void setMinimalCSBLength(int minimalCSBLength) {
        this.minimalCSBLength = minimalCSBLength;
    }

    public int getMaximumCSBLength() {
        return maximumCSBLength;
    }

    public void setMaximumCSBLength(int maximumCSBLength) {
        this.maximumCSBLength = maximumCSBLength;
    }

    public String getCsbPatternFilePath() {
        return csbPatternFilePath;
    }

    public void setCsbPatternFilePath(String csbPatternFilePath) {
        this.csbPatternFilePath = csbPatternFilePath;
    }

    public String getGeneInfoFilePath() {
        return geneInfoFilePath;
    }

    public void setGeneInfoFilePath(String geneInfoFilePath) {
        this.geneInfoFilePath = geneInfoFilePath;
    }

    public boolean isMultCount() {
        return multCount;
    }

    public void setMultCount(boolean multCount) {
        this.multCount = multCount;
    }

    public boolean isNonDirectons() {
        return nonDirectons;
    }

    public void setNonDirectons(boolean nonDirectons) {
        this.nonDirectons = nonDirectons;
    }

    public float getFamilyClusterThreshold() {
        return familyClusterThreshold;
    }

    public void setFamilyClusterThreshold(float familyClusterThreshold) {
        this.familyClusterThreshold = familyClusterThreshold;
    }

    public ClusterBy getClusterType() {
        return clusterType;
    }

    public void setClusterType(ClusterBy clusterType) {
        this.clusterType = clusterType;
    }

    public void setAlgorithm(AlgorithmType algorithm) {
        this.algorithmType = algorithm;
    }

    public String getInputGenomeFilesPath() {
        return inputGenomeFilesPath;
    }

    public void setInputGenomeFilesPath(String inputGenomeFilesPath) {
        this.inputGenomeFilesPath = inputGenomeFilesPath;
    }

    public String[] toArgArray() {
        List<String> argList = new LinkedList<>();

        argList.add("-in");
        argList.add(inputGenomeFilesPath);

        argList.add("-q");
        argList.add(String.valueOf(quorum));

        argList.add("-ins");
        argList.add(String.valueOf(numberOfInsertions));

        argList.add("-qexact");
        argList.add(String.valueOf(quorumWithoutInsertions));

        argList.add("-lmin");
        argList.add(String.valueOf(minimalCSBLength));

        argList.add("-lmax");
        argList.add(String.valueOf(maximumCSBLength));

        if (csbPatternFilePath != null) {
            argList.add("-p");
            argList.add(csbPatternFilePath);
        }

        if (geneInfoFilePath != null) {
            argList.add("-cog-info");
            argList.add(geneInfoFilePath);
        }

        argList.add("-t");
        argList.add(String.valueOf(familyClusterThreshold));

        argList.add("-clust-by");
        argList.add(clusterType.toString());

        argList.add("-alg");
        argList.add(algorithmType.toString());

        if (nonDirectons) {
            argList.add("-non-directons");
        }

        return argList.toArray(new String[0]);
    }

}
