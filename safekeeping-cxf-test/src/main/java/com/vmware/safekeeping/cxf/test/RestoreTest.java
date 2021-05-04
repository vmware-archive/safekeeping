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
import com.vmware.sapi.InternalServer_Exception;
import com.vmware.sapi.InvalidTask_Exception;
import com.vmware.sapi.ManagedFcoEntityInfo;
import com.vmware.sapi.OperationState;
import com.vmware.sapi.RestoreDiskPhases;
import com.vmware.sapi.RestoreOptions;
import com.vmware.sapi.RestorePhases;
import com.vmware.sapi.RestoreVappPhases;
import com.vmware.sapi.ResultAction;
import com.vmware.sapi.ResultActionDiskRestore;
import com.vmware.sapi.ResultActionIvdRestore;
import com.vmware.sapi.ResultActionRestore;
import com.vmware.sapi.ResultActionVappRestore;
import com.vmware.sapi.ResultActionVmRestore;
import com.vmware.sapi.Sapi;
import com.vmware.sapi.Task;
import com.vmware.sapi.TaskResult;
import com.vmware.sapi.Tasks;
import com.vmware.sapi.UnrecognizedToken_Exception;

public class RestoreTest extends AbstractTest {

    private boolean showWaitPhases;

    public RestoreTest(final Sapi sapi) {
        super(sapi);
    }

