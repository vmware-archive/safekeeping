package com.vmware.safekeeping.external.exception;

/**
 *
 */

import com.vmware.safekeeping.core.command.results.AbstractCoreResultDiskBackupRestore;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;

/**
 * @author mdaneri
 *
 */
public class InvalidTask extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 5920646287609073619L;

	public InvalidTask() {
		super("Token unknow");
	}

	public InvalidTask(final Class<AbstractCoreResultDiskBackupRestore> class1, final ICoreResultAction obj) {
		super(String.format("Invalid Task for type %s (Type %s expected)", obj.getClass().getName(), class1.getName()));
	}

	public InvalidTask(final ICoreResultAction obj) {
		super(String.format("Invalid Task for type %s ", obj.getClass().getName()));
	}

	public InvalidTask(final String message) {
		super(message);
	}
}
