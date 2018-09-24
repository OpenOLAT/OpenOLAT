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
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.id.Roles;
import org.olat.core.id.context.SiteContextEntryControllerCreator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.course.site.CourseSite;
import org.olat.course.site.CourseSiteContextEntryControllerCreator;
import org.olat.group.BusinessGroupModule;
import org.olat.repository.site.CatalogAdminSite;
import org.olat.repository.site.MyCoursesSite;
import org.olat.repository.site.RepositorySite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * The business group module initializes the OLAT repository environment.
 * Configurations are loaded from here.
 * <P>
 * Initial Date: 04.11.2009 <br>
 * 
 * @author gnaegi
 */
@Service("repositoryModule")
public class RepositoryModule extends AbstractSpringModule {
	
	private static final OLog log = Tracing.createLoggerFor(RepositoryModule.class);

	private static final String MANAGED_REPOENTRY_ENABLED = "managedRepositoryEntries";
	private static final String CATALOG_SITE_ENABLED = "site.catalog.enable";
	private static final String CATALOG_ENABLED = "catalog.enable";
	private static final String CATALOG_BROWSING_ENABLED = "catalog.brwosing.enable";
	private static final String MYCOURSES_SEARCH_ENABLED = "mycourses.search.enabled";
	private static final String MYCOURSES_ALL_RESOURCES_ENABLED = "mycourses.all.resources.enabled";
	
	private static final String COMMENT_ENABLED = "repo.comment.enabled";
	private static final String RATING_ENABLED = "repo.rating.enabled";
	
	private static final String ALLOW_TO_LEAVE_DEFAULT_OPTION = "repo.allow.to.leave";
	
	private static final String LIFECYCLE_AUTO_CLOSE = "repo.lifecycle.auto.close";
	private static final String LIFECYCLE_AUTO_DELETE = "repo.lifecycle.auto.delete";
	private static final String LIFECYCLE_NOTIFICATION_CLOSE_DELETE = "rrepo.lifecylce.notification.close.delete";
	
	private static final String TAXONOMY_TREE_KEY = "taxonomy.tree.key";

	@Value("${site.catalog.enable:true}")
	private boolean catalogSiteEnabled;
	@Value("${catalog.enable:true}")
	private boolean catalogEnabled;
	@Value("${repo.catalog.browsing.enable}")
	private boolean catalogBrowsingEnabled;
	
	@Value("${repo.managed}")
	private boolean managedRepositoryEntries;
	@Value("${mycourses.search.enabled:true}")
	private boolean myCoursesSearchEnabled;
	@Value("${mycourses.all.resources.enabled:true}")
	private boolean listAllResourceTypes;
	@Value("${repo.comment.enabled:true}")
	private boolean commentEnabled;
	@Value("${repo.rating.enabled:true}")
	private boolean ratingEnabled;

	@Value("${repo.lifecycle.auto.close:}")
	private String lifecycleAutoClose;
	@Value("${repo.lifecycle.auto.delete:}")
	private String lifecycleAutoDelete;
	@Value("${repo.lifecylce.notification.close.delete:}")
	private String lifecycleNotificationByCloseDelete;
	
	@Value("${repo.allow.to.leave:atAnyTime}")
	private String defaultAllowToLeaveOption;
	
	private String taxonomyTreeKey;
	
	@Autowired
	private BusinessGroupModule groupModule;
	
