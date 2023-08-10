package cn.mingbai.ScreenInMC.Utils.CraftUtils;

public interface InPacket {
    Class getNMSClass();
    static void initAll() throws Exception {
        InWindowClosePacket.init();
        InWindowClickPacket.init();
        InAnvilRenamePacket.init();
    }
    boolean load(Object obj);
    static InPacket create(Object obj){
        InPacket inPacket;
        inPacket = new InWindowClosePacket();
        if(obj.getClass().equals(inPacket.getNMSClass())) if(inPacket.load(obj)) return inPacket;
        inPacket = new InWindowClickPacket();
        if(obj.getClass().equals(inPacket.getNMSClass())) if(inPacket.load(obj)) return inPacket;
        inPacket = new InAnvilRenamePacket();
        if(obj.getClass().equals(inPacket.getNMSClass())) if(inPacket.load(obj)) return inPacket;
        return null;
    }
}
