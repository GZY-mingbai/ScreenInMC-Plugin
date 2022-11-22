#include "cn_mingbai_ScreenInMC_Natives_GPUDither.h"
#include <string.h>
//#include <iostream>


JNIEXPORT jobjectArray JNICALL Java_cn_mingbai_ScreenInMC_Natives_GPUDither_getPlatforms
(JNIEnv* env, jclass cls) {
	cl_uint count;
	char** names;
	getPlatformNames(&count, &names);
	jobjectArray result= env->NewObjectArray(count, env->FindClass("java/lang/String"), NULL);
	for (int i = 0; i < count; i++) {
		env->SetObjectArrayElement(result, i, env->NewStringUTF(names[i]));
	}
	return result;
}
JNIEXPORT jboolean JNICALL Java_cn_mingbai_ScreenInMC_Natives_GPUDither_init
(JNIEnv *env, jclass cls, jint id,jintArray palette_,jint paletteColorCount_) {
	usePlatform = id;
	jboolean jbool = false;
	paletteColorCount = paletteColorCount_;
	jint* palette__ = env->GetIntArrayElements(palette_, &jbool);
	if (palette__ == NULL) {
		return false;
	}
	palette = (int*)palette__;
	env->ReleaseIntArrayElements(palette_, palette__, 1);
	return init();
}
JNIEXPORT jbyteArray JNICALL Java_cn_mingbai_ScreenInMC_Natives_GPUDither_dither
  (JNIEnv *env, jclass cls, jintArray image, jint width, jint height){
	jboolean jbool = false;
    jint *image_ = env->GetIntArrayElements(image,&jbool);
    if (image_ == NULL) {
        return NULL;
    }
    int size = width * height;
	jbyte* result_ = new jbyte[size];
	dither((int*)image_, width, height, (char*)result_);
	jbyteArray result = env->NewByteArray(size);
	env->SetByteArrayRegion(result, 0, size, result_);
    env->ReleaseIntArrayElements(image, image_,0);
    return result;
  }
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
bool init() {
	const char* code = "int* intToRgba(int rgb) {\nint result[4];\nresult[0] = (rgb >> 16) & 0xff;\nresult[1] = (rgb >>  8) & 0xff;\nresult[2] = (rgb  ) & 0xff;\nresult[3] = (rgb >> 24) & 0xff;\nreturn result;\n}\nint rgbToInt(int r,int g,int b) {\nif(r>255) {\nr=255;\n\n}\nif(g>255) {\ng=255;\n}\nif(b>255) {\nb=255;\n}\nif(r<0) {\nr=0;\n}\nif(g<0) {\ng=0;\n}\nif(b<0) {\nb=0;\n}\nreturn 0xFF000000 | ((r << 16) & 0x00FF0000) | ((g << 8) & 0x0000FF00) | (b & 0x000000FF);\n}\nfloat colorDistance(int* c1, int* c2) {\nint rmean = (c1[0] + c2[0]) / 2;\nint r = c1[0] - c2[0];\nint g = c1[1] - c2[1];\nint b = c1[2] - c2[2];\nreturn sqrt((float)((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8)));\n}\nint* getNearlyColor(__global int *palette,int colorCount,int* rgb) {\nint min = colorDistance(intToRgba(palette[0]),rgb);\nint minIndex = 0;\nint* minColor;\nfor (int i=1;i<colorCount;i++) {\nint* tempColor = intToRgba(palette[i]);\nint temp = colorDistance(tempColor,rgb);\nif(temp<min) {\nmin = temp;\nminIndex = i;\nminColor=tempColor;\n}\n}\nint result[8];\nresult[0]=min;\nresult[1]=minIndex+4;\nresult[2]=minColor[0];\nresult[3]=minColor[1];\nresult[4]=minColor[2];\nresult[5]=rgb[0]-result[2];\nresult[6]=rgb[1]-result[3];\nresult[7]=rgb[2]-result[4];\nreturn result;\n}\n__kernel void dither(__global int *colors,__global int *palette,__global int *settings,__global char *result,__global int* temp) {\nint gid = get_global_id(0);\nint width = settings[0];\nint height = settings[1];\nint colorCount = settings[2];\nint size = width*height;\nwhile (temp[0]<gid)\n{\n}\nint* rgba = intToRgba(colors[gid]);\nif(rgba[3]!=255) {\nresult[gid]=0;\nreturn;\n}\nint* near = getNearlyColor(palette,colorCount,rgba);\ncolors[gid] = rgbToInt(near[2],near[3],near[4]);\nresult[gid] = (char)((near[1] / 4) << 2 | (near[1] % 4) & 3);\nint x = gid%width;\nint y = gid/width;\nif(!(x == width)-1) {\nint index = width*y+x+1;\nint* rgba_ = intToRgba(colors[index]);\nif(rgba_[3]==255) {\ncolors[index]=rgbToInt(rgba_[0]+near[5]*7/16,rgba_[1]+near[6]*7/16,rgba_[2]+near[7]*7/16);\n}\nif(!(y == height)-1) {\nint index_ = width*(y+1)+x+1;\nrgba_ = intToRgba(colors[index_]);\nif(rgba[3]==255) {\ncolors[index_]=rgbToInt(rgba_[0]+near[5]*1/16,rgba_[1]+near[6]*1/16,rgba_[2]+near[7]*1/16);\n}\n}\n}\nif(!(y == height)-1) {\nint index = width*(y+1)+x;\nint* rgba_ = intToRgba(colors[index]);\nif(rgba_[3]==255) {\ncolors[index]=rgbToInt(rgba_[0]+near[5]*7/16,rgba_[1]+near[6]*7/16,rgba_[2]+near[7]*3/16);\n}\nif(x != 0) {\nint index_ = width*(y+1)+x-1;\nrgba_ = intToRgba(colors[index_]);\nif(rgba[3]==255) {\ncolors[index_]=rgbToInt(rgba_[0]+near[5]*1/16,rgba_[1]+near[6]*1/16,rgba_[2]+near[7]*5/16);\n}\n}\n}\ntemp[0]++;\n}";
	return init(code);
}
bool init(const char* code) {
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
	program = clCreateProgramWithSource(context, 1, &code, &codeSize, &error);
	if (error != CL_SUCCESS) { return false; }
 	error = clBuildProgram(program, 1, devices, NULL, NULL, NULL);
	if (error != CL_SUCCESS) {
		size_t len;
		char buffer[8 * 1024];
		printf("Error: Failed to build program.\n");
		clGetProgramBuildInfo(program, *devices, CL_PROGRAM_BUILD_LOG, sizeof(buffer), buffer, &len);
		printf(buffer);
		return false;
	}
	kernel = clCreateKernel(program, "dither", &error);
 	if (error != CL_SUCCESS) {  return false; }
	commandQueue = clCreateCommandQueueWithProperties(context, devices[0], 0, &error);
 	if (error != CL_SUCCESS) {  return false; }
	delete[] devices;
	return true;
}
bool dither(const int* image, const int width, const int height, char* result) {
	cl_int error = 0;
	int size = width * height;
	int len1 = size * sizeof(int);
	int len2 = paletteColorCount * sizeof(int);
	int len3 = 3 * sizeof(int);
	int len4 = size * sizeof(char);
	int* settings = new int[3] {width,height,paletteColorCount};
	int* temp = new int[1];
	cl_mem data1 = clCreateBuffer(context, CL_MEM_READ_WRITE, len1, NULL, &error); if (error != CL_SUCCESS) { return false; }
	cl_mem data2 = clCreateBuffer(context, CL_MEM_READ_WRITE, len2, NULL, &error); if (error != CL_SUCCESS) { return false; }
	cl_mem data3 = clCreateBuffer(context, CL_MEM_READ_WRITE, len3, NULL, &error); if (error != CL_SUCCESS) { return false; }
	cl_mem data4 = clCreateBuffer(context, CL_MEM_READ_WRITE, len4, NULL, &error); if (error != CL_SUCCESS) { return false; }
	cl_mem data5 = clCreateBuffer(context, CL_MEM_READ_WRITE, sizeof(int), NULL, &error); if (error != CL_SUCCESS) { return false; }
	clEnqueueWriteBuffer(commandQueue, data1, CL_TRUE, 0, len1, image, 0, NULL, NULL); if (error != CL_SUCCESS) { return false; }
	clEnqueueWriteBuffer(commandQueue, data2, CL_TRUE, 0, len2, palette, 0, NULL, NULL); if (error != CL_SUCCESS) { return false; }
	clEnqueueWriteBuffer(commandQueue, data3, CL_TRUE, 0, len3, settings, 0, NULL, NULL); if (error != CL_SUCCESS) { return false; }
	clEnqueueWriteBuffer(commandQueue, data4, CL_TRUE, 0, len4, result, 0, NULL, NULL); if (error != CL_SUCCESS) { return false; }
	clEnqueueWriteBuffer(commandQueue, data5, CL_TRUE, 0, len4, temp, 0, NULL, NULL); if (error != CL_SUCCESS) { return false; }
	error = clSetKernelArg(kernel, 0, sizeof(cl_mem), &data1); if (error != CL_SUCCESS) { return false; }
	error = clSetKernelArg(kernel, 1, sizeof(cl_mem), &data2); if (error != CL_SUCCESS) { return false; }
	error = clSetKernelArg(kernel, 2, sizeof(cl_mem), &data3); if (error != CL_SUCCESS) { return false; }
	error = clSetKernelArg(kernel, 3, sizeof(cl_mem), &data4); if (error != CL_SUCCESS) { return false; }
	error = clSetKernelArg(kernel, 4, sizeof(cl_mem), &data5); if (error != CL_SUCCESS) { return false; }
	size_t global_work_size[] = { size };
	size_t local_work_size[] = { 1 };
	error = clEnqueueNDRangeKernel(commandQueue, kernel, 1, NULL, &global_work_size[0], &local_work_size[0], 0, NULL, NULL);
	if (error != CL_SUCCESS) { return false; }
	error = clFinish(commandQueue);
	clEnqueueReadBuffer(commandQueue, data4, CL_TRUE, 0, len4, result, 0, NULL, NULL);
	delete[] settings;
	delete[] temp;
	error = clReleaseMemObject(data1);  if (error != CL_SUCCESS) { return false; }
	error = clReleaseMemObject(data2);  if (error != CL_SUCCESS) { return false; }
	error = clReleaseMemObject(data3);  if (error != CL_SUCCESS) { return false; }
	error = clReleaseMemObject(data4);  if (error != CL_SUCCESS) { return false; }
	//error = clReleaseMemObject(data5);  if (error != CL_SUCCESS) { return false; }
}
JNIEXPORT jboolean JNICALL Java_cn_mingbai_ScreenInMC_Natives_GPUDither_unInit
(JNIEnv*, jclass) {
	cl_int error = 0;
	delete[] palette;
	error = clReleaseProgram(program); if (error != CL_SUCCESS) { return false; }
	error = clReleaseKernel(kernel);  if (error != CL_SUCCESS) { return false; }
	error = clReleaseCommandQueue(commandQueue); if (error != CL_SUCCESS) { return false; }
	error = clReleaseContext(context); if (error != CL_SUCCESS) { return false; }
}


