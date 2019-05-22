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
 * jDiskLibImpl implements the jDiskLib interface. This allows us to hide all
 * implementation details for the vixDiskLib JNI from users of the interface.
 */

class jDiskLibImpl implements jDiskLib {

    /* Load the jDiskLib shared library. */
    static {
	System.loadLibrary("jDiskLib");
    }

    @Override
    public ByteBuffer AllocateBuffer(final int size, final int alignedment) {
	return AllocateBufferJNI(size, alignedment);
    }

    private native ByteBuffer AllocateBufferJNI(int size, int alignment);

    @Override
    public long Attach(final DiskHandle parent, final DiskHandle child) {
	return AttachJNI(GetDiskHandle(parent), GetDiskHandle(child));
    }

    private native long AttachJNI(long parent, long child);

    /*
     *
     * accessor functions for VixDiskLib functionality.
     *
     */

    private native long BufferReadJNI(long diskHandle, long startSector, long numSectors, ByteBuffer buffer);

    private native long BufferWriteJNI(long diskHandle, long startSector, long numSectors, ByteBuffer buffer);

    @Override
    public long CheckRepair(final Connection connHandle, final String path, final boolean repair) {
	return CheckRepairJNI(GetConnHandle(connHandle), path, repair);
    }

    private native long CheckRepairJNI(long conn, String path, boolean repair);

    @Override
    public long Cleanup(final ConnectParams connection, final int[] numCleaned, final int[] numRemaining) {
	return CleanupJNI(connection, numCleaned, numRemaining);
    }

    private native long CleanupJNI(ConnectParams connection, int[] numCleaned, int[] numRemaining);

    @Override
    public long Clone(final Connection dstConn, final String dstPath, final Connection srcConn, final String srcPath,
	    final CreateParams createParams, final Progress progress, final boolean overwrite) {
	return CloneJNI(GetConnHandle(dstConn), dstPath, GetConnHandle(srcConn), srcPath, createParams, progress,
		overwrite);
    }

    private native long CloneJNI(long dstConnection, String dstPath, long srcConnection, String srcPath,
	    CreateParams createParams, Progress progress, boolean overwrite);

    @Override
    public long Close(final DiskHandle handle) {
	return CloseJNI(GetDiskHandle(handle));
    }

    private native long CloseJNI(long diskHandle);

    @Override
    public long Connect(final ConnectParams connectParams, final Connection connHandle) {
	final long conn[] = new long[1];
	long result;

	if (connHandle == null) {
	    // user wants to pass null to the C function
	    result = ConnectJNI(connectParams, null);
	} else {
	    result = ConnectJNI(connectParams, conn);
	}
	if ((result == VIX_OK) && (connHandle != null)) {
	    connHandle.handle = conn[0];
	}
	return result;
    }

    @Override
    public long ConnectEx(final ConnectParams connectParams, final boolean readOnly, final String snapshotRef,
	    final String transportModes, final Connection connHandle) {
	final long conn[] = new long[1];
	long result;

	if (connHandle == null) {
	    // user wants to pass null to the C function
	    result = ConnectJNI(connectParams, null);
	} else {
	    connHandle.handle = 0;
	    result = ConnectExJNI(connectParams, readOnly, snapshotRef, transportModes, conn);
	    if (result == VIX_OK) {
		connHandle.handle = conn[0];
	    }
	}
	return result;
    }

    private native long ConnectExJNI(ConnectParams connection, boolean readOnly, String snapshotRef,
	    String transportModes, long[] connHandle);

    private native long ConnectJNI(ConnectParams connection, long[] connHandle);

    @Override
    public long Create(final Connection connHandle, final String path, final CreateParams createParams,
	    final Progress progress) {
	return CreateJNI(GetConnHandle(connHandle), path, createParams, progress);
    }

    @Override
    public long CreateChild(final DiskHandle diskHandle, final String childPath, final int diskType,
	    final Progress progress) {
	return CreateChildJNI(GetDiskHandle(diskHandle), childPath, diskType, progress);
    }

    private native long CreateChildJNI(long diskHandle, String childPath, int diskType, Progress progress);

    private native long CreateJNI(long connHandle, String path, CreateParams createParams, Progress progress);

    @Override
    public long Defragment(final jDiskLib.DiskHandle diskHandle, final Progress progress) {
	return DefragmentJNI(GetDiskHandle(diskHandle), progress);
    }

    private native long DefragmentJNI(long diskHandle, Progress progress);

    @Override
    public long Disconnect(final Connection connHandle) {
	return DisconnectJNI(GetConnHandle(connHandle));
    }

    private native long DisconnectJNI(long connHandle);

