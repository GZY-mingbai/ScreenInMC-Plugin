import cn.mingbai.ScreenInMC.BrowserCoreInitializations.ChromiumCoreInitialization;
import cn.mingbai.ScreenInMC.BuiltInGUIs.testAwt;
import cn.mingbai.ScreenInMC.Cores.AWTCore;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Natives.GPUDither;
import cn.mingbai.ScreenInMC.Utils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Logger;

import static cn.mingbai.ScreenInMC.Utils.ImageUtils.imageToMapColors;
import static cn.mingbai.ScreenInMC.Utils.ImageUtils.initImageUtils;

public class test {
    public static enum Brightness {
        LOW(0, 180),
        NORMAL(1, 220),
        HIGH(2, 255),
        LOWEST(3, 135);

        private static final Brightness[] VALUES;
        public final int id;
        public final int brightness;

        private Brightness(int id, int brightness) {
            this.id = id;
            this.brightness = brightness;
        }

        static Brightness get(int id) {
            return VALUES[id];
        }

        static {
            VALUES = new Brightness[]{LOW, NORMAL, HIGH, LOWEST};
        }
    }
    public static void main(String[] args) throws Exception{
        Robot robot = new Robot();
        int width = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int height = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        long time =System.currentTimeMillis();
        robot.createScreenCapture(new Rectangle(width, height));
        System.out.println((System.currentTimeMillis()-time)+"");
//        Utils.Pair<String,String> typeArch = Utils.getSystem();
//        String prefix="screen-in-mc-"+typeArch.getKey()+"-"+typeArch.getValue();
//        String suffix = Utils.getLibraryPrefix(typeArch.getKey());
//        String fileName = prefix+suffix;
//        try {
//            File file = File.createTempFile(prefix,suffix);
//            InputStream stream = test.class.getResourceAsStream("/lib/" + fileName);
//            ReadableByteChannel inputChannel = Channels.newChannel(stream);
//            FileOutputStream fileOutputStream = new FileOutputStream(file);
//            FileChannel fileChannel = fileOutputStream.getChannel();
//            fileChannel.transferFrom(inputChannel, 0, Long.MAX_VALUE);
//            fileChannel.close();
//            fileOutputStream.close();
//            inputChannel.close();
//            stream.close();
//            System.load(file.getAbsolutePath());
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        BufferedImage image = ImageIO.read(new File("test.png"));
//        ImageUtils.initImageUtils();
//        int[] p = ImageUtils.getPalette();
//        GPUDither.init(1, p,p.length);
//        byte[] ok = ImageUtils.imageToMapColorsWithGPU(image);
//        System.out.println(114514);
////        ImageIO.write(image,"png",new File("test.png"));
////        ChromiumCoreInitialization initialization = new ChromiumCoreInitialization();
////        initialization.installCore();
    }
}
