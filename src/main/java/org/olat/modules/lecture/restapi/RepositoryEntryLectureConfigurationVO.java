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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;

/**
 * 
 * Initial date: 8 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "repositoryEntryLectureConfigurationVO")
public class RepositoryEntryLectureConfigurationVO {
	
	private Boolean lectureEnabled;
	private Boolean overrideModuleDefault;
	private Boolean rollCallEnabled;
	private Boolean calculateAttendanceRate;
	private Double requiredAttendanceRate;
	private Boolean teacherCalendarSyncEnabled;
	private Boolean courseCalendarSyncEnabled;
	
	
	public RepositoryEntryLectureConfigurationVO() {
		// make JAXB happy
	}
	
	public RepositoryEntryLectureConfigurationVO(RepositoryEntryLectureConfiguration config) {
		lectureEnabled = config.isLectureEnabled();
		overrideModuleDefault = config.isOverrideModuleDefault();
		rollCallEnabled = config.getRollCallEnabled();
		calculateAttendanceRate = config.getCalculateAttendanceRate();
		requiredAttendanceRate = config.getRequiredAttendanceRate();
		teacherCalendarSyncEnabled = config.getTeacherCalendarSyncEnabled();
		courseCalendarSyncEnabled = config.getCourseCalendarSyncEnabled();
	}
	
	public Boolean getLectureEnabled() {
		return lectureEnabled;
	}
	
	public void setLectureEnabled(Boolean lectureEnabled) {
		this.lectureEnabled = lectureEnabled;
	}
	
	public Boolean getOverrideModuleDefault() {
		return overrideModuleDefault;
	}
	
	public void setOverrideModuleDefault(Boolean overrideModuleDefault) {
		this.overrideModuleDefault = overrideModuleDefault;
	}
	
	public Boolean getRollCallEnabled() {
		return rollCallEnabled;
	}
	
	public void setRollCallEnabled(Boolean rollCallEnabled) {
		this.rollCallEnabled = rollCallEnabled;
	}
	
	public Boolean getCalculateAttendanceRate() {
		return calculateAttendanceRate;
	}
	
	public void setCalculateAttendanceRate(Boolean calculateAttendanceRate) {
		this.calculateAttendanceRate = calculateAttendanceRate;
	}
	
	public Double getRequiredAttendanceRate() {
		return requiredAttendanceRate;
	}
	
	public void setRequiredAttendanceRate(Double requiredAttendanceRate) {
		this.requiredAttendanceRate = requiredAttendanceRate;
	}
	
	public Boolean getTeacherCalendarSyncEnabled() {
		return teacherCalendarSyncEnabled;
	}
	
	public void setTeacherCalendarSyncEnabled(Boolean teacherCalendarSyncEnabled) {
		this.teacherCalendarSyncEnabled = teacherCalendarSyncEnabled;
	}

	public Boolean getCourseCalendarSyncEnabled() {
		return courseCalendarSyncEnabled;
	}

	public void setCourseCalendarSyncEnabled(Boolean courseCalendarSyncEnabled) {
		this.courseCalendarSyncEnabled = courseCalendarSyncEnabled;
	}
}
