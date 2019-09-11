package MVC;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class ProgramProperties {

    private static final String PROPERTIES_FILE = "config.properties";
    public static final String SESSION_PROPERTY = "session";
    public static final String COG_INFO_PROPERTY = "orthology";
    public static final String TAXA_PROPERTY = "taxonomy";
    public static final String SHOW_SAVE_MSG_PROPERTY = "show_save_msg";

    private Properties properties;

    public ProgramProperties(){
        properties = new Properties();
    }

    public void loadProperties(){
        FileReader reader = null;
        try {
            reader = new FileReader(PROPERTIES_FILE);
        } catch (FileNotFoundException e) {
            //ignore
        }

        try {
            properties.load(reader);
        } catch (IOException e) {
            System.out.println("A problem occurred while reading " + PROPERTIES_FILE);
        }
    }

    private void addProperty(String key, String value){
        properties.setProperty(key, value);

        try {
            properties.store(new FileWriter(PROPERTIES_FILE), null);
        } catch (IOException e) {
            //skip
        }
    }

    public String getSessionPath(){
        return properties.getProperty(SESSION_PROPERTY, "");
    }

    public String getCogInfoPath(){
        return properties.getProperty(COG_INFO_PROPERTY, "");
    }

    public String getTaxaPath(){
        return properties.getProperty(TAXA_PROPERTY, "");
    }

    public void setShowSaveMsg(boolean value){
        addProperty(SHOW_SAVE_MSG_PROPERTY, Boolean.toString(value));
    }

    public boolean getShowSaveMsg(){
        String val = properties.getProperty(SHOW_SAVE_MSG_PROPERTY, "true");
        return Boolean.valueOf(val);
    }

}
