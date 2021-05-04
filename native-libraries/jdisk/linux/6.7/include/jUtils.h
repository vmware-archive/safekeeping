/* **************************************************************************
 * Copyright 2008 VMware, Inc.  All rights reserved. -- VMware Confidential
 * **************************************************************************/

/*
 *  jUtils.c
 *  
 *    Some helper functions for implementing JNI bindings.
 */

#ifndef _JUTILS_H_
#define _JUTILS_H_

/*
 * Some macros to help JDK 1.6 to gracefully deal with null pointers being
 * passed in for strings.
 */
#define GETSTRING(JSTR) \
   (((JSTR) == NULL) ? NULL : (*env)->GetStringUTFChars(env, (JSTR), 0))
#define FREESTRING(CSTR,JSTR) \
   (((CSTR) != NULL) ? (*env)->ReleaseStringUTFChars(env, (JSTR), (CSTR)) : 0)

/*
 * Read/write Java fields
 */
jlong JUtils_GetLongField(JNIEnv *env, jobject obj, const char *name);
void JUtils_SetLongField(JNIEnv *env, jobject obj, const char *name,
                         jlong value);

jint JUtils_GetIntField(JNIEnv *env, jobject obj, const char *name);
void JUtils_SetIntField(JNIEnv *env, jobject obj, const char *name,
                        jint value);
void JUtils_SetBoolField(JNIEnv *env, jobject obj, const char *name,
                        jboolean value);
char *JUtils_GetStringField(JNIEnv *env, jobject obj, const char *name);
void JUtils_SetStringField(JNIEnv *env, jobject obj, const char *name,
                           const char *value);
void JUtils_SetStringArray(JNIEnv *env, jobject obj, const char *name,
                           jobject value);

jobject JUtils_GetObjectField(JNIEnv *env, jobject obj, const char *name,
                              const char *typeId);
void JUtils_SetObjectField(JNIEnv *env, jobject obj, const char *name,
                           const char *typeId, jobject value);

/*
 * Convert an array of strings into a Java string array.
 */
jobject JUtils_MakeStringArray(JNIEnv *env, int length,  const char **values);

/*
 * Forward declarator for callback structure
 */
typedef struct jUtilsAsyncCallback jUtilsAsyncCallback;

/*
 * Completion callback for async operation
 */
void jUtilsCompletionCB(jUtilsAsyncCallback *callbackInfo,
                        jlong result);

/*
 * Init the async callback info for async writes.
 */
jUtilsAsyncCallback *jUtils_CreateAsyncCallback(JNIEnv *env,
                                                jobject callbackObj);

/*
 * Cleanup the async callback for async writes.
 */
void jUtils_ReleaseAsyncCallback(jUtilsAsyncCallback *callbackInfo);


/*
 *
 * Handling of callbacks into Java for progress updates and logging
 *
 */
typedef enum {
   MsgLog = 0,
   MsgWarn = 1,
   MsgPanic = 2,
} JUtilsLogLevel;

typedef struct JUtilsLogger JUtilsLogger;

JUtilsLogger *JUtils_InitLogging(JNIEnv *env, jobject logger);
void JUtils_LogMsg(JUtilsLogger *logger, JUtilsLogLevel level, const char *fmt,
                   va_list args);
Bool JUtils_LogProgress(JUtilsLogger *logger, void *progressData,
                        int percentCompleted);
void JUtils_ExitLogging(JUtilsLogger *env);

/*
 * Macros to automatically declare static functions that are suitable for
 * passing down into VIX libraries, yet use a specified logger object to
 * do all the logging.
 *
 * Just put "DECLARE_LOG_FUNCS(myGlobalLogger)" in your source file.
 */
#define DECLARE_LOG_CALLBACKS(logger,level) \
   static void JUtils_##level##Func(const char *fmt, va_list args) \
   {                                                               \
      JUtils_LogMsg((logger), Msg##level, fmt, args);              \
   }
#define DECLARE_LOG_FUNC \
   static void JUtils_Log(const char *fmt, ...) \
   {                                            \
      va_list ap;                               \
      va_start(ap, fmt);                        \
      JUtils_LogFunc(fmt, ap);                  \
      va_end(ap);                               \
}
#define DECLARE_PROGRESS_FUNC(logger)                                        \
   static Bool JUtils_ProgressFunc(void *progressData, int percentCompleted) \
   {                                                                         \
      return JUtils_LogProgress((logger), progressData, percentCompleted);   \
   }
#define DECLARE_LOG_FUNCS(logger) \
   DECLARE_LOG_CALLBACKS(logger, Log)   \
   DECLARE_LOG_CALLBACKS(logger, Warn)  \
   DECLARE_LOG_CALLBACKS(logger, Panic) \
   DECLARE_LOG_FUNC                     \
   DECLARE_PROGRESS_FUNC(logger)

#endif // _JUTILS_H_
