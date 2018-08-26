package MVC.Common;

import java.util.LinkedList;
import java.util.List;

public class CSBFinderRequest {

    private int minimalInputSequesnces  = 1;
    private int numberOfInsertions = 0;
    private int quorumWithoutInsertions = 1;
    private int minimalCSBLength = 2;
    private int maximumCSBLength = Integer.MAX_VALUE;
    private String datasetName = "dataset1";
    private String csbPatternFilePath = null;
    private String geneInfoFilePath = null;
    private boolean mult_count = true;
    private boolean isDirectons = false;
    private float familyClusterThreshold = 0.8f;
    private String clusterType = "score";

    public int getMinimalInputSequesnces() {
        return minimalInputSequesnces;
    }

    public void setMinimalInputSequesnces(int minimalInputSequesnces) {
        this.minimalInputSequesnces = minimalInputSequesnces;
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

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
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
        return mult_count;
    }

    public void setMultCount(boolean multCount) {
        this.mult_count= multCount;
    }

    public boolean isDirectons() {
        return isDirectons;
    }

    public void setDirectons(boolean directons) {
        isDirectons = directons;
    }

    public float getFamilyClusterThreshold() {
        return familyClusterThreshold;
    }

    public void setFamilyClusterThreshold(float familyClusterThreshold) {
        this.familyClusterThreshold = familyClusterThreshold;
    }

    public String getClusterType() {
        return clusterType;
    }

    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
    }

    public String[] toArgArray() {
        List<String> argList = new LinkedList<>();


        argList.add("-in");
        argList.add("");

        argList.add("-q");
        argList.add(String.valueOf(minimalInputSequesnces));

        argList.add("-ins");
        argList.add(String.valueOf(numberOfInsertions));

        argList.add("-qexact");
        argList.add(String.valueOf(quorumWithoutInsertions));

        argList.add("-lmin");
        argList.add(String.valueOf(minimalCSBLength));

        argList.add("-lmax");
        argList.add(String.valueOf(maximumCSBLength));

//        argList.add("-mult-count");
//        argList.add(String.valueOf(mult_count));

        argList.add("-ds");
        argList.add(datasetName);

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
        argList.add(clusterType);

//        argList.add("-directon");
//        argList.add(String.valueOf(isDirectons));

        return argList.toArray(new String[0]);
    }

}
