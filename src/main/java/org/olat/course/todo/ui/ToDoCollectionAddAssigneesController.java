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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultiSelectionFilterElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.course.todo.manager.CourseCollectionToDoTaskProvider;
import org.olat.modules.todo.ToDoTask;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 Jan 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ToDoCollectionAddAssigneesController extends FormBasicController {
	
	private static final String KEY_ALL = "all";
	private static final String KEY_SELECTION = "selection";

	private SingleSelection selectionEl;
	private MultiSelectionFilterElement participantsEl;

	private final ToDoTask toDoTaskCollection;
	private final boolean coachOnly;
	
	@Autowired
	private CourseCollectionToDoTaskProvider courseCollectionToDoTaskProvider;
	@Autowired
	private UserManager userManager;

	protected ToDoCollectionAddAssigneesController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTaskCollection, boolean coachOnly) {
		super(ureq, wControl);
		this.toDoTaskCollection = toDoTaskCollection;
		this.coachOnly = coachOnly;
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<Identity> participants = courseCollectionToDoTaskProvider.getAssigneeCandidates(getIdentity(), () -> toDoTaskCollection.getOriginId(), coachOnly);
		Set<Long> toDoAssigneeKeys = courseCollectionToDoTaskProvider.getCollectionElementAssigneeKeys(toDoTaskCollection);
		List<Identity> participantsWithoutToDoTask = participants.stream()
			.filter(participant -> !toDoAssigneeKeys.contains(participant.getKey()))
			.toList();
		
		SelectionValues selectionSV = new SelectionValues();
		selectionSV.add(SelectionValues.entry(KEY_ALL,
				translate("course.todo.collection.add.participants.selection.all"),
				translate("course.todo.collection.add.participants.selection.all.desc", String.valueOf(participantsWithoutToDoTask.size())), null, null, true));
		selectionSV.add(
				SelectionValues.entry(KEY_SELECTION,
						translate("course.todo.collection.add.participants.selection.individual"),
						translate("course.todo.collection.add.participants.selection.individual.desc"), null, null, true));
		selectionEl = uifactory.addCardSingleSelectHorizontal("selection", "course.todo.collection.add.participants.selection", formLayout, selectionSV);
		selectionEl.addActionListener(FormEvent.ONCHANGE);
		selectionEl.select(KEY_ALL, true);
		
		SelectionValues participantSV = new SelectionValues();
		participantsWithoutToDoTask
				.forEach(participant -> participantSV.add(SelectionValues.entry(
						participant.getKey().toString(),
						userManager.getUserDisplayName(participant))));
		participantsEl = uifactory.addCheckboxesFilterDropdown("participants",
				"course.todo.collection.add.participants.participants", formLayout, getWindowControl(), participantSV);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("course.todo.collection.add.participants", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
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
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, FormEvent.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (KEY_ALL.equals(selectionEl.getSelectedKey())) {
			courseCollectionToDoTaskProvider.addRemainingAssignees(getIdentity(), toDoTaskCollection, coachOnly);
		} else {
			Collection<Long> identityKeys = participantsEl.getSelectedKeys().stream().map(Long::valueOf).toList();
			courseCollectionToDoTaskProvider.addRemainingAssignees(getIdentity(), toDoTaskCollection, identityKeys);
		}
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

}
