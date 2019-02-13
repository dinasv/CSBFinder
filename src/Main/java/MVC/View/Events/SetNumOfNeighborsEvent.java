package MVC.View.Events;

public class SetNumOfNeighborsEvent implements Event {

    private int numOfNeighbors;

    public SetNumOfNeighborsEvent(int numOfNeighbors){
        this.numOfNeighbors = numOfNeighbors;
    }

    public int getNumOfNeighbors() {
        return numOfNeighbors;
    }

}
