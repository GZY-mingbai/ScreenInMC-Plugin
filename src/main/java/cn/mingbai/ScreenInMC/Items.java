package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Utils.LangUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class Items {
    public final static ItemStack KEYBOARD = new ItemStack(Material.WRITTEN_BOOK);
    public static void setAllItems() {
//        BookMeta keyboardMeta = (BookMeta) KEYBOARD.getItemMeta();
//        keyboardMeta.spigot().addPage(
//                new BaseComponent[]{
//                        new TextComponent(LangUtils.getText("kb-basic")),
//                        new TextComponent(LangUtils.getText("kb-extra")),
//                        new TextComponent(LangUtils.getText("kb-backspace")),
//                        new TextComponent(LangUtils.getText("kb-enter")),
//
//
//                }
//        );
    }
}
