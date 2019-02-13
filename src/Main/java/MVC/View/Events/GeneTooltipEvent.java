package MVC.View.Events;

import javax.swing.*;

/**
 */
public class GeneTooltipEvent implements Event{

    private String cogId;
    private JPanel src;

    public GeneTooltipEvent(JPanel src, String cogId) {
        this.cogId = cogId;
        this.src = src;
    }

    public String getCogId() {
        return cogId;
    }

    public JPanel getSrc() {
        return src;
    }
}
