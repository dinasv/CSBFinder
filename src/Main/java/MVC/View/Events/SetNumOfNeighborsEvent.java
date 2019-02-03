package MVC.View.Events;

public class SetNumOfNeighborsEvent {

    private int numOfNeighbors;

    public SetNumOfNeighborsEvent(int numOfNeighbors){
        this.numOfNeighbors = numOfNeighbors;
    }

    public int getNumOfNeighbors() {
        return numOfNeighbors;
    }

}
