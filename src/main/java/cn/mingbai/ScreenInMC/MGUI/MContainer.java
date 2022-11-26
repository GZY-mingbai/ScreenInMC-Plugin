package cn.mingbai.ScreenInMC.MGUI;

import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.ImageUtils;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;

public class MContainer extends MControl {
    BufferedImage image;
    private final Screen screen;
    private final Graphics2D graphics;
    private final FontRenderContext frc;
    private boolean canClick = true;

    public MContainer(Screen screen) {
        this.screen = screen;
        this.setWidth(screen.getWidth() * 128);
        this.setHeight(screen.getHeight() * 128);
        image = new BufferedImage((int) this.getWidth(), (int) this.getHeight(), BufferedImage.TYPE_INT_ARGB);
        graphics = (Graphics2D) image.getGraphics();
        frc = graphics.getFontRenderContext();
    }

    public Screen getScreen() {
        return screen;
    }

    @Override
    public void reRender() {
        if (graphics == null) {
            return;
        }
        graphics.setPaint(new Color(0, 0, 0, 0));
        graphics.setStroke(new BasicStroke(1));
        graphics.setClip(0, 0, image.getWidth(), image.getHeight());
        graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
        MRenderer renderer = new MRenderer(this, graphics, frc);
        renderer.setControl(this);
        onRender(renderer);
        screen.sendView(ImageUtils.imageToMapColors(image));
    }

    public void clickAt(int x, int y, ClickType type) {
        if (canClick) {
            canClick = false;
            try {
                for (MControl i : getAllChildMControls()) {
                    double left = i.getAbsoluteLeft();
                    double top = i.getAbsoluteTop();
                    if (i.isVisible() && left < x && (left + i.getWidth()) > x && top < y && (top + i.getHeight()) > y) {
                        i.onClick((int) (x - left), (int) (y - top), type);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            canClick = true;
        }
    }
}
