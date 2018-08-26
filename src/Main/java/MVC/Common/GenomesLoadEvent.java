package MVC.Common;

import Utils.Gene;

import java.util.List;
import java.util.Map;

public class GenomesLoadEvent {
    private Map<String, List<Gene>> genomeToGeneListMap;

    public GenomesLoadEvent(Map<String,List<Gene>> genomeToGeneListMap) {
        this.genomeToGeneListMap = genomeToGeneListMap;
    }

    public Map<String, List<Gene>> getGenomeToGeneListMap() {
        return genomeToGeneListMap;
    }

}
