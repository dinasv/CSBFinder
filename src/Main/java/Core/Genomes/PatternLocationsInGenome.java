package Core.Genomes;

import java.util.*;

/**
 * Locations of instance patterns in a single genome
 */
public class PatternLocationsInGenome {

    Map<Integer, List<InstanceLocation>> repliconToLocations;

    public PatternLocationsInGenome(){
        repliconToLocations = new TreeMap<Integer, List<InstanceLocation>>();
    }

    public void addLocation(InstanceLocation instanceLocation){
        if (instanceLocation == null){
            return;
        }
        List<InstanceLocation> locations = repliconToLocations.get(instanceLocation.getRepliconId());
        if (locations == null){
            locations = new ArrayList<>();
        }
        locations.add(instanceLocation);
        repliconToLocations.put(instanceLocation.getRepliconId(), locations);

    }

    public Map<Integer, List<InstanceLocation>>  getSortedLocations(){
        for (List<InstanceLocation> locations: repliconToLocations.values()){
            locations.sort(Comparator.comparing(InstanceLocation::getActualStartIndex));
        }
        return repliconToLocations;
    }

    public String getGenomeName(){
        String name = "";
        if (repliconToLocations.size() > 0){
            Iterator<Integer> it = repliconToLocations.keySet().iterator();
            if (it.hasNext()){
                List<InstanceLocation> locations = repliconToLocations.get(it.next());
                if (locations.size() > 0){
                    name = locations.get(0).getGenomeName();
                }
            }
        }
        return name;
    }

}
