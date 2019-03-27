package MVC.View.Models.Filters;

import Model.Genomes.Gene;
import Model.Patterns.Pattern;
import Model.PostProcess.Family;
import MVC.View.Models.ColumnProperty;
import MVC.View.Models.FamilyProperty;
import MVC.View.Models.PatternProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 */
public class FamiliesFilter {

    public static final String SEPARATOR = ",";

    private List<Family> families;
    private List<Family> filteredFamilies;

    private List<Filter<Pattern>> patternFilters;
    private List<Filter<Family>> familyFilters;

    public FamiliesFilter() {
        this.families = new ArrayList<>();
        filteredFamilies = families;

        patternFilters = new ArrayList<>();
        familyFilters = new ArrayList<>();
    }

    public void setFamilies(List<Family> families) {
        this.families = families;
        filteredFamilies = families;
    }

    public void setFamilyIds(String familyIds){
        String[] ids = familyIds.split(SEPARATOR);

        List<Filter<Family>> numberFilters = new ArrayList<>();
        for (String id: ids){
            try {
                int intId = Integer.valueOf(id);
                List<Filter<Family>> familyIdFilter = new ArrayList<>();
                familyIdFilter.add(new NumberFilter<>(intId, NumberComparison.LARGER_EQ, FamilyProperty.FAMILY_ID));
                familyIdFilter.add(new NumberFilter<>(intId, NumberComparison.LESS_EQ, FamilyProperty.FAMILY_ID));

                numberFilters.add(new AndFilter<>(familyIdFilter));

            }catch (NumberFormatException e){
                //skip
            }
        }

        if (numberFilters.size() > 0) {
            OrFilter<Family> orFilter = new OrFilter<>(numberFilters);
            familyFilters.add(orFilter);
        }
    }

    public void setPatternIds(String patternIds) {
        String[] ids = patternIds.split(SEPARATOR);
        List<Filter<Pattern>> matchFilters = Arrays.stream(ids)
                .map(id -> new MatchStringFilter<>(id, PatternProperty.ID))
                .collect(Collectors.toList());

        OrFilter<Pattern> orFilter = new OrFilter<>(matchFilters);
        patternFilters.add(orFilter);
    }

    public void setStrand(PatternStrand patternStrand) {
        patternFilters.add(new StrandFilter(patternStrand));
    }

    public void setPatternMinLength(int val) {
        patternFilters.add(new NumberFilter<>(val, NumberComparison.LARGER_EQ, PatternProperty.LENGTH));
    }

    public void setPatternMaxLength(int val) {
        patternFilters.add(new NumberFilter<>(val, NumberComparison.LESS_EQ, PatternProperty.LENGTH));
    }

    public void setPatternMinScore(int val) {
        patternFilters.add(new NumberFilter<>(val, NumberComparison.LARGER_EQ, PatternProperty.SCORE));
    }

    public void setPatternMaxScore(int val) {
        patternFilters.add(new NumberFilter<>(val, NumberComparison.LESS_EQ, PatternProperty.SCORE));
    }

    public void setPatternMinCount(int val) {
        patternFilters.add(new NumberFilter<>(val, NumberComparison.LARGER_EQ, PatternProperty.INSTANCE_COUNT));
    }

    public void setPatternMaxCount(int val) {
        patternFilters.add(new NumberFilter<>(val, NumberComparison.LESS_EQ, PatternProperty.INSTANCE_COUNT));
    }


    public void setGenes(String genes){
        String[] ids = genes.split(SEPARATOR);
        patternFilters.addAll(Arrays.stream(ids).map(gene -> new ContainsStringFilter<>(gene,
                PatternProperty.CSB)).collect(Collectors.toList()));
    }

    public void clear() {
        patternFilters.clear();
        familyFilters.clear();
    }

    public void applyFilters() {
        if (familyFilters.size() == 0 && patternFilters.size() == 0) {
            filteredFamilies = families;
            return;
        }

        filteredFamilies = new ArrayList<>();
        for (Family family : families) {
            boolean includeFamily = filterObj(family, familyFilters);

            if (includeFamily) {
                List<Pattern> filteredPatterns = filterFamilyPatterns(family.getPatterns());
                if (filteredPatterns.size() > 0) {
                    filteredFamilies.add(new Family(family, filteredPatterns));
                }
            }
        }
    }


