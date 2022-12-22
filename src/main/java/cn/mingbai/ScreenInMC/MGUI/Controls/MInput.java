package cn.mingbai.ScreenInMC.MGUI.Controls;

import cn.mingbai.ScreenInMC.MGUI.MRenderer;
import org.bukkit.Bukkit;

import java.awt.*;

public class MInput extends MTextBlock{
    @Override
    public void onRender(MRenderer mRenderer) {
        mRenderer.setStroke(new BasicStroke(1));
        Bukkit.broadcastMessage(""+isActive());
        if(isActive()){
            mRenderer.setPaint(new Color(150,150,150));
        }else{
            mRenderer.setPaint(new Color(255,255,255));
        }
        mRenderer.drawRect(0,0, (int) getWidth(), (int) getHeight(),true);
        mRenderer.setPaint(new Color(0,0,0));
        mRenderer.drawRect(0,0, (int) getWidth(), (int) getHeight(),false);
        super.onRender(mRenderer);
    }

    @Override
    public void onActive() {
        super.onActive();
        reRender();
    }

    @Override
    public void onPassive() {
        super.onPassive();
        reRender();
    }

    @Override
    public void onTextInput(String text) {
        super.onTextInput(text);
        setText(text);
    }
}
