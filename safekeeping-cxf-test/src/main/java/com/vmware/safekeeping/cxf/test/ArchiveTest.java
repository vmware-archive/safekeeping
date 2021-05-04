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

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.cxf.test.common.ConsoleWrapper;
import com.vmware.safekeeping.cxf.test.common.TestUtility;
import com.vmware.sapi.ArchiveCheckGenerationsOptions;
import com.vmware.sapi.ArchiveListOptions;
import com.vmware.sapi.ArchiveObjects;
import com.vmware.sapi.ArchiveRemoveGenerationsOptions;
import com.vmware.sapi.ArchiveRemoveProfileOptions;
import com.vmware.sapi.ArchiveShowOptions;
import com.vmware.sapi.ArchiveStatusOptions;
import com.vmware.sapi.CheckGenerationsPhases;
import com.vmware.sapi.EntityType;
import com.vmware.sapi.FcoTarget;
import com.vmware.sapi.GenerationDiskInfo;
import com.vmware.sapi.GenerationDisksInfoList;
import com.vmware.sapi.GenerationVirtualMachinesInfoList;
import com.vmware.sapi.GenerationsFilter;
import com.vmware.sapi.InfoData;
import com.vmware.sapi.InternalCoreResult_Exception;
import com.vmware.sapi.InternalServer_Exception;
import com.vmware.sapi.InvalidOptions_Exception;
import com.vmware.sapi.InvalidTask_Exception;
import com.vmware.sapi.ManagedFcoEntityInfo;
import com.vmware.sapi.OperationState;
import com.vmware.sapi.RemoveGenerationsPhases;
import com.vmware.sapi.RemoveProfilePhases;
import com.vmware.sapi.ResultAction;
import com.vmware.sapi.ResultActionArchiveCheckGeneration;
import com.vmware.sapi.ResultActionArchiveCheckGenerationWithDependencies;
import com.vmware.sapi.ResultActionArchiveItem;
import com.vmware.sapi.ResultActionArchiveItemsList;
import com.vmware.sapi.ResultActionArchiveIvdStatus;
import com.vmware.sapi.ResultActionArchiveRemoveGeneration;
import com.vmware.sapi.ResultActionArchiveRemoveGenerationWithDependencies;
import com.vmware.sapi.ResultActionArchiveRemovedProfile;
import com.vmware.sapi.ResultActionArchiveShow;
import com.vmware.sapi.ResultActionArchiveStatus;
import com.vmware.sapi.ResultActionArchiveVappStatus;
import com.vmware.sapi.ResultActionArchiveVmStatus;
import com.vmware.sapi.Sapi;
import com.vmware.sapi.StatusDiskInfo;
import com.vmware.sapi.StatusProfilePhases;
import com.vmware.sapi.StatusVirtualMachineDiskInfo;
import com.vmware.sapi.Task;
import com.vmware.sapi.Tasks;
import com.vmware.sapi.UnrecognizedToken_Exception;

public class ArchiveTest extends AbstractTest {

    public ArchiveTest(final Sapi sapi) {
        super(sapi);
    }

    public List<ResultAction> execute(final ArchiveCheckGenerationsOptions options)
            throws InvalidTask_Exception, UnrecognizedToken_Exception, InterruptedException, InternalServer_Exception {
        ResultActionArchiveCheckGenerationWithDependencies rab = null;
        MessagesTemplate.printTestHeader(this.getClass().getName(), options);

        final Tasks taskCheckGenerations = this.sapi.checkArchiveGenerations(options);

        if (taskCheckGenerations.getState() == OperationState.SUCCESS) {
            final List<Task> tasks = taskCheckGenerations.getTaskList();
            Task tb = null;
            if (!tasks.isEmpty()) {
                boolean tasksDone = false;
                final boolean[] processed = new boolean[tasks.size()];
                while (!tasksDone) {
                    for (int mainIndex = 0; mainIndex < tasks.size(); mainIndex++) {
                        tasksDone = areTasksDone(processed);
                        if (tasksDone) {
                            break;
                        }
                        tb = tasks.get(mainIndex);
                        rab = (ResultActionArchiveCheckGenerationWithDependencies) this.sapi.getTaskInfo(tb)
                                .getResult();
                        switch (rab.getState()) {
                        case ABORTED:

                        case FAILED:
                        case SKIPPED:
                            if (!processed[mainIndex]) {
                                ConsoleWrapper.console.printf("Check %s: %s (%s)\n",
                                        TestUtility.fcoToString(rab.getFcoEntityInfo()), rab.getState().toString(),
                                        rab.getReason());
                                processed[mainIndex] = true;
                            }
                            break;
                        case QUEUED:
                            break;
                        case SUCCESS:
                        case STARTED:
                            if (!processed[mainIndex]) {
                                try {
                                    ConsoleWrapper.console.printf("Check %s:\n",
                                            TestUtility.fcoToString(rab.getFcoEntityInfo()));

                                    rab = waitForPhases(tb, CheckGenerationsPhases.END_RETRIEVE_DEPENDING_GENERATIONS,
                                            true);

                                    ConsoleWrapper.console.printf("%d generation(s) to check\n",
                                            rab.getSubOperations().size());
                                    boolean subTasksDone = rab.getSubOperations().isEmpty();
                                    final boolean[] subProcessed = new boolean[rab.getSubOperations().size()];
                                    while (!subTasksDone) {
                                        for (int subIndex = rab.getSubOperations().size() - 1,
                                                k = 1; subIndex >= 0; subIndex--, k++) {
                                            subTasksDone = areTasksDone(subProcessed);
                                            if (subTasksDone) {
                                                break;
                                            }
                                            final Task task3 = rab.getSubOperations().get(subIndex);
                                            ResultActionArchiveCheckGeneration raargi = (ResultActionArchiveCheckGeneration) this.sapi
                                                    .getTaskInfo(task3).getResult();
                                            switch (raargi.getState()) {
                                            case ABORTED:

                                            case FAILED:
                                            case SKIPPED:
                                                subProcessed[subIndex] = true;
                                                break;
                                            case QUEUED:
                                                break;
                                            case SUCCESS:
                                            case STARTED:
                                                if (!subProcessed[subIndex]) {
                                                    try {

                                                        raargi = (ResultActionArchiveCheckGeneration) TestUtility
                                                                .waitForTask(this.sapi, task3, false).getResult();

                                                        switch (raargi.getDependenciesInfo().getMode()) {
                                                        case FULL:
                                                        case UNKNOW:
                                                            ConsoleWrapper.console.printf("%3d %2d(%s)    \t", k,
                                                                    raargi.getGenId(), "full");
                                                            break;
                                                        case INCREMENTAL:

                                                            ConsoleWrapper.console.printf("%3d     %2d(%s)\t", k,
                                                                    raargi.getGenId(), "incr");
                                                            break;

                                                        default:
                                                            break;

                                                        }
                                                        ConsoleWrapper.console
                                                                .print(String.format("Generation %03d Blocks %-4d:",
                                                                        raargi.getGenId(), raargi.getNumOfFiles()));
                                                        int j = 0;
                                                        int any = 10;
                                                        if (raargi.getMd5FileCheck().size() > 500) {
                                                            any = 50;
                                                        }

                                                        for (final Boolean md5Check : raargi.getMd5FileCheck()) {
                                                            j++;
                                                            if (Boolean.TRUE.equals(md5Check)) {
                                                                if ((raargi.getNumOfFiles()
                                                                        - j) >= (raargi.getNumOfFiles() % any)) {
                                                                    if (((j % any) == 0)) {
                                                                        ConsoleWrapper.console.print('O');
                                                                    } else {
                                                                        ConsoleWrapper.console.print('o');
                                                                    }
                                                                }
                                                            } else {
                                                                ConsoleWrapper.console.print('X');
                                                            }
                                                        }
                                                        ConsoleWrapper.console.printf("\t     -> Result: %s",
                                                                (raargi.getState() == OperationState.SUCCESS ? "ok"
                                                                        : "currupted"));
                                                        ConsoleWrapper.console.println();
                                                    } finally {
                                                        subProcessed[subIndex] = true;
                                                    }
                                                }
                                                break;
                                            }

                                        }
                                    }
                                } finally {
                                    processed[mainIndex] = true;
                                }
                            }
                            break;
                        default:
                            break;

                        }
                        Thread.sleep(Utility.FIVE_HUNDRED_MILLIS);
                    }
                }
                return showTasksResults(tasks);
            } else {
                ConsoleWrapper.console.println("Task list empty");
            }
        } else {
            ConsoleWrapper.console.println(taskCheckGenerations.getReason());
        }
        return Collections.emptyList();
    }

