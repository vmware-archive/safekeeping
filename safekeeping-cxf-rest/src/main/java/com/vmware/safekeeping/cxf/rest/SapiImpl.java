/*******************************************************************************
 * Copyright (C) 2021, VMware Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package com.vmware.safekeeping.cxf.rest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.CoreAwsS3TargetOptions;
import com.vmware.safekeeping.core.command.options.CoreFileTargetOptions;
import com.vmware.safekeeping.core.command.options.ExtensionManagerOptions;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionBackup;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionBackupForEntityWithDisks;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionBackupRestore;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionDiskVirtualBackupAndRestore;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionImpl;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionRestore;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionRestoreForEntityWithDisks;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionVirtualBackup;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionVirtualBackupForEntityWithDisks;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultActionWithSubOperations;
import com.vmware.safekeeping.core.command.results.AbstractCoreResultDiskBackupRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionDiskVirtualBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionIvdBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionIvdRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionIvdVirtualBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionVappVirtualBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVersion;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmBackup;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmRestore;
import com.vmware.safekeeping.core.command.results.CoreResultActionVmVirtualBackup;
import com.vmware.safekeeping.core.command.results.ICoreResultAction;
import com.vmware.safekeeping.core.command.results.archive.AbstractCoreResultActionArchive;
import com.vmware.safekeeping.core.command.results.archive.AbstractCoreResultActionArchiveStatus;
import com.vmware.safekeeping.core.command.results.archive.AbstractCoreResultActionArchiveWithSubOperations;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveCheckGeneration;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveCheckGenerationWithDependencies;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveItem;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveItemsList;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveIvdStatus;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveRemoveGeneration;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveRemoveGenerationWithDependencies;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveRemoveProfile;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveShow;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveVappStatus;
import com.vmware.safekeeping.core.command.results.archive.CoreResultActionArchiveVmStatus;
import com.vmware.safekeeping.core.command.results.connectivity.AbstractCoreResultActionConnectRepository;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnect;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnectAwsS3Repository;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnectFileRepository;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnectSso;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionConnectVcenter;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionDisconnect;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionDisconnectRepository;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionDisconnectSso;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionDisconnectVcenter;
import com.vmware.safekeeping.core.command.results.connectivity.CoreResultActionExtension;
import com.vmware.safekeeping.core.command.results.list.ResultActionsList;
import com.vmware.safekeeping.core.command.results.support.OperationState;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.control.info.ExBlockInfo;
import com.vmware.safekeeping.core.control.target.AwsS3Target;
import com.vmware.safekeeping.core.control.target.FileTarget;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.exception.CoreResultActionException;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.core.logger.VmbkLogFormatter;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.soap.ConnectionManager;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.cxf.rest.support.InvalidOptions;
import com.vmware.safekeeping.cxf.rest.support.ThreadUsersReaperPools;
import com.vmware.safekeeping.cxf.rest.support.UnrecognizedToken;
import com.vmware.safekeeping.cxf.rest.support.User;

@WebService(targetNamespace = "http://cxf.safekeeping.vmware.com/", endpointInterface = "com.vmware.safekeeping.cxf.Sapi", portName = "SapiPort", serviceName = "SapiService")
public class SapiImpl  {

	private static final Map<Integer, Class<?>> classMapping;

	static {
		classMapping = new HashMap<>();
/*
		classMapping.put(AbstractCoreResultActionBackupForEntityWithDisks.class.getName().hashCode(),
				AbstractResultActionBackupForEntityWithDisks.class);
		classMapping.put(AbstractCoreResultActionBackupRestore.class.getName().hashCode(),
				AbstractResultActionBackupRestore.class);
		classMapping.put(AbstractCoreResultActionVirtualBackupForEntityWithDisks.class.getName().hashCode(),
				AbstractResultActionVirtualBackupForEntityWithDisks.class);
		classMapping.put(AbstractCoreResultActionDiskVirtualBackupAndRestore.class.getName().hashCode(),
				AbstractResultActionDiskVirtualOperation.class);
		classMapping.put(AbstractCoreResultActionRestoreForEntityWithDisks.class.getName().hashCode(),
				AbstractResultActionRestoreForEntityWithDisks.class);
		classMapping.put(AbstractCoreResultActionWithSubOperations.class.getName().hashCode(),
				AbstractResultActionWithSubOperations.class);
		classMapping.put(AbstractCoreResultDiskBackupRestore.class.getName().hashCode(),
				AbstractResultDiskBackupRestore.class);
		classMapping.put(AbstractCoreResultActionImpl.class.getName().hashCode(), ResultAction.class);
		classMapping.put(AbstractCoreResultActionBackup.class.getName().hashCode(), ResultActionBackup.class);
		classMapping.put(AbstractCoreResultActionVirtualBackup.class.getName().hashCode(),
				AbstractResultActionVirtualBackup.class);
		classMapping.put(CoreResultActionDiskBackup.class.getName().hashCode(), ResultActionDiskBackup.class);
		classMapping.put(CoreResultActionDiskVirtualBackup.class.getName().hashCode(),
				ResultActionDiskVirtualBackup.class);
		classMapping.put(CoreResultActionDiskRestore.class.getName().hashCode(), ResultActionDiskRestore.class);
		classMapping.put(CoreResultActionIvdBackup.class.getName().hashCode(), ResultActionIvdBackup.class);
		classMapping.put(CoreResultActionIvdVirtualBackup.class.getName().hashCode(),
				ResultActionIvdVirtualBackup.class);
		classMapping.put(CoreResultActionIvdRestore.class.getName().hashCode(), ResultActionIvdRestore.class);
		classMapping.put(AbstractCoreResultActionRestore.class.getName().hashCode(), ResultActionRestore.class);
		classMapping.put(CoreResultActionVappBackup.class.getName().hashCode(), ResultActionVappBackup.class);
		classMapping.put(CoreResultActionVappVirtualBackup.class.getName().hashCode(),
				ResultActionVappVirtualBackup.class);
		classMapping.put(CoreResultActionVappRestore.class.getName().hashCode(), ResultActionVappRestore.class);
		classMapping.put(CoreResultActionVersion.class.getName().hashCode(), ResultActionVersion.class);
		classMapping.put(CoreResultActionVmBackup.class.getName().hashCode(), ResultActionVmBackup.class);
		classMapping.put(CoreResultActionVmVirtualBackup.class.getName().hashCode(), ResultActionVmVirtualBackup.class);
		classMapping.put(CoreResultActionVmRestore.class.getName().hashCode(), ResultActionVmRestore.class);

		// com.vmware.safekeeping.external.result.connectivity
		classMapping.put(AbstractCoreResultActionConnectRepository.class.getName().hashCode(),
				AbstractResultActionConnectRepository.class);
		classMapping.put(CoreResultActionConnect.class.getName().hashCode(), ResultActionConnect.class);
		classMapping.put(CoreResultActionConnectAwsS3Repository.class.getName().hashCode(),
				ResultActionConnectAwsS3Repository.class);
		classMapping.put(CoreResultActionConnectFileRepository.class.getName().hashCode(),
				ResultActionConnectFileRepository.class);
		classMapping.put(CoreResultActionConnectSso.class.getName().hashCode(), ResultActionConnectSso.class);
		classMapping.put(CoreResultActionConnectVcenter.class.getName().hashCode(), ResultActionConnectVcenter.class);
		classMapping.put(CoreResultActionDisconnect.class.getName().hashCode(), ResultActionDisconnect.class);
		classMapping.put(CoreResultActionDisconnectRepository.class.getName().hashCode(),
				ResultActionDisconnectRepository.class);
		classMapping.put(CoreResultActionDisconnectSso.class.getName().hashCode(), ResultActionDisconnectSso.class);
		classMapping.put(CoreResultActionDisconnectVcenter.class.getName().hashCode(),
				ResultActionDisconnectVcenter.class);

		// com.vmware.safekeeping.external.result.archive
		classMapping.put(AbstractCoreResultActionArchive.class.getName().hashCode(), AbstractResultActionArchive.class);
		classMapping.put(AbstractCoreResultActionArchiveWithSubOperations.class.getName().hashCode(),
				AbstractResultActionArchiveWithSubOperations.class);
		classMapping.put(CoreResultActionArchiveCheckGeneration.class.getName().hashCode(),
				ResultActionArchiveCheckGeneration.class);
		classMapping.put(CoreResultActionArchiveCheckGenerationWithDependencies.class.getName().hashCode(),
				ResultActionArchiveCheckGenerationWithDependencies.class);
		classMapping.put(CoreResultActionArchiveItem.class.getName().hashCode(), ResultActionArchiveItem.class);
		classMapping.put(CoreResultActionArchiveItemsList.class.getName().hashCode(),
				ResultActionArchiveItemsList.class);
		classMapping.put(CoreResultActionArchiveIvdStatus.class.getName().hashCode(),
				ResultActionArchiveIvdStatus.class);
		classMapping.put(CoreResultActionArchiveRemoveProfile.class.getName().hashCode(),
				ResultActionArchiveRemovedProfile.class);
		classMapping.put(CoreResultActionArchiveRemoveGeneration.class.getName().hashCode(),
				ResultActionArchiveRemoveGeneration.class);
		classMapping.put(CoreResultActionArchiveRemoveGenerationWithDependencies.class.getName().hashCode(),
				ResultActionArchiveRemoveGenerationWithDependencies.class);
		classMapping.put(CoreResultActionArchiveShow.class.getName().hashCode(), ResultActionArchiveShow.class);
		classMapping.put(AbstractCoreResultActionArchiveStatus.class.getName().hashCode(),
				ResultActionArchiveStatus.class);
		classMapping.put(CoreResultActionArchiveVappStatus.class.getName().hashCode(),
				ResultActionArchiveVappStatus.class);
		classMapping.put(CoreResultActionArchiveVmStatus.class.getName().hashCode(), ResultActionArchiveVmStatus.class);
		classMapping.put(CoreResultActionExtension.class.getName().hashCode(), ResultActionExtension.class);*/

	}

	private boolean debugMode;
	private final Logger logger;
 	private final ConcurrentHashMap<String, User> usersList;
	private final FileHandler filehandler;

	@Resource(name = "wsContext")
	private WebServiceContext wsContext;

	private final Vmbk vmbk;

	private ExtensionManagerOptions extensionOptions;

	public SapiImpl(final Vmbk vmbk) throws IOException {
		this.vmbk = vmbk;
		 this.usersList = new ConcurrentHashMap<>();
		this.logger = Logger.getLogger(SapiImpl.class.getName());
		this.filehandler = new FileHandler(CoreGlobalSettings.getLogsPath() + File.separator + "soap.log");
		this.filehandler.setFormatter(new VmbkLogFormatter());
		this.logger.addHandler(this.filehandler);
		this.logger.setUseParentHandlers(false);
	//	ThreadReaperPools.initialize();
	//	ThreadUsersReaperPools.initialize(this.usersList);
		this.debugMode = false;

	}

	public SapiImpl(final Vmbk vmbk, final boolean debugMode, final ExtensionManagerOptions extensionOptions)
			throws IOException {
		this(vmbk);
		this.debugMode = debugMode;
		this.extensionOptions = extensionOptions;

	}
	
	void close() {
		for (final User user : this.usersList.values()) {
			user.close();
		}
		this.usersList.clear();
	//	ThreadReaperPools.shutdown();
		ThreadUsersReaperPools.shutdown();
		this.logger.removeHandler(this.filehandler);
		this.vmbk.close();
	}
	
	public boolean isDebugMode() {
		return this.debugMode;
	}
