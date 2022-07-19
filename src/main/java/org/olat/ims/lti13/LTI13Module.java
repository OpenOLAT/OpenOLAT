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
package org.olat.ims.lti13;

import java.util.Collection;
import java.util.List;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LTI13Module extends AbstractSpringModule implements ConfigOnOff {

	private static final String PROP_ENABLED = "lti13.enabled";
	private static final String PROP_PLATFORM_ISS = "lti13.platform.iss";
	private static final String PROP_MATCHING_BY_EMAIL = "lti13.platform.matching.by.email";
	private static final String PROP_DEFAULT_ORGANISATION = "lti13.default.organisation";
	
	private static final String PROP_DEPLOYMENT_REPOSITORY_ENTRY_ROLE = "lti13.deployment.repository.entry.role";
	private static final String PROP_DEPLOYMENT_REPOSITORY_ENTRY_OWNER_PERMISSION = "lti13.deployment.repository.entry.owner.permission";
	private static final String PROP_DEPLOYMENT_BUSINESS_GROUP_ROLE = "lti13.deployment.business.group.role";
	private static final String PROP_DEPLOYMENT_BUSINESS_GROUP_COACH_PERMISSION = "lti13.deployment.business.group.owner.permission";
	
	
	@Value("${lti13.enabled}")
	private boolean enabled;

	@Value("${lti13.platform.iss}")
	private String platformIss;
	
	@Value("${lti13.platform.matching.by.email:enabled}")
	private String matchingByEmail;
	
	@Value("${lti13.default.organisation}")
	private String defaultOrganisationKey;
	
	@Value("${lti13.deployment.repository.entry.role:administrator}")
	private String deploymentRepositoryEntryRolesConfiguration;
	@Value("${lti13.deployment.repository.entry.owner.permission:perResource}")
	private String deploymentRepositoryEntryOwnerPermission;
	
	@Value("${lti13.deployment.business.group.role:administrator}")
	private String deploymentBusinessGroupRolesConfiguration;
	@Value("${lti13.deployment.business.group.owner.permission:perResource}")
	private String deploymentBusinessGroupCoachPermission;
	
	@Autowired
	public LTI13Module(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		String enabledObj = getStringPropertyValue(PROP_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		matchingByEmail = getStringPropertyValue(PROP_MATCHING_BY_EMAIL, matchingByEmail);
		defaultOrganisationKey = getStringPropertyValue(PROP_DEFAULT_ORGANISATION, defaultOrganisationKey);
		
		deploymentRepositoryEntryRolesConfiguration = getStringPropertyValue(PROP_DEPLOYMENT_REPOSITORY_ENTRY_ROLE, deploymentRepositoryEntryRolesConfiguration);
		deploymentRepositoryEntryOwnerPermission = getStringPropertyValue(PROP_DEPLOYMENT_REPOSITORY_ENTRY_OWNER_PERMISSION, deploymentRepositoryEntryOwnerPermission);
		deploymentBusinessGroupRolesConfiguration = getStringPropertyValue(PROP_DEPLOYMENT_BUSINESS_GROUP_ROLE, deploymentBusinessGroupRolesConfiguration);
		deploymentBusinessGroupCoachPermission = getStringPropertyValue(PROP_DEPLOYMENT_BUSINESS_GROUP_COACH_PERMISSION, deploymentBusinessGroupCoachPermission);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setBooleanProperty(PROP_ENABLED, enabled, true);
	}

	public String getPlatformIss() {
		return platformIss;
	}

	public void setPlatformIss(String platformIss) {
		this.platformIss = platformIss;
		setStringProperty(PROP_PLATFORM_ISS, platformIss, true);
	}
	
	public String getDefaultOrganisationKey() {
		return defaultOrganisationKey;
	}

	public void setDefaultOrganisationKey(String defaultOrganisationKey) {
		this.defaultOrganisationKey = defaultOrganisationKey;
		setStringProperty(PROP_DEFAULT_ORGANISATION, defaultOrganisationKey, true);
	}

	public boolean isMatchingByEmailEnabled() {
		return "enabled".equals(matchingByEmail);
	}

	public void setMatchByEmail(String enabled) {
		this.matchingByEmail = enabled;
		setStringProperty(PROP_MATCHING_BY_EMAIL, enabled, true);
	}
	
	public List<String> getDeploymentRepositoryEntryRolesConfigurationList() {
		if(StringHelper.containsNonWhitespace(deploymentRepositoryEntryRolesConfiguration)) {
			String[] roles = deploymentRepositoryEntryRolesConfiguration.split("[,]");
			return List.of(roles);
		}
		return List.of();
	}

	public void setDeploymentRepositoryEntryRolesConfigurationList(Collection<String> roles) {
		String rolesString = String.join(",", roles);
		deploymentRepositoryEntryRolesConfiguration = rolesString;
		setStringProperty(PROP_DEPLOYMENT_REPOSITORY_ENTRY_ROLE, rolesString, true);
	}

	public DeploymentConfigurationPermission getDeploymentRepositoryEntryOwnerPermission() {
		return DeploymentConfigurationPermission.valueOfSecure(deploymentRepositoryEntryOwnerPermission);
	}

	public void setDeploymentRepositoryEntryOwnerPermission(String permission) {
		this.deploymentRepositoryEntryOwnerPermission = permission;
		setStringProperty(PROP_DEPLOYMENT_REPOSITORY_ENTRY_OWNER_PERMISSION, permission, true);
	}
	
	public List<String> getDeploymentBusinessGroupRolesConfigurationList() {
		if(StringHelper.containsNonWhitespace(deploymentBusinessGroupRolesConfiguration)) {
			String[] roles = deploymentBusinessGroupRolesConfiguration.split("[,]");
			return List.of(roles);
		}
		return List.of();
	}

	public void setDeploymentBusinessGroupRolesConfigurationList(Collection<String> roles) {
		String rolesString = String.join(",", roles);
		deploymentBusinessGroupRolesConfiguration = rolesString;
		setStringProperty(PROP_DEPLOYMENT_BUSINESS_GROUP_ROLE, rolesString, true);
	}

	public DeploymentConfigurationPermission getDeploymentBusinessGroupCoachPermission() {
		return DeploymentConfigurationPermission.valueOfSecure(deploymentBusinessGroupCoachPermission);
	}

	public void setDeploymentBusinessGroupCoachPermission(String permission) {
		this.deploymentBusinessGroupCoachPermission = permission;
		setStringProperty(PROP_DEPLOYMENT_BUSINESS_GROUP_COACH_PERMISSION, permission, true);
	}
	
	public boolean isAllowedToDeploy(Roles roles, BusinessGroup businessGroup) {
		if(!isEnabled()) {
			return false;
		}
		
		List<String> allowedRoles = getDeploymentBusinessGroupRolesConfigurationList();
		if((roles.isAdministrator() && allowedRoles.contains(OrganisationRoles.administrator.name()))
				|| (roles.isGroupManager() && allowedRoles.contains(OrganisationRoles.groupmanager.name()))) {
			return true;
		}
		return roles.isAuthor() && (getDeploymentBusinessGroupCoachPermission() == DeploymentConfigurationPermission.allResources
				|| (getDeploymentBusinessGroupCoachPermission() == DeploymentConfigurationPermission.perResource
						&& businessGroup.isLTI13DeploymentByCoachWithAuthorRightsEnabled()));
	}
	
	public boolean isAllowedToDeploy(Roles roles, RepositoryEntry entry) {
		if(!isEnabled()) {
			return false;
		}
		
		List<String> allowedRoles = getDeploymentRepositoryEntryRolesConfigurationList();
		if((roles.isAdministrator() && allowedRoles.contains(OrganisationRoles.administrator.name()))
				|| (roles.isGroupManager() && allowedRoles.contains(OrganisationRoles.groupmanager.name()))) {
			return true;
		}
		return roles.isAuthor() && (getDeploymentRepositoryEntryOwnerPermission() == DeploymentConfigurationPermission.allResources
				|| (getDeploymentRepositoryEntryOwnerPermission() == DeploymentConfigurationPermission.perResource
						&& entry.isLTI13DeploymentByOwnerWithAuthorRightsEnabled()));
		
	}

	public String getPlatformJwkSetUri() {
		return Settings.getServerContextPathURI() + LTI13Dispatcher.LTI_JWKSET_PATH;
	}
	
	public String getPlatformAuthorizationUri() {
		return Settings.getServerContextPathURI() + LTI13Dispatcher.LTI_AUTHORIZATION_PATH;
	}
	
	public String getPlatformTokenUri() {
		return Settings.getServerContextPathURI() + LTI13Dispatcher.LTI_TOKEN_PATH;
	}
	
	public String getToolLoginInitiationUri() {
		return Settings.getServerContextPathURI() + LTI13Dispatcher.LTI_LOGIN_INITIATION_PATH;
	}
	
	public String getToolLoginRedirectUri() {
		return Settings.getServerContextPathURI() + LTI13Dispatcher.LTI_LOGIN_REDIRECT_PATH;
	}
}
