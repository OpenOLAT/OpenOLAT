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
package org.olat.group.ui;

import java.util.Collection;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroupModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupModuleAdminController extends FormBasicController {

	private MultipleSelectionElement allowEl;
	private MultipleSelectionElement enrolmentEl;
	private MultipleSelectionElement membershipEl;
	private MultipleSelectionElement assignCoursesEl;
	private MultipleSelectionElement assignGroupsEl;
	private MultipleSelectionElement allowLeavingGroupsEl;
	
	private String[] onKeys = new String[]{"user","author"};
	private String[] assignKeys = new String[]{"granted"};
	private String[] allowLeavingKeys = new String[]{
			"groupMadeByLearners", "groupMadeByAuthors", "groupOverride"
	};
	
	@Autowired
	private BusinessGroupModule module;
	
	public BusinessGroupModuleAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "bg_admin");
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer optionsContainer = FormLayoutContainer.createDefaultFormLayout("options", getTranslator());
		optionsContainer.setFormTitle(translate("module.admin.title"));
		optionsContainer.setFormDescription(translate("module.admin.desc"));
		optionsContainer.setElementCssClass("o_sel_group_admin_options");
		formLayout.add(optionsContainer);
		String[] values = new String[]{
				translate("user.allow.create"),
				translate("author.allow.create")
		};
		allowEl = uifactory.addCheckboxesVertical("module.admin.allow.create", optionsContainer, onKeys, values, 1);
		allowEl.select("user", module.isUserAllowedCreate());
		allowEl.select("author", module.isAuthorAllowedCreate());
		allowEl.addActionListener(FormEvent.ONCHANGE);

		FormLayoutContainer resourceAssignmentContainer = FormLayoutContainer.createDefaultFormLayout("resourceAssignment", getTranslator());
		resourceAssignmentContainer.setFormTitle(translate("module.resource.title"));
		resourceAssignmentContainer.setFormDescription(translate("module.resource.desc"));
		formLayout.add(resourceAssignmentContainer);
		
		String[] courseValues = new String[]{ translate("module.resource.courses.grant") };
		assignCoursesEl = uifactory.addCheckboxesHorizontal("module.resource.courses", resourceAssignmentContainer, assignKeys, courseValues);
		assignCoursesEl.select(assignKeys[0], module.isGroupManagersAllowedToLinkCourses());
		assignCoursesEl.addActionListener(FormEvent.ONCHANGE);
		
		String[] groupValues = new String[]{ translate("module.resource.groups.grant") };
		assignGroupsEl = uifactory.addCheckboxesHorizontal("module.resource.groups", resourceAssignmentContainer, assignKeys, groupValues);
		assignGroupsEl.select(assignKeys[0], module.isResourceManagersAllowedToLinkGroups());
		assignGroupsEl.addActionListener(FormEvent.ONCHANGE);
		
		FormLayoutContainer privacyOptionsContainer = FormLayoutContainer.createDefaultFormLayout("privacy_options", getTranslator());
		privacyOptionsContainer.setFormTitle(translate("module.privacy.title"));
		privacyOptionsContainer.setFormDescription(translate("module.privacy.desc"));
		privacyOptionsContainer.setElementCssClass("o_sel_group_admin_privacy");
		formLayout.add(privacyOptionsContainer);

		OrganisationRoles[] roles = BaseSecurityModule.getUserAllowedRoles();
		String[] enrollmentKeys = new String[roles.length];
		String[] enrollmentValues = new String[roles.length];
		for(int i=roles.length; i-->0; ) {
			enrollmentKeys[i] = roles[i].name();
			enrollmentValues[i] = translate("enrolment.email." + roles[i].name() + "s");
		}
		enrolmentEl = uifactory.addCheckboxesVertical("mandatory.enrolment", privacyOptionsContainer, enrollmentKeys, enrollmentValues, 1);
		for(OrganisationRoles adminProp:roles) {
			if(Boolean.parseBoolean(module.getMandatoryEnrolmentEmailFor(adminProp))) {
				enrolmentEl.select(adminProp.name(), true);
			}
		}
		enrolmentEl.addActionListener(FormEvent.ONCHANGE);
		
		String[] membershipValues = new String[enrollmentKeys.length];
		for(int i=roles.length; i-->0; ) {
			membershipValues[i] = translate("membership." + roles[i].name() + "s");
		}
		membershipEl = uifactory.addCheckboxesVertical("mandatory.membership", privacyOptionsContainer, enrollmentKeys, membershipValues, 1);
		membershipEl.setElementCssClass("o_select_membership_confirmation");
		for(OrganisationRoles adminProp:roles) {
			if(Boolean.parseBoolean(module.getAcceptMembershipFor(adminProp))) {
				membershipEl.select(adminProp.name(), true);
			}
		}
		membershipEl.addActionListener(FormEvent.ONCHANGE);
		
		String[] allowLeavingValues = new String[]{
				translate("leaving.group.learners"),
				translate("leaving.group.authors"),
				translate("leaving.group.override"),
		};
		allowLeavingGroupsEl = uifactory.addCheckboxesVertical("leaving.group", privacyOptionsContainer, allowLeavingKeys, allowLeavingValues, 1);
		allowLeavingGroupsEl.select("groupMadeByLearners", module.isAllowLeavingGroupCreatedByLearners());
		allowLeavingGroupsEl.select("groupMadeByAuthors", module.isAllowLeavingGroupCreatedByAuthors());
		allowLeavingGroupsEl.select("groupOverride", module.isAllowLeavingGroupOverride());
		allowLeavingGroupsEl.addActionListener(FormEvent.ONCHANGE);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == allowEl) {
			module.setUserAllowedCreate(allowEl.isSelected(0));
			module.setAuthorAllowedCreate(allowEl.isSelected(1));
		} else if(source == membershipEl) {
			Collection<String> selectedKeys = membershipEl.getSelectedKeys();
			OrganisationRoles[] roleArray = BaseSecurityModule.getUserAllowedRoles();
			for(OrganisationRoles role:roleArray) {
				module.setAcceptMembershipFor(role, Boolean.toString(selectedKeys.contains(role.name())));
			}
		} else if(source == enrolmentEl) {
			Collection<String> selectedKeys = enrolmentEl.getSelectedKeys();
			OrganisationRoles[] roleArray = BaseSecurityModule.getUserAllowedRoles();
			for(OrganisationRoles role:roleArray) {
				module.setMandatoryEnrolmentEmailFor(role, Boolean.toString(selectedKeys.contains(role.name())));
			}
		} else if(source == allowLeavingGroupsEl) {
			Collection<String> leavingSelectedKeys = allowLeavingGroupsEl.getSelectedKeys();
			module.setAllowLeavingGroupCreatedByLearners(leavingSelectedKeys.contains("groupMadeByLearners"));
			module.setAllowLeavingGroupCreatedByAuthors(leavingSelectedKeys.contains("groupMadeByAuthors"));
			module.setAllowLeavingGroupOverride(leavingSelectedKeys.contains("groupOverride"));
		} else if(assignCoursesEl == source) {
			module.setGroupManagersAllowedToLinkCourses(assignCoursesEl.isSelected(0));
		} else if(assignGroupsEl == source) {
			module.setResourceManagersAllowedToLinkGroups(assignGroupsEl.isSelected(0));
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}