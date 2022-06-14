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
package com.vmware.safekeeping.cxf.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.math.IntRange;

import com.vmware.safekeeping.common.FirstClassObjectFilterType;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.cxf.test.common.ConsoleWrapper;
import com.vmware.safekeeping.cxf.test.common.FormatMismatch;
import com.vmware.safekeeping.cxf.test.common.Menu;
import com.vmware.safekeeping.cxf.test.common.TestUtility;
import com.vmware.safekeeping.cxf.test.common.TrustAllTrustManager;
import com.vmware.sapi.ArchiveCheckGenerationsOptions;
import com.vmware.sapi.ArchiveListOptions;
import com.vmware.sapi.ArchiveObjects;
import com.vmware.sapi.ArchiveRemoveGenerationsOptions;
import com.vmware.sapi.ArchiveRemoveProfileOptions;
import com.vmware.sapi.ArchiveShowOptions;
import com.vmware.sapi.ArchiveStatusOptions;
import com.vmware.sapi.AwsS3RepositoryOptions;
import com.vmware.sapi.BackupMode;
import com.vmware.sapi.BackupOptions;
import com.vmware.sapi.CspConnectOptions;
import com.vmware.sapi.ExtensionManagerOperation;
import com.vmware.sapi.ExtensionOptions;
import com.vmware.sapi.FcoTarget;
import com.vmware.sapi.FcoTypeSearch;
import com.vmware.sapi.FileRepositoryOptions;
import com.vmware.sapi.InternalCoreResult_Exception;
import com.vmware.sapi.InternalServer_Exception;
import com.vmware.sapi.InvalidOptions_Exception;
import com.vmware.sapi.InvalidTask_Exception;
import com.vmware.sapi.OperationState;
import com.vmware.sapi.PscConnectOptions;
import com.vmware.sapi.RestoreOptions;
import com.vmware.sapi.RestoreVmdkOption;
import com.vmware.sapi.ResultAction;
import com.vmware.sapi.ResultActionConnect;
import com.vmware.sapi.ResultActionConnectAwsS3Repository;
import com.vmware.sapi.ResultActionConnectFileRepository;
import com.vmware.sapi.ResultActionConnectSso;
import com.vmware.sapi.ResultActionConnectVcenter;
import com.vmware.sapi.ResultActionDisconnect;
import com.vmware.sapi.ResultActionDisconnectRepository;
import com.vmware.sapi.ResultActionDisconnectSso;
import com.vmware.sapi.ResultActionDisconnectVcenter;
import com.vmware.sapi.ResultActionExtension;
import com.vmware.sapi.ResultActionVersion;
import com.vmware.sapi.Sapi;
import com.vmware.sapi.SapiService;
import com.vmware.sapi.SearchManagementEntityInfoType;
import com.vmware.sapi.Task;
import com.vmware.sapi.TaskResult;
import com.vmware.sapi.UnrecognizedToken_Exception;
import com.vmware.sapi.VirtualBackupOptions;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public final class App {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(App.class.getName());

	public static final int DEFAULT_NUMBER_OF_THREADS = 10;

	public static final int DEFAULT_BLOCK_SIZE = 20 * 1024 * 1024;
	public static final int MAX_ALLOWED_BLOCK_SIZE = DEFAULT_BLOCK_SIZE * 4;

	private static final int MAX_NUMBER_OF_THREADS = 100;
	private static final int MAX_NUMBER_OF_GENERATIONS = 50;

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) {
		final App test = new App();
		try {
			if (test.parse(args)) {
				test.run();
			}
		} catch (IOException | InternalServer_Exception e) {
			Utility.logWarning(logger, e);
		}
	}

	private Sapi sapi;
	private boolean debug;

	private boolean keepSessionOpen;
	private byte[] password;

	private String user;

	private String server;
	private String sapiServer;

	private AwsS3RepositoryOptions s3TargetOptions;
	private FileRepositoryOptions fileTargetOptions;

	private App() {
		TrustAllTrustManager.trustAllHttpsCertificates();

	}

	private OptionParser configureParser() {
		final OptionParser parser = new OptionParser();
		parser.accepts("help");
		parser.accepts("debug", "Debug mode");
		parser.accepts("keep", "keep the session open (no logout)").availableIf("debug");
		parser.accepts("sapi").withRequiredArg().ofType(String.class).describedAs("Sapi server endpoint URL")
				.defaultsTo("https://localhost:8443/sdk");
		parser.accepts("server").requiredUnless("help").withRequiredArg().ofType(String.class)
				.describedAs("Authentication server URL or FQDN");
		parser.accepts("user").requiredUnless("help").withRequiredArg().ofType(String.class).describedAs("user");
		parser.accepts("password").requiredUnless("help").withRequiredArg().ofType(String.class)
				.describedAs("password");
		parser.accepts("filePath", "Repository Directory").withRequiredArg().ofType(String.class);
		parser.accepts("region").requiredUnless("help", "filePath").withRequiredArg().ofType(String.class)
				.describedAs("AWS region");
		parser.accepts("backet").requiredUnless("help", "filePath").withRequiredArg().ofType(String.class)
				.describedAs("S3 backet");
		parser.accepts("accessKey").requiredUnless("help", "filePath").withRequiredArg().ofType(String.class)
				.describedAs("AWS AccessKey");
		parser.accepts("secretKey").requiredIf("accessKey").withRequiredArg().ofType(String.class)
				.describedAs("AWS SecretKey");
		parser.accepts("base64", "AWS SecretKey in base64").availableIf("secretKey");
		parser.accepts("base64", "AWS SecretKey in base64").availableIf("secretKey");

		return parser;

	}

	private ResultActionConnect connect()
			throws UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
		ResultActionConnect rac;
		ConsoleWrapper.console.printf("Connecting to vCenter(s) ");
		final Task task = this.sapi.connectAsync();

		rac = (ResultActionConnect) TestUtility.waitForTask(this.sapi, task, true).getResult();

		final List<ResultActionConnectVcenter> vims = new LinkedList<>();
		for (final Task s : rac.getSubTasksActionConnectVCenters()) {
			final TaskResult r = this.sapi.getTaskInfo(s);
			vims.add((ResultActionConnectVcenter) r.getResult());
		}
		ConsoleWrapper.console.printf("LookupService reports %d VimService Instance %s%n", vims.size(),
				vims.size() > 1 ? "s" : "");
		int index = 0;
		for (final ResultActionConnectVcenter vim : vims) {
			++index;
			ConsoleWrapper.console.printf("vCenter %d:%n", index);
			ConsoleWrapper.console.printf("\tVimService uuid: %s url: %s%n", vim.getInstanceUuid(), vim.getUrl());
			ConsoleWrapper.console.printf("\t\t%s - Api Version: %s - Platform: %s%n", vim.getName(), vim.getApi(),
					TestUtility.cloudPlatformToString(vim.getCloudPlatform()));
			ConsoleWrapper.console.printf("\tStorage Profile Service Ver.%s url: %s%n", vim.getPbmVersion(),
					vim.getPbmUrl());
			ConsoleWrapper.console.printf("\t%s url: %s%n", vim.getVslmName(), vim.getVslmUrl());
			ConsoleWrapper.console.printf("\tVapi Service url: %s%n", vim.getVapiUrl());
		}
		return rac;

	}

	private boolean connectS3Target()
			throws UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
		boolean result = false;
		ConsoleWrapper.console.printf("Connecting to Target: %s:%s", this.s3TargetOptions.getRegion(),
				this.s3TargetOptions.getBacket());
		final Task conTargetTask = this.sapi.connectRepositoryAsync(this.s3TargetOptions);

		final ResultActionConnectAwsS3Repository racAwsS3 = (ResultActionConnectAwsS3Repository) TestUtility
				.waitForTask(this.sapi, conTargetTask, true).getResult();
		if (racAwsS3.getState() == OperationState.SUCCESS) {
			result = true;
		} else {
			ConsoleWrapper.console.printf("Failed to Connected to Target: %s:%s%nReason:%s%n",
					this.s3TargetOptions.getRegion(), this.s3TargetOptions.getBacket(), racAwsS3.getReason());
		}
		return result;
	}

	private boolean connectFileTarget()
			throws UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
		boolean result = false;
		ConsoleWrapper.console.printf("Connecting to Target: %s", this.fileTargetOptions.getRoot());
		final Task conTargetTask = this.sapi.connectRepositoryAsync(this.fileTargetOptions);

		final ResultActionConnectFileRepository racAwsS3 = (ResultActionConnectFileRepository) TestUtility
				.waitForTask(this.sapi, conTargetTask, true).getResult();
		if (racAwsS3.getState() == OperationState.SUCCESS) {
			result = true;
		} else {
			ConsoleWrapper.console.printf("Failed to Connected to Target: %s %n", this.fileTargetOptions.getName());
		}
		return result;
	}

	private ResultActionDisconnect disconnect()
			throws UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
		final Task taskDisconnect = this.sapi.disconnectAsync();
		ConsoleWrapper.console.printf("Disconnecting vCenter taskId:%n" + taskDisconnect.getId());

		final ResultActionDisconnect rad = (ResultActionDisconnect) TestUtility
				.waitForTask(this.sapi, taskDisconnect, true).getResult();

		final List<ResultActionDisconnectVcenter> radv = new LinkedList<>();
		for (final Task s : rad.getSubTasksActionConnectVCenters()) {
			final TaskResult r = this.sapi.getTaskInfo(s);
			radv.add((ResultActionDisconnectVcenter) r.getResult());
		}
		ConsoleWrapper.console.printf("%d VimService Instance %s connected", radv.size(), radv.size() > 1 ? "s" : "");
		int vcIndex = 0;
		for (final ResultActionDisconnectVcenter vim : radv) {
			++vcIndex;
			ConsoleWrapper.console.printf("vCenter %d:%n", vcIndex);
			ConsoleWrapper.console.printf("\tVimService uuid: %s url: %s - Disconnected%n", vim.getInstanceUuid(),
					vim.getUrl());
			ConsoleWrapper.console.printf("\tStorage Profile Service Ver.%s url: %s - Disconnected%n",
					vim.getPbmVersion(), vim.getPbmUrl());
			ConsoleWrapper.console.printf("\t%s url: %s - Disconnected%n", vim.getVslmName(), vim.getVslmUrl());
			ConsoleWrapper.console.printf("\tVapi Service url: %s - Disconnected%n", vim.getVapiUrl());
		}
		return rad;
	}

	private boolean disconnectTargets() throws UnrecognizedToken_Exception {
		boolean result = true;
		final List<String> targetList = this.sapi.getRepositoriesNames();
		for (final String targetName : targetList) {
			final ResultActionDisconnectRepository t = this.sapi.disconnectRepository(targetName);
			result &= t.getState() == OperationState.SUCCESS;
		}
		return result;
	}

	private ResultActionVersion getVersion() throws InternalCoreResult_Exception {
		final ResultActionVersion version = this.sapi.getVersion();
		ConsoleWrapper.console.printf("%s%n%s%n%s%n%s%n", this.sapi.echo("Max"), version.getExtendedVersion(),
				version.getServerInfo().getExtendedVersion(), version.getJavaRuntime());
		return version;
	}

	private ResultActionConnectSso login(final boolean csp) throws InternalCoreResult_Exception {
		ResultActionConnectSso loginResult = null;
		if (csp) {
			final CspConnectOptions cspOptions = new CspConnectOptions();
			cspOptions.setBase64(false);
			cspOptions.setAuthServer(
					"https://ConsoleWrapper.console.cloud.vmware.com/csp/gateway/am/api/auth/api-tokens/authorize");
			// cspOptions.setAuthServer("https://ConsoleWrapper.console.cloud-us-gov.vmware.com/csp/gateway/am/api/auth/api-tokens/authorize");
			cspOptions.setRefreshToken("Yk0957HMA3deUWHCKCXLP7PuQ1xE7F7liaC8H4ykZulsEguLlrKNEqadquYTfheK");
			cspOptions.setTokenExchangeServer("https://vcenter.sddc-44-233-221-247.vmwarevmc.com/");

			loginResult = this.sapi.login(cspOptions);
		} else {
			final PscConnectOptions pscOptions = new PscConnectOptions();
			pscOptions.setBase64(false);
			pscOptions.setAuthServer(this.server);
			pscOptions.setUser(this.user);
			pscOptions.setPassword(new String(this.password));
			loginResult = this.sapi.login(pscOptions);
		}
		return loginResult;
	}

	/**
	 * @return
	 * @throws UnrecognizedToken_Exception
	 * @throws InternalCoreResult_Exception
	 *
	 */
	private ResultActionDisconnectSso logout() throws UnrecognizedToken_Exception, InternalCoreResult_Exception {
		final ResultActionDisconnectSso rads = this.sapi.logout();

		ConsoleWrapper.console.printf("LookupService disconnected%n");
		ConsoleWrapper.console.printf("PSC %s disconnected%n", rads.getSsoEndPointUrl());
		return rads;

	}

	private void menuTest() {
		final Menu menu = new Menu("Test Menu");

		menu.addOption("Backup Fco", () -> {
			try {
				testBackupFco();
			} catch (FormatMismatch | UnrecognizedToken_Exception | InvalidTask_Exception
					| InternalServer_Exception e) {
				Utility.logWarning(logger, e);
			} catch (final InterruptedException e) {
				// Restore interrupted state...
				Thread.currentThread().interrupt();
			}
			menu.start();
		});
		menu.addOption("Consolidate Fco", () -> {
			try {
				testConsolidateFco();
			} catch (FormatMismatch | UnrecognizedToken_Exception | InvalidTask_Exception
					| InternalServer_Exception e) {
				Utility.logWarning(logger, e);
			} catch (final InterruptedException e) {
				// Restore interrupted state...
				Thread.currentThread().interrupt();
			}
			menu.start();
		});
		menu.addOption("Restore Fco", () -> {
			try {
				testRestoreFco();
			} catch (FormatMismatch | UnrecognizedToken_Exception | InvalidTask_Exception
					| InternalServer_Exception e) {
				Utility.logWarning(logger, e);
			} catch (final InterruptedException e) {
				// Restore interrupted state...
				Thread.currentThread().interrupt();
			}

			menu.start();
		});
		menu.addOption("Check Fco Archive", () -> {
			try {
				testCheckArchive();
			} catch (FormatMismatch | UnrecognizedToken_Exception | InvalidTask_Exception
					| InternalServer_Exception e) {
				Utility.logWarning(logger, e);
			} catch (final InterruptedException e) {
				// Restore interrupted state...
				Thread.currentThread().interrupt();
			}
			menu.start();
		});
		menu.addOption("List Fco Archive", () -> {
			try {
				testListArchive();
			} catch (FormatMismatch | UnrecognizedToken_Exception | InvalidTask_Exception | NumberFormatException
					| InternalCoreResult_Exception | InvalidOptions_Exception | InternalServer_Exception e) {
				Utility.logWarning(logger, e);
			}
			menu.start();
		});
		menu.addOption("Archive State", () -> {
			try {
				testStatusArchive();
			} catch (FormatMismatch | UnrecognizedToken_Exception | InvalidTask_Exception | NumberFormatException
					| InternalServer_Exception e) {
				Utility.logWarning(logger, e);
			} catch (final InterruptedException e) {
				// Restore interrupted state...
				Thread.currentThread().interrupt();
			}
			menu.start();
		});
		menu.addOption("Show Archive", () -> {
			try {
				testShowArchive();
			} catch (FormatMismatch | UnrecognizedToken_Exception | InvalidTask_Exception | NumberFormatException
					| InternalServer_Exception e) {
				Utility.logWarning(logger, e);
			} catch (final InterruptedException e) {
				// Restore interrupted state...
				Thread.currentThread().interrupt();
			}
			menu.start();
		});
		menu.addOption("Remove Generation from Archive", () -> {
			try {
				testRemoveArchiveGenerations();
			} catch (FormatMismatch | UnrecognizedToken_Exception | InvalidTask_Exception | NumberFormatException
					| InternalServer_Exception e) {
				Utility.logWarning(logger, e);
			} catch (final InterruptedException e) {
				// Restore interrupted state...
				Thread.currentThread().interrupt();
			}
			menu.start();
		});
		menu.addOption("Remove Profile from Archive", () -> {
			try {
				testRemoveArchiveProfile();
			} catch (FormatMismatch | UnrecognizedToken_Exception | InvalidTask_Exception | NumberFormatException
					| InternalServer_Exception e) {
				Utility.logWarning(logger, e);
			} catch (final InterruptedException e) {
				// Restore interrupted state...
				Thread.currentThread().interrupt();
			}
			menu.start();
		});
		menu.addOption("Manage Extension", () -> {
			try {
				testExtension();
			} catch (UnrecognizedToken_Exception | InternalServer_Exception e) {
				Utility.logWarning(logger, e);
			}
			menu.start();
		});
		menu.addOption("Old tests", () -> {
			boolean exit = false;
			try {
				final LegacyTests legacyTests = new LegacyTests(this.sapi);
				exit = legacyTests.execute();
			} catch (FormatMismatch | UnrecognizedToken_Exception | InvalidTask_Exception | InvalidOptions_Exception
					| InternalCoreResult_Exception | NumberFormatException | InternalServer_Exception e) {
				Utility.logWarning(logger, e);
			} catch (final InterruptedException e) {
				// Restore interrupted state...
				Thread.currentThread().interrupt();
			}
			if (exit) {
				ConsoleWrapper.console.println("Exiting");
			} else {
				menu.start();
			}
		});

		menu.addOption("Exit", () -> ConsoleWrapper.console.println("Exiting"));
		menu.start();
	}

	public boolean parse(final String[] args) throws IOException {
		final OptionParser parser = configureParser();

		final OptionSet options = parser.parse(args);
		if (options.has("help")) {
			parser.printHelpOn(ConsoleWrapper.console.getWriter());
			return false;
		}
		this.debug = options.has("debug");
		this.keepSessionOpen = options.has("keep");
		this.sapiServer = options.valueOf("sapi").toString();
		if (!this.sapiServer.startsWith("http")) {
			this.sapiServer = String.format("https://%s:8443/sdk", this.sapiServer);
		}

		this.server = options.valueOf("server").toString();
		this.user = options.valueOf("user").toString();
		this.password = options.valueOf("password").toString().getBytes();
		if (options.has("filePath")) {

			this.fileTargetOptions = new FileRepositoryOptions();
			this.fileTargetOptions.setRoot(options.valueOf("filePath").toString());
			this.fileTargetOptions.setActive(true);
			this.fileTargetOptions.setName("filePath");
		} else {
			this.s3TargetOptions = new AwsS3RepositoryOptions();
			this.s3TargetOptions.setRegion(options.valueOf("region").toString());
			this.s3TargetOptions.setBacket(options.valueOf("backet").toString());
			this.s3TargetOptions.setAccessKey(options.valueOf("accessKey").toString());
			this.s3TargetOptions.setSecretKey(options.valueOf("secretKey").toString());
			this.s3TargetOptions.setBase64(options.has("base64"));
			this.s3TargetOptions
					.setName(WordUtils.capitalize(this.s3TargetOptions.getRegion() + this.s3TargetOptions.getBacket()));
			this.fileTargetOptions.setName("S3");
			this.s3TargetOptions.setActive(true);
		}
		return true;
	}

	private void run() throws InternalServer_Exception {

		final SapiService sapiService = new SapiService();
		this.sapi = sapiService.getSapiPort();
		((BindingProvider) this.sapi).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				this.sapiServer);

		final boolean csp = false;
		boolean connected = false;
		try {
			getVersion();
			if (this.debug) {
				final String key = this.sapi.echo("cheat");
				if ((key != null) && !key.startsWith("Hello world from")) {
					setHeader(key);
					connected = (this.sapi.isConnected());
				}
			}
			if (!connected) {
				final ResultActionConnectSso loginResult = login(csp);
				if (loginResult.isConnected()) {
					setHeader(loginResult.getToken());
					ConsoleWrapper.console.printf("Connected to Platform Service Controller: %s%n",
							loginResult.getSsoEndPointUrl());
					if (fileTargetOptions == null) {
						if (connectS3Target()) {
							connect();
						}
					} else {
						if (connectFileTarget()) {
							connect();
						}
					}
				} else {
					ConsoleWrapper.console.printf("Failed to Connected to Platform Service Controller: %s%nReason:%s%n",
							loginResult.getSsoEndPointUrl(), loginResult.getReason());
				}
			}
			ConsoleWrapper.console.printf("Server Time:%s\n", this.sapi.getCurrentTime().toString());
			menuTest();
			if (!this.keepSessionOpen) {
				disconnect();
				disconnectTargets();
				logout();
			}

		} catch (final UnrecognizedToken_Exception | InternalCoreResult_Exception | InvalidTask_Exception
				| NumberFormatException e) {
			ConsoleWrapper.console.printf("Server at url %s error:%s", this.sapiServer, e.getMessage());
			Utility.logWarning(logger, e);
		}

		ConsoleWrapper.console.println("Goodbye");
	}

	private void setHeader(final String token) {
		/******************* UserName & Password ******************************/
		final Map<String, Object> reqCtx = ((BindingProvider) this.sapi).getRequestContext();

		final Map<String, List<String>> headers = new HashMap<>();

		headers.put("Auth-token", Collections.singletonList(token));
		reqCtx.put(MessageContext.HTTP_REQUEST_HEADERS,
				headers);/**********************************************************************/
	}

	private List<ResultAction> testBackupFco() throws UnrecognizedToken_Exception, InvalidTask_Exception,
			InterruptedException, FormatMismatch, InternalServer_Exception {

		final BackupOptions options = new BackupOptions();
		Integer numberOfGenerations;
		final List<ResultAction> result = new LinkedList<>();
		ConsoleWrapper.console.printf("%n");
		options.getTargetList().addAll(ConsoleWrapper.console.readFcoString("type:name (ex vm:max_possa)"));

		ConsoleWrapper.console.printf("%n");
		numberOfGenerations = ConsoleWrapper.console.readInt("Number of generation to create:", 1,
				new IntRange(1, MAX_NUMBER_OF_GENERATIONS));

		options.setRequestedTransportMode(ConsoleWrapper.console.readTransportMode("Transport Mode", null));
		final BackupMode mode = ConsoleWrapper.console.readBackupMode("Backup Mode", "unknow");
		options.setRequestedBackupMode(mode);
		options.setCompression(ConsoleWrapper.console.readBoolean("Compression", true));
		options.setCipher(ConsoleWrapper.console.readBoolean("Cipher", false));
		options.setMaxBlockSize(ConsoleWrapper.console.readInt("Block size", App.DEFAULT_BLOCK_SIZE,
				new IntRange(Utility.FOUR_KBYTES, MAX_ALLOWED_BLOCK_SIZE)));
		options.setNumberOfThreads(ConsoleWrapper.console.readInt("Number of Thread", DEFAULT_NUMBER_OF_THREADS,
				new IntRange(1, MAX_NUMBER_OF_THREADS)));

		options.setForce(false);
		final boolean showDisksTasks = ConsoleWrapper.console.readBoolean("Show Disks tasks", true);
		final boolean showDisksDetails = ConsoleWrapper.console.readBoolean("Show Disks details", true);

		final boolean async = ConsoleWrapper.console.readBoolean("Async SOAP call", true);

		for (Integer i = 0; i < numberOfGenerations; i++) {

			final BackupTest backupTest = new BackupTest(this.sapi);
			if (async) {
				result.addAll(backupTest.executeAsync(options, showDisksTasks, showDisksDetails));
			} else {
				result.addAll(backupTest.execute(options, showDisksTasks, showDisksDetails));
			}

		}
		ConsoleWrapper.console.println("End Backup.");
		return result;
	}

	private void testCheckArchive() throws InterruptedException, InvalidTask_Exception, UnrecognizedToken_Exception,
			FormatMismatch, InternalServer_Exception {
		final ArchiveCheckGenerationsOptions options = new ArchiveCheckGenerationsOptions();
		ConsoleWrapper.console.printf("%n");
		ConsoleWrapper.console.printf("Check Archive custom fco%n");
		options.getTargetList().addAll(ConsoleWrapper.console.readFcoString("type:name (ex vm:max_possa)", true));
		options.getGenerationId().addAll(ConsoleWrapper.console
				.readGenerations("Generations to check (comma separated.-1 for the last one", -1));
		ConsoleWrapper.console.printf("%n");

		final ArchiveTest archiveTest = new ArchiveTest(this.sapi);
		archiveTest.execute(options);

	}

	private List<ResultAction> testConsolidateFco() throws UnrecognizedToken_Exception, InvalidTask_Exception,
			InterruptedException, FormatMismatch, InternalServer_Exception {
		final VirtualBackupOptions options = new VirtualBackupOptions();

		ConsoleWrapper.console.printf("%n");
		options.getTargetList().addAll(ConsoleWrapper.console.readFcoString("type:name (ex vm:max_possa)"));

		options.setGenerationId(ConsoleWrapper.console.readInt("Generation Id to consolidate (-1 last):", -1, null));
		options.setNumberOfThreads(ConsoleWrapper.console.readInt("Number of Thread", DEFAULT_NUMBER_OF_THREADS,
				new IntRange(1, MAX_NUMBER_OF_THREADS)));

		options.setForce(false);
		final boolean showDisksTasks = ConsoleWrapper.console.readBoolean("Show Disks tasks", true);
		final boolean showDisksDetails = ConsoleWrapper.console.readBoolean("Show Disks details", true);

		final boolean async = ConsoleWrapper.console.readBoolean("Async SOAP call", true);

		final VirtualBackupTest consolidateTest = new VirtualBackupTest(this.sapi);
		final List<ResultAction> result = new ArrayList<>();
		if (async) {
			result.addAll(consolidateTest.executeAsync(options, showDisksTasks, showDisksDetails));
		} else {
			result.addAll(consolidateTest.execute(options, showDisksTasks, showDisksDetails));
		}
		ConsoleWrapper.console.println("End Consolidation.");
		return result;
	}

	private ResultAction testExtension() throws UnrecognizedToken_Exception, InternalServer_Exception {
		final ExtensionOptions options = new ExtensionOptions();
		ConsoleWrapper.console.printf("%n");
		ConsoleWrapper.console.printf("Manage Extension%n");
		options.setExtensionOperation(
				ConsoleWrapper.console.readExtensionOperation("Operation", ExtensionManagerOperation.CHECK));
		options.setForce(ConsoleWrapper.console.readBoolean("Force the operation", false));
		ConsoleWrapper.console.printf("%n");

		final ResultActionExtension resultAction = this.sapi.extension(options);
		if (resultAction.getState() == OperationState.SUCCESS) {
			ConsoleWrapper.console.printf("Extension ver:%s  - Health Information Available: %s",
					resultAction.getVersion(), resultAction.isHealthInfo() ? "Yes" : "No");
			ConsoleWrapper.console.printf("%n");
			ConsoleWrapper.console.printf("Extension operation %s success", options.getExtensionOperation().toString());

		} else {
			ConsoleWrapper.console.printf("Failed operation Reason:%s%n", resultAction.getReason());
		}
		ConsoleWrapper.console.printf("%n");
		return resultAction;
	}

	private void testListArchive() throws InvalidTask_Exception, UnrecognizedToken_Exception, FormatMismatch,
			InternalCoreResult_Exception, InvalidOptions_Exception, InternalServer_Exception {
		final ArchiveListOptions options = new ArchiveListOptions();
		ConsoleWrapper.console.printf("%n");
		ConsoleWrapper.console.printf("List Archive%n");
		options.getTargetList().addAll(ConsoleWrapper.console.readFcoString("type:name (ex vm:max_possa)", true));

		options.setAnyFcoOfType(FirstClassObjectFilterType.all | FirstClassObjectFilterType.any);
		ConsoleWrapper.console.printf("%n");

		final ArchiveTest archiveTest = new ArchiveTest(this.sapi);
		archiveTest.execute(options);
		ConsoleWrapper.console.printf("%n");
	}

	private Collection<? extends ResultAction> testRemoveArchiveGenerations() throws InterruptedException,
			InvalidTask_Exception, UnrecognizedToken_Exception, FormatMismatch, InternalServer_Exception {
		final ArchiveRemoveGenerationsOptions options = new ArchiveRemoveGenerationsOptions();
		ConsoleWrapper.console.printf("%n");
		ConsoleWrapper.console.printf("Check Archive custom fco%n");
		options.getTargetList().addAll(ConsoleWrapper.console.readFcoString("type:name (ex vm:max_possa)", true));
		options.getGenerationId().addAll(ConsoleWrapper.console
				.readGenerations("Generations to remove (comma separated.-1 for the last one", -1));
		final boolean async = ConsoleWrapper.console.readBoolean("Async SOAP call", true);

		ConsoleWrapper.console.printf("%n");

		final ArchiveTest archiveTest = new ArchiveTest(this.sapi);

		final List<ResultAction> result = new LinkedList<>();

		if (async) {
			result.addAll(archiveTest.executeAsync(options));
		} else {
			result.addAll(archiveTest.execute(options));
		}
		return result;

	}

	private Collection<? extends ResultAction> testRemoveArchiveProfile() throws InterruptedException,
			InvalidTask_Exception, UnrecognizedToken_Exception, FormatMismatch, InternalServer_Exception {
		final List<ResultAction> result = new LinkedList<>();
		final ArchiveRemoveProfileOptions options = new ArchiveRemoveProfileOptions();
		ConsoleWrapper.console.printf("%n");
		ConsoleWrapper.console.printf("Remove Archive profile custom fco%n");
		options.getTargetList().addAll(ConsoleWrapper.console.readFcoString("type:name (ex vm:max_possa)", true));
		final boolean async = ConsoleWrapper.console.readBoolean("Async SOAP call", true);

		ConsoleWrapper.console.printf("%n");

		final ArchiveTest archiveTest = new ArchiveTest(this.sapi);

		if (async) {
			result.addAll(archiveTest.executeAsync(options));
		} else {
			result.addAll(archiveTest.execute(options));
		}
		return result;

	}

	/**
	 * restore
	 *
	 * @param numberOfGenerations
	 * @param remove              remove profile
	 * @return
	 * @throws InterruptedException
	 * @throws InvalidTask_Exception
	 * @throws UnrecognizedToken_Exception
	 * @throws FormatMismatch
	 * @throws InternalServer_Exception
	 *
	 * @throws Exception
	 */
	private Collection<? extends ResultAction> testRestoreFco() throws UnrecognizedToken_Exception,
			InvalidTask_Exception, InterruptedException, FormatMismatch, InternalServer_Exception {
		final RestoreOptions options = new RestoreOptions();
		ConsoleWrapper.console.printf("%n");
		ConsoleWrapper.console.printf("Restore custom fco%n");

		options.getTargetList().addAll(ConsoleWrapper.console.readFcoString("type:name (ex vm:max_possa)"));
		ConsoleWrapper.console.printf("%n");
		options.setGenerationId(ConsoleWrapper.console.readInt("Generation Id to restore (-1 last):", -1, null));
		options.setRequestedTransportMode(ConsoleWrapper.console.readTransportMode("Transport Mode", "nbdssl:nbd"));

		options.setPowerOn(ConsoleWrapper.console.readBoolean("PowerOn after restore", false));

		options.setRecover(ConsoleWrapper.console.readBoolean("Recovery Mode", false));
		if (!options.isRecover()) {
			final String name = ConsoleWrapper.console.readString("Name");
			if (StringUtils.isNotBlank(name)) {
				options.setName(name);
			} else {
				options.setPostfix(ConsoleWrapper.console.readString("Name Postfix "));
				options.setPrefix(ConsoleWrapper.console.readString("Name Prefix "));
			}

			final String resourecPoolStr = ConsoleWrapper.console.readString("Destination Resource Pool");
			if (StringUtils.isNotBlank(resourecPoolStr)) {
				options.setResourcePool(
						TestUtility.newSearchManagementEntity(SearchManagementEntityInfoType.NAME, resourecPoolStr));
			}
			final String folderStr = ConsoleWrapper.console.readString("Destination folder");
			if (StringUtils.isNotBlank(folderStr)) {
				options.setFolder(
						TestUtility.newSearchManagementEntity(SearchManagementEntityInfoType.NAME, folderStr));
			}
			final String datacenterStr = ConsoleWrapper.console.readString("Destination Datacenter");
			if (StringUtils.isNotBlank(folderStr)) {
				options.setDatacenter(
						TestUtility.newSearchManagementEntity(SearchManagementEntityInfoType.NAME, datacenterStr));
			}
			final String datastoreStr = ConsoleWrapper.console.readString("Destination Datastore");
			if (StringUtils.isNotBlank(datastoreStr)) {
				options.setDatastore(
						TestUtility.newSearchManagementEntity(SearchManagementEntityInfoType.NAME, datastoreStr));
			}
			final String hostStr = ConsoleWrapper.console.readString("Destination Host");
			if (StringUtils.isNotBlank(hostStr)) {
				options.setHost(TestUtility.newSearchManagementEntity(SearchManagementEntityInfoType.NAME, hostStr));
			}
			for (final FcoTarget t : options.getTargetList()) {
				if ((t.getKeyType() == FcoTypeSearch.VAPP_MOREF) || (t.getKeyType() == FcoTypeSearch.VAPP_UUID)
						|| (t.getKeyType() == FcoTypeSearch.VAPP_NAME)) {
					options.setAllowDuplicatedVmNamesInsideVapp(
							ConsoleWrapper.console.readBoolean("Allow Duplicated VM names", true));
					break;
				}
			}
			final String vmdkDatastoreStr = ConsoleWrapper.console.readString(
					"Destination VMDK Datastores (comma separated ex: datastoreVMDK0,datastoreVMDK1,,datastoreVMDK3");
			if (StringUtils.isNotBlank(vmdkDatastoreStr)) {
				final String[] vmdkDatastores = vmdkDatastoreStr.split(",");
				int diskId = 0;
				for (final String data : vmdkDatastores) {
					RestoreVmdkOption diskOption;
					if (options.getDisks().size() > diskId) {
						diskOption = options.getDisks().get(diskId);
					} else {
						diskOption = new RestoreVmdkOption();
						options.getDisks().add(diskOption);
					}
					if (StringUtils.isEmpty(data)) {
						diskOption.setDatastore(null);
					} else {
						diskOption.setDatastore(
								TestUtility.newSearchManagementEntity(SearchManagementEntityInfoType.NAME, data));
					}
					++diskId;
				}
			}

			final String vmdkStorageProfileStr = ConsoleWrapper.console
					.readString("VMDK Storage Profile  (comma separated ex: profileVMDK0,profileVMDK1,,profileVMDK3");
			if (StringUtils.isNotBlank(vmdkStorageProfileStr)) {
				final String[] vmdkStorageProfiles = vmdkStorageProfileStr.split(",");
				int diskId = 0;
				for (final String data : vmdkStorageProfiles) {
					RestoreVmdkOption diskOption;
					if (options.getDisks().size() > diskId) {
						diskOption = options.getDisks().get(diskId);
					} else {
						diskOption = new RestoreVmdkOption();
						options.getDisks().add(diskOption);
					}
					if (StringUtils.isEmpty(data)) {
						diskOption.setSpbmProfile(null);
					} else {
						diskOption.setSpbmProfile(
								TestUtility.newSearchManagementEntity(SearchManagementEntityInfoType.NAME, data));
					}
					++diskId;
				}
			}

			final String vmNetworkStr = ConsoleWrapper.console
					.readString("Destination Networks (comma separated ex: networkNIC0,networkNIC1,,networkNIC3");
			if (StringUtils.isNotBlank(vmNetworkStr)) {
				final String[] vmNets = vmNetworkStr.split(",");
				for (final String data : vmNets) {
					if (StringUtils.isEmpty(data)) {
						options.getNetworks().add(null);
					} else {
						options.getNetworks()
								.add(TestUtility.newSearchManagementEntity(SearchManagementEntityInfoType.NAME, data));
					}
				}
			}

		}
		options.setNumberOfThreads(ConsoleWrapper.console.readInt("Number of Thread", DEFAULT_NUMBER_OF_THREADS,
				new IntRange(1, MAX_NUMBER_OF_THREADS)));
		options.setOverwrite(ConsoleWrapper.console.readBoolean("Overwrite if exist", false));
		if (options.isOverwrite()) {
			options.setForce(ConsoleWrapper.console.readBoolean("Overwrite if exist and powered on", false));
		}

		options.setImportVmxFile(ConsoleWrapper.console.readBoolean("Import Vmx as create VM", false));
		final boolean showDisksTasks = ConsoleWrapper.console.readBoolean("Show Disks tasks", true);
		final boolean showDisksDetails = ConsoleWrapper.console.readBoolean("Show Disks details", true);
		final boolean async = ConsoleWrapper.console.readBoolean("Async SOAP call", true);
		// start restore
		final RestoreTest test = new RestoreTest(this.sapi);
		final List<ResultAction> result = new ArrayList<>();

		if (async) {
			result.addAll(test.executeAsync(options, showDisksTasks, showDisksDetails, true));
		} else {
			result.addAll(test.execute(options, showDisksTasks, showDisksDetails, true));
		}
		ConsoleWrapper.console.printf("End Restore.%n");
		return result;
	}

	private Collection<? extends ResultAction> testShowArchive() throws InterruptedException, InvalidTask_Exception,
			UnrecognizedToken_Exception, FormatMismatch, InternalServer_Exception {
		final ArchiveShowOptions options = new ArchiveShowOptions();
		ConsoleWrapper.console.printf("%n");
		ConsoleWrapper.console.printf("Show Archive custom fco%n");
		options.getTargetList().addAll(ConsoleWrapper.console.readFcoString("type:name (ex vm:max_possa)"));

		options.setGenerationId(ConsoleWrapper.console.readInt("Generation Id to restore (-1 last):", -1, null));
		final String archiveStr = ConsoleWrapper.console.readString("Object to retrieve", true, "fcoprofile",
				new String[] { "globalprofile", "fcoprofile", "generationprofile", "vmxfile", "reportfile", "md5file",
						"vappconfig", });

		final ArchiveObjects aObj = ArchiveObjects.valueOf(archiveStr.toUpperCase(Utility.LOCALE));
		options.setArchiveObject(aObj);
		switch (aObj) {
		case FCOPROFILE:
		case GENERATIONPROFILE:
		case GLOBALPROFILE:
		case VAPPCONFIG:
			options.setPrettyJason(ConsoleWrapper.console.readBoolean("Human Readable output", true));
			break;
		case MD_5_FILE:
		case NONE:
		case REPORTFILE:
		case VMXFILE:
		default:
			break;

		}
		final boolean async = ConsoleWrapper.console.readBoolean("Async SOAP call", true);

		ConsoleWrapper.console.printf("%n");

		final ArchiveTest archiveTest = new ArchiveTest(this.sapi);
		final List<ResultAction> result = new ArrayList<>();
		if (async) {
			result.addAll(archiveTest.executeAsync(options));
		} else {
			result.addAll(archiveTest.execute(options));
		}
		return result;

	}

	private Collection<? extends ResultAction> testStatusArchive() throws FormatMismatch, InvalidTask_Exception,
			UnrecognizedToken_Exception, InterruptedException, InternalServer_Exception {
		final ArchiveStatusOptions options = new ArchiveStatusOptions();
		ConsoleWrapper.console.printf("%n");
		ConsoleWrapper.console.printf("Check Status Archive custom fco%n");
		options.getTargetList().addAll(ConsoleWrapper.console.readFcoString("type:name (ex vm:max_possa)"));

		options.getGenerationId().addAll(ConsoleWrapper.console
				.readGenerations("Generations to check (comma separated.-1 for the last one", -1));
		final boolean async = ConsoleWrapper.console.readBoolean("Async SOAP call", true);

		ConsoleWrapper.console.printf("%n");
		final List<ResultAction> result = new ArrayList<>();
		final ArchiveTest archiveTest = new ArchiveTest(this.sapi);
		if (async) {
			result.addAll(archiveTest.executeAsync(options));
		} else {
			result.addAll(archiveTest.execute(options));
		}
		return result;

	}
}
