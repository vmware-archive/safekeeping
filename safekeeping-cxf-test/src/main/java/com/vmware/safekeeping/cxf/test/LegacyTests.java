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
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.cxf.test.common.ConsoleWrapper;
import com.vmware.safekeeping.cxf.test.common.FormatMismatch;
import com.vmware.safekeeping.cxf.test.common.TestUtility;
import com.vmware.sapi.ArchiveCheckGenerationsOptions;
import com.vmware.sapi.ArchiveListOptions;
import com.vmware.sapi.ArchiveRemoveGenerationsOptions;
import com.vmware.sapi.ArchiveRemoveProfileOptions;
import com.vmware.sapi.BackupMode;
import com.vmware.sapi.BackupOptions;
import com.vmware.sapi.FcoTarget;
import com.vmware.sapi.FcoTypeSearch;
import com.vmware.sapi.GenerationsFilter;
import com.vmware.sapi.InternalCoreResult_Exception;
import com.vmware.sapi.InternalServer_Exception;
import com.vmware.sapi.InvalidOptions_Exception;
import com.vmware.sapi.InvalidTask_Exception;
import com.vmware.sapi.OperationState;
import com.vmware.sapi.RestoreOptions;
import com.vmware.sapi.ResultAction;
import com.vmware.sapi.ResultActionBackup;
import com.vmware.sapi.Sapi;
import com.vmware.sapi.SearchManagementEntityInfoType;
import com.vmware.sapi.UnrecognizedToken_Exception;

public class LegacyTests {
    private static final Random rand = new Random();
    private static final String LINE_SEPARATOR = "##################################################################################################################################################################################################################";

    private final Sapi sapi;

    private Scanner scan;

    public LegacyTests(final Sapi sapi) {
        this.sapi = sapi;
    }

    protected boolean execute() throws UnrecognizedToken_Exception, InvalidTask_Exception, InterruptedException,
            InternalCoreResult_Exception, InvalidOptions_Exception, FormatMismatch, InternalServer_Exception {
        ConsoleWrapper.console.printf("Test number:%n");
        ConsoleWrapper.console.printf("1 - standard %n");
        ConsoleWrapper.console.printf("2 - multigeneration backup and single restore %n");
        ConsoleWrapper.console.printf("3 - IVD  %n");
        ConsoleWrapper.console.printf("4 - multi-backup  %n");
        ConsoleWrapper.console.printf("5 - multigeneration backup and single restore BIG Windows VM%n");
        ConsoleWrapper.console.printf("6 - multi-backup  3 disks VM%n");
        ConsoleWrapper.console.printf("7 - Archive test%n");
        ConsoleWrapper.console.printf("8 - Backup custom fco %n");

        ConsoleWrapper.console.printf("0 - quit %n");
        final String testcase = ConsoleWrapper.console.readLine("Select test :  ");

        if ("0".equals(testcase)) {
            return true;
        } else {
            testTasks(testcase);
        }
        return false;
    }

    private Collection<? extends ResultAction> testBackup(final FcoTarget target, final BackupMode mode)
            throws InterruptedException, InvalidTask_Exception, UnrecognizedToken_Exception, InternalServer_Exception {
        final List<FcoTarget> list = new LinkedList<>();
        list.add(target);
        return testBackup(list, mode);

    }

    private Collection<? extends ResultAction> testBackup(final List<FcoTarget> targetList, final BackupMode mode)
            throws InterruptedException, InvalidTask_Exception, UnrecognizedToken_Exception, InternalServer_Exception {
        ConsoleWrapper.console.println();
        ConsoleWrapper.console.println(LINE_SEPARATOR);
        ConsoleWrapper.console.println(LINE_SEPARATOR);
        ConsoleWrapper.console
                .printf("************************************************************************\t\tTEST BACKUP - ");
        for (final FcoTarget fco : targetList) {
            ConsoleWrapper.console.printf(" %s:%s ", fco.getKeyType().toString(), fco.getKey());
        }
        ConsoleWrapper.console
                .println("\t\t**********************************************************************************");
        ConsoleWrapper.console.println();
        ConsoleWrapper.console.println(LINE_SEPARATOR);
        ConsoleWrapper.console.println(LINE_SEPARATOR);
        ConsoleWrapper.console.println();
        ConsoleWrapper.console.println("Sleeping 4secs");
        Thread.sleep(Utility.FIVE_SECONDS_IN_MILLIS);
        final BackupOptions options = new BackupOptions();
        options.setRequestedTransportMode("nbdssl:nbd");
        options.setCompression(true);
        options.setRequestedBackupMode(mode);
        options.setForce(false);
        options.setCipher(false);
        options.setMaxBlockSize(App.DEFAULT_BLOCK_SIZE);
        options.setNumberOfThreads(10);
        options.getTargetList().addAll(targetList);

        final BackupTest backupTest = new BackupTest(this.sapi);
        final Collection<? extends ResultAction> result = backupTest.executeAsync(options, true, true);
        ConsoleWrapper.console.println("End Backup.");
        return result;
    }

