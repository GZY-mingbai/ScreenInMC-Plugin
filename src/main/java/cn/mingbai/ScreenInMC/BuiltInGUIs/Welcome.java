package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Cores.MGUICore;
import cn.mingbai.ScreenInMC.MGUI.MContainer;
import cn.mingbai.ScreenInMC.Utils.Utils;

import java.awt.*;

public class Welcome extends MGUICore {
    public Welcome() {
        super("Welcome");
    }

    @Override
    public void onCreate(MContainer container) {
        container.setBackground(Color.BLUE);
    }
}
