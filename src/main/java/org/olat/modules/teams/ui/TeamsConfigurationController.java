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
package org.olat.modules.teams.ui;

import java.util.Collection;

import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.teams.TeamsModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsConfigurationController extends FormBasicController {

	private static final String FOR_COURSES_KEY = "courses";
	private static final String FOR_APPOINTMENTS_KEY = "appointments";
	private static final String FOR_GROUPS_KEY = "groups";
	private static final String FOR_CHATEXAMS_KEY = "chatexams";
	private static final String FOR_LECTURES_KEY = "lectures";
	private static final String[] ENABLED_KEY = new String[]{ "on" };

	private MultipleSelectionElement moduleEnabled;
	private MultipleSelectionElement enabledForEl;
	private StaticTextElement clientIdEl;
	private StaticTextElement secretEl;
	private StaticTextElement tenantEl;
	
	@Autowired
	private TeamsModule teamsModule;
	
	public TeamsConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("teams.title");
		setFormInfo("teams.intro");
		setFormContextHelp("manual_user/learningresources/Course_Element_Microsoft_Teams/");
		String[] enabledValues = new String[]{ translate("enabled") };
		
		moduleEnabled = uifactory.addCheckboxesHorizontal("teams.module.enabled", formLayout, ENABLED_KEY, enabledValues);
		moduleEnabled.select(ENABLED_KEY[0], teamsModule.isEnabled());
		moduleEnabled.addActionListener(FormEvent.ONCHANGE);
		
		SelectionValues forPK = new SelectionValues();
		forPK.add(SelectionValues.entry(FOR_COURSES_KEY, translate("teams.module.enabled.for.courses")));
		forPK.add(SelectionValues.entry(FOR_LECTURES_KEY, translate("teams.module.enabled.for.lectures")));
		forPK.add(SelectionValues.entry(FOR_APPOINTMENTS_KEY, translate("teams.module.enabled.for.appointments")));
		forPK.add(SelectionValues.entry(FOR_GROUPS_KEY, translate("teams.module.enabled.for.groups")));
		forPK.add(SelectionValues.entry(FOR_CHATEXAMS_KEY, translate("teams.module.enabled.for.chat.exams")));
		enabledForEl = uifactory.addCheckboxesVertical("teams.module.enabled.for", formLayout, forPK.keys(), forPK.values(), 1);
		enabledForEl.select(FOR_COURSES_KEY, teamsModule.isCoursesEnabled());
		enabledForEl.select(FOR_LECTURES_KEY, teamsModule.isLecturesEnabled());
		enabledForEl.select(FOR_APPOINTMENTS_KEY, teamsModule.isAppointmentsEnabled());
		enabledForEl.select(FOR_GROUPS_KEY, teamsModule.isGroupsEnabled());
		enabledForEl.select(FOR_CHATEXAMS_KEY, teamsModule.isChatExamsEnabled());
		
		String clientId = teamsModule.getApiKey();
		boolean showOldConfiguration = StringHelper.containsNonWhitespace(clientId);
		clientIdEl = uifactory.addStaticTextElement("client.id", "azure.adfs.id", clientId, formLayout);
		clientIdEl.setVisible(showOldConfiguration);
		String clientSecret = teamsModule.getApiSecret();
		secretEl = uifactory.addStaticTextElement("secret", "azure.adfs.secret", clientSecret, formLayout);
		secretEl.setVisible(showOldConfiguration);
		String tenant = teamsModule.getTenantGuid();
		tenantEl = uifactory.addStaticTextElement("tenant", "azure.tenant.guid", tenant, formLayout);
		tenantEl.setVisible(showOldConfiguration);
		
		//buttons save - check
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("save", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
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
		boolean showOldConfiguration = StringHelper.containsNonWhitespace(clientIdEl.getValue());
		clientIdEl.setVisible(enabled && showOldConfiguration);
		secretEl.setVisible(enabled && showOldConfiguration);
		tenantEl.setVisible(enabled && showOldConfiguration);
		enabledForEl.setVisible(enabled);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = moduleEnabled.isSelected(0);
		teamsModule.setEnabled(enabled);
		if(enabled) {
			Collection<String> selectedFor = enabledForEl.getSelectedKeys();
			teamsModule.setCoursesEnabled(selectedFor.contains(FOR_COURSES_KEY));
			teamsModule.setAppointmentsEnabled(selectedFor.contains(FOR_APPOINTMENTS_KEY));
			teamsModule.setLecturesEnabled(selectedFor.contains(FOR_LECTURES_KEY));
			teamsModule.setGroupsEnabled(selectedFor.contains(FOR_GROUPS_KEY));
			teamsModule.setChatExamsEnabled(selectedFor.contains(FOR_CHATEXAMS_KEY));
			showInfo("info.saved");
		}
		CollaborationToolsFactory.getInstance().initAvailableTools();
	}
}