    public ResultActionArchiveItemsList execute(final ArchiveListOptions options) throws UnrecognizedToken_Exception,
            InvalidTask_Exception, InternalCoreResult_Exception, InvalidOptions_Exception, InternalServer_Exception {
        MessagesTemplate.printTestHeader(this.getClass().getName(), options);
        final Task task = this.sapi.listArchive(options);
        final ResultActionArchiveItemsList list = (ResultActionArchiveItemsList) TestUtility
                .waitForTask(this.sapi, task, true).getResult();
        ConsoleWrapper.console.println();
        ConsoleWrapper.console.println("LG  - Last Generation     -     LSG - Last Succeded Generation");
        ConsoleWrapper.console.println("type\t\t moRef \tuuid\t\t\t\t\tLG\tLG Date\t\t\t\tLSG\tLSG Date\t\t\tName");
        for (final ResultActionArchiveItem ss : list.getItems()) {

            final InfoData infoData = ss.getInfo();
            final String latestGenerationIdStr = ((infoData.getLatestGenerationId() > -1)
                    ? String.valueOf(infoData.getLatestGenerationId())
                    : "-");
            final String latestSucceededGenerationIdStr = ((infoData.getLatestSucceededGenerationId() > -1)
                    ? String.valueOf(infoData.getLatestSucceededGenerationId())
                    : "-");

            final String morefSt = ((infoData.getEntityType() == EntityType.IMPROVED_VIRTUAL_DISK)
                    || (infoData.getEntityType() == EntityType.K_8_S_NAMESPACE)) ? "-" : infoData.getMoref();
            ConsoleWrapper.console.printf("%s\t%14s\t%s\t%s\t%-24s\t%s\t%s \t%s",
                    TestUtility.entityTypeToString(infoData.getEntityType()), morefSt, infoData.getUuid(),
                    latestSucceededGenerationIdStr, infoData.getTimestampOfLatestSucceededGenerationId(),
                    latestGenerationIdStr, infoData.getTimestampOfLatestGenerationId(), infoData.getName());
            ConsoleWrapper.console.println();
        }
        return list;
    }

