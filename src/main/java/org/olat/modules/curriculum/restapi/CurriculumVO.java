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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumManagedFlag;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 
 * Initial date: 15 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "curriculumVO")
public class CurriculumVO {
	
	private Long key;

	private String identifier;
	private String displayName;
	private String description;
	private String status;
	private String degree;
	
	private String externalId;
	@Schema(required = true, description = "Action to be performed on managedFlagsString", allowableValues = { 
			"all",
			 "identifier(all)",
			 "displayName(all)",
			 "description(all)",
			 "externalId(all)",
			 "delete(all)",
			 "members(all)"})
	private String managedFlagsString;
	
	private Long organisationKey;
	
	public CurriculumVO() {
		//
	}
	
	public static final CurriculumVO valueOf(Curriculum curriculum) {
		CurriculumVO vo = new CurriculumVO();
		vo.setKey(curriculum.getKey());
		vo.setIdentifier(curriculum.getIdentifier());
		vo.setDisplayName(curriculum.getDisplayName());
		vo.setDescription(curriculum.getDescription());
		vo.setStatus(curriculum.getStatus());
		vo.setDegree(curriculum.getDegree());
		vo.setExternalId(curriculum.getExternalId());
		vo.setManagedFlagsString(CurriculumManagedFlag.toString(curriculum.getManagedFlags()));
		if(curriculum.getOrganisation() != null) {
			vo.setOrganisationKey(curriculum.getOrganisation().getKey());
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

	public String getDegree() {
		return degree;
	}

	public void setDegree(String degree) {
		this.degree = degree;
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

	public Long getOrganisationKey() {
		return organisationKey;
	}

	public void setOrganisationKey(Long organisationKey) {
		this.organisationKey = organisationKey;
	}

}
