/*******************************************************************************
 * Copyright (C) 2019, VMware, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package com.vmware.jvix;

import java.nio.ByteBuffer;
import java.util.List;

/*
 * Interface into vixDiskLib for Java programmers
 */

public interface jDiskLib {

    /*
     * VixDiskLib's Block structure
     */
    public static class Block {
	public long offset;

	public long length;

	public Block() {
	    offset = 0;
	    length = 0;
	}
    }

    /*
     * Wrapper type to achieve some level of type safety for DiskLib Connections.
     */
    public static class Connection {
	protected long handle;

	public Connection() {
	    handle = 0;
	}

	public long getHandle() {
	    return handle;
	}

	public boolean IsValid() {
	    return handle != 0;
	}

    }

    /*
     * VixDiskLib Error Constants
     */

    /*
     * Input Parameters for a Connect call.
     */
    public static class ConnectParams {

	public static final int VIXDISKLIB_CRED_UID = 1; // use userid password
	public static final int VIXDISKLIB_CRED_SESSIONID = 2; // http session id
	public static final int VIXDISKLIB_CRED_TICKETID = 3; // vim ticket id
	public static final int VIXDISKLIB_CRED_SSPI = 4; // Windows only - use current thread credentials.
	public static final int VIXDISKLIB_CRED_UNKNOWN = 256;

	public static final int VIXDISKLIB_SPEC_VMX = 0;
	public static final int VIXDISKLIB_SPEC_VSTORAGE_OBJECT = 1;
	public static final int VIXDISKLIB_SPEC_UNKNOWN = 2; // must be last

	public int credType;

	// spec
	public String vmxSpec;
	public String id;
	public String datastoreMoRef;
	public String ssId;
	public int specType;
	public String serverName;

	public String thumbPrint;
	public int port; // No default -- Set to 443
	public String username;
	public String password;
	public String key;
	public String cookie;
	public int nfcHostPort;// No default -- Set to 902

	public ConnectParams() {
	    credType = VIXDISKLIB_CRED_UID;
	}
    }

    /*
     * Properties for creating and cloning of disks.
     */
    public static class CreateParams {
	public int diskType;
	public int adapterType;
	public int hwVersion;
	public long capacityInSectors;
    }

    /*
     * Wrapper type to achieve some level of type safety for DiskHandles
     */
    public static class DiskHandle {
	protected long handle;

	public DiskHandle() {
	    handle = 0;
	}

	public long getHandle() {
	    return handle;
	}

	public boolean IsValid() {
	    return handle != 0;
	}
    }

    /*
     * Disk Geometry
     */
    public static class Geometry {
	public int cylinders;
	public int heads;
	public int sectors;
    }

    /*
     * VixDiskLib's DiskInfo structure
     */
    public static class Info {
	public Geometry biosGeo;

	public Geometry physGeo;
	public long capacityInSectors;
	public int adapterType;
	public int numLinks;
	public String parentFileNameHint;

	public Info() {
	    biosGeo = new Geometry();
	    physGeo = new Geometry();
	}
    }

    /*
     * jDiskLib Version
     */
    public static final int MAJOR_VERSION = 6;
    public static final int MINOR_VERSION = 7;
    /* No error */
    public static final int VIX_OK = 0;
    /* General errors */
    public static final int VIX_E_FAIL = 1;
    public static final int VIX_E_OUT_OF_MEMORY = 2;
    public static final int VIX_E_INVALID_ARG = 3;
    public static final int VIX_E_FILE_NOT_FOUND = 4;
    public static final int VIX_E_OBJECT_IS_BUSY = 5;
    public static final int VIX_E_NOT_SUPPORTED = 6;
    public static final int VIX_E_FILE_ERROR = 7;
    public static final int VIX_E_DISK_FULL = 8;
    public static final int VIX_E_INCORRECT_FILE_TYPE = 9;
    public static final int VIX_E_CANCELLED = 10;
    public static final int VIX_E_FILE_READ_ONLY = 11;
    public static final int VIX_E_FILE_ALREADY_EXISTS = 12;
    public static final int VIX_E_FILE_ACCESS_ERROR = 13;
    public static final int VIX_E_REQUIRES_LARGE_FILES = 14;
    public static final int VIX_E_FILE_ALREADY_LOCKED = 15;
    public static final int VIX_E_VMDB = 16;
    public static final int VIX_E_NOT_SUPPORTED_ON_REMOTE_OBJECT = 20;
    public static final int VIX_E_FILE_TOO_BIG = 21;
    public static final int VIX_E_FILE_NAME_INVALID = 22;
    public static final int VIX_E_ALREADY_EXISTS = 23;
    public static final int VIX_E_BUFFER_TOOSMALL = 24;
    public static final int VIX_E_OBJECT_NOT_FOUND = 25;
    public static final int VIX_E_HOST_NOT_CONNECTED = 26;
    public static final int VIX_E_INVALID_UTF8_STRING = 27;
    public static final int VIX_E_OPERATION_ALREADY_IN_PROGRESS = 31;
    public static final int VIX_E_UNFINISHED_JOB = 29;

