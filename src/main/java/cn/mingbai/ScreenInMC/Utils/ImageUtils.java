package cn.mingbai.ScreenInMC.Utils;

import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Natives.GPUDither;
import cn.mingbai.ScreenInMC.Screen.Screen;
import net.minecraft.world.level.material.MaterialColor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageUtils {
    public static class DelayConverter{
        public static class DelayImage{
            Image image;
            int[] imageData;
            byte[] data;
            int x=-1;
            int y=-1;
            int width=-1;
            int height=-1;
            int bWidth = -1;
            int bHeight = -1;
            BukkitRunnable runnable;
            byte processState = 0;
            public byte[] getData() {
                return data;
            }

            public Image getImage() {
                return image;
            }
            public DelayImage(Image image){
                this.image = image;
            }
            public DelayImage(int[] image,int width,int height){
                if(width<0||height<0){
                    throw new RuntimeException("X,Y,Width,Height can't be non-positive number.");
                }
                this.imageData = image;
                this.bWidth=width;
                this.bHeight=height;
            }

            public DelayImage(Image image,int x,int y,int width,int height){
                if(x<0||y<0||width<0||height<0){
                    throw new RuntimeException("X,Y,Width,Height can't be non-positive number.");
                }
                this.image = image;
                this.x=x;
                this.y=y;
                this.width=width;
                this.height=height;
            }

            public int getX() {
                return x;
            }

            public int getY() {
                return y;
            }

            public int getWidth() {
                return width;
            }

            public int getHeight() {
                return height;
            }
        }
        public abstract static class DelayOnReady{
            public abstract void apply(DelayImage imageData);
            public abstract void apply(DelayImage imageData,int x,int y,int width,int height);


        }

        private DelayOnReady onReady = null;
        private BukkitRunnable runnable = null;
        private long lastImageTime = 0;

        public DelayConverter(DelayOnReady onReady){
            this.onReady = onReady;
        }
        private List<DelayImage> images = Collections.synchronizedList(new ArrayList<>());
        private DelayImage lastImage;
        private int delay = 50;
        private boolean unloaded = false;
        private void applyDelayImage(DelayImage img){
            if(img.x==-1||img.y==-1||img.height==-1||img.width==-1) {
                onReady.apply(img);
            }else{
                onReady.apply(img,img.x,img.y,img.width,img.height);
            }
        }
        public synchronized void addImage(DelayImage image){
            synchronized (images){
                lastImage = image;
                if(System.currentTimeMillis()-lastImageTime<delay){
                    return;
                }
                images.add(image);
            }
            if(runnable==null){
                runnable=new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            while (!this.isCancelled() && !unloaded){
                                List<DelayImage> processImages = new ArrayList<>();
                                synchronized (images) {
                                    for (DelayImage img : images ) {
                                        if (img.processState == 0 && processImages.size() < maxDelayImages) {
                                            processImages.add(img);
                                        }
                                    }
                                }
                                if(processImages.size()==maxDelayImages) {
                                    synchronized (images) {
                                        for (int i = 0; i < maxDelayImages; i++) {
                                            processImages.get(i).processState = 1;
                                        }
                                    }
                                        new BukkitRunnable() {
                                            @Override
                                        public void run() {
                                                long startTime = System.currentTimeMillis();
                                                Thread[] threads = new Thread[maxDelayImages];
                                                for (int i = 0; i < maxDelayImages; i++) {
                                                    synchronized (images) {
                                                        DelayImage img = processImages.get(i);
                                                        Image processImage;
                                                        int[] processImageData;
                                                        int bWidth;
                                                        int bHeight;
                                                        if(img.image==null){
                                                            processImage = null;
                                                            processImageData = image.imageData;
                                                            bWidth = image.bWidth;
                                                            bHeight = image.bHeight;
                                                        }else{
                                                            bHeight = -1;
                                                            bWidth = -1;
                                                            processImageData = null;
                                                            processImage = img.image;
                                                        }
                                                        threads[i] = new Thread(() -> {
                                                            byte[] data;
                                                            if(processImage==null){
                                                                data = imageToMapColors(processImageData,bWidth,bHeight);
                                                            }else{
                                                                data = imageToMapColors(processImage);
                                                            }
                                                            synchronized (images) {
                                                                img.data = data;
                                                            }
                                                        });
                                                        threads[i].start();
                                                    }
                                                }
                                                for (int i = 0; i < maxDelayImages; i++) {
                                                    try {
                                                        threads[i].join();
                                                    } catch (Exception e) {
                                                    }
                                                }
                                                synchronized (images) {
                                                    for (int i = 0; i < maxDelayImages; i++) {
                                                        processImages.get(i).processState = 2;
                                                    }
                                                }
                                                double processTime = System.currentTimeMillis() - startTime;
                                                int newDelay = (int) (processTime / ((double) maxDelayImages));
                                                if (newDelay < 50) {
                                                    newDelay = 50;
                                                }
                                                newDelay = (delay+newDelay)/2;
                                                Main.getPluginLogger().info("New delay: " + newDelay);
                                                delay = newDelay;
                                            }
                                        }.runTaskAsynchronously(Main.thisPlugin());
                                    }
                                int processedImages = 0;
                                int processingImages = 0;
                                int waitingImages = 0;
                                synchronized (images) {
                                    for (DelayImage img : images) {
                                        if (img.processState==2) {
                                            processedImages++;
                                        } else if (img.processState==1) {
                                            processingImages++;
                                        } else if (img.processState==0){
                                            waitingImages++;
                                        }
                                    }
                                }
//                                Main.getPluginLogger().info(processedImages+" "+processingImages+" "+waitingImages);

                                if(processedImages>0){
                                    DelayImage img;
                                    short state;
                                    synchronized (images) {
                                        img = images.get(0);
                                        state = img.processState;
                                    }
                                    if(state==2){
                                        long timeStart = System.currentTimeMillis();
                                        synchronized (images) {
                                            applyDelayImage(img);
                                        }
                                        long leftTime = delay - (System.currentTimeMillis()-timeStart);
                                        synchronized (images){
                                            if(images.size()>0) {
                                                images.remove(0);
                                            }
                                        }
                                        if(leftTime>0){
                                            Thread.sleep(leftTime);
                                        }
                                        continue;
                                    }
                                    Thread.sleep(delay*maxDelayImages);
                                }else {
                                    if(lastImage!=null && runnable!=null && processingImages==0 && waitingImages<maxDelayImages){
                                        DelayImage img = lastImage;
                                        lastImage=null;
                                        for(int i=0;i<maxDelayImages-waitingImages;i++) {
                                            addImage(img);
                                        }
                                    }
                                    Thread.sleep(delay);
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                };
                runnable.runTaskAsynchronously(Main.thisPlugin());
            }
            lastImageTime = System.currentTimeMillis();
        }
        public void stop(){
            unloaded = true;
            if(runnable!=null) {
                runnable.cancel();
                runnable = null;
            }
            synchronized (images){
                while (images.size()>0){
                    DelayImage image = images.get(0);
                    if(image.runnable!=null){
                        image.runnable.cancel();
                        image.runnable = null;
                    }
                    images.remove(0);
                }
            }
        }
    }



    private static int pieceSize = 4;
    private static int maxDelayImages = 4;
    private static Color[] palette;
    private static int[] palette_;
    private static boolean useOpenCL = true;

    public static int[] getPalette() {
        return palette_;
    }
    private static String[] platforms;

    public static String[] getPlatforms() {
        return platforms.clone();
    }

    public static void setMaxDelayImages(int maxDelayImages) {
        ImageUtils.maxDelayImages = maxDelayImages;
    }

    public static void setPieceSize(int pieceSize) {
        ImageUtils.pieceSize = pieceSize;
    }

    public static int getPieceSize() {
        return pieceSize;
    }

    public static void initImageUtils() {
        try {
            platforms = GPUDither.getPlatforms();
            List<Color> colors = new ArrayList<>();
            List<Integer> colors_ = new ArrayList<>();
            for (int i = 1; i < MaterialColor.MATERIAL_COLORS.length - 1; i++) {
                MaterialColor materialColor = MaterialColor.byId(i);
                if (materialColor == null || materialColor.equals(MaterialColor.NONE)) {
                    break;
                }
                for (int b = 0; b < 4; b++) {
                    Color color = new Color(materialColor.calculateRGBColor(MaterialColor.Brightness.byId(b)));
                    int cr = color.getRed();
                    int cg = color.getGreen();
                    int cb = color.getBlue();
                    int ca = color.getAlpha();
                    color = new Color(cb, cg, cr, ca);
                    colors.add(color);
                    colors_.add(color.getRGB());
                }
            }
            palette = colors.toArray(new Color[0]);
            palette_ = Utils.toPrimitive(colors_.toArray(new Integer[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static byte[] imageToMapColorsWithGPU(int[] data,int width,int height) {
        byte[] result = GPUDither.dither(data, width, height, pieceSize);
        return result;
    }

    public static boolean isUseOpenCL() {
        return useOpenCL;
    }

    public static void setUseOpenCL(boolean useOpenCL) {
        ImageUtils.useOpenCL = useOpenCL;
    }

    public static int colorDistance(Color c1, Color c2) {
        int rmean = (c1.getBlue() + c2.getBlue()) / 2;
        int r = c1.getRed() - c2.getRed();
        int g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        return (int) Math.sqrt((((512 + rmean) * r * r) >> 8) + 4 * g * g + (((767 - rmean) * b * b) >> 8));
    }

    public static Utils.Pair<Integer, Color> getClosestMatch(Color color) {
        int minimum_index = 0;
        int minimum_difference = colorDistance(color, palette[0]);
        for (int i = 1; i < palette.length; i++) {
            int current_difference = colorDistance(color, palette[i]);
            if (current_difference < minimum_difference) {
                minimum_difference = current_difference;
                minimum_index = i;
            }
        }

        return new Utils.Pair<>(minimum_index, palette[minimum_index]);
    }

    public static int colorClip(int i) {
        if (i > 255) {
            return 255;
        }
        if (i < 0) {
            return 0;
        }
        return i;
    }

    public static int rgbToInt(int r, int g, int b) {
        return 0xFF000000 | ((r << 16) & 0x00FF0000) | ((g << 8) & 0x0000FF00) | (b & 0x000000FF);
    }
    public static byte[] imageToMapColors(Image image){
        BufferedImage img = imageToBufferedImage(image);
        int height = img.getHeight();
        int width = img.getWidth();
        int[] data = img.getRGB(0, 0, width, height, null, 0, width);
        return imageToMapColors(data,width,height);
    }

    public static byte[] imageToMapColors(int[] data,int width,int height) {
        if (useOpenCL) {
            return imageToMapColorsWithGPU(data,width,height);
        }
        byte[] result = new byte[height * width];
        int i = 0;
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                Color current_color = new Color(data[i], true);
                if (current_color.getAlpha() != 255) {
                    data[i] = 0;
                    i++;
                    continue;
                }
                Utils.Pair<Integer, Color> closest_match = getClosestMatch(current_color);
                Color co = closest_match.getValue();
                int errorR = current_color.getRed() - co.getRed();
                int errorG = current_color.getGreen() - co.getGreen();
                int errorB = current_color.getBlue() - co.getBlue();
                data[i] = co.getRGB();
                int key = closest_match.getKey() + 4;
                result[i] = (byte) ((key / 4) << 2 | (key % 4) & 3);
                if (!(x == width - 1)) {
                    int t = i + 1;
                    Color c = new Color(data[t], true);
                    if (c.getAlpha() == 255) {
                        data[t] = rgbToInt(colorClip((int) (c.getRed() + errorR * 0.4375f)),
                                colorClip((int) (c.getGreen() + errorG * 0.4375f)),
                                colorClip((int) (c.getBlue() + errorB * 0.4375f)));
                    }
                    if (!(y == height - 1)) {
                        t = i + 1 + width;
                        c = new Color(data[t], true);
                        if (c.getAlpha() == 255) {
                            data[t] = rgbToInt(colorClip((int) (c.getRed() + errorR * 0.0625f)),
                                    colorClip((int) (c.getGreen() + errorG * 0.0625f)),
                                    colorClip((int) (c.getBlue() + errorB * 0.0625f)));
                        }
                    }
                }
                if (!(y == height - 1)) {
                    int t = i + width;
                    Color c = new Color(data[t], true);
                    if (c.getAlpha() == 255) {
                        data[t] = rgbToInt(colorClip((int) (c.getRed() + errorR * 0.1875f)),
                                colorClip((int) (c.getGreen() + errorG * 0.1875f)),
                                colorClip((int) (c.getBlue() + errorB * 0.1875f)));
                    }
                    if (!(x == 0)) {
                        t = i - 1 + width;
                        c = new Color(data[t], true);
                        if (c.getAlpha() == 255) {
                            data[t] = rgbToInt(colorClip((int) (c.getRed() + errorR * 0.3125f)),
                                    colorClip((int) (c.getGreen() + errorG * 0.3125f)),
                                    colorClip((int) (c.getBlue() + errorB * 0.3125f)));
                        }
                    }
                }
                i++;
            }
        }
        return result;
    }

    public static BufferedImage imageToBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.drawImage(img, 0, 0, null);
        graphics.dispose();
        return bufferedImage;
    }
}
