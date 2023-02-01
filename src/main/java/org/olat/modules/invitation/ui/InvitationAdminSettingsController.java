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
package org.olat.modules.invitation.ui;

import java.util.List;

import org.olat.admin.user.UserAdminController;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.invitation.InvitationConfigurationPermission;
import org.olat.modules.invitation.InvitationModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationAdminSettingsController extends FormBasicController {
	
	private static final String[] keys = new String[]{ "on" };
	private final SelectionValues onKeyValues = new SelectionValues();
	
	private MultipleSelectionElement invitationCourseEl;
	private MultipleSelectionElement invitationBusinessGroupEl;
	private MultipleSelectionElement invitationPortfolioEl;
	private MultipleSelectionElement invitationProjectEl;
	private MultipleSelectionElement rolesCourseEl;
	private MultipleSelectionElement rolesBusinessGroupEl;
	private SingleSelection courseOwnerPermissionEl;
	private SingleSelection businessGroupCoachPermissionEl;
	
	@Autowired
	private InvitationModule invitationModule;
	
	public InvitationAdminSettingsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(UserAdminController.class, ureq.getLocale(), getTranslator()));
		
		onKeyValues.add(SelectionValues.entry("on", translate("on")));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initCourseForm(formLayout);
		initBusinessGroupForm(formLayout);
		initPortfolioForm(formLayout);
		initProjectForm(formLayout);
	}
	
	private void initCourseForm(FormItemContainer formLayout) {
		FormLayoutContainer courseCont = FormLayoutContainer.createDefaultFormLayout("courseCont", getTranslator());
		formLayout.add(courseCont);
		courseCont.setFormTitle(translate("admin.course.title"));
		
		invitationCourseEl = uifactory.addCheckboxesHorizontal("invitee.course.login", "invitee.course.login", courseCont,
				onKeyValues.keys(), onKeyValues.values());
		invitationCourseEl.select(keys[0], invitationModule.isCourseInvitationEnabled());
		invitationCourseEl.addActionListener(FormEvent.ONCHANGE);
		
		SelectionValues rolesKeyValues = new SelectionValues();
		rolesKeyValues.add(SelectionValues.entry(OrganisationRoles.administrator.name(),
				translate("role." + OrganisationRoles.administrator.name())));
		rolesKeyValues.add(SelectionValues.entry(OrganisationRoles.learnresourcemanager.name(),
				translate("role." + OrganisationRoles.learnresourcemanager.name())));
		rolesCourseEl= uifactory.addCheckboxesVertical("roles.course.invitation", "roles.course.invitation", courseCont,
				rolesKeyValues.keys(), rolesKeyValues.values(), 1);
		rolesCourseEl.setEnabled(OrganisationRoles.administrator.name(), false);
		rolesCourseEl.addActionListener(FormEvent.ONCHANGE);
		
		List<String> roles = invitationModule.getCourseRolesConfigurationList();
		for(String role:roles) {
			if(rolesKeyValues.containsKey(role)) {
				rolesCourseEl.select(role, true);
			}
		}
	
		SelectionValues permissionsKeyValues = new SelectionValues();
		permissionsKeyValues.add(SelectionValues.entry(InvitationConfigurationPermission.allResources.name(), translate("activate.all.courses")));
		permissionsKeyValues.add(SelectionValues.entry(InvitationConfigurationPermission.perResource.name(), translate("activate.per.course")));
		courseOwnerPermissionEl = uifactory.addRadiosVertical("course.owner.permission", "course.owner.permission", courseCont,
				permissionsKeyValues.keys(), permissionsKeyValues.values());
		courseOwnerPermissionEl.setHelpText(translate("course.owner.permission.help"));
		courseOwnerPermissionEl.addActionListener(FormEvent.ONCHANGE);
		
		InvitationConfigurationPermission permission = invitationModule.getCourseOwnerPermission();
		if(permission != null && permissionsKeyValues.containsKey(permission.name())) {
			courseOwnerPermissionEl.select(permission.name(), true);
		}
	}

	private void initBusinessGroupForm(FormItemContainer formLayout) {
		FormLayoutContainer groupCont = FormLayoutContainer.createDefaultFormLayout("businessGroupCont", getTranslator());
		formLayout.add(groupCont);
		groupCont.setFormTitle(translate("admin.business.group.title"));
		
		invitationBusinessGroupEl = uifactory.addCheckboxesHorizontal("invitee.business.group.login", "invitee.business.group.login", groupCont,
				onKeyValues.keys(), onKeyValues.values());
		invitationBusinessGroupEl.select(keys[0], invitationModule.isBusinessGroupInvitationEnabled());
		invitationBusinessGroupEl.addActionListener(FormEvent.ONCHANGE);
		
		SelectionValues rolesKeyValues = new SelectionValues();
		rolesKeyValues.add(SelectionValues.entry(OrganisationRoles.administrator.name(),
				translate("role." + OrganisationRoles.administrator.name())));
		rolesKeyValues.add(SelectionValues.entry(OrganisationRoles.groupmanager.name(),
				translate("role." + OrganisationRoles.groupmanager.name())));
		rolesBusinessGroupEl = uifactory.addCheckboxesVertical("roles.business.group.invitation", "roles.business.group.invitation", groupCont,
				rolesKeyValues.keys(), rolesKeyValues.values(), 1);
		rolesBusinessGroupEl.setEnabled(OrganisationRoles.administrator.name(), false);
		rolesBusinessGroupEl.addActionListener(FormEvent.ONCHANGE);
		
		List<String> roles = invitationModule.getBusinessGroupRolesConfigurationList();
		for(String role:roles) {
			if(rolesKeyValues.containsKey(role)) {
				rolesBusinessGroupEl.select(role, true);
			}
		}
		
		SelectionValues permissionsKeyValues = new SelectionValues();
		permissionsKeyValues.add(SelectionValues.entry(InvitationConfigurationPermission.allResources.name(), translate("activate.all.business.groups")));
		permissionsKeyValues.add(SelectionValues.entry(InvitationConfigurationPermission.perResource.name(), translate("activate.per.business.group")));
		businessGroupCoachPermissionEl = uifactory.addRadiosVertical("business.group.coach.permission", "business.group.coach.permission", groupCont,
				permissionsKeyValues.keys(), permissionsKeyValues.values());
		businessGroupCoachPermissionEl.addActionListener(FormEvent.ONCHANGE);
		businessGroupCoachPermissionEl.setHelpText(translate("business.group.coach.permission.help"));
		
		InvitationConfigurationPermission permission = invitationModule.getBusinessGroupCoachPermission();
		if(permission != null && permissionsKeyValues.containsKey(permission.name())) {
			businessGroupCoachPermissionEl.select(permission.name(), true);
		}
	}
	
	private void initPortfolioForm(FormItemContainer formLayout) {
		FormLayoutContainer portfolioCont = FormLayoutContainer.createDefaultFormLayout("portfolioCont", getTranslator());
		formLayout.add(portfolioCont);
		portfolioCont.setFormTitle(translate("admin.portfolio.title"));
		
		invitationPortfolioEl = uifactory.addCheckboxesHorizontal("invitee.portfolio.login", "invitee.portfolio.login", portfolioCont,
				onKeyValues.keys(), onKeyValues.values());
		invitationPortfolioEl.select(keys[0], invitationModule.isPortfolioInvitationEnabled());
		invitationPortfolioEl.addActionListener(FormEvent.ONCHANGE);
	}
	
	private void initProjectForm(FormItemContainer formLayout) {
		FormLayoutContainer projectCont = FormLayoutContainer.createDefaultFormLayout("projectCont", getTranslator());
		formLayout.add(projectCont);
		projectCont.setFormTitle(translate("admin.project.title"));
		
		invitationProjectEl = uifactory.addCheckboxesHorizontal("invitee.project.login", "invitee.project.login", projectCont,
				onKeyValues.keys(), onKeyValues.values());
		invitationProjectEl.select(keys[0], invitationModule.isProjectInvitationEnabled());
		invitationProjectEl.addActionListener(FormEvent.ONCHANGE);
	}
	
	private void updateUI() {
		boolean courseInvitationEnabled = invitationCourseEl.isAtLeastSelected(1);
		rolesCourseEl.setVisible(courseInvitationEnabled);
		courseOwnerPermissionEl.setVisible(courseInvitationEnabled);
		
		boolean businessGroupInvitationEnabled = invitationBusinessGroupEl.isAtLeastSelected(1);
		rolesBusinessGroupEl.setVisible(businessGroupInvitationEnabled);
		businessGroupCoachPermissionEl.setVisible(businessGroupInvitationEnabled);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(invitationCourseEl == source) {
			invitationModule.setCourseInvitationEnabled(invitationCourseEl.isAtLeastSelected(1));
			updateUI();
		} else if(invitationBusinessGroupEl == source) {
			invitationModule.setBusinessGroupInvitationEnabled(invitationBusinessGroupEl.isAtLeastSelected(1));
			updateUI();
		} else if(invitationPortfolioEl == source) {
			invitationModule.setPortfolioInvitationEnabled(invitationPortfolioEl.isAtLeastSelected(1));
		} else if(invitationProjectEl == source) {
			invitationModule.setProjectInvitationEnabled(invitationProjectEl.isAtLeastSelected(1));
		} else if(rolesCourseEl == source) {
			invitationModule.setCourseRolesConfiguration(rolesCourseEl.getSelectedKeys());
		} else if(rolesBusinessGroupEl == source) {
			invitationModule.setBusinessGroupRolesConfiguration(rolesBusinessGroupEl.getSelectedKeys());
		} else if(courseOwnerPermissionEl == source) {
			if(courseOwnerPermissionEl.isOneSelected()) {
				invitationModule.setCourseOwnerPermission(courseOwnerPermissionEl.getSelectedKey());
			}
		} else if(businessGroupCoachPermissionEl == source) {
			if(businessGroupCoachPermissionEl.isOneSelected()) {
				invitationModule.setBusinessGroupCoachPermission(businessGroupCoachPermissionEl.getSelectedKey());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
