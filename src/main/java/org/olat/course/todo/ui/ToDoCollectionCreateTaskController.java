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
package org.olat.course.todo.ui;

import java.util.List;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.todo.CourseToDoService;
import org.olat.course.todo.model.ToDoTaskCollectionCreateContext;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ui.ToDoTaskEditForm;
import org.olat.modules.todo.ui.ToDoTaskEditForm.CopyValues;
import org.olat.modules.todo.ui.ToDoTaskEditForm.MemberSelection;
import org.olat.modules.todo.ui.ToDoTaskEditForm.ToDoTaskValues;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Jan 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoCollectionCreateTaskController extends StepFormBasicController {

	private ToDoTaskEditForm toDoTaskEditForm;
	
	private final ToDoTask sourceToDoTask;
	private final boolean convert;
	private final ToDoTaskCollectionCreateContext context;
	
	@Autowired
	private CourseToDoService courseToDoService;
	@Autowired
	private ToDoService toDoService;

	public ToDoCollectionCreateTaskController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, ToDoTask sourceToDoTask, boolean convert) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		this.sourceToDoTask = sourceToDoTask;
		this.convert = convert;
		
		context = (ToDoTaskCollectionCreateContext)getFromRunContext(ToDoTaskCollectionCreateContext.KEY);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("course.todo.collection.todo.step");
		
		List<TagInfo> tagInfos = toDoService.getTagInfos(courseToDoService.createCourseTagSearchParams(context.getRepositoryEntry()), sourceToDoTask);
		
		toDoTaskEditForm = new ToDoTaskEditForm(ureq, getWindowControl(), mainForm, false, List.of(), null,
				MemberSelection.disabled, List.of(), List.of(), MemberSelection.disabled, List.of(), List.of(),
				tagInfos, true);
		if (sourceToDoTask != null) {
			toDoTaskEditForm.setValues(convert ? new ToDoTaskValues(sourceToDoTask) : new CopyValues(getLocale(), sourceToDoTask));
		}
		listenTo(toDoTaskEditForm);
		formLayout.add("content", toDoTaskEditForm.getInitialFormItem());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		context.setTitle(toDoTaskEditForm.getTitle());
		context.setDescription(toDoTaskEditForm.getDescription());
		context.setStatus(toDoTaskEditForm.getStatus());
		context.setPriority(toDoTaskEditForm.getPriority());
		context.setExpenditureOfWork(toDoTaskEditForm.getExpenditureOfWork());
		context.setStartDate(toDoTaskEditForm.getStartDate());
		context.setDueDate(toDoTaskEditForm.getDueDate());
		context.setTagDisplayNames(toDoTaskEditForm.getTagDisplayNames());
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

}
