package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Natives.GPUDither;
import cn.mingbai.ScreenInMC.Utils.FileUtils;
import cn.mingbai.ScreenInMC.Utils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

import static cn.mingbai.ScreenInMC.Utils.ImageUtils.*;

public class CLI {
    public static class MaterialColor {
        public static final MaterialColor[] MATERIAL_COLORS = new MaterialColor[64];
        public static final MaterialColor NONE = new MaterialColor(0, 0);
        public static final MaterialColor GRASS = new MaterialColor(1, 8368696);
        public static final MaterialColor SAND = new MaterialColor(2, 16247203);
        public static final MaterialColor WOOL = new MaterialColor(3, 13092807);
        public static final MaterialColor FIRE = new MaterialColor(4, 16711680);
        public static final MaterialColor ICE = new MaterialColor(5, 10526975);
        public static final MaterialColor METAL = new MaterialColor(6, 10987431);
        public static final MaterialColor PLANT = new MaterialColor(7, 31744);
        public static final MaterialColor SNOW = new MaterialColor(8, 16777215);
        public static final MaterialColor CLAY = new MaterialColor(9, 10791096);
        public static final MaterialColor DIRT = new MaterialColor(10, 9923917);
        public static final MaterialColor STONE = new MaterialColor(11, 7368816);
        public static final MaterialColor WATER = new MaterialColor(12, 4210943);
        public static final MaterialColor WOOD = new MaterialColor(13, 9402184);
        public static final MaterialColor QUARTZ = new MaterialColor(14, 16776437);
        public static final MaterialColor COLOR_ORANGE = new MaterialColor(15, 14188339);
        public static final MaterialColor COLOR_MAGENTA = new MaterialColor(16, 11685080);
        public static final MaterialColor COLOR_LIGHT_BLUE = new MaterialColor(17, 6724056);
        public static final MaterialColor COLOR_YELLOW = new MaterialColor(18, 15066419);
        public static final MaterialColor COLOR_LIGHT_GREEN = new MaterialColor(19, 8375321);
        public static final MaterialColor COLOR_PINK = new MaterialColor(20, 15892389);
        public static final MaterialColor COLOR_GRAY = new MaterialColor(21, 5000268);
        public static final MaterialColor COLOR_LIGHT_GRAY = new MaterialColor(22, 10066329);
        public static final MaterialColor COLOR_CYAN = new MaterialColor(23, 5013401);
        public static final MaterialColor COLOR_PURPLE = new MaterialColor(24, 8339378);
        public static final MaterialColor COLOR_BLUE = new MaterialColor(25, 3361970);
        public static final MaterialColor COLOR_BROWN = new MaterialColor(26, 6704179);
        public static final MaterialColor COLOR_GREEN = new MaterialColor(27, 6717235);
        public static final MaterialColor COLOR_RED = new MaterialColor(28, 10040115);
        public static final MaterialColor COLOR_BLACK = new MaterialColor(29, 1644825);
        public static final MaterialColor GOLD = new MaterialColor(30, 16445005);
        public static final MaterialColor DIAMOND = new MaterialColor(31, 6085589);
        public static final MaterialColor LAPIS = new MaterialColor(32, 4882687);
        public static final MaterialColor EMERALD = new MaterialColor(33, 55610);
        public static final MaterialColor PODZOL = new MaterialColor(34, 8476209);
        public static final MaterialColor NETHER = new MaterialColor(35, 7340544);
        public static final MaterialColor TERRACOTTA_WHITE = new MaterialColor(36, 13742497);
        public static final MaterialColor TERRACOTTA_ORANGE = new MaterialColor(37, 10441252);
        public static final MaterialColor TERRACOTTA_MAGENTA = new MaterialColor(38, 9787244);
        public static final MaterialColor TERRACOTTA_LIGHT_BLUE = new MaterialColor(39, 7367818);
        public static final MaterialColor TERRACOTTA_YELLOW = new MaterialColor(40, 12223780);
        public static final MaterialColor TERRACOTTA_LIGHT_GREEN = new MaterialColor(41, 6780213);
        public static final MaterialColor TERRACOTTA_PINK = new MaterialColor(42, 10505550);
        public static final MaterialColor TERRACOTTA_GRAY = new MaterialColor(43, 3746083);
        public static final MaterialColor TERRACOTTA_LIGHT_GRAY = new MaterialColor(44, 8874850);
        public static final MaterialColor TERRACOTTA_CYAN = new MaterialColor(45, 5725276);
        public static final MaterialColor TERRACOTTA_PURPLE = new MaterialColor(46, 8014168);
        public static final MaterialColor TERRACOTTA_BLUE = new MaterialColor(47, 4996700);
        public static final MaterialColor TERRACOTTA_BROWN = new MaterialColor(48, 4993571);
        public static final MaterialColor TERRACOTTA_GREEN = new MaterialColor(49, 5001770);
        public static final MaterialColor TERRACOTTA_RED = new MaterialColor(50, 9321518);
        public static final MaterialColor TERRACOTTA_BLACK = new MaterialColor(51, 2430480);
        public static final MaterialColor CRIMSON_NYLIUM = new MaterialColor(52, 12398641);
        public static final MaterialColor CRIMSON_STEM = new MaterialColor(53, 9715553);
        public static final MaterialColor CRIMSON_HYPHAE = new MaterialColor(54, 6035741);
        public static final MaterialColor WARPED_NYLIUM = new MaterialColor(55, 1474182);
        public static final MaterialColor WARPED_STEM = new MaterialColor(56, 3837580);
        public static final MaterialColor WARPED_HYPHAE = new MaterialColor(57, 5647422);
        public static final MaterialColor WARPED_WART_BLOCK = new MaterialColor(58, 1356933);
        public static final MaterialColor DEEPSLATE = new MaterialColor(59, 6579300);
        public static final MaterialColor RAW_IRON = new MaterialColor(60, 14200723);
        public static final MaterialColor GLOW_LICHEN = new MaterialColor(61, 8365974);
        public final int col;
        public final int id;

