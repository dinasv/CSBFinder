package mvc.view.components.panels;

import model.postprocess.Family;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StatusBar extends JPanel {

    JLabel label;

    public StatusBar(){
        setLayout(new FlowLayout(FlowLayout.LEADING));

        label = new JLabel();
        //add(new JLabel( Icon.LOAD.getIcon()));
        add(label);
    }

    public void updateStatus(List<Family> familyList){
        int patternsCount = familyList.stream().mapToInt(Family::size).sum();

        String text = String.format("# of families: %d | # of CSBs: %d", familyList.size(), patternsCount);
        setLabel(text);
    }

    public void setLabel(String text){
        label.setText(text);
    }

    public void clearText(){
        label.setText("");
    }
}
