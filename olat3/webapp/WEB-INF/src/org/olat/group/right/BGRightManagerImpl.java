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
* <p>
*/ 

package org.olat.group.right;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Policy;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.manager.BasicManager;
import org.olat.group.BusinessGroup;
import org.olat.group.context.BGContext;

/**
 * Description:<BR>
 * TODO: Class Description for BGRightManagerImpl Initial Date: Aug 24, 2004
 * 
 * @author gnaegi
 */
public class BGRightManagerImpl extends BasicManager implements BGRightManager {

	private static BGRightManagerImpl INSTANCE;
	static {
		INSTANCE = new BGRightManagerImpl();
	}

	/**
	 * @return singleton instance
	 */
	public static BGRightManagerImpl getInstance() {
		return INSTANCE;
	}

	private BGRightManagerImpl() {
	// no public constructor
	}

	/**
	 * @see org.olat.group.right.BGRightManager#addBGRight(java.lang.String,
	 *      org.olat.group.BusinessGroup)
	 */
	public void addBGRight(String bgRight, BusinessGroup rightGroup) {
		if (bgRight.indexOf(BG_RIGHT_PREFIX) == -1) throw new AssertException("Groups rights must start with prefix '" + BG_RIGHT_PREFIX
				+ "', but given right is ::" + bgRight);
		if (BusinessGroup.TYPE_RIGHTGROUP.equals(rightGroup.getType())) {
			BaseSecurity secm = BaseSecurityManager.getInstance();
			BGContext context = rightGroup.getGroupContext();
			secm.createAndPersistPolicy(rightGroup.getPartipiciantGroup(), bgRight, context);
		} else {
			throw new AssertException("Only right groups can have bg rights, but type was ::" + rightGroup.getType());
		}
	}

	/**
	 * @see org.olat.group.right.BGRightManager#removeBGRight(java.lang.String,
	 *      org.olat.group.BusinessGroup)
	 */
	public void removeBGRight(String bgRight, BusinessGroup rightGroup) {
		if (BusinessGroup.TYPE_RIGHTGROUP.equals(rightGroup.getType())) {
			BaseSecurity secm = BaseSecurityManager.getInstance();
			BGContext context = rightGroup.getGroupContext();
			secm.deletePolicy(rightGroup.getPartipiciantGroup(), bgRight, context);
		} else {
			throw new AssertException("Only right groups can have bg rights, but type was ::" + rightGroup.getType());
		}
	}

	/**
	 * @see org.olat.group.right.BGRightManager#hasBGRight(java.lang.String,
	 *      org.olat.group.BusinessGroup)
	 */
	/*
	 * public boolean hasBGRight(String bgRight, BusinessGroup rightGroup) { if
	 * (BusinessGroup.TYPE_RIGHTGROUP.equals(rightGroup.getType())) { Manager secm =
	 * ManagerFactory.getManager(); return
	 * secm.isGroupPermittedOnResourceable(rightGroup.getPartipiciantGroup(),
	 * bgRight, rightGroup.getGroupContext()); } throw new AssertException("Only
	 * right groups can have bg rights, but type was ::" + rightGroup.getType()); }
	 */

	/**
	 * @see org.olat.group.right.BGRightManager#hasBGRight(java.lang.String,
	 *      org.olat.core.id.Identity, org.olat.group.context.BGContext)
	 */
	public boolean hasBGRight(String bgRight, Identity identity, BGContext bgContext) {
		if (BusinessGroup.TYPE_RIGHTGROUP.equals(bgContext.getGroupType())) {
			BaseSecurity secm = BaseSecurityManager.getInstance();
			return secm.isIdentityPermittedOnResourceable(identity, bgRight, bgContext);
		}
		throw new AssertException("Only right groups can have bg rights, but type was ::" + bgContext.getGroupType());
	}

	/**
	 * @see org.olat.group.right.BGRightManager#findBGRights(org.olat.group.BusinessGroup)
	 */
	public List findBGRights(BusinessGroup rightGroup) {
		BaseSecurity secm = BaseSecurityManager.getInstance();
		List results = secm.getPoliciesOfSecurityGroup(rightGroup.getPartipiciantGroup());
		// filter all business group rights permissions. group right permissions
		// start with bgr.
		List rights = new ArrayList();
		for (int i = 0; i < results.size(); i++) {
			Policy rightPolicy = (Policy) results.get(i);
			String right = rightPolicy.getPermission();
			if (right.indexOf(BG_RIGHT_PREFIX) == 0) rights.add(right);
		}
		return rights;
	}
}
