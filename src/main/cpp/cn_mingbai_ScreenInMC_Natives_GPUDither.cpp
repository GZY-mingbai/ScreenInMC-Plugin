#include "cn_mingbai_ScreenInMC_Natives_GPUDither.h"
#include <string.h>
JNIEXPORT jbyteArray JNICALL Java_cn_mingbai_ScreenInMC_Natives_GPUDither_dither
  (JNIEnv *env, jclass cls, jintArray image, jintArray palette, jint width, jint height){
    jint *image_ = env->GetIntArrayElements(image,false);
    if (image_ == NULL) {
        return NULL;
    }
    jint* palette_ = env->GetIntArrayElements(image, false);
    if (palette_ == NULL) {
        return NULL;
    }
    int size = width * height;
    env->ReleaseIntArrayElements(image, image_,0);
    env->ReleaseIntArrayElements(palette, palette_, 0);
    jbyteArray result = env->NewByteArray(size);
    jbyte* result_ = env->GetByteArrayElements(result);
    return NULL;
  }
static const char* code = "";
static int usePlatform = 1;
bool getPlatforms(cl_uint* count,cl_platform_id** result) {
	cl_int error = 0;
	cl_uint platformsCount;
	error = clGetPlatformIDs(0, 0, &platformsCount);
	if (error != CL_SUCCESS) { return false; }
	cl_platform_id* platforms = new cl_platform_id[platformsCount];
	error = clGetPlatformIDs(platformsCount, platforms, 0);
	if (error != CL_SUCCESS) { return false; }
	*result = platforms;
	*count = platformsCount;
	return true;
}
bool getPlatformNames(cl_uint* count, char*** names) {
	cl_int error = 0;
	cl_uint platformsCount;
	cl_platform_id* platforms;
	if (!getPlatforms(&platformsCount, &platforms)) { return false; };
	char** platformNames = new char*[platformsCount];
	for (cl_uint i = 0; i < platformsCount; i++)
	{
		size_t platformNameLength = 0;
		error = clGetPlatformInfo(platforms[i], CL_PLATFORM_NAME, 0, 0, &platformNameLength);
		if (error != CL_SUCCESS) { return false; }
		platformNames[i] = new char[platformNameLength];
		error = clGetPlatformInfo(platforms[i], CL_PLATFORM_NAME, platformNameLength, platformNames[i], 0);
		if (error != CL_SUCCESS) { return false; }
	}
	delete[] platforms;
	*names = platformNames;
	*count = platformsCount;
	return true;
}
static cl_kernel kernel;
static cl_command_queue commandQueue;
static cl_context context;

bool init() {
	cl_int error = 0;
	cl_uint platformsCount;
	cl_platform_id* platforms;
	if (!getPlatforms(&platformsCount, &platforms)) { return false; };
	if (usePlatform > (platformsCount - 1)) {
		return false;
	}
	cl_platform_id platform = platforms[usePlatform];
	delete[] platforms;
	cl_context_properties contextProperties[3] = { CL_CONTEXT_PLATFORM, (cl_context_properties)platform, 0 };
	context = clCreateContextFromType(contextProperties, CL_DEVICE_TYPE_GPU, NULL, NULL, &error);
	if (error != CL_SUCCESS) { return false; }
	size_t devicesSize = 0;
	error = clGetContextInfo(context, CL_CONTEXT_DEVICES, 0, NULL, &devicesSize);
	if (error != CL_SUCCESS) { return false; }
	cl_device_id* devices = new cl_device_id[devicesSize];
	error = clGetContextInfo(context, CL_CONTEXT_DEVICES, devicesSize, devices, NULL);
	if (error != CL_SUCCESS) { return false; }
	size_t codeSize = strlen(code);
	cl_program program = clCreateProgramWithSource(context, 1, &code, &codeSize, &error);
	if (error != CL_SUCCESS) { return false; }
 	error = clBuildProgram(program, 1, devices, NULL, NULL, NULL);
	if (error != CL_SUCCESS) {return false;}
	kernel = clCreateKernel(program, "dither", &error);
	if (error != CL_SUCCESS) {  return false; }
	commandQueue = clCreateCommandQueueWithProperties(context, devices[0], 0, &error);
 	if (error != CL_SUCCESS) {  return false; }
	return false;
}
bool dither(const int* image, const int* palette,int paletteColorCount, const int width, const int height, short* result) {
	cl_int error = 0;
	int size = width * height;
	int len1 = size * sizeof(int);
	int len2 = paletteColorCount * sizeof(int);
	int len3 = 3 * sizeof(int);
	int len4 = size * sizeof(short);
	int* settings = new int[3] {width,height,paletteColorCount};
	cl_mem data1 = clCreateBuffer(context, CL_MEM_READ_WRITE, len1, NULL, &error); if (error != CL_SUCCESS) { return false; }
	cl_mem data2 = clCreateBuffer(context, CL_MEM_READ_WRITE, len2, NULL, &error); if (error != CL_SUCCESS) { return false; }
	cl_mem data3 = clCreateBuffer(context, CL_MEM_READ_WRITE, len3, NULL, &error); if (error != CL_SUCCESS) { return false; }
	cl_mem data4 = clCreateBuffer(context, CL_MEM_READ_WRITE, len4, NULL, &error); if (error != CL_SUCCESS) { return false; }
	clEnqueueWriteBuffer(commandQueue, data1, CL_TRUE, 0, len1, image, 0, NULL, NULL); if (error != CL_SUCCESS) { return false; }
	clEnqueueWriteBuffer(commandQueue, data2, CL_TRUE, 0, len2, palette, 0, NULL, NULL); if (error != CL_SUCCESS) { return false; }
	clEnqueueWriteBuffer(commandQueue, data3, CL_TRUE, 0, len3, settings, 0, NULL, NULL); if (error != CL_SUCCESS) { return false; }
	clEnqueueWriteBuffer(commandQueue, data4, CL_TRUE, 0, len4, result, 0, NULL, NULL); if (error != CL_SUCCESS) { return false; }
	error = clSetKernelArg(kernel, 0, sizeof(cl_mem), &data1); if (error != CL_SUCCESS) { return false; }
	error = clSetKernelArg(kernel, 1, sizeof(cl_mem), &data2); if (error != CL_SUCCESS) { return false; }
	error = clSetKernelArg(kernel, 2, sizeof(cl_mem), &data3); if (error != CL_SUCCESS) { return false; }
	error = clSetKernelArg(kernel, 3, sizeof(cl_mem), &data4); if (error != CL_SUCCESS) { return false; }
	size_t global_work_size[] = { size };
	size_t local_work_size[] = { 1 };
	error = clEnqueueNDRangeKernel(commandQueue, kernel, 1, NULL, &global_work_size[0], &local_work_size[0], 0, NULL, NULL);
	if (error != CL_SUCCESS) { return false; }
	error = clFinish(commandQueue);
	clEnqueueReadBuffer(commandQueue, data4, CL_TRUE, 0, len4, result, 0, NULL, NULL);
	delete[] settings;
}


