/**
 *
 */
package com.vmware.safekeeping.external.type;

import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.support.OperationState;

public class ResultThread {

	private final ICoreResultAction resultAction;
	private final long threadId;
	private final String taskId;

	public ResultThread(final ICoreResultAction ra, final long threadId) {
		this.resultAction = ra;
		this.threadId = threadId;
		if (ra != null) {
			this.taskId = ra.getResultActionId();
		} else {
			this.taskId = null;
		}
	}

	/**
	 * @return the ra
	 */
	public ICoreResultAction getResultAction() {
		return this.resultAction;
	}

	public OperationState getState() {
		return this.resultAction.getState();
	}

	/**
	 * @return the taskId
	 */
	public String getTaskId() {
		return this.taskId;
	}

	/**
	 * @return the threadId
	 */
	public long getThreadId() {
		return this.threadId;
	}

}