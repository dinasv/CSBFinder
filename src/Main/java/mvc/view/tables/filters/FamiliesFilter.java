package mvc.view.tables.filters;

import mvc.view.components.dialogs.BooleanOperator;
import mvc.view.components.dialogs.FunctionalCategoryOption;
import mvc.view.tables.ColumnProperty;
import model.genomes.Gene;
import model.patterns.Pattern;
import model.postprocess.Family;
import mvc.view.tables.FamilyProperty;
import mvc.view.tables.PatternProperty;

import java.util.*;
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

        if (familyIds == null || familyIds.length() == 0){
            return;
        }

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

        if (patternIds == null || patternIds.length() == 0){
            return;
        }

        String[] ids = patternIds.split(SEPARATOR);
        List<Filter<Pattern>> matchFilters = Arrays.stream(ids)
                .map(id -> new MatchStringFilter<>(id, PatternProperty.ID))
                .collect(Collectors.toList());

        OrFilter<Pattern> orFilter = new OrFilter<>(matchFilters);
        patternFilters.add(orFilter);
    }

    public void setStrand(mvc.view.tables.filters.PatternStrand patternStrand) {
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

    public void setGeneCategory(String genes, BooleanOperator operator, Function<Gene[], List<String>> genesToCogsDesc){

        if (genes == null || genes.length() == 0){
            return;
        }

        String[] functionalCategories = genes.split(SEPARATOR);
        Map<String, Integer> counter = getCounter(functionalCategories);

        List<Filter<Pattern>> containsStringFilters = Arrays.stream(functionalCategories).map(category ->
                new ContainsGenesFilter<>(category, PatternProperty.GENES,
                        genesToCogsDesc, Gene[].class, counter.get(category))).collect(Collectors.toList());

        addFiltersBooleanOperator(containsStringFilters, operator);

    }
    public void setGeneCategoryExclude(String genes, Function<Gene[], List<String>> genesToCogsDesc){

        if (genes == null || genes.length() == 0){
            return;
        }

        String[] functionalCategories = genes.split(SEPARATOR);

        List<Filter<Pattern>> containsStringFilters = Arrays.stream(functionalCategories).map(category ->
                new ContainsGenesFilter<>(category, PatternProperty.GENES,
                        genesToCogsDesc, Gene[].class)).collect(Collectors.toList());

        NotFilter<Pattern> notFilter = new NotFilter<>(containsStringFilters);
        patternFilters.add(notFilter);

    }

    private Map<String, Integer> getCounter(String[] strs){
        Map<String, Integer> counter = new HashMap<>();
        for (String s : strs) {
            counter.putIfAbsent(s, 0);
            counter.put(s, counter.get(s)+1);
        }
        return counter;
    }

    private void addFiltersBooleanOperator(List<Filter<Pattern>> filters, BooleanOperator operator){
        if (operator == BooleanOperator.AND) {
            patternFilters.addAll(filters);
        }else if (operator == BooleanOperator.OR){
            OrFilter<Pattern> orFilter = new OrFilter<>(filters);
            patternFilters.add(orFilter);
        }
    }

    public void setFunctionalCategory(String functionalCategory, FunctionalCategoryOption option){

        if (functionalCategory == null || functionalCategory.length() == 0){
            return;
        }

        String[] functionalCategories = functionalCategory.split(SEPARATOR);

        List<Filter<Pattern>> containsStringFilters = Arrays.stream(functionalCategories).map(category ->
                new ContainsStringFilter<>(category, PatternProperty.MAIN_CATEGORY)).collect(Collectors.toList());

        if (option == FunctionalCategoryOption.INCLUDE){
            OrFilter<Pattern> orFilter = new OrFilter<>(containsStringFilters);
            patternFilters.add(orFilter);
        }else if (option == FunctionalCategoryOption.EXCLUDE){
            NotFilter<Pattern> notFilter = new NotFilter<>(containsStringFilters);
            patternFilters.add(notFilter);
        }
    }


    public void setGenes(String genes, BooleanOperator operator){

        if (genes == null || genes.length() == 0){
            return;
        }

        String[] ids = genes.split(SEPARATOR);

        List<Filter<Pattern>> containsStringFilters = Arrays.stream(ids).map(gene -> new ContainsStringFilter<>(gene,
                PatternProperty.CSB)).collect(Collectors.toList());

        addFiltersBooleanOperator(containsStringFilters, operator);
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
        private mvc.view.tables.ColumnProperty<T> patternProperty;

        NumberFilter(int value, NumberComparison numberComparison, mvc.view.tables.ColumnProperty<T> patternProperty) {
            this.value = value;
            this.patternProperty = patternProperty;
            this.numberComparison = numberComparison;
        }

        @Override
        public boolean include(T obj) {
            Function<T, ?> function = patternProperty.getFunction();

            Object result = function.apply(obj);
            if (result instanceof Number) {
                Number num = (Number) result;
                return numberComparison.comparisonFunc.apply(num.doubleValue(), value.doubleValue());
            }

            return false;
        }
    }

    private class StrandFilter implements Filter<Pattern> {

        private mvc.view.tables.filters.PatternStrand patternStrand;

        public StrandFilter(mvc.view.tables.filters.PatternStrand patternStrand) {
            this.patternStrand = patternStrand;
        }

        private boolean isMultiStrand(Gene[] genes) {

            return Arrays.stream(genes).map(Gene::getStrand).distinct().count() > 1;

        }

        @Override
        public boolean include(Pattern pattern) {
            return patternStrand == PatternStrand.ALL ||
                    isMultiStrand(pattern.getPatternGenes()) == patternStrand.isMultiStrand;
        }
    }

    private abstract class StringFilter<T> implements Filter<T> {

        protected String strToFind;

        protected Function<T, ?> propertyFunction;

        StringFilter(String str, mvc.view.tables.ColumnProperty<T> patternProperty) {
            strToFind = str;
            this.propertyFunction = patternProperty.getFunction();
        }
    }

    private class MatchStringFilter<T> extends StringFilter<T> {

        public MatchStringFilter(String str, mvc.view.tables.ColumnProperty<T> patternProperty) {
            super(str, patternProperty);
        }

        public boolean include(T obj) {

            Object result = propertyFunction.apply(obj);

            if (result instanceof String) {

                String str = (String) result;

                return isMatching(str);
            }

            return false;
        }

        protected boolean isMatching(String str){
            if (this.strToFind.equals("")) {
                return true;
            }
            return this.strToFind.toLowerCase().matches(str.toLowerCase());
        }
    }

    private class ContainsGenesFilter<T, E> extends ContainsStringFilter<T> {

        private Function<E, List<String>> preprocessFunction;
        private Class<E> paramType;
        private int counter;

        public ContainsGenesFilter(String str, mvc.view.tables.ColumnProperty<T> patternProperty,
                                   Function<E, List<String>> preprocessFunction, Class<E> paramType, int counter) {
            super(str, patternProperty);
            this.preprocessFunction = preprocessFunction;
            this.paramType = paramType;
            this.counter = counter;
        }

        public ContainsGenesFilter(String str, mvc.view.tables.ColumnProperty<T> patternProperty,
                                   Function<E, List<String>> preprocessFunction, Class<E> paramType) {
            this(str, patternProperty, preprocessFunction, paramType, 1);
        }

        public boolean include(T obj) {

            Object result = propertyFunction.apply(obj);
            if (result != null && paramType.isAssignableFrom(result.getClass())) {
                List<String> str = preprocessFunction.apply((E)result);
                int containedCount = 0;
                for (String s : str) {
                    if (isContained(s)){
                        containedCount += 1;
                    }

                    if (containedCount >= counter){
                        return true;
                    }
                }
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

                return isContained(patternStr);
            }

            return false;
        }

        protected boolean isContained(String str){

            if (this.strToFind.equals("")) {
                return true;
            }

            return str.toLowerCase().contains(this.strToFind.toLowerCase());
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

    private class NotFilter<T> implements Filter<T> {

        private List<Filter<T>> filters;

        public NotFilter(List<Filter<T>> filters){
            this.filters = filters;
        }

        @Override
        public boolean include(T obj) {
            for (Filter<T> filter: filters){
                if(filter.include(obj)){
                    return false;
                }
            }
            return true;
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
