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
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.teams.TeamsModule;
import org.olat.modules.teams.TeamsService;
import org.olat.modules.teams.model.ConnectionInfos;
import org.olat.modules.teams.model.TeamsError;
import org.olat.modules.teams.model.TeamsErrorCodes;
import org.olat.modules.teams.model.TeamsErrors;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsConfigurationController extends FormBasicController {

	private static final String[] FOR_KEYS = { "courses", "appointments", "groups" };
	private static final String[] ENABLED_KEY = new String[]{ "on" };

	private MultipleSelectionElement moduleEnabled;
	private MultipleSelectionElement enabledForEl;
	private TextElement clientIdEl;
	private TextElement secretEl;
	private TextElement tenantEl;
	private StaticTextElement organisationEl;
	private TextElement producerIdEl;
	private StaticTextElement producerEl;
	private FormLink checkConnectionButton;
	private SpacerElement appSpacer;
	
	@Autowired
	private TeamsModule teamsModule;
	@Autowired
	private TeamsService teamsService;
	
	public TeamsConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
		updateUI();
		if(teamsModule.isEnabled()) {
			loadModel();
		}
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
		
		String[] forValues = new String[] {
			translate("teams.module.enabled.for.courses"), translate("teams.module.enabled.for.appointments"),
			translate("teams.module.enabled.for.groups")
		};
		enabledForEl = uifactory.addCheckboxesVertical("teams.module.enabled.for", formLayout, FOR_KEYS, forValues, 1);
		enabledForEl.select(FOR_KEYS[0], teamsModule.isCoursesEnabled());
		enabledForEl.select(FOR_KEYS[1], teamsModule.isAppointmentsEnabled());
		enabledForEl.select(FOR_KEYS[2], teamsModule.isGroupsEnabled());
		
		String clientId = teamsModule.getApiKey();
		clientIdEl = uifactory.addTextElement("client.id", "azure.adfs.id", 255, clientId, formLayout);
		clientIdEl.setMandatory(true);
		String clientSecret = teamsModule.getApiSecret();
		secretEl = uifactory.addTextElement("secret", "azure.adfs.secret", 255, clientSecret, formLayout);
		secretEl.setMandatory(true);
		String tenant = teamsModule.getTenantGuid();
		tenantEl = uifactory.addTextElement("tenant", "azure.tenant.guid", 255, tenant, formLayout);
		tenantEl.setMandatory(true);
		String organisation = teamsModule.getTenantOrganisation();
		organisationEl = uifactory.addStaticTextElement("organisation", "azure.tenant.organisation", organisation, formLayout);
		organisationEl.setVisible(StringHelper.containsNonWhitespace(organisation));
		
		appSpacer = uifactory.addSpacerElement("spacer1", formLayout, false);
		
		String producerId = teamsModule.getProducerId();
		producerIdEl = uifactory.addTextElement("producer.id", "graph.producer.id", 255, producerId, formLayout);
		producerEl = uifactory.addStaticTextElement("producer", "graph.producer.displayname", organisation, formLayout);
		producerEl.setVisible(false);
		
		//buttons save - check
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("save", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		checkConnectionButton = uifactory.addFormLink("check.connection", "check.connection", null, buttonLayout, Link.BUTTON);
	}
	
	private void loadModel() {
		TeamsErrors errors = new TeamsErrors();
		ConnectionInfos infos = teamsService.checkConnection(errors);
		updateModel(infos);
	}
	
	private void updateModel(ConnectionInfos infos) {
		String organisation = infos == null ? "" : infos.getOrganisation();
		organisationEl.setValue(organisation);
		organisationEl.setVisible(StringHelper.containsNonWhitespace(organisation));
		
		String producer = infos == null ? "" : infos.getProducerDisplayName();
		producerEl.setValue(producer);
		producerEl.setVisible(StringHelper.containsNonWhitespace(producer));
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(moduleEnabled == source) {
			updateUI();
		} else if(this.checkConnectionButton == source) {
			doCheckConnection();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateUI() {
		boolean enabled = moduleEnabled.isAtLeastSelected(1);
		clientIdEl.setVisible(enabled);
		secretEl.setVisible(enabled);
		tenantEl.setVisible(enabled);
		producerIdEl.setVisible(enabled);
		checkConnectionButton.setVisible(enabled);
		organisationEl.setVisible(enabled && StringHelper.containsNonWhitespace(organisationEl.getValue()));
		producerEl.setVisible(enabled && StringHelper.containsNonWhitespace(producerEl.getValue()));
		appSpacer.setVisible(enabled);
		enabledForEl.setVisible(enabled);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		boolean enabled = moduleEnabled.isSelected(0);
		if(enabled) {
			allOk &= validateMandatory(clientIdEl);
			allOk &= validateMandatory(secretEl);
			allOk &= validateMandatory(tenantEl);
			allOk &= validateConnection();
		}
		
		return allOk;
	}
	
	private boolean validateConnection() {
		boolean allOk = true;

		TeamsErrors errors = new TeamsErrors();
		ConnectionInfos infos = teamsService.checkConnection(clientIdEl.getValue(), secretEl.getValue(), tenantEl.getValue(),
				producerIdEl.getValue(), errors);
		
		producerIdEl.clearError();
		if(infos != null) {
			if(StringHelper.containsNonWhitespace(producerIdEl.getValue())
					&& !StringHelper.containsNonWhitespace(infos.getProducerDisplayName())) {
				producerIdEl.setErrorKey("error.producerNotFound", null);
				allOk &= false;
			}
		}
		
		if(errors.hasErrors()) {
			clientIdEl.setErrorKey("error.connection", null);
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
			teamsModule.setTenantOrganisation(organisationEl.getValue());
			teamsModule.setProducerId(producerIdEl.getValue());
			teamsModule.setCoursesEnabled(enabledForEl.isSelected(0));
			teamsModule.setAppointmentsEnabled(enabledForEl.isSelected(1));
			teamsModule.setGroupsEnabled(enabledForEl.isSelected(2));
			showInfo("info.saved");
		} else {
			teamsModule.setApiKey(null);
			teamsModule.setApiSecret(null);
			teamsModule.setTenantGuid(null);
			teamsModule.setTenantOrganisation(null);
			teamsModule.setProducerId(null);
			showInfo("info.saved");
		}

		CollaborationToolsFactory.getInstance().initAvailableTools();
	}
	
	private void doCheckConnection() {
		String producerId = producerIdEl.getValue();
		
		TeamsErrors errors = new TeamsErrors();
		ConnectionInfos infos = teamsService.checkConnection(clientIdEl.getValue(), secretEl.getValue(), tenantEl.getValue(),
				producerId, errors);
		updateModel(infos);
		
		if(infos == null) {
			showError("error.connection");
		} else {
			if(StringHelper.containsNonWhitespace(producerId)
					&& !StringHelper.containsNonWhitespace(infos.getProducerDisplayName())) {
				errors.append(new TeamsError(TeamsErrorCodes.producerNotFound));
			}

			if(errors.getErrors().isEmpty()) {
				showInfo("info.connection.ok");
			} else {
				String formattedErrors = TeamsUIHelper.formatErrors(getTranslator(), errors);
				getWindowControl().setError(formattedErrors);
			}
		}
	}
}
