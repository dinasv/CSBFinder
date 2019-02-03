package MVC.View.Models.Filters;

import Core.Genomes.Gene;
import Core.Patterns.Pattern;
import Core.PostProcess.Family;
import MVC.View.Models.PatternProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 */
public class FamiliesFilter {

    public static final String SEPARATOR = ",";

    private List<Family> families;
    private List<PatternFilter> filters;

    public FamiliesFilter() {
        this.families = new ArrayList<>();
        filters = new ArrayList<>();
    }

    public void setFamilies(List<Family> families) {
        this.families = families;
    }

    public void setId(String patternIds) {
        String[] ids = patternIds.split(SEPARATOR);
        List<PatternFilter> matchFilters = Arrays.stream(ids).map(id -> new MatchStringFilter(id, PatternProperty.ID)).collect(Collectors.toList());
        OrFilter orFilter = new OrFilter(matchFilters);
        filters.add(orFilter);
    }

    public void setStrand(PatternStrand patternStrand) {
        filters.add(new StrandFilter(patternStrand));
    }

    public void setPatternLength(int from, int to) {
        filters.add(new NumberFilter(from, to, PatternProperty.LENGTH));
    }

    public void setPatternScore(int from, int to) {
        filters.add(new NumberFilter(from, to, PatternProperty.SCORE));
    }

    public void setPatternCount(int from, int to) {
        filters.add(new NumberFilter(from, to, PatternProperty.INSTANCE_COUNT));
    }

    public void setGenes(String genes){
        String[] ids = genes.split(SEPARATOR);
        filters.addAll(Arrays.stream(ids).map(gene -> new ContainsStringFilter(gene,
                PatternProperty.CSB)).collect(Collectors.toList()));
    }

    public void clear() {
        filters.clear();
    }

    public List<Family> applyFilters() {
        if (filters.size() == 0) {
            return families;
        }

        List<Family> filteredFamilies = new ArrayList<>();
        for (Family family : families) {
            List<Pattern> filteredPatterns = new ArrayList<>();
            for (Pattern pattern : family.getPatterns()) {
                boolean includePattern = true;
                for (PatternFilter filter : filters) {
                    if (!filter.include(pattern)) {
                        includePattern = false;
                        break;
                    }
                }
                if (includePattern) {
                    filteredPatterns.add(pattern);
                }
            }
            if (filteredPatterns.size() > 0) {
                filteredFamilies.add(new Family(family, filteredPatterns));
            }
        }
        return filteredFamilies;
    }

    private class NumberFilter implements PatternFilter {

        private int from;
        private int to;
        private PatternProperty patternProperty;

        NumberFilter(int from, int to, PatternProperty patternProperty) {
            this.from = from;
            this.to = to;
            this.patternProperty = patternProperty;
        }

        @Override
        public boolean include(Pattern pattern) {
            Function<Pattern, ? extends Object> function = patternProperty.getFunction();
            Object result = function.apply(pattern);
            if (result instanceof Number) {
                Number num = (Number) result;
                return num.doubleValue() >= from && num.doubleValue() <= to;
            }
            return false;
        }

    }

    private class StrandFilter implements PatternFilter {

        private PatternStrand patternStrand;

        public StrandFilter(PatternStrand patternStrand) {
            this.patternStrand = patternStrand;
        }

        private boolean isMultiStrand(List<Gene> genes) {

            return genes.stream().map(Gene::getStrand).distinct().count() > 1;

        }

        @Override
        public boolean include(Pattern pattern) {
            if (patternStrand == PatternStrand.ALL ||
                    isMultiStrand(pattern.getPatternGenes()) == patternStrand.isMultiStrand) {
                return true;
            }
            return false;
        }
    }

    private abstract class StringFilter implements PatternFilter {

        protected String strToFind;

        protected Function<Pattern, ? extends Object> patternPropertyFunction;

        StringFilter(String str, PatternProperty patternProperty) {
            strToFind = str;
            this.patternPropertyFunction = patternProperty.getFunction();
        }
    }

    private class MatchStringFilter extends StringFilter {

        public MatchStringFilter(String str, PatternProperty patternProperty) {
            super(str, patternProperty);
        }

        public boolean include(Pattern pattern) {

            Object result = patternPropertyFunction.apply(pattern);

            if (result instanceof String) {

                String patternStr = (String) result;

                if (this.strToFind.equals("")) {
                    return true;
                }
                return this.strToFind.matches(patternStr);
            }

            return false;
        }
    }

    private class ContainsStringFilter extends StringFilter {

        public ContainsStringFilter(String str, PatternProperty patternProperty) {
            super(str, patternProperty);
        }

        public boolean include(Pattern pattern) {

            Object result = patternPropertyFunction.apply(pattern);

            if (result instanceof String) {

                String patternStr = (String) result;

                if (this.strToFind.equals("")) {
                    return true;
                }

                return patternStr.contains(this.strToFind);
            }

            return false;
        }
    }

    private class OrFilter implements PatternFilter {

        private List<PatternFilter> filters;

        public OrFilter(List<PatternFilter> filters){
            this.filters = filters;
        }

        @Override
        public boolean include(Pattern pattern) {
            for (PatternFilter filter: filters){
                if(filter.include(pattern)){
                    return true;
                }
            }
            return false;
        }
    }
}
