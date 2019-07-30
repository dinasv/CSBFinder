package Model.Genomes;

public class Taxon {
    public final String kingdom;
    public final String phylum;
    public final String taxClass;
    public final String genus;

    public Taxon(String kingdom, String phylum, String taxClass, String genus){
        this.kingdom = kingdom;
        this.phylum = phylum;
        this.taxClass = taxClass;
        this.genus = genus;
    }

    public Taxon(){
        this("-","-","-","-");
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
            default:
                taxa = "-";
            break;
        }
        return taxa;
    }

}
