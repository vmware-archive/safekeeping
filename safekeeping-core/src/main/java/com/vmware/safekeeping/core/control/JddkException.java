/**
 *
 */
package com.vmware.safekeeping.core.control;

import com.vmware.vim25.LocalizedMethodFault;

/**
 * @author mdaneri
 *
 */
public class JddkException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -4942711938185101394L;
	/**
	*
	*/
	private final LocalizedMethodFault methodFault;
	private long vddkError;

	public JddkException(final LocalizedMethodFault methodFault) {
		super(methodFault.getLocalizedMessage());
		this.methodFault = methodFault;
	}

	/**
	 * @param vddkCallResult
	 * @param getErrorText
	 */
	public JddkException(final long vddkError, final String getErrorText) {
		super(getErrorText);
		this.methodFault = new LocalizedMethodFault();
		this.methodFault.setLocalizedMessage(getErrorText);
		this.vddkError = vddkError;
	}

	public long getVddkError() {
		return this.vddkError;
	}

	public void setVddkError(final long vddkError) {
		this.vddkError = vddkError;
	}

}
