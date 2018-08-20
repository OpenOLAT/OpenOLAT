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
package org.olat.commons.memberlist.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.commons.memberlist.ui.MembersTableController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.nodes.members.Member;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.MemberViewQueries;
import org.olat.group.model.MemberView;
import org.olat.group.ui.main.CourseMembership;
import org.olat.group.ui.main.SearchMembersParams;
import org.olat.group.ui.run.GroupMembersRunController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntryMembership;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Initial Date: 28.03.2017
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
@Service
public class MembersExportManager {
	
	public static final String USER_PROPS_ID = MembersTableController.class.getName();
	
	public static final int USER_PROPS_OFFSET = 500;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private MemberViewQueries memberQueries;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	protected BaseSecurity securityManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	public Map<Long,RepositoryEntryMembership> getRepoMembershipMap(RepositoryEntry repoEntry) {
		List<RepositoryEntryMembership> repoMemberships = repositoryManager.getRepositoryEntryMembership(repoEntry);
		Map<Long,RepositoryEntryMembership> map = new HashMap<>();
		for (RepositoryEntryMembership membership : repoMemberships) {
			map.put(membership.getIdentityKey(), membership);
		}
		return map;
	}
	
	public Map<Long,BusinessGroupMembership> getGroupMembershipMap(List<BusinessGroup> groups) {
		List<BusinessGroupMembership> groupMemberships = businessGroupService.getBusinessGroupsMembership(groups);
		Map<Long,BusinessGroupMembership> map = new HashMap<>();
		for (BusinessGroupMembership membership : groupMemberships) {
			map.put(membership.getIdentityKey(), membership);
		}
		return map;
	}
	
	private void putRoleToMember(List<Identity> rows, Map<Identity, StringBuilder> membersMap, Identity member, String role, Translator roleTranslator) {
		if (membersMap.containsKey(member)) {
			String roleString = roleTranslator.translate("members." + role);
			StringBuilder roleBuilder = membersMap.get(member);
			if (!roleBuilder.toString().contains(roleString)) {
				roleBuilder.append(", ").append(roleString);				
			}
		} else {
			membersMap.put(member, new StringBuilder(roleTranslator.translate("members." + role)));
			rows.add(member);
		}
	}
	
	public MediaResource getXlsMediaResource(boolean showOwners, boolean showCoaches, boolean showParticipants, boolean showWaiting, 
			List<Identity> owners, List<Identity> coaches, List<Identity> participants, List<Identity> waiting,			
			Translator translator, List<UserPropertyHandler> userPropertyHandlers, RepositoryEntry repoEntry, BusinessGroup businessGroup) {
		List<MemberView> memberViews;
		SearchMembersParams params = new SearchMembersParams();
		params.setRoles(new GroupRoles[] { GroupRoles.owner, GroupRoles.coach, GroupRoles.participant, GroupRoles.waiting});
		if(repoEntry != null) {
			memberViews = memberQueries.getRepositoryEntryMembers(repoEntry, params, userPropertyHandlers, translator.getLocale());
		} else if(businessGroup != null) {
			memberViews = memberQueries.getBusinessGroupMembers(businessGroup, params, userPropertyHandlers, translator.getLocale());
		} else {
			memberViews = Collections.emptyList();
		}
		Map<Long,MemberView> memberViewsMap = memberViews.stream()
				.collect(Collectors.toMap(MemberView::getIdentityKey, m -> m, (u,v) -> u));
		
		List<List<Identity>> roleMembers = new ArrayList<>();
		if (showOwners) {
			roleMembers.add(owners);
		}
		if (showCoaches) {
			roleMembers.add(coaches);
		}
		if (showParticipants) {
			roleMembers.add(participants);
		}
		if (showWaiting) {
			roleMembers.add(waiting);
		}
		Translator repoTranslator = Util.createPackageTranslator(Member.class, translator.getLocale());
		Translator groupTranslator = Util.createPackageTranslator(GroupMembersRunController.class, translator.getLocale());
		
		Map<Identity, StringBuilder> membersMap = new HashMap<>();
		List<Identity> rows = new ArrayList<>();
		for (List<Identity> membersList : roleMembers) {
			for (Identity member : membersList) {
				Long memberKey = member.getKey();
				MemberView memberView = memberViewsMap.get(memberKey);
				if(memberView != null) {
					CourseMembership membership = memberView.getMemberShip();
					// business groups
					if (membership.isBusinessGroupCoach()) {
						putRoleToMember(rows, membersMap, member, "coaches", groupTranslator);
					}
					if (membership.isBusinessGroupParticipant()) {
						putRoleToMember(rows, membersMap, member, "participants", groupTranslator);
					} 
					if (membership.isWaiting()) {
						putRoleToMember(rows, membersMap, member, "waiting", groupTranslator);
					}
					
					// course
					if (membership.isRepositoryEntryOwner()) {
						putRoleToMember(rows, membersMap, member, "owners", repoTranslator);
					}
					if (membership.isRepositoryEntryCoach()) {
						putRoleToMember(rows, membersMap, member, "coaches", repoTranslator);
					} 
					if (membership.isRepositoryEntryParticipant()) {
						putRoleToMember(rows, membersMap, member, "participants", repoTranslator);
					}
					
					// curriculum
					if (membership.isCurriculumElementOwner()) {
						putRoleToMember(rows, membersMap, member, "curriculum.owners", repoTranslator);
					}
					if (membership.isCurriculumElementCoach()) {
						putRoleToMember(rows, membersMap, member, "curriculum.coaches", repoTranslator);
					} 
					if (membership.isCurriculumElementParticipant()) {
						putRoleToMember(rows, membersMap, member, "curriculum.participants", repoTranslator);
					}
				}
			}			
		}
		
		Translator handlerTranslator = userManager.getPropertyHandlerTranslator(translator);
		XlsMembersExport exporter = new XlsMembersExport();
		return exporter.export(rows, membersMap, handlerTranslator, userPropertyHandlers);		
	}
}
