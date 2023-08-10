package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class OutOpenWindowPacket implements OutPacket{
    static Class IChatBaseComponentClass;
    static Class PacketPlayOutOpenWindowClass;
    static Constructor PacketPlayOutOpenWindowConstructor;
    static Class ContainersClass;
    static byte method=0;
    static Object Generic9x1=null;
    static Object Generic9x2=null;
    static Object Generic9x3=null;
    static Object Generic9x4=null;
    static Object Generic9x5=null;
    static Object Generic9x6=null;
    static Object Anvil=null;
    static Object Hopper=null;

    protected static void init() throws Exception {
        PacketPlayOutOpenWindowClass = CraftUtils.getMinecraftClass("PacketPlayOutOpenWindow");
        IChatBaseComponentClass = CraftUtils.getMinecraftClass("IChatBaseComponent");
        try {
            ContainersClass = CraftUtils.getMinecraftClass("Containers");
        }catch (Exception e){
        }
        try {
            PacketPlayOutOpenWindowConstructor = PacketPlayOutOpenWindowClass.getDeclaredConstructor(int.class,String.class,IChatBaseComponentClass);
            method=0;
        }catch (Exception e){
            PacketPlayOutOpenWindowConstructor = PacketPlayOutOpenWindowClass.getDeclaredConstructor(int.class,ContainersClass,IChatBaseComponentClass);
            method=1;
        }
        for(Field i:ContainersClass.getDeclaredFields()){
            if(i.getGenericType().getTypeName().equals("ContainerChest")){
                if(Generic9x1==null) Generic9x1=i.get(null);
                if(Generic9x2==null) Generic9x2=i.get(null);
                if(Generic9x3==null) Generic9x3=i.get(null);
                if(Generic9x4==null) Generic9x4=i.get(null);
                if(Generic9x5==null) Generic9x5=i.get(null);
                if(Generic9x6==null) Generic9x6=i.get(null);
            }
            if(i.getGenericType().getTypeName().equals("ContainerAnvil")) {
                Anvil = i.get(null);
            }
            if(i.getGenericType().getTypeName().equals("ContainerHopper")) {
                Hopper = i.get(null);
            }
        }
    }
    public static Object create(int id, String type, Object title){
        try {
            if(method==0){
                return PacketPlayOutOpenWindowConstructor.newInstance(id,"minecraft:"+type,title);
            }
            if(method==1){
                switch (type){
                    case "generic_9x1":
                        return PacketPlayOutOpenWindowConstructor.newInstance(id,Generic9x1,title);
                    case "generic_9x2":
                        return PacketPlayOutOpenWindowConstructor.newInstance(id,Generic9x2,title);
                    case "generic_9x3":
                        return PacketPlayOutOpenWindowConstructor.newInstance(id,Generic9x3,title);
                    case "generic_9x4":
                        return PacketPlayOutOpenWindowConstructor.newInstance(id,Generic9x4,title);
                    case "generic_9x5":
                        return PacketPlayOutOpenWindowConstructor.newInstance(id,Generic9x5,title);
                    case "generic_9x6":
                        return PacketPlayOutOpenWindowConstructor.newInstance(id,Generic9x6,title);
                    case "anvil":
                        return PacketPlayOutOpenWindowConstructor.newInstance(id,Anvil,title);
                    case "hopper":
                        return PacketPlayOutOpenWindowConstructor.newInstance(id,Hopper,title);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
