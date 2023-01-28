package cn.mingbai.ScreenInMC.Utils;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;

public class CraftUtils {
    public static void sendPacket(Player player, Packet packet){
        ServerPlayer sp = ((CraftPlayer) player).getHandle();
        ServerPlayerConnection spc = sp.connection;
        spc.send(packet);
    }
    public static ItemStack itemBukkitToNMS(org.bukkit.inventory.ItemStack itemStack){
        return CraftItemStack.asNMSCopy(itemStack);
    }
    public static org.bukkit.inventory.ItemStack itemNMSToBukkit(ItemStack itemStack){
        return CraftItemStack.asBukkitCopy(itemStack);
    }
}
