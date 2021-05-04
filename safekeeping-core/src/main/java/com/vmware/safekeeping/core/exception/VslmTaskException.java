package com.vmware.safekeeping.core.exception;

import com.vmware.vim25.LocalizedMethodFault;
import com.vmware.vslm.VslmTaskInfo;

public class VslmTaskException extends Exception {
	/**
	 *
	 */
	private static final long serialVersionUID = 3675943327511516257L;

	private final LocalizedMethodFault methodFault;

	public VslmTaskException(final LocalizedMethodFault methodFault) {
		super(methodFault.getLocalizedMessage());
		this.methodFault = methodFault;
	}

	public VslmTaskException(final VslmTaskInfo taskInfo) {
		super(taskInfo.getError().getLocalizedMessage());
		this.methodFault = taskInfo.getError();
	}

	public LocalizedMethodFault getMethodFault() {
		return this.methodFault;
	}
}