    @Override
    public long EndAccess(final ConnectParams connectParams, final String identity) {
	return EndAccessJNI(connectParams, identity);
    }

    private native long EndAccessJNI(ConnectParams connection, String identity);

    /*
     * VixDiskLib convenience functions for error checks.
     */
    @Override
    public long ErrorCode(final long err) {
	return err & 0xffff;
    }

    @Override
    public void Exit() {
	ExitJNI();
    }

    private native void ExitJNI();

    @Override
    public boolean Failed(final long err) {
	return err != VIX_OK;
    }

    @Override
    public long Flush(final DiskHandle diskHandle) {
	return FlushJNI(GetDiskHandle(diskHandle));
    }

    private native long FlushJNI(long diskHandle);

    @Override
    public void FreeBuffer(final ByteBuffer buffer) {
	FreeBufferJNI(buffer);
    }

    private native void FreeBufferJNI(ByteBuffer buffer);

    private long GetConnHandle(final Connection connHandle) {
	if (connHandle == null) {
	    return 0;
	} else {
	    return connHandle.handle;
	}
    }

    private long GetDiskHandle(final DiskHandle diskHandle) {
	if (diskHandle == null) {
	    return 0;
	} else {
	    return diskHandle.handle;
	}
    }

    @Override
    public String GetErrorText(final long error, final String locale) {
	return GetErrorTextJNI(error, locale);
    }

    private native String GetErrorTextJNI(long error, String locale);

    @Override
    public long GetInfo(final DiskHandle diskHandle, final Info info) {
	return GetInfoJNI(GetDiskHandle(diskHandle), info);
    }

    private native long GetInfoJNI(long connHandle, Info info);

    @Override
    public String[] GetMetadataKeys(final DiskHandle diskHandle) {
	return GetMetadataKeysJNI(GetDiskHandle(diskHandle));
    }

    private native String[] GetMetadataKeysJNI(long diskHandle);

    @Override
    public String GetTransportMode(final DiskHandle diskHandle) {
	return GetTransportModeJNI(GetDiskHandle(diskHandle));
    }

    private native String GetTransportModeJNI(long diskHandle);

    @Override
    public long Grow(final Connection connHandle, final String path, final long capacityInSectors,
	    final boolean updateGeometry, final Progress progress) {
	return GrowJNI(GetConnHandle(connHandle), path, capacityInSectors, updateGeometry, progress);
    }

    private native long GrowJNI(long connHandle, String path, long capacityInSectors, boolean updateGeometry,
	    Progress progress);

    /*
     * Java Native Interface (JNI) functions provided by the jDiskLib JNI Library.
     * Wrappers are provided above.
     */

    @Override
    public long Init(final int majorVersion, final int minorVersion, final JVixLogger logger, final String libDir) {
	return InitJNI(majorVersion, minorVersion, logger, libDir);
    }

    @Override
    public long InitEx(final int majorVersion, final int minorVersion, final JVixLogger logger, final String libDir,
	    final String configFile) {
	return InitExJNI(majorVersion, minorVersion, logger, libDir, configFile);
    }

    private native long InitExJNI(int majorVersion, int minorVersion, JVixLogger logger, String libDir,
	    String configFile);

    private native long InitJNI(int majorVersion, int minorVersion, JVixLogger logger, String libDir);

    @Override
    public String ListTransportModes() {
	return ListTransportModesJNI();
    }

    private native String ListTransportModesJNI();

    @Override
    public long Open(final Connection connHandle, final String path, final int flags, final DiskHandle handle) {
	final long diskHandle[] = new long[1];
	long result;

	if (handle == null) {
	    // explicitly allow this for code testing purposes
	    result = OpenJNI(GetConnHandle(connHandle), path, flags, null);
	} else {
	    handle.handle = 0;
	    result = OpenJNI(GetConnHandle(connHandle), path, flags, diskHandle);
	    if (result == VIX_OK) {
		handle.handle = diskHandle[0];
	    }
	}
	return result;
    }

    private native long OpenJNI(long connHandle, String path, int flags, long[] diskHandle);

    @Override
    public void PerturbEnable(final String fName, final int enable) {
	PerturbEnableJNI(fName, enable);
    }

    private native void PerturbEnableJNI(String fName, int enable);

    @Override
    public long PrepareForAccess(final ConnectParams connectParams, final String identity) {
	return PrepareForAccessJNI(connectParams, identity);
    }

    private native long PrepareForAccessJNI(ConnectParams connection, String identity);

    @Override
    public long QueryAllocatedBlocks(final DiskHandle diskHandle, final long startSector, final long numSectors,
	    final long chunkSize, final List<Block> blockList) {
	return QueryAllocatedBlocksJNI(GetDiskHandle(diskHandle), startSector, numSectors, chunkSize, blockList);
    }

