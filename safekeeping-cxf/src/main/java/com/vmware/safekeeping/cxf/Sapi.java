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
package com.vmware.safekeeping.cxf;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.vmware.safekeeping.cxf.support.InvalidOptions;
import com.vmware.safekeeping.cxf.support.UnrecognizedToken;
import com.vmware.safekeeping.external.command.support.Task;
import com.vmware.safekeeping.external.command.support.Tasks;
import com.vmware.safekeeping.external.exception.InternalCoreResult;
import com.vmware.safekeeping.external.exception.InternalServer;
import com.vmware.safekeeping.external.exception.InvalidTask;
import com.vmware.safekeeping.external.result.AbstractResultActionVirtualBackup;
import com.vmware.safekeeping.external.result.ResultActionBackup;
import com.vmware.safekeeping.external.result.ResultActionDiskBackup;
import com.vmware.safekeeping.external.result.ResultActionDiskRestore;
import com.vmware.safekeeping.external.result.ResultActionDiskVirtualBackup;
import com.vmware.safekeeping.external.result.ResultActionIvdBackup;
import com.vmware.safekeeping.external.result.ResultActionIvdRestore;
import com.vmware.safekeeping.external.result.ResultActionIvdVirtualBackup;
import com.vmware.safekeeping.external.result.ResultActionRestore;
import com.vmware.safekeeping.external.result.ResultActionVappBackup;
import com.vmware.safekeeping.external.result.ResultActionVappRestore;
import com.vmware.safekeeping.external.result.ResultActionVappVirtualBackup;
import com.vmware.safekeeping.external.result.ResultActionVersion;
import com.vmware.safekeeping.external.result.ResultActionVmBackup;
import com.vmware.safekeeping.external.result.ResultActionVmRestore;
import com.vmware.safekeeping.external.result.ResultActionVmVirtualBackup;
import com.vmware.safekeeping.external.result.TaskResult;
import com.vmware.safekeeping.external.result.archive.ResultActionArchiveCheckGeneration;
import com.vmware.safekeeping.external.result.archive.ResultActionArchiveCheckGenerationWithDependencies;
import com.vmware.safekeeping.external.result.archive.ResultActionArchiveItem;
import com.vmware.safekeeping.external.result.archive.ResultActionArchiveItemsList;
import com.vmware.safekeeping.external.result.archive.ResultActionArchiveIvdStatus;
import com.vmware.safekeeping.external.result.archive.ResultActionArchiveRemoveGeneration;
import com.vmware.safekeeping.external.result.archive.ResultActionArchiveRemoveGenerationWithDependencies;
import com.vmware.safekeeping.external.result.archive.ResultActionArchiveRemovedProfile;
import com.vmware.safekeeping.external.result.archive.ResultActionArchiveShow;
import com.vmware.safekeeping.external.result.archive.ResultActionArchiveStatus;
import com.vmware.safekeeping.external.result.archive.ResultActionArchiveVappStatus;
import com.vmware.safekeeping.external.result.archive.ResultActionArchiveVmStatus;
import com.vmware.safekeeping.external.result.connectivity.AbstractResultActionConnectRepository;
import com.vmware.safekeeping.external.result.connectivity.ResultActionConnect;
import com.vmware.safekeeping.external.result.connectivity.ResultActionConnectAwsS3Repository;
import com.vmware.safekeeping.external.result.connectivity.ResultActionConnectFileRepository;
import com.vmware.safekeeping.external.result.connectivity.ResultActionConnectSso;
import com.vmware.safekeeping.external.result.connectivity.ResultActionConnectVcenter;
import com.vmware.safekeeping.external.result.connectivity.ResultActionDisconnect;
import com.vmware.safekeeping.external.result.connectivity.ResultActionDisconnectRepository;
import com.vmware.safekeeping.external.result.connectivity.ResultActionDisconnectSso;
import com.vmware.safekeeping.external.result.connectivity.ResultActionDisconnectVcenter;
import com.vmware.safekeeping.external.result.connectivity.ResultActionExtension;
import com.vmware.safekeeping.external.type.BlockInfo;
import com.vmware.safekeeping.external.type.RestoreIvdManagedInfo;
import com.vmware.safekeeping.external.type.RestoreVappManagedInfo;
import com.vmware.safekeeping.external.type.RestoreVmManagedInfo;
import com.vmware.safekeeping.external.type.options.BackupOptions;
import com.vmware.safekeeping.external.type.options.CspConnectOptions;
import com.vmware.safekeeping.external.type.options.ExtensionOptions;
import com.vmware.safekeeping.external.type.options.PscConnectOptions;
import com.vmware.safekeeping.external.type.options.RepositoryOptions;
import com.vmware.safekeeping.external.type.options.RestoreOptions;
import com.vmware.safekeeping.external.type.options.VirtualBackupOptions;
import com.vmware.safekeeping.external.type.options.archive.ArchiveCheckGenerationsOptions;
import com.vmware.safekeeping.external.type.options.archive.ArchiveListOptions;
import com.vmware.safekeeping.external.type.options.archive.ArchiveRemoveGenerationsOptions;
import com.vmware.safekeeping.external.type.options.archive.ArchiveRemoveProfileOptions;
import com.vmware.safekeeping.external.type.options.archive.ArchiveShowOptions;
import com.vmware.safekeeping.external.type.options.archive.ArchiveStatusOptions;

