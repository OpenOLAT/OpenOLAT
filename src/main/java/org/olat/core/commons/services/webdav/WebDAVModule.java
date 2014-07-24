/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.commons.services.webdav;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

public class WebDAVModule extends AbstractOLATModule implements ConfigOnOff {
	
	private static final OLog log = Tracing.createLoggerFor(WebDAVModule.class);

	private static final String WEBDAV_ENABLED = "webdav.enabled";
	private static final String WEBDAV_LINKS_ENABLED = "webdav.links.enabled";
	private static final String DIGEST_AUTH_ENABLED = "auth.digest.enabled";
	private static final String TERMS_FOLDERS_ENABLED = "webdav.termsfolders.enabled";
	
	private Map<String, WebDAVProvider> webdavProviders;

	private boolean enabled;
	private boolean linkEnabled;
	private boolean digestAuthenticationEnabled;
	private boolean termsFoldersEnabled;
	
	@Override
	public void init() {
		//module enabled/disabled
		String enabledObj = getStringPropertyValue(WEBDAV_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String linkEnabledObj = getStringPropertyValue(WEBDAV_LINKS_ENABLED, true);
		if(StringHelper.containsNonWhitespace(linkEnabledObj)) {
			linkEnabled = "true".equals(linkEnabledObj);
		}
		
		String digestEnabledObj = getStringPropertyValue(DIGEST_AUTH_ENABLED, true);
		if(StringHelper.containsNonWhitespace(digestEnabledObj)) {
			digestAuthenticationEnabled = "true".equals(digestEnabledObj);
		}

		String termsFoldersEnabledObj = getStringPropertyValue(TERMS_FOLDERS_ENABLED, true);
		if(StringHelper.containsNonWhitespace(termsFoldersEnabledObj)) {
			termsFoldersEnabled = "true".equals(termsFoldersEnabledObj);
		}

	}
	
	@Override
	protected void initDefaultProperties() {
		enabled = getBooleanConfigParameter(WEBDAV_ENABLED, true);
		linkEnabled = getBooleanConfigParameter(WEBDAV_LINKS_ENABLED, true);
		digestAuthenticationEnabled = getBooleanConfigParameter(DIGEST_AUTH_ENABLED, true);
		termsFoldersEnabled = getBooleanConfigParameter(TERMS_FOLDERS_ENABLED, true);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		String enabledStr = enabled ? "true" : "false";
		setStringProperty(WEBDAV_ENABLED, enabledStr, true);
	}
	
	public boolean isLinkEnabled() {
		return linkEnabled;
	}

	public void setLinkEnabled(boolean linkEnabled) {
		String enabledStr = linkEnabled ? "true" : "false";
		setStringProperty(WEBDAV_LINKS_ENABLED, enabledStr, true);
	}

	public boolean isDigestAuthenticationEnabled() {
		return digestAuthenticationEnabled;
	}
	
	public void setDigestAuthenticationEnabled(boolean digestAuthenticationEnabled) {
		String enabledStr = digestAuthenticationEnabled ? "true" : "false";
		setStringProperty(DIGEST_AUTH_ENABLED, enabledStr, true);
	}
	

	public void setTermsFoldersEnabled(boolean termsFoldersEnabled) {
		String enabledStr = termsFoldersEnabled ? "true" : "false";
		setStringProperty(TERMS_FOLDERS_ENABLED, enabledStr, true);
	}

	public boolean isTermsFoldersEnabled() {
		return termsFoldersEnabled;
	}


	/**
	 * Return an unmodifiable map
	 * @return
	 */
	public Map<String, WebDAVProvider> getWebDAVProviders() {
		return Collections.unmodifiableMap(webdavProviders); 
	}
	
	/**
	 * Set the list of webdav providers.
	 * @param webdavProviders
	 */
	public void setWebdavProviderList(List<WebDAVProvider> webdavProviders) {
		if (webdavProviders == null) return;//nothing to do
		
		for (WebDAVProvider provider : webdavProviders) {
			addWebdavProvider(provider);
		}
	}
	
	/**
	 * Add a new webdav provider.
	 * @param provider
	 */
	public void addWebdavProvider(WebDAVProvider provider) {
		if (webdavProviders == null) {
			webdavProviders = new HashMap<String, WebDAVProvider>();
		}
		if (webdavProviders.containsKey(provider.getMountPoint()))
			throw new AssertException("May not add two providers with the same mount point.");
		webdavProviders.put(provider.getMountPoint(), provider);
		log.info("Adding webdav mountpoint '" + provider.getMountPoint() + "'.");
	}
}