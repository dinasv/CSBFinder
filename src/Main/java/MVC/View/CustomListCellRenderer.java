package MVC.View;

import javax.swing.*;
import java.awt.*;

public class CustomListCellRenderer extends JLabel implements ListCellRenderer {


    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String val = value.toString();

//        if (val.contains("-")) {
//            setBackground(new Color(100));
//            setFont(new Font(Font.SERIF, Font.BOLD, 16));
//        } else {
//            setBackground(new Color(100, 100, 20));
//        }
//        set
        setText(val.substring(0, val.length() - 1));
        return this;
    }
}
