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
package org.olat.modules.project.ui;

import java.util.List;
import java.util.Set;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.todo.ToDoRole;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoTaskMembers;
import org.olat.modules.todo.ui.ToDoTaskEditForm;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 Feb 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjToDoContentEditController extends FormBasicController {

	private ToDoTaskEditForm toDoTaskEditForm;

	private final ProjProject project;
	private final boolean template;
	private ProjToDo toDo;
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ToDoService toDoService;

	public ProjToDoContentEditController(UserRequest ureq, WindowControl wControl, Form mainForm, ProjProject project, ProjToDo toDo) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, mainForm);
		this.project = project;
		this.template = project.isTemplatePrivate() || project.isTemplatePublic();
		this.toDo = toDo;
		
		initForm(ureq);
	}

	public ProjToDo getToDo() {
		return toDo;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<Identity> projectMembers = projectService.getMembers(project, ProjectRole.PROJECT_ROLES);
		Set<Identity> assignees = Set.of(getIdentity());
		Set<Identity> delegatees = Set.of();
		if (toDo != null) {
			ToDoTaskMembers toDoTaskMembers = toDoService
					.getToDoTaskGroupKeyToMembers(List.of(toDo.getToDoTask()), ToDoRole.ASSIGNEE_DELEGATEE)
					.get(toDo.getToDoTask().getBaseGroup().getKey());
			assignees = toDoTaskMembers.getMembers(ToDoRole.assignee);
			delegatees = toDoTaskMembers.getMembers(ToDoRole.delegatee);
		}
		
		List<TagInfo> tagInfos = projectService.getTagInfos(project, toDo != null? toDo.getArtefact(): null);
		
		toDoTaskEditForm = new ToDoTaskEditForm(ureq, getWindowControl(), mainForm,
				toDo != null ? toDo.getToDoTask() : null, false, null, null, projectMembers, false, assignees,
				delegatees, !template, tagInfos, !template);
		listenTo(toDoTaskEditForm);
		formLayout.add(toDoTaskEditForm.getInitialFormItem());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (toDo == null) {
			toDo = projectService.createToDo(getIdentity(), project);
		}
		
		projectService.updateToDo(getIdentity(), toDo,
				toDoTaskEditForm.getTitle(),
				toDoTaskEditForm.getStatus(),
				toDoTaskEditForm.getPriority(),
				toDoTaskEditForm.getStartDate(),
				toDoTaskEditForm.getDueDate(),
				toDoTaskEditForm.getExpenditureOfWork(),
				toDoTaskEditForm.getDescription());
		
		projectService.updateMembers(getIdentity(), toDo, toDoTaskEditForm.getAssignees(), toDoTaskEditForm.getDelegatees());
		
		projectService.updateTags(getIdentity(), toDo, toDoTaskEditForm.getTagDisplayNames());
	}

}
