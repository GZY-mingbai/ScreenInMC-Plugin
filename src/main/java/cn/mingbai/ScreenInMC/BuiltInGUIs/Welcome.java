package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.MGUI.Alignment;
import cn.mingbai.ScreenInMC.MGUI.Controls.MTextBlock;
import cn.mingbai.ScreenInMC.MGUI.MContainer;
import cn.mingbai.ScreenInMC.MGUI.MGUICore;

import java.awt.*;

public class Welcome extends MGUICore {
    public Welcome() {
        super("Welcome");
    }

    @Override
    public void onCreate(MContainer container) {
        container.setBackground(new Color(255, 255, 255));
        MTextBlock textBlock = new MTextBlock("请在服务器关闭后修改ScreenInMC/screens.json");
        textBlock.setVerticalAlignment(Alignment.VerticalAlignment.Stretch);
        textBlock.setHorizontalAlignment(Alignment.HorizontalAlignment.Stretch);
        container.addChildControl(textBlock);
    }
}
