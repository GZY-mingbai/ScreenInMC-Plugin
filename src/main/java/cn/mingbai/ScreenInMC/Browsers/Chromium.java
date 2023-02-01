package cn.mingbai.ScreenInMC.Browsers;

import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.FileUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import com.google.gson.Gson;
import org.cef.SystemBootstrap;
import sun.misc.Unsafe;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Logger;

public class Chromium extends Browser {
    private static class Chromium_{
        private static void load(){
            Utils.Pair<String,String> system = Utils.getSystem();
            String systemName = "";
            switch (system.getKey()){
                case "windows":
                    systemName="win";
                    break;
                case "linux":
                    systemName="linux";
                    break;
            }
            switch (system.getValue()){
                case "amd64":
                case "arm64":
                    systemName+="64";
                    break;
                case "i386":
                case "arm":
                    systemName+="32";
                    break;
            }
            String finalSystemName = systemName;
            System.setProperty("java.library.path",System.getProperty("java.library.path")+";"+
                    new File(Main.PluginFilesPath + "Chromium/bin/lib/" + finalSystemName +"/").getAbsolutePath());
            org.cef.SystemBootstrap.setLoader(new SystemBootstrap.Loader() {
                @Override
                public synchronized void loadLibrary(String libname) {
                    try {
                        System.loadLibrary(libname);
                    } catch (Throwable e) {
                        try {
                            String path = new File(Main.PluginFilesPath + "Chromium/bin/lib/" + finalSystemName + "/" + libname + Utils.getLibraryPrefix(system.getKey())).getAbsolutePath();
                            System.load(path);
                        } catch (Throwable er) {
                            er.printStackTrace();
                        }
                    }
                }
            });
            org.cef.CefSettings settings = new org.cef.CefSettings();
            settings.windowless_rendering_enabled = true;
            String[] args = new String[]{};
            try {
                org.cef.CefApp.addAppHandler(new org.cef.handler.CefAppHandlerAdapter(args) {
                    @Override
                    public void stateHasChanged(org.cef.CefApp.CefAppState state) {
                        if (state == org.cef.CefApp.CefAppState.TERMINATED) {
                            app=null;
                            client=null;
                        }
                    }
                });
            }catch (Exception e){}
            try{
                app = org.cef.CefApp.getInstance(args,settings);
            }catch (Exception e){
                app = org.cef.CefApp.getInstance();
            }
            client = app.createClient();
        }
        private static org.cef.CefApp app;
        private static org.cef.CefClient client;
        private static Map<Screen, org.cef.browser.CefBrowser> clients = new HashMap<>();
        private static void createBrowser(Screen screen,int width,int height){
            if(clients.get(clients)!=null && app!=null){
                org.cef.browser.CefBrowser browser = client.createBrowser(Main.getConfiguration().getString("download-browser-core.main-page"),true,true);
                browser.getUIComponent().setSize(width,height);
                clients.put(screen,browser);
            }
        }
        private static void executeJavascript(Screen screen,String script){
            org.cef.browser.CefBrowser browser = clients.get(screen);
            if(browser!=null) {
                browser.executeJavaScript(script,browser.getURL(),0);
            }
        }
        private static void destroyBrowser(Screen screen){
            org.cef.browser.CefBrowser browser = clients.get(screen);
            if(browser!=null) {
                browser.close(true);
                clients.remove(browser);
            }
        }
        private static void clickAt(Screen screen,int x,int y){
            executeJavascript(screen,"document.elementFromPoint("+x+","+y+").click();");
        }
        private static void inputText(Screen screen,String text){
            executeJavascript(screen,
                    "var text = \""+text.replace("\"","\\\"")+"\";\n"+
                            "var dom = document.activeElement;\n" +
                            "var evt = new InputEvent('input', {\n" +
                            "    inputType: 'insertText',\n" +
                            "    data: text,\n" +
                            "    dataTransfer: null,\n" +
                            "    isComposing: false\n" +
                            "});\n" +
                            "dom.value = text;\n" +
                            "dom.dispatchEvent(evt);"
            );
        }
        private static BufferedImage onRender(Screen screen){
            org.cef.browser.CefBrowser browser = clients.get(screen);
            if(browser!=null) {
                CompletableFuture<BufferedImage> image = browser.createScreenshot(false);
                try {
                    return image.get();
                }catch (Exception e){
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        }
        private static void unload(){
            client.dispose();
            app.dispose();
        }
    }
    public static final String[] CHROMIUM_LIBRARIES;
    static {
        Utils.Pair<String, String> system = Utils.getSystem();
        String systemName = system.getKey()+"-"+system.getValue();
        CHROMIUM_LIBRARIES = new String[]{
                "gluegen-rt-natives-"+systemName+".jar",
                "gluegen-rt.jar",
                "jcef-tests.jar",
                "jcef.jar",
                "jogl-all-natives-"+systemName+".jar",
                "jogl-all.jar"
        };
    }

    public Chromium() {
        super("Chromium");
    }

    @Override
    public void installCore() {
        Logger logger = Main.getPluginLogger();
        logger.info(LangUtils.getText("start-download-chromium-core"));
        int type = Main.getConfiguration().getInt("download-browser-core.jcef-download-url.type");
        String downloadUrl = "";
        String httpProxyUrl = Main.getConfiguration().getString("download-browser-core.http-proxy");
        switch (type) {
            case 0:
                downloadUrl = Main.getConfiguration().getString("download-browser-core.jcef-download-url.url");
                break;
            case 1:
                //https://github.com/jcefmaven/jcefbuild
                String githubProxyUrl = Main.getConfiguration().getString("download-browser-core.github-proxy");
                String repoUrl = Main.getConfiguration().getString("download-browser-core.jcef-download-url.url");
                String version = Main.getConfiguration().getString("download-browser-core.jcef-download-url.version");
                String systemType = Main.getConfiguration().getString("download-browser-core.jcef-download-url.system-type");
                String systemName;
                String systemArch;
                if (systemType.length() == 0) {
                    Utils.Pair<String, String> systemNameAndArch = Utils.getSystem();
                    systemName = systemNameAndArch.getKey();
                    systemArch = systemNameAndArch.getValue();
                } else {
                    String[] systemNameAndArch = systemType.split("-", 1);
                    systemName = systemNameAndArch[0];
                    systemArch = systemNameAndArch[1];
                }
                if (systemName == null) {
                    throw new RuntimeException("Current system is not supported: " + systemName + ".");
                }
                if (systemArch == null) {
                    throw new RuntimeException("Current arch is not supported: " + systemArch + ".");
                }
                if (version.length() == 0) {
                    String repoReleasesUrl = repoUrl.replace("github.com", "api.github.com/repos") + "/releases";
                    String repoReleasesString = FileUtils.getString(repoReleasesUrl, httpProxyUrl);
                    Gson gson = new Gson();
                    FileUtils.GithubReleasesObject[] repoReleasesObject = gson.fromJson(repoReleasesString, FileUtils.GithubReleasesObject[].class);
                    if (repoReleasesObject.length == 0) {
                        throw new RuntimeException("Get Github Repositories TagName failed.");
                    }
                    version = repoReleasesObject[0].tag_name;
                }
                downloadUrl = repoUrl + "/releases/download/" + version + "/" + systemName + "-" + systemArch + ".tar.gz";
                if (githubProxyUrl.length() != 0) {
                    downloadUrl = githubProxyUrl.replace("%URL%", downloadUrl);
                }
                break;
        }
        String progressText1 = LangUtils.getText("download-progress");
        String progressText2 = LangUtils.getText("decompress-progress");
        String path1 = Main.PluginFilesPath + "Temp/jcef.tar.gz";
        String path2 = Main.PluginFilesPath+"Chromium/";
        FileUtils.downloadFile(downloadUrl, path1, httpProxyUrl, new Function<Utils.Pair<Long, Long>, Void>() {
            long count = 0;

            @Override
            public Void apply(Utils.Pair<Long, Long> progress) {
                count++;
                if (count % 10 == 0) {
                    logger.info(progressText1.replace("%%", String.format("%.2f", (float) ((double) progress.getValue() /
                            (double) progress.getKey() * 100.0)) + "%"));
                }
                return null;
            }
        });
        logger.info(LangUtils.getText("download-success"));
        FileUtils.decompressTarGz(path1, path2, new Function<String, Void>() {
                @Override
                public Void apply(String name) {
                    logger.info(progressText2.replace("%%", name));
                    return null;
                }
            });
        logger.info(LangUtils.getText("decompress-success"));

    }

    @Override
    public void loadCore(){
        if(getCoreState()==LOADED){
            return;
        }
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if(loader instanceof URLClassLoader) {
                try {
                    Method method = URLClassLoader.class.getDeclaredMethod("addURL");
                    method.setAccessible(true);
                    for (int i = 0; i < CHROMIUM_LIBRARIES.length; i++) {
                        URL url = new File(Main.PluginFilesPath + "Chromium/bin/" + CHROMIUM_LIBRARIES[i]).toURI().toURL();
                        method.invoke(url);
                    }
                }catch (Throwable e){
                    try {
                        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                        unsafeField.setAccessible(true);
                        Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
                        Field field = URLClassLoader.class.getDeclaredField("ucp");
                        long offset = unsafe.objectFieldOffset(field);
                        Object ucp = unsafe.getObject(loader, offset);
                        field = ucp.getClass().getDeclaredField("unopenedUrls");
                        offset = unsafe.objectFieldOffset(field);
                        Collection<URL> unopenedURLs = (Collection<URL>) unsafe.getObject(ucp, offset);
                        field = ucp.getClass().getDeclaredField("path");
                        offset = unsafe.objectFieldOffset(field);
                        Collection<URL> pathURLs = (Collection<URL>) unsafe.getObject(ucp, offset);
                        for (int i = 0; i < CHROMIUM_LIBRARIES.length; i++) {
                            URL url = new File(Main.PluginFilesPath + "Chromium/bin/" + CHROMIUM_LIBRARIES[i]).toURI().toURL();
                            unopenedURLs.add(url);
                            pathURLs.add(url);
                        }
                    }catch (Exception er){
                        er.printStackTrace();
                        throw new Exception("Try to add argument: --illegal-access=permit");
                    }
                }
                Chromium_.load();
            }else{
                throw new Exception("ClassLoader is not URLClassLoader");
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    @Override
    public int getCoreState() {
        if(new File(Main.PluginFilesPath+"Chromium/bin").exists()){
            try {
                org.cef.CefApp.getState();
            }catch (Throwable e){
                return INSTALLED_NOT_LOADED;
            }
            if(Chromium_.app==null){
                return INSTALLED_NOT_LOADED;
            }
            return LOADED;
        }else{
            return NOT_INSTALLED;
        }
    }
    @Override
    public void createBrowser(Screen screen,int width,int height) {
        Chromium_.createBrowser(screen,width,height);
    }
    @Override
    public void executeJavascript(Screen screen,String script) {
        Chromium_.executeJavascript(screen,script);
    }

    @Override
    public void destroyBrowser(Screen screen) {
        Chromium_.destroyBrowser(screen);
    }

    @Override
    public void clickAt(Screen screen, int x, int y) {
        Chromium_.clickAt(screen,x,y);
    }

    @Override
    public void inputText(Screen screen, String text) {
        Chromium_.inputText(screen,text);
    }
    @Override
    public BufferedImage onRender(Screen screen){
        return Chromium_.onRender(screen);
    }

    @Override
    public void unloadCore() {
        Chromium_.unload();
    }
}
