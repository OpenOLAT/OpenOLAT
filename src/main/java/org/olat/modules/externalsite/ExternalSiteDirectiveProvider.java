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

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import org.olat.core.commons.services.csp.CSPDirectiveProvider;
import org.olat.core.gui.control.navigation.SiteDefinitions;
import org.olat.modules.externalsite.model.ExternalSiteLangConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial date: Nov 21, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Component
public class ExternalSiteDirectiveProvider implements CSPDirectiveProvider {

	@Autowired
	private SiteDefinitions sitesModule;

	@Override
	public Collection<String> getScriptSrcUrls() {
		return Collections.emptyList();
	}

	@Override
	public Collection<String> getImgSrcUrls() {
		return Collections.emptyList();
	}

	@Override
	public Collection<String> getFontSrcUrls() {
		return Collections.emptyList();
	}

	@Override
	public Collection<String> getConnectSrcUrls() {
		return Collections.emptyList();
	}

	@Override
	public Collection<String> getFrameSrcUrls() {
		Collection<String> externalSiteConfig1 = sitesModule.getConfigurationExternalSite1() != null ?
				sitesModule.getConfigurationExternalSite1()
						.getConfigurationList()
						.stream()
						.map(ExternalSiteLangConfiguration::getExternalUrl)
						.toList()
				: Collections.emptyList();
		Collection<String> externalSiteConfig2 = sitesModule.getConfigurationExternalSite2() != null ?
				sitesModule.getConfigurationExternalSite2()
						.getConfigurationList()
						.stream()
						.map(ExternalSiteLangConfiguration::getExternalUrl)
						.toList()
				: Collections.emptyList();

		return Stream.concat(externalSiteConfig1.stream(), externalSiteConfig2.stream()).distinct().toList();
	}

	@Override
	public Collection<String> getMediaSrcUrls() {
		return Collections.emptyList();
	}
}
