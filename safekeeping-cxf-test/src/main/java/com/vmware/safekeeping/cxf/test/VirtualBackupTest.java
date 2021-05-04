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

import java.util.Collection;
import java.util.List;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.cxf.test.common.ConsoleWrapper;
import com.vmware.safekeeping.cxf.test.common.TestUtility;
import com.vmware.sapi.AbstractResultActionVirtualBackup;
import com.vmware.sapi.InternalServer_Exception;
import com.vmware.sapi.InvalidTask_Exception;
import com.vmware.sapi.ManagedFcoEntityInfo;
import com.vmware.sapi.OperationState;
import com.vmware.sapi.ResultAction;
import com.vmware.sapi.ResultActionDiskVirtualBackup;
import com.vmware.sapi.ResultActionIvdVirtualBackup;
import com.vmware.sapi.ResultActionVappVirtualBackup;
import com.vmware.sapi.ResultActionVmVirtualBackup;
import com.vmware.sapi.Sapi;
import com.vmware.sapi.Task;
import com.vmware.sapi.TaskResult;
import com.vmware.sapi.Tasks;
import com.vmware.sapi.UnrecognizedToken_Exception;
import com.vmware.sapi.VirtualBackupDiskPhases;
import com.vmware.sapi.VirtualBackupOptions;
import com.vmware.sapi.VirtualBackupPhases;
import com.vmware.sapi.VirtualBackupVappPhases;

public class VirtualBackupTest extends AbstractTest {

    public VirtualBackupTest(final Sapi sapi) {
        super(sapi);
    }

    public Collection<? extends ResultAction> execute(final VirtualBackupOptions options, final boolean showDisksTask,
            final boolean showDiskDetails)
            throws InvalidTask_Exception, UnrecognizedToken_Exception, InterruptedException, InternalServer_Exception {
        MessagesTemplate.printTestHeader(this.getClass().getName(), options);
        ConsoleWrapper.console.printf("Call to sapi.consolidate Synchronously\n");
        final List<AbstractResultActionVirtualBackup> result = this.sapi.virtualBackup(options);
        for (final AbstractResultActionVirtualBackup rab : result) {
            switch (rab.getState()) {
            case ABORTED:

            case FAILED:
            case SKIPPED:
                ConsoleWrapper.console.printf("VirtualBackup %s: %s (%s)\n",
                        TestUtility.fcoToString(rab.getFcoEntityInfo()), rab.getState().toString(), rab.getReason());
                break;
            case QUEUED:
                break;
            case SUCCESS:
            case STARTED:
                ConsoleWrapper.console.println();
                ConsoleWrapper.console.println(AbstractTest.LINESEPARATOR);
                ConsoleWrapper.console.printf("VirtualBackup of %s %s.",
                        TestUtility.fcoToString(rab.getFcoEntityInfo()), rab.getState().toString());
                ConsoleWrapper.console.println();
                switch (rab.getFcoEntityInfo().getEntityType()) {

                case IMPROVED_VIRTUAL_DISK:
                    showImprovedVirtualDiskTask(rab.getTask(), showDisksTask, showDiskDetails);
                    break;
                case K_8_S_NAMESPACE:
                    break;
                case VIRTUAL_APP:
                    showVirtualApplianceTask(rab.getTask(), showDisksTask, showDiskDetails);
                    break;
                case VIRTUAL_MACHINE:
                    showVirtualMachineTask(rab.getTask(), showDisksTask, showDiskDetails);
                    break;
                default:
                    break;

                }
                break;
            default:
                break;

            }
            Thread.sleep(Utility.FIVE_HUNDRED_MILLIS);
        }
        return result;
    }

