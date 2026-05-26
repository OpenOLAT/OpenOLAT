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

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.modules.todo.ToDoContext;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoRelativeDates;
import org.olat.modules.todo.ToDoRight;
import org.olat.modules.todo.ToDoRole;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskMembers;
import org.olat.modules.todo.ToDoTaskRef;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ui.ToDoTaskEditForm.CopyValues;
import org.olat.modules.todo.ui.ToDoTaskEditForm.ToDoTaskValues;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Oct 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoTaskEditController extends FormBasicController {
	
	private ToDoTaskEditForm toDoTaskEditForm;
	private ToDoTaskMetadataController metadataCtrl;

	private ToDoTask toDoTask;
	private final ToDoTask toDoTaskCopySource;
	private final ToDoTaskContextConfig contextConfig;
	private final ToDoTaskMemberConfig assigneeConfig;
	private final ToDoTaskMemberConfig delegateeConfig;
	private final ToDoTaskMembers preloadedMembers;
	private final ToDoTaskDateConfig dateConfig;
	private final ToDoTaskSearchParams tagInfoSearchParams;
	private final ToDoRight[] defaultAssigneeRights;
	private final ToDoRight[] assigneeRightsOverride;
	private Boolean metadataOpen = Boolean.FALSE;

	@Autowired
	private ToDoService toDoService;

	public ToDoTaskEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask,
			ToDoTask toDoTaskCopySource, ToDoTaskContextConfig contextConfig, ToDoTaskMemberConfig assigneeConfig,
			ToDoTaskMemberConfig delegateeConfig, ToDoTaskMembers preloadedMembers, ToDoTaskDateConfig dateConfig,
			ToDoTaskSearchParams tagInfoSearchParams, ToDoRight[] defaultAssigneeRights,
			ToDoRight[] assigneeRightsOverride) {
		super(ureq, wControl, "todo_edit");
		this.toDoTask = toDoTask;
		this.toDoTaskCopySource = toDoTaskCopySource;
		this.contextConfig = contextConfig;
		this.dateConfig = dateConfig;
		this.assigneeConfig = assigneeConfig;
		this.delegateeConfig = delegateeConfig;
		this.preloadedMembers = preloadedMembers;
		this.tagInfoSearchParams = tagInfoSearchParams;
		this.defaultAssigneeRights = defaultAssigneeRights;
		this.assigneeRightsOverride = assigneeRightsOverride;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Identity creator = null;
		Identity modifier = null;
		List<TagInfo> tagInfos;

		if (toDoTask != null) {
			ToDoTaskMembers members = preloadedMembers != null
					? preloadedMembers
					: toDoService.getToDoTaskMembers(toDoTask, ToDoRole.ALL);
			creator = members.getMembers(ToDoRole.creator).stream().findAny().orElse(null);
			modifier = members.getMembers(ToDoRole.modifier).stream().findAny().orElse(null);
			tagInfos = toDoService.getTagInfos(tagInfoSearchParams, toDoTask);
		} else if (toDoTaskCopySource != null) {
			tagInfos = toDoService.getTagInfos(tagInfoSearchParams, toDoTaskCopySource);
		} else {
			tagInfos = toDoService.getTagInfos(tagInfoSearchParams, null);
		}

		toDoTaskEditForm = new ToDoTaskEditForm(ureq, getWindowControl(), mainForm, contextConfig, assigneeConfig,
				delegateeConfig, dateConfig, tagInfos, true);
		if (toDoTask != null) {
			toDoTaskEditForm.setValues(new ToDoTaskValues(toDoTask));
			ToDoRight[] effectiveRights = assigneeRightsOverride != null ? assigneeRightsOverride : toDoTask.getAssigneeRights();
			toDoTaskEditForm.updateUIByAssigneeRight(effectiveRights);
		} else if (toDoTaskCopySource != null) {
			toDoTaskEditForm.setValues(new CopyValues(getLocale(), toDoTaskCopySource));
		}
		listenTo(toDoTaskEditForm);
		formLayout.add("content", toDoTaskEditForm.getInitialFormItem());
		
		if (toDoTask != null) {
			metadataCtrl = new ToDoTaskMetadataController(ureq, getWindowControl(), mainForm, creator,
					toDoTask.getCreationDate(), modifier, toDoTask.getContentModifiedDate());
			listenTo(metadataCtrl);
			formLayout.add("metadata", metadataCtrl.getInitialFormItem());
			flc.contextPut("metadataOpen", metadataOpen);
		}
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("ONCLICK".equals(event.getCommand())) {
			String metadataOpenVal = ureq.getParameter("metadataOpen");
			if (StringHelper.containsNonWhitespace(metadataOpenVal)) {
				metadataOpen = Boolean.valueOf(metadataOpenVal);
				flc.contextPut("metadataOpen", metadataOpen);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, FormEvent.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (toDoTask == null) {
			ToDoContext ctx = toDoTaskEditForm.getContext() != null ? toDoTaskEditForm.getContext() : contextConfig.getCurrentContext();
			toDoTask = toDoService.createToDoTask(getIdentity(),
					ctx.getType(),
					ctx.getOriginId(),
					ctx.getOriginSubPath(),
					ctx.getOriginTitle(),
					ctx.getOriginSubTitle(), null);
			toDoTask.setAssigneeRights(defaultAssigneeRights);
		} else {
			toDoTask = getToDoTask(toDoTask, true);
			if (toDoTask == null) {
				return;
			}
			updateContext(getIdentity(), toDoTask, toDoTaskEditForm.getContext());
		}
		if (toDoTask == null) {
			return;
		}

		ToDoRelativeDates relativeDates = toDoTaskEditForm.getRelativeDates();
		Date startDate;
		Date dueDate;
		if (relativeDates != null && dateConfig != null && dateConfig.getPicker() != null) {
			startDate = dateConfig.getPicker().resolve(relativeDates, true);
			dueDate   = dateConfig.getPicker().resolve(relativeDates, false);
		} else {
			startDate = toDoTaskEditForm.getStartDate();
			dueDate   = toDoTaskEditForm.getDueDate();
		}
		updateToDo(getIdentity(), toDoTask,
				toDoTaskEditForm.getTitle(),
				toDoTaskEditForm.getStatus(),
				toDoTaskEditForm.getPriority(),
				startDate,
				dueDate,
				relativeDates,
				toDoTaskEditForm.getExpenditureOfWork(),
				toDoTaskEditForm.getDescription());
		
		toDoService.updateMember(getIdentity(), toDoTask, toDoTaskEditForm.getAssignees(), toDoTaskEditForm.getDelegatees());
		
		toDoService.updateTags(toDoTask, toDoTaskEditForm.getTagDisplayNames());
		
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}
	
	public void updateToDo(Identity doer, ToDoTaskRef toDoTaskRef, String title, ToDoStatus status,
			ToDoPriority priority, Date startDate, Date dueDate, ToDoRelativeDates relativeDates,
			Long expenditureOfWork, String description) {
		ToDoTask toDoTask = getToDoTask(toDoTaskRef, true);
		if (toDoTask == null) {
			return;
		}
		
		updateReloadedToDo(doer, toDoTask, title, status, priority, startDate, dueDate, relativeDates, expenditureOfWork, description);
	}

	private void updateReloadedToDo(Identity doer, ToDoTask toDoTask, String title, ToDoStatus status,
			ToDoPriority priority, Date startDate, Date dueDate, ToDoRelativeDates relativeDates,
			Long expenditureOfWork, String description) {
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
		if (!Objects.equals(toDoTask.getRelativeDates(), relativeDates)) {
			toDoTask.setRelativeDates(relativeDates);
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
	
	private void updateContext(Identity doer, ToDoTask toDoTask, ToDoContext context) {
		if (context == null) {
			return;
		}
		boolean changed = false;
		if (!Objects.equals(toDoTask.getType(), context.getType())) {
			toDoTask.setType(context.getType());
			changed = true;
		}
		if (!Objects.equals(toDoTask.getOriginId(), context.getOriginId())) {
			toDoTask.setOriginId(context.getOriginId());
			changed = true;
		}
		if (!Objects.equals(toDoTask.getOriginSubPath(), context.getOriginSubPath())) {
			toDoTask.setOriginSubPath(context.getOriginSubPath());
			changed = true;
		}
		if (!Objects.equals(toDoTask.getOriginTitle(), context.getOriginTitle())) {
			toDoTask.setOriginTitle(context.getOriginTitle());
			changed = true;
		}
		if (!Objects.equals(toDoTask.getOriginSubTitle(), context.getOriginSubTitle())) {
			toDoTask.setOriginSubTitle(context.getOriginSubTitle());
			changed = true;
		}
		if (changed) {
			toDoService.update(doer, toDoTask, toDoTask.getStatus());
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
