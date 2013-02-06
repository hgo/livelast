package models;

import java.util.TreeMap;

public class ParameterBuilder {
    TreeMap<String, String> map;
    
    private ParameterBuilder() {
    }
    
    public static ParameterBuilder newInstance(){
        return new ParameterBuilder();
    }
    public ParameterBuilder add(String key, String val) {
        map.put(key, val);
        return this;
    }
    
    public TreeMap<String, String> build(){
        return map;
    }
}
