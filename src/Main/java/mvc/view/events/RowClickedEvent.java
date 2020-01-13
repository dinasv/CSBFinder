package mvc.view.events;

public class RowClickedEvent<V> {

    private V row;

    public RowClickedEvent(V row) {
        this.row = row;
    }

    public V getRow() {
        return row;
    }
}
