package Model.Genomes;

import java.util.HashMap;
import java.util.Map;

public class Taxon {
    public final static String NO_TAXA = "";
    public final String kingdom;
    public final String phylum;
    public final String taxClass;
    public final String genus;
    public final String species;

    public Taxon(String kingdom, String phylum, String taxClass, String genus, String species){
        this.kingdom = kingdom;
        this.phylum = phylum;
        this.taxClass = taxClass;
        this.genus = genus;
        this.species = species;
    }

    public Taxon(){
        this(NO_TAXA,NO_TAXA,NO_TAXA,NO_TAXA,NO_TAXA);
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
