#include<jni.h>
#include<CL/cl.h>
#ifndef _Included_cn_mingbai_ScreenInMC_Natives_GPUDither
#define _Included_cn_mingbai_ScreenInMC_Natives_GPUDither
#ifdef __cplusplus
extern "C" {
#endif
JNIEXPORT jobjectArray JNICALL Java_cn_mingbai_ScreenInMC_Natives_GPUDither_getPlatforms
(JNIEnv*, jclass);

JNIEXPORT jboolean JNICALL Java_cn_mingbai_ScreenInMC_Natives_GPUDither_init
(JNIEnv*, jclass, jint, jintArray, jint);

JNIEXPORT jbyteArray JNICALL Java_cn_mingbai_ScreenInMC_Natives_GPUDither_dither
(JNIEnv*, jclass, jintArray, jint, jint,jint);

JNIEXPORT jboolean JNICALL Java_cn_mingbai_ScreenInMC_Natives_GPUDither_unInit
(JNIEnv*, jclass);
#ifdef __cplusplus
}
#endif
#endif
static int usePlatform = 0;
static cl_kernel kernel;
static cl_command_queue commandQueue;
static cl_context context;
static cl_program program;
static int paletteColorCount;
static int* palette;

bool getPlatforms(cl_uint* count, cl_platform_id** result);
bool getPlatformNames(cl_uint* count, char*** names);
bool init();
bool init(const char* code);
bool dither(const int* image, const int width, const int height, char* result,int pieceSize);