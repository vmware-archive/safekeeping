/* **************************************************************************
 * Copyright 2008-2020 VMware, Inc.  All rights reserved. -- VMware Confidential
 * **************************************************************************/

/*
 *  jDiskLib.c
 *
 *    JNI C implementation for vixDiskLib
 */

#include <string.h>
#include <assert.h>
#include <stdio.h>
#include "jDiskLibImpl.h"
#include "vixDiskLib.h"
#include "jUtils.h"
#include "vddkFaultInjection.h"

#ifdef _WIN32
#define strdup _strdup
#endif

/*
 * Global variable for logger callbacks and declaration of logging callback
 * functions to pass down into vixMntApi.
 */
static JUtilsLogger *gLogger = NULL;
DECLARE_LOG_FUNCS(gLogger)

#ifndef _WIN32
extern void Perturb_Enable(const char *fName, int enable);
#else
void
Perturb_Enable(const char *fName, int enable)
{
   /* This is a dummy function for Windows. This will be implemented */
   /* for real later */
}
#endif

/*
 *
 * Primitives for mapping vixDiskLib data types to their corresponding
 * Java data types and vice versa. Uses access primitives to
 * manipulate individual entries.
 *
 */


/*
 *-----------------------------------------------------------------------------
 *
 * JNISetDiskGeometry --
 *
 *      Forward disk geometry data from vixDiskLib to Java.
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
JNISetDiskGeometry(const VixDiskLibGeometry *geo, // IN geometry to forward
                   JNIEnv *env,                   // IN: Java Environment
                   jobject obj)                   // IN: Object
{
   JUtils_SetIntField(env, obj, "cylinders", geo->cylinders);
   JUtils_SetIntField(env, obj, "heads", geo->heads);
   JUtils_SetIntField(env, obj, "sectors", geo->sectors);
}


/*
 *-----------------------------------------------------------------------------
 *
 * JNISetDiskLibInfo --
 *
 *      Forward DiskLibInfo data from vixDiskLib to Java.
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
JNISetDiskLibInfo(VixDiskLibInfo *info, // IN: disklibinfo to forward
                  JNIEnv *env,          // IN: Java Environment
                  jobject dli)          // In: Object to forward to
{
   jclass cDli;
   jobject oGeo;

   // class for disk lib info object
   cDli = (*env)->GetObjectClass(env, dli);

   // Set the bios geometry
   oGeo = JUtils_GetObjectField(env, dli, "biosGeo",
                                 "Lcom/vmware/jvix/jDiskLib$Geometry;");
   JNISetDiskGeometry(&info->biosGeo, env, oGeo);

   // Set the physical geometry
   oGeo = JUtils_GetObjectField(env, dli, "physGeo",
                                "Lcom/vmware/jvix/jDiskLib$Geometry;");
   JNISetDiskGeometry(&info->physGeo, env, oGeo);

   // Set simple data members
   JUtils_SetLongField(env, dli, "capacityInSectors", (jlong)info->capacity);
   JUtils_SetIntField(env, dli, "numLinks", (jint)info->numLinks);
   JUtils_SetIntField(env, dli, "adapterType", (jint)info->adapterType);
   JUtils_SetStringField(env, dli, "parentFileNameHint",
                         info->parentFileNameHint);
   JUtils_SetLongField(env, dli, "logicalSectorSize",
                       (jlong)info->logicalSectorSize);
   JUtils_SetLongField(env, dli, "physicalSectorSize",
                       (jlong)info->physicalSectorSize);
}


/*
 *-----------------------------------------------------------------------------
 *
 * JNISetConnectParams
 *
 *      Forward VixDiskLibConnectParams data from vixDiskLib to Java.
 *
 * Results:
 *      None.
 *
 * Side effects:
 *      None.
 *
 *-----------------------------------------------------------------------------
 */

static void
JNISetConnectParams(VixDiskLibConnectParams *params, // IN: params to forward
                    JNIEnv *env,                     // IN: Java Environment
                    jobject cp)                      // IN: Object to forward to
{
   jclass cls;

   // class for connect params object
   cls = (*env)->GetObjectClass(env, cp);

   JUtils_SetIntField(env, cp, "credType", (jint)params->credType);
   if (params->credType == VIXDISKLIB_CRED_UID) {
      JUtils_SetStringField(env, cp, "username", params->creds.uid.userName);
      JUtils_SetStringField(env, cp, "password", params->creds.uid.password);
   } else if (params->credType == VIXDISKLIB_CRED_SESSIONID) {
      JUtils_SetStringField(env, cp, "cookie", params->creds.sessionId.cookie);
      JUtils_SetStringField(env, cp, "username",
                            params->creds.sessionId.userName);
      JUtils_SetStringField(env, cp, "key", params->creds.sessionId.key);
   }

   JUtils_SetIntField(env, cp, "specType", (jint)params->specType);
   if (params->specType == VIXDISKLIB_SPEC_VMX) {
      JUtils_SetStringField(env, cp, "vmxSpec", params->vmxSpec);
   } else if (params->specType == VIXDISKLIB_SPEC_VSTORAGE_OBJECT) {
      JUtils_SetStringField(env, cp, "id", params->spec.vStorageObjSpec.id);
      JUtils_SetStringField(env, cp, "datastoreMoRef",
                            params->spec.vStorageObjSpec.datastoreMoRef);
      JUtils_SetStringField(env, cp, "ssId", params->spec.vStorageObjSpec.ssId);
   }
   /* else if (params->specType == VIXDISKLIB_SPEC_DATASTORE) {
     JUtils_SetStringField(env, cp, "datastoreMoRef",
                           params->spec.dsSpec.datastoreMoRef);
   }*/

   JUtils_SetStringField(env, cp, "serverName", params->serverName);
   JUtils_SetStringField(env, cp, "thumbPrint", params->thumbPrint);
   JUtils_SetIntField(env, cp, "port", (jint)params->port);
    JUtils_SetIntField(env, cp,"nfcHostPort",(jint)params->nfcHostPort); 

}


