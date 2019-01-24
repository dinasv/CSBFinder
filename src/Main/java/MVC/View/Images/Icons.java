package MVC.View.Images;

import javax.swing.*;

/**
 */
public class Icons {

    private static final String QUESION_MARK_PATH = "/question.png";
    private static final String QUESION_MARK_DESC = "question mark icon";
    private ImageIcon questionMark;

    private static final String RUN_ICON_PATH = "/right-arrow.png";
    private static final String RUN_ICON_DESC = "run icon";
    private ImageIcon runIcon;

    public Icons(){
        questionMark = createImageIcon(QUESION_MARK_PATH, QUESION_MARK_DESC);
        runIcon = createImageIcon(RUN_ICON_PATH, RUN_ICON_DESC);
    }

    public ImageIcon getQuestionMark(){
        return questionMark;
    }

    public ImageIcon getRunIcon(){
        return runIcon;
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
