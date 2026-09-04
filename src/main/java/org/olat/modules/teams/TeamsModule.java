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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.logging.Tracing;
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
	
	private static final Logger log = Tracing.createLoggerFor(TeamsModule.class);
	
	private static final String PROP_ENABLED = "vc.teams.enabled";
	private static final String PROP_GROUP_ENABLED = "vc.teams.groups";
	private static final String PROP_COURSE_ENABLED = "vc.teams.courses";
	private static final String PROP_CHAT_EXAM_ENABLED = "vc.teams.chat.exams";
	private static final String PROP_APPOINTMENTS_ENABLED = "vc.teams.appointments";
	private static final String PROP_LECTURES_ENABLED = "vc.teams.lectures";
	private static final String PROP_PRODUCER_ID = "vc.teams.producer.id";
	private static final String PROP_PERMANENT_MEETINGS_ENABLED = "vc.teams.permanent.meetings";

	private static final String PROP_RECORDINGS_ENABLED = "vc.teams.recording";
	private static final String PROP_RECORDINGS_DEFAULT_ENABLED = "vc.teams.recording.default";
	private static final String PROP_RECORDINGS_AUTO_START_ENABLED = "vc.teams.recording.auto.start";
	private static final String PROP_RECORDING_DEFAULT_PUBLICATION_SETTINGS = "vc.teams.recording.default.publication.settings";
	private static final String PROP_RECORDINGS_DELETION_DAYS = "vc.teams.recording.deletion.days";
	
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
	@Value("${vc.teams.lectures:true}")
	private String lecturesEnabled;
	
	@Value("${vc.teams.permanent.meetings:true}")
	private String permanentMeetingsEnabled;
	
	@Value("${vc.teams.recording:true}")
	private String recordingsEnabled;
	@Value("${vc.teams.recording.default:false}")
	private String recordingsDefault;
	@Value("${vc.teams.recording.auto.start:false}")
	private String recordingsAutoStart;
	@Value("${vc.teams.recording.default.publication.settings}")
	private String recordingsDefaultPublicationSettings;
	@Value("${vc.teams.recording.deletion.days}")
	private Integer recordingsDeletionDays;
	
	@Value("${vc.teams.recording.dir}")
	private String recordingsDir;
	
	/**
	 * Deployment only setting, on purpose not a persisted property: it must not be
	 * editable in the administration.
	 */
	@Value("${vc.teams.recording.token.key}")
	private String recordingTokenKey;
	
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
		lecturesEnabled = getStringPropertyValue(PROP_LECTURES_ENABLED, lecturesEnabled);
		permanentMeetingsEnabled = getStringPropertyValue(PROP_PERMANENT_MEETINGS_ENABLED, permanentMeetingsEnabled);

		recordingsEnabled = getStringPropertyValue(PROP_RECORDINGS_ENABLED, recordingsEnabled);
		recordingsDefault = getStringPropertyValue(PROP_RECORDINGS_DEFAULT_ENABLED, recordingsDefault);
		recordingsAutoStart = getStringPropertyValue(PROP_RECORDINGS_AUTO_START_ENABLED, recordingsAutoStart);
		recordingsDefaultPublicationSettings = getStringPropertyValue(PROP_RECORDING_DEFAULT_PUBLICATION_SETTINGS, recordingsDefaultPublicationSettings);
		
		String recordingsDeletionDaysObj = getStringPropertyValue(PROP_RECORDINGS_DELETION_DAYS, true);
		if(StringHelper.containsNonWhitespace(recordingsDeletionDaysObj)) {
			recordingsDeletionDays = Integer.valueOf(recordingsDeletionDaysObj);
		}
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

	public String getApiSecret() {
		return apiSecret;
	}

	public String getTenantGuid() {
		return tenantGuid;
	}
	
	public String getTenantOrganisation() {
		return organisation;
	}
	
	public String getProducerId() {
		return producerId;
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
	
	public boolean isLecturesEnabled() {
		return "true".equals(lecturesEnabled);
	}

	public void setLecturesEnabled(boolean enabled) {
		lecturesEnabled = enabled ? "true" : "false";
		setStringProperty(PROP_LECTURES_ENABLED, lecturesEnabled, true);
	}

	public boolean isPermanentMeetingsEnabled() {
		return "true".equals(permanentMeetingsEnabled);
	}

	public void setPermanentMeetings(boolean enabled) {
		permanentMeetingsEnabled = enabled ? "true" : "false";
		setStringProperty(PROP_PERMANENT_MEETINGS_ENABLED, permanentMeetingsEnabled, true);
	}

	public boolean isRecordingsEnabled() {
		return "true".equals(recordingsEnabled);
	}

	public void setRecordingsEnabled(boolean enabled) {
		recordingsEnabled = enabled ? "true" : "false";
		setStringProperty(PROP_RECORDINGS_ENABLED, recordingsEnabled, true);
	}

	public String getRecordingsDir() {
		return recordingsDir;
	}

	public boolean isRecordingsDefaultEnabled() {
		return "true".equals(recordingsDefault);
	}

	public void setRecordingsDefaultEnabled(boolean enabled) {
		recordingsDefault = enabled ? "true" : "false";
		setStringProperty(PROP_RECORDINGS_DEFAULT_ENABLED, recordingsDefault, true);
	}

	public boolean isRecordingsAutoStartEnabled() {
		return "true".equals(recordingsAutoStart);
	}

	public void setRecordingsAutoStartEnabled(boolean enabled) {
		recordingsAutoStart = enabled ? "true" : "false";
		setStringProperty(PROP_RECORDINGS_AUTO_START_ENABLED, recordingsAutoStart, true);
	}
	
	public TeamsRecordingsPublishedRoles[] getRecordingsDefaultPublicationSettings() {
		if (!StringHelper.containsNonWhitespace(recordingsDefaultPublicationSettings)) {
			return new TeamsRecordingsPublishedRoles[0];
		}
		try {
			Set<TeamsRecordingsPublishedRoles> roles = Arrays.stream(recordingsDefaultPublicationSettings.split(","))
					.map(TeamsRecordingsPublishedRoles::secureValueOf)
					.collect(Collectors.toSet());
			return roles.toArray(new TeamsRecordingsPublishedRoles[roles.size()]);
		}  catch (IllegalArgumentException e) {
			log.error("", e);
			return new TeamsRecordingsPublishedRoles[0];
		}
	}

	public void setRecordingsDefaultPublicationSettings(Set<TeamsRecordingsPublishedRoles> value) {
		recordingsDefaultPublicationSettings = publicationEnumSetToString(value);
		setStringProperty(PROP_RECORDING_DEFAULT_PUBLICATION_SETTINGS, recordingsDefaultPublicationSettings, true);
	}
	
	private String publicationEnumSetToString(Set<TeamsRecordingsPublishedRoles> value) {
		if (value == null || value.isEmpty()) {
			return "";
		}
		return value.stream().map(TeamsRecordingsPublishedRoles::name).collect(Collectors.joining(","));
	}
	
	public Integer getRecordingsDeletionDays() {
		return recordingsDeletionDays;
	}

	public void setRecordingsDeletionDays(Integer days) {
		this.recordingsDeletionDays = days;
		String value = days != null? days.toString(): null;
		setStringProperty(PROP_RECORDINGS_DELETION_DAYS, value, true);
	}
	
	public String getRecordingTokenKey() {
		return recordingTokenKey;
	}
}
