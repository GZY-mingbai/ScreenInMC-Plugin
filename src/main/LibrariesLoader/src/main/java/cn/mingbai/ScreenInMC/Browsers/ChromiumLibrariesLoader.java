package cn.mingbai.ScreenInMC.Browsers;

import org.cef.CefApp;
import org.cef.CefSettings;
import org.cef.SystemBootstrap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.cef.CefAppHelper.clearSelf;
import static org.cef.CefAppHelper.setState;

public class ChromiumLibrariesLoader {
    private static String libPath = "";
    private static String chromiumPath = "";
    private static String serverFilesPath = "";
    public static void load(String pluginFilesPath, String systemName, String prefix, boolean addLib) {
        serverFilesPath = (new File(pluginFilesPath+"../../")).getAbsolutePath().replace("\\","/");
        if(serverFilesPath.endsWith("/")){
            serverFilesPath = serverFilesPath.substring(0,serverFilesPath.length()-1);
        }
        libPath = new File(pluginFilesPath + "Chromium/bin/lib/" + systemName + "/").getAbsolutePath().replace("\\","/");
        if(libPath.endsWith("/")){
            libPath = libPath.substring(0,libPath.length()-1);
        }
        chromiumPath = new File(pluginFilesPath + "Chromium/").getAbsolutePath().replace("\\","/");
        if(chromiumPath.endsWith("/")){
            chromiumPath = chromiumPath.substring(0,chromiumPath.length()-1);
        }
        org.cef.SystemBootstrap.setLoader(new org.cef.SystemBootstrap.Loader() {
            @Override
            public synchronized void loadLibrary(String libname) {
                try {
                    File path = new File(pluginFilesPath + "Chromium/bin/lib/" + systemName + "/"+ (addLib?"lib":"") + libname + prefix);
                    System.load(path.getAbsolutePath());
                    System.out.println("!!!Loaded Library: " + path.getAbsolutePath());
                } catch (Throwable e) {
                    e.printStackTrace();
                    try {
                    System.loadLibrary(libname);
                    } catch (Throwable er) {
                        er.printStackTrace();
                    }
                }
            }
        });
    }
    public static void linkJoglLibraries(){
        for(File i:new File(chromiumPath+"/bin/").listFiles()){
            if(i.isFile()){
                if(i.getName().startsWith("gluegen-rt-natives")||
                    i.getName().startsWith("jogl-all-natives")
                ){
                    if(i.getName().endsWith(".jar")){
                        try {
                            JarFile jar = new JarFile(i);
                            for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
                                JarEntry entry = (JarEntry) enums.nextElement();
                                if(entry.getName().startsWith("natives")){
                                    try {
                                        String fileName = entry.getName();
                                        File f = new File(serverFilesPath+"/"+fileName);
                                        if (fileName.endsWith("/")) {
                                            f.mkdirs();
                                        }else{
                                            Path path = f.toPath();
                                            path = path.getParent();
                                            path.toFile().mkdirs();
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                            for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
                                JarEntry entry = (JarEntry) enums.nextElement();
                                if(entry.getName().startsWith("natives")) {
                                    try {
                                        String fileName = entry.getName();
                                        File f = new File(serverFilesPath+"/"+fileName);
                                        if (!fileName.endsWith("/")) {
                                            InputStream is = jar.getInputStream(entry);
                                            FileOutputStream fos = new FileOutputStream(f);
                                            while (is.available() > 0) {
                                                fos.write(is.read());
                                            }
                                            fos.close();
                                            is.close();
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    public static void setPermissions(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    setPermissions(file);
                }else{
                    try {
                        file.setExecutable(true,false);
                        file.setReadable(true,false);
                        file.setWritable(true,false);
                    }catch (Exception e){
                    }
                }
            }
        }
    }
    public static void linkJdkLibrary(String filename){
        try {
            String javaHome = System.getProperty("java.home").replace("\\","/");
            if(javaHome.endsWith("/")){
                javaHome = javaHome.substring(0,javaHome.length()-1);
            }
            File src1 = new File(javaHome+"/lib/"+filename);
            File src2 = new File(javaHome+"/bin/"+filename);
            File src = null;
            if(src1.exists()){
                src = src1;
            }else if(src2.exists()){
                src = src2;
            }
            if(src==null){
                System.out.println("File "+src.getAbsolutePath()+" not found.");
                return;
            }
            File dest = new File(libPath+"/"+filename);
            System.out.println("Linking "+src.getAbsolutePath()+" to "+dest.getAbsolutePath()+" .");
            Files.createSymbolicLink(
                    dest.toPath(),
                    src.toPath()
            );
        }catch (Throwable e){
        }
    }
    public static void loadLinuxLibraries(){
        setPermissions(new File(chromiumPath));
        try {
            SystemBootstrap.loadLibrary("jcef");
        }catch (Error e){
        }
        catch (Throwable e){
        }
    }
    public static void resetState(){
        setState(CefApp.CefAppState.NONE);
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
