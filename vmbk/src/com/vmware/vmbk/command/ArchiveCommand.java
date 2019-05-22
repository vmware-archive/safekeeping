/*******************************************************************************
 * Copyright (C) 2019, VMware Inc
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
package com.vmware.vmbk.command;

import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.vmware.vmbk.control.FcoArchiveManager;
import com.vmware.vmbk.control.FcoArchiveManager.ArchiveManagerMode;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.control.Vmbk;
import com.vmware.vmbk.control.info.InfoData;
import com.vmware.vmbk.control.target.ITarget;
import com.vmware.vmbk.logger.LoggerUtils;
import com.vmware.vmbk.profile.FcoProfile;
import com.vmware.vmbk.profile.GenerationProfile;
import com.vmware.vmbk.profile.GlobalProfile;
import com.vmware.vmbk.soap.ConnectionManager;
import com.vmware.vmbk.type.FirstClassObject;
import com.vmware.vmbk.type.ImprovedVirtuaDisk;
import com.vmware.vmbk.type.ManagedFcoEntityInfo;
import com.vmware.vmbk.type.PrettyBoolean;
import com.vmware.vmbk.type.VirtualAppManager;
import com.vmware.vmbk.type.VirtualMachineManager;
import com.vmware.vmbk.util.Utility;

public class ArchiveCommand extends CommandWithOptions {

    public enum ArchiveObjects {
	GlobalProfile, FcoProfile, GenerationProfile, VmxFile, ReportFile, Md5File, none
    }

    protected boolean check;
    protected boolean commit;

    protected LinkedList<Integer> generationId;

    private boolean isDetail;

    protected boolean isMtime;

    protected boolean list;
    protected long mtime;
    protected String mTimeString;
    protected boolean profile;
    protected boolean remove;
    protected Character signMtime;
    protected boolean status;

    protected ArchiveObjects show;

    protected ArchiveCommand() {
	initialize();
    }

    @Override
    public void action(final Vmbk vmbk) throws Exception {
	logger.entering(getClass().getName(), "action", new Object[] { vmbk });
	final GlobalProfile profAllFco = vmbk.getRepositoryTarget().initializeProfileAllFco();
	final ConnectionManager connetionManager = vmbk.getConnetionManager();
	if (this.list) {

	    action_List(connetionManager, profAllFco, vmbk.getRepositoryTarget());

	} else if (this.check || this.remove || this.status) {
	    action_check(profAllFco, vmbk.getRepositoryTarget());
	} else if (this.commit) {
	    action_commit(profAllFco, vmbk.getRepositoryTarget());
	} else if (this.show != ArchiveObjects.none) {
	    action_show(profAllFco, vmbk.getRepositoryTarget());
	}
	logger.exiting(getClass().getName(), "action");
    }

    @SuppressWarnings("unchecked")
    private void action_check(final GlobalProfile profAllFco, final ITarget repositoryTarget) throws IOException {
	logger.entering(getClass().getName(), "action_check", new Object[] { profAllFco, repositoryTarget });
	final LinkedList<ManagedFcoEntityInfo> Entities = getTargetfromRepository(profAllFco);

	int success = 0;
	int fails = 0;
	int skip = 0;
	for (final ManagedFcoEntityInfo vmInfo : Entities) {
	    if (Vmbk.isAbortTriggered()) {
		IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);
		++skip;
	    } else {
		if (this.remove && this.profile) {
		    if (!this.isQuiet()) {
			if (!IoFunction.confirmOperation("Remove profile %s ", vmInfo.toString())) {
			    ++skip;
			    continue;
			}
		    }
		    IoFunction.showInfo(logger, "Removing profile: %s", vmInfo.toString());
		    if (repositoryTarget.removeFcoProfile(vmInfo)) {
			IoFunction.print("\t-> Archive content removed.");
			if (profAllFco.delFcoProfile(vmInfo)) {
			    if (repositoryTarget.postProfAllFco(profAllFco.toByteArrayInOutputStream())) {
				++success;
				IoFunction.println();
				IoFunction.println("\t-> Profile removed.");
				LoggerUtils.logInfo(logger, "Profile: %s removed", vmInfo.toString());
				continue;
			    }
			}
		    }
		    ++fails;
		} else {
		    boolean isClean = true;
		    LinkedList<Integer> generationId = null;

		    FirstClassObject fco = null;
		    FcoArchiveManager fcoArcMgr = null;
		    try {
			switch (vmInfo.getEntityType()) {
			case VirtualMachine:
			    fco = new VirtualMachineManager(null, vmInfo);
			    repositoryTarget.setFcoTarget(fco);
			    fcoArcMgr = new FcoArchiveManager(fco, repositoryTarget, ArchiveManagerMode.readOnly);
			    break;
			case ImprovedVirtualDisk:
			    fco = new ImprovedVirtuaDisk(null, vmInfo);
			    repositoryTarget.setFcoTarget(fco);
			    fcoArcMgr = new FcoArchiveManager(fco, repositoryTarget, ArchiveManagerMode.readOnly);
			    break;
			case VirtualApp:
			    break;
			default:
			    break;
			}

			IoFunction.showInfo(logger, "%s %s\t",
				(this.check) ? "Check" : (this.remove) ? "Remove" : "Status", fco.toString());

			if (this.status) {
			    final boolean isAvailable = profAllFco.isAvailableWithUuid(fco.getUuid());
			    IoFunction.println(getStatusString(fcoArcMgr, isAvailable));
			    continue;
			}
			generationId = (LinkedList<Integer>) this.generationId.clone();
			if (generationId.size() == 0) {
			    if (fcoArcMgr.getLatestSucceededGenerationId() < 0) {
				IoFunction.println("\tNo archive available.\n");
				continue;
			    }
			    generationId.add(fcoArcMgr.getLatestSucceededGenerationId());
			} else if (generationId.size() == 1) {
			    if (generationId.get(0) == -1) {
				generationId.clear();
				generationId.addAll(fcoArcMgr.getGenerationIdList());
				if (generationId.size() == 0) {
				    IoFunction.showInfo(logger, "No generations available");
				    continue;
				}
			    }
			    if (generationId.get(0) == -2) {
				generationId.clear();
				final int latestSucceededGenerationId = fcoArcMgr.getLatestSucceededGenerationId();

				generationId.add(latestSucceededGenerationId);
				if ((generationId.size() == 0) || (latestSucceededGenerationId == -2)) {
				    IoFunction.showInfo(logger, "No suceeded last generation available");
				    continue;
				}
			    }
			    if (generationId.get(0) == -3) {
				generationId.clear();
				generationId.addAll(fcoArcMgr.getFailedGenerationList());
				if (generationId.size() == 0) {
				    IoFunction.showInfo(logger, "No failed generations available");
				    continue;
				}
			    }
			    if (generationId.get(0) == -4) {
				generationId.clear();
				generationId.add(fcoArcMgr.getLatestGenerationId());
				if (generationId.size() == 0) {
				    IoFunction.showInfo(logger, "No last generation available");
				    continue;
				}
			    }
			}

			for (final Integer genId : generationId) {
			    if (Vmbk.isAbortTriggered()) {
				IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);

			    } else {
				IoFunction.printf("\tGeneration %d\n\t\t", genId);

				if (this.check) {
				    isClean &= checkGeneration(fcoArcMgr, genId, this.isDryRun());
				    IoFunction.printf("\t     -> Result: %s\n\n", (isClean ? "ok" : "currupted"));

				} else if (this.remove) {
				    isClean &= deleteGeneration(fcoArcMgr, genId, this.isQuiet(), this.isDryRun());
				}
			    }
			}

		    } catch (final Exception e) {
			IoFunction.showWarning(logger, "checkGeneration of %s failed.", vmInfo.getUuid());
			logger.warning(Utility.toString(e));

			isClean = false;
		    }
		    if (generationId.size() > 0) {
			if (isClean) {
			    if (this.remove) {
				if (repositoryTarget
					.postProfileFco(fcoArcMgr.getFcoProfile().toByteArrayInOutputStream())) {
				    ++success;
				} else {
				    ++fails;
				}
			    } else if (this.status) {
				IoFunction.println("(*) Latest generation");
			    } else {
				++success;
			    }
			} else {
			    ++fails;
			}
		    } else {
			++skip;
		    }

		}
	    }
	}
	if ((success + fails + skip) > 1) {
	    IoFunction.println();
	    IoFunction.printf("%s Total:%d Success:%d fails:%d skips:%d ", (this.check) ? "Check" : "Delete",
		    success + fails + skip, success, fails, skip);
	    IoFunction.println();
	}
	IoFunction.println();
	logger.exiting(getClass().getName(), "action_check");
    }

    private void action_commit(final GlobalProfile profAllFco, final ITarget iTarget) throws IOException {
	logger.entering(getClass().getName(), "action_commit");

	iTarget.postProfAllFco(profAllFco.toByteArrayInOutputStream());
	IoFunction.showInfo(logger, "Profile committed");

	logger.exiting(getClass().getName(), "action_commit");
    }

    private void action_List(final ConnectionManager connetionManager, final GlobalProfile profAllFco,
	    final ITarget repositoryTarget) {
	logger.entering(getClass().getName(), "action_List",
		new Object[] { connetionManager, profAllFco, repositoryTarget });

	final List<ManagedFcoEntityInfo> fcoList = getTargetfromRepository(profAllFco);
	List<FirstClassObject> tlist = null;

	tlist = new LinkedList<>();

	for (final ManagedFcoEntityInfo fcoInfo : fcoList) {
	    FirstClassObject fco = null;
	    switch (fcoInfo.getEntityType()) {
	    case VirtualMachine:
		fco = new VirtualMachineManager(connetionManager.getVimConnection(fcoInfo.getServerUuid()), fcoInfo);
		break;
	    case ImprovedVirtualDisk:
		fco = new ImprovedVirtuaDisk(connetionManager.getVimConnection(fcoInfo.getServerUuid()), fcoInfo);
		break;
	    case VirtualApp:
		fco = new VirtualAppManager(connetionManager.getVimConnection(fcoInfo.getServerUuid()), fcoInfo);
		break;
	    case K8sNamespace:
		break;
	    default:
		break;
	    }
	    if (fco != null) {
		tlist.add(fco);
	    } else {
		logger.warning("fco " + fcoInfo.toString() + "is invalid");
	    }

	}

	final List<InfoData> infoData = new LinkedList<>();
	for (final FirstClassObject vmm : tlist) {
	    if (Vmbk.isAbortTriggered()) {
		IoFunction.showWarning(logger, Vmbk.OPERATION_ABORTED_BY_USER);

	    } else {
		try {
		    repositoryTarget.setFcoTarget(vmm);

		    if (repositoryTarget.isProfileVmExist()) {
			FcoArchiveManager vmArcMgr = null;
//			if (vmm instanceof VirtualMachineManager) {
			vmArcMgr = new FcoArchiveManager(vmm, repositoryTarget, ArchiveManagerMode.readOnly);
//			} else if (vmm instanceof VirtualAppManager) {
//			    vmArcMgr = new FcoArchiveManager(GlobalConfiguration.config, vmm, repositoryTarget,
//				    ArchiveManagerMode.readOnly);}
//			    else if (vmm instanceof ImprovedVirtuaDisk) {
//			    vmArcMgr = new FcoArchiveManager(GlobalConfiguration.config, vmm, repositoryTarget,
//				    ArchiveManagerMode.readOnly);
//			}
			if (vmArcMgr != null) {
			    final InfoData data = new InfoData(vmArcMgr);

			    final long tsNow = Calendar.getInstance().getTimeInMillis();
			    if (this.isMtime()) {
				if (this.isSatisfyTime(data.getTimestampMsOfLatestSucceededGenerationId(), tsNow)) {
				    infoData.add(data);
				}
			    } else {
				infoData.add(data);
			    }
			}

		    }
		} catch (final Exception e) {
		    logger.warning(Utility.toString(e));
		}
	    }
	}
	IoFunction.println();
	IoFunction.println(this.toString());
	IoFunction.println(InfoData.getTableKeys());
	IoFunction.println();
	IoFunction.println(InfoData.getTableHeader());

	for (final InfoData data : infoData) {

	    IoFunction.println(data.toString());
	}
	logger.exiting(getClass().getName(), "action_list");
    }

    private void action_show(final GlobalProfile profAllFco, final ITarget repositoryTarget) {
	logger.entering(getClass().getName(), "action_show", new Object[] { profAllFco, repositoryTarget });
	final StringBuilder report = new StringBuilder();
	byte[] reportContent = null;
	if (this.show == ArchiveObjects.GlobalProfile) {
	    reportContent = repositoryTarget.getGlobalProfileToByteArray();
	    report.append("GlobalProfile Content:\n");
	    report.append(new String(reportContent));
	    IoFunction.println(report.toString());
	} else {
	    final LinkedList<ManagedFcoEntityInfo> Entities = getTargetfromRepository(profAllFco);
	    for (final ManagedFcoEntityInfo vmInfo : Entities) {
		FirstClassObject fco = null;
		try {
		    switch (vmInfo.getEntityType()) {
		    case VirtualMachine:
			fco = new VirtualMachineManager(null, vmInfo);
			repositoryTarget.setFcoTarget(fco);
			break;
		    case ImprovedVirtualDisk:
			fco = new ImprovedVirtuaDisk(null, vmInfo);
			repositoryTarget.setFcoTarget(fco);
			break;
		    case VirtualApp:
			break;
		    default:
			break;
		    }

		} catch (final Exception e) {
		    IoFunction.showWarning(logger, "checkGeneration of %s failed.", vmInfo.getUuid());
		    logger.warning(Utility.toString(e));
		}

		switch (this.show) {
		case FcoProfile:
		    reportContent = repositoryTarget.getFcoProfileToByteArray();
		    report.append("FcoProfile Content:\n");
		    break;
		case GenerationProfile:

		    report.append("GenerationProfile ");
		    report.append(repositoryTarget.getFcoProfileToByteArray());
		    if (this.generationId != null) {
			report.append("generation ");
			report.append(this.generationId);
			for (final Integer gen : this.generationId) {
			    reportContent = repositoryTarget.getGenerationProfileToByteArray(gen);
			}
		    } else {

			reportContent = repositoryTarget.getGenerationProfileToByteArray();
		    }
		    report.append(" Content:\n");
		    break;
		case GlobalProfile:
		    reportContent = repositoryTarget.getGlobalProfileToByteArray();
		    report.append("GlobalProfile Content:\n");
		    break;
		case Md5File:
		    reportContent = repositoryTarget.getMd5();
		    report.append("Md5 Content:\n");
		    break;
		case ReportFile:

		    break;
		case VmxFile:
		    reportContent = repositoryTarget.getVmxToByteArray();
		    report.append("VMX configuration file Content:\n");

		    break;
		case none:
		    break;
		}
		report.append(new String(reportContent));
		IoFunction.println(report.toString());
	    }
	}
    }

    private boolean checkGeneration(final FcoArchiveManager vmArcMgr, final int genId, final boolean isDryRun) {
	logger.entering(getClass().getName(), "checkGeneration", new Object[] { vmArcMgr, genId, isDryRun });
	final boolean result = checkGeneration(vmArcMgr, genId, isDryRun, true);
	logger.exiting(getClass().getName(), "checkGeneration", result);
	return result;
    }

    private boolean checkGeneration(final FcoArchiveManager vmArcMgr, int genId, final boolean isDryRun,
	    final boolean first) {
	boolean ret = true;
	GenerationProfile profGen = null;
	profGen = vmArcMgr.loadProfileGeneration(genId);

	if (profGen == null) {
	    LoggerUtils.logWarning(logger, "No Generation %d is available.", genId);
	    IoFunction.printf(" %3d (missing)", genId);
	    return false;
	}
	if (profGen.isSucceeded() == false) {
	    LoggerUtils.logWarning(logger, "Generation %d is marked FAILED.", genId);
	    IoFunction.printf(" %3d (failed)", genId);
	    return false;
	}
	genId = profGen.getGenerationId();
	LoggerUtils.logInfo(logger, "Starting check Generation %d", genId);

	final int dependId = vmArcMgr.getDependingGenerationId(genId);
	if (dependId > -1) {
	    if (first) {
		IoFunction.print("Dependency: ");
	    }
	    LoggerUtils.logInfo(logger, "Generation %d depend on %d. Loading generation %d", genId, dependId, dependId);
	    IoFunction.printf(" %3d ->", genId);
	    ret &= checkGeneration(vmArcMgr, dependId, isDryRun, false);
	    if (!ret) {
		return ret;
	    }
	} else {
	    if (first) {
		IoFunction.print("No dependence\n");
	    } else {
		IoFunction.printf(" %3d\n", genId);
	    }
	}

	assert profGen != null;
	vmArcMgr.setTargetGeneration(profGen);
	vmArcMgr.getRepositoryTarget().setProfGen(profGen);

	ret &= doCheck(vmArcMgr, genId, isDryRun);
	return ret;
    }

    private boolean deleteGeneration(final FcoArchiveManager vmArcMgr, final int genId, final boolean isQuiet,
	    final boolean isDryRun) {
	return deleteGeneration(vmArcMgr, genId, isQuiet, isDryRun, true);
    }

    private boolean deleteGeneration(final FcoArchiveManager vmArcMgr, final int genId, final boolean isQuiet,
	    final boolean isDryRun, final boolean first) {
	logger.entering(getClass().getName(), "deleteGeneration",
		new Object[] { vmArcMgr, genId, isQuiet, isDryRun, first });
	boolean result = true;
	final FcoProfile fcoProfile = vmArcMgr.getFcoProfile();
	if (fcoProfile.isGenerationExist(genId)) {
	    LoggerUtils.logInfo(logger, "Starting delete Generation %d", genId);
	    final int dependId = vmArcMgr.getDependingOnGenerationId(genId);
	    if (dependId > -1) {
		if (first) {
		    IoFunction.print("Dependency: ");
		}
		LoggerUtils.logInfo(logger, "Generation %d depend on %d. Loading generation %d", genId, dependId,
			dependId);
		IoFunction.printf(" Gen %2d ->", genId);
		result &= deleteGeneration(vmArcMgr, dependId, isQuiet, isDryRun, false);
	    } else {
		if (first) {
		    IoFunction.print("No dependence");
		} else {
		    IoFunction.printf(" Gen %2d", genId);
		}
		if (!isQuiet) {
		    if (!IoFunction.confirmOperation("\nReady to be removed")) {
			result = false;
		    }
		}
		if (result) {
		    IoFunction.println();
		    IoFunction.print("\t\tRemoving:   ");
		}
	    }
	    if (result) {
		IoFunction.printf(" %2d(", genId);
		final GenerationProfile profGen = vmArcMgr.loadProfileGeneration(genId);
		if (profGen == null) {
		    LoggerUtils.logWarning(logger, "The specified generation %d has no content.", genId);
		    IoFunction.print("X");
		} else {
		    vmArcMgr.getRepositoryTarget().setProfGen(profGen);
		    if (isDryRun) {
			IoFunction.print("O");
		    } else {
			if (vmArcMgr.getRepositoryTarget().removeGeneration(genId)) {
			    IoFunction.print("O");
			    LoggerUtils.logInfo(logger, "%s %s generation %d removed.",
				    vmArcMgr.getEntityType().toString(), vmArcMgr.getName(), genId);
			} else {
			    IoFunction.print("X");
			    LoggerUtils.logWarning(logger, "%s %s generation %d removing failed.",
				    vmArcMgr.getEntityType().toString(), vmArcMgr.getName(), genId);
			}
		    }
		}
		try {
		    if (isDryRun) {
			IoFunction.print("O)");
		    } else {
			fcoProfile.delGenerationInfo(genId);
			IoFunction.print("O)");
			LoggerUtils.logInfo(logger, "%s %s generation %d profile entry deleted.",
				vmArcMgr.getEntityType().toString(), vmArcMgr.getName(), genId);
		    }
		} catch (final Exception e) {
		    IoFunction.print("X)");
		    LoggerUtils.logWarning(logger, "%s %s generation %d delete profile entry failed.",
			    vmArcMgr.getEntityType().toString(), vmArcMgr.getName(), genId);
		    logger.warning(Utility.toString(e));
		}
	    }
	} else {
	    LoggerUtils.logWarning(logger, "No Generation %d is available.", genId);
	    IoFunction.printf(" Gen %3d (missing)", genId);
	    IoFunction.println();
	    IoFunction.print("\t\tRemoving:   ");
	    result = true;
	}
	logger.exiting(getClass().getName(), "deleteGeneration", result);
	return result;
    }

    private boolean doCheck(final FcoArchiveManager vmArcMgr, final int genId, final boolean isDryRun) {
	if (vmArcMgr.getRepositoryTarget().isMd5FileExist(genId)) {
	    if (isDryRun) {
		return true;
	    }
	    boolean ret = true;

	    final byte[] md5 = vmArcMgr.getRepositoryTarget().getMd5();
	    final String md5St = new String(md5).replaceAll("\t", "");

	    final String[] md5Split = md5St.split("[*?\\r?\\n]");
	    final int numFiles = md5Split.length / 2;
	    final String msg = String.format("\tGeneration %d Blocks %d.", genId, numFiles);
	    logger.fine(msg);
	    IoFunction.print('\t');
	    IoFunction.print(msg);
	    IoFunction.print(": ");
	    for (int i = 0; i < md5Split.length; i += 2) {
		final int j = (i / 2) + 1;
		final String md5DecodedST = new String(vmArcMgr.getRepositoryTarget().getObjectMd5(md5Split[i + 1]));
		if (md5DecodedST.equalsIgnoreCase(md5Split[i])) {
		    LoggerUtils.logInfo(logger, "MD5:%s File:%s", md5Split[i], md5Split[i + 1]);
		    if ((numFiles - j) >= ((numFiles % 10))) {
			if (((j % 10) == 0)) {
			    IoFunction.print('O');
			}
		    } else {
			IoFunction.print('o');
		    }
		} else {
		    LoggerUtils.logWarning(logger, "Generation %d files checksum check failed", genId);
		    LoggerUtils.logWarning(logger, "File:%s Aspected MD5:%s Real MD5:%s", md5Split[i + 1], md5Split[i],
			    md5DecodedST);
		    IoFunction.print('X');
		    ret &= false;
		}
	    }
	    LoggerUtils.logInfo(logger, "Generation %d files checksum check completed %s.", genId,
		    (ret) ? "succesfully" : "with errors");
	    IoFunction.println();
	    return ret;
	} else {
	    return false;
	}
    }

    public LinkedList<Integer> getGenerationId() {
	return this.generationId;
    }

    private String getStatusString(final FcoArchiveManager arcMgr, final boolean isAvailable) {
	final FcoProfile profFco = arcMgr.getFcoProfile();

	final StringBuffer sb = new StringBuffer();
	sb.append(profFco.getStatusString(isAvailable));
	final int ActiveGenId = arcMgr.getLatestSucceededGenerationId();
	if (ActiveGenId < 0) {
	    sb.append("\n\tArchive is empty.\n");
	} else {
	    sb.append("\n\tGen\tDate\t\t\t");
	    sb.append("\tn device  size(GB)  mode  Exec Time");
	    if (isDetail()) {

		for (final Integer genId : profFco.getGenerationIdList()) {
		    assert genId != null;
		    assert genId >= 0;
		    if (genId == ActiveGenId) {
			sb.append("\n     (*)");
		    } else {
			sb.append("\n\t");
		    }

		    sb.append(arcMgr.getGenerationStatusString(genId));
		}
		sb.append('\n');
	    } else {
		sb.append("\n     (*)");
		sb.append(arcMgr.getGenerationStatusString(ActiveGenId));
	    }

	    sb.append("\n\t(*) Latest generation\n");
	}
	return sb.toString();
    }

    @Override
    public void initialize() {
	super.initialize();
	this.isDetail = false;
	this.generationId = new LinkedList<>();
	this.isMtime = false;
	this.signMtime = null;
	this.mtime = 0L;
	this.remove = false;
	this.profile = false;
	this.commit = false;
	this.show = ArchiveObjects.none;
	this.check = false;
	this.list = false;
	this.mtime = 0;
	this.mTimeString = StringUtils.EMPTY;
	this.signMtime = null;
	this.status = false;
    }

    public boolean isDetail() {
	return this.isDetail;
    }

    public boolean isMtime() {
	return this.isMtime;
    }

    private boolean isSatisfyTime(final long tgtMs, final long nowMs) {
	boolean isPass = false;
	long baseMs;
	if (this.isMtime) {
	    baseMs = nowMs - (this.mtime);

	    if (this.signMtime.equals('+')) {
		if (baseMs > tgtMs) {
		    isPass = true;
		}
	    } else if (this.signMtime.equals('-')) {
		if (baseMs < tgtMs) {
		    isPass = true;
		}
	    }

	}
	return isPass;
    }

    public void setDetail(final boolean isDetail) {
	this.isDetail = isDetail;
    }

    @Override
    public String toString() {
	final StringBuffer sb = new StringBuffer();
	sb.append("RepositoryInfo: ");

	sb.append(String.format("[isDetails %s]", PrettyBoolean.toString(this.isDetail)));
	if (this.isMtime) {
	    sb.append(String.format("[mtime %s %s]", (this.signMtime == null ? "" : this.signMtime.toString()),
		    this.mTimeString));
	}
	return sb.toString();
    }
}
