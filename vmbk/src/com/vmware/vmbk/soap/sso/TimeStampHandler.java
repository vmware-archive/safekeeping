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
package com.vmware.vmbk.soap.sso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_utility_1_0.AttributedDateTime;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_utility_1_0.TimestampType;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import com.vmware.vmbk.util.Utility;

public class TimeStampHandler extends SSOHeaderHandler {
    private static final String GMT = "GMT";

    private static final Logger logger_ = Logger.getLogger(TimeStampHandler.class.getName());

    private static DateFormat createDateFormatter() {
	final DateFormat dateFormat = new SimpleDateFormat(Constants.XML_DATE_FORMAT);

	dateFormat.setTimeZone(TimeZone.getTimeZone(TimeStampHandler.GMT));
	return dateFormat;
    }

    private JAXBElement<TimestampType> createTimestamp() {
	final org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_utility_1_0.ObjectFactory wssuObjFactory = new org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_utility_1_0.ObjectFactory();

	final TimestampType timestamp = wssuObjFactory.createTimestampType();

	final long now = System.currentTimeMillis();
	final Date createDate = new Date(now);
	final Date expirationDate = new Date(now + TimeUnit.MINUTES.toMillis(Constants.REQUEST_VALIDITY_IN_MINUTES));

	final DateFormat wssDateFormat = createDateFormatter();
	final AttributedDateTime createTime = wssuObjFactory.createAttributedDateTime();
	createTime.setValue(wssDateFormat.format(createDate));

	final AttributedDateTime expirationTime = wssuObjFactory.createAttributedDateTime();
	expirationTime.setValue(wssDateFormat.format(expirationDate));

	timestamp.setCreated(createTime);
	timestamp.setExpires(expirationTime);
	return wssuObjFactory.createTimestamp(timestamp);
    }

    @Override
    public boolean handleMessage(final SOAPMessageContext smc) {
	if (SsoUtils.isOutgoingMessage(smc)) {
	    try {
		final Node securityNode = SsoUtils.getSecurityElement(SsoUtils.getSOAPHeader(smc));
		final Node timeStampNode = SsoUtils.marshallJaxbElement(createTimestamp()).getDocumentElement();
		securityNode.appendChild(securityNode.getOwnerDocument().importNode(timeStampNode, true));
	    } catch (final DOMException e) {
		logger_.warning(Utility.toString(e));
		throw new RuntimeException(e);
	    } catch (final SOAPException e) {
		logger_.warning(Utility.toString(e));
		throw new RuntimeException(e);
	    }
	}

	return true;

    }
}
