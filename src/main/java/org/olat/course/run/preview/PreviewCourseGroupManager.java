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

package org.olat.course.run.preview;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;

/**
 * Initial Date:  08.02.2005
 *
 * @author Mike Stock
 */
final class PreviewCourseGroupManager implements CourseGroupManager {

	private List<BGArea> areas;
	private List<BusinessGroup> groups;
	
	private RepositoryEntry courseResource;
	private boolean isCoach;
	private boolean isCourseAdmin;
	
	private final BGAreaManager areaManager;
	private final RepositoryService repositoryService;
	private final CurriculumService curriculumService;
	private final BusinessGroupService businessGroupService;
	
	/**
	 * @param groups
	 * @param areas
	 * @param isCoach
	 * @param isCourseAdmin
	 */
	public PreviewCourseGroupManager(RepositoryEntry courseResource, List<BusinessGroup> groups,
			List<BGArea> areas, boolean isCoach, boolean isCourseAdmin) {
		this.courseResource = courseResource;
		this.groups = groups;
		this.areas = areas;
		this.isCourseAdmin = isCourseAdmin;
		this.isCoach = isCoach;

		areaManager = CoreSpringFactory.getImpl(BGAreaManager.class);
		repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		curriculumService = CoreSpringFactory.getImpl(CurriculumService.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
	}

	@Override
	public OLATResource getCourseResource() {
		return courseResource.getOlatResource();
	}

	@Override
	public RepositoryEntry getCourseEntry() {
		return courseResource;
	}

	@Override
	public boolean isNotificationsAllowed() {
		RepositoryEntry re = getCourseEntry();
		if(re == null || re.getEntryStatus() == null) {
			return false;
		}
		RepositoryEntryStatusEnum status = re.getEntryStatus();
		return status != RepositoryEntryStatusEnum.closed
				&& status != RepositoryEntryStatusEnum.trash
				&& status != RepositoryEntryStatusEnum.deleted;
	}

	@Override
	public boolean hasRight(Identity identity, String courseRight) {
		return !courseRight.equals(CourseRights.RIGHT_COURSEEDITOR);
	}

	@Override
	public List<String> getRights(Identity identity) {
		return new ArrayList<>(1);
	}

	@Override
	public boolean isIdentityInGroup(Identity identity, Long groupKey) {
		for(BusinessGroup group:groups) {
			if(groupKey.equals(group.getKey())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isBusinessGroupFull(Long groupKey){
		for(BusinessGroup group:groups) {
			if(groupKey.equals(group.getKey())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isIdentityInLearningArea(Identity identity, Long areaKey) {
		for(BGArea area:areas) {
			if(areaKey.equals(area.getKey())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean existGroup(String nameOrKey) {
		for(BusinessGroup group:groups) {
			if(nameOrKey.equals(group.getName()) || nameOrKey.equals(group.getKey().toString())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean existArea(String nameOrKey) {
		for(BGArea area:areas) {
			if(nameOrKey.equals(area.getName()) || nameOrKey.equals(area.getKey().toString())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isIdentityCourseCoach(Identity identity) {
		return isCoach;
	}

	@Override
	public boolean isIdentityCourseParticipant(Identity identity) {
		return false;
	}

	@Override
	public boolean isIdentityCourseAdministrator(Identity identity) {
		return isCourseAdmin;
	}

	@Override
	public boolean isIdentityAnyCourseCoach(Identity identity) {
		return isCoach;
	}

	@Override
	public boolean isIdentityAnyCourseAdministrator(Identity identity) {
		return isCourseAdmin;
	}

	@Override
	public boolean isIdentityAnyCourseParticipant(Identity identity) {
		return false;
	}
	
	@Override
	public boolean isIdentityInOrganisation(IdentityRef identity, String organisationIdentifier, OrganisationRoles... roles) {
		return false;
	}

	@Override
	public boolean hasBusinessGroups() {
		return groups != null && !groups.isEmpty();
	}

	@Override
	public List<BusinessGroup> getAllBusinessGroups() {
		return groups;
	}

	@Override
	public List<BusinessGroup> getOwnedBusinessGroups(Identity identity) {
		return new ArrayList<>(1);
	}

	@Override
	public List<BusinessGroup> getParticipatingBusinessGroups(Identity identity) {
		return new ArrayList<>(1);
	}
	
	@Override
	public List<CurriculumElement> getAllCurriculumElements() {
		return new ArrayList<>(1);
	}

	@Override
	public List<CurriculumElement> getCoachedCurriculumElements(Identity identity) {
		return new ArrayList<>(1);
	}

	@Override
	public boolean hasAreas() {
		return areas != null && !areas.isEmpty();
	}

	@Override
	public List<BGArea> getAllAreas() {
		return areas;
	}

	@Override
	public void deleteCourseGroupmanagement() {
		//do nothing in preview
	}

	@Override
	public List<Integer> getNumberOfMembersFromGroups(List<BusinessGroup> groupList) {
		List<Integer> members = new ArrayList<>();
		for (BusinessGroup group:groups) {
			int numbMembers = businessGroupService.countMembers(group, GroupRoles.participant.name());
			members.add(Integer.valueOf(numbMembers));
		}
		return members;
	}

	@Override
	public List<String> getUniqueBusinessGroupNames() {
		List<String> names = new ArrayList<>();
		if(groups != null) {
			for (BusinessGroup group:groups) {
				if (!names.contains(group.getName())) {
					names.add(group.getName().trim());
				}
			}
			Collections.sort(names);
		}
		return names;
	}

	@Override
	public List<String> getUniqueAreaNames() {
		List<String> areaNames = new ArrayList<>();
		if(areas != null) {
			for (BGArea area:areas) {
				if (!areaNames.contains(area.getName())) {
					areaNames.add(area.getName().trim());
				}
			}
			Collections.sort(areaNames);
		}
		return areaNames;
	}

	@Override
	public List<Identity> getCoachesFromBusinessGroups() {
		return businessGroupService.getMembers(groups, GroupRoles.coach.name());
	}

	@Override
	public List<Identity> getCoachesFromAreas() {
		List<BusinessGroup> groupList = areaManager.findBusinessGroupsOfAreas(areas);
		return businessGroupService.getMembers(groupList, GroupRoles.coach.name());
	}
	
	@Override
	public List<Identity> getCoachesFromCurriculumElements() {
		return repositoryService.getMembers(getCourseEntry(), RepositoryEntryRelationType.curriculums, GroupRoles.coach.name());
	}

	@Override
	public List<Identity> getParticipantsFromBusinessGroups() {
		return businessGroupService.getMembers(groups, GroupRoles.participant.name());
	}

	@Override
	public List<Identity> getCoachesFromBusinessGroups(List<Long> groupKeys) {
		List<BusinessGroup> bgs = businessGroupService.loadBusinessGroups(groupKeys);
		return businessGroupService.getMembers(bgs, GroupRoles.coach.name());
	}

	@Override
	public List<Identity> getCoachesFromAreas(List<Long> areaKeys) {
		List<BGArea> areaList = areaManager.loadAreas(areaKeys);
		List<BusinessGroup> groupList = areaManager.findBusinessGroupsOfAreas(areaList);
		return businessGroupService.getMembers(groupList, GroupRoles.coach.name());
	}

	@Override
	public List<Identity> getParticipantsFromBusinessGroups(List<Long> groupKeys) {
		return businessGroupService.getMembers(groups, GroupRoles.participant.name());
	}

	@Override
	public List<Identity> getParticipantsFromAreas(List<Long> areaKeys) {
		List<BGArea> areaList = areaManager.loadAreas(areaKeys);
		List<BusinessGroup> groupList = areaManager.findBusinessGroupsOfAreas(areaList);
		return businessGroupService.getMembers(groupList, GroupRoles.participant.name());
	}

	@Override
	public List<Identity> getCoachesFromCurriculumElements(List<Long> curriculumElementKeys) {
		return curriculumService.getMembersIdentity(curriculumElementKeys, CurriculumRoles.coach);
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
	public List<Identity> getParticipantsFromAreas() {
		return businessGroupService.getMembers(groups, GroupRoles.participant.name());
	}
	
	@Override
	public List<Identity> getCoaches() {
		return repositoryService.getMembers(getCourseEntry(), RepositoryEntryRelationType.defaultGroup, GroupRoles.coach.name());
	}

	@Override
	public List<Identity> getParticipants() {
		return repositoryService.getMembers(getCourseEntry(), RepositoryEntryRelationType.defaultGroup, GroupRoles.participant.name());
	}

	@Override
	public CourseEnvironmentMapper getBusinessGroupEnvironment() {
		throw new AssertException("unsupported");
	}

	@Override
	public void exportCourseBusinessGroups(File fExportDirectory, CourseEnvironmentMapper env) {
		throw new AssertException("unsupported");
	}

	@Override
	public CourseEnvironmentMapper importCourseBusinessGroups(File fImportDirectory) {
		throw new AssertException("unsupported");
	}

	@Override
	public void archiveCourseGroups(File exportDirectory) {
		//
	}

	@Override
	public List<BusinessGroup> getWaitingListGroups(Identity identity) {
		return new ArrayList<>(1);
	}
}