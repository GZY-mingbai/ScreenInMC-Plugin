package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import cn.mingbai.ScreenInMC.Utils.Utils;

import java.lang.reflect.Field;

public class InWindowClosePacket implements InPacket{
    static Class PacketPlayInCloseWindowClass;
    static Object packet;
    static Field containerId;
    protected static void init() throws Exception {
        PacketPlayInCloseWindowClass = CraftUtils.getMinecraftClass("PacketPlayInCloseWindow");
        if(PacketPlayInCloseWindowClass==null){
            PacketPlayInCloseWindowClass = CraftUtils.getMinecraftClass("ServerboundContainerClosePacket");
        }
        for(Field i:PacketPlayInCloseWindowClass.getDeclaredFields()){
            if(i.getType().equals(int.class)){
                containerId = i;
                containerId.setAccessible(true);
            }
        }
    }

    @Override
    public Class getNMSClass() {
        return PacketPlayInCloseWindowClass;
    }

    @Override
    public boolean load(Object obj) {
        this.packet=obj;
        return true;
    }

    public int getContainerId() {
        try {
            return Utils.getInt(containerId.get(packet));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
