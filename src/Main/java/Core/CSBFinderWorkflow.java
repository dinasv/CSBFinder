package Core;

import Core.PostProcess.Family;
import Core.PostProcess.FamilyClustering;
import Core.SuffixTrees.DatasetTreeBuilder;
import Core.SuffixTrees.GeneralizedSuffixTree;
import Core.SuffixTrees.TreeType;
import Core.SuffixTrees.Trie;
import Genomes.GenomesInfo;
import Genomes.Pattern;
import Genomes.PatternScore;

import java.util.List;

/**
 */
public class CSBFinderWorkflow {

    private GeneralizedSuffixTree datasetSuffixTree;
    private Trie patternTree;

    private Parameters params;
    private GenomesInfo gi;
    private int patternsCount;

    public CSBFinderWorkflow(GenomesInfo gi){

        datasetSuffixTree = null;

        this.params = null;
        this.gi = gi;

        patternsCount = 0;

        buildDatasetSuffixTree();
    }

    public List<Family> run(Parameters params, List<Pattern> patternsFromFile){
        this.params = params;

        buildPatternsTree(patternsFromFile);
        List<Pattern> patterns = findPatterns(false);
        computeScores(patterns);
        List<Family> families = clusterToFamilies(patterns);

        return families;
    }

    public List<Family> run(Parameters params){
        this.params = params;

        List<Pattern> patterns = findPatterns(true);
        patternsCount = patterns.size();
        computeScores(patterns);
        List<Family> families = clusterToFamilies(patterns);

        return families;
    }

    private List<Pattern> findPatterns(boolean removeredundantPatterns){

        MainAlgorithm MainAlgorithm = new MainAlgorithm(params, datasetSuffixTree, patternTree, gi);
        if (removeredundantPatterns) {
            MainAlgorithm.removeRedundantPatterns();
        }

        return MainAlgorithm.getPatterns();

    }

    private void buildDatasetSuffixTree(){
        datasetSuffixTree = new GeneralizedSuffixTree();
        DatasetTreeBuilder.buildTree(datasetSuffixTree, false, gi);
    }

    private void buildPatternsTree(List<Pattern> patternsFromFile){
        if (patternsFromFile.size() > 0){
            patternTree = new Trie(TreeType.STATIC);
            DatasetTreeBuilder.buildPatternsTree(patternsFromFile, patternTree, gi);
        }
    }

    private void computeScores(List<Pattern> patterns){

        PatternScore pattern_score = new PatternScore(gi.getMaxGenomeSize(), gi.getNumberOfGenomes(), gi.getDatasetLengthSum(),
                gi.cogToContainingGenomes, gi.genomeToCogParalogCount);

        for (Pattern pattern : patterns) {
            double score = PatternScore.computePatternScore(pattern_score, pattern.getPatternArr(), params.maxInsertion, params.maxError,
                    params.maxDeletion, pattern.getInstanceCount());
            pattern.setScore(score);
        }
    }

    private List<Family> clusterToFamilies(List<Pattern> patterns){
        List<Family> families = FamilyClustering.Cluster(patterns, params.threshold, params.clusterBy, gi,
                params.nonDirectons);
        return families;
    }

    public int getPatternsCount() {
        return patternsCount;
    }
}