/*
 *-----------------------------------------------------------------------------
 *
 * JNIGetConnectParams --
 *
 *      Create a VixDiskLibConnectParams structure from a corresponding
 *      Java object.
 *
 * Results:
 *      Newly allocated VixDiskLibConnectParams object. Caller must free by
 *      calling JNIFreeConnectParams.
 *
 * Side effects:
 *      None
 *
 *-----------------------------------------------------------------------------
 */

static VixDiskLibConnectParams *
JNIGetConnectParams(JNIEnv *env,  // IN: Java Environment
                    jobject conn) // IN: ConnectParams Java object
{
   jclass cls;
   VixDiskLibConnectParams *params;

   cls = (*env)->GetObjectClass(env, conn);

   params = VixDiskLib_AllocateConnectParams();
   params->credType = JUtils_GetIntField(env, conn, "credType");
   // get spec
   params->specType = JUtils_GetIntField(env, conn, "specType");
   if (params->specType == VIXDISKLIB_SPEC_VMX) {
      params->vmxSpec = JUtils_GetStringField(env, conn, "vmxSpec");
   } else if (params->specType == VIXDISKLIB_SPEC_VSTORAGE_OBJECT) {
      params->spec.vStorageObjSpec.id = JUtils_GetStringField(env, conn, "id");
      params->spec.vStorageObjSpec.datastoreMoRef =
         JUtils_GetStringField(env, conn, "datastoreMoRef");
      params->spec.vStorageObjSpec.ssId =
         JUtils_GetStringField(env, conn, "ssId");
  /* } else if (params->specType == VIXDISKLIB_SPEC_DATASTORE) {
      params->spec.dsSpec.datastoreMoRef =
         JUtils_GetStringField(env, conn, "datastoreMoRef");*/
   } else {
      params->specType = VIXDISKLIB_SPEC_UNKNOWN;
   }

   params->serverName = JUtils_GetStringField(env, conn, "serverName");
   params->thumbPrint = JUtils_GetStringField(env, conn, "thumbPrint");
   if (params->credType == VIXDISKLIB_CRED_UID) {
      params->creds.uid.userName = JUtils_GetStringField(env, conn,
                                                         "username");
      params->creds.uid.password = JUtils_GetStringField(env, conn,
                                                         "password");
   } else if (params->credType == VIXDISKLIB_CRED_SESSIONID) {
      params->creds.sessionId.cookie = JUtils_GetStringField(env, conn,
                                                             "cookie");
      params->creds.sessionId.userName = JUtils_GetStringField(env, conn,
                                                             "username");
      params->creds.sessionId.key = JUtils_GetStringField(env, conn,
                                                             "key");
   }
   params->port = JUtils_GetIntField(env, conn, "port");
   params->nfcHostPort = JUtils_GetIntField(env, conn, "nfcHostPort");
   return params;
}


/*
 *-----------------------------------------------------------------------------
 *
 * JNIFreeConnectParams --
 *
 *      Release a VixDiskLibConnectParams object.
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
JNIFreeConnectParams(VixDiskLibConnectParams *params) // IN structure to free
{
   if (params == NULL) {
      return;
   }
   if (params->specType == VIXDISKLIB_SPEC_VMX) {
      free(params->vmxSpec);
   } else if (params->specType == VIXDISKLIB_SPEC_VSTORAGE_OBJECT) {
      free(params->spec.vStorageObjSpec.id);
      free(params->spec.vStorageObjSpec.datastoreMoRef);
      free(params->spec.vStorageObjSpec.ssId);
   }

   free(params->serverName);
   free(params->thumbPrint);
   if (params->credType == VIXDISKLIB_CRED_UID) {
      free(params->creds.uid.userName);
      free(params->creds.uid.password);
   } else if(params->credType == VIXDISKLIB_CRED_SESSIONID) {
      free(params->creds.sessionId.cookie);
      free(params->creds.sessionId.userName);
      free(params->creds.sessionId.key);
   }
   VixDiskLib_FreeConnectParams(params);
}


/*
 *-----------------------------------------------------------------------------
 *
 * JNIGetCreateParams --
 *
 *      Initialize a VixDiskLibCreateParams structure from the corresponding
 *      Java object.
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
JNIGetCreateParams(JNIEnv *env,                     // IN: Java Environment
                   jobject params,                  // IN: Java object
                   VixDiskLibCreateParams *cParams) // OUT: C structure o init.
{
   cParams->diskType = JUtils_GetIntField(env, params, "diskType");
   cParams->adapterType = JUtils_GetIntField(env, params, "adapterType");
   cParams->hwVersion = JUtils_GetIntField(env, params, "hwVersion");
   cParams->capacity = JUtils_GetLongField(env, params, "capacityInSectors");
   cParams->logicalSectorSize = JUtils_GetIntField(env, params,
                                                   "logicalSectorSize");
   cParams->physicalSectorSize = JUtils_GetIntField(env, params,
                                                    "physicalSectorSize");
}


/*
 *
 * JNI Interface implementation
 *
 */


