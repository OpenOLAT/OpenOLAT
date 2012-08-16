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
import java.util.Collections;
import java.util.List;

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
	public void addBGRight(String bgRight, BusinessGroup rightGroup) {
		List<OLATResource> resources = businessGroupRelationDAO.findResources(Collections.singletonList(rightGroup), 0, -1);
		for(OLATResource resource:resources) {
			securityManager.createAndPersistPolicy(rightGroup.getPartipiciantGroup(), bgRight, resource);
		}
	}

	/**
	 * @see org.olat.group.right.BGRightManager#removeBGRight(java.lang.String,
	 *      org.olat.group.BusinessGroup)
	 */
	@Override
	public void removeBGRight(String bgRight, BusinessGroup rightGroup) {
		List<OLATResource> resources = businessGroupRelationDAO.findResources(Collections.singletonList(rightGroup), 0, -1);
		for(OLATResource resource:resources) {
			securityManager.deletePolicy(rightGroup.getPartipiciantGroup(), bgRight, resource);
		}
	}

	@Override
	public boolean hasBGRight(String bgRight, Identity identity, OLATResource resource) {
		return securityManager.isIdentityPermittedOnResourceable(identity, bgRight, resource);
	}

	/**
	 * @see org.olat.group.right.BGRightManager#findBGRights(org.olat.group.BusinessGroup)
	 */
	@Override
	public List<String> findBGRights(BusinessGroup rightGroup) {
		List<Policy> results = securityManager.getPoliciesOfSecurityGroup(rightGroup.getPartipiciantGroup());
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
	public int countBGRight(List<BusinessGroup> groups) {
		if(groups == null || groups.isEmpty()) return 0;
		
		List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>(groups.size());
		for(BusinessGroup group:groups) {
			secGroups.add(group.getPartipiciantGroup());
		}
		
		List<Policy> results = securityManager.getPoliciesOfSecurityGroup(secGroups);
		// filter all business group rights permissions. group right permissions
		// start with bgr.
		int count = 0;
		for (Policy rightPolicy:results) {
			String right = rightPolicy.getPermission();
			if (right.indexOf(BG_RIGHT_PREFIX) == 0) {
				count++;
			}
		}
		return count;
	}
}
