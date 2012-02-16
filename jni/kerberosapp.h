/*
 * kerberosapp.h
 *
 * Copyright (C) 2012 by the Massachusetts Institute of Technology.
 * All rights reserved.
 *
 * Export of this software from the United States of America may
 *   require a specific license from the United States Government.
 *   It is the responsibility of any person or organization contemplating
 *   export to obtain such a license before exporting.
 *
 * WITHIN THAT CONSTRAINT, permission to use, copy, modify, and
 * distribute this software and its documentation for any purpose and
 * without fee is hereby granted, provided that the above copyright
 * notice appear in all copies and that both that copyright notice and
 * this permission notice appear in supporting documentation, and that
 * the name of M.I.T. not be used in advertising or publicity pertaining
 * to distribution of the software without specific, written prior
 * permission.  Furthermore if you modify this software you must label
 * your software as modified software and not distribute it in such a
 * fashion that it might be confused with the original M.I.T. software.
 * M.I.T. makes no representations about the suitability of
 * this software for any purpose.  It is provided "as is" without express
 * or implied warranty.
 *
 * Original source developed by yaSSL (http://www.yassl.com)
 *
 * Header file for kerberosapp.c prototypes
 *
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

extern JNIEnv* GetJNIEnv(JavaVM *jvm);
extern void generateArgv(char*, int, char**);
extern void releaseArgv(int, char**);

extern void androidPrint(const char*, ...);
extern void androidError(const char*, errcode_t, const char*, ...);
extern int appendText(char*);

extern int kinit_driver(JNIEnv*, jobject, int, char**);
extern int klist_driver(JNIEnv*, jobject, int, char**);
extern int kvno_driver(JNIEnv*, jobject, int, char**);
extern int kdestroy_driver(JNIEnv*, jobject, int, char**);

#ifdef __cplusplus
}
#endif
#endif
