package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Browsers.Browser;
import cn.mingbai.ScreenInMC.Controller.EditGUI;
import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import com.google.gson.internal.LinkedTreeMap;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class WebBrowser extends Core {
    private Browser browser = null;
    private BukkitRunnable renderRunnable = null;
    private boolean unloaded = false;

    public WebBrowser() {
        super("WebBrowser");
    }

    public Browser getBrowser() {
        return browser;
    }
    private WebBrowserStoredData data = new WebBrowserStoredData();
    public static class WebBrowserStoredData {
        public String browser;
        public String uri;
    }

    public WebBrowserStoredData getData() {
        return data;
    }

    @Override
    public void onCreate() {
        try {
            LinkedTreeMap map = (LinkedTreeMap) getStoredData();
            if(map==null){
                return;
            }
            data.browser = (String) map.get("browser");
            if(data.browser==null){
                return;
            }
            browser = Browser.getBrowser(data.browser);
            loadBrowser();
        } catch (Exception e) {
            onUnload();
            e.printStackTrace();
        }
    }
    private void loadBrowser(){
        if(browser.getCoreState()==Browser.NOT_INSTALLED){
            Main.getPluginLogger().warning("Can't use "+browser.getName()+" browser because it is not installed.");
            return;
        }
        if (browser.getCoreState() != Browser.LOADED) {
            try {
                browser.loadCore();
            } catch (Exception e) {
            }
        }
        if (browser.getCoreState() == Browser.LOADED) {
            browser.createBrowser(getScreen(), getScreen().getWidth() * 128, getScreen().getHeight() * 128);
            renderRunnable = new BukkitRunnable() {
                @Override
                public void run() {
                    while (!this.isCancelled() && browser != null && !unloaded) {
                        long startTime = System.currentTimeMillis();
                        Utils.Pair<Utils.Pair<Integer, Integer>, int[]> image = browser.onRender(getScreen());
                        if (image.getValue().length != 0) {
                            byte[] data = ImageUtils.imageToMapColors(image.getValue(), image.getKey().getKey(), image.getKey().getValue());
                            getScreen().sendView(data);
                        }
                        long waitTime = 50 - (System.currentTimeMillis() - startTime);
                        if (waitTime > 0) {
                            try {
                                Thread.sleep(waitTime);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            };
            renderRunnable.runTaskAsynchronously(Main.thisPlugin());
            return;
        }
    }
    @Override
    public void onUnload() {
        if (browser != null) {
            if (browser.getCoreState() == Browser.LOADED) {
                browser.destroyBrowser(getScreen());
                browser = null;
                if (renderRunnable != null) {
                    renderRunnable.cancel();
                }
            }
        }
        unloaded = true;
    }

    @Override
    public void onMouseClick(int x, int y, Utils.MouseClickType type) {
        if (browser != null) {
            if(browser.getCoreState()==Browser.LOADED) {
                browser.clickAt(getScreen(), x, y, type);
            }
        }
    }

    @Override
    public void onTextInput(String text) {
        if (browser != null) {
            if(browser.getCoreState()==Browser.LOADED) {
                browser.inputText(getScreen(), text);
            }
        }
    }
    public static class BrowserInstalledCoresList implements EditGUI.EditGUICoreInfo.EditGUICoreSettingsList {
        private static List<Browser> getInstalledBrowserList(){
            List<Browser> browsers = new ArrayList<>();
            for(Browser i:Browser.getAllBrowsers()){
                if(i.getCoreState()!=Browser.NOT_INSTALLED){
                    browsers.add(i);
                }
            }
            return browsers;
        }
        @Override
        public String[] getList() {
            List<Browser> list = getInstalledBrowserList();
            String[] array = new String[list.size()];
            for(int i=0;i<list.size();i++){
                array[i]=list.get(i).getName();
            }
            return array;
        }

    }
    @Override
    public void addToEditGUI() {
        EditGUI.registerCoreInfo(new EditGUI.EditGUICoreInfo(
                "@controller-editor-cores-browser-name",
                this,
                "@controller-editor-cores-browser-details",
                "green",
                Material.GLASS,
                new LinkedHashMap<>(){
                    {
                        put("@controller-editor-cores-browser-uri", String.class);
                        put("@controller-editor-cores-browser-refresh", Boolean.class);
                        put("@controller-editor-cores-browser-core", BrowserInstalledCoresList.class);
                    }
                }));
    }

    @Override
    public Object getEditGUISettingValue(String name) {
        switch (name){
            case "@controller-editor-cores-browser-uri":
                if(browser!=null){
                    return browser.getNowURL(getScreen());
                }
                return "";
            case "@controller-editor-cores-browser-refresh":
                return false;
            case "@controller-editor-cores-browser-core":
                if(browser!=null){
                    List<Browser> list = BrowserInstalledCoresList.getInstalledBrowserList();
                    for(int i=0;i<list.size();i++){
                        if(list.get(i).getName().equals(browser.getName())){
                            return i;
                        }
                    }
                }
                return -1;
        }
        return null;
    }

    @Override
    public void setEditGUISettingValue(String name, Object value) {
        switch (name){
            case "@controller-editor-cores-browser-uri":
                if(browser!=null){
                    browser.openURL(getScreen(),(String) value);
                }
                break;
            case "@controller-editor-cores-browser-refresh":
                if(browser!=null&&value.equals(true)){
                    browser.refreshPage(getScreen());
                }
                break;
            case "@controller-editor-cores-browser-core":
                Browser newBrowser = Browser.getBrowser(new BrowserInstalledCoresList().getList()[(int) value]);
                data.browser=newBrowser.getName();
                setStoredData(data);
                if(browser!=null) {
                    browser.destroyBrowser(getScreen());
                }
                browser = newBrowser;
                loadBrowser();
                break;
        }

    }
}
