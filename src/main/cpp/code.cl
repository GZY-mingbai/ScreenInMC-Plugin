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
float colorDistance(int* c1, int* c2) {
    int rmean = (c1[0] + c2[0]) / 2;
    int r = c1[0] - c2[0];
    int g = c1[1] - c2[1];
    int b = c1[2] - c2[2];
    return sqrt((float)((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8)));
}
int* getNearlyColor(__global int *palette,int colorCount,int* rgb) {
    int min = colorDistance(intToRgba(palette[0]),rgb);
    int minIndex = 0;
    int* minColor;
    for (int i=1;i<colorCount;i++) {
        int* tempColor = intToRgba(palette[i]);
        int temp = colorDistance(tempColor,rgb);
        if(temp<min) {
            min = temp;
            minIndex = i;
            minColor=tempColor;
        }
    }
    int result[8];
    result[0]=min;
    result[1]=minIndex+4;
    result[2]=minColor[0];
    result[3]=minColor[1];
    result[4]=minColor[2];
    result[5]=rgb[0]-result[2];
    result[6]=rgb[1]-result[3];
    result[7]=rgb[2]-result[4];
    return result;
}
__kernel void dither(__global int *colors,__global int *palette,__global int *settings,__global char *result,
__global volatile uint *temp) {
    int gid = get_global_id(0);
    printf("%d START\n",gid);
    int width = settings[0];
    int height = settings[1];
    int colorCount = settings[2];
    int size = width*height;
    barrier(CLK_LOCAL_MEM_FENCE);
    int* rgba = intToRgba(colors[gid]);
    if(rgba[3]!=255) {
        result[gid]=0;
        return;
    }
    int* near = getNearlyColor(palette,colorCount,rgba);
    colors[gid] = rgbToInt(near[2],near[3],near[4]);
    result[gid] = (char)((near[1] / 4) << 2 | (near[1] % 4) & 3);
    int x = gid%width;
    int y = gid/width;
    if(!(x == width)-1) {
        int index = width*y+x+1;
        int* rgba_ = intToRgba(colors[index]);
        if(rgba_[3]==255) {
            colors[index]=rgbToInt(rgba_[0]+near[5]*7/16,rgba_[1]+near[6]*7/16,rgba_[2]+near[7]*7/16);
        }
        if(!(y == height)-1) {
            int index_ = width*(y+1)+x+1;
            rgba_ = intToRgba(colors[index_]);
            if(rgba[3]==255) {
                colors[index_]=rgbToInt(rgba_[0]+near[5]*1/16,rgba_[1]+near[6]*1/16,rgba_[2]+near[7]*1/16);
            }
        }
    }
    if(!(y == height)-1) {
        int index = width*(y+1)+x;
        int* rgba_ = intToRgba(colors[index]);
        if(rgba_[3]==255) {
            colors[index]=rgbToInt(rgba_[0]+near[5]*7/16,rgba_[1]+near[6]*7/16,rgba_[2]+near[7]*3/16);
        }
        if(x != 0) {
            int index_ = width*(y+1)+x-1;
            rgba_ = intToRgba(colors[index_]);
            if(rgba[3]==255) {
                colors[index_]=rgbToInt(rgba_[0]+near[5]*1/16,rgba_[1]+near[6]*1/16,rgba_[2]+near[7]*5/16);
            }
        }
    }
    printf("%d END\n",gid);
}