@XmlSeeAlso({ ResultActionConnect.class, ResultActionDisconnect.class, ResultActionVmBackup.class,
		ResultActionIvdBackup.class, ResultActionVappBackup.class, ResultActionVmVirtualBackup.class,
		ResultActionIvdVirtualBackup.class, ResultActionVappVirtualBackup.class, ResultActionVmRestore.class,
		ResultActionVappRestore.class, ResultActionIvdRestore.class, ResultActionDiskBackup.class,
		ResultActionDiskRestore.class, ResultActionDiskVirtualBackup.class, ResultActionConnectVcenter.class,
		ResultActionDisconnectVcenter.class, ResultActionArchiveItemsList.class, ResultActionArchiveItem.class,
		ResultActionArchiveRemoveGenerationWithDependencies.class, ResultActionArchiveRemoveGeneration.class,
		ResultActionArchiveRemovedProfile.class, ResultActionArchiveVappStatus.class, ResultActionArchiveVmStatus.class,
		ResultActionArchiveIvdStatus.class, ResultActionArchiveCheckGenerationWithDependencies.class,
		ResultActionArchiveCheckGeneration.class, ResultActionArchiveShow.class, RestoreIvdManagedInfo.class,
		RestoreVappManagedInfo.class, RestoreVmManagedInfo.class, AbstractResultActionConnectRepository.class,
		ResultActionConnectAwsS3Repository.class, ResultActionConnectFileRepository.class })

@WebService(name = "Sapi", targetNamespace = "http://cxf.safekeeping.vmware.com/")
public interface Sapi {

	@WebMethod
	List<ResultActionBackup> backup(@WebParam(name = "options") BackupOptions options)
			throws UnrecognizedToken, InvalidTask;

	@WebMethod
	Tasks backupAsync(@WebParam(name = "options") BackupOptions options) throws UnrecognizedToken;

	@WebMethod
	Tasks checkArchiveGenerations(@WebParam(name = "options") ArchiveCheckGenerationsOptions options)
			throws UnrecognizedToken;

	@WebMethod
	ResultActionConnect connect() throws UnrecognizedToken;

	@WebMethod
	Task connectAsync() throws UnrecognizedToken;

	@WebMethod
	AbstractResultActionConnectRepository connectRepository(@WebParam(name = "options") RepositoryOptions options)
			throws UnrecognizedToken, InvalidTask;

	@WebMethod
	Task connectRepositoryAsync(@WebParam(name = "options") RepositoryOptions options)
			throws UnrecognizedToken, InvalidTask;

	@WebMethod
	ResultActionDisconnect disconnect() throws UnrecognizedToken;

	@WebMethod
	Task disconnectAsync() throws UnrecognizedToken;

	@WebMethod
	ResultActionDisconnectRepository disconnectRepository(@WebParam(name = "name") String name)
			throws UnrecognizedToken;

	@WebMethod
	Task disconnectRepositoryAsync(@WebParam(name = "name") String name) throws UnrecognizedToken;

	@WebMethod
	String echo(@WebParam(name = "extra") String extra);

	@WebMethod
	ResultActionExtension extension(@WebParam(name = "options") ExtensionOptions options)
			throws UnrecognizedToken, InternalServer;

