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
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.IdentityToIdentityRelation;
import org.olat.basesecurity.IdentityToIdentityRelationManagedFlag;
import org.olat.basesecurity.RelationRight;
import org.olat.basesecurity.RelationRightProvider;
import org.olat.basesecurity.RelationRole;
import org.olat.basesecurity.RelationRoleManagedFlag;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 29 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class IdentityRelationshipServiceImpl implements IdentityRelationshipService {
	
	@Autowired
	private RelationRoleDAO relationRoleDao;
	@Autowired
	private RelationRightDAO relationRightDao;
	@Autowired
	private IdentityToIdentityRelationDAO identityRelationshipDao;
	
	@Autowired
	private List<RelationRightProvider> relationRightProviders;
	
	@PostConstruct
	void ensureRightsExists() {
		for (RelationRightProvider relationRightProvider : relationRightProviders) {
			relationRightDao.ensureRightExists(relationRightProvider.getRight());
		}
	}
	
	@Override
	public RelationRole createRole(String role, List<RelationRight> rights) {
		RelationRole relationRole = relationRoleDao.createRelationRole(role, null, null, null);
		if(rights != null && !rights.isEmpty()) {
			relationRoleDao.setRights(relationRole, rights);
		}
		return relationRole;
	}

	@Override
	public RelationRole createRole(String role, String externalId, String externalRef,
			RelationRoleManagedFlag[] managedFlags, List<RelationRight> rights) {
		RelationRole relationRole = relationRoleDao.createRelationRole(role, externalId, externalRef, managedFlags);
		if(rights != null && !rights.isEmpty()) {
			relationRoleDao.setRights(relationRole, rights);
		}
		return relationRole;
	}

	@Override
	public RelationRole updateRole(RelationRole relationRole, List<RelationRight> rights) {
		if(rights == null) {
			rights = new ArrayList<>();
		}
		return relationRoleDao.setRights(relationRole, rights);
	}
	
	@Override
	public RelationRole getRole(Long key) {
		return relationRoleDao.loadRelationRoleByKey(key);
	}

	@Override
	public List<RelationRole> getAvailableRoles() {
		return relationRoleDao.loadRelationRoles();
	}

	@Override
	public List<RelationRight> getAvailableRights() {
		return relationRightDao.loadRelationRights();
	}

	@Override
	public String getTranslatedName(RelationRight right, Locale locale) {
		RelationRightProvider provider = getRelationRightProvider(right);
		return provider != null? provider.getTranslatedName(locale): "???";
	}

	private RelationRightProvider getRelationRightProvider(RelationRight right) {
		for (RelationRightProvider provider : relationRightProviders) {
			if (provider.getRight().equals(right.getRight())) {
				return provider;
			}
		}
		return null;
	}

	@Override
	public boolean isInUse(RelationRole relationRole) {
		return identityRelationshipDao.isUsed(relationRole);
	}

	@Override
	public void deleteRole(RelationRole role) {
		RelationRole reloadedRole = relationRoleDao.loadRelationRoleByKey(role.getKey());
		if(reloadedRole != null) {
			relationRoleDao.delete(reloadedRole);
		}
	}
	
	@Override
	public IdentityToIdentityRelation addRelation(Identity source, Identity target, RelationRole relationRole, String externalId,
			IdentityToIdentityRelationManagedFlag[] managedFlags) {
		return identityRelationshipDao.createRelation(source, target, relationRole,
				externalId, IdentityToIdentityRelationManagedFlag.toString(managedFlags));
	}

	@Override
	public void addRelations(Identity source, Identity target, List<RelationRole> relationRoles) {
		for(RelationRole relationRole:relationRoles) {
			if(!identityRelationshipDao.hasRelation(source, target, relationRole)) {
				identityRelationshipDao.createRelation(source, target, relationRole, null, null);
			}
		}
	}

	@Override
	public List<IdentityToIdentityRelation> getRelationsAsSource(IdentityRef asSource) {
		return identityRelationshipDao.getRelationsAsSource(asSource);
	}

	@Override
	public List<IdentityToIdentityRelation> getRelationsAsTarget(IdentityRef asTarget) {
		return identityRelationshipDao.getRelationsAsTarget(asTarget);
	}

	@Override
	public IdentityToIdentityRelation getRelation(Long relationKey) {
		return identityRelationshipDao.getRelation(relationKey);
	}

	@Override
	public void deleteRelation(IdentityToIdentityRelation relation) {
		identityRelationshipDao.removeRelation(relation);
	}

	@Override
	public void removeRelation(IdentityRef source, IdentityRef target, RelationRole relationRole) {
		IdentityToIdentityRelation relation = identityRelationshipDao.getRelation(source, target, relationRole);
		if(relation != null) {
			identityRelationshipDao.removeRelation(relation);
		}
	}
}
