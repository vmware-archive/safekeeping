/*******************************************************************************
 * Copyright (C) 2021, VMware Inc
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
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * jDiskLibImpl implements the jDiskLib interface. This allows us to hide all
 * implementation details for the vixDiskLib JNI from users of the interface.
 */

public class JDisk extends jDiskLibImpl {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(JDisk.class.getName());

	private final NativeLibraryVersion vddkVersion;

	/**
	 * @param nativeVersion
	 */
	public JDisk(final NativeLibraryVersion vddkVersion) {
		this.vddkVersion = vddkVersion;
	}

	@Override
	public ByteBuffer allocateBuffer(final int size, final int alignedment) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("int, int - start"); //$NON-NLS-1$
		}

		final ByteBuffer returnByteBuffer = AllocateBufferJNI(size, alignedment);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("int, int - end"); //$NON-NLS-1$
		}
		return returnByteBuffer;
	}

	@Override
	public long attach(final DiskHandle parent, final DiskHandle child) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, DiskHandle - start"); //$NON-NLS-1$
		}

		final long returnlong = AttachJNI(getDiskHandle(parent), getDiskHandle(child));
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, DiskHandle - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long checkRepair(final Connection connHandle, final String path, final boolean repair) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("Connection, String, boolean - start"); //$NON-NLS-1$
		}

		final long returnlong = CheckRepairJNI(getConnHandle(connHandle), path, repair);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("Connection, String, boolean - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public CleanUpResults cleanup(final ConnectParams connectParams) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("ConnectParams - start"); //$NON-NLS-1$
		}

		final CleanUpResults result = new CleanUpResults();
		if (connectParams == null) {
			result.setVddkCallResult(-1);
		} else {
			final int[] numCleaned = new int[2];
			final int[] numRemaining = new int[2];
			final long vddkCallResult = CleanupJNI(connectParams, numCleaned, numRemaining);
			result.setNumCleaned(numCleaned[0]);
			result.setNumRemaining(numRemaining[0]);
			result.setVddkCallResult(vddkCallResult);
			if (vddkCallResult != jDiskLibConst.VIX_OK) {
				final String msg = getErrorText(vddkCallResult, null);
				logger.warning(msg);
			} else {
				logger.log(Level.INFO, () -> String.format("Cleanup result: Object Cleaned: %d , Object Remaining: %d",
						result.getNumCleaned(), result.getNumRemaining()));
			}
		}
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("ConnectParams - end"); //$NON-NLS-1$
		}
		return result;

	}

	@Override
	public long cleanup(final ConnectParams connection, final int[] numCleaned, final int[] numRemaining) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("ConnectParams, int[], int[] - start"); //$NON-NLS-1$
		}

		final long returnlong = CleanupJNI(connection, numCleaned, numRemaining);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("ConnectParams, int[], int[] - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long clone(final Connection dstConn, final String dstPath, final Connection srcConn, final String srcPath,
			final CreateParams createParams, final Progress progress, final boolean overwrite) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("Connection, String, Connection, String, CreateParams, Progress, boolean - start"); //$NON-NLS-1$
		}

		final long returnlong = CloneJNI(getConnHandle(dstConn), dstPath, getConnHandle(srcConn), srcPath, createParams,
				progress, overwrite);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("Connection, String, Connection, String, CreateParams, Progress, boolean - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long close(final DiskHandle handle) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle - start"); //$NON-NLS-1$
		}

		final long returnlong = CloseJNI(getDiskHandle(handle));
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long connect(final ConnectParams connectParams, final Connection connHandle) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("ConnectParams, Connection - start"); //$NON-NLS-1$
		}

		final long[] conn = new long[1];
		long result;

		if (connHandle == null) {
			// user wants to pass null to the C function
			result = ConnectJNI(connectParams, null);
		} else {
			result = ConnectJNI(connectParams, conn);
		}
		if ((result == jDiskLibConst.VIX_OK) && (connHandle != null)) {
			connHandle.handle = conn[0];
		}

		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("ConnectParams, Connection - end"); //$NON-NLS-1$
		}
		return result;
	}

	@Override
	public long connectEx(final ConnectParams connectParams, final boolean readOnly, final String snapshotRef,
			final String transportModes, final Connection connHandle) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("ConnectParams, boolean, String, String, Connection - start"); //$NON-NLS-1$
		}

		long result;
		if ((connectParams.getSpecType() == ConnectParams.VIXDISKLIB_SPEC_DATASTORE)
				&& ((this.vddkVersion == NativeLibraryVersion.VDDK67)
						|| (this.vddkVersion == NativeLibraryVersion.VDDK65))) {
			result = 0;
		} else {
			final long[] conn = new long[1];

			if (connHandle == null) {
				// user wants to pass null to the C function
				result = ConnectJNI(connectParams, null);
			} else {
				connHandle.handle = 0;
				result = ConnectExJNI(connectParams, readOnly, snapshotRef, transportModes, conn);
				if (result == jDiskLibConst.VIX_OK) {
					connHandle.handle = conn[0];
				}
			}
		}

		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("ConnectParams, boolean, String, String, Connection - end"); //$NON-NLS-1$
		}
		return result;
	}

	@Override
	public long create(final Connection connHandle, final String path, final CreateParams createParams,
			final Progress progress) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("Connection, String, CreateParams, Progress - start"); //$NON-NLS-1$
		}

		final long returnlong = CreateJNI(getConnHandle(connHandle), path, createParams, progress);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("Connection, String, CreateParams, Progress - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long createChild(final DiskHandle diskHandle, final String childPath, final int diskType,
			final Progress progress) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, String, int, Progress - start"); //$NON-NLS-1$
		}

		final long returnlong = CreateChildJNI(getDiskHandle(diskHandle), childPath, diskType, progress);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, String, int, Progress - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long defragment(final jDiskLib.DiskHandle diskHandle, final Progress progress) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("jDiskLib.DiskHandle, Progress - start"); //$NON-NLS-1$
		}

		final long returnlong = DefragmentJNI(getDiskHandle(diskHandle), progress);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("jDiskLib.DiskHandle, Progress - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long disconnect(final Connection connHandle) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("Connection - start"); //$NON-NLS-1$
		}

		final long returnlong = DisconnectJNI(getConnHandle(connHandle));
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("Connection - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long endAccess(final ConnectParams connectParams, final String identity) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("ConnectParams, String - start"); //$NON-NLS-1$
		}

		final long returnlong = EndAccessJNI(connectParams, identity);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("ConnectParams, String - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	/*
	 * VixDiskLib convenience functions for error checks.
	 */
	@Override
	public long errorCode(final long err) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("long - start"); //$NON-NLS-1$
		}

		final long returnlong = err & 0xffff;
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("long - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public void exit() {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("<no args> - start"); //$NON-NLS-1$
		}

		ExitJNI();

		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("<no args> - end"); //$NON-NLS-1$
		}
	}

	@Override
	public boolean failed(final long err) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("long - start"); //$NON-NLS-1$
		}

		final boolean returnboolean = err != jDiskLibConst.VIX_OK;
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("long - end"); //$NON-NLS-1$
		}
		return returnboolean;
	}

	@Override
	public long flush(final DiskHandle diskHandle) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle - start"); //$NON-NLS-1$
		}

		final long returnlong = FlushJNI(getDiskHandle(diskHandle));
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public void freeBuffer(final ByteBuffer buffer) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("ByteBuffer - start"); //$NON-NLS-1$
		}

		FreeBufferJNI(buffer);

		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("ByteBuffer - end"); //$NON-NLS-1$
		}
	}

	@Override
	public long getConnectParams(final Connection connHandle, final ConnectParams connectParams) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("Connection, ConnectParams - start"); //$NON-NLS-1$
		}

		final long returnlong = GetConnectParamsJNI(getConnHandle(connHandle), connectParams);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("Connection, ConnectParams - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long getConnHandle(final Connection connHandle) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("Connection - start"); //$NON-NLS-1$
		}

		if (connHandle == null) {
			if (logger.isLoggable(Level.CONFIG)) {
				logger.config("Connection - end"); //$NON-NLS-1$
			}
			return 0;
		} else {
			if (logger.isLoggable(Level.CONFIG)) {
				logger.config("Connection - end"); //$NON-NLS-1$
			}
			return connHandle.handle;
		}
	}

	@Override
	public long getDiskHandle(final DiskHandle diskHandle) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle - start"); //$NON-NLS-1$
		}

		if (diskHandle == null) {
			if (logger.isLoggable(Level.CONFIG)) {
				logger.config("DiskHandle - end"); //$NON-NLS-1$
			}
			return 0;
		} else {
			if (logger.isLoggable(Level.CONFIG)) {
				logger.config("DiskHandle - end"); //$NON-NLS-1$
			}
			return diskHandle.handle;
		}
	}

	@Override
	public String getErrorText(final long error, final String locale) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("long, String - start"); //$NON-NLS-1$
		}

		final String returnString = GetErrorTextJNI(error, locale);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("long, String - end"); //$NON-NLS-1$
		}
		return returnString;
	}

	public int getFlags(final String transportModes, final boolean readOnly) {
		int flags = (readOnly) ? jDiskLibConst.OPEN_READ_ONLY : 0;
		if ((transportModes != null) && transportModes.contains("nbdssl")) {
			flags |= jDiskLibConst.OPEN_COMPRESSION_SKIPZ;
		}
		return flags;

	}

	@Override
	public long getInfo(final DiskHandle diskHandle, final Info info) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, Info - start"); //$NON-NLS-1$
		}

		final long returnlong = GetInfoJNI(getDiskHandle(diskHandle), info);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, Info - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public String[] getMetadataKeys(final DiskHandle diskHandle) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle - start"); //$NON-NLS-1$
		}

		final String[] returnStringArray = GetMetadataKeysJNI(getDiskHandle(diskHandle));
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle - end"); //$NON-NLS-1$
		}
		return returnStringArray;
	}

	@Override
	public String getTransportMode(final DiskHandle diskHandle) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle - start"); //$NON-NLS-1$
		}

		final String returnString = GetTransportModeJNI(getDiskHandle(diskHandle));
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle - end"); //$NON-NLS-1$
		}
		return returnString;
	}

	@Override
	public long grow(final Connection connHandle, final String path, final long capacityInSectors,
			final boolean updateGeometry, final Progress progress) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("Connection, String, long, boolean, Progress - start"); //$NON-NLS-1$
		}

		final long returnlong = GrowJNI(getConnHandle(connHandle), path, capacityInSectors, updateGeometry, progress);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("Connection, String, long, boolean, Progress - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long init(final int majorVersion, final int minorVersion, final JVixLogger jvixLogger, final String libDir) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("int, int, JVixLogger, String - start"); //$NON-NLS-1$
		}
		final long returnlong = InitJNI(majorVersion, minorVersion, jvixLogger, libDir);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("int, int, JVixLogger, String - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long init(final JVixLogger jvixLogger) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("JVixLogger - start"); //$NON-NLS-1$
		}

		final long returnlong = InitJNI(JDiskLibFactory.getVddkVersion().getMajor(),
				JDiskLibFactory.getVddkVersion().getMinor(), jvixLogger, JDiskLibFactory.getLibDir());
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("JVixLogger - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long initEx(final int majorVersion, final int minorVersion, final JVixLogger jvixLogger, final String libDir,
			final String configFile) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("int, int, JVixLogger, String, String - start"); //$NON-NLS-1$
		}

		final long returnlong = InitExJNI(majorVersion, minorVersion, jvixLogger, libDir, configFile);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("int, int, JVixLogger, String, String - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long initEx(final JVixLogger jvixLogger, final String configFile) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("JVixLogger, String - start"); //$NON-NLS-1$
		}
		final long returnlong = InitExJNI(JDiskLibFactory.getVddkVersion().getMajor(),
				JDiskLibFactory.getVddkVersion().getMinor(), jvixLogger, JDiskLibFactory.getVddkPluginsDirectory(),
				configFile);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("JVixLogger, String - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long isAttachPossible(final DiskHandle parent, final DiskHandle child) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, DiskHandle - start"); //$NON-NLS-1$
		}

		final long returnlong = IsAttachPossibleJNI(getDiskHandle(parent), getDiskHandle(child));
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, DiskHandle - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public String listTransportModes() {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("<no args> - start"); //$NON-NLS-1$
		}

		final String returnString = ListTransportModesJNI();
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("<no args> - end"); //$NON-NLS-1$
		}
		return returnString;
	}

	@Override
	public long open(final Connection connHandle, final String path, final int flags, final DiskHandle handle) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("Connection, String, int, DiskHandle - start"); //$NON-NLS-1$
		}

		final long[] diskHandle = new long[1];
		long result;

		if (handle == null) {
			// explicitly allow this for code testing purposes
			result = OpenJNI(getConnHandle(connHandle), path, flags, null);
		} else {
			handle.handle = 0;
			result = OpenJNI(getConnHandle(connHandle), path, flags, diskHandle);
			if (result == VIX_OK) {
				handle.handle = diskHandle[0];
			}
		}

		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("Connection, String, int, DiskHandle - end"); //$NON-NLS-1$
		}
		return result;
	}

	@Override
	public void perturbEnable(final String fName, final int enable) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("String, int - start"); //$NON-NLS-1$
		}

		PerturbEnableJNI(fName, enable);

		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("String, int - end"); //$NON-NLS-1$
		}
	}

	@Override
	public long prepareForAccess(final ConnectParams connectParams, final String identity) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("ConnectParams, String - start"); //$NON-NLS-1$
		}

		final long returnlong = PrepareForAccessJNI(connectParams, identity);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("ConnectParams, String - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long queryAllocatedBlocks(final DiskHandle diskHandle, final long startSector, final long numSectors,
			final long chunkSize, final List<Block> blockList) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, long, long, long, List<Block> - start"); //$NON-NLS-1$
		}

		final long returnlong = QueryAllocatedBlocksJNI(getDiskHandle(diskHandle), startSector, numSectors, chunkSize,
				blockList);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, long, long, long, List<Block> - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long read(final DiskHandle diskHandle, final long startSector, final byte[] buffer) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, long, byte[] - start"); //$NON-NLS-1$
		}

		final long returnlong = read(diskHandle, startSector, buffer.length / 512, buffer);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, long, byte[] - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long read(final DiskHandle diskHandle, final long startSector, final long numSectors, final byte[] buffer) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, long, long, byte[] - start"); //$NON-NLS-1$
		}

		final long returnlong = ReadJNI(getDiskHandle(diskHandle), startSector, numSectors, buffer);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, long, long, byte[] - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long read(final DiskHandle diskHandle, final long startSector, final long numSectors,
			final ByteBuffer buffer) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, long, long, ByteBuffer - start"); //$NON-NLS-1$
		}

		final long returnlong = BufferReadJNI(getDiskHandle(diskHandle), startSector, numSectors, buffer);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, long, long, ByteBuffer - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long readAsync(final DiskHandle diskHandle, final long startSector, final ByteBuffer buffer,
			final int sectorCount, final AsyncIOListener callbackObj) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, long, ByteBuffer, int, AsyncIOListener - start"); //$NON-NLS-1$
		}

		final long returnlong = ReadAsyncJNI(getDiskHandle(diskHandle), startSector, buffer, sectorCount, callbackObj);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, long, ByteBuffer, int, AsyncIOListener - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long readMetadata(final DiskHandle diskHandle, final String key, final StringBuffer val) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, String, StringBuffer - start"); //$NON-NLS-1$
		}

		final long returnlong = ReadMetadataJNI(getDiskHandle(diskHandle), key, val);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, String, StringBuffer - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long rename(final String src, final String dst) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("String, String - start"); //$NON-NLS-1$
		}

		final long returnlong = RenameJNI(src, dst);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("String, String - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long setInjectedFault(final FaultInjectionType id, final int enabled, final int faultErr) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("FaultInjectionType, int, int - start"); //$NON-NLS-1$
		}

		final long returnlong = SetInjectedFaultJNI(id.ordinal(), enabled, faultErr);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("FaultInjectionType, int, int - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long shrink(final DiskHandle diskHandle, final Progress progress) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, Progress - start"); //$NON-NLS-1$
		}

		final long returnlong = ShrinkJNI(getDiskHandle(diskHandle), progress);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, Progress - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long spaceNeededForClone(final DiskHandle diskHandle, final int diskType, final long[] spaceNeeded) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, int, long[] - start"); //$NON-NLS-1$
		}

		final long returnlong = SpaceNeededForCloneJNI(getDiskHandle(diskHandle), diskType, spaceNeeded);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, int, long[] - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public boolean succeeded(final long err) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("long - start"); //$NON-NLS-1$
		}

		final boolean returnboolean = err == jDiskLibConst.VIX_OK;
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("long - end"); //$NON-NLS-1$
		}
		return returnboolean;
	}

	@Override
	public long unlink(final Connection connHandle, final String path) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("Connection, String - start"); //$NON-NLS-1$
		}

		final long returnlong = UnlinkJNI(getConnHandle(connHandle), path);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("Connection, String - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long wait(final DiskHandle diskHandle) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle - start"); //$NON-NLS-1$
		}

		final long returnlong = WaitJNI(getDiskHandle(diskHandle));
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	@Deprecated
	public long write(final DiskHandle diskHandle, final long startSector, final byte[] buffer) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, long, byte[] - start"); //$NON-NLS-1$
		}

		final long returnlong = write(diskHandle, startSector, buffer.length / 512, buffer);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, long, byte[] - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long write(final DiskHandle diskHandle, final long startSector, final long numSectors, final byte[] buffer) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, long, long, byte[] - start"); //$NON-NLS-1$
		}

		final long returnlong = WriteJNI(getDiskHandle(diskHandle), startSector, numSectors, buffer);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, long, long, byte[] - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long write(final DiskHandle diskHandle, final long startSector, final long numSectors,
			final ByteBuffer buffer) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, long, long, ByteBuffer - start"); //$NON-NLS-1$
		}

		final long returnlong = BufferWriteJNI(getDiskHandle(diskHandle), startSector, numSectors, buffer);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, long, long, ByteBuffer - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long writeAsync(final DiskHandle diskHandle, final long startSector, final ByteBuffer buffer,
			final int sectorCount, final AsyncIOListener callbackObj) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, long, ByteBuffer, int, AsyncIOListener - start"); //$NON-NLS-1$
		}

		final long returnlong = WriteAsyncJNI(getDiskHandle(diskHandle), startSector, buffer, sectorCount, callbackObj);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, long, ByteBuffer, int, AsyncIOListener - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

	@Override
	public long writeMetadata(final DiskHandle diskHandle, final String key, final String val) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, String, String - start"); //$NON-NLS-1$
		}

		final long returnlong = WriteMetadataJNI(getDiskHandle(diskHandle), key, val);
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("DiskHandle, String, String - end"); //$NON-NLS-1$
		}
		return returnlong;
	}

}
