package com.vmware.safekeeping.core.exception;

public class ProfileException extends SafekeepingException {

	/**
	 *
	 */
	private static final long serialVersionUID = -6242578584570800922L;

	public ProfileException(final Exception e) {
		super(e);
	}

	public ProfileException(final String format, final Object... arg1) {
		super(String.format(format, arg1));
	}

}
