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
package com.vmware.vmbk.soap;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.DOMException;

final class VcSessionHandler implements SOAPHandler<SOAPMessageContext> {

    private final String vcSessionCookie;

    VcSessionHandler(final String vcSessionCookie) {
	this.vcSessionCookie = vcSessionCookie;
    }

    @Override
    public void close(final MessageContext arg0) {
    }

    @Override
    public Set<QName> getHeaders() {
	return null;
    }

    private SOAPHeader getSOAPHeader(final SOAPMessageContext smc) throws SOAPException {
	return smc.getMessage().getSOAPPart().getEnvelope().getHeader() == null
		? smc.getMessage().getSOAPPart().getEnvelope().addHeader()
		: smc.getMessage().getSOAPPart().getEnvelope().getHeader();
    }

    @Override
    public boolean handleFault(final SOAPMessageContext arg0) {
	return false;
    }

    @Override
    public boolean handleMessage(final SOAPMessageContext smc) {
	if (isOutgoingMessage(smc)) {
	    try {
		final SOAPHeader header = getSOAPHeader(smc);

		final SOAPElement vcsessionHeader = header
			.addChildElement(new javax.xml.namespace.QName("#", "vcSessionCookie"));
		vcsessionHeader.setValue(this.vcSessionCookie);

	    } catch (final DOMException e) {
		e.printStackTrace();
		throw new RuntimeException(e);
	    } catch (final SOAPException e) {
		e.printStackTrace();
		throw new RuntimeException(e);
	    }
	}
	return true;
    }

    private boolean isOutgoingMessage(final SOAPMessageContext smc) {
	final Boolean outboundProperty = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
	return outboundProperty.booleanValue();
    }

}
