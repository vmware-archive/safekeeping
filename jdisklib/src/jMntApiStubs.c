/* **************************************************************************
 * Copyright 2008 VMware, Inc.  All rights reserved. -- VMware Confidential
 * **************************************************************************/

/*
 *  jMntApiStubs.c
 *
 *    Stubbed out JNI bindings for vixMntApi. We use this on Linux until
 *    VixmntApi is available on this platform.
 */

#include "jMntApi.h"
#include "vixDiskLib.h"

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jMntApiImpl_InitJNI (JNIEnv *env,
                                          jobject obj,
                                          jint major,
                                          jint minor,
                                          jobject logger,
                                          jstring libDir,
                                          jstring tmpDir)
{
   return VIX_E_FAIL;
}

JNIEXPORT void JNICALL
Java_com_vmware_jvix_jMntApiImpl_ExitJNI(JNIEnv *env,
                                         jobject obj)
{}

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jMntApiImpl_OpenDiskSetJNI(JNIEnv *env,
                                                jobject obj,
                                                jlongArray diskHandles,
                                                jint openMode,
                                                jlongArray diskSet)     // OUT
{
   return VIX_E_FAIL;
}

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jMntApiImpl_CloseDiskSetJNI (JNIEnv *env,
                                                  jobject obj,
                                                  jlong diskSet)
{
   return VIX_E_FAIL;
}

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jMntApiImpl_GetVolumeHandlesJNI(JNIEnv *env,
                                                     jobject obj,
                                                     jlong diskSet,
                                                     jobject volumes) // OUT
{
   return VIX_E_FAIL;
}

JNIEXPORT void JNICALL
Java_com_vmware_jvix_jMntApiImpl_FreeVolumeHandlesJNI(JNIEnv *env,
                                                      jobject obj,
                                                      jlong volumes)
{}

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jMntApiImpl_GetOsInfoJNI(JNIEnv *env,
                                              jobject obj,
                                              jlong diskSet,
                                              jobject osInfo)
{
   return VIX_E_FAIL;
}

JNIEXPORT void JNICALL
Java_com_vmware_jvix_jMntApiImpl_FreeOsInfoJNI(JNIEnv *env,
                                               jobject obj,
                                               jlong osInfo)
{}

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jMntApiImpl_MountVolumeJNI(JNIEnv *env,
                                                jobject obj,
                                                jlong volumeHandle,
                                                jboolean readOnly)
{
   return VIX_E_FAIL;
}

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jMntApiImpl_DismountVolumeJNI(JNIEnv *env,
                                                   jobject obj,
                                                   jlong volumeHandle,
                                                   jboolean force)
{
   return VIX_E_FAIL;
}

JNIEXPORT jlong JNICALL
Java_com_vmware_jvix_jMntApiImpl_GetVolumeInfoJNI(JNIEnv *env,
                                                  jobject obj,
                                                  jlong volumeHandle,
                                                  jobject volumeInfo)
{
   return VIX_E_FAIL;
}

JNIEXPORT void JNICALL
Java_com_vmware_jvix_jMntApiImpl_FreeVolumeInfoJNI(JNIEnv *env,
                                                   jobject obj,
                                                   jlong volumeInfo)
{}
