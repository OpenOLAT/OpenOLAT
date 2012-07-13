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
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.manager.BasicManager;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.group.BusinessGroup;
import org.olat.group.area.BGArea;
import org.olat.resource.OLATResource;

/**
 * Initial Date:  08.02.2005
 *
 * @author Mike Stock
 */
final class PreviewCourseGroupManager extends BasicManager implements CourseGroupManager {

	private List<BusinessGroup> groups;
	private List<BGArea> areas;
	private boolean isCoach, isCourseAdmin;
	
	/**
	 * @param groups
	 * @param areas
	 * @param isCoach
	 * @param isCourseAdmin
	 */
	public PreviewCourseGroupManager(List<BusinessGroup> groups, List<BGArea> areas, boolean isCoach, boolean isCourseAdmin) {
		this.groups = groups;
		this.areas = areas;
		this.isCourseAdmin = isCourseAdmin;
		this.isCoach = isCoach;
	}

	@Override
	public OLATResource getCourseResource() {
		return null;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#hasRight(org.olat.core.id.Identity, java.lang.String)
	 */
	public boolean hasRight(Identity identity, String courseRight) {
		if (courseRight.equals(CourseRights.RIGHT_COURSEEDITOR)) {
			return false;
		}
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityInLearningGroup(org.olat.core.id.Identity, java.lang.String)
	 */
	public boolean isIdentityInGroup(Identity identity, String groupName) {
		for(BusinessGroup group:groups) {
			if(groupName.equals(group.getName())) {
				return true;
			}
		}
		return false;
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

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isLearningGroupFull(java.lang.String)
	 */
	public boolean isLearningGroupFull(String groupName){
		for(BusinessGroup group:groups) {
			if(groupName.equals(group.getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityInLearningArea(org.olat.core.id.Identity, java.lang.String)
	 */
	public boolean isIdentityInLearningArea(Identity identity, String areaName) {
		for(BGArea area:areas) {
			if(areaName.equals(area.getName())) {
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

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityCourseCoach(org.olat.core.id.Identity)
	 */
	public boolean isIdentityCourseCoach(Identity identity) {
		return isCoach;
	}

	@Override
	public boolean isIdentityCourseParticipant(Identity identity) {
		return false;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityCourseAdministrator(org.olat.core.id.Identity)
	 */
	public boolean isIdentityCourseAdministrator(Identity identity) {
		return isCourseAdmin;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getAllLearningGroupsFromAllContexts()
	 */
	public List<BusinessGroup> getAllBusinessGroups() {
		return groups;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getBusinessGroups(java.lang.String)
	 */
	public List<BusinessGroup> getBusinessGroups(String groupName) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getBusinessGroupsInArea(java.lang.String)
	 */
	public List<BusinessGroup> getBusinessGroupsInArea(String areaName) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipatingBusinessGroups(org.olat.core.id.Identity, java.lang.String)
	 */
	public List<BusinessGroup> getParticipatingBusinessGroups(Identity identity, String groupName) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipatingBusinessGroupsInArea(org.olat.core.id.Identity, java.lang.String)
	 */
	public List<BusinessGroup> getParticipatingBusinessGroupsInArea(Identity identity, String araName) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getOwnedBusinessGroups(org.olat.core.id.Identity)
	 */
	public List<BusinessGroup> getOwnedBusinessGroups(Identity identity) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipatingBusinessGroups(org.olat.core.id.Identity)
	 */
	public List<BusinessGroup> getParticipatingBusinessGroups(Identity identity) {
		throw new AssertException("unsupported");
	}
	
	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getAllAreasFromAllContexts()
	 */
	@Override
	public List<BGArea> getAllAreas() {
		return areas;
	}

	@Override
	public List<BGArea> getAreas(String areaname) {
		return areas;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#deleteCourseGroupmanagement()
	 */
	public void deleteCourseGroupmanagement() {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getNumberOfMembersFromGroups(java.util.List)
	 */
	public List<Integer> getNumberOfMembersFromGroups(List<BusinessGroup> groupList) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getUniqueBusinessGroupNames()
	 */
	public List<String> getUniqueBusinessGroupNames() {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getUniqueAreaNames()
	 */
	public List<String> getUniqueAreaNames() {
		throw new AssertException("unsupported");
	}
	
	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getAreasOfBusinessGroup(java.lang.String)
	 */
  public List<BGArea> getAreasOfBusinessGroup(String groupName) {
		throw new AssertException("unsupported");
  }

	public List<Identity> getCoachesFromBusinessGroup(String groupName) {
		throw new AssertException("unsupported");
	}

	public List<Identity> getCoachesFromArea(String areaName) {
		throw new AssertException("unsupported");
	}

	public List<Identity> getParticipantsFromBusinessGroup(String groupName) {
		throw new AssertException("unsupported");
	}

	@Override
	public List<Identity> getCoachesFromBusinessGroups(List<Long> groupKeys) {
		throw new AssertException("unsupported");
	}

	@Override
	public List<Identity> getCoachesFromAreas(List<Long> areaKeys) {
		throw new AssertException("unsupported");
	}

	@Override
	public List<Identity> getParticipantsFromBusinessGroups(List<Long> groupKeys) {
		throw new AssertException("unsupported");
	}

	@Override
	public List<Identity> getParticipantsFromAreas(List<Long> areaKeys) {
		throw new AssertException("unsupported");
	}

	public List<Identity> getParticipantsFromArea(String areaName) {
		throw new AssertException("unsupported");
	}
	
	//fxdiff VCRP-1,2: access control of resources
	@Override
	public List<Identity> getCoaches() {
		throw new AssertException("unsupported");
	}

	@Override
	public List<Identity> getParticipants() {
		throw new AssertException("unsupported");
	}

	public List<BusinessGroup> getRightGroupsFromAllContexts(String groupName) {
		throw new AssertException("unsupported");
	}

	public void exportCourseRightGroups(File fExportDirectory) {
		throw new AssertException("unsupported");
	}

	public void importCourseRightGroups(File fImportDirectory) {
		throw new AssertException("unsupported");
	}

	public void exportCourseBusinessGroups(File fExportDirectory) {
		throw new AssertException("unsupported");
	}

	public void importCourseBusinessGroups(File fImportDirectory) {
		throw new AssertException("unsupported");
	}

	public List<BusinessGroup> getWaitingListGroups(Identity identity) {
		throw new AssertException("unsupported");
	}

}