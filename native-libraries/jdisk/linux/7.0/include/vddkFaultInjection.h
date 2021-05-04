/* **********************************************************
 * Copyright 2013-2020 VMware, Inc.  All rights reserved. -- VMware Confidential
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

   VDDK_VIXDISKLIB_VIXDISKLIB_INIT_DISKLIB_FAILED,
   VDDK_VIXDISKLIB_VIXDISKLIB_INIT_SSL_FAILED,
   VDDK_VIXDISKLIB_VIXDISKLIB_DISKLIB_CLONE_FAILED,
   VDDK_VIXDISKLIB_VIXDISKLIB_CREATECHILD_GETINFO_FAILED,
   VDDK_VIXDISKLIB_VIXDISKLIB_CREATECHILD_FAILED,
   VDDK_VIXDISKLIB_VIXDISKLIB_GETINFO_FAILED,
   VDDK_VIXDISKLIB_VIXDISKLIB_CONNECT_NO_MEMORY_VMXSPEC,
   VDDK_VIXDISKLIB_VIXDISKLIB_CONNECT_NO_MEMORY_FCD,
   VDDK_VIXDISKLIB_VIXDISKLIB_CONNECT_NO_MEMORY_RDS,
   VDDK_VIXDISKLIB_VIXDISKLIB_CONNECT_NO_MEMORY_SERVER,
   VDDK_VIXDISKLIB_VIXDISKLIB_CONNECT_NO_MEMORY_CONN,
   VDDK_VIXDISKLIB_VIXDISKLIB_CONNECT_NO_MEMORY_CRED_UID,
   VDDK_VIXDISKLIB_VIXDISKLIB_CONNECT_NO_MEMORY_CRED_SESSIONID,
   VDDK_VIXDISKLIB_VIXDISKLIB_CONNECT_NO_MEMORY_CRED_TICKETID_ALL,
   VDDK_VIXDISKLIB_VIXDISKLIB_CONNECT_NO_MEMORY_CRED_TICKETID,
   VDDK_VIXDISKLIB_VIXDISKLIB_OPENWITHINFO_FAIL,
   VDDK_VIXDISKLIB_VIXDISKLIB_SPACEUSED_FAIL,
   VDDK_VIXDISKLIB_VIXDISKLIB_PUTFILE_NO_SPACE,
   VDDK_VIXDISKLIB_VIXDISKLIB_PUTFILE_START_SESSION_FAIL,
   VDDK_VIXDISKLIB_VIXDISKLIB_PUTFILE_FAIL,
   VDDK_VIXDISKLIB_VIXDISKLIB_PUTFILE_OPEN_FAIL,
   VDDK_VIXDISKLIB_VIXDISKLIB_PUTFILE_UPDADAPTER_FAIL,
   VDDK_VIXDISKLIB_VIXDISKLIB_PUTFILE_UPDVERSION_FAIL,
   VDDK_VIXDISKLIB_VIXDISKLIB_PUTFILE_FILEEXIST,
   VDDK_VIXDISKLIB_VIXDISKLIB_GETFILE_STARTSESSION_FAIL,
   VDDK_VIXDISKLIB_VIXDISKLIB_GETFILE_GETENCRYPT_FAIL,
   VDDK_VIXDISKLIB_VIXDISKLIB_CLONELOCAL_OPEN_FAIL,
   VDDK_VIXDISKLIB_VIXDISKLIB_SPACENEEDEDFORCLONE_FAIL,
   VDDK_VIXDISKLIB_VIXDISKLIB_ATTACH_FAIL,
   VDDK_VIXDISKLIB_VIXDISKLIB_WAIT_FAIL,
   VDDK_VIXDISKLIB_VIXDISKLIB_NOAVAILABLEMODES,
   VDDK_VIXDISKLIB_VIXDISKLIB_ADDTHUMB_INVPARAM,
   VDDK_VIXDISKLIB_VIXDISKLIB_ADDTHUMB_INVTHUMB,
   VDDK_VIXDISKLIB_VIXDISKLIB_ADDTHUMB_OPENDB_FAIL,
   VDDK_VIXDISKLIB_VIXDISKLIB_ADDTHUMB_RETHOST_FAIL,
   VDDK_VIXDISKLIB_VIXDISKLIB_ADDTHUMB_ADDTHUMB_FAIL,
   VDDK_VIXDISKLIB_VIXDISKLIB_OPEN_PLUGIN_FAIL,
   VDDK_VIXDISKLIB_VIXDISKLIB_INIT_TRANSPORT_FAIL,
   VDDK_VIXDISKLIB_VIXDISKLIB_RELEASEDISKTOKEN_FAIL,
   VDDK_VIXDISKLIB_VIXDISKLIB_PUSHCRYPTOKEY_FAIL,
   VDDK_VIXDISKLIB_VIXDISKLIB_READMETADATA_FAIL,
   VDDK_VIXDISKLIB_VIXDISKLIB_CREATESESSION_FAIL_AGENT,
   VDDK_VIXDISKLIB_VIXDISKLIB_CREATESESSION_FAIL_STATUS,
   VDDK_VIXDISKLIB_VIXDISKLIB_UNLINK_FILENOTEXIST,
   VDDK_VIXDISKLIB_ALCBLOCK_OPEN_GETINFO_FAILED,
   VDDK_VIXDISKLIB_ALCBLOCK_QUERY_NBDGETALC_FAILED,
   VDDK_VIXDISKLIB_ALCBLOCK_QUERY_ALLOC_FAILED,
   VDDK_VIXDISKLIB_ALCBLOCK_CLOSE_NBDCLOSE_FAILED,
   VDDK_VCBLIB_HOTADD_RECONFIG_FAIL,
   VDDK_VCBLIB_HOTADD_ADDDISK_DISK_NOT_FOUND,
   VDDK_VCBLIB_HOTADD_REMOVEDISK_FAILED,
   VDDK_VCBLIB_HOTADD_ADDDISK_DISK_ADD_FAILED,
   VDDK_VCBLIB_HOTADD_ACQUIRE_LOCK_FAIL,
   VDDK_VCBLIB_HOTADD_ALLOCATESCSITARGET_FAIL,
   VDDK_BLOCKLISTVMOMI_SANMP_WRITE_FAIL,
   VDDK_BLOCKLISTVMOMI_SANMP_PATH_INACTIVE,
   VDDK_BLOCKLISTVMOMI_MAPTABLE_ASYNCALCBLOCKS_FAILED,
   VDDK_BLOCKLISTVMOMI_MAPTABLE_ASYNCWRITE_FAIL,
   VDDK_BLOCKLISTVMOMI_ASYNCWRITE_UPDATEALCMAP_FAIL,
   VDDK_BLOCKLISTVMOMI_ASYNCWRITE_STARTTHREADS_FAIL,
   VDDK_PLUGIN_SAN_SERVER_CONNECT_FAIL,
   VDDK_PLUGIN_SAN_BLKLIST_INIT_FAIL,
   VDDK_PLUGIN_SAN_STARTIO_FAIL,
   VDDK_PLUGIN_SAN_UNKNOWN_ADAPTER,
   VDDK_PLUGIN_SAN_DISK_OPEN_FAIL,
   VDDK_VIMACCESS_SESSION_GETNFCTICKET_FAIL_NO_DISKSPEC,
   VDDK_VIMACCESS_SESSION_GETNFCTICKET_FAIL_NO_TICKET,
   VDDK_VIMACCESS_SESSION_GETNFCTICKET_FAIL_NO_SERVICE,
   VDDK_VIMACCESS_SESSION_GETNFCTICKET_FAIL_NO_SESSIONID,
   VDDK_VIMACCESS_SESSION_GETFILENAME_FAIL_NO_DISKSPEC,
   VDDK_VIMACCESS_SESSION_GETFILENAME_FAIL_NO_FILENAME,
   VDDK_VIMACCESS_SESSION_GETABOUTINFO_FAIL_NO_CONTENT,
   VDDK_VIMACCESS_SESSION_GETABOUTINFO_FAIL_NO_ABOUT,
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


/*
 * Fault Injection Function Call Wrapper
 */