    public Collection<? extends ResultAction> execute(final ArchiveRemoveGenerationsOptions options)
            throws UnrecognizedToken_Exception, InvalidTask_Exception, InterruptedException, InternalServer_Exception {
        MessagesTemplate.printTestHeader(this.getClass().getName(), options);
        ConsoleWrapper.console.printf("Call to sapi.removeArchiveGenerations Synchronously\n");
        final List<ResultActionArchiveRemoveGenerationWithDependencies> result = this.sapi
                .removeArchiveGenerations(options);

        for (final ResultActionArchiveRemoveGenerationWithDependencies rab : result) {
            switch (rab.getState()) {
            case ABORTED:

            case FAILED:
            case SKIPPED:
            case STARTED:
            case QUEUED:
                ConsoleWrapper.console.printf("Check %s: %s (%s)\n", TestUtility.fcoToString(rab.getFcoEntityInfo()),
                        rab.getState().toString(), rab.getReason());
                break;
            case SUCCESS:
                ConsoleWrapper.console.printf("%s:\n", TestUtility.fcoToString(rab.getFcoEntityInfo()));
                ConsoleWrapper.console.printf("Removing %d generations\n", rab.getSubOperations().size());
                for (final Task task3 : rab.getSubOperations()) {
                    final ResultActionArchiveRemoveGeneration raargi = (ResultActionArchiveRemoveGeneration) this.sapi
                            .getTaskInfo(task3).getResult();
                    switch (raargi.getState()) {
                    case ABORTED:

                    case FAILED:
                    case SKIPPED:
                    case STARTED:
                    case QUEUED:
                        ConsoleWrapper.console.printf("Check %s: %s (%s)\n",
                                TestUtility.fcoToString(rab.getFcoEntityInfo()), rab.getState().toString(),
                                rab.getReason());
                        break;

                    case SUCCESS:
                        switch (raargi.getDependenciesInfo().getMode()) {
                        case FULL:
                            ConsoleWrapper.console.printf("Generation %2d(Full) ", raargi.getGenId());
                            break;
                        case INCREMENTAL:
                            ConsoleWrapper.console.printf("Generation %2d(Incremental) ", raargi.getGenId());
                            break;
                        case UNKNOW:
                            ConsoleWrapper.console.printf("Generation %2d(Unknow) ", raargi.getGenId());
                            break;
                        default:
                            break;
                        }
                        if (raargi.isGenerationDataRemoved()) {
                            ConsoleWrapper.console.println("Done");
                        } else {
                            ConsoleWrapper.console.println("Failed");
                        }
                        break;
                    }

                }
                break;
            default:
                break;
            }
            Thread.sleep(Utility.FIVE_HUNDRED_MILLIS);
        }

        return result;
    }

    public Collection<? extends ResultAction> execute(final ArchiveRemoveProfileOptions options)
            throws InvalidTask_Exception, UnrecognizedToken_Exception, InterruptedException, InternalServer_Exception {
        MessagesTemplate.printTestHeader(this.getClass().getName(), options);
        ConsoleWrapper.console.printf("Call to sapi.removeArchiveProfile Synchronously\n");
        final List<ResultActionArchiveRemovedProfile> result = this.sapi.removeArchiveProfile(options);
        for (final ResultActionArchiveRemovedProfile resultAction : result) {
            switch (resultAction.getState()) {
            case ABORTED:

            case FAILED:
            case SKIPPED:
            case STARTED:
            case QUEUED:
                ConsoleWrapper.console.printf("Check %s: %s (%s)\n",
                        TestUtility.fcoToString(resultAction.getFcoEntityInfo()), resultAction.getState().toString(),
                        resultAction.getReason());
                break;
            case SUCCESS:

                ConsoleWrapper.console.printf("Removing profile: %s\n",
                        TestUtility.fcoToString(resultAction.getFcoEntityInfo()));

                ConsoleWrapper.console.println("\t-> Archive content removed.");

                ConsoleWrapper.console.println("\t-> Profile metadata removed.");
                ConsoleWrapper.console.println("\t-> Fco Profile Catalog updated.");

                break;
            default:
                break;
            }
            Thread.sleep(Utility.FIVE_HUNDRED_MILLIS);
        }

        return result;
    }

    /**
     * @param acShow
     * @throws UnrecognizedToken_Exception
     * @throws InvalidTask_Exception
     * @throws InterruptedException
     * @throws InternalServer_Exception
     * @throws Exception
     */
    public Collection<? extends ResultAction> execute(final ArchiveShowOptions options)
            throws UnrecognizedToken_Exception, InvalidTask_Exception, InterruptedException, InternalServer_Exception {
        MessagesTemplate.printTestHeader(this.getClass().getName(), options);
        ConsoleWrapper.console.printf("Call to sapi.showArchive Synchronously\n");
        final List<ResultActionArchiveShow> result = this.sapi.showArchive(options);
        for (final ResultActionArchiveShow resultAction : result) {
            switch (resultAction.getState()) {
            case ABORTED:

            case FAILED:
            case SKIPPED:
            case QUEUED:
            case STARTED:
                ConsoleWrapper.console.printf("Show %s: Request %s - State: %s (%s)\n",
                        TestUtility.fcoToString(resultAction.getFcoEntityInfo()),
                        resultAction.getArchiveObject().toString(), resultAction.getState().toString(),
                        resultAction.getReason());

                break;
            case SUCCESS:
                ConsoleWrapper.console.println(TestUtility.fcoToString(resultAction.getFcoEntityInfo()));
                ConsoleWrapper.console.print("Request : ");
                ConsoleWrapper.console.println(resultAction.getArchiveObject().toString());
                ConsoleWrapper.console.println(resultAction.getContent());

                break;
            default:
                break;
            }
            Thread.sleep(Utility.FIVE_HUNDRED_MILLIS);
        }
        return result;
    }

