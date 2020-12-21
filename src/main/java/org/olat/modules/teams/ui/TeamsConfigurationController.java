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
	
	private static final String[] ENABLED_KEY = new String[]{ "on" };

	private MultipleSelectionElement moduleEnabled;
	private TextElement clientIdEl;
	private TextElement secretEl;
	private TextElement tenantEl;
	private StaticTextElement organisationEl;
	private TextElement applicationIdEl;
	private StaticTextElement applicationEl;
	private TextElement producerIdEl;
	private StaticTextElement producerEl;
	private TextElement onBehalfUserIdEl;
	private StaticTextElement onBehalfUserEl;
	private FormLink checkConnectionButton;
	private SpacerElement appSpacer;
	private SpacerElement onBehlafSpacer;
	
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
		
		String applicationId = teamsModule.getApplicationId();
		applicationIdEl = uifactory.addTextElement("appId", "graph.application.id", 255, applicationId, formLayout);
		applicationEl = uifactory.addStaticTextElement("application", "graph.application.displayname", organisation, formLayout);
		applicationEl.setVisible(false);
		
		String producerId = teamsModule.getProducerId();
		producerIdEl = uifactory.addTextElement("producer.id", "graph.producer.id", 255, producerId, formLayout);
		producerEl = uifactory.addStaticTextElement("producer", "graph.producer.displayname", organisation, formLayout);
		producerEl.setVisible(false);
		
		onBehlafSpacer = uifactory.addSpacerElement("spacer2", formLayout, false);
		
		String onBehalfUserId = teamsModule.getOnBehalfUserId();
		onBehalfUserIdEl = uifactory.addTextElement("onbehalf.id", "graph.onbehalf.user", 255, onBehalfUserId, formLayout);
		onBehalfUserEl = uifactory.addStaticTextElement("onbehalf", "graph.onbehalf.displayname", organisation, formLayout);
		onBehalfUserEl.setVisible(false);
		
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
		
		String application = infos == null ? "" : infos.getApplication();
		applicationEl.setValue(application);
		applicationEl.setVisible(StringHelper.containsNonWhitespace(application));
		
		String producer = infos == null ? "" : infos.getProducerDisplayName();
		producerEl.setValue(producer);
		producerEl.setVisible(StringHelper.containsNonWhitespace(producer));
		
		String onBehalf = infos == null ? "" : infos.getOnBehalfDisplayName();
		onBehalfUserEl.setValue(onBehalf);
		onBehalfUserEl.setVisible(StringHelper.containsNonWhitespace(onBehalf));
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
		applicationIdEl.setVisible(enabled);
		onBehalfUserIdEl.setVisible(enabled);
		checkConnectionButton.setVisible(enabled);
		organisationEl.setVisible(enabled && StringHelper.containsNonWhitespace(organisationEl.getValue()));
		applicationEl.setVisible(enabled && StringHelper.containsNonWhitespace(applicationEl.getValue()));
		producerEl.setVisible(enabled && StringHelper.containsNonWhitespace(producerEl.getValue()));
		onBehalfUserEl.setVisible(enabled && StringHelper.containsNonWhitespace(onBehalfUserEl.getValue()));
		appSpacer.setVisible(enabled);
		onBehlafSpacer.setVisible(enabled);
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
				applicationIdEl.getValue(), producerIdEl.getValue(), onBehalfUserIdEl.getValue(), errors);
		
		producerIdEl.clearError();
		applicationIdEl.clearError();
		onBehalfUserIdEl.clearError();
		if(infos != null) {
			if(StringHelper.containsNonWhitespace(producerIdEl.getValue())
					&& !StringHelper.containsNonWhitespace(infos.getProducerDisplayName())) {
				producerIdEl.setErrorKey("error.producerNotFound", null);
				allOk &= false;
			}
			if(StringHelper.containsNonWhitespace(onBehalfUserIdEl.getValue())
					&& !StringHelper.containsNonWhitespace(infos.getOnBehalfDisplayName())) {
				onBehalfUserIdEl.setErrorKey("error.onBehalfUserNotFound", null);
				allOk &= false;
			}
			if(StringHelper.containsNonWhitespace(applicationIdEl.getValue())
					&& !StringHelper.containsNonWhitespace(infos.getApplication())) {
				applicationIdEl.setErrorKey("error.applicationNotFound", null);
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
			teamsModule.setApplicationId(applicationIdEl.getValue());
			teamsModule.setProducerId(producerIdEl.getValue());
			teamsModule.setOnBehalfUserId(onBehalfUserIdEl.getValue());
			showInfo("info.saved");
		} else {
			teamsModule.setApiKey(null);
			teamsModule.setApiSecret(null);
			teamsModule.setTenantGuid(null);
			teamsModule.setTenantOrganisation(null);
			teamsModule.setApplicationId(null);
			teamsModule.setProducerId(null);
			teamsModule.setOnBehalfUserId(null);
			showInfo("info.saved");
		}
	}
	
	private void doCheckConnection() {
		String applicationId = applicationIdEl.getValue();
		String producerId = producerIdEl.getValue();
		String onBehalfUserId = onBehalfUserIdEl.getValue();
		
		TeamsErrors errors = new TeamsErrors();
		ConnectionInfos infos = teamsService.checkConnection(clientIdEl.getValue(), secretEl.getValue(), tenantEl.getValue(),
				applicationId, producerId, onBehalfUserId, errors);
		updateModel(infos);
		
		if(infos == null) {
			showError("error.connection");
		} else {
			if(StringHelper.containsNonWhitespace(producerId)
					&& !StringHelper.containsNonWhitespace(infos.getProducerDisplayName())) {
				errors.append(new TeamsError(TeamsErrorCodes.producerNotFound));
			}
			if(StringHelper.containsNonWhitespace(onBehalfUserId)
					&& !StringHelper.containsNonWhitespace(infos.getOnBehalfDisplayName())) {
				errors.append(new TeamsError(TeamsErrorCodes.onBehalfUserNotFound));
			}
			if(StringHelper.containsNonWhitespace(applicationId)
					&& !StringHelper.containsNonWhitespace(infos.getApplication())) {
				errors.append(new TeamsError(TeamsErrorCodes.applicationNotFound));
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
