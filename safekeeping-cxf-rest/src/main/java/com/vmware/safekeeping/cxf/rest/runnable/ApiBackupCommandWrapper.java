package com.vmware.safekeeping.cxf.rest.runnable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.AbstractCommandWithOptions;
import com.vmware.safekeeping.core.command.BackupCommand;
import com.vmware.safekeeping.core.command.options.CoreBackupOptions;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionIvdBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmBackup;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnect;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.core.ThreadsManager;
import com.vmware.safekeeping.core.core.ThreadsManager.ThreadType;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.type.AbstractRunnableCommand;
import com.vmware.safekeeping.core.type.fco.IFirstClassObject;
import com.vmware.safekeeping.core.type.fco.ImprovedVirtualDisk;
import com.vmware.safekeeping.core.type.fco.VirtualAppManager;
import com.vmware.safekeeping.core.type.fco.VirtualMachineManager;
import com.vmware.safekeeping.cxf.rest.exception.InvalidTask;
import com.vmware.safekeeping.cxf.rest.model.BackupOptions;
import com.vmware.safekeeping.cxf.rest.model.ResultActionBackup;
import com.vmware.safekeeping.cxf.rest.model.ResultActionConnect;
import com.vmware.safekeeping.cxf.rest.model.ResultActionIvdBackup;
import com.vmware.safekeeping.cxf.rest.model.ResultActionVappBackup;
import com.vmware.safekeeping.cxf.rest.model.ResultActionVmBackup;
import com.vmware.safekeeping.cxf.rest.model.SapiTask;
import com.vmware.safekeeping.cxf.rest.model.SapiTasks;
import com.vmware.safekeeping.cxf.rest.support.Convert;
import com.vmware.safekeeping.cxf.rest.support.ResultThread;
import com.vmware.safekeeping.cxf.rest.support.SapiTasksSupport;
import com.vmware.safekeeping.cxf.rest.support.interactive.BackupNoInteractive;
import com.vmware.safekeeping.cxf.rest.support.interactive.BackupVappNoInteractive; 

public class ApiBackupCommandWrapper extends AbstractCommandWithOptions {
    static class RunnableBackup extends AbstractRunnableCommand {
        private final ITarget itarget;
        private final BackupCommand backupCmd;
        private final Logger logger;

        public RunnableBackup(final ITarget target, final AbstractCoreResultActionBackup ra, final Logger logger) {
            super(ra);
            this.itarget = target;
            setName("backup_" + ra.getFcoToString());
            this.backupCmd = new BackupCommand(ra);
            this.logger = logger;
        }

        @Override
        public AbstractCoreResultActionBackup getResultAction() {
            return (AbstractCoreResultActionBackup) this.ra;
        }

        @Override
        public void run() {
            try {
                this.backupCmd.actionBackup(this.itarget, getResultAction());

            } catch (final CoreResultActionException e) {
                Utility.logWarning(this.logger, e);
            } finally {
                if (this.logger.isLoggable(Level.INFO)) {
                    this.logger.info("ResultActionConnectThread end");
                }
                getResultAction().done();
            }
        }
    }

    final private Logger logger;
    final private ConnectionManager connectionManager;
     private AbstractCoreResultActionBackup rab;
     final private    BackupOptions options;

    public ApiBackupCommandWrapper(ConnectionManager connectionManager, BackupOptions options) {

	this.logger = Logger.getLogger(this.getClass().getName());
	this.connectionManager = connectionManager;
	this.options=options;
    }

  /*  public List<ResultActionBackup> action() {
	  final List<ResultActionBackup> result = new ArrayList<>();
	        final ITarget target = connectionManager.getRepositoryTarget();
	        if (connectionManager.isConnected()) {
	            final List<IFirstClassObject> fcoList = getFcoTarget(connectionManager, getOptions().getAnyFcoOfType());
	            if (fcoList.isEmpty()) {
	                throw new InvalidTask(Convert.NO_VALID_FCO_TARGETS);
	            } else if (target == null) {
	                throw new InvalidTask(Convert.NO_REPOSITORY_TARGET_ERROR_MESSAGE);
	            } else if (target.isEnable()) {
	                for (final IFirstClassObject fco : fcoList) {
	                   // AbstractCoreResultActionBackup rab = null;
	                    if (fco instanceof VirtualAppManager) {
	                        rab = new CoreResultActionVappBackup(fco, getOptions());
	                        rab.setInteractive(new BackupVappNoInteractive((CoreResultActionVappBackup) rab));
	                    } else if (fco instanceof VirtualMachineManager) {
	                        rab = new CoreResultActionVmBackup(fco, getOptions());
	                        rab.setInteractive(new BackupNoInteractive((CoreResultActionVmBackup) rab));
	                    } else if (fco instanceof ImprovedVirtualDisk) {
	                        rab = new CoreResultActionIvdBackup(fco, getOptions());
	                        rab.setInteractive(new BackupNoInteractive((CoreResultActionIvdBackup) rab));
	                    } else {
	                        throw new InvalidTask("Unsupported type" + fco.getEntityType().toString());
	                    }
//	                    final RunnableBackup runnable = new RunnableBackup(connectionManager.getRepositoryTarget(), rab,
//	                            this.logger);
//	                    runnable.run();
	                    
	                    final Thread thread = new Thread(this);

	            	new ResultThread(this.rab, thread.getId());

	            	thread.setName(BackupApiServiceRunnable.class.getName());
	            	thread.start();
	                    if (fco instanceof VirtualAppManager) {
	                        final ResultActionVappBackup rs = new ResultActionVappBackup();
	                        Convert.ResultActionVappBackup((CoreResultActionVappBackup) rab,rs);
	                        result.add(rs);
	                    } else if (fco instanceof VirtualMachineManager) {
	                        final ResultActionVmBackup rs = new ResultActionVmBackup();
	                       Convert.ResultActionVmBackup((CoreResultActionVmBackup)rab,rs);
	                        result.add(rs);
	                    } else if (fco instanceof ImprovedVirtualDisk) {
	                        final ResultActionIvdBackup rs = new ResultActionIvdBackup();
	                        Convert.ResultActionIvdBackup((CoreResultActionIvdBackup)rab,rs);
	                        result.add(rs);
	                    } else {
	                        throw new InvalidTask(Convert.UNSUPPORTED_ENTITY_TYPE + " " + fco.getEntityType().toString());
	                    }

	                }
	            } else {
	                throw new InvalidTask(Convert.REPOSITORY_NOT_ACTIVE);
	            }
	        } else {
	            throw new InvalidTask(Convert.NO_VCENTER_CONNECTION);
	        }
	 

	if (this.logger.isLoggable(Level.CONFIG)) {
	    this.logger.config("ConnectionManager - end"); //$NON-NLS-1$
	}

	return result;
    }*/
    
