package cn.mingbai.ScreenInMC.Utils;


import cn.mingbai.ScreenInMC.Screen.Screen;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.io.Serializable;
import java.util.Objects;

import static cn.mingbai.ScreenInMC.Screen.Screen.Facing.*;

public class Utils {
    public static final byte[] intToByteArray(int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }

    public static Pair<String, String> getSystem() {
        String systemName = System.getProperty("os.name").replace(" ", "").toLowerCase();
        String systemArch = System.getProperty("os.arch");
        if (systemName.indexOf("windows") != -1) {
            systemName = "windows";
        } else if (systemName.indexOf("linux") != -1) {
            systemName = "linux";
        } else if (systemName.indexOf("macosx") != -1) {
            systemName = "macosx";
        } else {
            systemName = null;
        }
        if (systemArch.equals("x86_64") || systemArch.equals("amd64") || systemArch.equals("x64") || systemArch.equals("ia64")) {
            systemArch = "amd64";
        } else if (systemArch.equals("x86_32") || systemArch.equals("i386") || systemArch.equals("x86") || systemArch.equals("x32") || systemArch.equals("ia32")) {
            systemArch = "i386";
        } else if (systemArch.equals("arm") || systemArch.equals("arm32") || systemArch.equals("aarch32")) {
            systemArch = "arm";
        } else if (systemArch.equals("arm64") || systemArch.equals("aarch64")) {
            systemArch = "arm64";
        } else {
            systemArch = null;
        }
        return new Pair<>(systemName, systemArch);
    }

    public static String getLibraryPrefix(String type) {
        switch (type) {
            case "windows":
                return ".dll";
            case "linux":
                return ".so";
            case "macosx":
                return "dylib";
        }
        return null;
    }

    public static int[] toPrimitive(Integer[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return new int[0];
        }
        final int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].intValue();
        }
        return result;
    }

    public enum MouseClickType {
        LEFT,
        RIGHT
    }

    //From javafx.utils.Pair
    public static class Pair<K, V> implements Serializable {

        /**
         * Key of this <code>Pair</code>.
         */
        private final K key;
        /**
         * Value of this this <code>Pair</code>.
         */
        private final V value;

        /**
         * Creates a new pair
         *
         * @param key   The key for this pair
         * @param value The value to use for this pair
         */
        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Gets the key for this pair.
         *
         * @return key for this pair
         */
        public K getKey() {
            return key;
        }

        /**
         * Gets the value for this pair.
         *
         * @return value for this pair
         */
        public V getValue() {
            return value;
        }

        /**
         * <p><code>String</code> representation of this
         * <code>Pair</code>.</p>
         *
         * <p>The default name/value delimiter '=' is always used.</p>
         *
         * @return <code>String</code> representation of this <code>Pair</code>
         */
        @Override
        public String toString() {
            return key + "=" + value;
        }

        /**
         * <p>Generate a hash code for this <code>Pair</code>.</p>
         *
         * <p>The hash code is calculated using both the name and
         * the value of the <code>Pair</code>.</p>
         *
         * @return hash code for this <code>Pair</code>
         */
        @Override
        public int hashCode() {
            // name's hashCode is multiplied by an arbitrary prime number (13)
            // in order to make sure there is a difference in the hashCode between
            // these two parameters:
            //  name: a  value: aa
            //  name: aa value: a
            return key.hashCode() * 13 + (value == null ? 0 : value.hashCode());
        }

        /**
         * <p>Test this <code>Pair</code> for equality with another
         * <code>Object</code>.</p>
         *
         * <p>If the <code>Object</code> to be tested is not a
         * <code>Pair</code> or is <code>null</code>, then this method
         * returns <code>false</code>.</p>
         *
         * <p>Two <code>Pair</code>s are considered equal if and only if
         * both the names and values are equal.</p>
         *
         * @param o the <code>Object</code> to test for
         *          equality with this <code>Pair</code>
         * @return <code>true</code> if the given <code>Object</code> is
         * equal to this <code>Pair</code> else <code>false</code>
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof Pair) {
                Pair pair = (Pair)o;
                if (!Objects.equals(key, pair.key)) return false;
                return Objects.equals(value, pair.value);
            }
            return false;
        }
    }
    public static class ScreenClickResult{
        private boolean clicked=false;
        private double mouseX=-1;
        private double mouseY=-1;
        private ScreenClickResult(boolean clicked,double mouseX,double mouseY){
            this.clicked = clicked;
            this.mouseX=mouseX;
            this.mouseY=mouseY;
        }
        private ScreenClickResult(){}

        public double getMouseX() {
            return mouseX;
        }

        public double getMouseY() {
            return mouseY;
        }

        public boolean isClicked() {
            return clicked;
        }
    }
    public static ScreenClickResult getScreenClickAt(Location playerEyeLocation,Location screenLocation,Screen.Facing facing,int screenWidth,int screenHeight,int maxDistance){
        if(!playerEyeLocation.getWorld().equals(screenLocation.getWorld()) || screenLocation.distance(playerEyeLocation)>maxDistance){
            return new ScreenClickResult();
        }
        Vector v1;
        switch (facing) {
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
        Vector v3 = playerEyeLocation.getDirection();
        Vector v4 = playerEyeLocation.toVector();
        double d = (v2.clone().subtract(v4).dot(v1)) / (v3.dot(v1));
        if (d < 0) {
            return new ScreenClickResult();
        }
        Vector v5 = v3.clone().normalize().multiply(d).add(v4);
        Location clickedLocation = v5.toLocation(playerEyeLocation.getWorld());
        double clickedLocationX = clickedLocation.getX();
        double clickedLocationY = clickedLocation.getY();
        double clickedLocationZ = clickedLocation.getZ();
        double screenLocationX = screenLocation.getX();
        double screenLocationY = screenLocation.getY();
        double screenLocationZ = screenLocation.getZ();
        double mouseX;
        double mouseY;
                switch (facing) {
            case UP:
                if (clickedLocationX < screenLocationX || clickedLocationX > screenLocationX + screenWidth ||
                        clickedLocationZ < screenLocationZ || clickedLocationZ > screenLocationZ + screenHeight
                ) {
                    return new ScreenClickResult();
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
                    return new ScreenClickResult();
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
                    return new ScreenClickResult();
                }
                mouseX = screenLocationZ - clickedLocationZ;
                mouseY = screenLocationY - clickedLocationY;
                break;
            case SOUTH:
                screenLocationY++;
                if (clickedLocationX < screenLocationX || clickedLocationX > screenLocationX + screenWidth ||
                        clickedLocationY < screenLocationY - screenHeight || clickedLocationY > screenLocationY
                ) {
                    return new ScreenClickResult();
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
                    return new ScreenClickResult();
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
                    return new ScreenClickResult();
                }
                mouseX = screenLocationX - clickedLocationX;
                mouseY = screenLocationY - clickedLocationY;
                break;
            default:
                return new ScreenClickResult();
        }
        return new ScreenClickResult(true,mouseX,mouseY);
    }
}
