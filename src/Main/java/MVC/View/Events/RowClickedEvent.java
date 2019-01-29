package MVC.View.Events;

public class RowClickedEvent<T> {

    private T row;

    public RowClickedEvent(T row) {
        this.row = row;
    }

    public T getRow() {
        return row;
    }
}
