package Core.Patterns;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 */
public class PatternLocationsInReplicon {

    private boolean isSorted;
    private List<InstanceLocation> instanceLocations;

    public PatternLocationsInReplicon(){
        instanceLocations = new ArrayList<>();
        isSorted = false;
    }

    public void addLocation(InstanceLocation instanceLocation){
        instanceLocations.add(instanceLocation);
        isSorted = false;
    }

    public List<InstanceLocation> getSortedLocations(){
        if (!isSorted) {
            instanceLocations.sort(Comparator.comparing(InstanceLocation::getActualStartIndex));
            isSorted = true;
        }
        return instanceLocations;
    }

}
