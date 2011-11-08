/*
 * Header file for kerberosapp.c prototypes
 */

#include <jni.h>

#ifndef KRBAPP_H
#define KRBAPP_H
#ifdef __cplusplus
extern "C" {
#endif

#include <android/log.h>
#include <com_err.h>

extern JavaVM* cached_jvm;
extern jobject cached_obj;

#define MAX_APPEND_STRING_SZ    4096

#define  LOG_TAG    "---KERBEROS---"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

#define log(...)        androidPrint(__VA_ARGS__)
#define com_err(progname, code, ...)    androidError(progname, code, __VA_ARGS__)

extern void generateArgv(char*, int, char**);
extern void releaseArgv(int, char**);
extern JNIEnv* GetJNIEnv(JavaVM *jvm);
extern int kinit_driver(JNIEnv*, jobject, int, char**);
extern int klist_driver(JNIEnv*, jobject, int, char**);
extern int kvno_driver(JNIEnv*, jobject, int, char**);
extern int kdestroy_driver(JNIEnv*, jobject, int, char**);
extern void androidPrint(const char*, ...);
extern void androidError(const char*, errcode_t, const char*, ...);
extern int appendText(char*);

extern int klist_driver(JNIEnv*, jobject, int, char**);
#ifdef __cplusplus
}
#endif
#endif
