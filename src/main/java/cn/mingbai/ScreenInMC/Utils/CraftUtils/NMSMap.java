package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import cn.mingbai.ScreenInMC.Utils.LangUtils;

public class NMSMap extends NMSItemStack{
    public NMSMap() {
        super(new String[]{"filled_map"}, 1);
    }
    private int map=0;

    public void setMap(int map) {
        this.map = map;
    }
    @Override
    protected void setNbt(Object nbt) throws Exception {
        super.setNbt(nbt);
        Object mapNbt = NBTTagIntClassConstructor.newInstance(map);
        NBTTagCompoundPut.invoke(nbt,"map",mapNbt);
    }
}