    protected Collection<? extends ResultAction> testRestore(final FcoTarget target)
            throws UnrecognizedToken_Exception, InvalidTask_Exception, InterruptedException, InternalServer_Exception {
        final List<FcoTarget> list = new LinkedList<>();
        list.add(target);
        return testRestore(list);

    }

    protected Collection<? extends ResultAction> testRestore(final List<FcoTarget> targetList)
            throws UnrecognizedToken_Exception, InvalidTask_Exception, InterruptedException, InternalServer_Exception {
        ConsoleWrapper.console.printf("%n");
        ConsoleWrapper.console.println(LINE_SEPARATOR);
        ConsoleWrapper.console.println(LINE_SEPARATOR);
        ConsoleWrapper.console
                .printf("************************************************************************\t\tTEST Restore - ");
        for (final FcoTarget fco : targetList) {
            ConsoleWrapper.console.printf(" %s:%s ", fco.getKeyType().toString(), fco.getKey());
        }
        ConsoleWrapper.console
                .println("\t\t**********************************************************************************");
        ConsoleWrapper.console.println();
        ConsoleWrapper.console.println(LINE_SEPARATOR);
        ConsoleWrapper.console.println(LINE_SEPARATOR);
        ConsoleWrapper.console.println();
        ConsoleWrapper.console.println("Sleeping 4secs");
        Thread.sleep(Utility.FIVE_SECONDS_IN_MILLIS);
        // Obtain a number between [0 - 49].
        final Integer n = rand.nextInt(500);
        final RestoreOptions options = new RestoreOptions();
        options.setRequestedTransportMode("nbdssl:nbd");
        options.setPowerOn(true);
        options.setPostfix("-restore" + n.toString());
        options.setNumberOfThreads(10);
        options.setOverwrite(true);
        options.setForce(true);
        options.setFolder(TestUtility.newSearchManagementEntity(SearchManagementEntityInfoType.NAME, "max2"));
        options.getTargetList().addAll(targetList);
        final RestoreTest test = new RestoreTest(this.sapi);
        final Collection<? extends ResultAction> result = test.execute(options, true, true, true);
        ConsoleWrapper.console.printf("End Restore.%n");
        return result;

    }

    /**
     * @throws InterruptedException
     * @throws InvalidTask_Exception
     * @throws UnrecognizedToken_Exception
     * @throws InvalidOptions_Exception
     * @throws InternalCoreResult_Exception
     * @throws FormatMismatch
     * @throws InternalServer_Exception
     * @throws NumberFormatException
     * @throws Exception
     *
     */
    private void testTasks(final String testcase)
            throws UnrecognizedToken_Exception, InvalidTask_Exception, InterruptedException,
            InternalCoreResult_Exception, InvalidOptions_Exception, FormatMismatch, InternalServer_Exception {
        int numberOfGenerations = 1;
        switch (testcase) {
        case "1":
            testTasks1();
            break;
        case "2":
        case "4":
        case "5":
        case "6":
            ConsoleWrapper.console.printf("%n");
            ConsoleWrapper.console.printf("Remove profile (y/n)  ");
            final boolean remove = "y".equalsIgnoreCase(this.scan.next("[ynNN]"));
            ConsoleWrapper.console.printf("%n");
            ConsoleWrapper.console.printf("Number of generation to create:   ");
            numberOfGenerations = this.scan.nextInt();
            ConsoleWrapper.console.printf("%n");
            ConsoleWrapper.console.printf("%n");
            switch (testcase) {
            case "2":
                testTasks2(numberOfGenerations, remove);
                break;
            case "5":
                testTasks5(numberOfGenerations, remove);
                break;
            case "4":
                testTasks4(numberOfGenerations, remove);
                break;
            case "6":
                testTasks6(numberOfGenerations, remove);
                break;
            default:
                break;
            }

            break;
        case "7":
            testTasks7();
            break;
        case "8":
            ConsoleWrapper.console.printf("%n");
            ConsoleWrapper.console.printf("type:name (ex vm:max_possa)  : ");
            final String name8 = this.scan.next();
            ConsoleWrapper.console.printf("%n");
            ConsoleWrapper.console.printf("Number of generation to create:   ");
            numberOfGenerations = this.scan.nextInt();
            ConsoleWrapper.console.printf("%n");
            ConsoleWrapper.console.printf("%n");

            testTasks8(name8, numberOfGenerations);

            break;

        case "3":
            testTasksIvd();
            break;
        default:
            break;
        }
    }

