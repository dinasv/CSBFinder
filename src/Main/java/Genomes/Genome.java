package Genomes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class Genome {
    private String name;
    private int id;
    private int genome_size;
    private Map<Integer, Replicon> replicons;

    public Genome(String name, int id){
        this.name = name;
        this.id = id;
        genome_size = 0;
        replicons = new HashMap<>();
    }

    public Genome(){
        this("", -1);
    }

    public void addReplicon(Replicon replicon){
        replicons.put(replicon.getId(), replicon);
        genome_size += replicon.size();
    }

    public Collection<Replicon> getReplicons(){
        return replicons.values();
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public Replicon getReplicon(int id){
        return replicons.get(id);
    }

    public int getGenomeSize() {
        return genome_size;
    }
}
