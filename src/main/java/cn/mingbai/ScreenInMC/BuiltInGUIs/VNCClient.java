package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Controller.EditGUI;
import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.MGUI.*;
import cn.mingbai.ScreenInMC.MGUI.Controls.MButton;
import cn.mingbai.ScreenInMC.MGUI.Controls.MInput;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.ImmediatelyCancellableBukkitRunnable;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import com.shinyhut.vernacular.client.VernacularClient;
import com.shinyhut.vernacular.client.VernacularConfig;
import com.shinyhut.vernacular.client.exceptions.VncException;
import com.shinyhut.vernacular.client.rendering.ColorDepth;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.function.Consumer;

import static cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils.scaleImageAndGetPosition;

public class VNCClient extends Core {
    Image nowImage = null;
    ImmediatelyCancellableBukkitRunnable runnable;
    boolean update = false;
    private boolean isConnected = false;
    private VernacularClient client;
    private MContainer container;

    public VNCClient() {
        super("VNCClient");
    }

    @Override
    public StoredData createStoredData() {
        return new VNCClientStoredData();
    }
    private void createContainer(boolean clearData){
        if(clearData){
            VNCClientStoredData storedData = ((VNCClientStoredData)getStoredData());
            storedData.IP = "";
            storedData.password = "";
        }
        this.container = new MContainer(getScreen());
        container.setBackground(new Color(255, 255, 255));
        MInput IPInput = new MInput(LangUtils.getText("vnc-client-address"));
        IPInput.setHeight(64);
        IPInput.setWidth(256);
        IPInput.setPaddingLeft(32);
        MInput passwordInput = new MInput(LangUtils.getText("vnc-client-password"));
        passwordInput.setHeight(64);
        passwordInput.setWidth(256);
        passwordInput.setTop(72);
        passwordInput.setPaddingLeft(32);
        MButton connectButton = new MButton(LangUtils.getText("vnc-client-connect")) {
            @Override
            public void onClick(int x, int y, ClickType type) {
                super.onClick(x, y, type);
                BukkitRunnable thread = new BukkitRunnable() {

                    @Override
                    public void run() {
                        VNCClientStoredData storedData = ((VNCClientStoredData)getStoredData());
                        storedData.IP = IPInput.getText();
                        storedData.password = Base64.getEncoder().encodeToString(passwordInput.getText().getBytes(StandardCharsets.UTF_8));
                        connectServer(IPInput.getText(), passwordInput.getText());
                    }
                };
                thread.runTaskAsynchronously(Main.thisPlugin());
            }

        };
        connectButton.setHeight(64);
        connectButton.setWidth(128);
        connectButton.setTop(144);
        connectButton.setHorizontalAlignment(Alignment.HorizontalAlignment.Center);

        MControl control = new MControl();
        control.addChildControl(IPInput);
        control.addChildControl(passwordInput);
        control.addChildControl(connectButton);
        control.setWidth(256);
        control.setHeight(208);
        control.setVerticalAlignment(Alignment.VerticalAlignment.Center);
        control.setHorizontalAlignment(Alignment.HorizontalAlignment.Center);
        container.addChildControl(control);
        container.load();

    }
    @Override
    public void onUnload() {
        if (isConnected) {
            client.stop();
        }
        if(runnable!=null) {
            runnable.cancel();
        }
    }

