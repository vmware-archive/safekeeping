/* **********************************************************
 * Copyright 2013-2016 VMware, Inc.  All rights reserved. -- VMware Confidential
 * **********************************************************/

#ifndef _VDDK_FAULT_INJECTION_H_
#define _VDDK_FAULT_INJECTION_H_

#include "vm_basic_types.h"

#if defined(__cplusplus)
extern "C" {
#endif

enum diskLibFaultInjection
{
   VDDK_SAN_SERVER_CONNECT_ERROR = 0,
   VDDK_SAN_BLKLIST_INIT_ERROR,
   VDDK_SAN_STARTIO_ERROR,
   VDDK_SAN_UNKNOWN_ADAPTER,
   VDDK_SAN_DISK_OPEN_ERROR,
   VDDK_SAN_ASYNC_READ_WRITE_ERROR,
   VDDK_SAN_READ_WRITE_ERROR,
   VDDK_HOTADD_ADDDISK_DISK_NOT_FOUND,
   VDDK_HOTADD_ADDDISK_DISK_ADD_FAILED,
   VDDK_HOTADD_REMOVEDISK_FAILED,
   VDDK_VIXDISKLIB_INIT_DISKLIB_FAILED,
   VDDK_VIXDISKLIBVIM_LOAD_DISK_BAD_KEY,
   VDDK_VIXDISKLIBVIM_BAD_TICKET,
   VDDK_HOTADD_AHCI_ONLY,
   VDDK_FAULT_INJECTION_LAST_ENTRY
};

typedef struct vddkFaultEntry
{
   Bool enabled;
   int  faultErr;
} vddkFaultEntry;

typedef Bool (*IsFaultEnabled_T)(int id, int *faultErr);

/*
 *
 * The function returns TRUE if enabled and FALSE if not enabled.
 *
 *  id       IN  a diskLibFaultInjection member from enum diskLibFaultInjection
 *  faultErr OUT the error to return at the fault injection point.
 *
 */

#ifdef VDDK_FAULT_IS_EXTERN
extern
#endif
IsFaultEnabled_T VixDiskLib_IsFaultEnabled;

/*
 *
 * This is a public function that is used to enable a fault in VDDK
 *
 * The function returns TRUE on success and FALSE on error.
 *
 *  id           IN the fault entry to be set or cleared.
 *  enabled      IN the desired state, TRUE is enabled, FALSE is disabled.
 *  faultErr IN the error to be returned as a fault.
 *
 */

Bool VixDiskLib_SetInjectedFault(int id, Bool enabled, int faultErr);

#if defined(__cplusplus)
} // extern "C"
#endif
#endif // _VDDK_FAULT_INJECTION_H_

