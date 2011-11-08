#include <stdio.h>
#include <stdlib.h>

#include "com_mit_kerberos_KerberosAppActivity.h"
#include "kerberosapp.h"

/* Global JNI Variables */
JavaVM* cached_jvm;
jobject cached_obj;

/*
 * Generate NULL-terminated argv array from a string.
 * Note: argv begins with command name, and is
 *       NULL terminated (thus the +2)
 */
void generateArgv(char* input, int argc, char** argv)
{
    int i;
    char* tmp;

    LOGI("Entered generateArgv");
    for(i = 0; i < argc+2; i++) {
        if(i == 0) {                /* add command name */
            argv[i] = (char*)malloc(5*sizeof(char*));
            strcpy(argv[i], "kinit");
        }
        else if (i == argc+1)    /* add NULL termination */
            argv[i] = NULL;
        else if (i == 1) {
            tmp = strtok(input, " ");
            argv[i] = (char*)malloc((strlen(tmp)+1)*sizeof(char*));
            strcpy(argv[i], tmp);
        }
        else {
            tmp = strtok(NULL, " ");
            argv[i] = (char*)malloc((strlen(tmp)+1)*sizeof(char*));
            strcpy(argv[i], tmp);
        }
    }
    return;
}

/*
 * Free argv array.
 */
void releaseArgv(int argc, char** argv)
{
    int i;
    LOGI("Entered releaseArgv");

    for (i = 0; i < argc; i++){
        LOGI("releaseArgv... trying to free argv[%d] = %s", i, argv[i]);
        LOGI("releaseArgv... argv[%d] = %p", i, &argv[i]);
        free(argv[i]);
        LOGI("releaseArgv... freed argv[%d]", i);
    }

    LOGI("Freed argv elements");
    free(argv);
    LOGI("Freed argv itself");
}

/*
 * Get the current JNIEnv pointer from global JavaVM.
 */
JNIEnv* GetJNIEnv(JavaVM *jvm)
{
    JNIEnv *env;
    int status;

    status = (*jvm)->GetEnv(jvm, (void **) &env, JNI_VERSION_1_6);
    if (status < 0) {
        LOGI("Unable to get JNIEnv pointer from JavaVM");
        return NULL;
    }

    return env;
}

/*
 * Is called automatically when library is loaded.
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved)
{
    JNIEnv* env;
    
    LOGI("Loaded libkerberosapp, entered JNI_OnLoad()");

    if ((*jvm)->GetEnv(jvm, (void**) &env, JNI_VERSION_1_6) != JNI_OK)
        return -1;

    /* Cache our JavaVM pointer */
    cached_jvm = jvm;

    return JNI_VERSION_1_6;
}

/*
 * Is called automatically when library is unloaded.
 */
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *jvm, void *reserved)
{
    JNIEnv *env;

    if( (env = GetJNIEnv(jvm)) == NULL) {
        return;
    }

    (*env)->DeleteGlobalRef(env, cached_obj);
    return;

}

/*
 * Class:     com_mit_kerberos_KerberosAppActivity
 * Method:    nativeKinit
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_mit_kerberos_KerberosAppActivity_nativeKinit
  (JNIEnv* env, jobject obj, jstring argString, jint argCount)
{
    jboolean isCopy;
    int ret;
    int numArgs = (int) argCount;
    const char *args;
    char *args_copy;
    char **argv = (char**)malloc((numArgs+2) * sizeof(char*));

    LOGI("Entered nativeKinit function");

    /* Cache a reference to the calling object */
    cached_obj = (*env)->NewGlobalRef(env, obj);

    /* get original argv string from Java */
    args = (*env)->GetStringUTFChars(env, argString, &isCopy);
    LOGI("String from JAVA is: %s", args);

    /* make a copy of argString to use with strtok */
    args_copy = malloc(strlen(args) + 1);
    strcpy(args_copy, args);

    /* free argString */
    (*env)->ReleaseStringUTFChars(env, argString, args);
    LOGI("Freed argString");

    /* generate argv list */ 
    generateArgv(args_copy, numArgs, argv);

    /* run kinit */
    ret = kinit_driver(env, obj, numArgs+1, argv); 
    LOGI("Returned back to nativeKinit function");
   
    free(args_copy);
    LOGI("Freed args_copy");
   
    releaseArgv(numArgs+1, argv);
    LOGI("Freed argv");
    
    if(ret == 1)
        return 1;
    return 0;
}

/*
 * Class:     com_mit_kerberos_KerberosAppActivity
 * Method:    nativeKlist
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_mit_kerberos_KerberosAppActivity_nativeKlist
  (JNIEnv* env, jobject obj, jstring argString, jint argCount)
{
    jboolean isCopy;
    int ret;
    int numArgs = (int) argCount;
    const char *args;
    char *args_copy;
    char **argv = (char**)malloc((numArgs+2) * sizeof(char*));

    LOGI("Entered nativeKlist function");

    /* Cache a reference to the calling object */
    cached_obj = (*env)->NewGlobalRef(env, obj);

    /* get original argv string from Java */
    args = (*env)->GetStringUTFChars(env, argString, &isCopy);
    LOGI("String from JAVA is: %s", args);

    /* make a copy of argString to use with strtok */
    args_copy = malloc(strlen(args) + 1);
    strcpy(args_copy, args);

    /* free argString */
    (*env)->ReleaseStringUTFChars(env, argString, args);
    LOGI("Freed argString");

    /* generate argv list */ 
    generateArgv(args_copy, numArgs, argv);

    /* run kinit */
    ret = klist_driver(env, obj, numArgs+1, argv); 
    LOGI("Returned back to nativeKlist function");
   
    free(args_copy);
    LOGI("Freed args_copy");
   
    releaseArgv(numArgs+1, argv);
    LOGI("Freed argv");
    
    if(ret == 1)
        return 1;
    return 0;
}

