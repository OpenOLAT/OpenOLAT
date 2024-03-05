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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultiSelectionFilterElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.todo.manager.CourseCollectionToDoTaskProvider;
import org.olat.course.todo.model.ToDoTaskCollectionCreateContext;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Jan 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoCollectionCreateAssigneeController extends StepFormBasicController {
	
	private static final String KEY_ALL = "all";
	private static final String KEY_SELECTION = "selection";

	private SingleSelection selectionEl;
	private MultiSelectionFilterElement participantsEl;
	
	private final ToDoTaskCollectionCreateContext context;
	
	@Autowired
	private CourseCollectionToDoTaskProvider courseCollectionToDoTaskProvider;
	@Autowired
	private UserManager userManager;

	public ToDoCollectionCreateAssigneeController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		
		context = (ToDoTaskCollectionCreateContext)getFromRunContext(ToDoTaskCollectionCreateContext.KEY);
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("course.todo.collection.assignees.step");
		
		FormLayoutContainer generalCont = FormLayoutContainer.createDefaultFormLayout("general", getTranslator());
		generalCont.setRootForm(mainForm);
		formLayout.add(generalCont);
		
		SelectionValues selectionSV = new SelectionValues();
		selectionSV.add(SelectionValues.entry(KEY_ALL, translate("course.todo.collection.assignees.selection.all"),
				translate("course.todo.collection.assignees.selection.all.desc"), null, null, true));
		selectionSV.add(
				SelectionValues.entry(KEY_SELECTION, translate("course.todo.collection.assignees.selection.individual"),
						translate("course.todo.collection.assignees.selection.individual.desc"), null, null, true));
		selectionEl = uifactory.addCardSingleSelectHorizontal("selection", "course.todo.collection.assignees.selection", formLayout, selectionSV);
		selectionEl.addActionListener(FormEvent.ONCHANGE);
		String selectionKey = context.isAssigneesSelected()? KEY_SELECTION: KEY_ALL;
		selectionEl.select(selectionKey, true);
		
		SelectionValues participantSV = new SelectionValues();
		List<Identity> participants = courseCollectionToDoTaskProvider.getAssigneeCandidates(getIdentity(), context.getRepositoryEntry(), context.isCoach());
		participants.forEach(participant -> participantSV.add(SelectionValues.entry(
						participant.getKey().toString(),
						StringHelper.escapeHtml(userManager.getUserDisplayName(participant)))));
		participantsEl = uifactory.addCheckboxesFilterDropdown("participants",
				"course.todo.collection.assignees.participants", formLayout, getWindowControl(), participantSV);
		if (context.getAssigneeKeys() != null) {
			context.getAssigneeKeys().forEach(key -> participantsEl.select(key.toString(), true));
		}
	}
	
	private void updateUI() {
		participantsEl.setVisible(selectionEl.isKeySelected(KEY_SELECTION));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == selectionEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		context.setAssigneesSelected(selectionEl.isKeySelected(KEY_SELECTION));
		if (participantsEl.isVisible()) {
			context.setAssigneeKeys(participantsEl.getSelectedKeys().stream().map(Long::valueOf).toList());
		}
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

}
