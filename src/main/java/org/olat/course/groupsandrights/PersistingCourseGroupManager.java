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

package org.olat.course.groupsandrights;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.manager.BasicManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.area.BGAreaManagerImpl;
import org.olat.group.context.BGContext;
import org.olat.group.context.BGContextManager;
import org.olat.group.context.BGContextManagerImpl;
import org.olat.group.right.BGRightManager;
import org.olat.group.right.BGRightManagerImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;

/**
 * Description:<BR/> Implementation of the CourseGroupManager that persists its
 * data on the database <P/>
 * 
 * Initial Date: Aug 25, 2004
 * @author gnaegi
 */
public class PersistingCourseGroupManager extends BasicManager implements CourseGroupManager {

	private static final String LEARNINGGROUPEXPORT_XML = "learninggroupexport.xml";
	private static final String RIGHTGROUPEXPORT_XML = "rightgroupexport.xml";
	private static final String LEARNINGGROUPARCHIVE_XLS = "learninggroup_archiv.xls";
	private static final String RIGHTGROUPARCHIVE_XLS = "rightgroup_archiv.xls";

	private OLATResource courseResource;
	private List learningGroupContexts;
	private List rightGroupContexts;

	private PersistingCourseGroupManager(OLATResourceable course) {
		this.courseResource = OLATResourceManager.getInstance().findOrPersistResourceable(course);
		initGroupContextsList();
	}

