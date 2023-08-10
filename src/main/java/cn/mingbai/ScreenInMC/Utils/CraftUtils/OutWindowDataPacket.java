package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import java.lang.reflect.Constructor;

public class OutWindowDataPacket implements OutPacket{
    static Class PacketPlayOutWindowDataClass;
    static Constructor PacketPlayOutWindowDataConstructor;
    protected static void init() throws Exception {
        PacketPlayOutWindowDataClass = CraftUtils.getMinecraftClass("PacketPlayOutWindowData");
        PacketPlayOutWindowDataConstructor = PacketPlayOutWindowDataClass.getDeclaredConstructor(int.class,int.class,int.class);
    }
    public static Object create(int a,int b,int c){
        try {
            return PacketPlayOutWindowDataConstructor.newInstance(a,b,c);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
