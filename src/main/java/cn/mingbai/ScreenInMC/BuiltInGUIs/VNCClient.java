package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Browsers.Browser;
import cn.mingbai.ScreenInMC.Controller.EditGUI;
import cn.mingbai.ScreenInMC.MGUI.*;
import cn.mingbai.ScreenInMC.MGUI.Controls.MButton;
import cn.mingbai.ScreenInMC.MGUI.Controls.MInput;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import com.google.gson.internal.LinkedTreeMap;
import com.shinyhut.vernacular.client.VernacularClient;
import com.shinyhut.vernacular.client.VernacularConfig;
import com.shinyhut.vernacular.client.exceptions.VncException;
import com.shinyhut.vernacular.client.rendering.ColorDepth;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

public class VNCClient extends MGUICore {
    boolean directMode = true;
    Image nowImage = null;
    BukkitRunnable runnable;
    boolean update = false;
    private MControl control = null;
    private boolean isConnected = false;
    private VernacularClient client;
    private boolean sending = false;

    public VNCClient() {
        super("VNCClient");
    }

    @Override
    public StoredData createStoredData() {
        return new VNCClientStoredData();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (isConnected) {
            client.stop();
        }
    }

    //    private MControl VNCControl; }
    @Override
    public void onCreate(MContainer container) {
        setLock=new Object();

        container.setBackground(new Color(255, 255, 255));
        MInput IPInput = new MInput(LangUtils.getText("vnc-client-address")) {
            @Override
            public void onActive() {
                super.onActive();
            }

            @Override
            public void onRender(MRenderer mRenderer) {
                if (!isConnected) {
                    super.onRender(mRenderer);
                }
            }
        };
        IPInput.setHeight(64);
        IPInput.setWidth(256);
        IPInput.setPaddingLeft(32);
        MInput passwordInput = new MInput(LangUtils.getText("vnc-client-password")) {
            @Override
            public void onRender(MRenderer mRenderer) {
                if (!isConnected) {
                    super.onRender(mRenderer);
                }
            }
        };
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

            @Override
            public void onRender(MRenderer mRenderer) {
                if (!isConnected) {
                    super.onRender(mRenderer);
                }
            }
        };
        connectButton.setHeight(64);
        connectButton.setWidth(128);
        connectButton.setTop(144);
        connectButton.setHorizontalAlignment(Alignment.HorizontalAlignment.Center);

        control = new MControl();
        control.addChildControl(IPInput);
        control.addChildControl(passwordInput);
        control.addChildControl(connectButton);
        control.setWidth(256);
        control.setHeight(208);
        control.setVerticalAlignment(Alignment.VerticalAlignment.Center);
        control.setHorizontalAlignment(Alignment.HorizontalAlignment.Center);
        container.addChildControl(control);
        if (getStoredData() != null) {
            try {
                VNCClientStoredData data = (VNCClientStoredData) getStoredData();
                if(data.IP.length()!=0) {
                    connectServer(data.IP, new String(Base64.getDecoder().decode(data.password), StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onError(Exception e) {
        e.printStackTrace();
    }

    public void onBell() {

    }

    public void connectServer(String ip, String passwd) {
        try {
            if (control != null) {
                control.setVisible(false);
            }
            if (client != null) {
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
            if (passwd.length() != 0) {
                config.setPasswordSupplier(() -> passwd);
            }
            config.setBellListener(v -> onBell());
            config.setUseLocalMousePointer(false);
            config.setScreenUpdateListener(image -> {
                synchronized (this) {
                    if (!sending) {
                        nowImage = image;
                        update = true;
                    }
                }
            });
            String[] ipPort = ip.split(":");
            if (ipPort.length == 1) {
                client.start(ipPort[0], 5900);
            } else {
                client.start(ipPort[0], Integer.parseInt(ipPort[1]));
            }
            isConnected = true;
            runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    VNCClientStoredData data = (VNCClientStoredData)getStoredData();
                    while (getContainer().isLoaded() && isConnected) {
                        long timeStart = System.currentTimeMillis();
                        boolean send;
                        synchronized (this) {
                            send = directMode && nowImage != null && update;
                        }
                        if (send) {
                            synchronized (this) {
                                sending = true;
                            }
                            int w = nowImage.getWidth(null);
                            int h = nowImage.getHeight(null);
                            boolean subImage = false;
                            if (w > getContainer().getWidth()) {
                                w = (int) getContainer().getWidth();
                                subImage = true;
                            }
                            if (h > getContainer().getHeight()) {
                                h = (int) getContainer().getHeight();
                                subImage = true;
                            }
                            Image image = nowImage;
                            if (subImage) {
                                image = ImageUtils.imageToBufferedImage(image).getSubimage(0, 0, w, h);
                            }
                            getScreen().sendView(ImageUtils.imageToMapColors(image), 0, 0, w, h);
                            synchronized (this) {
                                sending = false;
                                update = false;
                            }
                        }
                        int fps = 20;
                        if(setLock !=null&&data.frameRateLimit>=1&&data.frameRateLimit<=20) {
                            synchronized (setLock) {
                                fps=data.frameRateLimit;
                            }
                        }
                        int time = (int) ((int)(1000/fps) - (System.currentTimeMillis() - timeStart));
                        if (time > 0) {
                            try {
                                Thread.sleep(time);
                            } catch (InterruptedException e) {
                                if (control != null) {
                                    directMode = false;
                                    isConnected = false;
                                    update = true;
                                    control.setVisible(true);
                                }
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    if (control != null) {
                        directMode = false;
                        isConnected = false;
                        update = true;
                        control.setVisible(true);
                    }
                }

            };
            runnable.runTaskAsynchronously(Main.thisPlugin());
        } catch (Exception e) {
            onError(e);
            if (control != null) {
                directMode = false;
                isConnected = false;
                update = true;
                control.setVisible(true);
            }
        }
    }

    @Override
    public void onMouseClick(int x, int y, Utils.MouseClickType type) {
        if (isConnected) {
            client.moveMouse(x, y);
            if (type == Utils.MouseClickType.LEFT) {
                client.click(1);
            }
            if (type == Utils.MouseClickType.RIGHT) {
                client.click(2);
            }
        } else {
            super.onMouseClick(x, y, type);
        }
    }

    @Override
    public void onTextInput(String text) {
        if (isConnected) {
            try {
                client.type(text);
            } catch (Exception e) {
                onError(e);
            }
        } else {
            super.onTextInput(text);
        }
    }

    public static class VNCClientStoredData implements StoredData {
        public String IP="";
        public String password="";
        public int frameRateLimit=18;
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

        @Override
        public Object getStorableObject() {
            return this;
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
                new LinkedHashMap<>(){
                    {
                        put("@controller-editor-cores-frame-rate-limit", Integer.class);
                    }
                }));
    }
    @Override
    public Object getEditGUISettingValue(String name) {
        switch (name){
            case "@controller-editor-cores-frame-rate-limit":
                VNCClientStoredData data = (VNCClientStoredData)getStoredData();
                return (int)data.frameRateLimit;
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
        }
    }

}