    public List<ResultAction> executeAsync(final VirtualBackupOptions options, final boolean showDisksTask,
            final boolean showDiskDetails)
            throws InvalidTask_Exception, UnrecognizedToken_Exception, InterruptedException, InternalServer_Exception {
        ResultAction rab = null;
        MessagesTemplate.printTestHeader(this.getClass().getName(), options);
        ConsoleWrapper.console.printf("Call to sapi.consolidate Asynchronously\n");
        final Tasks taskConolidates = this.sapi.virtualBackupAsync(options);
        final List<Task> tasks = taskConolidates.getTaskList();
        if (taskConolidates.getState() == OperationState.SUCCESS) {
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
                        rab = this.sapi.getTaskInfo(tb).getResult();
                        switch (rab.getState()) {
                        case ABORTED:

                        case FAILED:
                        case SKIPPED:
                            if (!processed[index]) {
                                ConsoleWrapper.console.printf("VirtualBackup %s: %s (%s)\n",
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
                                    ConsoleWrapper.console.println();
                                    ConsoleWrapper.console.println(AbstractTest.LINESEPARATOR);
                                    ConsoleWrapper.console.printf("VirtualBackup of %s %s.",
                                            TestUtility.fcoToString(rab.getFcoEntityInfo()), rab.getState().toString());
                                    ConsoleWrapper.console.println();
                                    switch (rab.getFcoEntityInfo().getEntityType()) {

                                    case IMPROVED_VIRTUAL_DISK:
                                        showImprovedVirtualDiskTask(tb, showDisksTask, showDiskDetails);
                                        break;
                                    case K_8_S_NAMESPACE:
                                        break;
                                    case VIRTUAL_APP:
                                        showVirtualApplianceTask(tb, showDisksTask, showDiskDetails);
                                        break;
                                    case VIRTUAL_MACHINE:
                                        showVirtualMachineTask(tb, showDisksTask, showDiskDetails);
                                        break;
                                    default:
                                        break;

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
            ConsoleWrapper.console.println(taskConolidates.getReason());
        }
        return showTasksResults(tasks);
    }

    private boolean showImprovedVirtualDiskTask(final Task task, final boolean showDisksTask,
            final boolean showDiskDetails)
            throws UnrecognizedToken_Exception, InterruptedException, InvalidTask_Exception, InternalServer_Exception {
        final TaskResult taskChildRab = this.sapi.getTaskInfo(task);

        ResultActionIvdVirtualBackup rabc = (ResultActionIvdVirtualBackup) taskChildRab.getResult();
        ConsoleWrapper.console.println(AbstractTest.LINESEPARATOR);
        ConsoleWrapper.console.printf("(Backup of %s %s.", TestUtility.fcoToString(rabc.getFcoEntityInfo()),
                rabc.getState().toString());
        ConsoleWrapper.console.println();

        rabc = waitForIvdPhase(task, VirtualBackupPhases.START_DISKS_CONSOLIDATE);
        ConsoleWrapper.console.println(MessagesTemplate.getLocationString(rabc));
        ConsoleWrapper.console.println(MessagesTemplate.getGenerationInfo(rabc));
        ConsoleWrapper.console.println();
        final Task taskDisk = rabc.getResultActionOnDisk();
        if (showDisksTask) {
            boolean processed = false;
            while (!processed) {
                ResultActionDiskVirtualBackup radb = waitForDiskVirtualBackupPhase(taskDisk,
                        VirtualBackupDiskPhases.START_DUMP_THREDS);
                ConsoleWrapper.console.println();
                ConsoleWrapper.console.println(MessagesTemplate.diskHeaderInfo(radb));
                switch (radb.getState()) {
                case ABORTED:

                case FAILED:
                case SKIPPED:
                    ConsoleWrapper.console.println();
                    ConsoleWrapper.console.printf("Result: %S  - Reason: %s%n", radb.getState().toString(),
                            radb.getReason());
                    processed = true;
                    break;
                case SUCCESS:
                case STARTED:
                    try {
                        radb = waitForDiskVirtualBackupPhase(taskDisk,
                                VirtualBackupDiskPhases.END_CALCULATE_NUMBER_OF_GENERATION_DISK_RESTORE);
                        if (radb.getNumberOfBlocks() == 0) {
                            ConsoleWrapper.console.print("Shouldn't be here\n");
                        } else {
                            ConsoleWrapper.console.println(MessagesTemplate.diskDumpHeaderInfo(radb));
                            radb = (ResultActionDiskVirtualBackup) TestUtility.waitForTask(this.sapi, taskDisk, true)
                                    .getResult();
                            if (showDiskDetails) {
                                showDiskTotal(radb, 0, rabc.getNumberOfDisk());
                            }
                        }
                    } finally {
                        processed = true;
                    }
                    break;
                default:
                    break;
                }
            }

            final ResultActionDiskVirtualBackup radb = (ResultActionDiskVirtualBackup) this.sapi.getTaskInfo(taskDisk)
                    .getResult();
            switch (radb.getState()) {
            case FAILED:
            case ABORTED:
            case SKIPPED:
                ConsoleWrapper.console.printf("Disk(0): [diskId %d][Transport %s][uuid %s][file %s] ", radb.getDiskId(),
                        radb.getUuid(), radb.getName());
                ConsoleWrapper.console.println();
                ConsoleWrapper.console.printf("Result: Disk %s - Reason: %s ", radb.getState().toString(),
                        radb.getReason());
                break;

            case SUCCESS:

            case QUEUED:
                break;
            default:
                break;
            }

        } else {
            rabc = (ResultActionIvdVirtualBackup) TestUtility.waitForTask(this.sapi, task, true).getResult();
        }
        if (rabc.getState() == OperationState.SUCCESS) {
            ConsoleWrapper.console.println(rabc.getFcoEntityInfo().getName() + " SUCCESS  ");
        } else {
            ConsoleWrapper.console.printf("Result: %s - Reason:%s \n", rabc.getState().toString(), rabc.getReason());
        }
        waitForIvdPhase(task, VirtualBackupPhases.END);
        ConsoleWrapper.console.println(AbstractTest.LINESEPARATOR);
        ConsoleWrapper.console.println();

        return true;
    }

    private boolean showVirtualApplianceTask(final Task task, final boolean showDisksTask,
            final boolean showDiskDetails)
            throws UnrecognizedToken_Exception, InterruptedException, InvalidTask_Exception, InternalServer_Exception {
        ResultActionVappVirtualBackup rab = waitForVappPhase(task,
                VirtualBackupVappPhases.END_RETRIEVE_CHILD_COLLECTION);
        ConsoleWrapper.console.println();
        final List<ManagedFcoEntityInfo> fcoChildren = rab.getFcoChildren();
        ConsoleWrapper.console.println(AbstractTest.LINESEPARATOR);
        if (!fcoChildren.isEmpty()) {
            ConsoleWrapper.console.println();
            ConsoleWrapper.console.printf("Contains %d VMs:", fcoChildren.size());
            int i = 0;
            ConsoleWrapper.console.println();
            for (final ManagedFcoEntityInfo child : fcoChildren) {
                ++i;
                ConsoleWrapper.console.printf("\t%d - %s", i, TestUtility.fcoToString(child));
                ConsoleWrapper.console.println();
            }
            ConsoleWrapper.console.println();
            ConsoleWrapper.console.println(AbstractTest.LINESEPARATOR);
            rab = waitForVappPhase(task, VirtualBackupVappPhases.END_RETRIEVE_PROFILE);

            List<Task> tasks = rab.getResultActionOnChildVms();
            rab = (ResultActionVappVirtualBackup) this.sapi.getTaskInfo(task).getResult();
            tasks = rab.getResultActionOnChildVms();

            Task childTask = null;
            boolean tasksDone = false;
            final boolean[] processed = new boolean[tasks.size()];
            while (!tasksDone) {
                for (int index = 0; index < tasks.size(); index++) {
                    tasksDone = areTasksDone(processed);
                    if (tasksDone) {
                        break;
                    }
                    childTask = tasks.get(index);
                    final ResultActionVmVirtualBackup rabc = (ResultActionVmVirtualBackup) this.sapi
                            .getTaskInfo(childTask).getResult();

                    switch (rabc.getState()) {
                    case ABORTED:
                    case FAILED:
                    case SKIPPED:
                        if (!processed[index]) {
                            ConsoleWrapper.console.printf("VirtualBackup %s: %s (%s)\n",
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
                                ConsoleWrapper.console.println();
                                ConsoleWrapper.console.println(AbstractTest.LINESEPARATOR);
                                ConsoleWrapper.console.printf("(%d/%d) Backup of %s %s.", rabc.getIndex(),
                                        fcoChildren.size(), TestUtility.fcoToString(rabc.getFcoEntityInfo()),
                                        rabc.getState().toString());
                                ConsoleWrapper.console.println();
                                showVirtualMachineTask(childTask, showDisksTask, showDiskDetails);
                                ConsoleWrapper.console.println(AbstractTest.LINESEPARATOR);
                            } finally {
                                processed[index] = true;
                            }
                        }
                        break;
                    }
                }
            }
            waitForVappPhase(task, VirtualBackupVappPhases.END);
            ConsoleWrapper.console.println(AbstractTest.LINESEPARATOR);
            showTasksResults(tasks);
            ConsoleWrapper.console.println();

        } else {
            ConsoleWrapper.console.printf("No children");
            ConsoleWrapper.console.println();
        }

        return true;
    }

    private boolean showVirtualMachineTask(final Task task, final boolean showDisksTask, final boolean showDiskDetails)
            throws UnrecognizedToken_Exception, InterruptedException, InvalidTask_Exception, InternalServer_Exception {
        final TaskResult taskChildRab = this.sapi.getTaskInfo(task);

        ResultActionVmVirtualBackup rabc = (ResultActionVmVirtualBackup) taskChildRab.getResult();
        ConsoleWrapper.console.println(AbstractTest.LINESEPARATOR);
        ConsoleWrapper.console.printf("VirtualBackup of %s %s.", TestUtility.fcoToString(rabc.getFcoEntityInfo()),
                rabc.getState().toString());
        ConsoleWrapper.console.println();

        rabc = waitForVmPhase(task, VirtualBackupPhases.START_DISKS_CONSOLIDATE);
        ConsoleWrapper.console.println(MessagesTemplate.getLocationString(rabc));
        ConsoleWrapper.console.println(MessagesTemplate.getGenerationInfo(rabc));
        ConsoleWrapper.console.println();
        if (showDisksTask) {
            final List<Task> tasks = rabc.getResultActionsOnDisk();
            Task taskDisk = null;
            boolean tasksDone = false;
            final boolean[] processed = new boolean[tasks.size()];
            if (!tasks.isEmpty()) {
                while (!tasksDone) {
                    for (int index = 0; index < tasks.size(); index++) {
                        tasksDone = areTasksDone(processed);
                        if (tasksDone) {
                            break;
                        }
                        taskDisk = tasks.get(index);
                        ResultActionDiskVirtualBackup rad = waitForDiskVirtualBackupPhase(taskDisk,
                                VirtualBackupDiskPhases.END_CALCULATE_NUMBER_OF_GENERATION_DISK_RESTORE);
                        ConsoleWrapper.console.println();
                        ConsoleWrapper.console.println(MessagesTemplate.diskHeaderInfo(rad));
                        switch (rad.getState()) {
                        case ABORTED:
                        case FAILED:
                        case SKIPPED:
                            if (!processed[index]) {
                                ConsoleWrapper.console.println();
                                ConsoleWrapper.console.printf("Result: %S  - Reason: %s\n", rad.getState().toString(),
                                        rad.getReason());
                                processed[index] = true;
                            }
                            break;
                        case SUCCESS:
                        case STARTED:
                            if (!processed[index]) {
                                try {
                                    rad = waitForDiskVirtualBackupPhase(taskDisk,
                                            VirtualBackupDiskPhases.END_CALCULATE_NUMBER_OF_GENERATION_DISK_RESTORE);
                                    ConsoleWrapper.console.println(MessagesTemplate.getDiskGenerationsInfo(rad));

                                    if (rad.getNumberOfBlocks() == 0) {
                                        ConsoleWrapper.console.print("Shouldn't be here\n");
                                    } else {
                                        ConsoleWrapper.console.println();
                                        ConsoleWrapper.console.println(MessagesTemplate.diskDumpHeaderInfo(rad));
                                        rad = (ResultActionDiskVirtualBackup) TestUtility
                                                .waitForTask(this.sapi, taskDisk, true).getResult();
                                        if (showDiskDetails) {
                                            showDiskTotal(rad, index, rabc.getNumberOfDisk());
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
                    }
                }
            }

            for (int index = 0; index < tasks.size(); index++) {
                taskDisk = tasks.get(index);
                final ResultActionDiskVirtualBackup radb = (ResultActionDiskVirtualBackup) this.sapi
                        .getTaskInfo(taskDisk).getResult();
                switch (radb.getState()) {
                case FAILED:
                case ABORTED:
                case SKIPPED:
                    ConsoleWrapper.console.printf("Disk(%d/%d): [diskId %d][Transport %s][uuid %s][file %s]%s ",
                            index + 1, rabc.getNumberOfDisk(), radb.getDiskId(), radb.getUsedTransportModes(),
                            radb.getUuid(), radb.getName(),
                            (radb.getVDiskId() != null) ? ("[IVD ID " + radb.getVDiskId() + " ]") : "");
                    ConsoleWrapper.console.println();
                    ConsoleWrapper.console.printf("Result: Disk %s - Reason: %s ", radb.getState().toString(),
                            radb.getReason());
                    break;
                case SUCCESS:
                case QUEUED:
                default:
                    break;
                }
            }
        } else {
            rabc = (ResultActionVmVirtualBackup) TestUtility.waitForTask(this.sapi, task, true).getResult();
        }
        if (rabc.getState() == OperationState.SUCCESS) {
            ConsoleWrapper.console.println(rabc.getFcoEntityInfo().getName() + " SUCCESS  ");
        } else {
            ConsoleWrapper.console.printf("Result: %s - Reason:%s \n", rabc.getState().toString(), rabc.getReason());
        }
        rabc = waitForVmPhase(task, VirtualBackupPhases.END);
        ConsoleWrapper.console.println(LINESEPARATOR);
        ConsoleWrapper.console.println();

        return true;
    }

    private ResultActionDiskVirtualBackup waitForDiskVirtualBackupPhase(final Task diskTask,
            final VirtualBackupDiskPhases waitingPhase)
            throws InterruptedException, UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
        ConsoleWrapper.console.printf("Wait for %s phase: ", waitingPhase.toString());
        ResultActionDiskVirtualBackup rabc = (ResultActionDiskVirtualBackup) this.sapi.getTaskInfo(diskTask)
                .getResult();
        String previous = "";
        while ((rabc.getPhase().ordinal() < waitingPhase.ordinal()) && !rabc.isDone()) {
            Thread.sleep(Utility.ONE_HUNDRED_MS);
            rabc = (ResultActionDiskVirtualBackup) this.sapi.getTaskInfo(diskTask).getResult();
            if (!previous.equals(rabc.getPhase().toString())) {
                previous = rabc.getPhase().toString();
                ConsoleWrapper.console.print(previous);
                ConsoleWrapper.console.print(" - ");
            }
        }
        ConsoleWrapper.console.println(rabc.getPhase().toString());
        rabc = (ResultActionDiskVirtualBackup) this.sapi.getTaskInfo(diskTask).getResult();
        return rabc;
    }

    private ResultActionIvdVirtualBackup waitForIvdPhase(final Task childTask, final VirtualBackupPhases waitingPhase)
            throws InterruptedException, UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
        ConsoleWrapper.console.printf("Wait for %s phase: ", waitingPhase.toString());
        ResultActionIvdVirtualBackup rabc = (ResultActionIvdVirtualBackup) this.sapi.getTaskInfo(childTask).getResult();
        String previous = "";
        while ((rabc.getPhase().ordinal() < waitingPhase.ordinal()) && !rabc.isDone()) {
            Thread.sleep(Utility.ONE_HUNDRED_MS);
            rabc = (ResultActionIvdVirtualBackup) this.sapi.getTaskInfo(childTask).getResult();
            if (rabc.isDone()) {
                break;
            }
            if (!previous.equals(rabc.getPhase().toString())) {
                previous = rabc.getPhase().toString();
                ConsoleWrapper.console.print(previous);
                ConsoleWrapper.console.print(" - ");
            }
        }
        ConsoleWrapper.console.println(rabc.getPhase().toString());
        rabc = (ResultActionIvdVirtualBackup) this.sapi.getTaskInfo(childTask).getResult();
        return rabc;
    }

    private ResultActionVappVirtualBackup waitForVappPhase(final Task childTask,
            final VirtualBackupVappPhases waitingPhase)
            throws InterruptedException, UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
        ConsoleWrapper.console.printf("Wait for %s phase: ", waitingPhase.toString());
        ResultActionVappVirtualBackup rabc = (ResultActionVappVirtualBackup) this.sapi.getTaskInfo(childTask)
                .getResult();
        String previous = "";
        while ((rabc.getPhase().ordinal() < waitingPhase.ordinal()) && !rabc.isDone()) {
            Thread.sleep(Utility.ONE_HUNDRED_MS);
            rabc = (ResultActionVappVirtualBackup) this.sapi.getTaskInfo(childTask).getResult();
            if (rabc.isDone()) {
                break;
            }
            if (!previous.equals(rabc.getPhase().toString())) {
                previous = rabc.getPhase().toString();
                ConsoleWrapper.console.print(previous);
                ConsoleWrapper.console.print(" - ");
            }
        }
        ConsoleWrapper.console.println(rabc.getPhase().toString());
        rabc = (ResultActionVappVirtualBackup) this.sapi.getTaskInfo(childTask).getResult();
        return rabc;
    }

    private ResultActionVmVirtualBackup waitForVmPhase(final Task childTask, final VirtualBackupPhases waitingPhase)
            throws InterruptedException, UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
        ConsoleWrapper.console.printf("Wait for %s phase: ", waitingPhase.toString());
        ResultActionVmVirtualBackup rabc = (ResultActionVmVirtualBackup) this.sapi.getTaskInfo(childTask).getResult();
        String previous = "";
        while ((rabc.getPhase().ordinal() < waitingPhase.ordinal()) && !rabc.isDone()) {
            Thread.sleep(Utility.ONE_HUNDRED_MS);
            rabc = (ResultActionVmVirtualBackup) this.sapi.getTaskInfo(childTask).getResult();
            if (rabc.isDone()) {
                break;
            }
            if (!previous.equals(rabc.getPhase().toString())) {
                previous = rabc.getPhase().toString();
                ConsoleWrapper.console.print(previous);
                ConsoleWrapper.console.print(" - ");
            }
        }
        ConsoleWrapper.console.println(rabc.getPhase().toString());
        rabc = (ResultActionVmVirtualBackup) this.sapi.getTaskInfo(childTask).getResult();
        return rabc;
    }

}
