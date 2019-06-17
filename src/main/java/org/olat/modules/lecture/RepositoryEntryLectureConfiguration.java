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
package org.olat.modules.lecture;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 3 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface RepositoryEntryLectureConfiguration extends CreateInfo, ModifiedInfo {

	public Long getKey();
	
	public RepositoryEntry getEntry();
	
	public boolean isOverrideModuleDefault();

	public void setOverrideModuleDefault(boolean overrideModuleDefault);
	
	public boolean isLectureEnabled();

	public void setLectureEnabled(boolean lectureEnabled);

	public Boolean getRollCallEnabled();

	public void setRollCallEnabled(Boolean rollCallEnabled);

	public Boolean getCalculateAttendanceRate();

	public void setCalculateAttendanceRate(Boolean calculateAttendanceRate);
	
	public Double getRequiredAttendanceRate();

	public void setRequiredAttendanceRate(Double requiredAttendanceRate);
	
	public Boolean getTeacherCalendarSyncEnabled();

	public void setTeacherCalendarSyncEnabled(Boolean teacherCalendarSyncEnabled);
	
	public Boolean getParticipantCalendarSyncEnabled();

	public void setParticipantCalendarSyncEnabled(Boolean participantCalendarSyncEnabled);
	
	public Boolean getCourseCalendarSyncEnabled();

	public void setCourseCalendarSyncEnabled(Boolean courseCalendarSyncEnabled);
	
	// assessment mode
	public Boolean getAssessmentModeEnabled();
	
	public void setAssessmentModeEnabled(Boolean enabled);
	
	public Integer getAssessmentModeLeadTime();

	public void setAssessmentModeLeadTime(Integer assessmentModeLeadTime);

	public Integer getAssessmentModeFollowupTime();

	public void setAssessmentModeFollowupTime(Integer assessmentModeFollowupTime);

	public String getAssessmentModeAdmissibleIps();

	public void setAssessmentModeAdmissibleIps(String assessmentModeAdmissibleIps);

	public String getAssessmentModeSebKeys();

	public void setAssessmentModeSebKeys(String assessmentModeSebKeys);
	
	
	
	
}
