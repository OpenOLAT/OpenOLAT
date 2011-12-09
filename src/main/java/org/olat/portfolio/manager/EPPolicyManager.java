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
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.Invitation;
import org.olat.basesecurity.Policy;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.id.Identity;
import org.olat.core.manager.BasicManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.resource.OLATResource;

/**
 * Description:<br>
 * manager for all map share and policy handling
 * 
 * <P>
 * Initial Date:  30.11.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPPolicyManager extends BasicManager {

	private final BaseSecurity securityManager;
	private final BusinessGroupManager groupManager;

	public EPPolicyManager(BaseSecurity securityManager, BusinessGroupManager groupManager){
		this.securityManager = securityManager;
		this.groupManager = groupManager;
	}
	
	/**
	 * Return a list of wrapper containing the read policies of the map
	 * @param map
	 */
	public List<EPMapPolicy> getMapPolicies(PortfolioStructureMap map) {
		OLATResource resource = map.getOlatResource();
		List<EPMapPolicy> policyWrappers = new ArrayList<EPMapPolicy>();
		List<Policy> policies = securityManager.getPoliciesOfResource(resource, null);
		for(Policy policy:policies) {
			if(!policy.getPermission().contains(Constants.PERMISSION_READ)) {
				continue;
			}
			
			EPMapPolicy wrapper = getWrapperWithSamePolicy(policy, policyWrappers);
			if(wrapper == null) {
				wrapper = new EPMapPolicy();
				wrapper.setTo(policy.getTo());
				wrapper.setFrom(policy.getFrom());
				policyWrappers.add(wrapper);
			}

			String permission = policy.getPermission();
			SecurityGroup secGroup = policy.getSecurityGroup();
			if(permission.startsWith(EPMapPolicy.Type.user.name())) {
				List<Identity> identities = securityManager.getIdentitiesOfSecurityGroup(secGroup);
				wrapper.addPolicy(policy);
				wrapper.setType(EPMapPolicy.Type.user);
				wrapper.addIdentities(identities);
			} else if (permission.startsWith(EPMapPolicy.Type.group.name())) {
				wrapper.addPolicy(policy);
				BusinessGroup group = groupManager.findBusinessGroup(policy.getSecurityGroup());
				wrapper.addGroup(group);
				wrapper.setType(EPMapPolicy.Type.group);
			} else if (permission.startsWith(EPMapPolicy.Type.invitation.name())) {
				wrapper.addPolicy(policy);
				Invitation invitation = securityManager.findInvitation(policy.getSecurityGroup());
				wrapper.setInvitation(invitation);
				wrapper.setType(EPMapPolicy.Type.invitation);
			} else if (permission.startsWith(EPMapPolicy.Type.allusers.name())) {
				wrapper.addPolicy(policy);
				wrapper.setType(EPMapPolicy.Type.allusers);
			}
		}
		
		return policyWrappers;
	}
	
	private EPMapPolicy getWrapperWithSamePolicy(Policy policy, List<EPMapPolicy> policyWrappers) {
		Date to = policy.getTo();
		Date from = policy.getFrom();
		String permission = policy.getPermission();
		
		a_a:
		for(EPMapPolicy wrapper:policyWrappers) {
			for(Policy p:wrapper.getPolicies()) {
				if(!permission.equals(p.getPermission())) {
					continue a_a;
				}
				if(from == null && p.getFrom() == null || (from != null && p.getFrom() != null && from.equals(p.getFrom()))) {	
					if(to == null && p.getTo() == null || (to != null && p.getTo() != null && to.equals(p.getTo()))) {
						return wrapper;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Update the map policies of a map. The missing policies are deleted!
	 * @param map
	 * @param policyWrappers
	 */
	public void updateMapPolicies(PortfolioStructureMap map, List<EPMapPolicy> policyWrappers) {
		List<Policy> currentPolicies = securityManager.getPoliciesOfResource(map.getOlatResource(), null);
		List<Policy> savedPolicies = new ArrayList<Policy>();
		for(EPMapPolicy wrapper:policyWrappers) {
			savedPolicies.addAll(applyPolicy(wrapper, map, currentPolicies));
		}
		
		for(Policy currentPolicy:currentPolicies) {
			boolean inUse = false;
			for(Policy savedPolicy:savedPolicies) {
				if(currentPolicy.equalsByPersistableKey(savedPolicy)) {
					inUse = true;
					break;
				}
			}
			
			if(!inUse && currentPolicy.getPermission().contains(Constants.PERMISSION_READ)) {
				deletePolicy(currentPolicy);
			}
		}
	}
	
	private void deletePolicy(Policy policy) {
		if(policy.getPermission().contains(Constants.PERMISSION_READ)) {
			String permission = policy.getPermission();
			securityManager.deletePolicy(policy.getSecurityGroup(), permission, policy.getOlatResource());
			if("invitation_read".equals(permission)) {
				Invitation invitation = securityManager.findInvitation(policy.getSecurityGroup());
				securityManager.deleteInvitation(invitation);
			}
		}
	}
	
	private List<Policy> applyPolicy(EPMapPolicy wrapper, PortfolioStructureMap map, List<Policy> currentPolicies) {
		List<Policy> policies = wrapper.getPolicies();
		List<Policy> savedPolicies = new ArrayList<Policy>();
		switch(wrapper.getType()) {
			case user:
				Policy policy = (policies == null || policies.isEmpty()) ? null : policies.get(0);
				if(policy == null) {
					SecurityGroup secGroup = securityManager.createAndPersistSecurityGroup();
					policy = securityManager.createAndPersistPolicy(secGroup, wrapper.getType() + "_" + Constants.PERMISSION_READ, wrapper.getFrom(), wrapper.getTo(), map.getOlatResource());
				} else {
					Policy currentPolicy = reusePolicyInSession(policy, currentPolicies);
					securityManager.updatePolicy(currentPolicy, wrapper.getFrom(), wrapper.getTo());
				}
				SecurityGroup secGroup = policy.getSecurityGroup();
				List<Object[]> allIdents = securityManager.getIdentitiesAndDateOfSecurityGroup(secGroup);
				for (Object[] objects : allIdents) {
					Identity identity = (Identity) objects[0];
					securityManager.removeIdentityFromSecurityGroup(identity, secGroup);
				}
				for(Identity identity:wrapper.getIdentities()) {
					if(!securityManager.isIdentityInSecurityGroup(identity, secGroup)) {
						securityManager.addIdentityToSecurityGroup(identity, secGroup);
					}
				}
				savedPolicies.add(policy);
				break;
			case group:
				for(BusinessGroup group:wrapper.getGroups()) {
					savedPolicies.add(applyPolicyTo(group.getPartipiciantGroup(), wrapper, map));
					savedPolicies.add(applyPolicyTo(group.getOwnerGroup(), wrapper, map));
				}
				break;
			case invitation:
				Invitation invitation = wrapper.getInvitation();
				Policy invitationPolicy = applyPolicyTo(invitation, wrapper, map);
				savedPolicies.add(invitationPolicy);
				break;
			case allusers:
				Policy allUsersPolicy = applyPolicyToAllUsers(wrapper, map);
				savedPolicies.add(allUsersPolicy);
				break;
		}
		return savedPolicies;
	}
	
	private Policy applyPolicyToAllUsers(EPMapPolicy wrapper, PortfolioStructureMap map) {
		SecurityGroup allUsers = securityManager.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
		List<Policy> currentPolicies = securityManager.getPoliciesOfResource(map.getOlatResource(), allUsers);
		if(!currentPolicies.isEmpty()) {
			Policy currentPolicy = currentPolicies.get(0);
			securityManager.updatePolicy(currentPolicy, wrapper.getFrom(), wrapper.getTo());
			return currentPolicy;
		}
		
		Policy policy = securityManager.createAndPersistPolicy(allUsers, wrapper.getType() + "_" + Constants.PERMISSION_READ, wrapper.getFrom(), wrapper.getTo(), map.getOlatResource());
		return policy;
	}
	
	private Policy applyPolicyTo(Invitation invitation, EPMapPolicy wrapper, PortfolioStructureMap map) {
		List<Policy> currentPolicies = securityManager.getPoliciesOfSecurityGroup(invitation.getSecurityGroup());
		for(Policy currentPolicy:currentPolicies) {
			if(currentPolicy.getOlatResource().equalsByPersistableKey(map.getOlatResource())) {
				currentPolicy = reusePolicyInSession(currentPolicy, currentPolicies);
				securityManager.updatePolicy(currentPolicy, wrapper.getFrom(), wrapper.getTo());
				securityManager.updateInvitation(invitation);
				return currentPolicy;
			}
		}
		
		SecurityGroup secGroup = invitation.getSecurityGroup();
		Policy policy = securityManager.createAndPersistPolicy(secGroup, wrapper.getType() + "_" + Constants.PERMISSION_READ, wrapper.getFrom(), wrapper.getTo(), map.getOlatResource());
		securityManager.updateInvitation(invitation);
		return policy;
	}
	
	/**
	 * Hibernate doesn't allow to update an object if the same object is already in the current
	 * hibernate session.
	 * @param policy
	 * @param currentPolicies
	 * @return
	 */
	private Policy reusePolicyInSession(Policy policy, List<Policy> currentPolicies) {
		for(Policy currentPolicy:currentPolicies) {
			if(policy.equalsByPersistableKey(currentPolicy)) {
				return currentPolicy;
			}
		}
		return policy;
	}
	
	private Policy applyPolicyTo(SecurityGroup secGroup, EPMapPolicy wrapper, PortfolioStructureMap map) {
		List<Policy> currentPolicies = securityManager.getPoliciesOfSecurityGroup(secGroup);
		for(Policy currentPolicy:currentPolicies) {
			if(currentPolicy.getOlatResource().equalsByPersistableKey(map.getOlatResource())) {
				currentPolicy = reusePolicyInSession(currentPolicy, currentPolicies);
				securityManager.updatePolicy(currentPolicy, wrapper.getFrom(), wrapper.getTo());
				return currentPolicy;
			}
		}
		
		Policy policy = securityManager.createAndPersistPolicy(secGroup, wrapper.getType() + "_read", wrapper.getFrom(), wrapper.getTo(), map.getOlatResource());
		return policy;
	}
	
}
