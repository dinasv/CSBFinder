package MVC;

import MVC.Controller.CSBFinderController;

import javax.swing.*;

public class Runnable {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CSBFinderController controller = new CSBFinderController();
        });
    }
}
