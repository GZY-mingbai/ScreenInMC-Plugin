package cn.mingbai.ScreenInMC.Browsers;

import org.cef.CefApp;
import org.cef.CefSettings;

import java.io.File;

import static org.cef.CefAppHelper.clearSelf;
import static org.cef.CefAppHelper.setState;

public class ChromiumLibrariesLoader {
    public static void load(String pluginFilesPath, String systemName, String prefix) {
        org.cef.SystemBootstrap.setLoader(new org.cef.SystemBootstrap.Loader() {
            @Override
            public synchronized void loadLibrary(String libname) {
                try {
                        File path = new File(pluginFilesPath + "Chromium/bin/lib/" + systemName + "/" + libname + prefix);
                        System.load(path.getAbsolutePath());
//                        System.out.println("!!!Loaded Library: " + path.getAbsolutePath())
                } catch (Throwable e) {
                    try {
                    System.loadLibrary(libname);
                    } catch (Throwable er) {
                        er.printStackTrace();
                    }
                }
            }
        });
    }
    public static CefApp getApp(){
//        setState(CefApp.CefAppState.NONE);
//        clearSelf();
        return org.cef.CefApp.getInstance();
    }
    public static CefApp.CefAppState getState(){
        return CefApp.getState();
    }
}
