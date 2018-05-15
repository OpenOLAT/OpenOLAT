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
package org.olat.modules.curriculum.restapi;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;

/**
 * 
 * Initial date: 15 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "curriculumElementVO")
public class CurriculumElementVO {
	
	private Long key;
	private String identifier;
	private String displayName;
	private String description;
	
	private String status;
	private Date beginDate;
	private Date endDate;
	
	private String externalId;
	private String managedFlagsString;

	private Long parentElementKey;
	private Long curriculumKey;
	private Long curriculumElementTypeKey;
	
	
	public CurriculumElementVO() {
		//
	}
	
	public static final CurriculumElementVO valueOf(CurriculumElement element) {
		CurriculumElementVO vo = new CurriculumElementVO();
		vo.setKey(element.getKey());
		vo.setIdentifier(element.getIdentifier());
		vo.setDisplayName(element.getDisplayName());
		vo.setDescription(element.getDescription());
		vo.setStatus(element.getStatus());
		vo.setBeginDate(element.getBeginDate());
		vo.setEndDate(element.getEndDate());
		vo.setExternalId(element.getExternalId());
		vo.setManagedFlagsString(CurriculumElementManagedFlag.toString(element.getManagedFlags()));
		if(element.getParent() != null) {
			vo.setParentElementKey(element.getParent().getKey());
		}
		vo.setCurriculumKey(element.getCurriculum().getKey());
		if(element.getType() != null) {
			vo.setCurriculumElementTypeKey(element.getType().getKey());
		}
		return vo;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getManagedFlagsString() {
		return managedFlagsString;
	}

	public void setManagedFlagsString(String managedFlagsString) {
		this.managedFlagsString = managedFlagsString;
	}

	public Long getParentElementKey() {
		return parentElementKey;
	}

	public void setParentElementKey(Long parentElementKey) {
		this.parentElementKey = parentElementKey;
	}

	public Long getCurriculumKey() {
		return curriculumKey;
	}

	public void setCurriculumKey(Long curriculumKey) {
		this.curriculumKey = curriculumKey;
	}

	public Long getCurriculumElementTypeKey() {
		return curriculumElementTypeKey;
	}

	public void setCurriculumElementTypeKey(Long curriculumElementTypeKey) {
		this.curriculumElementTypeKey = curriculumElementTypeKey;
	}

	
}
