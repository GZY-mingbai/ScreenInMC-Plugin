package cn.mingbai.ScreenInMC.Utils.JSONUtils;


import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JSONUtilsSimpleJSON extends JSONUtils{
    public static JSONUtils create() {
        return new JSONUtilsSimpleJSON();
    }
    private List toList(JSONArray array){
        List list = new ArrayList();
        for(int i=0;i<array.size();i++){
            try {
                if(array.getType(i)==null){
                    list.add(null);
                    continue;
                }
                if(array.getType(i).equals(JSONObject.class)){
                    list.add(i,toMap(array.getJSONObject(i)));
                    continue;
                }
                if(array.getType(i).equals(JSONArray.class)){
                    list.add(i,toList(array.getJSONArray(i)));
                    continue;
                }
                list.add(i,array.get(i));
            }catch (Exception e){
            }
        }
        return list;
    }
    private Map toMap(JSONObject obj){
        Map map = new LinkedHashMap();
        for(String i:obj.keySet()){
            try {
                if(obj.getType(i)==null){
                    map.put(i,null);
                    continue;
                }
                if(obj.getType(i).equals(JSONObject.class)){
                    map.put(i,toMap(obj.getJSONObject(i)));
                    continue;
                }
                if(obj.getType(i).equals(JSONArray.class)){
                    map.put(i,toList(obj.getJSONArray(i)));
                    continue;
                }
                map.put(i,obj.get(i));
            }catch (Exception e){
            }
        }
        return map;
    }
    private JSONObject fromSimpleObject(org.json.simple.JSONObject oldObj){
        JSONObject object = new JSONObject();
        for(Object i:oldObj.keySet()){
            if(i instanceof String){
                try {
                    String key = ((String) i);
                    Object value = oldObj.get(key);
                    if (value == null) {
                        object.setValueNull(key);
                        continue;
                    }
                    if(value instanceof Double){
                        object.setValue(key,((Number)value).doubleValue());
                    }
                    if(value instanceof Float){
                        object.setValue(key,((Number)value).floatValue());
                    }
                    if(value instanceof Integer){
                        object.setValue(key,((Number)value).intValue());
                    }
                    if(value instanceof Byte){
                        object.setValue(key,((Number)value).byteValue());
                    }
                    if(value instanceof Short){
                        object.setValue(key,((Number)value).shortValue());
                    }
                    if(value instanceof Long){
                        object.setValue(key,((Number)value).longValue());
                    }
                    if(value instanceof String){
                        object.setValue(key,(String) value);
                    }
                    if(value instanceof Boolean){
                        object.setValue(key,((Boolean) value).booleanValue());
                    }
                    if(value instanceof org.json.simple.JSONObject){
                        object.setValue(key,fromSimpleObject((org.json.simple.JSONObject) value));
                    }
                    if(value instanceof org.json.simple.JSONArray){
                        object.setValue(key,fromSimpleArray((org.json.simple.JSONArray) value));
                    }
                }catch (Exception e){
                }
            }
        }
        return object;
    }
    private JSONArray fromSimpleArray(org.json.simple.JSONArray oldObj){
        JSONArray object = new JSONArray();
        for(int i=0;i<oldObj.size();i++){
                try {
                    Object value = oldObj.get(i);
                    if (value == null) {
                        object.addNull();
                        continue;
                    }
                    if(value instanceof Double){
                        object.add(((Number)value).doubleValue());
                    }
                    if(value instanceof Float){
                        object.add(((Number)value).floatValue());
                    }
                    if(value instanceof Integer){
                        object.add(((Number)value).intValue());
                    }
                    if(value instanceof Byte){
                        object.add(((Number)value).byteValue());
                    }
                    if(value instanceof Short){
                        object.add(((Number)value).shortValue());
                    }
                    if(value instanceof Long){
                        object.add(((Number)value).longValue());
                    }
                    if(value instanceof String){
                        object.add((String) value);
                    }
                    if(value instanceof Boolean){
                        object.add(((Boolean) value).booleanValue());
                    }
                    if(value instanceof org.json.simple.JSONObject){
                        object.add(fromSimpleObject((org.json.simple.JSONObject) value));
                    }
                    if(value instanceof org.json.simple.JSONArray){
                        object.add(fromSimpleArray((org.json.simple.JSONArray) value));
                    }
                }catch (Exception e){
                }
        }
        return object;
    }

    @Override
    public String toJson(JSON obj) {
        if(obj.isArray()){
            return org.json.simple.JSONArray.toJSONString(toList((JSONArray) obj));
        }else{
            return org.json.simple.JSONObject.toJSONString(toMap((JSONObject) obj));
        }
    }

    @Override
    public JSON fromJson(String json) {
        try {
            Object obj = parser.parse(json);
            if(obj instanceof org.json.simple.JSONObject){
                return fromSimpleObject(((org.json.simple.JSONObject) obj));
            }
            if(obj instanceof org.json.simple.JSONArray){
                return fromSimpleArray(((org.json.simple.JSONArray) obj));
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private JSONParser parser;
    @Override
    protected void init() {
        parser = new JSONParser();
    }
}
