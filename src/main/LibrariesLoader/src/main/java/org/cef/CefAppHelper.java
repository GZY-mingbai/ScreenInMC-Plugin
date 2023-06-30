package org.cef;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class CefAppHelper {
    public static void setState(CefApp.CefAppState state){
        try {
            Method method = CefApp.class.getDeclaredMethod("setState", CefApp.CefAppState.class);
            method.setAccessible(true);
            method.invoke(null,state);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void clearSelf(){
        try {
            Field field = CefApp.class.getDeclaredField("self");
            field.setAccessible(true);
            field.set(null,null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