//
//	@Override
//	public List<ResultActionBackup> backup(final BackupOptions options) throws UnrecognizedToken, InvalidTask {
//		final User user = precheck();
//		final ConnectionManager connection = user.getConnection();
//		final ExternalBackupCommand backup = new ExternalBackupCommand(options);
//		return backup.action(connection);
//	}
//
//	@Override
//	public Tasks backupAsync(final BackupOptions options) throws UnrecognizedToken {
//		final User user = precheck();
//		final ConnectionManager connection = user.getConnection();
//		final ExternalBackupCommand backup = new ExternalBackupCommand(options);
//		return backup.actionAsync(connection);
//	}
//
//	/**
//	 * Check authentication
//	 *
//	 * @param messageContext
//	 * @return
//	 * @throws UnrecognizedToken
//	 */
//
//	private String check(final MessageContext messageContext) throws UnrecognizedToken {
//		// get request headers
//		final Map<?, ?> requestHeaders = (Map<?, ?>) messageContext.get(MessageContext.HTTP_REQUEST_HEADERS);
//		final List<?> tokenList = (List<?>) requestHeaders.get("Auth-token");
//
//		String result = null;
//		if (tokenList != null) {
//			result = tokenList.get(0).toString();
//		}
//		if (result == null) {
//			this.logger.warning("No Token");
//			throw new UnrecognizedToken();
//		}
//		if (!this.usersList.containsKey(result)) {
//			final String msg = "Unrecognized Token :" + result;
//			this.logger.warning(msg);
//			throw new UnrecognizedToken(result);
//		}
//
//		return result;
//
//	}
//
//	@Override
//	public Tasks checkArchiveGenerations(final ArchiveCheckGenerationsOptions options) throws UnrecognizedToken {
//		final User user = precheck();
//		final ExternalArchiveCommandCheckGenerations arcList = new ExternalArchiveCommandCheckGenerations(options);
//		return arcList.action(user.getTargetByName(options.getTargetName()));
//	}
//
	 
