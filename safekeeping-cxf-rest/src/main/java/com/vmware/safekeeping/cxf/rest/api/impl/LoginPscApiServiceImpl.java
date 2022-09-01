package com.vmware.safekeeping.cxf.rest.api.impl;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnectSso;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.cxf.rest.GlobalState;
import com.vmware.safekeeping.cxf.rest.api.*;
import com.vmware.safekeeping.cxf.rest.model.*;

import com.vmware.safekeeping.cxf.rest.model.PscConnectOptions;
import com.vmware.safekeeping.cxf.rest.model.ResultActionConnectSso;
import com.vmware.safekeeping.cxf.rest.support.Convert;
import com.vmware.safekeeping.cxf.rest.support.User;

import java.util.Map;
import java.util.List;
import com.vmware.safekeeping.cxf.rest.api.NotFoundException;
import com.vmware.safekeeping.cxf.rest.command.entry.ExternalConnectCommand;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class LoginPscApiServiceImpl extends LoginPscApiService {
    @Override
    public Response loginPsc(PscConnectOptions body, SecurityContext securityContext) throws NotFoundException {
	final CoreResultActionConnectSso racs = new CoreResultActionConnectSso();
	try {
		final ExternalConnectCommand connect = new ExternalConnectCommand(body);
		if (connect.connectSso(racs, body.getPassword())) {
			final User user = new User(connect.getConnectionManager());
			GlobalState.getUsersList().put(racs.getToken(), user);
		}
	} catch (final CoreResultActionException | SafekeepingException e) {
	//	Utility.logWarning(this.logger, e);
	//	throw new InternalCoreResult();
	} finally {
		racs.done();
	}
	final ResultActionConnectSso result = new ResultActionConnectSso();
	  Convert.resultActionConnectSso(racs,result);
	 
        return Response.ok().entity(result ).build();
    }
}
