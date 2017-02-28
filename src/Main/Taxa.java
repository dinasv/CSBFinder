package Main;

/**
 * Created by Boris on 10/24/2016.
 */
public class Taxa {
    private String kingdom;
    private String phylum;
    private String tax_class;
    private String genus;
    private String species;
    private String order;
    private String name;
    private int sortindex;

    public Taxa(String kingdom, String phylum, String tax_class, String genus, String species, String order, String name, int sortindex){
        this.kingdom = kingdom;
        this.phylum = phylum;
        this.tax_class = tax_class;
        this.genus = genus;
        this.species = species;
        this.order = order;
        this.name = name;
        this.sortindex = sortindex;
    }

    public String getKingdom() {
        return kingdom;
    }

    public String getPhylum() {
        return phylum;
    }

    public String getTax_class() {
        return tax_class;
    }

    public String getGenus() {
        return genus;
    }

    public String getSpecies() {
        return species;
    }

    public String getOrder() {
        return order;
    }

    public String getName() {
        return name;
    }

    public int getSortindex() {
        return sortindex;
    }
}
