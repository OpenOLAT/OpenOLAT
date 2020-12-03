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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
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
	
	private static final String[] ENABLED_KEY = new String[]{ "on" };

	private MultipleSelectionElement moduleEnabled;
	private TextElement clientIdEl;
	private TextElement secretEl;
	private TextElement tenantEl;
	private TextElement applicationIdEl;
	private TextElement producerIdEl;
	
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
		setFormContextHelp("Teams+module");
		String[] enabledValues = new String[]{ translate("enabled") };
		
		moduleEnabled = uifactory.addCheckboxesHorizontal("teams.module.enabled", formLayout, ENABLED_KEY, enabledValues);
		moduleEnabled.select(ENABLED_KEY[0], teamsModule.isEnabled());
		moduleEnabled.addActionListener(FormEvent.ONCHANGE);
		
		String clientId = teamsModule.getApiKey();
		clientIdEl = uifactory.addTextElement("client.id", "azure.adfs.id", 255, clientId, formLayout);
		clientIdEl.setMandatory(true);
		String clientSecret = teamsModule.getApiSecret();
		secretEl = uifactory.addTextElement("secret", "azure.adfs.secret", 255, clientSecret, formLayout);
		secretEl.setMandatory(true);
		String tenant = teamsModule.getTenantGuid();
		tenantEl = uifactory.addTextElement("tenant", "azure.tenant.guid", 255, tenant, formLayout);
		tenantEl.setMandatory(true);
		
		String applicationid = teamsModule.getApplicationId();
		applicationIdEl = uifactory.addTextElement("appId", "graph.application.id", 255, applicationid, formLayout);
		String producerId = teamsModule.getProducerId();
		producerIdEl = uifactory.addTextElement("producer", "graph.producer.id", 255, producerId, formLayout);
		
		//buttons save - check
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("save", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
	}

	@Override
	protected void doDispose() {
		//
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
		clientIdEl.setVisible(enabled);
		secretEl.setVisible(enabled);
		tenantEl.setVisible(enabled);
		producerIdEl.setVisible(enabled);
		applicationIdEl.setVisible(enabled);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		boolean enabled = moduleEnabled.isSelected(0);
		if(enabled) {
			allOk &= validateMandatory(clientIdEl);
			allOk &= validateMandatory(secretEl);
			allOk &= validateMandatory(tenantEl);
		}
		
		return allOk;
	}
	
	private boolean validateMandatory(TextElement el) {
		boolean allOk = true;
		
		el.clearError();
		if(!StringHelper.containsNonWhitespace(el.getValue())) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = moduleEnabled.isSelected(0);
		teamsModule.setEnabled(enabled);
		if(enabled) {
			teamsModule.setApiKey(clientIdEl.getValue());
			teamsModule.setApiSecret(secretEl.getValue());
			teamsModule.setTenantGuid(tenantEl.getValue());
			teamsModule.setApplicationId(applicationIdEl.getValue());
			teamsModule.setProducerId(producerIdEl.getValue());
		} else {
			teamsModule.setApiKey(null);
			teamsModule.setApiSecret(null);
			teamsModule.setTenantGuid(null);
			teamsModule.setApplicationId(null);
			teamsModule.setProducerId(null);
		}
	}
}
