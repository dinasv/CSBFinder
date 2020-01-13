package model.patterns;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.*;

public class Locations {

    private boolean isSorted;
    private List<InstanceLocation> instanceLocations;
    private Comparator<InstanceLocation> comparator;

    public Locations(){
        instanceLocations = new ArrayList<>();
        isSorted = false;

        comparator = comparing(InstanceLocation::getGenomeId)
                    .thenComparing(InstanceLocation::getRepliconId)
                    .thenComparing(InstanceLocation::getActualStartIndex);
    }

    public void addLocation(InstanceLocation instanceLocation){
        instanceLocations.add(instanceLocation);
        isSorted = false;
    }

    public List<InstanceLocation> getInstanceLocations(){
        return instanceLocations;
    }

    public List<InstanceLocation> getSortedLocations(){
        if (!isSorted) {
            instanceLocations.sort(comparator);
            isSorted = true;
        }
        return instanceLocations;
    }
}
