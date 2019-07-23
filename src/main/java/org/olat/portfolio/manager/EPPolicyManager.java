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
package org.olat.portfolio.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.Invitation;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.portfolio.model.structel.EPMapShort;
import org.olat.portfolio.model.structel.EPStructureElement;
import org.olat.portfolio.model.structel.EPStructureElementToGroupRelation;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.portfolio.model.structel.PortfolioStructureMapRef;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * manager for all map share and policy handling
 * 
 * <P>
 * Initial Date:  30.11.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
@Service("epPolicyManager")
public class EPPolicyManager {

	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private InvitationDAO invitationDao;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	
	public List<Identity> getOwners(PortfolioStructureMapRef map) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ident from ").append(EPMapShort.class.getName()).append(" as map")
		  .append(" inner join map.groups as relGroup on relGroup.defaultGroup=true")
		  .append(" inner join relGroup.group as baseGroup ")
		  .append(" inner join baseGroup.members as members on members.role='").append(GroupRoles.owner.name()).append("'")
		  .append(" inner join members.identity as ident")
		  .append(" where map.key=:mapKey");	
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("mapKey", map.getKey()).getResultList();
	}
	
	public boolean isMapShared(OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(relGroup) from ").append(EPMapShort.class.getName()).append(" as map")
		  .append(" inner join map.groups as relGroup on relGroup.defaultGroup=false")
		  .append(" where map.olatResource=:resource");	
		
		Number count = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("resource", resource)
				.getSingleResult();
		return count == null ? false : count.intValue() > 0;
	}
	
	/**
	 * Return a list of wrapper containing the read policies of the map
	 * @param map
	 */
	public List<EPMapPolicy> getMapPolicies(PortfolioStructureMapRef mapRef) {
		EPMapShort map = dbInstance.getCurrentEntityManager().find(EPMapShort.class, mapRef.getKey());
		
		List<EPMapPolicy> policies = new ArrayList<>();
		Set<EPStructureElementToGroupRelation> relations = map.getGroups();
		for(EPStructureElementToGroupRelation relation:relations) {
			if(relation.isDefaultGroup()) {
				continue;
			}
			
			EPMapPolicy policy = getEquivalentWrapper(relation, policies);
			if(policy == null) {
				policy = new EPMapPolicy();
				policy.setTo(relation.getValidTo());
				policy.setFrom(relation.getValidFrom());
				policies.add(policy);
			}

			String role = relation.getRole();
			if(role.startsWith(EPMapPolicy.Type.user.name())) {
				List<Identity> identities = groupDao.getMembers(relation.getGroup(), GroupRoles.participant.name());

				policy.addRelation(relation);
				policy.setType(EPMapPolicy.Type.user);
				policy.addIdentities(identities);
			} else if (role.startsWith(EPMapPolicy.Type.group.name())) {
				policy.addRelation(relation);
				BusinessGroup group = businessGroupDao.findBusinessGroup(relation.getGroup());
				policy.addGroup(group);
				policy.setType(EPMapPolicy.Type.group);
			} else if (role.startsWith(EPMapPolicy.Type.invitation.name())) {
				policy.addRelation(relation);
				Invitation invitation = invitationDao.findInvitation(relation.getGroup());
				policy.setInvitation(invitation);
				policy.setType(EPMapPolicy.Type.invitation);
			} else if (role.startsWith(EPMapPolicy.Type.allusers.name())) {
				policy.addRelation(relation);
				policy.setType(EPMapPolicy.Type.allusers);
			}
		}
		
		return policies;
	}
	
	private EPMapPolicy getEquivalentWrapper(EPStructureElementToGroupRelation relation, List<EPMapPolicy> policies) {
		Date to = relation.getValidTo();
		Date from = relation.getValidFrom();
		String role = relation.getRole();
		
		a_a:
		for(EPMapPolicy policy:policies) {
			for(EPStructureElementToGroupRelation p:policy.getRelations()) {
				if(!role.equals(p.getRole())) {
					continue a_a;
				}
				if(from == null && p.getValidFrom() == null || (from != null && p.getValidFrom() != null && from.equals(p.getValidFrom()))) {	
					if(to == null && p.getValidTo() == null || (to != null && p.getValidTo() != null && to.equals(p.getValidTo()))) {
						return policy;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Update the map policies of a map. The missing policies are deleted!
	 * @param map
	 * @param policies
	 */
	public PortfolioStructureMap updateMapPolicies(PortfolioStructureMap map, List<EPMapPolicy> policies) {
		map = dbInstance.getCurrentEntityManager().merge(map);

		List<EPStructureElementToGroupRelation> savedPolicies = new ArrayList<>();
		for(EPMapPolicy wrapper:policies) {
			savedPolicies.addAll(applyPolicy(wrapper, map));
		}
		
		Collection<EPStructureElementToGroupRelation> currentRelations = new ArrayList<>(map.getGroups());
		for(EPStructureElementToGroupRelation currentRelation:currentRelations) {
			if(currentRelation.isDefaultGroup()) {
				continue;
			}
			
			boolean inUse = savedPolicies.contains(currentRelation);
			if(!inUse) {
				map.getGroups().remove(currentRelation);
			}
		}
		return dbInstance.getCurrentEntityManager().merge(map);
	}
	
	private List<EPStructureElementToGroupRelation> applyPolicy(EPMapPolicy policy, PortfolioStructureMap map) {
		List<EPStructureElementToGroupRelation> savedPolicies = new ArrayList<>();
		switch(policy.getType()) {
			case user:
				savedPolicies.add(applyPolicyToUsers(policy, map));
				break;
			case group:
				for(BusinessGroup group:policy.getGroups()) {
					savedPolicies.add(applyPolicyToGroup(group.getBaseGroup(), policy, map));
				}
				break;
			case invitation:
				Invitation invitation = policy.getInvitation();
				EPStructureElementToGroupRelation invitationPolicy = applyPolicyToInvitation(invitation, policy, map);
				savedPolicies.add(invitationPolicy);
				break;
			case allusers:
				EPStructureElementToGroupRelation allUsersPolicy = applyPolicyToAllUsers(policy, map);
				savedPolicies.add(allUsersPolicy);
				break;
		}
		return savedPolicies;
	}
	
	private EPStructureElementToGroupRelation applyPolicyToAllUsers(EPMapPolicy wrapper, PortfolioStructureMap map) {
		List<EPStructureElementToGroupRelation> currentRelations = wrapper.getRelations();
		if(!currentRelations.isEmpty()) {
			EPStructureElementToGroupRelation currentRelation = currentRelations.get(0);
			updatePolicy(currentRelation, wrapper.getFrom(), wrapper.getTo());
			return currentRelation;
		}
		return createBaseRelation(map, null, EPMapPolicy.Type.allusers.name(), wrapper.getFrom(), wrapper.getTo());
	}
	
	private EPStructureElementToGroupRelation applyPolicyToUsers(EPMapPolicy policy, PortfolioStructureMap map) {
		List<EPStructureElementToGroupRelation> currentRelations = policy.getRelations();
		EPStructureElementToGroupRelation relation = (currentRelations == null || currentRelations.isEmpty()) ? null : currentRelations.get(0);
		if(relation == null) {
			Group secGroup = groupDao.createGroup();
			relation = createBaseRelation(map, secGroup, EPMapPolicy.Type.user.name(), policy.getFrom(), policy.getTo());
			for(Identity identity:policy.getIdentities()) {
				groupDao.addMembershipTwoWay(secGroup, identity, GroupRoles.participant.name());
			}
		} else {
			EPStructureElementToGroupRelation currentPolicy = reusePolicyInSession(relation, map);
			updatePolicy(currentPolicy, policy.getFrom(), policy.getTo());
			
			Group secGroup = relation.getGroup();
			List<Identity> currentMembers = groupDao.getMembers(secGroup, GroupRoles.participant.name());
			List<Identity> newMembers = new ArrayList<>(policy.getIdentities());
			for (Identity newMember:policy.getIdentities()) {
				if(currentMembers.contains(newMember)) {
					newMembers.remove(newMember);
					currentMembers.remove(newMember);
				}
			}
			
			//re-attach the session to lazy load the members
			secGroup = dbInstance.getCurrentEntityManager().merge(secGroup);
			
			for(Identity currentMember:currentMembers) {
				groupDao.removeMembership(secGroup, currentMember);
			}
			for(Identity newMember:newMembers) {
				groupDao.addMembershipTwoWay(secGroup, newMember, GroupRoles.participant.name());
			}
		}
		return relation;
	}

	private EPStructureElementToGroupRelation applyPolicyToInvitation(Invitation invitation, EPMapPolicy policy, PortfolioStructureMap map) {
		invitation = dbInstance.getCurrentEntityManager().merge(invitation);
		Group secGroup = invitation.getBaseGroup();
		Collection<EPStructureElementToGroupRelation> currentRelations = map.getGroups();
		for(EPStructureElementToGroupRelation currentRelation:currentRelations) {
			if(secGroup.equals(currentRelation.getGroup())) {
				updatePolicy(currentRelation, policy.getFrom(), policy.getTo());
				return currentRelation;
			}
		}
		
		return createBaseRelation(map, secGroup, EPMapPolicy.Type.invitation.name(), policy.getFrom(), policy.getTo());
	}
	
	/**
	 * Hibernate doesn't allow to update an object if the same object is already in the current
	 * hibernate session.
	 * @param policy
	 * @param currentPolicies
	 * @return
	 */
	private EPStructureElementToGroupRelation reusePolicyInSession(EPStructureElementToGroupRelation relation, PortfolioStructureMap map) {
		Collection<EPStructureElementToGroupRelation> currentRelations = map.getGroups();
		for(EPStructureElementToGroupRelation currentRelation:currentRelations) {
			if(relation.equalsByPersistableKey(currentRelation)) {
				return currentRelation;
			}
		}
		return relation;
	}
	
	private EPStructureElementToGroupRelation applyPolicyToGroup(Group group, EPMapPolicy policy, PortfolioStructureMap map) {
		Collection<EPStructureElementToGroupRelation> currentRelations = map.getGroups();
		for(EPStructureElementToGroupRelation currentRelation:currentRelations) {
			if(currentRelation.getGroup() != null && currentRelation.getGroup().equals(group)) {
				updatePolicy(currentRelation, policy.getFrom(), policy.getTo());
				return currentRelation;
			}
		}
		return createBaseRelation(map, group, EPMapPolicy.Type.group.name(), policy.getFrom(), policy.getTo());
	}
	
	private void updatePolicy(EPStructureElementToGroupRelation relation, Date from, Date to) {
		relation.setValidFrom(from);
		relation.setValidTo(to);
	}
	
	private EPStructureElementToGroupRelation createBaseRelation(PortfolioStructureMap map, Group group, String role, Date from, Date to) {
		//create security group
		EPStructureElementToGroupRelation relation = new EPStructureElementToGroupRelation();
		relation.setDefaultGroup(false);
		relation.setCreationDate(new Date());
		relation.setGroup(group);
		relation.setStructureElement((EPStructureElement)map);
		relation.setRole(role);
		relation.setValidFrom(from);
		relation.setValidTo(to);
		map.getGroups().add(relation);
		return relation;
	}
}