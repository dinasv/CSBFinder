package Core;

import Core.Genomes.COG;

import java.util.Map;

/**
 */
public class CogInfo {

    private Map<String, COG> cogInfo;

    public CogInfo(){
        this.cogInfo = null;
    }

    public boolean cogInfoExists(){
        return cogInfo != null;
    }

    public COG getCog(String cog_id){
        COG cog = null;
        if (cogInfo != null){
            cog = cogInfo.get(cog_id);
        }
        return cog;
    }

    public void setCogInfo(Map<String, COG> cog_info) {
        this.cogInfo = cog_info;
    }

    public Map<String, COG> getCogInfo() {
        return this.cogInfo;
    }
}
