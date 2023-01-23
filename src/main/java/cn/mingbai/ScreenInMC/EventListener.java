package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Screen.Screen;
import cn.mingbai.ScreenInMC.Utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;

public class EventListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        for (Screen i : Screen.getAllScreens()) {
            i.sendPutScreenPacket(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent e) {
        for (Screen i : Screen.getAllScreens()) {
            i.sendPutScreenPacket(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Utils.MouseClickType type = Utils.MouseClickType.LEFT;
        if (e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            type = Utils.MouseClickType.LEFT;
        } else if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            type = Utils.MouseClickType.RIGHT;
        }
        Player player = e.getPlayer();
        Location playerLocation = player.getLocation();
        nextScreen:
        for (Screen i : Screen.getAllScreens()) {
            Location screenLocation = i.getLocation().clone();
            if (screenLocation.getWorld().equals(playerLocation.getWorld()) && screenLocation.distance(playerLocation) <= 1024) {
                Vector v1;
                switch (i.getFacing()) {
                    case UP:
                        v1 = new Vector(0, 1, 0);
                        break;
                    case DOWN:
                        v1 = new Vector(0, -1, 0);
                        screenLocation.add(0, 1, 0);
                        break;
                    case EAST:
                        v1 = new Vector(1, 0, 0);
                        break;
                    case SOUTH:
                        v1 = new Vector(0, 0, 1);
                        break;
                    case WEST:
                        v1 = new Vector(-1, 0, 0);
                        screenLocation.add(1, 0, 0);
                        break;
                    case NORTH:
                        v1 = new Vector(0, 0, -1);
                        screenLocation.add(0, 0, 1);
                        break;
                    default:
                        v1 = new Vector(0, 0, 0);
                        break;
                }
                Vector v2 = screenLocation.toVector();
                Vector v3 = player.getEyeLocation().getDirection();
                Vector v4 = player.getEyeLocation().toVector();
                double d = (v2.clone().subtract(v4).dot(v1)) / (v3.dot(v1));
                if (d < 0) {
                    continue nextScreen;
                }
                Vector v5 = v3.clone().normalize().multiply(d).add(v4);
                Location clickedLocation = v5.toLocation(playerLocation.getWorld());
                double clickedLocationX = clickedLocation.getX();
                double clickedLocationY = clickedLocation.getY();
                double clickedLocationZ = clickedLocation.getZ();
                double screenLocationX = screenLocation.getX();
                double screenLocationY = screenLocation.getY();
                double screenLocationZ = screenLocation.getZ();
                int screenWidth = i.getWidth();
                int screenHeight = i.getHeight();
                double mouseX;
                double mouseY;
                switch (i.getFacing()) {
                    case UP:
                        if (clickedLocationX < screenLocationX || clickedLocationX > screenLocationX + screenWidth ||
                                clickedLocationZ < screenLocationZ || clickedLocationZ > screenLocationZ + screenHeight
                        ) {
                            continue nextScreen;
                        }
                        mouseX = clickedLocationX - screenLocationX;
                        mouseY = clickedLocationZ - screenLocationZ;
                        break;
                    case DOWN:
                        screenLocationZ++;
                        screenLocation.add(0, -1, 0);
                        if (clickedLocationX < screenLocationX || clickedLocationX > screenLocationX + screenWidth ||
                                clickedLocationZ < screenLocationZ - screenHeight || clickedLocationZ > screenLocationZ
                        ) {
                            continue nextScreen;
                        }
                        mouseX = clickedLocationX - screenLocationX;
                        mouseY = screenLocationZ - clickedLocationZ;
                        break;
                    case EAST:
                        screenLocationY++;
                        screenLocationZ++;
                        if (clickedLocationY < screenLocationY - screenHeight || clickedLocationY > screenLocationY ||
                                clickedLocationZ < screenLocationZ - screenWidth || clickedLocationZ > screenLocationZ
                        ) {
                            continue nextScreen;
                        }
                        mouseX = screenLocationZ - clickedLocationZ;
                        mouseY = screenLocationY - clickedLocationY;
                        break;
                    case SOUTH:
                        screenLocationY++;
                        if (clickedLocationX < screenLocationX || clickedLocationX > screenLocationX + screenWidth ||
                                clickedLocationY < screenLocationY - screenHeight || clickedLocationY > screenLocationY
                        ) {
                            continue nextScreen;
                        }
                        mouseX = clickedLocationX - screenLocationX;
                        mouseY = screenLocationY - clickedLocationY;
                        break;
                    case WEST:
                        screenLocationY++;
                        screenLocation.add(-1, 0, 0);
                        if (clickedLocationY < screenLocationY - screenHeight || clickedLocationY > screenLocationY ||
                                clickedLocationZ < screenLocationZ || clickedLocationZ > screenLocationZ + screenWidth
                        ) {
                            continue nextScreen;
                        }
                        mouseX = clickedLocationZ - screenLocationZ;
                        mouseY = screenLocationY - clickedLocationY;
                        break;
                    case NORTH:
                        screenLocationX++;
                        screenLocationY++;
                        screenLocation.add(0, 0, -1);
                        if (clickedLocationX < screenLocationX - screenWidth || clickedLocationX > screenLocationX ||
                                clickedLocationY < screenLocationY - screenHeight || clickedLocationY > screenLocationY
                        ) {
                            continue nextScreen;
                        }
                        mouseX = screenLocationX - clickedLocationX;
                        mouseY = screenLocationY - clickedLocationY;
                        break;
                    default:
                        continue nextScreen;
                }
                i.getCore().onMouseClick((int) (mouseX * 128), (int) (mouseY * 128),type);
                return;
            }else {
            }
        }
    }
}
