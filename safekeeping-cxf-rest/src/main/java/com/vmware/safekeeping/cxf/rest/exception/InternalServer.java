package com.vmware.safekeeping.cxf.rest.exception;
/**
 *
 */

/**
 * @author mdaneri
 *
 */
public class InternalServer extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 7888118954174945424L;
	private static final String INTERNAL_ERROR_MESSAGE = "Internal Error Check the server log";

	public InternalServer() {
		super(InternalServer.INTERNAL_ERROR_MESSAGE);
	}

	public InternalServer(final String message) {
		super(message);
	}
}