    public static final int VIX_E_NEED_KEY = 30;
    public static final int VIX_E_LICENSE = 32;
    public static final int VIX_E_VM_HOST_DISCONNECTED = 34;

    public static final int VIX_E_AUTHENTICATION_FAIL = 35;
    public static final int VIX_E_HOST_CONNECTION_LOST = 36;
    public static final int VIX_E_DUPLICATE_NAME = 41;

    public static final int VIX_E_ARGUMENT_TOO_BIG = 44;
    /* Handle Errors */
    public static final int VIX_E_INVALID_HANDLE = 1000;
    public static final int VIX_E_NOT_SUPPORTED_ON_HANDLE_TYPE = 1001;
    public static final int VIX_E_TOO_MANY_HANDLES = 1002;
    /* XML errors */
    public static final int VIX_E_NOT_FOUND = 2000;
    public static final int VIX_E_TYPE_MISMATCH = 2001;
    public static final int VIX_E_INVALID_XML = 2002;
    /* VM Control Errors */
    public static final int VIX_E_TIMEOUT_WAITING_FOR_TOOLS = 3000;
    public static final int VIX_E_UNRECOGNIZED_COMMAND = 3001;
    public static final int VIX_E_OP_NOT_SUPPORTED_ON_GUEST = 3003;
    public static final int VIX_E_PROGRAM_NOT_STARTED = 3004;
    public static final int VIX_E_CANNOT_START_READ_ONLY_VM = 3005;
    public static final int VIX_E_VM_NOT_RUNNING = 3006;
    public static final int VIX_E_VM_IS_RUNNING = 3007;
    public static final int VIX_E_CANNOT_CONNECT_TO_VM = 3008;
    public static final int VIX_E_POWEROP_SCRIPTS_NOT_AVAILABLE = 3009;
    public static final int VIX_E_NO_GUEST_OS_INSTALLED = 3010;
    public static final int VIX_E_VM_INSUFFICIENT_HOST_MEMORY = 3011;
    public static final int VIX_E_SUSPEND_ERROR = 3012;
    public static final int VIX_E_VM_NOT_ENOUGH_CPUS = 3013;
    public static final int VIX_E_HOST_USER_PERMISSIONS = 3014;
    public static final int VIX_E_GUEST_USER_PERMISSIONS = 3015;
    public static final int VIX_E_TOOLS_NOT_RUNNING = 3016;
    public static final int VIX_E_GUEST_OPERATIONS_PROHIBITED = 3017;
    public static final int VIX_E_ANON_GUEST_OPERATIONS_PROHIBITED = 3018;
    public static final int VIX_E_ROOT_GUEST_OPERATIONS_PROHIBITED = 3019;
    public static final int VIX_E_MISSING_ANON_GUEST_ACCOUNT = 3023;
    public static final int VIX_E_CANNOT_AUTHENTICATE_WITH_GUEST = 3024;
    public static final int VIX_E_UNRECOGNIZED_COMMAND_IN_GUEST = 3025;
    public static final int VIX_E_CONSOLE_GUEST_OPERATIONS_PROHIBITED = 3026;
    public static final int VIX_E_MUST_BE_CONSOLE_USER = 3027;
    public static final int VIX_E_VMX_MSG_DIALOG_AND_NO_UI = 3028;
    /*
     * public static final int VIX_E_NOT_ALLOWED_DURING_VM_RECORDING = 3029; Removed
     * in version 1.11
     */
    /*
     * public static final int VIX_E_NOT_ALLOWED_DURING_VM_REPLAY = 3030; Removed in
     * version 1.11
     */
    public static final int VIX_E_OPERATION_NOT_ALLOWED_FOR_LOGIN_TYPE = 3031;
    public static final int VIX_E_LOGIN_TYPE_NOT_SUPPORTED = 3032;

