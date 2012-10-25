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
import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.id.Roles;
import org.olat.core.id.context.SiteContextEntryControllerCreator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.site.GroupsSite;

/**
 * Description:<br>
 * The business group module initializes the OLAT groups environment.
 * Configurations are loaded from here.
 * <P>
 * Initial Date: 04.11.2009 <br>
 * 
 * @author gnaegi
 */
public class BusinessGroupModule extends AbstractOLATModule {

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

	private boolean userAllowedCreate;
	private boolean authorAllowedCreate;
	private boolean userListDownloadDefaultAllowed;
	private String contactBusinessCard;
	
	private String mandatoryEnrolmentEmailForUsers;
	private String mandatoryEnrolmentEmailForAuthors;
	private String mandatoryEnrolmentEmailForUsermanagers;
	private String mandatoryEnrolmentEmailForGroupmanagers;
	private String mandatoryEnrolmentEmailForAdministrators;
	
	private String acceptMembershipForUsers;
	private String acceptMembershipForAuthors;
	private String acceptMembershipForUsermanagers;
	private String acceptMembershipForGroupmanagers;
	private String acceptMembershipForAdministrators;

	/**
	 * [used by spring]
	 */
	private BusinessGroupModule() {
		//
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

	/**
	 * @see org.olat.core.configuration.AbstractOLATModule#initDefaultProperties()
	 */
	@Override
	protected void initDefaultProperties() {
		userAllowedCreate = getBooleanConfigParameter(USER_ALLOW_CREATE_BG, true);
		authorAllowedCreate = getBooleanConfigParameter(AUTHOR_ALLOW_CREATE_BG, true);
		contactBusinessCard = getStringConfigParameter(CONTACT_BUSINESS_CARD, CONTACT_BUSINESS_CARD_NEVER, true);
		userListDownloadDefaultAllowed = getBooleanConfigParameter(USER_LIST_DOWNLOAD, true);
		
		mandatoryEnrolmentEmailForUsers = getStringConfigParameter(MANDATORY_ENROLMENT_EMAIL_USERS, "false", true);
		mandatoryEnrolmentEmailForAuthors = getStringConfigParameter(MANDATORY_ENROLMENT_EMAIL_AUTHORS, "false", true);
		mandatoryEnrolmentEmailForUsermanagers = getStringConfigParameter(MANDATORY_ENROLMENT_EMAIL_USERMANAGERS, "false", true);
		mandatoryEnrolmentEmailForGroupmanagers = getStringConfigParameter(MANDATORY_ENROLMENT_EMAIL_GROUPMANAGERS, "false", true);
		mandatoryEnrolmentEmailForAdministrators = getStringConfigParameter(MANDATORY_ENROLMENT_EMAIL_ADMINISTRATORS, "false", true);
		
		acceptMembershipForUsers = getStringConfigParameter(ACCEPT_MEMBERSHIP_USERS, "false", true);
		acceptMembershipForAuthors = getStringConfigParameter(ACCEPT_MEMBERSHIP_AUTHORS, "false", true);
		acceptMembershipForUsermanagers = getStringConfigParameter(ACCEPT_MEMBERSHIP_USERMANAGERS, "false", true);
		acceptMembershipForGroupmanagers = getStringConfigParameter(ACCEPT_MEMBERSHIP_GROUPMANAGERS, "false", true);
		acceptMembershipForAdministrators = getStringConfigParameter(ACCEPT_MEMBERSHIP_ADMINISTRATORS, "false", true);
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
	}
	
	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
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

	public void setMandatoryEnrolmentEmailForGroupmanagers(boolean mandatory) {
		setStringProperty(MANDATORY_ENROLMENT_EMAIL_GROUPMANAGERS, Boolean.toString(mandatory), true);
	}

	public String getMandatoryEnrolmentEmailForAdministrators() {
		return mandatoryEnrolmentEmailForAdministrators;
	}

	public void setMandatoryEnrolmentEmailForAdministrators(String mandatory) {
		setStringProperty(MANDATORY_ENROLMENT_EMAIL_ADMINISTRATORS, mandatory, true);
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

	public void setAcceptMembershipForAdministrators(boolean mandatory) {
		setStringProperty(ACCEPT_MEMBERSHIP_ADMINISTRATORS, Boolean.toString(mandatory), true);
	}
}