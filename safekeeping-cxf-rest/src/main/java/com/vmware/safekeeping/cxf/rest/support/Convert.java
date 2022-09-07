package com.vmware.safekeeping.cxf.rest.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.ConcurrentDoublyLinkedList;
import com.vmware.safekeeping.common.FirstClassObjectFilterType;
import com.vmware.safekeeping.core.command.options.CoreBackupOptions;
import com.vmware.safekeeping.core.command.options.CoreBackupRestoreCommonOptions;
import com.vmware.safekeeping.core.command.options.CoreBasicCommandOptions;
import com.vmware.safekeeping.core.command.options.VirtualMachineQuisceSpec;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionBackup;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionBackupForEntityWithDisks;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionBackupRestore;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnect;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnectSso;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnectVcenter;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionDisconnect;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionDisconnectSso;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionDisconnectVcenter;
import com.vmware.safekeeping.core.control.info.FcoTypeSearch;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.type.FcoTarget;
import com.vmware.safekeeping.core.type.enums.BackupMode;
import com.vmware.safekeeping.core.type.enums.WindowsQuiesceSpecVssBackupContext;
import com.vmware.safekeeping.core.type.enums.WindowsQuiesceSpecVssBackupType;
import com.vmware.safekeeping.core.type.location.AbstractCoreFcoLocation;
import com.vmware.safekeeping.core.type.location.CoreIvdLocation;
import com.vmware.safekeeping.core.type.location.CoreVappLocation;
import com.vmware.safekeeping.core.type.location.CoreVmLocation;

public class Convert {
    public static final String INTERNAL_ERROR_MESSAGE = "Internal Error Check the server log";
    public static final String NO_REPOSITORY_TARGET_ERROR_MESSAGE = "No Repository Target available";
    public static final String NO_VALID_CATALOG_ENTITY = "No valid catalogs entity";
    public static final String REPOSITORY_NOT_ACTIVE = "Repository is not active";
    public static final String NO_VCENTER_CONNECTION = "No vCenter connection";
    public static final String NO_VALID_FCO_TARGETS = "No valid Fco targets";
    public static final String UNSUPPORTED_ENTITY_TYPE = "Unsupported entity Type";
    public static final String GLOBAL_PROFILE_ERROR = "Errors accessing the Global Profile file";

    public static com.vmware.safekeeping.cxf.rest.model.ManagedEntityInfo convertManagedEntityInfo(
	    com.vmware.safekeeping.core.type.ManagedEntityInfo fco) {
	com.vmware.safekeeping.cxf.rest.model.ManagedEntityInfo mfco = new com.vmware.safekeeping.cxf.rest.model.ManagedEntityInfo();
	mfco.setEntityType(com.vmware.safekeeping.cxf.rest.model.EntityType.fromValue(fco.getEntityType().toString()));
	mfco.setMorefValue(fco.getMorefValue());
	mfco.setName(fco.getName());
	mfco.setServerUuid(fco.getServerUuid());
	return mfco;

    }

    public static com.vmware.safekeeping.cxf.rest.model.ManagedFcoEntityInfo convertManagedFcoEntityInfo(
	    com.vmware.safekeeping.core.type.ManagedFcoEntityInfo fco) {
	com.vmware.safekeeping.cxf.rest.model.ManagedFcoEntityInfo mfco = new com.vmware.safekeeping.cxf.rest.model.ManagedFcoEntityInfo();
	mfco.setEntityType(com.vmware.safekeeping.cxf.rest.model.EntityType.fromValue(fco.getEntityType().toString()));
	mfco.setMorefValue(fco.getMorefValue());
	mfco.setName(fco.getName());
	mfco.setServerUuid(fco.getServerUuid());
	mfco.setUuid(fco.getUuid());
	return mfco;

    }