	private PersistingCourseGroupManager(OLATResource courseResource) {
		this.courseResource = courseResource;
		initGroupContextsList();
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#initGroupContextsList()
	 */
	public void initGroupContextsList() {
		BGContextManager contextManager = BGContextManagerImpl.getInstance();
		learningGroupContexts = contextManager.findBGContextsForResource(courseResource, BusinessGroup.TYPE_LEARNINGROUP, true, true);
		rightGroupContexts = contextManager.findBGContextsForResource(courseResource, BusinessGroup.TYPE_RIGHTGROUP, true, true);
	}

	/**
	 * @param course The current course
	 * @return A course group manager that uses persisted data
	 */
	public static PersistingCourseGroupManager getInstance(OLATResourceable course) {
		return new PersistingCourseGroupManager(course);
	}

	/**
	 * @param courseResource The current course resource
	 * @return A course group manager that uses persisted data
	 */
	public static PersistingCourseGroupManager getInstance(OLATResource courseResource) {
		return new PersistingCourseGroupManager(courseResource);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#hasRight(org.olat.core.id.Identity,
	 *      java.lang.String)
	 */
	public boolean hasRight(Identity identity, String courseRight) {
		return hasRight(identity, courseRight, null);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#hasRight(org.olat.core.id.Identity,
	 *      java.lang.String, java.lang.String)
	 */
	public boolean hasRight(Identity identity, String courseRight, String groupContextName) {
		BGRightManager rightManager = BGRightManagerImpl.getInstance();
		Iterator iter = rightGroupContexts.iterator();
		while (iter.hasNext()) {
			BGContext context = (BGContext) iter.next();
			if (groupContextName == null || context.getName().equals(groupContextName)) {
				boolean hasRight = rightManager.hasBGRight(courseRight, identity, context);
				if (hasRight) return true; // finished
			}
		}
		return false;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityInLearningGroup(org.olat.core.id.Identity,
	 *      java.lang.String)
	 */
	public boolean isIdentityInLearningGroup(Identity identity, String groupName) {
		return isIdentityInLearningGroup(identity, groupName, null);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityInLearningGroup(org.olat.core.id.Identity,
	 *      java.lang.String, java.lang.String)
	 */
	public boolean isIdentityInLearningGroup(Identity identity, String groupName, String groupContextName) {
		return isIdentityInGroup(identity, groupName, groupContextName, this.learningGroupContexts);
	}
	
	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isLearningGroupFull(java.lang.String)
	 */
	public boolean isLearningGroupFull(String groupName){
		OLog logger = Tracing.createLoggerFor(getClass());
		List<BusinessGroup> groups = getLearningGroupsFromAllContexts(groupName);
		
		if (groups == null){
			logger.warn("no groups available");
			return false;
		} else {
			boolean isLearningGroupFull = false;
			for (BusinessGroup businessGroup : groups) {
				// if group null
				if (businessGroup == null) {
					logger.warn("group is null");
					return false;
				}
				// has group participants
				BaseSecurity secMgr = BaseSecurityManager.getInstance();
				List<Identity> members = secMgr.getIdentitiesOfSecurityGroup(businessGroup.getPartipiciantGroup());
				if (members == null) {
					logger.warn("group members are null");
					return false;
				}
				// has group no maximum of participants
				if (businessGroup.getMaxParticipants() == null) {
					logger.warn("group.getMaxParticipants() is null");
					return false;
				}
				// is the set of members greater equals than the maximum of participants
				if (members.size() >= businessGroup.getMaxParticipants().intValue()) {
					isLearningGroupFull = true;
				} else {
					return false;
				}
			}
			return isLearningGroupFull;
		}
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityInRightGroup(org.olat.core.id.Identity,
	 *      java.lang.String)
	 */
	public boolean isIdentityInRightGroup(Identity identity, String groupName) {
		return isIdentityInRightGroup(identity, groupName, null);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityInRightGroup(org.olat.core.id.Identity,
	 *      java.lang.String, java.lang.String)
	 */
	public boolean isIdentityInRightGroup(Identity identity, String groupName, String groupContextName) {
		return isIdentityInGroup(identity, groupName, groupContextName, this.rightGroupContexts);
	}

	/**
	 * Internal method to check if an identity is in a group
	 * 
	 * @param identity
	 * @param groupName the group name. must not be null
	 * @param groupContextName context name to restrict to a certain context or
	 *          null if in any context
	 * @param contextList list of contexts that should be searched
	 * @return true if in group, false otherwhise
	 */
	private boolean isIdentityInGroup(Identity identity, String groupName, String groupContextName, List contextList) {
		BusinessGroupManager groupManager = BusinessGroupManagerImpl.getInstance();
		Iterator iter = contextList.iterator();
		while (iter.hasNext()) {
			BGContext context = (BGContext) iter.next();
			if (groupContextName == null || context.getName().equals(groupContextName)) {
				boolean inGroup = groupManager.isIdentityInBusinessGroup(identity, groupName, context);
				if (inGroup) return true; // finished
			}
		}
		return false;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityInLearningArea(org.olat.core.id.Identity,
	 *      java.lang.String)
	 */
	public boolean isIdentityInLearningArea(Identity identity, String areaName) {
		return isIdentityInLearningArea(identity, areaName, null);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityInLearningArea(org.olat.core.id.Identity,
	 *      java.lang.String, java.lang.String)
	 */
	public boolean isIdentityInLearningArea(Identity identity, String areaName, String groupContextName) {
		BGAreaManager areaManager = BGAreaManagerImpl.getInstance();
		Iterator iter = learningGroupContexts.iterator();
		while (iter.hasNext()) {
			BGContext context = (BGContext) iter.next();
			if (groupContextName == null || context.getName().equals(groupContextName)) {
				boolean inArea = areaManager.isIdentityInBGArea(identity, areaName, context);
				if (inArea) return true; // finished
			}
		}
		return false;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityInGroupContext(org.olat.core.id.Identity,
	 *      java.lang.String)
	 */
	public boolean isIdentityInGroupContext(Identity identity, String groupContextName) {
		BGContextManager contextManager = BGContextManagerImpl.getInstance();
		Iterator iter = learningGroupContexts.iterator();
		while (iter.hasNext()) {
			BGContext context = (BGContext) iter.next();
			if (groupContextName == null || context.getName().equals(groupContextName)) {
				boolean inContext = contextManager.isIdentityInBGContext(identity, context, true, true);
				if (inContext) return true; // finished
			}
		}
		iter = rightGroupContexts.iterator();
		while (iter.hasNext()) {
			BGContext context = (BGContext) iter.next();
			if (groupContextName == null || context.getName().equals(groupContextName)) {
				boolean inContext = contextManager.isIdentityInBGContext(identity, context, true, true);
				if (inContext) return true; // finished
			}
		}
		return false;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getLearningGroupContexts()
	 */
	public List getLearningGroupContexts() {
		return learningGroupContexts;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getRightGroupContexts()
	 */
	public List getRightGroupContexts() {
		return rightGroupContexts;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getRightGroupsFromAllContexts(java.lang.String)
	 */
	public List getRightGroupsFromAllContexts(String groupName) {
		List groups = new ArrayList();
		BGContextManager contextManager = BGContextManagerImpl.getInstance();
		Iterator iterator = rightGroupContexts.iterator();
		while (iterator.hasNext()) {
			BGContext bgContext = (BGContext) iterator.next();
			BusinessGroup group = contextManager.findGroupOfBGContext(groupName, bgContext);
			if (group != null) groups.add(group);
		}
		return groups;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getAllLearningGroupsFromAllContexts()
	 */
	public List getAllLearningGroupsFromAllContexts() {
		List allGroups = new ArrayList();
		BGContextManager contextManager = BGContextManagerImpl.getInstance();
		Iterator iterator = learningGroupContexts.iterator();
		while (iterator.hasNext()) {
			BGContext bgContext = (BGContext) iterator.next();
			List contextGroups = contextManager.getGroupsOfBGContext(bgContext);
			allGroups.addAll(contextGroups);
		}
		return allGroups;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getLearningGroupsFromAllContexts(java.lang.String)
	 */
	public List<BusinessGroup> getLearningGroupsFromAllContexts(String groupName) {
		List<BusinessGroup> groups = new ArrayList<BusinessGroup>();
		BGContextManager contextManager = BGContextManagerImpl.getInstance();
		Iterator iterator = learningGroupContexts.iterator();
		while (iterator.hasNext()) {
			BGContext bgContext = (BGContext) iterator.next();
			BusinessGroup group = contextManager.findGroupOfBGContext(groupName, bgContext);
			if (group != null) groups.add(group);
		}
		return groups;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getAllAreasFromAllContexts()
	 */
	public List getAllAreasFromAllContexts() {
		List allAreas = new ArrayList();
		BGAreaManager areaManager = BGAreaManagerImpl.getInstance();
		Iterator iterator = learningGroupContexts.iterator();
		while (iterator.hasNext()) {
			BGContext bgContext = (BGContext) iterator.next();
			List contextAreas = areaManager.findBGAreasOfBGContext(bgContext);
			allAreas.addAll(contextAreas);
		}
		return allAreas;

	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getLearningGroupsInAreaFromAllContexts(java.lang.String)
	 */
	public List getLearningGroupsInAreaFromAllContexts(String areaName) {
		List groups = new ArrayList();
		BGAreaManager areaManager = BGAreaManagerImpl.getInstance();
		Iterator iterator = learningGroupContexts.iterator();
		while (iterator.hasNext()) {
			BGContext bgContext = (BGContext) iterator.next();
			BGArea area = areaManager.findBGArea(areaName, bgContext);
			if (area != null) {
				List areaGroups = areaManager.findBusinessGroupsOfArea(area);
				groups.addAll(areaGroups);
			}
		}
		return groups;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getLearningAreasOfGroupFromAllContexts(java.lang.String)
	 */
	public List getLearningAreasOfGroupFromAllContexts(String groupName) {
		List areas = new ArrayList();
		BGAreaManager areaManager = BGAreaManagerImpl.getInstance();
		BGContextManager contextManager = BGContextManagerImpl.getInstance();
		Iterator iterator = learningGroupContexts.iterator();
		while (iterator.hasNext()) {
			BGContext bgContext = (BGContext) iterator.next();
			BusinessGroup group = contextManager.findGroupOfBGContext(groupName, bgContext);
			if (group != null) {
				List groupAreas = areaManager.findBGAreasOfBusinessGroup(group);
				areas.addAll(groupAreas);
			}
		}
		return areas;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipatingLearningGroupsFromAllContexts(org.olat.core.id.Identity,
	 *      java.lang.String)
	 */
	public List getParticipatingLearningGroupsFromAllContexts(Identity identity, String groupName) {
		BGContextManager contextManager = BGContextManagerImpl.getInstance();
		List groups = new ArrayList();
		Iterator iter = learningGroupContexts.iterator();
		while (iter.hasNext()) {
			BGContext context = (BGContext) iter.next();
			BusinessGroup group = contextManager.findGroupAttendedBy(identity, groupName, context);
			if (group != null) groups.add(group);
		}
		return groups;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipatingLearningGroupsInAreaFromAllContexts(org.olat.core.id.Identity,
	 *      java.lang.String)
	 */
	public List getParticipatingLearningGroupsInAreaFromAllContexts(Identity identity, String areaName) {
		BGAreaManager areaManager = BGAreaManagerImpl.getInstance();
		List groups = new ArrayList();
		Iterator iter = learningGroupContexts.iterator();
		while (iter.hasNext()) {
			BGContext context = (BGContext) iter.next();
			List contextGroups = areaManager.findBusinessGroupsOfAreaAttendedBy(identity, areaName, context);
			groups.addAll(contextGroups);
		}
		return groups;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getAllRightGroupsFromAllContexts()
	 */
	public List getAllRightGroupsFromAllContexts() {
		List allGroups = new ArrayList();
		BGContextManager contextManager = BGContextManagerImpl.getInstance();
		Iterator iterator = rightGroupContexts.iterator();
		while (iterator.hasNext()) {
			BGContext bgContext = (BGContext) iterator.next();
			List contextGroups = contextManager.getGroupsOfBGContext(bgContext);
			allGroups.addAll(contextGroups);
		}
		return allGroups;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getOwnedLearningGroupsFromAllContexts(org.olat.core.id.Identity)
	 */
	public List getOwnedLearningGroupsFromAllContexts(Identity identity) {
		List allGroups = new ArrayList();
		BusinessGroupManager groupManager = BusinessGroupManagerImpl.getInstance();
		Iterator iterator = learningGroupContexts.iterator();
		while (iterator.hasNext()) {
			BGContext bgContext = (BGContext) iterator.next();
			List contextGroups = groupManager.findBusinessGroupsOwnedBy(bgContext.getGroupType(), identity, bgContext);
			allGroups.addAll(contextGroups);
		}
		return allGroups;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipatingLearningGroupsFromAllContexts(org.olat.core.id.Identity)
	 */
	public List getParticipatingLearningGroupsFromAllContexts(Identity identity) {
		List allGroups = new ArrayList();
		BusinessGroupManager groupManager = BusinessGroupManagerImpl.getInstance();
		Iterator iterator = learningGroupContexts.iterator();
		while (iterator.hasNext()) {
			BGContext bgContext = (BGContext) iterator.next();
			List contextGroups = groupManager.findBusinessGroupsAttendedBy(bgContext.getGroupType(), identity, bgContext);
			allGroups.addAll(contextGroups);
		}
		return allGroups;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipatingRightGroupsFromAllContexts(org.olat.core.id.Identity)
	 */
	public List getParticipatingRightGroupsFromAllContexts(Identity identity) {
		List allGroups = new ArrayList();
		BusinessGroupManager groupManager = BusinessGroupManagerImpl.getInstance();
		Iterator iterator = rightGroupContexts.iterator();
		while (iterator.hasNext()) {
			BGContext bgContext = (BGContext) iterator.next();
			List contextGroups = groupManager.findBusinessGroupsAttendedBy(bgContext.getGroupType(), identity, bgContext);
			allGroups.addAll(contextGroups);
		}
		return allGroups;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityCourseCoach(org.olat.core.id.Identity)
	 */
	public boolean isIdentityCourseCoach(Identity identity) {
		BaseSecurity secManager = BaseSecurityManager.getInstance();
		
	//fxdiff VCRP-1: access control of learn resource
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(courseResource, false);
		if(re != null && re.getTutorGroup() != null) {
			boolean isCoach = secManager.isIdentityInSecurityGroup(identity, re.getTutorGroup());
			if (isCoach) // don't check any further
				return true;
		}
		
		Iterator iterator = learningGroupContexts.iterator();
		while (iterator.hasNext()) {
			BGContext bgContext = (BGContext) iterator.next();
			boolean isCoach = secManager.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_COACH, bgContext);
			if (isCoach) // don't check any further
			return true;
		}
		return false;
	}
	
	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityCourseCoach(org.olat.core.id.Identity)
	 */
	public boolean isIdentityCourseParticipant(Identity identity) {
		BaseSecurity secManager = BaseSecurityManager.getInstance();
		
		//fxdiff VCRP-1: access control of learn resource
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(courseResource, false);
		if(re != null && re.getParticipantGroup() != null) {
			boolean isParticipant = secManager.isIdentityInSecurityGroup(identity, re.getParticipantGroup());
			if (isParticipant) // don't check any further
				return true;
		}
		
		Iterator<BGContext> iterator = learningGroupContexts.iterator();
		for( ; iterator.hasNext(); ) {
			BGContext bgContext = iterator.next();
			boolean isParticipant = secManager.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_PARTI, bgContext);
			if (isParticipant) // don't check any further
				return true;
		}
		
		
		return false;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityCourseAdministrator(org.olat.core.id.Identity)
	 */
	public boolean isIdentityCourseAdministrator(Identity identity) {
		// not really a group management method, for your convenience we have a
		// shortcut here...
		BaseSecurity secMgr = BaseSecurityManager.getInstance();
		return secMgr.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_ADMIN, courseResource);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityParticipantInAnyLearningGroup(org.olat.core.id.Identity)
	 */
	public boolean isIdentityParticipantInAnyLearningGroup(Identity identity) {
		BGContextManager contextManager = BGContextManagerImpl.getInstance();
		Iterator iterator = learningGroupContexts.iterator();
		while (iterator.hasNext()) {
			BGContext bgContext = (BGContext) iterator.next();
			if (contextManager.isIdentityInBGContext(identity, bgContext, false, true)) return true;
		}
		return false;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityParticipantInAnyRightGroup(org.olat.core.id.Identity)
	 */
	public boolean isIdentityParticipantInAnyRightGroup(Identity identity) {
		BGContextManager contextManager = BGContextManagerImpl.getInstance();
		Iterator iterator = rightGroupContexts.iterator();
		while (iterator.hasNext()) {
			BGContext bgContext = (BGContext) iterator.next();
			if (contextManager.isIdentityInBGContext(identity, bgContext, false, true)) return true;
		}
		return false;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#createCourseGroupmanagement(java.lang.String)
	 */
	public void createCourseGroupmanagement(String courseTitle) {
		BGContextManager contextManager = BGContextManagerImpl.getInstance();
		// 1. context for learning groups
		if (this.learningGroupContexts.size() == 0) {
			String learningGroupContextName = CourseGroupManager.DEFAULT_NAME_LC_PREFIX + courseTitle;
			contextManager.createAndAddBGContextToResource(learningGroupContextName, courseResource,
					BusinessGroup.TYPE_LEARNINGROUP, null, true);
			// no need to add it to list of contexts, already done by createAndAddBGContextToResource

		}
		// 2. context for right groups
		if (this.rightGroupContexts.size() == 0) {
			String rightGroupContextName = CourseGroupManager.DEFAULT_NAME_RC_PREFIX + courseTitle;
			contextManager.createAndAddBGContextToResource(rightGroupContextName, courseResource,
					BusinessGroup.TYPE_RIGHTGROUP, null, true);
			// no need to add it to list of contexts, already done by createAndAddBGContextToResource
		}
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#createCourseGroupmanagementAsCopy(org.olat.course.groupsandrights.CourseGroupManager,
	 *      java.lang.String)
	 */
	public void createCourseGroupmanagementAsCopy(CourseGroupManager originalCourseGroupManager, String courseTitle) {

		// wrap as transatcion: do everything or nothing

		// 1. do copy learning group contexts
		int count = 0;
		BGContextManager contextManager = BGContextManagerImpl.getInstance();
		List<BGContext> origLgC = originalCourseGroupManager.getLearningGroupContexts();
		for (BGContext origContext:origLgC) {
			if (origContext.isDefaultContext()) {
				// we found default context, copy this one
				String learningGroupContextName = CourseGroupManager.DEFAULT_NAME_LC_PREFIX + courseTitle;
				contextManager.copyAndAddBGContextToResource(learningGroupContextName, this.courseResource, origContext);
				// no need to add it to list of contexts, already done by copyAndAddBGContextToResource
			} else {
				// not a course default context but an associated context - copy only
				// reference
				contextManager.addBGContextToResource(origContext, courseResource);
				// no need to add it to list of contexts, already done by addBGContextToResource				
			}
			if(count++ % 2 == 0) {
				DBFactory.getInstance().intermediateCommit();
			}
		}
		
		// 2. do copy right group contexts
		List<BGContext> origRgC = originalCourseGroupManager.getRightGroupContexts();
		for (BGContext origContext:origRgC) {
			if (origContext.isDefaultContext()) {
				// we found default context, copy this one
				String rightGroupContextName = CourseGroupManager.DEFAULT_NAME_RC_PREFIX + courseTitle;
				contextManager.copyAndAddBGContextToResource(rightGroupContextName, this.courseResource, origContext);
				// no need to add it to list of contexts, already done by copyAndAddBGContextToResource
			} else {
				// not a course default context but an associated context - copy only
				// reference
				contextManager.addBGContextToResource(origContext, courseResource);
				// no need to add it to list of contexts, already done by addBGContextToResource
			}
			if(count++ % 2 == 0) {
				DBFactory.getInstance().intermediateCommit();
			}
		}
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#deleteCourseGroupmanagement()
	 */
	public void deleteCourseGroupmanagement() {
		BGContextManager contextManager = BGContextManagerImpl.getInstance();
		// contextManager.removeAllBGContextsFromResource(courseResource);

		List allContexts = contextManager.findBGContextsForResource(courseResource, true, true);
		Iterator iter = allContexts.iterator();
		while (iter.hasNext()) {
			BGContext context = (BGContext) iter.next();
			if (context.isDefaultContext()) {
				contextManager.deleteBGContext(context);
			} else {
				// not a default context, only unlink from this course
				contextManager.removeBGContextFromResource(context, courseResource);
			}
		}
		Tracing.logAudit("Deleting course groupmanagement for " + courseResource.toString(), this.getClass());
	}

	/**
	 * @param groups List of business groups
	 * @return list of Integers that contain the number of participants for each
	 *         group
	 */
	public List getNumberOfMembersFromGroups(List groups) {
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		List members = new ArrayList();
		Iterator iterator = groups.iterator();
		while (iterator.hasNext()) {
			BusinessGroup group = (BusinessGroup) iterator.next();
			int numbMembers = securityManager.countIdentitiesOfSecurityGroup(group.getPartipiciantGroup());
			members.add(new Integer(numbMembers));
		}
		return members;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getUniqueAreaNamesFromAllContexts()
	 */
	public List getUniqueAreaNamesFromAllContexts() {
		List areas = getAllAreasFromAllContexts();
		List areaNames = new ArrayList();

		Iterator iter = areas.iterator();
		while (iter.hasNext()) {
			BGArea area = (BGArea) iter.next();
			String areaName = area.getName();
			if (!areaNames.contains(areaName)) areaNames.add(areaName.trim());
		}

		Collections.sort(areaNames);
		
		return areaNames;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getUniqueLearningGroupNamesFromAllContexts()
	 */
	public List getUniqueLearningGroupNamesFromAllContexts() {
		List groups = getAllLearningGroupsFromAllContexts();
		List groupNames = new ArrayList();

		Iterator iter = groups.iterator();
		while (iter.hasNext()) {
			BusinessGroup group = (BusinessGroup) iter.next();
			String groupName = group.getName();
			if (!groupNames.contains(groupName)) groupNames.add(groupName.trim());
		}
		
		Collections.sort(groupNames);
		
		return groupNames;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#exportCourseLeaningGroups(java.io.File)
	 */
	public void exportCourseLeaningGroups(File fExportDirectory) {
		BGContext context = findDefaultLearningContext();
		File fExportFile = new File(fExportDirectory, LEARNINGGROUPEXPORT_XML);
		BusinessGroupManagerImpl.getInstance().exportGroups(context, fExportFile);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#importCourseLearningGroups(java.io.File)
	 */
	public void importCourseLearningGroups(File fImportDirectory) {
		File fGroupExportXML = new File(fImportDirectory, LEARNINGGROUPEXPORT_XML);
		BGContext context = findDefaultLearningContext();
		if (context == null) throw new AssertException(
				"Unable to find default context for imported course. Should have been created before calling importCourseLearningGroups()");
		BusinessGroupManagerImpl.getInstance().importGroups(context, fGroupExportXML);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#exportCourseRightGroups(java.io.File)
	 */
	public void exportCourseRightGroups(File fExportDirectory) {
		BGContext context = findDefaultRightsContext();
		File fExportFile = new File(fExportDirectory, RIGHTGROUPEXPORT_XML);
		BusinessGroupManagerImpl.getInstance().exportGroups(context, fExportFile);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#importCourseRightGroups(java.io.File)
	 */
	public void importCourseRightGroups(File fImportDirectory) {
		File fGroupExportXML = new File(fImportDirectory, RIGHTGROUPEXPORT_XML);
		BGContext context = findDefaultRightsContext();
		if (context == null) throw new AssertException(
				"Unable to find default context for imported course. Should have been created before calling importCourseLearningGroups()");
		BusinessGroupManagerImpl.getInstance().importGroups(context, fGroupExportXML);
	}

	private BGContext findDefaultLearningContext() {
		List contexts = getLearningGroupContexts();
		BGContext context = null;
		for (Iterator iter = contexts.iterator(); iter.hasNext();) {
			context = (BGContext) iter.next();
			if (context.isDefaultContext()) break;
		}
		return context;
	}

	private BGContext findDefaultRightsContext() {
		List contexts = getRightGroupContexts();
		BGContext context = null;
		for (Iterator iter = contexts.iterator(); iter.hasNext();) {
			context = (BGContext) iter.next();
			if (context.isDefaultContext()) break;
		}
		return context;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getCoachesFromLearningGroups(String)
	 */
	public List getCoachesFromLearningGroup(String groupName) {
		BaseSecurity secManager = BaseSecurityManager.getInstance();
		List retVal = new ArrayList();
		List bgs = null;
		if (groupName != null) {
			// filtered by name
			bgs = getLearningGroupsFromAllContexts(groupName);
		} else {
			// no filter
			bgs = getAllLearningGroupsFromAllContexts();
		}
		for (int i = 0; i < bgs.size(); i++) {
			// iterates over all business group in the course, fetching the identities
			// of the business groups OWNER
			BusinessGroup elm = (BusinessGroup) bgs.get(i);
			retVal.addAll(secManager.getIdentitiesOfSecurityGroup(elm.getOwnerGroup()));
		}
		return retVal;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipantsFromLearningGroups(String)
	 */
	public List getParticipantsFromLearningGroup(String groupName) {
		BaseSecurity secManager = BaseSecurityManager.getInstance();
		List retVal = new ArrayList();
		List bgs = null;
		if (groupName != null) {
			// filtered by name
			bgs = getLearningGroupsFromAllContexts(groupName);
		} else {
			// no filter
			bgs = getAllLearningGroupsFromAllContexts();
		}
		for (int i = 0; i < bgs.size(); i++) {
			// iterates over all business group in the course, fetching the identities
			// of the business groups PARTICIPANTS
			BusinessGroup elm = (BusinessGroup) bgs.get(i);
			retVal.addAll(secManager.getIdentitiesOfSecurityGroup(elm.getPartipiciantGroup()));
		}
		return retVal;
	}
	
	@Override
	//fxdiff VCRP-1,2: access control of resources
	public List<Identity> getCoaches() {
		BaseSecurity secManager = BaseSecurityManager.getInstance();
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(courseResource, false);
		if(re != null && re.getTutorGroup() != null) {
			return secManager.getIdentitiesOfSecurityGroup(re.getTutorGroup());
		}
		return Collections.emptyList();
	}

	@Override
	//fxdiff VCRP-1,2: access control of resources
	public List<Identity> getParticipants() {
		BaseSecurity secManager = BaseSecurityManager.getInstance();
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(courseResource, false);
		if(re != null && re.getParticipantGroup() != null) {
			return secManager.getIdentitiesOfSecurityGroup(re.getParticipantGroup());
		}
		return Collections.emptyList();
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getCoachesFromArea(java.lang.String)
	 */
	public List getCoachesFromArea(String areaName) {
		BaseSecurity secManager = BaseSecurityManager.getInstance();
		List retVal = new ArrayList();
		List bgs = null;
		if (areaName != null) {
			bgs = getLearningGroupsInAreaFromAllContexts(areaName);
		} else {
			bgs = getAllLearningGroupsFromAllContexts();
		}
		for (int i = 0; i < bgs.size(); i++) {
			// iterates over all business group in the course's area, fetching the
			// OWNER identities
			BusinessGroup elm = (BusinessGroup) bgs.get(i);
			retVal.addAll(secManager.getIdentitiesOfSecurityGroup(elm.getOwnerGroup()));
		}
		return retVal;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipantsFromArea(java.lang.String)
	 */
	public List getParticipantsFromArea(String areaName) {
		BaseSecurity secManager = BaseSecurityManager.getInstance();
		List retVal = new ArrayList();
		List bgs = null;
		if (areaName != null) {
			bgs = getLearningGroupsInAreaFromAllContexts(areaName);
		} else {
			bgs = getAllLearningGroupsFromAllContexts();
		}
		for (int i = 0; i < bgs.size(); i++) {
			// iterates over all business group in the course's area, fetching the
			// PARTIPICIANT identities
			BusinessGroup elm = (BusinessGroup) bgs.get(i);
			retVal.addAll(secManager.getIdentitiesOfSecurityGroup(elm.getPartipiciantGroup()));
		}
		return retVal;
	}

	/**
	 * 
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getWaitingListGroupsFromAllContexts(org.olat.core.id.Identity)
	 */
	public List getWaitingListGroupsFromAllContexts(Identity identity) {
		List allGroups = new ArrayList();
		BusinessGroupManager groupManager = BusinessGroupManagerImpl.getInstance();
		Iterator iterator = learningGroupContexts.iterator();
		while (iterator.hasNext()) {
			BGContext bgContext = (BGContext) iterator.next();
			List contextGroups = groupManager.findBusinessGroupsWithWaitingListAttendedBy(bgContext.getGroupType(), identity, bgContext);
			allGroups.addAll(contextGroups);
		}
		return allGroups;
	}

	/**
	 * Archive all learning-group-contexts and right-group-contexts.
	 * @param exportDirectory
	 */
	public void archiveCourseGroups(File exportDirectory) {
		archiveAllContextFor(getLearningGroupContexts(), LEARNINGGROUPARCHIVE_XLS, exportDirectory);
		archiveAllContextFor(getRightGroupContexts(),    RIGHTGROUPARCHIVE_XLS,    exportDirectory);
	}

	/**
	 * Archive a list of context.
	 * Archive the default context in a xls file with prefix 'default_' e.g. default_learninggroupexport.xml.
	 * Archive all other context in xls files with prefix 'context_<CONTEXTCOUNTER>_' e.g. context_2_learninggroupexport.xml
	 * @param contextList      List of BGContext
	 * @param fileName         E.g. learninggroupexport.xml
	 * @param exportDirectory  Archive files will be created in this dir.
	 */
	private void archiveAllContextFor(List contextList, String fileName, File exportDirectory) {
		int contextCounter = 1;
		for (Iterator iter = contextList.iterator(); iter.hasNext();) {
			BGContext context = (BGContext) iter.next();
			if (context.isDefaultContext()) {
				BusinessGroupManagerImpl.getInstance().archiveGroups(context, new File(exportDirectory, "default_" + fileName));
			} else {
				BusinessGroupManagerImpl.getInstance().archiveGroups(context, new File(exportDirectory, "context_" + contextCounter + "_" + fileName));
				contextCounter++;
			}
		}
		
	}
	
}