package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Browsers.Browser;
import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import com.google.gson.internal.LinkedTreeMap;
import org.bukkit.scheduler.BukkitRunnable;

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

    @Override
    public void onCreate() {
        try {
            Object data = getStoredData();
            if (data != null) {
                LinkedTreeMap map = (LinkedTreeMap) data;
                String browserName = (String) map.get("browser");
                browser = Browser.getBrowser(browserName);
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
        } catch (Exception e) {
            onUnload();
            e.printStackTrace();
        }
    }

    @Override
    public void onUnload() {
        browser.destroyBrowser(getScreen());
        browser = null;
        if (renderRunnable != null) {
            renderRunnable.cancel();
        }
        unloaded = true;
    }

    @Override
    public void onMouseClick(int x, int y, Utils.MouseClickType type) {
        if (browser != null) {
            browser.clickAt(getScreen(), x, y, type);
        }
    }

    @Override
    public void onTextInput(String text) {
        if (browser != null) {
            browser.inputText(getScreen(), text);
        }
    }
}
