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

import org.olat.modules.curriculum.model.CurriculumMember;

/**
 * 
 * Initial date: 4 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "curriculumElementMemberVO")
public class CurriculumElementMemberVO {
	
	private Long identityKey;
	private String role;
	private String inheritanceMode;
	
	public CurriculumElementMemberVO() {
		//
	}
	
	public static final CurriculumElementMemberVO valueOf(CurriculumMember membership) {
		CurriculumElementMemberVO vo = new CurriculumElementMemberVO();
		vo.setIdentityKey(membership.getIdentity().getKey());
		vo.setRole(membership.getRole());
		if(membership.getInheritanceMode() != null) {
			vo.setInheritanceMode(membership.getInheritanceMode().name());
		}
		return vo;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getInheritanceMode() {
		return inheritanceMode;
	}

	public void setInheritanceMode(String inheritanceMode) {
		this.inheritanceMode = inheritanceMode;
	}
	
	

}
