/**
 *
 */
package com.vmware.safekeeping.external.command.support;

import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.external.type.ResultThread;

public class Task {
	private String id;
	private String reason;
	private OperationState state;
	private ManagedFcoEntityInfo fcoEntity;

	public Task() {

	}

	public Task(final ICoreResultAction ra) {
		this.state = ra.getState();
		this.id = ra.getResultActionId();
		this.fcoEntity = ra.getFcoEntityInfo();
		this.reason = ra.getReason();
	}

	/**
	 * @param rt
	 */
	public Task(final ResultThread rt) {
		final ICoreResultAction ra = rt.getResultAction();
		if (ra != null) {
			this.state = ra.getState();
			this.id = ra.getResultActionId();
			this.fcoEntity = ra.getFcoEntityInfo();
			this.reason = ra.getReason();
		} else {
			this.state = OperationState.FAILED;
			this.fcoEntity = ManagedFcoEntityInfo.newNullManagedEntityInfo();
			this.reason = "unknown";
			this.id = null;
		}
	}

	/**
	 * @param e
	 */
	public void fails(final Exception e) {
		this.state = OperationState.FAILED;
		this.reason = e.getMessage();
	}

	public void fails(String reason) {
		this.state = OperationState.FAILED;
		this.reason = reason;
	}

	/**
	 * @return the fco
	 */
	public ManagedFcoEntityInfo getFcoEntity() {
		return this.fcoEntity;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * @return the error
	 */
	public String getReason() {
		return this.reason;
	}

	/**
	 * @return the state
	 */
	public OperationState getState() {
		return this.state;
	}

	public void noCatalogEntitySkip() {
		this.state = OperationState.SKIPPED;
		this.reason = Tasks.NO_VALID_CATALOG_ENTITY;
	}

	public void noTargetFailure() {
		this.state = OperationState.FAILED;
		this.reason = Tasks.NO_REPOSITORY_TARGET_ERROR_MESSAGE;
	}

	/**
	 * @param fco the fco to set
	 */
	public void setFcoEntity(final ManagedFcoEntityInfo fco) {
		this.fcoEntity = fco;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * @param error the error to set
	 */
	public void setReason(final String error) {
		this.reason = error;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(final OperationState state) {
		this.state = state;
	}
}