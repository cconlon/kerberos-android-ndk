/* kerberosapp.h
 *
 * Copyright (C) 2006-2011 Sawtooth Consulting Ltd.
 *
 * This package is free software; you can redistribute it 
 * and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 *
 * This package is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
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
