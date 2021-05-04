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

import java.util.LinkedList;
import java.util.List;

import com.vmware.safekeeping.cxf.test.common.ConsoleWrapper;
import com.vmware.sapi.AbstractResultDiskBackupRestore;
import com.vmware.sapi.BlockInfo;
import com.vmware.sapi.InternalServer_Exception;
import com.vmware.sapi.InvalidTask_Exception;
import com.vmware.sapi.ResultAction;
import com.vmware.sapi.ResultActionDiskBackup;
import com.vmware.sapi.ResultActionDiskRestore;
import com.vmware.sapi.ResultActionDiskVirtualBackup;
import com.vmware.sapi.ResultActionIvdBackup;
import com.vmware.sapi.ResultActionVmBackup;
import com.vmware.sapi.ResultActionVmRestore;
import com.vmware.sapi.Sapi;
import com.vmware.sapi.Task;
import com.vmware.sapi.TaskResult;
import com.vmware.sapi.UnrecognizedToken_Exception;

/**
 * @author mdaneri
 *
 */
public abstract class AbstractTest {
    protected static final String LINESEPARATOR = "##################################################################################################################################################################################################################";
    protected final Sapi sapi;

    /**
     * @param sapi
     */
    protected AbstractTest(final Sapi sapi) {
        this.sapi = sapi;
    }

    protected boolean areTasksDone(final boolean[] processed) {
        boolean tasksDone = true;
        for (final boolean element : processed) {
            tasksDone &= element;
            if (!tasksDone) {
                break;
            }
        }

        return tasksDone;
    }

