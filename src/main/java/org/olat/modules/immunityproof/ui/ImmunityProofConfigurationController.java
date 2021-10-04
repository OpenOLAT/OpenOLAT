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
package org.olat.modules.immunityproof.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
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
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nItem;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.immunityproof.ImmunityProof;
import org.olat.modules.immunityproof.ImmunityProofContext;
import org.olat.modules.immunityproof.ImmunityProofModule;
import org.olat.modules.immunityproof.ImmunityProofService;
import org.olat.modules.immunityproof.manager.ImmunityProofCertificateChecker;
import org.olat.modules.immunityproof.ui.event.ImmunityProofDeleteEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 08.09.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ImmunityProofConfigurationController extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(ImmunityProofConfigurationController.class);
	
	private FormLayoutContainer generalConfig;
	private MultipleSelectionElement enabledEl;
	private FormLink deleteAllProofsLink;
	
	private FormLayoutContainer validityConfig;
	private TextElement validVaccines;
	private IntegerElement validityPeriodVaccination;
	private IntegerElement maxValidityPeriodVaccination;
	private IntegerElement validityPeriodRecovery;
	private IntegerElement maxValidityPeriodRecovery;
	private IntegerElement validityPeriodTestPCR;
	private IntegerElement validityPeriodTestAntigen;
	private TextElement customHelpLinkEl;
	
	private FormLayoutContainer mailConfig;
	private IntegerElement reminderBeforeExpirationEl;
    private FormLink reminderMailReset;
    private FormLink reminderMailCustomize;
    private FormLink mailCommissionerAddedReset;
    private FormLink mailCommissionerAddedCustomize;
    private FormLink mailCommissionerRemovedReset;
    private FormLink mailCommissionerRemovedCustomize;
    private FormLink mailAllCertificatesRemovedReset;
    private FormLink mailAllCertificatesRemovedCustomize;
    
	private FormLayoutContainer scanConfig;
	private MultipleSelectionElement scanEnabledEl;
	private IntegerElement firstNameAccordanceEl;
	private IntegerElement lastNameAccordanceEl;
	private IntegerElement birthDateAccordanceEl;

    private ImmunityProofConfirmResetController confirmResetReminderMailController;
    private ImmunityProofConfirmResetController confirmResetCommissionerAddedMailController;
    private ImmunityProofConfirmResetController confirmResetCommissionerRemovedMailController;
    private ImmunityProofConfirmResetController confirmResetCertificatesRemovedMailController;
    private ImmunityProofConfirmDeleteAllProofsController confirmDeleteProofController;
    private SingleKeyTranslatorController singleKeyTranslatorController;
    private CloseableModalController cmc;
	
	@Autowired
	private ImmunityProofModule immunityProofModule;
	@Autowired
    private I18nModule i18nModule;
	@Autowired	
    private I18nManager i18nManager;
	@Autowired
	private ImmunityProofService immunityProofService;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private BaseSecurity securityManager;
	
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
		
		// Delete button
		deleteAllProofsLink = uifactory.addFormLink("delete.all.proofs", generalConfig, Link.BUTTON);
		deleteAllProofsLink.setCustomEnabledLinkCSS("btn btn-danger");
		deleteAllProofsLink.setIconLeftCSS("o_icon o_icon_lg o_icon_clear_all");
		
		
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
		
		maxValidityPeriodVaccination = uifactory.addIntegerElement("validity.max.vaccination", 0, validityConfig);
		maxValidityPeriodVaccination.setDisplaySize(3);
		maxValidityPeriodVaccination.setMaxLength(3);
		maxValidityPeriodVaccination.setElementCssClass("form-inline");
		maxValidityPeriodVaccination.setTextAddOn("days");

		// Validity period when recovered
		validityPeriodRecovery = uifactory.addIntegerElement("validity.recovery", 0, validityConfig);
		validityPeriodRecovery.setDisplaySize(3);
		validityPeriodRecovery.setMaxLength(3);
		validityPeriodRecovery.setElementCssClass("form-inline");
		validityPeriodRecovery.setTextAddOn("days");
		
		maxValidityPeriodRecovery = uifactory.addIntegerElement("validity.max.recovery", 0, validityConfig);
		maxValidityPeriodRecovery.setDisplaySize(3);
		maxValidityPeriodRecovery.setMaxLength(3);
		maxValidityPeriodRecovery.setElementCssClass("form-inline");
		maxValidityPeriodRecovery.setTextAddOn("days");
		
		// Validity period when tested with PCR
		validityPeriodTestPCR = uifactory.addIntegerElement("validity.test.pcr", 0, validityConfig);
		validityPeriodTestPCR.setDisplaySize(3);
		validityPeriodTestPCR.setMaxLength(1);
		validityPeriodTestPCR.setElementCssClass("form-inline");
		validityPeriodTestPCR.setTextAddOn("days");
		validityPeriodTestPCR.setHelpTextKey("validity.test.help", null);
		
		// Validity period when tested with antigen
		validityPeriodTestAntigen = uifactory.addIntegerElement("validity.test.antigen", 0, validityConfig);
		validityPeriodTestAntigen.setDisplaySize(3);
		validityPeriodTestAntigen.setMaxLength(1);
		validityPeriodTestAntigen.setElementCssClass("form-inline");
		validityPeriodTestAntigen.setTextAddOn("days");
		validityPeriodTestAntigen.setHelpTextKey("validity.test.help", null);
		
		// Custom help link
		customHelpLinkEl = uifactory.addTextElement("custom.help.link", -1, "", validityConfig);
		
		
		// Reminder config 
		mailConfig = FormLayoutContainer.createDefaultFormLayout("mailConfig", getTranslator());
		mailConfig.setRootForm(mainForm);
		mailConfig.setFormTitle(translate("config.reminder"));
		formLayout.add("reminderConfig", mailConfig);
		
		// Reminder period
		reminderBeforeExpirationEl = uifactory.addIntegerElement("reminder.before.expiration", 0, mailConfig);
		reminderBeforeExpirationEl.setDisplaySize(3);
		reminderBeforeExpirationEl.setMaxLength(2);
		reminderBeforeExpirationEl.setElementCssClass("form-inline");
		reminderBeforeExpirationEl.setTextAddOn("days");
		
		// Reminder mail
		FormLayoutContainer reminderMailButtons = FormLayoutContainer.createButtonLayout("reminderMailButtons", getTranslator());
		reminderMailButtons.setLabel("reminder.mail.customize", null); 
		mailConfig.add(reminderMailButtons);
        reminderMailReset = uifactory.addFormLink("reminder.reset", "reset", null, reminderMailButtons, Link.BUTTON);
        reminderMailCustomize = uifactory.addFormLink("reminder.customize", "customize", null, reminderMailButtons, Link.BUTTON);
        
        // All certs deleted mail
        FormLayoutContainer certificatesDeletedButtons = FormLayoutContainer.createButtonLayout("certificateDeletedButtons", getTranslator());
        certificatesDeletedButtons.setLabel("mail.template.proof.deleted", null);
		mailConfig.add(certificatesDeletedButtons);
        mailAllCertificatesRemovedReset = uifactory.addFormLink("deleted.reset", "reset", null, certificatesDeletedButtons, Link.BUTTON);
        mailAllCertificatesRemovedCustomize = uifactory.addFormLink("deleted.customize", "customize", null, certificatesDeletedButtons, Link.BUTTON);
        
        // Commissioner mail
        FormLayoutContainer commissionerAddedButtons = FormLayoutContainer.createButtonLayout("commissionerAddedButtons", getTranslator());
        commissionerAddedButtons.setLabel("mail.template.commissioner.added", null);
		mailConfig.add(commissionerAddedButtons);
        mailCommissionerAddedReset = uifactory.addFormLink("commissioner.added.reset", "reset", null, commissionerAddedButtons, Link.BUTTON);
        mailCommissionerAddedCustomize = uifactory.addFormLink("commissioner.added.customzie", "customize", null, commissionerAddedButtons, Link.BUTTON);
        
        FormLayoutContainer commissionerRemovedButtons = FormLayoutContainer.createButtonLayout("commissionerRemovedButtons", getTranslator());
        commissionerRemovedButtons.setLabel("mail.template.commissioner.removed", null);
		mailConfig.add(commissionerRemovedButtons);
        mailCommissionerRemovedReset = uifactory.addFormLink("commissioner.removed.reset", "reset", null, commissionerRemovedButtons, Link.BUTTON);
        mailCommissionerRemovedCustomize = uifactory.addFormLink("commissioner.removed.customize", "customize", null, commissionerRemovedButtons, Link.BUTTON);
		
		// Scan config
		scanConfig = FormLayoutContainer.createDefaultFormLayout("scanConfig", getTranslator());
		scanConfig.setRootForm(mainForm);
		scanConfig.setFormTitle(translate("config.scan"));
		formLayout.add(scanConfig);

		scanEnabledEl = uifactory.addCheckboxesVertical("scan.enabled", scanConfig, enabledOptions.keys(),
				enabledOptions.values(), 1);
		scanEnabledEl.addActionListener(FormEvent.ONCHANGE);
		firstNameAccordanceEl = uifactory.addIntegerElement("accordance.first.name", 0, scanConfig);
		lastNameAccordanceEl = uifactory.addIntegerElement("accordance.last.name", 0, scanConfig);
		birthDateAccordanceEl = uifactory.addIntegerElement("accordance.birthdate", 0, scanConfig);
		birthDateAccordanceEl.setHelpText(translate("accordance.birthdate.help"));

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
			mailConfig.setVisible(enabledEl.isAtLeastSelected(1));
			scanConfig.setVisible(enabledEl.isAtLeastSelected(1));
		} else if (source == reminderMailReset) {
			confirmResetReminderMailController = new ImmunityProofConfirmResetController(ureq, getWindowControl(), "reminder.mail.customize");
            cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmResetReminderMailController.getInitialComponent(), true, translate("reminder.mail.customize"), true, true);
            listenTo(confirmResetReminderMailController);
            listenTo(cmc);
            cmc.activate();
		} else if (source == reminderMailCustomize) {
			singleKeyTranslatorController = new SingleKeyTranslatorController(ureq, getWindowControl(), ImmunityProofModule.REMINDER_MAIL_TRANSLATION_KEY, ImmunityProofModule.class, SingleKeyTranslatorController.InputType.RICH_TEXT_ELEMENT, null);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), singleKeyTranslatorController.getInitialComponent(), true, translate("reminder.mail.customize"), true, true);
            listenTo(singleKeyTranslatorController);
            listenTo(cmc);
            cmc.activate();
		} else if (source == mailCommissionerAddedReset) {
			confirmResetCommissionerAddedMailController = new ImmunityProofConfirmResetController(ureq, getWindowControl(), "mail.template.commissioner.added");
            cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmResetCommissionerAddedMailController.getInitialComponent(), true, translate("mail.template.commissioner.added"), true, true);
            listenTo(confirmResetCommissionerAddedMailController);
            listenTo(cmc);
            cmc.activate();
		} else if (source == mailCommissionerAddedCustomize) {
			singleKeyTranslatorController = new SingleKeyTranslatorController(ureq, getWindowControl(), ImmunityProofModule.COMMISSIONER_ADDED_TRANSLATION_KEY, ImmunityProofModule.class, SingleKeyTranslatorController.InputType.RICH_TEXT_ELEMENT, null);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), singleKeyTranslatorController.getInitialComponent(), true, translate("mail.template.commissioner.added"), true, true);
            listenTo(singleKeyTranslatorController);
            listenTo(cmc);
            cmc.activate();
		} else if (source == mailCommissionerRemovedReset) {
			confirmResetCommissionerRemovedMailController = new ImmunityProofConfirmResetController(ureq, getWindowControl(), "mail.template.commissioner.removed");
            cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmResetCommissionerRemovedMailController.getInitialComponent(), true, translate("mail.template.commissioner.removed"), true, true);
            listenTo(confirmResetCommissionerRemovedMailController);
            listenTo(cmc);
            cmc.activate();
		} else if (source == mailCommissionerRemovedCustomize) {
			singleKeyTranslatorController = new SingleKeyTranslatorController(ureq, getWindowControl(), ImmunityProofModule.COMMISSIONER_REMOVED_TRANSLATION_KEY, ImmunityProofModule.class, SingleKeyTranslatorController.InputType.RICH_TEXT_ELEMENT, null);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), singleKeyTranslatorController.getInitialComponent(), true, translate("mail.template.commissioner.removed"), true, true);
            listenTo(singleKeyTranslatorController);
            listenTo(cmc);
            cmc.activate();
		} else if (source == mailAllCertificatesRemovedReset) {
			confirmResetCertificatesRemovedMailController = new ImmunityProofConfirmResetController(ureq, getWindowControl(), "mail.template.proof.deleted");
            cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmResetCertificatesRemovedMailController.getInitialComponent(), true, translate("mail.template.proof.deleted"), true, true);
            listenTo(confirmResetCertificatesRemovedMailController);
            listenTo(cmc);
            cmc.activate();
		} else if (source == mailAllCertificatesRemovedCustomize) {
			singleKeyTranslatorController = new SingleKeyTranslatorController(ureq, getWindowControl(), ImmunityProofModule.PROOF_DELETED_TRANSLATION_KEY, ImmunityProofModule.class, SingleKeyTranslatorController.InputType.RICH_TEXT_ELEMENT, null);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), singleKeyTranslatorController.getInitialComponent(), true, translate("mail.template.proof.deleted"), true, true);
            listenTo(singleKeyTranslatorController);
            listenTo(cmc);
            cmc.activate();
		} else if (source == deleteAllProofsLink) {
			long count = immunityProofService.getImmunityProofCount();
			confirmDeleteProofController = new ImmunityProofConfirmDeleteAllProofsController(ureq, getWindowControl(), count);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeleteProofController.getInitialComponent(), true, translate("delete.all.proofs", new String[] {String.valueOf(count)}), true, true);
            listenTo(confirmDeleteProofController);
            listenTo(cmc);
            cmc.activate();
		} else if (source == scanEnabledEl) {
			if (scanEnabledEl.isAtLeastSelected(1) && !verifyScriptSetupSuccessfully()) {
				scanEnabledEl.select("on", false);
			}
		}
	}
	
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmResetReminderMailController) {
			if (event == FormEvent.DONE_EVENT) {
	            resetI18nKeys(ImmunityProofModule.REMINDER_MAIL_TRANSLATION_KEY);
	            loadConfiguration();
            }
			
			cleanUp();
		} if (source == confirmResetCertificatesRemovedMailController) {
			if (event == FormEvent.DONE_EVENT) {
	            resetI18nKeys(ImmunityProofModule.PROOF_DELETED_TRANSLATION_KEY);
	            loadConfiguration();
            }
			
			cleanUp();
		} if (source == confirmResetCommissionerAddedMailController) {
			if (event == FormEvent.DONE_EVENT) {
	            resetI18nKeys(ImmunityProofModule.COMMISSIONER_ADDED_TRANSLATION_KEY);
	            loadConfiguration();
            }
			
			cleanUp();
		} if (source == confirmResetCommissionerRemovedMailController) {
			if (event == FormEvent.DONE_EVENT) {
	            resetI18nKeys(ImmunityProofModule.COMMISSIONER_REMOVED_TRANSLATION_KEY);
	            loadConfiguration();
            }
			
			cleanUp();
		} else if (source == singleKeyTranslatorController) {
            if (event == FormEvent.DONE_EVENT) {
                loadConfiguration();
            }

            cleanUp();
        } else if (source == confirmDeleteProofController) {
        	if (event == ImmunityProofDeleteEvent.DELETE_AND_NOTIFY) {
        		sendMail();
                immunityProofService.deleteAllImmunityProofs(true);
                showWarning("delete.proof.confirmation");
                loadConfiguration();
            } else if (event == ImmunityProofDeleteEvent.DELETE) {
            	immunityProofService.deleteAllImmunityProofs(false);
            	showWarning("delete.proof.confirmation");
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

        removeAsListenerAndDispose(confirmResetCommissionerRemovedMailController);
        removeAsListenerAndDispose(confirmResetCertificatesRemovedMailController);
        removeAsListenerAndDispose(confirmResetCommissionerAddedMailController);
        removeAsListenerAndDispose(confirmResetReminderMailController);
        removeAsListenerAndDispose(confirmDeleteProofController);
        removeAsListenerAndDispose(cmc);
        
        confirmResetCommissionerRemovedMailController = null;
        confirmResetCertificatesRemovedMailController = null;
        confirmResetCommissionerAddedMailController = null;
        confirmResetReminderMailController = null;
        confirmDeleteProofController = null;
        cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		immunityProofModule.setEnabled(enabledEl.isAtLeastSelected(1));
		
		immunityProofModule.setValidVaccines(validVaccines.getValue());
		immunityProofModule.setValidityVaccination(validityPeriodVaccination.getIntValue());
		immunityProofModule.setMaxValidityVaccination(maxValidityPeriodVaccination.getIntValue());
		immunityProofModule.setValidityRecovery(validityPeriodRecovery.getIntValue());
		immunityProofModule.setMaxValidityRecovery(maxValidityPeriodRecovery.getIntValue());
		immunityProofModule.setValidityPCR(validityPeriodTestPCR.getIntValue());
		immunityProofModule.setValidityAntigen(validityPeriodTestAntigen.getIntValue());
		
		immunityProofModule.setCustomHelpLink(customHelpLinkEl.getValue());
		
		immunityProofModule.setReminderPeriod(reminderBeforeExpirationEl.getIntValue());
		
		immunityProofModule.setScanningEnabled(scanEnabledEl.isAtLeastSelected(1));
		immunityProofModule.setAccordanceFirstName(firstNameAccordanceEl.getIntValue());
		immunityProofModule.setAccordanceLastName(lastNameAccordanceEl.getIntValue());
		immunityProofModule.setAccordanceBirthdate(birthDateAccordanceEl.getIntValue());

		loadConfiguration();
		
		fireEvent(ureq, FormEvent.CHANGED_EVENT);
	}

	@Override
	protected void doDispose() {
		
	}
	
	private void loadConfiguration() {
		long count = immunityProofService.getImmunityProofCount();
		deleteAllProofsLink.setI18nKey("delete.all.proofs", new String[] {String.valueOf(count)});
		deleteAllProofsLink.setVisible(count > 0);
		
		validityConfig.setVisible(immunityProofModule.isEnabled());
		mailConfig.setVisible(immunityProofModule.isEnabled());
		scanConfig.setVisible(immunityProofModule.isEnabled());
		
		enabledEl.select("on", immunityProofModule.isEnabled());
		
		validVaccines.setValue(immunityProofModule.getValidVaccines());
		validityPeriodVaccination.setIntValue(immunityProofModule.getValidityVaccination());
		maxValidityPeriodVaccination.setIntValue(immunityProofModule.getMaxValidityVaccination());
		validityPeriodRecovery.setIntValue(immunityProofModule.getValidityRecovery());
		maxValidityPeriodRecovery.setIntValue(immunityProofModule.getMaxValidityRecovery());
		validityPeriodTestPCR.setIntValue(immunityProofModule.getValidityPCR());
		validityPeriodTestAntigen.setIntValue(immunityProofModule.getValidityAntigen());
		
		customHelpLinkEl.setValue(immunityProofModule.getCustomHelpLink());
		
		reminderBeforeExpirationEl.setIntValue(immunityProofModule.getReminderPeriod());

		scanEnabledEl.select("on", immunityProofModule.isScanningEnabled());
		firstNameAccordanceEl.setIntValue(immunityProofModule.getAccordanceFirstName());
		lastNameAccordanceEl.setIntValue(immunityProofModule.getAccordanceLastName());
		birthDateAccordanceEl.setIntValue(immunityProofModule.getAccordanceBirthdate());
	}
	
	private void sendMail() {
		List<ImmunityProof> immunityProofs = immunityProofService.getAllCertificates();
		
		for (ImmunityProof certificate : immunityProofs) {
			// Create translator for user
			Identity identity = securityManager.loadIdentityByKey(certificate.getIdentity().getKey(), true);
			if (identity == null) {
				continue;
			}
			
			User user = identity.getUser();
			
			Locale userLocale = i18nManager.getLocaleOrDefault(identity.getUser().getPreferences().getLanguage());
			Translator userTranslator = Util.createPackageTranslator(ImmunityProofModule.class, userLocale);
			
			String name = "";
			String url = Settings.getServerContextPathURI() + "/covid";
			
			if (StringHelper.containsNonWhitespace(user.getFirstName())) {
				name += user.getFirstName();
			}
			
			if (StringHelper.containsNonWhitespace(user.getLastName())) {
				name += " " + user.getLastName();
			}
			
			if (!StringHelper.containsNonWhitespace(name)) {
				name = user.getNickName();
			}
			
			String[] params = new String[] {name, url};	
			
			String subject = userTranslator.translate("immunity.proof.deleted.mail.subject");
	        String body = userTranslator.translate("immunity.proof.deleted.mail.body", params);
	        String decoratedBody = mailManager.decorateMailBody(body, userLocale);
	        String recipientAddress = user.getEmail();
	        Address from;
	        Address[] to;
	
	        try {
	            from = new InternetAddress(WebappHelper.getMailConfig("mailSupport"));
	            to = new Address[] {new InternetAddress(((recipientAddress)))};
	        } catch (AddressException e) {
	            log.error("Could not send COVID certificate deletion notification message, bad mail address", e);
	            return;
	        }
	
	        MailerResult result = new MailerResult();
	        MimeMessage msg = mailManager.createMimeMessage(from, to, null, null, subject, decoratedBody, null, result);
	        mailManager.sendMessage(msg, result);
	        if (!result.isSuccessful()) {
	            log.error("Could not send COVID certificate deletion notification message to " + recipientAddress);
	        }
        
		}
    }

	private boolean verifyScriptSetupSuccessfully() {
		List<String> cmds = new ArrayList<String>();
		cmds.add(immunityProofModule.getPythonDir());
		cmds.add(immunityProofModule.getValidationScriptDir() + "/verify_ehc.py");
		cmds.add("--certs-file");
		cmds.add(immunityProofModule.getValidationScriptDir() + "/european_trustlits.json");
		cmds.add("--image");
		cmds.add(immunityProofModule.getValidationScriptDir() + "/examples/valid_cert.png");

		CountDownLatch doneSignal = new CountDownLatch(1);
		ImmunityProofContext context = new ImmunityProofContext();

		ImmunityProofCertificateChecker certificateChecker = new ImmunityProofCertificateChecker(immunityProofModule,
				context, cmds, doneSignal);
		certificateChecker.start();

		boolean isActivated = false;

		try {
			if (doneSignal.await(5000, TimeUnit.MILLISECONDS)) {
				context = certificateChecker.getContext();

				isActivated = context.isCertificateFound();
			} else {
				isActivated = false;
			}
		} catch (InterruptedException e) {
			isActivated = false;
		}

		certificateChecker.destroyProcess();

		if (isActivated) {
			log.info("Scanning successfully enabled");
			getWindowControl().setInfo(translate("info.activated.successfully"));
		} else {
			log.error("Scanning could not be enabled");
			getWindowControl().setError(translate("info.not.activated"));
		}

		return isActivated;
	}
}
