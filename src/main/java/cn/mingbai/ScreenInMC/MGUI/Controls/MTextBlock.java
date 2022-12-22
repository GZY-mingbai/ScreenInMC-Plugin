package cn.mingbai.ScreenInMC.MGUI.Controls;

import cn.mingbai.ScreenInMC.MGUI.Alignment;
import cn.mingbai.ScreenInMC.MGUI.MControl;
import cn.mingbai.ScreenInMC.MGUI.MRenderer;

import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;

public class MTextBlock extends MControl {
    private String text = "";
    private Font font;
    private Paint foreground = new Color(0, 0, 0, 255);
    private Alignment.VerticalAlignment textVerticalAlignment= Alignment.VerticalAlignment.Center;
    private Alignment.HorizontalAlignment textHorizontalAlignment= Alignment.HorizontalAlignment.Center;

    public Alignment.HorizontalAlignment getTextHorizontalAlignment() {
        return textHorizontalAlignment;
    }

    public Alignment.VerticalAlignment getTextVerticalAlignment() {
        return textVerticalAlignment;
    }

    public synchronized void setTextHorizontalAlignment(Alignment.HorizontalAlignment textHorizontalAlignment) {
        this.textHorizontalAlignment = textHorizontalAlignment;
        reRender();
    }

    public synchronized void setTextVerticalAlignment(Alignment.VerticalAlignment textVerticalAlignment) {
        this.textVerticalAlignment = textVerticalAlignment;
        reRender();
    }

    public MTextBlock() {

    }

    public MTextBlock(String text) {
        this.text = text;
    }

    public Paint getForeground() {
        return foreground;
    }

    public synchronized void setForeground(Paint foreground) {
        this.foreground = foreground;
        reRender();
    }

    public Font getFont() {
        return font;
    }

    public synchronized void setFont(Font font) {
        this.font = font;
        reRender();
    }

    public String getText() {
        return text;
    }

    public synchronized void setText(String text) {
        this.text = text;
        reRender();
    }

    @Override
    public void onRender(MRenderer mRenderer) {
        renderBackground(mRenderer);
        mRenderer.setFont(font);
        mRenderer.setPaint(foreground);
        LineMetrics metrics = mRenderer.getFont().getLineMetrics(text, mRenderer.getFontRenderContext());
        Rectangle2D rectangle = mRenderer.getFont().getStringBounds(text, mRenderer.getFontRenderContext());
        int left=0;
        int top=0;
        switch (textHorizontalAlignment){
            case Left:
                left=0;
                break;
            case Right:
                left= (int) (getWidth() - rectangle.getWidth());
                break;
            case Center:
            case Stretch:
            case None:
                left=(int) ((getWidth() - rectangle.getWidth()) / 2);
                break;
        }
        switch (textVerticalAlignment){
            case Top:
                top= (int) metrics.getAscent();
                break;
            case Bottom:
                top= (int) (getHeight() - rectangle.getHeight()+ metrics.getAscent());
                break;
            case Center:
            case Stretch:
            case None:
                top= (int) ((getHeight() - rectangle.getHeight()) / 2 + metrics.getAscent());
                break;
        }
        mRenderer.drawTextWithAscent(text, left,top);
        renderChildren(mRenderer);
    }
}
