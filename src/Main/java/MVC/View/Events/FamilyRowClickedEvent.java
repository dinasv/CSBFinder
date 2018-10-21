package MVC.View.Events;

import Core.Genomes.Pattern;

public class FamilyRowClickedEvent {

    private Pattern pattern;

    public FamilyRowClickedEvent(Pattern pattern) {
        this.pattern = pattern;
    }

    public Pattern getPattern() {
        return pattern;
    }
}
