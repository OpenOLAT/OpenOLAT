/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.ai.ui;

import org.olat.core.commons.services.ai.spi.openAI.OpenAiSPI;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Admin controller to configure the OpenAI AI service. 
 * 
 * Initial date: 13.06.2023<br>
 * 
 * @author gnaegi@frentix.com, http://www.frentix.com
 * *
 */
public class OpenAIAdminConfigFormController extends FormBasicController {
	private static final String PLACEHOLDER = "xxx-placeholder-xxx";

	private TextElement apiKeyEl;
	private TextElement chatModelEl;

	
	@Autowired
	private OpenAiSPI openAiSPI;

	/**
	 * Standard constructor
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public OpenAIAdminConfigFormController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("ai.openai.title");		
		setFormDescription("ai.openai.desc");
		
		String apiKey = openAiSPI.getApiKey();
		if(StringHelper.containsNonWhitespace(apiKey)) {
			apiKey = PLACEHOLDER;
		}
		apiKeyEl = uifactory.addPasswordElement("ai.openai.apikey", "ai.openai.apikey", 128, apiKey, formLayout);
		apiKeyEl.setMandatory(true);

		String chatModel = openAiSPI.getChatModel();
		chatModelEl = uifactory.addTextElement("ai.openai.chatmodel", "ai.openai.chatmodel", 64, chatModel, formLayout);
		chatModelEl.setPlaceholderText("gpt-3.5-turbo");
		chatModelEl.setMandatory(true);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormResetButton("reset", "reset", buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);		
		apiKeyEl.clearError();
		if(!StringHelper.containsNonWhitespace(apiKeyEl.getValue())) {
			apiKeyEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		chatModelEl.clearError();
		if(!StringHelper.containsNonWhitespace(chatModelEl.getValue())) {
			chatModelEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// just save to the service/module. No check for validity other than whitespace check
		String apiKey = apiKeyEl.getValue().trim();
		if(!PLACEHOLDER.equals(apiKey)) {
			openAiSPI.setApiKey(apiKey);
			apiKeyEl.setValue(PLACEHOLDER);
			// in log file show only beginning of api key for security reasons
			String logAPIKey = apiKey.length() > 7 ? apiKey.substring(0,6) + "..."  : "";
			logAudit("OpenAI API key has been updated. New value::", logAPIKey);										
		}

		String chatModel = chatModelEl.getValue();
		if (!chatModel.equals(openAiSPI.getChatModel())) {			
			openAiSPI.setChatModel(chatModel);
			logAudit("OpenAI API chat model Key has been updated. New value::", chatModel);										
		}
		
		fireEvent(ureq, Event.DONE_EVENT);		
	}
}
