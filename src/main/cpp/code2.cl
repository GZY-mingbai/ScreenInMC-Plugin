float colorDistance(int* c1, int* c2) {
    int rmean = (c1[0] + c2[0]) / 2;
    int r = c1[0] - c2[0];
    int g = c1[1] - c2[1];
    int b = c1[2] - c2[2];
    return sqrt((float)((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8)));
}
int* intToRgba(int rgb) {
    int result[4];
    result[0] = (rgb >> 16) & 0xff;
    result[1] = (rgb >>  8) & 0xff;
    result[2] = (rgb) & 0xff;
    result[3] = (rgb >> 24) & 0xff;
    return result;
}
int* closestColors(int *rgba,__global int *colors,int colorCount) {
    int closest[4] = { -256, -256, -256 ,-4};
    int secondClosest[4] = {-256, -256, -256, -4};
    int temp[4];
    for (int i = 0; i < colorCount; ++i) {
        int* colorRgb = intToRgba(colors[i]);
        temp[0]=colorRgb[0];
        temp[1]=colorRgb[1];
        temp[2]=colorRgb[2];
        temp[3]=i;
        float tempDistance = colorDistance(temp, rgba);
        if (closest[0]==-256 || tempDistance < colorDistance(closest, rgba)) {
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
            if (secondClosest[0]==-256 || tempDistance < colorDistance(secondClosest, rgba)) {
                secondClosest[0] = temp[0];
                secondClosest[1] = temp[1];
                secondClosest[2] = temp[2];
                secondClosest[3] = temp[3];
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
    int* cs = closestColors(rgba,palette,colorCount);
    int c1[4] = {cs[0],cs[1],cs[2],cs[6]+4};
    int c2[4] = {cs[3],cs[4],cs[5],cs[7]+4};
    float d = indexValue(settings[0],id);
    float colorDiff = colorDistance(rgba, c1) / colorDistance(c2, c1);
    result[id] = (char) (colorDiff < d ? c1[3] : c2[3]);
}