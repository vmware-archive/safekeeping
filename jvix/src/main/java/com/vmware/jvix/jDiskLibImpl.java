/******************************************************************************
 * Copyright 2008-2020 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * jDiskLib.java  --
 *
 *      Implementation of the Java component of the vixDiskLib Java API.
 *      jDiskLib defines the interface. jDiskLibImpl hides implementation
 *      details from users of the interface.
 *
 *******************************************************************************/

package com.vmware.jvix;

import java.nio.ByteBuffer;
import java.util.List;

/*
 * jDiskLibImpl implements the jDiskLib interface. This allows us to hide all
 * implementation details for the vixDiskLib JNI from users of the interface.
 */

abstract class jDiskLibImpl implements jDiskLib, jDiskLibConst {
	/*
	 * Java Native Interface (JNI) functions provided by the jDiskLib JNI Library.
	 * Wrappers are provided above.
	 */
	protected native ByteBuffer AllocateBufferJNI(int size, int alignment);

	protected native long AttachJNI(long parent, long child);

	/*
	 *
	 * accessory functions for VixDiskLib functionality.
	 *
	 */

	protected native long BufferReadJNI(long diskHandle, long startSector, long numSectors, ByteBuffer buffer);

	protected native long BufferWriteJNI(long diskHandle, long startSector, long numSectors, ByteBuffer buffer);

	protected native long CheckRepairJNI(long conn, String path, boolean repair);

	protected native long CleanupJNI(ConnectParams connection, int[] numCleaned, int[] numRemaining);

	protected native long CloneJNI(long dstConnection, String dstPath, long srcConnection, String srcPath,
			CreateParams createParams, Progress progress, boolean overwrite);

	protected native long CloseJNI(long diskHandle);

	protected native long ConnectExJNI(ConnectParams connection, boolean readOnly, String snapshotRef,
			String transportModes, long[] connHandle);

	protected native long ConnectJNI(ConnectParams connection, long[] connHandle);

	protected native long CreateChildJNI(long diskHandle, String childPath, int diskType, Progress progress);

	protected native long CreateJNI(long connHandle, String path, CreateParams createParams, Progress progress);

	protected native long DefragmentJNI(long diskHandle, Progress progress);

	protected native long DisconnectJNI(long connHandle);

	protected native long EndAccessJNI(ConnectParams connection, String identity);

	protected native void ExitJNI();

	protected native long FlushJNI(long diskHandle);

	protected native void FreeBufferJNI(ByteBuffer buffer);

	protected native long GetConnectParamsJNI(long connHandle, ConnectParams connection);

	protected native String GetErrorTextJNI(long error, String locale);

	protected native long GetInfoJNI(long connHandle, Info info);

	protected native String[] GetMetadataKeysJNI(long diskHandle);

	protected native String GetTransportModeJNI(long diskHandle);

	protected native long GrowJNI(long connHandle, String path, long capacityInSectors, boolean updateGeometry,
			Progress progress);

	protected native long InitExJNI(int majorVersion, int minorVersion, JVixLogger logger, String libDir,
			String configFile);

	protected native long InitJNI(int majorVersion, int minorVersion, JVixLogger logger, String libDir);

	protected native long IsAttachPossibleJNI(long parent, long child);

	protected native String ListTransportModesJNI();

	protected native long OpenJNI(long connHandle, String path, int flags, long[] diskHandle);

	protected native void PerturbEnableJNI(String fName, int enable);

	protected native long PrepareForAccessJNI(ConnectParams connection, String identity);

	protected native long QueryAllocatedBlocksJNI(long diskHandle, long startSector, long numSectors, long chunkSize,
			List<Block> blockList);

	protected native long ReadAsyncJNI(long diskHandle, long startSector, ByteBuffer buffer, int sectorCount,
			Object callbackObj);

	protected native long ReadJNI(long diskHandle, long startSector, long numSectors, byte[] buffer);

	protected native long ReadMetadataJNI(long diskHandle, String key, StringBuffer val);

	protected native long RenameJNI(String src, String dst);

	protected native long SetInjectedFaultJNI(int id, int enabled, int faultError);

	protected native long ShrinkJNI(long diskHandle, Progress progress);

	protected native long SpaceNeededForCloneJNI(long diskHandle, int diskType, long[] spaceNeeded);

	protected native long UnlinkJNI(long connHandle, String path);

	protected native long WaitJNI(long diskHandle);

	protected native long WriteAsyncJNI(long diskHandle, long startSector, ByteBuffer buffer, int sectorCount,
			Object callbackObj);

	protected native long WriteJNI(long diskHandle, long startSector, long numSectors, byte[] buffer);

	protected native long WriteMetadataJNI(long diskHandle, String key, String val);
}
