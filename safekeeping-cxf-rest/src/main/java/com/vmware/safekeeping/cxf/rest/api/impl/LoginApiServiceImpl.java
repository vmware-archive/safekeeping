package com.vmware.safekeeping.cxf.rest.api.impl;

import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnectSso;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.cxf.rest.GlobalState;
import com.vmware.safekeeping.cxf.rest.api.LoginApiService;
import com.vmware.safekeeping.cxf.rest.api.NotFoundException;
import com.vmware.safekeeping.cxf.rest.command.entry.ExternalConnectCommand;
import com.vmware.safekeeping.cxf.rest.model.ResultActionConnectSso;
import com.vmware.safekeeping.cxf.rest.support.Convert;
import com.vmware.safekeeping.cxf.rest.support.User;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T16:32:57.901Z[GMT]")public class LoginApiServiceImpl extends LoginApiService {
    private Logger logger;
    public LoginApiServiceImpl() {
	this.logger = Logger.getLogger(this.getClass().getName());
    }
    @Override
    public Response login(String server, String user, String password, Boolean base64, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
	final CoreResultActionConnectSso racs = new CoreResultActionConnectSso();
	try {
	    final ExternalConnectCommand connect = new ExternalConnectCommand(server,user,base64);
	    if (connect.connectSso(racs, password)) {
		final User userType = new User(connect.getConnectionManager(),racs.getToken());
		GlobalState.getUsersList().put(racs.getToken(), userType);
	    }
	} catch (final CoreResultActionException | SafekeepingException e) {
	      Utility.logWarning(this.logger, e);
	     throw new NotFoundException(500,"Internal error");
	} finally {
	    racs.done();
	}
	final ResultActionConnectSso result = new ResultActionConnectSso();
	Convert.resultActionConnectSso(racs, result);

	return Response.ok().entity(result).build();

    }
}
