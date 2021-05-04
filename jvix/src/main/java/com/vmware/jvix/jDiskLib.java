/******************************************************************************
 * Copyright 2008-2020 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * jDiskLib.java  --
 *
 *      Implementation of the Java component of the vixDiskLib Java API.
 *      jDiskLib defines the interface.
 *
 *******************************************************************************/
package com.vmware.jvix;

import java.nio.ByteBuffer;
import java.util.List;

/*
 * Interface into vixDiskLib for Java programmers
 */

public interface jDiskLib {

    public static class Block {
        public long offset;
        public long length;

        public Block() {
            this.offset = 0;
            this.length = 0;
        }

        public Block(final long begin, final long end) {
            this.offset = begin;
            this.length = end - begin;
        }

        public long getBegin() {
            return this.offset;
        }

        public long getLastBlock() {
            return (this.offset + this.length) - 1;
        }

        public long getLength() {
            return this.length;
        }

        public boolean isNul() {
            return this.length == 0;
        }

    }

    /*
     * Wrapper type to achieve some level of type safety for DiskLib Connections.
     */
    public static class Connection {
        protected long handle;

        public Connection() {
            this.handle = 0;
        }

        public boolean isValid() {
            return this.handle != 0;
        }
    }

    /*
     * VixDiskLib Error Constants
     */

    /*
     * Input Parameters for a Connect call.
     */
    public static class ConnectParams {
        // use userid password
        public static final int VIXDISKLIB_CRED_UID = 1;
        // http session id
        public static final int VIXDISKLIB_CRED_SESSIONID = 2;
        // vim ticket id
        public static final int VIXDISKLIB_CRED_TICKETID = 3;
        // Windows only - use current thread credentials.
        public static final int VIXDISKLIB_CRED_SSPI = 4;

        public static final int VIXDISKLIB_CRED_UNKNOWN = 256;

        public static final int VIXDISKLIB_SPEC_VMX = 0;

        public static final int VIXDISKLIB_SPEC_VSTORAGE_OBJECT = 1;

        // internal spec
        public static final int VIXDISKLIB_SPEC_DATASTORE = 128;
        // must be last
        public static final int VIXDISKLIB_SPEC_UNKNOWN = 256;

        public static int getVixdisklibCredSessionid() {
            return VIXDISKLIB_CRED_SESSIONID;
        }

        private int credType;

        // spec
        private String vmxSpec;

        private String id;

        private String datastoreMoRef;

        private String ssId;

        private int specType;

        private String serverName;

        private String thumbPrint;
        // No default -- Set to 443
        private int port;
        // No default -- Set to 902
        private int nfcHostPort;

        private String username;

        private String password;

        private String key;

        private String cookie;

        public ConnectParams() {
            this.credType = VIXDISKLIB_CRED_UID;
        }

        public String getCookie() {
            return this.cookie;
        }

        public int getCredType() {
            return this.credType;
        }

        public String getDatastoreMoRef() {
            return this.datastoreMoRef;
        }

        public String getId() {
            return this.id;
        }

        public String getKey() {
            return this.key;
        }

        public int getNfcHostPort() {
            return this.nfcHostPort;
        }

        public String getPassword() {
            return this.password;
        }

        public int getPort() {
            return this.port;
        }

        public String getServerName() {
            return this.serverName;
        }

        public int getSpecType() {
            return this.specType;
        }

        public String getSsId() {
            return this.ssId;
        }

        public String getThumbPrint() {
            return this.thumbPrint;
        }

        public String getUsername() {
            return this.username;
        }

        public String getVmxSpec() {
            return this.vmxSpec;
        }

        public void setCookie(final String cookie) {
            this.cookie = cookie;
        }

        public void setCredType(final int credType) {
            this.credType = credType;
        }

