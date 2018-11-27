package MVC.View.Components.Panels;

import MVC.View.Listeners.ToggleCallBackListener;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class HiddenPanel extends JPanel {

    private JComponent component;
    private JLabel toggleBtn;
    private ToggleCallBackListener toggleCallBackListener;

    public static final String HIDE = " < ";
    public static final String SHOW = " > ";

    public HiddenPanel(JComponent component, String initialState) {
        this.component = component;
        initToggleButton();

        if (HIDE.equals(initialState)) {
            toggleBtn.setText(HIDE);
            this.component.setVisible(true);
        } else {
            toggleBtn.setText(SHOW);
        }

        setLayout(new BorderLayout());
        add(component, BorderLayout.CENTER);
        add(toggleBtn, BorderLayout.EAST);

        this.setMinimumSize(component.getSize());
    }

    public void toggle() {
        toggleButtonText();
        toggleComponent();
    }

    private void toggleButtonText() {
        toggleBtn.setText(HIDE.equals(toggleBtn.getText()) ? SHOW : HIDE);
    }

    private void toggleComponent() {
        component.setVisible(!component.isVisible());
    }

    public void setToggleCallBackListener(ToggleCallBackListener listener) {
        this.toggleCallBackListener = listener;
        toggleBtn.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggle();
                toggleCallBackListener.toggleOccurred();
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    private void initToggleButton() {
        toggleBtn = new JLabel();
        toggleBtn.setBackground(Color.GRAY);
        toggleBtn.setForeground(Color.white);
        toggleBtn.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        toggleBtn.setOpaque(true);
        toggleBtn.setFont(new Font(Font.SERIF, Font.BOLD, 16));
    }
}
