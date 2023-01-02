package cn.mingbai.ScreenInMC.Utils;


import java.io.Serializable;
import java.util.Objects;

//From javafx.utils.Pair
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
}
