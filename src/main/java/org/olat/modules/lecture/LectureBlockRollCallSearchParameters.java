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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 28 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockRollCallSearchParameters {
	
	private Boolean closed;
	private Boolean hasAbsence;
	private Boolean hasSupervisorNotificationDate;

	private Long rollCallKey;
	private Long lectureBlockKey;
	
	private Date startDate;
	private Date endDate;
	private String searchString;

	private IdentityRef teacher;
	private IdentityRef manager;
	private IdentityRef masterCoach;
	private IdentityRef calledIdentity;
	private RepositoryEntryRef entry;
	private List<LectureBlockAppealStatus> appealStatus;
	private List<LectureBlockRef> lectureBlockRefs;
	

	public Boolean getClosed() {
		return closed;
	}

	public void setClosed(Boolean closed) {
		this.closed = closed;
	}

	public Boolean getHasAbsence() {
		return hasAbsence;
	}

	public void setHasAbsence(Boolean hasAbsence) {
		this.hasAbsence = hasAbsence;
	}

	public Boolean getHasSupervisorNotificationDate() {
		return hasSupervisorNotificationDate;
	}

	public void setHasSupervisorNotificationDate(Boolean hasSupervisorNotificationDate) {
		this.hasSupervisorNotificationDate = hasSupervisorNotificationDate;
	}

	public Long getRollCallKey() {
		return rollCallKey;
	}

	public void setRollCallKey(Long rollCallKey) {
		this.rollCallKey = rollCallKey;
	}

	public Long getLectureBlockKey() {
		return lectureBlockKey;
	}

	public void setLectureBlockKey(Long lectureBlockKey) {
		this.lectureBlockKey = lectureBlockKey;
	}

	public RepositoryEntryRef getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntryRef entry) {
		this.entry = entry;
	}

	public List<LectureBlockAppealStatus> getAppealStatus() {
		return appealStatus;
	}

	public void setAppealStatus(List<LectureBlockAppealStatus> appealStatus) {
		this.appealStatus = appealStatus;
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

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	/**
	 * Identity which want to access the data (for permission restrictions)
	 * @return
	 */
	public IdentityRef getManager() {
		return manager;
	}

	/**
	 * Identity which want to access the data (for permission restrictions)
	 * 
	 * @param identity
	 */
	public void setManager(IdentityRef manager) {
		this.manager = manager;
	}

	public IdentityRef getTeacher() {
		return teacher;
	}

	public void setTeacher(IdentityRef teacher) {
		this.teacher = teacher;
	}

	public IdentityRef getMasterCoach() {
		return masterCoach;
	}

	public void setMasterCoach(IdentityRef masterCoach) {
		this.masterCoach = masterCoach;
	}
	
	public void setViewAs(IdentityRef identity, LectureRoles role) {
		switch(role) {
			case lecturemanager: setManager(identity); break;
			case mastercoach: setMasterCoach(identity); break;
			case teacher: setTeacher(identity); break;
			case participant:
			default: setCalledIdentity(identity); break;
		}
	}

	public IdentityRef getCalledIdentity() {
		return calledIdentity;
	}

	public void setCalledIdentity(IdentityRef calledIdentity) {
		this.calledIdentity = calledIdentity;
	}

	public List<LectureBlockRef> getLectureBlockRefs() {
		return lectureBlockRefs;
	}

	public void setLectureBlockRefs(List<? extends LectureBlockRef> lectureBlockRefs) {
		this.lectureBlockRefs = new ArrayList<>(lectureBlockRefs);
	}
}
