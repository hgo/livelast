package controllers.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import play.Logger;
import play.libs.Codec;

public class LastFmParameterBuilder {
    public final static String api_key = "ef1b8ec486144479aea70cc1bb73a7d5";
    public final static String api_secret = "96c36d6fb5bd26c2b7ed4a3e2b4b04bf";
    TreeMap<String, String> map = new TreeMap<String, String>();
    
    private LastFmParameterBuilder() {
    }
    
    public static LastFmParameterBuilder newInstance(){
        return new LastFmParameterBuilder();
    }
    
    public LastFmParameterBuilder add(String key, String val) {
        map.put(key, val);
        return this;
    }
    
    public Map<String, Object> build(boolean withApiKey){
        if(withApiKey){
            map.put("api_key", api_key);
        }
        String api_sig = api_sig(map);
        HashMap<String, Object> pMap = new HashMap<String, Object>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            pMap.put(entry.getKey(), entry.getValue());
        }
        pMap.put("api_sig", api_sig);
        return pMap;
    }
    
    public static String api_sig(TreeMap<String, String> orderedMap){
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry: orderedMap.entrySet()) {
            if(entry.getKey().equalsIgnoreCase("callback") || entry.getKey().equalsIgnoreCase("format")){
                continue;
            }
            Logger.info("enty - val -> %s", entry.toString());
            builder.append(entry.getKey()).append(entry.getValue());
        }
        String format = builder.append(api_secret).toString();
        Logger.info("before hash = %s", format);
        String api_sig = Codec.hexMD5(format);
        Logger.info("api_sig = "+api_sig);
        return api_sig;
    }
}
