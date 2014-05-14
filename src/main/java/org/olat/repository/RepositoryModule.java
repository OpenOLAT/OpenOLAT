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
package org.olat.repository;

import org.olat.NewControllerFactory;
import org.olat.catalog.CatalogEntry;
import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.id.Roles;
import org.olat.core.id.context.SiteContextEntryControllerCreator;
import org.olat.core.util.StringHelper;
import org.olat.course.site.CourseSiteContextEntryControllerCreator;
import org.olat.group.BusinessGroupModule;
import org.olat.repository.site.MyCoursesSite;
import org.olat.repository.site.RepositorySite;

/**
 * Description:<br>
 * The business group module initializes the OLAT repository environment.
 * Configurations are loaded from here.
 * <P>
 * Initial Date: 04.11.2009 <br>
 * 
 * @author gnaegi
 */
public class RepositoryModule extends AbstractOLATModule {

	private static final String MANAGED_REPOENTRY_ENABLED = "managedRepositoryEntries";
	private static final String LIST_ALL_COURSES = "listallcourse";
	private static final String LIST_ALL_RESOURCETYPES = "listallresourcetypes";
	private static final String CATALOG_SITE_ENABLED = "site.catalog.enable";
	private static final String CATALOG_ENABLED = "catalog.enable";
	private static final String CATALOG_BROWSING_ENABLED = "catalog.brwosing.enable";
	
	private boolean catalogSiteEnabled;
	private boolean catalogEnabled;
	private boolean catalogBrowsingEnabled;
	private boolean listAllCourses;
	private boolean listAllResourceTypes;
	private boolean managedRepositoryEntries;
	
	private BusinessGroupModule groupModule;
	
	/**
	 * @see org.olat.core.configuration.AbstractOLATModule#init()
	 */
	@Override
	public void init() {
		// Add controller factory extension point to launch groups
		NewControllerFactory.getInstance().addContextEntryControllerCreator(RepositoryEntry.class.getSimpleName(),
				new CourseSiteContextEntryControllerCreator());
		
		NewControllerFactory.getInstance().addContextEntryControllerCreator(CatalogEntry.class.getSimpleName(),
				new CatalogContextEntryControllerCreator(this));
		
		NewControllerFactory.getInstance().addContextEntryControllerCreator(RepositorySite.class.getSimpleName(),
				new SiteContextEntryControllerCreator(RepositorySite.class));
		
		NewControllerFactory.getInstance().addContextEntryControllerCreator(MyCoursesSite.class.getSimpleName(),
				new SiteContextEntryControllerCreator(MyCoursesSite.class));
		
		updateProperties();
	}

	/**
	 * [used by Spring]
	 * @param groupModule
	 */
	public void setGroupModule(BusinessGroupModule groupModule) {
		this.groupModule = groupModule;
	}

	/**
	 * @see org.olat.core.configuration.AbstractOLATModule#initDefaultProperties()
	 */
	@Override
	protected void initDefaultProperties() {
		String listAllCoursesStr = getStringConfigParameter(LIST_ALL_COURSES, "true", false);
		listAllCourses = "true".equals(listAllCoursesStr);
		String listAllResourceTypesStr = getStringConfigParameter(LIST_ALL_RESOURCETYPES, "true", false);
		listAllResourceTypes = "true".equals(listAllResourceTypesStr);
		
		managedRepositoryEntries = getBooleanConfigParameter(MANAGED_REPOENTRY_ENABLED, false);

		catalogSiteEnabled = getBooleanConfigParameter(CATALOG_SITE_ENABLED, true);
		catalogEnabled = getBooleanConfigParameter(CATALOG_ENABLED, true);
		catalogBrowsingEnabled = getBooleanConfigParameter(CATALOG_BROWSING_ENABLED, true);
	}

	private void updateProperties() {
		String listAllCoursesStr = getStringPropertyValue(LIST_ALL_COURSES, true);
		if(StringHelper.containsNonWhitespace(listAllCoursesStr)) {
			listAllCourses = "true".equals(listAllCoursesStr);
		}
		String listAllResourceTypesStr = getStringPropertyValue(LIST_ALL_RESOURCETYPES, true);
		if(StringHelper.containsNonWhitespace(listAllResourceTypesStr)) {
			listAllResourceTypes = "true".equals(listAllResourceTypesStr);
		}
		
		String managedRepo = getStringPropertyValue(MANAGED_REPOENTRY_ENABLED, true);
		if(StringHelper.containsNonWhitespace(managedRepo)) {
			managedRepositoryEntries = "true".equals(managedRepo);
		}
		
		String catalogSite = getStringPropertyValue(CATALOG_SITE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(catalogSite)) {
			catalogSiteEnabled = "true".equals(catalogSite);
		}
		
		String catalogRepo = getStringPropertyValue(CATALOG_ENABLED, true);
		if(StringHelper.containsNonWhitespace(catalogRepo)) {
			catalogEnabled = "true".equals(catalogRepo);
		}
		
		String myCourses = getStringPropertyValue(CATALOG_BROWSING_ENABLED, true);
		if(StringHelper.containsNonWhitespace(myCourses)) {
			catalogBrowsingEnabled = "true".equals(myCourses);
		}
	}

	/**
	 * @see org.olat.core.configuration.AbstractOLATModule#initFromChangedProperties()
	 */
	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}
	
	public boolean isAcceptMembership(Roles roles) {
		return groupModule.isAcceptMembership(roles);
	}
	
	public boolean isMandatoryEnrolmentEmail(Roles roles) {
		return groupModule.isMandatoryEnrolmentEmail(roles);
	}

	public boolean isListAllCourses() {
		return listAllCourses;
	}

	public void setListAllCourses(boolean listAllCourses) {
		setBooleanProperty(LIST_ALL_COURSES, listAllCourses, true);
	}

	public boolean isListAllResourceTypes() {
		return listAllResourceTypes;
	}

	public void setListAllResourceTypes(boolean listAllResourceTypes) {
		setBooleanProperty(LIST_ALL_RESOURCETYPES, listAllResourceTypes, true);
	}

	public boolean isManagedRepositoryEntries() {
		return managedRepositoryEntries;
	}

	public void setManagedRepositoryEntries(boolean enabled) {
		setStringProperty(MANAGED_REPOENTRY_ENABLED, Boolean.toString(enabled), true);
	}
	
	public boolean isCatalogSiteEnabled() {
		return catalogSiteEnabled;
	}

	public void setCatalogSiteEnabled(boolean enabled) {
		setStringProperty(CATALOG_SITE_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isCatalogEnabled() {
		return catalogEnabled;
	}

	public void setCatalogEnabled(boolean enabled) {
		setStringProperty(CATALOG_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isCatalogBrowsingEnabled() {
		return catalogBrowsingEnabled;
	}

	public void setCatalogBrowsingEnabled(boolean enabled) {
		setStringProperty(CATALOG_BROWSING_ENABLED, Boolean.toString(enabled), true);
	}
}