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
package org.olat.group;

import org.olat.NewControllerFactory;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.id.Roles;
import org.olat.core.id.context.SiteContextEntryControllerCreator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.site.GroupsSite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * The business group module initializes the OLAT groups environment.
 * Configurations are loaded from here.
 * <P>
 * Initial Date: 04.11.2009 <br>
 * 
 * @author gnaegi
 */
@Service("businessGroupModule")
public class BusinessGroupModule extends AbstractSpringModule {

	public static String ORES_TYPE_GROUP = OresHelper.calculateTypeName(BusinessGroup.class);
	
	private static final String USER_ALLOW_CREATE_BG = "user.allowed.create";
	private static final String AUTHOR_ALLOW_CREATE_BG = "author.allowed.create";
	private static final String CONTACT_BUSINESS_CARD = "contact.business.card";
	private static final String USER_LIST_DOWNLOAD = "userlist.download.default.allowed";
	
	public static final String CONTACT_BUSINESS_CARD_NEVER = "never";
	public static final String CONTACT_BUSINESS_CARD_ALWAYS = "always";
	public static final String CONTACT_BUSINESS_CARD_GROUP_CONFIG = "groupconfig";
	
	private static final String MANDATORY_ENROLMENT_EMAIL_USERS = "mandatoryEnrolmentEmailForUsers";
	private static final String MANDATORY_ENROLMENT_EMAIL_AUTHORS = "mandatoryEnrolmentEmailForAuthors";
	private static final String MANDATORY_ENROLMENT_EMAIL_USERMANAGERS = "mandatoryEnrolmentEmailForUsermanagers";
	private static final String MANDATORY_ENROLMENT_EMAIL_GROUPMANAGERS = "mandatoryEnrolmentEmailForGroupmanagers";
	private static final String MANDATORY_ENROLMENT_EMAIL_ADMINISTRATORS = "mandatoryEnrolmentEmailForAdministrators";

	private static final String ACCEPT_MEMBERSHIP_USERS = "acceptMembershipForUsers";
	private static final String ACCEPT_MEMBERSHIP_AUTHORS = "acceptMembershipForAuthors";
	private static final String ACCEPT_MEMBERSHIP_USERMANAGERS = "acceptMembershipForUsermanagers";
	private static final String ACCEPT_MEMBERSHIP_GROUPMANAGERS = "acceptMembershipForGroupmanagers";
	private static final String ACCEPT_MEMBERSHIP_ADMINISTRATORS = "acceptMembershipForAdministrators";
	
	private static final String ALLOW_LEAVING_GROUP_BY_LEARNERS = "allowLeavingGroupCreatedByLearners";
	private static final String ALLOW_LEAVING_GROUP_BY_AUTHORS = "allowLeavingGroupCreatedByAuthors";
	private static final String ALLOW_LEAVING_GROUP_OVERRIDE = "allowLeavingGroupOverride";
	
	private static final String GROUP_MGR_LINK_COURSE_ALLOWED = "groupManagersAllowedToLinkCourses";
	private static final String RESOURCE_MGR_LINK_GROUP_ALLOWED = "resourceManagersAllowedToLinkGroups";
	
	private static final String MANAGED_GROUPS_ENABLED = "managedBusinessGroups";
	
	@Value("${group.user.create:true}")
	private boolean userAllowedCreate;
	@Value("${group.author.create}")
	private boolean authorAllowedCreate;
	@Value("${group.userlist.download.default.allowed}")
	private boolean userListDownloadDefaultAllowed;
	@Value("${group.card.contact}")
	private String contactBusinessCard;
	
	@Value("${group.mandatory.enrolment.email.users}")
	private String mandatoryEnrolmentEmailForUsers;
	@Value("${group.mandatory.enrolment.email.authors}")
	private String mandatoryEnrolmentEmailForAuthors;
	@Value("${group.mandatory.enrolment.email.usermanagers}")
	private String mandatoryEnrolmentEmailForUsermanagers;
	@Value("${group.mandatory.enrolment.email.groupmanagers}")
	private String mandatoryEnrolmentEmailForGroupmanagers;
	@Value("${group.mandatory.enrolment.email.administrators}")
	private String mandatoryEnrolmentEmailForAdministrators;

	@Value("${group.accept.membership.users}")
	private String acceptMembershipForUsers;
	@Value("${group.accept.membership.authors}")
	private String acceptMembershipForAuthors;
	@Value("${group.accept.membership.usermanagers}")
	private String acceptMembershipForUsermanagers;
	@Value("${group.accept.membership.groupmanagers}")
	private String acceptMembershipForGroupmanagers;
	@Value("${group.accept.membership.administrators}")
	private String acceptMembershipForAdministrators;
	
