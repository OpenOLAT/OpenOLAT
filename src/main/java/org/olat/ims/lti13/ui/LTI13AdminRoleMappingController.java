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
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.nodes.basiclti.LTIConfigForm;
import org.olat.ims.lti13.LTI13Constants;
import org.olat.ims.lti13.LTI13Module;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2025-11-10<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class LTI13AdminRoleMappingController extends FormBasicController {

	private MultipleSelectionElement configurableByOwnerEl;
	private MultipleSelectionElement defaultSettingsForOwnersEl;
	private MultipleSelectionElement defaultSettingsForCoachesEl;
	private MultipleSelectionElement defaultSettingsForParticipantsEl;

	@Autowired
	private LTI13Module ltiModule;

	public LTI13AdminRoleMappingController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(LTIConfigForm.class, getLocale(), getTranslator()));

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("openolat.to.lti.role.mapping");
		
		SelectionValues roles = new SelectionValues();
		roles.add(SelectionValues.entry(LTI13Constants.Roles.LEARNER.name(), translate("roles.lti.learner")));
		roles.add(SelectionValues.entry(LTI13Constants.Roles.INSTRUCTOR.name(), translate("roles.lti.instructor")));
		roles.add(SelectionValues.entry(LTI13Constants.Roles.ADMINISTRATOR.name(), translate("roles.lti.administrator")));
		roles.add(SelectionValues.entry(LTI13Constants.Roles.TEACHING_ASSISTANT.name(), translate("roles.lti.teachingAssistant")));
		roles.add(SelectionValues.entry(LTI13Constants.Roles.CONTENT_DEVELOPER.name(), translate("roles.lti.contentDeveloper")));
		roles.add(SelectionValues.entry(LTI13Constants.Roles.MENTOR.name(), translate("roles.lti.mentor")));

		configurableByOwnerEl = uifactory.addCheckboxesHorizontal("configurable.by.course.owner", formLayout, 
				roles.keys(), roles.values());
		Set<LTI13Constants.Roles> ltiRolesConfigurableByCourseOwner = ltiModule.getLtiRolesConfigurableByCourseOwner();
		for (LTI13Constants.Roles ltiRoleConfigurableByCourseOwner : ltiRolesConfigurableByCourseOwner) {
			configurableByOwnerEl.select(ltiRoleConfigurableByCourseOwner.name(), true);
		}

		uifactory.addSpacerElement("roles", formLayout, false);

		uifactory.addStaticTextElement("roles.title", "roles.title.oo", translate("roles.title.lti"), formLayout);

		defaultSettingsForOwnersEl = uifactory.addCheckboxesHorizontal("default.settings.for.owners", formLayout,
				roles.keys(), roles.values());
		Set<LTI13Constants.Roles> defaultSettingsForOwners = ltiModule.getDefaultRoleSettingsForOwners();
		for (LTI13Constants.Roles defaultSettingForOwners : defaultSettingsForOwners) {
			defaultSettingsForOwnersEl.select(defaultSettingForOwners.name(), true);
		}

		defaultSettingsForCoachesEl = uifactory.addCheckboxesHorizontal("default.settings.for.coaches", formLayout,
				roles.keys(), roles.values());
		Set<LTI13Constants.Roles> defaultSettingsForCoaches = ltiModule.getDefaultRoleSettingsForCoaches();
		for (LTI13Constants.Roles defaultSettingForCoaches : defaultSettingsForCoaches) {
			defaultSettingsForCoachesEl.select(defaultSettingForCoaches.name(), true);
		}

		defaultSettingsForParticipantsEl = uifactory.addCheckboxesHorizontal("default.settings.for.participants", formLayout,
				roles.keys(), roles.values());
		Set<LTI13Constants.Roles> defaultSettingsForParticipants = ltiModule.getDefaultRoleSettingsForParticipants();
		for (LTI13Constants.Roles defaultSettingForParticipants : defaultSettingsForParticipants) {
			defaultSettingsForParticipantsEl.select(defaultSettingForParticipants.name(), true);
		}

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		ltiModule.setLtiRolesConfigurableByCourseOwner(toLtiRolesSet(configurableByOwnerEl.getSelectedKeys()));
		ltiModule.setDefaultRoleSettingsForOwners(toLtiRolesSet(defaultSettingsForOwnersEl.getSelectedKeys()));
		ltiModule.setDefaultRoleSettingsForCoaches(toLtiRolesSet(defaultSettingsForCoachesEl.getSelectedKeys()));
		ltiModule.setDefaultRoleSettingsForParticipants(toLtiRolesSet(defaultSettingsForParticipantsEl.getSelectedKeys()));
	}

	private Set<LTI13Constants.Roles> toLtiRolesSet(Collection<String> selectedKeys) {
		if (selectedKeys == null || selectedKeys.isEmpty()) {
			return Set.of();
		}
		try {
			return selectedKeys.stream().map(LTI13Constants.Roles::valueOf).collect(Collectors.toSet());
		}  catch (IllegalArgumentException e) {
			return Set.of();
		}
	}
}
