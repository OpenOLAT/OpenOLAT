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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.commons.memberlist.ui.MembersTableController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.nodes.members.Member;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupService;
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
	private RepositoryManager repositoryManager;
	@Autowired
	protected BaseSecurity securityManager;
	@Autowired
	private BusinessGroupService businessGroupService;	
	@Autowired
	private UserManager userManager;
	
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
			Translator translator, List<UserPropertyHandler> userPropertyHandlers, RepositoryEntry repoEntry, BusinessGroup businessGroup) {//TODO
		Map<Long,BusinessGroupMembership> groupmemberships;
		Map<Long,RepositoryEntryMembership> repomemberships;
		if (repoEntry == null) {
			List<BusinessGroup> groups = new ArrayList<>(); 
			groups.add(businessGroup);
			groupmemberships = getGroupMembershipMap(groups);
			repomemberships = new HashMap<>();
		} else {
			repomemberships = getRepoMembershipMap(repoEntry);
			List<BusinessGroup> groups = businessGroupService.findBusinessGroups(null, repoEntry, 0, -1);
			groupmemberships = getGroupMembershipMap(groups);
		}	
		
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
				if (repomemberships != null && !repomemberships.isEmpty() && repomemberships.containsKey(memberKey)) {
					RepositoryEntryMembership repomembership = repomemberships.get(memberKey);
					if (repomembership.isOwner()) {
						putRoleToMember(rows, membersMap, member, "owners", repoTranslator);
					}
					if (repomembership.isCoach()) {
						putRoleToMember(rows, membersMap, member, "coaches", repoTranslator);
					} 
					if (repomembership.isParticipant()) {
						putRoleToMember(rows, membersMap, member, "participants", repoTranslator);
					}
				}
				if (groupmemberships != null && !groupmemberships.isEmpty() && groupmemberships.containsKey(memberKey)) {
					BusinessGroupMembership groupmembership = groupmemberships.get(memberKey);
					if (groupmembership.isOwner()) {
						putRoleToMember(rows, membersMap, member, "coaches", groupTranslator);
					}
					if (groupmembership.isParticipant()) {
						putRoleToMember(rows, membersMap, member, "participants", groupTranslator);
					} 
					if (groupmembership.isWaiting()) {
						putRoleToMember(rows, membersMap, member, "waiting", groupTranslator);
					}
				}
			}			
		}
		
		Translator handlerTranslator = userManager.getPropertyHandlerTranslator(translator);
		XlsMembersExport exporter = new XlsMembersExport();
		MediaResource resource = exporter.export(rows, membersMap, handlerTranslator, userPropertyHandlers);		
		
		return resource;
	}

}
