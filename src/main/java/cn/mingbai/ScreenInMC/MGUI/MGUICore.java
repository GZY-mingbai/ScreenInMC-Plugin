package cn.mingbai.ScreenInMC.MGUI;

import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Utils.Utils;

import java.awt.geom.Rectangle2D;

public abstract class MGUICore extends Core {
    MContainer container;

    public MGUICore(String name) {
        super(name);
    }

    public abstract void onCreate(MContainer container);

    @Override
    public void onCreate() {
        this.container = new MContainer(getScreen());
        onCreate(container);
        container.load();
    }

    public void crash() {
        container.crash();
    }


    @Override
    public void onUnload() {
        container.unload();
    }

    @Override
    public void onMouseClick(int x, int y, Utils.MouseClickType type) {
        container.clickAt(x, y, ClickType.Left);
    }

    @Override
    public void onTextInput(String text) {
        container.inputText(text);
    }

    public MContainer getContainer() {
        return container;
    }

    @Override
    public void reRender() {
        if(container!=null){
            container.addReRender(new Rectangle2D.Double(0,0,container.getWidth(),container.getHeight()));
            container.reRender();
        }
    }
}
