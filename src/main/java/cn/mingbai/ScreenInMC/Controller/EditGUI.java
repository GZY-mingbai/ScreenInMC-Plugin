package cn.mingbai.ScreenInMC.Controller;

import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.CraftUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils.JsonText;
import cn.mingbai.ScreenInMC.Utils.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import org.bukkit.entity.Player;

public class EditGUI {
    //The last 2 digits in decimal form of the SHA1 value of "ScreenInMC".
    private static byte lastContainerID = 86;
    private Screen screen;
    private int containerID;
    private int stateID = 0;
    public EditGUI(Screen screen){
        this.screen = screen;
        containerID=lastContainerID;
        lastContainerID++;
    }
    private void setItem(Player player,int slot,net.minecraft.world.item.ItemStack item){
        stateID = stateID + 1 & 32767;
        ClientboundContainerSetSlotPacket packet = new ClientboundContainerSetSlotPacket(containerID,stateID,slot, item);
        CraftUtils.sendPacket(player,packet);
    }
    private void openContainer(Player player){
        JsonText jsonText = new JsonText(LangUtils.getText("controller-editor-gui-title"));
        ClientboundOpenScreenPacket packet = new ClientboundOpenScreenPacket(containerID, MenuType.GENERIC_9x6, jsonText.toComponent());
        CraftUtils.sendPacket(player,packet);
    }
    private void setBaseItems(Player player,Integer clickX, Integer clickY, Utils.MouseClickType clickType){
        net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(Items.ITEM_FRAME);
        stack.setCount(1);
        CompoundTag displayTag = new CompoundTag();
        JsonText jsonText = new JsonText(LangUtils.getText("controller-editor-title")).setColor("gold");
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(new JsonText(LangUtils.getText("controller-editor-id"))
                .addExtra(new JsonText(String.valueOf(screen.getID())).setColor("yellow").setItalic(false))
                .setItalic(false)
                .setColor("gold").toJSONWithoutExtra()));
        lore.add(StringTag.valueOf(new JsonText(LangUtils.getText("controller-editor-location"))
                .addExtra(
                        new JsonText(screen.getLocation().getWorld().getName()+" X:"+
                        screen.getLocation().getBlockX()+" Y:"+
                        screen.getLocation().getBlockY()+" Z:"+
                        screen.getLocation().getBlockZ()
                        ).setColor("yellow").setItalic(false)
                )
                .setItalic(false)
                .setColor("gold").toJSONWithoutExtra()));
        lore.add(StringTag.valueOf(new JsonText(LangUtils.getText("controller-editor-facing"))
                .addExtra(new JsonText(screen.getFacing().getTranslatedFacingName()).setColor("yellow").setItalic(false))
                .setItalic(false)
                .setColor("gold").toJSONWithoutExtra()));
        lore.add(StringTag.valueOf(new JsonText(LangUtils.getText("controller-editor-size"))
                .addExtra(new JsonText(screen.getWidth()+"x"+screen.getHeight()).setColor("yellow").setItalic(false))
                .setItalic(false)
                .setColor("gold").toJSONWithoutExtra()));
        lore.add(StringTag.valueOf(new JsonText(LangUtils.getText("controller-editor-core"))
                .addExtra(new JsonText(screen.getCore().getCoreName()).setColor("yellow").setItalic(false))
                .setItalic(false)
                .setColor("gold").toJSONWithoutExtra()));
        lore.add(StringTag.valueOf(new JsonText(LangUtils.getText("controller-editor-clicked"))
                .addExtra(new JsonText(clickType.getTranslatedName()+" X:"+clickX+" Y:"+clickY).setColor("yellow").setItalic(false))
                .setItalic(false)
                .setColor("gold").toJSONWithoutExtra()));
        displayTag.putString("Name",jsonText.toJSON());
        displayTag.put("Lore",lore);

        stack.getOrCreateTag().put("display",displayTag);
        setItem(player,0,stack);

    }
    public void openGUI(Player player, Integer clickX, Integer clickY, Utils.MouseClickType clickType){
        openContainer(player);
        setBaseItems(player,clickX,clickY,clickType);

    }
}