//
//	@Override
//	public ResultActionConnect connect() throws UnrecognizedToken {
//		final User user = precheck();
//		final ConnectionManager connection = user.getConnection();
//		final ExternalConnectCommand connect = new ExternalConnectCommand();
//		return connect.connect(connection);
//	}
//
//	@Override
//	public Task connectAsync() throws UnrecognizedToken {
//		final User user = precheck();
//		final ConnectionManager connection = user.getConnection();
//		Task result;
//		final ExternalConnectCommand connect = new ExternalConnectCommand();
//		final ResultThread rt = connect.connectAsync(connection);
//		result = new Task(rt);
//		return result;
//	}
//
//	@Override
//	public AbstractResultActionConnectRepository connectRepository(final RepositoryOptions options)
//			throws UnrecognizedToken, InvalidTask {
//		final User user = precheck();
//		final ExternalConnectTargetCommand connect = new ExternalConnectTargetCommand(options, user.getConnection(),
//				user.getTargets());
//		return connect.connect();
//	}
//
//	@Override
//	public Task connectRepositoryAsync(final RepositoryOptions options) throws UnrecognizedToken, InvalidTask {
//		final User user = precheck();
//		Task result;
//		final ExternalConnectTargetCommand connect = new ExternalConnectTargetCommand(options, user.getConnection(),
//				user.getTargets());
//		final ResultThread rt = connect.connectAsync();
//		result = new Task(rt);
//		return result;
//	}
//
//	@Override
//	public List<AbstractResultActionVirtualBackup> virtualBackup(final VirtualBackupOptions options)
//			throws UnrecognizedToken, InvalidTask {
//		final User user = precheck();
//		final ConnectionManager connection = user.getConnection();
//		final ExternalVirtualBackupCommand vBackup = new ExternalVirtualBackupCommand(options);
//		return vBackup.action(connection);
//	}
//
//	@Override
//	public Tasks virtualBackupAsync(final VirtualBackupOptions options) throws UnrecognizedToken {
//		final User user = precheck();
//		final ConnectionManager connection = user.getConnection();
//		final ExternalVirtualBackupCommand vBackup = new ExternalVirtualBackupCommand(options);
//		return vBackup.actionAsync(connection);
//	}
//
//	@Override
//	public ResultActionDisconnect disconnect() throws UnrecognizedToken {
//		final User user = precheck();
//		final ConnectionManager connection = user.getConnection();
//		final ExternalDisconnectComand connect = new ExternalDisconnectComand();
//		return connect.action(connection);
//	}
//
//	@Override
//	public ResultActionExtension extension(ExtensionOptions options) throws UnrecognizedToken, InternalServer {
//		final User user = precheck();
//		final ConnectionManager connection = user.getConnection();
//		final ExternalExtensionCommand connect = new ExternalExtensionCommand(options);
//		try {
//			return connect.action(connection, extensionOptions);
//
//		} catch (final CoreResultActionException e) {
//			Utility.logWarning(this.logger, e);
//			throw new InternalServer();
//		}
//
//	}
//
//	@Override
//	public Task disconnectAsync() throws UnrecognizedToken {
//		final User user = precheck();
//		final ConnectionManager connection = user.getConnection();
//		final ExternalDisconnectComand connect = new ExternalDisconnectComand();
//
//		ResultThread rt;
//		rt = connect.actionAsync(connection);
//
//		return new Task(rt);
//	}
//
//	@Override
//	public ResultActionDisconnectRepository disconnectRepository(final String name) throws UnrecognizedToken {
//
//		final User user = precheck();
//		final ExternalDisconnectTargetCommand connect = new ExternalDisconnectTargetCommand(name, user.getConnection(),
//				user.getTargets());
//		return connect.disconnect();
//	}
//
//	@Override
//	public Task disconnectRepositoryAsync(final String name) throws UnrecognizedToken {
//		final User user = precheck();
//		Task result;
//		final ExternalDisconnectTargetCommand connect = new ExternalDisconnectTargetCommand(name, user.getConnection(),
//				user.getTargets());
//		final ResultThread rt = connect.disconnectAsync();
//		result = new Task(rt);
//		return result;
//	}
//
//	@Override
//	public RepositoryOptions getActiveRepository() throws UnrecognizedToken {
//		final User user = precheck();
//		return getRepository(user.getConnection().getRepositoryTarget().getName());
//	}
//
//	@Override
//	public List<BlockInfo> getAllDumps(final Task id) throws UnrecognizedToken, InvalidTask {
//		return getDumps(id, 0, -1);
//	}
//
//	@Override
//	public Calendar getCurrentTime() throws UnrecognizedToken {
//		precheck();
//		return new GregorianCalendar(TimeZone.getTimeZone("GMT"));
//	}
//
//	@Override
//	public List<BlockInfo> getDumps(final Task id, final int start, int end) throws UnrecognizedToken, InvalidTask {
//		precheck();
//		final List<BlockInfo> result = new LinkedList<>();
//
//		if (ResultActionsList.contains(id.getId())) {
//			final ICoreResultAction obj = ResultActionsList.getResultAction(id.getId());
//			if (obj instanceof AbstractCoreResultDiskBackupRestore) {
//				final Map<Integer, ExBlockInfo> dump = ((AbstractCoreResultDiskBackupRestore) obj).getDumpMap();
//				if (dump != null) {
//					if (end == -1) {
//						end = dump.size();
//					}
//					for (Integer index = start; index < end; index++) {
//						if (dump.containsKey(index)) {
//							result.add(BlockInfo.newInstance(dump.get(index)));
//						} else {
//							break;
//						}
//					}
//				}
//			} else {
//				throw new InvalidTask(AbstractCoreResultDiskBackupRestore.class, obj);
//			}
//		}
//
//		return result;
//	}
//
//	@Override
//	public Set<RepositoryOptions> getRepositories() throws UnrecognizedToken {
//		final User user = precheck();
//		final Set<RepositoryOptions> result = new HashSet<>();
//		for (final Entry<String, ITarget> entry : user.getTargets().entrySet()) {
//			final boolean active = (entry.getKey().equals(user.getConnection().getRepositoryTarget().getName()));
//
//			switch (entry.getValue().getTargetType()) {
//			case AwsS3Target.TARGET_TYPE_NAME:
//				final AwsS3RepositoryOptions resS3 = new AwsS3RepositoryOptions();
//				resS3.convert((CoreAwsS3TargetOptions) entry.getValue().getOptions());
//				resS3.setActive(active);
//				result.add(resS3);
//				break;
//			case FileTarget.TARGET_TYPE_NAME:
//				final FileRepositoryOptions resFl = new FileRepositoryOptions();
//				resFl.convert((CoreFileTargetOptions) entry.getValue().getOptions());
//				resFl.setActive(active);
//				result.add(resFl);
//				break;
//
//			default:
//				break;
//			}
//		}
//
//		return result;
//
//	}
//
//	@Override
//	public Set<String> getRepositoriesNames() throws UnrecognizedToken {
//		final User user = precheck();
//		return user.getTargets().keySet();
//	}
//
//	@Override
//	public RepositoryOptions getRepository(final String name) throws UnrecognizedToken {
//		final User user = precheck();
//		RepositoryOptions result = null;
//		final ITarget target = user.getTargetByName(name);
//
//		switch (target.getTargetType()) {
//		case AwsS3Target.TARGET_TYPE_NAME:
//			final AwsS3RepositoryOptions resS3 = new AwsS3RepositoryOptions();
//			resS3.convert((CoreAwsS3TargetOptions) target);
//			result = resS3;
//			break;
//		case FileTarget.TARGET_TYPE_NAME:
//			final FileRepositoryOptions resFl = new FileRepositoryOptions();
//			resFl.convert((CoreFileTargetOptions) target);
//			result = resFl;
//			break;
//
//		default:
//			break;
//		}
//		if (result != null) {
//			final boolean active = (name.equals(user.getConnection().getRepositoryTarget().getName()));
//			result.setActive(active);
//		}
//		return result;
//	}
//
//	@Override
//	public TaskResult getTaskInfo(final Task id) throws UnrecognizedToken, InvalidTask, InternalServer {
//		final TaskResult result = new TaskResult();
//
//		try {
//			precheck();
//			if (id == null) {
//				throw new InvalidTask("invalid id");
//			}
//			result.setId(id.getId());
//			LockSupport.parkUntil(Utility.HALF_SECOND_IN_MILLIS);
//
//			if (ResultActionsList.contains(id.getId())) {
//				final ICoreResultAction obj = ResultActionsList.getResultAction(id.getId());
//				final Integer key = obj.getClass().getName().hashCode();
//				if (classMapping.containsKey(key)) {
//					final Class<?> cls = classMapping.get(key);
//					final ResultAction clsInstance = (ResultAction) cls.getDeclaredConstructor().newInstance();
//					clsInstance.convert(obj);
//					result.setResult(clsInstance);
//				} else {
//					final String msg = String.format("Class %s has no mapping", obj.getClass().getName());
//					this.logger.warning(msg);
//					throw new InternalServer(msg);
//				}
//			}
//		} catch (final UnrecognizedToken | InvalidTask e) {
//			Utility.logWarning(this.logger, e);
//			throw e;
//		} catch (InstantiationException | SecurityException | NoSuchMethodException | InvocationTargetException
//				| IllegalArgumentException | IllegalAccessException e) {
//
//			Utility.logWarning(this.logger, e);
//			throw new InternalServer();
//		}
//
//		return result;
//	}
//
//	/**
//	 * @return the usersList
//	 */
//
//	Map<String, User> getUsersList() {
//		return this.usersList;
//	}
//
//	@Override
//	public ResultActionVersion getVersion() throws InternalCoreResult {
//		ResultActionVersion result = null;
//		final ExternalVersionCommand version = new ExternalVersionCommand();
//
//		try {
//			result = version.action();
//		} catch (final CoreResultActionException e) {
//			Utility.logWarning(this.logger, e);
//			throw new InternalCoreResult();
//		}
//
//		return result;
//
//	}
//
//	@Override
//	public GregorianCalendar keepalive() throws UnrecognizedToken {
//		final User user = precheck();
//		GregorianCalendar calendar = new GregorianCalendar();
//		calendar.setTime(new Date(user.getLastOperation()));
//		return calendar;
//	}
//
//	@Override
//	public String echo(final String extra) {
//		if (extra != null) {
//			if (this.debugMode && StringUtils.equals(extra, "cheat")) {
//				if (this.usersList.isEmpty()) {
//					return null;
//				}
//				return this.usersList.keySet().iterator().next();
//
//			} else {
//				return "Hello world from " + extra;
//			}
//		}
//		return "";
//	}
//
//	@Override
//	public boolean isConnected() throws UnrecognizedToken {
//		final User user = precheck();
//		return user.getConnection().isConnected();
//	}
//
	 
