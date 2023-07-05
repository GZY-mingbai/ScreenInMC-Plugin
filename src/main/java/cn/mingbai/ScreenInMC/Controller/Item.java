package cn.mingbai.ScreenInMC.Controller;

import cn.mingbai.ScreenInMC.BuiltInGUIs.Welcome;
import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.RedstoneBridge;
import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.CraftUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils.JsonText;
import cn.mingbai.ScreenInMC.Utils.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import static cn.mingbai.ScreenInMC.Main.getGson;
import static cn.mingbai.ScreenInMC.Screen.Screen.getMaxScreenSize;
import static cn.mingbai.ScreenInMC.Utils.CraftUtils.itemBukkitToNMS;
import static cn.mingbai.ScreenInMC.Utils.CraftUtils.itemNMSToBukkit;
import static cn.mingbai.ScreenInMC.Utils.LangUtils.EMPTY_JSON_TEXT;
import static org.bukkit.inventory.ItemFlag.*;

public class Item {
    public static final int CONTROLLER = 1575771175;
    public static final int SELECT_MODE = 0; //框选模式
    public static final int PLACE_MODE = 1; //放置模式
    public static final int EDIT_MODE = 2; //编辑模式
    public static final int CONNECT_MODE = -1; //连接模式
    public static final int FINAL_MODE = 2;
    public static final int FIRST_MODE = 0;
    private static BukkitRunnable runnable = null;
    private static Color BLUE;
    private static Color RED;
    private static Color YELLOW;
    private static Color GREEN;