	@Value("${group.leaving.group.created.by.learners:true}")
	private boolean allowLeavingGroupCreatedByLearners;
	@Value("${group.leaving.group.created.by.authors:true}")
	private boolean allowLeavingGroupCreatedByAuthors;
	@Value("${group.leaving.group.override:true}")
	private boolean allowLeavingGroupOverride;

	private boolean groupManagersAllowedToLinkCourses;
	private boolean resourceManagersAllowedToLinkGroups;
	@Value("${group.managed}")
	private boolean managedBusinessGroups;

	@Autowired
	public BusinessGroupModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	/**
	 * @see org.olat.core.configuration.AbstractOLATModule#init()
	 */
	@Override
	public void init() {
		// Add controller factory extension point to launch groups
		NewControllerFactory.getInstance().addContextEntryControllerCreator(BusinessGroup.class.getSimpleName(),
				new BusinessGroupContextEntryControllerCreator());
		NewControllerFactory.getInstance().addContextEntryControllerCreator("GroupCard",
				new BusinessGroupCardContextEntryControllerCreator());
		NewControllerFactory.getInstance().addContextEntryControllerCreator(GroupsSite.class.getSimpleName(),
				new SiteContextEntryControllerCreator(GroupsSite.class));
		
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		//set properties
		String userAllowed = getStringPropertyValue(USER_ALLOW_CREATE_BG, true);
		if(StringHelper.containsNonWhitespace(userAllowed)) {
			userAllowedCreate = "true".equals(userAllowed);
		}
		String authorAllowed = getStringPropertyValue(AUTHOR_ALLOW_CREATE_BG, true);
		if(StringHelper.containsNonWhitespace(authorAllowed)) {
			authorAllowedCreate = "true".equals(authorAllowed);
		}
		
		String contactAllowed = getStringPropertyValue(CONTACT_BUSINESS_CARD, true);
		if(StringHelper.containsNonWhitespace(contactAllowed)) {
			contactBusinessCard = contactAllowed;
		}
		
		String downloadAllowed = getStringPropertyValue(USER_LIST_DOWNLOAD, true);
		if(StringHelper.containsNonWhitespace(downloadAllowed)) {
			userListDownloadDefaultAllowed = "true".equals(downloadAllowed);
		}

		String enabled = getStringPropertyValue(MANDATORY_ENROLMENT_EMAIL_USERS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			mandatoryEnrolmentEmailForUsers = enabled;
		}
		enabled = getStringPropertyValue(MANDATORY_ENROLMENT_EMAIL_AUTHORS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			mandatoryEnrolmentEmailForAuthors = enabled;
		}
		enabled = getStringPropertyValue(MANDATORY_ENROLMENT_EMAIL_USERMANAGERS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			mandatoryEnrolmentEmailForUsermanagers = enabled;
		}
		enabled = getStringPropertyValue(MANDATORY_ENROLMENT_EMAIL_GROUPMANAGERS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			mandatoryEnrolmentEmailForGroupmanagers = enabled;
		}
		enabled = getStringPropertyValue(MANDATORY_ENROLMENT_EMAIL_ADMINISTRATORS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			mandatoryEnrolmentEmailForAdministrators = enabled;
		}

		enabled = getStringPropertyValue(ACCEPT_MEMBERSHIP_USERS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			acceptMembershipForUsers = enabled;
		}
		enabled = getStringPropertyValue(ACCEPT_MEMBERSHIP_AUTHORS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			acceptMembershipForAuthors = enabled;
		}
		enabled = getStringPropertyValue(ACCEPT_MEMBERSHIP_USERMANAGERS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			acceptMembershipForUsermanagers = enabled;
		}
		enabled = getStringPropertyValue(ACCEPT_MEMBERSHIP_GROUPMANAGERS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			acceptMembershipForGroupmanagers = enabled;
		}
		enabled = getStringPropertyValue(ACCEPT_MEMBERSHIP_ADMINISTRATORS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			acceptMembershipForAdministrators = enabled;
		}
		
		String linkCourseAllowed = getStringPropertyValue(GROUP_MGR_LINK_COURSE_ALLOWED, true);
		if(StringHelper.containsNonWhitespace(linkCourseAllowed)) {
			groupManagersAllowedToLinkCourses = "true".equals(linkCourseAllowed);
		}
		String linkGroupAllowed = getStringPropertyValue(RESOURCE_MGR_LINK_GROUP_ALLOWED, true);
		if(StringHelper.containsNonWhitespace(linkGroupAllowed)) {
			resourceManagersAllowedToLinkGroups = "true".equals(linkGroupAllowed);
		}
		
		String allowLeavingIfCreatedByLearners = getStringPropertyValue(ALLOW_LEAVING_GROUP_BY_LEARNERS, true);
		if(StringHelper.containsNonWhitespace(allowLeavingIfCreatedByLearners)) {
			allowLeavingGroupCreatedByLearners = "true".equals(allowLeavingIfCreatedByLearners);
		}
		String allowLeavingIfCreatedByAuthors = getStringPropertyValue(ALLOW_LEAVING_GROUP_BY_AUTHORS, true);
		if(StringHelper.containsNonWhitespace(allowLeavingIfCreatedByAuthors)) {
			allowLeavingGroupCreatedByAuthors = "true".equals(allowLeavingIfCreatedByAuthors);
		}
		String allowLeavingOverride = getStringPropertyValue(ALLOW_LEAVING_GROUP_OVERRIDE, true);
		if(StringHelper.containsNonWhitespace(allowLeavingOverride)) {
			allowLeavingGroupOverride = "true".equals(allowLeavingOverride);
		}
		
		String managedGroups = getStringPropertyValue(MANAGED_GROUPS_ENABLED, true);
		if(StringHelper.containsNonWhitespace(managedGroups)) {
			managedBusinessGroups = "true".equals(managedGroups);
		}
	}