//
//	@Override
//	public Task listArchive(final ArchiveListOptions options)
//			throws UnrecognizedToken, InvalidOptions, InternalCoreResult {
//		final User user = precheck();
//		Task result = null;
//		try {
//			final ExternalArchiveCommandList arcList = new ExternalArchiveCommandList(options);
//			final ResultThread rt = arcList.action(user.getTargetByName(options.getTargetName()));
//
//			result = new Task(rt);
//			result.setState(rt.getState());
//		} catch (final ParseException e) {
//			Utility.logWarning(this.logger, e);
//			throw new InvalidOptions(e.getMessage());
//		}
//		return result;
//	}
//
//	@Override
//	public ResultActionConnectSso loginPsc(final PscConnectOptions options) throws InternalCoreResult {
//		final CoreResultActionConnectSso racs = new CoreResultActionConnectSso();
//		try {
//			final ExternalConnectCommand connect = new ExternalConnectCommand(options);
//			if (connect.connectSso(racs, options.getPassword())) {
//				final User user = new User(connect.getConnectionManager());
//				getUsersList().put(racs.getToken(), user);
//			}
//		} catch (final CoreResultActionException | SafekeepingException e) {
//			Utility.logWarning(this.logger, e);
//			throw new InternalCoreResult();
//		} finally {
//			racs.done();
//		}
//		final ResultActionConnectSso result = new ResultActionConnectSso();
//		result.convert(racs);
//		return result;
//	}
//
//	@Override
//	public ResultActionConnectSso loginCsp(final CspConnectOptions options) throws InternalCoreResult {
//		final CoreResultActionConnectSso racs = new CoreResultActionConnectSso();
//		try {
//
//			final ExternalConnectCommand connect = new ExternalConnectCommand(options);
//			if (connect.connectSso(racs, options.getRefreshToken())) {
//				final User user = new User(connect.getConnectionManager());
//				getUsersList().put(racs.getToken(), user);
//			}
//		} catch (final CoreResultActionException | SafekeepingException e) {
//			Utility.logWarning(this.logger, e);
//			throw new InternalCoreResult();
//		} finally {
//			racs.done();
//		}
//		final ResultActionConnectSso result = new ResultActionConnectSso();
//		result.convert(racs);
//		return result;
//	}
//
//	@Override
//	public ResultActionDisconnectSso logout() throws UnrecognizedToken, InternalCoreResult {
//		if (this.wsContext == null) {
//			throw new UnrecognizedToken("wsContext is null");
//		}
//		final String id = check(this.wsContext.getMessageContext());
//		final CoreResultActionDisconnectSso rads = new CoreResultActionDisconnectSso();
//		try {
//			final ExternalDisconnectComand connect = new ExternalDisconnectComand();
//			connect.actionEndSsoSession(getUsersList().get(id).getConnection(), rads);
//			if (getUsersList().remove(id) == null) {
//				rads.failure("User not registered");
//			}
//		} catch (final CoreResultActionException e) {
//			Utility.logWarning(this.logger, e);
//			throw new InternalCoreResult();
//		} finally {
//			rads.done();
//		}
//		final ResultActionDisconnectSso result = new ResultActionDisconnectSso();
//		ResultActionDisconnectSso.convert(rads, result);
//		return result;
//	}
//
//	/**
//	 * Check the validity of the WSContext
//	 *
//	 * @return
//	 * @throws UnrecognizedToken
//	 */
//	private User precheck() throws UnrecognizedToken {
//		if (this.wsContext == null) {
//			throw new UnrecognizedToken("wsContext is null");
//		}
//		final String id = check(this.wsContext.getMessageContext());
//		final User result = getUsersList().get(id);
//		result.setLastOperation((new Date()).getTime());
//		return result;
//	}
//
//	@Override
//	public List<ResultActionArchiveRemoveGenerationWithDependencies> removeArchiveGenerations(
//			final ArchiveRemoveGenerationsOptions options) throws UnrecognizedToken, InternalServer, InvalidTask {
//		final User user = precheck();
//		final ExternalArchiveCommandRemoveGenerations arcList = new ExternalArchiveCommandRemoveGenerations(options);
//		return arcList.action(user.getTargetByName(options.getTargetName()));
//
//	}
//
//	@Override
//	public Tasks removeArchiveGenerationsAsync(final ArchiveRemoveGenerationsOptions options) throws UnrecognizedToken {
//		final User user = precheck();
//		final ExternalArchiveCommandRemoveGenerations arcList = new ExternalArchiveCommandRemoveGenerations(options);
//		return arcList.actionAsync(user.getTargetByName(options.getTargetName()));
//
//	}
//
//	@Override
//	public List<ResultActionArchiveRemovedProfile> removeArchiveProfile(final ArchiveRemoveProfileOptions options)
//			throws UnrecognizedToken, InternalServer, InvalidTask {
//		final User user = precheck();
//		final ExternalArchiveCommandRemoveProfile arcProfileRemove = new ExternalArchiveCommandRemoveProfile(options);
//		return arcProfileRemove.action(user.getTargetByName(options.getTargetName()));
//	}
//
//	@Override
//	public Tasks removeArchiveProfileAsync(final ArchiveRemoveProfileOptions options) throws UnrecognizedToken {
//		final User user = precheck();
//		final ExternalArchiveCommandRemoveProfile arcProfileRemove = new ExternalArchiveCommandRemoveProfile(options);
//		return arcProfileRemove.actionAsync(user.getTargetByName(options.getTargetName()));
//	}
//
//	@Override
//	public List<ResultActionRestore> restore(final RestoreOptions options)
//			throws UnrecognizedToken, InvalidTask, InternalServer {
//		final User user = precheck();
//		final ConnectionManager connection = user.getConnection();
//		final ExternalRestoreCommand restore = new ExternalRestoreCommand(options);
//		return restore.action(connection);
//
//	}
//
//	@Override
//	public Tasks restoreAsync(final RestoreOptions options) throws UnrecognizedToken {
//		final User user = precheck();
//		final ConnectionManager connection = user.getConnection();
//		final ExternalRestoreCommand restore = new ExternalRestoreCommand(options);
//		return restore.actionAsync(connection);
//
//	}
//
//	@Override
//	public Task setActiveRepository(final String name) throws UnrecognizedToken {
//		final User user = precheck();
//		final Task result = new Task();
//
//		result.setFcoEntity(ManagedFcoEntityInfo.newNullManagedEntityInfo());
//		if (user.getTargets().containsKey(name)) {
//			final ITarget target = user.getTargets().get(name);
//			user.getConnection().setRepositoryTarget(target);
//			result.setState(OperationState.SUCCESS);
//		} else {
//			result.setState(OperationState.FAILED);
//			result.setReason("No target named " + name + " exist");
//		}
//		return result;
//	}
//
//	@Override
//	public List<ResultActionArchiveShow> showArchive(final ArchiveShowOptions options)
//			throws UnrecognizedToken, InternalServer, InvalidTask {
//		final User user = precheck();
//		final ExternalArchiveCommandShow arcShow = new ExternalArchiveCommandShow(options);
//		return arcShow.action(user.getTargetByName(options.getTargetName()));
//
//	}
//
//	@Override
//	public Tasks showArchiveAsync(final ArchiveShowOptions options) throws UnrecognizedToken {
//		final User user = precheck();
//		final ExternalArchiveCommandShow arcShow = new ExternalArchiveCommandShow(options);
//		return arcShow.actionAsync(user.getTargetByName(options.getTargetName()));
//
//	}
//
//	@Override
//	public List<ResultActionArchiveStatus> statusArchive(final ArchiveStatusOptions options)
//			throws UnrecognizedToken, InternalServer, InvalidTask {
//		final User user = precheck();
//		final ExternalArchiveCommandStatus arcList = new ExternalArchiveCommandStatus(options);
//		return arcList.action(user.getTargetByName(options.getTargetName()));
//
//	}
//
//	@Override
//	public Tasks statusArchiveAsync(final ArchiveStatusOptions options) throws UnrecognizedToken {
//		final User user = precheck();
//		final ExternalArchiveCommandStatus arcList = new ExternalArchiveCommandStatus(options);
//		return arcList.actionAsync(user.getTargetByName(options.getTargetName()));
//
//	}
}
