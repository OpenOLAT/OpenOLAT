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
package org.olat.modules.externalsite.model;

import java.util.List;

/**
 * XStream mapping class
 * <p>
 * Initial date: Nov 10, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ExternalSiteConfiguration {

	private boolean isExternalUrlInIFrame;
	private String navIconCssClass = "site_external";
	private String externalSiteHeight;
	private List<ExternalSiteLangConfiguration> configurationList;


	public boolean isExternalUrlInIFrame() {
		return isExternalUrlInIFrame;
	}

	public void setExternalUrlInIFrame(boolean externalUrlInIFrame) {
		isExternalUrlInIFrame = externalUrlInIFrame;
	}

	public String getNavIconCssClass() {
		return navIconCssClass;
	}

	public void setNavIconCssClass(String navIconCssClass) {
		this.navIconCssClass = navIconCssClass;
	}

	public String getExternalSiteHeight() {
		return externalSiteHeight;
	}

	public void setExternalSiteHeight(String externalSiteHeight) {
		this.externalSiteHeight = externalSiteHeight;
	}

	public List<ExternalSiteLangConfiguration> getConfigurationList() {
		return configurationList;
	}

	public void setConfigurationList(List<ExternalSiteLangConfiguration> configurationList) {
		this.configurationList = configurationList;
	}
}
