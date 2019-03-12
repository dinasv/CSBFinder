package MVC;

import Model.Controller;
import MVC.Controller.CSBFinderController;

import javax.swing.*;

public class Runnable {

    public static void main(String[] args) {
        if (args.length > 0){
            Controller controller = new Controller(args);
        }else {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }

            SwingUtilities.invokeLater(() -> {
                CSBFinderController controller = new CSBFinderController();
            });
        }
    }
}
