/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.sharepoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.modules.sharepoint.model.SiteConfigurationXstream;
import org.olat.modules.sharepoint.model.SitesAndDrivesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.microsoft.graph.models.User;

/**
 * 
 * Initial date: 24 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class SharePointModule extends AbstractSpringModule implements ConfigOnOff {
	
	private static final String PROP_ENABLED = "sharepoint.enabled";
	private static final String PROP_SITES_ENABLED = "sharepoint.sites.enabled";
	private static final String PROP_SITES_WRITE = "sharepoint.sites.write";
	private static final String PROP_ONEDRIVE_ENABLED = "sharepoint.onedrive.enabled";
	private static final String PROP_ROLES_ENABLED = "sharepoint.roles.enabled";
	private static final String PROP_EXCLUDE_SITES_AND_DRIVES = "sharepoint.exclude.sites.drives";
	private static final String PROP_EXCLUDE_LABELS = "sharepoint.exclude.labels";
	private static final String PROP_SITES_CONFIGURATION = "sharepoint.sites.configuration";
	
	@Value("${sharepoint.enabled:false}")
	private boolean enabled;
	@Value("${sharepoint.sites.enabled:false}")
	private boolean sitesEnabled;
	@Value("${sharepoint.sites.write:false}")
	private boolean sitesWriteEnabled;
	@Value("${sharepoint.onedrive.enabled:false}")
	private boolean oneDriveEnabled;
	@Value("${sharepoint.roles.enabled:administrator,learnresourcemanager,author}")
	private String rolesEnabled;
	private List<OrganisationRoles> rolesEnabledList;
	
	@Value("${sharepoint.exclude.sites.drives}")
	private String excludeSitesAndDrives;
	@Value("${sharepoint.exclude.labels}")
	private String excludeLabels;
	@Value("${sharepoint.sites.configuration}")
	private String sitesConfiguration;
	
	@Autowired
	public SharePointModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	@Override
	public void init() {
		String enabledObj = getStringPropertyValue(PROP_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String sitesEnabledObj = getStringPropertyValue(PROP_SITES_ENABLED, true);
		if(StringHelper.containsNonWhitespace(sitesEnabledObj)) {
			sitesEnabled = "true".equals(sitesEnabledObj);
		}
		String sitesWriteObj = getStringPropertyValue(PROP_SITES_WRITE, true);
		if(StringHelper.containsNonWhitespace(sitesWriteObj)) {
			sitesWriteEnabled = "true".equals(sitesWriteObj);
		}
		
		String oneDriveEnabledObj = getStringPropertyValue(PROP_ONEDRIVE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(oneDriveEnabledObj)) {
			oneDriveEnabled = "true".equals(oneDriveEnabledObj);
		}
		rolesEnabled = getStringPropertyValue(PROP_ROLES_ENABLED, rolesEnabled);
		rolesEnabledList = OrganisationRoles.toValues(toList(rolesEnabled, ","));
		excludeSitesAndDrives = getStringPropertyValue(PROP_EXCLUDE_SITES_AND_DRIVES, excludeSitesAndDrives);
		excludeLabels = getStringPropertyValue(PROP_EXCLUDE_LABELS, excludeLabels);
		sitesConfiguration = getStringPropertyValue(PROP_SITES_CONFIGURATION, sitesConfiguration);
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
		setStringProperty(PROP_ENABLED, Boolean.toString(enabled), true);
	}
	
	public boolean isSitesEnabled() {
		return sitesEnabled;
	}

	public void setSitesEnabled(boolean enabled) {
		this.sitesEnabled = enabled;
		setStringProperty(PROP_SITES_ENABLED, Boolean.toString(enabled), true);
	}
	
	public boolean isSitesWriteEnabled() {
		return sitesWriteEnabled;
	}
	
	public void setSitesWriteEnabled(boolean enabled) {
		this.sitesWriteEnabled = enabled;
		setStringProperty(PROP_SITES_WRITE, Boolean.toString(enabled), true);
	}

	public boolean isOneDriveEnabled() {
		return oneDriveEnabled;
	}

	public void setOneDriveEnabled(boolean oneDriveEnabled) {
		this.oneDriveEnabled = oneDriveEnabled;
		setStringProperty(PROP_ONEDRIVE_ENABLED, Boolean.toString(oneDriveEnabled), true);
	}

	public boolean canSharePoint(UserSession usess) {
		return isEnabled() && isSitesEnabled()
				&& usess != null && usess.getOAuth2Tokens() != null && usess.getOAuth2Tokens().getUser(User.class) != null
				&& isEnabledFor(usess.getRoles());
	}
	
	private boolean isEnabledFor(Roles roles) {
		for(OrganisationRoles enabledRole:rolesEnabledList) {
			if(roles.hasRole(enabledRole)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean canOneDrive(UserSession usess) {
		return isEnabled() && isOneDriveEnabled()
				&& usess != null && usess.getOAuth2Tokens() != null && usess.getOAuth2Tokens().getUser(User.class) != null;
	}
	
	public List<String> getRolesEnabledList() {
		return toList(rolesEnabled, ",");
	}
	
	public String getRolesEnabled() {
		return rolesEnabled;
	}
	
	public void setRolesEnabledList(Collection<String> roles) {
		setRolesEnabled(Strings.join(roles, ','));
	}

	public void setRolesEnabled(String rolesEnabled) {
		this.rolesEnabled = rolesEnabled;
		rolesEnabledList = OrganisationRoles.toValues(toList(rolesEnabled, ","));
		setStringProperty(PROP_ROLES_ENABLED, rolesEnabled, true);
	}

	public List<String> getExcludeSitesAndDrives() {
		return toList(excludeSitesAndDrives);
	}

	public void setExcludeSitesAndDrives(List<String> exclusionList) {
		setExcludeSitesAndDrives(toString(exclusionList));
	}

	public void setExcludeSitesAndDrives(String exclusionList) {
		this.excludeSitesAndDrives = exclusionList;
		setStringProperty(PROP_EXCLUDE_SITES_AND_DRIVES, exclusionList, true);
	}

	public List<String> getExcludeLabels() {
		return toList(excludeLabels);
	}

	public void setExcludeLabels(List<String> exclusionList) {
		setExcludeLabels(toString(exclusionList));
	}

	public void setExcludeLabels(String exclusionList) {
		this.excludeLabels = exclusionList;
		setStringProperty(PROP_EXCLUDE_LABELS, exclusionList, true);
	}

	public SitesAndDrivesConfiguration getSitesConfiguration() {
		return SiteConfigurationXstream.fromXML(sitesConfiguration);
	}

	public void setSitesConfiguration(SitesAndDrivesConfiguration configuration) {
		sitesConfiguration = SiteConfigurationXstream.toXML(configuration);
		setStringProperty(PROP_SITES_CONFIGURATION, sitesConfiguration, true);
	}

	private String toString(List<String> list) {
		if(list == null || list.isEmpty()) return "";
		return String.join("|", list);
	}
	
	private List<String> toList(String val) {
		return toList(val, "[\n\r|]");
	}
	
	private List<String> toList(String val, String separator) {
		if(StringHelper.containsNonWhitespace(val)) {
			String[] array = val.split(separator);
			List<String> list = new ArrayList<>();
			for(String string:array) {
				if(StringHelper.containsNonWhitespace(string)) {
					list.add(string);
				}
			}
			return list;
		}
		return List.of();
	}

}
