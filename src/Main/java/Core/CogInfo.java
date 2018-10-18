package Core;

import Genomes.COG;

import java.util.Map;

/**
 */
public class CogInfo {

    private Map<String, COG> cog_info;

    public CogInfo(){
        this.cog_info = null;
    }

    public boolean cogInfoExists(){
        return cog_info != null;
    }

    public COG getCog(String cog_id){
        COG cog = null;
        if (cog_info != null){
            cog = cog_info.get(cog_id);
        }
        return cog;
    }

    public void setCogInfo(Map<String, COG> cog_info) {
        this.cog_info = cog_info;
    }

    public Map<String, COG> getCogInfo() {
        return this.cog_info;
    }
}
