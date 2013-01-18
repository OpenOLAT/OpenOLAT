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
package org.olat.catalog;

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.util.StringHelper;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CatalogModule extends AbstractOLATModule {
	
	private static final String CATALOG_SITE_ENABLED = "site.catalog.enable";
	private static final String CATALOG_REPO_ENABLED = "repo.catalog.enable";
	private static final String MY_COURSES_ENABLED = "my.courses.enable";
	
	private boolean catalogSiteEnabled;
	private boolean catalogRepoEnabled;
	private boolean myCoursesEnabled;

	@Override
	public void init() {
		String catalogSite = getStringPropertyValue(CATALOG_SITE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(catalogSite)) {
			catalogSiteEnabled = "true".equals(catalogSite);
		}
		
		String catalogRepo = getStringPropertyValue(CATALOG_REPO_ENABLED, true);
		if(StringHelper.containsNonWhitespace(catalogRepo)) {
			catalogRepoEnabled = "true".equals(catalogRepo);
		}
		
		String myCourses = getStringPropertyValue(MY_COURSES_ENABLED, true);
		if(StringHelper.containsNonWhitespace(myCourses)) {
			myCoursesEnabled = "true".equals(myCourses);
		}
	}
	
	@Override
	protected void initDefaultProperties() {
		catalogSiteEnabled = getBooleanConfigParameter(CATALOG_SITE_ENABLED, true);
		catalogRepoEnabled = getBooleanConfigParameter(CATALOG_REPO_ENABLED, true);
		myCoursesEnabled = getBooleanConfigParameter(MY_COURSES_ENABLED, true);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		moduleConfigProperties = persistedProperties;
	}

	public boolean isCatalogSiteEnabled() {
		return catalogSiteEnabled;
	}

	public void setCatalogSiteEnabled(boolean enabled) {
		setStringProperty(CATALOG_SITE_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isCatalogRepoEnabled() {
		return catalogRepoEnabled;
	}

	public void setCatalogRepoEnabled(boolean enabled) {
		setStringProperty(CATALOG_REPO_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isMyCoursesEnabled() {
		return myCoursesEnabled;
	}

	public void setMyCoursesEnabled(boolean enabled) {
		setStringProperty(MY_COURSES_ENABLED, Boolean.toString(enabled), true);
	}


	
	

}
