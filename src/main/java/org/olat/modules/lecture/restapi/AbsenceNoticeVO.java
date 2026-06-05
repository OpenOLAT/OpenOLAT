/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.lecture.restapi;

import java.util.Date;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.modules.lecture.AbsenceNotice;

/**
 * 
 * Initial date: 4 juin 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "lectureBlockRollCallVO")
public class AbsenceNoticeVO {
	
	private Long key;
	private Date creationDate;
	private Date lastModified;
	
	private String type;
	private String absenceReason;
	private Boolean absenceAuthorized;
	
	private Date startDate;
	private Date endDate;
	private String target;

	private Long identityKey;
	private Long absenceCategoryKey;
	
	private List<Long> lectureBlocks;
	private List<Long> repositoryEntries;
	
	public AbsenceNoticeVO() {
		//
	}
	
	public static final AbsenceNoticeVO valueOf(AbsenceNotice notice, List<Long> lectureBlocks, List<Long> repositoryEntries) {
		AbsenceNoticeVO vo = new AbsenceNoticeVO();
		vo.setKey(notice.getKey());
		vo.setCreationDate(notice.getCreationDate());
		vo.setLastModified(notice.getLastModified());
		if(notice.getNoticeType() != null) {
			vo.setType(notice.getNoticeType().name());
		}
		if(notice.getNoticeTarget() != null) {
			vo.setTarget(notice.getNoticeTarget().name());
		}
		vo.setStartDate(notice.getStartDate());
		vo.setEndDate(notice.getEndDate());
		vo.setAbsenceReason(notice.getAbsenceReason());
		vo.setAbsenceAuthorized(notice.getAbsenceAuthorized());
		vo.setIdentityKey(notice.getIdentity().getKey());
		vo.setLectureBlocks(lectureBlocks);
		vo.setRepositoryEntries(repositoryEntries);
		
		if(notice.getAbsenceCategory() != null) {
			vo.setAbsenceCategoryKey(notice.getAbsenceCategory().getKey());
		}
		return vo;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	public Long getAbsenceCategoryKey() {
		return absenceCategoryKey;
	}

	public void setAbsenceCategoryKey(Long absenceCategoryKey) {
		this.absenceCategoryKey = absenceCategoryKey;
	}
	
	public List<Long> getLectureBlocks() {
		return lectureBlocks;
	}

	public void setLectureBlocks(List<Long> lectureBlocks) {
		this.lectureBlocks = lectureBlocks;
	}

	public List<Long> getRepositoryEntries() {
		return repositoryEntries;
	}

	public void setRepositoryEntries(List<Long> repositoryEntries) {
		this.repositoryEntries = repositoryEntries;
	}

	@Override
	public String toString() {
		return "AbsenceNoticeVO[key=" + key + ":type=" + type + "]";
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
		if(obj instanceof AbsenceNoticeVO notice) {
			return key != null && key.equals(notice.key);
		}
		return false;
	}
}
