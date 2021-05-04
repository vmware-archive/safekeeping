/* **************************************************************************
 * Copyright 2008 VMware, Inc.  All rights reserved. -- VMware Confidential
 * **************************************************************************/

/*
 *  jMntApi.c
 *
 *    JNI bindings for vixMntApi.
 */

#include <assert.h>
#include <string.h>
#include "jMntApi.h"
#include "vixDiskLib.h"
#include "vixMntapi.h"
#include "jUtils.h"

/*
 * Global variable forlogger callbacks and declaration of logging callback
 * functions to pass down into vixMntApi.
 */
static JUtilsLogger *gLogger = NULL;
DECLARE_LOG_FUNCS(gLogger)


/*
 *-----------------------------------------------------------------------------
 *
 * JNISetVolumeInfo --
 *
 *     Helper function to translate a VixVolumeInfo structure into its
 *     Java equivalent.
 *
 * Results:
 *      None.
 *
 * Side effects:
 *      None
 *
 *-----------------------------------------------------------------------------
 */

static void
JNISetVolumeInfo(JNIEnv *env,         // IN: Java Environment
                 jobject volumeInfo,  // INOUT: Java object to modify
                 VixVolumeInfo *info) // IN: VixVolumeInfo to translate
{
   jobject arr;

   if (info == NULL || volumeInfo == NULL) {
      return;
   }

   JUtils_SetIntField(env, volumeInfo, "type", info->type);
   JUtils_SetBoolField(env, volumeInfo, "isMounted", info->isMounted);
   JUtils_SetStringField(env, volumeInfo, "symbolicLink", info->symbolicLink);

   arr = JUtils_MakeStringArray(env, info->numGuestMountPoints,
                                info->inGuestMountPoints);
   JUtils_SetStringArray(env, volumeInfo, "inGuestMountPoints", arr);

   JUtils_SetLongField(env, volumeInfo, "ptr", (jlong)(size_t)info);
}


/*
 *
 * JNI Interface implementation
 *
 */


/*
 *-----------------------------------------------------------------------------
 *
 * JNISetOsInfo --
 *
 *     Helper function to translate a VixOsInfo structure into its
 *     Java equivalent.
 *
 * Results:
 *      None.
 *
 * Side effects:
 *      None
 *
 *-----------------------------------------------------------------------------
 */

static void
JNISetOsInfo(JNIEnv *env,     // IN: Java Environment
             jobject osInfo,  // INOUT: Java object to modify
             VixOsInfo *info) // IN: VixOsInfo to translate
{
   if (info == NULL || osInfo == NULL) {
      return;
   }

   JUtils_SetIntField(env, osInfo, "family", info->family);
   JUtils_SetIntField(env, osInfo, "majorVersion", info->majorVersion);
   JUtils_SetIntField(env, osInfo, "minorVersion", info->minorVersion);
   JUtils_SetBoolField(env, osInfo, "osIs64Bit", info->osIs64Bit);
   JUtils_SetStringField(env, osInfo, "vendor", info->vendor);
   JUtils_SetStringField(env, osInfo, "edition", info->edition);
   JUtils_SetStringField(env, osInfo, "osFolder", info->osFolder);
   JUtils_SetLongField(env, osInfo, "ptr", (jlong)(size_t)info);
}