    protected boolean areTasksDone(final List<Task> tasks)
            throws UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
        boolean done = true;
        for (final Task t : tasks) {
            final TaskResult r = this.sapi.getTaskInfo(t);
            done &= (r.getResult() != null) && r.getResult().isDone();
            if (!done) {
                break;
            }
        }
        return done;
    }

    protected void showDisksTotal(final ResultActionIvdBackup rabr)
            throws UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
        showDiskTotal(rabr.getResultActionOnDisk());
        ConsoleWrapper.console.println();
        ConsoleWrapper.console.println(MessagesTemplate.separetorBar(true));

    }

    protected void showDisksTotal(final ResultActionVmBackup rabr)
            throws UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
        int index = 0;
        for (final Task taskDisk : rabr.getResultActionsOnDisk()) {
            ++index;
            showDiskTotal(taskDisk, index, rabr.getNumberOfDisk());
            ConsoleWrapper.console.println();
            ConsoleWrapper.console.println(MessagesTemplate.separetorBar(true));
        }
    }

    protected void showDisksTotal(final ResultActionVmRestore rabr)
            throws UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
        int index = 0;
        for (final Task taskDisk : rabr.getResultActionsOnDisk()) {
            ++index;
            showDiskTotal(taskDisk, index, rabr.getNumberOfDisk());
            ConsoleWrapper.console.println();
            ConsoleWrapper.console.println(MessagesTemplate.separetorBar(true));
        }
    }

    protected void showDiskTotal(final AbstractResultDiskBackupRestore radb, final int index, final int numberOfDisks)
            throws UnrecognizedToken_Exception, InvalidTask_Exception {
        ConsoleWrapper.console.println();
        ConsoleWrapper.console.printf("Details:");
        ConsoleWrapper.console.println();
        ConsoleWrapper.console.printf("Disk(%d/%d): [diskId %d][Transport %s][uuid %s][file %s]%s ", index + 1,
                numberOfDisks, radb.getDiskId(), radb.getUsedTransportModes(), radb.getUuid(), radb.getName(),
                ((radb.getVDiskId() != null) ? ("[IVD ID " + radb.getVDiskId() + " ]") : ""));
        switch (radb.getState()) {
        case ABORTED:
            break;
        case FAILED:
            break;
        case QUEUED:
            break;
        case SKIPPED:
            ConsoleWrapper.console.println();
            ConsoleWrapper.console.println(radb.getReason());
            break;
        case STARTED:
            break;
        case SUCCESS:
//            ConsoleWrapper.console.println();
            if (radb instanceof ResultActionDiskRestore) {
                ConsoleWrapper.console.println(MessagesTemplate.diskDumpHeaderInfo((ResultActionDiskRestore) radb));
            } else if (radb instanceof ResultActionDiskVirtualBackup) {
                ConsoleWrapper.console
                        .println(MessagesTemplate.diskDumpHeaderInfo((ResultActionDiskVirtualBackup) radb));
            } else if (radb instanceof ResultActionDiskBackup) {
                ConsoleWrapper.console.println(MessagesTemplate.diskDumpHeaderInfo((ResultActionDiskBackup) radb));
            } else {
                ConsoleWrapper.console.println("Unsupported Disk opeation");
            }
            ConsoleWrapper.console.println();

            final List<BlockInfo> dumps = this.sapi.getAllDumps(radb.getTask());
            if (!dumps.isEmpty()) {
                final boolean compressed = dumps.get(0).isCompress();
                ConsoleWrapper.console
                        .println((compressed) ? MessagesTemplate.compressHeader() : MessagesTemplate.standardHeader());
                for (final BlockInfo dump : dumps) {
                    final MyDumpFileInfo myDump = new MyDumpFileInfo(dump);
                    ConsoleWrapper.console.println(myDump.toString());
                }
                ConsoleWrapper.console.println(MessagesTemplate.separetorBar(compressed));
                final TotalDumpFileInfo myDump = new TotalDumpFileInfo(radb.getFcoEntityInfo().getEntityType(),
                        radb.getDiskId(), dumps);
                ConsoleWrapper.console.println(myDump.toString());
            }
            break;
        default:
            break;

        }

    }

    protected void showDiskTotal(final ResultActionDiskVirtualBackup radb, final int index, final int numberOfDisks)
            throws UnrecognizedToken_Exception, InvalidTask_Exception {
        ConsoleWrapper.console.printf("Disk(%d/%d): [diskId %d][Transport %s][uuid %s][file %s]%s ", index + 1,
                numberOfDisks, radb.getDiskId(), radb.getUsedTransportModes(), radb.getUuid(), radb.getName(),
                ((radb.getVDiskId() != null) ? ("[IVD ID " + radb.getVDiskId() + " ]") : ""));
        switch (radb.getState()) {
        case ABORTED:
            break;
        case FAILED:
            break;
        case QUEUED:
            break;
        case SKIPPED:
            ConsoleWrapper.console.println();
            ConsoleWrapper.console.println(radb.getReason());
            break;
        case STARTED:
            break;
        case SUCCESS:
            ConsoleWrapper.console.println();
            ConsoleWrapper.console.println(MessagesTemplate.diskDumpHeaderInfo(radb));

            ConsoleWrapper.console.println();

            final List<BlockInfo> dumps = this.sapi.getAllDumps(radb.getTask());
            if (!dumps.isEmpty()) {
                final boolean compressed = dumps.get(0).isCompress();
                ConsoleWrapper.console
                        .println((compressed) ? MessagesTemplate.compressHeader() : MessagesTemplate.standardHeader());

                for (final BlockInfo dump : dumps) {
                    final MyDumpFileInfo myDump = new MyDumpFileInfo(dump);
                    ConsoleWrapper.console.println(myDump.toString());
                }
                ConsoleWrapper.console.println(MessagesTemplate.separetorBar(compressed));
                final TotalDumpFileInfo myDump = new TotalDumpFileInfo(radb.getFcoEntityInfo().getEntityType(),
                        radb.getDiskId(), dumps);
                ConsoleWrapper.console.println(myDump.toString());
            }
            break;
        default:
            break;

        }

    }

    protected void showDiskTotal(final Task taskDisk)
            throws UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
        final ResultActionDiskBackup radb = (ResultActionDiskBackup) this.sapi.getTaskInfo(taskDisk).getResult();
        showDiskTotal(radb, 0, 1);
    }

    protected void showDiskTotal(final Task taskDisk, final int index, final int numberOfDisks)
            throws UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
        final ResultActionDiskBackup radb = (ResultActionDiskBackup) this.sapi.getTaskInfo(taskDisk).getResult();
        showDiskTotal(radb, index, numberOfDisks);
    }

    protected List<ResultAction> showTasksResults(final List<Task> tasks)
            throws UnrecognizedToken_Exception, InvalidTask_Exception, InternalServer_Exception {
        final List<ResultAction> result = new LinkedList<>();
        if (tasks != null) {
            for (final Task t : tasks) {
                final TaskResult r = this.sapi.getTaskInfo(t);
                final ResultAction ra = r.getResult();
                result.add(ra);
                switch (ra.getState()) {
                case ABORTED:
                case FAILED:
                case QUEUED:
                case SKIPPED:
                    ConsoleWrapper.console.printf("Result: %s - Reason: %s\n", ra.getState().toString(),
                            ra.getReason());
                    break;

                case SUCCESS:
                    ConsoleWrapper.console.println(ra.getFcoEntityInfo().getName() + " SUCCESS  ");
                    break;

                case STARTED:

                    break;

                }
            }
        }
        return result;
    }

}
