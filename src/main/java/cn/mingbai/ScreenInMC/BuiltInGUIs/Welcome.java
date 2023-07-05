package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Controller.EditGUI;
import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.MGUI.*;
import cn.mingbai.ScreenInMC.MGUI.Controls.MBorder;
import cn.mingbai.ScreenInMC.MGUI.Controls.MButton;
import cn.mingbai.ScreenInMC.MGUI.Controls.MTextBlock;
import cn.mingbai.ScreenInMC.RedstoneBridge;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.awt.*;
import java.util.LinkedHashMap;

public class Welcome extends MGUICore {
    public Welcome() {
        super("Welcome");
    }
    @Override
    public void onCreate(MContainer container) {
        container.setBackground(new Color(255, 255, 255));
        MTextBlock textBlock= new MTextBlock("请在服务器关闭后修改ScreenInMC/screens.json");
        textBlock.setVerticalAlignment(Alignment.VerticalAlignment.Stretch);
        textBlock.setHorizontalAlignment(Alignment.HorizontalAlignment.Stretch);
        final Core core = this;
        MButton button = new MButton("点我同步红石信号"){
            @Override
            public void onClick(int x, int y, ClickType type) {
                super.onClick(x, y, type);
                core.getRedstoneBridge().getRedstoneSignalInterface("World").sendRedstoneSignal(strength);

            }
        };
        button.setHeight(128);
        button.setWidth(256);
        container.addChildControl(button);
        container.addChildControl(textBlock);
    }
    public static class WelcomeSettingsList implements EditGUI.EditGUICoreInfo.EditGUICoreSettingsList {
        @Override
        public String[] getList() {
            return new String[]{
                    "2333","2334"
            };
        }

    }
    @Override
    public void addToEditGUI() {
        EditGUI.registerCoreInfo(new EditGUI.EditGUICoreInfo(
                "欢迎光临ScreenInMC (ノ｀Д)ノ",
                this,
                this.getClass().getName(),
                "gold",
                Material.TNT,
                new LinkedHashMap<>(){
                    {
                        put("\u2517|\uff40O\u2032|\u251b",Integer.class);
                        put("\u2267 \ufe4f \u2266",Double.class);
                        put("(T_T)",Boolean.class);
                        put("(\u256f\u2594\u76bf\u2594)\u256f",String.class);
                        put("\uffe3\u3078\uffe3",String[].class);
                        put("(\uffe3_,\uffe3 )", Location.class);
                        put("(\u02c9\u25bd\u02c9\uff1b)...", WelcomeSettingsList.class);
                        put("(p\u2267w\u2266q)", MinecraftClient.class);
                        put("?????", Vector.class);
                        for(int i=0;i<1000;i++){
                            put("i"+i,Boolean.class);
                        }
                    }
                }));
    }

    @Override
    public Object getEditGUISettingValue(String name) {
        switch (name){
            case "\u2517|\uff40O\u2032|\u251b":
                return 114514;
            case "\u2267 \ufe4f \u2266":
                return 114.514;
            case "(T_T)":
                return false;
            case "(\u256f\u2594\u76bf\u2594)\u256f":
                return "Hello World?";
            case "\uffe3\u3078\uffe3":
                return new String[]{"Hello?","World?"};
            case "(\uffe3_,\uffe3 )":
                return new Location(Bukkit.getWorld("world"),11,45,14);
            case "(\u02c9\u25bd\u02c9\uff1b)...":
                return 0;
            case "?????":
                return new Vector(11,45,14);
        }
        return null;
    }

    @Override
    public void setEditGUISettingValue(String name, Object value) {
        if(value instanceof String[]){
            Bukkit.broadcastMessage(name+" set to: [");
            for(String i:(String[]) value){
                Bukkit.broadcastMessage("   \""+i+"\",");
            }
            Bukkit.broadcastMessage("]");
        }else
        Bukkit.broadcastMessage(name+" set to: "+value);
    }

    @Override
    public StoredData createStoredData() {
        return null;
    }
    private int strength = 0;
    @Override
    public void registerRedstoneBridge() {
        getRedstoneBridge().addRedstoneSignalInterface("World", new RedstoneBridge.RedstoneSignalInterface(false) {

        });
        getRedstoneBridge().addRedstoneSignalInterface("Hello", new RedstoneBridge.RedstoneSignalInterface(true) {
            @Override
            public void onReceiveRedstoneSignal(Core core, int strength) {
                ((Welcome) core).strength = strength;
                for(MControl c:((Welcome)core).getContainer().getChildControls()) {
                    if (c instanceof MTextBlock && !(c instanceof MButton)) {
                        ((MTextBlock)c).setText("当前红石信号强度: " + strength);
                    }
                }
            }
        });

    }
}
