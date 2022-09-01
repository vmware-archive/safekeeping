package com.vmware.safekeeping.cxf.rest.api.impl;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnect;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.cxf.rest.GlobalState;
import com.vmware.safekeeping.cxf.rest.api.*;
import com.vmware.safekeeping.cxf.rest.model.*;

import com.vmware.safekeeping.cxf.rest.model.SapiTask;
import com.vmware.safekeeping.cxf.rest.support.ResultThread;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import com.vmware.safekeeping.cxf.rest.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class ConnectApiServiceImpl extends ConnectApiService implements Runnable{


    private Logger logger;
    private ConnectionManager connectionManager;
    private CoreResultActionConnect rac;
    
    
    public ConnectApiServiceImpl() {

        this.logger = Logger.getLogger(this.getClass().getName()); 
    }

    @Override
    public Response connect(SecurityContext securityContext) throws NotFoundException {
	 this. connectionManager=  GlobalState.precheck().getConnection();
	if (this.logger.isLoggable(Level.CONFIG)) {
	            this.logger.config("ConnectionManager connectionManager=" + connectionManager + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
	        }
	  
	        ResultThread result = null;
	        this.rac = new CoreResultActionConnect();
	        final Thread thread = new Thread(this);

	        result = new ResultThread(this.rac, thread.getId());

	        thread.setName(ConnectApiServiceImpl.class.getName());
	        thread.start();

	        if (this.logger.isLoggable(Level.CONFIG)) {
	            this.logger.config("ConnectionManager - end"); //$NON-NLS-1$
	        }
	         
        return Response.ok().entity(result).build();
    }
    protected void actionConnect(final ConnectionManager connetionManager, final CoreResultActionConnect result)
		throws CoreResultActionException {
	connetionManager.connectVimConnetions(result);

}
    @Override
    public void run() {
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("<no args> - start"); //$NON-NLS-1$
        }

        try {
            actionConnect(this.connectionManager, this.rac);
        } catch (final CoreResultActionException e) {
            this.logger.severe("<no args> - exception: " + e); //$NON-NLS-1$

            Utility.logWarning(this.logger, e);
            this.rac.failure(e);
        } finally {
            this.rac.done();
        }

        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("<no args> - end"); //$NON-NLS-1$
        }
    }
}
