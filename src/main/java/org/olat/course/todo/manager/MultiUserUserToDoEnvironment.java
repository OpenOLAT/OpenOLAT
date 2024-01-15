/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.todo.manager;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.todo.CourseToDoEnvironment;
import org.olat.modules.todo.ToDoRight;
import org.olat.modules.todo.ToDoRole;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskMembers;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;

/**
 * 
 * Initial date: 1 Nov 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MultiUserUserToDoEnvironment implements CourseToDoEnvironment {

	private static final ToDoRight[] ASSIGNEE_RIGHTS = new ToDoRight[] {ToDoRight.view};
	
	private final Collection<String> toDoProviderTypes;
	private final Collection<? extends IdentityRef> identities;
	private Map<CourseNodeIdentityTypeKey, ToDoTask> keyToToDoTask;
	private Map<Long, Date> identityToLaunchDate;
	private List<Long> memberKeys;
	
	private ToDoService toDoService;

	public MultiUserUserToDoEnvironment(Collection<String> toDoProviderTypes, Collection<? extends IdentityRef> identities) {
		this.toDoProviderTypes = toDoProviderTypes;
		this.identities = identities;
	}
	
	@Override
	public void reset() {
		keyToToDoTask = null;
		identityToLaunchDate = null;
		memberKeys = null;
	}

	@Override
	public ToDoTask getToDoTask(UserCourseEnvironment userCourseEnv, CourseNode courseNode, String toDoTaskType) {
		if (keyToToDoTask == null) {
			ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
			searchParams.setTypes(toDoProviderTypes);
			searchParams.setAssigneeOrDelegatee(identities);
			searchParams.setOriginIds(List.of(userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry().getKey()));
			List<ToDoTask> toDoTasks = getToDoService().getToDoTasks(searchParams);
			keyToToDoTask = new HashMap<>(toDoTasks.size());
			
			Map<Long, ToDoTaskMembers> toDoTaskGroupKeyToMembers = getToDoService().getToDoTaskGroupKeyToMembers(toDoTasks, List.of(ToDoRole.assignee));
			
			for (ToDoTask toDoTask : toDoTasks) {
				toDoTaskGroupKeyToMembers.get(toDoTask.getBaseGroup().getKey())
						.getMembers(ToDoRole.assignee)
						.stream().findAny()
						.ifPresent(assignee -> keyToToDoTask.put(new CourseNodeIdentityTypeKey(toDoTask.getOriginSubPath(), assignee.getKey(), toDoTask.getType()), toDoTask));
			}
		}
		return keyToToDoTask.get(new CourseNodeIdentityTypeKey(courseNode.getIdent(), userCourseEnv.getIdentityEnvironment().getIdentity().getKey(), toDoTaskType));
	}

	@Override
	public ToDoTask createToDoTask(UserCourseEnvironment userCourseEnv, CourseNode courseNode, String toDoTaskType, String title) {
		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		Long originId = courseEntry.getKey();
		String originSubPath = courseNode.getIdent();
		String originTitle = courseEntry.getDisplayname();
		String originSubTitle = courseNode.getLongTitle();
		Identity assignee = userCourseEnv.getIdentityEnvironment().getIdentity();
		
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setOriginIds(List.of(originId));
		searchParams.setOriginSubPaths(List.of(originSubPath));
		searchParams.setAssigneeOrDelegatee(assignee);
		searchParams.setTypes(List.of(toDoTaskType));
		List<ToDoTask> toDoTasks = getToDoService().getToDoTasks(searchParams);
		if (!toDoTasks.isEmpty()) {
			return toDoTasks.get(0);
		}
		
		ToDoTask toDoTask = getToDoService().createToDoTask(null, toDoTaskType, originId, originSubPath, originTitle, originSubTitle, null);
		toDoTask.setTitle(title); // Needed in email template
		getToDoService().updateMember(null, toDoTask, List.of(assignee), List.of());
		toDoTask.setAssigneeRights(ASSIGNEE_RIGHTS);
		return toDoTask;
	}
	
	@Override
	public void updateToDoTask(ToDoTask toDoTask, String title, String description, ToDoStatus status, Date dueDate,
			String originTitle, String originSubTitle) {
		ToDoStatus previousStatus = toDoTask.getStatus();
		
		boolean contentChanged = false;
		boolean originChanged = false;
		if (!Objects.equals(toDoTask.getTitle(), title)) {
			toDoTask.setTitle(title);
			contentChanged = true;
		}
		if (!Objects.equals(toDoTask.getDescription(), description)) {
			toDoTask.setDescription(description);
			contentChanged = true;
		}
		if (!Objects.equals(toDoTask.getStatus(), status)) {
			toDoTask.setStatus(status);
			contentChanged = true;
		}
		if (!DateUtils.isSameDay(toDoTask.getDueDate(), dueDate)) {
			toDoTask.setDueDate(dueDate);
			contentChanged = true;
		}
		if (!Objects.equals(toDoTask.getOriginTitle(), originTitle)) {
			toDoTask.setOriginTitle(originTitle);
			originChanged = true;
		}
		if (!Objects.equals(toDoTask.getOriginSubTitle(), originSubTitle)) {
			toDoTask.setOriginSubTitle(originSubTitle);
			originChanged = true;
		}
		
		if (contentChanged) {
			toDoTask.setContentModifiedDate(new Date());
		}
		if (contentChanged || originChanged) {
			getToDoService().update(null, toDoTask, previousStatus);
		}
	}

	@Override
	public void updateOriginDeleted(ToDoTask toDoTask, boolean deleted) {
		getToDoService().updateOriginDeleted(toDoTask, deleted, deleted ? new Date() : null, null);
	}
	
	@Override
	public Date getCourseLaunchDate(UserCourseEnvironment userCourseEnv) {
		if (identityToLaunchDate == null) {
			RepositoryEntry entry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			identityToLaunchDate = CoreSpringFactory.getImpl(UserCourseInformationsManager.class).getInitialLaunchDates(entry, identities);
		}
		
		return identityToLaunchDate.get(userCourseEnv.getIdentityEnvironment().getIdentity().getKey());
	}

	@Override
	public boolean isCourseParticipantMember(UserCourseEnvironment userCourseEnv) {
		if (memberKeys == null) {
			if (identities.size() == 1) {
				memberKeys = userCourseEnv.isMemberParticipant()
						? List.of(userCourseEnv.getIdentityEnvironment().getIdentity().getKey())
						: List.of();
			} else {
				memberKeys = CoreSpringFactory.getImpl(RepositoryService.class).getMemberKeys(
						userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), 
						RepositoryEntryRelationType.all, GroupRoles.participant.name());
			}
		}
		return memberKeys.contains(userCourseEnv.getIdentityEnvironment().getIdentity().getKey());
	}
	
	private ToDoService getToDoService() {
		if (toDoService == null) {
			toDoService = CoreSpringFactory.getImpl(ToDoService.class);
		}
		return toDoService;
	}

}