    private void testTasks1() throws UnrecognizedToken_Exception, InvalidTask_Exception, InterruptedException,
            InternalCoreResult_Exception, InvalidOptions_Exception, InternalServer_Exception {
        final int numberOfBackup = 10;

        final FcoTarget vmPossa = new FcoTarget();
        vmPossa.setKey("max_possa");
        vmPossa.setKeyType(FcoTypeSearch.VM_NAME);
        final FcoTarget centosOvf = new FcoTarget();
        centosOvf.setKey("vm-216");
        centosOvf.setKeyType(FcoTypeSearch.VM_MOREF);
        final ArchiveTest archiveTest = new ArchiveTest(this.sapi);
        final FcoTarget ivdTest = new FcoTarget();
        ivdTest.setKey("test");
        ivdTest.setKeyType(FcoTypeSearch.IVD_NAME);

        final FcoTarget disksVapp = new FcoTarget();
        disksVapp.setKey("max_3disks-vapp");
        disksVapp.setKeyType(FcoTypeSearch.VAPP_NAME);
        final FcoTarget tagDaily = new FcoTarget();
        tagDaily.setKey("Daily");
        tagDaily.setKeyType(FcoTypeSearch.TAG);

        testRestore(ivdTest);
        archiveTest.testShow(centosOvf);
        archiveTest.testShow(ivdTest);

        final ArchiveListOptions alOpt = new ArchiveListOptions();
        alOpt.getTargetList().add(vmPossa);
        // list
        archiveTest.execute(alOpt);

        testBackup(ivdTest, BackupMode.INCREMENTAL);
        testBackup(disksVapp, BackupMode.INCREMENTAL);

        final Collection<? extends ResultAction> res = testBackup(vmPossa, BackupMode.FULL);
        if (res == null) {
            ConsoleWrapper.console.printf("Something is wrong %n");
        }
        for (int i = 0; i < numberOfBackup; i++) {
            testBackup(vmPossa, BackupMode.INCREMENTAL);
        }
        // Show

        archiveTest.testShow(vmPossa);

        testBackup(tagDaily, BackupMode.INCREMENTAL);
        Integer genId = null;
        for (final ResultAction r : res) {
            if ((r.getState() == OperationState.SUCCESS)
                    && StringUtils.equals(r.getFcoEntityInfo().getName(), vmPossa.getKey())) {
                final ResultActionBackup rab = (ResultActionBackup) r;
                genId = rab.getGenerationInfo().getGenerationId();
                break;

            }
        }

        testRestore(ivdTest);
        testRestore(disksVapp);
        testRestore(vmPossa);
        // check
        final ArchiveCheckGenerationsOptions acOpt = new ArchiveCheckGenerationsOptions();
        acOpt.getTargetList().add(vmPossa);
        archiveTest.execute(acOpt);
        if (genId != null) {
            genId += 4;
            ConsoleWrapper.console.printf("Remove Generation: " + genId);
            final ArchiveRemoveGenerationsOptions arOpt = new ArchiveRemoveGenerationsOptions();
            arOpt.setFilter(GenerationsFilter.LIST);
            arOpt.getGenerationId().add(genId);
            arOpt.getTargetList().add(vmPossa);

            archiveTest.execute(arOpt);
            ConsoleWrapper.console.printf("Restore %s after removing generation %d - Restore generation %d",
                    vmPossa.getKey(), genId, genId - 1);
            testRestore(vmPossa);
            final ArchiveRemoveProfileOptions arpopt = new ArchiveRemoveProfileOptions();
            arpopt.getTargetList().add(vmPossa);
            archiveTest.execute(arpopt);
            ConsoleWrapper.console.printf("End archiveTest.%n");
        }
    }

