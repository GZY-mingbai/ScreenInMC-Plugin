package cn.mingbai.ScreenInMC.MGUI.Controls;

import cn.mingbai.ScreenInMC.MGUI.MControl;
import cn.mingbai.ScreenInMC.MGUI.MRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MImage extends MControl {
    private BufferedImage image;
    private Image resizedImage;
    private int scaleMode = Image.SCALE_DEFAULT;

    public MImage() {

    }

    public MImage(BufferedImage image) {
        this.image = image;
    }

    public int getScaleMode() {
        return scaleMode;
    }

    public synchronized void setScaleMode(int scaleMode) {
        this.scaleMode = scaleMode;
        refreshImage();
        reRender();
    }

    public BufferedImage getImage() {
        return image;
    }

    public synchronized void setImage(BufferedImage image) {
        this.image = image;
        refreshImage();
        reRender();
    }

    @Override
    public void onResize() {
        super.onResize();
        refreshImage();
    }

    private void refreshImage() {
        if (getWidth() <= 0 || getHeight() <= 0 || (image.getWidth() == (int) getWidth() && image.getHeight() == (int) getHeight())) {
            resizedImage = image;
        } else {
            resizedImage = image.getScaledInstance((int) getWidth(), (int) getHeight(), scaleMode);
        }
    }

    @Override
    public void onRender(MRenderer mRenderer) {
        renderBackground(mRenderer);
        mRenderer.drawImage(resizedImage, 0, 0);
        renderChildren(mRenderer);
    }
}
