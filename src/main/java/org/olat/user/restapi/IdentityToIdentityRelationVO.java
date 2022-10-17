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
package org.olat.user.restapi;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.basesecurity.IdentityToIdentityRelation;
import org.olat.basesecurity.IdentityToIdentityRelationManagedFlag;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 
 * Initial date: 31 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "identityToIdentityRelationVO")
public class IdentityToIdentityRelationVO {
	
	private Long key;
	private Long identitySourceKey;
	private Long identityTargetKey;

	private String externalId;
	@Schema(required = true, description = "Action to be performed on managedFlagsString", allowableValues = { 
			"all",
			"delete(all)"})
	private String managedFlagsString;
	
	private Long relationRoleKey;
	private String relationRole;
	
	public IdentityToIdentityRelationVO() {
		//
	}
	
	public static IdentityToIdentityRelationVO valueOf(IdentityToIdentityRelation relation) {
		IdentityToIdentityRelationVO relationVo = new IdentityToIdentityRelationVO();
		relationVo.setKey(relation.getKey());
		relationVo.setExternalId(relation.getExternalId());
		relationVo.setIdentitySourceKey(relation.getSource().getKey());
		relationVo.setIdentityTargetKey(relation.getTarget().getKey());
		relationVo.setExternalId(relation.getExternalId());
		relationVo.setManagedFlagsString(IdentityToIdentityRelationManagedFlag.toString(relation.getManagedFlags()));
		relationVo.setRelationRoleKey(relation.getRole().getKey());
		relationVo.setRelationRole(relation.getRole().getRole());
		return relationVo;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public Long getIdentitySourceKey() {
		return identitySourceKey;
	}

	public void setIdentitySourceKey(Long identitySourceKey) {
		this.identitySourceKey = identitySourceKey;
	}

	public Long getIdentityTargetKey() {
		return identityTargetKey;
	}

	public void setIdentityTargetKey(Long identityTargetKey) {
		this.identityTargetKey = identityTargetKey;
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

	public Long getRelationRoleKey() {
		return relationRoleKey;
	}

	public void setRelationRoleKey(Long relationRoleKey) {
		this.relationRoleKey = relationRoleKey;
	}

	public String getRelationRole() {
		return relationRole;
	}

	public void setRelationRole(String relationRole) {
		this.relationRole = relationRole;
	}
}
