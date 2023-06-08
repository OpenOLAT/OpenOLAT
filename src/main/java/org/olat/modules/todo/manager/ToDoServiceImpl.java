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
package org.olat.modules.todo.manager;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.TagService;
import org.olat.core.id.Identity;
import org.olat.core.util.date.DateModule;
import org.olat.modules.todo.ToDoExpenditureOfWork;
import org.olat.modules.todo.ToDoProvider;
import org.olat.modules.todo.ToDoRole;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskMembers;
import org.olat.modules.todo.ToDoTaskRef;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskTag;
import org.olat.modules.todo.model.ToDoExpenditureOfWorkImpl;
import org.olat.modules.todo.model.ToDoTaskMembersImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 24 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ToDoServiceImpl implements ToDoService {
	
	@Autowired
	private ToDoTaskDAO toDoTaskDao;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	protected BaseSecurity securityManager;
	@Autowired
	private TagService tagService;
	@Autowired
	private ToDoTaskTagDAO tagDao;
	@Autowired
	private DateModule date2Module;
	
	@Autowired
	private List<ToDoProvider> providers;
	private Map<String, ToDoProvider> typeToProvider;
	
	@PostConstruct
	private void initProviders() {
		typeToProvider = providers.stream().collect(Collectors.toMap(ToDoProvider::getType, Function.identity()));
	}
	
	@Override
	public ToDoProvider getProvider(String type) {
		return typeToProvider.get(type);
	}
	
	@Override
	public ToDoTask createToDoTask(Identity doer, String type) {
		return createToDoTask(doer, type, null, null, null);
	}

	@Override
	public ToDoTask createToDoTask(Identity doer, String type, Long originId, String originSubPath, String originTitle) {
		ToDoTask toDoTask = toDoTaskDao.create(doer, type, originId, originSubPath, originTitle);
		groupDao.addMembershipOneWay(toDoTask.getBaseGroup(), doer, ToDoRole.creator.name());
		groupDao.addMembershipOneWay(toDoTask.getBaseGroup(), doer, ToDoRole.modifier.name());
		return toDoTask;
	}

	@Override
	public ToDoTask update(Identity doer, ToDoTask toDoTask) {
		List<GroupMembership> memberships = groupDao.getMemberships(toDoTask.getBaseGroup(), ToDoRole.modifier.name(), false);
		if (memberships.size() == 1) {
			GroupMembership membership = memberships.get(0);
			if (!membership.getIdentity().getKey().equals(doer.getKey())) {
				groupDao.removeMembership(membership);
				groupDao.addMembershipOneWay(toDoTask.getBaseGroup(), doer, ToDoRole.modifier.name());
			}
		} else {
			if (memberships.size() > 1) {
				memberships.forEach(membership -> groupDao.removeMembership(membership));
			}
			groupDao.addMembershipOneWay(toDoTask.getBaseGroup(), doer, ToDoRole.modifier.name());
		}
		
		return toDoTaskDao.save(toDoTask);
	}
	
	@Override
	public void updateOriginTitle(String type, Long originId, String originSubPath, String originTitle) {
		toDoTaskDao.save(type, originId, originSubPath, originTitle);
	}
	
	@Override
	public void updateOriginDeleted(String type, Long originId, String originSubPath, boolean deleted, Date originDeletedDate, Identity originDeletedBy) {
		toDoTaskDao.save(type, originId, originSubPath, deleted, originDeletedDate, originDeletedBy);
	}

	@Override
	public void deleteToDoTaskPermanently(ToDoTask toDoTask) {
		if (toDoTask == null) return;
		
		tagDao.deleteByToDoTask(toDoTask);
		toDoTaskDao.delete(toDoTask);
	}

	@Override
	public ToDoTask getToDoTask(ToDoTaskRef toDoTask) {
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setToDoTasks(List.of(toDoTask));
		List<ToDoTask> toDoTasks = getToDoTasks(searchParams);
		return !toDoTasks.isEmpty()? toDoTasks.get(0): null;
	}
	
	@Override
	public ToDoTask getToDoTask(String type, Long originId, String originSubPath) {
		return toDoTaskDao.load(type, originId, originSubPath);
	}

	@Override
	public List<ToDoTask> getToDoTasks(ToDoTaskSearchParams searchParams) {
		return toDoTaskDao.loadToDoTasks(searchParams);
	}
	
	@Override
	public Long getToDoTaskCount(ToDoTaskSearchParams searchParams) {
		return toDoTaskDao.loadToDoTaskCount(searchParams);
	}
	
	@Override
	public void updateMember(ToDoTask toDoTask, Collection<? extends IdentityRef> assignees, Collection<? extends IdentityRef> delegatees) {
		Map<Long, Set<ToDoRole>> identityKeyToRoles = new HashMap<>();
		
		for (IdentityRef identityRef : assignees) {
			Set<ToDoRole> roles = identityKeyToRoles.computeIfAbsent(identityRef.getKey(), key -> new HashSet<>(1));
			roles.add(ToDoRole.assignee);
		}
		for (IdentityRef identityRef : delegatees) {
			Set<ToDoRole> roles = identityKeyToRoles.computeIfAbsent(identityRef.getKey(), key -> new HashSet<>(1));
			roles.add(ToDoRole.delegatee);
		}
		
		// Add all current members to remove someone who has no role anymore
		List<Identity> currentMembers = groupDao.getMembers(
				List.of(toDoTask.getBaseGroup()),
				ToDoRole.ASSIGNEE_DELEGATEE.stream().map(ToDoRole::name).toList());
		for (IdentityRef identityRef : currentMembers) {
			identityKeyToRoles.computeIfAbsent(identityRef.getKey(), key -> Set.of());
		}
		
		identityKeyToRoles.entrySet().forEach(identityToRole -> updateMember(toDoTask, new IdentityRefImpl(identityToRole.getKey()),
				identityToRole.getValue()));
	}
	
	private void updateMember(ToDoTask toDoTask, IdentityRef identity, Set<ToDoRole> roles) {
		Group group = toDoTask.getBaseGroup();
		
		List<ToDoRole> currentRoles = groupDao.getMemberships(group, identity).stream()
				.map(GroupMembership::getRole)
				.map(ToDoRole::valueOf)
				.collect(Collectors.toList());
		if (currentRoles.isEmpty() && roles.isEmpty()) {
			return;
		}
		
		for (ToDoRole role : roles) {
			if (!currentRoles.contains(role)) {
				Identity reloadedIdentity = securityManager.loadIdentityByKey(identity.getKey());
				groupDao.addMembershipOneWay(group, reloadedIdentity, role.name());
			}
			
		}
		
		// Delete membership of old roles. Creators are never removed
		for (ToDoRole currentRole : currentRoles) {
			if (ToDoRole.creator != currentRole && ToDoRole.modifier != currentRole && !roles.contains(currentRole)) {
				groupDao.removeMembership(group, identity, currentRole.name());
			}
		}
	}
	
	@Override
	public Map<Long, ToDoTaskMembers> getToDoTaskGroupKeyToMembers(Collection<ToDoTask> toDoTasks, Collection<ToDoRole> roles) {
		Collection<Long> groupKeys = toDoTasks.stream().map(ToDoTask::getBaseGroup).map(Group::getKey).collect(Collectors.toSet());
		Collection<String> roleNames = roles.stream().map(ToDoRole::name).collect(Collectors.toSet());
		
		Map<Long, ToDoTaskMembers> toDoTaskGroupKeyToMembers = toDoTasks.stream()
				.collect(Collectors.toMap(toDoTask -> toDoTask.getBaseGroup().getKey(), k -> new ToDoTaskMembersImpl()));
		for (GroupMembership membership : groupDao.getMemberships(groupKeys, roleNames)) {
			Long groupKey = membership.getGroup().getKey();
			ToDoTaskMembersImpl members = (ToDoTaskMembersImpl)toDoTaskGroupKeyToMembers.get(groupKey);
			members.add(membership.getRole(), membership.getIdentity());
			
		}
		
		return toDoTaskGroupKeyToMembers;
	}

	@Override
	public void updateTags(ToDoTaskRef toDoTask, List<String> displayNames) {
		ToDoTask reloadedToDoTask = getToDoTask(toDoTask);
		if (reloadedToDoTask == null) return;
		
		List<ToDoTaskTag> toDoTaskTags = tagDao.loadToDoTaskTags(reloadedToDoTask);
		List<Tag> currentTags = toDoTaskTags.stream().map(ToDoTaskTag::getTag).toList();
		List<Tag> tags = tagService.getOrCreateTags(displayNames);
		
		for (Tag tag : tags) {
			if (!currentTags.contains(tag)) {
				 tagDao.create(reloadedToDoTask, tag);
			}
		}
		
		for (ToDoTaskTag toDoTaskTag : toDoTaskTags) {
			if (!tags.contains(toDoTaskTag.getTag())) {
				tagDao.delete(toDoTaskTag);
			}
		}
	}

	@Override
	public List<ToDoTaskTag> getToDoTaskTags(ToDoTaskSearchParams searchParams) {
		return toDoTaskDao.loadToDoTaskTags(searchParams);
	}
	
	@Override
	public List<TagInfo> getTagInfos(ToDoTaskSearchParams searchParams, ToDoTaskRef selectionTask) {
		return toDoTaskDao.loadTagInfos(searchParams, selectionTask);
	}

	@Override
	public ToDoExpenditureOfWork getExpenditureOfWork(Long hours) {
		if (hours == null) return null;
		
		long hoursIntern = hours.longValue();
		long eowHours = hoursIntern % date2Module.getDailyWorkHours();
		hoursIntern = hoursIntern - eowHours;
		long eowWeekHours = hoursIntern % date2Module.getWeeklyWorkHours();
		long eowDays = eowWeekHours / date2Module.getDailyWorkHours();
		hoursIntern = hoursIntern - eowWeekHours;
		long eowWeeks = hoursIntern / date2Module.getWeeklyWorkHours();
		
		return new ToDoExpenditureOfWorkImpl(eowWeeks, eowDays, eowHours);
	}

	@Override
	public Long getHours(ToDoExpenditureOfWork expenditureOfWork) {
		if (expenditureOfWork == null) return null;
		
		return date2Module.getWeeklyWorkHours() * expenditureOfWork.getWeeks() 
				+ date2Module.getDailyWorkHours() * expenditureOfWork.getDays()
				+ expenditureOfWork.getHours();
	}
	
}
