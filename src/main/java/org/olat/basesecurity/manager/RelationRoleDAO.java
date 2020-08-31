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
package org.olat.basesecurity.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.RelationRight;
import org.olat.basesecurity.RelationRole;
import org.olat.basesecurity.RelationRoleManagedFlag;
import org.olat.basesecurity.RelationRoleToRight;
import org.olat.basesecurity.model.RelationRoleImpl;
import org.olat.basesecurity.model.RelationRoleToRightImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RelationRoleDAO {
	
	@Autowired
	private DB dbInstance;
	
	public RelationRole createRelationRole(String role, String externalId, String externalRef, RelationRoleManagedFlag[] flags) {
		RelationRoleImpl relationRole = new RelationRoleImpl();
		relationRole.setCreationDate(new Date());
		relationRole.setLastModified(relationRole.getCreationDate());
		relationRole.setRole(role);
		relationRole.setExternalRef(externalRef);
		relationRole.setExternalId(externalId);
		relationRole.setManagedFlags(flags);
		dbInstance.getCurrentEntityManager().persist(relationRole);
		return relationRole;
	}
	
	public RelationRole update(RelationRole relationRole) {
		return dbInstance.getCurrentEntityManager().merge(relationRole);
	}
	
	public RelationRole loadRelationRoleByKey(Long key) {
		List<RelationRole> roles = dbInstance.getCurrentEntityManager()
			.createNamedQuery("loadRelationRoleByKey", RelationRole.class)
			.setParameter("roleKey", key)
			.getResultList();
		return roles.isEmpty() ? null : roles.get(0);
	}
	
	public RelationRole loadRelationRoleByRole(String role) {
		List<RelationRole> roles = dbInstance.getCurrentEntityManager()
			.createNamedQuery("loadRelationRoleByRole", RelationRole.class)
			.setParameter("role", role)
			.getResultList();
		return roles.isEmpty() ? null : roles.get(0);
	}

	public List<RelationRole> loadRelationRolesByRight(String right) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rr.role");
		sb.append("  from relationroletoright as rr");
		sb.and().append(" rr.right.right = :right");
		
		List<RelationRole> roles = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RelationRole.class)
				.setParameter("right", right)
				.getResultList();
			return roles;
	}
	
	public List<RelationRole> loadRelationRoles() {
		String q = "select relRole from relationrole as relRole";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, RelationRole.class)
				.getResultList();
	}
	
	public RelationRole setRights(RelationRole role, List<RelationRight> rights) {
		RelationRoleImpl relationRole = (RelationRoleImpl)role;
		
		List<RelationRoleToRight> currentRoleToRights = new ArrayList<>(relationRole.getRights());
		List<RelationRight> currentRights = currentRoleToRights
				.stream().map(RelationRoleToRight::getRelationRight).collect(Collectors.toList());
		List<RelationRight> rightsToAdd = rights.stream()
				.filter(r -> !currentRights.contains(r)).collect(Collectors.toList());

		for(RelationRight rightToAdd:rightsToAdd) {
			RelationRoleToRightImpl roleToRight = new RelationRoleToRightImpl();
			roleToRight.setCreationDate(new Date());
			roleToRight.setRole(relationRole);
			roleToRight.setRight(rightToAdd);
			dbInstance.getCurrentEntityManager().persist(roleToRight);
			relationRole.getRights().add(roleToRight);
		}
		
		for(RelationRoleToRight currentRoleToRight:currentRoleToRights) {
			if(!rights.contains(currentRoleToRight.getRelationRight())) {
				relationRole.getRights().remove(currentRoleToRight);
				//dbInstance.getCurrentEntityManager().remove(currentRoleToRight);
			}
		}
		
		relationRole = dbInstance.getCurrentEntityManager().merge(relationRole);
		dbInstance.commit();
		
		//relationRole.setL
		return relationRole;
	}
	
	public RelationRole addRight(RelationRole role, RelationRight right) {
		RelationRoleImpl relationRole = (RelationRoleImpl)role;
		for(RelationRoleToRight roleToRight:relationRole.getRights()) {
			if(roleToRight.getRelationRight().equals(right)) {
				return relationRole; // already added
			}
		}
		
		RelationRoleToRightImpl roleToRight = new RelationRoleToRightImpl();
		roleToRight.setCreationDate(new Date());
		roleToRight.setRole(relationRole);
		roleToRight.setRight(right);
		dbInstance.getCurrentEntityManager().persist(roleToRight);
		relationRole.getRights().add(roleToRight);
		//relationRole.setL
		dbInstance.getCurrentEntityManager().merge(relationRole);
		return relationRole;
	}
	
	public void delete(RelationRole role) {
		String del = "delete from relationroletoright where role.key=:roleKey";
		dbInstance.getCurrentEntityManager().createQuery(del)
			.setParameter("roleKey", role.getKey())
			.executeUpdate();
		dbInstance.getCurrentEntityManager().remove(role);
		dbInstance.commit();
	}
}