    public SapiTasks action (final ConnectionManager connectionManager) {
        final SapiTasks result = new SapiTasks();
        final ITarget target = connectionManager.getRepositoryTarget();
        if (connectionManager.isConnected()) {
            final List<IFirstClassObject> fcoList = getFcoTarget(connectionManager, getOptions().getAnyFcoOfType());
            if (fcoList.isEmpty()) {
        	SapiTasksSupport.skipNoValidFcoTargets(result);
            } else if (target == null) {
        	SapiTasksSupport.noRepositoryTargetFailure(result);
            } else if (target.isEnable()) {
                for (final IFirstClassObject fco : fcoList) {
                    AbstractCoreResultActionBackup rab = null;
                    if (fco instanceof VirtualAppManager) {
                        rab = new CoreResultActionVappBackup(fco, getOptions());
                        rab.setInteractive(new BackupVappNoInteractive((CoreResultActionVappBackup) rab));
                    } else if (fco instanceof VirtualMachineManager) {
                        rab = new CoreResultActionVmBackup(fco, getOptions());
                        rab.setInteractive(new BackupNoInteractive((CoreResultActionVmBackup) rab));
                    } else if (fco instanceof ImprovedVirtualDisk) {
                        rab = new CoreResultActionIvdBackup(fco, getOptions());
                        rab.setInteractive(new BackupNoInteractive((CoreResultActionIvdBackup) rab));
                    } else {
                        rab = null;
                        SapiTasksSupport.   unsupportedTypeFailure(result,fco.getEntityType());
                    }
                    if (rab != null) {
                        final RunnableBackup runnable = new RunnableBackup(connectionManager.getRepositoryTarget(), rab,
                                this.logger);
                        
                        SapiTask task=SapiTasksSupport.newTask(new ResultThread(rab, runnable.getId()));
                       
                        result.addTaskListItem(task);
                        
                        ThreadsManager.executor(ThreadType.FCO).execute(runnable);
                        result.setState(com.vmware.safekeeping.cxf.rest.model.OperationState.SUCCESS);
                    }
                }
            } else {
                SapiTasksSupport.repositoryNotActiveFailure(result);
            }
        } else {
            SapiTasksSupport.noVcenterConnectionFailure(result);
        }
        return result;
    }


    protected void actionBackup(final ConnectionManager connetionManager, final AbstractCoreResultActionBackup rab2)
	    throws CoreResultActionException {
	//connetionManager.connectVimConnetions(rab2);

    }

//    @Override
//    public void run() {
//	if (this.logger.isLoggable(Level.CONFIG)) {
//	    this.logger.config("<no args> - start"); //$NON-NLS-1$
//	}
//	try {
//	    actionBackup(this.connectionManager, this.rab);
//	} catch (final CoreResultActionException e) {
//	    this.logger.severe("<no args> - exception: " + e); //$NON-NLS-1$
//
//	    Utility.logWarning(this.logger, e);
//	    this.rab.failure(e);
//	} finally {
//	    this.rab.done();
//	}
//
//	if (this.logger.isLoggable(Level.CONFIG)) {
//	    this.logger.config("<no args> - end"); //$NON-NLS-1$
//	}
//    }

    public CoreBackupOptions getOptions() {
	CoreBackupOptions result=new CoreBackupOptions();
	  Convert.backupOptions(this.options,result);
	  return result;
    }

    @Override
    protected String getLogName() {return this.getClass().getName();
    }

    @Override
    protected void initialize() {
	// TODO Auto-generated method stub
	
    }
}
