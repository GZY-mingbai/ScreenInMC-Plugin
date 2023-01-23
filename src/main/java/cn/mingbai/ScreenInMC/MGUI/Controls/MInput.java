package cn.mingbai.ScreenInMC.MGUI.Controls;

import cn.mingbai.ScreenInMC.MGUI.Alignment;
import cn.mingbai.ScreenInMC.MGUI.MRenderer;

import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;

public class MInput extends MTextBlock{
    public MInput(){
        this("");
    }
    public MInput(String text){
        this.placeholder=text;
        super.setPaddingLeft(16);
        super.setPaddingRight(16);
        super.setTextHorizontalAlignment(Alignment.HorizontalAlignment.Left);
    }

    private String placeholder;

    public synchronized void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        reRender();
    }

    public String getPlaceholder() {
        return placeholder;
    }

    @Override
    public void onRender(MRenderer mRenderer) {
        mRenderer.setStroke(new BasicStroke(2));
        if(isActive()){
            mRenderer.setPaint(new Color(182, 183, 183));
        }else{
            mRenderer.setPaint(new Color(241, 243, 244));
        }
        mRenderer.drawRoundRect(0,0, (int) getWidth(), (int) getHeight(),(int) getHeight(),(int) getHeight(),true);
        mRenderer.setPaint(new Color(143, 143, 143));
        mRenderer.drawRoundRect(2,2, (int) getWidth()-4, (int) getHeight()-4,(int) getHeight()-4,(int) getHeight()-4,false);
        super.onRender(mRenderer);
        if(getText().length()==0 && placeholder.length()!=0){
            LineMetrics metrics = mRenderer.getFont().getLineMetrics(placeholder, mRenderer.getFontRenderContext());
            Rectangle2D rectangle = mRenderer.getFont().getStringBounds(placeholder, mRenderer.getFontRenderContext());
            int left = (int) (getHeight()/2);
            int top = (int) ((getHeight() - rectangle.getHeight()) / 2 + metrics.getAscent());
            mRenderer.setPaint(new Color(100,100,100));
            mRenderer.drawTextWithAscent(placeholder,left,top);
        }
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
