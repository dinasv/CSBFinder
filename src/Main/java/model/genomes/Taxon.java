package model.genomes;

import java.util.HashMap;
import java.util.Map;

public class Taxon {
    public final static String NO_TAXA = "";
    public final String kingdom;
    public final String phylum;
    public final String taxClass;
    public final String genus;
    public final String species;

    private static Map<String, Integer> countTaxa = new HashMap<>();

    public Taxon(String kingdom, String phylum, String taxClass, String genus, String species){
        this.kingdom = kingdom;
        this.phylum = phylum;
        this.taxClass = taxClass;
        this.genus = genus;
        this.species = species;

        updateCountTaxa(kingdom);
        updateCountTaxa(phylum);
        updateCountTaxa(taxClass);
        updateCountTaxa(genus);
        updateCountTaxa(species);
    }

    public Taxon(){
        this(NO_TAXA,NO_TAXA,NO_TAXA,NO_TAXA,NO_TAXA);
    }

    private void updateCountTaxa(String taxa){
        int count = countTaxa.getOrDefault(taxa, 0);
        count += 1;
        countTaxa.put(taxa, count);
    }

    public static int getTaxaCount(String taxa){

        if (taxa.equals(NO_TAXA)){
            return -1;
        }

        return countTaxa.getOrDefault(taxa, -1);
    }

    public String getTaxaAtLevel(int level){
        String taxa;
        switch (level){
            case 0:
                taxa = kingdom;
                break;
            case 1:
                taxa = phylum;
                break;
            case 2:
                taxa = taxClass;
                break;
            case 3:
                taxa = genus;
                break;
            case 4:
                taxa = species;
                break;
            default:
                taxa = "-";
            break;
        }
        return taxa;
    }

}
