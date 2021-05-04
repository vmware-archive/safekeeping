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
package com.vmware.safekeeping.cxf.test.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.regex.Pattern;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.Utility;
import com.vmware.sapi.BackupMode;
import com.vmware.sapi.EntityType;
import com.vmware.sapi.FcoTarget;
import com.vmware.sapi.FcoTypeSearch;
import com.vmware.sapi.InternalServer_Exception;
import com.vmware.sapi.InvalidTask_Exception;
import com.vmware.sapi.ManagedFcoEntityInfo;
import com.vmware.sapi.OperationState;
import com.vmware.sapi.ResultAction;
import com.vmware.sapi.Sapi;
import com.vmware.sapi.SearchManagementEntity;
import com.vmware.sapi.SearchManagementEntityInfoType;
import com.vmware.sapi.Task;
import com.vmware.sapi.TaskResult;
import com.vmware.sapi.UnrecognizedToken_Exception;
import com.vmware.sapi.VMwareCloudPlatforms;

/**
 * @author mdaneri
 *
 */
public final class TestUtility {
    public static final String UUID_ZERO = "00000000-00000000-00000000-00000000";
    private static final Pattern IP4PATTERN = Pattern
            .compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    private static final Pattern UUIDPATTERN = Pattern
            .compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    public static String backupModeToString(final BackupMode mode) {
        switch (mode) {
        case FULL:
            return "Full";
        case INCREMENTAL:
            return "Incr";
        case UNKNOW:
        default:
            return "Unkn";
        }
    }

    public static String entityTypeToString(final EntityType entity) {
        switch (entity) {
        case IMPROVED_VIRTUAL_DISK:
            return "ivd";
        case K_8_S_NAMESPACE:
            return "k8s";
        case VIRTUAL_APP:
            return "vapp";
        case VIRTUAL_MACHINE:
            return "vm";
        case VIRTUAL_MACHINE_SNAPSHOT:
        case RESOURCE_POOL:
        case VIRTUALIZATION_MANAGER:
        case CLUSTER_COMPUTE_RESOURCE:
        case COMPUTE_RESOURCE:
        case CONTAINER_VIEW:
        case DATACENTER:
        case DATASTORE:
        case FOLDER:
        case HOST_SYSTEM:
        default:
            return entity.toString();
        }

    }

    public static String fcoToString(final ManagedFcoEntityInfo entity) {
        String lname;
        if (entity.getUuid().equals(UUID_ZERO)) {
            return "No Entity";
        } else {
            if (entity.getName().length() < 30) {
                lname = entity.getName();
            } else {
                lname = entity.getName().substring(0, 27) + "...";
            }
            if (entity.getMorefValue() != null) {
                return String.format("%s:%s uuid:%s moref:%s", entityTypeToString(entity.getEntityType()), lname,
                        entity.getUuid(), entity.getMorefValue());
            }
            return String.format("%-8s%36s\t%-30s ", entityTypeToString(entity.getEntityType()), entity.getUuid(),
                    lname);
        }
    }

    public static List<FcoTarget> getFcoTargets(final String fcoName) throws FormatMismatch {
        final String st = fcoName.trim();
        final List<FcoTarget> res = new ArrayList<>();
        String searchType = null;
        int index = 0;
        while (index < st.length()) {
            if (searchType == null) {
                if (st.charAt(index) == 'v') {
                    if (st.charAt(index + 1) == 'm') {
                        if (st.charAt(index + 2) == ':') {
                            index += 3;
                            searchType = "vm";
                        }
                    } else if ((st.charAt(index + 1) == 'a') && (st.charAt(index + 2) == 'p')
                            && (st.charAt(index + 3) == 'p') && (st.charAt(index + 4) == ':')) {
                        index += 5;
                        searchType = "vapp";
                    } else {
                        ++index;
                    }
                } else if ((st.charAt(index) == 'i') && (st.charAt(index + 1) == 'v') && (st.charAt(index + 2) == 'd')
                        && (st.charAt(index + 3) == ':')) {
                    index += 4;
                    searchType = "ivd";
                } else if ((st.charAt(index) == 't') && (st.charAt(index + 1) == 'a') && (st.charAt(index + 2) == 'g')
                        && (st.charAt(index + 3) == ':')) {
                    index += 4;
                    searchType = "tag";
                } else {
                    throw new FormatMismatch("fcoName :" + st + " is not valid");
                }
            } else {
                final StringBuilder fcoValue = new StringBuilder();
                final char endFco = (st.charAt(index) == '"') ? '"' : ' ';
                if (endFco == '"') {
                    ++index;
                }
                while (index < st.length()) {
                    if ((st.charAt(index) != endFco)) {
                        fcoValue.append(st.charAt(index));
                        ++index;
                    } else {
                        ++index;
                        break;
                    }
                }

                FcoTypeSearch typeVm = null;
                final String key = fcoValue.toString();
                switch (searchType) {
                case "ivd":
                    if (UUIDPATTERN.matcher(key).matches()) {
                        typeVm = FcoTypeSearch.IVD_UUID;
                    } else {
                        typeVm = FcoTypeSearch.IVD_NAME;
                    }
                    break;
                case "vapp":
                    if (key.startsWith("resgroup-")) {
                        typeVm = FcoTypeSearch.VAPP_MOREF;
                    } else if (UUIDPATTERN.matcher(key).matches()) {
                        typeVm = FcoTypeSearch.VAPP_UUID;
                    } else {
                        typeVm = FcoTypeSearch.VAPP_NAME;
                    }
                    break;
                case "vm":
                    if (UUIDPATTERN.matcher(key).matches()) {
                        typeVm = FcoTypeSearch.VM_UUID;
                    } else if (IP4PATTERN.matcher(key).matches()) {
                        typeVm = FcoTypeSearch.VM_IP;
                    } else if (key.startsWith("vm-")) {
                        typeVm = FcoTypeSearch.VM_MOREF;
                    } else {
                        typeVm = FcoTypeSearch.VM_NAME;
                    }
                    break;

                case "tag":
                    typeVm = FcoTypeSearch.TAG;
                    break;
                default:
                    break;
                }
                final FcoTarget fcoTarget = new FcoTarget();
                fcoTarget.setKey(key);
                fcoTarget.setKeyType(typeVm);
                res.add(fcoTarget);
                searchType = null;

            }
        }
        return res;
    }