    public Collection<? extends ResultAction> execute(final ArchiveStatusOptions options)
            throws InvalidTask_Exception, UnrecognizedToken_Exception, InterruptedException, InternalServer_Exception {

        MessagesTemplate.printTestHeader(this.getClass().getName(), options);
        ConsoleWrapper.console.printf("Call to sapi.statusArchive Synchronously\n");
        final List<ResultActionArchiveStatus> result = this.sapi.statusArchive(options);
        for (final ResultActionArchiveStatus rab : result) {
            switch (rab.getState()) {
            case ABORTED:

            case FAILED:
            case SKIPPED:
            case STARTED:
            case QUEUED:
                ConsoleWrapper.console.printf("Check %s: %s (%s)\n", TestUtility.fcoToString(rab.getFcoEntityInfo()),
                        rab.getState().toString(), rab.getReason());
                break;
            case SUCCESS:
                ConsoleWrapper.console.printf("%s:\n", TestUtility.fcoToString(rab.getFcoEntityInfo()));

                if (rab.getNumOfSuccceededGeneration() >= 0) {
                    ConsoleWrapper.console.printf("Generation(s):%d/%d\n", rab.getNumOfSuccceededGeneration(),
                            rab.getNumOfGeneration());
                } else {
                    ConsoleWrapper.console.print("No succedded generation");
                }
                ConsoleWrapper.console.println();
                if (rab.isEmpty()) {
                    ConsoleWrapper.console.println("\tArchive is empty. ");
                }

                ConsoleWrapper.console.print("\tGen\tDate\t\t\t");
                switch (rab.getFcoEntityInfo().getEntityType()) {
                case VIRTUAL_MACHINE:
                    for (int i = 0; i < 3; ++i) {
                        ConsoleWrapper.console.print("\tn device  size(GB)  mode  Exec Time");
                    }
                    ConsoleWrapper.console.println("\t...");
                    break;
                case VIRTUAL_APP:
                    for (int i = 0; i < 3; ++i) {
                        ConsoleWrapper.console.print("\tn:Virtual Machine\t\t\t\t\t\t\t");
                    }
                    ConsoleWrapper.console.println("\t...");
                    break;
                case IMPROVED_VIRTUAL_DISK:
                    ConsoleWrapper.console.println("\tsize(GB)  mode  Exec Time");
                    break;
                default:
                    break;
                }
                for (int id = 0; id < rab.getNumOfGeneration(); id++) {

                    switch (rab.getFcoEntityInfo().getEntityType()) {
                    case VIRTUAL_MACHINE:
                        ConsoleWrapper.console.print(getGenerationStatusString((ResultActionArchiveVmStatus) rab, id));
                        break;
                    case VIRTUAL_APP:
                        ConsoleWrapper.console
                                .print(getGenerationStatusString((ResultActionArchiveVappStatus) rab, id));
                        break;
                    case IMPROVED_VIRTUAL_DISK:
                        ConsoleWrapper.console.print(getGenerationStatusString((ResultActionArchiveIvdStatus) rab, id));
                        break;
                    default:
                        break;
                    }
                    ConsoleWrapper.console.println();
                }

                break;
            default:
                break;

            }
            Thread.sleep(Utility.FIVE_HUNDRED_MILLIS);
        }
        return result;
    }

    public Collection<? extends ResultAction> executeAsync(final ArchiveRemoveGenerationsOptions options)
            throws UnrecognizedToken_Exception, InvalidTask_Exception, InterruptedException, InternalServer_Exception {

        ResultActionArchiveRemoveGenerationWithDependencies rab = null;
        MessagesTemplate.printTestHeader(this.getClass().getName(), options);
        ConsoleWrapper.console.printf("Call to sapi.removeArchiveGenerationsAsync Asynchronously\n");
        final Tasks taskRemoveGenerations = this.sapi.removeArchiveGenerationsAsync(options);
        final List<Task> tasks = taskRemoveGenerations.getTaskList();
        if (taskRemoveGenerations.getState() == OperationState.SUCCESS) {
            Task tb = null;
            if (!tasks.isEmpty()) {
                boolean tasksDone = false;
                final boolean[] processed = new boolean[tasks.size()];
                while (!tasksDone) {
                    for (int index = 0; index < tasks.size(); index++) {
                        tasksDone = areTasksDone(processed);
                        if (tasksDone) {
                            break;
                        }
                        tb = tasks.get(index);
                        rab = (ResultActionArchiveRemoveGenerationWithDependencies) this.sapi.getTaskInfo(tb)
                                .getResult();
                        switch (rab.getState()) {
                        case ABORTED:

                        case FAILED:
                        case SKIPPED:
                            if (!processed[index]) {
                                ConsoleWrapper.console.printf("Check %s: %s (%s)\n",
                                        TestUtility.fcoToString(rab.getFcoEntityInfo()), rab.getState().toString(),
                                        rab.getReason());
                                processed[index] = true;
                            }
                            break;
                        case QUEUED:
                            break;
                        case SUCCESS:
                        case STARTED:
                            if (!processed[index]) {
                                try {
                                    ConsoleWrapper.console.printf("%s:\n",
                                            TestUtility.fcoToString(rab.getFcoEntityInfo()));

                                    rab = waitForPhases(tb, RemoveGenerationsPhases.START_DELETE_GENERATIONS, true);
                                    ConsoleWrapper.console.printf("Removing %d generations\n",
                                            rab.getSubOperations().size());
                                    while (rab.getSubOperations().isEmpty()) {
                                        rab = (ResultActionArchiveRemoveGenerationWithDependencies) this.sapi
                                                .getTaskInfo(tb).getResult();
                                        ConsoleWrapper.console.println("Zero");
                                        Thread.sleep(300);
                                    }
                                    boolean subTasksDone = false;
                                    final boolean[] subProcessed = new boolean[rab.getSubOperations().size()];
                                    while (!subTasksDone) {
                                        for (int index1 = 0; index1 < rab.getSubOperations().size(); index1++) {
                                            subTasksDone = areTasksDone(subProcessed);
                                            if (subTasksDone) {
                                                break;
                                            }
                                            final Task task3 = rab.getSubOperations().get(index1);
                                            ResultActionArchiveRemoveGeneration raargi = (ResultActionArchiveRemoveGeneration) this.sapi
                                                    .getTaskInfo(task3).getResult();
                                            switch (raargi.getState()) {
                                            case ABORTED:

                                            case FAILED:
                                            case SKIPPED:
                                                subProcessed[index1] = true;
                                                break;
                                            case QUEUED:
                                                break;
                                            case STARTED:
                                            case SUCCESS:
                                                if (!subProcessed[index1]) {
                                                    try {
                                                        raargi = (ResultActionArchiveRemoveGeneration) TestUtility
                                                                .waitForTask(this.sapi, task3, true).getResult();
                                                        switch (raargi.getDependenciesInfo().getMode()) {
                                                        case FULL:
                                                            ConsoleWrapper.console.printf("Generation %2d(Full) ",
                                                                    raargi.getGenId());
                                                            break;
                                                        case INCREMENTAL:
                                                            ConsoleWrapper.console.printf(
                                                                    "Generation %2d(Incremental) ", raargi.getGenId());
                                                            break;
                                                        case UNKNOW:
                                                            ConsoleWrapper.console.printf("Generation %2d(Unknow) ",
                                                                    raargi.getGenId());
                                                            break;
                                                        default:
                                                            break;

                                                        }

                                                        if (raargi.isGenerationDataRemoved()) {
                                                            ConsoleWrapper.console.println("Done");
                                                        } else {
                                                            ConsoleWrapper.console.println("Failed");
                                                        }
                                                    } finally {
                                                        subProcessed[index1] = true;
                                                    }
                                                }
                                                break;
                                            }

                                        }
                                    }
                                } finally {
                                    processed[index] = true;
                                }
                            }
                            break;
                        default:
                            break;

                        }
                        Thread.sleep(Utility.FIVE_HUNDRED_MILLIS);
                    }
                }
            } else {
                ConsoleWrapper.console.println("Task list empty");
            }
        } else {
            ConsoleWrapper.console.println(taskRemoveGenerations.getReason());
        }
        return showTasksResults(tasks);
    }

