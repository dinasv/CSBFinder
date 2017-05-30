package Main;

import Utils.COG;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Boris on 19/05/2017.
 */
public class MotifReader {

    public MotifReader(){

    }

    /**
     * Read COG_INFO_TABLE.txt and fill cog_info. For each cog that is used in our data, save information of functional category
     * @throws FileNotFoundException
     */
    public HashMap<String, COG> read_cog_info_table(HashMap<String, Integer> cog_to_index) throws FileNotFoundException {
        HashMap<String, COG> cog_info = new HashMap<>();

        BufferedReader br = new BufferedReader(new FileReader("input/COG_INFO_TABLE.txt"));
        try {

            int counter = 0;
            String line = br.readLine();
            while (line != null) {

                String[] cog_line = line.split(";");

                String cog_id = cog_line[0];
                cog_id = cog_id.substring(3);

                if (cog_to_index.containsKey(cog_id)) {
                    String letters = cog_line[1];

                    int index = 2;
                    String char_letters = ""+letters.charAt(0);
                    String fun_cats = cog_line[index++];
                    String letter_descs = cog_line[index++].replace("/", "_");
                    letter_descs = letter_descs.replace(":", "");
                    for (int i = 1; i < letters.length(); i++) {
                        char_letters += letters.charAt(i);
                        fun_cats += "_OR_" + cog_line[index++];
                        letter_descs += "_OR_" + cog_line[index++].replace("/", "_").replace(":", "");
                    }

                    String sub_cat_desc = cog_line[index];
                    String cog_type = "";

                    COG cog = new COG(cog_id, char_letters, fun_cats, letter_descs, sub_cat_desc, cog_type);
                    cog_info.put(cog_id, cog);
                    counter ++;
                }else{
                    //System.out.println(cog_id);
                    //System.out.println("COGs with info: " + counter);
                }
                line = br.readLine();

            }
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return  cog_info;
    }
}