    @Override
    public void onCreate() {
        setLock=new Object();
        imageMappingSetLock = new Object();
        if (getStoredData() != null) {
            try {
                VNCClientStoredData data = (VNCClientStoredData) getStoredData();
                if(data.IP.length()!=0) {
                    connectServer(data.IP, new String(Base64.getDecoder().decode(data.password), StandardCharsets.UTF_8));
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        createContainer(false);
    }


    public void onBell() {

    }
    private int imageMappingX;
    private int imageMappingY;
    private int imageMappingW;
    private int imageMappingH;
    private int imageMappingCW;
    private int imageMappingCH;
    private Object imageMappingSetLock;
    public void connectServer(String ip, String passwd) {
        try {
            if(container!=null) {
                container.unload();
                container = null;
            }
            if (client != null) {
                client.stop();
            }
            VernacularConfig config = new VernacularConfig();
            client = new VernacularClient(config);
            config.setColorDepth(ColorDepth.BPP_24_TRUE);
            config.setErrorListener(new Consumer<VncException>() {
                @Override
                public void accept(VncException e) {
                    e.printStackTrace();
                    if(!client.isRunning()){
                        isConnected=false;
                    }
                }
            });
            if (passwd.length() != 0) {
                config.setPasswordSupplier(() -> passwd);
            }
            config.setBellListener(v -> onBell());
            config.setUseLocalMousePointer(false);
            config.setScreenUpdateListener(image -> {
                synchronized (VNCClient.this) {
                    nowImage = image;
                    update = true;
                }
            });
            String[] ipPort = ip.split(":");
            if (ipPort.length == 1) {
                client.start(ipPort[0], 5900);
            } else {
                client.start(ipPort[0], Integer.parseInt(ipPort[1]));
            }
            isConnected = true;
            runnable = new ImmediatelyCancellableBukkitRunnable() {
                @Override
                public void run() {
                    VNCClientStoredData data = (VNCClientStoredData)getStoredData();
                    int toWidth = getScreen().getWidth()*128;
                    int toHeight = getScreen().getHeight()*128;
                    while (isConnected&&!this.isCancelled()) {
                        int fps = 20;
                        int scaleMode = 0;
                        if(setLock !=null) {
                            synchronized (setLock) {
                                if(data.frameRateLimit>=1&&data.frameRateLimit<=20) {
                                    fps = data.frameRateLimit;
                                }
                                scaleMode= data.scaleMode;
                            }
                        }
                        long timeStart = System.currentTimeMillis();
                        boolean send;
                        synchronized (VNCClient.this) {
                            send = nowImage != null && update;
                        }
                        if (send) {
                            Utils.Pair<BufferedImage, Rectangle2D.Float> scaled = scaleImageAndGetPosition(ImageUtils.imageToBufferedImage(nowImage),scaleMode,toWidth,toHeight,1);
                            synchronized (imageMappingSetLock){
                                imageMappingX = (int) scaled.getValue().getX();
                                imageMappingY = (int) scaled.getValue().getY();
                                imageMappingW = (int) scaled.getValue().getWidth();
                                imageMappingH = (int) scaled.getValue().getHeight();
                                imageMappingCW = nowImage.getWidth(null);
                                imageMappingCH = nowImage.getHeight(null);
                            }
                            getScreen().sendView(ImageUtils.imageToMapColors(scaled.getKey()),
                                    (int) scaled.getValue().x,
                                    (int) scaled.getValue().y,
                                    (int) scaled.getValue().width,
                                    (int) scaled.getValue().height
                            );
                            synchronized (VNCClient.this) {
                                update = false;
                            }
                        }

                        int time = (int) ((int)(1000/fps) - (System.currentTimeMillis() - timeStart));
                        if (time > 0) {
                            try {
                                Thread.sleep(time);
                            } catch (InterruptedException e) {
                                runnable=null;
                                if(!this.isCancelled()) {
                                    isConnected = false;
                                    update = true;
                                    createContainer(true);
                                }
                                return;
                            }
                        }
                    }
                    runnable=null;
                    if(!this.isCancelled()) {
                        createContainer(true);
                    }
                    isConnected = false;
                    update = true;

                }

            };
            runnable.runTaskAsynchronously(Main.thisPlugin());
        } catch (Exception e) {
            e.printStackTrace();
            isConnected = false;
            createContainer(true);
        }
    }

    @Override
    public void onMouseClick(int x, int y, Utils.MouseClickType type) {
        if (isConnected) {
            int actuallyX;
            int actuallyY;
            synchronized (imageMappingSetLock){
                int ax = x - imageMappingX;
                int ay = y - imageMappingY;
                actuallyX = ax*imageMappingCW/imageMappingW;
                actuallyY = ay*imageMappingCH/imageMappingH;
            }
            client.moveMouse(actuallyX, actuallyY);
            if (type == Utils.MouseClickType.LEFT) {
                client.click(1);
            }
            if (type == Utils.MouseClickType.RIGHT) {
                client.click(2);
            }
        } else if(container!=null){
            container.clickAt(x, y, ClickType.Left);
        }
    }

    @Override
    public void onTextInput(String text) {
        if (isConnected) {
            try {
                client.type(text);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(container!=null) {
            container.inputText(text);
        }
    }

    public static class VNCClientStoredData implements StoredData {
        public String IP="";
        public String password="";
        public int frameRateLimit=18;
        public int scaleMode=1;
        public VNCClientStoredData(){};

        public VNCClientStoredData(String IP, String password) {
            this.IP = IP;
            this.password = password;
        }

        @Override
        public StoredData clone() {
            VNCClientStoredData data = new VNCClientStoredData(this.IP,this.password);
            return data;
        }

    }
    @Override
    public void addToEditGUI() {
        EditGUI.registerCoreInfo(new EditGUI.EditGUICoreInfo(
                "@controller-editor-cores-vnc-name",
                this,
                "@controller-editor-cores-vnc-details",
                "blue",
                Material.DIAMOND_BLOCK,
                new LinkedHashMap(){
                    {
                        put("@controller-editor-cores-scale-mode", ImageViewer.ScaleModeSettingsList.class);
                        put("@controller-editor-cores-frame-rate-limit", Integer.class);
                    }
                }));
    }
    @Override
    public Object getEditGUISettingValue(String name) {
        VNCClientStoredData data = (VNCClientStoredData)getStoredData();
        switch (name){
            case "@controller-editor-cores-frame-rate-limit":
                return (int)data.frameRateLimit;
            case "@controller-editor-cores-scale-mode":
                return (int)data.scaleMode;
        }
        return null;
    }
    private Object setLock =null;
    @Override
    public void setEditGUISettingValue(String name, Object value) {
        VNCClientStoredData data = (VNCClientStoredData)getStoredData();
        switch (name) {
            case "@controller-editor-cores-frame-rate-limit":
                int v = (int)value;
                if(v>=1&&v<=20){
                    if(setLock !=null) {
                        synchronized (setLock) {
                            data.frameRateLimit = v;
                        }
                    }
                }
                break;
            case "@controller-editor-cores-scale-mode":
                if(setLock !=null) {
                    synchronized (setLock) {
                        data.scaleMode = (int) value;
                    }
                }
                break;
        }
    }

    @Override
    public void reRender() {
        if(container!=null){
            container.reRenderAll();
        }
    }
}
