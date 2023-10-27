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
import org.olat.modules.todo.ui.ToDoTaskEditForm.MemberSelection;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityToDoEditController extends FormBasicController {
	
	private ToDoTaskEditForm toDoTaskEditForm;
	private QualityToDoMetadataController metadataCtrl;
	
	private final boolean showContext;
	private final Collection<ToDoContext> availableContexts;
	private final ToDoContext currentContext;
	private Long dataCollectionId;
	private String originSubPath;
	private ToDoTask toDoTask;
	private Boolean metadataOpen = Boolean.FALSE;

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
		this.showContext = true;
		this.availableContexts = availableContexts;
		this.currentContext = currentContext;

		initForm(ureq);
	}

	public QualityToDoEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask, boolean showContext) {
		super(ureq, wControl, "todo_edit");
		this.toDoTask = toDoTask;
		this.showContext = showContext;
		this.availableContexts = List.of(toDoTask);
		this.currentContext = toDoTask;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Identity creator = null;
		Identity modifier = null;
		Set<Identity> memberCandidates = Set.of(getIdentity());
		Set<Identity> assignees = Set.of(getIdentity());
		Set<Identity> delegatees = Set.of();
		List<TagInfo> tagInfos;
		ToDoTaskSearchParams tagSearchParams = new ToDoTaskSearchParams();
		tagSearchParams.setTypes(QualityToDoTaskProvider.ALL_TYPES);
		
		if (toDoTask != null) {
			ToDoTaskMembers toDoTaskMembers = toDoService
					.getToDoTaskGroupKeyToMembers(List.of(toDoTask), ToDoRole.ALL)
					.get(toDoTask.getBaseGroup().getKey());
			creator = toDoTaskMembers.getMembers(ToDoRole.creator).stream().findAny().orElse(null);
			modifier = toDoTaskMembers.getMembers(ToDoRole.modifier).stream().findAny().orElse(null);
			assignees = toDoTaskMembers.getMembers(ToDoRole.assignee);
			delegatees = toDoTaskMembers.getMembers(ToDoRole.delegatee);
			memberCandidates = new HashSet<>();
			memberCandidates.addAll(assignees);
			memberCandidates.addAll(delegatees);
			
			tagInfos = toDoService.getTagInfos(tagSearchParams, toDoTask);
		} else {
			tagInfos = toDoService.getTagInfos(tagSearchParams, null);
		}
		
		toDoTaskEditForm = new ToDoTaskEditForm(ureq, getWindowControl(), mainForm, toDoTask, showContext,
				availableContexts, currentContext, MemberSelection.search, memberCandidates, assignees,
				MemberSelection.search, memberCandidates, delegatees, tagInfos, true);
		listenTo(toDoTaskEditForm);
		formLayout.add("content", toDoTaskEditForm.getInitialFormItem());
		
		if (toDoTask != null) {
			metadataCtrl = new QualityToDoMetadataController(ureq, getWindowControl(), mainForm, toDoTask, creator,
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