    public List<ResultAction> executeAsync(final ArchiveRemoveProfileOptions options)
            throws InvalidTask_Exception, UnrecognizedToken_Exception, InterruptedException, InternalServer_Exception {
        ResultActionArchiveRemovedProfile resultAction = null;
        MessagesTemplate.printTestHeader(this.getClass().getName(), options);
        ConsoleWrapper.console.printf("Call to sapi.removeArchiveProfile Asynchronously\n");
        final Tasks taskRemoveProfiles = this.sapi.removeArchiveProfileAsync(options);
        final List<Task> tasks = taskRemoveProfiles.getTaskList();
        if (taskRemoveProfiles.getState() == OperationState.SUCCESS) {

            Task tb = null;
            if (!tasks.isEmpty()) {
                boolean tasksDone = false;
                final boolean[] processed = new boolean[tasks.size()];
                while (!tasksDone) {
                    for (int index = 0; index < tasks.size(); index++) {
                        tasksDone = areTasksDone(processed);
                        if (tasksDone) {
                            break;
                        }
                        tb = tasks.get(index);
                        resultAction = (ResultActionArchiveRemovedProfile) this.sapi.getTaskInfo(tb).getResult();
                        switch (resultAction.getState()) {
                        case ABORTED:

                        case FAILED:
                        case SKIPPED:
                            if (!processed[index]) {
                                ConsoleWrapper.console.printf("Check %s: %s (%s)\n",
                                        TestUtility.fcoToString(resultAction.getFcoEntityInfo()),
                                        resultAction.getState().toString(), resultAction.getReason());
                                processed[index] = true;
                            }
                            break;
                        case QUEUED:
                            break;
                        case SUCCESS:
                        case STARTED:
                            if (!processed[index]) {
                                try {
                                    ConsoleWrapper.console.printf("Removing profile: %s\n",
                                            TestUtility.fcoToString(resultAction.getFcoEntityInfo()));

                                    resultAction = waitForPhases(tb, RemoveProfilePhases.END_REMOVE_FCO_PROFILE_CONTENT,
                                            true);
                                    ConsoleWrapper.console.println("\t-> Archive content removed.");

                                    resultAction = waitForPhases(tb,
                                            RemoveProfilePhases.END_REMOVE_FCO_PROFILE_METADATA, true);

                                    ConsoleWrapper.console.println("\t-> Profile metadata removed.");
                                    resultAction = waitForPhases(tb,
                                            RemoveProfilePhases.END_UPDATE_FCO_PROFILES_CATALOG, true);
                                    ConsoleWrapper.console.println("\t-> Fco Profile Catalog updated.");
                                } finally {
                                    processed[index] = true;
                                }
                            }
                            break;
                        default:
                            break;
                        }
                        Thread.sleep(Utility.FIVE_HUNDRED_MILLIS);
                    }
                }
            } else {
                ConsoleWrapper.console.println("Task list empty");
            }
        } else {
            ConsoleWrapper.console.println(taskRemoveProfiles.getReason());
        }
        return showTasksResults(tasks);
    }

    public List<ResultAction> executeAsync(final ArchiveShowOptions options)
            throws UnrecognizedToken_Exception, InvalidTask_Exception, InterruptedException, InternalServer_Exception {
        ResultActionArchiveShow resultAction = null;
        MessagesTemplate.printTestHeader(this.getClass().getName(), options);
        ConsoleWrapper.console.printf("Call to sapi.showArchive Asynchronously\n");
        final Tasks taskShow = this.sapi.showArchiveAsync(options);
        final List<Task> tasks = taskShow.getTaskList();
        if (taskShow.getState() == OperationState.SUCCESS) {
            Task tb = null;
            if (!tasks.isEmpty()) {
                boolean tasksDone = false;
                final boolean[] processed = new boolean[tasks.size()];
                while (!tasksDone) {
                    for (int index = 0; index < tasks.size(); index++) {
                        tasksDone = areTasksDone(processed);
                        if (tasksDone) {
                            break;
                        }
                        tb = tasks.get(index);
                        resultAction = (ResultActionArchiveShow) this.sapi.getTaskInfo(tb).getResult();
                        switch (resultAction.getState()) {
                        case ABORTED:

                        case FAILED:
                        case SKIPPED:
                            if (!processed[index]) {
                                ConsoleWrapper.console.printf("Show %s: Request %s - State: %s (%s)\n",
                                        TestUtility.fcoToString(resultAction.getFcoEntityInfo()),
                                        resultAction.getArchiveObject().toString(), resultAction.getState().toString(),
                                        resultAction.getReason());
                                processed[index] = true;
                            }
                            break;
                        case QUEUED:
                        case STARTED:
                            break;
                        case SUCCESS:
                            if (!processed[index]) {
                                try {
                                    ConsoleWrapper.console
                                            .println(TestUtility.fcoToString(resultAction.getFcoEntityInfo()));
                                    ConsoleWrapper.console.print("Request : ");
                                    ConsoleWrapper.console.println(resultAction.getArchiveObject().toString());
                                    ConsoleWrapper.console.println(resultAction.getContent());
                                } finally {
                                    processed[index] = true;
                                }
                            }
                            break;
                        default:
                            break;
                        }
                        Thread.sleep(Utility.FIVE_HUNDRED_MILLIS);
                    }
                }
            } else {
                ConsoleWrapper.console.println("Task list empty");
            }
        } else {
            ConsoleWrapper.console.println(taskShow.getReason());
        }
        return showTasksResults(tasks);
    }

