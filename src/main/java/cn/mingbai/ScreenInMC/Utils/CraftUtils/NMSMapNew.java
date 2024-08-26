package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import java.lang.reflect.Constructor;

public class NMSMapNew extends NMSItemStackNew implements NMSMap {
    static Class MapIdClass;
    static Constructor MapIdConstructor;
    static Object MapIdDataComponentType;
    public static void init() throws Exception{
        MapIdClass = CraftUtils.getMinecraftClass("MapId");
        MapIdConstructor = MapIdClass.getDeclaredConstructor(int.class);
        MapIdDataComponentType = NMSItemStackNew.getDataComponentType("map_id");
    }
    private int map=0;
    protected NMSMapNew() {
        super(new String[]{"filled_map","map"}, 1);
    }
    @Override
    protected void setNbtNew(Object itemStack) {
        super.setNbtNew(itemStack);
        try {
            Object patchedDataComponentMap = DataComponentHolderGetComponents.invoke(itemStack);
            PatchedDataComponentMapSet.invoke(patchedDataComponentMap,MapIdDataComponentType,MapIdConstructor.newInstance(map));
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setMap(int map) {
        this.map = map;
    }
}
