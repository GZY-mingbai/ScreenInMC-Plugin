package cn.mingbai.ScreenInMC.Utils;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class CraftUtils {
    public static void sendPacket(Player player, Packet packet){
        ServerPlayer sp = ((CraftPlayer) player).getHandle();
        ServerPlayerConnection spc = sp.connection;
        spc.send(packet);
    }
}
