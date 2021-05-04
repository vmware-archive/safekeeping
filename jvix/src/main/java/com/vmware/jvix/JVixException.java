package com.vmware.jvix;

public class JVixException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 3389093002296784737L;

	private final long vddkError;

	public JVixException(final Exception e) {
		super(e);
		this.vddkError = e.hashCode();
	}

	/**
	 * @param vddkCallResult
	 * @param getErrorText
	 */
	public JVixException(final long vddkError, final String getErrorText) {
		super(getErrorText);
		this.vddkError = vddkError;
	}

	public JVixException(final String message) {
		super(message);
		this.vddkError = 3;
	}

	public long getVddkError() {
		return this.vddkError;
	}

}
