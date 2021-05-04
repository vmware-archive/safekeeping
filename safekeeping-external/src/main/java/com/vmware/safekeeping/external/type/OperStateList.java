/**
 *
 */
package com.vmware.safekeeping.external.type;

import java.io.Serializable;
import java.util.List;

import com.vmware.safekeeping.core.command.results.support.OperationState;

/**
 * @author mdaneri
 *
 */
public class OperStateList implements Serializable {
	private static final long serialVersionUID = 654947470216276017L;
	private List<OperationState> list;
	private String reason;
	private boolean quit = false;

	/**
	 *
	 */
	public OperStateList() {
		// TODO Auto-generated constructor stub
	}

	public List<OperationState> getList() {
		return this.list;
	}

	public String getReason() {
		return this.reason;
	}

	public boolean isQuit() {
		return this.quit;
	}

	public void setList(final List<OperationState> list) {
		this.list = list;
	}

	public void setQuit(final boolean quit) {
		this.quit = quit;
	}

	public void setReason(final String reason) {
		this.reason = reason;
	}

}
