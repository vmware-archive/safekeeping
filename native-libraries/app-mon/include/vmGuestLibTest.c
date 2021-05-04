/* **********************************************************
 * Copyright 2005-2016,2019 VMware, Inc.  All rights reserved. -- VMware Confidential
 * **********************************************************/

/*
 * vmGuestlibTest.c
 *
 * Sample/test code for the VMware Guest API.
 *
 * This can be compiled for Linux by doing something like the following:
 *
 * gcc -g -o vmguestlibtest -ldl -I<path to VMware headers> vmGuestLibTest.c
 *
 */


#include <stdlib.h>
#include <stdarg.h>
#include <stdio.h>
#ifdef _WIN32
#   include <windows.h>
#else
#   include <unistd.h>
#   include <dlfcn.h>
#endif

#include "vmGuestLib.h"



#ifdef _WIN32
#define SLEEP(x) Sleep(x * 1000)
#else
#define SLEEP(x) sleep(x)
#endif


static Bool done = FALSE;


/* Functions to dynamically load from the GuestLib library. */
char const * (*GuestLib_GetErrorText)(VMGuestLibError);
VMGuestLibError (*GuestLib_OpenHandle)(VMGuestLibHandle*);
VMGuestLibError (*GuestLib_CloseHandle)(VMGuestLibHandle);
VMGuestLibError (*GuestLib_UpdateInfo)(VMGuestLibHandle handle);
VMGuestLibError (*GuestLib_GetSessionId)(VMGuestLibHandle handle,
                                         VMSessionId *id);
VMGuestLibError (*GuestLib_GetCpuReservationMHz)(VMGuestLibHandle handle,
                                                 uint32 *cpuReservationMHz);
VMGuestLibError (*GuestLib_GetCpuLimitMHz)(VMGuestLibHandle handle, uint32 *cpuLimitMHz);
VMGuestLibError (*GuestLib_GetCpuShares)(VMGuestLibHandle handle, uint32 *cpuShares);
VMGuestLibError (*GuestLib_GetCpuUsedMs)(VMGuestLibHandle handle, uint64 *cpuUsedMs);
VMGuestLibError (*GuestLib_GetHostProcessorSpeed)(VMGuestLibHandle handle, uint32 *mhz);
VMGuestLibError (*GuestLib_GetMemReservationMB)(VMGuestLibHandle handle,
                                                uint32 *memReservationMB);
VMGuestLibError (*GuestLib_GetMemLimitMB)(VMGuestLibHandle handle, uint32 *memLimitMB);
VMGuestLibError (*GuestLib_GetMemShares)(VMGuestLibHandle handle, uint32 *memShares);
VMGuestLibError (*GuestLib_GetMemShares64)(VMGuestLibHandle handle, uint64 *memShares64);
VMGuestLibError (*GuestLib_GetMemMappedMB)(VMGuestLibHandle handle,
                                           uint32 *memMappedMB);
VMGuestLibError (*GuestLib_GetMemActiveMB)(VMGuestLibHandle handle, uint32 *memActiveMB);
VMGuestLibError (*GuestLib_GetMemOverheadMB)(VMGuestLibHandle handle,
                                             uint32 *memOverheadMB);
VMGuestLibError (*GuestLib_GetMemBalloonedMB)(VMGuestLibHandle handle,
                                              uint32 *memBalloonedMB);
VMGuestLibError (*GuestLib_GetMemSwappedMB)(VMGuestLibHandle handle,
                                            uint32 *memSwappedMB);
VMGuestLibError (*GuestLib_GetMemSharedMB)(VMGuestLibHandle handle,
                                           uint32 *memSharedMB);
VMGuestLibError (*GuestLib_GetMemSharedSavedMB)(VMGuestLibHandle handle,
                                                uint32 *memSharedSavedMB);
VMGuestLibError (*GuestLib_GetMemUsedMB)(VMGuestLibHandle handle,
                                         uint32 *memUsedMB);