    public static void onEnable() {

        BLUE = Color.fromRGB(85, 85, 255);
        RED = Color.fromRGB(255, 85, 85);
        YELLOW = Color.fromRGB(255, 255, 85);
        GREEN = Color.fromRGB(85, 255, 85);

        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.isOp()) {
                        ItemStack item = player.getInventory().getItemInMainHand();
                        if (item != null && item.hasItemMeta()) {
                            ItemMeta meta = item.getItemMeta();
                            if (meta.hasCustomModelData() && meta.getCustomModelData() == CONTROLLER) {
                                if (meta.hasLocalizedName()) {
                                    try {
                                        ItemData data = getGson().fromJson(meta.getLocalizedName(), ItemData.class);
                                        JsonText extra = new JsonText("");
                                        switch (data.nowMode) {
                                            case SELECT_MODE:
                                                Location eyeLoc = player.getEyeLocation();
                                                Location closest;
                                                if (data.world != null && data.p1x != null && data.p1y != null && data.p1z != null) {
                                                    JsonText jsonText = new JsonText(" " + LangUtils.getText("controller-point-start").replace("%%",
                                                            data.p1x + "," + data.p1y + "," + data.p1z));
                                                    jsonText.color = "blue";
                                                    extra.addExtra(jsonText);
                                                    World world = Bukkit.getWorld(data.world);
                                                    if (data.p2x != null && data.p2y != null && data.p2z != null) {
                                                        jsonText = new JsonText(" " + LangUtils.getText("controller-point-end").replace("%%",
                                                                data.p2x + "," + data.p2y + "," + data.p2z));
                                                            jsonText.color="red";
                                                            extra.addExtra(jsonText);
                                                        jsonText = new JsonText(" " + data.w + "x" + data.h);
                                                        jsonText.color="yellow";
                                                        extra.addExtra(jsonText);
                                                        world.spawnParticle(Particle.REDSTONE, (double) data.p2x, (double) data.p2y, (double) data.p2z, 0, 0, 0, 0, 0, new Particle.DustOptions(RED, 1));
                                                        Utils.Pair<Utils.Pair<Screen.Facing, Location>, Utils.Pair<Screen.Facing, Location>> locations = getScreenLocations(world, data.p1x, data.p1y, data.p1z, data.p2x, data.p2y, data.p2z);
                                                        if (data.w != null && data.h != null) {
                                                            Utils.ScreenClickResult result1 = Utils.getScreenClickAt(player.getEyeLocation(), locations.getKey().getValue(), locations.getKey().getKey(), data.w, data.h, 64);
                                                            Utils.ScreenClickResult result2 = Utils.getScreenClickAt(player.getEyeLocation(), locations.getValue().getValue(), locations.getValue().getKey(), data.w, data.h, 64);
                                                            if (result1.isClicked() || result2.isClicked()) {
                                                                spawnRectParticle(world, data.p1x, data.p1y, data.p1z, data.p2x, data.p2y, data.p2z, YELLOW);
                                                            }
                                                        }
                                                        closest = getClosestBlock(eyeLoc);
                                                    } else {
                                                        closest = getClosestPlane(eyeLoc, data.p1x, data.p1y, data.p1z);
                                                        if (closest != null) {
                                                            Utils.Pair<Integer, Integer> size = getScreenSize(new Location(world, data.p1x, data.p1y, data.p1z), closest);
                                                            jsonText = new JsonText(" " + size.getKey() + "x" + size.getValue());
                                                            jsonText.color="yellow";
                                                            extra.addExtra(jsonText);
                                                        }
                                                    }
                                                    world.spawnParticle(Particle.REDSTONE, (double) data.p1x, (double) data.p1y, (double) data.p1z, 0, 0, 0, 0, 0, new Particle.DustOptions(BLUE, 1));
                                                } else {
                                                    closest = getClosestBlock(eyeLoc);
                                                }
                                                if (closest == null) {
                                                    Vector eyeVec = eyeLoc.toVector();
                                                    Vector direction = eyeLoc.getDirection();
                                                    direction.multiply(5);
                                                    eyeVec.add(direction);
                                                    Location point = eyeVec.toLocation(eyeLoc.getWorld());
                                                    eyeLoc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, point, 0, 0, 0, 0, 0);
                                                } else {
                                                    eyeLoc.getWorld().spawnParticle(Particle.REDSTONE, closest, 0, 0, 0, 0, 0, new Particle.DustOptions(GREEN, 1));
                                                }
                                                break;
                                            case PLACE_MODE:
                                                if (data.world != null && data.p1x != null && data.p1y != null && data.p1z != null && data.p2x != null && data.p2y != null && data.p2z != null && data.w != null && data.h != null) {
                                                    JsonText jsonText = new JsonText(" " + LangUtils.getText("controller-point-start").replace("%%",
                                                            data.p1x + "," + data.p1y + "," + data.p1z));
                                                    jsonText.color="blue";
                                                    extra.addExtra(jsonText);
                                                    jsonText = new JsonText(" " + LangUtils.getText("controller-point-end").replace("%%",
                                                            data.p2x + "," + data.p2y + "," + data.p2z));
                                                    jsonText.color="red";
                                                    extra.addExtra(jsonText);
                                                    jsonText = new JsonText(" " + data.w + "x" + data.h);
                                                    jsonText.color="yellow";
                                                    extra.addExtra(jsonText);
                                                    World world = Bukkit.getWorld(data.world);
                                                    world.spawnParticle(Particle.REDSTONE, (double) data.p1x, (double) data.p1y, (double) data.p1z, 0, 0, 0, 0, 0, new Particle.DustOptions(BLUE, 1));
                                                    world.spawnParticle(Particle.REDSTONE, (double) data.p2x, (double) data.p2y, (double) data.p2z, 0, 0, 0, 0, 0, new Particle.DustOptions(RED, 1));
                                                    spawnRectParticle(world, data.p1x, data.p1y, data.p1z, data.p2x, data.p2y, data.p2z, YELLOW);
                                                }
                                                break;
                                            case EDIT_MODE:
                                                for (Screen i : Screen.getAllScreens()) {
                                                    Location screenLocation = i.getLocation().clone();
                                                    Utils.ScreenClickResult result = Utils.getScreenClickAt(player.getEyeLocation(), screenLocation, i.getFacing(), i.getWidth(), i.getHeight(), 1024);
                                                    if (result.isClicked()) {
                                                        Utils.Pair<Location,Location> corners = getScreenCorners(i);
                                                        spawnRectParticle(i.getLocation().getWorld(),
                                                                corners.getKey().getBlockX(),corners.getKey().getBlockY(),corners.getKey().getBlockZ(),
                                                                corners.getValue().getBlockX(),corners.getValue().getBlockY(),corners.getValue().getBlockZ(),
                                                                YELLOW);
                                                        break;
                                                    }
                                                }
                                                break;
                                            case CONNECT_MODE:
                                                boolean finish = false;
                                                if(data.conn!=null){
                                                    Screen[] screens = Screen.getAllScreens();
                                                    if(data.conn.id<screens.length&&data.conn.id>=0) {
                                                        Screen screen = screens[data.conn.id];
                                                        if (screen.getCore()!=null&&screen.getCore().getCoreName().equals(data.conn.core)) {
                                                            if(data.conn.i>=0&&screen.getCore().getRedstoneBridge().getRedstoneSignalInterfaces().size()> data.conn.i) {
                                                                Location location = getClosestBlock(player.getEyeLocation(), Material.REDSTONE_WIRE);
                                                                if (location != null) {
                                                                    spawnRectParticle(player.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                                                                            location.getBlockX() + 1, location.getBlockY(), location.getBlockZ() + 1,
                                                                            Color.RED);
                                                                }
                                                                finish = true;
                                                            }
                                                        }
                                                    }
                                                }
                                                if(!finish){
                                                    switchMode(player,FIRST_MODE);
                                                }
                                                break;
                                            default:
                                                switchMode(player,FIRST_MODE);
                                        }
                                        JsonText component = new JsonText(LangUtils.getText("controller-item-now-mode") + " " + getModeName(data.nowMode));
                                        component.addExtra(extra);
                                        component.color="gold";
                                        ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(component.toComponent());
                                        CraftUtils.sendPacket(player,packet);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        player.getInventory().setItemInMainHand(null);
                                    }
                                } else {
                                    player.getInventory().setItemInMainHand(null);
                                }
                            }
                        }
                    }

                }
            }
        };
        runnable.runTaskTimer(Main.thisPlugin(), 0L, 1L);
    }
    public static ItemStack getItemFromPlayer(Player player){
        ItemStack item =player.getInventory().getItemInMainHand();
        if(item==null||!item.hasItemMeta()){
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if(meta.hasCustomModelData()&&meta.getCustomModelData()!=CONTROLLER){
            return null;
        }
        return item;
    }
    public static ItemData getData(ItemStack item){
        return getGson().fromJson(item.getItemMeta().getLocalizedName(), ItemData.class);
    }
    public static void setData(ItemStack item,ItemData data){
        ItemMeta meta = item.getItemMeta();
        meta.setLocalizedName(getGson().toJson(data));
        item.setItemMeta(meta);
    }
    public static void switchMode(Player player,int to){
        ItemStack item = getItemFromPlayer(player);
        if(item==null){
            return;
        }
        ItemData data = getData(item);
        data.nowMode=to;
        if(to!=CONNECT_MODE){
            data.conn=null;
        }
        setData(item,data);
        setItemLore(item, data.nowMode);
    }
    public static void onDisable() {
        if (runnable != null) {
            runnable.cancel();
        }
    }

    public static void giveItem(Player player) {
        ItemStack item = new ItemStack(Material.WOODEN_HOE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6" + LangUtils.getText("controller-item-name"));
        meta.setCustomModelData(CONTROLLER);
        meta.setUnbreakable(true);
        meta.addEnchant(Enchantment.DURABILITY, 1, false);
        meta.addItemFlags(HIDE_ENCHANTS,
                HIDE_ATTRIBUTES,
                HIDE_UNBREAKABLE,
                HIDE_DESTROYS,
                HIDE_PLACED_ON,
                HIDE_POTION_EFFECTS,
                HIDE_DYE);
        meta.setLocalizedName(getGson().toJson(new ItemData()));
        item.setItemMeta(meta);
        setItemLore(item, FIRST_MODE);
        player.getInventory().addItem(item);
    }

    public static String getModeName(int id) {
        switch (id) {
            case SELECT_MODE:
                return LangUtils.getText("controller-item-select-mode");
            case PLACE_MODE:
                return LangUtils.getText("controller-item-place-mode");
            case EDIT_MODE:
                return LangUtils.getText("controller-item-edit-mode");
            case CONNECT_MODE:
                return LangUtils.getText("controller-item-connect-mode");
        }
        return LangUtils.getText("controller-item-unknown-mode");
    }

    private static void spawnLineParticle(World world, int x1, int y1, int z1, int x2, int y2, int z2, Color color) {
        Vector v1 = new Vector(x1, y1, z1);
        Vector v2 = new Vector(x2, y2, z2);
        Vector v3 = v2.clone().subtract(v1);
        double d1 = v1.distance(v2);
        v3.divide(new Vector(d1, d1, d1));
        v3.multiply(0.1);
        for (double i = 0; i < d1; i += 0.1) {
            world.spawnParticle(Particle.REDSTONE, v1.getX(), v1.getY(), v1.getZ(), 0, 0, 0, 0, 0, new Particle.DustOptions(color, 1));
            v1.add(v3);
        }
    }

    private static void spawnRectParticle(World world, int x1, int y1, int z1, int x2, int y2, int z2, Color color) {
        if (x1 == x2) {
            spawnLineParticle(world, x1, y1, z1, x1, y1, z2, color);
            spawnLineParticle(world, x1, y1, z1, x1, y2, z1, color);
            spawnLineParticle(world, x1, y2, z2, x1, y2, z1, color);
            spawnLineParticle(world, x1, y2, z2, x1, y1, z2, color);
        }
        if (y1 == y2) {
            spawnLineParticle(world, x1, y1, z1, x1, y1, z2, color);
            spawnLineParticle(world, x1, y1, z1, x2, y1, z1, color);
            spawnLineParticle(world, x2, y1, z2, x2, y1, z1, color);
            spawnLineParticle(world, x2, y1, z2, x1, y1, z2, color);
        }
        if (z1 == z2) {
            spawnLineParticle(world, x1, y1, z1, x2, y1, z1, color);
            spawnLineParticle(world, x1, y1, z1, x1, y2, z1, color);
            spawnLineParticle(world, x2, y2, z1, x1, y2, z1, color);
            spawnLineParticle(world, x2, y2, z1, x2, y1, z1, color);
        }
    }


    private static JsonText[] replaceLoreKeybind(String text, String from, String to, String textColor, String keyBindColor) {
        JsonText[] loreArray = new JsonText[3];
        String[] lore = text.split(from);
        if (lore.length == 0) {
            return new JsonText[]{getLoreColorfulJsonText(text, textColor)};
        }
        JsonText loreTag = new JsonText();
        loreTag.text = lore[0];
        loreTag.color = textColor;
        loreTag.italic = false;
        loreArray[0] = loreTag;
        loreTag = new JsonText();
        loreTag.keybind = to;
        loreTag.color = keyBindColor;
        loreTag.italic = false;
        loreArray[1] = loreTag;
        loreTag = new JsonText();
        loreTag.text = lore[1];
        loreTag.color = textColor;
        loreTag.italic = false;
        loreArray[2] = loreTag;
        return loreArray;
    }

    private static JsonText getLoreColorfulJsonText(String text, String color) {
        JsonText loreTag = new JsonText();
        loreTag.text = text;
        loreTag.color = color;
        loreTag.italic = false;
        return loreTag;
    }

    private static void setItemLore(ItemStack item, int nowMode) {
        net.minecraft.world.item.ItemStack stack = itemBukkitToNMS(item);
        CompoundTag displayTag = (CompoundTag) stack.getOrCreateTag().get("display");
        ListTag lore = new ListTag();
        String[] lore1 = LangUtils.getText("controller-mode-info").split("\n");
        lore.add(StringTag.valueOf(JsonText.toJSON(
                getLoreColorfulJsonText(lore1[0].replace("%now-mode%", getModeName(nowMode)), "gold")
        )));
        lore.add(StringTag.valueOf(JsonText.toJSON(
                replaceLoreKeybind(lore1[1], "%sneak%", "key.sneak", "gold", "gold")
        )));
        lore.add(StringTag.valueOf(EMPTY_JSON_TEXT));
        switch (nowMode) {
            case SELECT_MODE:
                String[] modeLore = LangUtils.getText("controller-select-mode-info").split("\n");
                lore.add(StringTag.valueOf(JsonText.toJSON(
                        replaceLoreKeybind(modeLore[0], "%left-click%", "key.attack", "gold", "blue")
                )));
                lore.add(StringTag.valueOf(JsonText.toJSON(
                        replaceLoreKeybind(modeLore[1], "%right-click%", "key.use", "gold", "red")
                )));
                lore.add(StringTag.valueOf(JsonText.toJSON(
                        getLoreColorfulJsonText(modeLore[2], "gold")
                )));
                Utils.Pair maxSize = getMaxScreenSize();
                lore.add(StringTag.valueOf(JsonText.toJSON(
                        getLoreColorfulJsonText(modeLore[3].replace("%max-size%", maxSize.getKey() + "x" + maxSize.getValue()), "gold")
                )));
                break;
            case PLACE_MODE:
                modeLore = LangUtils.getText("controller-place-mode-info").split("\n");
                lore.add(StringTag.valueOf(JsonText.toJSON(
                        getLoreColorfulJsonText(modeLore[0], "gold")
                )));
                lore.add(StringTag.valueOf(JsonText.toJSON(
                        replaceLoreKeybind(modeLore[1], "%right-click%", "key.use", "gold", "yellow")
                )));
                lore.add(StringTag.valueOf(JsonText.toJSON(
                        getLoreColorfulJsonText(modeLore[2], "gold")
                )));
                break;
            case EDIT_MODE:
            case CONNECT_MODE:
                modeLore = LangUtils.getText(nowMode==EDIT_MODE?"controller-edit-mode-info":"controller-connect-mode-info").split("\n");
                lore.add(StringTag.valueOf(JsonText.toJSON(
                        replaceLoreKeybind(modeLore[0], "%right-click%", "key.use", "gold", "yellow")
                )));
                lore.add(StringTag.valueOf(JsonText.toJSON(
                        getLoreColorfulJsonText(modeLore[1], "gold")
                )));
                break;
        }
        displayTag.put("Lore", lore);
        item.setItemMeta(itemNMSToBukkit(stack).getItemMeta());
    }

    public static void onPlayerSwitchMode(Player player, ItemStack item, boolean next, int slot) {
        try {
            ItemMeta meta = item.getItemMeta();
            ItemData data = getGson().fromJson(meta.getLocalizedName(), ItemData.class);
            if (next) {
                if (data.nowMode == FINAL_MODE) {
                    data.nowMode = 0;
                } else {
                    data.nowMode++;
                }
            } else {
                if (data.nowMode == 0) {
                    data.nowMode = FINAL_MODE;
                } else {
                    data.nowMode--;
                }
            }
            meta.setLocalizedName(getGson().toJson(data));
            item.setItemMeta(meta);
            setItemLore(item, data.nowMode);
        } catch (Exception e) {
            player.getInventory().setItemInMainHand(null);
        }
    }

    public static boolean onPlayerClick(Player player, ItemStack item, Utils.MouseClickType type) {
        try {
            ItemMeta meta = item.getItemMeta();
            ItemData data = getGson().fromJson(meta.getLocalizedName(), ItemData.class);
            if (data.nowMode == SELECT_MODE) {
                if (type.equals(Utils.MouseClickType.LEFT)) {
                    Location eyeLoc = player.getEyeLocation();
                    Location closest = getClosestBlock(eyeLoc);
                    if (closest == null) {
                        Main.sendMessage(player, LangUtils.getText("controller-block-not-found"));
                        clearPoints(data);
                    } else {
                        data.world = closest.getWorld().getName();
                        data.p1x = (int) closest.getX();
                        data.p1y = (int) closest.getY();
                        data.p1z = (int) closest.getZ();
                        data.p2x = null;
                        data.p2y = null;
                        data.p2z = null;
                        Main.sendMessage(player, "§9" + LangUtils.getText("controller-set-point-start").replace("%%",
                                (int) closest.getX() + "," + (int) closest.getY() + "," + (int) closest.getZ()));
                    }
                    meta.setLocalizedName(getGson().toJson(data));
                    item.setItemMeta(meta);
                    return true;
                }
                if (type.equals(Utils.MouseClickType.RIGHT)) {
                    if (data.world == null || data.p1x == null || data.p1y == null || data.p1z == null) {
                        return true;
                    }
                    Location eyeLoc = player.getEyeLocation();
                    Location closest = getClosestPlane(eyeLoc, data.p1x, data.p1y, data.p1z);
                    if (closest == null) {
                        Main.sendMessage(player, LangUtils.getText("controller-block-not-found"));
                        clearPoints(data);
                    } else {
                        String worldName = closest.getWorld().getName();
                        if (!data.world.equals(worldName)) {
                            Main.sendMessage(player, LangUtils.getText("controller-points-different-worlds"));
                            clearPoints(data);
                        } else {
                            data.world = worldName;
                            data.p2x = (int) closest.getX();
                            data.p2y = (int) closest.getY();
                            data.p2z = (int) closest.getZ();
                            Utils.Pair<Integer, Integer> size = getScreenSize(new Location(closest.getWorld(), data.p1x, data.p1y, data.p1z), closest);
                            data.w = size.getKey();
                            data.h = size.getValue();
                            Main.sendMessage(player, "§c" + LangUtils.getText("controller-set-point-end").replace("%%",
                                    (int) closest.getX() + "," + (int) closest.getY() + "," + (int) closest.getZ()));
                        }
                    }
                    meta.setLocalizedName(getGson().toJson(data));
                    item.setItemMeta(meta);
                    return true;
                }
            }
            if (data.nowMode == PLACE_MODE) {
                if (data.world != null && data.p1x != null && data.p1y != null && data.p1z != null && data.p2x != null && data.p2y != null && data.p2z != null && data.w != null && data.h != null) {
                    World world = Bukkit.getWorld(data.world);
                    Utils.Pair<Utils.Pair<Screen.Facing, Location>, Utils.Pair<Screen.Facing, Location>> locations = getScreenLocations(world, data.p1x, data.p1y, data.p1z, data.p2x, data.p2y, data.p2z);
                    Utils.ScreenClickResult result1 = Utils.getScreenClickAt(player.getEyeLocation(), locations.getKey().getValue(), locations.getKey().getKey(), data.w, data.h, 64);
                    Utils.ScreenClickResult result2 = Utils.getScreenClickAt(player.getEyeLocation(), locations.getValue().getValue(), locations.getValue().getKey(), data.w, data.h, 64);
                    Screen.Facing facing = null;
                    Location location = null;
                    if (result1.isClicked()) {
                        location = locations.getKey().getValue();
                        facing = locations.getKey().getKey();
                    } else if (result2.isClicked()) {
                        location = locations.getValue().getValue();
                        facing = locations.getValue().getKey();
                    }
                    if (facing == null || location == null) {
                        Main.sendMessage(player, LangUtils.getText("controller-selection-not-found"));
                        return true;
                    }
                    clearPoints(data);
                    Main.sendMessage(player, LangUtils.getText("controller-place-start"));
                    Screen screen = new Screen(location, facing, data.w, data.h);
                    screen.setCore(Core.createCore("Welcome"));
                    screen.putScreen();
                    Main.sendMessage(player, "§a" + LangUtils.getText("controller-place-success")
                            .replace("%location%", "§9" + world.getName() + "(" + (int) location.getX() + "," + (int) location.getY() + "," + (int) location.getZ() + ")§a")
                            .replace("%facing%", "§c" + facing.getTranslatedFacingName() + "§a")
                            .replace("%size%", "§e" + data.w + "x" + data.h + "§a")
                    );
                    meta.setLocalizedName(getGson().toJson(data));
                    item.setItemMeta(meta);
                } else {
                    Main.sendMessage(player, LangUtils.getText("controller-selection-not-found"));
                    return true;
                }
            }
            if(data.nowMode == CONNECT_MODE){
                if(type.equals(Utils.MouseClickType.RIGHT)){
                    if(data.conn!=null) {
                        Screen[] screens = Screen.getAllScreens();
                        if (data.conn.id < screens.length && data.conn.id >= 0) {
                            Screen screen = screens[data.conn.id];
                            if (screen.getCore()!=null&&screen.getCore().getCoreName().equals(data.conn.core)) {
                                if(data.conn.i>=0&&screen.getCore().getRedstoneBridge().getRedstoneSignalInterfaces().size()> data.conn.i) {
                                    Location location = getClosestBlock(player.getEyeLocation(), Material.REDSTONE_WIRE);
                                    if (location!=null&&location.getBlock().getType().equals(Material.REDSTONE_WIRE)) {
                                        Utils.Pair<String, RedstoneBridge.RedstoneSignalInterface> pair = screen.getCore().getRedstoneBridge().getRedstoneSignalInterfaces().get(data.conn.i);
                                        try {
                                            pair.getValue().connect(location);
                                            Main.sendMessage(player, LangUtils.getText("controller-connect-success"));
                                        }catch (RedstoneBridge.RedstoneSignalInterface.ConnectException e){
                                            if(e.getType()==0){
                                                Main.sendMessage(player, LangUtils.getText("controller-connect-failed-output"));
                                            }else
                                            if(e.getType()==1){
                                                Main.sendMessage(player, LangUtils.getText("controller-connect-failed-input"));
                                            }
                                        }
                                        switchMode(player, EDIT_MODE);
                                    }
                                    return true;
                                }
                            }
                        }
                    }
                    switchMode(player,FIRST_MODE);
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            player.getInventory().setItemInMainHand(null);
        }
        return false;
    }

    public static void onPlayerClickScreen(Player player, ItemStack item, Utils.MouseClickType type,int x,int y,Screen screen){
        try {
            ItemMeta meta = item.getItemMeta();
            ItemData data = getGson().fromJson(meta.getLocalizedName(), ItemData.class);
            if (data.nowMode == EDIT_MODE) {
                screen.getEditGUI().openGUI(player,x,y,type);
            }
        }catch (Exception e){
            e.printStackTrace();
            player.getInventory().setItemInMainHand(null);
        }
    }
    private static void clearPoints(ItemData data) {
        data.world = null;
        data.p1x = null;
        data.p1y = null;
        data.p1z = null;
        data.p2x = null;
        data.p2y = null;
        data.p2z = null;
    }
    private static Utils.Pair<Location,Location> getScreenCorners(Screen screen){
        switch (screen.getFacing()){
            case EAST:
                return new Utils.Pair<>(
                        new Location(
                                screen.getLocation().getWorld(),
                                screen.getLocation().getBlockX(),
                                screen.getLocation().getBlockY()+1,
                                screen.getLocation().getBlockZ()+1
                        ),
                        new Location(
                                screen.getLocation().getWorld(),
                                screen.getLocation().getBlockX(),
                                screen.getLocation().getBlockY()-screen.getHeight()+1,
                                screen.getLocation().getBlockZ()-screen.getWidth()+1
                        )
                );
            case WEST:
                return new Utils.Pair<>(
                        new Location(
                                screen.getLocation().getWorld(),
                                screen.getLocation().getBlockX()+1,
                                screen.getLocation().getBlockY()+1,
                                screen.getLocation().getBlockZ()
                        ),
                        new Location(
                                screen.getLocation().getWorld(),
                                screen.getLocation().getBlockX()+1,
                                screen.getLocation().getBlockY()-screen.getHeight()+1,
                                screen.getLocation().getBlockZ()+screen.getWidth()
                        )
                );
            case UP:
                return new Utils.Pair<>(
                        new Location(
                                screen.getLocation().getWorld(),
                                screen.getLocation().getBlockX(),
                                screen.getLocation().getBlockY(),
                                screen.getLocation().getBlockZ()
                        ),
                        new Location(
                                screen.getLocation().getWorld(),
                                screen.getLocation().getBlockX()+screen.getWidth(),
                                screen.getLocation().getBlockY(),
                                screen.getLocation().getBlockZ()+screen.getHeight()
                        )
                );
            case DOWN:
                return new Utils.Pair<>(
                        new Location(
                                screen.getLocation().getWorld(),
                                screen.getLocation().getBlockX(),
                                screen.getLocation().getBlockY()+1,
                                screen.getLocation().getBlockZ()+1
                        ),
                        new Location(
                                screen.getLocation().getWorld(),
                                screen.getLocation().getBlockX()+screen.getWidth(),
                                screen.getLocation().getBlockY()+1,
                                screen.getLocation().getBlockZ()-screen.getHeight()+1
                        )
                );
            case SOUTH:
                return new Utils.Pair<>(
                        new Location(
                                screen.getLocation().getWorld(),
                                screen.getLocation().getBlockX(),
                                screen.getLocation().getBlockY()+1,
                                screen.getLocation().getBlockZ()
                        ),
                        new Location(
                                screen.getLocation().getWorld(),
                                screen.getLocation().getBlockX()+screen.getWidth(),
                                screen.getLocation().getBlockY()-screen.getHeight()+1,
                                screen.getLocation().getBlockZ()
                        )
                );
            case NORTH:
                return new Utils.Pair<>(
                        new Location(
                                screen.getLocation().getWorld(),
                                screen.getLocation().getBlockX()+1,
                                screen.getLocation().getBlockY()+1,
                                screen.getLocation().getBlockZ()+1
                        ),
                        new Location(
                                screen.getLocation().getWorld(),
                                screen.getLocation().getBlockX()-screen.getWidth()+1,
                                screen.getLocation().getBlockY()-screen.getHeight()+1,
                                screen.getLocation().getBlockZ()+1
                        )
                );
        }
        throw new RuntimeException("Unknown direction.");
    }

    private static Utils.Pair<Utils.Pair<Screen.Facing, Location>, Utils.Pair<Screen.Facing, Location>> getScreenLocations(World world, int p1x, int p1y, int p1z, int p2x, int p2y, int p2z) {
        Utils.Pair<Screen.Facing, Location> l1 = null, l2 = null;
        if (p1x == p2x) {
            l1 = new Utils.Pair<>(Screen.Facing.EAST,
                    new Location(
                            world,
                            p1x,
                            Math.max(p1y, p2y) - 1,
                            Math.max(p1z, p2z) - 1
                    ));
            l2 = new Utils.Pair<>(Screen.Facing.WEST,
                    new Location(
                            world,
                            p1x - 1,
                            Math.max(p1y, p2y) - 1,
                            Math.min(p1z, p2z)
                    ));
        }
        if (p1y == p2y) {
            l1 = new Utils.Pair<>(Screen.Facing.UP,
                    new Location(
                            world,
                            Math.min(p1x, p2x),
                            p1y,
                            Math.min(p1z, p2z)
                    ));
            l2 = new Utils.Pair<>(Screen.Facing.DOWN,
                    new Location(
                            world,
                            Math.min(p1x, p2x),
                            p1y - 1,
                            Math.max(p1z, p2z) - 1
                    ));
        }
        if (p1z == p2z) {
            l1 = new Utils.Pair<>(Screen.Facing.SOUTH,
                    new Location(
                            world,
                            Math.min(p1x, p2x),
                            Math.max(p1y, p2y) - 1,
                            p1z
                    ));
            l2 = new Utils.Pair<>(Screen.Facing.NORTH,
                    new Location(
                            world,
                            Math.max(p1x, p2x) - 1,
                            Math.max(p1y, p2y) - 1,
                            p1z - 1
                    ));
        }
        return new Utils.Pair<>(l1, l2);
    }

    private static Location getClosestBlock(Location eyeLoc,Material type) {
        for (int i = 1; i < 21; i++) {
            Vector eyeVec = eyeLoc.toVector();
            Vector direction = eyeLoc.getDirection();
            direction.multiply(i * 0.25);
            eyeVec.add(direction);
            Location point = eyeVec.toLocation(eyeLoc.getWorld());
            Block block = point.getBlock();
            if (block != null && block.getType().equals(type)) {
                return new Location(eyeLoc.getWorld(), block.getX(),block.getY(),block.getZ());
            }
        }
        return null;
    }
    private static Location getClosestBlock(Location eyeLoc) {
        for (int i = 1; i < 21; i++) {
            Vector eyeVec = eyeLoc.toVector();
            Vector direction = eyeLoc.getDirection();
            direction.multiply(i * 0.25);
            eyeVec.add(direction);
            Location point = eyeVec.toLocation(eyeLoc.getWorld());
            Block block = point.getBlock();
            if (block != null && !block.getType().isAir()) {
                int x = (int) Math.round(point.getX());
                int y = (int) Math.round(point.getY());
                int z = (int) Math.round(point.getZ());
                return new Location(eyeLoc.getWorld(), x, y, z);
            }
        }
        return null;
    }

    private static Location getClosestPlane(Location eyeLoc, int locX, int locY, int locZ) {
        for (int i = 1; i < 21; i++) {
            Vector eyeVec = eyeLoc.toVector();
            Vector direction = eyeLoc.getDirection();
            direction.multiply(i * 0.25);
            eyeVec.add(direction);
            Location point = eyeVec.toLocation(eyeLoc.getWorld());
            int x = (int) Math.round(point.getX());
            int y = (int) Math.round(point.getY());
            int z = (int) Math.round(point.getZ());
            int j = 0;
            if (x == locX) {
                j++;
            }
            if (y == locY) {
                j++;
            }
            if (z == locZ) {
                j++;
            }
            Block block = point.getBlock();
            if (j == 1 && block != null && !block.getType().isAir()) {
                return new Location(eyeLoc.getWorld(), x, y, z);
            }
        }
        return null;
    }

    private static Utils.Pair<Integer, Integer> getScreenSize(Location l1, Location l2) {
        int w = 0, h = 0;
        if ((int) l1.getX() == (int) l2.getX()) {
            w = (int) Math.abs(l1.getZ() - l2.getZ());
            h = (int) Math.abs(l1.getY() - l2.getY());
        }
        if ((int) l1.getY() == (int) l2.getY()) {
            w = (int) Math.abs(l1.getX() - l2.getX());
            h = (int) Math.abs(l1.getZ() - l2.getZ());
        }
        if ((int) l1.getZ() == (int) l2.getZ()) {
            w = (int) Math.abs(l1.getX() - l2.getX());
            h = (int) Math.abs(l1.getY() - l2.getY());
        }
        return new Utils.Pair<>(w, h);
    }

    public static class ItemData {
        public int nowMode = SELECT_MODE;
        public String world = null;
        public Integer p1x = null, p1y = null, p1z = null;
        public Integer p2x = null, p2y = null, p2z = null;
        public Integer w = null, h = null;
        public ConnectModeData conn;
    }
    public static class ConnectModeData{
        public String core = null;
        public int id = -1;
        public int i = -1;
    }
}
