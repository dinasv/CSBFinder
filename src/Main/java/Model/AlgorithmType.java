package Model;

import Model.MatchPointPatternFinder.MatchPointAlgorithm;
import Model.SuffixTreePatternFinder.SuffixTreeAlgorithm;

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
