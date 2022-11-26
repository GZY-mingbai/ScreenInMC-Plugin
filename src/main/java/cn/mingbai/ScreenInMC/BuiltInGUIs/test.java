package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.ImageUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

import static java.awt.Image.SCALE_SMOOTH;

public class test extends Core {

    BufferedImage image;
    Graphics graphics;
    String imageUrl;
    private final byte color = 0;

    public test(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public void onCreate() {
        image = new BufferedImage(screen.getWidth() * 128, screen.getHeight() * 128, BufferedImage.TYPE_INT_ARGB);
        graphics = image.getGraphics();
        try {
            Image dimage = ImageIO.read(new URL(imageUrl));
            dimage = dimage.getScaledInstance(image.getWidth(), image.getHeight(), SCALE_SMOOTH);
            graphics.drawImage(dimage, 0, 0, image.getWidth(), image.getHeight(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        graphics.setColor(Color.white);
        screen.sendView(ImageUtils.imageToMapColors(image));
    }

    @Override
    public void onMouseClick(int x, int y) {
        new BukkitRunnable() {
            @Override
            public void run() {

                graphics.fillOval(x - 8, y - 8, 16, 16);
                long start = System.currentTimeMillis();
                byte[] colors = ImageUtils.imageToMapColors(image.getScaledInstance(image.getWidth(), image.getHeight(), Image.SCALE_FAST));
                Bukkit.broadcastMessage("Time: " + (System.currentTimeMillis() - start));
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
