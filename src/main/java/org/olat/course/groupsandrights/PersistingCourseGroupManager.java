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

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
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
import org.olat.ims.lti13.LTI13Service;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;

/**
 * Description:<BR/> Implementation of the CourseGroupManager that persists its
 * data on the database <P/>
 * 
 * Initial Date: Aug 25, 2004
 * @author gnaegi
 */
public class PersistingCourseGroupManager implements CourseGroupManager {
	
	private static final Logger log = Tracing.createLoggerFor(PersistingCourseGroupManager.class);

	private static final String LEARNINGGROUPEXPORT_XML = "learninggroupexport.xml";
	private static final String RIGHTGROUPEXPORT_XML = "rightgroupexport.xml";
	private static final String LEARNINGGROUPARCHIVE_XLS = "learninggroup_archiv.xls";

	private RepositoryEntry courseRepoEntry;
	private final OLATResource courseResource;
	
	private final BGAreaManager areaManager;
	private final BGRightManager rightManager;
	private final RepositoryManager repositoryManager;
	private final RepositoryService repositoryService;
	private final OrganisationService organisationService;
	private final BusinessGroupService businessGroupService;
	private final CurriculumService curriculumService;

	private PersistingCourseGroupManager(OLATResourceable course) {
		this(OLATResourceManager.getInstance().findOrPersistResourceable(course));
	}

	private PersistingCourseGroupManager(OLATResource courseResource) {
		this.courseResource = courseResource;
		areaManager = CoreSpringFactory.getImpl(BGAreaManager.class);
		rightManager = CoreSpringFactory.getImpl(BGRightManager.class);
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		curriculumService = CoreSpringFactory.getImpl(CurriculumService.class);
	}
	
	private PersistingCourseGroupManager(RepositoryEntry courseRepoEntry) {
		this.courseRepoEntry = courseRepoEntry;
		this.courseResource = courseRepoEntry.getOlatResource();
		areaManager = CoreSpringFactory.getImpl(BGAreaManager.class);
		rightManager = CoreSpringFactory.getImpl(BGRightManager.class);
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		curriculumService = CoreSpringFactory.getImpl(CurriculumService.class);
	}

	@Override
	public OLATResource getCourseResource() {
		return courseResource;
	}

	@Override
	public RepositoryEntry getCourseEntry() {
		if(courseRepoEntry == null) {
			courseRepoEntry = repositoryManager.lookupRepositoryEntry(courseResource, false);
		}
		return courseRepoEntry;
	}
	
	@Override
	public boolean isNotificationsAllowed() {
		RepositoryEntry re = getCourseEntry();
		if(re == null || re.getEntryStatus() == null) {
			return false;
		}
		return !re.getEntryStatus().decommissioned();
	}

	public void updateRepositoryEntry(RepositoryEntry entry) {
		courseRepoEntry = entry;
	}

	/**
	 * @param course The current course
	 * @return A course group manager that uses persisted data
	 */
	public static PersistingCourseGroupManager getInstance2(OLATResourceable course) {
		return new PersistingCourseGroupManager(course);
	}

	/**
	 * @param courseResource The current course resource
	 * @return A course group manager that uses persisted data
	 */
	public static PersistingCourseGroupManager getInstance(OLATResource courseResource) {
		return new PersistingCourseGroupManager(courseResource);
	}
	
	public static PersistingCourseGroupManager getInstance(RepositoryEntry courseRepoEntry) {
		return new PersistingCourseGroupManager(courseRepoEntry);
	}

	@Override
	public boolean hasRight(Identity identity, String courseRight, GroupRoles role) {
		return rightManager.hasBGRight(courseRight, identity, getCourseResource(), role);
	}

	@Override
	public List<String> getRights(Identity identity, GroupRoles role) {
		return rightManager.getBGRights(identity, getCourseResource(), role);
	}

	@Override
	public boolean isIdentityInGroup(Identity identity, Long groupKey) {
		return businessGroupService.isIdentityInBusinessGroup(identity, groupKey, true, true, getCourseEntry());
	}

