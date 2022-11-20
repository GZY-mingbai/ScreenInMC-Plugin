package cn.mingbai.ScreenInMC.Cores;


import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Utils.ImageUtils;
import org.bukkit.Bukkit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

public class AWTCore extends Core {
    public static class ScreenInMCAWTContainer extends JPanel {
        private AWTCore core = null;
        private Container subContainer;

        public ScreenInMCAWTContainer(AWTCore core,Container panel) {
            Bukkit.broadcastMessage((core!=null)+"");
            this.core = core;
            this.subContainer = panel;
            this.add(panel);
        }

        public void clickAt(int x, int y) {
            Component nowComponent = this;
            while (true) {
                for (MouseListener i : nowComponent.getMouseListeners()) {
                    i.mouseClicked(new MouseEvent(nowComponent, MouseEvent.MOUSE_CLICKED,
                            System.currentTimeMillis(), 0, x, y, 0, 0, 1,
                            false, MouseEvent.BUTTON1));
                }
                if (nowComponent instanceof Container) {
                    Component newComponent = ((Container) nowComponent).getComponentAt(x, y);
                    Bukkit.broadcastMessage(newComponent.getClass().getName());
                    x -= newComponent.getX();
                    y -= newComponent.getY();
                    if (nowComponent.equals(newComponent)) {
                        break;
                    } else {
                        nowComponent = newComponent;
                        continue;
                    }
                }
                break;
            }
        }

        @Override
        public void repaint() {
            super.repaint();
            Bukkit.broadcastMessage("rerender...."+(core != null));
            if (core != null) {
                Bukkit.broadcastMessage("rerender....");
                core.renderGUI();
            }
        }
        @Override
        public void repaint(long tm) {
            repaint();
        }
        @Override
        public void repaint(int x, int y, int width, int height) {
            repaint();
        }

        @Override
        public void setSize(int width, int height) {
            subContainer.setSize(width,height);
            super.setSize(width, height);
        }
    }

    protected ScreenInMCAWTContainer container;

    public AWTCore(Container container) {
        this.container = new ScreenInMCAWTContainer(this,container);
    }

    private BufferedImage image;
    private Graphics graphics;

    @Override
    public void onCreate() {
        int screenWidth = screen.getWidth() * 128;
        int screenHeight = screen.getHeight() * 128;
        image = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
        graphics.setColor(new Color(0,0,0,0));
        graphics.fillRect(0,0,screenWidth,screenHeight);
        container.setSize(screenWidth, screenHeight);
        container.addNotify();
        container.repaint();
    }

    @Override
    public void onMouseClick(int x, int y) {
        container.clickAt(x, y);
    }

    public void renderGUI() {
        try {
            container.paintComponents(graphics);
        }catch (Exception e){
            e.printStackTrace();
        }
        screen.sendView(ImageUtils.imageToMapColors(image));
    }
}