VMGuestLibError (*GuestLib_GetElapsedMs)(VMGuestLibHandle handle, uint64 *elapsedMs);
VMGuestLibError (*GuestLib_GetResourcePoolPath)(VMGuestLibHandle handle,
                                                size_t *bufferSize,
                                                char *pathBuffer);
VMGuestLibError (*GuestLib_GetCpuStolenMs)(VMGuestLibHandle handle,
                                           uint64 *cpuStolenMs);
VMGuestLibError (*GuestLib_GetMemTargetSizeMB)(VMGuestLibHandle handle,
                                               uint64 *memTargetSizeMB);
VMGuestLibError (*GuestLib_GetHostNumCpuCores)(VMGuestLibHandle handle,
                                               uint32 *hostNumCpuCores);
VMGuestLibError (*GuestLib_GetHostCpuUsedMs)(VMGuestLibHandle handle,
                                             uint64 *hostCpuUsedMs);
VMGuestLibError (*GuestLib_GetHostMemSwappedMB)(VMGuestLibHandle handle,
                                                uint64 *hostMemSwappedMB);
VMGuestLibError (*GuestLib_GetHostMemSharedMB)(VMGuestLibHandle handle,
                                               uint64 *hostMemSharedMB);
VMGuestLibError (*GuestLib_GetHostMemUsedMB)(VMGuestLibHandle handle,
                                             uint64 *hostMemUsedMB);
VMGuestLibError (*GuestLib_GetHostMemPhysMB)(VMGuestLibHandle handle,
                                             uint64 *hostMemPhysMB);
VMGuestLibError (*GuestLib_GetHostMemPhysFreeMB)(VMGuestLibHandle handle,
                                                 uint64 *hostMemPhysFreeMB);
VMGuestLibError (*GuestLib_GetHostMemKernOvhdMB)(VMGuestLibHandle handle,
                                                 uint64 *hostMemKernOvhdMB);
VMGuestLibError (*GuestLib_GetHostMemMappedMB)(VMGuestLibHandle handle,
                                               uint64 *hostMemMappedMB);
VMGuestLibError (*GuestLib_GetHostMemUnmappedMB)(VMGuestLibHandle handle,
                                                 uint64 *hostMemUnmappedMB);
/*
 * Handle for use with shared library.
 */

#ifdef _WIN32
HMODULE dlHandle = NULL;
#else
void *dlHandle = NULL;
#endif

/*
 * GuestLib handle.
 */
VMGuestLibHandle glHandle;



/*
 * Macro to load a single GuestLib function from the shared library.
 */

