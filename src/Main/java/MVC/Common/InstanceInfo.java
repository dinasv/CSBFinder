package MVC.Common;

import Utils.Gene;
import Utils.InstanceLocation;

import java.util.List;

/**
 */
public class InstanceInfo {

    private InstanceLocation instanceLocation;
    private List<Gene> genes;

    public InstanceInfo(InstanceLocation instanceLoc, List<Gene> genes){

        this.instanceLocation = instanceLoc;
        this.genes = genes;
    }

    public InstanceLocation getInstanceLocation() {
        return instanceLocation;
    }

    public void setInstanceLocation(InstanceLocation instanceLocation) {
        this.instanceLocation = instanceLocation;
    }

    public List<Gene> getGenes() {
        return genes;
    }

    public void setGenes(List<Gene> genes) {
        this.genes = genes;
    }
}