	@Override
	public boolean isBusinessGroupFull(Long groupKey){
		boolean isLearningGroupFull = false;
		BusinessGroup group = businessGroupService.loadBusinessGroup(groupKey);
		if (group == null){
			log.warn("no groups available");
			return false;
		} else {
			// has group participants
			int members = businessGroupService.countMembers(group, GroupRoles.participant.name());
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
		return areaManager.isIdentityInBGArea(identity, null, areaKey, getCourseResource());
	}
	
	@Override
	public boolean hasBusinessGroups() {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		return businessGroupService.countBusinessGroups(params, getCourseEntry()) > 0;
	}

	@Override
	public List<BusinessGroup> getAllBusinessGroups() {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		return businessGroupService.findBusinessGroups(params, getCourseEntry(), 0, -1);
	}

	@Override
	public boolean existGroup(String nameOrKey) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		if(StringHelper.isLong(nameOrKey)) {
			try {
				params.setGroupKeys(Collections.singletonList(Long.valueOf(nameOrKey)));
			} catch (NumberFormatException e) {
				params.setExactName(nameOrKey);
			}
		} else {
			params.setExactName(nameOrKey);
		}
		return businessGroupService.countBusinessGroups(params, getCourseEntry()) > 0;
	}
	
	@Override
	public boolean hasAreas() {
		return areaManager.countBGAreasInContext(getCourseResource()) > 0;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getAllAreasFromAllContexts()
	 */
	@Override
	public List<BGArea> getAllAreas() {
		return areaManager.findBGAreasInContext(getCourseResource());
	}

	@Override
	public boolean existArea(String nameOrKey) {
		return areaManager.existArea(nameOrKey, getCourseResource());
	}

	@Override
	public List<BusinessGroup> getOwnedBusinessGroups(Identity identity) {
		if(identity == null) return new ArrayList<>();
		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams(identity, true, false);
		return businessGroupService.findBusinessGroups(params, getCourseEntry(), 0, -1);
	}

	@Override
	public List<BusinessGroup> getParticipatingBusinessGroups(Identity identity) {
		if(identity == null) return new ArrayList<>();
		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams(identity, false, true);
		return businessGroupService.findBusinessGroups(params, getCourseEntry(), 0, -1);
	}	

	@Override
	public List<CurriculumElement> getAllCurriculumElements() {
		return curriculumService.getCurriculumElements(getCourseEntry());
	}

	@Override
	public List<CurriculumElement> getCoachedCurriculumElements(Identity identity) {
		List<CurriculumRoles> coachRoles = Collections.singletonList(CurriculumRoles.coach);
		return curriculumService.getCurriculumElements(getCourseEntry(), identity, coachRoles);
	}

	@Override
	public boolean isIdentityCourseCoach(Identity identity) {
		return repositoryService.hasRoleExpanded(identity, getCourseEntry(), GroupRoles.coach.name());
	}
	
	@Override
	public boolean isIdentityCourseParticipant(Identity identity) {
		return repositoryService.hasRoleExpanded(identity, getCourseEntry(), GroupRoles.participant.name());
	}

	@Override
	public boolean isIdentityCourseAdministrator(Identity identity) {
		// not really a group management method, for your convenience we have a
		// shortcut here...
		return repositoryService.hasRoleExpanded(identity, getCourseEntry(), OrganisationRoles.administrator.name(),
				OrganisationRoles.principal.name(), OrganisationRoles.learnresourcemanager.name(), GroupRoles.owner.name());
	}

	@Override
	public boolean isIdentityInOrganisation(IdentityRef identity, String organisationIdentifier, OrganisationRoles... roles) {
		return organisationService.hasRole(organisationIdentifier, identity, roles);
	}

	@Override
	public boolean isIdentityAnyCourseAdministrator(Identity identity) {
		return repositoryService.hasRoleExpanded(identity, GroupRoles.owner.name());
	}

	@Override
	public boolean isIdentityAnyCourseCoach(Identity identity) {
		return repositoryService.hasRoleExpanded(identity, GroupRoles.coach.name());
	}

	@Override
	public boolean isIdentityAnyCourseParticipant(Identity identity) {
		return repositoryService.hasRoleExpanded(identity, GroupRoles.participant.name());
	}

	@Override
	public void deleteCourseGroupmanagement() {
		//delete permission group to course
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(getCourseResource(), false);
		if(re != null) {
			businessGroupService.removeResource(re);
			//delete areas
			List<BGArea> areas = getAllAreas();
			for(BGArea area:areas) {
				areaManager.deleteBGArea(area);
			}
			log.info(Tracing.M_AUDIT, "Deleting course groupmanagement for " + re.toString());
		}
	}

	/**
	 * @param groups List of business groups
	 * @return list of Integers that contain the number of participants for each
	 *         group
	 */
	@Override
	public List<Integer> getNumberOfMembersFromGroups(List<BusinessGroup> groups) {
		List<Integer> members = new ArrayList<>();
		for (BusinessGroup group:groups) {
			int numbMembers = businessGroupService.countMembers(group, GroupRoles.participant.name());
			members.add(Integer.valueOf(numbMembers));
		}
		return members;
	}

	@Override
	public List<String> getUniqueAreaNames() {
		List<BGArea> areas = getAllAreas();
		List<String> areaNames = new ArrayList<>();
		for (BGArea area:areas) {
			if (!areaNames.contains(area.getName())) {
				areaNames.add(area.getName().trim());
			}
		}
		Collections.sort(areaNames);
		return areaNames;
	}

	@Override
	public List<String> getUniqueBusinessGroupNames() {
		List<BusinessGroup> groups = getAllBusinessGroups();
		List<String> groupNames = new ArrayList<>();
		for (BusinessGroup group:groups) {
			if (!groupNames.contains(group.getName())) {
				groupNames.add(group.getName().trim());
			}
		}
		Collections.sort(groupNames);
		return groupNames;
	}

	@Override
	public void exportCourseBusinessGroups(File fExportDirectory, CourseEnvironmentMapper courseEnv) {
		File fExportFile = new File(fExportDirectory, LEARNINGGROUPEXPORT_XML);
		List<BGArea> areas = getAllAreas();
		List<BusinessGroup> groups = getAllBusinessGroups();
		List<BusinessGroup> exportedGroups = new ArrayList<>();
		for(BusinessGroup group:groups) {
			if(!LTI13Service.LTI_GROUP_TYPE.equals(group.getTechnicalType())) {
				exportedGroups.add(group);
			}
		}
		businessGroupService.exportGroups(exportedGroups, areas, fExportFile);
	}
	
	/**
	 * This operation load all business groups and areas. Use with caution, costly!
	 * @param resource
	 * @param fGroupExportXML
	 * @return
	 */
	@Override
	public CourseEnvironmentMapper getBusinessGroupEnvironment() {
		CourseEnvironmentMapper env = new CourseEnvironmentMapper(getCourseEntry(), getCourseEntry());
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(null, getCourseEntry(), 0, -1);
		for(BusinessGroup group:groups) {
			env.getGroups().add(new BusinessGroupReference(group));
		}
		List<BGArea> areas = areaManager.findBGAreasInContext(getCourseResource());
		for(BGArea area:areas) {
			env.getAreas().add(new BGAreaReference(area));
		}
		return env;
	}

	@Override
	public CourseEnvironmentMapper importCourseBusinessGroups(File fImportDirectory) {
		CourseEnvironmentMapper envMapper = new CourseEnvironmentMapper(getCourseEntry(), getCourseEntry());
		OLATResource resource = getCourseResource();
		RepositoryEntry courseRe = repositoryManager.lookupRepositoryEntry(resource, true);
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

	@Override
	public List<Identity> getCoachesFromBusinessGroups() {
		List<BusinessGroup> bgs = getAllBusinessGroups();
		return businessGroupService.getMembers(bgs, GroupRoles.coach.name());
	}

	@Override
	public List<Identity> getParticipantsFromBusinessGroups() {
		List<BusinessGroup> bgs = getAllBusinessGroups();
		return businessGroupService.getMembers(bgs, GroupRoles.participant.name());
	}
	
	@Override
	public List<Identity> getCoachesFromBusinessGroups(List<Long> groupKeys) {
		List<BusinessGroup> bgs = businessGroupService.loadBusinessGroups(groupKeys);
		return businessGroupService.getMembers(bgs, GroupRoles.coach.name());
	}
	
	@Override
	public List<Identity> getParticipantsFromBusinessGroups(List<Long> groupKeys) {
		List<BusinessGroup> bgs = businessGroupService.loadBusinessGroups(groupKeys);
		return businessGroupService.getMembers(bgs, GroupRoles.participant.name());
	}

	@Override
	public List<Identity> getCoaches() {
		return repositoryService.getMembers(getCourseEntry(), RepositoryEntryRelationType.entryAndCurriculums, GroupRoles.coach.name());
	}

	@Override
	public List<Identity> getParticipants() {
		return repositoryService.getMembers(getCourseEntry(), RepositoryEntryRelationType.entryAndCurriculums, GroupRoles.participant.name());
	}

	@Override
	public List<Identity> getCoachesFromAreas() {
		List<BusinessGroup> bgs = getAllBusinessGroups();
		return businessGroupService.getMembers(bgs, GroupRoles.coach.name());
	}
	
	@Override
	public List<Identity> getCoachesFromAreas(List<Long> areaKeys) {
		List<BGArea> areas = areaManager.loadAreas(areaKeys);
		List<BusinessGroup> groups = areaManager.findBusinessGroupsOfAreas(areas);
		return businessGroupService.getMembers(groups, GroupRoles.coach.name());
	}

	@Override
	public List<Identity> getCoachesFromCurriculumElements() {
		return repositoryService.getMembers(getCourseEntry(), RepositoryEntryRelationType.curriculums, GroupRoles.coach.name());
	}

	@Override
	public List<Identity> getCoachesFromCurriculumElements(List<Long> curriculumElementKeys) {
		return curriculumService.getMembersIdentity(curriculumElementKeys, CurriculumRoles.coach);
	}

	@Override
	public List<Identity> getParticipantsFromAreas() {
		List<BusinessGroup> bgs = getAllBusinessGroups();
		return businessGroupService.getMembers(bgs, GroupRoles.participant.name());
	}

	@Override
	public List<Identity> getParticipantsFromAreas(List<Long> areaKeys) {
		List<BGArea> areas = areaManager.loadAreas(areaKeys);
		List<BusinessGroup> groups = areaManager.findBusinessGroupsOfAreas(areas);
		return businessGroupService.getMembers(groups, GroupRoles.participant.name());
	}

	@Override
	public List<Identity> getParticipantsFromCurriculumElements() {
		return repositoryService.getMembers(getCourseEntry(), RepositoryEntryRelationType.curriculums, GroupRoles.participant.name());
	}

	@Override
	public List<Identity> getParticipantsFromCurriculumElements(List<Long> curriculumElementKeys) {
		return curriculumService.getMembersIdentity(curriculumElementKeys, CurriculumRoles.participant);
	}

	@Override
	public List<BusinessGroup> getWaitingListGroups(Identity identity) {
		return businessGroupService.findBusinessGroupsWithWaitingListAttendedBy(identity, getCourseEntry());
	}

	/**
	 * Archive all learning-group-contexts and right-group-contexts.
	 * @param exportDirectory
	 */
	@Override
	public void archiveCourseGroups(File exportDirectory) {
		File exportLearningGroupFile = new File(exportDirectory, "default_" + LEARNINGGROUPARCHIVE_XLS);
		businessGroupService.archiveGroups(getAllBusinessGroups(), exportLearningGroupFile);
	}
}