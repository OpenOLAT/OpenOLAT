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
package org.olat.modules.lecture.restapi;

import java.util.Date;

/**
 * 
 * 
 * Initial date: 20 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Examples {
	
	public static final LectureBlockVO SAMPLE_LECTUREBLOCKVO = new LectureBlockVO();
	public static final LectureBlockRollCallVO SAMPLE_LECTUREBLOCKROLLCALLVO = new LectureBlockRollCallVO();
	public static final RepositoryEntryLectureConfigurationVO SAMPLE_REPOSITORYENTRYLECTURECONFIGURATIONVO = new RepositoryEntryLectureConfigurationVO();
  
	static {
		SAMPLE_LECTUREBLOCKVO.setKey(345l);
		SAMPLE_LECTUREBLOCKVO.setTitle("Lecture");
		SAMPLE_LECTUREBLOCKVO.setDescription("A little description");
		SAMPLE_LECTUREBLOCKVO.setComment("A comment");
		SAMPLE_LECTUREBLOCKVO.setLocation("The secret location");
		SAMPLE_LECTUREBLOCKVO.setManagedFlagsString("all");
		SAMPLE_LECTUREBLOCKVO.setPreparation("Lot of");
		SAMPLE_LECTUREBLOCKVO.setPlannedLectures(4);
		SAMPLE_LECTUREBLOCKVO.setExternalId("EXT-234");
		SAMPLE_LECTUREBLOCKVO.setStartDate(new Date());
		SAMPLE_LECTUREBLOCKVO.setEndDate(new Date());
		
		SAMPLE_LECTUREBLOCKROLLCALLVO.setKey(23l);
		SAMPLE_LECTUREBLOCKROLLCALLVO.setLecturesAbsentNumber(2);
		SAMPLE_LECTUREBLOCKROLLCALLVO.setLecturesAttendedNumber(3);
		SAMPLE_LECTUREBLOCKROLLCALLVO.setComment("A comment");
		SAMPLE_LECTUREBLOCKROLLCALLVO.setAbsenceReason("The reason of the absence");
		SAMPLE_LECTUREBLOCKROLLCALLVO.setAbsenceAuthorized(Boolean.TRUE);
		SAMPLE_LECTUREBLOCKROLLCALLVO.setAbsenceSupervisorNotificationDate(new Date());
		SAMPLE_LECTUREBLOCKROLLCALLVO.setIdentityKey(2439873895l);
		SAMPLE_LECTUREBLOCKROLLCALLVO.setLectureBlockKey(345l);
		
		SAMPLE_REPOSITORYENTRYLECTURECONFIGURATIONVO.setCalculateAttendanceRate(Boolean.TRUE);
		SAMPLE_REPOSITORYENTRYLECTURECONFIGURATIONVO.setLectureEnabled(Boolean.TRUE);
		SAMPLE_REPOSITORYENTRYLECTURECONFIGURATIONVO.setCalculateAttendanceRate(Boolean.TRUE);
		SAMPLE_REPOSITORYENTRYLECTURECONFIGURATIONVO.setOverrideModuleDefault(Boolean.TRUE);
		SAMPLE_REPOSITORYENTRYLECTURECONFIGURATIONVO.setCourseCalendarSyncEnabled(Boolean.TRUE);
		SAMPLE_REPOSITORYENTRYLECTURECONFIGURATIONVO.setRequiredAttendanceRate(34.0d);
		SAMPLE_REPOSITORYENTRYLECTURECONFIGURATIONVO.setRollCallEnabled(Boolean.TRUE);
		SAMPLE_REPOSITORYENTRYLECTURECONFIGURATIONVO.setTeacherCalendarSyncEnabled(Boolean.TRUE);
	}
}
