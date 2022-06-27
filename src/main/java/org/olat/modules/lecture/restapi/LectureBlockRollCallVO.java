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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.olat.modules.lecture.LectureBlockRollCall;

/**
 * 
 * Initial date: 28 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "lectureBlockRollCallVO")
public class LectureBlockRollCallVO {

	private Long key;

	private Integer lecturesAttendedNumber;
	private Integer lecturesAbsentNumber;
	
	private String comment;
	private String absenceReason;
	private Boolean absenceAuthorized;
	private Date absenceSupervisorNotificationDate;
	
	private Long identityKey;
	private String identityExternalId;
	private Long lectureBlockKey;
	
	public LectureBlockRollCallVO() {
		//
	}
	
	public LectureBlockRollCallVO(LectureBlockRollCall rollCall) {
		key = rollCall.getKey();
		comment = rollCall.getComment();
		absenceReason = rollCall.getAbsenceReason();
		absenceAuthorized = rollCall.getAbsenceAuthorized();
		absenceSupervisorNotificationDate = rollCall.getAbsenceSupervisorNotificationDate();
		
		if(rollCall.getLecturesAbsentNumber() > 0) {
			lecturesAbsentNumber = Integer.valueOf(rollCall.getLecturesAbsentNumber());
		}
		if(rollCall.getLecturesAttendedNumber() > 0) {
			lecturesAttendedNumber = Integer.valueOf(rollCall.getLecturesAttendedNumber());
		}

		identityKey = rollCall.getIdentity().getKey();
		identityExternalId = rollCall.getIdentity().getExternalId();
		lectureBlockKey = rollCall.getLectureBlock().getKey();		
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public Integer getLecturesAttendedNumber() {
		return lecturesAttendedNumber;
	}

	public void setLecturesAttendedNumber(Integer lecturesAttendedNumber) {
		this.lecturesAttendedNumber = lecturesAttendedNumber;
	}

	public Integer getLecturesAbsentNumber() {
		return lecturesAbsentNumber;
	}

	public void setLecturesAbsentNumber(Integer lecturesAbsentNumber) {
		this.lecturesAbsentNumber = lecturesAbsentNumber;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getAbsenceReason() {
		return absenceReason;
	}

	public void setAbsenceReason(String absenceReason) {
		this.absenceReason = absenceReason;
	}

	public Boolean getAbsenceAuthorized() {
		return absenceAuthorized;
	}

	public void setAbsenceAuthorized(Boolean absenceAuthorized) {
		this.absenceAuthorized = absenceAuthorized;
	}

	public Date getAbsenceSupervisorNotificationDate() {
		return absenceSupervisorNotificationDate;
	}

	public void setAbsenceSupervisorNotificationDate(Date absenceSupervisorNotificationDate) {
		this.absenceSupervisorNotificationDate = absenceSupervisorNotificationDate;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	public String getIdentityExternalId() {
		return identityExternalId;
	}

	public void setIdentityExternalId(String identityExternalId) {
		this.identityExternalId = identityExternalId;
	}

	public Long getLectureBlockKey() {
		return lectureBlockKey;
	}

	public void setLectureBlockKey(Long lectureBlockKey) {
		this.lectureBlockKey = lectureBlockKey;
	}
}
