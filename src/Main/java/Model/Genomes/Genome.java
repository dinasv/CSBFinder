package Model.Genomes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class Genome {

    private String name;
    private int id;
    private int genomeSize;
    private Map<Integer, Replicon> repliconsById;
    private Map<String, Replicon> repliconsByName;

    public Genome(String name, int id){
        this.name = name;
        this.id = id;
        genomeSize = 0;
        repliconsById = new HashMap<>();
        repliconsByName = new HashMap<>();
    }

    public Genome(){
        this("", -1);
    }

    public void addReplicon(Replicon replicon){
        repliconsById.put(replicon.getRepliconId(), replicon);
        repliconsByName.put(replicon.getName(), replicon);
        genomeSize += replicon.size();
    }

    public Collection<Replicon> getReplicons(){
        return repliconsById.values();
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public Replicon getReplicon(int id){
        return repliconsById.get(id);
    }

    public Replicon getReplicon(String name){
        return repliconsByName.get(name);
    }

    public int getGenomeSize() {
        return genomeSize;
    }
}
