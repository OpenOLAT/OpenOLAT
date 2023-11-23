/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.externalsite;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.AbstractSiteDefinition;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteDefinitions;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.gui.control.navigation.SiteSecurityCallback;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.modules.externalsite.model.ExternalSiteConfiguration;
import org.olat.modules.externalsite.model.ExternalSiteLangConfiguration;

/**
 * Initial date: Nov 10, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ExternalSiteDef extends AbstractSiteDefinition implements SiteDefinition {


	public ExternalSiteDef() {
		//
	}

	@Override
	public boolean isEnabled() {
		return isFeatureEnabled() && super.isEnabled();
	}

	@Override
	protected SiteInstance createSite(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		if (StringHelper.containsNonWhitespace(config.getSecurityCallbackBeanId())) {
			return createSite(ureq, getExternalSiteConfiguration(), config);
		}
		UserSession usess = ureq.getUserSession();
		if (!usess.getRoles().isInvitee()) {
			// only for registered users and guests
			return createSite(ureq, getExternalSiteConfiguration(), config);
		}
		return null;
	}

	protected SiteInstance createSite(UserRequest ureq, ExternalSiteConfiguration externalSiteConfig, SiteConfiguration config) {
		if (externalSiteConfig == null) return null;

		String secCallbackBeanId = config.getSecurityCallbackBeanId();
		SiteSecurityCallback siteSecCallback = (SiteSecurityCallback) CoreSpringFactory.getBean(secCallbackBeanId);

		UserSession usess = ureq.getUserSession();
		if (usess == null || usess.getRoles() == null) {
			return null;
		}

		ExternalSiteLangConfiguration langConfig = getLanguageConfiguration(ureq, externalSiteConfig);
		if (langConfig == null) {
			return null;
		}
		String icon = externalSiteConfig.getNavIconCssClass();
		return createExternalSiteInstance(langConfig, siteSecCallback, externalSiteConfig.getExternalSiteHeight(), icon);
	}

	protected ExternalSite createExternalSiteInstance(ExternalSiteLangConfiguration langConfig,
													  SiteSecurityCallback siteSecCallback, String height, String icon) {
		return new ExternalSite(this, siteSecCallback, langConfig.getTitle(), langConfig.getExternalUrl(), langConfig.isExternalUrlInIFrame(), height, icon);
	}

	protected final ExternalSiteLangConfiguration getLanguageConfiguration(UserRequest ureq, ExternalSiteConfiguration config) {
		if (config == null || config.getConfigurationList() == null) return null;
		String language = ureq.getUserSession().getLocale().getLanguage();

		ExternalSiteLangConfiguration myLangConfig = null;
		ExternalSiteLangConfiguration defaultLangConfig = null;
		for (ExternalSiteLangConfiguration langConfig : config.getConfigurationList()) {
			if (langConfig.isDefaultConfiguration()) {
				defaultLangConfig = langConfig;
			}
			if (language.equals(langConfig.getLanguage())) {
				myLangConfig = langConfig;
			}
		}

		if (myLangConfig == null) {
			myLangConfig = defaultLangConfig;
		}

		if (myLangConfig == null && !config.getConfigurationList().isEmpty()) {
			myLangConfig = config.getConfigurationList().get(0);
		}

		return myLangConfig;
	}

	protected ExternalSiteConfiguration getExternalSiteConfiguration() {
		SiteDefinitions siteModule = CoreSpringFactory.getImpl(SiteDefinitions.class);
		return siteModule.getConfigurationExternalSite1();
	}

	@Override
	public boolean isFeatureEnabled() {
		SiteConfiguration config = getSiteConfiguration();
		return config != null && config.isEnabled();
	}
}
