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
package org.olat.login.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;
import static org.olat.login.LoginModule.AT_LEAST_1;
import static org.olat.login.LoginModule.AT_LEAST_2;
import static org.olat.login.LoginModule.AT_LEAST_3;
import static org.olat.login.LoginModule.DISABLED;
import static org.olat.login.LoginModule.FORBIDDEN;
import static org.olat.login.LoginModule.VALIDATE_SEPARATELY;
import static org.olat.login.ui.LoginUIFactory.validateInteger;

import java.util.Arrays;
import java.util.Collection;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.login.LoginModule;
import org.olat.login.validation.PasswordValidationConfig;
import org.olat.login.validation.PasswordValidationConfig.Builder;
import org.olat.login.validation.PasswordValidationRulesFactory;
import org.olat.login.validation.SyntaxValidator;
import org.olat.login.validation.ValidationRulesProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PasswordSyntaxController extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(PasswordSyntaxController.class);
	
	private static final String FORBIDDEN_USERNAME = "username";
	private static final String FORBIDDEN_FIRSTNAME = "firstname";
	private static final String FORBIDDEN_LASTNAME = "lastname";

	private TextElement minLengthEl;
	private TextElement maxLengthEl;
	private SingleSelection lettersEl;
	private SingleSelection lettersUppercaseEl;
	private SingleSelection lettersLowercaseEl;
	private SingleSelection digitsAndSpecialsEl;
	private SingleSelection digitsEl;
	private SingleSelection specialsEl;
	private MultipleSelectionElement forbiddenValuesEl;
	private SingleSelection historyEl;
	private FormLink previewLink;
	
	private CloseableModalController cmc;
	private PasswordPreviewController previewCtrl;
	
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private PasswordValidationRulesFactory passwordValidationRulesFactory;

	public PasswordSyntaxController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(LoginModule.class, ureq.getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		minLengthEl = uifactory.addTextElement("admin.syntax.min.length", 10,
				String.valueOf(loginModule.getPasswordMinLength()), formLayout);
		minLengthEl.setMandatory(true);

		maxLengthEl = uifactory.addTextElement("admin.syntax.max.length", 10,
				String.valueOf(loginModule.getPasswordMaxLength()), formLayout);
		maxLengthEl.setMandatory(true);
		
		SelectionValues lettersKV = new SelectionValues();
		lettersKV.add(entry(DISABLED, translate("admin.syntax.permitted")));
		lettersKV.add(entry(AT_LEAST_1, translate("admin.syntax.min.1")));
		lettersKV.add(entry(AT_LEAST_2, translate("admin.syntax.min.2")));
		lettersKV.add(entry(AT_LEAST_3, translate("admin.syntax.min.3")));
		lettersKV.add(entry(FORBIDDEN, translate("admin.syntax.forbidden")));
		lettersKV.add(entry(VALIDATE_SEPARATELY, translate("admin.syntax.letters.lower.upper")));
		lettersEl = uifactory.addDropdownSingleselect("admin.syntax.letters", formLayout, lettersKV.keys(),
				lettersKV.values());
		if (Arrays.asList(lettersKV.keys()).contains(loginModule.getPasswordLetters())) {
			lettersEl.select(loginModule.getPasswordLetters(), true);
		}
		lettersEl.addActionListener(FormEvent.ONCHANGE);
		
		SelectionValues lettersUppercaseKV = new SelectionValues();
		lettersUppercaseKV.add(entry(DISABLED, translate("admin.syntax.permitted")));
		lettersUppercaseKV.add(entry(AT_LEAST_1, translate("admin.syntax.min.1")));
		lettersUppercaseKV.add(entry(AT_LEAST_2, translate("admin.syntax.min.2")));
		lettersUppercaseKV.add(entry(AT_LEAST_3, translate("admin.syntax.min.3")));
		lettersUppercaseKV.add(entry(FORBIDDEN, translate("admin.syntax.forbidden")));
		lettersUppercaseEl = uifactory.addDropdownSingleselect("admin.syntax.letters.uppercase", formLayout,
				lettersUppercaseKV.keys(), lettersUppercaseKV.values());
		if (Arrays.asList(lettersUppercaseKV.keys()).contains(loginModule.getPasswordLettersUppercase())) {
			lettersUppercaseEl.select(loginModule.getPasswordLettersUppercase(), true);
		}
		
		SelectionValues lettersLowercaseKV = new SelectionValues();
		lettersLowercaseKV.add(entry(DISABLED, translate("admin.syntax.permitted")));
		lettersLowercaseKV.add(entry(AT_LEAST_1, translate("admin.syntax.min.1")));
		lettersLowercaseKV.add(entry(AT_LEAST_2, translate("admin.syntax.min.2")));
		lettersLowercaseKV.add(entry(AT_LEAST_3, translate("admin.syntax.min.3")));
		lettersLowercaseKV.add(entry(FORBIDDEN, translate("admin.syntax.forbidden")));
		lettersLowercaseEl = uifactory.addDropdownSingleselect("admin.syntax.letters.lowercase", formLayout,
				lettersLowercaseKV.keys(), lettersLowercaseKV.values());
		if (Arrays.asList(lettersLowercaseKV.keys()).contains(loginModule.getPasswordLettersLowercase())) {
			lettersLowercaseEl.select(loginModule.getPasswordLettersLowercase(), true);
		}
		
		SelectionValues digitsOrSpecialsKV = new SelectionValues();
		digitsOrSpecialsKV.add(entry(DISABLED, translate("admin.syntax.permitted")));
		digitsOrSpecialsKV.add(entry(AT_LEAST_1, translate("admin.syntax.min.1")));
		digitsOrSpecialsKV.add(entry(AT_LEAST_2, translate("admin.syntax.min.2")));
		digitsOrSpecialsKV.add(entry(AT_LEAST_3, translate("admin.syntax.min.3")));
		digitsOrSpecialsKV.add(entry(FORBIDDEN, translate("admin.syntax.forbidden")));
		digitsOrSpecialsKV.add(entry(VALIDATE_SEPARATELY, translate("admin.syntax.digits.or.specials")));
		digitsAndSpecialsEl = uifactory.addDropdownSingleselect("admin.syntax.digits.specials", formLayout, digitsOrSpecialsKV.keys(),
				digitsOrSpecialsKV.values());
		if (Arrays.asList(digitsOrSpecialsKV.keys()).contains(loginModule.getPasswordDigitsAndSpecialSigns())) {
			digitsAndSpecialsEl.select(loginModule.getPasswordDigitsAndSpecialSigns(), true);
		}
		digitsAndSpecialsEl.addActionListener(FormEvent.ONCHANGE);
		
		SelectionValues digitsKV = new SelectionValues();
		digitsKV.add(entry(DISABLED, translate("admin.syntax.permitted")));
		digitsKV.add(entry(AT_LEAST_1, translate("admin.syntax.min.1")));
		digitsKV.add(entry(AT_LEAST_2, translate("admin.syntax.min.2")));
		digitsKV.add(entry(AT_LEAST_3, translate("admin.syntax.min.3")));
		digitsKV.add(entry(FORBIDDEN, translate("admin.syntax.forbidden")));
		digitsEl = uifactory.addDropdownSingleselect("admin.syntax.digits", formLayout, digitsKV.keys(),
				digitsKV.values());
		if (Arrays.asList(digitsKV.keys()).contains(loginModule.getPasswordDigits())) {
			digitsEl.select(loginModule.getPasswordDigits(), true);
		}
		
		SelectionValues specialsKV = new SelectionValues();
		specialsKV.add(entry(DISABLED, translate("admin.syntax.permitted")));
		specialsKV.add(entry(AT_LEAST_1, translate("admin.syntax.min.1")));
		specialsKV.add(entry(AT_LEAST_2, translate("admin.syntax.min.2")));
		specialsKV.add(entry(AT_LEAST_3, translate("admin.syntax.min.3")));
		specialsKV.add(entry(FORBIDDEN, translate("admin.syntax.forbidden")));
		specialsEl = uifactory.addDropdownSingleselect("admin.syntax.specials", formLayout, specialsKV.keys(),
				specialsKV.values());
		if (Arrays.asList(specialsKV.keys()).contains(loginModule.getPasswordSpecialSigns())) {
			specialsEl.select(loginModule.getPasswordSpecialSigns(), true);
		}
		
		SelectionValues forbiddenValuesKV = new SelectionValues();
		forbiddenValuesKV.add(entry(FORBIDDEN_USERNAME, translate("admin.syntax.forbidden.username")));
		forbiddenValuesKV.add(entry(FORBIDDEN_FIRSTNAME, translate("admin.syntax.forbidden.firstname")));
		forbiddenValuesKV.add(entry(FORBIDDEN_LASTNAME, translate("admin.syntax.forbidden.lastname")));
		forbiddenValuesEl = uifactory.addCheckboxesVertical("admin.syntax.forbidden.values", formLayout,
				forbiddenValuesKV.keys(), forbiddenValuesKV.values(), 1);
		forbiddenValuesEl.select(FORBIDDEN_USERNAME, loginModule.isPasswordUsernameForbidden());
		forbiddenValuesEl.select(FORBIDDEN_FIRSTNAME, loginModule.isPasswordFirstnameForbidden());
		forbiddenValuesEl.select(FORBIDDEN_LASTNAME, loginModule.isPasswordLastnameForbidden());
		
		String selectedVal = Integer.toString(loginModule.getPasswordHistory());
		boolean hasVal = false;
		String[] historyKeys = new String[] { "0", "1", "2", "5", "10", "15" };
		for(String historyKey:historyKeys) {
			if(selectedVal.equals(historyKey)) {
				hasVal = true;
			}
		}
		String[] historyValues = new String[] { translate("disable.history"), translate("password.after","1"), translate("password.after","2"), translate("password.after","5"), translate("password.after","10"), translate("password.after","15")};
		if(!hasVal) {
			historyKeys = append(historyKeys, selectedVal);
			historyValues = append(historyValues, selectedVal);
		}
		historyEl = uifactory.addDropdownSingleselect("password.history", "password.history", formLayout, historyKeys, historyValues, null);
		historyEl.select(selectedVal, true);
		
		updateLettersUI();
		updateDigitsSpecialsUI();
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		previewLink = uifactory.addFormLink("admin.syntax.preview", buttonsCont, Link.BUTTON);
		previewLink.setIconLeftCSS("o_icon o_icon_preview");
		previewLink.getComponent().setSuppressDirtyFormWarning(true);
	}
	
	private String[] append(String[] array, String val) {
		String[] newArray = new String[array.length + 1];
		System.arraycopy(array, 0, newArray, 0, array.length);
		newArray[array.length] = val;
		return newArray;
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		if (fiSrc != this.previewLink) {
			super.propagateDirtinessToContainer(fiSrc, fe);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == lettersEl) {
			updateLettersUI();
		} else if (source == digitsAndSpecialsEl) {
			updateDigitsSpecialsUI();
		} else if (source == previewLink) {
			doOpenPreview(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void updateLettersUI() {
		boolean validateSeparately = lettersEl.isOneSelected() && VALIDATE_SEPARATELY.equals(lettersEl.getSelectedKey());
		lettersUppercaseEl.setVisible(validateSeparately);
		lettersLowercaseEl.setVisible(validateSeparately);
	}

	private void updateDigitsSpecialsUI() {
		boolean validateSeparately = digitsAndSpecialsEl.isOneSelected()
				&& VALIDATE_SEPARATELY.equals(digitsAndSpecialsEl.getSelectedKey());
		digitsEl.setVisible(validateSeparately);
		specialsEl.setVisible(validateSeparately);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		 if (source == previewCtrl) {
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(previewCtrl);
		removeAsListenerAndDispose(cmc);
		previewCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateInteger(minLengthEl, 1);
		allOk &= validateInteger(maxLengthEl, 1);
		if (allOk) {
			int min = Integer.parseInt(minLengthEl.getValue());
			int max = Integer.parseInt(maxLengthEl.getValue());
			if (max < min) {
				maxLengthEl.setErrorKey("error.password.length.lower", null);
				allOk = false;
			}
		}
		
		historyEl.clearError();
		if(!historyEl.isOneSelected()) {
			historyEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		PasswordValidationConfig config = getPasswordValidationConfig();
		loginModule.setPasswordMinLength(config.getPasswordMinLength());
		loginModule.setPasswordMaxLength(config.getPasswordMaxLength());
		loginModule.setPasswordLetters(config.getPasswordLetters());
		loginModule.setPasswordLettersUppercase(config.getPasswordLettersUppercase());
		loginModule.setPasswordLettersLowercase(config.getPasswordLettersLowercase());
		loginModule.setPasswordDigitsAndSpecialSigns(config.getPasswordDigitsAndSpecialSigns());
		loginModule.setPasswordDigits(config.getPasswordDigits());
		loginModule.setPasswordSpecialSigns(config.getPasswordSpecialSigns());
		loginModule.setPasswordUsernameForbidden(config.isPasswordUsernameForbidden());
		loginModule.setPasswordFirstnameForbidden(config.isPasswordFirstnameForbidden());
		loginModule.setPasswordLastnameForbidden(config.isPasswordLastnameForbidden());
		loginModule.setPasswordHistory(config.getPasswordHistory());
		
		log.info("Passwort syntax validation configuration changed by " + getIdentity());
	}
	
	private PasswordValidationConfig getPasswordValidationConfig() {
		Builder builder = PasswordValidationConfig.builder();
		
		int passwordMinLength = Integer.parseInt(minLengthEl.getValue());
		builder.withPasswordMinLength(passwordMinLength);

		int passwordMaxLength = Integer.parseInt(maxLengthEl.getValue());
		builder.withPasswordMaxLength(passwordMaxLength);
		
		String passwordLetters = lettersEl.isOneSelected()
				? lettersEl.getSelectedKey()
				: DISABLED;
		builder.withPasswordLetters(passwordLetters);
		
		String passwordLettersUppercase = VALIDATE_SEPARATELY.equals(passwordLetters)//NOSONAR
										&& lettersUppercaseEl.isOneSelected()
				? lettersUppercaseEl.getSelectedKey()
				: DISABLED;
		builder.withPasswordLettersUppercase(passwordLettersUppercase);
		
		String passwordLettersLowercase = VALIDATE_SEPARATELY.equals(passwordLetters)//NOSONAR
										&& lettersLowercaseEl.isOneSelected()
				? lettersLowercaseEl.getSelectedKey()
				: DISABLED;
		builder.withPasswordLettersLowercase(passwordLettersLowercase);
		
		String passwordDigitsAndSpecialSigns = digitsAndSpecialsEl.isOneSelected()
				? digitsAndSpecialsEl.getSelectedKey()
				: DISABLED;
		builder.withPasswordDigitsAndSpecialSigns(passwordDigitsAndSpecialSigns);
		
		String passwordDigits = VALIDATE_SEPARATELY.equals(passwordDigitsAndSpecialSigns)//NOSONAR
							&& digitsEl.isOneSelected()
				? digitsEl.getSelectedKey()
				: DISABLED;
		builder.withPasswordDigits(passwordDigits);
		
		String passwordSpecialSigns = VALIDATE_SEPARATELY.equalsIgnoreCase(passwordDigitsAndSpecialSigns)
									&& specialsEl.isOneSelected()
				? specialsEl.getSelectedKey()
				: DISABLED;
		builder.withPasswordSpecialSigns(passwordSpecialSigns);
		
		Collection<String> forbiddenValuesKeys = forbiddenValuesEl.getSelectedKeys();
		builder.withPasswordUsernameForbidden(forbiddenValuesKeys.contains(FORBIDDEN_USERNAME));
		builder.withPasswordFirstnameForbidden(forbiddenValuesKeys.contains(FORBIDDEN_FIRSTNAME));
		builder.withPasswordLastnameForbidden(forbiddenValuesKeys.contains(FORBIDDEN_LASTNAME));
		
		int history = Integer.parseInt(historyEl.getSelectedKey());
		builder.withPasswordHistory(history);
		
		return builder.build();
	}

	private void doOpenPreview(UserRequest ureq) {
		boolean allOk = validateFormLogic(ureq);
		
		if (allOk) {
			PasswordValidationConfig config = getPasswordValidationConfig();
			ValidationRulesProvider rulesProvider = passwordValidationRulesFactory.createRules(config);
			SyntaxValidator validator = new SyntaxValidator(rulesProvider, true);
			previewCtrl = new PasswordPreviewController(ureq, getWindowControl(), validator);
			listenTo(previewCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					previewCtrl.getInitialComponent(), true, translate("admin.syntax.preview.title"));
			cmc.activate();
			listenTo(cmc);
		}
	}

}
