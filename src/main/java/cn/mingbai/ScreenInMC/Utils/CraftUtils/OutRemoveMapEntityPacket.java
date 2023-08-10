package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import java.lang.reflect.Constructor;

public class OutRemoveMapEntityPacket implements OutPacket{
    static Class PacketPlayOutEntityDestroyClass;
    static Constructor PacketPlayOutEntityDestroyConstructor;
    protected static void init() throws Exception {
        PacketPlayOutEntityDestroyClass = CraftUtils.getMinecraftClass("PacketPlayOutEntityDestroy");
        PacketPlayOutEntityDestroyConstructor = PacketPlayOutEntityDestroyClass.getDeclaredConstructor(int[].class);
    }
    public static Object create(int[] entityIds){
        try {
            return PacketPlayOutEntityDestroyConstructor.newInstance(entityIds);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
