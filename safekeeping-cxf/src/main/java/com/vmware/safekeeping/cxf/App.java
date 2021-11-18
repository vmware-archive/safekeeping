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

import java.io.File;
import java.util.logging.Logger;

import com.vmware.safekeeping.common.JavaWarning;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;

public class App {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(final String[] args) {
        try {
            JavaWarning.disableWarning();
            CoreGlobalSettings.createLogFolder();
            CoreGlobalSettings.loadLogSetting();
            CoreGlobalSettings.createConfigFolder(App.class);
            final Cxf safekeeping = new Cxf(new File(CoreGlobalSettings.getDefaulConfigPropertiesFile()));
            if (safekeeping.parse(args)) {
                safekeeping.run();
            }
        } catch (final Exception e) {
            Utility.logWarning(logger, e);
            // Default exit code, 0, indicates success. Non-zero value means failure.
            System.exit(1);
        }
    }

}