        private MaterialColor(int var0, int var1) {
            if (var0 >= 0 && var0 <= 63) {
                this.id = var0;
                this.col = var1;
                MATERIAL_COLORS[var0] = this;
            } else {
                throw new IndexOutOfBoundsException("Map colour ID must be between 0 and 63 (inclusive)");
            }
        }

        public int calculateRGBColor(MaterialColor.Brightness var0) {
            if (this == NONE) {
                return 0;
            } else {
                int var1 = var0.modifier;
                int var2 = (this.col >> 16 & 255) * var1 / 255;
                int var3 = (this.col >> 8 & 255) * var1 / 255;
                int var4 = (this.col & 255) * var1 / 255;
                return -16777216 | var4 << 16 | var3 << 8 | var2;
            }
        }

        public static MaterialColor byId(int var0) {
            return byIdUnsafe(var0);
        }

        private static MaterialColor byIdUnsafe(int var0) {
            MaterialColor var1 = MATERIAL_COLORS[var0];
            return var1 != null ? var1 : NONE;
        }

        public static int getColorFromPackedId(int var0) {
            int var1 = var0 & 255;
            return byIdUnsafe(var1 >> 2).calculateRGBColor(MaterialColor.Brightness.byIdUnsafe(var1 & 3));
        }

        public byte getPackedId(MaterialColor.Brightness var0) {
            return (byte) (this.id << 2 | var0.id & 3);
        }

        public static enum Brightness {
            LOW(0, 180),
            NORMAL(1, 220),
            HIGH(2, 255),
            LOWEST(3, 135);

            private static final MaterialColor.Brightness[] VALUES = new MaterialColor.Brightness[]{LOW, NORMAL, HIGH, LOWEST};
            public final int id;
            public final int modifier;

            private Brightness(int var2, int var3) {
                this.id = var2;
                this.modifier = var3;
            }

            public static MaterialColor.Brightness byId(int var0) {
                return byIdUnsafe(var0);
            }

