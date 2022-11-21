float hslDistance(float* h1, float* h2) {
    const float pi=3.1415926;
    const float r=0.999958243;
    const float h=0.00913839539;
    float x1 = r * h1[2] * h1[1] * cos(h1[0] / 2 * pi);
    float y1 = r * h1[2] * h1[1] * sin(h1[0] / 2 * pi);
    float z1 = h * (1 - h1[2]);
    float x2 = r * h2[2] * h2[1] * cos(h2[0] / 2 * pi);
    float y2 = r * h2[2] * h2[1] * sin(h2[0] / 2 * pi);
    float z2 = h * (1 - h2[2]);
    float dx = x1 - x2;
    float dy = y1 - y2;
    float dz = z1 - z2;
    return sqrt(dx * dx + dy * dy + dz * dz);
}
float* rgbToHsl(int* rgb)
{
    float H,S,L;
    float r = rgb[0];
    float g = rgb[1];
    float b = rgb[2];
    r = r / 255.0;
    g = g / 255.0;
    b = b / 255.0;
    float M = max(max(r, g), b);
    float m = min(min(r, g), b);
    float d = M - m;
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
        S = d / (1 - fabs(2 * L - 1));
    H/=360.0;
    float hsl[3]={H,S,L};
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
    result[2] = (rgb) & 0xff;
    result[3] = (rgb >> 24) & 0xff;
    return result;
}
float* closestColors(float *hsl,__global int *colors,int colorCount) {
    float closest[4] = { -2, 0, 0 ,-4};
    float secondClosest[4] = {-2, 0, 0, -4};
    float temp[4];
    for (int i = 0; i < colorCount; ++i) {
        temp[0]=((float)colors[i*3])/360.0;
        temp[1]=((float)colors[i*3+1])/255.0;
        temp[2]=((float)colors[i*3+2])/255.0;
        temp[3]=i;
        float tempDistance = hslDistance(temp, hsl);
        if (closest[0]==-2 || tempDistance < hslDistance(closest, hsl)) {
            secondClosest[0] = closest[0];
            secondClosest[1] = closest[1];
            secondClosest[2] = closest[2];
            secondClosest[3] = closest[3];
            closest[0] = temp[0];
            closest[1] = temp[1];
            closest[2] = temp[2];
            closest[3] = temp[3];
        }
        else {
            if (secondClosest[0]==-2 || tempDistance < hslDistance(secondClosest, hsl)) {
                secondClosest[0] = temp[0];
                secondClosest[1] = temp[1];
                secondClosest[2] = temp[2];
                secondClosest[3] = temp[3];
            }
        }
    }
    float ret[8];
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
float indexValue(int width,int index) {
    int y = index / width;
    int x = index % width;
    int indexMatrix[64] = {0,  32, 8,  40, 2,  34, 10, 42,
                            48, 16, 56, 24, 50, 18, 58, 26,
                            12, 44, 4,  36, 14, 46, 6,  38,
                            60, 28, 52, 20, 62, 30, 54, 22,
                            3,  35, 11, 43, 1,  33, 9,  41,
                            51, 19, 59, 27, 49, 17, 57, 25,
                            15, 47, 7,  39, 13, 45, 5,  37,
                            63, 31, 55, 23, 61, 29, 53, 21};
    return indexMatrix[(x%8) + (y%8) * 8] / 64.0;
}
__kernel void dither(__global int *image,__global int *palette,__global int *settings,__global char *result) {
    int colorCount = settings[2];
    int id = get_global_id(0);
    int* rgba = intToRgba(image[id]);
    if(rgba[3]!=255){
        result[id]=0;
        return;
    }
    int rgb[3] = {rgba[0],rgba[1],rgba[2]};
    float* hsl = rgbToHsl(rgb);
    float* cs = closestColors(hsl,palette,colorCount);
    float c1[4] = {cs[0],cs[1],cs[2],cs[6]+4};
    float c2[4] = {cs[3],cs[4],cs[5],cs[7]+4};
    float d = indexValue(settings[0],id);
    float hslDiff = hslDistance(hsl, c1) / hslDistance(c2, c1);
    result[id] = (char) (hslDiff < d ? (int)c1[3] : (int)c2[3]);
}