	@WebMethod
	RepositoryOptions getActiveRepository() throws UnrecognizedToken;

	@WebMethod
	List<BlockInfo> getAllDumps(@WebParam(name = "id") Task id) throws UnrecognizedToken, InvalidTask;

	@WebMethod
	Calendar getCurrentTime() throws UnrecognizedToken;

	@WebMethod
	List<BlockInfo> getDumps(@WebParam(name = "id") Task id, @WebParam(name = "arg1") int start,
			@WebParam(name = "arg2") int end) throws UnrecognizedToken, InvalidTask;

	@WebMethod
	Set<RepositoryOptions> getRepositories() throws UnrecognizedToken;

	@WebMethod
	Set<String> getRepositoriesNames() throws UnrecognizedToken;

	@WebMethod
	RepositoryOptions getRepository(@WebParam(name = "name") String name) throws UnrecognizedToken;

	@WebMethod
	TaskResult getTaskInfo(@WebParam(name = "id") Task id) throws UnrecognizedToken, InvalidTask, InternalServer;

	@WebMethod
	ResultActionVersion getVersion() throws InternalCoreResult;

	@WebMethod
	boolean isConnected() throws UnrecognizedToken;

	@WebMethod
	GregorianCalendar keepalive() throws UnrecognizedToken;

	@WebMethod
	Task listArchive(@WebParam(name = "options") ArchiveListOptions options)
			throws UnrecognizedToken, InvalidOptions, InternalCoreResult;

	@WebMethod
	ResultActionConnectSso loginCsp(@WebParam(name = "options") CspConnectOptions options) throws InternalCoreResult;

	@WebMethod
	ResultActionConnectSso loginPsc(@WebParam(name = "options") PscConnectOptions options) throws InternalCoreResult;

	@WebMethod
	ResultActionDisconnectSso logout() throws UnrecognizedToken, InternalCoreResult;

	@WebMethod
	List<ResultActionArchiveRemoveGenerationWithDependencies> removeArchiveGenerations(
			@WebParam(name = "options") ArchiveRemoveGenerationsOptions options)
			throws UnrecognizedToken, InternalServer, InvalidTask;

	@WebMethod
	Tasks removeArchiveGenerationsAsync(@WebParam(name = "options") ArchiveRemoveGenerationsOptions options)
			throws UnrecognizedToken;

	@WebMethod
	List<ResultActionArchiveRemovedProfile> removeArchiveProfile(
			@WebParam(name = "options") ArchiveRemoveProfileOptions options)
			throws UnrecognizedToken, InternalServer, InvalidTask;

	@WebMethod
	Tasks removeArchiveProfileAsync(@WebParam(name = "options") ArchiveRemoveProfileOptions options)
			throws UnrecognizedToken;

	@WebMethod
	List<ResultActionRestore> restore(@WebParam(name = "options") RestoreOptions options)
			throws UnrecognizedToken, InvalidTask, InternalServer;

	@WebMethod
	Tasks restoreAsync(@WebParam(name = "options") RestoreOptions options) throws UnrecognizedToken;

	@WebMethod
	Task setActiveRepository(@WebParam(name = "name") String name) throws UnrecognizedToken;

	@WebMethod
	List<ResultActionArchiveShow> showArchive(@WebParam(name = "options") ArchiveShowOptions options)
			throws UnrecognizedToken, InternalServer, InvalidTask;

	@WebMethod
	Tasks showArchiveAsync(@WebParam(name = "options") ArchiveShowOptions options) throws UnrecognizedToken;

	@WebMethod
	List<ResultActionArchiveStatus> statusArchive(@WebParam(name = "options") ArchiveStatusOptions options)
			throws UnrecognizedToken, InternalServer, InvalidTask;

	@WebMethod
	Tasks statusArchiveAsync(@WebParam(name = "options") ArchiveStatusOptions options) throws UnrecognizedToken;

	@WebMethod
	List<AbstractResultActionVirtualBackup> virtualBackup(@WebParam(name = "options") VirtualBackupOptions options)
			throws UnrecognizedToken, InvalidTask;

	@WebMethod
	Tasks virtualBackupAsync(@WebParam(name = "options") VirtualBackupOptions options) throws UnrecognizedToken;

}
