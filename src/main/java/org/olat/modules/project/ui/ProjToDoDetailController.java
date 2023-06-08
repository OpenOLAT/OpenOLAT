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

import org.olat.core.commons.services.tag.Tag;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ui.event.OpenArtefactEvent;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ui.ToDoTaskDetailsController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjToDoDetailController extends FormBasicController {

	private ToDoTaskDetailsController toDoTaskDetailsCtrl;
	private ProjArtefactReferencesController referenceCtrl;
	
	private final ToDoTaskSecurityCallback secCallback;
	private final ToDoTask toDoTask;
	private final List<Tag> tags;
	private final Identity modifier;
	private final Set<Identity> assignees;
	private final Set<Identity> delegatees;
	private final ProjToDo toDo;
	
	@Autowired
	private ProjectService projectService;

	public ProjToDoDetailController(UserRequest ureq, WindowControl wControl, Form mainForm,
			ToDoTaskSecurityCallback secCallback, ToDoTask toDoTask, List<Tag> tags, Identity modifier,
			Set<Identity> assignees, Set<Identity> delegatees) {
		super(ureq, wControl, LAYOUT_CUSTOM, "todo_detail", mainForm);
		this.secCallback = secCallback;
		this.toDoTask = toDoTask;
		this.tags = tags;
		this.modifier = modifier;
		this.assignees = assignees;
		this.delegatees = delegatees;
		this.toDo = projectService.getToDo(toDoTask.getOriginSubPath());
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		toDoTaskDetailsCtrl = new ToDoTaskDetailsController(ureq, getWindowControl(), mainForm, secCallback, toDoTask, tags,
				modifier, assignees, delegatees);
		listenTo(toDoTaskDetailsCtrl);
		formLayout.add("toToTask", toDoTaskDetailsCtrl.getInitialFormItem());
		
		referenceCtrl = new ProjArtefactReferencesController(ureq, getWindowControl(), mainForm,
				toDo.getArtefact().getProject(), toDo.getArtefact(), false, true, true);
		listenTo(referenceCtrl);
		formLayout.add("references", referenceCtrl.getInitialFormItem());
		flc.contextPut("hasReferences", Boolean.valueOf(referenceCtrl.getNumReferences() > 0));
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == toDoTaskDetailsCtrl) {
			fireEvent(ureq, event);
		} else if (source == referenceCtrl && event instanceof OpenArtefactEvent) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