        public void setDatastoreMoRef(final String datastoreMoRef) {
            this.datastoreMoRef = datastoreMoRef;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public void setKey(final String key) {
            this.key = key;
        }

        public void setNfcHostPort(final int nfcHostPort) {
            this.nfcHostPort = nfcHostPort;
        }

        public void setPassword(final String password) {
            this.password = password;
        }

        public void setPort(final int port) {
            this.port = port;
        }

        public void setServerName(final String serverName) {
            this.serverName = serverName;
        }

        public void setSpecType(final int specType) {
            this.specType = specType;
        }

        public void setSsId(final String ssId) {
            this.ssId = ssId;
        }

        public void setThumbPrint(final String thumbPrint) {
            this.thumbPrint = thumbPrint;
        }

        public void setUsername(final String username) {
            this.username = username;
        }

        public void setVmxSpec(final String vmxSpec) {
            this.vmxSpec = vmxSpec;
        }
    }

    /*
     * Properties for creating and cloning of disks.
     */
    public static class CreateParams {
        private int diskType;
        private int adapterType;
        private int hwVersion;
        private long capacityInSectors;
        private int logicalSectorSize;
        private int physicalSectorSize;

        public int getAdapterType() {
            return this.adapterType;
        }

        public long getCapacityInSectors() {
            return this.capacityInSectors;
        }

        public int getDiskType() {
            return this.diskType;
        }

        public int getHwVersion() {
            return this.hwVersion;
        }

        public int getLogicalSectorSize() {
            return this.logicalSectorSize;
        }

        public int getPhysicalSectorSize() {
            return this.physicalSectorSize;
        }

        public void setAdapterType(final int adapterType) {
            this.adapterType = adapterType;
        }

        public void setCapacityInSectors(final long capacityInSectors) {
            this.capacityInSectors = capacityInSectors;
        }

        public void setDiskType(final int diskType) {
            this.diskType = diskType;
        }

        public void setHwVersion(final int hwVersion) {
            this.hwVersion = hwVersion;
        }

        public void setLogicalSectorSize(final int logicalSectorSize) {
            this.logicalSectorSize = logicalSectorSize;
        }

        public void setPhysicalSectorSize(final int physicalSectorSize) {
            this.physicalSectorSize = physicalSectorSize;
        }
    }

    /*
     * Wrapper type to achieve some level of type safety for DiskHandles
     */
    public static class DiskHandle {
        protected long handle;

        public DiskHandle() {
            this.handle = 0;
        }

        /**
         *
         * @param _handle
         */
        public DiskHandle(final long _handle) {
            this.handle = _handle;
        }

        public Long getHandle() {
            return this.handle;
        }

        public boolean isValid() {
            return this.handle != 0;
        }
    }

    /* Fault Injection Codes */
    public enum FaultInjectionType {
        VDDK_SAN_SERVER_CONNECT_ERROR, VDDK_SAN_BLKLIST_INIT_ERROR, VDDK_SAN_STARTIO_ERROR, VDDK_SAN_UNKNOWN_ADAPTER,
        VDDK_SAN_DISK_OPEN_ERROR, VDDK_SAN_ASYNC_READ_WRITE_ERROR, VDDK_SAN_READ_WRITE_ERROR,
        VDDK_HOTADD_ADDDISK_DISK_NOT_FOUND, VDDK_HOTADD_ADDDISK_DISK_ADD_FAILED, VDDK_HOTADD_REMOVEDISK_FAILED,
        VDDK_VIXDISKLIB_INIT_DISKLIB_FAILED, VDDK_VIXDISKLIBVIM_LOAD_DISK_BAD_KEY, VDDK_VIXDISKLIBVIM_BAD_TICKET,
        VDDK_HOTADD_AHCI_ONLY,

