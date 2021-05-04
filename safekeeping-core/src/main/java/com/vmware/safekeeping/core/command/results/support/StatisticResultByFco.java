/**
 *
 */
package com.vmware.safekeeping.core.command.results.support;

class StatisticResultByFco {
	private int aborted;
	private int failure;
	private int skip;
	private int success;

	/**
	 * @param result
	 */
	int countResult(final OperationState opResult) {
		int result = -1;
		switch (opResult) {
		case ABORTED:
			result = incAborted();
			break;
		case FAILED:
			result = incFailure();
			break;
		case SKIPPED:
			result = incSkip();
			break;
		case SUCCESS:
			result = incSuccess();
			break;

		default:
			break;

		}
		return result;

	}

	public int getAborted() {
		return this.aborted;
	}

	public int getFailure() {
		return this.failure;
	}

	public int getSkip() {
		return this.skip;
	}

	public int getSuccess() {
		return this.success;
	}

	public int getTotal() {
		return this.aborted + this.failure + this.skip + this.success;
	}

	private int incAborted() {
		return ++this.aborted;
	}

	private int incFailure() {
		return ++this.failure;
	}

	private int incSkip() {
		return ++this.skip;
	}

	private int incSuccess() {
		return ++this.success;
	}

	public void setAborted(final int aborted) {
		this.aborted = aborted;
	}

	public void setFailure(final int failure) {
		this.failure = failure;
	}

	public void setSkip(final int skip) {
		this.skip = skip;
	}

	public void setSuccess(final int success) {
		this.success = success;
	}
}