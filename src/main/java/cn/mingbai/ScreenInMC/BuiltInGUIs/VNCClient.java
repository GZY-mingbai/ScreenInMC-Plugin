package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Cores.MGUICore;
import cn.mingbai.ScreenInMC.MGUI.*;
import cn.mingbai.ScreenInMC.MGUI.Controls.MButton;
import cn.mingbai.ScreenInMC.MGUI.Controls.MInput;
import cn.mingbai.ScreenInMC.MGUI.Controls.MTextBlock;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import com.shinyhut.vernacular.client.VernacularClient;
import com.shinyhut.vernacular.client.VernacularConfig;
import com.shinyhut.vernacular.client.exceptions.VncException;
import com.shinyhut.vernacular.client.rendering.ColorDepth;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.function.Consumer;

import static java.awt.Font.PLAIN;

public class VNCClient extends MGUICore {
    public VNCClient() {
        super("VNCClient");
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if(isConnected){
            client.stop();
        }
    }
    private MControl VNCControl;
    @Override
    public void onCreate(MContainer container) {
        container.setBackground(new Color(255,255,255));
        VNCControl = new MControl(){
            @Override
            public void reRender() {
                MContainer container = getMContainer();
                if(container!=null&&nowImage!=null) {
                    int width = nowImage.getWidth(null);
                    int height = nowImage.getHeight(null);
                    if(width>container.getWidth()){
                        width= (int) container.getWidth();
                    }
                    if(height>container.getHeight()){
                        height= (int) container.getHeight();
                    }
                    container.addReRender(new Rectangle2D.Double(0,0,width,height));
                    container.reRender();
                }
            }

            @Override
            public void onRender(MRenderer mRenderer) {
                if(isConnected&&nowImage!=null){
                    mRenderer.drawImage(nowImage,0,0);
                }
            }
        };
        VNCControl.setHeight(container.getHeight());
        VNCControl.setWidth(container.getWidth());
        MInput IPInput = new MInput(){
            @Override
            public void onActive() {
                super.onActive();
                Bukkit.broadcastMessage("active!");
            }

            @Override
            public void onRender(MRenderer mRenderer) {
                if(!isConnected){
                    super.onRender(mRenderer);
                }
            }
        };
        IPInput.setHeight(64);
        IPInput.setWidth(256);
        MInput passwordInput = new MInput(){
            @Override
            public void onRender(MRenderer mRenderer) {
                if(!isConnected){
                    super.onRender(mRenderer);
                }
            }
        };
        passwordInput.setHeight(64);
        passwordInput.setWidth(256);
        passwordInput.setTop(64);
        MButton connectButton = new MButton("链接"){
            @Override
            public void onClick(int x, int y, ClickType type) {
                super.onClick(x, y, type);
                BukkitRunnable thread = new BukkitRunnable(){

                    @Override
                    public void run() {
                        connectServer(IPInput.getText(),passwordInput.getText());
                    }
                };
                thread.runTaskAsynchronously(Main.thisPlugin());
            }
            @Override
            public void onRender(MRenderer mRenderer) {
                if(!isConnected){
                    super.onRender(mRenderer);
                }
            }
        };
        connectButton.setForeground(new Color(255,255,255));
        connectButton.setBackground(new Color(100,100,100));
        connectButton.setHeight(64);
        connectButton.setWidth(128);
        connectButton.setTop(128);
        container.addChildControl(IPInput);
        container.addChildControl(passwordInput);
        container.addChildControl(connectButton);
        container.addChildControl(VNCControl);
        MTextBlock mTextBlock = new MTextBlock1("0 FPS");
        mTextBlock.setTextHorizontalAlignment(Alignment.HorizontalAlignment.Left);
        mTextBlock.setTextVerticalAlignment(Alignment.VerticalAlignment.Top);
        mTextBlock.setLeft(0);
        mTextBlock.setTop(0);
        mTextBlock.setHeight(128);
        mTextBlock.setWidth(256);
        mTextBlock.setForeground(new Color(255,0,0));
        time = System.currentTimeMillis();
        mTextBlock.addRenderTask(new Runnable() {
            @Override
            public void run() {
                thread = new Thread(()->{
                    try {
                        synchronized (container.getRenderLock()) {
                            container.getRenderLock().wait();
                        }
                        long newTime = System.currentTimeMillis();
                        if(newTime-time>=20) {
                            mTextBlock.setText(Math.round((1f / (((float) (newTime - time)) / 1000f))) + " FPS");
                        }
                        time = System.currentTimeMillis();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
            }
        });
        container.addChildControl(mTextBlock);
    }
    public void onError(Exception e){
        e.printStackTrace();
    }
    public void onBell(){

    }
    Thread thread;
    long time;
    public class MTextBlock1 extends MTextBlock{
        public MTextBlock1(String text){
            super(text);
        }

        @Override
        public synchronized void setText(String text) {
            this.text = text;
        }

        @Override
        public void onUnload() {
            super.onUnload();
            thread.interrupt();
        }
    }
    private boolean isConnected =false;
    private VernacularClient client;
    private Image nowImage;

    public void connectServer(String ip, String passwd){
        try {
            if(client!=null){
                client.stop();
            }
            VernacularConfig config = new VernacularConfig();
            client = new VernacularClient(config);
            config.setColorDepth(ColorDepth.BPP_16_TRUE);
            config.setErrorListener(new Consumer<VncException>() {
                @Override
                public void accept(VncException e) {
                    onError(e);
                }
            });
            if(passwd.length()!=0){
                config.setPasswordSupplier(() -> passwd);
            }
            config.setBellListener(v -> onBell());
            config.setUseLocalMousePointer(false);
            config.setScreenUpdateListener(image -> {
                if(VNCControl!=null) {
                    nowImage=image;
                    VNCControl.reRender();
                }
            });
            String[] ipPort = ip.split(":");
            if(ipPort.length==1){
                client.start(ipPort[0],5900);
            }else{
                client.start(ipPort[0],Integer.parseInt(ipPort[1]));
            }
            isConnected=true;
        }catch (Exception e){
            onError(e);
        }
    }

    @Override
    public void onMouseClick(int x, int y, Utils.MouseClickType type) {
        if(isConnected){
            client.moveMouse(x, y);
            if(type== Utils.MouseClickType.LEFT){
                client.click(1);
            }
            if(type== Utils.MouseClickType.RIGHT){
                client.click(2);
            }
        }else{
            super.onMouseClick(x, y, type);
        }
    }

    @Override
    public void onTextInput(String text) {
        if(isConnected){
            try {
                client.type(text);
            }catch (Exception e){
                onError(e);
            }
        }else{
            super.onTextInput(text);
        }
    }
}
