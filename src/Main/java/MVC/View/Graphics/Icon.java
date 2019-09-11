package MVC.View.Graphics;

import javax.swing.*;

/**
 */
public enum Icon {

    QUESTION_MARK("/question.png", "question mark icon"),
    RUN("/right-arrow.png", "run icon"),
    CLUSTER("/network.png", "cluster icon"),
    SAVE("/save.png", "save icon"),
    FILTER("/funnel.png", "include results icon"),
    RANK("/ranking.png", "rank icon"),
    ZOOM_OUT("/zoom-out.png", "zoom out icon"),
    ZOOM_IN("/zoom-in.png", "zoom out icon"),
    LOAD("/load.gif", "loading");

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
