/* **************************************************************************
 * Copyright 2008 VMware, Inc.  All rights reserved. -- VMware Confidential
 * **************************************************************************/

/*
 *  jUtils.c
 *
 *    Some helper functions for implementing JNI bindings.
 */

#include <string.h>
#include <assert.h>
#include <stdio.h>
#include "jni.h"
#include "vixDiskLib.h"
#include "jUtils.h"

#ifdef _WIN32
//#define vsnprintf(A,B,C,D) vsnprintf_s(A, B, _TRUNCATE, C, D)
#define vsnprintf _vsnprintf
#define strdup _strdup
#endif

/*
 * Maximum number of bytes in a log/warn/panic message that we can handle
 * without malloc/free calls. Should be large enough to handle most of
 * the logging calls.
 */
#define LOGBUFSIZE 4096

#define LGPFX "jDiskLib_JNI: "

/*
 * Method name in the calling Java class that will receive async
 * write callbacks
 */

#define ASYNC_IO_LISTENER "com/vmware/jvix/AsyncIOListener"
#define ASYNC_IO_LISTENER_ONCOMPLETE "onComplete"
#define ASYNC_IO_LISTENER_ONCOMPLETE_SIG "(J)V"


/*
 * Structure for holding logging callbacks.
 */
struct JUtilsLogger {
   JavaVM *javaVM;     /* Pointer to Java VM context */
   jobject loggerObj;  /* Reference to Java object implementing logging */
   jmethodID midLog;   /* Method ID for "Log" messages. */
   jmethodID midWarn;  /* Method ID for "Warn" messages. */
   jmethodID midPanic; /* Method ID for "Panic" messages.  */
};


/*
 * Structure for holding async write callback data.
 */
struct jUtilsAsyncCallback {
   JavaVM *javaVM;       /* Pointer to Java VM context */
   jobject callbackObj;  /* Object on which the callback will be made */
};

static jmethodID gAsyncCallbackId = NULL; /* ID for the callback method */


/*
 *
 * Primitives for reading/writing Java data types from C
 *
 */


/*
 *-----------------------------------------------------------------------------
 *
 * GetField --
 *
 *      Return a fieldID for an object's field given its name and its type
 *      signature. Will assert if the field does not exist or has the wrong
 *      type.
 *
 * Results:
 *      JNI fieldID of field "name" belonging to "obj".
 *
 * Side effects:
 *      None
 *
 *-----------------------------------------------------------------------------
 */

static jfieldID
GetField(JNIEnv *env,        // IN: Java Environment
         jobject obj,        // IN: Object
         const char *name,   // IN: Field name
         const char *typeId) // IN: Type signature of field
{
   jclass oClass;
   jfieldID field;

   oClass = (*env)->GetObjectClass(env, obj);
   field = (*env)->GetFieldID(env, oClass, name, typeId);
   if (field == NULL) {
      fprintf(stderr, "Unknown object field: %s\n", name);
      assert(0);
   }
   return field;
}


/*
 *-----------------------------------------------------------------------------
 *
 * JUtils_GetLongField --
 *
 *      Return a long value from an object's field.
 *
 * Results:
 *      Value of long. If "name" is not a valid field name, we'll assert.
 *
 * Side effects:
 *      None
 *
 *-----------------------------------------------------------------------------
 */

jlong
JUtils_GetLongField(JNIEnv *env,      // IN: Java Environment
                    jobject obj,      // IN: Object
                    const char *name) // IN: Field name
{
   return (*env)->GetLongField(env, obj, GetField(env, obj, name, "J"));
}


/*
 *-----------------------------------------------------------------------------
 *
 * JUtils_SetLongField --
 *
 *      Set a java object long field. If "name" is not a valid
 *      field name, the function will assert.
 *
 * Results:
 *      None.
 *
 * Side effects:
 *      None
 *
 *-----------------------------------------------------------------------------
 */

void
JUtils_SetLongField(JNIEnv *env,      // IN: Java Environment
                    jobject obj,      // IN: Object
                    const char *name, // IN: Field Name
                    jlong value)      // IN: New value for field
{
   (*env)->SetLongField(env, obj, GetField(env, obj, name, "J"), value);
}


/*
 *-----------------------------------------------------------------------------
 *
 * JUtils_GetIntField --
 *
 *      Return a int value from an object's field.
 *
 * Results:
 *      Value of int. If "name" is not a valid field name, we'll assert.
 *
 * Side effects:
 *      None
 *
 *-----------------------------------------------------------------------------
 */

