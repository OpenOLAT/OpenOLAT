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
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.model.BGAreaReference;
import org.olat.group.model.BusinessGroupEnvironment;
import org.olat.group.model.BusinessGroupReference;
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
	
	private static final OLog log = Tracing.createLoggerFor(PersistingCourseGroupManager.class);

	private static final String LEARNINGGROUPEXPORT_XML = "learninggroupexport.xml";
	private static final String RIGHTGROUPEXPORT_XML = "rightgroupexport.xml";
	private static final String LEARNINGGROUPARCHIVE_XLS = "learninggroup_archiv.xls";
	//private static final String RIGHTGROUPARCHIVE_XLS = "rightgroup_archiv.xls";

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
	}

	@Override
	public OLATResource getCourseResource() {
		return courseResource;
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

	@Override
	public boolean isIdentityInGroup(Identity identity, Long groupKey) {
		return businessGroupService.isIdentityInBusinessGroup(identity, groupKey, true, true, courseResource);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isLearningGroupFull(java.lang.String)
	 */
	@Override
	public boolean isBusinessGroupFull(Long groupKey){
		boolean isLearningGroupFull = false;
		BusinessGroup group = businessGroupService.loadBusinessGroup(groupKey);
		if (group == null){
			log.warn("no groups available");
			return false;
		} else {
			// has group participants
			int members = securityManager.countIdentitiesOfSecurityGroup(group.getPartipiciantGroup());
			// has group no maximum of participants
			if (group.getMaxParticipants() == null) {
				log.warn("group.getMaxParticipants() is null");
			} else if (members >= group.getMaxParticipants().intValue()) {
			// is the set of members greater equals than the maximum of participants
				isLearningGroupFull = true;
			}
		}
		return isLearningGroupFull;
	}
	
	@Override
	public boolean isIdentityInLearningArea(Identity identity, Long areaKey) {
		return areaManager.isIdentityInBGArea(identity, null, areaKey, courseResource);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getAllLearningGroupsFromAllContexts()
	 */
	@Override
	public List<BusinessGroup> getAllBusinessGroups() {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		return businessGroupService.findBusinessGroups(params, courseResource, 0, -1);
	}

	@Override
	public boolean existGroup(String nameOrKey) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		if(StringHelper.isLong(nameOrKey)) {
			params.setGroupKeys(Collections.singletonList(new Long(nameOrKey)));
		}else {
			params.setExactName(nameOrKey);
		}
		return businessGroupService.countBusinessGroups(params, courseResource) > 0;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getAllAreasFromAllContexts()
	 */
	@Override
	public List<BGArea> getAllAreas() {
		List<BGArea> areas = areaManager.findBGAreasInContext(courseResource);
		return areas;
	}

	@Override
	public boolean existArea(String nameOrKey) {
		return areaManager.existArea(nameOrKey, courseResource);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getOwnedBusinessGroups(org.olat.core.id.Identity)
	 */
	@Override
	public List<BusinessGroup> getOwnedBusinessGroups(Identity identity) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams(identity, true, false);
		List<BusinessGroup> allGroups =
				businessGroupService.findBusinessGroups(params, courseResource, 0, -1);
		return allGroups;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipatingBusinessGroups(org.olat.core.id.Identity)
	 */
	@Override
	public List<BusinessGroup> getParticipatingBusinessGroups(Identity identity) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams(identity, false, true);
		List<BusinessGroup> allGroups =
				businessGroupService.findBusinessGroups(params, courseResource, 0, -1);
		return allGroups;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityCourseCoach(org.olat.core.id.Identity)
	 */
	public boolean isIdentityCourseCoach(Identity identity) {
		//fxdiff VCRP-1: access control of learn resource
		boolean isCoach = RepositoryManager.getInstance().isIdentityInTutorSecurityGroup(identity, courseResource);
		if (isCoach) { // don't check any further
			return true;
		}

		BaseSecurity secManager = BaseSecurityManager.getInstance();
		boolean isParticipant = secManager.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_COACH, courseResource)
				|| businessGroupService.isIdentityInBusinessGroup(identity, null, true, false, courseResource);
		return isParticipant;
	}
	
	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityCourseCoach(org.olat.core.id.Identity)
	 */
	public boolean isIdentityCourseParticipant(Identity identity) {
		//fxdiff VCRP-1: access control of learn resource
		boolean participant = RepositoryManager.getInstance().isIdentityInParticipantSecurityGroup(identity, courseResource);
		if (participant) {// don't check any further
			return true;
		}
		
		BaseSecurity secManager = BaseSecurityManager.getInstance();
		boolean isParticipant = secManager.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_PARTI, courseResource)
				|| businessGroupService.isIdentityInBusinessGroup(identity, null, false, true, courseResource);
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
	 * @see org.olat.course.groupsandrights.CourseGroupManager#deleteCourseGroupmanagement()
	 */
	public void deleteCourseGroupmanagement() {
		//TODO gm
		//delete something???
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
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getUniqueAreaNames()
	 */
	public List<String> getUniqueAreaNames() {
		List<BGArea> areas = getAllAreas();
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
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getUniqueBusinessGroupNames()
	 */
	public List<String> getUniqueBusinessGroupNames() {
		List<BusinessGroup> groups = getAllBusinessGroups();
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
	 * @see org.olat.course.groupsandrights.CourseGroupManager#exportCourseBusinessGroups(java.io.File)
	 */
	@Override
	public void exportCourseBusinessGroups(File fExportDirectory, CourseEnvironmentMapper courseEnv, boolean backwardsCompatible) {
		File fExportFile = new File(fExportDirectory, LEARNINGGROUPEXPORT_XML);
		List<BGArea> areas = getAllAreas();
		List<BusinessGroup> groups = getAllBusinessGroups();

		BusinessGroupEnvironment bgEnv = new BusinessGroupEnvironment();
		bgEnv.getGroups().addAll(courseEnv.getGroups());
		bgEnv.getAreas().addAll(courseEnv.getAreas());
		businessGroupService.exportGroups(groups, areas, fExportFile, bgEnv, backwardsCompatible);
	}
	
	/**
	 * This operation load all business groups and areas. Use with caution, costly!
	 * @param resource
	 * @param fGroupExportXML
	 * @return
	 */
	public CourseEnvironmentMapper getBusinessGroupEnvironment() {
		CourseEnvironmentMapper env = new CourseEnvironmentMapper();
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(null, courseResource, 0, -1);
		for(BusinessGroup group:groups) {
			env.getGroups().add(new BusinessGroupReference(group));
		}
		List<BGArea> areas = areaManager.findBGAreasInContext(courseResource);
		for(BGArea area:areas) {
			env.getAreas().add(new BGAreaReference(area));
		}
		return env;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#importCourseBusinessGroups(java.io.File)
	 */
	@Override
	public CourseEnvironmentMapper importCourseBusinessGroups(File fImportDirectory) {
		CourseEnvironmentMapper envMapper = new CourseEnvironmentMapper();
		RepositoryEntry courseRe = RepositoryManager.getInstance().lookupRepositoryEntry(courseResource, true);
		File fGroupXML1 = new File(fImportDirectory, LEARNINGGROUPEXPORT_XML);
		if(fGroupXML1.exists()) {
			BusinessGroupEnvironment env = businessGroupService.importGroups(courseRe, fGroupXML1);
			envMapper.addBusinessGroupEnvironment(env);
		}
		File fGroupXML2 = new File(fImportDirectory, RIGHTGROUPEXPORT_XML);
		if(fGroupXML2.exists()) {
			BusinessGroupEnvironment env = businessGroupService.importGroups(courseRe, fGroupXML2);
			envMapper.addBusinessGroupEnvironment(env);	
		}
		return envMapper;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getCoachesFromBusinessGroups(String)
	 */
	public List<Identity> getCoachesFromBusinessGroups() {
		List<BusinessGroup> bgs = getAllBusinessGroups();

		List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>();
		for(BusinessGroup group:bgs) {
			secGroups.add(group.getOwnerGroup());
		}
		return securityManager.getIdentitiesOfSecurityGroups(secGroups);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipantsFromBusinessGroups(String)
	 */
	public List<Identity> getParticipantsFromBusinessGroups() {
		List<BusinessGroup> bgs = getAllBusinessGroups();
		List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>();
		for(BusinessGroup group:bgs) {
			secGroups.add(group.getPartipiciantGroup());
		}
		return securityManager.getIdentitiesOfSecurityGroups(secGroups);
	}
	
	@Override
	public List<Identity> getCoachesFromBusinessGroups(List<Long> groupKeys) {
		List<BusinessGroup> bgs = businessGroupService.loadBusinessGroups(groupKeys);
		List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>();
		for(BusinessGroup group:bgs) {
			secGroups.add(group.getOwnerGroup());
		}
		return securityManager.getIdentitiesOfSecurityGroups(secGroups);
	}
	
	@Override
	public List<Identity> getParticipantsFromBusinessGroups(List<Long> groupKeys) {
		List<BusinessGroup> bgs = businessGroupService.loadBusinessGroups(groupKeys);
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
	public List<Identity> getCoachesFromAreas() {
		List<BusinessGroup> bgs = getAllBusinessGroups();
		
		List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>();
		for(BusinessGroup group:bgs) {
			secGroups.add(group.getOwnerGroup());
		}
		return securityManager.getIdentitiesOfSecurityGroups(secGroups);
	}
	
	@Override
	public List<Identity> getCoachesFromAreas(List<Long> areaKeys) {
		List<BGArea> areas = areaManager.loadAreas(areaKeys);
		List<BusinessGroup> groups = areaManager.findBusinessGroupsOfAreas(areas);
		List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>();
		for(BusinessGroup group:groups) {
			secGroups.add(group.getOwnerGroup());
		}
		return securityManager.getIdentitiesOfSecurityGroups(secGroups);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipantsFromArea(java.lang.String)
	 */
	public List<Identity> getParticipantsFromAreas() {
		List<BusinessGroup> bgs = getAllBusinessGroups();
		
		List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>();
		for(BusinessGroup group:bgs) {
			secGroups.add(group.getPartipiciantGroup());
		}
		return securityManager.getIdentitiesOfSecurityGroups(secGroups);
	}

	@Override
	public List<Identity> getParticipantsFromAreas(List<Long> areaKeys) {
		List<BGArea> areas = areaManager.loadAreas(areaKeys);
		List<BusinessGroup> groups = areaManager.findBusinessGroupsOfAreas(areas);
		List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>();
		for(BusinessGroup group:groups) {
			secGroups.add(group.getPartipiciantGroup());
		}
		return securityManager.getIdentitiesOfSecurityGroups(secGroups);
	}

	/**
	 * 
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getWaitingListGroups(org.olat.core.id.Identity)
	 */
	public List<BusinessGroup> getWaitingListGroups(Identity identity) {
		List<BusinessGroup> groups = businessGroupService.findBusinessGroupsWithWaitingListAttendedBy(identity, courseResource);
		return groups;
	}

	/**
	 * Archive all learning-group-contexts and right-group-contexts.
	 * @param exportDirectory
	 */
	public void archiveCourseGroups(File exportDirectory) {
		File exportLearningGroupFile = new File(exportDirectory, "default_" + LEARNINGGROUPARCHIVE_XLS);
		businessGroupService.archiveGroups(getAllBusinessGroups(), exportLearningGroupFile);
	}
}