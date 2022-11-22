float colorDistance(int* h1, int* h2) {
    float diff = abs((h1 - h2));
    return min(abs((1.0 - diff)), diff);
}
int* rgbToHsl(int* rgb)
{
    double H,S,L;
    double r = rgb[0];
    double g = rgb[1];
    double b = rgb[2];
    r = r / 255;
    g = g / 255;
    b = b / 255;
    double M = max(max(r, g), b);
    double m = min(min(r, g), b);
    double d = M - m;
    if (d == 0) H = 0;
    else if (M == r)
    {
        H = ((g - b) / d);
        H = H + ((int)H % 6);
    }
    else if (M == g) H = (b - r) / d + 2;
    else H = (r - g) / d + 4;
    H *= 60;
    if (H < 0) H += 360;
    L = (M + m) / 2;
    if (d == 0)
        S = 0;
    else
        S = d / (1 - abs(2 * L - 1));
    S = S * 255;
    L = L * 255;
    int hsl[3] = {H,S,L};
    return hsl;
}
int* intToRgb(int rgb) {
    int result[3];
    result[0] = (rgb >> 16) & 0xff;
    result[1] = (rgb >>  8) & 0xff;
    result[2] = (rgb  ) & 0xff;
    return result;
}
int* intToRgba(int rgb) {
    int result[4];
    result[0] = (rgb >> 16) & 0xff;
    result[1] = (rgb >>  8) & 0xff;
    result[2] = (rgb  ) & 0xff;
    result[3] = (rgb >> 24) & 0xff;
    return result;
}
// int* closestColors(float hue,__global int *colors,int colorCount) {
int* closestColors(float hue,int *colors,int colorCount) {
    int closest[4] = { -2, 0, 0 ,0};
    int secondClosest[4] = {-2, 0, 0, 0};
    int *temp;
    for (int i = 0; i < colorCount; ++i) {
        temp = intToRgb(colors[i]);
        float tempDistance = hueDistance(temp[0], hue);
        if (tempDistance < hueDistance(closest[0], hue)) {
            secondClosest[0] = closest[0];
            secondClosest[1] = closest[1];
            secondClosest[2] = closest[2];
            secondClosest[3] = closest[3];
            closest[0] = temp[0];
            closest[1] = temp[1];
            closest[2] = temp[2];
            closest[3] = i;
        }
        else {
            if (tempDistance < hueDistance(secondClosest[0], hue)) {
                secondClosest[0] = temp[0];
                secondClosest[1] = temp[1];
                secondClosest[2] = temp[2];
                secondClosest[3] = i;
            }
        }
    }
    int ret[8];
    ret[0] = closest[0];
    ret[1] = closest[1];
    ret[2] = closest[2];
    ret[3] = secondClosest[0];
    ret[4] = secondClosest[1];
    ret[5] = secondClosest[2];
    ret[6] = closest[3];
    ret[7] = secondClosest[3];
    return ret;

}
// __kernel void dither(__global int *image,__global int *palette,__global int *settings,__global char *result) {
void dither(int *image,int *palette,int *settings,char *result) {
    int colorCount = settings[2];
    // int id = get_global_id(0);
    int id = 0;
    int* hsl = rgbToHsl(intToRgb(image[id]));
    int* cs = closestColors(hsl[0],palette,colorCount);
    int c1[4] = {cs[0],cs[1],cs[2],cs[6]+4};
    int c2[4] = {cs[3],cs[4],cs[5],cs[7]+4};
    float d = id;
    float hueDiff = colorDistance(hsl[0], c1[0] / hueDistance(c2[0], c1[0]));
    result[id] = (char) hueDiff < d ? c1[3] : c2[3];
}