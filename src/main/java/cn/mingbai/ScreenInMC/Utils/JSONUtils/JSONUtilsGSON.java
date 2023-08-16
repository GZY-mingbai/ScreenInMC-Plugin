package cn.mingbai.ScreenInMC.Utils.JSONUtils;

import com.google.gson.*;

import java.util.Map;

public class JSONUtilsGSON extends JSONUtils{
    private Gson gson;
    public static JSONUtils create() {
        return new JSONUtilsGSON();
    }
    private static JsonArray toGsonJsonArray(JSONArray oldObj) {
        JsonArray object = new JsonArray();
        for(int i=0;i<oldObj.size();i++){
            try {
                if(oldObj.getType(i)==null){
                    continue;
                }
                if (oldObj.getType(i).equals(Integer.class)) {
                    object.add(oldObj.getInt(i));
                    continue;
                }
                if (oldObj.getType(i).equals(Short.class)) {
                    object.add(oldObj.getShort(i));
                    continue;
                }
                if (oldObj.getType(i).equals(Double.class)) {
                    object.add(oldObj.getDouble(i));
                    continue;
                }
                if (oldObj.getType(i).equals(Float.class)) {
                    object.add(oldObj.getFloat(i));
                    continue;
                }
                if (oldObj.getType(i).equals(Long.class)) {
                    object.add(oldObj.getLong(i));
                    continue;
                }
                if (oldObj.getType(i).equals(Byte.class)) {
                    object.add(oldObj.getByte(i));
                    continue;
                }
                if (oldObj.getType(i).equals(Boolean.class)) {
                    object.add(oldObj.getBoolean(i));
                    continue;
                }
                if (oldObj.getType(i).equals(String.class)) {
                    object.add(oldObj.getString(i));
                    continue;
                }
                if (oldObj.getType(i).equals(JSONObject.class)) {
                    object.add(toGsonJsonObject(oldObj.getJSONObject(i)));
                }
                if (oldObj.getType(i).equals(JSONArray.class)) {
                    object.add(toGsonJsonArray(oldObj.getJSONArray(i)));
                }
            }catch (Exception e){

            }
        }
        return object;
    }
    private static JsonObject toGsonJsonObject(JSONObject oldObj){
        JsonObject object = new JsonObject();
        for(String i: oldObj.keySet()){
            try {
                if(oldObj.getType(i)==null){
                    continue;
                }
                if (oldObj.getType(i).equals(Integer.class)) {
                    object.addProperty(i, oldObj.getInt(i));
                    continue;
                }
                if (oldObj.getType(i).equals(Short.class)) {
                    object.addProperty(i, oldObj.getShort(i));
                    continue;
                }
                if (oldObj.getType(i).equals(Double.class)) {
                    object.addProperty(i, oldObj.getDouble(i));
                    continue;
                }
                if (oldObj.getType(i).equals(Float.class)) {
                    object.addProperty(i, oldObj.getFloat(i));
                    continue;
                }
                if (oldObj.getType(i).equals(Long.class)) {
                    object.addProperty(i, oldObj.getLong(i));
                    continue;
                }
                if (oldObj.getType(i).equals(Byte.class)) {
                    object.addProperty(i, oldObj.getByte(i));
                    continue;
                }
                if (oldObj.getType(i).equals(Boolean.class)) {
                    object.addProperty(i, oldObj.getBoolean(i));
                    continue;
                }
                if (oldObj.getType(i).equals(String.class)) {
                    object.addProperty(i, oldObj.getString(i));
                    continue;
                }
                if (oldObj.getType(i).equals(JSONObject.class)) {
                    object.add(i, toGsonJsonObject(oldObj.getJSONObject(i)));
                }
                if (oldObj.getType(i).equals(JSONArray.class)) {
                    object.add(i, toGsonJsonArray(oldObj.getJSONArray(i)));
                }
            }catch (Exception e){

            }
        }
        return object;
    }
    private static JSONArray fromGsonJsonArray(JsonArray oldObj) {
        JSONArray object = new JSONArray();
        for(int i=0;i<oldObj.size();i++){
            try {
                if(oldObj.get(i).isJsonNull()){
                    object.addNull();
                    continue;
                }
                if(oldObj.get(i).isJsonArray()){
                    object.add(fromGsonJsonArray(oldObj.get(i).getAsJsonArray()));
                    continue;
                }
                if(oldObj.get(i).isJsonObject()){
                    object.add(fromGsonJsonObject(oldObj.get(i).getAsJsonObject()));
                    continue;
                }
                if(oldObj.get(i).isJsonPrimitive()){
                    JsonPrimitive primitive = oldObj.get(i).getAsJsonPrimitive();
                    if(primitive.isBoolean()){
                        object.add(primitive.getAsBoolean());
                    }
                    if(primitive.isNumber()){
                        Number number = primitive.getAsNumber();
                        if(number instanceof Double){
                            object.add(number.doubleValue());
                        }
                        if(number instanceof Float){
                            object.add(number.floatValue());
                        }
                        if(number instanceof Integer){
                            object.add(number.intValue());
                        }
                        if(number instanceof Byte){
                            object.add(number.byteValue());
                        }
                        if(number instanceof Short){
                            object.add(number.shortValue());
                        }
                        if(number instanceof Long){
                            object.add(number.longValue());
                        }
                    }
                    if(primitive.isString()){
                        object.add(primitive.getAsString());
                    }
                }

            }catch (Exception e){

            }
        }
        return object;
    }
    private static JSONObject fromGsonJsonObject(JsonObject oldObj){
        JSONObject object = new JSONObject();
        for(Map.Entry<String,JsonElement> entry : oldObj.entrySet()){
            try {
                if(entry.getValue().isJsonNull()){
                    object.setValueNull(entry.getKey());
                    continue;
                }
                if(entry.getValue().isJsonArray()){
                    object.setValue(entry.getKey(),fromGsonJsonArray(entry.getValue().getAsJsonArray()));
                    continue;
                }
                if(entry.getValue().isJsonObject()){
                    object.setValue(entry.getKey(),fromGsonJsonObject(entry.getValue().getAsJsonObject()));
                    continue;
                }
                if(entry.getValue().isJsonPrimitive()){
                    JsonPrimitive primitive = entry.getValue().getAsJsonPrimitive();
                    if(primitive.isBoolean()){
                        object.setValue(entry.getKey(),primitive.getAsBoolean());
                    }
                    if(primitive.isNumber()){
                        Number number = primitive.getAsNumber();
                        if(number instanceof Double){
                            object.setValue(entry.getKey(),number.doubleValue());
                        }else
                        if(number instanceof Float){
                            object.setValue(entry.getKey(),number.floatValue());
                        }else
                        if(number instanceof Integer){
                            object.setValue(entry.getKey(),number.intValue());
                        }else
                        if(number instanceof Byte){
                            object.setValue(entry.getKey(),number.byteValue());
                        }else
                        if(number instanceof Short){
                            object.setValue(entry.getKey(),number.shortValue());
                        }else
                        if(number instanceof Long){
                            object.setValue(entry.getKey(),number.longValue());
                        }else
                            object.setValue(entry.getKey(),number.doubleValue());
                    }
                    if(primitive.isString()){
                        object.setValue(entry.getKey(),primitive.getAsString());
                    }
                }

            }catch (Exception e){

            }
        }
        return object;
    }

    @Override
    public String toJson(JSON obj) {
        if(obj.isArray()){
            JSONArray oldObj = ((JSONArray) obj);
            return gson.toJson(toGsonJsonArray(oldObj));
        }else{
            JSONObject oldObj = ((JSONObject) obj);
            return gson.toJson(toGsonJsonObject(oldObj));
        }
    }

    @Override
    public JSON fromJson(String json) {
        JsonElement element = gson.fromJson(json, JsonElement.class);
        if(element.isJsonObject()){
            return fromGsonJsonObject(element.getAsJsonObject());
        }
        if(element.isJsonArray()){
            return fromGsonJsonArray(element.getAsJsonArray());
        }
        return new JSONObject();
    }


    @Override
    protected void init() {
        if(isPretty()){
            gson =  new GsonBuilder().setPrettyPrinting().create();
        }else{
            gson = new Gson();
        }
    }
}
