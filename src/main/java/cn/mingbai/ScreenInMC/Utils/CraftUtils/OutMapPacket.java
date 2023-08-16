package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import static cn.mingbai.ScreenInMC.Utils.CraftUtils.CraftUtils.getConstructor;
import static cn.mingbai.ScreenInMC.Utils.CraftUtils.CraftUtils.minecraftVersion;

public class OutMapPacket implements OutPacket{
    static Class PacketPlayOutMapClass;
    static Constructor PacketPlayOutMapConstructor;
    static Class MapDataClass;
    static Constructor MapDataConstructor;
    protected static void init() throws Exception{
        PacketPlayOutMapClass = CraftUtils.getMinecraftClass("PacketPlayOutMap");
        for(Constructor i:PacketPlayOutMapClass.getDeclaredConstructors()){
            if(i.getParameterCount()!=0 && !i.getParameters()[0].getType().getSimpleName().equals("PacketDataSerializer")){
                PacketPlayOutMapConstructor=i;
                PacketPlayOutMapConstructor.setAccessible(true);
            }
        }
        if(PacketPlayOutMapConstructor.getParameterCount() == 5) {
            MapDataClass=PacketPlayOutMapConstructor.getParameters()[4].getType();
            MapDataConstructor= getConstructor(MapDataClass);
            MapDataConstructor.setAccessible(true);
        }
    }
    private static byte[] mapDataTo128x128(byte[] colors,int startX,int startY,int width,int height){
        if(colors.length==16384){
            return colors;
        }
        byte[] newColors = new byte[16384];
        for(int y=0;y<height;y++){
            for(int x=0;x<width;x++) {
                newColors[(startY+y)*128+(startX+x)] = colors[y*width+x];
            }
        }
        return newColors;
    }
    public static Object create(int mapId,byte[] colors,int startX,int startY,int width,int height){
        try {
            //1.8-1.11
            if (PacketPlayOutMapConstructor.getParameterCount() == 8) {
                return PacketPlayOutMapConstructor.newInstance(NMSMap.toShortId(mapId), (byte) 0, new ArrayList<>(), mapDataTo128x128(colors,startX,startY,width,height), startX, startY, width, height);
            }
            //1.12-1.13
            if (PacketPlayOutMapConstructor.getParameterCount() == 9) {
                if(minecraftVersion<=12){
                    return PacketPlayOutMapConstructor.newInstance(NMSMap.toShortId(mapId), (byte) 0,false, new ArrayList<>(), mapDataTo128x128(colors,startX,startY,width,height), startX, startY, width, height);
                }else{
                    return PacketPlayOutMapConstructor.newInstance(mapId, (byte) 0,false, new ArrayList<>(), mapDataTo128x128(colors,startX,startY,width,height), startX, startY, width, height);
                }
            }
            //1.14
            if (PacketPlayOutMapConstructor.getParameterCount() == 10) {

                return PacketPlayOutMapConstructor.newInstance(mapId, (byte) 0,false,false, new ArrayList<>(), mapDataTo128x128(colors,startX,startY,width,height), startX, startY, width, height);
            }
            //1.17+
            if(PacketPlayOutMapConstructor.getParameterCount() == 5){
                Object data = MapDataConstructor.newInstance(startX, startY, width, height,colors);
                return PacketPlayOutMapConstructor.newInstance(mapId, (byte) 0,false, new ArrayList<>(), data);
            }

        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("PacketPlayOutMap create error.");
    }
}
