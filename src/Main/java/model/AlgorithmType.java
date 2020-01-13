package model;

import model.matchpointsbased.MatchPointAlgorithm;
import model.suffixtreebased.SuffixTreeAlgorithm;

/**
 */
public enum AlgorithmType {

    SUFFIX_TREE,
    MATCH_POINTS;

    public Algorithm getAlgorithm(){
        if (this == SUFFIX_TREE){
            return new SuffixTreeAlgorithm();
        }else{
            return new MatchPointAlgorithm();
        }
    }
}