int main(int a,const char* b[]){
	code = "int* intToRgba(int rgb) {int result[4];result[0] = (rgb >> 16) & 0xff;result[1] = (rgb >>  8) & 0xff;result[2] = (rgb  ) & 0xff;result[3] = (rgb >> 24) & 0xff;return result;}int rgbToInt(int r,int g,int b) {if(r>255) {r=255;}if(g>255) {g=255;}if(b>255) {b=255;}if(r<0) {r=0;}if(g<0) {g=0;}if(b<0) {b=0;}return 0xFF000000 | ((r << 16) & 0x00FF0000) | ((g << 8) & 0x0000FF00) | (b & 0x000000FF);}int* getNearlyColor(__global int *palette,int colorCount,int r,int g,int b) {int* color = intToRgba(palette[0]);int cr=r-color[0];int cg=g-color[1];int cb=b-color[2];int min = abs(cr)+abs(cg)+abs(cb);int minIndex = 0;int mr = color[0];int mg = color[1];int mb = color[2];for (int i=1;i<colorCount;i++) {color = intToRgba(palette[i]);int pr = color[0];int pg = color[1];int pb = color[2];int temp = abs(r-pr)+abs(g-pg)+abs(b-pb);if(temp<min) {min = temp;minIndex = i;mr = pr;mg = pg;mb = pb;cr = r-mr;cg = r-mg;cb = r-mb;}}int result[8];result[0]=min;result[1]=minIndex;result[2]=mr;result[3]=mg;result[4]=mb;result[5]=cr;result[6]=cg;result[7]=cb;return result;}__kernel void dither(__global int *colors,__global int *palette,__global *settings,__global int *result) {int gid = get_global_id(0);int width = settings[0];int height = settings[1];int colorCount = settings[2];int size = width*height;int* rgba = intToRgba(colors[gid]);if(rgba[3]!=255) {result[gid]=0;return;}int* near = getNearlyColor(palette,colorCount,rgba[0],rgba[1],rgba[2]);colors[gid] = rgbToInt(near[2],near[3],near[4]);result[gid] = near[1];int x = gid%width;int y = gid/width;if(!(x == width)-1) {int index = width*y+x+1;int* rgba_ = intToRgba(colors[index]);if(rgba_[3]==255) {colors[index]=rgbToInt(rgba_[0]+near[5]*7/16,rgba_[1]+near[6]*7/16,rgba_[2]+near[7]*7/16);}if(!(y == height)-1) {int index_ = width*(y+1)+x+1;int* rgba__ = intToRgba(colors[index_]);if(rgba[3]==255) {colors[index_]=rgbToInt(rgba__[0]+near[5]*1/16,rgba__[1]+near[6]*1/16,rgba__[2]+near[7]*1/16);}}}if(!(y == height)-1) {int index = width*(y+1)+x;int* rgba_ = intToRgba(colors[index]);if(rgba_[3]==255) {colors[index]=rgbToInt(rgba_[0]+near[5]*7/16,rgba_[1]+near[6]*7/16,rgba_[2]+near[7]*3/16);}if(x != 0) {int index_ = width*(y+1)+x-1;int* rgba__ = intToRgba(colors[index_]);if(rgba[3]==255) {colors[index_]=rgbToInt(rgba__[0]+near[5]*1/16,rgba__[1]+near[6]*1/16,rgba__[2]+near[7]*5/16);}}}}";
	init();
}