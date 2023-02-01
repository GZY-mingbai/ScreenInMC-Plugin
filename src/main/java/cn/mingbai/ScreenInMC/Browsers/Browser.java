package cn.mingbai.ScreenInMC.Browsers;

import cn.mingbai.ScreenInMC.Screen.Screen;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class Browser {
    private static List<Browser> allBrowsers = Collections.synchronizedList(new ArrayList<>());
    private String name;
    public static void addBrowser(Browser browser){
        allBrowsers.add(browser);
    }
    public static Browser getBrowser(String name){
        for(Browser i:allBrowsers){
            if(i.name.equals(name)){
                return i;
            }
        }
        return null;
    }
    public static Browser[] getAllBrowsers(){
        Browser[] browsers = new Browser[allBrowsers.size()];
        for(int i=0;i<allBrowsers.size();i++){
            browsers[i] = allBrowsers.get(i);
        }
        return browsers;
    }
    public Browser(String name) {
        this.name=name;
    }

    public String getName() {
        return name;
    }
    public static final int NOT_INSTALLED = 0;
    public static final int INSTALLED_NOT_LOADED = 1;
    public static final int LOADED = 2;
    public abstract void installCore();
    public abstract void loadCore();
    public abstract int getCoreState();
    public abstract void createBrowser(Screen screen,int width,int height);
    public abstract void executeJavascript(Screen screen,String script);
    public abstract void destroyBrowser(Screen screen);
    public abstract void clickAt(Screen screen,int x,int y);
    public abstract void inputText(Screen screen,String text);
    public abstract BufferedImage onRender(Screen screen);
    public abstract void unloadCore();

}
