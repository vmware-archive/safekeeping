/**
 *
 */
package com.vmware.safekeeping.cxf.support;

/**
 * @author mdaneri
 *
 */
public class InvalidOptions extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 2864381676664066963L;

	public InvalidOptions() {
		super("Token unknow");
	}

	public InvalidOptions(final String message) {
		super(message);
	}
}
