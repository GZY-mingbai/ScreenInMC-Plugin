package cn.mingbai.ScreenInMC.Utils.JSONUtils;

import cn.mingbai.ScreenInMC.Screen.Screen;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public abstract class JSONUtils {
    private boolean pretty;
    public static JSONUtils create(){
        return create(false);
    }
    private static Method getCreateMethod(Class cls){
        Method createMethod;
        try {
            createMethod = cls.getDeclaredMethod("create");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return createMethod;
    }
    public static JSONUtils create(boolean pretty){
        JSONUtils jsonUtils=null;
        for(String className:new String[]{
                "JSONUtilsGSON",
                "JSONUtilsSimpleJSON"
        }){
            try{
                Method createMethod = getCreateMethod(Class.forName("cn.mingbai.ScreenInMC.Utils.JSONUtils."+className));
                jsonUtils = (JSONUtils) createMethod.invoke(null);
                jsonUtils.pretty=pretty;
                jsonUtils.init();
            }
            catch (Error e){}
            catch (RuntimeException e){}
            catch (Exception e){}
            catch (Throwable e){}
            if(jsonUtils!=null) return jsonUtils;
        }
        throw new RuntimeException("Not found JSON implementation.");
    }
    public abstract String toJson(JSON obj);
    public abstract JSON fromJson(String json);

    protected boolean isPretty() {
        return pretty;
    }
    protected abstract void init();
    public static class JSON{
        private JSON(boolean isArray){
            this.isArray = isArray;
        }
        public static JSON create(Object obj){
            if(obj.getClass().isArray()){
                return JSONArray.createJSONArray(obj);
            }
            return JSONObject.createJSONObject(obj);
        }
        public Object write(Class cls){
            return null;
        }
        private boolean isArray;
        public boolean isArray(){
            return isArray;
        }
    }
    public static class JSONObject extends JSON{
        private Map<String,Object> values = new LinkedHashMap<>();
        public JSONObject(){
            super(false);
        }
        public Set<String> keySet(){
            return values.keySet();
        }

        @Override
        public Object write(Class cls) {
            return writeJSONObject(cls);
        }

        protected static JSONObject createJSONObject(Object obj) {
            if(obj==null) return null;
            JSONObject json = new JSONObject();
            for(Field i:obj.getClass().getDeclaredFields()){
                Object value;
                try {
                    value=i.get(obj);
                }catch (Exception e){continue;}
                if(value==null) {
                    json.setValueNull(i.getName());
                    continue;
                }
                if(value.getClass().isArray()){
                    json.setValue(i.getName(), JSONArray.createJSONArray(value));
                    continue;
                }
                if(value.getClass().equals(int.class)){
                    json.setValue(i.getName(),(int)value);
                    continue;
                }
                if(value.getClass().equals(short.class)){
                    json.setValue(i.getName(),(short)value);
                    continue;
                }
                if(value.getClass().equals(double.class)){
                    json.setValue(i.getName(),(double)value);
                    continue;
                }
                if(value.getClass().equals(float.class)){
                    json.setValue(i.getName(),(float)value);
                    continue;
                }
                if(value.getClass().equals(long.class)){
                    json.setValue(i.getName(),(long)value);
                    continue;
                }
                if(value.getClass().equals(byte.class)){
                    json.setValue(i.getName(),(byte)value);
                    continue;
                }
                if(value.getClass().equals(boolean.class)){
                    json.setValue(i.getName(),(boolean)value);
                    continue;
                }
                if(value.getClass().equals(Integer.class)){
                    json.setValue(i.getName(),((Integer)value).intValue());
                    continue;
                }
                if(value.getClass().equals(Short.class)){
                    json.setValue(i.getName(),((Short)value).shortValue());
                    continue;
                }
                if(value.getClass().equals(Double.class)){
                    json.setValue(i.getName(),((Double)value).doubleValue());
                    continue;
                }
                if(value.getClass().equals(Float.class)){
                    json.setValue(i.getName(),((Float)value).floatValue());
                    continue;
                }
                if(value.getClass().equals(Long.class)){
                    json.setValue(i.getName(),((Long)value).longValue());
                    continue;
                }
                if(value.getClass().equals(Byte.class)){
                    json.setValue(i.getName(),((Byte)value).byteValue());
                    continue;
                }
                if(value.getClass().equals(Boolean.class)){
                    json.setValue(i.getName(),((Boolean)value).booleanValue());
                    continue;
                }
                if(value.getClass().equals(String.class)){
                    json.setValue(i.getName(),(String)value);
                    continue;
                }
                if(value.getClass().isEnum()){
                    json.setValue(i.getName(),((Enum)value).name());
                    continue;
                }
                if(Object.class.isAssignableFrom(value.getClass())){
                    json.setValue(i.getName(),JSONObject.createJSONObject(value));
                    continue;
                }
            }
            return json;
        }
        public Object writeJSONObject(Class cls) {
            Object obj;
            try {
                obj = cls.getDeclaredConstructor().newInstance();
            }catch (Exception e){
                throw new RuntimeException(e);
            }
            for(Field i:obj.getClass().getDeclaredFields()){
                if(containsKey(i.getName())){
                    try {
                        if(i.getType().equals(int.class) || i.getType().equals(Integer.class)){
                            i.set(obj,getInt(i.getName()));
                            continue;
                        }
                        if(i.getType().equals(short.class) || i.getType().equals(Short.class)){
                            i.set(obj,getShort(i.getName()));
                            continue;
                        }
                        if(i.getType().equals(double.class) || i.getType().equals(Double.class)){
                            i.set(obj,getDouble(i.getName()));
                            continue;
                        }
                        if(i.getType().equals(float.class) || i.getType().equals(Float.class)){
                            i.set(obj,getFloat(i.getName()));
                            continue;
                        }
                        if(i.getType().equals(long.class) || i.getType().equals(Long.class)){
                            i.set(obj,getLong(i.getName()));
                            continue;
                        }
                        if(i.getType().equals(byte.class) || i.getType().equals(Byte.class)){
                            i.set(obj,getByte(i.getName()));
                            continue;
                        }
                        if(i.getType().equals(boolean.class) || i.getType().equals(Boolean.class)){
                            i.set(obj,getBoolean(i.getName()));
                            continue;
                        }
                        if(i.getType().equals(String.class)){
                            i.set(obj,getString(i.getName()));
                            continue;
                        }
                        if(i.getType().isEnum()){
                            i.set(obj, Enum.valueOf((Class<Enum>)i.getType(),getString(i.getName())));
                            continue;
                        }
                        if(i.getType().isArray()){
                            i.set(obj,getJSONArray(i.getName()).writeJSONArray(i.getType().getComponentType()));
                            continue;
                        }
                        if(i.getType().equals(Object.class)){
                            i.set(obj,getJSONObject(i.getName()));
                            continue;
                        }
                        if(Object.class.isAssignableFrom(i.getType())){
                            i.set(obj,getJSONObject((i.getName())).writeJSONObject(i.getType()));
                            continue;
                        }
                    }catch (Exception e){

                    }
                }
            }
            return obj;
        }
        public boolean containsKey(String key){
            return values.containsKey(key);
        }
        public JSONObject(Map<String,Object> values){
            super(false);
            for(String key:values.keySet()){
                setValue(key,values.get(key));
            }
        }
        public int getInt(String key) throws Exception {
            Object value = values.get(key);
            if(value instanceof Number){
                return ((Number)value).intValue();
            }
            throw new Exception();
        }
        public int getShort(String key) throws Exception {
            Object value = values.get(key);
            if(value instanceof Number){
                return ((Number)value).shortValue();
            }
            throw new Exception();
        }
        public byte getByte(String key) throws Exception {
            Object value = values.get(key);
            if(value instanceof Number){
                return ((Number)value).byteValue();
            }
            throw new Exception();
        }
        public double getDouble(String key) throws Exception {
            Object value = values.get(key);
            if(value instanceof Number){
                return ((Number)value).doubleValue();
            }
            throw new Exception();
        }
        public float getFloat(String key) throws Exception {
            Object value = values.get(key);
            if(value instanceof Number){
                return ((Number)value).floatValue();
            }
            throw new Exception();
        }
        public boolean getBoolean(String key) throws Exception {
            Object value = values.get(key);
            if(value instanceof Boolean){
                return ((Boolean)value).booleanValue();
            }
            if(value instanceof Number){
                return ((Number)value).intValue()!=0;
            }
            throw new Exception();
        }
        public JSONObject getJSONObject(String key) throws Exception {
            Object value = values.get(key);
            if(value==null) return null;
            if(value instanceof JSONObject){
                return ((JSONObject)value);
            }
            throw new Exception();
        }
        public String getString(String key) throws Exception {
            Object value = values.get(key);
            if(value instanceof String){
                return ((String)value);
            }
            throw new Exception();
        }
        public Long getLong(String key) throws Exception {
            Object value = values.get(key);
            if(value instanceof Number){
                return ((Number)value).longValue();
            }
            throw new Exception();
        }
        public JSONArray getJSONArray(String key)  throws Exception {
            Object value = values.get(key);
            if(value==null)return new JSONArray();
            if(value instanceof JSONArray){
                return ((JSONArray)value);
            }
            throw new Exception();
        }
        public Object get(String key){
            return values.get(key);
        }
        public Class getType(String key){
            Object v = values.get(key);
            if(v==null){
                return null;
            }else
            return v.getClass();
        }
        public void setValue(String key,int value){
            setValue(key,(Object) value);
        }
        public void setValue(String key,double value){
            setValue(key,(Object) value);
        }
        public void setValue(String key,boolean value){
            setValue(key,(Object) value);
        }
        public void setValue(String key,float value){
            setValue(key,(Object) value);
        }
        public void setValue(String key,byte value){
            setValue(key,(Object) value);
        }
        public void setValue(String key,long value){
            setValue(key,(Object) value);
        }
        public void setValue(String key,short value){
            setValue(key,(Object) value);
        }
        public void setValue(String key,String value){
            setValue(key,(Object) value);
        }
        public void setValue(String key,JSONObject value){
            setValue(key,(Object) value);
        }
        public void setValue(String key,JSONArray value){
            setValue(key,(Object) value);
        }
        private void setValue(String key,Object value){
            values.put(key,value);
        }
        public void setValueNull(String key){
            setValue(key,(Void)null);
        }
    }
    public static class JSONArray extends JSON{
        @Override
        public Object write(Class cls) {
            return writeJSONArray(cls.getComponentType());
        }
        //if array is int[], cls = int
        protected Object writeJSONArray(Class cls){
            Object obj = Array.newInstance(cls,size());
            for(int i=0;i<Array.getLength(obj);i++){
                if(i<size()){
                    try {
                        if(cls.equals(int.class) || cls.equals(Integer.class)){
                            Array.set(obj,i,getInt(i));
                            continue;
                        }
                        if(cls.equals(short.class) || cls.equals(Short.class)){
                            Array.set(obj,i,getShort(i));
                            continue;
                        }
                        if(cls.equals(double.class) || cls.equals(Double.class)){
                            Array.set(obj,i,getDouble(i));
                            continue;
                        }
                        if(cls.equals(float.class) || cls.equals(Float.class)){
                            Array.set(obj,i,getFloat(i));
                            continue;
                        }
                        if(cls.equals(long.class) || cls.equals(Long.class)){
                            Array.set(obj,i,getLong(i));
                            continue;
                        }
                        if(cls.equals(byte.class) || cls.equals(Byte.class)){
                            Array.set(obj,i,getByte(i));
                            continue;
                        }
                        if(cls.equals(boolean.class) || cls.equals(Boolean.class)){
                            Array.set(obj,i,getBoolean(i));
                            continue;
                        }
                        if(cls.equals(String.class)){
                            Array.set(obj,i,getString(i));
                            continue;
                        }
                        if(cls.isEnum()){
                            Array.set(obj,i, Enum.valueOf((Class<Enum>)cls,getString(i)));
                            continue;
                        }
                        if(cls.isArray()){
                            Array.set(obj,i,getJSONArray(i).writeJSONArray(cls.getComponentType()));
                            continue;
                        }
                        if(cls.equals(Object.class)){
                            Array.set(obj,i,getJSONObject(i));
                            continue;
                        }
                        if(Object.class.isAssignableFrom(cls)){
                            Array.set(obj,i,getJSONObject(i).writeJSONObject(cls));
                            continue;
                        }
                    }catch (Exception e){

                    }
                }
            }
            return obj;
        }
        protected static JSONArray createJSONArray(Object obj){
            if(obj==null) return null;
            JSONArray array = new JSONArray();
            for(int i=0;i< Array.getLength(obj);i++){
                Object value = Array.get(obj,i);
                if(value==null) {
                    array.addNull();
                    continue;
                };
                if(value.getClass().isArray()){
                    array.add(createJSONArray(value));
                    continue;
                }
                if(value.getClass().equals(int.class)){
                    array.add((int)value);
                    continue;
                }
                if(value.getClass().equals(short.class)){
                    array.add((short)value);
                    continue;
                }
                if(value.getClass().equals(double.class)){
                    array.add((double)value);
                    continue;
                }
                if(value.getClass().equals(float.class)){
                    array.add((float)value);
                    continue;
                }
                if(value.getClass().equals(long.class)){
                    array.add((long)value);
                    continue;
                }
                if(value.getClass().equals(byte.class)){
                    array.add((byte)value);
                    continue;
                }
                if(value.getClass().equals(boolean.class)){
                    array.add((boolean)value);
                    continue;
                }
                if(value.getClass().equals(Integer.class)){
                    array.add(((Integer)value).intValue());
                    continue;
                }
                if(value.getClass().equals(Short.class)){
                    array.add(((Short)value).shortValue());
                    continue;
                }
                if(value.getClass().equals(Double.class)){
                    array.add(((Double)value).doubleValue());
                    continue;
                }
                if(value.getClass().equals(Float.class)){
                    array.add(((Float)value).floatValue());
                    continue;
                }
                if(value.getClass().equals(Long.class)){
                    array.add(((Long)value).longValue());
                    continue;
                }
                if(value.getClass().equals(Byte.class)){
                    array.add(((Byte)value).byteValue());
                    continue;
                }
                if(value.getClass().equals(Boolean.class)){
                    array.add(((Boolean)value).booleanValue());
                    continue;
                }
                if(value.getClass().equals(String.class)){
                    array.add((String)value);
                    continue;
                }
                if(value.getClass().isEnum()){
                    array.add(((Enum)value).name());
                    continue;
                }
                if(Object.class.isAssignableFrom(value.getClass())){
                    array.add(JSONObject.createJSONObject(value));
                    continue;
                }
            }
            return array;
        }
        public JSONArray(){
            super(true);
        }
        private ArrayList<Object> objects = new ArrayList<>();
        public Class getType(int index){
            Object v = objects.get(index);
            if(v==null){
                return null;
            }else
                return v.getClass();
        }
        public int size(){
            return objects.size();
        }
        public Object get(int index){
            return objects.get(index);
        }
        public int getInt(int index) throws Exception {
            Object value = objects.get(index);
            if(value instanceof Number){
                return ((Number)value).intValue();
            }
            throw new Exception();
        }
        public int getShort(int index) throws Exception {
            Object value = objects.get(index);
            if(value instanceof Number){
                return ((Number)value).shortValue();
            }
            throw new Exception();
        }
        public byte getByte(int index) throws Exception {
            Object value = objects.get(index);
            if(value instanceof Number){
                return ((Number)value).byteValue();
            }
            throw new Exception();
        }
        public double getDouble(int index) throws Exception {
            Object value = objects.get(index);
            if(value instanceof Number){
                return ((Number)value).doubleValue();
            }
            throw new Exception();
        }
        public float getFloat(int index) throws Exception {
            Object value = objects.get(index);
            if(value instanceof Number){
                return ((Number)value).floatValue();
            }
            throw new Exception();
        }
        public boolean getBoolean(int index) throws Exception {
            Object value = objects.get(index);
            if(value instanceof Boolean){
                return ((Boolean)value).booleanValue();
            }
            if(value instanceof Number){
                return ((Number)value).intValue()!=0;
            }
            throw new Exception();
        }
        public JSONObject getJSONObject(int index) throws Exception {
            Object value = objects.get(index);
            if(value==null) return null;
            if(value instanceof JSONObject){
                return ((JSONObject)value);
            }
            throw new Exception();
        }
        public String getString(int index) throws Exception {
            Object value = objects.get(index);
            if(value instanceof String){
                return ((String)value);
            }
            throw new Exception();
        }
        public Long getLong(int index) throws Exception {
            Object value = objects.get(index);
            if(value instanceof Number){
                return ((Number)value).longValue();
            }
            throw new Exception();
        }
        public JSONArray getJSONArray(int index)  throws Exception {
            Object value = objects.get(index);
            if(value==null)return new JSONArray();
            if(value instanceof JSONArray){
                return ((JSONArray)value);
            }
            throw new Exception();
        }
        public void add(int value){
            add((Object) value);
        }
        public void add(short value){
            add((Object) value);
        }
        public void add(double value){
            add((Object) value);
        }
        public void add(boolean value){
            add((Object) value);
        }
        public void add(float value){
            add((Object) value);
        }
        public void add(byte value){
            add((Object) value);
        }
        public void add(long value){
            add((Object) value);
        }
        public void add(String value){
            add((Object) value);
        }
        public void add(JSONObject value){
            add((Object) value);
        }
        public void add(JSONArray value){
            add((Object) value);
        }
        public void addNull(){
            add((Void)null);
        }
        private void add(Object value){
            objects.add(value);
        }
    }
}
