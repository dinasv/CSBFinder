package Core;

import Core.Genomes.Pattern;

/**
 */
public enum AlgorithmType {

    SUFFIX_TREE(new SuffixTreeAlgorithm()),
    MATCH_POINTS(new MatchPointAlgorithm());

    public final Algorithm algorithm;

    AlgorithmType(Algorithm algorithm){
        this.algorithm = algorithm;
    }
}