        VDDK_VIXDISKLIB_VIXDISKLIB_INIT_DISKLIB_FAILED, VDDK_VIXDISKLIB_VIXDISKLIB_INIT_SSL_FAILED,
        VDDK_VIXDISKLIB_VIXDISKLIB_DISKLIB_CLONE_FAILED, VDDK_VIXDISKLIB_VIXDISKLIB_CREATECHILD_GETINFO_FAILED,
        VDDK_VIXDISKLIB_VIXDISKLIB_CREATECHILD_FAILED, VDDK_VIXDISKLIB_VIXDISKLIB_GETINFO_FAILED,
        VDDK_VIXDISKLIB_VIXDISKLIB_CONNECT_NO_MEMORY_VMXSPEC, VDDK_VIXDISKLIB_VIXDISKLIB_CONNECT_NO_MEMORY_FCD,
        VDDK_VIXDISKLIB_VIXDISKLIB_CONNECT_NO_MEMORY_RDS, VDDK_VIXDISKLIB_VIXDISKLIB_CONNECT_NO_MEMORY_SERVER,
        VDDK_VIXDISKLIB_VIXDISKLIB_CONNECT_NO_MEMORY_CONN, VDDK_VIXDISKLIB_VIXDISKLIB_CONNECT_NO_MEMORY_CRED_UID,
        VDDK_VIXDISKLIB_VIXDISKLIB_CONNECT_NO_MEMORY_CRED_SESSIONID,
        VDDK_VIXDISKLIB_VIXDISKLIB_CONNECT_NO_MEMORY_CRED_TICKETID_ALL,
        VDDK_VIXDISKLIB_VIXDISKLIB_CONNECT_NO_MEMORY_CRED_TICKETID, VDDK_VIXDISKLIB_VIXDISKLIB_OPENWITHINFO_FAIL,
        VDDK_VIXDISKLIB_VIXDISKLIB_SPACEUSED_FAIL, VDDK_VIXDISKLIB_VIXDISKLIB_PUTFILE_NO_SPACE,
        VDDK_VIXDISKLIB_VIXDISKLIB_PUTFILE_START_SESSION_FAIL, VDDK_VIXDISKLIB_VIXDISKLIB_PUTFILE_FAIL,
        VDDK_VIXDISKLIB_VIXDISKLIB_PUTFILE_OPEN_FAIL, VDDK_VIXDISKLIB_VIXDISKLIB_PUTFILE_UPDADAPTER_FAIL,
        VDDK_VIXDISKLIB_VIXDISKLIB_PUTFILE_UPDVERSION_FAIL, VDDK_VIXDISKLIB_VIXDISKLIB_PUTFILE_FILEEXIST,
        VDDK_VIXDISKLIB_VIXDISKLIB_GETFILE_STARTSESSION_FAIL, VDDK_VIXDISKLIB_VIXDISKLIB_GETFILE_GETENCRYPT_FAIL,
        VDDK_VIXDISKLIB_VIXDISKLIB_CLONELOCAL_OPEN_FAIL, VDDK_VIXDISKLIB_VIXDISKLIB_SPACENEEDEDFORCLONE_FAIL,
        VDDK_VIXDISKLIB_VIXDISKLIB_ATTACH_FAIL, VDDK_VIXDISKLIB_VIXDISKLIB_WAIT_FAIL,
        VDDK_VIXDISKLIB_VIXDISKLIB_NOAVAILABLEMODES, VDDK_VIXDISKLIB_VIXDISKLIB_ADDTHUMB_INVPARAM,
        VDDK_VIXDISKLIB_VIXDISKLIB_ADDTHUMB_INVTHUMB, VDDK_VIXDISKLIB_VIXDISKLIB_ADDTHUMB_OPENDB_FAIL,
        VDDK_VIXDISKLIB_VIXDISKLIB_ADDTHUMB_RETHOST_FAIL, VDDK_VIXDISKLIB_VIXDISKLIB_ADDTHUMB_ADDTHUMB_FAIL,
        VDDK_VIXDISKLIB_VIXDISKLIB_OPEN_PLUGIN_FAIL, VDDK_VIXDISKLIB_VIXDISKLIB_INIT_TRANSPORT_FAIL,
        VDDK_VIXDISKLIB_VIXDISKLIB_RELEASEDISKTOKEN_FAIL, VDDK_VIXDISKLIB_VIXDISKLIB_PUSHCRYPTOKEY_FAIL,
        VDDK_VIXDISKLIB_VIXDISKLIB_READMETADATA_FAIL, VDDK_VIXDISKLIB_VIXDISKLIB_CREATESESSION_FAIL_AGENT,
        VDDK_VIXDISKLIB_VIXDISKLIB_CREATESESSION_FAIL_STATUS, VDDK_VIXDISKLIB_VIXDISKLIB_UNLINK_FILENOTEXIST,
        VDDK_VIXDISKLIB_ALCBLOCK_OPEN_GETINFO_FAILED, VDDK_VIXDISKLIB_ALCBLOCK_QUERY_NBDGETALC_FAILED,
        VDDK_VIXDISKLIB_ALCBLOCK_QUERY_ALLOC_FAILED, VDDK_VIXDISKLIB_ALCBLOCK_CLOSE_NBDCLOSE_FAILED,
        VDDK_VCBLIB_HOTADD_RECONFIG_FAIL, VDDK_VCBLIB_HOTADD_ADDDISK_DISK_NOT_FOUND,
        VDDK_VCBLIB_HOTADD_REMOVEDISK_FAILED, VDDK_VCBLIB_HOTADD_ADDDISK_DISK_ADD_FAILED,
        VDDK_VCBLIB_HOTADD_ACQUIRE_LOCK_FAIL, VDDK_VCBLIB_HOTADD_ALLOCATESCSITARGET_FAIL,
        VDDK_BLOCKLISTVMOMI_SANMP_WRITE_FAIL, VDDK_BLOCKLISTVMOMI_SANMP_PATH_INACTIVE,
        VDDK_BLOCKLISTVMOMI_MAPTABLE_ASYNCALCBLOCKS_FAILED, VDDK_BLOCKLISTVMOMI_MAPTABLE_ASYNCWRITE_FAIL,
        VDDK_BLOCKLISTVMOMI_ASYNCWRITE_UPDATEALCMAP_FAIL, VDDK_BLOCKLISTVMOMI_ASYNCWRITE_STARTTHREADS_FAIL,
        VDDK_PLUGIN_SAN_SERVER_CONNECT_FAIL, VDDK_PLUGIN_SAN_BLKLIST_INIT_FAIL, VDDK_PLUGIN_SAN_STARTIO_FAIL,
        VDDK_PLUGIN_SAN_UNKNOWN_ADAPTER, VDDK_PLUGIN_SAN_DISK_OPEN_FAIL,
        VDDK_VIMACCESS_SESSION_GETNFCTICKET_FAIL_NO_DISKSPEC, VDDK_VIMACCESS_SESSION_GETNFCTICKET_FAIL_NO_TICKET,
        VDDK_VIMACCESS_SESSION_GETNFCTICKET_FAIL_NO_SERVICE, VDDK_VIMACCESS_SESSION_GETNFCTICKET_FAIL_NO_SESSIONID,
        VDDK_VIMACCESS_SESSION_GETFILENAME_FAIL_NO_DISKSPEC, VDDK_VIMACCESS_SESSION_GETFILENAME_FAIL_NO_FILENAME,
        VDDK_VIMACCESS_SESSION_GETABOUTINFO_FAIL_NO_CONTENT, VDDK_VIMACCESS_SESSION_GETABOUTINFO_FAIL_NO_ABOUT
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
        public long logicalSectorSize;
        public long physicalSectorSize;

