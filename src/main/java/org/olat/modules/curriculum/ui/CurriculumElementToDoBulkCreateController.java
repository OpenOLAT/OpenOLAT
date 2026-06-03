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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.manager.CurriculumElementToDoProvider;
import org.olat.modules.todo.ui.ToDoTaskContextConfig;
import org.olat.modules.todo.ui.ToDoTaskEditForm;
import org.olat.modules.todo.ui.ToDoTaskMemberConfig;
import org.olat.user.IdentitySelectionSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 3 Jun 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementToDoBulkCreateController extends FormBasicController {

	private ToDoTaskEditForm toDoTaskEditForm;

	private final Collection<CurriculumElement> elements;

	@Autowired
	private CurriculumElementToDoProvider provider;

	public CurriculumElementToDoBulkCreateController(UserRequest ureq, WindowControl wControl,
			Collection<CurriculumElement> elements) {
		super(ureq, wControl, "todo_bulk_create");
		this.elements = elements;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Set<Identity> candidates = provider.getCandidateIntersection(elements);
		IdentitySelectionSource source = new IdentitySelectionSource(getLocale(), List.of(),
				() -> new ArrayList<>(candidates));

		ToDoTaskMemberConfig assigneeCfg = candidates.isEmpty()
				? ToDoTaskMemberConfig.disabled(source, false)
				: ToDoTaskMemberConfig.editable(source, false);
		ToDoTaskMemberConfig delegateeCfg = candidates.isEmpty()
				? ToDoTaskMemberConfig.disabled(source, false)
				: ToDoTaskMemberConfig.editable(source, false);

		toDoTaskEditForm = new ToDoTaskEditForm(ureq, getWindowControl(), mainForm,
				ToDoTaskContextConfig.off(null),
				assigneeCfg,
				delegateeCfg,
				provider.createBulkDateConfig(getLocale()),
				provider.getTagInfos(), true);
		listenTo(toDoTaskEditForm);
		formLayout.add("content", toDoTaskEditForm.getInitialFormItem());

		FormLayoutContainer buttons = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttons);
		uifactory.addFormSubmitButton("create", buttons);
		uifactory.addFormCancelButton("cancel", buttons, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		provider.createToDoTasks(getIdentity(), elements,
				toDoTaskEditForm.getTitle(),
				toDoTaskEditForm.getDescription(),
				toDoTaskEditForm.getStatus(),
				toDoTaskEditForm.getPriority(),
				toDoTaskEditForm.getStartDate(),
				toDoTaskEditForm.getDueDate(),
				toDoTaskEditForm.getRelativeDates(),
				toDoTaskEditForm.getExpenditureOfWork(),
				toDoTaskEditForm.getAssignees(),
				toDoTaskEditForm.getDelegatees(),
				toDoTaskEditForm.getTagDisplayNames());
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, FormEvent.CANCELLED_EVENT);
	}
}