jint
JUtils_GetIntField(JNIEnv *env,      // IN: Java Environment
                   jobject obj,      // IN: Object
                   const char *name) // IN: Field name
{
   return (*env)->GetIntField(env, obj, GetField(env, obj, name, "I"));
}


/*
 *-----------------------------------------------------------------------------
 *
 * JUtils_SetIntField --
 *
 *      Set a java object int field. If "name" is not a valid field
 *      name, the function will assert.
 *
 * Results:
 *      None.
 *
 * Side effects:
 *      None
 *
 *-----------------------------------------------------------------------------
 */

void
JUtils_SetIntField(JNIEnv *env,      // IN: Java Environment
                   jobject obj,      // IN: Object
                   const char *name, // IN: Field name
                   jint value)       // IN: New value for field
{
   (*env)->SetIntField(env, obj, GetField(env, obj, name, "I"), value);
}


/*
 *-----------------------------------------------------------------------------
 *
 * JUtils_SetBoolField --
 *
 *      Set a java object boolean field. If "name" is not a valid field
 *      name, the function will assert.
 *
 * Results:
 *      None.
 *
 * Side effects:
 *      None
 *
 *-----------------------------------------------------------------------------
 */

void
JUtils_SetBoolField(JNIEnv *env,      // IN: Java Environment
                    jobject obj,      // IN: Object
                    const char *name, // IN: Field name
                    jboolean value)   // IN: New value for field
{
   (*env)->SetBooleanField(env, obj, GetField(env, obj, name, "Z"), value);
}


/*
 *-----------------------------------------------------------------------------
 *
 * JUtils_GetStringField --
 *
 *      Return a C string from an object's field.
 *
 * Results:
 *      C string corresponding to Java string. Caller is responsible for
 *      freeing it. Can return NULL if string object is null in Java land.
 *      Also, the function will assert if "name" is not a valid string field.
 *
 * Side effects:
 *      None
 *
 *-----------------------------------------------------------------------------
 */

