package MVC.View.Models.Filters;

import Core.Genomes.Gene;
import Core.Patterns.Pattern;
import MVC.View.Models.PatternsTableModel;

import javax.swing.*;
import java.util.List;

/**
 */
public class StrandFilter extends RowFilter<PatternsTableModel, Integer>{

    private PatternStrand patternStrand;

    public StrandFilter(PatternStrand patternStrand){
        this.patternStrand = patternStrand;
    }

    @Override
    public boolean include(Entry<? extends PatternsTableModel, ? extends Integer> entry) {
        PatternsTableModel model = entry.getModel();
        Pattern pattern = model.getRowAt(entry.getIdentifier());

        if (isMultiStrand(pattern.getPatternGenes()) == patternStrand.isMultiStrand){
            return true;
        }
        return false;
    }

    private boolean isMultiStrand(List<Gene> genes){

        return genes.stream().map(Gene::getStrand).distinct().count() > 1;

    }
}
