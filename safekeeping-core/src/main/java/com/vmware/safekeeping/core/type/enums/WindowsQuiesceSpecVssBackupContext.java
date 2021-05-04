package com.vmware.safekeeping.core.type.enums;

public enum WindowsQuiesceSpecVssBackupContext {
	CTX_AUTO, CTX_BACKUP, CTX_FILE_SHARE_BACKUP;

	public static WindowsQuiesceSpecVssBackupContext parse(final String modeStr) {

		switch (modeStr.toLowerCase()) {
		case "ctx_auto":

			return CTX_AUTO;
		case "ctx_backup":

			return CTX_BACKUP;

		case "ctx_file_share_backup":
			return CTX_FILE_SHARE_BACKUP;
		default:
			return CTX_AUTO;

		}
	}

	@Override
	public String toString() {
		switch (this) {
		case CTX_BACKUP:
			return "ctx_backup";
		case CTX_FILE_SHARE_BACKUP:
			return "ctx_file_share_backup";
		case CTX_AUTO:
		default:
			return "ctx_auto";
		}
	}
}