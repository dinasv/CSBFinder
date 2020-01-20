package model.genomes;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 */
public class Replicon implements GenomicSegment {

    private String name;

    private int repliconId;
    private int genomeId;

    protected List<Gene> genes;
    private Strand strand;
    private int startIndex;

    private boolean circular;
    private static final int GENES_FROM_START = 50;

    public Replicon(){
        this("", -1, -1, Strand.INVALID, false);
    }

    public Replicon(String name, int repliconId, int genomeId, Strand strand, boolean circular){
        this(name, repliconId, genomeId, strand, new ArrayList<>(), circular);
    }

    public Replicon(String name, int repliconId, int genomeId, Strand strand, List<Gene> genes, boolean circular){
        this.name = name;
        this.repliconId = repliconId;
        this.genomeId = genomeId;
        this.strand = strand;
        this.genes = genes;
        this.circular = circular;
        startIndex = 0;
    }

    public Replicon(Replicon other){
        this(other.name, other.repliconId, other.genomeId, other.strand, other.circular);

        genes.addAll(other.genes);
    }

    public Replicon(Strand strand){
        this();
        this.strand = strand;
    }


    private static Strand reverseStrand(Strand strand){
        return strand == Strand.REVERSE ? Strand.FORWARD : Strand.REVERSE;
    }

    public Replicon reverseComplement(){

        Strand reversedStrand = reverseStrand(strand);
        List<Gene> reverseComplementGenes = reverseComplementGenes(genes);

        return new Replicon(name, repliconId, genomeId, reversedStrand, reverseComplementGenes, circular);
    }

    public static List<Gene> reverseComplementGenes(List<Gene> genes){
        List<Gene> reversedGenes = new ArrayList<>(genes);
        Collections.reverse(reversedGenes);

        List<Gene> reverseComplementGenes = reversedGenes.stream()
                .map(gene -> new Gene(gene.getCogId().intern(), Gene.reverseStrand(gene.getStrand())))
                .collect(Collectors.toList());

        return reverseComplementGenes;
    }


    public List<Directon> splitRepliconToDirectons(String UNK_CHAR) {

        List<Directon> directons = new ArrayList<>();

        int directonId = 1;
        Directon directon = new Directon(directonId++, this, getGenomeId());

        int geneIndex = 0;
        List<Gene> genes = getGenes();
        for (Gene gene : genes) {
            //end directon if it is the last gene in the replicon, or if next gene is on different strand
            boolean endDirecton = (geneIndex == genes.size()-1) ||
                    !(gene.getStrand().equals(genes.get(geneIndex+1).getStrand()));
            if (directon.getGenes().size() == 0) {
                //returned to start of replicon in circular replicon, don't create new directon
                if (geneIndex >= this.size()){
                    break;
                }
                if (!gene.getCogId().equals(UNK_CHAR) && !endDirecton) {
                    directon.setStrand(gene.getStrand());
                    directon.addGene(new Gene(gene.getCogId(), Strand.INVALID));
                }
            } else {
                directon.addGene(new Gene(gene.getCogId(), Strand.INVALID));

                if (endDirecton){//directon.size()>0

                    if (directon.size() > 1) {

                        directon.setStartIndex(geneIndex-directon.getGenes().size()+1);

                        directon.removeUnkChars(UNK_CHAR);
                        if (directon.getGenes().size() > 1) {

                            directons.add(directon);
                        }
                    }

                    directon = new Directon(directonId++, this, getGenomeId());
                }
            }
            geneIndex ++;
        }

        return directons;
    }

    public String getName() {
        return name;
    }

    public void setStrand(Strand strand) {
        this.strand = strand;
    }

    @Override
    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    @Override
    public List<Gene> getGenes() {
        if (circular) {
            List<Gene> firstGenes = genes.subList(0, Math.min(GENES_FROM_START, genes.size()-1));
            return Stream.of(genes, firstGenes)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
        return genes;
    }

    @Override
    public int getRepliconId() {
        return repliconId;
    }

    @Override
    public int getGenomeId() {
        return genomeId;
    }

    @Override
    public int getId() {
        return strand.numericValue * repliconId;
    }

    @Override
    public int size() {
        return genes.size();
    }

    @Override
    public void addGene(Gene gene) {
        genes.add(gene);
    }

    @Override
    public void addAllGenes(List<Gene> genes) {
        this.genes.addAll(genes);
    }

    @Override
    public Strand getStrand() {
        return strand;
    }

}