	@Autowired
	public RepositoryModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	/**
	 * @see org.olat.core.configuration.AbstractOLATModule#init()
	 */
	@Override
	public void init() {
		// Add controller factory extension point to launch groups
		NewControllerFactory.getInstance().addContextEntryControllerCreator(RepositoryEntry.class.getSimpleName(),
				new CourseSiteContextEntryControllerCreator());
		
		NewControllerFactory.getInstance().addContextEntryControllerCreator(CourseSite.class.getSimpleName(),
				new CourseSiteContextEntryControllerCreator());
		
		NewControllerFactory.getInstance().addContextEntryControllerCreator(CatalogEntry.class.getSimpleName(),
				new CatalogContextEntryControllerCreator(this));
		NewControllerFactory.getInstance().addContextEntryControllerCreator("Catalog",
				new CatalogContextEntryControllerCreator(this));
		NewControllerFactory.getInstance().addContextEntryControllerCreator("CatalogAdmin",
				new SiteContextEntryControllerCreator(CatalogAdminSite.class));
		
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

	private void updateProperties() {
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
		
		String myCoursesSearch = getStringPropertyValue(MYCOURSES_SEARCH_ENABLED, true);
		if(StringHelper.containsNonWhitespace(myCoursesSearch)) {
			myCoursesSearchEnabled = "true".equals(myCoursesSearch);
		}
		
		String myCoursesAllResources = getStringPropertyValue(MYCOURSES_ALL_RESOURCES_ENABLED, true);
		if(StringHelper.containsNonWhitespace(myCoursesAllResources)) {
			listAllResourceTypes = "true".equals(myCoursesAllResources);
		}
		
		String comment = getStringPropertyValue(COMMENT_ENABLED, true);
		if(StringHelper.containsNonWhitespace(comment)) {
			commentEnabled = "true".equals(comment);
		}
		
		String rating = getStringPropertyValue(RATING_ENABLED, true);
		if(StringHelper.containsNonWhitespace(rating)) {
			ratingEnabled = "true".equals(rating);
		}
		
		String leaveOption = getStringPropertyValue(ALLOW_TO_LEAVE_DEFAULT_OPTION, true);
		if(StringHelper.containsNonWhitespace(leaveOption)) {
			defaultAllowToLeaveOption = leaveOption;
		}
		
		String autoClose = getStringPropertyValue(LIFECYCLE_AUTO_CLOSE, true);
		if(StringHelper.containsNonWhitespace(autoClose)) {
			lifecycleAutoClose = autoClose;
		}
		
		String autoDelete = getStringPropertyValue(LIFECYCLE_AUTO_DELETE, true);
		if(StringHelper.containsNonWhitespace(autoDelete)) {
			lifecycleAutoDelete = autoDelete;
		}
		
		String notificationCloseDelete = getStringPropertyValue(LIFECYCLE_NOTIFICATION_CLOSE_DELETE, true);
		if(StringHelper.containsNonWhitespace(notificationCloseDelete)) {
			lifecycleNotificationByCloseDelete = notificationCloseDelete;
		}
		
		String taxonomyTreeKeyObj = getStringPropertyValue(TAXONOMY_TREE_KEY, true);
		if(StringHelper.containsNonWhitespace(taxonomyTreeKeyObj)) {
			taxonomyTreeKey = taxonomyTreeKeyObj;
		}
	}

	/**
	 * @see org.olat.core.configuration.AbstractOLATModule#initFromChangedProperties()
	 */
	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	public boolean isAcceptMembership(Roles roles) {
		return groupModule.isAcceptMembership(roles);
	}
	
	public boolean isMandatoryEnrolmentEmail(Roles roles) {
		return groupModule.isMandatoryEnrolmentEmail(roles);
	}

	public boolean isListAllResourceTypes() {
		return listAllResourceTypes;
	}
	
	public void setListAllResourceTypes(boolean enabled) {
		listAllResourceTypes = enabled;
		setStringProperty(MYCOURSES_ALL_RESOURCES_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isManagedRepositoryEntries() {
		return managedRepositoryEntries;
	}

	public void setManagedRepositoryEntries(boolean enabled) {
		managedRepositoryEntries = enabled;
		setStringProperty(MANAGED_REPOENTRY_ENABLED, Boolean.toString(enabled), true);
	}
	
	public boolean isCatalogSiteEnabled() {
		return catalogSiteEnabled;
	}

	public void setCatalogSiteEnabled(boolean enabled) {
		catalogSiteEnabled = enabled;
		setStringProperty(CATALOG_SITE_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isCatalogEnabled() {
		return catalogEnabled;
	}

	public void setCatalogEnabled(boolean enabled) {
		catalogEnabled = enabled;
		setStringProperty(CATALOG_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isCatalogBrowsingEnabled() {
		return catalogBrowsingEnabled;
	}

	public void setCatalogBrowsingEnabled(boolean enabled) {
		catalogBrowsingEnabled = enabled;
		setStringProperty(CATALOG_BROWSING_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isMyCoursesSearchEnabled() {
		return myCoursesSearchEnabled;
	}

	public void setMyCoursesSearchEnabled(boolean enabled) {
		myCoursesSearchEnabled = enabled;
		setStringProperty(MYCOURSES_SEARCH_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isCommentEnabled() {
		return commentEnabled;
	}

	public void setCommentEnabled(boolean enabled) {
		commentEnabled = enabled;
		setStringProperty(COMMENT_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isRatingEnabled() {
		return ratingEnabled;
	}

	public void setRatingEnabled(boolean enabled) {
		ratingEnabled = enabled;
		setStringProperty(RATING_ENABLED, Boolean.toString(enabled), true);
	}
	
	public RepositoryEntryAllowToLeaveOptions getAllowToLeaveDefaultOption() {
		if(StringHelper.containsNonWhitespace(defaultAllowToLeaveOption)) {
			try {
				return RepositoryEntryAllowToLeaveOptions.valueOf(defaultAllowToLeaveOption);
			} catch (Exception e) {
				log.error("Unrecognised option for repo.allow.to.leave: " + defaultAllowToLeaveOption);
				return RepositoryEntryAllowToLeaveOptions.atAnyTime;
			}
		}
		return RepositoryEntryAllowToLeaveOptions.atAnyTime;
	}
	
	public void setAllowToLeaveDefaultOption(RepositoryEntryAllowToLeaveOptions option) {
		if(option == null) {
			defaultAllowToLeaveOption = null;
			setStringProperty(ALLOW_TO_LEAVE_DEFAULT_OPTION, "", true);
		} else {
			defaultAllowToLeaveOption = option.name();
			setStringProperty(ALLOW_TO_LEAVE_DEFAULT_OPTION, option.name(), true);
		}
	}

	public String getLifecycleAutoClose() {
		return lifecycleAutoClose;
	}
	
	public RepositoryEntryLifeCycleValue getLifecycleAutoCloseValue() {
		return RepositoryEntryLifeCycleValue.parse(lifecycleAutoClose);
	}

	public void setLifecycleAutoClose(String lifecycleAutoClose) {
		this.lifecycleAutoClose = lifecycleAutoClose;
		setStringProperty(LIFECYCLE_AUTO_CLOSE, lifecycleAutoClose, true);
	}

	public String getLifecycleAutoDelete() {
		return lifecycleAutoDelete;
	}
	
	public RepositoryEntryLifeCycleValue getLifecycleAutoDeleteValue() {
		return RepositoryEntryLifeCycleValue.parse(lifecycleAutoDelete);
	}

	public void setLifecycleAutoDelete(String lifecycleAutoDelete) {
		this.lifecycleAutoDelete = lifecycleAutoDelete;
		setStringProperty(LIFECYCLE_AUTO_DELETE, lifecycleAutoDelete, true);
	}
	
	public boolean isLifecycleNotificationByCloseDeleteEnabled() {
		return "enabled".equals(lifecycleNotificationByCloseDelete);
	}
	
	public void setLifecycleNotificationByCloseDeleteEnabled(boolean enable) {
		lifecycleNotificationByCloseDelete = enable ? "enabled" : "disabled";
		setStringProperty(LIFECYCLE_NOTIFICATION_CLOSE_DELETE, lifecycleNotificationByCloseDelete, true);
	}

	public String getTaxonomyTreeKey() {
		return taxonomyTreeKey;
	}

	public void setTaxonomyTreeKey(String taxonomyTreeKey) {
		this.taxonomyTreeKey = taxonomyTreeKey;
		setStringProperty(TAXONOMY_TREE_KEY, taxonomyTreeKey, true);
	}
}