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

import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
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

	private static final String[] FOR_KEYS = { "courses", "appointments", "groups", "chatexams" };
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
		
		String[] forValues = new String[] {
			translate("teams.module.enabled.for.courses"), translate("teams.module.enabled.for.appointments"),
			translate("teams.module.enabled.for.groups"),
			translate("teams.module.enabled.for.chat.exams")
		};
		enabledForEl = uifactory.addCheckboxesVertical("teams.module.enabled.for", formLayout, FOR_KEYS, forValues, 1);
		enabledForEl.select(FOR_KEYS[0], teamsModule.isCoursesEnabled());
		enabledForEl.select(FOR_KEYS[1], teamsModule.isAppointmentsEnabled());
		enabledForEl.select(FOR_KEYS[2], teamsModule.isGroupsEnabled());
		enabledForEl.select(FOR_KEYS[3], teamsModule.isChatExamsEnabled());
		
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
		enabledForEl.setVisible(enabled && showOldConfiguration);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = moduleEnabled.isSelected(0);
		teamsModule.setEnabled(enabled);
		if(enabled) {
			teamsModule.setCoursesEnabled(enabledForEl.isSelected(0));
			teamsModule.setAppointmentsEnabled(enabledForEl.isSelected(1));
			teamsModule.setGroupsEnabled(enabledForEl.isSelected(2));
			teamsModule.setChatExamsEnabled(enabledForEl.isSelected(3));
			showInfo("info.saved");
		}
		CollaborationToolsFactory.getInstance().initAvailableTools();
	}
}
