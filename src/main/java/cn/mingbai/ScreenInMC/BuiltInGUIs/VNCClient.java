package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Cores.MGUICore;
import cn.mingbai.ScreenInMC.MGUI.*;
import cn.mingbai.ScreenInMC.MGUI.Controls.MButton;
import cn.mingbai.ScreenInMC.MGUI.Controls.MInput;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.Utils;
import com.shinyhut.vernacular.client.VernacularClient;
import com.shinyhut.vernacular.client.VernacularConfig;
import com.shinyhut.vernacular.client.exceptions.VncException;
import com.shinyhut.vernacular.client.rendering.ColorDepth;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.util.function.Consumer;

public class VNCClient extends MGUICore {

    @Override
    public void onCreate(MContainer container) {
        container.setBackground(new Color(255,255,255));
        MControl VNCControl = new MControl(){
            @Override
            public void onRender(MRenderer mRenderer) {
                if(nowImage!=null){
                    mRenderer.drawImage(nowImage,0,0);
                }
            }
        };
        VNCControl.setHorizontalAlignment(Alignment.HorizontalAlignment.Stretch);
        VNCControl.setVerticalAlignment(Alignment.VerticalAlignment.Stretch);
        MInput IPInput = new MInput(){
            @Override
            public void onActive() {
                super.onActive();
                Bukkit.broadcastMessage("active!");
            }
        };
        IPInput.setHeight(64);
        IPInput.setWidth(256);
        MInput passwordInput = new MInput();
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
        container.addRenderTask(new Runnable() {
            @Override
            public void run() {

            }
        });
    }
    public void onError(Exception e){
        e.printStackTrace();
    }
    public void onBell(){

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
            config.setColorDepth(ColorDepth.BPP_8_INDEXED);
            config.setErrorListener(new Consumer<VncException>() {
                @Override
                public void accept(VncException e) {
                    onError(e);
                }
            });
            config.setPasswordSupplier(() -> passwd);
            config.setBellListener(v -> onBell());
            config.setUseLocalMousePointer(false);
            config.setScreenUpdateListener(image -> {
                nowImage=image;
                getContainer().reRender();
            });
            String[] ipPort = ip.split(":");
            client.start(ipPort[0],Integer.parseInt(ipPort[1]));
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