	public boolean isAllowedCreate(Roles roles) {
		if(roles.isOLATAdmin() || roles.isGroupManager()
				|| (roles.isAuthor() && isAuthorAllowedCreate())
				|| (!roles.isGuestOnly() && !roles.isInvitee() && isUserAllowedCreate())) {
			return true;
		}
		return false;
	}

	public boolean isUserAllowedCreate() {
		return userAllowedCreate;
	}

	public void setUserAllowedCreate(boolean userAllowedCreate) {
		setStringProperty(USER_ALLOW_CREATE_BG, Boolean.toString(userAllowedCreate), true);
	}

	public boolean isAuthorAllowedCreate() {
		return authorAllowedCreate;
	}

	public void setAuthorAllowedCreate(boolean authorAllowedCreate) {
		setStringProperty(AUTHOR_ALLOW_CREATE_BG, Boolean.toString(authorAllowedCreate), true);
	}

	public String getContactBusinessCard() {
		return contactBusinessCard;
	}

	public void setContactBusinessCard(String contactBusinessCard) {
		setStringProperty(CONTACT_BUSINESS_CARD, contactBusinessCard, true);
	}

	public boolean isUserListDownloadDefaultAllowed() {
		return userListDownloadDefaultAllowed;
	}

	public void setUserListDownloadDefaultAllowed(boolean userListDownload) {
		setStringProperty(USER_LIST_DOWNLOAD, Boolean.toString(userListDownload), true);
	}
	
	public boolean isMandatoryEnrolmentEmail(Roles roles) {
		if(roles == null) return true;
		if(roles.isOLATAdmin()) {
			return "true".equals(getMandatoryEnrolmentEmailForAdministrators());
		}
		if(roles.isGroupManager()) {
			return "true".equals(getMandatoryEnrolmentEmailForGroupmanagers());
		}
		if(roles.isUserManager()) {
			return "true".equals(getMandatoryEnrolmentEmailForUsermanagers());
		}
		if(roles.isAuthor()) {
			return "true".equals(getMandatoryEnrolmentEmailForAuthors());
		}
		if(roles.isInvitee()) {
			return true;
		}
		return "true".equals(getMandatoryEnrolmentEmailForUsers());
	}

	public String getMandatoryEnrolmentEmailForUsers() {
		return mandatoryEnrolmentEmailForUsers;
	}

	public void setMandatoryEnrolmentEmailForUsers(String mandatory) {
		setStringProperty(MANDATORY_ENROLMENT_EMAIL_USERS, mandatory, true);
	}

	public String getMandatoryEnrolmentEmailForAuthors() {
		return mandatoryEnrolmentEmailForAuthors;
	}

	public void setMandatoryEnrolmentEmailForAuthors(String mandatory) {
		setStringProperty(MANDATORY_ENROLMENT_EMAIL_AUTHORS, mandatory, true);
	}

	public String getMandatoryEnrolmentEmailForUsermanagers() {
		return mandatoryEnrolmentEmailForUsermanagers;
	}

	public void setMandatoryEnrolmentEmailForUsermanagers(String mandatory) {
		setStringProperty(MANDATORY_ENROLMENT_EMAIL_USERMANAGERS, mandatory, true);
	}

	public String getMandatoryEnrolmentEmailForGroupmanagers() {
		return mandatoryEnrolmentEmailForGroupmanagers;
	}

