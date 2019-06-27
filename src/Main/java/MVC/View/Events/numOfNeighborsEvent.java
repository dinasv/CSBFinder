package MVC.View.Events;

public class numOfNeighborsEvent implements Event {

    private int numOfNeighbors;

    public numOfNeighborsEvent(int numOfNeighbors){
        this.numOfNeighbors = numOfNeighbors;
    }

    public int getNumOfNeighbors() {
        return numOfNeighbors;
    }

}