    public static final int VIX_E_EMPTY_PASSWORD_NOT_ALLOWED_IN_GUEST = 3033;
    public static final int VIX_E_INTERACTIVE_SESSION_NOT_PRESENT = 3034;
    public static final int VIX_E_INTERACTIVE_SESSION_USER_MISMATCH = 3035;
    /*
     * public static final int VIX_E_UNABLE_TO_REPLAY_VM = 3039; Removed in version
     * 1.11
     */
    public static final int VIX_E_CANNOT_POWER_ON_VM = 3041;
    public static final int VIX_E_NO_DISPLAY_SERVER = 3043;
    /*
     * public static final int VIX_E_VM_NOT_RECORDING = 3044; Removed in version
     * 1.11
     */
    /*
     * public static final int VIX_E_VM_NOT_REPLAYING = 3045; Removed in version
     * 1.11
     */
    public static final int VIX_E_TOO_MANY_LOGONS = 3046;
    public static final int VIX_E_INVALID_AUTHENTICATION_SESSION = 3047;

    /* VM Errors */
    public static final int VIX_E_VM_NOT_FOUND = 4000;
    public static final int VIX_E_NOT_SUPPORTED_FOR_VM_VERSION = 4001;
    public static final int VIX_E_CANNOT_READ_VM_CONFIG = 4002;
    public static final int VIX_E_TEMPLATE_VM = 4003;
    public static final int VIX_E_VM_ALREADY_LOADED = 4004;
    public static final int VIX_E_VM_ALREADY_UP_TO_DATE = 4006;

    public static final int VIX_E_VM_UNSUPPORTED_GUEST = 4011;

    /* Property Errors */
    public static final int VIX_E_UNRECOGNIZED_PROPERTY = 6000;
    public static final int VIX_E_INVALID_PROPERTY_VALUE = 6001;

    public static final int VIX_E_READ_ONLY_PROPERTY = 6002;
    public static final int VIX_E_MISSING_REQUIRED_PROPERTY = 6003;
    public static final int VIX_E_INVALID_SERIALIZED_DATA = 6004;
    public static final int VIX_E_PROPERTY_TYPE_MISMATCH = 6005;
    /* Completion Errors */
    public static final int VIX_E_BAD_VM_INDEX = 8000;
    /* Message errors */
    public static final int VIX_E_INVALID_MESSAGE_HEADER = 10000;
    public static final int VIX_E_INVALID_MESSAGE_BODY = 10001;
    /* Snapshot errors */
    public static final int VIX_E_SNAPSHOT_INVAL = 13000;
    public static final int VIX_E_SNAPSHOT_DUMPER = 13001;
    public static final int VIX_E_SNAPSHOT_DISKLIB = 13002;
    public static final int VIX_E_SNAPSHOT_NOTFOUND = 13003;
    public static final int VIX_E_SNAPSHOT_EXISTS = 13004;
    public static final int VIX_E_SNAPSHOT_VERSION = 13005;
    public static final int VIX_E_SNAPSHOT_NOPERM = 13006;
    public static final int VIX_E_SNAPSHOT_CONFIG = 13007;
    public static final int VIX_E_SNAPSHOT_NOCHANGE = 13008;
    public static final int VIX_E_SNAPSHOT_CHECKPOINT = 13009;
    public static final int VIX_E_SNAPSHOT_LOCKED = 13010;
    public static final int VIX_E_SNAPSHOT_INCONSISTENT = 13011;
    public static final int VIX_E_SNAPSHOT_NAMETOOLONG = 13012;
    public static final int VIX_E_SNAPSHOT_VIXFILE = 13013;
    public static final int VIX_E_SNAPSHOT_DISKLOCKED = 13014;
    public static final int VIX_E_SNAPSHOT_DUPLICATEDDISK = 13015;

    public static final int VIX_E_SNAPSHOT_INDEPENDENTDISK = 13016;
    public static final int VIX_E_SNAPSHOT_NONUNIQUE_NAME = 13017;
    public static final int VIX_E_SNAPSHOT_MEMORY_ON_INDEPENDENT_DISK = 13018;
    public static final int VIX_E_SNAPSHOT_MAXSNAPSHOTS = 13019;
    public static final int VIX_E_SNAPSHOT_MIN_FREE_SPACE = 13020;
    public static final int VIX_E_SNAPSHOT_HIERARCHY_TOODEEP = 13021;
    // DEPRECRATED public static final int VIX_E_SNAPSHOT_RRSUSPEND = 13022;
    public static final int VIX_E_SNAPSHOT_NOT_REVERTABLE = 13024;
    /* Host Errors */
    public static final int VIX_E_HOST_DISK_INVALID_VALUE = 14003;
    public static final int VIX_E_HOST_DISK_SECTORSIZE = 14004;
    public static final int VIX_E_HOST_FILE_ERROR_EOF = 14005;
    public static final int VIX_E_HOST_NETBLKDEV_HANDSHAKE = 14006;

