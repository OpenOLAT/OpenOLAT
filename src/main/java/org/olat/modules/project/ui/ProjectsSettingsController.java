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
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
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
	
	private static final String[] ON_KEYS = new String[] {"on"};

	@Autowired
	private ProjectModule projectModule;

	private MultipleSelectionElement enabledEl;

	private MultipleSelectionElement createRestrictedEl;

	private MultipleSelectionElement createRolesEl;
	
	public ProjectsSettingsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.settings");
		
		String[] onValues = new String[] { translate("on") };
		enabledEl = uifactory.addCheckboxesVertical("admin.enabled", formLayout, ON_KEYS, onValues, 1);
		enabledEl.addActionListener(FormEvent.ONCHANGE);
		enabledEl.select(enabledEl.getKey(0), projectModule.isEnabled());
		
		createRestrictedEl = uifactory.addCheckboxesVertical("admin.create.roles.restricted", formLayout, ON_KEYS, onValues, 1);
		createRestrictedEl.addActionListener(FormEvent.ONCHANGE);
		createRestrictedEl.select(createRestrictedEl.getKey(0), !projectModule.isCreateAllRoles());
		
		SelectionValues createRolesSV = new SelectionValues();
		createRolesSV.add(SelectionValues.entry(OrganisationRoles.author.name(), translate("admin.role.authors")));
		createRolesEl = uifactory.addCheckboxesVertical("admin.create.roles", formLayout, createRolesSV.keys(), createRolesSV.values(), 1);
		createRolesEl.setHelpTextKey("admin.create.roles.help", null);
		createRolesEl.addActionListener(FormEvent.ONCHANGE);
		if (projectModule.getCreateRoles() != null) {
			projectModule.getCreateRoles().forEach(role -> createRolesEl.select(role.name(), true));
		}
	}

	private void updateUI() {
		createRolesEl.setVisible(createRestrictedEl.isAtLeastSelected(1));
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enabledEl) {
			projectModule.setEnabled(enabledEl.isAtLeastSelected(1));
		} else if (source == createRestrictedEl) {
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
		boolean createRestriced = createRestrictedEl.isAtLeastSelected(1);
		projectModule.setCreateAllRoles(!createRestriced);
		
		Set<OrganisationRoles> createRoles = createRestriced
				? createRolesEl.getSelectedKeys().stream().map(OrganisationRoles::valueOf).collect(Collectors.toSet())
				: null;
		projectModule.setCreateRoles(createRoles);
	}

}
