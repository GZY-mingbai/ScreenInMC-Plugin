package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.MGUI.*;
import cn.mingbai.ScreenInMC.MGUI.Controls.MTextBlock;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.ImmediatelyCancellableBukkitRunnable;
import cn.mingbai.ScreenInMC.Utils.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class Welcome extends Core {
    public Welcome() {
        super("Welcome");
    }
    @Override
    public void onCreate() {
        runnable = new ImmediatelyCancellableBukkitRunnable() {
            @Override
            public void run() {
                sendFrame(1);
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    runnable=null;
                    return;
                }
                for(int i=2;i<21;i++){
                    sendFrame(i);
                    try {
                        Thread.sleep(50);
                    } catch (Exception e) {
                        runnable=null;
                        return;
                    }
                }
                for(int i=21;i<55;i++){
                    sendFrame(i);
                    try {
                        Thread.sleep(50);
                    } catch (Exception e) {
                        runnable=null;
                        return;
                    }
                }
                runnable=null;
            }
        };
        runnable.runTaskAsynchronously(Main.thisPlugin());
    }

    @Override
    public void reRender() {
        if(getScreen()!=null&&runnable==null){
            sendFrame(54);
        }
    }

    @Override
    public void onMouseClick(int x, int y, Utils.MouseClickType type) {

    }

    @Override
    public void onTextInput(String text) {

    }

    ImmediatelyCancellableBukkitRunnable runnable=null;

    @Override
    public StoredData createStoredData() {
        return null;
    }

    @Override
    public void onUnload() {
        if(runnable!=null){
            runnable.cancel();
        }
        while (runnable!=null){
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void addToEditGUI() {

    }
    private void sendFrame(int index){
        if(!getScreen().canSleep()) return;
        BufferedImage frame = readFrame(index);
        Utils.Pair<BufferedImage, Rectangle2D.Float> image = ImageUtils.scaleImageAndGetPosition(frame,1,
                getScreen().getWidth()*128,getScreen().getHeight()*128,1
                );
        getScreen().sendView(ImageUtils.imageToMapColors(image.getKey()),
                (int) image.getValue().x,
                (int) image.getValue().y,
                (int) image.getValue().width,
                (int) image.getValue().height
        );

    }
    private static BufferedImage readFrame(int index){
        try {
            InputStream stream = Main.class.getResourceAsStream("/builtin/welcome/welcome-"+index+".jpg");
            BufferedImage image = ImageIO.read(stream);
            stream.close();
            return image;
        } catch (Exception e) {
            return null;
        }
    }
}
