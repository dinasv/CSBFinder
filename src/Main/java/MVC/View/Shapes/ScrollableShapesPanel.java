package MVC.View.Shapes;

import javax.swing.*;
import java.awt.event.MouseAdapter;

public class ScrollableShapesPanel extends JScrollPane {

    public ScrollableShapesPanel(ShapesPanel shapesPanel) {

        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        MouseAdapter ma = new MouseAdapterScroller(shapesPanel);

        shapesPanel.addMouseListener(ma);
        shapesPanel.addMouseMotionListener(ma);

        add(shapesPanel);
    }


}
