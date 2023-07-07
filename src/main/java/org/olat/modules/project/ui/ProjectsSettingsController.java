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

import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.controllers.accordion.AssistanceAccordionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.project.ProjectModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjectsSettingsController extends FormBasicController {
	
	private static final String KEY_ALL = "all";
	private static final String KEY_ROLE_BASED = "role";
	
	private ProjectsRolesAssistanceController rolesAssistanceCtrl;
	private AssistanceAccordionController assistanceCtrl;

	private FormToggle enabledEl;
	private SingleSelection createAllowedEl;
	private MultipleSelectionElement createRolesEl;
	
	@Autowired
	private ProjectModule projectModule;

	public ProjectsSettingsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer settingsCont = FormLayoutContainer.createDefaultFormLayout("settings", getTranslator());
		settingsCont.setFormTitle(translate("admin.settings"));
		settingsCont.setRootForm(mainForm);
		formLayout.add(settingsCont);
		
		enabledEl = uifactory.addToggleButton("admin.enabled", "admin.enabled", null, null, settingsCont);
		enabledEl.addActionListener(FormEvent.ONCHANGE);
		if (projectModule.isEnabled()) {
			enabledEl.toggleOn();
		} else {
			enabledEl.toggleOff();
		}
		
		FormLayoutContainer rightsCont = FormLayoutContainer.createDefaultFormLayout("rights", getTranslator());
		rightsCont.setFormTitle(translate("admin.rights"));
		rightsCont.setRootForm(mainForm);
		formLayout.add(rightsCont);
		
		SelectionValues createAllowedSV = new SelectionValues();
		createAllowedSV.add(SelectionValues.entry(KEY_ALL, translate("admin.create.all")));
		createAllowedSV.add(SelectionValues.entry(KEY_ROLE_BASED, translate("admin.create.role.based")));
		createAllowedEl = uifactory.addRadiosVertical("admin.create.allowed", rightsCont, createAllowedSV.keys(), createAllowedSV.values());
		createAllowedEl.addActionListener(FormEvent.ONCHANGE);
		if (projectModule.getCreateRoles().isEmpty()) {
			createAllowedEl.select(KEY_ALL, true);
		} else {
			createAllowedEl.select(KEY_ROLE_BASED, true);
		}
		
		SelectionValues createRolesSV = new SelectionValues();
		createRolesSV.add(SelectionValues.entry(OrganisationRoles.administrator.name(), translate("admin.role.administrator")));
		createRolesSV.add(SelectionValues.entry(OrganisationRoles.projectmanager.name(), translate("admin.role.project.manager")));
		createRolesSV.add(SelectionValues.entry(OrganisationRoles.author.name(), translate("admin.role.author")));
		createRolesEl = uifactory.addCheckboxesVertical("admin.roles", rightsCont, createRolesSV.keys(), createRolesSV.values(), 1);
		createRolesEl.addActionListener(FormEvent.ONCHANGE);
		createRolesEl.setEnabled(OrganisationRoles.administrator.name(), false);
		createRolesEl.select(OrganisationRoles.administrator.name(), true);
		if (projectModule.getCreateRoles() != null) {
			projectModule.getCreateRoles().forEach(role -> createRolesEl.select(role.name(), true));
		}
		
		rolesAssistanceCtrl = new ProjectsRolesAssistanceController(ureq, getWindowControl());
		assistanceCtrl = new AssistanceAccordionController(ureq, getWindowControl(), getTranslator(), "assistance");
		assistanceCtrl.setCssClass("o_proj_roles_assistance");
		listenTo(assistanceCtrl);
		flc.add("assistance", new ComponentWrapperElement(assistanceCtrl.getInitialComponent()));
		assistanceCtrl.addQuestionAnswer("roles.rights", null, new Component[] {rolesAssistanceCtrl.getInitialComponent()});
	}

	private void updateUI() {
		createRolesEl.setVisible(createAllowedEl.isOneSelected() && createAllowedEl.isKeySelected(KEY_ROLE_BASED));
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enabledEl) {
			projectModule.setEnabled(enabledEl.isOn());
		} else if (source == createAllowedEl) {
			doUpdateCreateRoles();
			updateUI();
		} else if (source == createRolesEl) {
			doUpdateCreateRoles();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doUpdateCreateRoles() {
		boolean createRestriced = createAllowedEl.isOneSelected() && createAllowedEl.isKeySelected(KEY_ROLE_BASED);
		Set<OrganisationRoles> createRoles = createRestriced
				? createRolesEl.getSelectedKeys().stream()
						.map(OrganisationRoles::valueOf)
						.collect(Collectors.toSet())
				: null;
		projectModule.setCreateRoles(createRoles);
	}

}
