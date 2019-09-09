package MVC.View.Events;

import java.io.File;

public class LoadFileEvent implements Event{

    private File filePath;

    public File getFile() {
        return filePath;
    }

    public void setFilePath(File filePath) {
        this.filePath = filePath;
    }

    public LoadFileEvent(Object source, File file) {
        this.filePath = file;
    }


}
