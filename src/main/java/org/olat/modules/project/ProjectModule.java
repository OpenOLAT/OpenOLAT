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
package org.olat.modules.project;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.modules.project.site.ProjectsContextEntryControllerCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 21 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ProjectModule extends AbstractSpringModule implements ConfigOnOff {
	
	private static final String KEY_ENABLED = "project.enabled";
	private static final String KEY_CREATE_ALL_ROLES = "project.create.roles.all";
	private static final String KEY_CREATE_ROLES= "project.create.roles";

	@Value("${project.enabled:true}")
	private boolean enabled;
	@Value("${project.create.roles.all:false}")
	private boolean createAllRoles;
	@Value("${project.create.roles}")
	private String createRolesStr;
	private Set<OrganisationRoles> createRoles;
	
	@Autowired
	public ProjectModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		NewControllerFactory.getInstance().addContextEntryControllerCreator("Projects", new ProjectsContextEntryControllerCreator(this));
		
		String enabledObj = getStringPropertyValue(KEY_ENABLED, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String createAllRolesObj = getStringPropertyValue(KEY_CREATE_ALL_ROLES, true);
		if (StringHelper.containsNonWhitespace(createAllRolesObj)) {
			createAllRoles = "true".equals(createAllRolesObj);
		}
		
		createRolesStr = getStringPropertyValue(KEY_CREATE_ROLES, createRolesStr);
		createRoles = null;
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
		setBooleanProperty(KEY_ENABLED, enabled, true);
	}

	public boolean isCreateAllRoles() {
		return createAllRoles;
	}

	public void setCreateAllRoles(boolean createAllRoles) {
		this.createAllRoles = createAllRoles;
		setBooleanProperty(KEY_CREATE_ALL_ROLES, createAllRoles, true);
	}
	
	public boolean canCreateProject(Roles roles) {
		return createAllRoles
				|| roles.isAdministrator()
				|| roles.isProjectManager()
				|| getCreateRoles().stream().anyMatch(role -> roles.hasRole(role));
	}
	
	public Set<OrganisationRoles> getCreateRoles() {
		if (createRoles == null) {
			if (StringHelper.containsNonWhitespace(createRolesStr)) {
				createRoles = Arrays.stream(createRolesStr.split(",")).map(OrganisationRoles::valueOf).collect(Collectors.toSet());
			} else {
				createRoles = Set.of();
			}
		}
		return createRoles;
	}

	public void setCreateRoles(Set<OrganisationRoles> createRoles) {
		this.createRoles = null;
		this.createRolesStr = createRoles != null && !createRoles.isEmpty()
				? createRoles.stream().map(OrganisationRoles::name).collect(Collectors.joining(","))
				: null;
		setStringProperty(KEY_CREATE_ROLES, createRolesStr, true);
	}
	
}
