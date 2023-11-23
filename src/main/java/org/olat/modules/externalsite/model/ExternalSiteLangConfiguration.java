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

/**
 * XStream mapping class
 * <p>
 * Initial date: Nov 17, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ExternalSiteLangConfiguration {

	private boolean defaultConfiguration;
	private String language;
	private String title;
	private String externalUrl;
	private boolean isExternalUrlInIFrame;

	public ExternalSiteLangConfiguration(String language) {
		this.language = language;
	}

	public ExternalSiteLangConfiguration() {
		//
	}

	public boolean isDefaultConfiguration() {
		return defaultConfiguration;
	}

	public void setDefaultConfiguration(boolean defaultConfiguration) {
		this.defaultConfiguration = defaultConfiguration;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getExternalUrl() {
		return externalUrl;
	}

	public void setExternalUrl(String externalUrl) {
		this.externalUrl = externalUrl;
	}

	public boolean isExternalUrlInIFrame() {
		return isExternalUrlInIFrame;
	}

	public void setExternalUrlInIFrame(boolean externalUrlInIFrame) {
		isExternalUrlInIFrame = externalUrlInIFrame;
	}
}
