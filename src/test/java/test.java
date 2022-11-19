import cn.mingbai.ScreenInMC.BrowserCoreInitializations.ChromiumCoreInitialization;
import cn.mingbai.ScreenInMC.BuiltInGUIs.testAwt;
import cn.mingbai.ScreenInMC.Cores.AWTCore;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
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
        ChromiumCoreInitialization initialization = new ChromiumCoreInitialization();
        initialization.installCore();
    }
}
