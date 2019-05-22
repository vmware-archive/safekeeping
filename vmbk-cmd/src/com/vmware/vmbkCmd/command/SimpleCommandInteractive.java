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
package com.vmware.vmbkCmd.command;

import java.util.LinkedHashMap;
import java.util.List;

import com.vmware.vmbk.control.info.vmTypeSearch;

abstract class SimpleCommandInteractive implements CommandInteractive {

    private boolean help;

    @Override
    public LinkedHashMap<String, vmTypeSearch> getIvdTargets() {

	return null;
    }

    @Override
    public LinkedHashMap<String, vmTypeSearch> getK8sTargets() {

	return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.command.Command#getTargets()
     */
    @Override
    public List<String> getTargets() {

	return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.command.Command#getVappTargets()
     */
    @Override
    public LinkedHashMap<String, vmTypeSearch> getVappTargets() {

	return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.vmbk.command.Command#getVmTargets()
     */
    @Override
    public LinkedHashMap<String, vmTypeSearch> getVmTargets() {

	return null;
    }

    @Override
    public boolean isHelp() {
	return this.help;
    }

    public void setHelp(final boolean help) {
	this.help = help;
    }
}