    public Collection<? extends ResultAction> execute(final RestoreOptions options, final boolean showDisksTask,
            final boolean showDiskDetails, final boolean showWaitPhases)
            throws UnrecognizedToken_Exception, InvalidTask_Exception, InterruptedException, InternalServer_Exception {

        this.showWaitPhases = showWaitPhases;
        MessagesTemplate.printTestHeader(this.getClass().getName(), options);
        ConsoleWrapper.console.printf("Call to sapi.restore Synchronously\n");
        final List<ResultActionRestore> result = this.sapi.restore(options);
        for (final ResultActionRestore rar : result) {
            switch (rar.getState()) {
            case ABORTED:

            case FAILED:
            case SKIPPED:
            case STARTED:
            case QUEUED:
                ConsoleWrapper.console.printf("Restore %s: %s (%s)\n", TestUtility.fcoToString(rar.getFcoEntityInfo()),
                        rar.getState().toString(), rar.getReason());
                break;

            case SUCCESS:
                ConsoleWrapper.console.println();
                ConsoleWrapper.console.println(AbstractTest.LINESEPARATOR);
                ConsoleWrapper.console.printf("Restore of %s %s.", TestUtility.fcoToString(rar.getFcoEntityInfo()),
                        rar.getState().toString());
                ConsoleWrapper.console.println();
                switch (rar.getFcoEntityInfo().getEntityType()) {

                case IMPROVED_VIRTUAL_DISK:
                    showImprovedVirtualDiskTask(rar.getTask(), showDisksTask, showDiskDetails);
                    break;
                case K_8_S_NAMESPACE:
                    break;
                case VIRTUAL_APP:
                    showVirtualApplianceTask(rar.getTask(), showDisksTask, showDiskDetails);
                    break;
                case VIRTUAL_MACHINE:
                    showVirtualMachineTask(rar.getTask(), showDisksTask, showDiskDetails);
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

    public Collection<? extends ResultAction> executeAsync(final RestoreOptions options, final boolean showDisksTask,
            final boolean showDiskDetails, final boolean showWaitPhases)
            throws UnrecognizedToken_Exception, InvalidTask_Exception, InterruptedException, InternalServer_Exception {
        ResultAction rar = null;
        this.showWaitPhases = showWaitPhases;
        MessagesTemplate.printTestHeader(this.getClass().getName(), options);
        ConsoleWrapper.console.printf("Call to sapi.restore Asynchronously\n");

        final Tasks taskRestores = this.sapi.restoreAsync(options);
        final List<Task> tasks = taskRestores.getTaskList();
        if (taskRestores.getState() == OperationState.SUCCESS) {
            Task tr = null;
            boolean tasksDone = false;
            final boolean[] processed = new boolean[tasks.size()];
            while (!tasksDone) {
                for (int index = 0; index < tasks.size(); index++) {
                    tasksDone = areTasksDone(processed);
                    if (tasksDone) {
                        break;
                    }
                    tr = tasks.get(index);
                    rar = this.sapi.getTaskInfo(tr).getResult();
                    switch (rar.getState()) {
                    case ABORTED:

                    case FAILED:
                    case SKIPPED:
                        if (!processed[index]) {
                            ConsoleWrapper.console.printf("Check %s: %s (%s)\n",
                                    TestUtility.fcoToString(rar.getFcoEntityInfo()), rar.getState().toString(),
                                    rar.getReason());
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
                                ConsoleWrapper.console.printf("Restore of %s %s.",
                                        TestUtility.fcoToString(rar.getFcoEntityInfo()), rar.getState().toString());
                                ConsoleWrapper.console.println();
                                switch (rar.getFcoEntityInfo().getEntityType()) {

                                case IMPROVED_VIRTUAL_DISK:
                                    showImprovedVirtualDiskTask(tr, showDisksTask, showDiskDetails);
                                    break;
                                case K_8_S_NAMESPACE:
                                    break;
                                case VIRTUAL_APP:
                                    showVirtualApplianceTask(tr, showDisksTask, showDiskDetails);
                                    break;
                                case VIRTUAL_MACHINE:
                                    showVirtualMachineTask(tr, showDisksTask, showDiskDetails);
                                    break;
                                default:
                                    break;

                                }
                            } catch (final Exception e) {
                                ConsoleWrapper.console.println(e.getMessage());
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
            ConsoleWrapper.console.println(taskRestores.getReason());
        }
        return showTasksResults(tasks);
    }

    private boolean showImprovedVirtualDiskTask(final Task task, final boolean showDisksTask,
            final boolean showDiskDetails)
            throws UnrecognizedToken_Exception, InterruptedException, InvalidTask_Exception, InternalServer_Exception {
        final TaskResult taskChildRab = this.sapi.getTaskInfo(task);

        ResultActionIvdRestore rar = (ResultActionIvdRestore) taskChildRab.getResult();

        ConsoleWrapper.console.println(AbstractTest.LINESEPARATOR);
        ConsoleWrapper.console.printf("(Restore of %s %s.", TestUtility.fcoToString(rar.getFcoEntityInfo()),
                rar.getState().toString());
        ConsoleWrapper.console.println();

        rar = waitForIvdPhase(task, RestorePhases.END_RESTORE_METADATA, this.showWaitPhases);
        ConsoleWrapper.console.println();
        ConsoleWrapper.console.println(MessagesTemplate.getRestoreManagedInfo(rar.getManagedInfo()));
        ConsoleWrapper.console.println(MessagesTemplate.getLocationString(rar));

        rar = waitForIvdPhase(task, RestorePhases.START_DISKS_RESTORE, this.showWaitPhases);
        final Task taskDisk = rar.getResultActionOnDisk();
        if (showDisksTask) {
            boolean processed = false;
            while (!processed) {
                ResultActionDiskRestore radr = waitForDiskRestorePhase(taskDisk, RestoreDiskPhases.END_OPEN_VMDK,
                        this.showWaitPhases);

                switch (radr.getState()) {
                case ABORTED:

                case FAILED:
                case SKIPPED:
                    ConsoleWrapper.console.println(MessagesTemplate.diskHeaderInfo(radr));
                    ConsoleWrapper.console.println();
                    ConsoleWrapper.console.printf("Result: %S  - Reason: %s", radr.getState().toString(),
                            radr.getReason());
                    processed = true;
                    break;
                case QUEUED:
                    break;
                case STARTED:
                case SUCCESS:
                    try {
                        radr = waitForDiskRestorePhase(taskDisk,
                                RestoreDiskPhases.END_CALCULATE_NUMBER_OF_GENERATION_DISK_RESTORE, this.showWaitPhases);

                        ConsoleWrapper.console.println(MessagesTemplate.getDiskGenerationsInfo(radr));
                        if (radr.getTotalBlocks() == 0) {
                            ConsoleWrapper.console.print("Shouldn't be here\n");
                        } else {
                            radr = waitForDiskRestorePhase(taskDisk, RestoreDiskPhases.START_DUMP_THREDS,
                                    this.showWaitPhases);

                            ConsoleWrapper.console.println(MessagesTemplate.diskHeaderInfo(radr));
                            ConsoleWrapper.console.println(MessagesTemplate.diskDumpHeaderInfo(radr));
                            radr = (ResultActionDiskRestore) TestUtility.waitForTask(this.sapi, taskDisk, true)
                                    .getResult();
                            if (showDiskDetails) {
                                showDiskTotal(radr, 0, rar.getNumberOfDisk());
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

            final ResultActionDiskRestore radr = (ResultActionDiskRestore) this.sapi.getTaskInfo(taskDisk).getResult();
            switch (radr.getState()) {

            case FAILED:
            case ABORTED:
            case SKIPPED:
                ConsoleWrapper.console.printf("Disk(0): [diskId %d][Transport %s][uuid %s][file %s] ", radr.getDiskId(),
                        radr.getUsedTransportModes(), radr.getUuid(), radr.getName());

                ConsoleWrapper.console.println();
                ConsoleWrapper.console.printf("Result: Disk %s - Reason: %s ", radr.getState().toString(),
                        radr.getReason());
                break;
            case SUCCESS:

            case QUEUED:
                break;
            default:
                break;
            }
        } else {
            rar = (ResultActionIvdRestore) TestUtility.waitForTask(this.sapi, task, true).getResult();
        }
        if (rar.getState() == OperationState.SUCCESS) {
            ConsoleWrapper.console.println(rar.getFcoEntityInfo().getName() + " SUCCESS  ");
        } else {
            ConsoleWrapper.console.printf("Result: %s - Reason:%s \n", rar.getState().toString(), rar.getReason());
        }
        waitForIvdPhase(task, RestorePhases.END, this.showWaitPhases);
        ConsoleWrapper.console.println(AbstractTest.LINESEPARATOR);
        ConsoleWrapper.console.println();

        return true;
    }

    private boolean showVirtualApplianceTask(final Task task, final boolean showDisksTask,
            final boolean showDiskDetails)
            throws UnrecognizedToken_Exception, InterruptedException, InvalidTask_Exception, InternalServer_Exception {
        ResultActionVappRestore rar = waitForVappPhase(task, RestoreVappPhases.END_RETRIEVE_CHILD_COLLECTION,
                this.showWaitPhases);
        final List<ManagedFcoEntityInfo> fcoChildren = rar.getFcoChildren();
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
            rar = waitForVappPhase(task, RestoreVappPhases.END_RESTORE_METADATA, this.showWaitPhases);

            ConsoleWrapper.console.println();
            ConsoleWrapper.console.println(MessagesTemplate.getRestoreManagedInfo(rar.getManagedInfo()));
            final List<Task> tasks = rar.getResultActionOnChildVms();

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
                    final ResultActionVmRestore rabc = (ResultActionVmRestore) this.sapi.getTaskInfo(childTask)
                            .getResult();

                    switch (rabc.getState()) {
                    case ABORTED:
                    case FAILED:
                    case SKIPPED:
                        if (!processed[index]) {
                            ConsoleWrapper.console.printf("Restore %s: %s (%s)\n",
                                    TestUtility.fcoToString(rar.getFcoEntityInfo()), rar.getState().toString(),
                                    rar.getReason());
                            processed[index] = true;
                        }
                        rar = (ResultActionVappRestore) this.sapi.getTaskInfo(task).getResult();
                        break;
                    case QUEUED:
                        break;
                    case SUCCESS:
                    case STARTED:
                        if (!processed[index]) {
                            try {
                                ConsoleWrapper.console.println();
                                ConsoleWrapper.console.println(AbstractTest.LINESEPARATOR);
                                ConsoleWrapper.console.printf("(%d/%d) Restore of %s %s.", rabc.getIndex(),
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
            waitForVappPhase(task, RestoreVappPhases.END, this.showWaitPhases);
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

        ResultActionVmRestore rar = (ResultActionVmRestore) taskChildRab.getResult();

        ConsoleWrapper.console.println(AbstractTest.LINESEPARATOR);
        ConsoleWrapper.console.printf("Restore of %s %s.", TestUtility.fcoToString(rar.getFcoEntityInfo()),
                rar.getState().toString());
        ConsoleWrapper.console.println();

        rar = waitForRestorePhase(task, RestorePhases.END_RESTORE_METADATA, this.showWaitPhases);
        ConsoleWrapper.console.println();
        ConsoleWrapper.console.println(MessagesTemplate.getRestoreManagedInfo(rar.getManagedInfo()));
        ConsoleWrapper.console.println(MessagesTemplate.getLocationString(rar));

        rar = waitForRestorePhase(task, RestorePhases.START_DISKS_RESTORE, this.showWaitPhases);

        if (showDisksTask) {
            final List<Task> tasks = rar.getResultActionsOnDisk();
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
                        ResultActionDiskRestore rad = waitForDiskRestorePhase(taskDisk, RestoreDiskPhases.END_OPEN_VMDK,
                                this.showWaitPhases);
                        switch (rad.getState()) {
                        case ABORTED:
                        case FAILED:
                        case SKIPPED:
                            if (!processed[index]) {
                                ConsoleWrapper.console.println(MessagesTemplate.diskHeaderInfo(rad));
                                ConsoleWrapper.console.println();
                                ConsoleWrapper.console.printf("Result: %S  - Reason: %s", rad.getState().toString(),
                                        rad.getReason());
                                processed[index] = true;
                            }
                            break;
                        case QUEUED:
                            break;
                        case STARTED:
                        case SUCCESS:
                            if (!processed[index]) {
                                try {
                                    rad = waitForDiskRestorePhase(taskDisk,
                                            RestoreDiskPhases.END_CALCULATE_NUMBER_OF_GENERATION_DISK_RESTORE,
                                            this.showWaitPhases);

                                    ConsoleWrapper.console.println(MessagesTemplate.getDiskGenerationsInfo(rad));
                                    if (rad.getTotalBlocks() == 0) {
                                        ConsoleWrapper.console.print("Shouldn't be here\n");
                                    } else {
                                        rad = waitForDiskRestorePhase(taskDisk, RestoreDiskPhases.START_DUMP_THREDS,
                                                this.showWaitPhases);

                                        ConsoleWrapper.console.println(MessagesTemplate.diskHeaderInfo(rad));
                                        ConsoleWrapper.console.println(MessagesTemplate.diskDumpHeaderInfo(rad));
                                        rad = (ResultActionDiskRestore) TestUtility
                                                .waitForTask(this.sapi, taskDisk, true).getResult();
                                        if (showDiskDetails) {
                                            showDiskTotal(rad, index, rar.getNumberOfDisk());
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
                final ResultActionDiskRestore radr = (ResultActionDiskRestore) this.sapi.getTaskInfo(taskDisk)
                        .getResult();
                switch (radr.getState()) {
                case FAILED:
                case ABORTED:
                case SKIPPED:
                    ConsoleWrapper.console.printf("Disk(%d/%d): [diskId %d][Transport %s][uuid %s][file %s]%s ",
                            index + 1, rar.getNumberOfDisk(), radr.getDiskId(), radr.getUsedTransportModes(),
                            radr.getUuid(), radr.getName(),
                            (radr.getVDiskId() != null) ? ("[IVD ID " + radr.getVDiskId() + " ]") : "");
                    ConsoleWrapper.console.println();
                    ConsoleWrapper.console.printf("Result: Disk %s - Reason: %s ", radr.getState().toString(),
                            radr.getReason());
                    break;
                case SUCCESS:
                case QUEUED:
                default:
                    break;
                }
            }
        } else {
            rar = (ResultActionVmRestore) TestUtility.waitForTask(this.sapi, task, true).getResult();
        }
        rar = waitForRestorePhase(task, RestorePhases.END, this.showWaitPhases);
        if (rar.getState() == OperationState.SUCCESS) {
            ConsoleWrapper.console.println(rar.getFcoEntityInfo().getName() + " SUCCESS  ");
        } else {
            ConsoleWrapper.console.printf("Result: %s - Reason:%s \n", rar.getState().toString(), rar.getReason());
        }

        ConsoleWrapper.console.println(AbstractTest.LINESEPARATOR);
        ConsoleWrapper.console.println();

        return true;
    }

    private ResultActionDiskRestore waitForDiskRestorePhase(final Task diskTask, final RestoreDiskPhases waitingPhase,
            final boolean visible)
            throws InterruptedException, UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
        ConsoleWrapper.console.printf("Wait for phase: %s", waitingPhase.toString());
        if (visible) {
            ConsoleWrapper.console.printf(": [ ");
        }
        ResultActionDiskRestore rar = (ResultActionDiskRestore) this.sapi.getTaskInfo(diskTask).getResult();
        String previous = "";
        while ((rar.getPhase().ordinal() < waitingPhase.ordinal())) {
            Thread.sleep(Utility.ONE_HUNDRED_MS);
            rar = (ResultActionDiskRestore) this.sapi.getTaskInfo(diskTask).getResult();
            if (!previous.equals(rar.getPhase().toString())) {
                previous = rar.getPhase().toString();
                if (visible) {
                    ConsoleWrapper.console.print(previous);
                    ConsoleWrapper.console.print(" - ");
                }
            }
        }
        if (visible) {
            ConsoleWrapper.console.print(rar.getPhase().toString());
            ConsoleWrapper.console.print(" ] ");
        }
        ConsoleWrapper.console.println(" - Done");
        rar = (ResultActionDiskRestore) this.sapi.getTaskInfo(diskTask).getResult();
        return rar;
    }

    private ResultActionIvdRestore waitForIvdPhase(final Task childTask, final RestorePhases waitingPhase,
            final boolean visible)
            throws InterruptedException, UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
        ConsoleWrapper.console.printf("Wait for phase: %s", waitingPhase.toString());
        if (visible) {
            ConsoleWrapper.console.printf(": [ ");
        }
        ResultActionIvdRestore rabc = (ResultActionIvdRestore) this.sapi.getTaskInfo(childTask).getResult();
        String previous = "";
        while ((rabc.getPhase().ordinal() < waitingPhase.ordinal())) {
            Thread.sleep(Utility.ONE_HUNDRED_MS);
            rabc = (ResultActionIvdRestore) this.sapi.getTaskInfo(childTask).getResult();
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
        rabc = (ResultActionIvdRestore) this.sapi.getTaskInfo(childTask).getResult();
        return rabc;
    }

    private ResultActionVmRestore waitForRestorePhase(final Task childTask, final RestorePhases waitingPhase,
            final boolean visible)
            throws InterruptedException, UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
        ConsoleWrapper.console.printf("Wait for phase: %s", waitingPhase.toString());
        if (visible) {
            ConsoleWrapper.console.printf(": [ ");
        }
        ResultActionVmRestore rabc = (ResultActionVmRestore) this.sapi.getTaskInfo(childTask).getResult();
        String previous = "";
        while ((rabc.getPhase().ordinal() < waitingPhase.ordinal())) {
            Thread.sleep(Utility.ONE_HUNDRED_MS);
            rabc = (ResultActionVmRestore) this.sapi.getTaskInfo(childTask).getResult();
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
        rabc = (ResultActionVmRestore) this.sapi.getTaskInfo(childTask).getResult();
        return rabc;
    }

    private ResultActionVappRestore waitForVappPhase(final Task childTask, final RestoreVappPhases waitingPhase,
            final boolean visible)
            throws InterruptedException, UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
        ConsoleWrapper.console.printf("Wait for phase: %s", waitingPhase.toString());
        if (visible) {
            ConsoleWrapper.console.printf(": [ ");
        }
        ResultActionVappRestore rabc = (ResultActionVappRestore) this.sapi.getTaskInfo(childTask).getResult();
        String previous = "";
        while ((rabc.getPhase().ordinal() < waitingPhase.ordinal())) {
            Thread.sleep(Utility.ONE_HUNDRED_MS);
            rabc = (ResultActionVappRestore) this.sapi.getTaskInfo(childTask).getResult();
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
        rabc = (ResultActionVappRestore) this.sapi.getTaskInfo(childTask).getResult();
        return rabc;
    }

}
