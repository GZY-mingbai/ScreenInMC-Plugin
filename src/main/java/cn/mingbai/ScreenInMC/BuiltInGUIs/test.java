package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.ImageUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.awt.image.BufferedImage;

public class test extends Core {

    private byte color = 0;

    BufferedImage image;
    Graphics graphics;

    public test() {
    }

    @Override
    public void onCreate() {
        image = new BufferedImage(screen.getWidth() * 128, screen.getHeight() * 128, BufferedImage.TYPE_INT_ARGB);
        graphics=image.getGraphics();
        graphics.setColor(Color.white);
        graphics.fillRect(0,0,image.getWidth(),image.getHeight());
        graphics.setColor(Color.black);
        screen.sendView(ImageUtils.imageToMapColors(image));
    }

    @Override
    public void onMouseClick(int x, int y) {
        new BukkitRunnable() {
            @Override
            public void run() {
                graphics.fillOval(x-8,y-8,16,16);
                byte[] colors = ImageUtils.imageToMapColors(image);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        screen.sendView(colors);
                    }
                }.runTask(Main.thisPlugin());
            }
        }.runTaskAsynchronously(Main.thisPlugin());
    }
}
