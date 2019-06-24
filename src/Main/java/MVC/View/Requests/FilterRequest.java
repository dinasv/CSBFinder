package MVC.View.Requests;

import MVC.View.Components.Dialogs.BooleanOperator;
import MVC.View.Components.Dialogs.FunctionalCategoryOption;
import MVC.View.Models.Filters.PatternStrand;

import java.util.Optional;

/**
 */
public class FilterRequest implements Request{

    private Integer minCSBLength;
    private Integer maxInstanceCount;
    private Integer maxCSBLength;
    private Integer minScore;
    private Integer maxScore;
    private Integer minInstanceCount;
    private PatternStrand patternStrand;
    private String patternIds;
    private String patternGenes;
    private BooleanOperator genesOperator;
    private String familyIds;

    private String functionalCategory;
    private FunctionalCategoryOption functionalCategoryOption;

    public FilterRequest(){
        initAllFields();
    }

    public void initAllFields(){
        minCSBLength = null;
        maxCSBLength = null;
        minScore = null;
        maxScore = null;
        minInstanceCount = null;
        maxInstanceCount = null;
        patternStrand = null;
        patternIds = null;
        patternGenes = null;
        familyIds = null;
        genesOperator = BooleanOperator.AND;
        functionalCategory = null;
        functionalCategoryOption = FunctionalCategoryOption.INCLUDE;
    }

    public void setMinCSBLength(int minCSBLength) {
        this.minCSBLength = minCSBLength;
    }

    public void setMaxCSBLength(int maxCSBLength) {
        this.maxCSBLength = maxCSBLength;
    }

    public Optional<Integer> getMinCSBLength() {
        return Optional.ofNullable(minCSBLength);
    }

    public Optional<Integer> getMaxCSBLength() {
        return Optional.ofNullable(maxCSBLength);
    }

    public Optional<Integer> getMinScore() {
        return Optional.ofNullable(minScore);
    }

    public void setMinScore(int minScore) {
        this.minScore = minScore;
    }

    public Optional<Integer> getMaxScore() {
        return Optional.ofNullable(maxScore);
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public Optional<Integer> getMinInstanceCount() {
        return Optional.ofNullable(minInstanceCount);
    }

    public void setMinInstanceCount(int minInstanceCount) {
        this.minInstanceCount = minInstanceCount;
    }

    public Optional<Integer> getMaxInstanceCount() {
        return Optional.ofNullable(maxInstanceCount);
    }

    public void setMaxInstanceCount(int maxInstanceCount) {
        this.maxInstanceCount = maxInstanceCount;
    }

    public Optional<PatternStrand> getPatternStrand() {
        return Optional.ofNullable(patternStrand);
    }

    public void setPatternStrand(PatternStrand patternStrand) {
        this.patternStrand = patternStrand;
    }

    public Optional<String> getPatternIds() {
        return Optional.ofNullable(patternIds);
    }

    public void setPatternIds(String patternIds) {
        this.patternIds = patternIds;
    }

    public void setPatternGenes(String patternGenes) {
        this.patternGenes = patternGenes;
    }

    public Optional<String> getPatternGenes() {
        return Optional.ofNullable(patternGenes);
    }

    public void setGenesOperator(BooleanOperator operator) {
        this.genesOperator = operator;
    }

    public BooleanOperator getGenesOperator() {
        return genesOperator;
    }


    public Optional<String> getFamilyIds() {
        return Optional.ofNullable(familyIds);
    }

    public void setFamilyIds(String familyIds) {
        this.familyIds = familyIds;
    }

    public Optional<String> getFunctionalCategory() {
        return Optional.ofNullable(functionalCategory);
    }

    public void setFunctionalCategory(String functionalCategory) {
        this.functionalCategory = functionalCategory;
    }

    public FunctionalCategoryOption getFunctionalCategoryOption() {
        return functionalCategoryOption;
    }

    public void setFunctionalCategoryOption(FunctionalCategoryOption functionalCategoryOption) {
        this.functionalCategoryOption = functionalCategoryOption;
    }
}
