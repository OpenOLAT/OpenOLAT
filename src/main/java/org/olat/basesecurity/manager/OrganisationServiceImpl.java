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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationDataDeletable;
import org.olat.basesecurity.OrganisationManagedFlag;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationStatus;
import org.olat.basesecurity.OrganisationType;
import org.olat.basesecurity.OrganisationTypeRef;
import org.olat.basesecurity.RightProvider;
import org.olat.basesecurity.model.IdentityToRoleKey;
import org.olat.basesecurity.model.OrganisationImpl;
import org.olat.basesecurity.model.OrganisationMember;
import org.olat.basesecurity.model.OrganisationMembershipEvent;
import org.olat.basesecurity.model.OrganisationMembershipStats;
import org.olat.basesecurity.model.OrganisationNode;
import org.olat.basesecurity.model.SearchMemberParameters;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.resource.OresHelper;
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
	
	private static final Logger log = Tracing.createLoggerFor(OrganisationServiceImpl.class);
	
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
	@Autowired
	private OrganisationRoleRightDAO organisationRoleRightDAO;
	@Autowired
	private List<RightProvider> allRights;

	@Override
	public void afterPropertiesSet() throws Exception {
		List<Organisation> defaultOrganisations = organisationDao.loadDefaultOrganisation();
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
			List<GroupMembership> memberships = groupDao.getMemberships(parentOrganisation.getGroup(),
					GroupMembershipInheritance.inherited, GroupMembershipInheritance.root);
			List<OrganisationMembershipEvent> events = new ArrayList<>(memberships.size());
			for(GroupMembership membership:memberships) {
				if(membership.getInheritanceMode() == GroupMembershipInheritance.inherited
						|| membership.getInheritanceMode() == GroupMembershipInheritance.root) {
					groupDao.addMembershipOneWay(organisationGroup, membership.getIdentity(), membership.getRole(), GroupMembershipInheritance.inherited);
					events.add(OrganisationMembershipEvent.identityAdded(organisation, membership.getIdentity()));
				}
			}
			sendDeferredEvents(organisation, events);
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
	public void deleteOrganisation(OrganisationRef organisation, OrganisationRef organisationAlt) {
		// Delete all rights for this organisation
		Organisation org = getOrganisation(organisation);
		for (OrganisationRoles role : OrganisationRoles.values()) {
			setGrantedOrganisationRights(org, role, Collections.emptyList());
		}
		OrganisationImpl reloadedOrganisation = (OrganisationImpl)organisationDao.loadByKey(organisation.getKey());
		if(DEFAULT_ORGANISATION_IDENTIFIER.equals(reloadedOrganisation.getIdentifier())) {
			log.error("Someone try to delete the default organisation");
			return;
		}
		
		List<Organisation> children = organisationDao.getChildren(reloadedOrganisation, OrganisationStatus.values());
		for(Organisation child:children) {
			deleteOrganisation(child, organisationAlt);
		}
		
		//TODO organisation: move memberships to default organisation or a lost+found???
		Group organisationGroup = reloadedOrganisation.getGroup();
		List<GroupMembership> users = groupDao.getMemberships(organisationGroup, OrganisationRoles.user.name());
		List<OrganisationMembershipEvent> events = new ArrayList<>(users.size());
		for(GroupMembership user:users) {
			addMember(user.getIdentity(), OrganisationRoles.user);
			events.add(OrganisationMembershipEvent.identityRemoved(reloadedOrganisation, user.getIdentity()));
		}
		groupDao.removeMemberships(organisationGroup);
		sendDeferredEvents(reloadedOrganisation, events);
		
		Organisation replacementOrganisation = null;
		if(organisationAlt != null) {
			replacementOrganisation = organisationDao.loadByKey(organisationAlt.getKey());
		}
		
		boolean delete = true;
		Map<String,OrganisationDataDeletable> deleteDelegates = CoreSpringFactory.getBeansOfType(OrganisationDataDeletable.class);
		for(OrganisationDataDeletable delegate:deleteDelegates.values()) {
			delete &= delegate.deleteOrganisationData(reloadedOrganisation, replacementOrganisation);
		}
		
		if(delete) {
			organisationDao.delete(reloadedOrganisation);
		} else {
			reloadedOrganisation.setParent(null);
			reloadedOrganisation.setMaterializedPathKeys("");
			reloadedOrganisation.setExternalId(null);
			reloadedOrganisation.setStatus(OrganisationStatus.deleted.name());
			organisationDao.update(reloadedOrganisation);
		}
	}

	@Override
	public void moveOrganisation(OrganisationRef organisationToMove, OrganisationRef newParentRef) {
		// Delete all rights
		Organisation org = getOrganisation(organisationToMove);
		for (OrganisationRoles role : OrganisationRoles.values()) {
			setGrantedOrganisationRights(org, role, Collections.emptyList());
		}

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
		if(newParent.getRoot() == null && newParent.getParent() == null) {
			toMove.setRoot(newParent);
		} else {
			toMove.setRoot(newParent.getRoot());
		}
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
		List<GroupMembership> memberships = groupDao.getMemberships(newParent.getGroup(),
				GroupMembershipInheritance.inherited, GroupMembershipInheritance.root);
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
		Map<IdentityToRoleKey,GroupMembership> identityRoleToMembership = new HashMap<>();
		for(GroupMembership nodeMembership:nodeMemberships) {
			identityRoleToMembership.put(new IdentityToRoleKey(nodeMembership), nodeMembership);
		}

		List<OrganisationMembershipEvent> events = new ArrayList<>();
		for(GroupMembership membershipToPropagate:membershipsToPropagate) {
			GroupMembership nodeMembership = identityRoleToMembership.get(new IdentityToRoleKey(membershipToPropagate));
			if(nodeMembership == null) {
				groupDao.addMembershipOneWay(group, membershipToPropagate.getIdentity(), membershipToPropagate.getRole(), GroupMembershipInheritance.inherited);
				events.add(OrganisationMembershipEvent.identityAdded(node.getOrganisation(), membershipToPropagate.getIdentity()));
			} else if(nodeMembership.getInheritanceMode() != GroupMembershipInheritance.inherited)  {
				groupDao.updateInheritanceMode(nodeMembership, GroupMembershipInheritance.inherited);
			}
		}
		sendDeferredEvents(node.getOrganisation(), events);

		List<OrganisationNode> children = node.getChildrenNode();
		if(children != null && !children.isEmpty()) {
			for(OrganisationNode child:children) {
				propagateMembership(child, membershipsToPropagate);
			}
		}
	}
	
	private void cleanMembership(OrganisationNode node, Set<IdentityToRoleKey> inheritance) {
		List<GroupMembership> memberships = groupDao.getMemberships(node.getOrganisation().getGroup());
		List<OrganisationMembershipEvent> events = new ArrayList<>();
		for(GroupMembership membership:memberships) {
			if(membership.getInheritanceMode() == GroupMembershipInheritance.inherited) {
				if(!inheritance.contains(new IdentityToRoleKey(membership))) {
					groupDao.removeMembership(node.getOrganisation().getGroup(), membership.getIdentity(), membership.getRole());
					events.add(OrganisationMembershipEvent.identityRemoved(node.getOrganisation(), membership.getIdentity()));
				}
			} else if(membership.getInheritanceMode() == GroupMembershipInheritance.root) {
				inheritance.add(new IdentityToRoleKey(membership));
			}
		}
		sendDeferredEvents(node.getOrganisation(), events);
		
		List<OrganisationNode> children = node.getChildrenNode();
		if(children != null && !children.isEmpty()) {
			for(OrganisationNode child:children) {
				cleanMembership(child, new HashSet<>(inheritance));
			}
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
		return organisationDao.find(OrganisationStatus.notDelete());
	}
	
	@Override
	public boolean isMultiOrganisations() {
		return  organisationDao.count(OrganisationStatus.notDelete()) > 1;
	}

	@Override
	public List<Organisation> getOrganisations(OrganisationStatus[] status) {
		return organisationDao.find(status);
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
		return organisationDao.getOrganisations(member, roleList, true);
	}

	@Override
	public List<Organisation> getOrganisationsNotInherited(IdentityRef member, OrganisationRoles... role) {
		List<String> roleList = new ArrayList<>(role.length);
		for(OrganisationRoles r:role) {
			roleList.add(r.name());
		}
		return organisationDao.getOrganisations(member, roleList, false);
	}

	@Override
	public List<Organisation> getOrganisations(IdentityRef member, Roles roles, OrganisationRoles... organisationRoles) {
		if(organisationRoles == null || organisationRoles.length == 0 || organisationRoles[0] == null) {
			return Collections.emptyList();
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
	public List<OrganisationMember> getMembers(Organisation organisation, SearchMemberParameters params) {
		return organisationDao.getMembers(organisation, params);
	}

	@Override
	public List<Identity> getMembersIdentity(Organisation organisation, OrganisationRoles role) {
		if(role == null) {
			return new ArrayList<>();
		}
		return organisationDao.getMembersIdentity(organisation, role.name());
	}

	@Override
	public void setAsGuest(Identity identity) {
		OrganisationImpl defOrganisation = (OrganisationImpl)getDefaultOrganisation();
		if(!groupDao.hasRole(defOrganisation.getGroup(), identity, OrganisationRoles.guest.name())) {
			groupDao.removeMemberships(identity);
			sendDeferredEvent(defOrganisation, OrganisationMembershipEvent.identityRemoved(defOrganisation, identity));
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
		removeMember(defOrganisation, member, role, true);
	}
	

	@Override
	public void addMember(Organisation organisation, Identity member, OrganisationRoles role) {
		GroupMembershipInheritance inheritanceMode;
		if(OrganisationRoles.isInheritedByDefault(role)) {
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
		List<OrganisationMembershipEvent> events = new ArrayList<>();
		if(membership == null) {
			groupDao.addMembershipOneWay(org.getGroup(), member, role.name(), inheritanceMode);
			events.add(OrganisationMembershipEvent.identityAdded(org, member));
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
					events.add(OrganisationMembershipEvent.identityAdded(org, member));
				} else if(inheritedMembership.getInheritanceMode() == GroupMembershipInheritance.none) {
					groupDao.updateInheritanceMode(inheritedMembership, GroupMembershipInheritance.inherited);
				}
			}
		}
		sendDeferredEvents(organisation, events);
	}
	
	@Override
	public void removeMember(Organisation organisation, IdentityRef member) {
		List<GroupMembership> memberships = groupDao.getMemberships(organisation.getGroup(), member);
		
		OrganisationNode organisationTree = null;
		
		for(GroupMembership membership:memberships) {
			if(membership.getInheritanceMode() == GroupMembershipInheritance.none) {
				groupDao.removeMembership(membership);
				sendDeferredEvent(organisation, OrganisationMembershipEvent.identityRemoved(organisation, member));
			} else if(membership.getInheritanceMode() == GroupMembershipInheritance.root) {
				String role = membership.getRole();
				groupDao.removeMembership(membership);
				sendDeferredEvent(organisation, OrganisationMembershipEvent.identityRemoved(organisation, member));
				
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
		Organisation organisation = organisationNode.getOrganisation();
		GroupMembership membership = groupDao.getMembership(organisation.getGroup(), member, role);
		if(membership != null && membership.getInheritanceMode() == GroupMembershipInheritance.inherited) {
			groupDao.removeMembership(membership);
			sendDeferredEvent(organisation, OrganisationMembershipEvent.identityRemoved(organisation, member));
			for(OrganisationNode child:organisationNode.getChildrenNode()) {
				removeInherithedMembership(child, member, role);
			}
		}
	}
	
	@Override
	public boolean removeMember(Organisation organisation, IdentityRef member, OrganisationRoles role, boolean excludeInherited) {
		GroupMembership membership = groupDao.getMembership(organisation.getGroup(), member, role.name());
		if(membership != null && (!excludeInherited || membership.getInheritanceMode() == GroupMembershipInheritance.root
				|| membership.getInheritanceMode() == GroupMembershipInheritance.none)) {
			groupDao.removeMembership(membership);
			sendDeferredEvent(organisation, OrganisationMembershipEvent.identityRemoved(organisation, member));
			if(membership.getInheritanceMode() == GroupMembershipInheritance.root
					|| membership.getInheritanceMode() == GroupMembershipInheritance.inherited) {
				OrganisationNode organisationTree = organisationDao.getDescendantTree(organisation);
				for(OrganisationNode child:organisationTree.getChildrenNode()) {
					removeInherithedMembership(child, member, role.name());
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void moveMembers(OrganisationRef sourceOrganisation, OrganisationRef targetOrganisation,
			List<Identity> identities, List<OrganisationRoles> roles) {
		Organisation sourceOrg = getOrganisation(sourceOrganisation);
		Organisation targetOrg = getOrganisation(targetOrganisation);
		
		for(OrganisationRoles role:roles) {
			List<Identity> currentMembers = organisationDao.getNonInheritedMembersIdentity(sourceOrganisation, role.name());
			Set<Identity> currentMemberSet = new HashSet<>(currentMembers);

			for(Identity identity:identities) {
				if(currentMemberSet.contains(identity)) {
					removeMember(sourceOrg, identity, role, true);
					addMember(targetOrg, identity, role);
					dbInstance.commit();
				}
			}
		}
	}
	
	private void sendDeferredEvent(Organisation organisation, MultiUserEvent event) {
		sendDeferredEvents(organisation, List.of(event));
	}

	private void sendDeferredEvents(Organisation organisation, List<? extends MultiUserEvent> events) {
		EventBus eventBus = CoordinatorManager.getInstance().getCoordinator().getEventBus();
		for(MultiUserEvent event:events) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(Organisation.class, organisation.getKey());
			eventBus.fireEventToListenersOf(event, ores);
		}
	}

	@Override
	public boolean hasRole(String organisationIdentifier, IdentityRef identity, OrganisationRoles... roles) {
		List<String> roleList = OrganisationRoles.toList(roles);
		if(roleList.isEmpty()) {
			return false;
		}
		return organisationDao.hasRole(identity, organisationIdentifier, null, roleList.toArray(new String[roleList.size()]));
	}

	@Override
	public boolean hasRole(IdentityRef identity, OrganisationRef organisation, OrganisationRoles... roles) {
		List<String> roleList = OrganisationRoles.toList(roles);
		if(roleList.isEmpty()) {
			return false;
		}
		return organisationDao.hasRole(identity, null, organisation, roleList.toArray(new String[roleList.size()]));
	}

	@Override
	public List<Identity> getDefaultsSystemAdministator() {
		return organisationDao.getIdentities(DEFAULT_ORGANISATION_IDENTIFIER, OrganisationRoles.administrator.name());
	}

	@Override
	public boolean hasRole(IdentityRef identity, OrganisationRoles role) {
		return organisationDao.hasRole(identity, null, null, role.name());
	}

	@Override
	public List<Identity> getIdentitiesWithRole(OrganisationRoles role) {
		return organisationDao.getIdentities(role.name());
	}
	
	@Override
	public List<Long> getMemberKeys(OrganisationRef organisation, OrganisationRoles... roles) {
		return organisationDao.getMemberKeys(organisation, roles);
	}

	@Override
	public List<OrganisationMembershipStats> getOrganisationStatistics(OrganisationRef organisation,
			List<IdentityRef> identities) {
		return organisationDao.getStatistics(organisation, identities);
	}

	@Override
	public List<RightProvider> getAllOrganisationRights(OrganisationRoles role) {
		return allRights.stream()
				.filter(right -> right.getOrganisationRoles().contains(role))
				.sorted(Comparator.comparing(RightProvider::getOrganisationPosition))
				.collect(Collectors.toList());
	}

	@Override
	public List<RightProvider> getGrantedOrganisationRights(Organisation organisation, OrganisationRoles role) {
		List<String> organisationRights = organisationRoleRightDAO.getGrantedOrganisationRights(organisation.getRoot() != null ? organisation.getRoot() : organisation, role);

		return allRights.stream().filter(right -> organisationRights.contains(right.getRight())).collect(Collectors.toList());
	}

	@Override
	public void setGrantedOrganisationRights(Organisation organisation, OrganisationRoles role, Collection<String> rights) {
		// Get all rights
		List<String> grantedRights = organisationRoleRightDAO.getGrantedOrganisationRights(organisation, role);
		List<String> deleteRights = grantedRights.stream().filter(grantedRight -> !rights.contains(grantedRight)).collect(Collectors.toList());
		List<String> addRights = rights.stream().filter(addRight -> !grantedRights.contains(addRight)).collect(Collectors.toList());

		// Remove rights, which are not granted anymore
		organisationRoleRightDAO.deleteGrantedOrganisationRights(organisation, role, deleteRights);

		// Add rights, which are not granted yet
		for (String right : addRights) {
			organisationRoleRightDAO.createOrganisationRoleRight(organisation, role, right);
		}
	}
}