	public void setMandatoryEnrolmentEmailForGroupmanagers(String mandatory) {
		setStringProperty(MANDATORY_ENROLMENT_EMAIL_GROUPMANAGERS, mandatory, true);
	}

	public String getMandatoryEnrolmentEmailForAdministrators() {
		return mandatoryEnrolmentEmailForAdministrators;
	}

	public void setMandatoryEnrolmentEmailForAdministrators(String mandatory) {
		setStringProperty(MANDATORY_ENROLMENT_EMAIL_ADMINISTRATORS, mandatory, true);
	}
	
	public boolean isAcceptMembership(Roles roles) {
		if(roles == null) return true;
		if(roles.isOLATAdmin()) {
			return "true".equals(getAcceptMembershipForAdministrators());
		}
		if(roles.isGroupManager()) {
			return "true".equals(getAcceptMembershipForGroupmanagers());
		}
		if(roles.isUserManager()) {
			return "true".equals(getAcceptMembershipForUsermanagers());
		}
		if(roles.isAuthor()) {
			return "true".equals(getAcceptMembershipForAuthors());
		}
		if(roles.isInvitee()) {
			return true;
		}
		return "true".equals(getAcceptMembershipForUsers());
	}

	public String getAcceptMembershipForUsers() {
		return acceptMembershipForUsers;
	}

	public void setAcceptMembershipForUsers(String mandatory) {
		setStringProperty(ACCEPT_MEMBERSHIP_USERS, mandatory, true);
	}

	public String getAcceptMembershipForAuthors() {
		return acceptMembershipForAuthors;
	}

	public void setAcceptMembershipForAuthors(String mandatory) {
		setStringProperty(ACCEPT_MEMBERSHIP_AUTHORS, mandatory, true);
	}

	public String getAcceptMembershipForUsermanagers() {
		return acceptMembershipForUsermanagers;
	}

	public void setAcceptMembershipForUsermanagers(String mandatory) {
		setStringProperty(ACCEPT_MEMBERSHIP_USERMANAGERS, mandatory, true);
	}

	public String getAcceptMembershipForGroupmanagers() {
		return acceptMembershipForGroupmanagers;
	}

	public void setAcceptMembershipForGroupmanagers(String mandatory) {
		setStringProperty(ACCEPT_MEMBERSHIP_GROUPMANAGERS, mandatory, true);
	}

	public String getAcceptMembershipForAdministrators() {
		return acceptMembershipForAdministrators;
	}

	public void setAcceptMembershipForAdministrators(String mandatory) {
		setStringProperty(ACCEPT_MEMBERSHIP_ADMINISTRATORS, mandatory, true);
	}

	public boolean isGroupManagersAllowedToLinkCourses() {
		return groupManagersAllowedToLinkCourses;
	}

	public void setGroupManagersAllowedToLinkCourses(boolean enabled) {
		setStringProperty(GROUP_MGR_LINK_COURSE_ALLOWED, Boolean.toString(enabled), true);
	}

	public boolean isResourceManagersAllowedToLinkGroups() {
		return resourceManagersAllowedToLinkGroups;
	}

	public void setResourceManagersAllowedToLinkGroups(boolean enabled) {
		setStringProperty(RESOURCE_MGR_LINK_GROUP_ALLOWED, Boolean.toString(enabled), true);
	}

	public boolean isAllowLeavingGroupCreatedByLearners() {
		return allowLeavingGroupCreatedByLearners;
	}

	public void setAllowLeavingGroupCreatedByLearners(boolean allow) {
		this.allowLeavingGroupCreatedByLearners = allow;
		setStringProperty(ALLOW_LEAVING_GROUP_BY_LEARNERS, Boolean.toString(allow), true);
	}

	public boolean isAllowLeavingGroupCreatedByAuthors() {
		return allowLeavingGroupCreatedByAuthors;
	}

	public void setAllowLeavingGroupCreatedByAuthors(boolean allow) {
		this.allowLeavingGroupCreatedByAuthors = allow;
		setStringProperty(ALLOW_LEAVING_GROUP_BY_AUTHORS, Boolean.toString(allow), true);
	}

	public boolean isAllowLeavingGroupOverride() {
		return allowLeavingGroupOverride;
	}

	public void setAllowLeavingGroupOverride(boolean allow) {
		this.allowLeavingGroupOverride = allow;
		setStringProperty(ALLOW_LEAVING_GROUP_OVERRIDE, Boolean.toString(allow), true);
	}

	public boolean isManagedBusinessGroups() {
		return managedBusinessGroups;
	}

	public void setManagedBusinessGroups(boolean enabled) {
		setStringProperty(MANAGED_GROUPS_ENABLED, Boolean.toString(enabled), true);
	}
}