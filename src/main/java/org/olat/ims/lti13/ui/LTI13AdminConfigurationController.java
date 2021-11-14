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
package org.olat.ims.lti13.ui;

import java.util.Collection;
import java.util.List;

import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.lti13.LTI13Module;
import org.olat.ims.lti13.LTI13Roles;
import org.olat.user.ui.organisation.OrganisationAdminController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13AdminConfigurationController extends FormBasicController {

	private static final String[] ENABLED_KEY = new String[]{ "on" };
	
	private MultipleSelectionElement moduleEnabled;
	private TextElement platformIssEl;
	private SingleSelection organisationsEl;
	private MultipleSelectionElement deploymentRolesForEntriesEl;
	private MultipleSelectionElement deploymentRolesForGroupsEl;
	
	@Autowired
	private LTI13Module lti13Module;
	@Autowired
	private OrganisationService organisationService;
	
	public LTI13AdminConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(OrganisationAdminController.class, ureq.getLocale()));
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("LTI Integrations");
		
		String[] enabledValues = new String[]{ translate("enabled") };
		
		moduleEnabled = uifactory.addCheckboxesHorizontal("lti13.module.enabled", formLayout, ENABLED_KEY, enabledValues);
		moduleEnabled.select(ENABLED_KEY[0], lti13Module.isEnabled());
		moduleEnabled.addActionListener(FormEvent.ONCHANGE);
		
		String platformIss = lti13Module.getPlatformIss();
		platformIssEl = uifactory.addTextElement("lti13.platform.iss", "lti13.platform.iss", 255, platformIss, formLayout);
		platformIssEl.setEnabled(false);
		
		initOrganisationsEl(formLayout);
		
		SelectionValues rolesEntriesValues = new SelectionValues();
		rolesEntriesValues.add(SelectionValues.entry(LTI13Roles.author.name(), translate("role.author.plus")));
		rolesEntriesValues.add(SelectionValues.entry(LTI13Roles.learnresourcemanager.name(), translate("role.learnresourcemanager")));
		rolesEntriesValues.add(SelectionValues.entry(LTI13Roles.administrator.name(), translate("role.administrator")));
		deploymentRolesForEntriesEl = uifactory.addCheckboxesVertical("lti13.deployment.roles.entries", formLayout,
				rolesEntriesValues.keys(), rolesEntriesValues.values(), 1);
		List<LTI13Roles> rolesForEntries = lti13Module.getDeploymentRolesListForRepositoryEntries();
		for(LTI13Roles role:rolesForEntries) {
			if(rolesEntriesValues.containsKey(role.name())) {
				deploymentRolesForEntriesEl.select(role.name(), true);
			}
		}
		
		SelectionValues rolesGroupsValues = new SelectionValues();
		rolesGroupsValues.add(SelectionValues.entry(LTI13Roles.groupCoach.name(), translate("role.group.coach")));
		rolesGroupsValues.add(SelectionValues.entry(LTI13Roles.groupCoachAndAuthor.name(), translate("role.group.coach.author")));
		rolesGroupsValues.add(SelectionValues.entry(LTI13Roles.author.name(), translate("role.author")));
		rolesGroupsValues.add(SelectionValues.entry(LTI13Roles.groupmanager.name(), translate("role.groupmanager")));
		rolesGroupsValues.add(SelectionValues.entry(LTI13Roles.administrator.name(), translate("role.administrator")));
		deploymentRolesForGroupsEl = uifactory.addCheckboxesVertical("lti13.deployment.roles.groups", formLayout,
				rolesGroupsValues.keys(), rolesGroupsValues.values(), 1);
		List<LTI13Roles> rolesForGroups = lti13Module.getDeploymentRolesListForBusinessGroups();
		for(LTI13Roles role:rolesForGroups) {
			if(rolesGroupsValues.containsKey(role.name())) {
				deploymentRolesForGroupsEl.select(role.name(), true);
			}
		}
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
	}
	
	private void initOrganisationsEl(FormItemContainer formLayout) {
		List<Organisation> organisations = organisationService.getOrganisations(OrganisationStatus.notDelete());
		String defaultLtiOrgKey = lti13Module.getDefaultOrganisationKey();
		
		SelectionValues keyValues = new SelectionValues();
		for(Organisation organisation:organisations) {
			keyValues.add(SelectionValues.entry(organisation.getKey().toString(), organisation.getDisplayName()));
		}
		organisationsEl = uifactory.addDropdownSingleselect("organisations", "lti13.default.organisation", formLayout,
				keyValues.keys(), keyValues.values());
		
		if(StringHelper.containsNonWhitespace(defaultLtiOrgKey) && keyValues.containsKey(defaultLtiOrgKey)) {
			organisationsEl.select(defaultLtiOrgKey, true);
		} else {
			Organisation organisation = organisationService.getDefaultOrganisation();
			String organisationKey = organisation.getKey().toString();
			if(keyValues.containsKey(organisationKey)) {
				organisationsEl.select(organisationKey, true);
			}
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		organisationsEl.clearError();
		deploymentRolesForEntriesEl.clearError();
		deploymentRolesForGroupsEl.clearError();
		if(moduleEnabled.isAtLeastSelected(1)) {
			if(!organisationsEl.isOneSelected()) {
				organisationsEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		
			if(!deploymentRolesForEntriesEl.isAtLeastSelected(1)) {
				deploymentRolesForEntriesEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
			
			if(!deploymentRolesForGroupsEl.isAtLeastSelected(1)) {
				deploymentRolesForGroupsEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(moduleEnabled == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateUI() {
		boolean enabled = moduleEnabled.isAtLeastSelected(1);
		platformIssEl.setVisible(enabled);
		organisationsEl.setVisible(enabled);
		deploymentRolesForEntriesEl.setVisible(enabled);
		deploymentRolesForGroupsEl.setVisible(enabled);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = moduleEnabled.isAtLeastSelected(1);
		lti13Module.setEnabled(enabled);
		
		String selectedOrganisationKey = organisationsEl.getSelectedKey();
		lti13Module.setDefaultOrganisationKey(selectedOrganisationKey);
		
		String rolesForEntries = toString(deploymentRolesForEntriesEl.getSelectedKeys());
		lti13Module.setDeploymentRolesForRepositoryEntries(rolesForEntries);
		String rolesForGroups = toString(deploymentRolesForGroupsEl.getSelectedKeys());
		lti13Module.setDeploymentRolesForBusinessGroups(rolesForGroups);
	}
	
	private String toString(Collection<String> selectedKeys) {
		StringBuilder sb = new StringBuilder();
		for(String selectedKey:selectedKeys)  {
			if(LTI13Roles.valid(selectedKey)) {
				if(sb.length() > 0) {
					sb.append(",");
				}
				sb.append(selectedKey);
			}
		}
		return sb.toString();
	}
}