    public static com.vmware.safekeeping.cxf.rest.model.SapiTask newSapiTask(ICoreResultAction src) {
	com.vmware.safekeeping.cxf.rest.model.SapiTask task = new com.vmware.safekeeping.cxf.rest.model.SapiTask();
	task.setState(com.vmware.safekeeping.cxf.rest.model.OperationState.valueOf(src.getState().toString()));
	task.setId(src.getResultActionId());
	task.setFcoEntity(convertManagedFcoEntityInfo(src.getFcoEntityInfo()));
	task.setReason(src.getReason());
	return task;
    }

//    public static void  cspConnectOptions(final CspConnectOptions src, final CoreCspConnectOptions dst) {
//        if ((src == null) || (dst == null)) {
//            return;
//        }
//         Convert.connectOptions(src, dst);
//        dst.setTokenExchangeServer(src.getTokenExchangeServer());
//        dst.setRefreshToken(src.getRefreshToken());
//    }
//    public static void pscConnectOptions(final PscConnectOptions src, final CorePscConnectOptions dst) {
//        if ((src == null) || (dst == null)) {
//            return;
//        }
//        Convert.connectOptions(src, dst);
//        dst.setUser(src.getUser());
//        if (src.getPort() != null) {
//            dst.setPort(src.getPort());
//        }
//    }
//    public static void connectOptions(final ConnectOptions src, final AbstractCoreBasicConnectOptions dst) {
//        if ((src == null) || (dst == null)) {
//            return;
//        }
//        dst.setBase64(src.isBase64());
//        dst.setAuthServer(src.getAuthServer());
//    }

