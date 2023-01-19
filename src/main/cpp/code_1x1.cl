typedef struct RGBA{
        int r;
        int g;
        int b;
        int a;
}RGBA;
RGBA intToRgba(int rgb) {
    RGBA rgba;
    rgba.r = (rgb >> 16) & 0xff;
    rgba.g = (rgb >>  8) & 0xff;
    rgba.b = (rgb  ) & 0xff;
    rgba.a = (rgb >> 24) & 0xff;
    return rgba;
}
int rgbToInt(int r,int g,int b) {
    if(r>255) {
        r=255;
    }
    if(g>255) {
        g=255;
    }
    if(b>255) {
        b=255;
    }
    if(r<0) {
        r=0;
    }
    if(g<0) {
        g=0;
    }
    if(b<0) {
        b=0;
    }
    return 0xFF000000 | ((r << 16) & 0x00FF0000) | ((g << 8) & 0x0000FF00) | (b & 0x000000FF);
}
int colorDistance(int r1,int g1,int b1, int r2,int g2,int b2) {
    int rmean = (b1 + b2) / 2;
    int r = r1 - r2;
    int g = g1 - g2;
    int b = b1 - b2;
    return (int)(sqrt((float)((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8))));
}
int getNearlyColor(__global int *palette,int colorCount,RGBA rgb) {
    int c1=rgb.r;
    int c2=rgb.g;
    int c3=rgb.b;
    RGBA minColor= intToRgba(palette[0]);
    int m1 = minColor.r;
    int m2 = minColor.g;
    int m3 = minColor.b;
    int min = colorDistance(m1,m2,m3,c1,c2,c3);
    int minIndex = 0;
    for (int i=1;i<colorCount;i++) {
        RGBA tempColor = intToRgba(palette[i]);
        int t1 = tempColor.r;
        int t2 = tempColor.g;
        int t3 = tempColor.b;
        int temp = colorDistance(t1,t2,t3,c1,c2,c3);
        if(temp<min) {
            min = temp;
            minIndex = i;
            m1=t1;
            m2=t2;
            m3=t3;
        }
    }
    return minIndex+4;
}
__kernel void dither(__global int *colors,__global int *palette,__global int *settings,__global char *result) {
    int id = get_global_id(0);
    int width = settings[0];
    int colorCount = settings[1];
    RGBA rgba = intToRgba(colors[id]);
    int near = getNearlyColor(palette, colorCount, rgba);
    result[id] = (char)((near / 4) << 2 | (near % 4) & 3);
}