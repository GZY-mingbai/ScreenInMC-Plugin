package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Natives.GPUDither;
import cn.mingbai.ScreenInMC.Utils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import net.minecraft.world.level.material.MaterialColor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import static cn.mingbai.ScreenInMC.Utils.ImageUtils.*;

public class VideoProcessor {
    private static void listenProcess(Process process,InputStream stream){
        Thread thread = new Thread(()->{
            try {
                InputStreamReader reader = new InputStreamReader(stream);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String str = null;
                while (((str = bufferedReader.readLine()) != null)&&process.isAlive()) {
                    System.out.println(str);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        thread.start();
    }
    private static void listenProcess(Process process){
        listenProcess(process,process.getInputStream());
        listenProcess(process,process.getErrorStream());
    }
    public static void generateDitheredVideo(File ffmpegFile,File tempDir,File videoFile,File outputFile){
        try {
            Runtime runtime = Runtime.getRuntime();
            if(tempDir.exists()&&!tempDir.isDirectory()){
                throw new Exception();
            }
            if(!tempDir.exists()){
                tempDir.mkdirs();
            }
            Process process;
            if(ffmpegFile==null){
                process = runtime.exec(new String[]{
                        "ffmpeg", "-i", videoFile.getAbsolutePath(), "-r","20", "-f","image2", tempDir + "/%01d.png", "-y"
                });
            }else {
                process = runtime.exec(new String[]{
                        ffmpegFile.getAbsolutePath(), "-i ",videoFile.getAbsolutePath(), "-r","20", "-f","image2", tempDir + "/%01d.png", "-y"
                });
            }
            listenProcess(process);
            process.waitFor();
            if(process.exitValue()==0){

            }else {
                throw new Exception();
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static class Encoder{
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
                return (byte)(this.id << 2 | var0.id & 3);
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
                e.printStackTrace();
            }
            int device=-3;
            int pieceSize = 4;
            File ffmpegFile=null;
            File videoFile=null;
            File tempDir = new File("./temp/");
            File outputFile = new File("output.smv");
            boolean gzip = false;
            for(int i=0;i<args.length;i++){
                if(args[i].equals("-f")){
                    if(i==args.length-1){
                        throw new RuntimeException();
                    }else{
                        ffmpegFile=new File(args[i+1]);
                    }
                }
                if(args[i].equals("-v")){
                    if(i==args.length-1){
                        throw new RuntimeException();
                    }else{
                        videoFile=new File(args[i+1]);
                    }
                }
                if(args[i].equals("-t")){
                    if(i==args.length-1){
                        throw new RuntimeException();
                    }else{
                        tempDir=new File(args[i+1]);
                    }
                }
                if(args[i].equals("-o")){
                    if(i==args.length-1){
                        throw new RuntimeException();
                    }else{
                        outputFile=new File(args[i+1]);
                    }
                }
                if(args[i].equals("-d")){
                    if(i==args.length-1){
                        throw new RuntimeException();
                    }else{
                        device=Integer.parseInt(args[i+1]);
                    }
                }
                if(args[i].equals("-p")){
                    if(i==args.length-1){
                        throw new RuntimeException();
                    }else{
                        pieceSize=Integer.parseInt(args[i+1]);
                    }
                }
                if(args[i].equals("-g")){
                    gzip = true;
                }
            }
            if(videoFile==null||!videoFile.exists()){
                throw new RuntimeException();
            }
            if(tempDir.exists()){
                Utils.deleteDir(tempDir);
            }
            try {
                ImageUtils.initImageUtils();
            }catch (Throwable e){
                initImageUtils();
            }
            if(device==-3) {
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
                if(!found && plats.length>0){
                    device = 0;
                }
                if(plats.length==0){
                    device=-1;
                }
            }
            if (device >= -1) {
                ImageUtils.setUseOpenCL(true);
                int[] p = getPalette();
                if(!GPUDither.init(device, p, p.length,getPieceSize())){
                    ImageUtils.setUseOpenCL(false);
                }
            }
            ImageUtils.setPieceSize(pieceSize);
            generateDitheredVideo(ffmpegFile,tempDir,videoFile,outputFile);
            File[] files = tempDir.listFiles();
            if(files.length>0){
                File file = files[0];
                int width,height,size;
                BufferedImage image = ImageIO.read(file);
                width = image.getWidth();
                height = image.getHeight();
                size = width*height;
                File out = new File(tempDir.getAbsoluteFile()+"/out.smv");
                FileOutputStream outputStream = new FileOutputStream(out);
                outputStream.write((byte)0xE7);
                outputStream.write((byte)0xAA);
                outputStream.write((byte)0x9C);
                outputStream.write((byte)0x09);
                outputStream.write((byte)19);
                if(gzip){
                    outputStream.write((byte)1);
                }else{
                    outputStream.write((byte)0);
                }
                outputStream.write((width>>8)&0xFF);
                outputStream.write(width&0xFF);
                outputStream.write((height>>8)&0xFF);
                outputStream.write(height&0xFF);
                for(int i=0;i<files.length;i++){
                    image = ImageIO.read(files[i]);
                    byte[] data = imageToMapColors(image);
                    if(data.length>size){
                        byte[] d = data;
                        data = new byte[size];
                        System.arraycopy(d,0,data,0,size);
                    }
                    if(data.length<size){
                        byte[] d = data;
                        data = new byte[size];
                        System.arraycopy(d,0,data,0,data.length);
                    }
                    if(gzip){
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
                        gzipOutputStream.write(data);
                        data = byteArrayOutputStream.toByteArray();
                        gzipOutputStream.close();
                        byteArrayOutputStream.close();
                        outputStream.write((data.length>>24)&0xFF);
                        outputStream.write((data.length>>16)&0xFF);
                        outputStream.write((data.length>>8)&0xFF);
                        outputStream.write(data.length&0xFF);
                    }
                    outputStream.write(data);
                    System.out.println("Dither: "+(i+1)+"/"+files.length);
                }
                outputStream.close();
                FileChannel inputChannel = new FileInputStream(out).getChannel();
                FileChannel outputChannel = new FileOutputStream(outputFile).getChannel();
                outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
                inputChannel.close();
                outputChannel.close();
            }
            Utils.deleteDir(tempDir);
            System.out.println("Finished");
        }
        private static void initImageUtils() {
            try {
                Field field = ImageUtils.class.getDeclaredField("platforms");
                field.setAccessible(true);
                field.set(String[].class,GPUDither.getPlatforms());
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
                field.set(Color[].class,colors.toArray(new Color[0]));
                field = ImageUtils.class.getDeclaredField("palette_");
                field.setAccessible(true);
                field.set(int[].class,Utils.toPrimitive(colors_.toArray(new Integer[0])));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

}


