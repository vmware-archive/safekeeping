package com.vmware.safekeeping.cxf.rest.api.impl;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnect;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.cxf.rest.GlobalState;
import com.vmware.safekeeping.cxf.rest.api.ConnectApiService;
import com.vmware.safekeeping.cxf.rest.api.NotFoundException;
import com.vmware.safekeeping.cxf.rest.model.ResultActionConnect;
import com.vmware.safekeeping.cxf.rest.model.ResultActionConnectSso;
import com.vmware.safekeeping.cxf.rest.runnable.ConnectApiServiceRunnable;
import com.vmware.safekeeping.cxf.rest.support.ResultThread;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")
public class ConnectApiServiceImpl extends ConnectApiService {

    @Override
    public Response connect(SecurityContext securityContext) throws NotFoundException {

	ConnectionManager connectionManager = GlobalState.precheck(securityContext).getConnection();
	ConnectApiServiceRunnable connect = new ConnectApiServiceRunnable(connectionManager);
	 ResultActionConnect result = connect.action();

	return Response.ok().entity(result).build();
    }
 
}
