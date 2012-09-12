/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.group.right;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Policy;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.id.Identity;
import org.olat.core.manager.BasicManager;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:<BR>
 * 
 * Initial Date: Aug 24, 2004
 * 
 * @author gnaegi
 */
@Service("rightManager")
public class BGRightManagerImpl extends BasicManager implements BGRightManager {

	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDAO;

	/**
	 * @see org.olat.group.right.BGRightManager#addBGRight(java.lang.String,
	 *      org.olat.group.BusinessGroup)
	 */
	@Override
	public void addBGRight(String bgRight, BusinessGroup rightGroup, BGRightsRole roles) {
		List<OLATResource> resources = businessGroupRelationDAO.findResources(Collections.singletonList(rightGroup), 0, -1);
		for(OLATResource resource:resources) {
			if(roles == BGRightsRole.participant) {
				securityManager.createAndPersistPolicy(rightGroup.getPartipiciantGroup(), bgRight, resource);
			} else if(roles == BGRightsRole.tutor) {
				securityManager.createAndPersistPolicy(rightGroup.getOwnerGroup(), bgRight, resource);
			}
		}
	}
	
	@Override
	public void addBGRight(String bgRight, BusinessGroup rightGroup, OLATResource resource, BGRightsRole roles) {
		if(roles == BGRightsRole.participant) {
			securityManager.createAndPersistPolicy(rightGroup.getPartipiciantGroup(), bgRight, resource);
		} else if(roles == BGRightsRole.tutor) {
			securityManager.createAndPersistPolicy(rightGroup.getOwnerGroup(), bgRight, resource);
		}
	}

	/**
	 * @see org.olat.group.right.BGRightManager#removeBGRight(java.lang.String,
	 *      org.olat.group.BusinessGroup)
	 */
	@Override
	public void removeBGRight(String bgRight, BusinessGroup rightGroup, OLATResource resource, BGRightsRole roles) {
		if(roles == BGRightsRole.participant) {
			securityManager.deletePolicy(rightGroup.getPartipiciantGroup(), bgRight, resource);
		} else if (roles == BGRightsRole.tutor) {
			securityManager.deletePolicy(rightGroup.getOwnerGroup(), bgRight, resource);
		}
	}
	
	@Override
	public void removeBGRights(BusinessGroup rightGroup, OLATResource resource, BGRightsRole role) {
		if(role == BGRightsRole.tutor) {
			securityManager.deletePolicies(Collections.singletonList(rightGroup.getOwnerGroup()), Collections.singletonList(resource));
		} else if(role == BGRightsRole.participant) {
			securityManager.deletePolicies(Collections.singletonList(rightGroup.getPartipiciantGroup()), Collections.singletonList(resource));
		}
	}

	@Override
	public void removeBGRights(Collection<BusinessGroup> groups, OLATResource resource) {
		List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>(groups.size() * 2);
		for(BusinessGroup group:groups) {
			secGroups.add(group.getOwnerGroup());
			secGroups.add(group.getPartipiciantGroup());
		}
		securityManager.deletePolicies(secGroups, Collections.singletonList(resource));
	}

	@Override
	public boolean hasBGRight(String bgRight, Identity identity, OLATResource resource) {
		return securityManager.isIdentityPermittedOnResourceable(identity, bgRight, resource);
	}

	/**
	 * @see org.olat.group.right.BGRightManager#findBGRights(org.olat.group.BusinessGroup)
	 */
	@Override
	public List<String> findBGRights(BusinessGroup group, BGRightsRole role) {
		SecurityGroup secGroup = null;
		if(role == BGRightsRole.tutor) {
			secGroup = group.getOwnerGroup();
		} else if(role == BGRightsRole.participant) {
			secGroup = group.getPartipiciantGroup();
		} else {
			return Collections.emptyList();
		}
		
		List<Policy> results = securityManager.getPoliciesOfSecurityGroup(secGroup);
		// filter all business group rights permissions. group right permissions
		// start with bgr.
		List<String> rights = new ArrayList<String>();
		for (Policy rightPolicy:results) {
			String right = rightPolicy.getPermission();
			if (right.indexOf(BG_RIGHT_PREFIX) == 0) rights.add(right);
		}
		return rights;
	}
	
	@Override
	public List<BGRights> findBGRights(List<BusinessGroup> groups, OLATResource resource) {
		Map<SecurityGroup,BusinessGroup> secToGroupMap = new HashMap<SecurityGroup,BusinessGroup>();
		List<SecurityGroup> tutorSecGroups = new ArrayList<SecurityGroup>(groups.size());
		List<SecurityGroup> participantSecGroups = new ArrayList<SecurityGroup>(groups.size());
		for(BusinessGroup group:groups) {
			tutorSecGroups.add(group.getOwnerGroup());
			secToGroupMap.put(group.getOwnerGroup(), group);
			participantSecGroups.add(group.getPartipiciantGroup());
			secToGroupMap.put(group.getPartipiciantGroup(), group);
		}
		List<BGRights> rights = new ArrayList<BGRights>();
		List<Policy> tutorPolicies = securityManager.getPoliciesOfSecurityGroup(tutorSecGroups, resource);
		rights.addAll(findBGRights(tutorPolicies, secToGroupMap, resource, BGRightsRole.tutor));
		List<Policy> participantPolicies = securityManager.getPoliciesOfSecurityGroup(participantSecGroups, resource);
		rights.addAll(findBGRights(participantPolicies, secToGroupMap, resource, BGRightsRole.participant));
		return rights;
	}
	
	private List<BGRights> findBGRights(List<Policy> policies, Map<SecurityGroup,BusinessGroup> secToGroupMap, OLATResource resource, BGRightsRole role) {
		List<BGRights> rights = new ArrayList<BGRights>();
		Map<BGRights,BGRights> rightsMap = new HashMap<BGRights,BGRights>();
		for (Policy policy:policies) {
			String right = policy.getPermission();
			if (right.indexOf(BG_RIGHT_PREFIX) == 0 && policy.getOlatResource().equals(resource)) {
				BusinessGroup group = secToGroupMap.get(policy.getSecurityGroup());
				BGRights wrapper = new BGRightsImpl(group.getKey(), role);
				if(rightsMap.containsKey(wrapper)) {
					wrapper = rightsMap.get(wrapper);
				} else {
					rightsMap.put(wrapper, wrapper);
				}
				wrapper.getRights().add(right);
				rights.add(wrapper);
			}
		}
		
		return rights;
	}

	@Override
	public boolean hasBGRight(List<BusinessGroup> groups) {
		if(groups == null || groups.isEmpty()) return false;
		
		List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>(groups.size());
		for(BusinessGroup group:groups) {
			secGroups.add(group.getOwnerGroup());
			secGroups.add(group.getPartipiciantGroup());
		}
		
		List<Policy> results = securityManager.getPoliciesOfSecurityGroup(secGroups);
		// filter all business group rights permissions. group right permissions
		// start with bgr.
		for (Policy rightPolicy:results) {
			String right = rightPolicy.getPermission();
			if (right.indexOf(BG_RIGHT_PREFIX) == 0) {
				return true;
			}
		}
		return false;
	}
}