/*
 * Class:     com_mit_kerberos_KerberosAppActivity
 * Method:    nativeKvno
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_mit_kerberos_KerberosAppActivity_nativeKvno
  (JNIEnv* env, jobject obj, jstring argString, jint argCount)
{
    jboolean isCopy;
    int ret;
    int numArgs = (int) argCount;
    const char *args;
    char *args_copy;
    char **argv = (char**)malloc((numArgs+2) * sizeof(char*));

    LOGI("Entered nativeKvno function");

    /* Cache a reference to the calling object */
    cached_obj = (*env)->NewGlobalRef(env, obj);

    /* get original argv string from Java */
    args = (*env)->GetStringUTFChars(env, argString, &isCopy);
    LOGI("String from JAVA is: %s", args);

    /* make a copy of argString to use with strtok */
    args_copy = malloc(strlen(args) + 1);
    strcpy(args_copy, args);

    /* free argString */
    (*env)->ReleaseStringUTFChars(env, argString, args);
    LOGI("Freed argString");

    /* generate argv list */ 
    generateArgv(args_copy, numArgs, argv);

    /* run kinit */
    ret = kvno_driver(env, obj, numArgs+1, argv); 
    LOGI("Returned back to nativeKlist function");
   
    free(args_copy);
    LOGI("Freed args_copy");
   
    releaseArgv(numArgs+1, argv);
    LOGI("Freed argv");
    
    if(ret == 1)
        return 1;
    return 0;
}

/*
 * Class:     com_mit_kerberos_KerberosAppActivity
 * Method:    nativeKdestroy
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_mit_kerberos_KerberosAppActivity_nativeKdestroy
  (JNIEnv* env, jobject obj, jstring argString, jint argCount)
{
    jboolean isCopy;
    int ret;
    int numArgs = (int) argCount;
    const char *args;
    char *args_copy;
    char **argv = (char**)malloc((numArgs+2) * sizeof(char*));

    LOGI("Entered nativeKdestroy function");

    /* Cache a reference to the calling object */
    cached_obj = (*env)->NewGlobalRef(env, obj);

    /* get original argv string from Java */
    args = (*env)->GetStringUTFChars(env, argString, &isCopy);
    LOGI("String from JAVA is: %s", args);

    /* make a copy of argString to use with strtok */
    args_copy = malloc(strlen(args) + 1);
    strcpy(args_copy, args);

    /* free argString */
    (*env)->ReleaseStringUTFChars(env, argString, args);
    LOGI("Freed argString");

    /* generate argv list */ 
    generateArgv(args_copy, numArgs, argv);

    /* run kdestroy */
    ret = kdestroy_driver(env, obj, numArgs+1, argv); 
    LOGI("Returned back to nativeKdestroy function");
   
    free(args_copy);
    LOGI("Freed args_copy");
   
    releaseArgv(numArgs+1, argv);
    LOGI("Freed argv");
    
    if(ret == 1)
        return 1;
    return 0;
}

/*
 * Android log function, printf-style.
 * Logs input string to Android log as well as
 * GUI TextView.
 */
void androidPrint(const char* format, ...) 
{
    va_list args;
    char appendString[4096];

    LOGI("Entered androidPrint...");
    va_start(args, format);
    LOGI("androidPrint.... A");
    //LOGI(format, args);
    vsnprintf(appendString, sizeof(appendString), format, args);
    LOGI("androidPrint.... B");
    appendText(appendString);
    va_end(args);
}

/*
 * Android error log function, replaces com_err calls
 */
void androidError(const char* progname, errcode_t code, const char* format, ...)
{
    va_list args;
    char errString[4096] = "Error ";

    va_start(args, format);
    vsnprintf(errString+6, sizeof(errString), format, args);
    strcat(errString, "\n");
    appendText(errString);
    va_end(args); 
}

/*
 * Appends text to Java TextView.
 *
 * Note: Set jni_env, class_obj before calling.
 * Return: 0 (success), 1 (failure)
 */
int appendText(char* input)
{
    JNIEnv* env;
    jclass cls;         /* com.mit.kerberos.KerberosApp */
    jmethodID mid;      /* com.mit.kerberos.KerberosApp.appendText() */
    jstring javaOutput; /* text to append */
  
    LOGI("Entered appendText..."); 
    env = GetJNIEnv(cached_jvm);
    cls = (*env)->GetObjectClass(env, cached_obj);
    mid = (*env)->GetMethodID(env, cls, "appendText", "(Ljava/lang/String;)V");
    if (mid == 0)
    {
        LOGI("Unable to find Java appendText method");
        return 1;
    }
    else {
        javaOutput = (*env)->NewStringUTF(env, input);
        if (env == NULL || cached_obj == NULL || mid == NULL || javaOutput == NULL)
        {
            LOGI("We have a null variable in native code");
            return 1;
        }
        (*env)->CallVoidMethod(env, cached_obj, mid, javaOutput);
    }
    return 0;
}

