package cn.mingbai.ScreenInMC.Cores;

import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.MGUI.ClickType;
import cn.mingbai.ScreenInMC.MGUI.MContainer;

public abstract class MGUICore extends Core {
    MContainer container;

    public abstract void onCreate(MContainer container);

    @Override
    public void onCreate() {
        container = new MContainer(screen);
        onCreate(container);
    }

    @Override
    public void onMouseClick(int x, int y) {
        container.clickAt(x, y, ClickType.Left);
    }

    public MContainer getContainer() {
        return container;
    }
}
