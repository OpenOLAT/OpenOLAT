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

import java.util.Set;
import java.util.stream.Collectors;

import org.olat.admin.user.tools.UserToolsModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.todo.ToDoExtension;
import org.olat.modules.todo.ToDoModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 Oct 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoAdminSettingsController extends FormBasicController {
	
	private FormToggle userToolEnableEl;
	private SingleSelection createEl;
	private SingleSelection assigneeEl;
	private SingleSelection delegateeEl;
	
	@Autowired
	private UserToolsModule userToolsModule;
	@Autowired
	private ToDoModule toDoModule;

	public ToDoAdminSettingsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.user.tool.title");
		
		boolean userToolEnabled = userToolsModule.getAvailableUserToolSet().stream()
				.anyMatch(id -> ToDoExtension.TODO_USER_TOOL_ID.equals(id));
		userToolEnableEl = uifactory.addToggleButton("admin.user.tool.enabled", "enabled", translate("on"), translate("off"), formLayout);
		userToolEnableEl.toggle(userToolEnabled);
		userToolEnableEl.addActionListener(FormEvent.ONCHANGE);
		
		SelectionValues createValues = new SelectionValues();
		createValues.add(SelectionValues.entry(ToDoModule.PERSONAL_CREATE_ALL, translate("admin.user.tool.all.users")));
		createValues.add(SelectionValues.entry(ToDoModule.PERSONAL_CREATE_AUTHORS_MANAGERS, translate("admin.user.tool.authors.managers")));
		createValues.add(SelectionValues.entry(ToDoModule.PERSONAL_CREATE_NONE, translate("admin.user.tool.disabled")));
		createEl = uifactory.addCardSingleSelectHorizontal("admin.user.tool.create", "admin.user.tool.create", formLayout, createValues);
		createEl.select(toDoModule.getPersonalCreate(), true);
		createEl.addActionListener(FormEvent.ONCHANGE);
		
		SelectionValues candidateValues = new SelectionValues();
		candidateValues.add(SelectionValues.entry(ToDoModule.PERSONAL_CANDIDATE_ALL, translate("admin.user.tool.all.users")));
		candidateValues.add(SelectionValues.entry(ToDoModule.PERSONAL_CANDIDATE_BUDDIES, translate("admin.user.tool.buddies")));
		candidateValues.add(SelectionValues.entry(ToDoModule.PERSONAL_CANDIDATE_NONE, translate("admin.user.tool.disabled")));
		assigneeEl = uifactory.addCardSingleSelectHorizontal("admin.user.tool.assignee", "admin.user.tool.assignee", formLayout, candidateValues);
		assigneeEl.select(toDoModule.getPersonalAssigneeCandidate(), true);
		assigneeEl.addActionListener(FormEvent.ONCHANGE);
		
		delegateeEl = uifactory.addCardSingleSelectHorizontal("admin.user.tool.delegatee", "admin.user.tool.delegatee", formLayout, candidateValues);
		delegateeEl.select(toDoModule.getPersonalDelegateeCandidate(), true);
		delegateeEl.addActionListener(FormEvent.ONCHANGE);
	}

	private void updateUI() {
		boolean userToolEnabled = userToolEnableEl.isOn();
		createEl.setVisible(userToolEnabled);
		assigneeEl.setVisible(userToolEnabled);
		delegateeEl.setVisible(userToolEnabled);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == userToolEnableEl) {
			updateUserToolEnabled(userToolEnableEl.isOn());
			updateUI();
		} else if (source == createEl) {
			toDoModule.setPersonalCreate(createEl.getSelectedKey());
		} else if (source == assigneeEl) {
			toDoModule.setPersonalAssigneeCandidate(assigneeEl.getSelectedKey());
		} else if (source == delegateeEl) {
			toDoModule.setPersonalDelegateeCandidate(delegateeEl.getSelectedKey());
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void updateUserToolEnabled(boolean enabled) {
		Set<String> availableTools = userToolsModule.getAvailableUserToolSet();
		if (!availableTools.isEmpty()) {
			if (enabled && !availableTools.contains(ToDoExtension.TODO_USER_TOOL_ID)) {
				availableTools.add(ToDoExtension.TODO_USER_TOOL_ID);
			}
			
			if (!enabled && availableTools.contains(ToDoExtension.TODO_USER_TOOL_ID)) {
				availableTools.remove(ToDoExtension.TODO_USER_TOOL_ID);
			}
			
			String tools = availableTools.stream().collect(Collectors.joining(","));
			userToolsModule.setAvailableUserTools(tools);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
