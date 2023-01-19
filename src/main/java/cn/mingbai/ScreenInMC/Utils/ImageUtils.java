package cn.mingbai.ScreenInMC.Utils;

import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Natives.GPUDither;
import cn.mingbai.ScreenInMC.Screen.Screen;
import net.minecraft.world.level.material.MaterialColor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageUtils {
    public static class DelayConverter{
        public static class DelayImage{
            Image image;
            byte[] data;
            int x=-1;
            int y=-1;
            int width=-1;
            int height=-1;
            boolean processed = false;
            BukkitRunnable runnable;
            DelayConverter converter;
            private void process(){
                runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(converter!=null) {
                            synchronized (converter) {
                                if (converter.globalProcessedCount == 0) {
                                    converter.globalProcessStartTime = System.currentTimeMillis();
                                }
                            }
                        }
                        data = ImageUtils.imageToMapColors(image);
                        if(converter!=null) {
                            synchronized (converter) {
                                if (converter.globalProcessedCount == maxDelayImages-1) {
                                    converter.globalProcessEndTime = System.currentTimeMillis();
                                }
                                if(converter.globalProcessedCount<maxDelayImages) {
                                    converter.globalProcessedCount++;
                                }
                            }
                        }
                        processed = true;
                    }
                };
                runnable.runTaskAsynchronously(Main.thisPlugin());
            }

            public byte[] getData() {
                return data;
            }

            public Image getImage() {
                return image;
            }
            public DelayImage(Image image){
                this.image = image;
            }
            public DelayImage(Image image,int x,int y,int width,int height){
                this.image = image;
                if(x<0||y<0||width<0||height<0){
                    throw new RuntimeException("X,Y,Width,Height can't be non-positive number.");
                }
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
        private long globalProcessStartTime = 0;
        private long globalProcessEndTime = 0;
        private long globalProcessedCount = 0;
        private int delay = 50;
        public synchronized void addImage(DelayImage image){
            image.converter=this;
            if(System.currentTimeMillis()-lastImageTime<delay || images.size()>=maxDelayImages){
                lastImage = image;
                return;
            }
            synchronized (images){
                images.add(image);
                image.process();
            }
            if(runnable==null){
                runnable=new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            while (!this.isCancelled()){
                                synchronized(this) {
                                    if (globalProcessedCount >= maxDelayImages) {
                                        if(globalProcessEndTime!=0 && globalProcessStartTime != 0) {
                                            double processTime = globalProcessEndTime - globalProcessStartTime;
                                            int newDelay = (int) (processTime / ((double) maxDelayImages));
                                            if (newDelay < 50) {
                                                newDelay = 50;
                                            }
                                            Main.getPluginLogger().info("New delay: " + newDelay);
                                            delay = newDelay;
                                        }
                                        globalProcessStartTime=0;
                                        globalProcessEndTime=0;
                                        globalProcessedCount=0;
                                    }
                                }
                                int size;
                                synchronized (images) {
                                    size = images.size();
                                }
                                if(size>0){
                                    DelayImage img;
                                    synchronized (images){
                                        img = images.get(0);
                                    }
                                    if(img.processed){
                                        long timeStart = System.currentTimeMillis();
                                        BukkitRunnable runnable1 = new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                if(img.x==-1||img.y==-1||img.height==-1||img.width==-1) {
                                                    onReady.apply(img);
                                                }else{
                                                    onReady.apply(img,img.x,img.y,img.width,img.height);
                                                }
                                            }
                                        };
                                        runnable1.runTask(Main.thisPlugin());
                                        long leftTime = delay - (System.currentTimeMillis()-timeStart);
                                        if(leftTime>0){
                                            Thread.sleep(delay);
                                        }
                                        synchronized (images){
                                            if(images.size()>0) {
                                                images.remove(0);
                                            }
                                        }
                                        continue;
                                    }
                                }else {
                                    if(lastImage!=null){
                                        images.add(lastImage);
                                        lastImage.process();
                                        lastImage = null;
                                    }
                                }
                                Thread.sleep(delay*maxDelayImages);
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
    public static byte[] imageToMapColorsWithGPU(Image image) {
        long timeStart = System.currentTimeMillis();
        BufferedImage img = imageToBufferedImage(image);
        int height = img.getHeight();
        int width = img.getWidth();
        int dh,dw;
        if(height%pieceSize==0) {
            dh = 0;
        }else{
            dh = pieceSize - height % pieceSize;
        }
        if(width%pieceSize==0) {
            dw = 0;
        }else{
            dw = pieceSize-width%pieceSize;
        }
        int height_=height+dh;
        int width_=width+dw;
        int[] data = img.getRGB(0, 0, width, height, null, 0, width);
        int[] newData = new int[width_*height_];
        for(int i=0;i<height;i++){
            System.arraycopy(data,i*width,newData,i*width_,width);
        }
        timeStart = System.currentTimeMillis();
        byte[] result = GPUDither.dither(newData, width_, height_, pieceSize);
        Main.getPluginLogger().info("OpenCL处理："+(System.currentTimeMillis()-timeStart));
        timeStart = System.currentTimeMillis();
        byte[] newResult = new byte[width*height];
        for(int i=0;i<height;i++){
            System.arraycopy(result,i*width_,newResult,i*width,width);
        }
        return newResult;
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

    public static byte[] imageToMapColors(Image image) {
        if (useOpenCL) {
            return imageToMapColorsWithGPU(image);
        }
        BufferedImage img = imageToBufferedImage(image);
        int height = img.getHeight();
        int width = img.getWidth();
        int dh,dw;
        if(height%pieceSize==0) {
            dh = 0;
        }else{
            dh = pieceSize - height % pieceSize;
        }
        if(width%pieceSize==0) {
            dw = 0;
        }else{
            dw = pieceSize-width%pieceSize;
        }
        int height_=height+dh;
        int width_=width+dw;
        int[] data = img.getRGB(0, 0, width, height, null, 0, width);
        int[] newData = new int[width_*height_];
        for(int i=0;i<height;i++){
            System.arraycopy(data,i*width,newData,i*width_,width);
        }
        byte[] result = new byte[height * width];
        int i = 0;
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                Color current_color = new Color(newData[i], true);
                if (current_color.getAlpha() != 255) {
                    newData[i] = 0;
                    i++;
                    continue;
                }
                Utils.Pair<Integer, Color> closest_match = getClosestMatch(current_color);
                Color co = closest_match.getValue();
                int errorR = current_color.getRed() - co.getRed();
                int errorG = current_color.getGreen() - co.getGreen();
                int errorB = current_color.getBlue() - co.getBlue();
                newData[i] = co.getRGB();
                int key = closest_match.getKey() + 4;
                result[i] = (byte) ((key / 4) << 2 | (key % 4) & 3);
                if (!(x == img.getWidth() - 1)) {
                    int t = i + 1;
                    Color c = new Color(newData[t], true);
                    if (c.getAlpha() == 255) {
                        newData[t] = rgbToInt(colorClip((int) (c.getRed() + errorR * 0.4375f)),
                                colorClip((int) (c.getGreen() + errorG * 0.4375f)),
                                colorClip((int) (c.getBlue() + errorB * 0.4375f)));
                    }
                    if (!(y == img.getHeight() - 1)) {
                        t = i + 1 + width;
                        c = new Color(newData[t], true);
                        if (c.getAlpha() == 255) {
                            newData[t] = rgbToInt(colorClip((int) (c.getRed() + errorR * 0.0625f)),
                                    colorClip((int) (c.getGreen() + errorG * 0.0625f)),
                                    colorClip((int) (c.getBlue() + errorB * 0.0625f)));
                        }
                    }
                }
                if (!(y == img.getHeight() - 1)) {
                    int t = i + width;
                    Color c = new Color(newData[t], true);
                    if (c.getAlpha() == 255) {
                        newData[t] = rgbToInt(colorClip((int) (c.getRed() + errorR * 0.1875f)),
                                colorClip((int) (c.getGreen() + errorG * 0.1875f)),
                                colorClip((int) (c.getBlue() + errorB * 0.1875f)));
                    }
                    if (!(x == 0)) {
                        t = i - 1 + width;
                        c = new Color(newData[t], true);
                        if (c.getAlpha() == 255) {
                            newData[t] = rgbToInt(colorClip((int) (c.getRed() + errorR * 0.3125f)),
                                    colorClip((int) (c.getGreen() + errorG * 0.3125f)),
                                    colorClip((int) (c.getBlue() + errorB * 0.3125f)));
                        }
                    }
                }
                i++;
            }
        }
        byte[] newResult = new byte[width*height];
        for(int j=0;j<height;j++){
            System.arraycopy(result,j*width_,newResult,j*width,width);
        }
        return newResult;
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
