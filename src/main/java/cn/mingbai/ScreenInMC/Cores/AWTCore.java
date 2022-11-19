package cn.mingbai.ScreenInMC.Cores;


import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.ImageUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

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
    public static class ScreenInMCAWTContainer extends JPanel{
        private AWTCore core;
        public ScreenInMCAWTContainer(Container panel){
            this.add(panel);
        }

    public void setCore(AWTCore core) {
        this.core = core;
    }

    public void clickAt(int x, int y){
            Component nowComponent = this;
            while (true){
                Bukkit.broadcastMessage(nowComponent.getClass().getName());
                for(MouseListener i:nowComponent.getMouseListeners()){
                    i.mouseClicked(new MouseEvent(nowComponent,MouseEvent.MOUSE_CLICKED,
                            System.currentTimeMillis(), 0,x,y,0,0,1,
                            false,MouseEvent.BUTTON1));
                }
                if(nowComponent instanceof Container){
                    Component newComponent = ((Container) nowComponent).getComponentAt(x,y);
                    Bukkit.broadcastMessage(newComponent.getClass().getName());
                    x-=newComponent.getX();
                    y-=newComponent.getY();
                    if(nowComponent.equals(newComponent)){
                        break;
                    }else{
                        nowComponent=newComponent;
                        continue;
                    }
                }
                break;
            }
        }
    @Override
    public void repaint() {
        if(core!=null){
            Bukkit.broadcastMessage("rerender....");
            core.renderGUI();
        }
    }
}
    protected ScreenInMCAWTContainer container;
    public AWTCore(Container container){
        this.container = new ScreenInMCAWTContainer(container);
    }
    private BufferedImage image;
    private Graphics graphics;
    @Override
    public void onCreate() {
        container.setCore(this);
        int screenWidth = screen.getWidth()*128;
        int screenHeight = screen.getHeight()*128;
        container.setSize(screenWidth,screenHeight);
        image = new BufferedImage(screenWidth,screenHeight, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
        container.addNotify();
    }

    @Override
    public void onMouseClick(int x, int y) {
        container.clickAt(x,y);
    }

    public void renderGUI(){
        container.paintComponents(graphics);
        screen.sendView(ImageUtils.imageToMapColors(image));
    }
}
