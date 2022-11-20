package cn.mingbai.ScreenInMC.Natives;

public class GPUDither {
    public static native byte[] dither(int[] image,int[] palette,int width,int height);
}
