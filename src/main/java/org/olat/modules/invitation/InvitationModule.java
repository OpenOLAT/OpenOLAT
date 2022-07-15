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
package org.olat.modules.invitation;

import java.util.Collection;
import java.util.List;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.login.LoginModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 8 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("invitationModule")
public class InvitationModule extends AbstractSpringModule {
	
	private static final String INVITATION_COURSE_ENABLED = "invitation.course";
	private static final String INVITATION_GROUP_ENABLED = "invitation.group";
	private static final String INVITATION_COURSE_ROLE = "invitation.course.role";
	private static final String INVITATION_BUSINESS_GROUP_ROLE = "invitation.business.group.role";
	private static final String INVITATION_COURSE_OWNER_PERMISSION = "invitation.course.owner.permission";
	private static final String INVITATION_BUSINESS_GROUP_COACH_PERMISSION = "invitation.business.group.coach.permission";
	private static final String INVITATION_EXPIRATION_ACCOUNT_AFTER = "invitation.expiration.account";

	@Value("${invitation.course:enabled}")
	private String courseInvitationEnabled;
	@Value("${invitation.course.role:administrator}")
	private String courseRolesConfiguration;
	@Value("${invitation.course.owner.permission:perResource}")
	private String courseOwnerPermission;
	
	@Value("${invitation.group:enabled}")
	private String businessGroupInvitationEnabled;
	@Value("${invitation.business.group.role:administrator}")
	private String businessGroupRolesConfiguration;
	@Value("${invitation.business.group.coach.permission:perResource}")
	private String businessGroupCoachPermission;
	
	@Value("${invitation.expiration.account:180}")
	private String expirationAccountInDays;

	private LoginModule loginModule;
	
	@Autowired
	public InvitationModule(CoordinatorManager coordinatorManager, LoginModule loginModule) {
		super(coordinatorManager);
		this.loginModule = loginModule;
	}

	@Override
	public void init() {
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		courseInvitationEnabled = getStringPropertyValue(INVITATION_COURSE_ENABLED, courseInvitationEnabled);
		businessGroupInvitationEnabled = getStringPropertyValue(INVITATION_GROUP_ENABLED, businessGroupInvitationEnabled);
		
		courseRolesConfiguration = getStringPropertyValue(INVITATION_COURSE_ROLE, courseRolesConfiguration);
		businessGroupRolesConfiguration = getStringPropertyValue(INVITATION_BUSINESS_GROUP_ROLE, businessGroupRolesConfiguration);
		
		courseOwnerPermission = getStringPropertyValue(INVITATION_COURSE_OWNER_PERMISSION, courseOwnerPermission);
		businessGroupCoachPermission = getStringPropertyValue(INVITATION_BUSINESS_GROUP_COACH_PERMISSION, businessGroupCoachPermission);
		
		expirationAccountInDays = getStringPropertyValue(INVITATION_EXPIRATION_ACCOUNT_AFTER, expirationAccountInDays);
	}
	
	public boolean isInvitationEnabled() {
		return isBusinessGroupInvitationEnabled() || isCourseInvitationEnabled() || isPortfolioInvitationEnabled();
	}

	public boolean isCourseInvitationEnabled() {
		return "enabled".equals(courseInvitationEnabled);
	}

	public void setCourseInvitationEnabled(boolean enabled) {
		courseInvitationEnabled = enabled ? "enabled" : "disabled";
		setStringProperty(INVITATION_COURSE_ENABLED, courseInvitationEnabled, true);
	}

	public List<String> getCourseRolesConfigurationList() {
		if(StringHelper.containsNonWhitespace(courseRolesConfiguration)) {
			String[] roles = courseRolesConfiguration.split("[,]");
			return List.of(roles);
		}
		return List.of();
	}

	public void setCourseRolesConfiguration(Collection<String> roles) {
		String rolesString = String.join(",", roles);
		courseRolesConfiguration = rolesString;
		setStringProperty(INVITATION_COURSE_ROLE, rolesString, true);
	}

	public InvitationConfigurationPermission getCourseOwnerPermission() {
		return InvitationConfigurationPermission.valueOfSecure(courseOwnerPermission);
	}

	public void setCourseOwnerPermission(String permission) {
		this.courseOwnerPermission = permission;
		setStringProperty(INVITATION_COURSE_OWNER_PERMISSION, permission, true);
	}

	public boolean isBusinessGroupInvitationEnabled() {
		return "enabled".equals(businessGroupInvitationEnabled);
	}

	public void setBusinessGroupInvitationEnabled(boolean enabled) {
		businessGroupInvitationEnabled = enabled ? "enabled" : "disabled";
		setStringProperty(INVITATION_GROUP_ENABLED, businessGroupInvitationEnabled, true);
	}
	
	public List<String> getBusinessGroupRolesConfigurationList() {
		if(StringHelper.containsNonWhitespace(businessGroupRolesConfiguration)) {
			String[] roles = businessGroupRolesConfiguration.split("[,]");
			return List.of(roles);
		}
		return List.of();
	}

	public void setBusinessGroupRolesConfiguration(Collection<String> roles) {
		String rolesString = String.join(",", roles);
		businessGroupRolesConfiguration = rolesString;
		setStringProperty(INVITATION_BUSINESS_GROUP_ROLE, rolesString, true);
	}
	
	public InvitationConfigurationPermission getBusinessGroupCoachPermission() {
		return InvitationConfigurationPermission.valueOfSecure(businessGroupCoachPermission);
	}

	public void setBusinessGroupCoachPermission(String permission) {
		businessGroupCoachPermission = permission;
		setStringProperty(INVITATION_BUSINESS_GROUP_COACH_PERMISSION, permission, true);
	}

	public boolean isPortfolioInvitationEnabled() {
		return loginModule.isInvitationEnabled();
	}

	public void setPortfolioInvitationEnabled(boolean enabled) {
		loginModule.setInvitationEnabled(enabled);
	}

	public int getExpirationAccountInDays() {
		if(StringHelper.isLong(expirationAccountInDays)) {
			return Integer.parseInt(expirationAccountInDays);
		}
		return -1;
	}

	public void setExpirationAccountInDays(String days) {
		expirationAccountInDays = days;
		setStringProperty(INVITATION_EXPIRATION_ACCOUNT_AFTER, days, true);
	}
}
