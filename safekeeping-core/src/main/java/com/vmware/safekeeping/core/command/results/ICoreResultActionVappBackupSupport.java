package com.vmware.safekeeping.core.command.results;

import com.vmware.safekeeping.common.ConcurrentDoublyLinkedList;
import com.vmware.safekeeping.core.command.interactive.IBackupInteractive;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.profile.GenerationProfile;

public interface ICoreResultActionVappBackupSupport {

	default OperationState getChildVmActionsResult() {
		OperationState result = OperationState.SUCCESS;
		for (final ICoreResultAction rab : getResultActionOnsChildVm()) {
			switch (rab.getState()) {
			case ABORTED:
				result = OperationState.ABORTED;
				break;
			case FAILED:
				if (result != OperationState.ABORTED) {
					result = OperationState.FAILED;
				}
				break;

			case QUEUED:
			case STARTED:
				if ((result != OperationState.ABORTED) && (result != OperationState.FAILED)
						&& (result != OperationState.SKIPPED)) {
					result = OperationState.STARTED;
				}
				break;

			case SKIPPED:
				if ((result != OperationState.ABORTED) && (result != OperationState.FAILED)) {
					result = OperationState.SKIPPED;
				}
				break;
			case SUCCESS:

			default:
				break;

			}
		}
		return result;
	}

	IBackupInteractive getInteractive();

	GenerationProfile getProfile();

	ConcurrentDoublyLinkedList<? extends AbstractCoreResultActionBackupRestore> getResultActionOnsChildVm();
}
