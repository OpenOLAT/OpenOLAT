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

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.id.Roles;
import org.olat.core.id.context.SiteContextEntryControllerCreator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.site.GroupsSite;
import org.olat.group.ui.lifecycle.BusinessGroupLifecycleTypeEnum;
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

	public static final String ORES_TYPE_GROUP = OresHelper.calculateTypeName(BusinessGroup.class);
	
	/**
	 * Graduate from administrator to user by importance
	 */
	private static final OrganisationRoles[] privacyRoles = new OrganisationRoles[]{
			OrganisationRoles.administrator, OrganisationRoles.sysadmin,
			OrganisationRoles.rolesmanager, OrganisationRoles.usermanager, 
			OrganisationRoles.learnresourcemanager, OrganisationRoles.groupmanager, 
			OrganisationRoles.poolmanager, OrganisationRoles.curriculummanager,
			OrganisationRoles.lecturemanager, OrganisationRoles.qualitymanager,
			OrganisationRoles.linemanager, OrganisationRoles.principal,
			OrganisationRoles.author, OrganisationRoles.user,
	};
	
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
	private static final String MANDATORY_ENROLMENT_EMAIL_ROLESMANAGERS = "mandatoryEnrolmentEmailForRolesmanagers";
	private static final String MANDATORY_ENROLMENT_EMAIL_GROUPMANAGERS = "mandatoryEnrolmentEmailForGroupmanagers";
	private static final String MANDATORY_ENROLMENT_EMAIL_LEARNRESOURCEMANAGERS = "mandatoryEnrolmentEmailForLearnresourcemanagers";
	private static final String MANDATORY_ENROLMENT_EMAIL_POOLMANAGERS = "mandatoryEnrolmentEmailForPoolmanagers";
	private static final String MANDATORY_ENROLMENT_EMAIL_CURRICULUMMANAGERS = "mandatoryEnrolmentEmailForCurriculummanagers";
	private static final String MANDATORY_ENROLMENT_EMAIL_LECTUREMANAGERS = "mandatoryEnrolmentEmailForLecturemanagers";
	private static final String MANDATORY_ENROLMENT_EMAIL_QUALITYMANAGERS = "mandatoryEnrolmentEmailForQualitymanagers";
	private static final String MANDATORY_ENROLMENT_EMAIL_LINEMANAGERS = "mandatoryEnrolmentEmailForLinemanagers";
	private static final String MANDATORY_ENROLMENT_EMAIL_PRINCIPALS = "mandatoryEnrolmentEmailForPrincipals";
	private static final String MANDATORY_ENROLMENT_EMAIL_ADMINISTRATORS = "mandatoryEnrolmentEmailForAdministrators";
	private static final String MANDATORY_ENROLMENT_EMAIL_SYSTEMADMINS = "mandatoryEnrolmentEmailForSystemAdmins";

	private static final String ACCEPT_MEMBERSHIP_USERS = "acceptMembershipForUsers";
	private static final String ACCEPT_MEMBERSHIP_AUTHORS = "acceptMembershipForAuthors";
	private static final String ACCEPT_MEMBERSHIP_USERMANAGERS = "acceptMembershipForUsermanagers";
	private static final String ACCEPT_MEMBERSHIP_ROLESMANAGERS = "acceptMembershipForRolesmanagers";
	private static final String ACCEPT_MEMBERSHIP_GROUPMANAGERS = "acceptMembershipForGroupmanagers";
	private static final String ACCEPT_MEMBERSHIP_LEARNRESOURCEMANAGERS = "acceptMembershipForLearnresourcemanagers";
	private static final String ACCEPT_MEMBERSHIP_POOLMANAGERS = "acceptMembershipForPoolmanagers";
	private static final String ACCEPT_MEMBERSHIP_CURRICULUMMANAGERS = "acceptMembershipForCurriculummanagers";
	private static final String ACCEPT_MEMBERSHIP_LECTUREMANAGERS = "acceptMembershipForLecturemanagers";
	private static final String ACCEPT_MEMBERSHIP_QUALITYMANAGERS = "acceptMembershipForQualitymanagers";
	private static final String ACCEPT_MEMBERSHIP_LINEMANAGERS = "acceptMembershipForLinemanagers";
	private static final String ACCEPT_MEMBERSHIP_PRINCIPALS = "acceptMembershipForPrincipals";
	private static final String ACCEPT_MEMBERSHIP_ADMINISTRATORS = "acceptMembershipForAdministrators";
	private static final String ACCEPT_MEMBERSHIP_SYSTEMADMINS = "acceptMembershipForSystemAdmins";
	
	private static final String ALLOW_LEAVING_GROUP_BY_LEARNERS = "allowLeavingGroupCreatedByLearners";
	private static final String ALLOW_LEAVING_GROUP_BY_AUTHORS = "allowLeavingGroupCreatedByAuthors";
	private static final String ALLOW_LEAVING_GROUP_OVERRIDE = "allowLeavingGroupOverride";
	
	private static final String GROUP_MGR_LINK_COURSE_ALLOWED = "groupManagersAllowedToLinkCourses";
	private static final String RESOURCE_MGR_LINK_GROUP_ALLOWED = "resourceManagersAllowedToLinkGroups";
	
	private static final String MANAGED_GROUPS_ENABLED = "managedBusinessGroups";
	
	private static final String GROUP_LIFECYCLE = "group.lifecycle";
	private static final String GROUP_LIFECYCLE_EXCLUDE_MANAGED = "group.lifecycle.exclude.managed";
	private static final String GROUP_LIFECYCLE_EXCLUDE_LTI = "group.lifecycle.exclude.lti";
	
	private static final String GROUP_AUTOMATIC_DEACTIVATION = "group.automatic.inactivation";
	private static final String GROUP_NUM_OF_DAYS_BEFORE_DEACTIVATION = "group.days.before.deactivation";
	private static final String GROUP_NUM_OF_DAYS_REACTIVATION_PERIOD = "group.days.reactivation.period";
	private static final String GROUP_MAIL_BEFORE_DEACTIVATION = "group.mail.before.deactivation";
	private static final String GROUP_NUM_OF_DAYS_BEFORE_MAIL_DEACTIVATION = "group.days.before.mail.deactivation";
	private static final String GROUP_MAIL_AFTER_DEACTIVATION = "group.mail.after.deactivation";
	private static final String GROUP_MAIL_COPY_AFTER_DEACTIVATION = "group.mail.copy.after.deactivation";
	private static final String GROUP_MAIL_COPY_BEFORE_DEACTIVATION = "group.mail.copy.before.deactivation";
	
	private static final String GROUP_AUTOMATIC_SOFT_DELETE = "group.automatic.soft.delete";
	private static final String GROUP_NUM_OF_DAYS_BEFORE_SOFT_DELETE = "group.days.before.soft.delete";
	private static final String GROUP_MAIL_BEFORE_SOFT_DELETE  = "group.mail.before.soft.delete";
	private static final String GROUP_NUM_OF_DAYS_BEFORE_MAIL_SOFT_DELETE  = "group.days.before.mail.soft.delete";
	private static final String GROUP_MAIL_AFTER_SOFT_DELETE  = "group.mail.after.soft.delete";
	private static final String GROUP_MAIL_COPY_AFTER_SOFT_DELETE = "group.mail.copy.after.soft.delete";
	private static final String GROUP_MAIL_COPY_BEFORE_SOFT_DELETE  = "group.mail.copy.before.soft.delete";
	
	private static final String GROUP_AUTOMATIC_DEFINITIVELY_DELETE = "group.automatic.definitively.delete";
	private static final String GROUP_NUM_OF_DAYS_BEFORE_DEFINITIVELY_DELETE = "group.days.before.definitively.delete";
	
	
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
	@Value("${group.mandatory.enrolment.email.rolesmanagers}")
	private String mandatoryEnrolmentEmailForRolesmanagers;
	@Value("${group.mandatory.enrolment.email.groupmanagers}")
	private String mandatoryEnrolmentEmailForGroupmanagers;
	@Value("${group.mandatory.enrolment.email.learnresourcemanagers}")
	private String mandatoryEnrolmentEmailForLearnresourcemanagers;
	@Value("${group.mandatory.enrolment.email.poolmanagers}")
	private String mandatoryEnrolmentEmailForPoolmanagers;
	@Value("${group.mandatory.enrolment.email.curriculummanagers}")
	private String mandatoryEnrolmentEmailForCurriculummanagers;
	@Value("${group.mandatory.enrolment.email.lecturemanagers}")
	private String mandatoryEnrolmentEmailForLecturemanagers;
	@Value("${group.mandatory.enrolment.email.qualitymanagers}")
	private String mandatoryEnrolmentEmailForQualitymanagers;
	@Value("${group.mandatory.enrolment.email.linemanagers}")
	private String mandatoryEnrolmentEmailForLinemanagers;
	@Value("${group.mandatory.enrolment.email.principals}")
	private String mandatoryEnrolmentEmailForPrincipals;
	@Value("${group.mandatory.enrolment.email.administrators}")
	private String mandatoryEnrolmentEmailForAdministrators;
	@Value("${group.mandatory.enrolment.email.systemadmins}")
	private String mandatoryEnrolmentEmailForSystemAdmins;
	
	@Value("${group.accept.membership.users}")
	private String acceptMembershipForUsers;
	@Value("${group.accept.membership.authors}")
	private String acceptMembershipForAuthors;
	@Value("${group.accept.membership.usermanagers}")
	private String acceptMembershipForUsermanagers;
	@Value("${group.accept.membership.rolesmanagers}")
	private String acceptMembershipForRolesmanagers;
	@Value("${group.accept.membership.groupmanagers}")
	private String acceptMembershipForGroupmanagers;
	@Value("${group.accept.membership.learnresourcemanagers}")
	private String acceptMembershipForLearnresourcemanagers;
	@Value("${group.accept.membership.poolmanagers}")
	private String acceptMembershipForPoolmanagers;
	@Value("${group.accept.membership.curriculummanagers}")
	private String acceptMembershipForCurriculummanagers;
	@Value("${group.accept.membership.lecturemanagers}")
	private String acceptMembershipForLecturemanagers;
	@Value("${group.accept.membership.qualitymanagers}")
	private String acceptMembershipForQualitymanagers;
	@Value("${group.accept.membership.linemanagers}")
	private String acceptMembershipForLinemanagers;
	@Value("${group.accept.membership.principals}")
	private String acceptMembershipForPrincipals;
	@Value("${group.accept.membership.administrators}")
	private String acceptMembershipForAdministrators;
	@Value("${group.accept.membership.systemadmins}")
	private String acceptMembershipForSystemAdmins;
	
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
	
	@Value("${group.lifecycle:all}")
	private String groupLifecycle;
	@Value("${group.lifecycle.exclude.managed}")
	private String groupLifecycleExcludeManaged;
	@Value("${group.lifecycle.exclude.lti:true}")
	private String groupLifecycleExcludeLti;

	@Value("${group.automatic.inactivation:enabled}")
	private String automaticGroupInactivation;
	
	@Value("${group.days.before.deactivation:400}")
	private int numberOfInactiveDayBeforeDeactivation;
	@Value("${group.days.reactivation.period:30}")
	private int numberOfDayReactivationPeriod;
	@Value("${group.mail.before.deactivation:true}")
	private boolean mailBeforeDeactivation;
	@Value("${group.days.before.mail.automatic.deactivation:10}")
	private int numberOfDayBeforeDeactivationMail;
	@Value("${group.mail.after.deactivation:false}")
	private boolean mailAfterDeactivation;
	@Value("${group.mail.copy.before.deactivation}")
	private String mailCopyBeforeDeactivation;
	@Value("${group.mail.copy.after.deactivation}")
	private String mailCopyAfterDeactivation;
	
	@Value("${group.automatic.soft.delete:true}")
	private String automaticGroupSoftDeletion;
	
	@Value("${group.days.before.soft.delete:200}")
	private int numberOfInactiveDayBeforeSoftDelete;
	@Value("${group.mail.before.soft.delete:true}")
	private boolean mailBeforeSoftDelete;
	@Value("${group.days.before.mail.automatic.soft.delete:10}")
	private int numberOfDayBeforeSoftDeleteMail;
	@Value("${group.mail.after.soft.delete:false}")
	private boolean mailAfterSoftDelete;
	@Value("${group.mail.copy.before.soft.delete}")
	private String mailCopyBeforeSoftDelete;
	@Value("${group.mail.copy.after.soft.delete}")
	private String mailCopyAfterSoftDelete;

	@Value("${group.automatic.definitively.delete:false}")
	private String automaticGroupDefinitivelyDeletion;
	@Value("${group.days.before.definitively.delete:100}")
	private int numberOfSoftDeleteDayBeforeDefinitivelyDelete;
	
	@Autowired
	public BusinessGroupModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
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

		mandatoryEnrolmentEmailForUsers = getStringPropertyValue(MANDATORY_ENROLMENT_EMAIL_USERS, mandatoryEnrolmentEmailForUsers);
		mandatoryEnrolmentEmailForAuthors = getStringPropertyValue(MANDATORY_ENROLMENT_EMAIL_AUTHORS, mandatoryEnrolmentEmailForAuthors);
		mandatoryEnrolmentEmailForUsermanagers = getStringPropertyValue(MANDATORY_ENROLMENT_EMAIL_USERMANAGERS, mandatoryEnrolmentEmailForUsermanagers);
		mandatoryEnrolmentEmailForRolesmanagers = getStringPropertyValue(MANDATORY_ENROLMENT_EMAIL_ROLESMANAGERS, mandatoryEnrolmentEmailForRolesmanagers);
		mandatoryEnrolmentEmailForGroupmanagers = getStringPropertyValue(MANDATORY_ENROLMENT_EMAIL_GROUPMANAGERS, mandatoryEnrolmentEmailForGroupmanagers);
		mandatoryEnrolmentEmailForLearnresourcemanagers = getStringPropertyValue(MANDATORY_ENROLMENT_EMAIL_LEARNRESOURCEMANAGERS, mandatoryEnrolmentEmailForLearnresourcemanagers);
		mandatoryEnrolmentEmailForPoolmanagers = getStringPropertyValue(MANDATORY_ENROLMENT_EMAIL_POOLMANAGERS, mandatoryEnrolmentEmailForPoolmanagers);
		mandatoryEnrolmentEmailForCurriculummanagers = getStringPropertyValue(MANDATORY_ENROLMENT_EMAIL_CURRICULUMMANAGERS, mandatoryEnrolmentEmailForCurriculummanagers);
		mandatoryEnrolmentEmailForLecturemanagers = getStringPropertyValue(MANDATORY_ENROLMENT_EMAIL_LECTUREMANAGERS, mandatoryEnrolmentEmailForLecturemanagers);
		mandatoryEnrolmentEmailForQualitymanagers = getStringPropertyValue(MANDATORY_ENROLMENT_EMAIL_QUALITYMANAGERS, mandatoryEnrolmentEmailForQualitymanagers);
		mandatoryEnrolmentEmailForLinemanagers = getStringPropertyValue(MANDATORY_ENROLMENT_EMAIL_LINEMANAGERS, mandatoryEnrolmentEmailForLinemanagers);
		mandatoryEnrolmentEmailForPrincipals = getStringPropertyValue(MANDATORY_ENROLMENT_EMAIL_PRINCIPALS, mandatoryEnrolmentEmailForPrincipals);
		mandatoryEnrolmentEmailForAdministrators = getStringPropertyValue(MANDATORY_ENROLMENT_EMAIL_ADMINISTRATORS, mandatoryEnrolmentEmailForAdministrators);
		mandatoryEnrolmentEmailForSystemAdmins = getStringPropertyValue(MANDATORY_ENROLMENT_EMAIL_SYSTEMADMINS, mandatoryEnrolmentEmailForSystemAdmins);

		acceptMembershipForUsers = getStringPropertyValue(ACCEPT_MEMBERSHIP_USERS, acceptMembershipForUsers);
		acceptMembershipForAuthors = getStringPropertyValue(ACCEPT_MEMBERSHIP_AUTHORS, acceptMembershipForAuthors);
		acceptMembershipForUsermanagers = getStringPropertyValue(ACCEPT_MEMBERSHIP_USERMANAGERS, acceptMembershipForUsermanagers);
		acceptMembershipForRolesmanagers = getStringPropertyValue(ACCEPT_MEMBERSHIP_ROLESMANAGERS, acceptMembershipForRolesmanagers);
		acceptMembershipForGroupmanagers = getStringPropertyValue(ACCEPT_MEMBERSHIP_GROUPMANAGERS, acceptMembershipForGroupmanagers);
		acceptMembershipForLearnresourcemanagers = getStringPropertyValue(ACCEPT_MEMBERSHIP_LEARNRESOURCEMANAGERS, acceptMembershipForLearnresourcemanagers);
		acceptMembershipForPoolmanagers = getStringPropertyValue(ACCEPT_MEMBERSHIP_POOLMANAGERS, acceptMembershipForPoolmanagers);
		acceptMembershipForCurriculummanagers = getStringPropertyValue(ACCEPT_MEMBERSHIP_CURRICULUMMANAGERS, acceptMembershipForCurriculummanagers);
		acceptMembershipForLecturemanagers = getStringPropertyValue(ACCEPT_MEMBERSHIP_LECTUREMANAGERS, acceptMembershipForLecturemanagers);
		acceptMembershipForQualitymanagers = getStringPropertyValue(ACCEPT_MEMBERSHIP_QUALITYMANAGERS, acceptMembershipForQualitymanagers);
		acceptMembershipForLinemanagers = getStringPropertyValue(ACCEPT_MEMBERSHIP_LINEMANAGERS, acceptMembershipForLinemanagers);
		acceptMembershipForPrincipals = getStringPropertyValue(ACCEPT_MEMBERSHIP_PRINCIPALS, acceptMembershipForPrincipals);
		acceptMembershipForAdministrators = getStringPropertyValue(ACCEPT_MEMBERSHIP_ADMINISTRATORS, acceptMembershipForAdministrators);
		acceptMembershipForSystemAdmins = getStringPropertyValue(ACCEPT_MEMBERSHIP_SYSTEMADMINS, acceptMembershipForSystemAdmins);
		
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
		
		groupLifecycle = getStringPropertyValue(GROUP_LIFECYCLE, groupLifecycle);
		groupLifecycleExcludeLti = getStringPropertyValue(GROUP_LIFECYCLE_EXCLUDE_LTI, groupLifecycleExcludeLti);
		groupLifecycleExcludeManaged = getStringPropertyValue(GROUP_LIFECYCLE_EXCLUDE_MANAGED, groupLifecycleExcludeManaged);
		
		// life-cycle: inactivation
		automaticGroupInactivation = getStringPropertyValue(GROUP_AUTOMATIC_DEACTIVATION, automaticGroupInactivation);
		numberOfInactiveDayBeforeDeactivation = getIntPropertyValue(GROUP_NUM_OF_DAYS_BEFORE_DEACTIVATION, numberOfInactiveDayBeforeDeactivation);
		numberOfDayReactivationPeriod = getIntPropertyValue(GROUP_NUM_OF_DAYS_REACTIVATION_PERIOD, numberOfDayReactivationPeriod);
		
		String mailBeforeDeactivationObj = getStringPropertyValue(GROUP_MAIL_BEFORE_DEACTIVATION, false);
		if(StringHelper.containsNonWhitespace(mailBeforeDeactivationObj)) {
			mailBeforeDeactivation = "true".equalsIgnoreCase(mailBeforeDeactivationObj);
		}
		numberOfDayBeforeDeactivationMail = getIntPropertyValue(GROUP_NUM_OF_DAYS_BEFORE_MAIL_DEACTIVATION, numberOfDayBeforeDeactivationMail);
		
		String mailAfterDeactivationObj = getStringPropertyValue(GROUP_MAIL_AFTER_DEACTIVATION, false);
		if(StringHelper.containsNonWhitespace(mailAfterDeactivationObj)) {
			mailAfterDeactivation = "true".equalsIgnoreCase(mailAfterDeactivationObj);
		}
		mailCopyBeforeDeactivation = getStringPropertyValue(GROUP_MAIL_COPY_BEFORE_DEACTIVATION, mailCopyBeforeDeactivation);
		mailCopyAfterDeactivation = getStringPropertyValue(GROUP_MAIL_COPY_AFTER_DEACTIVATION, mailCopyAfterDeactivation);

		// life-cycle: soft delete
		automaticGroupSoftDeletion = getStringPropertyValue(GROUP_AUTOMATIC_SOFT_DELETE, automaticGroupSoftDeletion);
		numberOfInactiveDayBeforeSoftDelete = getIntPropertyValue(GROUP_NUM_OF_DAYS_BEFORE_SOFT_DELETE, numberOfInactiveDayBeforeSoftDelete);
	
		String mailBeforeSoftDeleteObj = getStringPropertyValue(GROUP_MAIL_BEFORE_SOFT_DELETE, false);
		if(StringHelper.containsNonWhitespace(mailBeforeSoftDeleteObj)) {
			mailBeforeSoftDelete = "true".equalsIgnoreCase(mailBeforeSoftDeleteObj);
		}
		numberOfDayBeforeSoftDeleteMail = getIntPropertyValue(GROUP_NUM_OF_DAYS_BEFORE_MAIL_SOFT_DELETE, numberOfDayBeforeSoftDeleteMail);
		
		String mailAfterSoftDeleteObj = getStringPropertyValue(GROUP_MAIL_AFTER_SOFT_DELETE, false);
		if(StringHelper.containsNonWhitespace(mailAfterSoftDeleteObj)) {
			mailAfterSoftDelete = "true".equalsIgnoreCase(mailAfterSoftDeleteObj);
		}
		mailCopyBeforeSoftDelete = getStringPropertyValue(GROUP_MAIL_COPY_BEFORE_SOFT_DELETE, mailCopyBeforeSoftDelete);
		mailCopyAfterSoftDelete = getStringPropertyValue(GROUP_MAIL_COPY_AFTER_SOFT_DELETE, mailCopyAfterSoftDelete);
		
		// life-cycle: definitively deleted
		automaticGroupDefinitivelyDeletion = getStringPropertyValue(GROUP_AUTOMATIC_DEFINITIVELY_DELETE, automaticGroupDefinitivelyDeletion);
		numberOfSoftDeleteDayBeforeDefinitivelyDelete = getIntPropertyValue(GROUP_NUM_OF_DAYS_BEFORE_DEFINITIVELY_DELETE, numberOfSoftDeleteDayBeforeDefinitivelyDelete);
	}

	public boolean isAllowedCreate(Roles roles) {
		return roles.isAdministrator() || roles.isGroupManager()
				|| (roles.isAuthor() && isAuthorAllowedCreate())
				|| (!roles.isGuestOnly() && !roles.isInvitee() && isUserAllowedCreate());
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
		if(roles == null || roles.isGuestOnly() || roles.isInvitee()) return false;
		
		for(OrganisationRoles role:privacyRoles) {
			if(roles.hasRole(role)) {
				return Boolean.parseBoolean(getMandatoryEnrolmentEmailFor(role));
			}
		}
		return Boolean.parseBoolean(getMandatoryEnrolmentEmailFor(OrganisationRoles.user));
	}
	
	public String getMandatoryEnrolmentEmailFor(OrganisationRoles role) {
		switch(role) {
			case user: return mandatoryEnrolmentEmailForUsers;
			case author: return mandatoryEnrolmentEmailForAuthors;
			case usermanager: return mandatoryEnrolmentEmailForUsermanagers;
			case rolesmanager: return mandatoryEnrolmentEmailForRolesmanagers;
			case groupmanager: return mandatoryEnrolmentEmailForGroupmanagers;
			case learnresourcemanager: return mandatoryEnrolmentEmailForLearnresourcemanagers;
			case poolmanager: return mandatoryEnrolmentEmailForPoolmanagers;
			case curriculummanager: return mandatoryEnrolmentEmailForCurriculummanagers;
			case lecturemanager: return mandatoryEnrolmentEmailForLecturemanagers;
			case qualitymanager: return mandatoryEnrolmentEmailForQualitymanagers;
			case linemanager: return mandatoryEnrolmentEmailForLinemanagers;
			case principal: return mandatoryEnrolmentEmailForPrincipals;
			case administrator: return mandatoryEnrolmentEmailForAdministrators;
			case sysadmin: return mandatoryEnrolmentEmailForSystemAdmins;
			default: return "disabled";
		}
	}
	
	public void setMandatoryEnrolmentEmailFor(OrganisationRoles role, String enable) {
		switch(role) {
			case user:
				mandatoryEnrolmentEmailForUsers = setStringProperty(MANDATORY_ENROLMENT_EMAIL_USERS, enable, true);
				break;
			case author:
				mandatoryEnrolmentEmailForAuthors = setStringProperty(MANDATORY_ENROLMENT_EMAIL_AUTHORS, enable, true);
				break;
			case usermanager:
				mandatoryEnrolmentEmailForUsermanagers = setStringProperty(MANDATORY_ENROLMENT_EMAIL_USERMANAGERS, enable, true);
				break;
			case rolesmanager:
				mandatoryEnrolmentEmailForRolesmanagers = setStringProperty(MANDATORY_ENROLMENT_EMAIL_ROLESMANAGERS, enable, true);
				break;
			case groupmanager:
				mandatoryEnrolmentEmailForGroupmanagers = setStringProperty(MANDATORY_ENROLMENT_EMAIL_GROUPMANAGERS, enable, true);
				break;
			case learnresourcemanager:
				mandatoryEnrolmentEmailForLearnresourcemanagers = setStringProperty(MANDATORY_ENROLMENT_EMAIL_LEARNRESOURCEMANAGERS, enable, true);
				break;
			case poolmanager:
				mandatoryEnrolmentEmailForPoolmanagers = setStringProperty(MANDATORY_ENROLMENT_EMAIL_POOLMANAGERS, enable, true);
				break;
			case curriculummanager:
				mandatoryEnrolmentEmailForCurriculummanagers = setStringProperty(MANDATORY_ENROLMENT_EMAIL_CURRICULUMMANAGERS, enable, true);
				break;
			case lecturemanager:
				mandatoryEnrolmentEmailForLecturemanagers = setStringProperty(MANDATORY_ENROLMENT_EMAIL_LECTUREMANAGERS, enable, true);
				break;
			case qualitymanager:
				mandatoryEnrolmentEmailForQualitymanagers = setStringProperty(MANDATORY_ENROLMENT_EMAIL_QUALITYMANAGERS, enable, true);
				break;
			case linemanager:
				mandatoryEnrolmentEmailForLinemanagers = setStringProperty(MANDATORY_ENROLMENT_EMAIL_LINEMANAGERS, enable, true);
				break;
			case principal:
				mandatoryEnrolmentEmailForPrincipals = setStringProperty(MANDATORY_ENROLMENT_EMAIL_PRINCIPALS, enable, true);
				break;
			case administrator:
				mandatoryEnrolmentEmailForAdministrators = setStringProperty(MANDATORY_ENROLMENT_EMAIL_ADMINISTRATORS, enable, true);
				break;
			case sysadmin:
				mandatoryEnrolmentEmailForSystemAdmins = setStringProperty(MANDATORY_ENROLMENT_EMAIL_SYSTEMADMINS, enable, true);
				break;
			default: /* Ignore the other roles */
		}
	}
	
	public boolean isAcceptMembership(Roles roles) {
		if(roles == null || roles.isGuestOnly() || roles.isInvitee()) return false;
	
		for(OrganisationRoles role:privacyRoles) {
			if(roles.hasRole(role)) {
				return Boolean.parseBoolean(getAcceptMembershipFor(role));
			}
		}
		return Boolean.parseBoolean(getAcceptMembershipFor(OrganisationRoles.user));
	}
	
	public String getAcceptMembershipFor(OrganisationRoles role) {
		switch(role) {
			case user: return acceptMembershipForUsers;
			case author: return acceptMembershipForAuthors;
			case usermanager: return acceptMembershipForUsermanagers;
			case rolesmanager: return acceptMembershipForRolesmanagers;
			case groupmanager: return acceptMembershipForGroupmanagers;
			case learnresourcemanager: return acceptMembershipForLearnresourcemanagers;
			case poolmanager: return acceptMembershipForPoolmanagers;
			case curriculummanager: return acceptMembershipForCurriculummanagers;
			case lecturemanager: return acceptMembershipForLecturemanagers;
			case qualitymanager: return acceptMembershipForQualitymanagers;
			case linemanager: return acceptMembershipForLinemanagers;
			case principal: return acceptMembershipForPrincipals;
			case administrator: return acceptMembershipForAdministrators;
			case sysadmin: return acceptMembershipForSystemAdmins;
			default: return "disabled";
		}
	}

	public void setAcceptMembershipFor(OrganisationRoles role, String enable) {
		switch(role) {
			case user:
				acceptMembershipForUsers = setStringProperty(ACCEPT_MEMBERSHIP_USERS, enable, true);
				break;
			case author:
				acceptMembershipForAuthors = setStringProperty(ACCEPT_MEMBERSHIP_AUTHORS, enable, true);
				break;
			case usermanager:
				acceptMembershipForUsermanagers = setStringProperty(ACCEPT_MEMBERSHIP_USERMANAGERS, enable, true);
				break;
			case rolesmanager:
				acceptMembershipForRolesmanagers = setStringProperty(ACCEPT_MEMBERSHIP_ROLESMANAGERS, enable, true);
				break;
			case groupmanager:
				acceptMembershipForGroupmanagers = setStringProperty(ACCEPT_MEMBERSHIP_GROUPMANAGERS, enable, true);
				break;
			case learnresourcemanager:
				acceptMembershipForLearnresourcemanagers = setStringProperty(ACCEPT_MEMBERSHIP_LEARNRESOURCEMANAGERS, enable, true);
				break;
			case poolmanager:
				acceptMembershipForPoolmanagers = setStringProperty(ACCEPT_MEMBERSHIP_POOLMANAGERS, enable, true);
				break;
			case curriculummanager:
				acceptMembershipForCurriculummanagers = setStringProperty(ACCEPT_MEMBERSHIP_CURRICULUMMANAGERS, enable, true);
				break;
			case lecturemanager:
				acceptMembershipForLecturemanagers = setStringProperty(ACCEPT_MEMBERSHIP_LECTUREMANAGERS, enable, true);
				break;
			case qualitymanager:
				acceptMembershipForQualitymanagers = setStringProperty(ACCEPT_MEMBERSHIP_QUALITYMANAGERS, enable, true);
				break;
			case linemanager:
				acceptMembershipForLinemanagers = setStringProperty(ACCEPT_MEMBERSHIP_LINEMANAGERS, enable, true);
				break;
			case principal:
				acceptMembershipForPrincipals = setStringProperty(ACCEPT_MEMBERSHIP_PRINCIPALS, enable, true);
				break;
			case administrator:
				acceptMembershipForAdministrators = setStringProperty(ACCEPT_MEMBERSHIP_ADMINISTRATORS, enable, true);
				break;
			case sysadmin:
				acceptMembershipForSystemAdmins = setStringProperty(ACCEPT_MEMBERSHIP_SYSTEMADMINS, enable, true);
				break;
			default: /* Ignore the other roles */
		}
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
		this.managedBusinessGroups = enabled;
		setStringProperty(MANAGED_GROUPS_ENABLED, Boolean.toString(enabled), true);
	}
	
	public String getGroupLifecycle() {
		return groupLifecycle;
	}
	
	public BusinessGroupLifecycleTypeEnum getGroupLifecycleTypeEnum() {
		if(StringHelper.containsNonWhitespace(groupLifecycle)) {
			return BusinessGroupLifecycleTypeEnum.valueOf(groupLifecycle);
		}
		return null;
	}

	public void setGroupLifecycle(String type) {
		groupLifecycle = type;
		setStringProperty(GROUP_LIFECYCLE, groupLifecycle, true);
	}
	
	public boolean isGroupLifecycleExcludeManaged() {
		return "true".equals(groupLifecycleExcludeManaged);
	}
	
	public void setGroupLifecycleExcludeManaged(boolean excluded) {
		groupLifecycleExcludeManaged = excluded ? "true" : "false";
		setStringProperty(GROUP_LIFECYCLE_EXCLUDE_MANAGED, groupLifecycleExcludeManaged, true);
	}
	
	public boolean isGroupLifecycleExcludeLti() {
		return "true".equals(groupLifecycleExcludeLti);
	}
	
	public void setGroupLifecycleExcludeLti(boolean excluded) {
		groupLifecycleExcludeLti = excluded ? "true" : "false";
		setStringProperty(GROUP_LIFECYCLE_EXCLUDE_LTI, groupLifecycleExcludeLti, true);
	}
	
	public boolean isAutomaticGroupInactivationEnabled() {
		return "enabled".equals(automaticGroupInactivation);
	}
	
	public void setAutomaticGroupInactivationEnabled(String enable) {
		this.automaticGroupInactivation = enable;
		setStringProperty(GROUP_AUTOMATIC_DEACTIVATION, enable, true);
	}
	
	public int getNumberOfFocusDay() {
		return 30;
	}

	public int getNumberOfInactiveDayBeforeDeactivation() {
		return numberOfInactiveDayBeforeDeactivation;
	}

	public void setNumberOfInactiveDayBeforeDeactivation(int days) {
		this.numberOfInactiveDayBeforeDeactivation = days;
		setIntProperty(GROUP_NUM_OF_DAYS_BEFORE_DEACTIVATION, days, true);
	}
	
	/**
	 * This is a grace period after reactivation.
	 * 
	 * @return
	 */
	public int getNumberOfDayReactivationPeriod() {
		return numberOfDayReactivationPeriod;
	}

	public void setNumberOfDayReactivationPeriod(int days) {
		this.numberOfDayReactivationPeriod = days;
		setIntProperty(GROUP_NUM_OF_DAYS_REACTIVATION_PERIOD, days, true);
	}
	
	public boolean isMailBeforeDeactivation() {
		return mailBeforeDeactivation;
	}

	public void setMailBeforeDeactivation(boolean enabled) {
		this.mailBeforeDeactivation = enabled;
		setStringProperty(GROUP_MAIL_BEFORE_DEACTIVATION, Boolean.toString(enabled), true);
	}
	
	public int getNumberOfDayBeforeDeactivationMail() {
		return numberOfDayBeforeDeactivationMail;
	}
	
	public void setNumberOfDayBeforeDeactivationMail(int days) {
		this.numberOfDayBeforeDeactivationMail = days;
		setIntProperty(GROUP_NUM_OF_DAYS_BEFORE_MAIL_DEACTIVATION, days, true);
	}
	
	public List<String> getMailCopyBeforeDeactivation() {
		return convertStringToList(mailCopyBeforeDeactivation);
	}
	
	public void setMailCopyBeforeDeactivation(String mails) {
		mailCopyBeforeDeactivation = removeWhiteSpaces(mails);
		setStringProperty(GROUP_MAIL_COPY_BEFORE_DEACTIVATION, mailCopyBeforeDeactivation, true);
	}
	
	public boolean isMailAfterDeactivation() {
		return mailAfterDeactivation;
	}

	public void setMailAfterDeactivation(boolean enabled) {
		this.mailAfterDeactivation = enabled;
		setStringProperty(GROUP_MAIL_AFTER_DEACTIVATION, Boolean.toString(enabled), true);
	}
	
	public List<String> getMailCopyAfterDeactivation() {
		return convertStringToList(mailCopyAfterDeactivation);
	}
	
	public void setMailCopyAfterDeactivation(String mails) {
		mailCopyAfterDeactivation = removeWhiteSpaces(mails);
		setStringProperty(GROUP_MAIL_COPY_AFTER_DEACTIVATION, mailCopyAfterDeactivation, true);
	}

	public boolean isAutomaticGroupSoftDeleteEnabled() {
		return "true".equals(automaticGroupSoftDeletion);
	}
	
	public void setAutomaticGroupSoftDeleteEnabled(String enable) {
		this.automaticGroupSoftDeletion = enable;
		setStringProperty(GROUP_AUTOMATIC_SOFT_DELETE, enable, true);
	}

	public int getNumberOfInactiveDayBeforeSoftDelete() {
		return numberOfInactiveDayBeforeSoftDelete;
	}

	public void setNumberOfInactiveDayBeforeSoftDelete(int days) {
		this.numberOfInactiveDayBeforeSoftDelete = days;
		setIntProperty(GROUP_NUM_OF_DAYS_BEFORE_SOFT_DELETE, days, true);
	}
	
	public boolean isMailBeforeSoftDelete() {
		return mailBeforeSoftDelete;
	}

	public void setMailBeforeSoftDelete(boolean enabled) {
		this.mailBeforeSoftDelete = enabled;
		setStringProperty(GROUP_MAIL_BEFORE_SOFT_DELETE, Boolean.toString(enabled), true);
	}
	
	public int getNumberOfDayBeforeSoftDeleteMail() {
		return numberOfDayBeforeSoftDeleteMail;
	}
	
	public void setNumberOfDayBeforeSoftDeleteMail(int days) {
		this.numberOfDayBeforeSoftDeleteMail = days;
		setIntProperty(GROUP_NUM_OF_DAYS_BEFORE_MAIL_SOFT_DELETE, days, true);
	}
	
	public List<String> getMailCopyBeforeSoftDelete() {
		return convertStringToList(mailCopyBeforeSoftDelete);
	}
	
	public void setMailCopyBeforeSoftDelete(String mails) {
		mailCopyBeforeSoftDelete = removeWhiteSpaces(mails);
		setStringProperty(GROUP_MAIL_COPY_BEFORE_SOFT_DELETE, mailCopyBeforeSoftDelete, true);
	}
	
	public boolean isMailAfterSoftDelete() {
		return mailAfterSoftDelete;
	}

	public void setMailAfterSoftDelete(boolean enabled) {
		this.mailAfterSoftDelete = enabled;
		setStringProperty(GROUP_MAIL_AFTER_SOFT_DELETE, Boolean.toString(enabled), true);
	}
	
	public List<String> getMailCopyAfterSoftDelete() {
		return convertStringToList(mailCopyAfterSoftDelete);
	}
	
	public void setMailCopyAfterSoftDelete(String mails) {
		mailCopyAfterSoftDelete = removeWhiteSpaces(mails);
		setStringProperty(GROUP_MAIL_COPY_AFTER_SOFT_DELETE, mailCopyAfterSoftDelete, true);
	}
	
	public boolean isAutomaticGroupDefinitivelyDeleteEnabled() {
		return "true".equals(automaticGroupDefinitivelyDeletion);
	}
	
	public void setAutomaticGroupDefinitivelyDeleteEnabled(String enable) {
		this.automaticGroupDefinitivelyDeletion = enable;
		setStringProperty(GROUP_AUTOMATIC_DEFINITIVELY_DELETE, enable, true);
	}

	public int getNumberOfSoftDeleteDayBeforeDefinitivelyDelete() {
		return numberOfSoftDeleteDayBeforeDefinitivelyDelete;
	}

	public void setNumberOfSoftDeleteDayBeforeDefinitivelyDelete(int days) {
		this.numberOfSoftDeleteDayBeforeDefinitivelyDelete = days;
		setIntProperty(GROUP_NUM_OF_DAYS_BEFORE_DEFINITIVELY_DELETE, days, true);
	}
	
	private String removeWhiteSpaces(String stringToClean) {
		if (stringToClean == null) {
			return null;
		}
		return stringToClean.replace(" ", "");
	}
	
	private List<String> convertStringToList(String toList) {
		if (!StringHelper.containsNonWhitespace(toList)) {
			return new ArrayList<>();
		}
		
		String[] arr = toList.split("[,]");
		for(int i=arr.length; i-->0; ) {
			arr[i] = arr[i].trim();
		}
		return List.of(arr);
	}
}