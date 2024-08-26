package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import static cn.mingbai.ScreenInMC.Utils.CraftUtils.NMSMap.toShortId;

public class NMSMapOld extends NMSItemStack implements NMSMap{
    protected NMSMapOld() {
        super(new String[]{"filled_map","map"}, 1);
    }
    private int map=0;
    public void setMap(int map) {
        this.map = map;
        setOldId(toShortId(map));
    }
    @Override
    protected void setNbt(Object nbt) throws Exception {
        super.setNbt(nbt);
        if(!(CraftUtils.minecraftVersion<=12)) {
            Object mapNbt = NBTTagIntClassConstructor.newInstance(map);
            NBTTagCompoundPut.invoke(nbt,"map",mapNbt);
        }
    }

}
