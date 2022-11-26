package cn.mingbai.ScreenInMC.MGUI;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;

public class MRenderer {
    private MControl mControl;
    private final Graphics2D graphics;
    private final MContainer mContainer;
    private final FontRenderContext frc;

    protected MRenderer(MContainer mContainer, Graphics2D graphics, FontRenderContext frc) {
        this.mContainer = mContainer;
        this.graphics = graphics;
        this.frc = frc;
    }

    @Override
    protected Object clone() {
        return new MRenderer(mContainer, graphics, frc);
    }

    protected void setControl(MControl mControl) {
        this.mControl = mControl;
    }

    private void setClip() {
        if (mControl.isClipToBounds()) {
            graphics.setClip((int) mControl.getAbsoluteLeft(), (int) mControl.getAbsoluteTop(), (int) mControl.getWidth(), (int) mControl.getHeight());
        } else {
            graphics.setClip((int) mContainer.getAbsoluteLeft(), (int) mContainer.getAbsoluteTop(), (int) mContainer.getWidth(), (int) mContainer.getHeight());
        }
    }

    public void drawPixel(int x, int y) {
        if (mControl.isVisibleActually()) {
            setClip();
            graphics.fillRect((int) (mControl.getAbsoluteLeft() + x), (int) (mControl.getAbsoluteTop() + y), 1, 1);
        }
    }

    public void drawRect(int x, int y, int width, int height, boolean fill) {
        if (mControl.isVisibleActually()) {
            setClip();
            if (fill) {
                graphics.fillRect((int) (mControl.getAbsoluteLeft() + x), (int) (mControl.getAbsoluteTop() + y), width, height);
            } else {
                graphics.drawRect((int) (mControl.getAbsoluteLeft() + x), (int) (mControl.getAbsoluteTop() + y), width, height);
            }
        }
    }

    public void setPaint(Paint paint) {
        graphics.setPaint(paint);
    }

    public void setFont(Font font) {
        graphics.setFont(font);
    }

    public void setStroke(Stroke stroke) {
        graphics.setStroke(stroke);
    }

    public void drawText(String text, int x, int y) {
        if (mControl.isVisibleActually()) {
            setClip();
            LineMetrics metrics = graphics.getFont().getLineMetrics(text, null);
            graphics.drawString(text, (int) (mControl.getAbsoluteLeft() + x), (int) (mControl.getAbsoluteTop() + y + metrics.getAscent()));
        }
    }

    public void drawTextCenter(String text, int width, int height) {
        if (mControl.isVisibleActually()) {
            setClip();
            LineMetrics metrics = graphics.getFont().getLineMetrics(text, frc);
            Rectangle2D rectangle = graphics.getFont().getStringBounds(text, frc);
            int left = (int) ((width - rectangle.getWidth()) / 2);
            int top = (int) ((int) ((height - rectangle.getHeight()) / 2) + metrics.getAscent());
            graphics.drawString(text, (int) (mControl.getAbsoluteLeft() + left), (int) (mControl.getAbsoluteTop() + top));
        }
    }
}
