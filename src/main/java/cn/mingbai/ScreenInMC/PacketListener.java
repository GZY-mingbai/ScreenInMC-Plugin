package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Utils.CraftUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.protocol.Packet;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PacketListener {
    public static void removeGlobalListener(Player player) {
        try {
            CraftUtils.getConnection(player).connection.channel.pipeline().remove(ScreenInMCPacketHandlerName);
        }catch (Exception e){}
    }

    public interface PacketHandler{
        boolean handle(PacketListener listener,Packet packet);
    }
    private final static String ScreenInMCPacketHandlerName = "screen_in_mc_packet_handler";
    private static List<PacketListener> listeners = new ArrayList<>();

    private Player player;
    private Class type;
    private PacketHandler handler;

    public Player getPlayer() {
        return player;
    }

    public Class getType() {
        return type;
    }

    public PacketHandler getHandler() {
        return handler;
    }

    public static synchronized PacketListener addListener(PacketListener listener){
        synchronized (listeners) {
            listeners.add(listener);
        }
        return listener;
    }
    public static synchronized void removeListener(PacketListener listener){
        if(listener!=null){
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }
    }
    public static void addGlobalListener(Player player){
        CraftUtils.getConnection(player).connection.channel.pipeline().addBefore("packet_handler", ScreenInMCPacketHandlerName, new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
                if(packet instanceof Packet) {
                    List<PacketListener> listenersToHandle = new ArrayList<>();
                    synchronized (listeners) {
                        for (PacketListener i : listeners) {
                            if (i.getPlayer().equals(player)) {
                                if (i.getType().equals(packet.getClass())) {
                                    listenersToHandle.add(i);
                                }
                            }
                        }
                    }
                    boolean result = false;
                    for(PacketListener i:listenersToHandle){
                        if(i.getHandler().handle(i,(Packet) packet)&&!result){
                            result = true;
                        }
                    }
                    if(result){
                        return;
                    }
                }
                super.channelRead(ctx, packet);
            }
        });
    }
    public PacketListener(Player player,Class type,PacketHandler handler){
        this.player=player;
        this.type=type;
        this.handler=handler;
    }
}
