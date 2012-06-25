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
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.right.BGRightManager;
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

	private final OLATResource courseResource;
	
	private final BGAreaManager areaManager;
	private final BGRightManager rightManager;
	private final BaseSecurity securityManager;
	private final BusinessGroupService businessGroupService;

	private PersistingCourseGroupManager(OLATResourceable course) {
		this(OLATResourceManager.getInstance().findOrPersistResourceable(course));
	}

	private PersistingCourseGroupManager(OLATResource courseResource) {
		this.courseResource = courseResource;
		areaManager = CoreSpringFactory.getImpl(BGAreaManager.class);
		rightManager = CoreSpringFactory.getImpl(BGRightManager.class);
		securityManager = BaseSecurityManager.getInstance();
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		initGroupContextsList();
	}

	@Override
	public OLATResource getCourseResource() {
		return courseResource;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#initGroupContextsList()
	 */
	public void initGroupContextsList() {
		//
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
		boolean hasRight = rightManager.hasBGRight(courseRight, identity, courseResource);
		return hasRight;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityInLearningGroup(org.olat.core.id.Identity,
	 *      java.lang.String, java.lang.String)
	 */
	public boolean isIdentityInLearningGroup(Identity identity, String groupName) {
		return businessGroupService.isIdentityInBusinessGroup(identity, groupName, BusinessGroup.TYPE_LEARNINGROUP, true, true, courseResource);
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
		return businessGroupService.isIdentityInBusinessGroup(identity, groupName, BusinessGroup.TYPE_RIGHTGROUP, true, true, courseResource);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityInLearningArea(org.olat.core.id.Identity,
	 *      java.lang.String)
	 */
	public boolean isIdentityInLearningArea(Identity identity, String areaName) {
		return areaManager.isIdentityInBGArea(identity, areaName, courseResource);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getRightGroupsFromAllContexts(java.lang.String)
	 */
	public List<BusinessGroup> getRightGroupsFromAllContexts(String groupName) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.addTypes(BusinessGroup.TYPE_RIGHTGROUP);
		params.setExactName(groupName);
		return businessGroupService.findBusinessGroups(params, null, false, false, courseResource, 0, -1);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getAllLearningGroupsFromAllContexts()
	 */
	public List<BusinessGroup> getAllLearningGroupsFromAllContexts() {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.addTypes(BusinessGroup.TYPE_LEARNINGROUP);
		return businessGroupService.findBusinessGroups(params, null, false, false, courseResource, 0, -1);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getLearningGroupsFromAllContexts(java.lang.String)
	 */
	public List<BusinessGroup> getLearningGroupsFromAllContexts(String groupName) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.addTypes(BusinessGroup.TYPE_LEARNINGROUP);
		params.setExactName(groupName);
		return businessGroupService.findBusinessGroups(params, null, false, false, courseResource, 0, -1);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getAllAreasFromAllContexts()
	 */
	public List<BGArea> getAllAreasFromAllContexts() {
		List<BusinessGroup> learningGroups = getAllLearningGroupsFromAllContexts();
		List<BGArea> areas = areaManager.findBGAreasOfBusinessGroups(learningGroups);
		return areas;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getLearningGroupsInAreaFromAllContexts(java.lang.String)
	 */
	public List<BusinessGroup> getLearningGroupsInAreaFromAllContexts(String areaName) {
		List<BGArea> areas = areaManager.findBGAreasOfBGContext(courseResource);
		List<BusinessGroup> groups = areaManager.findBusinessGroupsOfAreas(areas);
		return groups;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getLearningAreasOfGroupFromAllContexts(java.lang.String)
	 */
	public List<BGArea> getLearningAreasOfGroupFromAllContexts(String groupName) {
		List<BusinessGroup> learningGroups = getLearningGroupsFromAllContexts(groupName);
		List<BGArea> areas = areaManager.findBGAreasOfBusinessGroups(learningGroups);
		return areas;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipatingLearningGroupsFromAllContexts(org.olat.core.id.Identity,
	 *      java.lang.String)
	 */
	public List<BusinessGroup> getParticipatingLearningGroupsFromAllContexts(Identity identity, String groupName) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.addTypes(BusinessGroup.TYPE_LEARNINGROUP);
		return businessGroupService.findBusinessGroups(params, identity, false, true, courseResource, 0, -1);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipatingLearningGroupsInAreaFromAllContexts(org.olat.core.id.Identity,
	 *      java.lang.String)
	 */
	public List<BusinessGroup> getParticipatingLearningGroupsInAreaFromAllContexts(Identity identity, String areaName) {
		List<BusinessGroup> groups = areaManager.findBusinessGroupsOfAreaAttendedBy(identity, areaName, courseResource);
		return groups;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getAllRightGroupsFromAllContexts()
	 */
	public List<BusinessGroup> getAllRightGroupsFromAllContexts() {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.addTypes(BusinessGroup.TYPE_RIGHTGROUP);
		return businessGroupService.findBusinessGroups(params, null, false, false, courseResource, 0, -1);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getOwnedLearningGroupsFromAllContexts(org.olat.core.id.Identity)
	 */
	@Override
	public List<BusinessGroup> getOwnedLearningGroupsFromAllContexts(Identity identity) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.addTypes(BusinessGroup.TYPE_LEARNINGROUP);
		List<BusinessGroup> allGroups =
				businessGroupService.findBusinessGroups(params, identity, true, false, courseResource, 0, -1);
		return allGroups;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipatingLearningGroupsFromAllContexts(org.olat.core.id.Identity)
	 */
	@Override
	public List<BusinessGroup> getParticipatingLearningGroupsFromAllContexts(Identity identity) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.addTypes(BusinessGroup.TYPE_LEARNINGROUP);
		List<BusinessGroup> allGroups =
				businessGroupService.findBusinessGroups(params, identity, false, true, courseResource, 0, -1);
		return allGroups;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipatingRightGroupsFromAllContexts(org.olat.core.id.Identity)
	 */
	@Override
	public List<BusinessGroup> getParticipatingRightGroupsFromAllContexts(Identity identity) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.addTypes(BusinessGroup.TYPE_RIGHTGROUP);
		List<BusinessGroup> allGroups =
				businessGroupService.findBusinessGroups(params, identity, false, true, courseResource, 0, -1);
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

		boolean isParticipant = secManager.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_COACH, courseResource)
				|| businessGroupService.isIdentityInBusinessGroup(identity, null, null, true, false, courseResource);
		return isParticipant;
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

		boolean isParticipant = secManager.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_PARTI, courseResource)
				|| businessGroupService.isIdentityInBusinessGroup(identity, null, null, false, true, courseResource);
		return isParticipant;
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
		return businessGroupService.isIdentityInBusinessGroup(identity, null, BusinessGroup.TYPE_LEARNINGROUP, false, true, courseResource);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityParticipantInAnyRightGroup(org.olat.core.id.Identity)
	 */
	public boolean isIdentityParticipantInAnyRightGroup(Identity identity) {
		return businessGroupService.isIdentityInBusinessGroup(identity, null, BusinessGroup.TYPE_RIGHTGROUP, false, true, courseResource);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#deleteCourseGroupmanagement()
	 */
	public void deleteCourseGroupmanagement() {
		// contextManager.removeAllBGContextsFromResource(courseResource);
		//TODO gm
		/*
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
		*/
		logAudit("Deleting course groupmanagement for " + courseResource.toString());
	}

	/**
	 * @param groups List of business groups
	 * @return list of Integers that contain the number of participants for each
	 *         group
	 */
	public List<Integer> getNumberOfMembersFromGroups(List<BusinessGroup> groups) {
		List<Integer> members = new ArrayList<Integer>();
		for (BusinessGroup group:groups) {
			int numbMembers = securityManager.countIdentitiesOfSecurityGroup(group.getPartipiciantGroup());
			members.add(new Integer(numbMembers));
		}
		return members;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getUniqueAreaNamesFromAllContexts()
	 */
	public List<String> getUniqueAreaNamesFromAllContexts() {
		List<BGArea> areas = getAllAreasFromAllContexts();
		List<String> areaNames = new ArrayList<String>();
		for (BGArea area:areas) {
			if (!areaNames.contains(area.getName())) {
				areaNames.add(area.getName().trim());
			}
		}
		Collections.sort(areaNames);
		return areaNames;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getUniqueLearningGroupNamesFromAllContexts()
	 */
	public List<String> getUniqueLearningGroupNamesFromAllContexts() {
		List<BusinessGroup> groups = getAllLearningGroupsFromAllContexts();
		List<String> groupNames = new ArrayList<String>();
		for (BusinessGroup group:groups) {
			if (!groupNames.contains(group.getName())) {
				groupNames.add(group.getName().trim());
			}
		}
		Collections.sort(groupNames);
		return groupNames;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#exportCourseLeaningGroups(java.io.File)
	 */
	public void exportCourseLeaningGroups(File fExportDirectory) {
		File fExportFile = new File(fExportDirectory, LEARNINGGROUPEXPORT_XML);
		List<BusinessGroup> learningGroups = this.getAllLearningGroupsFromAllContexts();
		businessGroupService.exportGroups(learningGroups, fExportFile);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#importCourseLearningGroups(java.io.File)
	 */
	public void importCourseLearningGroups(File fImportDirectory) {
		File fGroupExportXML = new File(fImportDirectory, LEARNINGGROUPEXPORT_XML);
		businessGroupService.importGroups(courseResource, fGroupExportXML);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#exportCourseRightGroups(java.io.File)
	 */
	public void exportCourseRightGroups(File fExportDirectory) {
		File fExportFile = new File(fExportDirectory, RIGHTGROUPEXPORT_XML);
		List<BusinessGroup> rightGroups = getAllRightGroupsFromAllContexts();
		businessGroupService.exportGroups(rightGroups, fExportFile);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#importCourseRightGroups(java.io.File)
	 */
	public void importCourseRightGroups(File fImportDirectory) {
		File fGroupExportXML = new File(fImportDirectory, RIGHTGROUPEXPORT_XML);
		businessGroupService.importGroups(courseResource, fGroupExportXML);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getCoachesFromLearningGroups(String)
	 */
	public List<Identity> getCoachesFromLearningGroup(String groupName) {
		List<BusinessGroup> bgs = null;
		if (groupName != null) {
			// filtered by name
			bgs = getLearningGroupsFromAllContexts(groupName);
		} else {
			// no filter
			bgs = getAllLearningGroupsFromAllContexts();
		}
		
		List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>();
		for(BusinessGroup group:bgs) {
			secGroups.add(group.getOwnerGroup());
		}
		return securityManager.getIdentitiesOfSecurityGroups(secGroups);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipantsFromLearningGroups(String)
	 */
	public List<Identity> getParticipantsFromLearningGroup(String groupName) {
		List<BusinessGroup> bgs = null;
		if (groupName != null) {
			// filtered by name
			bgs = getLearningGroupsFromAllContexts(groupName);
		} else {
			// no filter
			bgs = getAllLearningGroupsFromAllContexts();
		}
		
		List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>();
		for(BusinessGroup group:bgs) {
			secGroups.add(group.getPartipiciantGroup());
		}
		return securityManager.getIdentitiesOfSecurityGroups(secGroups);
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
	public List<Identity> getCoachesFromArea(String areaName) {
		List<BusinessGroup> bgs = null;
		if (StringHelper.containsNonWhitespace(areaName)) {
			bgs = getLearningGroupsInAreaFromAllContexts(areaName);
		} else {
			bgs = getAllLearningGroupsFromAllContexts();
		}
		
		List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>();
		for(BusinessGroup group:bgs) {
			secGroups.add(group.getOwnerGroup());
		}
		return securityManager.getIdentitiesOfSecurityGroups(secGroups);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipantsFromArea(java.lang.String)
	 */
	public List<Identity> getParticipantsFromArea(String areaName) {
		List<BusinessGroup> bgs;
		if (StringHelper.containsNonWhitespace(areaName)) {
			bgs = getLearningGroupsInAreaFromAllContexts(areaName);
		} else {
			bgs = getAllLearningGroupsFromAllContexts();
		}
		
		List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>();
		for(BusinessGroup group:bgs) {
			secGroups.add(group.getPartipiciantGroup());
		}
		return securityManager.getIdentitiesOfSecurityGroups(secGroups);
	}

	/**
	 * 
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getWaitingListGroupsFromAllContexts(org.olat.core.id.Identity)
	 */
	public List<BusinessGroup> getWaitingListGroupsFromAllContexts(Identity identity) {
		List<BusinessGroup> groups = businessGroupService.findBusinessGroupsWithWaitingListAttendedBy(null, identity, courseResource);
		return groups;
	}

	/**
	 * Archive all learning-group-contexts and right-group-contexts.
	 * @param exportDirectory
	 */
	public void archiveCourseGroups(File exportDirectory) {
		File exportLearningGroupFile = new File(exportDirectory, "default_" + LEARNINGGROUPARCHIVE_XLS);
		businessGroupService.archiveGroups(getAllLearningGroupsFromAllContexts(), exportLearningGroupFile);
		File exportRightGroupsFile = new File(exportDirectory, "default_" + RIGHTGROUPARCHIVE_XLS);
		businessGroupService.archiveGroups(getAllRightGroupsFromAllContexts(), exportRightGroupsFile);
	}
}