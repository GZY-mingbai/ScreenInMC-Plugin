package cn.mingbai.ScreenInMC.MGUI;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

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
        graphics.setClip((int) mControl.getAbsoluteLeft(), (int) mControl.getAbsoluteTop(), (int) mControl.getWidth(), (int) mControl.getHeight());
    }

    public void drawPixel(int x, int y) {
        if (mControl.isVisibleActually()) {
            setClip();
            graphics.fillRect((int) (mControl.getAbsoluteLeft() + x), (int) (mControl.getAbsoluteTop() + y), 1, 1);
        }
    }

    public void drawRoundRect(int x, int y, int width, int height,int roundWidth,int roundHeight, boolean fill) {
        if (mControl.isVisibleActually()) {
            setClip();
            if (fill) {
                graphics.fillRoundRect((int) (mControl.getAbsoluteLeft() + x), (int) (mControl.getAbsoluteTop() + y), width, height,roundWidth,roundHeight);
            } else {
                graphics.drawRoundRect((int) (mControl.getAbsoluteLeft() + x), (int) (mControl.getAbsoluteTop() + y), width, height,roundWidth,roundHeight);
            }
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

    public void drawTextWithAscent(String text, int x, int y) {
        if (mControl.isVisibleActually()) {
            setClip();
            graphics.drawString(text, (int) (mControl.getAbsoluteLeft() + x), (int) (mControl.getAbsoluteTop() + y));
        }
    }
    public void drawImage(Image image,int x,int y) {
        if (mControl.isVisibleActually()) {
            setClip();
            graphics.drawImage(image,(int) (mControl.getAbsoluteLeft() + x), (int) (mControl.getAbsoluteTop() + y),null);
        }
    }
    public void drawImage(Image image,int x,int y,int width,int height) {
        if (mControl.isVisibleActually()) {
            setClip();
            graphics.drawImage(image,(int) (mControl.getAbsoluteLeft() + x), (int) (mControl.getAbsoluteTop() + y),width,height,null);
        }
    }
    public Font getFont(){
        return graphics.getFont();
    }

    public FontRenderContext getFontRenderContext() {
        return frc;
    }
}
