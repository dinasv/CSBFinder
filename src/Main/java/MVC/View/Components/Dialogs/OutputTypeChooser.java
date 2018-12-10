package MVC.View.Components.Dialogs;

import Core.OutputType;
import MVC.View.Events.SaveOutputEvent;
import MVC.View.Listeners.SaveOutputListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 */
public class OutputTypeChooser extends JPanel {

    private ButtonGroup radioBtns;

    public OutputTypeChooser(){
        super(new BorderLayout());

        radioBtns = new ButtonGroup();

        //Create the radio buttons.
        JRadioButton btn1 = new JRadioButton(OutputType.TXT.toString());
        btn1.setActionCommand(OutputType.TXT.toString());
        btn1.setSelected(true);

        JRadioButton btn2 = new JRadioButton(OutputType.XLSX.toString());
        btn2.setActionCommand(OutputType.XLSX.toString());

        JRadioButton btn3 = new JRadioButton(OutputType.EXPORT.toString());
        btn3.setActionCommand(OutputType.EXPORT.toString());

        radioBtns.add(btn1);
        radioBtns.add(btn2);
        radioBtns.add(btn3);

        JLabel label = new JLabel("Output type:");
        //Put the radio buttons in a column in a panel.
        JPanel radioPanel = new JPanel(new GridLayout(0, 1));
        radioPanel.add(label);
        radioPanel.add(btn1);
        radioPanel.add(btn2);
        radioPanel.add(btn3);

        add(radioPanel, BorderLayout.LINE_START);
        setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
    }

    public OutputType getChosenOutput(){
        return OutputType.valueOf(radioBtns.getSelection().getActionCommand());
    }

}
