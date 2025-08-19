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

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
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
import org.olat.course.member.MemberSearchConfig;
import org.olat.course.member.MemberSearchController;
import org.olat.course.todo.model.ToDoTaskCollectionCreateContext;

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
	private FormLayoutContainer membersCont;
	private MemberSearchController memberSearchController;
	
	private final ToDoTaskCollectionCreateContext context;
	

	public ToDoCollectionCreateAssigneeController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
		
		context = (ToDoTaskCollectionCreateContext)getFromRunContext(ToDoTaskCollectionCreateContext.KEY);
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer generalCont = uifactory.addDefaultFormLayout("general", null, formLayout);
		generalCont.setRootForm(mainForm);
		generalCont.setFormTitle(translate("course.todo.collection.assignees.step"));
		
		SelectionValues selectionSV = new SelectionValues();
		selectionSV.add(SelectionValues.entry(KEY_ALL, translate("course.todo.collection.assignees.selection.all"),
				translate("course.todo.collection.assignees.selection.all.desc"), null, null, true));
		selectionSV.add(
				SelectionValues.entry(KEY_SELECTION, translate("course.todo.collection.assignees.selection.individual"),
						translate("course.todo.collection.assignees.selection.individual.desc"), null, null, true));
		selectionEl = uifactory.addCardSingleSelectHorizontal("selection", "course.todo.collection.assignees.selection", generalCont, selectionSV);
		selectionEl.addActionListener(FormEvent.ONCHANGE);
		String selectionKey = context.isAssigneesSelected()? KEY_SELECTION: KEY_ALL;
		selectionEl.select(selectionKey, true);
		
		membersCont = uifactory.addDefaultFormLayout("members", null, formLayout);
		membersCont.setRootForm(mainForm);
		membersCont.setFormTitle(translate("course.todo.search.members"));
		
		GroupRoles searchAs = context.isCoach() ? GroupRoles.coach : GroupRoles.owner;
		MemberSearchConfig config = MemberSearchConfig.defaultConfig(context.getRepositoryEntry(), searchAs, "to-do-assignees-v1.0")
				.showSelectButton(false);
		if (context.getAssigneeKeys() != null) {
			config = config.preselectedIdentitiesKeys(context.getAssigneeKeys());
		}
		memberSearchController = new MemberSearchController(ureq, getWindowControl(), mainForm, config);
		listenTo(memberSearchController);
		memberSearchController.getInitialFormItem().setFormLayout("0_12");
		membersCont.add(memberSearchController.getInitialFormItem());
		
		
	}
	
	private void updateUI() {
		membersCont.setVisible(selectionEl.isKeySelected(KEY_SELECTION));
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
		if (membersCont.isVisible()) {
			List<Long> selectedIdentities = memberSearchController.getSelectedIdentities()
					.stream().map(Identity::getKey).toList();
			context.setAssigneeKeys(selectedIdentities);
		}
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

}
