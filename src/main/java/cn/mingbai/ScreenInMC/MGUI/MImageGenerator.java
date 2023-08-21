package cn.mingbai.ScreenInMC.MGUI;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;

import static cn.mingbai.ScreenInMC.MGUI.MContainer.setDefaultFont;

public class MImageGenerator extends MControl{
    public MImageGenerator(int width, int height,Color background){
        setWidth(width);
        setHeight(height);
        setBackground(background);
    }
    public BufferedImage getImage(){
        if(this.getWidth()==0||this.getHeight()==0) return null;
        BufferedImage image = new BufferedImage((int) this.getWidth(), (int) this.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontRenderContext frc = graphics.getFontRenderContext();
        setDefaultFont(graphics);
        MRenderer renderer = new MRenderer(graphics,frc);
        renderer.setControl(this);
        this.onRender(renderer);
        graphics.dispose();
        return image;
    }
}