            static MaterialColor.Brightness byIdUnsafe(int var0) {
                return VALUES[var0];
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if(args.length==0){
            args = new String[]{"-h"};
        }
        Utils.Pair<String, String> typeArch = Utils.getSystem();
        String prefix = "screen-in-mc-" + typeArch.getKey() + "-" + typeArch.getValue();
        String suffix = Utils.getLibraryPrefix(typeArch.getKey());
        String fileName = prefix + suffix;
        try {
            File file = File.createTempFile(prefix, suffix);
            InputStream stream = VideoProcessor.class.getResourceAsStream("/lib/" + fileName);
            ReadableByteChannel inputChannel = Channels.newChannel(stream);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(inputChannel, 0, Long.MAX_VALUE);
            fileChannel.close();
            fileOutputStream.close();
            inputChannel.close();
            stream.close();
            System.load(file.getAbsolutePath());
        } catch (Exception e) {
        }
        try {
            ImageUtils.initImageUtils();
        } catch (Throwable e) {
            initImageUtils();
        }
        int device = -3;
        int pieceSize = 4;
        File ffmpegFile = null;
        File videoFile = null;
        File tempDir = new File("./temp/");
        File outputFile = new File("output.smv");
        boolean watch = false;
        boolean gzip = false;
        boolean ffmpegArg = false;
        boolean loop = false;
        int border = 0;
        String watchPath="";
        List<String> ffmpegArgs=new ArrayList<>();
        if(args[0].startsWith("-")){
            for (int i = 0; i < args.length; i++) {
                if(ffmpegArg){
                    ffmpegArgs.add(args[i]);
                    continue;
                }
                if (args[i].equals("-f")) {
                    if (i == args.length - 1) {
                        throw new RuntimeException("参数错误");
                    } else {
                        ffmpegFile = new File(args[i + 1]);
                    }
                }
                if (args[i].equals("-v")) {
                    if (i == args.length - 1) {
                        throw new RuntimeException("参数错误");
                    } else {
                        videoFile = new File(args[i + 1]);
                    }
                }
                if (args[i].equals("-t")) {
                    if (i == args.length - 1) {
                        throw new RuntimeException("参数错误");
                    } else {
                        tempDir = new File(args[i + 1]);
                    }
                }
                if (args[i].equals("-o")) {
                    if (i == args.length - 1) {
                        throw new RuntimeException("参数错误");
                    } else {
                        outputFile = new File(args[i + 1]);
                    }
                }
                if (args[i].equals("-d")) {
                    if (i == args.length - 1) {
                        throw new RuntimeException("参数错误");
                    } else {
                        device = Integer.parseInt(args[i + 1]);
                    }
                }
                if (args[i].equals("-p")) {
                    if (i == args.length - 1) {
                        throw new RuntimeException("参数错误");
                    } else {
                        pieceSize = Integer.parseInt(args[i + 1]);
                    }
                }
                if (args[i].equals("-g")) {
                    gzip = true;
                }
                if (args[i].equals("-w")) {
                    if (i == args.length - 1) {
                        throw new RuntimeException("参数错误");
                    } else {
                        watch = true;
                        watchPath = args[i+1];
                    }

                }
                if(args[i].equals("-l")){
                    System.out.println("序号\t名称");
                    System.out.println("-3\t自动选择");
                    System.out.println("-2\tJava层 CPU抖色");
                    System.out.println("-1\tC++层 CPU抖色");
                    String[] plats = ImageUtils.getPlatforms();
                    for(int ii=0;ii<plats.length;ii++){
                        System.out.println(ii+"\t"+plats[ii]);
                    }
                    return;
                }
                if(args[i].equals("-h")){
                    System.out.println("ScreenInMC 预处理器");
                    System.out.println();
                    System.out.println("处理视频 Processor [-v 视频文件] [-o 输出文件(.smv)] ([-f ffmpeg路径] [-t 临时目录] [-d 使用设备] [-p 分块大小] [-g(使用GZip压缩)] [-a ffmpeg额外参数(放在最后)])");
                    System.out.println("播放已处理视频 Processor [-w 视频文件/链接(.smv)] ([-x(循环播放)] [-b 边框大小])");
                    System.out.println("列出可用设备 Processor -l");
                    return;
                }
                if(args[i].equals("-x")){
                    loop=true;
                }
                if(args[i].equals("-b")){
                    if (i == args.length - 1) {
                        throw new RuntimeException("参数错误");
                    } else {
                        border = Integer.parseInt(args[i + 1]);
                    }
                }
                if(args[i].equals("-a")){
                    ffmpegArg=true;
                }
            }
        }else {
            watchPath = String.join(" ",args);
            watch = true;
        }

        if (!watch&&(videoFile == null || !videoFile.exists())) {
            throw new RuntimeException("没有-v参数或文件不存在");
        }
        if (watch) {
            VideoProcessor.DitheredVideo video = VideoProcessor.readDitheredVideo(watchPath,loop);
            BufferedImage nowImage = new BufferedImage(video.getWidth(), video.getHeight(), BufferedImage.TYPE_INT_ARGB);
            int finalBorder = border;
            JFrame frame = new JFrame() {
                @Override
                public void update(Graphics g) {
                    paint(g);
                }

                @Override
                public void paint(Graphics g) {
                    Insets insets = this.getInsets();
                    synchronized (nowImage){
                            g.drawImage(nowImage, insets.left+ finalBorder, insets.top+ finalBorder, this);
                    }
                }
            };
            frame.setResizable(false);
            Insets insets = frame.getInsets();
            int ww = video.getWidth()+insets.left+insets.right+border*2;
            int wh = video.getHeight()+insets.top+insets.bottom+border*2;
            if(ww<128){
                ww=128;
            }
            if(wh<128){
                wh=128;
            }
            frame.setSize(ww, wh);
            frame.setVisible(true);
            while (true) {
                long time = System.currentTimeMillis();
                byte[] data = video.readAFrame();
                if (data.length == 0 || !frame.isVisible()) {
                    frame.dispose();
                    System.out.println("播放完成");
                    video.close();
                    return;
                }
                synchronized (nowImage) {
                    for (int y = 0; y < video.getHeight(); y++) {
                        for (int x = 0; x < video.getWidth(); x++) {
                            byte index = data[x + y * video.getWidth()];
                            nowImage.setRGB(x, y, ImageUtils.getColorInt(index));
                        }
                    }
                }

                frame.repaint();
                long wait = 50 - (System.currentTimeMillis() - time);
                if (wait > 0) {
                    Thread.sleep(wait);
                }
            }
        }
        if (tempDir.exists()) {
            FileUtils.deleteDir(tempDir);
        }
        if (device == -3) {
            String[] plats = getPlatforms();
            String[] suggestions = new String[]{"openclon", "nvidia", "intel(r)cpu", "intel(r)opencl"};
            boolean found = false;
            for (int i = 0; i < suggestions.length; i++) {
                for (int j = 0; j < plats.length; j++) {
                    String plat = plats[j].replace(" ", "").toLowerCase();
                    if (plat.startsWith(suggestions[i])) {
                        device = j;
                        found = true;
                    }
                }
                if (found) {
                    break;
                }
            }
            if (!found && plats.length > 0) {
                device = 0;
            }
            if (plats.length == 0) {
                device = -1;
            }
        }
        if (device >= -1) {
            ImageUtils.setUseOpenCL(true);
            int[] p = getPalette();
            if (!GPUDither.init(device, p, p.length, getPieceSize())) {
                ImageUtils.setUseOpenCL(false);
            }
        }
        ImageUtils.setPieceSize(pieceSize);
        VideoProcessor.generateDitheredVideo(ffmpegFile, tempDir, videoFile, outputFile, gzip,ffmpegArgs.toArray(new String[0]));
    }

    private static void initImageUtils() {
        try {
            Field field;
            try {
                field = ImageUtils.class.getDeclaredField("platforms");
                field.setAccessible(true);
                field.set(String[].class, GPUDither.getPlatforms());
            }catch (Throwable e){

            }
            java.util.List<Color> colors = new ArrayList<>();
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
            field = ImageUtils.class.getDeclaredField("palette");
            field.setAccessible(true);
            field.set(Color[].class, colors.toArray(new Color[0]));
            field = ImageUtils.class.getDeclaredField("palette_");
            field.setAccessible(true);
            field.set(int[].class, Utils.toPrimitive(colors_.toArray(new Integer[0])));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