    /**
     * Multi-generation backup and 1 restore
     *
     * @param numberOfGenerations
     * @param remove              remove profile
     * @throws UnrecognizedToken_Exception
     * @throws InvalidTask_Exception
     * @throws InterruptedException
     * @throws InternalServer_Exception
     *
     * @throws Exception
     */
    private void testTasks2(final int numberOfGenerations, final boolean remove)
            throws InterruptedException, InvalidTask_Exception, UnrecognizedToken_Exception, InternalServer_Exception {

        final FcoTarget vmPossa = new FcoTarget();
        vmPossa.setKey("max_possa");
        vmPossa.setKeyType(FcoTypeSearch.VM_NAME);
        if (remove) {
            // Remove profile
            final ArchiveTest archiveTest = new ArchiveTest(this.sapi);
            final ArchiveRemoveProfileOptions arpopt = new ArchiveRemoveProfileOptions();
            arpopt.getTargetList().add(vmPossa);
            archiveTest.execute(arpopt);

            final Collection<? extends ResultAction> res = testBackup(vmPossa, BackupMode.FULL);
            if (res == null) {
                ConsoleWrapper.console.printf("Something is wrong ");
            }
        }
        for (Integer i = 0; i < numberOfGenerations; i++) {
            ConsoleWrapper.console.printf("Loop %d%n ", i);
            testBackup(vmPossa, BackupMode.INCREMENTAL);
        }
        // start restore
        testRestore(vmPossa);

    }

    /**
     * Multi generation backup and no restore
     *
     * @param numberOfGenerations
     * @param remove              remove profile
     * @throws UnrecognizedToken_Exception
     * @throws InvalidTask_Exception
     * @throws InterruptedException
     * @throws InternalServer_Exception
     *
     * @throws Exception
     */
    private void testTasks4(final int numberOfGenerations, final boolean remove)
            throws InterruptedException, InvalidTask_Exception, UnrecognizedToken_Exception, InternalServer_Exception {

        final FcoTarget vmPossa = new FcoTarget();
        vmPossa.setKey("max_possa");
        vmPossa.setKeyType(FcoTypeSearch.VM_NAME);
        if (remove) {
            // Remove profile
            final ArchiveTest archiveTest = new ArchiveTest(this.sapi);
            final ArchiveRemoveProfileOptions arpopt = new ArchiveRemoveProfileOptions();
            arpopt.getTargetList().add(vmPossa);
            archiveTest.execute(arpopt);

            final Collection<? extends ResultAction> res = testBackup(vmPossa, BackupMode.FULL);
            if (res == null) {
                ConsoleWrapper.console.println("Something is wrong");
            }
        }
        for (Integer i = 0; i < numberOfGenerations; i++) {
            ConsoleWrapper.console.printf("Loop %d%n", i);
            testBackup(vmPossa, BackupMode.INCREMENTAL);
        }

    }

    /**
     * Multi-generation backup and 1 restore
     *
     * @param numberOfGenerations
     * @param remove              remove profile
     * @throws UnrecognizedToken_Exception
     * @throws InvalidTask_Exception
     * @throws InterruptedException
     * @throws InternalServer_Exception
     *
     * @throws Exception
     */
    private void testTasks5(final int numberOfGenerations, final boolean remove)
            throws InterruptedException, InvalidTask_Exception, UnrecognizedToken_Exception, InternalServer_Exception {

        final FcoTarget vmFancy = new FcoTarget();
        vmFancy.setKey("max_fancyHw");
        vmFancy.setKeyType(FcoTypeSearch.VM_NAME);
        if (remove) {
            // Remove profile
            final ArchiveTest archiveTest = new ArchiveTest(this.sapi);
            final ArchiveRemoveProfileOptions arpopt = new ArchiveRemoveProfileOptions();
            arpopt.getTargetList().add(vmFancy);
            archiveTest.execute(arpopt);

            final Collection<? extends ResultAction> res = testBackup(vmFancy, BackupMode.FULL);
            if (res == null) {
                ConsoleWrapper.console.printf("Something is wrong ");
            }
        }
        for (Integer i = 0; i < numberOfGenerations; i++) {
            ConsoleWrapper.console.printf("Loop %d%n ", i);
            testBackup(vmFancy, BackupMode.INCREMENTAL);
        }
        // start restore
        testRestore(vmFancy);

    }

