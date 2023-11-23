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
import org.olat.core.gui.control.navigation.SiteDefinitions;
import org.olat.core.gui.control.navigation.SiteSecurityCallback;
import org.olat.modules.externalsite.model.ExternalSiteConfiguration;
import org.olat.modules.externalsite.model.ExternalSiteLangConfiguration;

/**
 * Initial date: Nov 10, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ExternalSiteDef2 extends ExternalSiteDef {

	@Override
	protected ExternalSiteConfiguration getExternalSiteConfiguration() {
		SiteDefinitions siteModule = CoreSpringFactory.getImpl(SiteDefinitions.class);
		return siteModule.getConfigurationExternalSite2();
	}

	@Override
	protected ExternalSite createExternalSiteInstance(ExternalSiteLangConfiguration langConfig,
													  SiteSecurityCallback siteSecCallback, String height, String icon) {
		return new ExternalSite2(this, siteSecCallback, langConfig.getTitle(), langConfig.getExternalUrl(),
				langConfig.isExternalUrlInIFrame(), height, icon);
	}
}
