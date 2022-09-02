package com.vmware.safekeeping.cxf.rest.api.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionDisconnect;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.cxf.rest.GlobalState;
import com.vmware.safekeeping.cxf.rest.api.DisconnectApiService;
import com.vmware.safekeeping.cxf.rest.api.NotFoundException;
import com.vmware.safekeeping.cxf.rest.model.ResultActionDisconnect;
import com.vmware.safekeeping.cxf.rest.support.Convert;
import com.vmware.safekeeping.cxf.rest.support.User;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")
public class DisconnectApiServiceImpl extends DisconnectApiService {
    @Override
    public Response disconnect(SecurityContext securityContext) throws NotFoundException {
	User user = GlobalState.precheck(securityContext);
	this.connectionManager = user.getConnection();
	if (this.logger.isLoggable(Level.CONFIG)) {
	    this.logger.config("ConnectionManager connectionManager=" + connectionManager + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	final CoreResultActionDisconnect raddis = new CoreResultActionDisconnect();

	try {
	    connectionManager.disconnectVimConnections(raddis);
	    
	} catch (final CoreResultActionException e) {
	    Utility.logWarning(this.logger, e);
	    raddis.failure(e);
	} finally {
	    raddis.done();
	}
	final ResultActionDisconnect  result = new ResultActionDisconnect ();
	Convert.ResultActionDisconnect (raddis, result);

	return Response.ok().entity(result).build();
    }

    private ConnectionManager connectionManager;
    private Logger logger;

    public DisconnectApiServiceImpl() {
	this.logger = Logger.getLogger(this.getClass().getName());
    }

}
