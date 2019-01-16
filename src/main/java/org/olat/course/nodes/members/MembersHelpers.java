/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.members;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.MembersCourseNode;
import org.olat.group.BusinessGroupService;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;

/**
 * 
 * <p>Initial date: May 20, 2016
 * @author lmihalkovic, http://www.frentix.com
 */
public class MembersHelpers {
	private MembersHelpers() {
		// CANNOT CREATE
	}
	
	private static void deduplicateList(List<Identity> list) {
		if(list == null || list.size() < 2) return;
		
		Set<Identity> deduplicates = new HashSet<>();
		for(Iterator<Identity> it=list.iterator(); it.hasNext(); ) {
			Identity identity = it.next();
			if(deduplicates.contains(identity)) {
				it.remove();
			} else {
				deduplicates.add(identity);
			}
		}
	}

	// -----------------------------------------------------
	
	public static List<Identity> getOwners(RepositoryService repositoryService, RepositoryEntry courseRepositoryEntry) {
		List<Identity> owners = repositoryService.getMembers(courseRepositoryEntry, RepositoryEntryRelationType.all, GroupRoles.owner.name());
		deduplicateList(owners);
		return owners;
	}

	// -----------------------------------------------------

	public static List<Identity> getCoaches(ModuleConfiguration moduleConfiguration, CourseGroupManager cgm, BusinessGroupService bgs) {
		List<Identity> list = new ArrayList<>();
		if(moduleConfiguration.has(MembersCourseNode.CONFIG_KEY_COACHES_GROUP)) {
			String coachGroupNames = moduleConfiguration.getStringValue(MembersCourseNode.CONFIG_KEY_COACHES_GROUP);
			List<Long> coachGroupKeys = moduleConfiguration.getList(MembersCourseNode.CONFIG_KEY_COACHES_GROUP_ID, Long.class);
			if(coachGroupKeys == null && StringHelper.containsNonWhitespace(coachGroupNames)) {
				coachGroupKeys = bgs.toGroupKeys(coachGroupNames, cgm.getCourseEntry());
			}
			list.addAll(retrieveCoachesFromGroups(coachGroupKeys, cgm));
		}

		if(moduleConfiguration.has(MembersCourseNode.CONFIG_KEY_COACHES_AREA)) {
			String coachAreaNames = moduleConfiguration.getStringValue(MembersCourseNode.CONFIG_KEY_COACHES_AREA);
			List<Long> coachAreaKeys = moduleConfiguration.getList(MembersCourseNode.CONFIG_KEY_COACHES_AREA_IDS, Long.class);
			if(coachAreaKeys == null && StringHelper.containsNonWhitespace(coachAreaNames)) {
				coachAreaKeys = bgs.toGroupKeys(coachAreaNames, cgm.getCourseEntry());
			}
			list.addAll(retrieveCoachesFromAreas(coachAreaKeys, cgm));
		}
		
		if(moduleConfiguration.has(MembersCourseNode.CONFIG_KEY_COACHES_CUR_ELEMENT)) {
			List<Long> coachElementKeys = moduleConfiguration.getList(MembersCourseNode.CONFIG_KEY_COACHES_CUR_ELEMENT_ID, Long.class);
			list.addAll(retrieveCoachesFromCurriculumElements(coachElementKeys, cgm));
		}
		
		if(moduleConfiguration.anyTrue(MembersCourseNode.CONFIG_KEY_COACHES_COURSE, MembersCourseNode.CONFIG_KEY_COACHES_ALL)) {
			list.addAll(retrieveCoachesFromCourse(cgm));
		}
		if(moduleConfiguration.anyTrue(MembersCourseNode.CONFIG_KEY_COACHES_ALL)) {
			list.addAll(retrieveCoachesFromCourseGroups(cgm));
		}
		
		deduplicateList(list);
		return list;
	}
	
	private static List<Identity> retrieveCoachesFromAreas(List<Long> areaKeys, CourseGroupManager cgm) {
		List<Identity> coaches = cgm.getCoachesFromAreas(areaKeys);
		return new ArrayList<>(new HashSet<>(coaches));
	}
	