    public static final int VIX_E_HOST_SOCKET_CREATION_ERROR = 14007;
    public static final int VIX_E_HOST_SERVER_NOT_FOUND = 14008;
    public static final int VIX_E_HOST_NETWORK_CONN_REFUSED = 14009;
    public static final int VIX_E_HOST_TCP_SOCKET_ERROR = 14010;
    public static final int VIX_E_HOST_TCP_CONN_LOST = 14011;
    public static final int VIX_E_HOST_NBD_HASHFILE_VOLUME = 14012;
    public static final int VIX_E_HOST_NBD_HASHFILE_INIT = 14013;
    /* Disklib errors */
    public static final int VIX_E_DISK_INVAL = 16000;
    public static final int VIX_E_DISK_NOINIT = 16001;
    public static final int VIX_E_DISK_NOIO = 16002;
    public static final int VIX_E_DISK_PARTIALCHAIN = 16003;
    public static final int VIX_E_DISK_NEEDSREPAIR = 16006;
    public static final int VIX_E_DISK_OUTOFRANGE = 16007;
    public static final int VIX_E_DISK_CID_MISMATCH = 16008;
    public static final int VIX_E_DISK_CANTSHRINK = 16009;
    public static final int VIX_E_DISK_PARTMISMATCH = 16010;
    public static final int VIX_E_DISK_UNSUPPORTEDDISKVERSION = 16011;
    public static final int VIX_E_DISK_OPENPARENT = 16012;
    public static final int VIX_E_DISK_NOTSUPPORTED = 16013;
    public static final int VIX_E_DISK_NEEDKEY = 16014;
    public static final int VIX_E_DISK_NOKEYOVERRIDE = 16015;
    public static final int VIX_E_DISK_NOTENCRYPTED = 16016;
    public static final int VIX_E_DISK_NOKEY = 16017;
    public static final int VIX_E_DISK_INVALIDPARTITIONTABLE = 16018;
    public static final int VIX_E_DISK_NOTNORMAL = 16019;
    public static final int VIX_E_DISK_NOTENCDESC = 16020;
    public static final int VIX_E_DISK_NEEDVMFS = 16022;
    public static final int VIX_E_DISK_RAWTOOBIG = 16024;
    public static final int VIX_E_DISK_TOOMANYOPENFILES = 16027;
    public static final int VIX_E_DISK_TOOMANYREDO = 16028;
    public static final int VIX_E_DISK_RAWTOOSMALL = 16029;
    public static final int VIX_E_DISK_INVALIDCHAIN = 16030;
    public static final int VIX_E_DISK_KEY_NOTFOUND = 16052; // metadata key is not found
    public static final int VIX_E_DISK_SUBSYSTEM_INIT_FAIL = 16053;
    public static final int VIX_E_DISK_INVALID_CONNECTION = 16054;
    public static final int VIX_E_DISK_ENCODING = 16061;
    public static final int VIX_E_DISK_CANTREPAIR = 16062;

    public static final int VIX_E_DISK_INVALIDDISK = 16063;
    public static final int VIX_E_DISK_NOLICENSE = 16064;
    public static final int VIX_E_DISK_NODEVICE = 16065;
    public static final int VIX_E_DISK_UNSUPPORTEDDEVICE = 16066;
    public static final int VIX_E_DISK_CAPACITY_MISMATCH = 16067;
    public static final int VIX_E_DISK_PARENT_NOTALLOWED = 16068;
    public static final int VIX_E_DISK_ATTACH_ROOTLINK = 16069;
    /* Crypto Library Errors */
    public static final int VIX_E_CRYPTO_UNKNOWN_ALGORITHM = 17000;
    public static final int VIX_E_CRYPTO_BAD_BUFFER_SIZE = 17001;
    public static final int VIX_E_CRYPTO_INVALID_OPERATION = 17002;
    public static final int VIX_E_CRYPTO_RANDOM_DEVICE = 17003;
    public static final int VIX_E_CRYPTO_NEED_PASSWORD = 17004;
    public static final int VIX_E_CRYPTO_BAD_PASSWORD = 17005;

    public static final int VIX_E_CRYPTO_NOT_IN_DICTIONARY = 17006;
    public static final int VIX_E_CRYPTO_NO_CRYPTO = 17007;
    public static final int VIX_E_CRYPTO_ERROR = 17008;

