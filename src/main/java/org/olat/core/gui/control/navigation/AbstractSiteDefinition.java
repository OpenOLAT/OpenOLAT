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

package org.olat.core.gui.control.navigation;

import org.olat.core.CoreSpringFactory;
import org.olat.core.configuration.AbstractConfigOnOff;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * @author Christian Guretzki
 */
public abstract class AbstractSiteDefinition extends AbstractConfigOnOff implements SiteDefinition {
	
	private static final OLog log = Tracing.createLoggerFor(AbstractSiteDefinition.class);
	
	private int order;
	private String defaultSiteSecurityCallbackBeanId;
		
	public void setOrder(int order) {
		this.order = order;
	}
	
	@Override
	public int getOrder() {
		return order;
	}


	public void setDefaultSiteSecurityCallbackBeanId(String siteSecurityCallbackId) {
		this.defaultSiteSecurityCallbackBeanId = siteSecurityCallbackId;
	}
	
	@Override
	public String getDefaultSiteSecurityCallbackBeanId() {
		return defaultSiteSecurityCallbackBeanId;
	}
	
	
	
	@Override
	public final SiteInstance createSite(UserRequest ureq, WindowControl wControl) {
		if(ureq == null) return null;
		
		SiteConfiguration config = getSiteConfiguration();
		
		String secCallbackBeanId = config.getSecurityCallbackBeanId();
		if(StringHelper.containsNonWhitespace(secCallbackBeanId)) {
			Object siteSecCallback = getSiteSecurityCallback(secCallbackBeanId);
			if (siteSecCallback instanceof SiteViewSecurityCallback) {
				if(!((SiteViewSecurityCallback)siteSecCallback).isAllowedToViewSite(ureq)) {
					return null;
				}
			} else if (siteSecCallback instanceof SiteSecurityCallback && !((SiteSecurityCallback)siteSecCallback).isAllowedToLaunchSite(ureq)) {
				return null;
			}
		}
		return createSite(ureq, wControl, config);
	}
	
	private Object getSiteSecurityCallback(String secCallbackBeanId) {
		try {
			return CoreSpringFactory.getBean(secCallbackBeanId);
		} catch (Exception e) {
			log.error("Cannot find security callback: " + secCallbackBeanId + " return administrator only security callback");
			return CoreSpringFactory.getBean("adminSiteSecurityCallback");
		}
	}
	
	protected abstract SiteInstance createSite(UserRequest ureq, WindowControl wControl, SiteConfiguration config);
	
	protected SiteConfiguration getSiteConfiguration() {
		SiteDefinitions siteModule = CoreSpringFactory.getImpl(SiteDefinitions.class);
		return siteModule.getConfigurationSite(this);
	}

	@Override
	public boolean isFeatureEnabled() {
		return true;
	}
}
