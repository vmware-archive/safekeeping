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
package com.vmware.safekeeping.core.soap;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.type.enums.VMwareCloudPlatforms;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.UserSession;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;

abstract class AbstractConnection extends AbstractHttpConf implements IConnection, IVimConnection {
    protected static Logger logger;
    private static final String VIMSERVICEINSTANCETYPE = "ServiceInstance";
    private static final String VIMSERVICEINSTANCEVALUE = "ServiceInstance";

    protected Map<String, Object> headers;
    protected ServiceContent serviceContent;
    private ManagedObjectReference svcInstRef;
    protected String thumbPrint;
    private final VMwareCloudPlatforms platform;
    /*
     * Record last time token has been updated. used to verify if vCenter is still
     * connected
     */
    private long tokenUpdateSessionEventTime;
    private URL url;
    protected UserSession userSession;
    protected VimPortType vimPort;
    protected VimService vimService;
    private int nfcHostPort;

    protected AbstractConnection(final int nfcHostPort, VMwareCloudPlatforms platform) {
        this.nfcHostPort = nfcHostPort;
        this.platform = platform;
    }

    @Override
    public Map<String, Object> getHeaders() {
        return this.headers;
    }

    @Override
    public String getHost() {
        return this.url.getHost();
    }

    public int getNfcHostPort() {
        return this.nfcHostPort;
    }

    @Override
    public Integer getPort() {
        return this.url.getPort();
    }

    @Override
    public ServiceContent getServiceContent() {
        return this.serviceContent;
    }

    @Override
    public String getServiceInstanceName() {
        return VIMSERVICEINSTANCETYPE;
    }

    @Override
    public ManagedObjectReference getServiceInstanceReference() {
        if (this.svcInstRef == null) {
            final ManagedObjectReference ref = new ManagedObjectReference();
            ref.setType(VIMSERVICEINSTANCETYPE);
            ref.setValue(VIMSERVICEINSTANCEVALUE);
            this.svcInstRef = ref;
        }
        return this.svcInstRef;
    }

    public String getThumbPrint() {
        return this.thumbPrint;
    }

    @Override
    public String getUrl() {
        return this.url.toString();
    }

    @Override
    public URL getURL() {
        return this.url;
    }

    @Override
    public UserSession getUserSession() {
        return this.userSession;
    }

    @Override
    public VimPortType getVimPort() {
        return this.vimPort;
    }

    @Override
    public VimService getVimService() {
        return this.vimService;
    }

    @Override
    public boolean isConnected() {
        if (this.userSession == null) {
            return false;
        }
        if (this.tokenUpdateSessionEventTime == 0) {
            this.tokenUpdateSessionEventTime = new Date().getTime();
        }
        final long now = new Date().getTime();
        final boolean stillConnected = now < (this.tokenUpdateSessionEventTime
                + CoreGlobalSettings.getTicketLifeExpectancyInMilliSeconds());

        if (!stillConnected) {
            final String msg = String.format("Actual time: %d Start time:%d", now, this.tokenUpdateSessionEventTime);
            logger.warning(msg);
        }
        return stillConnected;
    }

    protected void resetTokenUpdateSessionEventTime() {
        this.tokenUpdateSessionEventTime = new Date().getTime();
    }

    public void setNfcHostPort(final int nfcHostPort) {
        this.nfcHostPort = nfcHostPort;
    }

    @Override
    public void setUrl(final String url) throws MalformedURLException {
        this.url = new URL(url);
    }

    public VMwareCloudPlatforms getPlatform() {
        return platform;
    }

}