/*
 *-----------------------------------------------------------------------------
 *
 * InitJNI --
 *
 *      JNI implementation for VixDiskLib_Init. See VixDiskLib docs for
 *      details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_InitJNI(JNIEnv *env,
                                          jobject obj,
                                          jint major,
                                          jint minor,
                                          jobject logger,
                                          jstring libDir)
{
   const char *cLibDir;
   jlong result;

   gLogger = JUtils_InitLogging(env, logger);

   cLibDir = GETSTRING(libDir);

   result = VixDiskLib_Init(major, minor, &JUtils_LogFunc, &JUtils_WarnFunc,
                            &JUtils_PanicFunc, cLibDir);

   FREESTRING(cLibDir, libDir);
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * InitExJNI --
 *
 *      JNI implementation for VixDiskLib_InitEx. See VixDiskLib docs for
 *      details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_InitExJNI(JNIEnv *env,
                                          jobject obj,
                                          jint major,
                                          jint minor,
                                          jobject logger,
                                          jstring libDir,
                                          jstring configFile)
{
   const char *cLibDir;
   const char *cConfigFile;
   jlong result;
 
   gLogger = JUtils_InitLogging(env, logger);

   cLibDir = GETSTRING(libDir);
   cConfigFile = GETSTRING(configFile);

   result = VixDiskLib_InitEx(major, minor, &JUtils_LogFunc, &JUtils_WarnFunc,
                            &JUtils_PanicFunc, cLibDir, cConfigFile);

   FREESTRING(cLibDir, libDir);
   FREESTRING(cConfigFile, configFile);
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * ExitJNI --
 *
 *      JNI implementation for VixDiskLib_Exit. See VixDiskLib docs for
 *      details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT void JNICALL
Java_com_vmware_jvix_jDiskLibImpl_ExitJNI(JNIEnv *env,
                                          jobject obj)
{
   VixDiskLib_Exit();
   JUtils_ExitLogging(gLogger);
}


/*
 *-----------------------------------------------------------------------------
 *
 * ListTransportModesJNI --
 *
 *      JNI implementation for VixDiskLib_ListTransportModes. See
 *      VixDiskLib docs for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jstring JNICALL
Java_com_vmware_jvix_jDiskLibImpl_ListTransportModesJNI(JNIEnv *env,
                                                        jobject obj)
{
   const char *modes;

   modes = VixDiskLib_ListTransportModes();
   if (modes == NULL) {
      modes = "";
   }
   return (*env)->NewStringUTF(env, modes);
}


/*
 *-----------------------------------------------------------------------------
 *
 * CleanupJNI --
 *
 *      JNI implementation for VixDiskLib_Cleanup. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_CleanupJNI(JNIEnv *env,
                                             jobject obj,
                                             jobject connection,
                                             jintArray aCleaned,
                                             jintArray aRemaining)
{
   uint32 numCleaned = 0, numRemaining = 0;
   VixError result;
   VixDiskLibConnectParams *params;
   jint cleaned, remaining;

   params = JNIGetConnectParams(env, connection);
   result = VixDiskLib_Cleanup(params, &numCleaned, &numRemaining);
   cleaned = numCleaned;
   remaining = numRemaining;
   (*env)->SetIntArrayRegion(env, aCleaned, 0, 1, &cleaned);
   (*env)->SetIntArrayRegion(env, aRemaining, 0, 1, &remaining);
   JNIFreeConnectParams(params);
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * ConnectJNI --
 *
 *      JNI implementation for VixDiskLib_Connect. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_ConnectJNI(JNIEnv *env,
                                             jobject obj,
                                             jobject connection,
                                             jlongArray handle)
{
   VixDiskLibConnectParams *params;
   VixDiskLibConnection conn = NULL;
   VixError result;
   jlong jout;

   params = JNIGetConnectParams(env, connection);
   if (handle == NULL) {
      // user wants to pass NULL, go for it
      result = VixDiskLib_Connect(params, NULL);
   } else {
      result = VixDiskLib_Connect(params, &conn);
      assert(sizeof(jout) >= sizeof(conn));
      jout = (jlong)(size_t)conn;
      (*env)->SetLongArrayRegion(env, handle, 0, 1, &jout);
   }
   JNIFreeConnectParams(params);
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * ConnectExJNI --
 *
 *      JNI implementation for VixDiskLib_ConnectEx. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_ConnectExJNI(JNIEnv *env,
                                               jobject obj,
                                               jobject connection,
                                               jboolean ro,
                                               jstring ssMoref,
                                               jstring modes,
                                               jlongArray handle)
{
   VixDiskLibConnectParams *params;
   VixDiskLibConnection conn = NULL;
   VixError result;
   jlong jout;
   const char *cssMoref, *cmodes;

   cssMoref = GETSTRING(ssMoref);
   cmodes = GETSTRING(modes);
   params = JNIGetConnectParams(env, connection);

   if (handle == NULL) {
      // user wants to pass NULL, go for it
      result = VixDiskLib_ConnectEx(params, ro, cssMoref, cmodes, NULL);
   } else {
      result = VixDiskLib_ConnectEx(params, ro, cssMoref, cmodes, &conn);
      assert(sizeof(jout) >= sizeof(conn));
      jout = (jlong)(size_t)conn;
      (*env)->SetLongArrayRegion(env, handle, 0, 1, &jout);
   }

   JNIFreeConnectParams(params);
   FREESTRING(cssMoref, ssMoref);
   FREESTRING(cmodes, modes);
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * DisconnectJNI --
 *
 *      JNI implementation for VixDiskLib_Disonnect. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_DisconnectJNI(JNIEnv *env,
                                                jobject obj,
                                                jlong handle)
{
   VixDiskLibConnection conn = (VixDiskLibConnection)(size_t)handle;
   return VixDiskLib_Disconnect(conn);
}


/*
 *-----------------------------------------------------------------------------
 *
 * PrepareForAccessJNI --
 *
 *      JNI implementation for VixDiskLib_PrepareForAccess. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_PrepareForAccessJNI(JNIEnv *env,
                                                      jobject obj,
                                                      jobject connection,
                                                      jstring identity)
{
   VixDiskLibConnectParams *params;
   VixError result;
   const char *cIdentity;

   cIdentity = GETSTRING(identity);
   params = JNIGetConnectParams(env, connection);

   result = VixDiskLib_PrepareForAccess(params, cIdentity);

   JNIFreeConnectParams(params);
   FREESTRING(cIdentity, identity);
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * EndAccessJNI --
 *
 *      JNI implementation for VixDiskLib_EndAccess. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_EndAccessJNI(JNIEnv *env,
                                               jobject obj,
                                               jobject connection,
                                               jstring identity)
{
   VixDiskLibConnectParams *params;
   VixError result;
   const char *cIdentity;

   cIdentity = GETSTRING(identity);
   params = JNIGetConnectParams(env, connection);

   result = VixDiskLib_EndAccess(params, cIdentity);

   JNIFreeConnectParams(params);
   FREESTRING(cIdentity, identity);
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 *  QueryAllocatedBlocksJNI --
 *
 *      JNI implementation for VixDiskLib_QueryAllocatedBlocks. See
 *      VixDiskLib docs for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_QueryAllocatedBlocksJNI(JNIEnv *env,
                                                          jobject obj,
                                                          jlong diskHandle,
                                                          jlong startSector,
                                                          jlong numSectors,
                                                          jlong chunkSize,
                                                          jobject dli)
{
   VixError result;
   uint32 i;
   VixDiskLibBlockList *blockList = NULL;
   VixDiskLibHandle cDiskHandle = (VixDiskLibHandle)(size_t)diskHandle;
   jclass blockClass = (*env)->FindClass(env, "com/vmware/jvix/jDiskLib$Block");
   jmethodID blockInit = (*env)->GetMethodID(env, blockClass, "<init>", "()V");
   jclass listClass = (*env)->FindClass(env, "java/util/List");
   jmethodID listClassAdd = (*env)->GetMethodID(env, listClass, "add",
                                                "(Ljava/lang/Object;)Z");

   result = VixDiskLib_QueryAllocatedBlocks(cDiskHandle, startSector,
                                            numSectors, chunkSize, &blockList);
   if (result == VIX_OK) {
      for (i = 0; i < blockList->numBlocks; i++) {
         jobject newBlock = (*env)->NewObject(env, blockClass, blockInit);
         JUtils_SetLongField(env, newBlock, "offset",
                             blockList->blocks[i].offset);
         JUtils_SetLongField(env, newBlock, "length",
                             blockList->blocks[i].length);
         (*env)->CallBooleanMethod(env, dli, listClassAdd, newBlock);
         (*env)->DeleteLocalRef(env, newBlock);
      }
   }

   VixDiskLib_FreeBlockList(blockList);
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * OpenJNI --
 *
 *      JNI implementation for VixDiskLib_Open. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_OpenJNI(JNIEnv *env,
                                          jobject obj,
                                          jlong handle,
                                          jstring path,
                                          jint flags,
                                          jlongArray diskHandle)
{
   VixDiskLibConnection conn = (VixDiskLibConnection)(size_t)handle;
   const char *cPath;
   VixError result;
   VixDiskLibHandle cDiskHandle = NULL;
   jlong jout;

   cPath = GETSTRING(path);

   if (diskHandle == NULL) {
      // User wants to pass NULL, for great justice. Let them.
      result = VixDiskLib_Open(conn, cPath, flags, (VixDiskLibHandle*) NULL);
   } else {
      result = VixDiskLib_Open(conn, cPath, flags, &cDiskHandle);
      jout = (jlong)(size_t)cDiskHandle;
      (*env)->SetLongArrayRegion(env, diskHandle, 0, 1, &jout);
   }

   FREESTRING(cPath, path);
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * CloseJNI --
 *
 *      JNI implementation for VixDiskLib_Close. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_CloseJNI(JNIEnv *env,
                                           jobject obj,
                                           jlong diskHandle)
{
   VixDiskLibHandle cDiskHandle = (VixDiskLibHandle)(size_t)diskHandle;
   return VixDiskLib_Close(cDiskHandle);
}


/*
 *-----------------------------------------------------------------------------
 *
 * UnlinkJNI --
 *
 *      JNI implementation for VixDiskLib_Unlink. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_UnlinkJNI(JNIEnv *env,
                                            jobject obj,
                                            jlong connHandle,
                                            jstring path)
{
   VixDiskLibConnection conn = (VixDiskLibConnection)(size_t)connHandle;
   const char *cPath;
   VixError result;

   cPath = GETSTRING(path);
   result = VixDiskLib_Unlink(conn, cPath);
   FREESTRING(cPath, path);
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * CreateJNI --
 *
 *      JNI implementation for VixDiskLib_Create. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_CreateJNI(JNIEnv *env,
                                            jobject obj,
                                            jlong connHandle,
                                            jstring path,
                                            jobject createParams,
                                            jobject progress)
{
   VixDiskLibConnection conn = (VixDiskLibConnection)(size_t)connHandle;
   const char *cPath;
   VixDiskLibCreateParams cParams;
   VixError result;

   cPath = GETSTRING(path);
   JNIGetCreateParams(env, createParams, &cParams);

   result = VixDiskLib_Create(conn, cPath, &cParams, &JUtils_ProgressFunc, progress);

   FREESTRING(cPath, path);
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * CreateChildJNI --
 *
 *      JNI implementation for VixDiskLib_CreateChild. See VixDiskLib
 *      docs for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_CreateChildJNI(JNIEnv *env,
                                                 jobject obj,
                                                 jlong diskHandle,
                                                 jstring childPath,
                                                 jint diskType,
                                                 jobject progress)
{
   VixDiskLibHandle cDiskHandle = (VixDiskLibHandle)(size_t)diskHandle;
   const char *cChildPath;
   VixError result;

   cChildPath =  GETSTRING(childPath);

   result = VixDiskLib_CreateChild(cDiskHandle, cChildPath, diskType,
				   &JUtils_ProgressFunc, progress);

   FREESTRING(cChildPath, childPath);
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * CloneJNI --
 *
 *      JNI implementation for VixDiskLib_Clone. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_CloneJNI(JNIEnv *env,
                                           jobject obj,
                                           jlong dstConn,
                                           jstring dstPath,
                                           jlong srcConn,
                                           jstring srcPath,
                                           jobject createParams,
                                           jobject progress,
                                           jboolean overwrite)
{
   VixDiskLibConnection cDstConn = (VixDiskLibConnection)(size_t)dstConn;
   VixDiskLibConnection cSrcConn = (VixDiskLibConnection)(size_t)srcConn;
   const char *cDstPath;
   const char *cSrcPath;
   VixDiskLibCreateParams cParams;
   VixError result;

   cDstPath = GETSTRING(dstPath);
   cSrcPath = GETSTRING(srcPath);

   JNIGetCreateParams(env, createParams, &cParams);
   result = VixDiskLib_Clone(cDstConn, cDstPath, cSrcConn, cSrcPath, &cParams,
			     &JUtils_ProgressFunc, progress, overwrite);

   FREESTRING(cDstPath, dstPath);
   FREESTRING(cSrcPath, srcPath);

   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * GrowJNI --
 *
 *      JNI implementation for VixDiskLib_Grow. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_GrowJNI(JNIEnv *env,
                                          jobject obj,
                                          jlong connHandle,
                                          jstring path,
                                          jlong capacityInSectors,
                                          jboolean updateGeometry,
                                          jobject progress)
{
   VixDiskLibConnection conn = (VixDiskLibConnection)(size_t)connHandle;
   const char *cPath;
   VixError result;

   cPath = GETSTRING(path);

   result = VixDiskLib_Grow(conn, cPath, capacityInSectors, updateGeometry,
			    &JUtils_ProgressFunc, progress);

   FREESTRING(cPath, path);
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * ShrinkJNI --
 *
 *      JNI implementation for VixDiskLib_Shrink. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_ShrinkJNI(JNIEnv *env,
                                            jobject obj,
                                            jlong diskHandle,
                                            jobject progress)
{
   VixDiskLibHandle cDiskHandle = (VixDiskLibHandle)(size_t)diskHandle;

   return VixDiskLib_Shrink(cDiskHandle, &JUtils_ProgressFunc, progress);
}


/*
 *-----------------------------------------------------------------------------
 *
 * DefragmentJNI --
 *
 *      JNI implementation for VixDiskLib_Defragment. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_DefragmentJNI(JNIEnv *env,
                                                jobject obj,
                                                jlong diskHandle,
                                                jobject progress)
{
   VixDiskLibHandle cDiskHandle = (VixDiskLibHandle)(size_t)diskHandle;

   return VixDiskLib_Defragment(cDiskHandle, &JUtils_ProgressFunc, progress);
}


/*
 *-----------------------------------------------------------------------------
 *
 * IsAttachPossibleJNI --
 *
 *      JNI implementation for VixDiskLib_IsAttachPossible. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_IsAttachPossibleJNI(JNIEnv *env,
                                                      jobject obj,
                                                      jlong parent,
                                                      jlong child)
{
   VixDiskLibHandle cParent = (VixDiskLibHandle)(size_t)parent;
   VixDiskLibHandle cChild = (VixDiskLibHandle)(size_t)child;

   return VixDiskLib_IsAttachPossible(cParent, cChild);
}


/*
 *-----------------------------------------------------------------------------
 *
 * AttachJNI --
 *
 *      JNI implementation for VixDiskLib_Attach. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_AttachJNI(JNIEnv *env,
                                            jobject obj,
                                            jlong parent,
                                            jlong child)
{
   VixDiskLibHandle cParent = (VixDiskLibHandle)(size_t)parent;
   VixDiskLibHandle cChild = (VixDiskLibHandle)(size_t)child;

   return VixDiskLib_Attach(cParent, cChild);
}


/*
 *-----------------------------------------------------------------------------
 *
 * ReadJNI --
 *
 *      JNI implementation for VixDiskLib_Read. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_ReadJNI(JNIEnv *env,
                                          jobject obj,
                                          jlong diskHandle,
                                          jlong startSector,
                                          jlong numSectors,
                                          jbyteArray buf)
{
   VixDiskLibHandle cDiskHandle = (VixDiskLibHandle)(size_t)diskHandle;
   jbyte *jBuf;
   VixError result;

   jBuf = (*env)->GetByteArrayElements(env, buf, NULL);

   result = VixDiskLib_Read(cDiskHandle, startSector,
                            numSectors, (uint8*)jBuf);
   (*env)->ReleaseByteArrayElements(env, buf, jBuf, 0);
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * BufferReadJNI --
 *
 *      JNI implementation for VixDiskLib_Read. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_BufferReadJNI(JNIEnv *env,
                                                jobject obj,
                                                jlong diskHandle,
                                                jlong startSector,
                                                jlong numSectors,
                                                jobject jBuf)
{
   VixDiskLibHandle cDiskHandle = (VixDiskLibHandle)(size_t)diskHandle;
   jbyte *data = NULL;
   VixError result;

   if (jBuf) {
      data = (*env)->GetDirectBufferAddress(env, jBuf);
   }

   result = VixDiskLib_Read(cDiskHandle, startSector,
                            numSectors, (uint8*)data);
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * ReadAsyncJNI --
 *
 *      JNI implementation for VixDiskLib_ReadAsync. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_ReadAsyncJNI(JNIEnv *env,
                                               jobject obj,
                                               jlong diskHandle,
                                               jlong startSector,
                                               jobject buffer,
                                               jint sectorCount,
                                               jobject callbackObj)
{
   VixDiskLibHandle cDiskHandle = (VixDiskLibHandle)(size_t)diskHandle;
   jUtilsAsyncCallback *asyncCallback = NULL;
   VixDiskLibCompletionCB completionCB = NULL;
   void *data = NULL;
   VixError result;

   if (callbackObj) {
      asyncCallback = jUtils_CreateAsyncCallback(env, callbackObj);
      completionCB = (VixDiskLibCompletionCB)jUtilsCompletionCB;
   }

   if (buffer) {
      data = (*env)->GetDirectBufferAddress(env, buffer);
   }

   result = VixDiskLib_ReadAsync(cDiskHandle,
                                 startSector,
                                 sectorCount,
                                 (uint8*)data,
                                 completionCB,
                                 (void*)asyncCallback);

   return result;
}

/*
 *-----------------------------------------------------------------------------
 *
 * WriteJNI --
 *
 *      JNI implementation for VixDiskLib_Write. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_WriteJNI(JNIEnv *env,
                                           jobject obj,
                                           jlong diskHandle,
                                           jlong startSector,
                                           jlong numSectors,
                                           jbyteArray buf)
{
   VixDiskLibHandle cDiskHandle = (VixDiskLibHandle)(size_t)diskHandle;
   jbyte *jBuf;
   VixError result;

   jBuf = (*env)->GetByteArrayElements(env, buf, NULL);

   result = VixDiskLib_Write(cDiskHandle, startSector,
                             numSectors, (uint8*)jBuf);

   (*env)->ReleaseByteArrayElements(env, buf, jBuf, JNI_ABORT);
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * BufferWriteJNI --
 *
 *      JNI implementation for VixDiskLib_Write. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_BufferWriteJNI(JNIEnv *env,
                                                 jobject obj,
                                                 jlong diskHandle,
                                                 jlong startSector,
                                                 jlong numSectors,
                                                 jobject jBuf)
{
   VixDiskLibHandle cDiskHandle = (VixDiskLibHandle)(size_t)diskHandle;
   jbyte *data = NULL;
   VixError result;

   if (jBuf) {
      data = (*env)->GetDirectBufferAddress(env, jBuf);
   }

   result = VixDiskLib_Write(cDiskHandle, startSector,
                             numSectors, (uint8*)data);

   return result;
}

/*
 *-----------------------------------------------------------------------------
 *
 * WriteAsyncJNI --
 *
 *      JNI implementation for VixDiskLib_WriteAsync. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_WriteAsyncJNI(JNIEnv *env,
                                                jobject obj,
                                                jlong diskHandle,
                                                jlong startSector,
                                                jobject buffer,
                                                jint sectorCount,
                                                jobject callbackObj)
{
   VixDiskLibHandle cDiskHandle = (VixDiskLibHandle)(size_t)diskHandle;
   jUtilsAsyncCallback *asyncCallback = NULL;
   VixDiskLibCompletionCB completionCB = NULL;
   void *data = NULL;
   VixError result;

   if (callbackObj) {
      asyncCallback = jUtils_CreateAsyncCallback(env, callbackObj);
      completionCB = (VixDiskLibCompletionCB)jUtilsCompletionCB;
   }

   if (buffer) {
      data = (*env)->GetDirectBufferAddress(env, buffer);
   }

   result = VixDiskLib_WriteAsync(cDiskHandle,
                                  startSector,
                                  sectorCount,
                                  (uint8*)data,
                                  completionCB,
                                  (void*)asyncCallback);

   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 * WaitJNI --
 *
 *      JNI implementation for VixDiskLib_Flush. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_WaitJNI(JNIEnv *env,
                                           jobject obj,
                                           jlong diskHandle)
{
   VixDiskLibHandle cDiskHandle = (VixDiskLibHandle)(size_t)diskHandle;
   return VixDiskLib_Wait(cDiskHandle);
}

/*
 *-----------------------------------------------------------------------------
 *
 * FlushJNI --
 *
 *      JNI implementation for VixDiskLib_Flush. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_FlushJNI(JNIEnv *env,
                                           jobject obj,
                                           jlong diskHandle)
{
   VixDiskLibHandle cDiskHandle = (VixDiskLibHandle)(size_t)diskHandle;
   return VixDiskLib_Flush(cDiskHandle);
}


/*
 *-----------------------------------------------------------------------------
 *
 * GetMetadataKeysJNI --
 *
 *      JNI implementation for VixDiskLib_GetMetadataKeys. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jobjectArray JNICALL
Java_com_vmware_jvix_jDiskLibImpl_GetMetadataKeysJNI(JNIEnv *env,
                                                     jobject obj,
                                                     jlong diskHandle)
{
   VixDiskLibHandle cDiskHandle = (VixDiskLibHandle)(size_t)diskHandle;
   size_t required;
   VixError err;
   jobjectArray result = NULL;
   char *keys, *hlp;
   int i;
   jclass strClass;

   strClass =  (*env)->FindClass(env, "java/lang/String");

   err = VixDiskLib_GetMetadataKeys(cDiskHandle, NULL, 0, &required);
   if (err != VIX_E_BUFFER_TOOSMALL) {
      JUtils_Log("GetMetaDataKeys: Not finding any keys!\n");
      result = (*env)->NewObjectArray(env, 0, strClass, 0);
      goto out;
   }

   keys = malloc(required);
   assert(keys != NULL);
   err = VixDiskLib_GetMetadataKeys(cDiskHandle, keys, required, NULL);
   if (err != VIX_OK) {
      JUtils_Log("GetMetaDataKeys: Cannot fetch keys!\n");
      result = (*env)->NewObjectArray(env, 0, strClass, 0);
      goto out;
   }

   /* Count metadata keys */
   hlp = keys;
   i = 0;
   while (hlp[0] != '\0') {
      i += 1;
      hlp += strlen(hlp) + 1;
   }

   /* Create object array large enough to hold all strings */
   result = (*env)->NewObjectArray(env, i, strClass, 0);

   /* Fill object array. */
   hlp = keys;
   i = 0;
   while (hlp[0] != '\0') {
      jstring str;
      str = (*env)->NewStringUTF(env, hlp);
      (*env)->SetObjectArrayElement(env, result, i, str);
      i += 1;
      hlp += strlen(hlp) + 1;
   }
   free(keys);

 out:
   (*env)->DeleteLocalRef(env, strClass);
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 *  ReadMetadataJNI --
 *
 *      JNI implementation for VixDiskLib_ReadMetadata. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_ReadMetadataJNI(JNIEnv *env,
                                                  jobject obj,
                                                  jlong diskHandle,
                                                  jstring key,
                                                  jobject valOut)
{
   VixDiskLibHandle cDiskHandle = (VixDiskLibHandle)(size_t)diskHandle;
   const char *cKey;
   size_t required;
   VixError err;
   char *val = NULL;
   jstring result;
   jclass cls;
   jmethodID appendMid;

   cKey = GETSTRING(key);
   err = VixDiskLib_ReadMetadata(cDiskHandle, cKey, NULL, 0, &required);
   if (err != VIX_E_BUFFER_TOOSMALL) {
      JUtils_Log("ReadMetadataEx: Cannot get meta for key %s, err (%d).\n",
                 cKey, err);
      goto out;
   }

   val = malloc(required);
   assert(val != NULL);

   err = VixDiskLib_ReadMetadata(cDiskHandle, cKey, val, required, NULL);
   if (err != VIX_OK) {
      JUtils_Log("ReadMetadataEx: Cannot get meta for key %s, err (%d).\n",
                 cKey, err);
      goto out;
   }

   result = (*env)->NewStringUTF(env, val);
   cls = (*env)->GetObjectClass(env, valOut);
   appendMid = (*env)->GetMethodID(env, cls, "append",
                     "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
   if (appendMid) {
      (*env)->CallObjectMethod(env, valOut, appendMid, result);
   }

 out:
   FREESTRING(cKey, key);
   free(val);
   return err;
}


/*
 *-----------------------------------------------------------------------------
 *
 *  WriteMetadataJNI --
 *
 *      JNI implementation for VixDiskLib_WriteMetadata. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_WriteMetadataJNI(JNIEnv *env,
                                                   jobject obj,
                                                   jlong diskHandle,
                                                   jstring key,
                                                   jstring val)
{
   VixDiskLibHandle cDiskHandle = (VixDiskLibHandle)(size_t)diskHandle;
   const char *cKey;
   const char *cVal;
   VixError result;

   cKey = GETSTRING(key);
   cVal = GETSTRING(val);

   result = VixDiskLib_WriteMetadata(cDiskHandle, cKey, cVal);

   FREESTRING(cKey, key);
   FREESTRING(cVal, val);

   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 *  GetInfoJNI --
 *
 *      JNI implementation for VixDiskLib_GetInfo. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_GetInfoJNI(JNIEnv *env,
                                             jobject obj,
                                             jlong diskHandle,
                                             jobject dli)
{
   VixDiskLibInfo *info = NULL;
   VixDiskLibHandle cDiskHandle = (VixDiskLibHandle)(size_t)diskHandle;
   VixError result;

   if (dli == NULL) {
      result = VixDiskLib_GetInfo(cDiskHandle, NULL);
   } else {
      result = VixDiskLib_GetInfo(cDiskHandle, &info);
   }

   if (info != NULL) {
      JNISetDiskLibInfo(info, env, dli);
      VixDiskLib_FreeInfo(info);
   }
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 *  GetTransportModeJNI --
 *
 *      JNI implementation for VixDiskLib_GetTransportMode. See
 *      VixDiskLib docs for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jstring JNICALL
Java_com_vmware_jvix_jDiskLibImpl_GetTransportModeJNI(JNIEnv *env,
                                                      jobject obj,
                                                      jlong diskHandle)
{
   VixDiskLibHandle cDiskHandle = (VixDiskLibHandle)(size_t)diskHandle;
   const char *mode;

   mode = VixDiskLib_GetTransportMode(cDiskHandle);
   return (*env)->NewStringUTF(env, mode);
}


/*
 *-----------------------------------------------------------------------------
 *
 *  GetErrorTextJNI --
 *
 *      JNI implementation for VixDiskLib_GetErrorText. See VixDiskLib
 *      docs for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jstring JNICALL
Java_com_vmware_jvix_jDiskLibImpl_GetErrorTextJNI(JNIEnv *env,
                                                  jobject obj,
                                                  jlong error,
                                                  jstring locale)
{
   char *errTxt;
   const char *cLocale;
   jstring result;

   cLocale =  GETSTRING(locale);

   errTxt = VixDiskLib_GetErrorText(error, cLocale);
   if (errTxt != NULL) {
      result = (*env)->NewStringUTF(env, errTxt);
      VixDiskLib_FreeErrorText(errTxt);
   } else {
      result = (*env)->NewStringUTF(env, "");
   }

   FREESTRING(cLocale, locale);
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 *  RenameJNI --
 *
 *      JNI implementation for VixDiskLib_Rename. See VixDiskLib docs
 *      for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_RenameJNI(JNIEnv *env,
                                            jobject obj,
                                            jstring src,
                                            jstring dst)
{
   const char *cSrc;
   const char *cDst;
   VixError result;

   cSrc = GETSTRING(src);
   cDst = GETSTRING(dst);

   result = VixDiskLib_Rename(cSrc, cDst);

   FREESTRING(cSrc, src);
   FREESTRING(cDst, dst);
   return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 *  SpaceNeededForCloneJNI --
 *
 *      JNI implementation for VixDiskLib_SpaceNeedeForClone. See
 *      VixDiskLib docs for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_SpaceNeededForCloneJNI(JNIEnv *env,
                                                         jobject obj,
                                                         jlong diskHandle,
                                                         jint diskType,
                                                         jlongArray needed)
{
    VixDiskLibHandle cDiskHandle = (VixDiskLibHandle)(size_t)diskHandle;
    uint64 spaceNeeded;
    jlong jout = 0;
    VixError result;

    if (needed == NULL) {
       result = VixDiskLib_SpaceNeededForClone(cDiskHandle, diskType, NULL);
    } else {
       result = VixDiskLib_SpaceNeededForClone(cDiskHandle, diskType, &spaceNeeded);
       if (result == VIX_OK) {
          jout = (jlong)spaceNeeded;
       }

      (*env)->SetLongArrayRegion(env, needed, 0, 1, &jout);
    }
    return result;
}


/*
 *-----------------------------------------------------------------------------
 *
 *  CheckRepairJNI --
 *
 *      JNI implementation for VixDiskLib_CheckRepair. See
 *      VixDiskLib docs for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_CheckRepairJNI(JNIEnv *env,
                                                 jobject obj,
                                                 jlong connHandle,
                                                 jstring path,
                                                 jboolean repair)
{
   VixDiskLibConnection conn = (VixDiskLibConnection)(size_t)connHandle;
   const char *cPath;
   VixError result;

   cPath = GETSTRING(path);
   result = VixDiskLib_CheckRepair(conn, cPath, repair);
   FREESTRING(cPath, path);
   return result;
}

/*
 *-----------------------------------------------------------------------------
 *
 * Perturb_Enable --
 *
 *     JNI implementation for Fault Injection control of
 *     PerturbEnable.  The arguments are:
 *
 *     fName - Pointer to the name of the function to be replaced.
 *     enable - zero = disable, 1 = enable.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT void JNICALL
Java_com_vmware_jvix_jDiskLibImpl_PerturbEnableJNI(JNIEnv *env,
                                                    jobject obj,
                                                    jstring fName,
                                                    jint enable)
{
   int enableIt = (int) enable;
   const char *funcName = GETSTRING(fName);

   Perturb_Enable(funcName, enableIt);
   FREESTRING(funcName, fName);
}

/*
 *-----------------------------------------------------------------------------
 *
 * VixDiskLib_SetInjectedFault --
 *
 *     JNI implementation for control of injected faults.  The args are:
 *
 *     faultID    - One of the members of enum diskLibFaultInjection.
 *     enabled    - Zero is disable, one is enable.
 *     faultError - The error value to be returned from the fault point.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_SetInjectedFaultJNI(JNIEnv *env,
                                                       jobject obj,
                                                       jint faultID,
                                                       jint enabled,
                                                       jint faultError)
{
   int faultIDLocal = (int) faultID;
   int enabledLocal = (int) enabled;
   int faultErrorLocal = (int) faultError;

   return VixDiskLib_SetInjectedFault(faultIDLocal, enabledLocal,
                                      faultErrorLocal);
}


/*
 *-----------------------------------------------------------------------------
 *
 * AllocateBufferJNI --
 *
 *      JNI implementation for allcating memeory with aligned address
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jobject JNICALL
Java_com_vmware_jvix_jDiskLibImpl_AllocateBufferJNI(JNIEnv *env,
                                                    jobject obj,
                                                    jint size,
                                                    jint alignment)
{
   jbyte *buf = NULL;
   jobject ret = NULL;
   int err;

#ifdef _WIN32
   buf = _aligned_malloc(size, alignment);
   _get_errno(&err);
#else
   err = posix_memalign((void**) &buf, alignment, size);
#endif

   if (buf != NULL) {
      ret = (*env)->NewDirectByteBuffer(env, (void*) buf, (jlong)size);
   } else {
      JUtils_Log("AllocateBufferJNI: error no %d!\n", err);
   }
   return ret;
}


/*
 *-----------------------------------------------------------------------------
 *
 * FreeBufferJNI --
 *
 *      JNI implementation for freeing memory allocated before.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT void JNICALL
Java_com_vmware_jvix_jDiskLibImpl_FreeBufferJNI(JNIEnv *env,
                                                jobject obj,
                                                jobject jbuf)
{
   jbyte *data = NULL;
   if (jbuf) {
      data = (*env)->GetDirectBufferAddress(env, jbuf);
#ifdef _WIN32
      _aligned_free(data);
#else
      free(data);
#endif
   }
}


/*
 *-----------------------------------------------------------------------------
 *
 * GetConnectParamsJNI --
 *
 *      JNI implementation for VixDiskLib_GetConnectParams.
 *      See VixDiskLib docs for details.
 *
 *-----------------------------------------------------------------------------
 */

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jDiskLibImpl_GetConnectParamsJNI(JNIEnv *env,
                                                      jobject obj,
                                                      jlong connHandle,
                                                      jobject cp)
{
   VixDiskLibConnectParams *params = NULL;
   VixDiskLibConnection conn = (VixDiskLibConnection)(size_t)connHandle;
   VixError result;

   if (cp == NULL) {
      result = VixDiskLib_GetConnectParams(conn, NULL);
   } else {
      result = VixDiskLib_GetConnectParams(conn, &params);
   }

   if (params != NULL) {
      JNISetConnectParams(params, env, cp);
      VixDiskLib_FreeConnectParams(params);
   }
   return result;
}

