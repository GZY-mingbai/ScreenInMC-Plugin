package cn.mingbai.ScreenInMC.MGUI.Controls;

import cn.mingbai.ScreenInMC.MGUI.MRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MButtonWithProgressBar extends MButton{
    public MButtonWithProgressBar() {
        super();
    }

    public MButtonWithProgressBar(String text) {
        super(text);
    }
    private float progress = 0.0f;

    public void setProgress(float progress) {
        this.progress = progress;
        reRender();
    }

    public float getProgress() {
        return progress;
    }

    @Override
    public void onRender(MRenderer mRenderer) {
        super.onRender(mRenderer);
        int width = (int) (getWidth()*progress);
        if(width>0) {
            if (getWidth() > 0 && getHeight() >= 0) {
                BufferedImage image = new BufferedImage((int) getWidth(), (int) getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = image.createGraphics();
                g2d.setPaint(new Color(0, 0, 0, 40));
                g2d.fillRoundRect(0,0,(int) getWidth(), (int) getHeight(), (int) getHeight(), (int) getHeight());
                g2d.dispose();
                image = image.getSubimage(0,0,width,image.getHeight());
                mRenderer.drawImage(image,0,0);
            }
        }
    }
}