int main(int a, const char* b[]) {
	GdiplusStartupInput gdiplusstartupinput;
	ULONG_PTR gdiplustoken;
	FILE* fp;
	fopen_s(&fp, "code.cl", "rb");
	fseek(fp, 0, SEEK_END);				// 定位到文档结尾
	size_t code_size = ftell(fp);		// 统计文档大小
	fseek(fp, 0, SEEK_SET);				// 重新定位到文档开头
	char* code = new char[code_size + 1];
	char c, * p = code;
	while ((c = fgetc(fp)) != EOF) {	// 读取文件内容
		*p++ = c;
	}
	*p = '\0';
	fclose(fp);
	GdiplusStartup(&gdiplustoken, &gdiplusstartupinput, NULL);
	usePlatform = 1;
	//init();
	init(code);
	palette = new int[9] {0,0,0,30,50,50,40,100,100};
	paletteColorCount = 3;
	Gdiplus::Color color;
	std::wstring infilename(L"test.png");
	Bitmap* bm_f = new Bitmap(infilename.c_str());
	int w = bm_f->GetWidth();
	int h = bm_f->GetHeight();
	int* image = new int[w * h];
	for (int y = 0; y < h; y++) {
		for (int x = 0; x < w; x++) {
			bm_f->GetPixel(x, y, &color);
			image[y * w + x] = color.GetValue();
		}
	}
	char* r = new char[w * h];
	dither(image, w, h, r);
	for (int i = 0; i < 16; i++) {
		for (int j = 0; j < 16; j++) {
			std::cout << (int)r[i * 16 + j]<<" ";
		}
		std::cout << std::endl;
	}
	printf("114514");
}
