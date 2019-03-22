package Model.Patterns;

import java.util.*;

/**
 * Locations of instance patterns in a single genome
 */
public class PatternLocationsInGenome {

    private Map<Integer, PatternLocationsInReplicon> repliconToLocations;

    public PatternLocationsInGenome(){
        repliconToLocations = new HashMap<>();
    }

    public void addLocation(InstanceLocation instanceLocation){
        if (instanceLocation == null){
            return;
        }
        PatternLocationsInReplicon patternLocationsInReplicon = repliconToLocations.get(
                                                                instanceLocation.getRepliconId());
        if (patternLocationsInReplicon == null){
            patternLocationsInReplicon = new PatternLocationsInReplicon();
        }
        patternLocationsInReplicon.addLocation(instanceLocation);
        repliconToLocations.put(instanceLocation.getRepliconId(), patternLocationsInReplicon);

    }

    public Map<Integer, PatternLocationsInReplicon> getRepliconToLocations(){
        return repliconToLocations;
    }

}
