package IO;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Used for logging
 */
public class MyLogger {
    private Logger logger;
    private boolean debug;

    public MyLogger(String path, boolean debug){
        this.debug = debug;
        //create logger file
        try {
            logger = java.util.logging.Logger.getLogger("MyLog");
            LogManager.getLogManager().reset();//disable logging information printed to screen

            FileHandler fh = new FileHandler(path + "MainAlgorithm.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

        } catch (Exception e) {
            System.out.println("An exception occurred while trying to create a log file");
            logger = null;
        }
    }

    public void writeLogger(String msg){
        if (debug && logger != null){
            logger.info(msg);
        }
    }
}
