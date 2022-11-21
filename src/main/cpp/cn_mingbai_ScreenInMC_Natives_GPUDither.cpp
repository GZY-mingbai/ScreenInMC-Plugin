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
	const char* code = "float hslDistance(float* h1, float* h2) {\nconst float pi=3.1415926;\nconst float r=0.999958243;\nconst float h=0.00913839539;\nfloat x1 = r * h1[2] * h1[1] * cos(h1[0] / 2 * pi);\nfloat y1 = r * h1[2] * h1[1] * sin(h1[0] / 2 * pi);\nfloat z1 = h * (1 - h1[2]);\nfloat x2 = r * h2[2] * h2[1] * cos(h2[0] / 2 * pi);\nfloat y2 = r * h2[2] * h2[1] * sin(h2[0] / 2 * pi);\nfloat z2 = h * (1 - h2[2]);\nfloat dx = x1 - x2;\nfloat dy = y1 - y2;\nfloat dz = z1 - z2;\nreturn sqrt(dx * dx + dy * dy + dz * dz);\n}\nfloat* rgbToHsl(int* rgb)\n{\nfloat H,S,L;\nfloat r = rgb[0];\nfloat g = rgb[1];\nfloat b = rgb[2];\nr = r / 255.0;\ng = g / 255.0;\nb = b / 255.0;\nfloat M = max(max(r, g), b);\nfloat m = min(min(r, g), b);\nfloat d = M - m;\nif (d == 0) H = 0;\nelse if (M == r)\n{\nH = ((g - b) / d);\nH = H + ((int)H % 6);\n}\nelse if (M == g) H = (b - r) / d + 2;\nelse H = (r - g) / d + 4;\nH *= 60;\nif (H < 0) H += 360;\nL = (M + m) / 2;\nif (d == 0)\nS = 0;\nelse\nS = d / (1 - fabs(2 * L - 1));\nH/=360.0;\nfloat hsl[3]={H,S,L};\nreturn hsl;\n}\nint* intToRgb(int rgb) {\nint result[3];\nresult[0] = (rgb >> 16) & 0xff;\nresult[1] = (rgb >>  8) & 0xff;\nresult[2] = (rgb  ) & 0xff;\nreturn result;\n}\nint* intToRgba(int rgb) {\nint result[4];\nresult[0] = (rgb >> 16) & 0xff;\nresult[1] = (rgb >>  8) & 0xff;\nresult[2] = (rgb) & 0xff;\nresult[3] = (rgb >> 24) & 0xff;\nreturn result;\n}\nfloat* closestColors(float *hsl,__global int *colors,int colorCount) {\nfloat closest[4] = { -2, 0, 0 ,-4};\nfloat secondClosest[4] = {-2, 0, 0, -4};\nfloat temp[4];\nfor (int i = 0; i < colorCount; ++i) {\ntemp[0]=((float)colors[i*3])/360.0;\ntemp[1]=((float)colors[i*3+1])/255.0;\ntemp[2]=((float)colors[i*3+2])/255.0;\ntemp[3]=i;\nfloat tempDistance = hslDistance(temp, hsl);\nif (tempDistance < hslDistance(closest, hsl)) {\nsecondClosest[0] = closest[0];\nsecondClosest[1] = closest[1];\nsecondClosest[2] = closest[2];\nsecondClosest[3] = closest[3];\nclosest[0] = temp[0];\nclosest[1] = temp[1];\nclosest[2] = temp[2];\nclosest[3] = temp[3];\n}\nelse {\nif (tempDistance < hslDistance(secondClosest, hsl)) {\nsecondClosest[0] = temp[0];\nsecondClosest[1] = temp[1];\nsecondClosest[2] = temp[2];\nsecondClosest[3] = temp[3];\n}\n}\n}\nfloat ret[8];\nret[0] = closest[0];\nret[1] = closest[1];\nret[2] = closest[2];\nret[3] = secondClosest[0];\nret[4] = secondClosest[1];\nret[5] = secondClosest[2];\nret[6] = closest[3];\nret[7] = secondClosest[3];\nreturn ret;\n\n}\nfloat indexValue(int width,int index) {\nint y = index / width;\nint x = index % width;\nint indexMatrix[64] = {0,  32, 8,  40, 2,  34, 10, 42,\n48, 16, 56, 24, 50, 18, 58, 26,\n12, 44, 4,  36, 14, 46, 6,  38,\n60, 28, 52, 20, 62, 30, 54, 22,\n3,  35, 11, 43, 1,  33, 9,  41,\n51, 19, 59, 27, 49, 17, 57, 25,\n15, 47, 7,  39, 13, 45, 5,  37,\n63, 31, 55, 23, 61, 29, 53, 21};\nreturn indexMatrix[(x%8) + (y%8) * 8] / 64.0;\n}\n__kernel void dither(__global int *image,__global int *palette,__global int *settings,__global char *result) {\nint colorCount = settings[2];\nint id = get_global_id(0);\nint* rgba = intToRgba(image[id]);\nif(rgba[3]!=255){\nresult[id]=0;\nreturn;\n}\nint rgb[3] = {rgba[0],rgba[1],rgba[2]};\nfloat* hsl = rgbToHsl(rgb);\nfloat* cs = closestColors(hsl,palette,colorCount);\nfloat c1[4] = {cs[0],cs[1],cs[2],cs[6]+4};\nfloat c2[4] = {cs[3],cs[4],cs[5],cs[7]+4};\nfloat d = indexValue(settings[0],id);\nfloat hslDiff = hslDistance(hsl, c1) / hslDistance(c2, c1);\nresult[id] = (char) (hslDiff < d ? (int)c1[3] : (int)c2[3]);\n}";
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
	int len2 = paletteColorCount *3* sizeof(int);
	int len3 = 3 * sizeof(int);
	int len4 = size * sizeof(char);
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
	error = clReleaseMemObject(data1);  if (error != CL_SUCCESS) { return false; }
	error = clReleaseMemObject(data2);  if (error != CL_SUCCESS) { return false; }
	error = clReleaseMemObject(data3);  if (error != CL_SUCCESS) { return false; }
	error = clReleaseMemObject(data4);  if (error != CL_SUCCESS) { return false; }
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
	//GdiplusStartupInput gdiplusstartupinput;
	//ULONG_PTR gdiplustoken;
	//FILE* fp;
	//fopen_s(&fp, "code.cl", "rb");
	//fseek(fp, 0, SEEK_END);				// 定位到文档结尾
	//size_t code_size = ftell(fp);		// 统计文档大小
	//fseek(fp, 0, SEEK_SET);				// 重新定位到文档开头
	//char* code = new char[code_size + 1];
	//char c, * p = code;
	//while ((c = fgetc(fp)) != EOF) {	// 读取文件内容
	//	*p++ = c;
	//}
	//*p = '\0';
	//fclose(fp);
	//GdiplusStartup(&gdiplustoken, &gdiplusstartupinput, NULL);
	//usePlatform = 1;
	//init(code);
	////init();
	//palette = new int[9] {0,0,0,30,50,50,40,100,100};
	//paletteColorCount = 3;
	//Gdiplus::Color color;
	//std::wstring infilename(L"test.png");
	//Bitmap* bm_f = new Bitmap(infilename.c_str());
	//int w = bm_f->GetWidth();
	//int h = bm_f->GetHeight();
	//int* image = new int[w * h];
	//for (int y = 0; y < h; y++) {
	//	for (int x = 0; x < w; x++) {
	//		bm_f->GetPixel(x, y, &color);
	//		image[y * w + x] = color.GetValue();
	//	}
	//}
	//char* r = new char[w * h];
	//dither(image, w, h, r);

	//for (int i = 0; i < 16; i++) {
	//	for (int j = 0; j < 16; j++) {
	//		std::cout << (int)r[i * 16 + j]<<" ";
	//	}
	//	std::cout << std::endl;
	//}
	//printf("114514");
}
