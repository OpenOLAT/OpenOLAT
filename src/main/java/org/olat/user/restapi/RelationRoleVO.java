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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.basesecurity.RelationRole;
import org.olat.basesecurity.RelationRoleManagedFlag;
import org.olat.basesecurity.RelationRoleToRight;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 
 * Initial date: 31 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "relationRoleVO")
public class RelationRoleVO {
	
	private Long key;
	private String role;
	private String externalId;
	private String externalRef;
	@Schema(required = true, description = "Action to be performed on managedFlags", allowableValues = { 
			"all",
			 "name(all)",
			 "rights(all)",
		     "delete(all)"})
	private String managedFlags;
	private List<String> rights;
	
	public RelationRoleVO() {
		//
	}
	
	public static RelationRoleVO valueOf(RelationRole role) {
		RelationRoleVO roleVo = new RelationRoleVO();
		roleVo.setKey(role.getKey());
		roleVo.setRole(role.getRole());
		roleVo.setExternalId(role.getExternalId());
		roleVo.setExternalRef(role.getExternalRef());
		roleVo.setManagedFlags(RelationRoleManagedFlag.toString(role.getManagedFlags()));

		List<String> rightNames = new ArrayList<>();
		Set<RelationRoleToRight> roleToRights = role.getRights();
		for( RelationRoleToRight roleToRight:roleToRights) {
			rightNames.add(roleToRight.getRelationRight().getRight());
		}
		roleVo.setRights(rightNames);
		return roleVo;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
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

	public String getManagedFlags() {
		return managedFlags;
	}

	public void setManagedFlags(String managedFlags) {
		this.managedFlags = managedFlags;
	}

	public List<String> getRights() {
		return rights;
	}

	public void setRights(List<String> rights) {
		this.rights = rights;
	}
}
