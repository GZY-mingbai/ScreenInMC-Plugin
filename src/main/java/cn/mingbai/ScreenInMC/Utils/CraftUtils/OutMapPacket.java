package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import static cn.mingbai.ScreenInMC.Utils.CraftUtils.CraftUtils.getConstructor;

public class OutMapPacket implements OutPacket{
    static Class PacketPlayOutMapClass;
    static Constructor PacketPlayOutMapConstructor;
    static Class MapDataClass;
    static Constructor MapDataConstructor;
    protected static void init() throws Exception{
        PacketPlayOutMapClass = CraftUtils.getMinecraftClass("PacketPlayOutMap");
        for(Constructor i:PacketPlayOutMapClass.getDeclaredConstructors()){
            if(!i.getParameters()[0].getClass().getSimpleName().equals("PacketDataSerializer")){
                PacketPlayOutMapConstructor=i;
            }
        }
        if(PacketPlayOutMapConstructor.getParameterCount() == 5) {
            MapDataClass=PacketPlayOutMapConstructor.getParameters()[4].getClass();
            MapDataConstructor= getConstructor(MapDataClass);
        }
    }
    public static Object create(int mapId,byte[] colors,int minX,int minY,int maxX,int maxY){
        try {
            //1.8-1.12
            if (PacketPlayOutMapConstructor.getParameterCount() == 8) {
                return PacketPlayOutMapConstructor.newInstance(mapId, (byte) 0, new ArrayList<>(), colors, minX, minY, maxX, maxY);
            }
            //1.13
            if (PacketPlayOutMapConstructor.getParameterCount() == 9) {
                return PacketPlayOutMapConstructor.newInstance(mapId, (byte) 0,false, new ArrayList<>(), colors, minX, minY, maxX, maxY);
            }
            //1.14
            if (PacketPlayOutMapConstructor.getParameterCount() == 10) {
                return PacketPlayOutMapConstructor.newInstance(mapId, (byte) 0,false,false, new ArrayList<>(), colors, minX, minY, maxX, maxY);
            }
            //1.17+
            if(PacketPlayOutMapConstructor.getParameterCount() == 5){
                Object data = MapDataConstructor.newInstance(minX, minY, maxX, maxY,colors);
                return PacketPlayOutMapConstructor.newInstance(mapId, (byte) 0,false, new ArrayList<>(), data);
            }

        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("PacketPlayOutMap create error.");
    }
}
