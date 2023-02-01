package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Browsers.Browser;
import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Utils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import com.google.gson.internal.LinkedTreeMap;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.image.BufferedImage;

public class WebBrowser extends Core {
    public WebBrowser() {
        super("WebBrowser");
    }
    private Browser browser=null;
    private BukkitRunnable renderRunnable=null;
    @Override
    public void onCreate() {
        try {
            Object data = getStoredData();
            if(data!=null){
                LinkedTreeMap map = (LinkedTreeMap)data;
                String browserName = (String) map.get("browser");
                browser = Browser.getBrowser(browserName);
                if(browser.getCoreState()!=Browser.LOADED){
                    try{
                        browser.loadCore();
                    }catch (Exception e){}
                }
                if(browser.getCoreState()==Browser.LOADED){
                    browser.createBrowser(getScreen(),getScreen().getWidth()*128,getScreen().getHeight()*128);
                    renderRunnable= new BukkitRunnable() {
                        @Override
                        public void run() {
                            while (!this.isCancelled()&&browser!=null){
                                long startTime = System.currentTimeMillis();
                                BufferedImage image = browser.onRender(getScreen());
                                byte[] data = ImageUtils.imageToMapColors(image);
                                getScreen().sendView(data);
                                long waitTime = 50 - (System.currentTimeMillis()-startTime);
                                if(waitTime>0){
                                    try {
                                        Thread.sleep(waitTime);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }
                    };
                    return;
                }
            }
        }catch (Exception e){
            onUnload();
            e.printStackTrace();
        }
    }

    @Override
    public void onUnload() {
        browser=null;
        if(renderRunnable!=null){
            renderRunnable.cancel();
        }
    }

    @Override
    public void onMouseClick(int x, int y, Utils.MouseClickType type) {
        if(browser!=null){
            browser.clickAt(getScreen(),x,y);
        }
    }

    @Override
    public void onTextInput(String text) {
        if(browser!=null){
            browser.inputText(getScreen(),text);
        }
    }
}
