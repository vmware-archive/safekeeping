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

import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import com.vmware.vmbk.soap.sso.wssecurity.WsSecuritySignatureCertificate;
import com.vmware.vmbk.util.Utility;

public class WsSecurityUserCertificateSignatureHandler extends SSOHeaderHandler {

    private static final Logger logger_ = Logger.getLogger(WsSecurityUserCertificateSignatureHandler.class.getName());
    private final PrivateKey _privateKey;
    private final X509Certificate _userCert;

    WsSecurityUserCertificateSignatureHandler(final PrivateKey privateKey, final X509Certificate userCert) {
	this._privateKey = privateKey;
	this._userCert = userCert;
    }

    public WsSecurityUserCertificateSignatureHandler(final SecurityUtil userSignatureCert) {
	this._privateKey = userSignatureCert.getPrivateKey();
	this._userCert = userSignatureCert.getUserCert();
    }

    @Override
    public boolean handleMessage(final SOAPMessageContext smc) {

	if (SsoUtils.isOutgoingMessage(smc)) {
	    final WsSecuritySignatureCertificate wsSign = new WsSecuritySignatureCertificate(this._privateKey,
		    this._userCert);
	    try {
		wsSign.sign(smc.getMessage());
	    } catch (final SignatureException e) {
		logger_.warning("Could not sign the SOAPMessage - SignatureException");
		logger_.warning(Utility.toString(e));
		throw new RuntimeException(e);
	    } catch (final SOAPException e) {
		logger_.warning("Could not sign the SOAPMessage - SOAPException");
		logger_.warning(Utility.toString(e));
		throw new RuntimeException(e);
	    }
	}
	return true;
    }

}
