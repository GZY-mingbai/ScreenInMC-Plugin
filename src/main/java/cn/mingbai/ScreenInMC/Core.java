package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Screen.Screen;

public abstract class Core {
    protected Screen screen;
    public void create(Screen screen){
        this.screen=screen;
        onCreate();
    }
    public abstract void onCreate();
    protected void drawRGBPixels(byte[][] pixels){

    }
}
