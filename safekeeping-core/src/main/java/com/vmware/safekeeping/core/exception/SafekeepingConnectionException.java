package com.vmware.safekeeping.core.exception;

public class SafekeepingConnectionException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -6242578584570800922L;

	public SafekeepingConnectionException(final Exception e) {
		super(e);
	}

	public SafekeepingConnectionException(final String format, final Object... arg1) {
		super(String.format(format, arg1));
	}

}