    /**
     * Multi generation backup and no restore
     *
     * @param numberOfGenerations
     * @param remove              remove profile
     * @throws UnrecognizedToken_Exception
     * @throws InvalidTask_Exception
     * @throws InterruptedException
     * @throws InternalServer_Exception
     *
     * @throws Exception
     */
    private void testTasks6(final int numberOfGenerations, final boolean remove)
            throws InterruptedException, InvalidTask_Exception, UnrecognizedToken_Exception, InternalServer_Exception {

        final FcoTarget vmPossa = new FcoTarget();
        vmPossa.setKey("max_3disks-vm");
        vmPossa.setKeyType(FcoTypeSearch.VM_NAME);
        if (remove) {
            // Remove profile
            final ArchiveTest archiveTest = new ArchiveTest(this.sapi);
            final ArchiveRemoveProfileOptions arpopt = new ArchiveRemoveProfileOptions();
            arpopt.getTargetList().add(vmPossa);
            archiveTest.execute(arpopt);

            final Collection<? extends ResultAction> res = testBackup(vmPossa, BackupMode.FULL);
            if (res == null) {
                ConsoleWrapper.console.println("Something is wrong");
            }
        }
        for (Integer i = 0; i < numberOfGenerations; i++) {
            ConsoleWrapper.console.printf("Loop %d%n", i);
            testBackup(vmPossa, BackupMode.INCREMENTAL);
        }

    }

    private void testTasks7() throws UnrecognizedToken_Exception, InvalidTask_Exception, InterruptedException,
            InternalCoreResult_Exception, InvalidOptions_Exception, InternalServer_Exception {
        final FcoTarget vmPossa = new FcoTarget();
        vmPossa.setKey("max_possa");
        vmPossa.setKeyType(FcoTypeSearch.VM_NAME);
        final FcoTarget centosOvf = new FcoTarget();
        centosOvf.setKey("vm-216");
        centosOvf.setKeyType(FcoTypeSearch.VM_MOREF);
        final ArchiveTest archiveTest = new ArchiveTest(this.sapi);
        final FcoTarget ivdTest = new FcoTarget();
        ivdTest.setKey("test");
        ivdTest.setKeyType(FcoTypeSearch.IVD_NAME);

        final FcoTarget disksVapp = new FcoTarget();
        disksVapp.setKey("max_3disks-vapp");
        disksVapp.setKeyType(FcoTypeSearch.VAPP_NAME);
        final FcoTarget tagDaily = new FcoTarget();
        tagDaily.setKey("Daily");
        tagDaily.setKeyType(FcoTypeSearch.TAG);

        archiveTest.testShow(centosOvf);
        archiveTest.testShow(ivdTest);

        final ArchiveListOptions alOpt = new ArchiveListOptions();
        alOpt.getTargetList().add(vmPossa);
        // list
        archiveTest.execute(alOpt);

        // Show

        archiveTest.testShow(vmPossa);

        // check
        final ArchiveCheckGenerationsOptions acOpt = new ArchiveCheckGenerationsOptions();
        acOpt.getTargetList().add(vmPossa);
        archiveTest.execute(acOpt);

    }

    /**
     * Multi-generation backup
     *
     * @param numberOfGenerations
     * @param remove              remove profile
     * @throws UnrecognizedToken_Exception
     * @throws InvalidTask_Exception
     * @throws InterruptedException
     * @throws InternalServer_Exception
     *
     * @throws Exception
     */
    private void testTasks8(final String fcoName, final int numberOfGenerations)
            throws InterruptedException, InvalidTask_Exception, UnrecognizedToken_Exception, InternalServer_Exception {

        final FcoTarget fcoTarget = new FcoTarget();
        final String[] name = StringUtils.split(fcoName, ':');
        if (name.length == 2) {
            fcoTarget.setKey(name[1]);
            switch (name[0]) {
            case "vm":
                fcoTarget.setKeyType(FcoTypeSearch.VM_NAME);
                break;
            case "vapp":
                fcoTarget.setKeyType(FcoTypeSearch.VAPP_NAME);
                break;
            case "ivd":
                fcoTarget.setKeyType(FcoTypeSearch.IVD_UUID);
                break;
            default:
                ConsoleWrapper.console.printf("fcoName :%s type not valid%n", name[0]);
                break;
            }

            for (Integer i = 0; i < numberOfGenerations; i++) {
                ConsoleWrapper.console.printf("Loop %d%n ", i);
                testBackup(fcoTarget, BackupMode.INCREMENTAL);
            }
        } else {
            ConsoleWrapper.console.printf("fcoName :%s not valid%n", fcoName);
        }

    }

    private void testTasksIvd()
            throws InterruptedException, InvalidTask_Exception, UnrecognizedToken_Exception, InternalServer_Exception {
        final FcoTarget ivdTest = new FcoTarget();
        ivdTest.setKey("test");
        ivdTest.setKeyType(FcoTypeSearch.IVD_NAME);
        testBackup(ivdTest, BackupMode.INCREMENTAL);
        for (int i = 0; i < 4; i++) {
            testBackup(ivdTest, BackupMode.INCREMENTAL);
        }
        testRestore(ivdTest);
    }

}