    /**
     * Creeate a SearchManagementEntity
     *
     * @param searchType
     * @param value
     * @return
     */
    public static SearchManagementEntity newSearchManagementEntity(final SearchManagementEntityInfoType searchType,
            final String value) {
        final SearchManagementEntity sme = new SearchManagementEntity();
        sme.setSearchType(searchType);
        sme.setSearchValue(value);
        return sme;
    }

    public static String operationStateToString(final OperationState state) {
        switch (state) {
        case ABORTED:
            return "Aborted";
        case FAILED:
            return "Failed";
        case QUEUED:
            return "Queued";
        case SKIPPED:
            return "Skipped";
        case STARTED:
            return "Started";
        case SUCCESS:
            return "Success";
        default:
            return "UNDEFINED";

        }

    }

    public static String taskStatusToString(final String command, final ResultAction rab) {
        if (rab.getReason() == null) {
            return String.format("%s %s: %s%n", command, TestUtility.fcoToString(rab.getFcoEntityInfo()),
                    operationStateToString(rab.getState()));
        } else {
            return String.format("%s %s: %s (%s)%n", command, TestUtility.fcoToString(rab.getFcoEntityInfo()),
                    operationStateToString(rab.getState()), rab.getReason());
        }
    }

    public static String toGMTString(final Date date) {
        final SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
        sdf.applyPattern("dd MMM yyyy HH:mm:ss z");
        return sdf.format(date);

    }

    /**
     * @param timeStamp
     * @return
     */
    public static Object toGMTString(final XMLGregorianCalendar timeStamp) {
        return toGMTString(timeStamp.toGregorianCalendar().getTime());
    }

    public static TaskResult waitForTask(final Sapi sapi, final Task task, final boolean visible)
            throws UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
        ResultAction result = null;
        if (visible) {
            System.out.println();
            System.out.print("0%|");
            System.out.print(StringUtils.repeat("---------", "|", 10));
            System.out.println("|100%");
            System.out.print("   ");
        }
        int progressPercent = 0;
        try {
            int previousProgressPercent = 0;

            int isTen = 0;
            while ((progressPercent <= Utility.ONE_HUNDRED_PER_CENT_AS_INT)) {
                result = sapi.getTaskInfo(task).getResult();
                if (result.getState() == OperationState.FAILED) {
                    break;
                }
                if (result.isDone()) {
                    break;
                }

                progressPercent = result.getProgress();
                if (visible) {
                    for (int i = 0; i < (progressPercent - previousProgressPercent); i++) {
                        ++isTen;
                        if (isTen > 9) {
                            System.out.print('O');
                            isTen = 0;
                        } else {
                            System.out.print('o');
                        }
                    }
                }
                previousProgressPercent = progressPercent;
                Thread.sleep(2000);

            }
        } catch (final InterruptedException e) {
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        final TaskResult tr = sapi.getTaskInfo(task);
        result = tr.getResult();
        if (visible) {
            if (result.getState() == OperationState.FAILED) {

                System.out.println();
                System.out.println("Error: " + result.getReason());
            } else {
                if (progressPercent != Utility.ONE_HUNDRED_PER_CENT_AS_INT) {
                    for (int i = progressPercent; i < 99; i++) {
                        System.out.print('o');
                    }
                    System.out.print('O');
                }
            }
            System.out.println();
        }
        return tr;
    }

    public static String cloudPlatformToString(VMwareCloudPlatforms cloudPlatform) {
        switch (cloudPlatform) {

        case VMC_GOVCLOUD:
            return "VMCloud on AWS GovCloud";
        case VMC_ON_AWS:
            return "VMCloud on AWS";
        case VMC_ON_AZURE:
            return "Azure VMware Solution";
        case VMC_ON_DELL_EMC:
            return "VMCloud on Dell/EMC";
        case VMC_ON_GOOGLE:
            return "Google Cloud VMware Engine";
        case VMC_ON_IBM:
            return "IBM Cloud for VMware Solution";
        case VMC_ON_ORACLE:
            return "Oracle Cloud for VMware Solution";
        case VMC_OUTPOST:
            return "VMCloud on AWS Outpost";
        case ON_PREM:
        default:
            return "VMware Cloud Foundation On-Premise";
        }
    }

    private TestUtility() {
        throw new IllegalStateException("Utility class");
    }
}
