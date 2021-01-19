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
package org.olat.modules.edusharing.ui;

import static org.olat.core.gui.translator.TranslatorHelper.translateAll;
import static org.olat.modules.edusharing.ui.EdusharingUIFactory.toIntOrZero;
import static org.olat.modules.edusharing.ui.EdusharingUIFactory.validateInteger;
import static org.olat.modules.edusharing.ui.EdusharingUIFactory.validateIsMandatory;

import java.security.KeyPair;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.modules.edusharing.EdusharingDispatcher;
import org.olat.modules.edusharing.EdusharingModule;
import org.olat.modules.edusharing.EdusharingProperties;
import org.olat.modules.edusharing.EdusharingSecurityService;
import org.olat.modules.edusharing.EdusharingService;
import org.olat.modules.edusharing.Ticket;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 Nov 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdusharingAdminController extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(EdusharingAdminController.class);

	private static final String[] ENABLED_KEYS = new String[]{"on"};
	
	private MultipleSelectionElement enabledEl;
	private MultipleSelectionElement cnEnabledEl;
	private TextElement urlEl;
	private TextElement appIdEl;
	private TextElement hostEl;
	private TextElement ticketValidEl;
	private TextAreaElement soapPublicKeyEl;
	private TextAreaElement repoPublicKeyEl;
	private FormLink generateKeysLink;
	private FormLink importMetadataLink;
	private FormLink testLink;
	
	private DialogBoxController confirmEnableCtrl;
	private DialogBoxController confirmGenerateCtrl;
	
	private KeyPair soapKeys;
	
	@Autowired
	private EdusharingModule edusharingModule;
	@Autowired
	private EdusharingService edusharingService;
	@Autowired
	private EdusharingSecurityService edusharingSignature;

	public EdusharingAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.title");
		setFormDescription("admin.desc", new String[] { EdusharingDispatcher.getMetadataUrl() });
		
		enabledEl = uifactory.addCheckboxesHorizontal("admin.enabled", formLayout, ENABLED_KEYS, translateAll(getTranslator(), ENABLED_KEYS));
		if (edusharingModule.isEnabled()) {
			enabledEl.select(ENABLED_KEYS[0], true);
		}
		enabledEl.addActionListener(FormEvent.ONCHANGE);
		
		cnEnabledEl = uifactory.addCheckboxesHorizontal("admin.course.node.enabled", formLayout, ENABLED_KEYS,
				translateAll(getTranslator(), ENABLED_KEYS));
		cnEnabledEl.select(ENABLED_KEYS[0], edusharingModule.isCourseNodeEnabled());
		
		String url = edusharingModule.getBaseUrl();
		urlEl = uifactory.addTextElement("admin.url", 128, url, formLayout);
		urlEl.setMandatory(true);
		
		String appId = edusharingModule.getAppId();
		appIdEl = uifactory.addTextElement("admin.app.id", 128, appId, formLayout);
		appIdEl.setMandatory(true);
		
		String host = edusharingModule.getHost();
		hostEl = uifactory.addTextElement("admin.host", 128, host, formLayout);
		hostEl.setMandatory(true);
		
		int ticketValidSeconds = edusharingModule.getTicketValidSeconds();
		ticketValidEl = uifactory.addTextElement("admin.ticket.valid", 20, Integer.toString(ticketValidSeconds), formLayout);
		ticketValidEl.setMandatory(true);
		
		String soapPublicKey = edusharingModule.getSoapKeys() != null
				? edusharingSignature.getPublicKey(edusharingModule.getSoapKeys())
				: null;
		soapPublicKeyEl = uifactory.addTextAreaElement("admin.soap.key.public", 8, 72, soapPublicKey, formLayout);
		soapPublicKeyEl.setMandatory(true);
		soapPublicKeyEl.setEnabled(false);
		
		generateKeysLink = uifactory.addFormLink("admin.soap.key.generate", formLayout, Link.BUTTON);
		
		uifactory.addSpacerElement("admin.spacer", formLayout, false);
		
		String repoPublicKeyString = edusharingModule.getRepoPublicKeyString();
		repoPublicKeyEl = uifactory.addTextAreaElement("admin.repo.key.public", 8, 72, repoPublicKeyString, formLayout);
		repoPublicKeyEl.setMandatory(true);
		
		importMetadataLink = uifactory.addFormLink("admin.import.metadata", formLayout, Link.BUTTON);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		
		if (Settings.isDebuging()) {
			testLink = uifactory.addFormLink("admin.client.test", formLayout, Link.BUTTON);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enabledEl && enabledEl.isAtLeastSelected(1)) {
			doConfirmEnable(ureq);
		} else if (source == generateKeysLink) {
			doConfirmGenerateKeys(ureq);
		}if (source == importMetadataLink) {
			doImportMetadataKeys();
		} else if (source == testLink) {
			doTest();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (confirmEnableCtrl == source) {
			boolean confirmed = DialogBoxUIFactory.isYesEvent(event);
			doEnable(confirmed);
		} else if (confirmGenerateCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				doGenerateKeys();
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (enabledEl.isAtLeastSelected(1)) {
			allOk &= validateIsMandatory(urlEl);
			allOk &= validateIsMandatory(appIdEl);
			allOk &= validateIsMandatory(hostEl);
			allOk &= validateInteger(ticketValidEl, 0, 1000000);
			allOk &= validateIsMandatory(repoPublicKeyEl);
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enabledEl.isAtLeastSelected(1);
		edusharingModule.setEnabled(enabled);
		
		boolean courseNodeEnabled = cnEnabledEl.isAtLeastSelected(1);
		edusharingModule.setCourseNodeEnabled(enabled && courseNodeEnabled);
		
		String url = urlEl.getValue();
		url = url.endsWith("/")? url: url + "/";
		edusharingModule.setBaseUrl(url);
		
		String appId = appIdEl.getValue();
		edusharingModule.setAppId(appId);
		
		String host = hostEl.getValue();
		edusharingModule.setHost(host);
		
		int ticketValid = toIntOrZero(ticketValidEl.getValue());
		edusharingModule.setTicketValidSeconds(ticketValid);
		
		if (soapKeys != null) {
			edusharingModule.setSoapKeys(soapKeys);
		}
		
		String repoPublicKey = repoPublicKeyEl.getValue();
		edusharingModule.setRepoPublicKeyString(repoPublicKey);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void doConfirmEnable(UserRequest ureq) {
		String title = translate("admin.enable.confirm.title");
		String message = translate("admin.enable.confirm.message");
		confirmEnableCtrl = activateYesNoDialog(ureq, title, message, confirmEnableCtrl);
	}
	
	private void doEnable(boolean confirmed) {
		enabledEl.select(ENABLED_KEYS[0], confirmed);
	}
	
	private void doConfirmGenerateKeys(UserRequest ureq) {
		StringBuilder sb = new StringBuilder();
		sb.append("<p class='o_warning'>").append(translate("admin.soap.key.generate.confirm")).append("</p>");
		confirmGenerateCtrl = activateYesNoDialog(ureq, null, sb.toString(), confirmGenerateCtrl);
	}
	
	private void doGenerateKeys() {
		try {
			soapKeys = edusharingSignature.generateKeys();
			String soapPublicKey = edusharingSignature.getPublicKey(soapKeys);
			soapPublicKeyEl.setValue(soapPublicKey);
		} catch (Exception e) {
			showError("admin.soap.key.generate.error");
			log.error("", e);
		}
	}

	private void doImportMetadataKeys() {
		if (validateIsMandatory(urlEl)) {
			try {
				EdusharingProperties properties = edusharingService.getEdusharingRepoConfig();
				String publicKey = properties.getPublicKey();
				repoPublicKeyEl.setValue(publicKey);
			} catch (Exception e) {
				showWarning("admin.get.repo.config.failed");
			}
		}
	}
	
	private void doTest() {
		try {
			Ticket ticket = edusharingService.createTicket(getIdentity());
			if (ticket != null) {
				showInfo("admin.test.successful");
				return;
			}
		} catch (Exception e) {
			log.error("", e);
		}
		showInfo("admin.test.unsuccessful");
	 }

}
