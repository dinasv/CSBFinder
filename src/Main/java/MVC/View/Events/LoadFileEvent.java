package MVC.View.Events;

import java.io.File;

public class LoadFileEvent {

    private File filePath;

    public File getFilePath() {
        return filePath;
    }

    public void setFilePath(File filePath) {
        this.filePath = filePath;
    }

    public LoadFileEvent(Object source, File file) {
        this.filePath = file;
    }


}