    public List<ResultAction> executeAsync(final ArchiveStatusOptions options)
            throws InvalidTask_Exception, UnrecognizedToken_Exception, InterruptedException, InternalServer_Exception {
        ResultActionArchiveStatus rab = null;
        MessagesTemplate.printTestHeader(this.getClass().getName(), options);
        ConsoleWrapper.console.printf("Call to sapi.statusArchive Asynchronously\n");
        final Tasks taskRemoveGenerations = this.sapi.statusArchiveAsync(options);
        final List<Task> tasks = taskRemoveGenerations.getTaskList();
        if (taskRemoveGenerations.getState() == OperationState.SUCCESS) {
            Task tb = null;
            if (!tasks.isEmpty()) {
                boolean tasksDone = false;
                final boolean[] processed = new boolean[tasks.size()];
                while (!tasksDone) {
                    for (int index = 0; index < tasks.size(); index++) {
                        tasksDone = areTasksDone(processed);
                        if (tasksDone) {
                            break;
                        }
                        tb = tasks.get(index);
                        rab = (ResultActionArchiveStatus) this.sapi.getTaskInfo(tb).getResult();
                        switch (rab.getState()) {
                        case ABORTED:

                        case FAILED:
                        case SKIPPED:
                            if (!processed[index]) {
                                ConsoleWrapper.console.printf("Check %s: %s (%s)\n",
                                        TestUtility.fcoToString(rab.getFcoEntityInfo()), rab.getState().toString(),
                                        rab.getReason());
                                processed[index] = true;
                            }
                            break;
                        case QUEUED:
                            break;
                        case SUCCESS:
                        case STARTED:
                            if (!processed[index]) {
                                try {
                                    ConsoleWrapper.console.printf("%s:\n",
                                            TestUtility.fcoToString(rab.getFcoEntityInfo()));
                                    rab = waitForPhases(tb, StatusProfilePhases.END_RETRIEVE_GENERATIONS, true);
                                    if (rab.getNumOfSuccceededGeneration() >= 0) {
                                        ConsoleWrapper.console.printf("Generation(s):%d/%d\n",
                                                rab.getNumOfSuccceededGeneration(), rab.getNumOfGeneration());
                                    } else {
                                        ConsoleWrapper.console.print("No succedded generation");
                                    }
                                    ConsoleWrapper.console.println();
                                    if (rab.isEmpty()) {
                                        ConsoleWrapper.console.println("\tArchive is empty. ");
                                    }

                                    rab = waitForPhases(tb, StatusProfilePhases.END_RETRIEVE_GENERATIONS_INFO, true);
                                    ConsoleWrapper.console.print("\tGen\tDate\t\t\t");
                                    switch (rab.getFcoEntityInfo().getEntityType()) {
                                    case VIRTUAL_MACHINE:
                                        for (int i = 0; i < 3; ++i) {
                                            ConsoleWrapper.console.print("\tn device  size(GB)  mode  Exec Time");
                                        }
                                        ConsoleWrapper.console.println("\t...");
                                        break;
                                    case VIRTUAL_APP:
                                        for (int i = 0; i < 3; ++i) {
                                            ConsoleWrapper.console.print("\tn:Virtual Machine\t\t\t\t\t\t\t");
                                        }
                                        ConsoleWrapper.console.println("\t...");
                                        break;
                                    case IMPROVED_VIRTUAL_DISK:
                                        ConsoleWrapper.console.println("\tsize(GB)  mode  Exec Time");
                                        break;
                                    default:
                                        break;
                                    }
                                    for (int id = 0; id < rab.getNumOfGeneration(); id++) {

                                        switch (rab.getFcoEntityInfo().getEntityType()) {
                                        case VIRTUAL_MACHINE:
                                            ConsoleWrapper.console.print(
                                                    getGenerationStatusString((ResultActionArchiveVmStatus) rab, id));
                                            break;
                                        case VIRTUAL_APP:
                                            ConsoleWrapper.console.print(
                                                    getGenerationStatusString((ResultActionArchiveVappStatus) rab, id));
                                            break;
                                        case IMPROVED_VIRTUAL_DISK:
                                            ConsoleWrapper.console.print(
                                                    getGenerationStatusString((ResultActionArchiveIvdStatus) rab, id));
                                            break;
                                        default:
                                            break;
                                        }
                                        ConsoleWrapper.console.println();
                                    }
                                } finally {
                                    processed[index] = true;
                                }
                            }
                            break;
                        default:
                            break;

                        }
                        Thread.sleep(Utility.FIVE_HUNDRED_MILLIS);
                    }
                }
            } else {
                ConsoleWrapper.console.println("Task list empty");
            }
        } else {
            ConsoleWrapper.console.println(taskRemoveGenerations.getReason());
        }
        return showTasksResults(tasks);
    }

