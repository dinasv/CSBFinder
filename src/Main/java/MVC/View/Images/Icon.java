package MVC.View.Images;

import javax.swing.*;

/**
 */
public enum Icon {

    QUESTION_MARK("/question.png", "question mark icon"),
    RUN("/right-arrow.png", "run icon"),
    FILTER("/funnel.png", "filter results icon");

    private ImageIcon icon;

    Icon(String path, String description){
        icon = createImageIcon(path, description);
    }

    public ImageIcon getIcon(){
        return icon;
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    private ImageIcon createImageIcon(String path,
                                        String description) {
        java.net.URL imgURL = getClass().getResource(path);

        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
}
