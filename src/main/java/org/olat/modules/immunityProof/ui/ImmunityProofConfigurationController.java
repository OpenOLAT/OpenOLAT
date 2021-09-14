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
package org.olat.modules.immunityProof.ui;

import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
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
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nItem;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController;
import org.olat.modules.immunityProof.ImmunityProof;
import org.olat.modules.immunityProof.ImmunityProofModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 08.09.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ImmunityProofConfigurationController extends FormBasicController {

	private FormLayoutContainer generalConfig;
	private MultipleSelectionElement enabledEl;
	
	private FormLayoutContainer validityConfig;
	private TextElement validVaccines;
	private IntegerElement validityPeriodVaccination;
	private IntegerElement validityPeriodRecovery;
	private IntegerElement validityPeriodTestPCR;
	private IntegerElement validityPeriodTestAntigen;
	
	private FormLayoutContainer reminderConfig;
	private IntegerElement reminderBeforeExpirationEl;
	private StaticTextElement reminderMailEl;
    private FormLink reminderMailReset;
    private FormLink reminderMailCustomize;
    
    private ImmunityProofConfirmResetController confirmResetMailController;
    private SingleKeyTranslatorController singleKeyTranslatorController;
    private CloseableModalController cmc;
	
	@Autowired
	private ImmunityProofModule immunityProofModule;
	@Autowired
    private I18nModule i18nModule;
	@Autowired	
    private I18nManager i18nManager;
	
	public ImmunityProofConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		
		setTranslator(Util.createPackageTranslator(ImmunityProof.class, getLocale(), getTranslator()));
		
		initForm(ureq);
		loadConfiguration();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// General config
		generalConfig = FormLayoutContainer.createDefaultFormLayout("generalConfig", getTranslator());
		generalConfig.setRootForm(mainForm);
		generalConfig.setFormTitle(translate("config.general"));
		formLayout.add("generalConfig", generalConfig);
		
		// Enable / Disable
		SelectionValues enabledOptions = new SelectionValues(SelectionValues.entry("on", translate("on")));
		enabledEl = uifactory.addCheckboxesVertical("config.enabled", generalConfig, enabledOptions.keys(), enabledOptions.values(), 1);
		enabledEl.addActionListener(FormEvent.ONCHANGE);
		
		
		// Validity config
		validityConfig = FormLayoutContainer.createDefaultFormLayout("validityConfig", getTranslator());
		validityConfig.setRootForm(mainForm);
		validityConfig.setFormTitle(translate("config.validity"));
		formLayout.add("validityConfig", validityConfig);
		
		// Valid Vaccines
		validVaccines = uifactory.addTextElement("valid.vaccines", -1, null, validityConfig);
		
		// Validity period when vaccinated
		validityPeriodVaccination = uifactory.addIntegerElement("validity.vaccination", 0, validityConfig);
		validityPeriodVaccination.setDisplaySize(3);
		validityPeriodVaccination.setMaxLength(3);
		validityPeriodVaccination.setElementCssClass("form-inline");
		validityPeriodVaccination.setTextAddOn("days");

		// Validity period when recovered
		validityPeriodRecovery = uifactory.addIntegerElement("validity.recovery", 0, validityConfig);
		validityPeriodRecovery.setDisplaySize(3);
		validityPeriodRecovery.setMaxLength(3);
		validityPeriodRecovery.setElementCssClass("form-inline");
		validityPeriodRecovery.setTextAddOn("days");
		
		// Validity period when tested with PCR
		validityPeriodTestPCR = uifactory.addIntegerElement("validity.test.pcr", 0, validityConfig);
		validityPeriodTestPCR.setDisplaySize(3);
		validityPeriodTestPCR.setMaxLength(1);
		validityPeriodTestPCR.setElementCssClass("form-inline");
		validityPeriodTestPCR.setTextAddOn("days");
		
		// Validity period when tested with antigen
		validityPeriodTestAntigen = uifactory.addIntegerElement("validity.test.antigen", 0, validityConfig);
		validityPeriodTestAntigen.setDisplaySize(3);
		validityPeriodTestAntigen.setMaxLength(1);
		validityPeriodTestAntigen.setElementCssClass("form-inline");
		validityPeriodTestAntigen.setTextAddOn("days");
		
		
		// Reminder config 
		reminderConfig = FormLayoutContainer.createDefaultFormLayout("reminderConfig", getTranslator());
		reminderConfig.setRootForm(mainForm);
		reminderConfig.setFormTitle(translate("config.reminder"));
		formLayout.add("reminderConfig", reminderConfig);
		
		// Reminder period
		reminderBeforeExpirationEl = uifactory.addIntegerElement("reminder.before.expiration", 0, reminderConfig);
		reminderBeforeExpirationEl.setDisplaySize(3);
		reminderBeforeExpirationEl.setMaxLength(2);
		reminderBeforeExpirationEl.setElementCssClass("form-inline");
		reminderBeforeExpirationEl.setTextAddOn("days");
		
		// Reminder mail
		reminderMailEl = uifactory.addStaticTextElement("reminder.mail", null, reminderConfig);
		FormLayoutContainer reminderMailButtons = FormLayoutContainer.createButtonLayout("reminderMailButtons", getTranslator());
		reminderConfig.add(reminderMailButtons);
        reminderMailReset = uifactory.addFormLink("reset", reminderMailButtons, Link.BUTTON);
        reminderMailCustomize = uifactory.addFormLink("customize", reminderMailButtons, Link.BUTTON);
		
		// Submit button
		FormLayoutContainer buttonWrapper = FormLayoutContainer.createDefaultFormLayout("buttonWrapper", getTranslator());
		buttonWrapper.setRootForm(mainForm);
		formLayout.add("buttonWrapper", buttonWrapper);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonLayout.setRootForm(mainForm);
		buttonWrapper.add(buttonLayout);
		
		uifactory.addFormSubmitButton("submit", buttonLayout);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enabledEl) {
			validityConfig.setVisible(enabledEl.isAtLeastSelected(1));
			reminderConfig.setVisible(enabledEl.isAtLeastSelected(1));
		} else if (source == reminderMailReset) {
			confirmResetMailController = new ImmunityProofConfirmResetController(ureq, getWindowControl());
            cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmResetMailController.getInitialComponent(), true, translate("reminder.mail.reset"), true, true);
            listenTo(confirmResetMailController);
            listenTo(cmc);
            cmc.activate();
		} else if (source == reminderMailCustomize) {
			singleKeyTranslatorController = new SingleKeyTranslatorController(ureq, getWindowControl(), ImmunityProofModule.REMINDER_MAIL_TRANSLATION_KEY, ImmunityProofModule.class, SingleKeyTranslatorController.InputType.RICH_TEXT_ELEMENT, null);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), singleKeyTranslatorController.getInitialComponent(), true, translate("reminder.mail.customize"), true, true);
            listenTo(singleKeyTranslatorController);
            listenTo(cmc);
            cmc.activate();
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmResetMailController) {
			if (event == FormEvent.DONE_EVENT) {
	            resetI18nKeys(ImmunityProofModule.REMINDER_MAIL_TRANSLATION_KEY);
	            loadConfiguration();
            }
			
			cleanUp();
		} else if (source == singleKeyTranslatorController) {
            if (event == FormEvent.DONE_EVENT) {
                loadConfiguration();
            }

            cleanUp();
        } else if (source == cmc) {
			cleanUp();
		}
	}
	
	private void resetI18nKeys(String i18nKey) {
        // save new values
        Map<Locale, Locale> allOverlays = i18nModule.getOverlayLocales();
        for (String locale : i18nModule.getEnabledLanguageKeys()) {
            I18nItem item = i18nManager.getI18nItem(ImmunityProofModule.class.getPackage().getName(), i18nKey, allOverlays.get(Locale.forLanguageTag(locale)));
            i18nManager.saveOrUpdateI18nItem(item, null);
        }
    }
	
	private void cleanUp() {
		if (cmc != null && cmc.isCloseable()) {
            cmc.deactivate();
        }

        removeAsListenerAndDispose(cmc);
        removeAsListenerAndDispose(confirmResetMailController);

        cmc = null;
        confirmResetMailController = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		immunityProofModule.setEnabled(enabledEl.isAtLeastSelected(1));
		
		immunityProofModule.setValidVaccines(validVaccines.getValue());
		immunityProofModule.setValidityVaccination(validityPeriodVaccination.getIntValue());
		immunityProofModule.setValidityRecovery(validityPeriodRecovery.getIntValue());
		immunityProofModule.setValidityPCR(validityPeriodTestPCR.getIntValue());
		immunityProofModule.setValidityAntigen(validityPeriodTestAntigen.getIntValue());
		
		immunityProofModule.setReminderPeriod(reminderBeforeExpirationEl.getIntValue());
		
		loadConfiguration();
		
		fireEvent(ureq, FormEvent.CHANGED_EVENT);
	}

	@Override
	protected void doDispose() {
		
	}
	
	private void loadConfiguration() {
		validityConfig.setVisible(immunityProofModule.isEnabled());
		reminderConfig.setVisible(immunityProofModule.isEnabled());
		
		enabledEl.select("on", immunityProofModule.isEnabled());
		
		validVaccines.setValue(immunityProofModule.getValidVaccines());
		validityPeriodVaccination.setIntValue(immunityProofModule.getValidityVaccination());
		validityPeriodRecovery.setIntValue(immunityProofModule.getValidityRecovery());
		validityPeriodTestPCR.setIntValue(immunityProofModule.getValidityPCR());
		validityPeriodTestAntigen.setIntValue(immunityProofModule.getValidityAntigen());
		
		reminderBeforeExpirationEl.setIntValue(immunityProofModule.getReminderPeriod());
		reminderMailEl.setValue(StringHelper.truncateText(StringHelper.xssScan(translate(ImmunityProofModule.REMINDER_MAIL_TRANSLATION_KEY, new String[]{String.valueOf(immunityProofModule.getReminderPeriod())}))));
	}
}
