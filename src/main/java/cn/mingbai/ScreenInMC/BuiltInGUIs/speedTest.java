package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Cores.MGUICore;
import cn.mingbai.ScreenInMC.MGUI.Alignment;
import cn.mingbai.ScreenInMC.MGUI.Controls.MGIFImage;
import cn.mingbai.ScreenInMC.MGUI.MContainer;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;

public class speedTest extends MGUICore {

    public speedTest() {
        super("SpeedTest");
    }

    @Override
    public void onCreate(MContainer container) {
        try {
            URL url = new URL("https://img1.imgtp.com/2023/01/22/Sj2Bv3Ps.gif");
            InputStream stream = url.openStream();
            BufferedInputStream stream1 = new BufferedInputStream(stream);
            MGIFImage image = new MGIFImage(stream1);
            stream1.close();
            stream.close();
            image.setScaleMode(Image.SCALE_FAST);
            image.setHorizontalAlignment(Alignment.HorizontalAlignment.Stretch);
            image.setVerticalAlignment(Alignment.VerticalAlignment.Stretch);
            container.addChildControl(image);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