#if defined(__cplusplus)
// C++14
#include <type_traits>
#include <utility>


namespace VddkFaultInject {

namespace impl {


/**
 * CallFunc -
 *
 *    A wrapper to call a member function returning non-void with
 *    a reference of the instance.
 *
 */

template <typename T, typename C, typename R, typename... Args>
inline auto
CallFunc(R T::* fn, C&& cl, Args&&... args)
 -> std::enable_if_t<std::is_member_function_pointer<decltype(fn)>::value &&
                     std::is_base_of<T, std::decay_t<C>>::value &&
                     !std::is_void<std::result_of_t<decltype(fn)(T&&, Args&&...)>>::value,
                     std::result_of_t<decltype(fn)(T&&, Args&&...)>>
{
   return (std::forward<C>(cl).*fn)(std::forward<Args>(args)...);
}


/**
 * CallFunc -
 *
 *    A wrapper to call a member function returning non-void with
 *    a non-reference (pointer to) of the instance.
 *
 */

template <typename T, typename C, typename R, typename... Args>
inline auto
CallFunc(R T::* fn, C&& cl, Args&&... args)
 -> std::enable_if_t<std::is_member_function_pointer<decltype(fn)>::value &&
                     !std::is_base_of<T, std::decay_t<C>>::value &&
                     !std::is_void<std::result_of_t<decltype(fn)(T&&, Args&&...)>>::value,
                     std::result_of_t<decltype(fn)(T&&, Args&&...)>>
{
   return ((*std::forward<C>(cl)).*fn)(std::forward<Args>(args)...);
}


/**
 * CallFunc -
 *
 *    A wrapper to call a free function returning non-void.
 *
 */

template <typename F, typename... Args>
inline auto
CallFunc(F&& fn, Args&&... args)
 -> std::enable_if_t<!std::is_member_function_pointer<std::decay_t<F>>::value &&
                     !std::is_void<std::result_of_t<F&&(Args&&...)>>::value,
                     std::result_of_t<F&&(Args&&...)>>
{
   return std::forward<F>(fn)(std::forward<Args>(args)...);
}


/**
 * CallFunc -
 *
 *    A wrapper to call a member function returning void with
 *    a reference of the instance.
 *
 */

template <typename T, typename R, typename C, typename... Args>
inline auto
CallFunc(R T::* fn, C&& cl, Args&&... args)
 -> std::enable_if_t<std::is_member_function_pointer<decltype(fn)>::value &&
                     std::is_base_of<T, std::decay_t<C>>::value &&
                     std::is_void<std::result_of_t<decltype(fn)(T&&, Args&&...)>>::value,
                     std::result_of_t<decltype(fn)(T&&, Args&&...)>>
{
   (std::forward<C>(cl).*fn)(std::forward<Args>(args)...);
}


/**
 * CallFunc -
 *
 *    A wrapper to call a member function returning void with
 *    a non-reference (pointer to) of the instance.
 *
 */

template <typename T, typename R, typename C, typename... Args>
inline auto
CallFunc(R T::* fn, C&& cl, Args&&... args)
 -> std::enable_if_t<std::is_member_function_pointer<decltype(fn)>::value &&
                     !std::is_base_of<T, std::decay_t<C>>::value &&
                     std::is_void<std::result_of_t<decltype(fn)(T&&, Args&&...)>>::value,
                     std::result_of_t<decltype(fn)(T&&, Args&&...)>>
{
   ((*std::forward<C>(cl)).*fn)(std::forward<Args>(args)...);
}


/**
 * CallFunc -
 *
 *    A wrapper to call a free function returning void.
 *
 */

template <typename F, typename... Args>
inline auto
CallFunc(F&& fn, Args&&... args)
 -> std::enable_if_t<!std::is_member_function_pointer<std::decay_t<F>>::value &&
                     std::is_void<std::result_of_t<F&&(Args&&...)>>::value,
                     std::result_of_t<F&&(Args&&...)>>
{
   std::forward<F>(fn)(std::forward<Args>(args)...);
}

} // namespace impl

/**
 * FaultInjectCall -
 *
 *    Fault Inject for the function returns the fault error code instead
 *    of the original one.
 *
 */

template <typename F, typename... Args>
inline
std::enable_if_t<std::is_convertible<int, std::result_of_t<F&&(Args&&...)>>::value,
                 std::result_of_t<F&&(Args&&...)>>
FaultInjectCall(int faultId, F&& fn, Args&&... args)
{
#ifndef VMX86_RELEASE
   using ret_type = std::result_of_t<F&&(Args&&...)>;
   int err = 0;
   if (VixDiskLib_IsFaultEnabled != nullptr &&
       VixDiskLib_IsFaultEnabled(faultId, &err)) {
      return static_cast<ret_type>(err);
   }
#endif
   return impl::CallFunc(std::forward<F>(fn), std::forward<Args>(args)...);
}


/**
 * FaultInjectCall -
 *
 *    Fault Inject for the function returns object from the covert functor
 *    which coverts fault error code to replace the original.
 *
 */

template <typename Cov, typename F, typename... Args>
inline
std::enable_if_t<!std::is_void<std::result_of_t<F&&(Args&&...)>>::value,
                 std::result_of_t<F&&(Args&&...)>>
FaultInjectCall(Cov&& cov, int faultId, F&& fn, Args&&... args)
{
#ifndef VMX86_RELEASE
   int err = 0;
   if (VixDiskLib_IsFaultEnabled != nullptr &&
       VixDiskLib_IsFaultEnabled(faultId, &err)) {
      return std::forward<Cov>(cov)(err);
   }
#endif
   return impl::CallFunc(std::forward<F>(fn), std::forward<Args>(args)...);
}


/**
 * FaultInjectCall -
 *
 *    Fault Inject for the function returns void, but calls the covert functor
 *    which processes the fault error code.
 *
 */

template <typename Cov, typename F, typename... Args>
inline
std::enable_if_t<std::is_void<std::result_of_t<F&&(Args&&...)>>::value,
                 std::result_of_t<F&&(Args&&...)>>
FaultInjectCall(Cov&& cov, int faultId, F&& fn, Args&&... args)
{
#ifndef VMX86_RELEASE
   int err = 0;
   if (VixDiskLib_IsFaultEnabled != nullptr &&
       VixDiskLib_IsFaultEnabled(faultId, &err)) {
      std::forward<Cov>(cov)(err);
      return;
   }
#endif
   impl::CallFunc(std::forward<F>(fn), std::forward<Args>(args)...);
}


/**
 *
 * FAULT_INJECT_POINT
 *
 *    Fault Inject at one place calls a fault handle functor with the
 *    fault error code.
 *
 */

template <typename FaultHandle>
inline void
FaultInjectPoint(int faultInjectId, FaultHandle&& fh)
{
#ifndef VMX86_RELEASE
   int err = 0;
   if (VixDiskLib_IsFaultEnabled != nullptr &&
       VixDiskLib_IsFaultEnabled(faultInjectId, &err)) {
      std::forward<FaultHandle>(fh)(err);
   }
#endif
}

} // namespace VddkFaultInject