/*
 *-----------------------------------------------------------------------------
 *
 * InitJNI --
 *
 *      JNI implementation for VixmntApi_Init. See VixDiskLib docs for
 *      details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jMntApiImpl_InitJNI (JNIEnv *env,
                                          jobject obj,
                                          jint major,
                                          jint minor,
                                          jobject logger,
                                          jstring libDir,
                                          jstring tmpDir)
{
   const char *cLibDir, *cTmpDir;
   jlong result;

   gLogger = JUtils_InitLogging(env, logger);

   cLibDir = GETSTRING(libDir);
   cTmpDir = GETSTRING(tmpDir);

   result = VixMntapi_Init(major, minor, &JUtils_LogFunc, &JUtils_WarnFunc,
                           &JUtils_PanicFunc, cLibDir, cTmpDir);

   FREESTRING(cLibDir, libDir);
   FREESTRING(cTmpDir, tmpDir);
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * ExitJNI --
 *
 *      JNI implementation for VixmntApi_Exit. See VixDiskLib docs for
 *      details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT void JNICALL
Java_com_vmware_jvix_jMntApiImpl_ExitJNI(JNIEnv *env,
                                         jobject obj)
{
   VixMntapi_Exit();
   JUtils_ExitLogging(gLogger);
   gLogger = NULL;
}


/*
 *-----------------------------------------------------------------------------
 *
 * OpenDisksJNI --
 *
 *      JNI implementation for VixmntApi_OpenDisks. See VixDiskLib docs for
 *      details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jMntApiImpl_OpenDisksJNI(JNIEnv *env,
                                              jobject obj,
                                              jlong vixConnection,
                                              jobjectArray diskNames,
                                              jint openMode,
                                              jlongArray diskSet)     // OUT
{
   char* *cDiskNames = NULL;
   jsize bufSize = 0;
   jlong *cDiskSet = NULL;
   jlong result = VIX_E_FAIL, jout = 0;
   VixDiskSetHandle nDiskSet;
   VixDiskSetHandle *pDiskSet;
   VixDiskLibConnection conn = (VixDiskLibConnection)(size_t)vixConnection;
   int i;

   if (diskNames != NULL) {
      bufSize = (*env)->GetArrayLength(env, diskNames);
      cDiskNames = (char**)malloc(bufSize * sizeof (char*));
      if (cDiskNames == NULL) {
         JUtils_Log("OpenDisksJNI: Memory allocation failed.\n");
         return VIX_E_OUT_OF_MEMORY;
      }
      for(i = 0; i < bufSize; i++) {
         jstring theString = (jstring)(*env)->GetObjectArrayElement(env, diskNames, i);
         const char *cString = GETSTRING(theString);
         cDiskNames[i] = strdup(cString);
         FREESTRING(cString, theString);
      }
      if (diskSet != NULL) {
         pDiskSet = &nDiskSet;
      } else {
         pDiskSet = NULL;
      }

      result = VixMntapi_OpenDisks(conn, cDiskNames, bufSize, openMode, pDiskSet);
      if (pDiskSet != NULL) {
         jout = (size_t)*pDiskSet;
         (*env)->SetLongArrayRegion(env, diskSet, 0, 1, &jout);
      }

   }

   for(i = 0; i < bufSize; i++) {
      free(cDiskNames[i]);
   }

   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * OpenDiskSetJNI --
 *
 *      JNI implementation for VixmntApi_OpenDiskSet. See VixDiskLib docs for
 *      details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jMntApiImpl_OpenDiskSetJNI(JNIEnv *env,
                                                jobject obj,
                                                jlongArray diskHandles,
                                                jint openMode,
                                                jlongArray diskSet)     // OUT
{
   VixDiskLibHandle *cDiskHandles = NULL;
   jsize bufSize = 0;
   jlong *jBuf, *cDiskSet = NULL;
   jlong result, jout = 0;
   VixDiskSetHandle nDiskSet;
   VixDiskSetHandle *pDiskSet;
   int i;

   if (diskHandles != NULL) {
      bufSize = (*env)->GetArrayLength(env, diskHandles);
      cDiskHandles = malloc(bufSize * sizeof *cDiskHandles);
      if (cDiskHandles == NULL) {
         JUtils_Log("OpenDiskSetJNI: Memory allocation failed.\n");
         return VIX_E_OUT_OF_MEMORY;
      }
      jBuf = (*env)->GetLongArrayElements(env, diskHandles, NULL);
      for(i = 0; i < bufSize; i++) {
         cDiskHandles[i] = (VixDiskLibHandle)(size_t)(jBuf[i]);
      }
   }

   if (diskSet != NULL) {
      pDiskSet = &nDiskSet;
   } else {
      pDiskSet = NULL;
   }

   result = VixMntapi_OpenDiskSet(cDiskHandles, bufSize, openMode, pDiskSet);

   if (pDiskSet != NULL) {
      jout = (size_t)*pDiskSet;
      (*env)->SetLongArrayRegion(env, diskSet, 0, 1, &jout);
   }

   if (diskHandles != NULL) {
      (*env)->ReleaseLongArrayElements(env, diskHandles, jBuf, JNI_ABORT);
   }

   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * CloseDiskSetJNI --
 *
 *      JNI implementation for VixmntApi_CloseDiskSet. See VixDiskLib docs for
 *      details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jMntApiImpl_CloseDiskSetJNI (JNIEnv *env,
                                                  jobject obj,
                                                  jlong diskSet)
{
   return VixMntapi_CloseDiskSet((VixDiskSetHandle)(size_t)diskSet);
}


/*
 *-----------------------------------------------------------------------------
 *
 * GetVolumeHandlesJNI --
 *
 *      JNI implementation for VixmntApi_GetVolumeHandles. See
 *      VixDiskLib docs for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jMntApiImpl_GetVolumeHandlesJNI(JNIEnv *env,
                                                     jobject obj,
                                                     jlong diskSet,
                                                     jobject volumes) // OUT
{
   jlong result;
   size_t numberOfVolumes, *pNumberOfVolumes = NULL;
   VixVolumeHandle *volumeHandles, **pVolumeHandles = NULL;
   jlongArray jHandles = NULL;
   size_t i;

   if (volumes != NULL) {
      pNumberOfVolumes = &numberOfVolumes;
      pVolumeHandles = &volumeHandles;
   }

   result = VixMntapi_GetVolumeHandles((VixDiskSetHandle)(size_t)diskSet,
                                       pNumberOfVolumes, pVolumeHandles);

   if (result == VIX_OK && volumes != NULL) {
      JUtils_SetLongField(env, volumes, "ptr", (jlong)(size_t)*pVolumeHandles);
      if (*pVolumeHandles != NULL && *pNumberOfVolumes > 0) {
         jHandles = (*env)->NewLongArray(env, *pNumberOfVolumes);
         for (i = 0; i < *pNumberOfVolumes; i++) {
            jlong hlp = (jlong)(size_t)volumeHandles[i];
            (*env)->SetLongArrayRegion(env, jHandles, i, 1, &hlp);
         }
         JUtils_SetObjectField(env, volumes, "handles", "[J", jHandles);
      }
   }

   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * FreeVolumeHandlesJNI --
 *
 *      JNI implementation for VixmntApi_FreeVolumeHandles. See
 *      VixDiskLib docs for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT void JNICALL
Java_com_vmware_jvix_jMntApiImpl_FreeVolumeHandlesJNI(JNIEnv *env,
                                                      jobject obj,
                                                      jlong volumes)
{
   VixMntapi_FreeVolumeHandles((VixVolumeHandle *)(size_t)volumes);
}


/*
 *-----------------------------------------------------------------------------
 *
 * GetOsInfoJNI --
 *
 *      JNI implementation for VixmntApi_GetOsInfo. See
 *      VixDiskLib docs for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jMntApiImpl_GetOsInfoJNI(JNIEnv *env,
                                              jobject obj,
                                              jlong diskSet,
                                              jobject osInfo)
{
   VixOsInfo *info = NULL, **pInfo = NULL;
   jlong result;

   if (osInfo != NULL) {
      pInfo = &info;
   }

   result = VixMntapi_GetOsInfo((VixDiskSetHandle)(size_t)diskSet, pInfo);
   JNISetOsInfo(env, osInfo, info);

   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * FreeOsInfoJNI --
 *
 *      JNI implementation for VixmntApi_FreeOsInfo. See VixDiskLib
 *      docs for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT void JNICALL
Java_com_vmware_jvix_jMntApiImpl_FreeOsInfoJNI(JNIEnv *env,
                                               jobject obj,
                                               jlong osInfo)
{
   VixMntapi_FreeOsInfo((VixOsInfo *)(size_t)osInfo);
}


/*
 *-----------------------------------------------------------------------------
 *
 * MountVolumeJNI --
 *
 *      JNI implementation for VixmnApi_MountVolume. See VixDiskLib
 *      docs for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jMntApiImpl_MountVolumeJNI(JNIEnv *env,
                                                jobject obj,
                                                jlong volumeHandle,
                                                jboolean readOnly)
{
   return VixMntapi_MountVolume((VixVolumeHandle)(size_t)volumeHandle,
                                readOnly);
}


/*
 *-----------------------------------------------------------------------------
 *
 * DismountVolumeJNI --
 *
 *      JNI implementation for VixmnApi_DismountVolume. See VixDiskLib
 *      docs for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jMntApiImpl_DismountVolumeJNI(JNIEnv *env,
                                                   jobject obj,
                                                   jlong volumeHandle,
                                                   jboolean force)
{
   return VixMntapi_DismountVolume((VixVolumeHandle)(size_t)volumeHandle,
                                   force);
}


/*
 *-----------------------------------------------------------------------------
 *
 * GetVolumeInfoJNI --
 *
 *      JNI implementation for VixmnApi_GetVolumeInfo. See VixDiskLib
 *      docs for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jMntApiImpl_GetVolumeInfoJNI(JNIEnv *env,
                                                  jobject obj,
                                                  jlong volumeHandle,
                                                  jobject volumeInfo)
{
   VixVolumeInfo *info = NULL, **pInfo = NULL;
   jlong result;

   if (volumeInfo != NULL) {
      pInfo = &info;
   }

   result = VixMntapi_GetVolumeInfo((VixVolumeHandle)(size_t)volumeHandle, pInfo);
   JNISetVolumeInfo(env, volumeInfo, info);

   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * FreeVolumeInfoJNI --
 *
 *      JNI implementation for VixmnApi_FreeVolumeInfo. See VixDiskLib
 *      docs for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT void JNICALL
Java_com_vmware_jvix_jMntApiImpl_FreeVolumeInfoJNI(JNIEnv *env,
                                                   jobject obj,
                                                   jlong volumeInfo)
{
   VixMntapi_FreeVolumeInfo((VixVolumeInfo *)(size_t)volumeInfo);
}
