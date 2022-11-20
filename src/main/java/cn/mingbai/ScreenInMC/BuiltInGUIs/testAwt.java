package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Cores.AWTCore;
import org.bukkit.Bukkit;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class testAwt extends AWTCore {
    static Image image;

    public static class imagetest extends JComponent {
        @Override
        public void paint(Graphics graphics) {
            try {
                graphics.drawImage(image, getX(), getY(), null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static JPanel createContainer() {
        try {
            image = ImageIO.read(new URL("https://pic4.zhimg.com/v2-bd09649cf732f00c620b0be275cdbc48_r.jpg")).getScaledInstance(256, 256, Image.SCALE_SMOOTH);
        } catch (Exception e) {
        }
        JPanel container = new JPanel();
//        JButton button = new JButton("Just a Test");
//        button.setLocation(0,0);
//        button.setSize(256,128);
//        container.add(button);
        imagetest imagetes = new imagetest();
        imagetes.setLocation(0, 0);
        imagetes.setSize(256, 256);
        container.add(imagetes);
        container.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Bukkit.broadcastMessage("tested " + e.getX() + " " + e.getY());
                imagetes.setLocation(e.getX(), e.getY());
                Bukkit.broadcastMessage(container.getParent().getClass().getName());
                container.getParent().repaint();
            }
        });

        return container;
    }

    public testAwt() {
        super(createContainer());
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
