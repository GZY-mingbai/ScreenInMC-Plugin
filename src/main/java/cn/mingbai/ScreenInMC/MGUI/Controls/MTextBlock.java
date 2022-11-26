package cn.mingbai.ScreenInMC.MGUI.Controls;

import cn.mingbai.ScreenInMC.MGUI.MControl;
import cn.mingbai.ScreenInMC.MGUI.MRenderer;

import java.awt.*;

public class MTextBlock extends MControl {
    protected String text = "";
    private Font font;
    private Paint foreground = new Color(0, 0, 0, 255);

    public MTextBlock() {

    }

    public MTextBlock(String text) {
        this.text = text;
    }

    public Paint getForeground() {
        return foreground;
    }

    public void setForeground(Paint foreground) {
        this.foreground = foreground;
        reRender();
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        reRender();
    }

    @Override
    public void onRender(MRenderer MRenderer) {
        super.onRender(MRenderer);
        MRenderer.setFont(font);
        MRenderer.setPaint(foreground);
        MRenderer.drawTextCenter(text, (int) getWidth(), (int) getHeight());
    }
}
