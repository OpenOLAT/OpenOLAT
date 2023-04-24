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
package org.olat.modules.quality.ui;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.olat.core.util.StringHelper;
import org.olat.modules.quality.manager.DataCollectionToDoTaskProvider;
import org.olat.modules.quality.manager.EvaluationFormSessionToDoTaskProvider;
import org.olat.modules.quality.manager.GeneralToDoTaskProvider;
import org.olat.modules.quality.manager.QualityToDoTaskProvider;
import org.olat.modules.todo.ToDoContext;
import org.olat.modules.todo.ToDoRole;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskMembers;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ui.ToDoTaskEditForm;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityToDoEditController extends FormBasicController {
	
	private ToDoTaskEditForm toDoTaskEditForm;
	private QualityToDoActivityLogController activityLogCtrl;
	
	private final Collection<ToDoContext> availableContexts;
	private final ToDoContext currentContext;
	private Long dataCollectionId;
	private String originSubPath;
	private ToDoTask toDoTask;
	private Boolean auditOpen = Boolean.FALSE;

	@Autowired
	private ToDoService toDoService;
	@Autowired
	private GeneralToDoTaskProvider generalProvider;
	@Autowired
	private DataCollectionToDoTaskProvider dataCollectionProvider;
	@Autowired
	private EvaluationFormSessionToDoTaskProvider sessionProvider;

	public QualityToDoEditController(UserRequest ureq, WindowControl wControl, Long dataCollectionId,
			String originSubPath, Collection<ToDoContext> availableContexts, ToDoContext currentContext) {
		super(ureq, wControl, "todo_edit");
		this.dataCollectionId = dataCollectionId;
		this.originSubPath = originSubPath;
		this.availableContexts = availableContexts;
		this.currentContext = currentContext;

		initForm(ureq);
	}

	public QualityToDoEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask) {
		super(ureq, wControl, "todo_edit");
		this.toDoTask = toDoTask;
		this.availableContexts = List.of(toDoTask);
		this.currentContext = toDoTask;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Set<Identity> memberCandidates = Set.of(getIdentity());
		Set<Identity> assignees = Set.of(getIdentity());
		Set<Identity> delegatees = Set.of();
		List<TagInfo> tagInfos;
		ToDoTaskSearchParams tagSearchParams = new ToDoTaskSearchParams();
		tagSearchParams.setTypes(QualityToDoTaskProvider.ALL_TYPES);
		
		if (toDoTask != null) {
			ToDoTaskMembers toDoTaskMembers = toDoService
					.getToDoTaskGroupKeyToMembers(List.of(toDoTask), ToDoRole.ASSIGNEE_DELEGATEE)
					.get(toDoTask.getBaseGroup().getKey());
			assignees = toDoTaskMembers.getMembers(ToDoRole.assignee);
			delegatees = toDoTaskMembers.getMembers(ToDoRole.delegatee);
			memberCandidates = new HashSet<>();
			memberCandidates.addAll(assignees);
			memberCandidates.addAll(delegatees);
			
			tagInfos = toDoService.getTagInfos(tagSearchParams, toDoTask);
		} else {
			tagInfos = toDoService.getTagInfos(tagSearchParams, null);
		}
		
		toDoTaskEditForm = new ToDoTaskEditForm(ureq, getWindowControl(), mainForm, toDoTask, true, availableContexts,
				currentContext, memberCandidates, true, assignees, delegatees, tagInfos);
		listenTo(toDoTaskEditForm);
		formLayout.add("content", toDoTaskEditForm.getInitialFormItem());
		
		if (toDoTask != null) {
			activityLogCtrl = new QualityToDoActivityLogController(ureq, getWindowControl(), mainForm, toDoTask);
			listenTo(activityLogCtrl);
			formLayout.add("audit", activityLogCtrl.getInitialFormItem());
			flc.contextPut("auditOpen", auditOpen);
		}
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("ONCLICK".equals(event.getCommand())) {
			String auditOpenVal = ureq.getParameter("auditOpen");
			if (StringHelper.containsNonWhitespace(auditOpenVal)) {
				auditOpen = Boolean.valueOf(auditOpenVal);
				flc.contextPut("auditOpen", auditOpen);
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
		ToDoContext context = toDoTaskEditForm.getContext();
		if (context != null) {
			QualityToDoTaskProvider toDoTaskProvider = getToDoTaskProvider(context.getType());
			
			if (toDoTask == null) {
				toDoTask = toDoTaskProvider.createToDo(getIdentity(), dataCollectionId, originSubPath, context.getOriginTitle());
			}
			
			toDoTaskProvider.updateToDo(getIdentity(), toDoTask,
					toDoTaskEditForm.getTitle(),
					toDoTaskEditForm.getStatus(),
					toDoTaskEditForm.getPriority(),
					toDoTaskEditForm.getStartDate(),
					toDoTaskEditForm.getDueDate(),
					toDoTaskEditForm.getExpenditureOfWork(),
					toDoTaskEditForm.getDescription());
			
			toDoTaskProvider.updateMembers(getIdentity(), toDoTask, toDoTaskEditForm.getAssignees(), toDoTaskEditForm.getDelegatees());
			
			toDoTaskProvider.updateTags(getIdentity(), toDoTask, toDoTaskEditForm.getTagDisplayNames());
		}
		
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

	private QualityToDoTaskProvider getToDoTaskProvider(String type) {
		return switch (type) {
		case GeneralToDoTaskProvider.TYPE -> generalProvider;
		case DataCollectionToDoTaskProvider.TYPE -> dataCollectionProvider;
		case EvaluationFormSessionToDoTaskProvider.TYPE -> sessionProvider;
		default -> null;
		};
	}

}
