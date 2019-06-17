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
package org.olat.modules.lecture.ui;

import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;

/**
 * An helper to calculate the configuration with the override.
 * 
 * Initial date: 4 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfigurationHelper {

	public static boolean isRollCallEnabled(RepositoryEntryLectureConfiguration lectureConfig, LectureModule lectureModule) {
		return (lectureConfig != null && lectureConfig.isOverrideModuleDefault() && lectureConfig.getRollCallEnabled() != null && lectureConfig.getRollCallEnabled().booleanValue())
				|| ((lectureConfig == null || !lectureConfig.isOverrideModuleDefault()) && lectureModule.isRollCallDefaultEnabled())
				|| (lectureModule.isCanOverrideStandardConfiguration() && (lectureConfig == null || lectureConfig.getRollCallEnabled() == null) && lectureModule.isRollCallDefaultEnabled());
	}

	public static boolean isSyncTeacherCalendarEnabled(RepositoryEntryLectureConfiguration lectureConfig, LectureModule lectureModule) {
		return (lectureConfig != null && lectureConfig.isOverrideModuleDefault() && lectureConfig.getTeacherCalendarSyncEnabled() != null && lectureConfig.getTeacherCalendarSyncEnabled().booleanValue())
				|| ((lectureConfig == null || !lectureConfig.isOverrideModuleDefault()) && lectureModule.isTeacherCalendarSyncEnabledDefault());
	}
	
	public static boolean isSyncCourseCalendarEnabled(RepositoryEntryLectureConfiguration lectureConfig, LectureModule lectureModule) {
		return (lectureConfig != null && lectureConfig.isOverrideModuleDefault() && lectureConfig.getCourseCalendarSyncEnabled() != null && lectureConfig.getCourseCalendarSyncEnabled().booleanValue())
				|| ((lectureConfig == null || !lectureConfig.isOverrideModuleDefault()) && lectureModule.isCourseCalendarSyncEnabledDefault());
	}
	
	public static boolean isRateEnabled(RepositoryEntryLectureConfiguration lectureConfig, LectureModule lectureModule) {
		if(lectureConfig != null && lectureConfig.isOverrideModuleDefault()) {
			return lectureConfig.getCalculateAttendanceRate() == null ?
					lectureModule.isRollCallCalculateAttendanceRateDefaultEnabled() : lectureConfig.getCalculateAttendanceRate().booleanValue();
		}
		return lectureModule.isRollCallCalculateAttendanceRateDefaultEnabled();
	}
	
	public static boolean isAssessmentModeEnabled(RepositoryEntryLectureConfiguration lectureConfig, LectureModule lectureModule) {
		if(lectureConfig != null && lectureConfig.isOverrideModuleDefault()) {
			return lectureConfig.getAssessmentModeEnabled() == null ?
					lectureModule.isAssessmentModeEnabledDefault() : lectureConfig.getAssessmentModeEnabled().booleanValue();
		}
		return lectureModule.isAssessmentModeEnabledDefault();
	}
	
	public static int getLeadTime(RepositoryEntryLectureConfiguration lectureConfig, LectureModule lectureModule) {
		if(lectureConfig != null && lectureConfig.isOverrideModuleDefault()) {
			return lectureConfig.getAssessmentModeLeadTime() == null ?
					lectureModule.getAssessmentModeLeadTime() : lectureConfig.getAssessmentModeLeadTime().intValue();
		}
		return lectureModule.getAssessmentModeLeadTime();
	}
	
	public static int getFollowupTime(RepositoryEntryLectureConfiguration lectureConfig, LectureModule lectureModule) {
		if(lectureConfig != null && lectureConfig.isOverrideModuleDefault()) {
			return lectureConfig.getAssessmentModeFollowupTime() == null ?
					lectureModule.getAssessmentModeFollowupTime() : lectureConfig.getAssessmentModeFollowupTime().intValue();
		}
		return lectureModule.getAssessmentModeFollowupTime();
	}
	
	public static String getAdmissibleIps(RepositoryEntryLectureConfiguration lectureConfig, LectureModule lectureModule) {
		if(lectureConfig != null && lectureConfig.isOverrideModuleDefault()) {
			return lectureConfig.getAssessmentModeAdmissibleIps() == null ?
					lectureModule.getAssessmentModeAdmissibleIps() : lectureConfig.getAssessmentModeAdmissibleIps();
		}
		return lectureModule.getAssessmentModeAdmissibleIps();
	}
	
	public static String getSebKeys(RepositoryEntryLectureConfiguration lectureConfig, LectureModule lectureModule) {
		if(lectureConfig != null && lectureConfig.isOverrideModuleDefault()) {
			return lectureConfig.getAssessmentModeSebKeys() == null ?
					lectureModule.getAssessmentModeSebKeys() : lectureConfig.getAssessmentModeSebKeys();
		}
		return lectureModule.getAssessmentModeSebKeys();
	}
}
