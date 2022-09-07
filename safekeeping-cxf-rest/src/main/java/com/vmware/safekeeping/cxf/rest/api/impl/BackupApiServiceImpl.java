package com.vmware.safekeeping.cxf.rest.api.impl;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.cxf.rest.GlobalState;
import com.vmware.safekeeping.cxf.rest.api.ApiResponseMessage;
import com.vmware.safekeeping.cxf.rest.api.BackupApiService;
import com.vmware.safekeeping.cxf.rest.api.NotFoundException;
import com.vmware.safekeeping.cxf.rest.model.BackupOptions;
import com.vmware.safekeeping.cxf.rest.model.FcoTarget;
import com.vmware.safekeeping.cxf.rest.model.FcoTypeSearch;
import com.vmware.safekeeping.cxf.rest.model.ResultActionBackup;
import com.vmware.safekeeping.cxf.rest.model.SapiTasks;
import com.vmware.safekeeping.cxf.rest.runnable.ApiBackupCommandWrapper;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T22:00:47.492Z[GMT]")
public class BackupApiServiceImpl extends BackupApiService {
    @Override
    public Response backup(BackupOptions body, SecurityContext securityContext) throws NotFoundException {

	// do some magic!
	return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response backupFco(String fco, SecurityContext securityContext) throws NotFoundException {
//
//	final ExternalBackupCommand backup = new ExternalBackupCommand(options);
//	return backup.action(connection);
	BackupOptions body=new BackupOptions();
	FcoTarget e=new FcoTarget();
	e.setKeyType(FcoTypeSearch.VM_NAME);
	e.setKey(fco);
	body.getTargetList().add(e);
	ConnectionManager connectionManager = GlobalState.precheck(securityContext).getConnection();
	ApiBackupCommandWrapper backup = new ApiBackupCommandWrapper(connectionManager,body);
	SapiTasks result = backup.action(connectionManager);

	// do some magic!
	return Response.ok().entity(result).build();
    }
}