    public static final int VIX_E_CRYPTO_BAD_FORMAT = 17009;
    public static final int VIX_E_CRYPTO_LOCKED = 17010;
    public static final int VIX_E_CRYPTO_EMPTY = 17011;
    public static final int VIX_E_CRYPTO_KEYSAFE_LOCATOR = 17012;

    /* Remoting Errors. */
    public static final int VIX_E_CANNOT_CONNECT_TO_HOST = 18000;
    public static final int VIX_E_NOT_FOR_REMOTE_HOST = 18001;
    public static final int VIX_E_INVALID_HOSTNAME_SPECIFICATION = 18002;
    /* Screen Capture Errors. */
    public static final int VIX_E_SCREEN_CAPTURE_ERROR = 19000;
    public static final int VIX_E_SCREEN_CAPTURE_BAD_FORMAT = 19001;
    public static final int VIX_E_SCREEN_CAPTURE_COMPRESSION_FAIL = 19002;

    public static final int VIX_E_SCREEN_CAPTURE_LARGE_DATA = 19003;
    /* Guest Errors */
    public static final int VIX_E_GUEST_VOLUMES_NOT_FROZEN = 20000;
    public static final int VIX_E_NOT_A_FILE = 20001;
    public static final int VIX_E_NOT_A_DIRECTORY = 20002;
    public static final int VIX_E_NO_SUCH_PROCESS = 20003;
    public static final int VIX_E_FILE_NAME_TOO_LONG = 20004;
    public static final int VIX_E_OPERATION_DISABLED = 20005;
    /* Tools install errors */
    public static final int VIX_E_TOOLS_INSTALL_NO_IMAGE = 21000;
    public static final int VIX_E_TOOLS_INSTALL_IMAGE_INACCESIBLE = 21001;
    public static final int VIX_E_TOOLS_INSTALL_NO_DEVICE = 21002;
    public static final int VIX_E_TOOLS_INSTALL_DEVICE_NOT_CONNECTED = 21003;
    public static final int VIX_E_TOOLS_INSTALL_CANCELLED = 21004;
    public static final int VIX_E_TOOLS_INSTALL_INIT_FAILED = 21005;

    public static final int VIX_E_TOOLS_INSTALL_AUTO_NOT_SUPPORTED = 21006;
    public static final int VIX_E_TOOLS_INSTALL_GUEST_NOT_READY = 21007;
    public static final int VIX_E_TOOLS_INSTALL_SIG_CHECK_FAILED = 21008;
    public static final int VIX_E_TOOLS_INSTALL_ERROR = 21009;
    public static final int VIX_E_TOOLS_INSTALL_ALREADY_UP_TO_DATE = 21010;
    public static final int VIX_E_TOOLS_INSTALL_IN_PROGRESS = 21011;

    public static final int VIX_E_TOOLS_INSTALL_IMAGE_COPY_FAILED = 21012;
    /* Wrapper Errors */
    public static final int VIX_E_WRAPPER_WORKSTATION_NOT_INSTALLED = 22001;
    public static final int VIX_E_WRAPPER_VERSION_NOT_FOUND = 22002;
    public static final int VIX_E_WRAPPER_SERVICEPROVIDER_NOT_FOUND = 22003;
    public static final int VIX_E_WRAPPER_PLAYER_NOT_INSTALLED = 22004;
    public static final int VIX_E_WRAPPER_RUNTIME_NOT_INSTALLED = 22005;
    public static final int VIX_E_WRAPPER_MULTIPLE_SERVICEPROVIDERS = 22006;
    /* FuseMnt errors */
    public static final int VIX_E_MNTAPI_MOUNTPT_NOT_FOUND = 24000;
    public static final int VIX_E_MNTAPI_MOUNTPT_IN_USE = 24001;
    public static final int VIX_E_MNTAPI_DISK_NOT_FOUND = 24002;
    public static final int VIX_E_MNTAPI_DISK_NOT_MOUNTED = 24003;
    public static final int VIX_E_MNTAPI_DISK_IS_MOUNTED = 24004;
    public static final int VIX_E_MNTAPI_DISK_NOT_SAFE = 24005;
    public static final int VIX_E_MNTAPI_DISK_CANT_OPEN = 24006;
    public static final int VIX_E_MNTAPI_CANT_READ_PARTS = 24007;
    public static final int VIX_E_MNTAPI_UMOUNT_APP_NOT_FOUND = 24008;
    public static final int VIX_E_MNTAPI_UMOUNT = 24009;
    public static final int VIX_E_MNTAPI_NO_MOUNTABLE_PARTITONS = 24010;
    public static final int VIX_E_MNTAPI_PARTITION_RANGE = 24011;
    public static final int VIX_E_MNTAPI_PERM = 24012;
    public static final int VIX_E_MNTAPI_DICT = 24013;
    public static final int VIX_E_MNTAPI_DICT_LOCKED = 24014;
    public static final int VIX_E_MNTAPI_OPEN_HANDLES = 24015;

