package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import java.lang.reflect.Constructor;

public class OutCloseWindowPacket implements OutPacket{
    static Constructor PacketPlayOutCloseWindowConstructor;
    static Class PacketPlayOutCloseWindowClass;
    protected static void init() throws Exception {
        PacketPlayOutCloseWindowClass = CraftUtils.getMinecraftClass("PacketPlayOutCloseWindow");
        PacketPlayOutCloseWindowConstructor = PacketPlayOutCloseWindowClass.getDeclaredConstructor(int.class);
    }
    public static Object create(int windowID){
        try {
            return PacketPlayOutCloseWindowConstructor.newInstance(windowID);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