        public Info() {
            this.biosGeo = new Geometry();
            this.physGeo = new Geometry();
        }
    }

    ByteBuffer allocateBuffer(int size, int alignedment);

    long attach(DiskHandle parent, DiskHandle child);

    long checkRepair(Connection connHandle, String path, boolean repair);

    /*
     * accessory functions for VixDiskLib functionality.
     */

    CleanUpResults cleanup(final ConnectParams connectParams);

    long cleanup(ConnectParams connection, int[] numCleaned, int[] numRemaining);

    long clone(Connection dstConn, String dstPath, Connection srcConn, String srcPath, CreateParams createParams,
            Progress progress, boolean overwrite);

    long close(DiskHandle handle);

    long connect(ConnectParams connectParams, Connection connHandle);

    long connectEx(ConnectParams connectParams, boolean readOnly, String snapshotRef, String transportModes,
            Connection connHandle);

    long create(Connection connHandle, String path, CreateParams createParams, Progress progress);

    long createChild(DiskHandle diskHandle, String childPath, int diskType, Progress progress);

    long defragment(DiskHandle diskHandle, Progress progress);

    long disconnect(Connection connHandle);

    long endAccess(ConnectParams connectParams, String identity);

    /*
     * VixDiskLib convenience functions for error checks.
     */
    long errorCode(long err);

