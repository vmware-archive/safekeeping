package com.vmware.safekeeping.core.exception;

public class RestoreException extends SafekeepingException {

	/**
	 *
	 */
	private static final long serialVersionUID = 7029905647640075423L;

	public RestoreException(final String format, final Object... arg1) {
		super(String.format(format, arg1));
	}

}
