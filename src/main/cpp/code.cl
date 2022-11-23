int* intToRgba(int rgb) {
    int result[4];
    result[0] = (rgb >> 16) & 0xff;
    result[1] = (rgb >>  8) & 0xff;
    result[2] = (rgb  ) & 0xff;
    result[3] = (rgb >> 24) & 0xff;
    return result;
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
int colorDistance(int* c1, int* c2) {
    int rmean = (c1[0] + c2[0]) / 2;
    int r = c1[0] - c2[0];
    int g = c1[1] - c2[1];
    int b = c1[2] - c2[2];
    return (int)(sqrt((float)((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8))));
}
int* getNearlyColor(__global int *palette,int colorCount,int* rgb) {
    int* minColor= intToRgba(palette[0]);
    int min = colorDistance(minColor,rgb);
    int minIndex = 0;
    for (int i=1;i<colorCount;i++) {
        int* tempColor = intToRgba(palette[i]);
        int temp = colorDistance(tempColor,rgb);
        if(temp<min) {
            min = temp;
            minIndex = i;
            minColor[0]=tempColor[0];
            minColor[1]=tempColor[1];
            minColor[2]=tempColor[2];
        }
    }
    int result[5];
    result[0]=minIndex+4;
    result[1]=palette[minIndex];
    result[2]=rgb[0]-minColor[0];
    result[3]=rgb[1]-minColor[1];
    result[4]=rgb[2]-minColor[2];
    return result;
}
__kernel void dither(__global int *colors,__global int *palette,__global int *settings,__global char *result) {
    int id = get_global_id(0);
    int width = settings[0];
    int pieceSize=settings[2];
    int r = id*pieceSize/width*width/pieceSize*pieceSize*pieceSize+id%(width/pieceSize)*pieceSize;
    int colorCount = settings[1];
    for (int y = 0; y < pieceSize; ++y) {
        for (int x = 0; x < pieceSize; ++x) {
            int* rgba = intToRgba(colors[r]);
            if(rgba[3]==255) {
                int* near = getNearlyColor(palette,colorCount,rgba);
                colors[r] = near[1];
                result[r] = (char)((near[0] / 4) << 2 | (near[0] % 4) & 3);
                if(x != pieceSize-1) {
                    int index_ = r+1;
                    int* rgba_ = intToRgba(colors[index_]);
                    if(rgba_[3]==255) {
                        colors[index_]=rgbToInt(rgba_[0]+near[2]*0.4375,rgba_[1]+near[3]*0.4375,rgba_[2]+near[4]*0.4375);
                    }
                    if(y != pieceSize-1) {                        
                        index_ += width;
                        rgba_ = intToRgba(colors[index_]);
                        if(rgba_[3]==255) {
                            colors[index_]=rgbToInt(rgba_[0]+near[2]*0.0625,rgba_[1]+near[3]*0.0625,rgba_[2]+near[4]*0.0625);
                        }
                    }
                }
                if(y != pieceSize-1) {
                    int index_ = r+width;
                    int* rgba_ = intToRgba(colors[index_]);
                    if(rgba_[3]==255) {
                        colors[index_]=rgbToInt(rgba_[0]+near[2]*0.1875,rgba_[1]+near[3]*0.1875,rgba_[2]+near[4]*0.1875);
                    }
                    if(x != 0) {
                        index_ -= 1;
                        rgba_ = intToRgba(colors[index_]);
                        if(rgba_[3]==255) {
                            colors[index_]=rgbToInt(rgba_[0]+near[2]*0.3125,rgba_[1]+near[3]*0.3125,rgba_[2]+near[4]*0.3125);
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