    void exit();

    boolean failed(long err);

    long flush(DiskHandle diskHandle);

    void freeBuffer(ByteBuffer buffer);

    long getConnectParams(Connection connHandle, ConnectParams connectParams);

    long getConnHandle(final Connection connHandle);

    long getDiskHandle(final DiskHandle diskHandle);

    String getErrorText(long error, String locale);

    long getInfo(DiskHandle diskHandle, Info info);

    String[] getMetadataKeys(DiskHandle diskHandle);

    String getTransportMode(DiskHandle diskHandle);

    long grow(Connection connHandle, String path, long capacityInSectors, boolean updateGeometry, Progress progress);

    long init(int majorVersion, int minorVersion, JVixLogger jvixLogger, String libDir);

    long init(JVixLogger jvixLogger);

    long initEx(int majorVersion, int minorVersion, JVixLogger jvixLogger, String libDir, String configFile);

    long initEx(JVixLogger jvixLogger, String configFile);

    long isAttachPossible(DiskHandle parent, DiskHandle child);

    String listTransportModes();

    long open(Connection connHandle, String path, int flags, DiskHandle handle);

    void perturbEnable(String fName, int enable);

    long prepareForAccess(ConnectParams connectParams, String identity);

    long queryAllocatedBlocks(DiskHandle diskHandle, long startSector, long numSectors, long chunkSize,
            List<Block> blockList);

    @Deprecated
    long read(DiskHandle diskHandle, long startSector, byte[] buffer);

    long read(DiskHandle diskHandle, long startSector, long numSectors, byte[] buffer);

    long read(DiskHandle diskHandle, long startSector, long numSectors, ByteBuffer buffer);

    long readAsync(DiskHandle diskHandle, long startSector, ByteBuffer buffer, int sectorCount,
            AsyncIOListener callbackObj);

    long readMetadata(DiskHandle diskHandle, String key, StringBuffer val);

    long rename(String src, String dst);

    long setInjectedFault(FaultInjectionType id, int enabled, int faultError);

    long shrink(DiskHandle diskHandle, Progress progress);

    long spaceNeededForClone(DiskHandle diskHandle, int diskType, long[] spaceNeeded);

    boolean succeeded(long err);

    long unlink(Connection connHandle, String path);

    long wait(DiskHandle diskHandle);

    @Deprecated
    long write(DiskHandle diskHandle, long startSector, byte[] buffer);

    long write(DiskHandle diskHandle, long startSector, long numSectors, byte[] buffer);

    long write(DiskHandle diskHandle, long startSector, long numSectors, ByteBuffer buffer);

    long writeAsync(DiskHandle diskHandle, long startSector, ByteBuffer buffer, int sectorCount,
            AsyncIOListener callbackObj);

    long writeMetadata(DiskHandle diskHandle, String key, String val);

}
