#include "ScreenInMC.h"

JNIEXPORT jobjectArray JNICALL Java_cn_mingbai_ScreenInMC_Natives_GPUDither_getPlatforms(JNIEnv *env, jclass cls)
{
    cl_uint count;
    char **names;
    getPlatformNames(&count, &names);
    jobjectArray result = env->NewObjectArray(count, env->FindClass("java/lang/String"), 0);
    for (cl_uint i = 0; i < count; i++)
    {
        env->SetObjectArrayElement(result, i, env->NewStringUTF(names[i]));
    }
    return result;
}
JNIEXPORT jboolean JNICALL Java_cn_mingbai_ScreenInMC_Natives_GPUDither_init(JNIEnv *env, jclass cls, jint id, jintArray palette_, jint paletteColorCount_,jint psize)
{
    usePlatform = id;
    jboolean jbool = false;
    paletteColorCount = paletteColorCount_;
    jint *palette__ = env->GetIntArrayElements(palette_, &jbool);
    if (palette__ == 0)
    {
        return false;
    }
    palette = (int *)palette__;
    env->ReleaseIntArrayElements(palette_, palette__, 1);
    return init((int)psize);
}
JNIEXPORT jbyteArray JNICALL Java_cn_mingbai_ScreenInMC_Natives_GPUDither_dither(JNIEnv *env, jclass cls, jintArray image, jint width, jint height, jint pieceSize)
{
    jboolean jbool = false;
    jint *image_ = env->GetIntArrayElements(image, &jbool);
    if (image_ == 0)
    {
        return 0;
    }
    int size = width * height;
    jbyte *result_ = new jbyte[size];
    dither((int *)image_, width, height, (char *)result_, pieceSize);
    jbyteArray result = env->NewByteArray(size);
    env->SetByteArrayRegion(result, 0, size, result_);
    env->ReleaseIntArrayElements(image, image_, 0);
    return result;
}
bool getPlatforms(cl_uint *count, cl_platform_id **result)
{
    cl_int error = 0;
    cl_uint platformsCount;
    error = clGetPlatformIDs(0, 0, &platformsCount);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    cl_platform_id *platforms = new cl_platform_id[platformsCount];
    error = clGetPlatformIDs(platformsCount, platforms, 0);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    *result = platforms;
    *count = platformsCount;
    return true;
}
bool getPlatformNames(cl_uint *count, char ***names)
{
    cl_int error = 0;
    cl_uint platformsCount;
    cl_platform_id *platforms;
    if (!getPlatforms(&platformsCount, &platforms))
    {
        return false;
    };
    char **platformNames = new char *[platformsCount];
    for (cl_uint i = 0; i < platformsCount; i++)
    {
        size_t platformNameLength = 0;
        error = clGetPlatformInfo(platforms[i], CL_PLATFORM_NAME, 0, 0, &platformNameLength);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        platformNames[i] = new char[platformNameLength];
        error = clGetPlatformInfo(platforms[i], CL_PLATFORM_NAME, platformNameLength, platformNames[i], 0);
        if (error != CL_SUCCESS)
        {
            return false;
        }
    }
    delete[] platforms;
    *names = platformNames;
    *count = platformsCount;
    return true;
}
bool init(int size)
{
    const char *code;
    if(size==1){
        code = "typedef struct RGBA{\nint r;\nint g;\nint b;\nint a;\n}RGBA;\nRGBA intToRgba(int rgb) {\nRGBA rgba;\nrgba.r = (rgb >> 16) & 0xff;\nrgba.g = (rgb >>  8) & 0xff;\nrgba.b = (rgb  ) & 0xff;\nrgba.a = (rgb >> 24) & 0xff;\nreturn rgba;\n}\nint rgbToInt(int r,int g,int b) {\nif(r>255) {\nr=255;\n}\nif(g>255) {\ng=255;\n}\nif(b>255) {\nb=255;\n}\nif(r<0) {\nr=0;\n}\nif(g<0) {\ng=0;\n}\nif(b<0) {\nb=0;\n}\nreturn 0xFF000000 | ((r << 16) & 0x00FF0000) | ((g << 8) & 0x0000FF00) | (b & 0x000000FF);\n}\nint colorDistance(int r1,int g1,int b1, int r2,int g2,int b2) {\nint rmean = (b1 + b2) / 2;\nint r = r1 - r2;\nint g = g1 - g2;\nint b = b1 - b2;\nreturn (int)(sqrt((float)((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8))));\n}\nint getNearlyColor(__global int *palette,int colorCount,RGBA rgb) {\nint c1=rgb.r;\nint c2=rgb.g;\nint c3=rgb.b;\nRGBA minColor= intToRgba(palette[0]);\nint m1 = minColor.r;\nint m2 = minColor.g;\nint m3 = minColor.b;\nint min = colorDistance(m1,m2,m3,c1,c2,c3);\nint minIndex = 0;\nfor (int i=1;i<colorCount;i++) {\nRGBA tempColor = intToRgba(palette[i]);\nint t1 = tempColor.r;\nint t2 = tempColor.g;\nint t3 = tempColor.b;\nint temp = colorDistance(t1,t2,t3,c1,c2,c3);\nif(temp<min) {\nmin = temp;\nminIndex = i;\nm1=t1;\nm2=t2;\nm3=t3;\n}\n}\nreturn minIndex+4;\n}\n__kernel void dither(__global int *colors,__global int *palette,__global int *settings,__global char *result) {\nint id = get_global_id(0);\nint width = settings[0];\nint colorCount = settings[1];\nRGBA rgba = intToRgba(colors[id]);\nint near = getNearlyColor(palette, colorCount, rgba);\nresult[id] = (char)((near / 4) << 2 | (near % 4) & 3);\n}\n__kernel void scale(__global int* image1, __global int* image2, __global int* width) {\nint id = get_global_id(0);\nint w1 = width[0];\nint w2 = width[1];\nimage2[id / w1 * w2 + id % w1] = image1[id];\n}\n__kernel void scale_(__global char* image1, __global char* image2, __global int* width) {\nint id = get_global_id(0);\nint w1 = width[0];\nint w2 = width[1];\nimage2[id] = image1[id / w1 * w2 + id % w1];\n}";
    }else{
        code = "typedef struct RGBA{\nint r;\nint g;\nint b;\nint a;\n}RGBA;\ntypedef struct NearlyColorResult{\nint index;\nint color;\nint r;\nint g;\nint b;\n}NearlyColorResult;\nRGBA intToRgba(int rgb) {\nRGBA rgba;\nrgba.r = (rgb >> 16) & 0xff;\nrgba.g = (rgb >>  8) & 0xff;\nrgba.b = (rgb  ) & 0xff;\nrgba.a = (rgb >> 24) & 0xff;\nreturn rgba;\n}\nint rgbToInt(int r,int g,int b) {\nif(r>255) {\nr=255;\n}\nif(g>255) {\ng=255;\n}\nif(b>255) {\nb=255;\n}\nif(r<0) {\nr=0;\n}\nif(g<0) {\ng=0;\n}\nif(b<0) {\nb=0;\n}\nreturn 0xFF000000 | ((r << 16) & 0x00FF0000) | ((g << 8) & 0x0000FF00) | (b & 0x000000FF);\n}\nint colorDistance(int r1,int g1,int b1, int r2,int g2,int b2) {\nint rmean = (b1 + b2) / 2;\nint r = r1 - r2;\nint g = g1 - g2;\nint b = b1 - b2;\nreturn (int)(sqrt((float)((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8))));\n}\nNearlyColorResult getNearlyColor(__global int *palette,int colorCount,RGBA rgb) {\nint c1=rgb.r;\nint c2=rgb.g;\nint c3=rgb.b;\nRGBA minColor= intToRgba(palette[0]);\nint m1 = minColor.r;\nint m2 = minColor.g;\nint m3 = minColor.b;\nint min = colorDistance(m1,m2,m3,c1,c2,c3);\nint minIndex = 0;\nfor (int i=1;i<colorCount;i++) {\nRGBA tempColor = intToRgba(palette[i]);\nint t1 = tempColor.r;\nint t2 = tempColor.g;\nint t3 = tempColor.b;\nint temp = colorDistance(t1,t2,t3,c1,c2,c3);\nif(temp<min) {\nmin = temp;\nminIndex = i;\nm1=t1;\nm2=t2;\nm3=t3;\n}\n}\nNearlyColorResult result;\nresult.index=minIndex+4;\nresult.color=palette[minIndex];\nresult.r=c1-m1;\nresult.g=c2-m2;\nresult.b=c3-m3;\nreturn result;\n}\n__kernel void dither(__global int *colors,__global int *palette,__global int *settings,__global char *result) {\nint id = get_global_id(0);\nint width = settings[0];\nint pieceSize=settings[2];\nint colorCount = settings[1];\nif (pieceSize == 1) {\nRGBA rgba = intToRgba(colors[id]);\nNearlyColorResult near = getNearlyColor(palette, colorCount, rgba);\nresult[id] = (char)((near.index / 4) << 2 | (near.index % 4) & 3);\nreturn;\n}\nint r = id*pieceSize/width*width/pieceSize*pieceSize*pieceSize+id%(width/pieceSize)*pieceSize;\nfor (int y = 0; y < pieceSize; ++y) {\nfor (int x = 0; x < pieceSize; ++x) {\nRGBA rgba = intToRgba(colors[r]);\nif(rgba.a==255) {\nNearlyColorResult near = getNearlyColor(palette,colorCount,rgba);\ncolors[r] = near.color;\nresult[r] = (char)((near.index / 4) << 2 | (near.index % 4) & 3);\nif(x != pieceSize-1) {\nint index_ = r+1;\nRGBA rgba_ = intToRgba(colors[index_]);\nif(rgba_.a==255) {\ncolors[index_]=rgbToInt(rgba_.r+near.r*0.4375,rgba_.g+near.g*0.4375,rgba_.b+near.b*0.4375);\n}\nif(y != pieceSize-1) {\nindex_ += width;\nrgba_ = intToRgba(colors[index_]);\nif(rgba_.a==255) {\ncolors[index_]=rgbToInt(rgba_.r+near.r*0.0625,rgba_.g+near.g*0.0625,rgba_.b+near.b*0.0625);\n}\n}\n}\nif(y != pieceSize-1) {\nint index_ = r+width;\nRGBA rgba_ = intToRgba(colors[index_]);\nif(rgba_.a==255) {\ncolors[index_]=rgbToInt(rgba_.r+near.r*0.1875,rgba_.g+near.g*0.1875,rgba_.b+near.b*0.1875);\n}\nif(x != 0) {\nindex_ -= 1;\nrgba_ = intToRgba(colors[index_]);\nif(rgba_.a==255) {\ncolors[index_]=rgbToInt(rgba_.r+near.r*0.3125,rgba_.g+near.g*0.3125,rgba_.b+near.b*0.3125);\n}\n}\n}\n}else{\nresult[r]=0;\n}\nr++;\n}\nr+=width-pieceSize;\n}\n}\n__kernel void scale(__global int* image1, __global int* image2, __global int* width) {\nint id = get_global_id(0);\nint w1 = width[0];\nint w2 = width[1];\nimage2[id / w1 * w2 + id % w1] = image1[id];\n}\n__kernel void scale_(__global char* image1, __global char* image2, __global int* width) {\nint id = get_global_id(0);\nint w1 = width[0];\nint w2 = width[1];\nimage2[id] = image1[id / w1 * w2 + id % w1];\n}";
    }
    return init(code);
}
bool init(const char *code)
{
    if(usePlatform==-1){
        return true;
    }
    cl_int error = 0;
    cl_uint platformsCount;
    cl_platform_id *platforms;
    if (!getPlatforms(&platformsCount, &platforms))
    {
        return false;
    };
    if ((cl_uint)usePlatform > (platformsCount - 1))
    {
        return false;
    }
    cl_platform_id platform = platforms[usePlatform];
    delete[] platforms;
    cl_uint numDevice;
    error = clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, 0, 0,
                           &numDevice);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    cl_device_id *device = new cl_device_id[numDevice];
    error = clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, numDevice,
                           device, 0);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    cl_context_properties contextProperties[3] = {CL_CONTEXT_PLATFORM, (cl_context_properties)platform, 0};
    context = clCreateContext(contextProperties, numDevice, device, 0, 0, &error);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    size_t devicesSize = 0;
    error = clGetContextInfo(context, CL_CONTEXT_DEVICES, 0, 0, &devicesSize);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    cl_device_id *devices = new cl_device_id[devicesSize];
    error = clGetContextInfo(context, CL_CONTEXT_DEVICES, devicesSize, devices, 0);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    size_t codeSize = 0;
    for (int i = 0; code[i] != '\0'; i++)
    {
        codeSize++;
    }
    program = clCreateProgramWithSource(context, 1, &code, &codeSize, &error);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    error = clBuildProgram(program, 1, devices, 0, 0, 0);
    if (error != CL_SUCCESS)
    {
        size_t len;
        char buffer[8 * 1024];
        printf("Error: Failed to build program.\n");
        clGetProgramBuildInfo(program, *devices, CL_PROGRAM_BUILD_LOG, sizeof(buffer), buffer, &len);
        printf(buffer);
        return false;
    }
    kernel = clCreateKernel(program, "dither", &error);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    kernel_ = clCreateKernel(program, "scale", &error);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    kernel__ = clCreateKernel(program, "scale_", &error);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    commandQueue = clCreateCommandQueueWithProperties(context, devices[0], 0, &error);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    delete[] devices;
    delete device;
    return true;
}
bool dither(int *image, int width, int height, char *result, int pieceSize)
{
    if(usePlatform==-1){
        return ditherWithCPU(image,width,height,result);
    }
    int dh, dw;
    if (height % pieceSize == 0) {
        dh = 0;
    }
    else {
        dh = pieceSize - height % pieceSize;
    }
    if (width % pieceSize == 0) {
        dw = 0;
    }
    else {
        dw = pieceSize - width % pieceSize;
    }
    bool useNewImage = false;
    int newSize1;
    int newSize2;
    int newWidth;
    char* newResult = 0;
    cl_int error = 0;
    if (dh != 0 || dw != 0) {
        int width_ = width + dw;
        newWidth = width_;
        int height_ = height + dh;
        int size1 = width * height;
        newSize1 = size1;
        int size2 = width_ * height_;
        newSize2 = size2;
        int len1 = size1 * sizeof(int);
        int len2 = size2 * sizeof(int);
        int len3 = sizeof(int)*2;
        int* newImage = new int[size2];
        newResult = new char[size2];
        result = newResult;
        int* w = new int[2] {width, width_};
        cl_mem data1 = clCreateBuffer(context, CL_MEM_READ_WRITE, len1, 0, &error);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        cl_mem data2 = clCreateBuffer(context, CL_MEM_READ_WRITE, len2, 0, &error);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        cl_mem data3 = clCreateBuffer(context, CL_MEM_READ_WRITE, len3, 0, &error);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        clEnqueueWriteBuffer(commandQueue, data1, CL_TRUE, 0, len1, image, 0, 0, 0);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        clEnqueueWriteBuffer(commandQueue, data2, CL_TRUE, 0, len2, newImage, 0, 0, 0);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        clEnqueueWriteBuffer(commandQueue, data3, CL_TRUE, 0, len3, w, 0, 0, 0);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        error = clSetKernelArg(kernel_, 0, sizeof(cl_mem), &data1);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        error = clSetKernelArg(kernel_, 1, sizeof(cl_mem), &data2);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        error = clSetKernelArg(kernel_, 2, sizeof(cl_mem), &data3);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        size_t global_work_size[] = { (size_t)(size1)};
        size_t local_work_size[] = { 1 };
        error = clEnqueueNDRangeKernel(commandQueue, kernel_, 1, 0, &global_work_size[0], &local_work_size[0], 0, 0, 0);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        error = clFinish(commandQueue);
        clEnqueueReadBuffer(commandQueue, data2, CL_TRUE, 0, len2, newImage, 0, 0, 0);
        error = clReleaseMemObject(data1);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        error = clReleaseMemObject(data2);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        error = clReleaseMemObject(data3);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        useNewImage = true;
        image = newImage;
        width = width_;
        height = height_;
        delete[] w;
    }
    int size = width * height;
    int len1 = size * sizeof(int);
    int len2 = paletteColorCount * sizeof(int);
    int len3 = 3 * sizeof(int);
    int len4 = size * sizeof(char);
    int *settings = new int[3]{width, paletteColorCount, pieceSize};
    cl_mem data1 = clCreateBuffer(context, CL_MEM_READ_WRITE, len1, 0, &error);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    cl_mem data2 = clCreateBuffer(context, CL_MEM_READ_WRITE, len2, 0, &error);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    cl_mem data3 = clCreateBuffer(context, CL_MEM_READ_WRITE, len3, 0, &error);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    cl_mem data4 = clCreateBuffer(context, CL_MEM_READ_WRITE, len4, 0, &error);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    clEnqueueWriteBuffer(commandQueue, data1, CL_TRUE, 0, len1, image, 0, 0, 0);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    clEnqueueWriteBuffer(commandQueue, data2, CL_TRUE, 0, len2, palette, 0, 0, 0);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    clEnqueueWriteBuffer(commandQueue, data3, CL_TRUE, 0, len3, settings, 0, 0, 0);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    clEnqueueWriteBuffer(commandQueue, data4, CL_TRUE, 0, len4, result, 0, 0, 0);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    error = clSetKernelArg(kernel, 0, sizeof(cl_mem), &data1);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    error = clSetKernelArg(kernel, 1, sizeof(cl_mem), &data2);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    error = clSetKernelArg(kernel, 2, sizeof(cl_mem), &data3);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    error = clSetKernelArg(kernel, 3, sizeof(cl_mem), &data4);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    size_t global_work_size[] = {(size_t)(size / pieceSize / pieceSize)};
    size_t local_work_size[] = {1};
    error = clEnqueueNDRangeKernel(commandQueue, kernel, 1, 0, &global_work_size[0], &local_work_size[0], 0, 0, 0);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    error = clFinish(commandQueue);
    clEnqueueReadBuffer(commandQueue, data4, CL_TRUE, 0, len4, result, 0, 0, 0);
    delete[] settings;
    error = clReleaseMemObject(data1);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    error = clReleaseMemObject(data2);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    error = clReleaseMemObject(data3);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    error = clReleaseMemObject(data4);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    if (useNewImage) {
        delete[] image;
        int len1 = newSize2 * sizeof(char);
        int len2 = newSize1 * sizeof(char);
        int len3 = sizeof(int)*2;
        int* w = new int[2] {width, newWidth};
        result = new char[newSize1];
        cl_mem data1 = clCreateBuffer(context, CL_MEM_READ_WRITE, len1, 0, &error);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        cl_mem data2 = clCreateBuffer(context, CL_MEM_READ_WRITE, len2, 0, &error);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        cl_mem data3 = clCreateBuffer(context, CL_MEM_READ_WRITE, len3, 0, &error);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        clEnqueueWriteBuffer(commandQueue, data1, CL_TRUE, 0, len1, newResult, 0, 0, 0);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        clEnqueueWriteBuffer(commandQueue, data2, CL_TRUE, 0, len2, result, 0, 0, 0);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        clEnqueueWriteBuffer(commandQueue, data3, CL_TRUE, 0, len3, w, 0, 0, 0);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        error = clSetKernelArg(kernel__, 0, sizeof(cl_mem), &data1);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        error = clSetKernelArg(kernel__, 1, sizeof(cl_mem), &data2);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        error = clSetKernelArg(kernel__, 2, sizeof(cl_mem), &data3);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        size_t global_work_size[] = { (size_t)(newSize1)};
        size_t local_work_size[] = { 1 };
        error = clEnqueueNDRangeKernel(commandQueue, kernel__, 1, 0, &global_work_size[0], &local_work_size[0], 0, 0, 0);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        error = clFinish(commandQueue);
        clEnqueueReadBuffer(commandQueue, data2, CL_TRUE, 0, len2, result, 0, 0, 0);
        error = clReleaseMemObject(data1);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        error = clReleaseMemObject(data2);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        error = clReleaseMemObject(data3);
        if (error != CL_SUCCESS)
        {
            return false;
        }
        delete[] w;
        delete[] newResult;
    }
}
JNIEXPORT jboolean JNICALL Java_cn_mingbai_ScreenInMC_Natives_GPUDither_unInit(JNIEnv *, jclass)
{
    cl_int error = 0;
    delete[] palette;
    error = clReleaseProgram(program);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    error = clReleaseKernel(kernel);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    error = clReleaseCommandQueue(commandQueue);
    if (error != CL_SUCCESS)
    {
        return false;
    }
    error = clReleaseContext(context);
    if (error != CL_SUCCESS)
    {
        return false;
    }
}
bool ditherWithCPU(int* image, const int width, const int height, char* result){
    int i=0;
    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            unsigned char current_color_r,current_color_g,current_color_b,currect_color_a;
            intToRGBA(image[i] ,&current_color_r,& current_color_g,&current_color_b,&currect_color_a);
            if (currect_color_a != 255) {
                image[i] = 0;
                i++;
                continue;
            }
            unsigned char closest_match_r,closest_match_g,closest_match_b;
            int closest_match = getClosestMatch(current_color_r,current_color_g,current_color_b,&closest_match_r,&closest_match_g,&closest_match_b);
            int errorR = current_color_r - closest_match_r;
            int errorG = current_color_g - closest_match_g;
            int errorB = current_color_b - closest_match_b;
            image[i] = RGBAToInt(closest_match_r,closest_match_g,closest_match_b,255);
            int key = closest_match + 4;
            result[i] = (char) ((key / 4) << 2 | (key % 4) & 3);
            if (!(x == width - 1)) {
                int t = i + 1;
                unsigned char c1,c2,c3,c4;
                intToRGBA(image[t],&c1,&c2,&c3,&c4);
                if (c4 == 255) {
                    image[t] = RGBAToInt(colorClip((int) (c1 + errorR * 0.4375f)),
                            colorClip((int) (c2 + errorG * 0.4375f)),
                            colorClip((int) (c3 + errorB * 0.4375f)),255);
                }
                if (!(y == height - 1)) {
                    t = i + 1 + width;
                    intToRGBA(image[t],&c1,&c2,&c3,&c4);
                    if (c4 == 255) {
                        image[t]= RGBAToInt(colorClip((int) (c1 + errorR * 0.0625f)),
                                colorClip((int) (c2 + errorG * 0.0625f)),
                                colorClip((int) (c3 + errorB * 0.0625f)),255);
                    }
                }
            }
            if (!(y == height - 1)) {
                int t = i + width;
                unsigned char c1,c2,c3,c4;
                intToRGBA(image[t],&c1,&c2,&c3,&c4);
                if (c4 == 255) {
                    image[t] = RGBAToInt(colorClip((int) (c1 + errorR * 0.1875f)),
                            colorClip((int) (c2 + errorG * 0.1875f)),
                            colorClip((int) (c3) + errorB * 0.1875f),255);
                }
                if (!(x == 0)) {
                    t = i - 1 + width;
                    intToRGBA(image[t],&c1,&c2,&c3,&c4);
                    if (c4 == 255) {
                        image[t] = RGBAToInt(colorClip((int) (c1 + errorR * 0.3125f)),
                                colorClip((int) (c2 + errorG * 0.3125f)),
                                colorClip((int) (c3 + errorB * 0.3125f)),255);
                    }
                }
            }
            i++;
        }
    }
    return true;
}
int colorDistance(unsigned char c1r,unsigned char c1g,unsigned char c1b,unsigned char c2r,unsigned char c2g,unsigned char c2b){
    int rmean = ((int)c1b + (int)c2b) / 2;
    int r = (int)c1r - (int)c2r;
    int g = (int)c1g - (int)c2g;
    int b = (int)c1b - (int)c2b;
    return (int)fast_sqrt((((512 + rmean) * r * r) >> 8) + 4 * g * g + (((767 - rmean) * b * b) >> 8));
}
int getClosestMatch(unsigned char c1r,unsigned char c1g,unsigned char c1b,unsigned char *c2r,unsigned char *c2g,unsigned char *c2b){
    int minimum_index = 0;
    unsigned char pr,pg,pb;
    intToRGBA(palette[0],&pb,&pg,&pr,0);
    *c2r=pr;
    *c2g=pg;
    *c2b=pb;
    int minimum_difference = colorDistance(c1r,c1g,c1b, pr,pg,pb);
    for (int i = 1; i < paletteColorCount; i++) {
        intToRGBA(palette[i],&pr,&pg,&pb,0);
        int current_difference = colorDistance(c1r,c1g,c1b, pr,pg,pb);
        if (current_difference < minimum_difference) {
            minimum_difference = current_difference;
            minimum_index = i;
            *c2r=pr;
            *c2g=pg;
            *c2b=pb;
        }
    }
    return minimum_index;
}
void intToRGBA(int color,unsigned char *c1r,unsigned char *c1g,unsigned char *c1b,unsigned char *c1a){
    *c1r = (color >> 16) & 0xFF;
    *c1g = (color >> 8) & 0xFF;
    *c1b = color & 0xFF;
    if(c1a!=0){
        *c1a = (color >> 24) & 0xFF;
    }
}
int colorClip(int i){
    if (i > 255) {
        return 255;
    }
    if (i < 0) {
        return 0;
    }
    return i;
}
int RGBAToInt(unsigned char c1r,unsigned char c1g,unsigned char c1b,unsigned char c1a){
    return (((int)c1a & 0xFF) << 24) |
            (((int)c1r & 0xFF) << 16) |
            (((int)c1g & 0xFF) << 8) |
            (((int)c1b & 0xFF) << 0);
}
float fast_sqrt(float x)
{
    float xhalf = 0.5f * x;
    int i = *(int*)&x; // get bits for floating VALUE 
    i = 0x5f375a86 - (i >> 1); // gives initial guess y0
    x = *(float*)&i; // convert bits BACK to float
    x = x * (1.5f - xhalf * x * x); // Newton step, repeating increases accuracy
    x = x * (1.5f - xhalf * x * x); // Newton step, repeating increases accuracy
    x = x * (1.5f - xhalf * x * x); // Newton step, repeating increases accuracy

    return 1 / x;
}
int main(int a, const char *b[])
{
}
