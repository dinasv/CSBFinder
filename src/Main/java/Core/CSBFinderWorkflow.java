package Core;

import Core.Genomes.Gene;
import Core.PostProcess.Family;
import Core.PostProcess.FamilyClustering;
import Core.SuffixTrees.DatasetTree;
import Core.SuffixTrees.TreeType;
import Core.SuffixTrees.Trie;
import Core.Genomes.GenomesInfo;
import Core.Genomes.Pattern;
import Core.Genomes.PatternScore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 */
public class CSBFinderWorkflow {

    private DatasetTree datasetTree;
    private Trie patternTree;

    private Parameters params;
    private GenomesInfo gi;
    private int patternsCount;

    public CSBFinderWorkflow(GenomesInfo gi){

        this.params = null;
        this.gi = gi;

        patternsCount = 0;

        datasetTree = null;

        //new DatasetTree(nonDirectons, gi);
    }

    public List<Family> run(Parameters params, List<Pattern> patternsFromFile){
        this.params = params;

        buildPatternsTree(patternsFromFile);

        MainAlgorithm mainAlgorithm = executeMainAlgorithm();
        if (mainAlgorithm == null){
            return new ArrayList<>();
        }

        List<Pattern> patterns = mainAlgorithm.getPatterns();

        List<Family> families = processPatterns(patterns);

        return families;
    }

    public List<Family> run(Parameters params){
        this.params = params;

        MainAlgorithm mainAlgorithm = executeMainAlgorithm();
        if (mainAlgorithm == null){
            return new ArrayList<>();
        }

        mainAlgorithm.removeRedundantPatterns();
        List<Pattern> patterns = mainAlgorithm.getPatterns();

        List<Family> families = processPatterns(patterns);

        return families;
    }

    private List<Family> processPatterns(List<Pattern> patterns){

        patternsCount = patterns.size();
        computeScores(patterns);
        List<Family> families = clusterToFamilies(patterns);

        return families;
    }

    private MainAlgorithm executeMainAlgorithm(){
        if (params == null || gi == null){
            return null;
        }

        if (datasetTree == null || params.nonDirectons != datasetTree.nonDirectons){
            datasetTree = new DatasetTree(params.nonDirectons, gi);
        }
        MainAlgorithm mainAlgorithm = new MainAlgorithm(params, datasetTree, patternTree, gi);

        return mainAlgorithm;

    }



    private void buildPatternsTree(List<Pattern> patternsFromFile){
        if (patternsFromFile.size() > 0){
            patternTree = new Trie(TreeType.STATIC);
            datasetTree.buildPatternsTree(patternsFromFile, patternTree, gi);
        }
    }

    private void computeScores(List<Pattern> patterns){

        PatternScore pattern_score = new PatternScore(gi.getMaxGenomeSize(), gi.getNumberOfGenomes(), gi.getDatasetLengthSum(),
                gi.cogToContainingGenomes, gi.genomeToCogParalogCount);

        for (Pattern pattern : patterns) {
            List<Integer> patternChars = new ArrayList<>();

            for (Gene gene: pattern.getPatternGenes()){
                patternChars.add(gi.charToIndex.get(gene));
            }
            /*
            if (params.nonDirectons){
                patternChars = genesToStrArr(pattern.getPatternGenes());
            }else{
                patternChars = genesToStrArrWithoutStrand(pattern.getPatternGenes());
            }*/
            double score = PatternScore.computePatternScore(pattern_score, patternChars, params.maxInsertion, params.maxError,
                    params.maxDeletion, pattern.getInstanceCount());
            pattern.setScore(score);
        }
    }

    private String[] genesToStrArr(List<Gene> genes){
        return genes.stream().map(gene -> gene.getCogId() + gene.getStrand().toString())
                .collect(Collectors.toList())
                .toArray(new String[genes.size()]);
    }

    private String[] genesToStrArrWithoutStrand(List<Gene> genes){
        return genes.stream().map(gene -> gene.getCogId())
                .collect(Collectors.toList())
                .toArray(new String[genes.size()]);
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
