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

package org.olat.group;

import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.group.context.BGContext;
import org.olat.group.properties.BusinessGroupPropertyManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.testutils.codepoints.server.Codepoint;

/**
 * Description: <BR/> Use the business group factory to create new instances of
 * groups of type buddy groups, learning groups and right groups. Initial Date:
 * Aug 23, 2004
 * 
 * @author gnaegi
 */
public class BusinessGroupFactory {

	/**
	 * No constructor available
	 */
	private BusinessGroupFactory() {
	// no constructor needed, all methods are static
	}

	/**
	 * Factory method to create new business groups
	 * 
	 * @param type The business group type
	 * @param identity The identity that will be an initial owner or participant
	 *          of the group (depends on type). Can be null (depends on type)
	 * @param name The group name
	 * @param description The group description
	 * @param minParticipants The minimal number of participants (only
	 *          declarative)
	 * @param maxParticipants The maximal number of participants
	 * @param groupContext The group context or null
	 * @return The newly created group or null if this groupname is already taken
	 *         by another group in the given context.
	 */
	static BusinessGroup createAndPersistBusinessGroup(String type, Identity identity, String name, String description,
			Integer minParticipants, Integer maxParticipants, Boolean waitingListEnabled,Boolean autoCloseRanksEnabled, BGContext groupContext) {
		if (BusinessGroup.TYPE_BUDDYGROUP.equals(type)) {
			return BusinessGroupFactory.createAndPersistBuddyGroup(identity, name, description, minParticipants, maxParticipants);
		} else if (BusinessGroup.TYPE_LEARNINGROUP.equals(type)) {
			return BusinessGroupFactory
					.createAndPersistLearningGroup(identity, name, description, minParticipants, maxParticipants, waitingListEnabled, autoCloseRanksEnabled, groupContext);
		} else if (BusinessGroup.TYPE_RIGHTGROUP.equals(type)) {
			return BusinessGroupFactory.createAndPersistRightGroup(identity, name, description, minParticipants, maxParticipants, groupContext);
		} else {
			throw new AssertException("Unknown business group type::" + type);
		}
	}

	/**
	 * Create a group of type buddy group
	 * 
	 * @param identity
	 * @param name
	 * @param description
	 * @return the group
	 */
	private static BusinessGroup createAndPersistBuddyGroup(Identity identity, String name, String description, Integer minParticipants,
			Integer maxParticipants) {
		/*
		 * [1] create 2 security groups -> ownerGroup, partipiciantGroup........ [2]
		 * create a buddyGroup with name, description, introMsg and the 2 security
		 * groups...................................................... [3] create 2
		 * policies, ownerGroup -> PERMISSION_ACCESS -> buddygroup.
		 * ....partipiciantGroup -> PERMISSION_READ -> buddygroup ..............
		 */
		BusinessGroupImpl businessgroup = null;
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		// groups
		SecurityGroup ownerGroup = securityManager.createAndPersistSecurityGroup();
		SecurityGroup partipiciantGroup = securityManager.createAndPersistSecurityGroup();

		businessgroup = new BusinessGroupImpl(BusinessGroup.TYPE_BUDDYGROUP, name, description, ownerGroup, partipiciantGroup,null/* no waitingGroup*/, null);
		businessgroup.setMinParticipants(minParticipants);
		businessgroup.setMaxParticipants(maxParticipants);

		DBFactory.getInstance().saveObject(businessgroup);
		if(Tracing.isDebugEnabled(BusinessGroupFactory.class)){
			Tracing.logDebug("created Buddy Group named " + name + " for Identity " + identity, BusinessGroupFactory.class);
		}
		/*
		 * policies: - ownerGroup can do everything on this businessgroup -> is an
		 * admin, can invite people to owner.- & partipiciantgroup -
		 * partipiciantGroup can read this businessgroup
		 */
		OLATResource businessgroupOlatResource =  OLATResourceManager.getInstance().createOLATResourceInstance(businessgroup);
		OLATResourceManager.getInstance().saveOLATResource(businessgroupOlatResource);

		//		securityManager.createAndPersistPolicy(ownerGroup, Constants.PERMISSION_ACCESS, businessgroup);
		securityManager.createAndPersistPolicyWithResource(ownerGroup, Constants.PERMISSION_ACCESS, businessgroupOlatResource);
		securityManager.createAndPersistPolicyWithResource(partipiciantGroup, Constants.PERMISSION_READ, businessgroupOlatResource);
		// membership: add identity
		securityManager.addIdentityToSecurityGroup(identity, ownerGroup);

		// per default all collaboration-tools are disabled

		// group members visibility
		BusinessGroupPropertyManager bgpm = new BusinessGroupPropertyManager(businessgroup);
		bgpm.createAndPersistDisplayMembers(true, false, false);
		return businessgroup;
	}

