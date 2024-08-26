package cn.mingbai.ScreenInMC.Utils.CraftUtils;

public interface NMSMap {
    static NMSMap create(){
        if(NMSItemStack.newVersionItem){
            return new NMSMapNew();
        }
        return new NMSMapOld();
    }
    Object getItemStack();
    void setMap(int map);
    static short toShortId(int id){
        short newId = (short) (id-6000000+13400);
        if(newId<0){
            newId+=42768;
        }
        return newId;
    }
}
