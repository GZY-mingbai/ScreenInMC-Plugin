package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Cores.MGUICore;
import cn.mingbai.ScreenInMC.MGUI.Alignment;
import cn.mingbai.ScreenInMC.MGUI.ClickType;
import cn.mingbai.ScreenInMC.MGUI.Controls.MButton;
import cn.mingbai.ScreenInMC.MGUI.Controls.MTextBlock;
import cn.mingbai.ScreenInMC.MGUI.MContainer;

import java.awt.*;

import static java.awt.Font.PLAIN;

public class testMGUI extends MGUICore {
    int i = 0;
    MTextBlock textBlock_;

    @Override
    public void onCreate(MContainer container) {
        container.setBackground(new Color(255, 255, 255));
        MButton button = new MButton1("HelloWorld");
        button.setHeight(128);
        button.setWidth(256);
        button.setClipToBounds(false);
        button.setBackground(new Color(0, 103, 192));
        button.setForeground(new Color(255, 255, 255));
        button.setFont(new Font("微软雅黑", PLAIN, 20));
        button.setHorizontalAlignment(Alignment.HorizontalAlignment.Center);
        button.setVerticalAlignment(Alignment.VerticalAlignment.Center);
        container.addChildControl(button);
        textBlock_ = new MTextBlock("你按了0次按钮awa");
        textBlock_.setClipToBounds(false);
        textBlock_.setLeft(384);
        textBlock_.setTop(32);
        textBlock_.setFont(new Font("微软雅黑", PLAIN, 20));
        getContainer().addChildControl(textBlock_);
    }

    public class MButton1 extends MButton {
        public MButton1(String text) {
            super(text);
        }

        @Override
        public void onClick(int x, int y, ClickType type) {
            super.onClick(x, y, type);
            MTextBlock textBlock = new MTextBlock("你按了" + (i + 1) + "次按钮awa");
            textBlock.setClipToBounds(false);
            textBlock.setLeft(128);
            textBlock.setTop(32 + i * 32);
            textBlock.setFont(new Font("微软雅黑", PLAIN, 20));
            getContainer().addChildControl(textBlock);
            textBlock_.setText("你按了" + (i + 1) + "次按钮awa");
            i++;
        }
    }
}