#else
// C

#ifndef VMX86_RELEASE

#define FAULT_INJECT_RET(ret, id, func, ...)    \
do {                                            \
   int err = 0;                                 \
   if (VixDiskLib_IsFaultEnabled != NULL &&     \
       VixDiskLib_IsFaultEnabled(id, &err)) {   \
      ret = err;                                \
   } else {                                     \
      ret = func(__VA_ARGS__);                  \
   }                                            \
} while (0)

#define FAULT_INJECT_ERR_RET(ret, error, id, func, ...)  \
do {                                                     \
   int err = 0;                                          \
   if (VixDiskLib_IsFaultEnabled != NULL &&              \
       VixDiskLib_IsFaultEnabled(id, &err)) {            \
      ret = error;                                       \
   } else {                                              \
      ret = func(__VA_ARGS__);                           \
   }                                                     \
} while (0)

#define FAULT_INJECT_COV_RET(ret, cov, id, func, ...)    \
do {                                                     \
   if (VixDiskLib_IsFaultEnabled != NULL &&              \
       VixDiskLib_IsFaultEnabled(id, &err)) {            \
      ret = cov(err);                                    \
   } else {                                              \
      ret = func(__VA_ARGS__);                           \
   }                                                     \
} while (0)

#define FAULT_INJECT_NORET(cov, id, func, ...)  \
do {                                            \
   int err = 0;                                 \
   if (VixDiskLib_IsFaultEnabled != NULL &&     \
       VixDiskLib_IsFaultEnabled(id, &err)) {   \
      cov(err);                                 \
   } else {                                     \
      func(__VA_ARGS__);                        \
   }                                            \
} while (0)

#define FAULT_INJECT_POINT(id, fh, ...)         \
do {                                            \
   int err = 0;                                 \
   if (VixDiskLib_IsFaultEnabled != NULL &&     \
       VixDiskLib_IsFaultEnabled(id, &err)) {   \
      fh(err, __VA_ARGS__);                     \
   }                                            \
} while (0)

#else

#define FAULT_INJECT_RET(ret, id, func, ...)    \
do {                                            \
   ret = func(__VA_ARGS__);                     \
} while (0)

#define FAULT_INJECT_ERR_RET(ret, err, id, func, ...) \
do {                                                  \
   ret = func(__VA_ARGS__);                           \
} while (0)

#define FAULT_INJECT_COV_RET(ret, cov, id, func, ...) \
do {                                                  \
   ret = func(__VA_ARGS__);                           \
} while (0)

#define FAULT_INJECT_NORET(cov, id, func, ...)  \
do {                                            \
   func(__VA_ARGS__);                           \
} while (0)

#define FAULT_INJECT_POINT(id, fh, ...)

#endif // VMX86_RELEASE

#endif // __cplusplus

#endif // _VDDK_FAULT_INJECTION_H_

