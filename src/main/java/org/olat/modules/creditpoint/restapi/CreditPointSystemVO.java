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
package org.olat.modules.creditpoint.restapi;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.modules.creditpoint.CreditPointSystem;

/**
 * 
 * Initial date: 21 avr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "creditPointSystemVO")
public class CreditPointSystemVO {

	private Long key;
	
	private String name;
	private String label;
	private String description;
	
	private Integer defaultExpiration;
	private String defaultExpirationUnit;
	private String status;
	
	private Boolean rolesRestrictions;
	private Boolean organisationsRestrictions;
	
	public CreditPointSystemVO() {
		//
	}
	
	public static final CreditPointSystemVO valueOf(CreditPointSystem system) {
		CreditPointSystemVO vo = new CreditPointSystemVO();
		vo.setKey(system.getKey());
		vo.setName(system.getName());
		vo.setLabel(system.getLabel());
		vo.setDescription(system.getDescription());
		vo.setDefaultExpiration(system.getDefaultExpiration());
		if(system.getDefaultExpirationUnit() != null) {
			vo.setDefaultExpirationUnit(system.getDefaultExpirationUnit().name());
		}
		vo.setStatus(system.getStatus().name());
		vo.setRolesRestrictions(Boolean.valueOf(system.isRolesRestrictions()));
		vo.setOrganisationsRestrictions(Boolean.valueOf(system.isOrganisationsRestrictions()));
		return vo;
	}
	
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getDefaultExpiration() {
		return defaultExpiration;
	}

	public void setDefaultExpiration(Integer defaultExpiration) {
		this.defaultExpiration = defaultExpiration;
	}

	public String getDefaultExpirationUnit() {
		return defaultExpirationUnit;
	}

	public void setDefaultExpirationUnit(String defaultExpirationUnit) {
		this.defaultExpirationUnit = defaultExpirationUnit;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Boolean getRolesRestrictions() {
		return rolesRestrictions;
	}

	public void setRolesRestrictions(Boolean rolesRestrictions) {
		this.rolesRestrictions = rolesRestrictions;
	}

	public Boolean getOrganisationsRestrictions() {
		return organisationsRestrictions;
	}

	public void setOrganisationsRestrictions(Boolean organisationsRestrictions) {
		this.organisationsRestrictions = organisationsRestrictions;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 26169661 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof CreditPointSystemVO system) {
			return getKey() != null && getKey().equals(system.getKey());
		}
		return false;
	}
}
