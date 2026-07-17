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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.repository.RepositoryEntryRef;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

/**
 * 
 * Initial date: 8 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "lectureBlocksVO")
public class LectureBlockVO implements LectureBlockRef {
	
	private Long key;
	private String externalId;
	private String externalRef;
	
	@Schema(requiredMode = RequiredMode.NOT_REQUIRED, description = "Action to be performed on managedFlagsString", allowableValues = { 
			"all",
		      "details(all) //details tab",
		        "title(details,all)",
		        "compulsory(details,all)",
		        "plannedLectures(details,all)",
		        "teachers(details,all)",
		        "groups(details,all)",
		        "description(details,all)",
		        "preparation(details,all)",
		        "location(details,all)",
		        "dates(details,all)",
		      "settings(all)",
		      "delete(all)"})
	private String managedFlagsString;
	
	private String title;
	private String description;
	private String preparation;
	private String location;
	private String comment;
	
	private Date startDate;
	private Date endDate;
	private Boolean compulsory;
	private int plannedLectures;
	
	private String status;
	private String rollCallStatus;
	
	private String meetingTitle;
	private String meetingUrl;
	private String recordingUrl;
	
	@Schema(accessMode = AccessMode.READ_ONLY, description = "Signal the presence of a BigBlueButton meeting")
	private Long bigBlueButtonMeetingKey;

	private Long repoEntryKey;
	private Long curriculumElementKey;
	
	public LectureBlockVO() {
		//make jaxb happy
	}
	
	public static final LectureBlockVO valueOf(LectureBlock block, RepositoryEntryRef repoEntry, CurriculumElementRef curriculumElement) {
		LectureBlockVO vo = new LectureBlockVO();
		vo.setKey(block.getKey());
		vo.setExternalId(block.getExternalId());
		vo.setExternalRef(block.getExternalRef());
		vo.setManagedFlagsString(block.getManagedFlagsString());
		vo.setTitle(block.getTitle());
		vo.setDescription(block.getDescription());
		vo.setPreparation(block.getPreparation());
		vo.setLocation(block.getLocation());
		vo.setComment(block.getComment());
		
		vo.setStartDate(block.getStartDate());
		vo.setEndDate(block.getEndDate());
		vo.setCompulsory(block.isCompulsory()); 
		vo.setPlannedLectures(block.getPlannedLecturesNumber());
		
		if(repoEntry != null) {
			vo.setRepoEntryKey(repoEntry.getKey());
		}
		if(curriculumElement != null) {
			vo.setCurriculumElementKey(curriculumElement.getKey());
		}

		vo.setMeetingTitle(block.getMeetingTitle());
		vo.setMeetingUrl(block.getMeetingUrl());
		vo.setRecordingUrl(block.getRecordingUrl());
		
		if(block.getStatus() != null) {
			vo.setStatus(block.getStatus().name());
		}
		if(block.getRollCallStatus() != null) {
			vo.setRollCallStatus(block.getRollCallStatus().name());
		}
		if(block.getBBBMeeting() != null) {
			vo.setBigBlueButtonMeetingKey(block.getBBBMeeting().getKey());
		}
		return vo;
	}
	
	public void transferTo(LectureBlock block) {
		if(getExternalId() != null) {
			block.setExternalId(getExternalId());
		}
		if(getExternalRef() != null) {
			block.setExternalRef(getExternalRef());
		}
		if(getTitle() != null) {
			block.setTitle(getTitle());
		}
		if(getDescription() != null) {
			block.setDescription(getDescription());
		}
		if(getPreparation() != null) {
			block.setPreparation(getPreparation());
		}
		if(getLocation() != null) {
			block.setLocation(getLocation());
		}
		if(getComment() != null) {
			block.setComment(getComment());
		}
		if(getStartDate() != null) {
			block.setStartDate(getStartDate());
		}
		if(getEndDate() != null) {
			block.setEndDate(getEndDate());
		}
		if(getCompulsory() != null) {
			block.setCompulsory(getCompulsory().booleanValue());
		}
		if(getManagedFlagsString() != null) {
			block.setManagedFlagsString(getManagedFlagsString());
		}
		block.setPlannedLecturesNumber(getPlannedLectures());
		if(getMeetingTitle() != null) {
			block.setMeetingTitle(getMeetingTitle());
		}
		if(getMeetingUrl() != null) {
			block.setMeetingUrl(getMeetingUrl());
		}
		if(getRecordingUrl() != null) {
			block.setRecordingUrl(getRecordingUrl());
		}
	}

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public Long getRepoEntryKey() {
		return repoEntryKey;
	}

	public void setRepoEntryKey(Long repoEntryKey) {
		this.repoEntryKey = repoEntryKey;
	}

	public Long getCurriculumElementKey() {
		return curriculumElementKey;
	}

	public void setCurriculumElementKey(Long curriculumElementKey) {
		this.curriculumElementKey = curriculumElementKey;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getExternalRef() {
		return externalRef;
	}

	public void setExternalRef(String externalRef) {
		this.externalRef = externalRef;
	}

	public String getManagedFlagsString() {
		return managedFlagsString;
	}

	public void setManagedFlagsString(String managedFlagsString) {
		this.managedFlagsString = managedFlagsString;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPreparation() {
		return preparation;
	}

	public void setPreparation(String preparation) {
		this.preparation = preparation;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Boolean getCompulsory() {
		return compulsory;
	}

	public void setCompulsory(Boolean compulsory) {
		this.compulsory = compulsory;
	}

	public int getPlannedLectures() {
		return plannedLectures;
	}

	public void setPlannedLectures(int plannedLectures) {
		this.plannedLectures = plannedLectures;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRollCallStatus() {
		return rollCallStatus;
	}

	public void setRollCallStatus(String rollCallStatus) {
		this.rollCallStatus = rollCallStatus;
	}

	public String getMeetingTitle() {
		return meetingTitle;
	}

	public void setMeetingTitle(String meetingTitle) {
		this.meetingTitle = meetingTitle;
	}

	public String getMeetingUrl() {
		return meetingUrl;
	}

	public void setMeetingUrl(String meetingUrl) {
		this.meetingUrl = meetingUrl;
	}

	public String getRecordingUrl() {
		return recordingUrl;
	}

	public void setRecordingUrl(String recordingUrl) {
		this.recordingUrl = recordingUrl;
	}

	public Long getBigBlueButtonMeetingKey() {
		return bigBlueButtonMeetingKey;
	}

	public void setBigBlueButtonMeetingKey(Long bigBlueButtonMeetingKey) {
		this.bigBlueButtonMeetingKey = bigBlueButtonMeetingKey;
	}
	
	@Override
	public String toString() {
		return "LectureBlockVO[key=" + key + ":name=" + title + "]";
	}
	
	@Override
	public int hashCode() {
		return key == null ? -64582123 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof LectureBlockVO block) {
			return key != null && key.equals(block.key);
		}
		return false;
	}
}
