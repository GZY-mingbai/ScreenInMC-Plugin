package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Natives.GPUDither;
import cn.mingbai.ScreenInMC.Utils.FileUtils;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.ConfigPaletteLoader;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.DitheringProcessor;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.PaletteLoader;
import cn.mingbai.ScreenInMC.Utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils.*;

public class CLI {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            args = new String[]{"-h"};
        }
        Utils.Pair<String, String> typeArch = Utils.getSystem();
        String prefix = "screen-in-mc-" + typeArch.getKey() + "-" + typeArch.getValue();
        String suffix = Utils.getLibraryPrefix(typeArch.getKey());
        String fileName = prefix + suffix;
        try {
            File file = File.createTempFile(prefix, suffix);
            InputStream stream = VideoProcessor.class.getResourceAsStream("/lib/" + fileName);
            FileUtils.streamToFile(stream, file);
            System.load(file.getAbsolutePath());
        } catch (Exception e) {
        }
        PaletteLoader paletteLoader = new ConfigPaletteLoader();
        try {
            ImageUtils.initImageUtils(paletteLoader,new DitheringProcessor.JavaDitheringProcessor());
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
        boolean highSpeed = false;
        boolean gzip = false;
        boolean ffmpegArg = false;
        boolean loop = false;
        int border = 0;
        String watchPath = "";
        List<String> ffmpegArgs = new ArrayList<>();
        if (args[0].startsWith("-")) {
            for (int i = 0; i < args.length; i++) {
                if (ffmpegArg) {
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
                        watchPath = args[i + 1];
                    }

                }
                if (args[i].equals("-l")) {
                    System.out.println("序号\t名称");
                    System.out.println("-4\tJava层 快速CPU抖色");
                    System.out.println("-3\t自动选择");
                    System.out.println("-2\tJava层 CPU抖色");
                    System.out.println("-1\tC++层 CPU抖色");
                    String[] plats = ImageUtils.getOpenCLPlatforms();
                    for (int ii = 0; ii < plats.length; ii++) {
                        System.out.println(ii + "\t" + plats[ii]);
                    }
                    return;
                }
                if (args[i].equals("-h")) {
                    System.out.println("ScreenInMC 预处理器");
                    System.out.println();
                    System.out.println("处理视频 Processor [-v 视频文件] [-o 输出文件(.smv)] ([-f ffmpeg路径] [-t 临时目录] [-d 使用设备] [-p 分块大小] [-g(使用GZip压缩)] [-a ffmpeg额外参数(放在最后)] [-s 使用极速模式(实验性)])");
                    System.out.println("播放已处理视频 Processor [-w 视频文件/链接(.smv)] ([-x(循环播放)] [-b 边框大小])");
                    System.out.println("列出可用设备 Processor -l");
                    return;
                }
                if (args[i].equals("-x")) {
                    loop = true;
                }
                if (args[i].equals("-s")) {
                    highSpeed = true;
                }
                if (args[i].equals("-b")) {
                    if (i == args.length - 1) {
                        throw new RuntimeException("参数错误");
                    } else {
                        border = Integer.parseInt(args[i + 1]);
                    }
                }
                if (args[i].equals("-a")) {
                    ffmpegArg = true;
                }
            }
        } else {
            watchPath = String.join(" ", args);
            watch = true;
        }

        if (!watch && (videoFile == null || !videoFile.exists())) {
            throw new RuntimeException("没有-v参数或文件不存在");
        }
        if (watch) {
            VideoProcessor.DitheredVideo video = VideoProcessor.readDitheredVideo(watchPath, loop);
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
                    synchronized (nowImage) {
                        g.drawImage(nowImage, insets.left + finalBorder, insets.top + finalBorder, this);
                    }
                }
            };
            frame.setResizable(false);
            frame.setVisible(true);
            Insets insets = frame.getInsets();
            int ww = video.getWidth() + insets.left + insets.right + border * 2;
            int wh = video.getHeight() + insets.top + insets.bottom + border * 2;
            if (ww < 128) {
                ww = 128;
            }
            if (wh < 128) {
                wh = 128;
            }
            frame.setSize(ww, wh);
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
                            try {
                                byte index = data[x + y * video.getWidth()];
                                if(index==0||index==1||index==2||index==3){
                                    nowImage.setRGB(x, y,0x00000000);
                                }else{
                                    nowImage.setRGB(x, y,paletteLoader.getPaletteColorRGBs()[((int)(index-4))&0xff]);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
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
            device = getBestOpenCLDevice();
        }
        if (device >= -1) {
            ImageUtils.setDitheringProcessor(new DitheringProcessor.OpenCLDitheringProcessor());
            int[] p = getPalette();
            if(highSpeed){
                ImageUtils.setCustomOpenCLCode("code_fast.cl");
            }else{
                ImageUtils.setCustomOpenCLCode("code.cl");
            }
            if (!GPUDither.init(device, p, p.length, getPieceSize(),ImageUtils.getOpenCLCode())) {
                ImageUtils.setDitheringProcessor(new DitheringProcessor.JavaDitheringProcessor());
            }
        }
        if(device == -2){
            ImageUtils.setDitheringProcessor(new DitheringProcessor.JavaDitheringProcessor());
        }
        if(device == -4){
            ImageUtils.setDitheringProcessor(new DitheringProcessor.JavaFastDitheringProcessor());
        }
        ImageUtils.setPieceSize(pieceSize);
        VideoProcessor.generateDitheredVideo(ffmpegFile, tempDir, videoFile, outputFile, gzip, ffmpegArgs.toArray(new String[0]));

    }

    private static void initImageUtils() {
        try {
            Field field;
            try {
                field = ImageUtils.class.getDeclaredField("platforms");
                field.setAccessible(true);
                field.set(String[].class, GPUDither.getPlatforms());
            } catch (Throwable e) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
