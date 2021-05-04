package com.vmware.safekeeping.core.type.enums;

import com.vmware.safekeeping.core.type.fco.managers.SnapshotManager;

public enum WindowsQuiesceSpecVssBackupType {
	VSS_BT_COPY, VSS_BT_DIFFERENTIAL, VSS_BT_FULL, VSS_BT_INCREMENTAL, VSS_BT_LOG, VSS_BT_OTHER, VSS_BT_UNDEFINED;

	public static WindowsQuiesceSpecVssBackupType parse(final String modeStr) {

		switch (modeStr.toUpperCase()) {
		case "VSS_BT_DIFFERENTIAL":

			return VSS_BT_DIFFERENTIAL;

		case "VSS_BT_FULL":
			return VSS_BT_FULL;
		case "VSS_BT_INCREMENTAL":
			return VSS_BT_INCREMENTAL;
		case "VSS_BT_LOG":
			return VSS_BT_LOG;
		case "VSS_BT_OTHER":
			return VSS_BT_OTHER;
		case "VSS_BT_UNDEFINED":
			return VSS_BT_UNDEFINED;
		case "VSS_BT_COPY":
		default:
			return VSS_BT_COPY;

		}
	}

	public Integer toInteger() {
		switch (this) {
		case VSS_BT_DIFFERENTIAL:
			return SnapshotManager.VSS_BT_DIFFERENTIAL;
		case VSS_BT_FULL:
			return SnapshotManager.VSS_BT_FULL;
		case VSS_BT_INCREMENTAL:
			return SnapshotManager.VSS_BT_INCREMENTAL;
		case VSS_BT_LOG:
			return SnapshotManager.VSS_BT_LOG;
		case VSS_BT_OTHER:
			return SnapshotManager.VSS_BT_OTHER;
		case VSS_BT_UNDEFINED:
			return SnapshotManager.VSS_BT_UNDEFINED;
		case VSS_BT_COPY:
		default:
			return SnapshotManager.VSS_BT_COPY;
		}
	}
}