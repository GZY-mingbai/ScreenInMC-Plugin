package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.Utils;

public abstract class Core {
    protected Screen screen;

    public void create(Screen screen) {
        this.screen = screen;
        screen.setCore(this);
        onCreate();
    }

    public abstract void onCreate();

    public abstract void onMouseClick(int x, int y, Utils.MouseClickType type);
    public abstract void onTextInput(String text);
}
