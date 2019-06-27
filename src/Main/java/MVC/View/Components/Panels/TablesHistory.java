package MVC.View.Components.Panels;

import Model.Patterns.Pattern;
import Model.PostProcess.Family;

public class TablesHistory {

    private Family family;
    private Pattern pattern;
    private TableView tableView;

    public TablesHistory(){
        clearAll();
    }

    public void clearAll(){
        family = null;
        pattern = null;
        tableView = TableView.NONE;
    }

    public Family getFamily() {
        return family;
    }

    public void setFamily(Family family) {
        this.family = family;
        tableView = TableView.FAMILY;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
        tableView = TableView.PATTERN;
    }

    public TableView getTableView() {
        return tableView;
    }

    public void setTableView(TableView tableView) {
        this.tableView = tableView;
    }

}