    public static void resultActionConnectSso(final CoreResultActionConnectSso src,
	    final com.vmware.safekeeping.cxf.rest.model.ResultActionConnectSso dst) {
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

    public static void resultAction(final ICoreResultAction src,
	    final com.vmware.safekeeping.cxf.rest.model.ResultAction dst) {
	if ((src == null) || (dst == null)) {
	    return;
	}

	com.vmware.safekeeping.cxf.rest.model.SapiTask task = newSapiTask(src);

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

    public static void ResultActionDisconnectSso(final CoreResultActionDisconnectSso src,
	    final com.vmware.safekeeping.cxf.rest.model.ResultActionDisconnectSso dst) {
	if ((src == null) || (dst == null)) {
	    return;
	}
	Convert.resultAction(src, dst);

	dst.setConnected(src.isConnected());
	if (src.getSsoEndPointUrl() != null) {
	    dst.setSsoEndPointUrl(src.getSsoEndPointUrl().toString());
	}

    }

    public static void ResultActionDisconnect(final CoreResultActionDisconnect src,
	    final com.vmware.safekeeping.cxf.rest.model.ResultActionDisconnect dst) {
	if ((src == null) || (dst == null)) {
	    return;
	}
	Convert.resultAction(src, dst);
	// dst.setSubActionDisconnectSso();
	// Convert.ResultActionDisconnectSso(src.getSubActionDisconnectSso(),
	// dst.getSubActionDisconnectSso());

	try {
	    dst.setConnected(src.isConnected());
	    for (final CoreResultActionDisconnectVcenter _racvc : src.getSubActionDisconnectVCenters()) {
		dst.getSubTasksActionConnectVCenters().add(newSapiTask(_racvc));
	    }
	} catch (final Exception e) {
	    src.failure(e);

	}
    }

    public static void resultActionConnect(CoreResultActionConnect src,
	    com.vmware.safekeeping.cxf.rest.model.ResultActionConnect dst) {

	Convert.resultAction(src, dst);

	try {
	    dst.setConnect(src.isConnected());
//dst.setSsoEndPointUrl(src.getSubActionConnectSso().getSsoEndPointUrl().toString());
	    if (src.getSubActionConnectVCenters() != null) {
		for (final CoreResultActionConnectVcenter _racvc : src.getSubActionConnectVCenters()) {
		    dst.getSubTasksActionConnectVCenters().add(newSapiTask(_racvc));

		}
	    }
	} catch (final Exception e) {
	    src.failure(e);
	}
    }

    public static List<com.vmware.safekeeping.cxf.rest.model.ManagedFcoEntityInfo> toManagedFcoEntityInfo(
	    ConcurrentDoublyLinkedList<com.vmware.safekeeping.core.type.ManagedFcoEntityInfo> src) {
	List<com.vmware.safekeeping.cxf.rest.model.ManagedFcoEntityInfo> result = new ArrayList<>();
	for (com.vmware.safekeeping.core.type.ManagedFcoEntityInfo el : src) {
	    result.add(convertManagedFcoEntityInfo(el));
	}
	return result;
    }

    public static List<com.vmware.safekeeping.cxf.rest.model.ManagedEntityInfo> toManagedEntityInfo(
	    List<com.vmware.safekeeping.core.type.ManagedEntityInfo> src) {
	List<com.vmware.safekeeping.cxf.rest.model.ManagedEntityInfo> result = new ArrayList<>();
	for (com.vmware.safekeeping.core.type.ManagedEntityInfo el : src) {
	    result.add(convertManagedEntityInfo(el));
	}
	return result;
    }

    public static void ResultActionVappBackup(
	    com.vmware.safekeeping.core.command.results.CoreResultActionVappBackup src,
	    com.vmware.safekeeping.cxf.rest.model.ResultActionVappBackup dst) {
	try {
	    Convert.ResultActionBackup(src, dst);
	    dst.setPhase(src.getPhase().toString());
	    dst.setNumberOfChildVm(src.getNumberOfChildVm());
	    dst.getFcoChildren().addAll(toManagedFcoEntityInfo(src.getFcoChildren()));
	    for (final AbstractCoreResultActionBackupRestore _rabChild : src.getResultActionOnsChildVm()) {
		dst.getResultActionOnChildVms().add(newSapiTask(_rabChild));
	    }
	} catch (final Exception e) {
	    src.failure(e);
	    Convert.resultAction(src, dst);
	}
    }

    public static void AbstractResultActionBackupForEntityWithDisks(
	    final AbstractCoreResultActionBackupForEntityWithDisks src,
	    final com.vmware.safekeeping.cxf.rest.model.AbstractResultActionBackupForEntityWithDisks dst) {
	if ((src == null) || (dst == null)) {
	    return;
	}
	try {
	    Convert.ResultActionBackup(src, dst);
	    dst.setPhase(src.getPhase().toString());

	    dst.setCompressed(src.isCompressed());
	    dst.setCipher(src.isCipher());
	    dst.setNumberOfDisk(src.getNumberOfDisk());
	} catch (final Exception e) {
	    // Utility.logWarning(
	    // com.vmware.safekeeping.cxf.rest.model.AbstractResultActionBackupForEntityWithDisks
	    // .logger, e);
	    src.failure(e);
	    Convert.resultAction(src, dst);
	}
    }

    public static void ResultActionIvdBackup(com.vmware.safekeeping.core.command.results.CoreResultActionIvdBackup src,
	    com.vmware.safekeeping.cxf.rest.model.ResultActionIvdBackup dst) {
	if ((src == null) || (dst == null)) {
	    return;
	}
	try {
	    Convert.AbstractResultActionBackupForEntityWithDisks(src, dst);
	    if (src.getResultActionOnDisk() != null) {
		dst.setResultActionOnDisk(newSapiTask(src.getResultActionOnDisk()));
	    }

	} catch (final Exception e) {
	    src.failure(e);
	    Convert.resultAction(src, dst);
	}
    }

    public static com.vmware.safekeeping.cxf.rest.model.GuestInfoFlags toGuestInfoFlags(
	    com.vmware.safekeeping.core.type.GuestInfoFlags src) {
	com.vmware.safekeeping.cxf.rest.model.GuestInfoFlags result = new com.vmware.safekeeping.cxf.rest.model.GuestInfoFlags();
	return result;
    }

    public static void ResultActionVmBackup(com.vmware.safekeeping.core.command.results.CoreResultActionVmBackup src,
	    com.vmware.safekeeping.cxf.rest.model.ResultActionVmBackup dst) {
	if ((src == null) || (dst == null)) {
	    return;
	}
	try {
	    Convert.AbstractResultActionBackupForEntityWithDisks(src, dst);
	    dst.setGuestFlags(toGuestInfoFlags(src.getGuestFlags()));
	    dst.setTemplate(src.isTemplate());

	    for (final com.vmware.safekeeping.core.command.results.CoreResultActionDiskBackup _radb : src
		    .getResultActionsOnDisk()) {
		dst.getResultActionsOnDisk().add(newSapiTask(_radb));
	    }
	} catch (final Exception e) {
	    src.failure(e);
	    Convert.resultAction(src, dst);
	}
    }

    public static void ResultActionBackup(final AbstractCoreResultActionBackup src,
	    final com.vmware.safekeeping.cxf.rest.model.ResultActionBackup dst) {
	if ((src == null) || (dst == null)) {
	    return;
	}
	Convert.AbstractResultActionBackupRestore(src, dst);
	dst.setCbtEnabled(src.isCbtEnabled());
	dst.setBackupMode(com.vmware.safekeeping.cxf.rest.model.BackupMode.fromValue(src.getBackupMode().toString()));
    }

    static public void fcoLocation(final AbstractCoreFcoLocation src,
	    final com.vmware.safekeeping.cxf.rest.model.FcoLocation dst) {
	if ((src == null) || (dst == null)) {
	    return;
	}
	dst.setDatacenterInfo(convertManagedEntityInfo(src.getDatacenterInfo()));

    }

    static public void vappLocation(final CoreVappLocation src,
	    final com.vmware.safekeeping.cxf.rest.model.VappLocation dst) {
	if ((src == null) || (dst == null)) {
	    return;
	}
	fcoLocation(src, dst);
	dst.setResourcePoolFullPath(src.getResourcePoolFullPath());

	dst.setVmFolderFullPath(src.getVmFolderFullPath());

	dst.setVAppMember(src.isVAppMember());
	dst.setResourcePoolFullPath(src.getResourcePoolFullPath());

	dst.setResourcePoolPath(toManagedEntityInfo(src.getResourcePoolPath()));
	dst.setVmFolderPath(toManagedEntityInfo(src.getVmFolderPath()));
    }

    static public void vmLocation(final CoreVmLocation src,
	    final com.vmware.safekeeping.cxf.rest.model.VmLocation dst) {
	if ((src == null) || (dst == null)) {
	    return;
	}
	fcoLocation(src, dst);
	dst.setDatastoreInfo(convertManagedEntityInfo(src.getDatastoreInfo()));
	dst.setDatastorePath(src.getDatastorePath());
	dst.setResourcePoolFullPath(src.getResourcePoolFullPath());
	dst.setResourcePoolInfo(convertManagedEntityInfo(src.getResourcePoolInfo()));
	dst.setVmFolderInfo(convertManagedEntityInfo(src.getVmFolderInfo()));

	dst.setVmxFileName(src.getVmxFileName());
	dst.setVAppMember(src.isvAppMember());
	dst.setVmxFullPath(src.getVmxFullPath());
	dst.setResourcePoolPath(toManagedEntityInfo(src.getResourcePoolPath()));
	if (!src.isvAppMember()) {
	    dst.setVmFolderPath(toManagedEntityInfo(src.getVmFolderPath()));
	    dst.setVmFolderFullPath(src.getVmFolderFullPath());
	}
    }

    static public void ivdLocation(final CoreIvdLocation src,
	    final com.vmware.safekeeping.cxf.rest.model.IvdLocation dst) {
	if ((src == null) || (dst == null)) {
	    return;
	}
	fcoLocation(src, dst);
	dst.setDatastoreInfo(convertManagedEntityInfo(src.getDatastoreInfo()));
	dst.setDatastorePath(src.getDatastorePath());
	dst.setVmdkFileName(src.getVmdkFileName());
	dst.setVmdkFullPath(src.getVmdkFullPath());
    }

    static public com.vmware.safekeeping.cxf.rest.model.GenerationInfo toGenerationInfo(
	    final com.vmware.safekeeping.core.type.GenerationInfo src) {
	final com.vmware.safekeeping.cxf.rest.model.GenerationInfo result = new com.vmware.safekeeping.cxf.rest.model.GenerationInfo();
	result.setGenerationId(src.getGenerationId());
	result.setBackupMode(com.vmware.safekeeping.cxf.rest.model.BackupMode.valueOf(src.getBackupMode().toString()));
	result.setPreviousGenerationId(src.getPreviousGenerationId());
	result.setTargetUri(src.getTargetUri());
	return result;
    }

    public static void AbstractResultActionBackupRestore(final AbstractCoreResultActionBackupRestore src,
	    final com.vmware.safekeeping.cxf.rest.model.AbstractResultActionBackupRestore dst) {
	if ((src == null) || (dst == null)) {
	    return;
	}
	Convert.resultAction(src, dst);

	if (src.getLocations() != null) {
	    switch (src.getEntityType()) {
	    case K8sNamespace:
		break;
	    case ImprovedVirtualDisk:
		final com.vmware.safekeeping.cxf.rest.model.IvdLocation ivdLocation = new com.vmware.safekeeping.cxf.rest.model.IvdLocation();
		Convert.ivdLocation((CoreIvdLocation) src.getLocations(), ivdLocation);
		dst.setLocations(ivdLocation);
		break;
	    case VirtualApp:
		final com.vmware.safekeeping.cxf.rest.model.VappLocation vappLocation = new com.vmware.safekeeping.cxf.rest.model.VappLocation();
		Convert.vappLocation((CoreVappLocation) src.getLocations(), vappLocation);
		dst.setLocations(vappLocation);
		break;
	    case VirtualMachine:
		final com.vmware.safekeeping.cxf.rest.model.VmLocation vmLocation = new com.vmware.safekeeping.cxf.rest.model.VmLocation();
		Convert.vmLocation((CoreVmLocation) src.getLocations(), vmLocation);
		dst.setLocations(vmLocation);
		break;
	    default:
		break;
	    }
	}
	dst.setGenerationInfo(Convert.toGenerationInfo(src.getGenerationInfo()));
	dst.setGenerationId(src.getGenerationId());

	dst.setSuccess(src.isSuccessful());
	dst.setTargetName(src.getTargetName());
	dst.setIndex(src.getIndex());

    }

    public static void AbstractBasicCommandOptions(
	    final com.vmware.safekeeping.cxf.rest.model.AbstractBasicCommandOptions src,
	    final CoreBasicCommandOptions dst, final int defaultAnyFcoOfType) {
	if ((src == null) || (dst == null)) {
	    return;
	}
	if (src.getTargetList().isEmpty()) {
	    if (src.getAnyFcoOfType() == null) {
		dst.setAnyFcoOfType(FirstClassObjectFilterType.any | FirstClassObjectFilterType.all);
	    } else {
		dst.setAnyFcoOfType(src.getAnyFcoOfType());
	    }
	} else {
	    dst.setAnyFcoOfType(defaultAnyFcoOfType);
	}
	dst.setDryRun(src.isDryRun());

	dst.setVim((src.getVim() == null) ? StringUtils.EMPTY : src.getVim());
	if (src.getTargetList() != null) {
	    for (final com.vmware.safekeeping.cxf.rest.model.FcoTarget target : src.getTargetList()) {
		dst.getTargetFcoList().put(target.getKey(), Convert.toCoreTarget(target));
	    }
	}
	Convert.targetList2TargetFcoList(src.getTargetList(), dst.getTargetFcoList());
    }

    private static int targetList2TargetFcoList(final List<com.vmware.safekeeping.cxf.rest.model.FcoTarget> src,
	    final Map<String, FcoTarget> dst) {
	int result = 0;
	for (final com.vmware.safekeeping.cxf.rest.model.FcoTarget target : src) {
	    dst.put(target.getKey(), Convert.toCoreTarget(target));
	    ++result;
	}
	return result;
    }

    private static FcoTarget toCoreTarget(com.vmware.safekeeping.cxf.rest.model.FcoTarget src) {
	FcoTarget result = new FcoTarget(src.getKey(), FcoTypeSearch.valueOf(src.getKeyType().toString()),
		src.getVcenterUuid());
	return result;
    }

    public static void BackupRestoreCommonOptions(
	    final com.vmware.safekeeping.cxf.rest.model.BackupRestoreCommonOptions src,
	    final CoreBackupRestoreCommonOptions dst, final int defaultAnyFcoOfType) {
	if ((src == null) || (dst == null)) {
	    return;
	}
	Convert.AbstractBasicCommandOptions(src, dst, defaultAnyFcoOfType);

	dst.setForce(src.isForce());
	if (src.getNumberOfThreads() != null) {
	    dst.setNumberOfThreads(src.getNumberOfThreads());
	}
	dst.setNoVmdk(src.isNoVmdk());

    }
    
    public static com.vmware.safekeeping.core.command.options.VirtualMachineQuisceSpec toVirtualMachineQuisceSpec(final com.vmware.safekeeping.cxf.rest.model.VirtualMachineQuisceSpec src) {
	com.vmware.safekeeping.core.command.options.VirtualMachineQuisceSpec result=new com.vmware.safekeeping.core.command.options.VirtualMachineQuisceSpec();
	result.setVssPartialFileSupport(src.isVssPartialFileSupport() == null
                ? CoreGlobalSettings.getDefaultVssPartialFileSupport()
                : src.isVssPartialFileSupport());
	result.setVssBootableSystemState(src.isVssBootableSystemState() == null
                ? CoreGlobalSettings.getDefaultVssBootableSystemState()
                : src.isVssBootableSystemState());
	result.setVssBackupContext(src.getVssBackupContext() == null ? CoreGlobalSettings.getDefaultVssBackupContext()
                : WindowsQuiesceSpecVssBackupContext.valueOf( src.getVssBackupContext().toString()));
	result.setVssBackupType(src.getVssBackupType() == null ? CoreGlobalSettings.getDefaultVssBackupType()
                :WindowsQuiesceSpecVssBackupType.valueOf( src.getVssBackupType().toString()));
	result.setUseWindowsVss(src.isUseWindowsVss());
        if (result.isUseWindowsVss()) {
            result.setTimeout((src.getTimeout() == null) ? CoreGlobalSettings.getWindowsVssTimeOut() : src.getTimeout());
        } else {
            result.setTimeout((src.getTimeout() == null) ? CoreGlobalSettings.getQuisceTimeout() : src.getTimeout());
        }

        result.setVssRetryOnFail(src.isVssRetryOnFail() == null ? CoreGlobalSettings.getDefaultVssRetryOnFailure()
                : src.isVssRetryOnFail());
    return result;
    }

    public static void backupOptions(final com.vmware.safekeeping.cxf.rest.model.BackupOptions src,
	    final CoreBackupOptions dst) {
	if ((src == null) || (dst == null)) {
	    return;
	}
	Convert.BackupRestoreCommonOptions(src, dst, FirstClassObjectFilterType.any);
	if (src.isCompression() != null) {
	    dst.setCompression(src.isCompression());
	}
	if (src.isCipher() != null) {
	    dst.setCipher(src.isCipher());
	}

	if (src.getMaxBlockSize() != null) {
	    dst.setMaxBlockSize(src.getMaxBlockSize());
	}
	dst.setRequestedBackupMode((src.getRequestedBackupMode() == null) ? BackupMode.UNKNOW
		: com.vmware.safekeeping.core.type.enums.BackupMode.valueOf(src.getRequestedBackupMode().toString()));
	if (src.getQueryBlocksOption() != null) {
	    dst.setQueryBlocksOption(com.vmware.safekeeping.core.type.enums.QueryBlocksOption
		    .valueOf(src.getQueryBlocksOption().toString()));
	}
	if (src.getQuisceSpec() != null) {
	    dst.setQuisceSpec( Convert. toVirtualMachineQuisceSpec(src.getQuisceSpec()));
	}
	if (StringUtils.isNotEmpty(src.getRequestedTransportMode())) {
	    dst.setRequestedTransportModes(src.getRequestedTransportMode());
	}
    }
}
