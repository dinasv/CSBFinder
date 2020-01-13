package mvc.view.events;

public class ShowOnlyTablesEvent implements Event {

    private boolean showOnlyTables;

    public ShowOnlyTablesEvent(boolean showOnlyTables){
        this.showOnlyTables = showOnlyTables;
    }

    public boolean isShowOnlyTables() {
        return showOnlyTables;
    }
}
