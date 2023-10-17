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
package org.olat.modules.todo.ui;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.model.Buddy;
import org.olat.modules.todo.ToDoContext;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoRight;
import org.olat.modules.todo.ToDoRole;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskMembers;
import org.olat.modules.todo.ToDoTaskRef;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.manager.PersonalToDoProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Oct 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoTaskEditController extends FormBasicController {
	
	private ToDoTaskEditForm toDoTaskEditForm;
	
	private ToDoTask toDoTask;
	private final boolean showContext;
	private final Collection<ToDoContext> availableContexts;
	private final ToDoContext currentContext;
	
	@Autowired
	private ToDoService toDoService;
	@Autowired
	private PersonalToDoProvider personalToDoProvider;
	@Autowired
	private InstantMessagingService instantMessagingService;
	@Autowired
	private BaseSecurity securityManager;

	public ToDoTaskEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask, boolean showContext,
			Collection<ToDoContext> availableContexts, ToDoContext currentContext) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.toDoTask = toDoTask;
		this.showContext = showContext;
		this.availableContexts = availableContexts;
		this.currentContext = currentContext;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Make it configurable to user the controller in other scenarios (all parameters of the form)
		Set<Identity> memberCandidates = getMemberCandidates();
		Set<Identity> assignees = Set.of(getIdentity());
		Set<Identity> delegatees = Set.of();
		List<TagInfo> tagInfos;
		ToDoTaskSearchParams tagSearchParams = new ToDoTaskSearchParams();
		tagSearchParams.setAssigneeOrDelegatee(getIdentity());
		
		if (toDoTask != null) {
			ToDoTaskMembers toDoTaskMembers = toDoService
					.getToDoTaskGroupKeyToMembers(List.of(toDoTask), ToDoRole.ASSIGNEE_DELEGATEE)
					.get(toDoTask.getBaseGroup().getKey());
			assignees = toDoTaskMembers.getMembers(ToDoRole.assignee);
			delegatees = toDoTaskMembers.getMembers(ToDoRole.delegatee);
			
			tagInfos = toDoService.getTagInfos(tagSearchParams, toDoTask);
		} else {
			tagInfos = toDoService.getTagInfos(tagSearchParams, null);
		}
		
		toDoTaskEditForm = new ToDoTaskEditForm(ureq, getWindowControl(), mainForm, toDoTask, showContext,
				availableContexts, currentContext, memberCandidates, false, assignees, delegatees, true, tagInfos,
				true);
		listenTo(toDoTaskEditForm);
		formLayout.add("content", toDoTaskEditForm.getInitialFormItem());
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	private Set<Identity> getMemberCandidates() {
		Set<Long> buddyIdentityKeys = instantMessagingService.getBuddyGroups(getIdentity(), true).stream()
				.flatMap(buddyGroup -> buddyGroup.getBuddy().stream())
				.filter(buddy -> !buddy.isAnonym())
				.map(Buddy::getIdentityKey)
				.collect(Collectors.toSet());
		List<Identity> buddyIdentities = securityManager.loadIdentityByKeys(buddyIdentityKeys);
		buddyIdentities.add(getIdentity());
		return new HashSet<>(buddyIdentities);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, FormEvent.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (toDoTask == null) {
			toDoTask = toDoService.createToDoTask(getIdentity(), personalToDoProvider.getType(), null, null, null);
			toDoTask.setAssigneeRights(new ToDoRight[] {ToDoRight.all});
		} else {
			toDoTask = getToDoTask(toDoTask, true);
		}
		if (toDoTask == null) {
			return;
		}
		
		updateToDo(getIdentity(), toDoTask,
				toDoTaskEditForm.getTitle(),
				toDoTaskEditForm.getStatus(),
				toDoTaskEditForm.getPriority(),
				toDoTaskEditForm.getStartDate(),
				toDoTaskEditForm.getDueDate(),
				toDoTaskEditForm.getExpenditureOfWork(),
				toDoTaskEditForm.getDescription());
		
		toDoService.updateMember(getIdentity(), toDoTask, toDoTaskEditForm.getAssignees(), toDoTaskEditForm.getDelegatees());
		
		toDoService.updateTags(toDoTask, toDoTaskEditForm.getTagDisplayNames());
		
		fireEvent(ureq, FormEvent.DONE_EVENT);
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
		if (contentChanged || statusChanged) {
			toDoTask.setContentModifiedDate(new Date());
			toDoService.update(doer, toDoTask, previousStatus);
		}
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

}