    private native long QueryAllocatedBlocksJNI(long diskHandle, long startSector, long numSectors, long chunkSize,
	    List<Block> blockList);

    @Override
    @Deprecated
    public long Read(final DiskHandle diskHandle, final long startSector, final byte[] buffer) {
	return Read(diskHandle, startSector, buffer.length / 512, buffer);
    }

    @Override
    @Deprecated
    public long Read(final DiskHandle diskHandle, final long startSector, final long numSectors, final byte[] buffer) {
	return ReadJNI(GetDiskHandle(diskHandle), startSector, numSectors, buffer);
    }

    @Override
    public long Read(final DiskHandle diskHandle, final long startSector, final long numSectors,
	    final ByteBuffer buffer) {
	return BufferReadJNI(GetDiskHandle(diskHandle), startSector, numSectors, buffer);
    }

    @Override
    public long ReadAsync(final DiskHandle diskHandle, final long startSector, final ByteBuffer buffer,
	    final int sectorCount, final AsyncIOListener callbackObj) {
	return ReadAsyncJNI(GetDiskHandle(diskHandle), startSector, buffer, sectorCount, callbackObj);
    }

    private native long ReadAsyncJNI(long diskHandle, long startSector, ByteBuffer buffer, int sectorCount,
	    Object callbackObj);

    private native long ReadJNI(long diskHandle, long startSector, long numSectors, byte[] buffer);

    @Override
    public String ReadMetadata(final DiskHandle diskHandle, final String key) {
	return ReadMetadataJNI(GetDiskHandle(diskHandle), key);
    }

    private native String ReadMetadataJNI(long diskHandle, String key);

    @Override
    public long Rename(final String src, final String dst) {
	return RenameJNI(src, dst);
    }

    private native long RenameJNI(String src, String dst);

    @Override
    public long SetInjectedFault(final int id, final int enabled, final int faultErr) {
	return SetInjectedFaultJNI(id, enabled, faultErr);
    }

    private native long SetInjectedFaultJNI(int id, int enabled, int faultError);

    @Override
    public long Shrink(final DiskHandle diskHandle, final Progress progress) {
	return ShrinkJNI(GetDiskHandle(diskHandle), progress);
    }

    private native long ShrinkJNI(long diskHandle, Progress progress);

    @Override
    public long SpaceNeededForClone(final DiskHandle diskHandle, final int diskType, final long spaceNeeded[]) {
	return SpaceNeededForCloneJNI(GetDiskHandle(diskHandle), diskType, spaceNeeded);
    }

    private native long SpaceNeededForCloneJNI(long diskHandle, int diskType, long spaceNeeded[]);

    @Override
    public boolean Succeeded(final long err) {
	return err == VIX_OK;
    }

    @Override
    public long Unlink(final Connection connHandle, final String path) {
	return UnlinkJNI(GetConnHandle(connHandle), path);
    }

    private native long UnlinkJNI(long connHandle, String path);

    @Override
    public long Wait(final DiskHandle diskHandle) {
	return WaitJNI(GetDiskHandle(diskHandle));
    }

    private native long WaitJNI(long diskHandle);

    @Override
    @Deprecated
    public long Write(final DiskHandle diskHandle, final long startSector, final byte[] buffer) {
	return Write(diskHandle, startSector, buffer.length / 512, buffer);
    }

    @Override
    @Deprecated
    public long Write(final DiskHandle diskHandle, final long startSector, final long numSectors, final byte[] buffer) {
	return WriteJNI(GetDiskHandle(diskHandle), startSector, numSectors, buffer);
    }

    @Override
    public long Write(final DiskHandle diskHandle, final long startSector, final long numSectors,
	    final ByteBuffer buffer) {
	return BufferWriteJNI(GetDiskHandle(diskHandle), startSector, numSectors, buffer);
    }

    @Override
    public long WriteAsync(final DiskHandle diskHandle, final long startSector, final ByteBuffer buffer,
	    final int sectorCount, final AsyncIOListener callbackObj) {
	return WriteAsyncJNI(GetDiskHandle(diskHandle), startSector, buffer, sectorCount, callbackObj);
    }

    private native long WriteAsyncJNI(long diskHandle, long startSector, ByteBuffer buffer, int sectorCount,
	    Object callbackObj);

    private native long WriteJNI(long diskHandle, long startSector, long numSectors, byte[] buffer);

    @Override
    public long WriteMetadata(final DiskHandle diskHandle, final String key, final String val) {
	return WriteMetadataJNI(GetDiskHandle(diskHandle), key, val);
    }

    private native long WriteMetadataJNI(long diskHandle, String key, String val);
}