#ifdef _WIN32
#define LOAD_ONE_FUNC(funcname)                                      \
   do {                                                              \
      (FARPROC)funcname = GetProcAddress(dlHandle, "VM" #funcname);  \
      if (funcname == NULL) {                                        \
         error = GetLastError();                                     \
         printf("Failed to load \'%s\': %d\n",                       \
                #funcname, error);                                   \
         return FALSE;                                               \
      }                                                              \
   } while (0)

#else

#define LOAD_ONE_FUNC(funcname)                           \
   do {                                                   \
      funcname = dlsym(dlHandle, "VM" #funcname);         \
      if ((dlErrStr = dlerror()) != NULL) {               \
         printf("Failed to load \'%s\': \'%s\'\n",        \
                #funcname, dlErrStr);                     \
         return FALSE;                                    \
      }                                                   \
   } while (0)

#endif


/*
 *-----------------------------------------------------------------------------
 *
 * LoadFunctions --
 *
 *      Load the functions from the shared library.
 *
 * Results:
 *      TRUE on success
 *      FALSE on failure
 *
 * Side effects:
 *      None
 *
 *-----------------------------------------------------------------------------
 */

Bool
LoadFunctions(void)
{
   /*
    * First, try to load the shared library.
    */
#ifdef _WIN32
   DWORD error;

   dlHandle = LoadLibrary("vmGuestLib.dll");
   if (!dlHandle) {
      error = GetLastError();
      printf("LoadLibrary failed: %d\n", error);
      return FALSE;
   }
#else
   char const *dlErrStr;

   dlHandle = dlopen("libvmGuestLib.so", RTLD_NOW);
   if (!dlHandle) {
      dlErrStr = dlerror();
      printf("dlopen failed: \'%s\'\n", dlErrStr);
      return FALSE;
   }
#endif

   /* Load all the individual library functions. */
   LOAD_ONE_FUNC(GuestLib_GetErrorText);
   LOAD_ONE_FUNC(GuestLib_OpenHandle);
   LOAD_ONE_FUNC(GuestLib_CloseHandle);
   LOAD_ONE_FUNC(GuestLib_UpdateInfo);
   LOAD_ONE_FUNC(GuestLib_GetSessionId);
   LOAD_ONE_FUNC(GuestLib_GetCpuReservationMHz);
   LOAD_ONE_FUNC(GuestLib_GetCpuLimitMHz);
   LOAD_ONE_FUNC(GuestLib_GetCpuShares);
   LOAD_ONE_FUNC(GuestLib_GetCpuUsedMs);
   LOAD_ONE_FUNC(GuestLib_GetHostProcessorSpeed);
   LOAD_ONE_FUNC(GuestLib_GetMemReservationMB);
   LOAD_ONE_FUNC(GuestLib_GetMemLimitMB);
   LOAD_ONE_FUNC(GuestLib_GetMemShares);
   LOAD_ONE_FUNC(GuestLib_GetMemShares64);
   LOAD_ONE_FUNC(GuestLib_GetMemMappedMB);
   LOAD_ONE_FUNC(GuestLib_GetMemActiveMB);
   LOAD_ONE_FUNC(GuestLib_GetMemOverheadMB);
   LOAD_ONE_FUNC(GuestLib_GetMemBalloonedMB);
   LOAD_ONE_FUNC(GuestLib_GetMemSwappedMB);
   LOAD_ONE_FUNC(GuestLib_GetMemSharedMB);
   LOAD_ONE_FUNC(GuestLib_GetMemSharedSavedMB);
   LOAD_ONE_FUNC(GuestLib_GetMemUsedMB);
   LOAD_ONE_FUNC(GuestLib_GetElapsedMs);
   LOAD_ONE_FUNC(GuestLib_GetResourcePoolPath);
   LOAD_ONE_FUNC(GuestLib_GetCpuStolenMs);
   LOAD_ONE_FUNC(GuestLib_GetMemTargetSizeMB);
   LOAD_ONE_FUNC(GuestLib_GetHostNumCpuCores);
   LOAD_ONE_FUNC(GuestLib_GetHostCpuUsedMs);
   LOAD_ONE_FUNC(GuestLib_GetHostMemSwappedMB);
   LOAD_ONE_FUNC(GuestLib_GetHostMemSharedMB);
   LOAD_ONE_FUNC(GuestLib_GetHostMemUsedMB);
   LOAD_ONE_FUNC(GuestLib_GetHostMemPhysMB);
   LOAD_ONE_FUNC(GuestLib_GetHostMemPhysFreeMB);
   LOAD_ONE_FUNC(GuestLib_GetHostMemKernOvhdMB);
   LOAD_ONE_FUNC(GuestLib_GetHostMemMappedMB);
   LOAD_ONE_FUNC(GuestLib_GetHostMemUnmappedMB);

   return TRUE;
}


/*
 *-----------------------------------------------------------------------------
 *
 * TestGuestLib --
 *
 *      Test the VMware Guest API.
 *
 * Results:
 *      TRUE on success
 *      FALSE on failure
 *
 * Side effects:
 *      None
 *
 *-----------------------------------------------------------------------------
 */

Bool
TestGuestLib(void)
{
   VMGuestLibError glError;
   Bool success = TRUE;
   uint32 cpuReservationMHz = 0;
   uint32 cpuLimitMHz = 0;
   uint32 cpuShares = 0;
   uint64 cpuUsedMs = 0;
   uint32 hostMHz = 0;
   uint32 memReservationMB = 0;
   uint32 memLimitMB = 0;
   uint32 memShares = 0;
   uint64 memShares64 = 0;
   uint32 memMappedMB = 0;
   uint32 memActiveMB = 0;
   uint32 memOverheadMB = 0;
   uint32 memBalloonedMB = 0;
   uint32 memSwappedMB = 0;
   uint32 memSharedMB = 0;
   uint32 memSharedSavedMB = 0;
   uint32 memUsedMB = 0;
   uint64 elapsedMs = 0;
   uint64 cpuStolenMs = 0;
   uint64 memTargetSizeMB = 0;
   uint32 hostNumCpuCores = 0;
   uint64 hostCpuUsedMs = 0;
   uint64 hostMemSwappedMB = 0;
   uint64 hostMemSharedMB = 0;
   uint64 hostMemUsedMB = 0;
   uint64 hostMemPhysMB = 0;
   uint64 hostMemPhysFreeMB = 0;
   uint64 hostMemKernOvhdMB = 0;
   uint64 hostMemMappedMB = 0;
   uint64 hostMemUnmappedMB = 0;
   VMSessionId sessionId = 0;
   char resourcePoolPath[513];
   size_t poolBufSize;

   /* Try to load the library. */
   glError = GuestLib_OpenHandle(&glHandle);
   if (glError != VMGUESTLIB_ERROR_SUCCESS) {
      printf("OpenHandle failed: %s\n", GuestLib_GetErrorText(glError));
      return FALSE;
   }

   /* Attempt to retrieve info from the host. */
   while (!done) {
      VMSessionId tmpSession;

      glError = GuestLib_UpdateInfo(glHandle);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("UpdateInfo failed: %s\n", GuestLib_GetErrorText(glError));
         success = FALSE;
         goto out;
      }

      /* Retrieve and check the session ID */
      glError = GuestLib_GetSessionId(glHandle, &tmpSession);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get session ID: %s\n", GuestLib_GetErrorText(glError));
         success = FALSE;
         goto out;
      }

      if (tmpSession == 0) {
         printf("Error: Got zero sessionId from GuestLib\n");
         success = FALSE;
         goto out;
      }
      if (sessionId == 0) {
         sessionId = tmpSession;
         printf("Initial session ID is 0x%"FMT64"x\n", sessionId);
      } else if (tmpSession != sessionId) {
         sessionId = tmpSession;
         printf("SESSION CHANGED: New session ID is 0x%"FMT64"x\n", sessionId);
      }

      /* Retrieve all the stats. */
      glError = GuestLib_GetCpuReservationMHz(glHandle, &cpuReservationMHz);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get CPU reservation: %s\n", GuestLib_GetErrorText(glError));
         success = FALSE;
         goto out;
      }
      glError = GuestLib_GetCpuLimitMHz(glHandle, &cpuLimitMHz);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get CPU limit: %s\n", GuestLib_GetErrorText(glError));
         success = FALSE;
         goto out;
      }
      glError = GuestLib_GetCpuShares(glHandle, &cpuShares);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get cpu shares: %s\n", GuestLib_GetErrorText(glError));
         success = FALSE;
         goto out;
      }
      glError = GuestLib_GetCpuUsedMs(glHandle, &cpuUsedMs);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get used ms: %s\n", GuestLib_GetErrorText(glError));
         success = FALSE;
         goto out;
      }
      glError = GuestLib_GetHostProcessorSpeed(glHandle, &hostMHz);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get host proc speed: %s\n", GuestLib_GetErrorText(glError));
         success = FALSE;
         goto out;
      }
      glError = GuestLib_GetMemReservationMB(glHandle, &memReservationMB);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get mem reservation: %s\n",
                GuestLib_GetErrorText(glError));
         success = FALSE;
         goto out;
      }
      glError = GuestLib_GetMemLimitMB(glHandle, &memLimitMB);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get mem limit: %s\n", GuestLib_GetErrorText(glError));
         success = FALSE;
         goto out;
      }
      glError = GuestLib_GetMemShares(glHandle, &memShares);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get mem shares: %s\n", GuestLib_GetErrorText(glError));
         if (glError == VMGUESTLIB_ERROR_NOT_AVAILABLE) {
            memShares = 0;
            printf("Skipping mem shares\n");
         } else {
            success = FALSE;
            goto out;
         }
      }
      glError = GuestLib_GetMemShares64(glHandle, &memShares64);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get mem shares64: %s\n", GuestLib_GetErrorText(glError));
         if (glError == VMGUESTLIB_ERROR_UNSUPPORTED_VERSION) {
            memShares64 = 0;
            printf("Skipping mem shares64\n");
         } else {
            success = FALSE;
            goto out;
         }
      }
      glError = GuestLib_GetMemMappedMB(glHandle, &memMappedMB);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get mapped mem: %s\n", GuestLib_GetErrorText(glError));
         success = FALSE;
         goto out;
      }
      glError = GuestLib_GetMemActiveMB(glHandle, &memActiveMB);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get active mem: %s\n", GuestLib_GetErrorText(glError));
         success = FALSE;
         goto out;
      }
      glError = GuestLib_GetMemOverheadMB(glHandle, &memOverheadMB);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get overhead mem: %s\n", GuestLib_GetErrorText(glError));
         success = FALSE;
         goto out;
      }
      glError = GuestLib_GetMemBalloonedMB(glHandle, &memBalloonedMB);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get ballooned mem: %s\n", GuestLib_GetErrorText(glError));
         success = FALSE;
         goto out;
      }
      glError = GuestLib_GetMemSwappedMB(glHandle, &memSwappedMB);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get swapped mem: %s\n", GuestLib_GetErrorText(glError));
         success = FALSE;
         goto out;
      }
      glError = GuestLib_GetMemSharedMB(glHandle, &memSharedMB);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get shared mem: %s\n", GuestLib_GetErrorText(glError));
         success = FALSE;
         goto out;
      }
      glError = GuestLib_GetMemSharedSavedMB(glHandle, &memSharedSavedMB);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get shared saved mem: %s\n", GuestLib_GetErrorText(glError));
         success = FALSE;
         goto out;
      }
      glError = GuestLib_GetMemUsedMB(glHandle, &memUsedMB);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get used mem: %s\n", GuestLib_GetErrorText(glError));
         success = FALSE;
         goto out;
      }
      glError = GuestLib_GetElapsedMs(glHandle, &elapsedMs);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get elapsed ms: %s\n", GuestLib_GetErrorText(glError));
         success = FALSE;
         goto out;
      }
      poolBufSize = sizeof resourcePoolPath;
      glError = GuestLib_GetResourcePoolPath(glHandle, &poolBufSize, resourcePoolPath);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get resource pool path: %s\n", GuestLib_GetErrorText(glError));
         success = FALSE;
         goto out;
      }
      glError = GuestLib_GetCpuStolenMs(glHandle, &cpuStolenMs);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get CPU stolen: %s\n", GuestLib_GetErrorText(glError));
         if (glError == VMGUESTLIB_ERROR_UNSUPPORTED_VERSION) {
            cpuStolenMs = 0;
            printf("Skipping CPU stolen\n");
         } else {
            success = FALSE;
            goto out;
         }
      }
      glError = GuestLib_GetMemTargetSizeMB(glHandle, &memTargetSizeMB);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get target mem size: %s\n", GuestLib_GetErrorText(glError));
         if (glError == VMGUESTLIB_ERROR_UNSUPPORTED_VERSION) {
            memTargetSizeMB = 0;
            printf("Skipping target mem size\n");
         } else {
            success = FALSE;
            goto out;
         }
      }
      glError = GuestLib_GetHostNumCpuCores(glHandle, &hostNumCpuCores);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get host CPU cores: %s\n",
                GuestLib_GetErrorText(glError));
         if (glError == VMGUESTLIB_ERROR_UNSUPPORTED_VERSION ||
             glError == VMGUESTLIB_ERROR_NOT_AVAILABLE) {
            hostNumCpuCores = 0;
            printf("Skipping host CPU cores\n");
         } else {
            success = FALSE;
            goto out;
         }
      }
      glError = GuestLib_GetHostCpuUsedMs(glHandle, &hostCpuUsedMs);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get host CPU used: %s\n",
                GuestLib_GetErrorText(glError));
         if (glError == VMGUESTLIB_ERROR_UNSUPPORTED_VERSION ||
             glError == VMGUESTLIB_ERROR_NOT_AVAILABLE) {
            hostCpuUsedMs = 0;
            printf("Skipping host CPU used\n");
         } else {
            success = FALSE;
            goto out;
         }
      }
      glError = GuestLib_GetHostMemSwappedMB(glHandle, &hostMemSwappedMB);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get host mem swapped: %s\n",
                GuestLib_GetErrorText(glError));
         if (glError == VMGUESTLIB_ERROR_UNSUPPORTED_VERSION ||
             glError == VMGUESTLIB_ERROR_NOT_AVAILABLE) {
            hostMemSwappedMB = 0;
            printf("Skipping host mem swapped\n");
         } else {
            success = FALSE;
            goto out;
         }
      }
      glError = GuestLib_GetHostMemSharedMB(glHandle, &hostMemSharedMB);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get host mem shared: %s\n",
                GuestLib_GetErrorText(glError));
         if (glError == VMGUESTLIB_ERROR_UNSUPPORTED_VERSION ||
             glError == VMGUESTLIB_ERROR_NOT_AVAILABLE) {
            hostMemSharedMB = 0;
            printf("Skipping host mem shared\n");
         } else {
            success = FALSE;
            goto out;
         }
      }
      glError = GuestLib_GetHostMemUsedMB(glHandle, &hostMemUsedMB);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get host mem used: %s\n",
                GuestLib_GetErrorText(glError));
         if (glError == VMGUESTLIB_ERROR_UNSUPPORTED_VERSION ||
             glError == VMGUESTLIB_ERROR_NOT_AVAILABLE) {
            hostMemUsedMB = 0;
            printf("Skipping host mem used\n");
         } else {
            success = FALSE;
            goto out;
         }
      }
      glError = GuestLib_GetHostMemPhysMB(glHandle, &hostMemPhysMB);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get host phys mem: %s\n",
                GuestLib_GetErrorText(glError));
         if (glError == VMGUESTLIB_ERROR_UNSUPPORTED_VERSION ||
             glError == VMGUESTLIB_ERROR_NOT_AVAILABLE) {
            hostMemPhysMB = 0;
            printf("Skipping host phys mem\n");
         } else {
            success = FALSE;
            goto out;
         }
      }
      glError = GuestLib_GetHostMemPhysFreeMB(glHandle, &hostMemPhysFreeMB);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get host phys mem free: %s\n",
                GuestLib_GetErrorText(glError));
         if (glError == VMGUESTLIB_ERROR_UNSUPPORTED_VERSION ||
             glError == VMGUESTLIB_ERROR_NOT_AVAILABLE) {
            hostMemPhysFreeMB = 0;
            printf("Skipping host phys mem free\n");
         } else {
            success = FALSE;
            goto out;
         }
      }
      glError = GuestLib_GetHostMemKernOvhdMB(glHandle, &hostMemKernOvhdMB);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get host kernel overhead mem: %s\n",
                GuestLib_GetErrorText(glError));
         if (glError == VMGUESTLIB_ERROR_UNSUPPORTED_VERSION ||
             glError == VMGUESTLIB_ERROR_NOT_AVAILABLE) {
            hostMemKernOvhdMB = 0;
            printf("Skipping host kernel overhead mem\n");
         } else {
            success = FALSE;
            goto out;
         }
      }
      glError = GuestLib_GetHostMemMappedMB(glHandle, &hostMemMappedMB);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get host mem mapped: %s\n",
                GuestLib_GetErrorText(glError));
         if (glError == VMGUESTLIB_ERROR_UNSUPPORTED_VERSION ||
             glError == VMGUESTLIB_ERROR_NOT_AVAILABLE) {
            hostMemMappedMB = 0;
            printf("Skipping host mem mapped\n");
         } else {
            success = FALSE;
            goto out;
         }
      }
      glError = GuestLib_GetHostMemUnmappedMB(glHandle, &hostMemUnmappedMB);
      if (glError != VMGUESTLIB_ERROR_SUCCESS) {
         printf("Failed to get host mem unmapped: %s\n",
                GuestLib_GetErrorText(glError));
         if (glError == VMGUESTLIB_ERROR_UNSUPPORTED_VERSION ||
             glError == VMGUESTLIB_ERROR_NOT_AVAILABLE) {
            hostMemUnmappedMB = 0;
            printf("Skipping host mem unmapped\n");
         } else {
            success = FALSE;
            goto out;
         }
      }

      /* Print the stats. */
      printf("cpuReservationMHz: %u\n"
             "cpuLimitMHz: %u\n"
             "cpuShares: %u\n"
             "cpuUsedMs: %"FMT64"u\n"
             "hostMHz: %u\n"
             "memReservationMB: %u\n"
             "memLimitMB: %u\n"
             "memShares: %u\n"
             "memShares64: %"FMT64"u\n"
             "memMappedMB: %u\n"
             "memActiveMB: %u\n"
             "memOverheadMB: %u\n"
             "memBalloonedMB: %u\n"
             "memSwappedMB: %u\n"
             "memSharedMB: %u\n"
             "memSharedSavedMB: %u\n"
             "memUsedMB: %u\n"
             "elapsedMs: %"FMT64"u\n"
             "resourcePoolPath: '%s'\n"
             "cpuStolenMs: %"FMT64"u\n"
             "memTargetSizeMB: %"FMT64"u\n"
             "hostNumCpuCores: %u\n"
             "hostCpuUsedMs: %"FMT64"u\n"
             "hostMemSwappedMB: %"FMT64"u\n"
             "hostMemSharedMB: %"FMT64"u\n"
             "hostMemUsedMB: %"FMT64"u\n"
             "hostMemPhysMB: %"FMT64"u\n"
             "hostMemPhysFreeMB: %"FMT64"u\n"
             "hostMemKernOvhdMB: %"FMT64"u\n"
             "hostMemMappedMB: %"FMT64"u\n"
             "hostMemUnmappedMB: %"FMT64"u\n",
             cpuReservationMHz, cpuLimitMHz,
             cpuShares, cpuUsedMs,
             hostMHz, memReservationMB,
             memLimitMB, memShares, memShares64,
             memMappedMB, memActiveMB,
             memOverheadMB, memBalloonedMB,
             memSwappedMB, memSharedMB,
             memSharedSavedMB, memUsedMB,
             elapsedMs, resourcePoolPath,
             cpuStolenMs, memTargetSizeMB,
             hostNumCpuCores, hostCpuUsedMs,
             hostMemSwappedMB, hostMemSharedMB,
             hostMemUsedMB, hostMemPhysMB,
             hostMemPhysFreeMB, hostMemKernOvhdMB,
             hostMemMappedMB, hostMemUnmappedMB);

      /* Sleep for 1 second before repeating. */
      SLEEP(1);
   }

  out:
   glError = GuestLib_CloseHandle(glHandle);
   if (glError != VMGUESTLIB_ERROR_SUCCESS) {
      printf("Failed to CloseHandle: %s\n", GuestLib_GetErrorText(glError));
      success = FALSE;
   }

   return success;
}





int
main(int argc, char *argv[])
{
   int exitCode = 1;

   /* Try to load the library. */
   if (!LoadFunctions()) {
      printf("GuestLibTest: Failed to load shared library\n");
      goto out;
   }

   /* Test the VMware Guest API itself. */
   if (!TestGuestLib()) {
      printf("GuestLibTest: GuestLib testing failed\n");
      goto out;
   }

   exitCode = 0;

out:
   if (NULL != dlHandle) {
#ifdef _WIN32
      if (!FreeLibrary(dlHandle)) {
         DWORD error = GetLastError();
         printf("Failed to FreeLibrary: %d\n", error);
         exitCode = 1;
      }
#else
      if (dlclose(dlHandle)) {
         printf("dlclose failed with error: %s.\n", dlerror());
         exitCode = 1;
      }
#endif
   }

   if (!exitCode) {
      printf("Success!\n");
   }
   exit(exitCode);
}
