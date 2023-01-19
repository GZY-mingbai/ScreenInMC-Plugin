package cn.mingbai.ScreenInMC.Natives;

public class GPUDither {
    public static native String[] getPlatforms();

    public static native boolean init(int platformId, int[] palette, int colorCount,int pieceSize);

    public static native boolean unInit();

    public static native byte[] dither(int[] image, int width, int height, int pieceSize);
}
