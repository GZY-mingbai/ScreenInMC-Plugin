float colorDistance(int* c1, int* c2) {
    int rmean = (c1[0] + c2[0]) / 2;
    int r = c1[0] - c2[0];
    int g = c1[1] - c2[1];
    int b = c1[2] - c2[2];
    return sqrt((float)((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8)))/764.833313;
}
int* intToRgba(int rgb) {
    int result[4];
    result[0] = (rgb >> 16) & 0xff;
    result[1] = (rgb >>  8) & 0xff;
    result[2] = (rgb) & 0xff;
    result[3] = (rgb >> 24) & 0xff;
    return result;
}
char closestColors(int *rgba,__global int *colors,int colorCount,float v) {
    float a = 2; 
    int b = 0;
    for (int i = 0; i < colorCount; ++i) {
        int* colorRgb = intToRgba(colors[i]);
        float n = colorDistance(rgba,colorRgb);
        float c = fabs(n-v);
        if(c<a){
            a = c;
            b = i+4;
        }
    }
    return (char)((b / 4) << 2 | (b % 4) & 3);
}
float indexValue(int width,int index) {
    int y = index / width;
    int x = index % width;
    int indexMatrix[256] = {0,11,3,0,0,0,3,0,0,12,3,15,0,0,3,0,7,4,10,7,8,0,11,0,8,0,0,0,8,0,0,0,0,0,1,12,2,14,1,0,2,14,0,0,0,0,0,13,0,0,8,5,10,6,9,0,10,6,0,0,0,0,9,5,0,0,3,0,0,0,0,0,0,0,3,15,0,12,0,15,8,4,11,0,0,4,0,0,8,4,0,7,8,4,0,7,0,0,0,13,2,0,1,13,2,0,0,0,0,0,1,13,10,6,9,5,10,0,0,0,10,0,0,5,0,0,0,5,0,12,0,0,0,12,0,0,0,0,3,0,0,12,0,15,8,4,11,7,8,0,11,0,8,4,0,7,0,4,11,0,0,14,0,0,0,14,1,13,2,0,1,0,2,0,1,0,0,0,9,5,0,6,0,0,0,6,9,0,10,6,9,0,0,0,0,0,0,12,0,0,0,12,0,15,0,12,0,0,8,0,0,0,8,4,0,7,8,0,11,7,8,4,11,0,2,0,1,13,0,0,0,0,0,0,1,13,2,14,1,0,10,0,0,0,10,0,0,5,0,0,9,0,0,0,0,5};
    return indexMatrix[(x%16) + (y%16) * 16] / 256.0;
}
__kernel void dither(__global int *image,__global int *palette,__global  int *settings,__global char *result) {
    int colorCount = settings[2];
    int id = get_global_id(0);
    int* rgba = intToRgba(image[id]);
    if(rgba[3]!=255){
        result[id]=0;
        return;
    }
    result[id] = closestColors(rgba,palette,colorCount,indexValue(settings[0],id));
    
}