	private static List<Identity> retrieveCoachesFromCurriculumElements(List<Long> elementKeys, CourseGroupManager cgm) {
		List<Identity> coaches = cgm.getCoachesFromCurriculumElements(elementKeys);
		return new ArrayList<>(new HashSet<>(coaches));
	}

	
	private static List<Identity> retrieveCoachesFromGroups(List<Long> groupKeys, CourseGroupManager cgm) {
		return new ArrayList<>(new HashSet<>(cgm.getCoachesFromBusinessGroups(groupKeys)));
	}
	
	private static List<Identity> retrieveCoachesFromCourse(CourseGroupManager cgm) {
		return cgm.getCoaches();
	}

	private static List<Identity> retrieveCoachesFromCourseGroups(CourseGroupManager cgm) {
		Set<Identity> uniq = new HashSet<>();
		uniq.addAll(cgm.getCoachesFromAreas());
		uniq.addAll(cgm.getCoachesFromBusinessGroups());
		uniq.addAll(cgm.getCoachesFromCurriculumElements());
		return new ArrayList<>(uniq);
	}
	
	// -----------------------------------------------------
	
	public static List<Identity> getParticipants(ModuleConfiguration moduleConfiguration, CourseGroupManager cgm, BusinessGroupService bgs) {
		List<Identity> list = new ArrayList<>();

		if(moduleConfiguration.has(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_GROUP)) {
			String participantGroupNames = moduleConfiguration.getStringValue(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_GROUP);
			List<Long> participantGroupKeys = moduleConfiguration.getList(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_GROUP_ID, Long.class);
			if(participantGroupKeys == null && StringHelper.containsNonWhitespace(participantGroupNames)) {
				participantGroupKeys = bgs.toGroupKeys(participantGroupNames, cgm.getCourseEntry());
			}
			list.addAll(retrieveParticipantsFromGroups(participantGroupKeys, cgm));
		}
		
		if(moduleConfiguration.has(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_AREA)) {
			String participantAreaNames = moduleConfiguration.getStringValue(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_AREA);
			List<Long> participantAreaKeys = moduleConfiguration.getList(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_AREA_ID, Long.class);
			if(participantAreaKeys == null && StringHelper.containsNonWhitespace(participantAreaNames)) {
				participantAreaKeys = bgs.toGroupKeys(participantAreaNames, cgm.getCourseEntry());
			}
			list.addAll(retrieveParticipantsFromAreas(participantAreaKeys, cgm));
		}
		
		if(moduleConfiguration.has(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_CUR_ELEMENT)) {
			List<Long> coachElementKeys = moduleConfiguration.getList(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_CUR_ELEMENT_ID, Long.class);
			list.addAll(retrieveParticipantsFromCurriculumElements(coachElementKeys, cgm));
		}
		
		if(moduleConfiguration.anyTrue(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_COURSE, MembersCourseNode.CONFIG_KEY_PARTICIPANTS_ALL)) {
			list.addAll(retrieveParticipantsFromCourse(cgm));
		}
		
		if(moduleConfiguration.anyTrue(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_ALL)) {
			list.addAll(retrieveParticipantsFromCourseGroups(cgm));
		}
		deduplicateList(list);
		return list;
	}
	
	private static List<Identity> retrieveParticipantsFromAreas(List<Long> areaKeys, CourseGroupManager cgm) {
		return cgm.getParticipantsFromAreas(areaKeys);
	}
	
	private static List<Identity> retrieveParticipantsFromGroups(List<Long> groupKeys, CourseGroupManager cgm) {
		return cgm.getParticipantsFromBusinessGroups(groupKeys);
	}
	
	private static List<Identity> retrieveParticipantsFromCurriculumElements(List<Long> elementKeys, CourseGroupManager cgm) {
		return cgm.getParticipantsFromCurriculumElements(elementKeys);
	}
	
	private static List<Identity> retrieveParticipantsFromCourse(CourseGroupManager cgm) {
		return cgm.getParticipants();
	}
	
	public static List<Identity> retrieveParticipantsFromCourseGroups(CourseGroupManager cgm) {
		Set<Identity> uniq = new HashSet<>();
		uniq.addAll(cgm.getParticipantsFromAreas());
		uniq.addAll(cgm.getParticipantsFromBusinessGroups());
		uniq.addAll(cgm.getParticipantsFromCurriculumElements());
		return new ArrayList<>(uniq);
	}
}
