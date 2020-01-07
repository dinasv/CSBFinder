package MVC;

import Model.Controller;
import MVC.Controller.CSBFinderController;

import javax.swing.*;

public class Runnable {

    public static void main(String[] args) {
        if (args.length > 0){
            Controller controller = new Controller(args);
        }else {

            SwingUtilities.invokeLater(() -> {
                CSBFinderController controller = new CSBFinderController();
            });
        }
    }
}
