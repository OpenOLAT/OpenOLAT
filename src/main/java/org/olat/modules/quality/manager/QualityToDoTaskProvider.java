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
package org.olat.modules.quality.manager;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.modules.quality.QualityAuditLog.Action;
import org.olat.modules.quality.QualityModule;
import org.olat.modules.quality.ui.QualityToDoEditController;
import org.olat.modules.todo.ToDoContextFilter;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoProvider;
import org.olat.modules.todo.ToDoRight;
import org.olat.modules.todo.ToDoRole;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskMembers;
import org.olat.modules.todo.ToDoTaskRef;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ToDoTaskTag;
import org.olat.modules.todo.ui.ToDoDeleteConfirmationController;
import org.olat.modules.todo.ui.ToDoTaskDetailsController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class QualityToDoTaskProvider implements ToDoProvider, ToDoContextFilter {
	
	public static final List<String> ALL_TYPES = List.of(
			GeneralToDoTaskProvider.TYPE,
			DataCollectionToDoTaskProvider.TYPE,
			EvaluationFormSessionToDoTaskProvider.TYPE);

	@Autowired
	private ToDoService toDoService;
	@Autowired
	private QualityModule qualityModule;
	@Autowired
	private QualityAuditLogDAO auditLogDao;
	
	protected abstract ToDoRight[] getAssigneeRights();

	@Override
	public boolean isEnabled() {
		return qualityModule.isEnabled() && qualityModule.isToDoEnabled();
	}
	
	@Override
	public String getContextFilterType() {
		return getType();
	}
	
	@Override
	public String getModifiedBy(Locale locale, ToDoTask toDoTask) {
		return null;
	}

	@Override
	public Controller createCreateController(UserRequest ureq, WindowControl wControl, Identity doer, Long originId,
			String originSubPath) {
		return null;
	}

	@Override
	public void upateStatus(Identity doer, ToDoTaskRef toDoTaskRef, Long originId, String originSubPath, ToDoStatus status) {
		ToDoTask toDoTask = getToDoTask(toDoTaskRef, true);
		if (toDoTask == null) {
			return;
		}
		
		updateReloadedToDo(doer, toDoTask, toDoTask.getTitle(), status, toDoTask.getPriority(),
				toDoTask.getStartDate(), toDoTask.getDueDate(), toDoTask.getExpenditureOfWork(),
				toDoTask.getDescription());
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask, boolean showContext) {
		return new QualityToDoEditController(ureq, wControl, toDoTask, showContext);
	}

	@Override
	public FormBasicController createDetailController(UserRequest ureq, WindowControl wControl, Form mainForm,
			ToDoTaskSecurityCallback secCallback, ToDoTask toDoTask, List<Tag> tags, Identity modifier,
			Set<Identity> assignees, Set<Identity> delegatees) {
		return new ToDoTaskDetailsController(ureq, wControl, mainForm, secCallback, toDoTask, tags, modifier, assignees, delegatees);
	}

	@Override
	public void deleteToDoTaskSoftly(Identity doer, ToDoTask toDoTaskRef) {
		ToDoTask toDoTask = getToDoTask(toDoTaskRef, true);
		if (toDoTask == null) {
			return;
		}
		String before = QualityXStream.toXml(toDoTask);
		ToDoStatus previousStatus = toDoTask.getStatus();
		
		toDoTask.setStatus(ToDoStatus.deleted);
		toDoTask.setContentModifiedDate(new Date());
		toDoService.update(doer, toDoTask, previousStatus);
		
		toDoTask = getToDoTask(toDoTaskRef, false);
		String after = QualityXStream.toXml(toDoTask);
		auditLogDao.create(Action.toDoStatusUpdate, before, after, doer, toDoTask.getOriginId(), toDoTask, null);
	}

	@Override
	public Controller createDeleteConfirmationController(UserRequest ureq, WindowControl wControl, Locale locale, ToDoTask toDoTask) {
		return new ToDoDeleteConfirmationController(ureq, wControl, toDoTask);
	}

	private ToDoTask getToDoTask(ToDoTaskRef toDoTaskRef, boolean active) {
		ToDoTask toDoTask = toDoService.getToDoTask(toDoTaskRef);
		if (toDoTask == null) {
			return null;
		}
		if (active && toDoTask.getStatus() == ToDoStatus.deleted) {
			return null;
		}
		return toDoTask;
	}

	public ToDoTask createToDo(Identity doer, Long dataCollectionId, String originSubPath, String originTitle) {
		ToDoTask toDoTask = toDoService.createToDoTask(doer, getType(), dataCollectionId, originSubPath, originTitle, null);
		String after = QualityXStream.toXml(toDoService.getToDoTask(toDoTask));
		auditLogDao.create(Action.toDoCreate, null, after, doer, toDoTask.getOriginId(), toDoTask, null);
		return toDoTask;
	}

	public void updateToDo(Identity doer, ToDoTaskRef toDoTaskRef, String title, ToDoStatus status,
			ToDoPriority priority, Date startDate, Date dueDate, Long expenditureOfWork, String description) {
		ToDoTask toDoTask = getToDoTask(toDoTaskRef, true);
		if (toDoTask == null) {
			return;
		}
		
		updateReloadedToDo(doer, toDoTask, title, status, priority, startDate, dueDate, expenditureOfWork, description);
	}

	private void updateReloadedToDo(Identity doer, ToDoTask toDoTask, String title, ToDoStatus status,
			ToDoPriority priority, Date startDate, Date dueDate, Long expenditureOfWork, String description) {
		String before = QualityXStream.toXml(toDoTask);
		ToDoStatus previousStatus = toDoTask.getStatus();
		
		boolean contentChanged = false;
		boolean statusChanged = false;
		if (!Objects.equals(toDoTask.getTitle(), title)) {
			toDoTask.setTitle(title);
			contentChanged = true;
		}
		if (!Objects.equals(toDoTask.getStatus(), status)) {
			toDoTask.setStatus(status);
			statusChanged = true;
		}
		if (!Objects.equals(toDoTask.getPriority(), priority)) {
			toDoTask.setPriority(priority);
			contentChanged = true;
		}
		if (!DateUtils.isSameDay(toDoTask.getStartDate(), startDate)) {
			toDoTask.setStartDate(startDate);
			contentChanged = true;
		}
		if (!DateUtils.isSameDay(toDoTask.getDueDate(), dueDate)) {
			toDoTask.setDueDate(dueDate);
			contentChanged = true;
		}
		if (!Objects.equals(toDoTask.getExpenditureOfWork(), expenditureOfWork)) {
			toDoTask.setExpenditureOfWork(expenditureOfWork);
			contentChanged = true;
		}
		if (!Objects.equals(toDoTask.getDescription(), description)) {
			toDoTask.setDescription(description);
			contentChanged = true;
		}
		if (toDoTask.getAssigneeRights() == null || toDoTask.getAssigneeRights().length == 0) {
			// Assignee has no rights if template
			toDoTask.setAssigneeRights(getAssigneeRights());
			contentChanged = true;
		}
		if (contentChanged || statusChanged) {
			toDoTask.setContentModifiedDate(new Date());
			toDoService.update(doer, toDoTask, previousStatus);
			String after = QualityXStream.toXml(toDoService.getToDoTask(toDoTask));
			if (contentChanged) {
				auditLogDao.create(Action.toDoContentUpdate, before, after, doer, toDoTask.getOriginId(), toDoTask, null);
			}
			if (statusChanged) {
				auditLogDao.create(Action.toDoStatusUpdate, before, after, doer, toDoTask.getOriginId(), toDoTask, null);
			}
		}
	}

	public void updateMembers(Identity doer, ToDoTask toDoTaskRef, Collection<? extends IdentityRef> assignees,
			Collection<? extends IdentityRef> delegatees) {
		ToDoTask toDoTask = getToDoTask(toDoTaskRef, true);
		if (toDoTask == null) {
			return;
		}
		
		ToDoTaskMembers toDoTaskMembersBefore = toDoService
				.getToDoTaskGroupKeyToMembers(List.of(toDoTask), ToDoRole.ASSIGNEE_DELEGATEE)
				.get(toDoTask.getBaseGroup().getKey());
		Set<Identity> beforeMembers = toDoTaskMembersBefore.getMembers();
		
		toDoService.updateMember(doer, toDoTask, assignees, delegatees);
		
		ToDoTaskMembers toDoTaskMembersAfter = toDoService
				.getToDoTaskGroupKeyToMembers(List.of(toDoTask), ToDoRole.ASSIGNEE_DELEGATEE)
				.get(toDoTask.getBaseGroup().getKey());
		Set<Identity> afterMembers = toDoTaskMembersAfter.getMembers();
		
		for (Identity afterMember : afterMembers) {
			if (!beforeMembers.contains(afterMember)) {
				// New members
				auditLogDao.create(Action.toDoMemberAdd, null, null, doer, toDoTask.getOriginId(), toDoTask, afterMember);
				
				String rolesAfterXml = QualityXStream.rolesToXml(toDoTaskMembersAfter.getRoles(afterMember).stream()
						.map(ToDoRole::name)
						.collect(Collectors.toList()));
				auditLogDao.create(Action.toDoRolesUpdate, null, rolesAfterXml, doer, toDoTask.getOriginId(), toDoTask, afterMember);
			} else {
				// Member before and after
				Set<ToDoRole> rolesBefore = toDoTaskMembersBefore.getRoles(afterMember);
				Set<ToDoRole> rolesAfter = toDoTaskMembersAfter.getRoles(afterMember);
				if (!rolesBefore.equals(rolesAfter)) {
					// Roles changed
					String rolesBeforeXml = QualityXStream.rolesToXml(rolesBefore.stream()
							.map(ToDoRole::name)
							.collect(Collectors.toList()));
					String rolesAfterXml = QualityXStream.rolesToXml(rolesAfter.stream()
							.map(ToDoRole::name)
							.collect(Collectors.toList()));
					auditLogDao.create(Action.toDoRolesUpdate, rolesBeforeXml, rolesAfterXml, doer, toDoTask.getOriginId(), toDoTask, afterMember);
				}
			}
		}
		
		for (Identity beforeMember : beforeMembers) {
			if (!afterMembers.contains(beforeMember)) {
				// Not member anymore
				String rolesBeforeXml = QualityXStream.rolesToXml(toDoTaskMembersBefore.getRoles(beforeMember).stream()
						.map(ToDoRole::name)
						.collect(Collectors.toList()));
				auditLogDao.create(Action.toDoRolesUpdate, rolesBeforeXml, null, doer, toDoTask.getOriginId(), toDoTask, beforeMember);
				
				auditLogDao.create(Action.toDoMemberRemove, null, null, doer, toDoTask.getOriginId(), toDoTask, beforeMember);
			}
		}
		
	}

	public void updateTags(Identity doer, ToDoTask toDoTaskRef, List<String> tagDisplayNames) {
		ToDoTask toDoTask = getToDoTask(toDoTaskRef, true);
		if (toDoTask == null) {
			return;
		}
		
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setToDoTasks(List.of(toDoTask));
		List<ToDoTaskTag> toDoTaskTagsBefore = toDoService.getToDoTaskTags(searchParams);
		
		toDoService.updateTags(toDoTask, tagDisplayNames);
		List<ToDoTaskTag> toDoTaskTagsAfter = toDoService.getToDoTaskTags(searchParams);
		
		if (!Objects.equals(toDoTaskTagsBefore, toDoTaskTagsAfter)) {
			String before = QualityXStream.tagsToXml(toDoTaskTagsBefore.stream().map(ToDoTaskTag::getTag).map(Tag::getDisplayName).toList());
			String after = QualityXStream.tagsToXml(toDoTaskTagsAfter.stream().map(ToDoTaskTag::getTag).map(Tag::getDisplayName).toList());
			auditLogDao.create(Action.toDoTagsUpdate, before, after, doer, toDoTask.getOriginId(), toDoTask, null);
		}
	}

}