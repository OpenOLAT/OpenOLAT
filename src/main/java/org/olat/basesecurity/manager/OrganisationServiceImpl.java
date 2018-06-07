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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationManagedFlag;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationType;
import org.olat.basesecurity.OrganisationTypeRef;
import org.olat.basesecurity.model.OrganisationImpl;
import org.olat.basesecurity.model.OrganisationMember;
import org.olat.basesecurity.model.OrganisationNode;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OrganisationServiceImpl implements OrganisationService, InitializingBean {
	
	private static final OLog log = Tracing.createLoggerFor(OrganisationServiceImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private OrganisationDAO organisationDao;
	@Autowired
	private OrganisationTypeDAO organisationTypeDao;
	@Autowired
	private OrganisationTypeToTypeDAO organisationTypeToTypeDao;

	@Override
	public void afterPropertiesSet() throws Exception {
		List<Organisation> defaultOrganisations = organisationDao.loadByIdentifier(DEFAULT_ORGANISATION_IDENTIFIER);
		if(defaultOrganisations.isEmpty()) {
			Organisation organisation = organisationDao.create("OpenOLAT", DEFAULT_ORGANISATION_IDENTIFIER, null, null, null);
			organisation.setManagedFlags(new OrganisationManagedFlag[] {
					OrganisationManagedFlag.identifier, OrganisationManagedFlag.externalId,
					OrganisationManagedFlag.move, OrganisationManagedFlag.delete
				});
			organisationDao.update(organisation);
			dbInstance.commitAndCloseSession();
		}
	}

	@Override
	public Organisation createOrganisation(String displayName, String identifier, String description, Organisation parentOrganisation, OrganisationType type) {
		Organisation organisation = organisationDao.createAndPersistOrganisation(displayName, identifier, description, parentOrganisation, type);
		if(parentOrganisation != null) {
			Group organisationGroup = organisation.getGroup();
			List<GroupMembership> memberships = groupDao.getMemberships(parentOrganisation.getGroup());
			for(GroupMembership membership:memberships) {
				if(membership.getInheritanceMode() == GroupMembershipInheritance.inherited
						|| membership.getInheritanceMode() == GroupMembershipInheritance.root) {
					groupDao.addMembershipOneWay(organisationGroup, membership.getIdentity(), membership.getRole(), GroupMembershipInheritance.inherited);
				}
			}
		}
		return organisation;
	}

	@Override
	public Organisation getOrganisation(OrganisationRef organisation) {
		return organisationDao.loadByKey(organisation.getKey());
	}

	@Override
	public List<Organisation> getOrganisationParentLine(Organisation organisation) {
		return organisationDao.getParentLine(organisation);
	}

	@Override
	public Organisation updateOrganisation(Organisation organisation) {
		return organisationDao.update(organisation);
	}

	@Override
	public void moveOrganisation(OrganisationRef organisationToMove, OrganisationRef newParentRef) {
		OrganisationImpl toMove = (OrganisationImpl)organisationDao.loadByKey(organisationToMove.getKey());
		Organisation newParent = organisationDao.loadByKey(newParentRef.getKey());
		if(newParent == null || newParent.equals(toMove.getParent())) {
			return;// nothing to do
		}

		OrganisationNode treeToMove = organisationDao.getDescendantTree(toMove);
		// clean inheritance of memberships
		cleanMembership(treeToMove, new HashSet<>()); 

		String keysPath = toMove.getMaterializedPathKeys();
		
		toMove.setParent(newParent);
		toMove.setLastModified(new Date());
		String newKeysPath = organisationDao.getMaterializedPathKeys(newParent, toMove);
		toMove.setMaterializedPathKeys(newKeysPath);
		dbInstance.getCurrentEntityManager().merge(toMove);
		
		List<Organisation> descendants = new ArrayList<>();
		treeToMove.visit(node -> {
			if(node != treeToMove) {
				descendants.add(node.getOrganisation());
			}
		});
		

		for(Organisation descendant:descendants) {
			String descendantKeysPath = descendant.getMaterializedPathKeys();
			if(descendantKeysPath.indexOf(keysPath) == 0) {
				String end = descendantKeysPath.substring(keysPath.length(), descendantKeysPath.length());
				String updatedPath = newKeysPath + end;
				((OrganisationImpl)descendant).setMaterializedPathKeys(updatedPath);
			}
			dbInstance.getCurrentEntityManager().merge(descendant);
		}
		
		// propagate inheritance of the new parent
		List<GroupMembership> memberships = groupDao.getMemberships(newParent.getGroup());
		List<GroupMembership> membershipsToPropagate = new ArrayList<>();
		for(GroupMembership membership:memberships) {
			if(membership.getInheritanceMode() == GroupMembershipInheritance.inherited || membership.getInheritanceMode() == GroupMembershipInheritance.root) {
				membershipsToPropagate.add(membership);
			}
		}
		
		if(!membershipsToPropagate.isEmpty()) {
			propagateMembership(treeToMove, membershipsToPropagate);
		}
		
		dbInstance.commit();
	}
	
	private void propagateMembership(OrganisationNode node, List<GroupMembership> membershipsToPropagate) {
		Group group = node.getOrganisation().getGroup();
		List<GroupMembership> nodeMemberships = groupDao.getMemberships(group);
		Map<IdentityRoleKey,GroupMembership> identityRoleToMembership = new HashMap<>();
		for(GroupMembership nodeMembership:nodeMemberships) {
			identityRoleToMembership.put(new IdentityRoleKey(nodeMembership), nodeMembership);
		}

		for(GroupMembership membershipToPropagate:membershipsToPropagate) {
			GroupMembership nodeMembership = identityRoleToMembership.get(new IdentityRoleKey(membershipToPropagate));
			if(nodeMembership == null) {
				groupDao.addMembershipOneWay(group, membershipToPropagate.getIdentity(), membershipToPropagate.getRole(), GroupMembershipInheritance.inherited);
			} else if(nodeMembership.getInheritanceMode() != GroupMembershipInheritance.inherited)  {
				groupDao.updateInheritanceMode(nodeMembership, GroupMembershipInheritance.inherited);
			}
		}

		List<OrganisationNode> children = node.getChildrenNode();
		if(children != null && !children.isEmpty()) {
			for(OrganisationNode child:children) {
				propagateMembership(child, membershipsToPropagate);
			}
		}
	}
	
	private void cleanMembership(OrganisationNode node, Set<IdentityRoleKey> inheritance) {
		List<GroupMembership> memberships = groupDao.getMemberships(node.getOrganisation().getGroup());
		for(GroupMembership membership:memberships) {
			if(membership.getInheritanceMode() == GroupMembershipInheritance.inherited) {
				if(!inheritance.contains(new IdentityRoleKey(membership))) {
					groupDao.removeMembership(node.getOrganisation().getGroup(), membership.getIdentity(), membership.getRole());
				}
			} else if(membership.getInheritanceMode() == GroupMembershipInheritance.root) {
				inheritance.add(new IdentityRoleKey(membership));
			}
		}
		
		List<OrganisationNode> children = node.getChildrenNode();
		if(children != null && !children.isEmpty()) {
			for(OrganisationNode child:children) {
				cleanMembership(child, new HashSet<>(inheritance));
			}
		}
	}
	
	private static class IdentityRoleKey {
		
		private final Long identityKey;
		private final String role;
		
		public IdentityRoleKey(GroupMembership membership) {
			identityKey = membership.getIdentity().getKey();
			role = membership.getRole();
		}

		@Override
		public int hashCode() {
			return identityKey.hashCode() + role.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof IdentityRoleKey) {
				IdentityRoleKey m = (IdentityRoleKey)obj;
				return identityKey.equals(m.identityKey) && role.equals(m.role);
			}
			return false;
		}
	}

	@Override
	public Organisation getDefaultOrganisation() {
		List<Organisation> defOrganisations = organisationDao.loadDefaultOrganisation();
		if(defOrganisations.size() == 1) {
			return defOrganisations.get(0);
		}
		if(defOrganisations.size() > 1) {
			log.error("You have more than one default organisation");
			return defOrganisations.get(0);
		}
		log.error("You don't have a defualt organisation");
		return null;
	}

	@Override
	public List<Organisation> getOrganisations() {
		return organisationDao.find();
	}

	@Override
	public OrganisationType createOrganisationType(String displayName, String identifier, String description) {
		return organisationTypeDao.createAndPersist(displayName, identifier, description);
	}

	@Override
	public OrganisationType getOrganisationType(OrganisationTypeRef type) {
		return organisationTypeDao.loadByKey(type.getKey());
	}
	
	@Override
	public OrganisationType updateOrganisationType(OrganisationType type) {
		return organisationTypeDao.updateOrganisationType(type);
	}

	@Override
	public OrganisationType updateOrganisationType(OrganisationType type, List<OrganisationType> allowedSubTypes) {
		organisationTypeToTypeDao.setAllowedSubType(type, allowedSubTypes);
		return organisationTypeDao.updateOrganisationType(type);
	}

	@Override
	public void allowOrganisationSubType(OrganisationType parentType, OrganisationType allowedSubType) {
		organisationTypeToTypeDao.addAllowedSubType(parentType, allowedSubType);
	}

	@Override
	public void disallowOrganisationSubType(OrganisationType parentType, OrganisationType disallowedSubType) {
		organisationTypeToTypeDao.disallowedSubType(parentType, disallowedSubType);
	}

	@Override
	public List<OrganisationType> getOrganisationTypes() {
		return organisationTypeDao.load();
	}

	@Override
	public List<Organisation> getOrganisations(IdentityRef member, OrganisationRoles... role) {
		List<String> roleList = new ArrayList<>(role.length);
		for(OrganisationRoles r:role) {
			roleList.add(r.name());
		}
		return organisationDao.getOrganisations(member, roleList);
	}

	@Override
	public List<Organisation> getOrganisations(IdentityRef member, Roles roles, OrganisationRoles... organisationRoles) {
		if(organisationRoles == null || organisationRoles.length == 0 || organisationRoles[0] == null) {
			return Collections.emptyList();
		}
		
		if(roles.isOLATAdmin()) {
			return organisationDao.find();
		}
		
		Set<OrganisationRef> organisations = new HashSet<>();
		for(OrganisationRoles organisationRole:organisationRoles) {
			if(organisationRole != null) {
				organisations.addAll(roles.getOrganisationsWithRole(organisationRole));
			}
		}
		return organisationDao.getOrganisations(organisations);
	}

	@Override
	public List<OrganisationMember> getMembers(Organisation organisation) {
		return organisationDao.getMembers(organisation);
	}

	@Override
	public List<Identity> getMembersIdentity(Organisation organisation, OrganisationRoles role) {
		return organisationDao.getMembersIdentity(organisation, role.name());
	}

	@Override
	public void setAsGuest(Identity identity) {
		OrganisationImpl defOrganisation = (OrganisationImpl)getDefaultOrganisation();
		if(!groupDao.hasRole(defOrganisation.getGroup(), identity, OrganisationRoles.guest.name())) {
			groupDao.removeMemberships(identity);
			addMember(defOrganisation, identity, OrganisationRoles.guest, GroupMembershipInheritance.none);
		}
	}

	@Override
	public void addMember(Identity member, OrganisationRoles role) {
		Organisation defOrganisation = getDefaultOrganisation();
		addMember(defOrganisation, member, role);
	}

	@Override
	public void removeMember(IdentityRef member, OrganisationRoles role) {
		Organisation defOrganisation = getDefaultOrganisation();
		removeMember(defOrganisation, member, role);
	}
	

	@Override
	public void addMember(Organisation organisation, Identity member, OrganisationRoles role) {
		GroupMembershipInheritance inheritanceMode;
		if(role == OrganisationRoles.usermanager || role == OrganisationRoles.learnresourcemanager || role == OrganisationRoles.author) {
			inheritanceMode = GroupMembershipInheritance.root;
		} else {
			inheritanceMode = GroupMembershipInheritance.none;
		}
		addMember(organisation, member, role, inheritanceMode);
	}

	@Override
	public void addMember(Organisation organisation, Identity member, OrganisationRoles role, GroupMembershipInheritance inheritanceMode) {
		if(inheritanceMode == GroupMembershipInheritance.inherited) {
			throw new AssertException("Inherited are automatic");
		}
		
		OrganisationImpl org = (OrganisationImpl)organisation;
		GroupMembership membership = groupDao.getMembership(org.getGroup(), member, role.name());
		if(membership == null) {
			groupDao.addMembershipOneWay(org.getGroup(), member, role.name(), inheritanceMode);
		} else if(membership.getInheritanceMode() != inheritanceMode) {
			groupDao.updateInheritanceMode(membership, inheritanceMode);
		}

		if(inheritanceMode == GroupMembershipInheritance.root) {
			List<Organisation> descendants = organisationDao.getDescendants(organisation);
			for(Organisation descendant:descendants) {
				OrganisationImpl orgDescendant = (OrganisationImpl)descendant;
				GroupMembership inheritedMembership = groupDao.getMembership(orgDescendant.getGroup(), member, role.name());
				if(inheritedMembership == null) {
					groupDao.addMembershipOneWay(orgDescendant.getGroup(), member, role.name(), GroupMembershipInheritance.inherited);
				} else if(inheritedMembership.getInheritanceMode() == GroupMembershipInheritance.none) {
					groupDao.updateInheritanceMode(inheritedMembership, GroupMembershipInheritance.inherited);
				}
			}
		}
	}
	
	@Override
	public void removeMember(Organisation organisation, IdentityRef member) {
		List<GroupMembership> memberships = groupDao.getMemberships(organisation.getGroup(), member);
		
		OrganisationNode organisationTree = null;
		
		for(GroupMembership membership:memberships) {
			if(membership.getInheritanceMode() == GroupMembershipInheritance.none) {
				groupDao.removeMembership(membership);
			} else if(membership.getInheritanceMode() == GroupMembershipInheritance.root) {
				String role = membership.getRole();
				groupDao.removeMembership(membership);
				
				if(organisationTree == null) {
					organisationTree = organisationDao.getDescendantTree(organisation);
				}
				for(OrganisationNode child:organisationTree.getChildrenNode()) {
					removeInherithedMembership(child, member, role);
				}
			}
		}
	}
	
	/**
	 * The method will recursively delete the inherithed membership. If it
	 * found a mebership marked as "root" or "none". It will stop.
	 * 
	 * @param organisationNode The organization node
	 * @param member The user to remove
	 * @param role The role
	 */
	private void removeInherithedMembership(OrganisationNode organisationNode, IdentityRef member, String role) {
		GroupMembership membership = groupDao
				.getMembership(organisationNode.getOrganisation().getGroup(), member, role);
		if(membership != null && membership.getInheritanceMode() == GroupMembershipInheritance.inherited) {
			groupDao.removeMembership(membership);
			for(OrganisationNode child:organisationNode.getChildrenNode()) {
				removeInherithedMembership(child, member, role);
			}
		}
	}
	

	@Override
	public void removeMember(Organisation organisation, IdentityRef member, OrganisationRoles role) {
		groupDao.removeMembership(organisation.getGroup(), member, role.name());
	}
	
	@Override
	public boolean hasRole(String organisationIdentifier, IdentityRef identity, OrganisationRoles... roles) {
		List<String> roleList = new ArrayList<>();
		if(roles != null && roles.length > 0 && roles[0] != null) {
			for(int i=0; i<roles.length; i++) {
				if(roles[i] != null) {
					roleList.add(roles[i].name());
				}
			}
		}
		if(roleList.isEmpty()) {
			return false;
		}
		return organisationDao.hasRole(identity, organisationIdentifier, roleList.toArray(new String[roleList.size()]));
	}

	@Override
	public List<Identity> getDefaultsSystemAdministator() {
		return organisationDao.getIdentities(DEFAULT_ORGANISATION_IDENTIFIER, OrganisationRoles.administrator.name());
	}

	@Override
	public boolean hasRole(IdentityRef identity, OrganisationRoles role) {
		return organisationDao.hasRole(identity, null, role.name());
	}

	@Override
	public List<Identity> getIdentitiesWithRole(OrganisationRoles role) {
		return organisationDao.getIdentities(role.name());
	}
	
	
}
