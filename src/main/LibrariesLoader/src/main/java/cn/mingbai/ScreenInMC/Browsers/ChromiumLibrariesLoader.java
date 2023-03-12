package cn.mingbai.ScreenInMC.Browsers;

import java.io.File;

public class ChromiumLibrariesLoader {
    public static void load(String pluginFilesPath, String systemName, String prefix) {
        org.cef.SystemBootstrap.setLoader(new org.cef.SystemBootstrap.Loader() {
            @Override
            public synchronized void loadLibrary(String libname) {
                try {
                    System.loadLibrary(libname);
                } catch (Throwable e) {
                    try {
                        File path = new File(pluginFilesPath + "Chromium/bin/lib/" + systemName + "/" + libname + prefix);
                        System.load(path.getAbsolutePath());
//                        System.out.println("!!!Loaded Library: " + path.getAbsolutePath());
                    } catch (Throwable er) {
                        er.printStackTrace();
                    }
                }
            }
        });
    }
}
