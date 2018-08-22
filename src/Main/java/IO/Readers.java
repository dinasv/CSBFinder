package IO;

import Utils.COG;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class Readers {

    /**
     * Read COG_INFO_TABLE.txt and fill cog_info. For each cog that is used in our data,
     * save information of functional category
     * @throws FileNotFoundException
     */
    public static Map<String, COG> read_cog_info_table(String cog_info_file_name) {
        Map<String, COG> cog_info = new HashMap<>();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(cog_info_file_name));
            String line = br.readLine();
            while (line != null) {

                String[] cog_line = line.split(";");
                if (cog_line.length > 0) {
                    String cog_id = cog_line[0];
                    if (cog_line.length > 1) {
                        String cog_desc = cog_line[1];
                        if (cog_line.length > 2) {
                            String[] functional_letters = cog_line[2].split(",");
                            String[] functional_categories = new String[functional_letters.length];
                            if (cog_line.length > 2 + functional_letters.length) {
                                for (int i = 0; i < functional_letters.length; i++) {
                                    functional_categories[i] = cog_line[3 + i];
                                }
                                COG cog = new COG(cog_id, cog_desc, functional_letters, functional_categories);
                                cog_info.put(cog_id, cog);
                            }

                        }else{
                            COG cog = new COG(cog_id, cog_desc, new String[0], new String[0]);
                            cog_info.put(cog_id, cog);
                        }
                    }
                }
                line = br.readLine();
            }
            br.close();

        } catch (IOException e) {
            System.out.println("File " + cog_info_file_name + " was not found");
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return  cog_info;
    }
}
