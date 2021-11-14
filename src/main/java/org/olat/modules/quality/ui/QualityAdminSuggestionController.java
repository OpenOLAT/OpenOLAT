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
package org.olat.modules.quality.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.quality.QualityModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityAdminSuggestionController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	
	private MultipleSelectionElement enableEl;
	private TextElement emailAddressesEl;
	private TextElement emailSubjectEl;
	private TextElement emailBodyEl;
	
	@Autowired
	private QualityModule qualityModule;

	public QualityAdminSuggestionController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.suggestion.title");
		
		String[] onValues = new String[] { translate("on") };
		enableEl = uifactory.addCheckboxesHorizontal("admin.suggestion.enabled", formLayout, onKeys, onValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		if (qualityModule.isSuggestionEnabled()) {
			enableEl.select(onKeys[0], true);
		}
		
		String suggestionEemailAddresses = qualityModule.getSuggestionEmailAddresses().stream()
				.collect(Collectors.joining("\n"));
		emailAddressesEl = uifactory.addTextAreaElement("admin.suggestion.email.addresses",
				"admin.suggestion.email.addresses", -1, 3, 60, true, false, suggestionEemailAddresses, formLayout);

		String suggestionEemailSubject = qualityModule.getSuggestionEmailSubject();
		emailSubjectEl = uifactory.addTextElement("admin.suggestion.email.subject", 255, suggestionEemailSubject,
				formLayout);

		String suggestionEemailBody = qualityModule.getSuggestionEmailBody();
		emailBodyEl = uifactory.addRichTextElementForStringDataMinimalistic("admin.suggestion.email.body",
				"admin.suggestion.email.body", suggestionEemailBody, 8, 60, formLayout, getWindowControl());

		uifactory.addFormSubmitButton("save", formLayout);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		boolean enabled = enableEl.isAtLeastSelected(1);
		
		if (enabled) {
			allOk &= QualityUIFactory.validateIsMandatory(emailAddressesEl);
		}
		
		String emailAddressesStr = emailAddressesEl.getValue();
		if (StringHelper.containsNonWhitespace(emailAddressesStr)) {
			emailAddressesEl.clearError();
			List<String> emailAddresses = getEmailAddresses(emailAddressesStr);
			if (!QualityUIFactory.validateEmailAdresses(emailAddresses)) {
				emailAddressesEl.setErrorKey("error.email.invalid", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enableEl.isAtLeastSelected(1);
		qualityModule.setSuggestionEnabled(enabled);
		
		String emailAddressesStr = emailAddressesEl.getValue();
		List<String> emailAddresses = getEmailAddresses(emailAddressesStr);
		qualityModule.setSuggestionEmailAddresses(emailAddresses);
		
		String emailSubject = emailSubjectEl.getValue();
		qualityModule.setSuggestionEmailSubject(emailSubject);
		
		String emailBody = emailBodyEl.getValue();
		qualityModule.setSuggestionEmailBody(emailBody);
	}

	private List<String> getEmailAddresses(String value) {
		if (!StringHelper.containsNonWhitespace(value)) return new ArrayList<>(0);
		
		return Arrays.stream(value.split("\\r?\\n"))
				.map(String::trim)
				.collect(Collectors.toList());
	}
}
