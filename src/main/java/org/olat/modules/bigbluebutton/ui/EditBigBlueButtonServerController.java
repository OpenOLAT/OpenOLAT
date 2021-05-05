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
package org.olat.modules.bigbluebutton.ui;

import java.net.URI;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonServer;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.bigbluebutton.model.BigBlueButtonException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditBigBlueButtonServerController extends FormBasicController {

	private static final String PLACEHOLDER = "xxx-placeholder-xxx";
	private static final String[] onKeys = new String[] { "" };

	private FormLink checkLink;
	private TextElement urlEl;
	private TextElement sharedSecretEl;
	private TextElement capacityFactorEl;
	private MultipleSelectionElement enabledEl;
	private MultipleSelectionElement manualOnlyEl;

	private BigBlueButtonServer server;
	private String replacedSharedSecretValue;

	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	public EditBigBlueButtonServerController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}
	
	public EditBigBlueButtonServerController(UserRequest ureq, WindowControl wControl, BigBlueButtonServer server) {
		super(ureq, wControl);
		this.server = server;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String url = server == null ? null : server.getUrl();
		urlEl = uifactory.addTextElement("bbb.url", "option.baseurl", 255, url, formLayout);
		urlEl.setDisplaySize(60);
		urlEl.setExampleKey("option.baseurl.example", null);
		urlEl.setMandatory(true);

		String sharedSecret = server == null ? null : server.getSharedSecret();
		if(StringHelper.containsNonWhitespace(sharedSecret)) {
			replacedSharedSecretValue = sharedSecret;
			sharedSecret = PLACEHOLDER;
		}
		sharedSecretEl = uifactory.addPasswordElement("shared.secret", "option.bigbluebutton.shared.secret", 255, sharedSecret, formLayout);
		sharedSecretEl.setAutocomplete("new-password");
		sharedSecretEl.setMandatory(true);
		
		String capacityFactor = server == null || server.getCapacityFactory() == null
				? "1.0" : server.getCapacityFactory().toString();
		capacityFactorEl = uifactory.addTextElement("bbb.capacity", "option.capacity.factory", 255, capacityFactor, formLayout);
		capacityFactorEl.setDisplaySize(60);
		capacityFactorEl.setExampleKey("option.capacity.factor.example", null);
		
		String[] onValues = new String[] { translate("enabled") };
		enabledEl = uifactory.addCheckboxesVertical("option.enabled.server", formLayout, onKeys, onValues, 1);
		enabledEl.select(onKeys[0], server == null || server.isEnabled());

		manualOnlyEl = uifactory.addCheckboxesVertical("option.manual.only", formLayout, onKeys, new String[] { "" }, 1);
		manualOnlyEl.select(onKeys[0], server != null && server.isManualOnly());
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonLayout);
		checkLink = uifactory.addFormLink("check", buttonLayout, Link.BUTTON);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= validateUrlFields();
		if(allOk) {
			if(enabledEl.isAtLeastSelected(1)) {
				allOk &= validateConnection();
			}
			
			if((server == null || server.getKey() == null)
					&& bigBlueButtonManager.hasServer(urlEl.getValue())) {
				urlEl.setErrorKey("error.server.exists", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	private boolean validateUrlFields() {
		boolean allOk = true;
		
		allOk &= validateUrl(urlEl, true);
		//allOk &= validateUrl(recordingUrlEl, false);
		
		capacityFactorEl.clearError();
		if(StringHelper.containsNonWhitespace(capacityFactorEl.getValue())) {
			try {
				String factor = capacityFactorEl.getValue();
				double capacityFactory = Double.parseDouble(factor);
				if(capacityFactory < 1.0 || capacityFactory > 100.0) {
					capacityFactorEl.setErrorKey("error.capacity.factory", null);
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				capacityFactorEl.setErrorKey("error.capacity.factory", null);
				allOk &= false;
			}
		} else {
			capacityFactorEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		String password = sharedSecretEl.getValue();
		sharedSecretEl.clearError();
		if(!StringHelper.containsNonWhitespace(password)) {
			sharedSecretEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateUrl(TextElement el, boolean mandatory) {
		boolean allOk = true;
		
		String url = el.getValue();
		el.clearError();
		if(StringHelper.containsNonWhitespace(url)) {
			try {
				URI uri = new URI(url);
				uri.getHost();
			} catch(Exception e) {
				el.setErrorKey("error.url.invalid", null);
				allOk &= false;
			}
		} else if(mandatory) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateConnection() {
		boolean allOk = true;
		try {
			BigBlueButtonErrors errors = new BigBlueButtonErrors();
			boolean ok = checkConnection(errors);
			if(!ok || errors.hasErrors()) {
				sharedSecretEl.setValue("");
				urlEl.setErrorKey("error.connectionValidationFailed", new String[] {errors.getErrorMessages()});
				allOk &= false;
			}
		} catch (Exception e) {
			showError(BigBlueButtonException.SERVER_NOT_I18N_KEY);
			allOk &= false;
		}
		return allOk;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == checkLink) {
			if(validateUrlFields()) {
				doCheckConnection();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private Double getCapacityFactory() {
		String val = capacityFactorEl.getValue();
		Double factor = null;
		if(StringHelper.containsNonWhitespace(val)) {
			try {
				factor = Double.valueOf(val);
			} catch (NumberFormatException e) {
				logWarn("Cannot parse: " + val, null);
			}
		}
		
		if(factor == null || factor.doubleValue() < 1.0d) {
			factor = Double.valueOf(1.0d);
		}
		return factor;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(server == null) {
			String url = urlEl.getValue();	
			String sharedSecret = sharedSecretEl.getValue();
			if(!PLACEHOLDER.equals(sharedSecret)) {
				sharedSecretEl.setValue(PLACEHOLDER);
			} else if(StringHelper.containsNonWhitespace(replacedSharedSecretValue)) {
				sharedSecret = replacedSharedSecretValue;
			}
			server = bigBlueButtonManager.createServer(url, null, sharedSecret);
		} else {
			server.setUrl(urlEl.getValue());
			String sharedSecret = sharedSecretEl.getValue();
			if(!PLACEHOLDER.equals(sharedSecret)) {
				server.setSharedSecret(sharedSecret);
				sharedSecretEl.setValue(PLACEHOLDER);
			} else if(StringHelper.containsNonWhitespace(replacedSharedSecretValue)) {
				server.setSharedSecret(replacedSharedSecretValue);
			}
		}
		
		server.setEnabled(enabledEl.isAtLeastSelected(1));
		server.setManualOnly(manualOnlyEl.isAtLeastSelected(1));
		server.setCapacityFactory(getCapacityFactory());
		server = bigBlueButtonManager.updateServer(server);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private void doCheckConnection() {
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		boolean loginOk = checkConnection(errors);
		if(errors.hasErrors()) {
			getWindowControl().setError(BigBlueButtonErrorHelper.formatErrors(getTranslator(), errors));
		} else if(loginOk) {
			showInfo("connection.successful");
		} else {
			showError("connection.failed");
		}
	}
	
	private boolean checkConnection(BigBlueButtonErrors errors) {
		String url = urlEl.getValue();
		String sharedSecret = sharedSecretEl.getValue();
		if(PLACEHOLDER.equals(sharedSecret)) {
			if(StringHelper.containsNonWhitespace(replacedSharedSecretValue)) {
				sharedSecret = replacedSharedSecretValue;
			} else if(server != null) {
				sharedSecret = server.getSharedSecret();
			}
		} else {
			replacedSharedSecretValue = sharedSecret;
			sharedSecretEl.setValue(PLACEHOLDER);
		}
		return bigBlueButtonManager.checkConnection(url, sharedSecret, errors);
	}
}