	/**
	 * Create a group of type learning group
	 * 
	 * @param identity
	 * @param name
	 * @param description
	 * @param groupContext
	 * @return the group or null if the groupname is not unique in the given
	 *         context
	 */
	private static BusinessGroup createAndPersistLearningGroup(Identity identity, String name, String description, Integer minParticipants,
			Integer maxParticipants, Boolean waitingListEnabled,Boolean autoCloseRanksEnabled, BGContext groupContext) {
		/*
		 * [1] create 2 security groups -> ownerGroup, partipiciantGroup........ [2]
		 * create a learningGroup with name, description, introMsg and the 2
		 * security groups...................................................... [3]
		 * create 2 policies, ownerGroup -> PERMISSION_ACCESS ....partipiciantGroup ->
		 * PERMISSION_READ
		 */
		BusinessGroupImpl businessgroup = null;
		BaseSecurity securityManager = BaseSecurityManager.getInstance();

		// check if group does already exist in this learning context
		boolean groupExists = testIfGroupAlreadyExists(name, BusinessGroup.TYPE_LEARNINGROUP, groupContext);
		if (groupExists) {
			// there is already a group with this name, return without
			// creating a new group
			Tracing.logWarn("A group with this name already exists! You will get null instead of a businessGroup returned!", BusinessGroupFactory.class);
			return null;
		}
		Codepoint.codepoint(BusinessGroupFactory.class, "createAndPersistLearningGroup");
		// groups
		SecurityGroup ownerGroup = securityManager.createAndPersistSecurityGroup();
		SecurityGroup partipiciantGroup = securityManager.createAndPersistSecurityGroup();
		SecurityGroup waitingGroup = securityManager.createAndPersistSecurityGroup();
		//
		businessgroup = new BusinessGroupImpl(BusinessGroup.TYPE_LEARNINGROUP, name, description, ownerGroup, partipiciantGroup, waitingGroup, groupContext);
		businessgroup.setMinParticipants(minParticipants);
		businessgroup.setMaxParticipants(maxParticipants);
		businessgroup.setWaitingListEnabled(waitingListEnabled);
		businessgroup.setAutoCloseRanksEnabled(autoCloseRanksEnabled);
		
		DBFactory.getInstance().saveObject(businessgroup);
		if(Tracing.isDebugEnabled(BusinessGroupFactory.class)){
			Tracing.logDebug("created Learning Group named " + name, BusinessGroupFactory.class);
		}
		/*
		 * policies: - ownerGroup can do everything on this businessgroup -> is an
		 * admin, can invite people to owner.- & partipiciantgroup -
		 * partipiciantGroup can read this businessgroup
		 */
		OLATResource businessgroupOlatResource = OLATResourceManager.getInstance().createOLATResourceInstance(businessgroup);
		OLATResourceManager.getInstance().saveOLATResource(businessgroupOlatResource);
		OLATResource groupContextOlatResource = OLATResourceManager.getInstance().findResourceable(groupContext);
		if (groupContextOlatResource == null) {
			OLATResourceManager.getInstance().createOLATResourceInstance(groupContext);
			OLATResourceManager.getInstance().saveOLATResource(groupContextOlatResource);
		}
		securityManager.createAndPersistPolicyWithResource(ownerGroup, Constants.PERMISSION_ACCESS, businessgroupOlatResource);
		securityManager.createAndPersistPolicyWithResource(ownerGroup, Constants.PERMISSION_COACH, groupContextOlatResource);
		securityManager.createAndPersistPolicyWithResource(partipiciantGroup, Constants.PERMISSION_READ, businessgroupOlatResource);
		securityManager.createAndPersistPolicyWithResource(partipiciantGroup, Constants.PERMISSION_PARTI, groupContextOlatResource);
		// membership: add identity if available
		if (identity != null) {
			securityManager.addIdentityToSecurityGroup(identity, ownerGroup);
		}

		// per default all collaboration-tools are disabled

		// group members visibility
		BusinessGroupPropertyManager bgpm = new BusinessGroupPropertyManager(businessgroup);
		bgpm.createAndPersistDisplayMembers(true, false, false);
		return businessgroup;
	}

