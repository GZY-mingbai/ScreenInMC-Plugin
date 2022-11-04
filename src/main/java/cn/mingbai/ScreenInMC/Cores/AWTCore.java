package cn.mingbai.ScreenInMC.Cores;


import cn.mingbai.ScreenInMC.Core;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class AWTCore extends Core {
//    public static class ScreenInMCAWTContainer extends Container{
//        @Override
//        public void paint(Graphics g) {
//
//            g = image.getGraphics();
//            if (!isOpaque()) {
//                Graphics gg = g.create();
//                try {
//                    if (gg instanceof Graphics2D) {
//                        gg.setColor(getBackground());
//                        ((Graphics2D)gg).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
//                        gg.fillRect(0, 0, getWidth(), getHeight());
//                    }
//                } finally {
//                    gg.dispose();
//                }
//            }
//            super.paint(g);
//            try{
//                ImageIO.write(image, "PNG", new File("aaaa.png"));
//            }catch (Exception e){
//            }
//        }
//    }
    protected Container container;
    @Override
    public void onCreate() {
        int width = screen.getWidth();
        int height = screen.getHeight();
        container = new Container();
        container.setSize(width,height);
        BufferedImage image = new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.createGraphics();
        container.paintAll(graphics);
        graphics.dispose();
    }
}