char *
JUtils_GetStringField(JNIEnv *env,      // IN: Java Environment
                      jobject obj,      // IN: Object
                      const char *name) // IN: Field Name
{
   jfieldID field;
   jstring jstr;
   const char *hlp;
   char *result = NULL;

   field = GetField(env, obj, name, "Ljava/lang/String;");
   jstr = (*env)->GetObjectField(env, obj, field);
   if (jstr == NULL) {
      return NULL;
   }
   hlp = (*env)->GetStringUTFChars(env, jstr, NULL);
   if (strcmp(hlp, "") != 0) {
      result = strdup(hlp);
   }
   (*env)->ReleaseStringUTFChars(env, jstr, hlp);
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * JUtils_SetStringField --
 *
 *      Set a java object string field to the value corresponding to the
 *      passed in C string. If "name" is not a valid field name, this function
 *      will assert.
 *
 * Results:
 *      None.
 *
 * Side effects:
 *      None
 *
 *-----------------------------------------------------------------------------
 */

void
JUtils_SetStringField(JNIEnv *env,       // IN: Java environment
                      jobject obj,       // IN: Object
                      const char *name,  // IN: Field name
                      const char *value) // IN: New value for string field
{
   jfieldID field;
   jstring newVal = NULL;

   if (value != NULL) {
      newVal = (*env)->NewStringUTF(env, value);
   }
   field = GetField(env, obj, name, "Ljava/lang/String;");
   (*env)->SetObjectField(env, obj, field, newVal);
}


/*
 *-----------------------------------------------------------------------------
 *
 * JUtils_SetStringArray --
 *
 *      Set a java object string array field to the value
 *      corresponding to the passed in object, which must be of the
 *      correct type (string array).  If "name" is not a valid field
 *      name or is of the wrong type, this function will assert.
 *
 * Results:
 *      None.
 *
 * Side effects:
 *      None
 *
 *-----------------------------------------------------------------------------
 */

void
JUtils_SetStringArray(JNIEnv *env,      // IN: Java Environment
                      jobject obj,      // IN: Object
                      const char *name, // IN: Field name
                      jobject value)    // IN: New value for field
{
   jfieldID field;

   field = GetField(env, obj, name, "[Ljava/lang/String;");
   (*env)->SetObjectField(env, obj, field, value);
}


/*
 *-----------------------------------------------------------------------------
 *
 * JUtils_GetObjectField --
 *
 *      Return a reference to an object from a field of a class.
 *
 * Results:
 *      Object reference. Might be null if object is null in Java
 *      land. Also, will assert if "name" is not a valid field of the
 *      right type.
 *
 * Side effects:
 *      None
 *
 *-----------------------------------------------------------------------------
 */

jobject
JUtils_GetObjectField(JNIEnv *env,        // IN: Java Environment
                      jobject obj,        // IN: Object
                      const char *name,   // IN: Field name
                      const char *typeId) // IN: Type signature for field
{
   return (*env)->GetObjectField(env, obj, GetField(env, obj, name, typeId));
}


/*
 *-----------------------------------------------------------------------------
 *
 * JUtils_SetObjectField --
 *
 *      Set an object field to a new value (which might be null). The object
 *      must be of the correct type. The function will assert if "name" does
 *      not exist or is not of the correct type.
 *
 * Results:
 *      None.
 *
 * Side effects:
 *      None
 *
 *-----------------------------------------------------------------------------
 */

void
JUtils_SetObjectField(JNIEnv *env,        // IN: Java Environment
                      jobject obj,        // IN: Object
                      const char *name,   // IN: Field name
                      const char *typeId, // IN: Type signature for field
                      jobject value)      // IN: New value for field
{
   jfieldID field;

   field = GetField(env, obj, name, typeId);
   (*env)->SetObjectField(env, obj, field, value);
}


/*
 *-----------------------------------------------------------------------------
 *
 * JUtils_MakeStringArray --
 *
 *      Construct a Java object that is of type "array of string" from a list
 *      of C strings.
 *
 * Results:
 *      Java object ID of newly constructed object.
 *
 * Side effects:
 *      None
 *
 *-----------------------------------------------------------------------------
 */

jobject
JUtils_MakeStringArray(JNIEnv *env,         // IN: Java Environment
                       int length,          // IN: Length of list of C strings
                       const char **values) // IN: List of C strings
{
   jclass strClass;
   jobject result = NULL;
   int i;

   strClass =  (*env)->FindClass(env, "java/lang/String");

   result = (*env)->NewObjectArray(env, length, strClass, NULL);
   for (i = 0; i < length; i++) {
      jstring str = (*env)->NewStringUTF(env, values[i]);
      (*env)->SetObjectArrayElement(env, result, i, str);
   }

   /*
    * Get rid of implicit reference count increment to class 
    * from FindClass.
    */
   (*env)->DeleteLocalRef(env, strClass);
   return result;
}


/*
 * Create an async callback structure for an async write.
 *
 * Results:
 *      Pointer to async callback data that should be passed into the async
 *      operation.
 *
 * Side effects:
 *      None
 */

jUtilsAsyncCallback *
jUtils_CreateAsyncCallback(JNIEnv *env, jobject callbackObj)
{
   jUtilsAsyncCallback *callbackInfo = NULL;
   jclass cls;

   callbackInfo = (jUtilsAsyncCallback *)calloc(1, sizeof *callbackInfo);
   if (callbackInfo == NULL) {
      printf(LGPFX"Could not allocate memory for async data\n");
      assert(0);
   }
   (*env)->GetJavaVM(env, &callbackInfo->javaVM);
   if (callbackInfo->javaVM == NULL) {
      printf(LGPFX"Could not retrieve the Java VM\n");
      assert(0);
   }
   callbackInfo->callbackObj = (*env)->NewGlobalRef(env, callbackObj);
   if (callbackInfo->callbackObj == 0) {
      printf(LGPFX"Could not retrieve a reference for the Java object\n");
      assert(0);
   }

   if (gAsyncCallbackId == NULL) {
      cls = (*env)->FindClass(env, ASYNC_IO_LISTENER);
      if (cls == NULL) {
         printf(LGPFX"Could not find callback class %s\n", ASYNC_IO_LISTENER);
         assert(0);
      }
      gAsyncCallbackId = (*env)->GetMethodID(env, cls,
                                             ASYNC_IO_LISTENER_ONCOMPLETE,
                                             ASYNC_IO_LISTENER_ONCOMPLETE_SIG);
      if (gAsyncCallbackId == NULL) {
         printf(LGPFX"Could not find the callback method %s\n", ASYNC_IO_LISTENER_ONCOMPLETE);
         assert(0);
      }
   }

   return callbackInfo;
}


/*
 * Cleanup the async structure used in a callback.
 *
 * Results:
 *      None
 *
 * Side effects:
 *      None
 */

void
jUtils_ReleaseAsyncCallback(jUtilsAsyncCallback *callbackInfo)
{
   JNIEnv *env;

   if (callbackInfo == NULL) {
      printf(LGPFX"Invalid callbackInfo parameter\n");
      assert(0);
   }

   (*callbackInfo->javaVM)->GetEnv(callbackInfo->javaVM,
                                (void **)&env, JNI_VERSION_1_2);

   if (callbackInfo->callbackObj != NULL) {
      (*env)->DeleteGlobalRef(env, callbackInfo->callbackObj);
   }

   callbackInfo->callbackObj = NULL;
   free(callbackInfo);
}


/*
 * Completion callback handler for an async operation.
 *
 * Results:
 *      None
 *
 * Side effects:
 *      None
 */

void
jUtilsCompletionCB(jUtilsAsyncCallback *callbackInfo,
                   jlong result)
{
   Bool attached = FALSE;
   jint envStat;
   JNIEnv *env;
   JavaVM *vm = callbackInfo->javaVM;

   if (callbackInfo == NULL) {
      printf(LGPFX"Invalid userData parameter during callback\n");
      assert(0);
   }

   envStat = (*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_2);

   if (envStat == JNI_EDETACHED) {
      // Need to attach the thread
      if ((*vm)->AttachCurrentThread(vm, (void **) &env, NULL) != 0) {
         printf(LGPFX"Failed to attach the thread\n");
         assert(0);
      }
      attached = TRUE;
   }

   (*env)->CallVoidMethod(env,
                          callbackInfo->callbackObj,
                          gAsyncCallbackId,
                          result);

   jUtils_ReleaseAsyncCallback(callbackInfo);

   if (attached) {
      // Detach the thread again
      (*vm)->DetachCurrentThread(vm);
   }
}

/*
 *
 * Functions for dealing with VIX logging.
 *
 */


/*
 *-----------------------------------------------------------------------------
 *
 * JUtils_InitLogging --
 *
 *      Set up logging using the logger object reference passed in from Java.
 *      Will assert if log/warn/panic functions are unset in logger object
 *      passed in.
 *
 * Results:
 *      Logger object.
 *
 * Side effects:
 *      None
 *
 *-----------------------------------------------------------------------------
 */

JUtilsLogger *
JUtils_InitLogging(JNIEnv *env,    // IN: Java Environment
                   jobject logger) // IN: Logger object
{
   jclass cls;
   jmethodID midLog = NULL, midWarn = NULL, midPanic = NULL;
   JUtilsLogger *jLogger = NULL;

   jLogger = calloc(1, sizeof *jLogger);
   assert(jLogger != NULL);

   if (logger != NULL) {
      cls = (*env)->GetObjectClass(env, logger);
      midLog = (*env)->GetMethodID(env, cls, "Log", "(Ljava/lang/String;)V");
      if (midLog == NULL) {
         printf(LGPFX"Logger object must have a \"Log\" method.\n");
         assert(0);
      }
      midWarn = (*env)->GetMethodID(env, cls, "Warn", "(Ljava/lang/String;)V");
      if (midWarn == NULL) {
         printf(LGPFX"Logger object must have a \"Warn\" method.\n");
         assert(0);
      }
      midPanic = (*env)->GetMethodID(env, cls, "Panic", "(Ljava/lang/String;)V");
      if (midLog == NULL) {
         printf(LGPFX"Logger object must have a \"Panic\" method.\n");
         assert(0);
      }

      /*
       * Create a global reference to the Logger object to prevent it from
       * getting garbage-collected in Java-land while we are still using it.
       */
      jLogger->loggerObj = (*env)->NewGlobalRef(env, logger);
   }

   /*
    * Store a pointer to the java VM in a global variable. This will enable
    * us to look up our correct JNIEnv context when the log function is being
    * called from the underlying library. The app using this interface could
    * be multi-threaded and hence cacheing the JNIEnv pointer is a bad idea...
    */
   (*env)->GetJavaVM(env, &(jLogger->javaVM));

   jLogger->midLog = midLog;
   jLogger->midWarn = midWarn;
   jLogger->midPanic = midPanic;
   return jLogger;
}


/*
 *-----------------------------------------------------------------------------
 *
 * JUtils_LogMsg --
 *
 *      Forward log/warn/panic messages to Java.
 *
 * Results:
 *      None.
 *
 * Side effects:
 *      None
 *
 *-----------------------------------------------------------------------------
 */

void
JUtils_LogMsg(JUtilsLogger *jLogger, // IN: Logging context for module
              JUtilsLogLevel level,  // IN: Log level
              const char *fmt,       // IN: In message format string
              va_list args)          // IN: Message varargs
{
   jstring msg;
   JNIEnv *env = NULL;
   jint envStat;
   char buf[LOGBUFSIZE] = {0};
   int len;
   jmethodID mid = NULL;
   int attached = FALSE;
   JavaVM *javaVM = jLogger->javaVM;

   if (jLogger != NULL) {
      switch (level) {
      case MsgLog:   mid = jLogger->midLog;   break;
      case MsgWarn:  mid = jLogger->midWarn;  break;
      case MsgPanic: mid = jLogger->midPanic; break;
      default:
         assert(0);
      }
   }
   if (mid == NULL) {
      /*
       * respective function not available. Log to stdout in this case for
       * lack of better options.
       */
      vprintf(fmt, args);
      return;
   }

   len = vsnprintf(buf, LOGBUFSIZE, fmt, args);
#ifndef _WIN32
   /*
    * Linux/Windows has different vsnprintf implementation
    * Just ignore linux error, print to stdout
    */
   if (len < 0) {
      vprintf(fmt, args);
      return;
   }
#endif

   /*
    * This call is happening from the depths of vixDiskLib. We don't have
    * a reference for the JNI environment handy for the current thread,
    * so rely on the Java VM to give one to us.
    */
   envStat = (*javaVM)->GetEnv(javaVM, (void **)&env, JNI_VERSION_1_2);
   if (envStat == JNI_EDETACHED) {
      /* Need to attach the thread */
      if ((*javaVM)->AttachCurrentThread(javaVM, (void **)&env, NULL)
          == JNI_OK) {
         attached = TRUE;
      }
   }

   if (env == NULL) {
      printf(LGPFX"Can not get java Env in %s\n", __FUNCTION__);
      /*Log to stdout*/
      vprintf(fmt, args);
      return;
   }

   msg = (*env)->NewStringUTF(env, buf);
   if (msg != NULL) {
      (*env)->CallVoidMethod(env, jLogger->loggerObj, mid, msg);
   }

   if (attached) {
      /* Detach the thread again */
      (*javaVM)->DetachCurrentThread(javaVM);
   }
}


/*
 *-----------------------------------------------------------------------------
 *
 * JNIProgressFunc --
 *
 *      Progress update function to pass down into VixDiskLib calls that
 *      require one. Forwards calls back to Java.
 *
 * Results:
 *      Forwarded from Java.
 *
 * Side effects:
 *      None
 *
 *-----------------------------------------------------------------------------
 */

Bool
JUtils_LogProgress(JUtilsLogger *jLogger, // IN: Logging context for module
                   void *progressData,    // IN: Callback data
                   int percentCompleted)  // IN: Completion percentage update
{
   jobject obj = (jobject)progressData;
   JNIEnv *env;
   jclass cls;
   jmethodID mid;

   if (jLogger == NULL) {
      printf(LGPFX" Progress callback failed: Init loging first.\n");
      return FALSE;
   }

   (*(jLogger->javaVM))->GetEnv(jLogger->javaVM, (void **)&env, JNI_VERSION_1_2);
   cls = (*env)->GetObjectClass(env, obj);
   mid = (*env)->GetMethodID(env, cls, "Update", "(I)Z");
   if (mid == NULL) {
      /*
       * This should never happen unless the Java code encapsulating the JNI
       * layer has been tampered with.
       */
      printf(LGPFX"Progress object must have \"Update\" method.\n");
      assert(0);
   }

   return (*env)->CallBooleanMethod(env, obj, mid, percentCompleted);
}


/*
 *-----------------------------------------------------------------------------
 *
 * JUtils_ExitLogging --
 *
 *      Shut down logging and release global reference to logger object.
 *
 * Results:
 *      None.
 *
 * Side effects:
 *      None
 *
 *-----------------------------------------------------------------------------
 */

void
JUtils_ExitLogging(JUtilsLogger *jLogger) // IN: Logging context for module
{
   JNIEnv *env;

   if (jLogger == NULL) {
      return;
   }

   (*(jLogger->javaVM))->GetEnv(jLogger->javaVM, (void **)&env, JNI_VERSION_1_2);

   if (jLogger->loggerObj != NULL) {
      (*env)->DeleteGlobalRef(env, jLogger->loggerObj);
   }
   jLogger->loggerObj = NULL;
   jLogger->midLog = jLogger->midWarn = jLogger->midPanic = NULL;

   free(jLogger);
}
