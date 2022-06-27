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
package org.olat.modules.teams;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TeamsModule extends AbstractSpringModule implements ConfigOnOff {

	private static final String PROP_ENABLED = "vc.teams.enabled";
	private static final String PROP_GROUP_ENABLED = "vc.teams.groups";
	private static final String PROP_COURSE_ENABLED = "vc.teams.courses";
	private static final String PROP_CHAT_EXAM_ENABLED = "vc.teams.chat.exams";
	private static final String PROP_APPOINTMENTS_ENABLED = "vc.teams.appointments";
	private static final String PROP_PRODUCER_ID = "vc.teams.producer.id";
	
	private static final String MSGRAPH_API_KEY = "vc.teams.api.key";
	private static final String MSGRAPH_API_SECRET = "vc.teams.api.secret";
	private static final String MSGRAPH_TENANT_GUID = "vc.teams.tenant.guid";
	private static final String MSGRAPH_TENANT_ORG = "vc.teams.tenant.organisation";
	
	@Value("${vc.teams.enabled}")
	private boolean enabled;

	@Value("${vc.teams.api.key}")
	private String apiKey;
	@Value("${vc.teams.api.secret}")
	private String apiSecret;
	@Value("${vc.teams.tenant.guid}")
	private String tenantGuid;
	@Value("${vc.teams.tenant.organisation}")
	private String organisation;
	@Value("${vc.teams.producer.id}")
	private String producerId;
	
	@Value("${vc.teams.groups:true}")
	private String groupsEnabled;
	@Value("${vc.teams.courses:true}")
	private String coursesEnabled;
	@Value("${vc.teams.chat.exams:true}")
	private String chatExamsEnabled;
	@Value("${vc.teams.appointments:true}")
	private String appointmentsEnabled;
	
	@Autowired
	public TeamsModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		String enabledObj = getStringPropertyValue(PROP_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		apiKey = getStringPropertyValue(MSGRAPH_API_KEY, apiKey);
		apiSecret = getStringPropertyValue(MSGRAPH_API_SECRET, apiSecret);
		tenantGuid = getStringPropertyValue(MSGRAPH_TENANT_GUID, tenantGuid);
		organisation = getStringPropertyValue(MSGRAPH_TENANT_ORG, organisation);
		producerId = getStringPropertyValue(PROP_PRODUCER_ID, producerId);
		
		groupsEnabled = getStringPropertyValue(PROP_GROUP_ENABLED, groupsEnabled);
		coursesEnabled = getStringPropertyValue(PROP_COURSE_ENABLED, coursesEnabled);
		chatExamsEnabled = getStringPropertyValue(PROP_CHAT_EXAM_ENABLED, chatExamsEnabled);
		appointmentsEnabled = getStringPropertyValue(PROP_APPOINTMENTS_ENABLED, appointmentsEnabled);
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

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
		setStringProperty(MSGRAPH_API_KEY, apiKey, true);
	}

	public String getApiSecret() {
		return apiSecret;
	}

	public void setApiSecret(String apiSecret) {
		this.apiSecret = apiSecret;
		setStringProperty(MSGRAPH_API_SECRET, apiSecret, true);
	}

	public String getTenantGuid() {
		return tenantGuid;
	}

	public void setTenantGuid(String tenantGuid) {
		this.tenantGuid = tenantGuid;
		setStringProperty(MSGRAPH_TENANT_GUID, tenantGuid, true);
	}
	
	public String getTenantOrganisation() {
		return organisation;
	}
	
	public void setTenantOrganisation(String organisation) {
		this.organisation = organisation;
		setStringProperty(MSGRAPH_TENANT_ORG, organisation, true);
	}
	
	public String getProducerId() {
		return producerId;
	}

	public void setProducerId(String producerId) {
		this.producerId = producerId;
		setStringProperty(PROP_PRODUCER_ID, producerId, true);
	}

	public boolean isGroupsEnabled() {
		return "true".equals(groupsEnabled);
	}

	public void setGroupsEnabled(boolean enabled) {
		groupsEnabled = enabled ? "true" : "false";
		setStringProperty(PROP_GROUP_ENABLED, groupsEnabled, true);
	}

	public boolean isCoursesEnabled() {
		return "true".equals(coursesEnabled);
	}

	public void setCoursesEnabled(boolean enabled) {
		coursesEnabled = enabled ? "true" : "false";
		setStringProperty(PROP_COURSE_ENABLED, coursesEnabled, true);
	}
	
	public boolean isChatExamsEnabled() {
		return "true".equals(chatExamsEnabled);
	}

	public void setChatExamsEnabled(boolean enabled) {
		chatExamsEnabled = enabled ? "true" : "false";
		setStringProperty(PROP_CHAT_EXAM_ENABLED, chatExamsEnabled, true);
	}
	
	public boolean isAppointmentsEnabled() {
		return "true".equals(appointmentsEnabled);
	}

	public void setAppointmentsEnabled(boolean enabled) {
		appointmentsEnabled = enabled ? "true" : "false";
		setStringProperty(PROP_APPOINTMENTS_ENABLED, appointmentsEnabled, true);
	}
}
