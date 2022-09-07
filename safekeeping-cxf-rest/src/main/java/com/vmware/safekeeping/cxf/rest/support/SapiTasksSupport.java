package com.vmware.safekeeping.cxf.rest.support;

import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.EntityType;
import com.vmware.safekeeping.cxf.rest.model.OperationState;
import com.vmware.safekeeping.cxf.rest.model.SapiTask;
import com.vmware.safekeeping.cxf.rest.model.SapiTasks;

public class SapiTasksSupport {

    public static final String INTERNAL_ERROR_MESSAGE = "Internal Error Check the server log";
    public static final String NO_REPOSITORY_TARGET_ERROR_MESSAGE = "No Repository Target available";
    public static final String NO_VALID_CATALOG_ENTITY = "No valid catalogs entity";
    public static final String REPOSITORY_NOT_ACTIVE = "Repository is not active";
    public static final String NO_VCENTER_CONNECTION = "No vCenter connection";
    public static final String NO_VALID_FCO_TARGETS = "No valid Fco targets";
    public static final String UNSUPPORTED_ENTITY_TYPE = "Unsupported entity Type";
    public static final String GLOBAL_PROFILE_ERROR = "Errors accessing the Global Profile file";

    public static void skipNoCatalogEntity(SapiTasks task) {
	task.setState(OperationState.SKIPPED);
	task.setReason(NO_VALID_CATALOG_ENTITY);
    }

    public static void skipNoValidFcoTargets(SapiTasks task) {
	task.setState(OperationState.SKIPPED);
	task.setReason(NO_VALID_FCO_TARGETS);
    }

    public static void unsupportedTypeFailure(SapiTasks task, EntityType entityType) {
	task.setState(OperationState.FAILED);
	task.setReason(UNSUPPORTED_ENTITY_TYPE + " " + entityType.toString());
    }

    public static void globalProfileFailure(SapiTasks task) {
	task.setState(OperationState.FAILED);
	task.setReason(GLOBAL_PROFILE_ERROR);
    }

    /**
    *
    */
    public static void internalFailure(SapiTasks task) {
	task.setState(OperationState.FAILED);
	task.setReason(INTERNAL_ERROR_MESSAGE);

    }

    public static void noRepositoryTargetFailure(SapiTasks task) {
	task.setState(OperationState.FAILED);
	task.setReason(NO_REPOSITORY_TARGET_ERROR_MESSAGE);
    }

    public static void noVcenterConnectionFailure(SapiTasks task) {
	task.setState(OperationState.FAILED);
	task.setReason(NO_VCENTER_CONNECTION);
    }

    public static void repositoryNotActiveFailure(SapiTasks task) {
	task.setState(OperationState.FAILED);
	task.setReason(REPOSITORY_NOT_ACTIVE);
    }
    
    
    public static SapiTask newTask(final ResultThread rt) {
	SapiTask result=new SapiTask();
	final ICoreResultAction ra = rt.getResultAction();
	if (ra != null) {
	    result.setState(com.vmware.safekeeping.cxf.rest.model.OperationState.fromValue(   ra.getState().toString()));
	    result.setId(ra.getResultActionId());
	    result.setFcoEntity(Convert.convertManagedFcoEntityInfo( ra.getFcoEntityInfo()));
	    result.setReason(ra.getReason());
	} else {
	    result.setState(OperationState.FAILED);
	    result.setFcoEntity(Convert.convertManagedFcoEntityInfo(ManagedFcoEntityInfo.newNullManagedEntityInfo()));
	    result.setReason("unknown");
	    result.setId(null);
	}return result;
}
}
