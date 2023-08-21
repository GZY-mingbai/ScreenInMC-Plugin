package cn.mingbai.ScreenInMC.Browsers;

import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.Utils;

import java.util.*;

public abstract class Browser {
    public static final int NOT_INSTALLED = 0;
    public static final int INSTALLED_NOT_LOADED = 1;
    public static final int LOADED = 2;
    private static List<Browser> allBrowsers = Collections.synchronizedList(new ArrayList<>());
    private String name;

    public Browser(String name) {
        this.name = name;
    }

    public static void addBrowser(Browser browser) {
        allBrowsers.add(browser);
    }


    public static Browser getBrowser(String name) {
        for (Browser i : allBrowsers) {
            if (i.name.equals(name)) {
                return i;
            }
        }
        return null;
    }

    public static Browser[] getAllBrowsers() {
        Browser[] browsers = new Browser[allBrowsers.size()];
        for (int i = 0; i < allBrowsers.size(); i++) {
            browsers[i] = allBrowsers.get(i);
        }
        return browsers;
    }

    public String getName() {
        return name;
    }
    public abstract static class BrowserCallback{
        protected abstract void handle(String message,float progress);
        protected abstract void complete();
    }

    public abstract void installCore(BrowserCallback callback);
    public abstract void uninstallCore(BrowserCallback callback);

    public abstract void loadCore();

    public abstract int getCoreState();

    public abstract void createBrowser(Screen screen, int width, int height,String defaultURI);

    public abstract void executeJavascript(Screen screen, String script);

    public abstract void destroyBrowser(Screen screen);

    public abstract void clickAt(Screen screen, int x, int y, Utils.MouseClickType type);

    public abstract void inputText(Screen screen, String text);

    public abstract Utils.Pair<Utils.Pair<Integer, Integer>, int[]> onRender(Screen screen);

    public abstract void unloadCore();
    public abstract void killCore();

    public abstract void openURL(Screen screen, String url);

    public abstract void refreshPage(Screen screen);
    public abstract String getNowURL(Screen screen);
    public abstract boolean isInDeveloperMode(Screen screen);
    public abstract void setDeveloperMode(Screen screen,boolean enable);
    public static abstract class RedstoneInputCallback{
        private static long finalCallbackID = 0;
        public long callbackId = 0;
        private Screen screen;
        private int id;
        public RedstoneInputCallback(Screen screen,int id){
            this.screen = screen;
            this.id = id;
            this.callbackId = finalCallbackID;
            finalCallbackID++;
        }

        public long getCallbackID() {
            return callbackId;
        }

        public Screen getScreen() {
            return screen;
        }

        public int getID() {
            return id;
        }

        public abstract void onInput(int value);
    }
    private static Map<Screen,List<RedstoneInputCallback>> redstoneInputListeners = new HashMap<>();
    protected static RedstoneInputCallback addRedstoneInputListener(RedstoneInputCallback callback){
        synchronized (redstoneInputListeners) {
            if (!redstoneInputListeners.containsKey(callback.screen)) {
                redstoneInputListeners.put(callback.screen, new ArrayList<>());
            }
            List<RedstoneInputCallback> list = redstoneInputListeners.get(callback.screen);
            list.add(callback);
            return callback;
        }
    }
    protected static void removeRedstoneInputListener(long callbackID) {
        synchronized (redstoneInputListeners){
            for(Screen i:redstoneInputListeners.keySet()){
                for(RedstoneInputCallback j:redstoneInputListeners.get(i)){
                    if(j.getCallbackID()==callbackID){
                        removeRedstoneInputListener(j);
                        return;
                    }
                }
            }
        }
    }
    protected static void removeRedstoneInputListener(RedstoneInputCallback callback){
        synchronized (redstoneInputListeners) {
            if (redstoneInputListeners.containsKey(callback.screen)) {
                List<RedstoneInputCallback> list = redstoneInputListeners.get(callback.screen);
                list.remove(callback);
                if (list.size() == 0) {
                    redstoneInputListeners.remove(callback.screen);
                }
            }
        }
    }

    public static Map<Screen, List<RedstoneInputCallback>> getRedstoneInputListenersMap() {
        HashMap map = new HashMap();
        for(Screen i:redstoneInputListeners.keySet()){
            map.put(i,redstoneInputListeners.get(i));
        }
        return map;
    }
}
