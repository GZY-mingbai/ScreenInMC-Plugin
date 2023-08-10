package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import java.lang.reflect.Constructor;

public class OutSetSlotPacket implements OutPacket{
    static Constructor PacketPlayOutSetSlotConstructor;
    static Class PacketPlayOutSetSlotClass;
    static int method=0;
    protected static void init() throws Exception {
        PacketPlayOutSetSlotClass = CraftUtils.getMinecraftClass("PacketPlayOutSetSlot");
        try {
            PacketPlayOutSetSlotConstructor = PacketPlayOutSetSlotClass.getDeclaredConstructor(int.class,int.class,NMSItemStack.ItemStackClass);
            method=0;
        }catch (Exception e){
            PacketPlayOutSetSlotConstructor = PacketPlayOutSetSlotClass.getDeclaredConstructor(int.class,int.class,int.class,NMSItemStack.ItemStackClass);
            method=1;
        }
    }
    public static Object create(int windowID,int slotID,Object itemStack) {
        try {
            switch (method){
                case 0:
                    return PacketPlayOutSetSlotConstructor.newInstance(windowID,slotID,itemStack);
                case 1:
                    return PacketPlayOutSetSlotConstructor.newInstance(windowID,0,slotID,itemStack);
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        throw new RuntimeException();
    }
    public static Object create(int windowID,int slotID,NMSItemStack itemStack){
        return create(windowID,slotID,itemStack.getItemStack());
    }
}