    public static final int VIX_E_MNTAPI_CANT_MAKE_VAR_DIR = 24016;
    public static final int VIX_E_MNTAPI_NO_ROOT = 24017;
    public static final int VIX_E_MNTAPI_LOOP_FAILED = 24018;
    public static final int VIX_E_MNTAPI_DAEMON = 24019;
    public static final int VIX_E_MNTAPI_INTERNAL = 24020;
    public static final int VIX_E_MNTAPI_SYSTEM = 24021;
    public static final int VIX_E_MNTAPI_NO_CONNECTION_DETAILS = 24022;
    /* FuseMnt errors: Do not exceed 24299 */
    /* VixMntapi errors */
    public static final int VIX_E_MNTAPI_INCOMPATIBLE_VERSION = 24300;
    public static final int VIX_E_MNTAPI_OS_ERROR = 24301;
    public static final int VIX_E_MNTAPI_DRIVE_LETTER_IN_USE = 24302;
    public static final int VIX_E_MNTAPI_DRIVE_LETTER_ALREADY_ASSIGNED = 24303;
    public static final int VIX_E_MNTAPI_VOLUME_NOT_MOUNTED = 24304;
    public static final int VIX_E_MNTAPI_VOLUME_ALREADY_MOUNTED = 24305;
    public static final int VIX_E_MNTAPI_FORMAT_FAILURE = 24306;
    public static final int VIX_E_MNTAPI_NO_DRIVER = 24307;
    public static final int VIX_E_MNTAPI_ALREADY_OPENED = 24308;
    public static final int VIX_E_MNTAPI_ITEM_NOT_FOUND = 24309;
    public static final int VIX_E_MNTAPI_UNSUPPROTED_BOOT_LOADER = 24310;
    public static final int VIX_E_MNTAPI_UNSUPPROTED_OS = 24311;
    public static final int VIX_E_MNTAPI_CODECONVERSION = 24312;
    public static final int VIX_E_MNTAPI_REGWRITE_ERROR = 24313;
    public static final int VIX_E_MNTAPI_UNSUPPORTED_FT_VOLUME = 24314;
    public static final int VIX_E_MNTAPI_PARTITION_NOT_FOUND = 24315;

    public static final int VIX_E_MNTAPI_PUTFILE_ERROR = 24316;

    public static final int VIX_E_MNTAPI_GETFILE_ERROR = 24317;

    public static final int VIX_E_MNTAPI_REG_NOT_OPENED = 24318;
    public static final int VIX_E_MNTAPI_REGDELKEY_ERROR = 24319;
    public static final int VIX_E_MNTAPI_CREATE_PARTITIONTABLE_ERROR = 24320;
    public static final int VIX_E_MNTAPI_OPEN_FAILURE = 24321;
    public static final int VIX_E_MNTAPI_VOLUME_NOT_WRITABLE = 24322;
    /* Success on operation that completes asynchronously */
    public static final int VIX_ASYNC = 25000;
    /* Async errors */
    public static final int VIX_E_ASYNC_MIXEDMODE_UNSUPPORTED = 26000;
    /* Network Errors */
    public static final int VIX_E_NET_HTTP_UNSUPPORTED_PROTOCOL = 30001;
    public static final int VIX_E_NET_HTTP_URL_MALFORMAT = 30003;
    public static final int VIX_E_NET_HTTP_COULDNT_RESOLVE_PROXY = 30005;
    public static final int VIX_E_NET_HTTP_COULDNT_RESOLVE_HOST = 30006;
    public static final int VIX_E_NET_HTTP_COULDNT_CONNECT = 30007;

