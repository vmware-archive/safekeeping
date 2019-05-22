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

/*
 * jMntApiImpl implements the jMntApi interface. This allows us to hide all
 * implementation details for the vixMntApi JNI from users of the interface.
 */

class jMntApiImpl implements jMntApi {

    /*
     *
     * accessor functions for VixMntApi functionality.
     *
     */

    private static class VolumeSetInt {
	public long[] handles;

	public long ptr;

	VolumeSetInt() {
	    this.handles = null;
	}
    }

    @Override
    public long CloseDiskSet(final DiskSetHandle diskSet) {
	return CloseDiskSetJNI(GetDiskSetHandle(diskSet));
    }

    private native long CloseDiskSetJNI(long diskSet);

    @Override
    public long DismountVolume(final VolumeHandle volumeHandle, final boolean force) {
	return DismountVolumeJNI(GetVolHandle(volumeHandle), force);
    }

    private native long DismountVolumeJNI(long volumeHandle, boolean force);

    @Override
    public void Exit() {
	ExitJNI();
    }

    private native void ExitJNI();

    @Override
    public void FreeOsInfo(final OsInfo osInfo) {
	FreeOsInfoJNI(osInfo == null ? null : osInfo.ptr);
    }

    private native void FreeOsInfoJNI(long osInfo);

    @Override
    public void FreeVolumeHandles(final VolumeSet volumes) {
	FreeVolumeHandlesJNI(volumes == null ? null : volumes.ptr);
    }

    private native void FreeVolumeHandlesJNI(long volumes);

    @Override
    public void FreeVolumeInfo(final VolumeInfo volumeInfo) {
	FreeVolumeInfoJNI(volumeInfo == null ? null : volumeInfo.ptr);
    }

    private native void FreeVolumeInfoJNI(long volumeInfo);

    private long GetConnHandle(final jDiskLib.Connection conn) {
	return conn == null ? null : conn.handle;
    }

    private long GetDiskHandle(final jDiskLib.DiskHandle disk) {
	return disk == null ? null : disk.handle;
    }

    private long GetDiskSetHandle(final DiskSetHandle diskSet) {
	return diskSet == null ? null : diskSet.handle;
    }

    @Override
    public long GetOsInfo(final DiskSetHandle diskSet, final OsInfo osInfo) {
	return GetOsInfoJNI(GetDiskSetHandle(diskSet), osInfo);
    }

    private native long GetOsInfoJNI(long diskSet, OsInfo osInfo);

    /*
     * Java Native Interface (JNI) functions provided by the jDiskLib JNI Library
     * for VixMnt. Wrappers are provided above.
     */

    private long GetVolHandle(final VolumeHandle vol) {
	return vol == null ? null : vol.handle;
    }

    @Override
    public long GetVolumeHandles(final DiskSetHandle diskSet, final VolumeSet volumes) {
	VolumeSetInt volumesInt = null; // new VolumeSetInt();
	long result;
	int i;

	if (volumes != null) {
	    volumesInt = new VolumeSetInt();
	}
	result = GetVolumeHandlesJNI(GetDiskSetHandle(diskSet), volumesInt);

	if ((volumesInt != null) && (volumesInt.handles != null) && (volumesInt.handles.length > 0)) {

	    volumes.volumes = new VolumeHandle[volumesInt.handles.length];
	    for (i = 0; i < volumesInt.handles.length; i++) {
		volumes.volumes[i] = new VolumeHandle(volumesInt.handles[i]);
	    }
	    volumes.ptr = volumesInt.ptr;
	}

	return result;
    }

    private native long GetVolumeHandlesJNI(long diskSet, VolumeSetInt volumes);

    @Override
    public long GetVolumeInfo(final VolumeHandle volumeHandle, final VolumeInfo volumeInfo) {
	return GetVolumeInfoJNI(GetVolHandle(volumeHandle), volumeInfo);
    }

    private native long GetVolumeInfoJNI(long volumeHandle, VolumeInfo volumeInfo);

    @Override
    public long Init(final int majorVersion, final int minorVersion, final JVixLogger logger, final String libDir,
	    final String tmpDir) {
	return InitJNI(majorVersion, minorVersion, logger, libDir, tmpDir);
    }

    private native long InitJNI(int majorVersion, int minorVersion, JVixLogger logger, String libDir, String tmpDir);

    @Override
    public long MountVolume(final VolumeHandle volumeHandle, final boolean isReadOnly) {
	return MountVolumeJNI(GetVolHandle(volumeHandle), isReadOnly);
    }

    private native long MountVolumeJNI(long volumeHandle, boolean isReadOnly);

    @Override
    public long OpenDisks(final jDiskLib.Connection vixConnection, final String[] diskNames, final int openMode,
	    final DiskSetHandle diskSet) {
	long[] diskSetHlp = null;
	long result;

	if (diskSet != null) {
	    diskSetHlp = new long[1];
	}
	result = OpenDisksJNI(GetConnHandle(vixConnection), diskNames, openMode, diskSetHlp);

	if (diskSet != null) {
	    diskSet.handle = diskSetHlp[0];
	}
	return result;
    }

    @Override
    public long OpenDiskSet(final jDiskLib.DiskHandle[] diskHandles, final int openMode, final DiskSetHandle diskSet) {
	long[] handles = null;
	final long[] diskSetHlp = new long[1];
	int i;
	long result;

	if ((diskHandles != null) && (diskHandles.length > 0)) {
	    handles = new long[diskHandles.length];
	    for (i = 0; i < diskHandles.length; i++) {
		handles[i] = GetDiskHandle(diskHandles[i]);
	    }
	}

	result = OpenDiskSetJNI(handles, openMode, diskSetHlp);
	if (diskSet != null) {
	    diskSet.handle = diskSetHlp[0];
	}
	return result;
    }

    private native long OpenDiskSetJNI(long[] diskHandles, int openMode, long[] diskSet);

    private native long OpenDisksJNI(long vixConnection, String[] diskNames, int openMode, long[] diskSet);
}
