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
package com.vmware.safekeeping.core.soap.helpers;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vmware.cis.Session;
import com.vmware.cis.SessionTypes.Info;
import com.vmware.cis.tagging.Category;
import com.vmware.cis.tagging.CategoryModel;
import com.vmware.cis.tagging.Tag;
import com.vmware.cis.tagging.TagAssociation;
import com.vmware.cis.tagging.TagModel;
import com.vmware.safekeeping.core.soap.sso.SecurityUtil;
import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vapi.cis.authn.ProtocolFactory;
import com.vmware.vapi.cis.authn.SecurityContextFactory;
import com.vmware.vapi.core.ApiProvider;
import com.vmware.vapi.core.ExecutionContext.SecurityContext;
import com.vmware.vapi.protocol.HttpConfiguration;
import com.vmware.vapi.protocol.ProtocolConnection;
import com.vmware.vapi.saml.SamlToken;
import com.vmware.vapi.security.SessionSecurityContext;

public class VapiAuthenticationHelper {

	private static final Logger logger = Logger.getLogger(VapiAuthenticationHelper.class.getName());

	private Category categoryService;
	private final HttpConfiguration httpConfig;
	private final SecurityUtil securityUtil;
	private StubConfiguration sessionStubConfig;
	private Session sessionSvc;

	private StubFactory stubFactory;

	private TagAssociation tagAssociation;

	private Tag taggingService;
	private final URL vapiUrl;

	public VapiAuthenticationHelper(final URL vapiUrl, final HttpConfiguration httpConfig,
			final SecurityUtil securityUtil) {
		this.vapiUrl = vapiUrl;
		this.httpConfig = httpConfig;
		this.securityUtil = securityUtil;
	}

	private StubFactory createApiStubFactory(final HttpConfiguration httpConfig) {
		final ProtocolFactory pf = new ProtocolFactory();
		final ProtocolConnection connection = pf.getHttpConnection(getVapiURL().toString(), null, httpConfig);
		final ApiProvider provider = connection.getApiProvider();

		return new StubFactory(provider);
	}

	public Category getCategoryService() {
		return this.categoryService;
	}

	public StubFactory getStubFactory() {
		return this.stubFactory;
	}

	public TagAssociation getTagAssociation() {
		return this.tagAssociation;
	}

	public CategoryModel getTagCategoryByName(final String name) {
		final List<String> categories = this.categoryService.list();
		CategoryModel result = null;
		for (final String categoryId : categories) {
			final CategoryModel tag = this.categoryService.get(categoryId);
			if (tag.getName().equals(name)) {
				result = tag;
				break;
			}
		}
		return result;
	}

	public Tag getTaggingService() {
		return this.taggingService;
	}

	public URL getVapiURL() {

		return this.vapiUrl;
	}

	private void initTaggingService() {
		this.categoryService = getStubFactory().createStub(Category.class, this.sessionStubConfig);
		this.taggingService = getStubFactory().createStub(Tag.class, this.sessionStubConfig);
		this.tagAssociation = getStubFactory().createStub(TagAssociation.class, this.sessionStubConfig);

	}

	public void keepAlive() {
		final Info info = this.sessionSvc.get();
		this.categoryService.list();
		if (logger.isLoggable(Level.INFO)) {
			final String msg = String.format("keep alive  user %s: Creation time %s Last Accesst Time %s",
					info.getUser(), info.getCreatedTime(), info.getLastAccessedTime());
			logger.info(msg);
		}

	}

	public StubConfiguration loginBySamlBearerToken(final SamlToken samlBearerToken) {
		if (this.sessionSvc == null) {

			this.stubFactory = createApiStubFactory(this.httpConfig);

			final SecurityContext samlSecurityContext = SecurityContextFactory
					.createSamlSecurityContext(samlBearerToken, this.securityUtil.getPrivateKey());

			this.sessionStubConfig = new StubConfiguration(samlSecurityContext);

			final Session session = this.stubFactory.createStub(Session.class, this.sessionStubConfig);

			final char[] sessionId = session.create();

			final SessionSecurityContext sessionSecurityContext = new SessionSecurityContext(sessionId);

			this.sessionStubConfig.setSecurityContext(sessionSecurityContext);

			/*
			 * Create a stub for the session service using the authenticated session
			 */
			this.sessionSvc = this.stubFactory.createStub(Session.class, this.sessionStubConfig);

			initTaggingService();
		} else {
			logger.info("Session already created");

		}
		return this.sessionStubConfig;
	}

	public void logout() {
		if (this.sessionSvc != null) {
			this.sessionSvc.delete();
			this.sessionSvc = null;
		}
	}

	public void setTagAssociation(final TagAssociation tagAssociation) {
		this.tagAssociation = tagAssociation;
	}

	public List<CategoryModel> tagCategoriesList(final Set<String> set) {
		final List<String> categories = this.categoryService.list();
		final LinkedList<CategoryModel> result = new LinkedList<>();
		for (final String categoryId : categories) {
			final CategoryModel tag = this.categoryService.get(categoryId);
			if ((set == null) || (set.isEmpty())) {
				result.add(tag);
			} else {
				for (final String t : set) {
					if (tag.getName().equals(t)) {
						result.add(tag);
						break;
					}
				}
			}
		}
		return result;
	}

	public List<TagModel> tagsList(final Set<String> set) {
		final List<String> tags = this.taggingService.list();
		final List<TagModel> result = new LinkedList<>();
		for (final String tagId : tags) {
			final TagModel tag = this.taggingService.get(tagId);
			if ((set == null) || (set.isEmpty())) {
				result.add(tag);
			} else {
				for (final String t : set) {
					if (tag.getName().equals(t)) {
						result.add(tag);
						break;
					}
				}
			}
		}
		return result;
	}

	public StubConfiguration updateSamlBearerToken(final SamlToken samlToken) {
		final SecurityContext samlSecurityContext = SecurityContextFactory.createSamlSecurityContext(samlToken,
				this.securityUtil.getPrivateKey());

		this.sessionStubConfig = new StubConfiguration(samlSecurityContext);

		final Session session = this.stubFactory.createStub(Session.class, this.sessionStubConfig);

		final char[] sessionId = session.create();

		final SessionSecurityContext sessionSecurityContext = new SessionSecurityContext(sessionId);

		this.sessionStubConfig.setSecurityContext(sessionSecurityContext);

		/*
		 * Create a stub for the session service using the authenticated session
		 */

		return this.sessionStubConfig;
	}

}