    public String getGenerationStatusString(final ResultActionArchiveIvdStatus resultAction, final Integer index) {
        final GenerationDiskInfo genDiskInfo = resultAction.getGenerationDiskInfo().get(index);

        final int genId = genDiskInfo.getGenId();
        final StringBuilder sb = new StringBuilder();
        if (genId == resultAction.getLatestSucceededGenerationId()) {
            sb.append("     (*)");
        } else {
            sb.append(" \t");
        }
        sb.append(String.format("%d \t%s\t", genId, TestUtility.toGMTString(genDiskInfo.getTimeStamp())));
        if (genDiskInfo.isGenerationSucceeded()) {
            try {
                final StatusDiskInfo diskInfo = genDiskInfo.getDisksInfo();
                final long millis = genDiskInfo.getDisksInfo().getDumpElapsedTimeMs();
                final String timeElapse = new SimpleDateFormat("mm:ss:SSS").format(new Date(millis));
                sb.append(
                        String.format(Utility.LOCALE, "%-7.2f   %s  %s", diskInfo.getCapacity() / (Utility.ONE_GBYTES),
                                TestUtility.backupModeToString(diskInfo.getBackupMode()), timeElapse));
            } catch (final Exception e) {
                return String.format("%d ##########_ERROR_##########", genId);
            }
        } else {
            sb.append(" --------_FAILED_--------");
        }
        return sb.toString();
    }

    public String getGenerationStatusString(final ResultActionArchiveVappStatus resultAction, final int index) {
        final GenerationVirtualMachinesInfoList genVmsInfo = resultAction.getGenerationVmInfoList().get(index);
        final int genId = genVmsInfo.getGenId();
        final StringBuilder sb = new StringBuilder();
        if (genId == resultAction.getLatestSucceededGenerationId()) {
            sb.append("     (*)");
        } else {
            sb.append(" \t");
        }
        sb.append(String.format("%d \t%s\t", genId, TestUtility.toGMTString(genVmsInfo.getTimeStamp())));

        if (genVmsInfo.isGenerationSucceeded()) {
            try {
                int i = 0;
                for (final ManagedFcoEntityInfo entity : genVmsInfo.getVmsInfoList()) {
                    ++i;
                    sb.append(String.format("%d:%s\t", i, TestUtility.fcoToString(entity)));
                }
            } catch (final Exception e) {
                return String.format("%d ##########_ERROR_##########", genId);
            }
        } else {
            sb.append(" ----------_FAILED_----------");
        }
        return sb.toString();
    }

    public String getGenerationStatusString(final ResultActionArchiveVmStatus resultAction, final int index) {

        final GenerationDisksInfoList genDisksInfo = resultAction.getGenerationDisksInfoList().get(index);
        final Integer genId = genDisksInfo.getGenId();
        final StringBuilder sb = new StringBuilder();
        if (genId == resultAction.getLatestSucceededGenerationId()) {
            sb.append("     (*)");
        } else {
            sb.append(" \t");
        }
        sb.append(String.format("%d \t%s\t", genId, TestUtility.toGMTString(genDisksInfo.getTimeStamp())));

        if (genDisksInfo.isGenerationSucceeded()) {
            try {
                for (final StatusVirtualMachineDiskInfo diskInfo : genDisksInfo.getDisksInfoList()) {
                    final long millis = diskInfo.getDumpElapsedTimeMs();
                    final String timeElapse = new SimpleDateFormat("mm:ss:SSS").format(new Date(millis));
                    sb.append(String.format(Utility.LOCALE, "%d %s%s:%s %-7.2f   %s  %s\t", diskInfo.getDiskId(),
                            diskInfo.getAdapterType(), diskInfo.getBusNumber(), diskInfo.getUnitNumber(),
                            diskInfo.getCapacity() / (Utility.ONE_GBYTES),
                            TestUtility.backupModeToString(diskInfo.getBackupMode()), timeElapse));
                }

            } catch (final Exception e) {
                return String.format("%d ##########_ERROR_##########", genId);
            }
        } else {
            sb.append(" ----------_FAILED_----------");
        }
        return sb.toString();
    }

    public List<ResultAction> testShow(final FcoTarget target)
            throws UnrecognizedToken_Exception, InvalidTask_Exception, InterruptedException, InternalServer_Exception {
        final List<FcoTarget> list = new LinkedList<>();
        list.add(target);
        return testShow(list);
    }

    public List<ResultAction> testShow(final List<FcoTarget> targetList)
            throws UnrecognizedToken_Exception, InvalidTask_Exception, InterruptedException, InternalServer_Exception {
        final List<ResultAction> resultAction = new LinkedList<>();
        ConsoleWrapper.console.println();
        ConsoleWrapper.console.println(LINESEPARATOR);
        ConsoleWrapper.console.println(AbstractTest.LINESEPARATOR);
        ConsoleWrapper.console
                .print("************************************************************************\t\tTEST SHOW - ");
        for (final FcoTarget fco : targetList) {
            ConsoleWrapper.console.printf(" %s:%s ", fco.getKeyType().toString(), fco.getKey());
        }
        ConsoleWrapper.console
                .println("\t\t**********************************************************************************");
        ConsoleWrapper.console.println();
        ConsoleWrapper.console.println(AbstractTest.LINESEPARATOR);
        ConsoleWrapper.console.println(AbstractTest.LINESEPARATOR);
        ConsoleWrapper.console.println();
        final ArchiveShowOptions acShow = new ArchiveShowOptions();
        acShow.setFilter(GenerationsFilter.LAST);
        acShow.setPrettyJason(true);
        acShow.getTargetList().addAll(targetList);
        acShow.setArchiveObject(ArchiveObjects.FCOPROFILE);
        resultAction.addAll(execute(acShow));
        acShow.setArchiveObject(ArchiveObjects.GENERATIONPROFILE);

        resultAction.addAll(execute(acShow));
        acShow.setArchiveObject(ArchiveObjects.REPORTFILE);
        resultAction.addAll(execute(acShow));
        acShow.setArchiveObject(ArchiveObjects.MD_5_FILE);
        resultAction.addAll(execute(acShow));
        acShow.setArchiveObject(ArchiveObjects.VMXFILE);
        resultAction.addAll(execute(acShow));

        return resultAction;
    }

