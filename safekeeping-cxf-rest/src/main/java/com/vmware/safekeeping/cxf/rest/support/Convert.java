package com.vmware.safekeeping.cxf.rest.support;

import com.vmware.safekeeping.core.command.options.AbstractCoreBasicConnectOptions;
import com.vmware.safekeeping.core.command.options.CoreCspConnectOptions;
import com.vmware.safekeeping.core.command.options.CorePscConnectOptions;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnectSso;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionDisconnect;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionDisconnectSso;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionDisconnectVcenter;
import com.vmware.safekeeping.cxf.rest.model.ConnectOptions;
import com.vmware.safekeeping.cxf.rest.model.CspConnectOptions;
import com.vmware.safekeeping.cxf.rest.model.EntityType;
import com.vmware.safekeeping.cxf.rest.model.ManagedFcoEntityInfo;
import com.vmware.safekeeping.cxf.rest.model.OperationState;
import com.vmware.safekeeping.cxf.rest.model.PscConnectOptions;
import com.vmware.safekeeping.cxf.rest.model.ResultActionConnectSso;
import com.vmware.safekeeping.cxf.rest.model.SapiTask; 

public class Convert {
    
    public static  com.vmware.safekeeping.cxf.rest.model.ManagedFcoEntityInfo convertManagedFcoEntityInfo(   com.vmware.safekeeping.core.type.ManagedFcoEntityInfo fco){
	       com.vmware.safekeeping.cxf.rest.model.ManagedFcoEntityInfo mfco=new ManagedFcoEntityInfo();
	       mfco.setEntityType(EntityType.fromValue(fco.getEntityType().toString()));
	       mfco.setMorefValue(fco.getMorefValue());
	       mfco.setName(fco.getName());
	       mfco.setServerUuid(fco.getServerUuid());
	       mfco.setUuid(fco.getUuid());
	       mfco.setServerUuid(fco.getServerUuid());
	       return mfco;
	       
	   }

	    public static SapiTask newSapiTask(ICoreResultAction src) {
		 SapiTask task=  new SapiTask();
	         task.setState(OperationState.valueOf(  src.getState().toString()));
	         task.setId(src.getResultActionId());
	         task.setFcoEntity(convertManagedFcoEntityInfo(src.getFcoEntityInfo()));
	         task.setReason(src.getReason()); return task;
	    }
    
    
    public static void  cspConnectOptions(final CspConnectOptions src, final CoreCspConnectOptions dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
         Convert.connectOptions(src, dst);
        dst.setTokenExchangeServer(src.getTokenExchangeServer());
        dst.setRefreshToken(src.getRefreshToken());
    }
    public static void pscConnectOptions(final PscConnectOptions src, final CorePscConnectOptions dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        Convert.connectOptions(src, dst);
        dst.setUser(src.getUser());
        if (src.getPort() != null) {
            dst.setPort(src.getPort());
        }
    }
    public static void connectOptions(final ConnectOptions src, final AbstractCoreBasicConnectOptions dst) { 
        if ((src == null) || (dst == null)) {
            return;
        }
        dst.setBase64(src.isBase64());
        dst.setAuthServer(src.getAuthServer()); 
    }     
    
    
    public static void resultActionConnectSso(final CoreResultActionConnectSso src, final ResultActionConnectSso dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        Convert.resultAction(src, dst);
        dst.setConnected(src.isConnected());
        if (src.getSsoEndPointUrl() != null) {
            dst.setSsoEndPointUrl(src.getSsoEndPointUrl().toString());
        }
        if (src.getToken() != null) {
            dst.setToken(src.getToken());
        }

    }
    
 
    
    public static void resultAction(final ICoreResultAction src, final com.vmware.safekeeping.cxf.rest.model.ResultAction dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        
         SapiTask task=  newSapiTask(src); 
         
        dst.setTask(task);
        dst.setState(task.getState());
        dst.setFcoEntityInfo(task.getFcoEntity());
        dst.setReason(src.getReason());
        dst.setDone(src.isDone());

        dst.setEndTime(src.getEndTime());
        dst.setStartTime(src.getStartTime());
        if (src.getStartDate() != null) {
            dst.setStartDate(src.getStartDate().getTime());
        }
        if (src.getCreationDate() != null) {
            dst.setCreationDate(src.getCreationDate().getTime());
        }
        if (src.getEndDate() != null) {
            dst.setEndDate(src.getEndDate().getTime());
        }
        dst.setProgress(src.getProgress());

        if (src.getParent() != null) {
            dst.setParent(newSapiTask(src.getParent()));
        }
    }
    
    public static void ResultActionDisconnectSso(final CoreResultActionDisconnectSso src, final com.vmware.safekeeping.cxf.rest.model.ResultActionDisconnectSso dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        Convert.resultAction(src, dst);
        
        dst.setConnected(src.isConnected());
        if (src.getSsoEndPointUrl() != null) {
            dst.setSsoEndPointUrl(src.getSsoEndPointUrl().toString());
        }

    }
    public static void ResultActionDisconnect(final CoreResultActionDisconnect src, final com.vmware.safekeeping.cxf.rest.model.ResultActionDisconnect dst) {
        if ((src == null) || (dst == null)) {
            return;
        }
        Convert.resultAction(src, dst);
      //  dst.setSubActionDisconnectSso();
    //    Convert.ResultActionDisconnectSso(src.getSubActionDisconnectSso(), dst.getSubActionDisconnectSso());
         
           try {
            dst.setConnected(src.isConnected());
            for (final CoreResultActionDisconnectVcenter _racvc : src.getSubActionDisconnectVCenters()) {
                dst.getSubTasksActionConnectVCenters().add(newSapiTask(_racvc));
            }
        } catch (final Exception e) {
            src.failure(e);
          
        }
    }
    
    
}
