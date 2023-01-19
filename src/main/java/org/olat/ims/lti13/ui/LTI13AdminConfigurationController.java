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

import java.util.List;

import org.olat.basesecurity.OrganisationRoles;
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
import org.olat.ims.lti13.DeploymentConfigurationPermission;
import org.olat.ims.lti13.LTI13Module;
import org.olat.modules.invitation.InvitationConfigurationPermission;
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
	private SingleSelection entryOwnerPermissionEl;
	private SingleSelection businessGroupCoachPermissionEl;
	private MultipleSelectionElement rolesEntryEl;
	private MultipleSelectionElement rolesBusinessGroupEl;
	private FormLayoutContainer repositoryEntryCont;
	private FormLayoutContainer businessGroupCont;
	
	@Autowired
	private LTI13Module lti13Module;
	@Autowired
	private OrganisationService organisationService;
	
	public LTI13AdminConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE, Util.createPackageTranslator(OrganisationAdminController.class, ureq.getLocale()));
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer settingsCont = FormLayoutContainer.createDefaultFormLayout("settings", getTranslator());
		settingsCont.setElementCssClass("o_sel_lti13_admin_settings");
		formLayout.add(settingsCont);
		settingsCont.setFormContextHelp("manual_admin/administration/LTI_Integrations/");
		
		String[] enabledValues = new String[]{ translate("enabled") };
		
		moduleEnabled = uifactory.addCheckboxesHorizontal("lti13.module.enabled", settingsCont, ENABLED_KEY, enabledValues);
		moduleEnabled.setElementCssClass("o_sel_lti13_admin_enable");
		moduleEnabled.select(ENABLED_KEY[0], lti13Module.isEnabled());
		moduleEnabled.addActionListener(FormEvent.ONCHANGE);
		
		String platformIss = lti13Module.getPlatformIss();
		platformIssEl = uifactory.addTextElement("lti13.platform.iss", "lti13.platform.iss", 255, platformIss, settingsCont);
		platformIssEl.setEnabled(false);
		
		initOrganisationsEl(settingsCont);
	
		repositoryEntryCont = FormLayoutContainer.createDefaultFormLayout("entries", getTranslator());
		formLayout.add(repositoryEntryCont);
		initRepositoryEntryForm(repositoryEntryCont);
		
		businessGroupCont = FormLayoutContainer.createDefaultFormLayout("groups", getTranslator());
		formLayout.add(businessGroupCont);
		initBusinessGroupForm(businessGroupCont);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createDefaultFormLayout("buttons", getTranslator());
		buttonLayout.setElementCssClass("o_sel_lti13_admin_buttons");
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
	}
	

	private void initRepositoryEntryForm(FormLayoutContainer formLayout) {
		formLayout.setFormTitle(translate("admin.entry.title"));
		
		SelectionValues rolesKeyValues = new SelectionValues();
		rolesKeyValues.add(SelectionValues.entry(OrganisationRoles.administrator.name(),
				translate("role." + OrganisationRoles.administrator.name())));
		rolesKeyValues.add(SelectionValues.entry(OrganisationRoles.learnresourcemanager.name(),
				translate("role." + OrganisationRoles.learnresourcemanager.name())));
		rolesEntryEl= uifactory.addCheckboxesVertical("roles.entry.deployment", "roles.entry.deployment", formLayout,
				rolesKeyValues.keys(), rolesKeyValues.values(), 1);
		rolesEntryEl.setEnabled(OrganisationRoles.administrator.name(), false);
		
		List<String> roles = lti13Module.getDeploymentRepositoryEntryRolesConfigurationList();
		for(String role:roles) {
			if(rolesKeyValues.containsKey(role)) {
				rolesEntryEl.select(role, true);
			}
		}
		
		SelectionValues permissionsKeyValues = new SelectionValues();
		permissionsKeyValues.add(SelectionValues.entry(DeploymentConfigurationPermission.allResources.name(), translate("activate.all.courses")));
		permissionsKeyValues.add(SelectionValues.entry(DeploymentConfigurationPermission.perResource.name(), translate("activate.per.course")));
		entryOwnerPermissionEl = uifactory.addRadiosVertical("repo.owner.permission", "repo.owner.permission", formLayout,
				permissionsKeyValues.keys(), permissionsKeyValues.values());
		entryOwnerPermissionEl.setHelpText(translate("course.owner.permission.help"));
		
		DeploymentConfigurationPermission permission = lti13Module.getDeploymentRepositoryEntryOwnerPermission();
		if(permission != null && permissionsKeyValues.containsKey(permission.name())) {
			entryOwnerPermissionEl.select(permission.name(), true);
		}
	}

	private void initBusinessGroupForm(FormLayoutContainer formLayout) {
		formLayout.setFormTitle(translate("admin.group.title"));
		
		SelectionValues rolesKeyValues = new SelectionValues();
		rolesKeyValues.add(SelectionValues.entry(OrganisationRoles.administrator.name(),
				translate("role." + OrganisationRoles.administrator.name())));
		rolesKeyValues.add(SelectionValues.entry(OrganisationRoles.groupmanager.name(),
				translate("role." + OrganisationRoles.groupmanager.name())));
		rolesBusinessGroupEl = uifactory.addCheckboxesVertical("roles.business.group.deployment", "roles.business.group.deployment", formLayout,
				rolesKeyValues.keys(), rolesKeyValues.values(), 1);
		rolesBusinessGroupEl.setEnabled(OrganisationRoles.administrator.name(), false);
		
		List<String> roles = lti13Module.getDeploymentBusinessGroupRolesConfigurationList();
		for(String role:roles) {
			if(rolesKeyValues.containsKey(role)) {
				rolesBusinessGroupEl.select(role, true);
			}
		}
		
		SelectionValues permissionsKeyValues = new SelectionValues();
		permissionsKeyValues.add(SelectionValues.entry(InvitationConfigurationPermission.allResources.name(), translate("activate.all.business.groups")));
		permissionsKeyValues.add(SelectionValues.entry(InvitationConfigurationPermission.perResource.name(), translate("activate.per.business.group")));
		businessGroupCoachPermissionEl = uifactory.addRadiosVertical("business.group.coach.permission", "business.group.coach.permission", formLayout,
				permissionsKeyValues.keys(), permissionsKeyValues.values());
		
		DeploymentConfigurationPermission permission = lti13Module.getDeploymentBusinessGroupCoachPermission();
		if(permission != null && permissionsKeyValues.containsKey(permission.name())) {
			businessGroupCoachPermissionEl.select(permission.name(), true);
		}
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
		if(moduleEnabled.isAtLeastSelected(1)) {
			if(!organisationsEl.isOneSelected()) {
				organisationsEl.setErrorKey("form.legende.mandatory", null);
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
		entryOwnerPermissionEl.setVisible(enabled);
		businessGroupCoachPermissionEl.setVisible(enabled);
		rolesEntryEl.setVisible(enabled);
		rolesBusinessGroupEl.setVisible(enabled);
		repositoryEntryCont.setVisible(enabled);
		businessGroupCont.setVisible(enabled);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = moduleEnabled.isAtLeastSelected(1);
		lti13Module.setEnabled(enabled);
		
		String selectedOrganisationKey = organisationsEl.getSelectedKey();
		lti13Module.setDefaultOrganisationKey(selectedOrganisationKey);
		lti13Module.setDeploymentRepositoryEntryRolesConfigurationList(rolesEntryEl.getSelectedKeys());
		lti13Module.setDeploymentBusinessGroupRolesConfigurationList(rolesBusinessGroupEl.getSelectedKeys());
		if(entryOwnerPermissionEl.isOneSelected()) {
			lti13Module.setDeploymentRepositoryEntryOwnerPermission(entryOwnerPermissionEl.getSelectedKey());
		}
		if(businessGroupCoachPermissionEl.isOneSelected()) {
			lti13Module.setDeploymentBusinessGroupCoachPermission(businessGroupCoachPermissionEl.getSelectedKey());
		}
	}
}
