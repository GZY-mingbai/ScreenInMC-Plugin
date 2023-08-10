package cn.mingbai.ScreenInMC.Utils.CraftUtils;

public interface OutPacket {
    static void initAll() throws Exception{
        OutMapPacket.init();
        OutAddMapEntityPacket.init();
        OutSetMapEntityPacket.init();
        OutActionBarPacket.init();
        OutSetSlotPacket.init();
        OutWindowDataPacket.init();
        OutOpenWindowPacket.init();
        OutOpenBookPacket.init();
        OutWindowItemsPacket.init();
        OutSystemMessagePacket.init();
    }
}
