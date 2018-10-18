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

    private GeneralizedSuffixTree dataset_suffix_tree;
    private Trie pattern_tree;

    private Parameters params;
    private GenomesInfo gi;
    private int patterns_count;

    public CSBFinderWorkflow(GenomesInfo gi){

        dataset_suffix_tree = null;

        this.params = null;
        this.gi = gi;

        patterns_count = 0;

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
        patterns_count = patterns.size();
        computeScores(patterns);
        List<Family> families = clusterToFamilies(patterns);

        return families;
    }

    private List<Pattern> findPatterns(boolean removeredundantPatterns){

        MainAlgorithm MainAlgorithm = new MainAlgorithm(params, dataset_suffix_tree, pattern_tree, gi);
        if (removeredundantPatterns) {
            MainAlgorithm.removeRedundantPatterns();
        }

        return MainAlgorithm.getPatterns();

    }

    private void buildDatasetSuffixTree(){
        dataset_suffix_tree = new GeneralizedSuffixTree();
        DatasetTreeBuilder.buildTree(dataset_suffix_tree, false, gi);
    }

    private void buildPatternsTree(List<Pattern> patternsFromFile){
        if (patternsFromFile.size() > 0){
            pattern_tree = new Trie(TreeType.STATIC);
            DatasetTreeBuilder.buildPatternsTree(patternsFromFile, pattern_tree, gi);
        }
    }

    private void computeScores(List<Pattern> patterns){

        PatternScore pattern_score = new PatternScore(gi.getMaxGenomeSize(), gi.getNumberOfGenomes(), gi.getDatasetLengthSum(),
                gi.cog_to_containing_genomes, gi.genome_to_cog_paralog_count);

        for (Pattern pattern : patterns) {
            double score = PatternScore.computePatternScore(pattern_score, pattern.getPatternArr(), params.max_insertion, params.max_error,
                    params.max_deletion, pattern.getInstanceCount());
            pattern.setScore(score);
        }
    }

    private List<Family> clusterToFamilies(List<Pattern> patterns){
        List<Family> families = FamilyClustering.Cluster(patterns, params.threshold, params.cluster_by, gi,
                params.non_directons);
        return families;
    }

    public int getPatternsCount() {
        return patterns_count;
    }
}
