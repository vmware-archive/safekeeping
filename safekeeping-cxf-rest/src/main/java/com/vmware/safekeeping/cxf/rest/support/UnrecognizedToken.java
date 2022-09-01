/**
 *
 */
package com.vmware.safekeeping.cxf.rest.support;

/**
 * @author mdaneri
 *
 */
public class UnrecognizedToken extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1144134183707258245L;

	public UnrecognizedToken() {
		super("Token unknow");
	}

	public UnrecognizedToken(final String message) {
		super(message);
	}

}
