package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import java.awt.image.BufferedImage;

//From https://github.com/Devin-YU/LanczosImage/blob/master/src/LanczosImage/LanczosFliter.java
public class LanczosFilter {
    /*
         * This method is used to zoom in or zoom out a picture, which is based
         * on Lanczos Algorithm.
         * When 0 < widthScale, heightScale < 1 , the picture will be zoomed in.
         * When 1 < widthScale, heightScale, the picture will be zoomed out.
     */
    public static BufferedImage scale(BufferedImage bufferedImage, float widthScale, float heightScale){
        widthScale = 1/widthScale;
        heightScale = 1/heightScale;
        int lanczosSize = widthScale > 1 ? 3 : 2;
        int srcW = bufferedImage.getWidth();
        int srcH = bufferedImage.getHeight();
        int destW = (int)(bufferedImage.getWidth() / widthScale);
        int destH = (int)(bufferedImage.getHeight() / heightScale);

        int[] inPixels = bufferedImage.getRGB(0, 0, srcW, srcH, null, 0, srcW);
        int[] outPixels = new int[destW * destH];

        for (int col = 0; col < destW; col++) {


            double x = col * widthScale;
            double fx = (double)Math.floor(col * widthScale);
            for (int row = 0; row < destH; row ++) {

                double y = row * heightScale;
                double fy = (double)Math.floor(y);
                double[] argb = {0, 0, 0, 0};
                int[] pargb = {0, 0, 0, 0};
                double totalWeight = 0;

                for (int subrow = (int)(fy - lanczosSize + 1); subrow <= fy + lanczosSize; subrow++) {
                    if (subrow < 0 || subrow >= srcH)
                        continue;

                    for (int subcol = (int)(fx - lanczosSize + 1); subcol <= fx + lanczosSize; subcol++) {
                        if (subcol < 0 || subcol >= srcW)
                            continue;

                        double weight = getLanczosFactor(x - subcol,lanczosSize) * getLanczosFactor(y - subrow,lanczosSize);

                        if (weight > 0) {
                            int index = (subrow * srcW + subcol);
                            for (int i = 0; i < 4; i++)
                                pargb[i] = (inPixels[index] >> 24 - 8 * i) & 0xff;
                            totalWeight += weight;
                            for (int i = 0; i < 4; i++)
                                argb[i] += weight * pargb[i];
                        }
                    }
                }
                for (int i = 0; i < 4; i++)
                    pargb[i] = (int)(argb[i] / totalWeight);
                outPixels[row * destW + col] = (clamp(pargb[0]) << 24) |
                        (clamp(pargb[1]) << 16) |
                        (clamp(pargb[2]) << 8) |
                        clamp(pargb[3]);
            }
        }

        BufferedImage bufImg = new BufferedImage(destW, destH, BufferedImage.TYPE_INT_ARGB);
        bufImg.setRGB(0, 0, destW, destH, outPixels, 0, destW);
        return bufImg;
    }
    private static int clamp(int v)
    {
        return v > 255 ? 255 : (v < 0 ? 0 : v);
    }

    private static double getLanczosFactor(double x,int lanczosSize) {
        if (x >= lanczosSize)
            return 0;
        if (Math.abs(x) < 1e-16)
            return 1;
        x *= Math.PI;
        return Math.sin(x) * Math.sin(x/lanczosSize) / (x*x);
    }
}