	/**
	 * Create a group of type right group
	 * 
	 * @param identity
	 * @param name
	 * @param description
	 * @param groupContext
	 * @return the group or null if the groupname is not unique in the given
	 *         context
	 */
	private static BusinessGroup createAndPersistRightGroup(Identity identity, String name, String description, Integer minParticipants,
			Integer maxParticipants, BGContext groupContext) {
		/*
		 * [1] create 1 security group -> partipiciantGroup........ [2] create a
		 * learningGroup with name, description, introMsg and the security
		 * group...................................................... [3] create 2
		 * policies, partipiciantGroup -> PERMISSION_READ
		 */
		BusinessGroupImpl businessgroup = null;
		BaseSecurity securityManager = BaseSecurityManager.getInstance();

		// check if group does already exist in this learning context
		boolean groupExists = testIfGroupAlreadyExists(name, BusinessGroup.TYPE_RIGHTGROUP, groupContext);
		if (groupExists) {
			// there is already a group with this name, return without
			// creating a new group
			return null;
		}

		// group
		SecurityGroup partipiciantGroup = securityManager.createAndPersistSecurityGroup();
		//
		businessgroup = new BusinessGroupImpl(BusinessGroup.TYPE_RIGHTGROUP, name, description, null, partipiciantGroup,null/* no waitingGroup */, groupContext);
		businessgroup.setMinParticipants(minParticipants);
		businessgroup.setMaxParticipants(maxParticipants);
		//
		DBFactory.getInstance().saveObject(businessgroup);
		if(Tracing.isDebugEnabled(BusinessGroupFactory.class)){
			Tracing.logDebug("Created Right Group named " + name, BusinessGroupFactory.class);
		}
		/*
		 * policies: - partipiciantGroup can read this businessgroup
		 */
		OLATResource businessgroupOlatResource = OLATResourceManager.getInstance().createOLATResourceInstance(businessgroup);
		OLATResourceManager.getInstance().saveOLATResource(businessgroupOlatResource);
		securityManager.createAndPersistPolicyWithResource(partipiciantGroup, Constants.PERMISSION_READ, businessgroupOlatResource);
		// membership: add identity if available
		if (identity != null) {
			securityManager.addIdentityToSecurityGroup(identity, partipiciantGroup);
		}

		// per default all collaboration-tools are disabled

		// group members visibility
		BusinessGroupPropertyManager bgpm = new BusinessGroupPropertyManager(businessgroup);
		bgpm.createAndPersistDisplayMembers(false, true, false);
		return businessgroup;
	}

	/**
	 * true if one or more names of groups are alredy used in context, false otherwise.
	 * @param names
	 * @param type
	 * @param groupContext
	 * @return
	 */
	public static boolean checkIfOneOrMoreNameExistsInContext(Set names, BGContext groupContext) {
		DB db = DBFactory.getInstance();
		String query = "select count(bgs) from " 
			+ "  org.olat.group.BusinessGroupImpl as bgs " 
			+ "  where "
			+ "  bgs.groupContext = :context" 
			+ " and bgs.name in (:names)";
		DBQuery dbq = db.createQuery(query);
		dbq.setEntity("context", groupContext);		
		dbq.setParameterList("names", names);
		int result = ((Long) dbq.list().get(0)).intValue();
		//return false if none of the groups was found
		if (result == 0) return false;
		//true if one or more groups were found
		return true;
	}
	
	/**
	 * @param name Name of the business group
	 * @param type The group type
	 * @param groupContext The group context of null
	 * @return true if a group in such a context with the given name exists false
	 *         otherwhise
	 */
	private static boolean testIfGroupAlreadyExists(String name, String type, BGContext groupContext) {
		DB db = DBFactory.getInstance();
		String query = "select count(bgs) from " + "  org.olat.group.BusinessGroupImpl as bgs " + " where bgs.type = :type"
				+ " and bgs.groupContext = :context" + " and bgs.name = :name";
		DBQuery dbq = db.createQuery(query);
		dbq.setString("type", type);
		dbq.setEntity("context", groupContext);
		dbq.setString("name", name);
		int result = ((Long) dbq.list().get(0)).intValue();
		if (result != 0) return true;
		return false;
	}
}