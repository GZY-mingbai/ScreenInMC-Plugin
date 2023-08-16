package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import cn.mingbai.ScreenInMC.Utils.LangUtils;

public class NMSMap extends NMSItemStack{
    public NMSMap() {
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
    public static short toShortId(int id){
        short newId = (short) (id-6000000+13400);
        if(newId<0){
            newId+=42768;
        }
        return newId;
    }
}