    private ResultActionArchiveCheckGenerationWithDependencies waitForPhases(final Task childTask,
            final CheckGenerationsPhases waitingPhase, final boolean visible)
            throws InterruptedException, UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
        ConsoleWrapper.console.printf("Wait for phase: %s", waitingPhase.toString());
        if (visible) {
            ConsoleWrapper.console.printf(": [ ");
        }
        ResultActionArchiveCheckGenerationWithDependencies rabc = (ResultActionArchiveCheckGenerationWithDependencies) this.sapi
                .getTaskInfo(childTask).getResult();
        String previous = "";
        while ((rabc.getPhase().ordinal() < waitingPhase.ordinal())) {
            Thread.sleep(Utility.ONE_HUNDRED_MS);
            rabc = (ResultActionArchiveCheckGenerationWithDependencies) this.sapi.getTaskInfo(childTask).getResult();
            if (rabc.isDone()) {
                break;
            }
            if (!previous.equals(rabc.getPhase().toString())) {
                previous = rabc.getPhase().toString();
                if (visible) {
                    ConsoleWrapper.console.print(previous);
                    ConsoleWrapper.console.print(" - ");
                }
            }
        }
        if (visible) {
            ConsoleWrapper.console.print(rabc.getPhase().toString());
            ConsoleWrapper.console.print(" ] ");
        }
        ConsoleWrapper.console.println(" - Done");
        rabc = (ResultActionArchiveCheckGenerationWithDependencies) this.sapi.getTaskInfo(childTask).getResult();
        return rabc;
    }

    private ResultActionArchiveRemoveGenerationWithDependencies waitForPhases(final Task childTask,
            final RemoveGenerationsPhases waitingPhase, final boolean visible)
            throws InterruptedException, UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
        ConsoleWrapper.console.printf("Wait for phase: %s", waitingPhase.toString());
        if (visible) {
            ConsoleWrapper.console.printf(": [ ");
        }
        ResultActionArchiveRemoveGenerationWithDependencies rabc = (ResultActionArchiveRemoveGenerationWithDependencies) this.sapi
                .getTaskInfo(childTask).getResult();
        String previous = "";
        while ((rabc.getPhase().ordinal() < waitingPhase.ordinal())) {
            Thread.sleep(Utility.ONE_HUNDRED_MS);
            rabc = (ResultActionArchiveRemoveGenerationWithDependencies) this.sapi.getTaskInfo(childTask).getResult();
            if (rabc.isDone()) {
                break;
            }
            if (!previous.equals(rabc.getPhase().toString())) {
                previous = rabc.getPhase().toString();
                if (visible) {
                    ConsoleWrapper.console.print(previous);
                    ConsoleWrapper.console.print(" - ");
                }
            }
        }
        if (visible) {
            ConsoleWrapper.console.print(rabc.getPhase().toString());
            ConsoleWrapper.console.print(" ] ");
        }
        ConsoleWrapper.console.println(" - Done");
        rabc = (ResultActionArchiveRemoveGenerationWithDependencies) this.sapi.getTaskInfo(childTask).getResult();
        return rabc;
    }

    private ResultActionArchiveRemovedProfile waitForPhases(final Task childTask,
            final RemoveProfilePhases waitingPhase, final boolean visible)
            throws InterruptedException, UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
        ConsoleWrapper.console.printf("Wait for phase: %s", waitingPhase.toString());
        if (visible) {
            ConsoleWrapper.console.printf(": [ ");
        }
        ResultActionArchiveRemovedProfile rabc = (ResultActionArchiveRemovedProfile) this.sapi.getTaskInfo(childTask)
                .getResult();
        String previous = "";
        while ((rabc.getPhase().ordinal() < waitingPhase.ordinal())) {
            Thread.sleep(Utility.ONE_HUNDRED_MS);
            rabc = (ResultActionArchiveRemovedProfile) this.sapi.getTaskInfo(childTask).getResult();
            if (rabc.isDone()) {
                break;
            }
            if (!previous.equals(rabc.getPhase().toString())) {
                previous = rabc.getPhase().toString();
                if (visible) {
                    ConsoleWrapper.console.print(previous);
                    ConsoleWrapper.console.print(" - ");
                }
            }
        }
        if (visible) {
            ConsoleWrapper.console.print(rabc.getPhase().toString());
            ConsoleWrapper.console.print(" ] ");
        }
        ConsoleWrapper.console.println(" - Done");
        rabc = (ResultActionArchiveRemovedProfile) this.sapi.getTaskInfo(childTask).getResult();
        return rabc;
    }

    private ResultActionArchiveStatus waitForPhases(final Task childTask, final StatusProfilePhases waitingPhase,
            final boolean visible)
            throws InterruptedException, UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
        ConsoleWrapper.console.printf("Wait for phase: %s", waitingPhase.toString());
        if (visible) {
            ConsoleWrapper.console.printf(": [ ");
        }
        ResultActionArchiveStatus rabc = (ResultActionArchiveStatus) this.sapi.getTaskInfo(childTask).getResult();
        String previous = "";
        while ((rabc.getPhase().ordinal() < waitingPhase.ordinal())) {
            Thread.sleep(Utility.ONE_HUNDRED_MS);
            rabc = (ResultActionArchiveStatus) this.sapi.getTaskInfo(childTask).getResult();
            if (rabc.isDone()) {
                break;
            }
            if (!previous.equals(rabc.getPhase().toString())) {
                previous = rabc.getPhase().toString();
                if (visible) {
                    ConsoleWrapper.console.print(previous);
                    ConsoleWrapper.console.print(" - ");
                }
            }
        }
        if (visible) {
            ConsoleWrapper.console.print(rabc.getPhase().toString());
            ConsoleWrapper.console.print(" ] ");
        }
        ConsoleWrapper.console.println(" - Done");
        rabc = (ResultActionArchiveStatus) this.sapi.getTaskInfo(childTask).getResult();
        return rabc;
    }

}
