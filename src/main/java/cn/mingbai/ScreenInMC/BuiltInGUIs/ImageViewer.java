package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import org.bukkit.scheduler.BukkitRunnable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class ImageViewer extends Core {
    BufferedImage image;

    public ImageViewer() {
        super("ImageViewer");
    }

    @Override
    public void onCreate() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    image = ImageIO.read(new URL("https://i1.hdslb.com/bfs/archive/360d6633673f1b403cbbeb9d33d02161eda3486a.jpg"));
                    image = ImageUtils.imageToBufferedImage(image.getScaledInstance(getScreen().getWidth() * 128, getScreen().getHeight() * 128, Image.SCALE_SMOOTH));
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            send();
                        }
                    }.runTask(Main.thisPlugin());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Main.thisPlugin());
    }

    @Override
    public void onUnload() {
    }

    @Override
    public void onMouseClick(int x, int y, Utils.MouseClickType type) {
        if (image != null) {
            send();
        }
    }

    @Override
    public void onTextInput(String text) {
        if (image != null) {
            send();
        }
    }

    private void send() {
        getScreen().sendView(ImageUtils.imageToMapColors(image), 0, 0, image.getWidth(), image.getHeight());
    }
}