    public static final int VIX_E_NET_HTTP_HTTP_RETURNED_ERROR = 30022;
    public static final int VIX_E_NET_HTTP_OPERATION_TIMEDOUT = 30028;
    public static final int VIX_E_NET_HTTP_SSL_CONNECT_ERROR = 30035;
    public static final int VIX_E_NET_HTTP_TOO_MANY_REDIRECTS = 30047;
    public static final int VIX_E_NET_HTTP_TRANSFER = 30200;
    public static final int VIX_E_NET_HTTP_SSL_SECURITY = 30201;
    public static final int VIX_E_NET_HTTP_GENERIC = 30202;
    /* Fault Injection Codes */
    public static final int VDDK_SAN_SERVER_CONNECT_ERROR = 0;
    public static final int VDDK_SAN_BLKLIST_INIT_ERROR = 1;
    public static final int VDDK_SAN_STARTIO_ERROR = 2;
    public static final int VDDK_SAN_UNKNOWN_ADAPTER = 3;
    public static final int VDDK_SAN_DISK_OPEN_ERROR = 4;
    public static final int VDDK_SAN_ASYNC_READ_WRITE_ERROR = 5;

    /*
     * VixDiskLib Open Flags
     */

    public static final int VDDK_SAN_READ_WRITE_ERROR = 6;
    public static final int VDDK_HOTADD_ADDDISK_DISK_NOT_FOUND = 7;
    public static final int VDDK_HOTADD_ADDDISK_DISK_ADD_FAILED = 8;

    public static final int VDDK_HOTADD_REMOVEDISK_FAILED = 9;
    public static final int VDDK_VIXDISKLIB_INIT_DISKLIB_FAILED = 10;
    public static final int VDDK_VIXDISKLIBVIM_LOAD_DISK_BAD_KEY = 11;

    public static final int VDDK_VIXDISKLIBVIM_BAD_TICKET = 12;
    /**
     * Disable host disk caching.
     */
    public static final int OPEN_UNBUFFERED = (1 << 0);
    /**
     * Open the current link, not the entire chain (hosted disk only).
     */
    public static final int OPEN_SINGLE_LINK = (1 << 1);
    /**
     * Open the virtual disk read-only.
     */
    public static final int OPEN_READ_ONLY = (1 << 2);
    /**
     * Open for NBDSSL transport, zlib compression. Requires vSphere 6.5 or higher
     */
    public static final int OPEN_COMPRESSION_ZLIB = (1 << 4);
    /**
     * Open for NBDSSL transport, fastlz compression. Requires vSphere 6.5 or higher
     */
    public static final int OPEN_COMPRESSION_FASTLZ = (1 << 5);
    /**
     * Open for NBDSSL transport, skipz compression. Requires vSphere 6.5 or higher
     */
    public static final int OPEN_COMPRESSION_SKIPZ = (1 << 6);

    /*
     * VixDiskLib xDisk Types
     */
    public static final int DISK_MONOLITHIC_SPARSE = 1; // monolithic file, sparse
    public static final int DISK_MONOLITHIC_FLAT = 2; // monolithic file,
    // all space pre-allocated
    public static final int DISK_SPLIT_SPARSE = 3; // disk split into 2GB extents,
    // sparse
    public static final int DISK_SPLIT_FLAT = 4; // disk split into 2GB extents,

    /*
     * VixDiskLib Hardware Versions
     */

    // pre-allocated
    public static final int DISK_VMFS_FLAT = 5; // ESX 3.0 and above flat disks

    public static final int DISK_STREAM_OPTIMIZED = 6; // compressed monolithic sparse

    public static final int DISK_UNKNOWN = 256; // unknown type

    /*
     * VixDiskLib Adapter Types
     */
    public static final int ADAPTER_IDE = 1;

    public static final int ADAPTER_SCSI_BUSLOGIC = 2;
    public static final int ADAPTER_SCSI_LSILOGIC = 3;
    public static final int ADAPTER_UNKNOWN = 256;
    // VMware Workstation 4.x and GSX Server 3.x
    public static final int HWVERSION_WORKSTATION_4 = 3;
    // VMware Workstation 5.x and Server 1.x
    public static final int HWVERSION_WORKSTATION_5 = 4;
    // VMware ESX Server 3.0
    public static final int HWVERSION_ESX30 = HWVERSION_WORKSTATION_5;

    // VMware Workstation 6.x
    public static final int HWVERSION_WORKSTATION_6 = 6;

    public static final int HWVERSION_ESX4X = 7;

    public static final int HWVERSION_ESX50 = 8;
    public static final int HWVERSION_ESX51 = 9;
    public static final int HWVERSION_ESX55 = 10;
    public static final int HWVERSION_ESX60 = 11;

    public static final int HWVERSION_ESX65 = 13;

    // Current hardware version
    public static final int HWVERSION_CURRENT = HWVERSION_ESX65;

