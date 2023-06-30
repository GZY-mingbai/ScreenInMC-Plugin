typedef struct RGBA{
        int r;
        int g;
        int b;
        int a;
}RGBA;
typedef struct ClosestColorResult{
        int index;
        int color;
        int r;
        int g;
        int b;
}ClosestColorResult;

__constant unsigned char ColorsListA[] = {
##LIST1##
};

__constant unsigned char ColorsListB[] = {
##LIST2##
};

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
ClosestColorResult getClosestColor(__global int *palette,int colorCount,RGBA rgb) {
    int c1=rgb.r;
    int c2=rgb.g;
    int c3=rgb.b;
    ClosestColorResult result;
    int minIndex;
    int index = c3*##ColorsCount##/256*##ColorsCount##*##ColorsCount##+c2*##ColorsCount##/256*##ColorsCount##+c1*##ColorsCount##/256;
    if(index>=##listLength##){
        minIndex = ColorsListB[index-##listLength##];
    }else{
        minIndex = ColorsListA[index];
    }
    result.index=minIndex;
    result.color=palette[minIndex-4];
    RGBA tempColor = intToRgba(result.color);
    result.r=c1-tempColor.r;
    result.g=c2-tempColor.g;
    result.b=c3-tempColor.b;
    return result;
}
__kernel void dither(__global int *colors,__global int *palette,__global int *settings,__global char *result) {
    int id = get_global_id(0);
    int width = settings[0];
    int pieceSize=settings[2];
    int colorCount = settings[1];
    if (pieceSize == 1) {
        RGBA rgba = intToRgba(colors[id]);
        ClosestColorResult near = getClosestColor(palette, colorCount, rgba);
        result[id] = (char)((near.index / 4) << 2 | (near.index % 4) & 3);
        return;
    }
    int r = id*pieceSize/width*width/pieceSize*pieceSize*pieceSize+id%(width/pieceSize)*pieceSize;
    for (int y = 0; y < pieceSize; ++y) {
        for (int x = 0; x < pieceSize; ++x) {
            RGBA rgba = intToRgba(colors[r]);
            if(rgba.a==255) {
                ClosestColorResult near = getClosestColor(palette,colorCount,rgba);
                colors[r] = near.color;
                result[r] = (char)((near.index / 4) << 2 | (near.index % 4) & 3);
                if(x != pieceSize-1) {
                    int index_ = r+1;
                    RGBA rgba_ = intToRgba(colors[index_]);
                    if(rgba_.a==255) {
                        colors[index_]=rgbToInt(rgba_.r+near.r*0.4375,rgba_.g+near.g*0.4375,rgba_.b+near.b*0.4375);
                    }
                    if(y != pieceSize-1) {                        
                        index_ += width;
                        rgba_ = intToRgba(colors[index_]);
                        if(rgba_.a==255) {
                            colors[index_]=rgbToInt(rgba_.r+near.r*0.0625,rgba_.g+near.g*0.0625,rgba_.b+near.b*0.0625);
                        }
                    }
                }
                if(y != pieceSize-1) {
                    int index_ = r+width;
                    RGBA rgba_ = intToRgba(colors[index_]);
                    if(rgba_.a==255) {
                        colors[index_]=rgbToInt(rgba_.r+near.r*0.1875,rgba_.g+near.g*0.1875,rgba_.b+near.b*0.1875);
                    }
                    if(x != 0) {
                        index_ -= 1;
                        rgba_ = intToRgba(colors[index_]);
                        if(rgba_.a==255) {
                            colors[index_]=rgbToInt(rgba_.r+near.r*0.3125,rgba_.g+near.g*0.3125,rgba_.b+near.b*0.3125);
                        }
                    }
                }
            }else{
                result[r]=0;
            }
            r++;
        }
        r+=width-pieceSize;
    }
}
__kernel void scale(__global int* image1, __global int* image2, __global int* width) {
    int id = get_global_id(0);
    int w1 = width[0];
    int w2 = width[1];
    image2[id / w1 * w2 + id % w1] = image1[id];
}
__kernel void scale_(__global char* image1, __global char* image2, __global int* width) {
    int id = get_global_id(0);
    int w1 = width[0];
    int w2 = width[1];
    image2[id] = image1[id / w1 * w2 + id % w1];
}