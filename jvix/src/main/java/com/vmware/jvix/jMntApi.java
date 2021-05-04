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

import com.vmware.jvix.jDiskLib.Connection;
import com.vmware.jvix.jDiskLib.DiskHandle;

public interface jMntApi {

	public static class DiskSetHandle {
		protected long handle;

		public DiskSetHandle() {
			this.handle = 0;
		}

		public boolean IsValid() {
			return this.handle != 0;
		}
	}

	public static class OsInfo {
		public int family; /* VixOsFamily type */
		public int majorVersion; // On Windows, 4=NT, 5=2000 and above
		public int minorVersion; // On Windows, 0=2000, 1=XP, 2=2003
		public boolean osIs64Bit; // True if the OS is 64-bit
		public String vendor; // e.g. Microsoft, RedHat, etc...
		public String edition; // e.g. Desktop, Enterprise, etc...
		public String osFolder; // Location where the default OS is installed
		protected long ptr; // C structure for testing FreeOsInfo
	}

	public static class VolumeHandle {
		protected long handle;

		public VolumeHandle() {
			this.handle = 0;
		}

		protected VolumeHandle(final long h) {
			this.handle = h;
		}

		public boolean IsValid() {
			return this.handle != 0;
		}
	}

	/* Volume information */
	public static class VolumeInfo {
		public int type; /* VixVolumeType */

		public boolean isMounted; // True if the volume is mounted on the proxy.
		public String symbolicLink; // Path to the volume mount point,
		// NULL if the volume is not mounted on the proxy.
		public String[] inGuestMountPoints; // Mount points for the volume in the guest
		protected long ptr; // C structure for testing FreeVolumeInfo

		public VolumeInfo() {
			this.ptr = 0;
		}
	}

	public static class VolumeSet {
		public VolumeHandle[] volumes;

		protected long ptr; // C structure for testing FreeVolumeSet

		public VolumeSet() {
			this.volumes = null;
			this.ptr = 0;
		}
	}

	int VIXMNTAPI_MAJOR_VERSION = 1;
	int VIXMNTAPI_MINOR_VERSION = 0;
	/* VixOsFamily types */
	int VIXMNTAPI_NO_OS = 0;
	int VIXMNTAPI_WINDOWS = 1;
	int VIXMNTAPI_LINUX = 2;
	int VIXMNTAPI_NETWARE = 3;

	int VIXMNTAPI_SOLARIS = 4;
	int VIXMNTAPI_FREEBSD = 5;
	int VIXMNTAPI_OS2 = 6;
	int VIXMNTAPI_DARWIN = 7;
	int VIXMNTAPI_OTHER = 8;

	/* VixVolumeType */
	int VIXMNTAPI_UNKNOWN_VOLTYPE = 0;

	int VIXMNTAPI_BASIC_PARTITION = 1;

	/*
	 * Wrapper types to achieve some level of type safety for DiskSetHandle,
	 * VolumeHandle and VolumeSet.
	 *
	 * Note that VolumeSet does not exist in C Land. We need it in Java because Java
	 * does not have a concept of returning values in arguments.
	 *
	 * Also note that VolumeSet has a protected "ptr" that holds on to the VolumeSet
	 * list in C-Land. While this is "un-Java" in terms of using a garbage-collected
	 * language (and is actually not required), we need it in order to enable QA to
	 * reasonably test FreeVolumeSet.
	 */

	int VIXMNTAPI_GPT_PARTITION = 2;

	int VIXMNTAPI_DYNAMIC_VOLUME = 3;

	int VIXMNTAPI_LVM_VOLUME = 4;

	/*
	 * Public accessor functions for VixDiskLib functionality.
	 */

	long CloseDiskSet(DiskSetHandle diskSet);

	long DismountVolume(VolumeHandle volumeHandle, boolean force);

	void Exit();

	void FreeOsInfo(OsInfo osInfo);

	void FreeVolumeHandles(VolumeSet volumes);

	void FreeVolumeInfo(VolumeInfo volumeInfo);

	long GetOsInfo(DiskSetHandle diskSet, OsInfo osInfo);

	long GetVolumeHandles(DiskSetHandle diskSet, VolumeSet volumes);

	long GetVolumeInfo(VolumeHandle volumeHandle, VolumeInfo volumeInfo);

	long Init(int majorVersion, int minorVersion, JVixLogger logger, String libDir, String tmpDir);

	long MountVolume(VolumeHandle volumeHandle, boolean isReadOnly);

	long OpenDisks(Connection vixConn, String[] diskNames, int openMode, DiskSetHandle diskSet);

	long OpenDiskSet(DiskHandle[] diskHandles, int openMode, DiskSetHandle diskSet);
}
