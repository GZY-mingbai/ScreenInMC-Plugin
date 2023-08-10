package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class OutWindowItemsPacket implements OutPacket{
    static Class PacketPlayOutWindowItemsClass;
    static Constructor PacketPlayOutWindowItemsConstructor;
    static Class NonNullListClass;
    static Constructor NonNullListConstructor;
    static int method = 0;
    protected static void init() throws Exception {
        PacketPlayOutWindowItemsClass = CraftUtils.getMinecraftClass("PacketPlayOutWindowItems");
        try {
            PacketPlayOutWindowItemsConstructor=PacketPlayOutWindowItemsClass.getDeclaredConstructor(int.class, List.class);
            method = 0;
        }catch (Exception e){
            NonNullListClass = CraftUtils.getMinecraftClass("NonNullList");
            NonNullListConstructor = NonNullListClass.getDeclaredConstructor(List.class,Object.class);
            NonNullListConstructor.setAccessible(true);
            try {
                PacketPlayOutWindowItemsConstructor=PacketPlayOutWindowItemsClass.getDeclaredConstructor(int.class, NonNullListClass);
                method = 1;
            }catch (Exception er){
                PacketPlayOutWindowItemsConstructor=PacketPlayOutWindowItemsClass.getDeclaredConstructor(int.class, int.class,NonNullListClass,NMSItemStack.ItemStackClass);
                method = 2;
            }
        }
    }
    public static Object create(int containerId, List<NMSItemStack> items) {
        return create(containerId,0,items);
    }
    public static Object create(int containerId, int stateId, List<NMSItemStack> oldItems){
        List<Object> items = new ArrayList<>();
        for(NMSItemStack i:oldItems){
            items.add(i.getItemStack());
        }
        try {
            if(method==0){
                return PacketPlayOutWindowItemsConstructor.newInstance(containerId,items);
            }
            if(method==1){
                return PacketPlayOutWindowItemsConstructor.newInstance(containerId,NonNullListConstructor.newInstance(items,null));
            }
            if(method==2){
                return PacketPlayOutWindowItemsConstructor.newInstance(containerId,stateId,NonNullListConstructor.newInstance(items,null),NMSItemStack.EMPTY.getItemStack());
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        throw new RuntimeException();
    }
}
