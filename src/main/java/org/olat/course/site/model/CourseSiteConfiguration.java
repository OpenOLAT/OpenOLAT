/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.site.model;

import java.util.List;

/**
 * 
 * XStream mapping class
 * 
 * Initial date: 17.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseSiteConfiguration {

	private boolean toolbar;
	private String navIconCssClass = "site_demo_icon";
	private List<LanguageConfiguration> configurations;

	public boolean isToolbar() {
		return toolbar;
	}

	public void setToolbar(boolean toolbar) {
		this.toolbar = toolbar;
	}

	public String getNavIconCssClass() {
		return navIconCssClass;
	}

	public void setNavIconCssClass(String navIconCssClass) {
		this.navIconCssClass = navIconCssClass;
	}

	public List<LanguageConfiguration> getConfigurations() {
		return configurations;
	}

	public void setConfigurations(List<LanguageConfiguration> configurations) {
		this.configurations = configurations;
	}
}
