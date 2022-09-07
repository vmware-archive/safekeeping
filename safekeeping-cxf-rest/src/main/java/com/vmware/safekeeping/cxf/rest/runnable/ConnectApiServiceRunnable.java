package com.vmware.safekeeping.cxf.rest.runnable;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnect;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.cxf.rest.model.ResultActionConnect;
import com.vmware.safekeeping.cxf.rest.support.Convert;
import com.vmware.safekeeping.cxf.rest.support.ResultThread;

public class ConnectApiServiceRunnable implements Runnable {

    final private Logger logger;
    final private ConnectionManager connectionManager;
    private CoreResultActionConnect rac;

    public ConnectApiServiceRunnable(ConnectionManager connectionManager) {

	this.logger = Logger.getLogger(this.getClass().getName());
	this.connectionManager = connectionManager;
    }

    public ResultActionConnect action() {

	if (this.logger.isLoggable(Level.CONFIG)) {
	    this.logger.config("ConnectionManager connectionManager=" + connectionManager + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	ResultActionConnect result = new ResultActionConnect();
	this.rac = new CoreResultActionConnect();
	final Thread thread = new Thread(this);

	new ResultThread(this.rac, thread.getId());

	thread.setName(ConnectApiServiceRunnable.class.getName());
	thread.start();

	if (this.logger.isLoggable(Level.CONFIG)) {
	    this.logger.config("ConnectionManager - end"); //$NON-NLS-1$
	}

	Convert.resultActionConnect(rac, result);
	return result;
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