    public static final int SECTOR_SIZE = 512;

    // Default buffer size (in sectors) for read/write
    public static final int DEFAULT_BUFSIZE = 128;

    // Minimal number of sectors per chunk
    public static final long MIN_CHUNK_SIZE = (64 * 2); // 64K

    // Maximal number of sectors per chunk
    public static final long MAX_CHUNK_SIZE = (64 * 2 * 1024); // 64M

    // Maximal number of chunks per query
    public static final long MAX_CHUNK_NUMBER = (512 * 1024);

    public static Connection createConnectionHandle() {
	return new Connection();
    }

    public static DiskHandle createDiskHandle() {
	return new DiskHandle();
    }

    public ByteBuffer AllocateBuffer(int size, int alignedment);

    public long Attach(DiskHandle parent, DiskHandle child);

    public long CheckRepair(Connection connHandle, String path, boolean repair);

    /*
     * Public accessor functions for VixDiskLib functionality.
     */

    public long Cleanup(ConnectParams connection, int[] numCleaned, int[] numRemaining);

    public long Clone(Connection dstConn, String dstPath, Connection srcConn, String srcPath, CreateParams createParams,
	    Progress progress, boolean overwrite);

    public long Close(DiskHandle handle);

    public long Connect(ConnectParams connectParams, Connection connHandle);

    public long ConnectEx(ConnectParams connectParams, boolean readOnly, String snapshotRef, String transportModes,
	    Connection connHandle);

    public long Create(Connection connHandle, String path, CreateParams createParams, Progress progress);

    public long CreateChild(DiskHandle diskHandle, String childPath, int diskType, Progress progress);

    public long Defragment(DiskHandle diskHandle, Progress progress);

    public long Disconnect(Connection connHandle);

    public long EndAccess(ConnectParams connectParams, String identity);

    /*
     * VixDiskLib convenience functions for error checks.
     */
    public long ErrorCode(long err);

    public void Exit();

    public boolean Failed(long err);

    public long Flush(DiskHandle diskHandle);

    public void FreeBuffer(ByteBuffer buffer);

    public String GetErrorText(long error, String locale);

    public long GetInfo(DiskHandle diskHandle, Info info);

    public String[] GetMetadataKeys(DiskHandle diskHandle);

    public String GetTransportMode(DiskHandle diskHandle);

    public long Grow(Connection connHandle, String path, long capacityInSectors, boolean updateGeometry,
	    Progress progress);

    public long Init(int majorVersion, int minorVersion, JVixLogger logger, String libDir);

    public long InitEx(int majorVersion, int minorVersion, JVixLogger logger, String libDir, String configFile);

    public String ListTransportModes();

    public long Open(Connection connHandle, String path, int flags, DiskHandle handle);

    public void PerturbEnable(String fName, int enable);

    public long PrepareForAccess(ConnectParams connectParams, String identity);

    public long QueryAllocatedBlocks(DiskHandle diskHandle, long startSector, long numSectors, long chunkSize,
	    List<Block> blockList);

    @Deprecated
    public long Read(DiskHandle diskHandle, long startSector, byte[] buffer);

    @Deprecated
    public long Read(DiskHandle diskHandle, long startSector, long numSectors, byte[] buffer);

    public long Read(DiskHandle diskHandle, long startSector, long numSectors, ByteBuffer buffer);

    public long ReadAsync(DiskHandle diskHandle, long startSector, ByteBuffer buffer, int sectorCount,
	    AsyncIOListener callbackObj);

    public String ReadMetadata(DiskHandle diskHandle, String key);

    public long Rename(String src, String dst);

    public long SetInjectedFault(int id, int enabled, int faultError);

    public long Shrink(DiskHandle diskHandle, Progress progress);

    public long SpaceNeededForClone(DiskHandle diskHandle, int diskType, long spaceNeeded[]);

    public boolean Succeeded(long err);

    public long Unlink(Connection connHandle, String path);

    public long Wait(DiskHandle diskHandle);

    @Deprecated
    public long Write(DiskHandle diskHandle, long startSector, byte[] buffer);

    @Deprecated
    public long Write(DiskHandle diskHandle, long startSector, long numSectors, byte[] buffer);

    public long Write(DiskHandle diskHandle, long startSector, long numSectors, ByteBuffer buffer);

    public long WriteAsync(DiskHandle diskHandle, long startSector, ByteBuffer buffer, int sectorCount,
	    AsyncIOListener callbackObj);

    public long WriteMetadata(DiskHandle diskHandle, String key, String val);
}
