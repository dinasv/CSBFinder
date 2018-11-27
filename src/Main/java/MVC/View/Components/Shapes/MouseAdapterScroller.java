package MVC.View.Components.Shapes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 */
public class MouseAdapterScroller extends MouseAdapter {
    private Point origin;
    private JPanel panel;

    public MouseAdapterScroller(JPanel panel){
        this.panel = panel;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        origin = new Point(e.getPoint());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (origin != null) {
            JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, panel);
            if (viewPort != null) {
                int deltaX = origin.x - e.getX();
                int deltaY = origin.y - e.getY();

                Rectangle view = viewPort.getViewRect();
                view.x += deltaX;
                view.y += deltaY;

                panel.scrollRectToVisible(view);
            }
        }
    }
}
