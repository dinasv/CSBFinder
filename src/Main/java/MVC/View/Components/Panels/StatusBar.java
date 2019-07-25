package MVC.View.Components.Panels;

import Model.PostProcess.Family;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StatusBar extends JPanel {

    JLabel label;

    public StatusBar(){
        setLayout(new BorderLayout());

        label = new JLabel();
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
