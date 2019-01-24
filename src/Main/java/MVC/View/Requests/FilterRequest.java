package MVC.View.Requests;

/**
 */
public class FilterRequest implements Request{
    private int minCSBLength = 2;
    private int maxCSBLength = Integer.MAX_VALUE;

    public void setMinCSBLength(int minCSBLength) {
        this.minCSBLength = minCSBLength;
    }

    public void setMaxCSBLength(int maxCSBLength) {
        this.maxCSBLength = maxCSBLength;
    }

    public int getMinCSBLength() {
        return minCSBLength;
    }

    public int getMaxCSBLength() {
        return maxCSBLength;
    }
}
