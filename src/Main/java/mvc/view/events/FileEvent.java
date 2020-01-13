package mvc.view.events;

import java.io.File;

public class FileEvent implements Event{

    private File file;

    public FileEvent(Object source, File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }



}
