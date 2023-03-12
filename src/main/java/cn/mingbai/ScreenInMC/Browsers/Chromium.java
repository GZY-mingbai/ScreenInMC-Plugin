package cn.mingbai.ScreenInMC.Browsers;

import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.FileUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import com.google.gson.Gson;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

public class Chromium extends Browser {
    public static final String[] CHROMIUM_LIBRARIES;

    static {
        Utils.Pair<String, String> system = Utils.getSystem();
        String systemName = system.getKey() + "-" + system.getValue();
        CHROMIUM_LIBRARIES = new String[]{
                "gluegen-rt-natives-" + systemName + ".jar",
                "gluegen-rt.jar",
                "jcef-tests.jar",
                "jcef.jar",
                "jogl-all-natives-" + systemName + ".jar",
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
        String path2 = Main.PluginFilesPath + "Chromium/";
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
    public void loadCore() {
        if (getCoreState() == LOADED) {
            return;
        }
        try {
            Utils.loadLibrariesLoader();
            for (int i = 0; i < CHROMIUM_LIBRARIES.length; i++) {
                URL url = new File(Main.PluginFilesPath + "Chromium/bin/" + CHROMIUM_LIBRARIES[i]).toURI().toURL();
                Utils.loadJar(url);
            }
            Chromium_.load();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getCoreState() {
        if (new File(Main.PluginFilesPath + "Chromium/bin").exists()) {
            try {
                org.cef.CefApp.getState();
            } catch (Throwable e) {
                return INSTALLED_NOT_LOADED;
            }
            if (Chromium_.app == null) {
                return INSTALLED_NOT_LOADED;
            }
            return LOADED;
        } else {
            return NOT_INSTALLED;
        }
    }

    @Override
    public void createBrowser(Screen screen, int width, int height) {
        Chromium_.createBrowser(screen, width, height);
    }

    @Override
    public void executeJavascript(Screen screen, String script) {
        Chromium_.executeJavascript(screen, script);
    }

    @Override
    public void destroyBrowser(Screen screen) {
        Chromium_.destroyBrowser(screen);
    }

    @Override
    public void clickAt(Screen screen, int x, int y, Utils.MouseClickType type) {
        Chromium_.clickAt(screen, x, y, type);
    }

    @Override
    public void inputText(Screen screen, String text) {
        Chromium_.inputText(screen, text);
    }

    @Override
    public Utils.Pair<Utils.Pair<Integer, Integer>, int[]> onRender(Screen screen) {
        return Chromium_.onRender(screen);
    }

    @Override
    public void unloadCore() {
        Chromium_.unload();
    }

    @Override
    public void openURL(Screen screen, String url) {
        Chromium_.openURL(screen, url);
    }

    @Override
    public void refreshPage(Screen screen) {
        Chromium_.refreshPage(screen);
    }

    private static class Chromium_ {
        private static org.cef.CefApp app;
        private static org.cef.CefClient client;
        private static Map<Screen, org.cef.browser.ScreenInMCChromiumBrowser> clients = new HashMap<>();

        public static void openURL(Screen screen, String url) {
            org.cef.browser.CefBrowser browser = clients.get(screen);
            if (browser != null) {
                browser.loadURL(url);
            }
        }

        public static void refreshPage(Screen screen) {
            org.cef.browser.CefBrowser browser = clients.get(screen);
            if (browser != null) {
                browser.reload();
            }
        }

        private static void load() {
            Utils.Pair<String, String> system = Utils.getSystem();
            String systemName = "";
            switch (system.getKey()) {
                case "windows":
                    systemName = "win";
                    break;
                case "linux":
                    systemName = "linux";
                    break;
            }
            switch (system.getValue()) {
                case "amd64":
                case "arm64":
                    systemName += "64";
                    break;
                case "i386":
                case "arm":
                    systemName += "32";
                    break;
            }
            System.setProperty("java.library.path", System.getProperty("java.library.path") + ";" +
                    new File(Main.PluginFilesPath + "Chromium/bin/lib/" + systemName + "/").getAbsolutePath());
            cn.mingbai.ScreenInMC.Browsers.ChromiumLibrariesLoader.load(Main.PluginFilesPath, systemName, Utils.getLibraryPrefix(system.getKey()));
            org.cef.CefSettings settings = new org.cef.CefSettings();
            settings.windowless_rendering_enabled = true;
            String[] args = new String[]{};
            try {
                org.cef.CefApp.addAppHandler(new org.cef.handler.CefAppHandlerAdapter(args) {
                    @Override
                    public void stateHasChanged(org.cef.CefApp.CefAppState state) {
                        if (state == org.cef.CefApp.CefAppState.TERMINATED) {
                            app = null;
                            client = null;
                        }
                    }
                });
            } catch (Exception e) {
            }
//            try {
//                Field field = org.cef.CefApp.class.getDeclaredField("state_");
//                field.setAccessible(true);
//                field.set(null, org.cef.CefApp.CefAppState.NONE);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            try {
                app = org.cef.CefApp.getInstance(args, settings);
            } catch (Exception e) {
                app = org.cef.CefApp.getInstance();
            }
            client = app.createClient();
            client.addRequestHandler(new org.cef.handler.CefRequestHandlerAdapter() {
                @Override
                public boolean onOpenURLFromTab(org.cef.browser.CefBrowser browser, org.cef.browser.CefFrame frame, String target_url, boolean user_gesture) {
                    browser.loadURL(target_url);
                    return false;
                }
            });
            client.addLifeSpanHandler(new org.cef.handler.CefLifeSpanHandlerAdapter() {
                @Override
                public boolean onBeforePopup(org.cef.browser.CefBrowser browser, org.cef.browser.CefFrame frame, String target_url, String target_frame_name) {
                    browser.loadURL(target_url);
                    return false;
                }
            });
        }

        private static void createBrowser(Screen screen, int width, int height) {
            if (client != null && app != null) {
                org.cef.browser.ScreenInMCChromiumBrowser browser = new org.cef.browser.ScreenInMCChromiumBrowser(client, Main.getConfiguration().getString("download-browser-core.main-page"),
                        true);
                browser.createImmediately();
                browser.setSize(width, height);
                clients.put(screen, browser);
            }
        }

        private static void executeJavascript(Screen screen, String script) {
            org.cef.browser.CefBrowser browser = clients.get(screen);
            if (browser != null) {
                browser.executeJavaScript(script, browser.getURL(), 0);
            }
        }

        private static void destroyBrowser(Screen screen) {
            org.cef.browser.CefBrowser browser = clients.get(screen);
            if (browser != null) {
                browser.close(true);
                clients.remove(browser);
            }
        }

        private static void clickAt(Screen screen, int x, int y, Utils.MouseClickType type) {
            org.cef.browser.ScreenInMCChromiumBrowser browser = clients.get(screen);
            if (browser != null) {
                browser.clickAt(x, y, type.getCode());
            }
        }

        private static void inputText(Screen screen, String text) {
            org.cef.browser.ScreenInMCChromiumBrowser browser = clients.get(screen);
            if (browser != null) {
                browser.inputText(text);
            }
        }

        private static Utils.Pair<Utils.Pair<Integer, Integer>, int[]> onRender(Screen screen) {
            org.cef.browser.ScreenInMCChromiumBrowser browser = clients.get(screen);
            if (browser != null) {
                Utils.Pair image;
                try {
                    synchronized (browser) {
                        int[] newImage = new int[browser.imageWidth * browser.imageHeight];
                        for (int i = 0; i < newImage.length; i++) {
                            newImage[i] = browser.imageData[i * 4] & 0xFF |
                                    (browser.imageData[i * 4 + 1] & 0xFF) << 8 |
                                    (browser.imageData[i * 4 + 2] & 0xFF) << 16 |
                                    (browser.imageData[i * 4 + 3] & 0xFF) << 24;
                        }
                        image = new Utils.Pair<>(new Utils.Pair<>(browser.imageWidth, browser.imageHeight), newImage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    image = new Utils.Pair(new Utils.Pair<>(0, 0), new int[0]);
                }
                return image;
            }
            return null;
        }

        private static void unload() {
            try {
                client.dispose();
            } catch (Exception e) {
            }
//            try {
//                app.dispose();
//            }catch (Exception e){}
        }
    }
}