    private <T> boolean filterObj(T obj, List<Filter<T>> filters){
        boolean includeObj = true;
        for (Filter<T> filter : filters) {
            if (!filter.include(obj)) {
                includeObj = false;
                break;
            }
        }
        return includeObj;
    }

    private List<Pattern> filterFamilyPatterns(List<Pattern> patterns){
        List<Pattern> filteredPatterns = new ArrayList<>();
        for (Pattern pattern : patterns) {
            boolean includePattern = filterObj(pattern, patternFilters);

            if (includePattern) {
                filteredPatterns.add(pattern);
            }
        }
        return filteredPatterns;
    }

    private class NumberFilter<T> implements Filter<T> {

        private Integer value;
        private NumberComparison numberComparison;
        private ColumnProperty<T> patternProperty;

        NumberFilter(int value, NumberComparison numberComparison, ColumnProperty<T> patternProperty) {
            this.value = value;
            this.patternProperty = patternProperty;
            this.numberComparison = numberComparison;
        }

        @Override
        public boolean include(T obj) {
            Function<T, ? extends Object> function = patternProperty.getFunction();

            Object result = function.apply(obj);
            if (result instanceof Number) {
                Number num = (Number) result;
                return numberComparison.comparisonFunc.apply(num.doubleValue(), value.doubleValue());
            }

            return false;
        }
    }

    private class StrandFilter implements Filter<Pattern> {

        private PatternStrand patternStrand;

        public StrandFilter(PatternStrand patternStrand) {
            this.patternStrand = patternStrand;
        }

        private boolean isMultiStrand(Gene[] genes) {

            return Arrays.stream(genes).map(Gene::getStrand).distinct().count() > 1;

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

    private abstract class StringFilter<T> implements Filter<T> {

        protected String strToFind;

        protected Function<T, ? extends Object> propertyFunction;

        StringFilter(String str, ColumnProperty<T> patternProperty) {
            strToFind = str;
            this.propertyFunction = patternProperty.getFunction();
        }
    }

    private class MatchStringFilter<T> extends StringFilter<T> {

        public MatchStringFilter(String str, ColumnProperty<T> patternProperty) {
            super(str, patternProperty);
        }

        public boolean include(T obj) {

            Object result = propertyFunction.apply(obj);

            if (result instanceof String) {

                String str = (String) result;

                if (this.strToFind.equals("")) {
                    return true;
                }
                return this.strToFind.toLowerCase().matches(str.toLowerCase());
            }

            return false;
        }
    }

    private class ContainsStringFilter<T> extends StringFilter<T> {

        public ContainsStringFilter(String str, ColumnProperty<T> patternProperty) {
            super(str, patternProperty);
        }

        public boolean include(T obj) {

            Object result = propertyFunction.apply(obj);

            if (result instanceof String) {

                String patternStr = (String) result;

                if (this.strToFind.equals("")) {
                    return true;
                }

                return patternStr.toLowerCase().contains(this.strToFind.toLowerCase());
            }

            return false;
        }
    }

    private class AndFilter<T> implements Filter<T> {

        private List<Filter<T>> filters;

        public AndFilter(List<Filter<T>> filters){
            this.filters = filters;
        }

        @Override
        public boolean include(T obj) {
            for (Filter<T> filter: filters){
                if(!filter.include(obj)){
                    return false;
                }
            }
            return true;
        }
    }

    private class OrFilter<T> implements Filter<T> {

        private List<Filter<T>> filters;

        public OrFilter(List<Filter<T>> filters){
            this.filters = filters;
        }

        @Override
        public boolean include(T obj) {
            for (Filter<T> filter: filters){
                if(filter.include(obj)){
                    return true;
                }
            }
            return false;
        }
    }

    public List<Family> getFilteredFamilies() {
        return filteredFamilies;
    }

    private enum NumberComparison{

        LESS_EQ(NumberComparison::aLessEqb),
        LARGER_EQ(NumberComparison::aLargerEqb);

        public final BiFunction<Double, Double, Boolean> comparisonFunc;

        NumberComparison(BiFunction<Double, Double, Boolean> comparisonFunc){
            this.comparisonFunc = comparisonFunc;
        }

        public static boolean aLessEqb(double a, double b){
            return a <= b;
        }
        public static boolean aLargerEqb(double a, double b){
            return a >= b;
        }
    }
}
