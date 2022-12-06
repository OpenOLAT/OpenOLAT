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
package org.olat.modules.contacttracing.ui;

import java.util.Locale;
import java.util.Map;

import org.olat.core.commons.editor.htmleditor.HTMLEditorControllerWithoutFile;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.TranslatorHelper;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nItem;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController;
import org.olat.modules.contacttracing.ContactTracingModule;
import org.olat.modules.contacttracing.ContactTracingModule.AttributeState;
import org.olat.user.UserPropertiesConfig;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 12.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingConfigurationController extends FormBasicController {

    public static final String CONTACT_TRACING_CONFIRMATION_MAIL_KEY = "contact.tracing.registration.confirmation.mail.body";
    public static final String CONTACT_TRACING_REGISTRATION_INTRO_KEY = "contact.tracing.registration.intro";

    private static final String[] ENABLED_KEYS = new String[]{"on"};
    private static final String[] STATE_KEYS = new String[] {
            AttributeState.mandatory.name(),
            AttributeState.optional.name(),
            AttributeState.disabled.name()
    };
    private static final String[] EXTENDED_STATE_KEYS = new String[] {
            AttributeState.automatic.name(),
            AttributeState.mandatory.name(),
    };
    private final String[] STATE_VALUES;
    private final String[] EXTENDED_STATE_VALUES;

    private FormLayoutContainer generalConfig;
    private FormLayoutContainer questionnaireConfig;
    private FormLayoutContainer messagesConfig;

    private MultipleSelectionElement enabledEl;
    private TextElement retentionPeriodEl;
    private TextElement defaultDurationEl;
    private MultipleSelectionElement allowAnonymousRegistrationEl;

    private SingleSelection startTimeEL;
    private SingleSelection endTimeEl;
    private SingleSelection nickNameEl;
    private SingleSelection firstNameEl;
    private SingleSelection lastNameEl;
    private SingleSelection streetEL;
    private SingleSelection extraAddressLineEl;
    private SingleSelection zipCodeEl;
    private SingleSelection cityEl;
    private SingleSelection emailEl;
    private SingleSelection institutionalEmailEl;
    private SingleSelection genericEmailEl;
    private SingleSelection privatePhoneEl;
    private SingleSelection mobilePhoneEl;
    private SingleSelection officePhoneEl;

    private StaticTextElement registrationIntroEl;
    private FormLink registrationIntroReset;
    private FormLink registrationIntroCustomize;

    private StaticTextElement registrationConfirmationMailEl;
    private FormLink registrationConfirmationMailReset;
    private FormLink registrationConfirmationMailCustomize;

    private StaticTextElement qrCodeInstructionsEl;
    private FormLink qrCodeInstructionsReset;
    private FormLink qrCodeInstructionsCustomize;

    private CloseableModalController cmc;
    private SingleKeyTranslatorController singleKeyTranslatorController;
    private HTMLEditorControllerWithoutFile qrCodeInstructionsEditController;
    private ContactTracingConfirmResetController confirmResetIntroController;
    private ContactTracingConfirmResetController confirmResetMailController;
    private ContactTracingConfirmResetController confirmResetQRCodeInstructionsController;

    @Autowired
    private ContactTracingModule contactTracingModule;
    @Autowired
    private UserPropertiesConfig userPropertiesConfig;
    @Autowired
    private I18nModule i18nModule;
    @Autowired
    private I18nManager i18nManager;

    public ContactTracingConfigurationController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl, LAYOUT_VERTICAL);
        setTranslator(userPropertiesConfig.getTranslator(getTranslator()));
        setTranslator(Util.createPackageTranslator(UserPropertyHandler.class, getLocale(), getTranslator()));
        setTranslator(Util.createPackageTranslator(ContactTracingModule.class, getLocale(), getTranslator()));

        STATE_VALUES = TranslatorHelper.translateAll(getTranslator(), STATE_KEYS);
        EXTENDED_STATE_VALUES = TranslatorHelper.translateAll(getTranslator(), EXTENDED_STATE_KEYS);

        initForm(ureq);
        initNumericTextElement(retentionPeriodEl, "days");
        initNumericTextElement(defaultDurationEl, "minutes");
        initVelocityContainers();

        loadData();
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        // General contact tracing config
        generalConfig = FormLayoutContainer.createDefaultFormLayout("generalConfig", getTranslator());
        generalConfig.setRootForm(mainForm);
        generalConfig.setFormTitle(translate("contact.tracing.general.title"));
        generalConfig.setFormDescription(translate("contact.tracing.info"));
        generalConfig.setFormContextHelp("manual_admin/administration/Modules_Contact_Tracing/");
        formLayout.add("generalConfig", generalConfig);

        // Enable
        enabledEl = uifactory.addCheckboxesHorizontal("contact.tracing.enabled", generalConfig, ENABLED_KEYS, TranslatorHelper.translateAll(getTranslator(), ENABLED_KEYS));
        enabledEl.setElementCssClass("o_sel_contacttracing_enable");
        enabledEl.addActionListener(FormEvent.ONCHANGE);
        // Retention period
        retentionPeriodEl = uifactory.addTextElement("contact.tracing.retention.period", 3, null, generalConfig);
        retentionPeriodEl.setMandatory(true);
        // Default stay duration
        defaultDurationEl = uifactory.addTextElement("contact.tracing.duration.default", 3, null, generalConfig);
        defaultDurationEl.setMandatory(true);
        // Allow anonymous registration for registered users
        allowAnonymousRegistrationEl = uifactory.addCheckboxesHorizontal("contact.tracing.registration.anonymous.allowed.always.label", generalConfig, ENABLED_KEYS, TranslatorHelper.translateAll(getTranslator(), ENABLED_KEYS));
        allowAnonymousRegistrationEl.setHelpTextKey("contact.tracing.registration.anonymous.allowed.always.help", null);
        allowAnonymousRegistrationEl.setElementCssClass("o_sel_contacttracing_anonymous");
        allowAnonymousRegistrationEl.addActionListener(FormEvent.ONCHANGE);

        // Questionnaire config
        questionnaireConfig = FormLayoutContainer.createDefaultFormLayout("questionnaireConfig", getTranslator());
        questionnaireConfig.setRootForm(mainForm);
        questionnaireConfig.setFormTitle(translate("contact.tracing.questionnaire.title"));
        formLayout.add("questionnaireConfig", questionnaireConfig);

        // Start time
        startTimeEL = uifactory.addDropdownSingleselect("contact.tracing.start.time", questionnaireConfig, EXTENDED_STATE_KEYS, EXTENDED_STATE_VALUES);
        // End time
        endTimeEl = uifactory.addDropdownSingleselect("contact.tracing.end.time", questionnaireConfig, STATE_KEYS, STATE_VALUES);
        // Nick name
        UserPropertyHandler nickNameHandler = userPropertiesConfig.getPropertyHandler(UserConstants.NICKNAME);
        nickNameEl = uifactory.addDropdownSingleselect(nickNameHandler.i18nFormElementLabelKey(), questionnaireConfig, STATE_KEYS, STATE_VALUES);
        // First name
        UserPropertyHandler firstNameHandler = userPropertiesConfig.getPropertyHandler(UserConstants.FIRSTNAME);
        firstNameEl = uifactory.addDropdownSingleselect(firstNameHandler.i18nFormElementLabelKey(), questionnaireConfig, STATE_KEYS, STATE_VALUES);
        // Last name
        UserPropertyHandler lastNameHandler = userPropertiesConfig.getPropertyHandler(UserConstants.LASTNAME);
        lastNameEl = uifactory.addDropdownSingleselect(lastNameHandler.i18nFormElementLabelKey(), questionnaireConfig, STATE_KEYS, STATE_VALUES);
        // Street
        UserPropertyHandler streetHandler = userPropertiesConfig.getPropertyHandler(UserConstants.STREET);
        streetEL = uifactory.addDropdownSingleselect(streetHandler.i18nFormElementLabelKey(), questionnaireConfig, STATE_KEYS, STATE_VALUES);
        // Extra address line
        UserPropertyHandler extraLineHandler = userPropertiesConfig.getPropertyHandler(UserConstants.EXTENDEDADDRESS);
        extraAddressLineEl = uifactory.addDropdownSingleselect(extraLineHandler.i18nFormElementLabelKey(), questionnaireConfig, STATE_KEYS, STATE_VALUES);
        // Zip code
        UserPropertyHandler zipHandler = userPropertiesConfig.getPropertyHandler(UserConstants.ZIPCODE);
        zipCodeEl = uifactory.addDropdownSingleselect(zipHandler.i18nFormElementLabelKey(), questionnaireConfig, STATE_KEYS, STATE_VALUES);
        // City
        UserPropertyHandler cityHandler = userPropertiesConfig.getPropertyHandler(UserConstants.CITY);
        cityEl = uifactory.addDropdownSingleselect(cityHandler.i18nFormElementLabelKey(), questionnaireConfig, STATE_KEYS, STATE_VALUES);
        // Email
        UserPropertyHandler emailHandler = userPropertiesConfig.getPropertyHandler(UserConstants.EMAIL);
        emailEl = uifactory.addDropdownSingleselect(emailHandler.i18nFormElementLabelKey(), questionnaireConfig, STATE_KEYS, STATE_VALUES);
        // Institutional email
        UserPropertyHandler institutionalMailHandler = userPropertiesConfig.getPropertyHandler(UserConstants.INSTITUTIONALEMAIL);
        institutionalEmailEl = uifactory.addDropdownSingleselect(institutionalMailHandler.i18nFormElementLabelKey(), questionnaireConfig, STATE_KEYS, STATE_VALUES);
        // Generic email
        UserPropertyHandler genericMailHandler = userPropertiesConfig.getPropertyHandler("genericEmailProperty1");
        genericEmailEl = uifactory.addDropdownSingleselect(genericMailHandler.i18nFormElementLabelKey(), questionnaireConfig, STATE_KEYS, STATE_VALUES);
        // Private phone
        UserPropertyHandler privatePhoneHandler = userPropertiesConfig.getPropertyHandler(UserConstants.TELPRIVATE);
        privatePhoneEl = uifactory.addDropdownSingleselect(privatePhoneHandler.i18nFormElementLabelKey(), questionnaireConfig, STATE_KEYS, STATE_VALUES);
        // Mobile phone
        UserPropertyHandler mobilePhoneHandler = userPropertiesConfig.getPropertyHandler(UserConstants.TELMOBILE);
        mobilePhoneEl = uifactory.addDropdownSingleselect(mobilePhoneHandler.i18nFormElementLabelKey(), questionnaireConfig, STATE_KEYS, STATE_VALUES);
        // Office phone
        UserPropertyHandler officePhoneHandler = userPropertiesConfig.getPropertyHandler(UserConstants.TELOFFICE);
        officePhoneEl = uifactory.addDropdownSingleselect(officePhoneHandler.i18nFormElementLabelKey(), questionnaireConfig, STATE_KEYS, STATE_VALUES);

        // Save and cancel
        FormLayoutContainer questionnaireButtons = FormLayoutContainer.createButtonLayout("questionnaireButtons", getTranslator());
        questionnaireConfig.add(questionnaireButtons);
        uifactory.addFormCancelButton("questionnaire.config.cancel", questionnaireButtons, ureq, getWindowControl());
        uifactory.addFormSubmitButton("questionnaire.config.submit", questionnaireButtons);

        // Messages and texts
        messagesConfig = FormLayoutContainer.createDefaultFormLayout("messagesAndTexts", getTranslator());
        messagesConfig.setRootForm(mainForm);
        messagesConfig.setFormTitle(translate("contact.tracing.messages.title"));
        formLayout.add(messagesConfig);

        // Registration intro
        registrationIntroEl = uifactory.addStaticTextElement("contact.tracing.registration.intro", "contact.tracing.registration.intro.label", null, messagesConfig);
        registrationIntroEl.setHelpTextKey("contact.tracing.registration.intro.help", null);
        FormLayoutContainer registrationIntroButtons = FormLayoutContainer.createButtonLayout("registrationIntroButtons", getTranslator());
        messagesConfig.add(registrationIntroButtons);
        registrationIntroReset = uifactory.addFormLink("contact.tracing.registration.intro.reset", registrationIntroButtons, Link.BUTTON);
        registrationIntroCustomize = uifactory.addFormLink("contact.tracing.registration.intro.customize", registrationIntroButtons, Link.BUTTON);

        // Registration confirmation mail
        registrationConfirmationMailEl = uifactory.addStaticTextElement("contact.tracing.registration.confirmation.mail", "contact.tracing.registration.confirmation.mail.label", null, messagesConfig);
        FormLayoutContainer registrationConfirmationMailButtons = FormLayoutContainer.createButtonLayout("registrationConfirmationMailButtons", getTranslator());
        messagesConfig.add(registrationConfirmationMailButtons);
        registrationConfirmationMailReset = uifactory.addFormLink("contact.tracing.registration.confirmation.mail.reset", registrationConfirmationMailButtons, Link.BUTTON);
        registrationConfirmationMailCustomize = uifactory.addFormLink("contact.tracing.registration.confirmation.mail.customize", registrationConfirmationMailButtons, Link.BUTTON);

        // QR code instructions
        qrCodeInstructionsEl = uifactory.addStaticTextElement("contact.tracing.qr.code.instructions", "contact.tracing.qr.code.instructions.label", null, messagesConfig);
        FormLayoutContainer qrCodeButtons = FormLayoutContainer.createButtonLayout("qrCodeButtons", getTranslator());
        messagesConfig.add(qrCodeButtons);
        qrCodeInstructionsReset = uifactory.addFormLink("contact.tracing.qr.reset", qrCodeButtons, Link.BUTTON);
        qrCodeInstructionsCustomize = uifactory.addFormLink("contact.tracing.qr.customize", qrCodeButtons, Link.BUTTON);
    }

    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        if (source == enabledEl) {
            contactTracingModule.setEnabled(enabledEl.isSelected(0));
            initVelocityContainers();
            fireEvent(ureq, Event.CHANGED_EVENT);
        } else if (source == allowAnonymousRegistrationEl) {
            contactTracingModule.setAllowAnonymousRegistrationForRegisteredUsers(allowAnonymousRegistrationEl.isSelected(0));
            initVelocityContainers();
        } else if (source == registrationIntroCustomize) {
            singleKeyTranslatorController = new SingleKeyTranslatorController(ureq, getWindowControl(), CONTACT_TRACING_REGISTRATION_INTRO_KEY, ContactTracingAdminController.class, SingleKeyTranslatorController.InputType.RICH_TEXT_ELEMENT, null);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), singleKeyTranslatorController.getInitialComponent(), true, translate("contact.tracing.registration.intro.translate.title"), true, true);
            listenTo(singleKeyTranslatorController);
            listenTo(cmc);
            cmc.activate();
        } else if (source == registrationConfirmationMailCustomize) {
            singleKeyTranslatorController = new SingleKeyTranslatorController(ureq, getWindowControl(), CONTACT_TRACING_CONFIRMATION_MAIL_KEY, ContactTracingAdminController.class, SingleKeyTranslatorController.InputType.RICH_TEXT_ELEMENT, null);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), singleKeyTranslatorController.getInitialComponent(), true, translate("contact.tracing.registration.confirmation.mail.translate.title"), true, true);
            listenTo(singleKeyTranslatorController);
            listenTo(cmc);
            cmc.activate();
        } else if (source == qrCodeInstructionsCustomize) {
            String qrCodeInstructions = StringHelper.xssScan(contactTracingModule.getQrCodeInstructions());
            qrCodeInstructionsEditController = WysiwygFactory.createWysiwygControllerWithoutFile(ureq, getWindowControl(), null, qrCodeInstructions, null);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), qrCodeInstructionsEditController.getInitialComponent(), true, translate("contact.tracing.qr.code.instructions.edit.title"), true, true);
            listenTo(qrCodeInstructionsEditController);
            listenTo(cmc);
            cmc.activate();
        } else if (source == registrationIntroReset) {
            confirmResetIntroController = new ContactTracingConfirmResetController(ureq, getWindowControl(), translate("contact.tracing.registration.intro.reset.warning"), translate("contact.tracing.reset.confirmation"));
            cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmResetIntroController.getInitialComponent(), true, translate("contact.tracing.registration.intro.reset.title"), true, true);
            listenTo(confirmResetIntroController);
            listenTo(cmc);
            cmc.activate();
        } else if (source == registrationConfirmationMailReset) {
            confirmResetMailController = new ContactTracingConfirmResetController(ureq, getWindowControl(), translate("contact.tracing.registration.confirmation.mail.reset.warning"), translate("contact.tracing.reset.confirmation"));
            cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmResetMailController.getInitialComponent(), true, translate("contact.tracing.registration.confirmation.mail.reset.title"), true, true);
            listenTo(confirmResetMailController);
            listenTo(cmc);
            cmc.activate();
        } else if (source == qrCodeInstructionsReset) {
            confirmResetQRCodeInstructionsController = new ContactTracingConfirmResetController(ureq, getWindowControl(), translate("contact.tracing.qr.reset.warning"), translate("contact.tracing.reset.confirmation"));
            cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmResetQRCodeInstructionsController.getInitialComponent(), true, translate("contact.tracing.qr.reset.title"), true, true);
            listenTo(confirmResetQRCodeInstructionsController);
            listenTo(cmc);
            cmc.activate();
        }

        super.formInnerEvent(ureq, source, event);
    }

    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == singleKeyTranslatorController) {
            if (event == FormEvent.DONE_EVENT) {
                loadData();
            }

            cleanUp();
        } else if (source == qrCodeInstructionsEditController) {
            if (event == FormEvent.DONE_EVENT) {
                contactTracingModule.setQrCodeInstructions(qrCodeInstructionsEditController.getHTMLContent());
                loadData();
            }

            cleanUp();
        } else if (source == confirmResetIntroController) {
            if (event == FormEvent.DONE_EVENT) {
                resetI18nKeys(CONTACT_TRACING_REGISTRATION_INTRO_KEY);
                loadData();
            }

            cleanUp();
        } else if (source == confirmResetMailController) {
            if (event == FormEvent.DONE_EVENT) {
                resetI18nKeys(CONTACT_TRACING_CONFIRMATION_MAIL_KEY);
                loadData();
            }

            cleanUp();
        } else if (source == confirmResetQRCodeInstructionsController) {
            if (event == FormEvent.DONE_EVENT) {
                contactTracingModule.setQrCodeInstructions(null);
                loadData();
            }

            cleanUp();
        } else if (source == cmc) {
            cleanUp();
        }
    }

    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        boolean allOk = super.validateFormLogic(ureq);

        allOk &= validatePeriod(ureq, retentionPeriodEl, 1, 365, translate("days"));
        allOk &= validatePeriod(ureq, defaultDurationEl, 1, 300, translate("minutes"));

        return allOk;
    }

    @Override
    protected void formOK(UserRequest ureq) {
        // Check if values are different than those already saved to prevent spammed log files
        if (contactTracingModule.getRetentionPeriod() != Integer.parseInt(retentionPeriodEl.getValue())) {
            contactTracingModule.setRetentionPeriod(Integer.parseInt(retentionPeriodEl.getValue()));
        }
        if (contactTracingModule.getDefaultDuration() != Integer.parseInt(defaultDurationEl.getValue())) {
            contactTracingModule.setDefaultDuration(Integer.parseInt(defaultDurationEl.getValue()));
        }
        if (contactTracingModule.getAttendanceStartTimeState() != getState(startTimeEL)) {
            contactTracingModule.setAttendanceStartTimeState(getState(startTimeEL));
        }
        if (contactTracingModule.getAttendanceEndTimeState() != getState(endTimeEl)) {
            contactTracingModule.setAttendanceEndTimeState(getState(endTimeEl));
        }
        if (contactTracingModule.getNickNameState() != getState(nickNameEl)) {
            contactTracingModule.setNickNameState(getState(nickNameEl));
        }
        if (contactTracingModule.getFirstNameState() != getState(firstNameEl)) {
            contactTracingModule.setFirstNameState(getState(firstNameEl));
        }
        if (contactTracingModule.getLastNameState() != getState(lastNameEl)) {
            contactTracingModule.setLastNameState(getState(lastNameEl));
        }
        if (contactTracingModule.getStreetState() != getState(streetEL)) {
            contactTracingModule.setStreetState(getState(streetEL));
        }
        if (contactTracingModule.getExtraAddressLineState() != getState(extraAddressLineEl)) {
            contactTracingModule.setExtraAddressLineState(getState(extraAddressLineEl));
        }
        if (contactTracingModule.getCityState() != getState(cityEl)) {
            contactTracingModule.setCityState(getState(cityEl));
        }
        if (contactTracingModule.getZipCodeState() != getState(zipCodeEl)) {
            contactTracingModule.setZipCodeState(getState(zipCodeEl));
        }
        if (contactTracingModule.getEmailState() != getState(emailEl)) {
            contactTracingModule.setEmailState(getState(emailEl));
        }
        if (contactTracingModule.getInstitutionalEMailState() != getState(institutionalEmailEl)) {
            contactTracingModule.setInstitutionalEMailState(getState(institutionalEmailEl));
        }
        if (contactTracingModule.getGenericEmailState() != getState(genericEmailEl)) {
            contactTracingModule.setGenericEmailState(getState(genericEmailEl));
        }
        if (contactTracingModule.getPrivatePhoneState() != getState(privatePhoneEl)) {
            contactTracingModule.setPrivatePhoneState(getState(privatePhoneEl));
        }
        if (contactTracingModule.getMobilePhoneState() != getState(mobilePhoneEl)) {
            contactTracingModule.setMobilePhoneState(getState(mobilePhoneEl));
        }
        if (contactTracingModule.getOfficePhoneState() != getState(officePhoneEl)) {
            contactTracingModule.setOfficePhoneState(getState(officePhoneEl));
        }

        loadData();
    }

    private void cleanUp() {
        if (cmc != null && cmc.isCloseable()) {
            cmc.deactivate();
        }

        removeAsListenerAndDispose(cmc);
        removeAsListenerAndDispose(singleKeyTranslatorController);
        removeAsListenerAndDispose(qrCodeInstructionsEditController);
        removeAsListenerAndDispose(confirmResetIntroController);
        removeAsListenerAndDispose(confirmResetMailController);
        removeAsListenerAndDispose(confirmResetQRCodeInstructionsController);

        cmc = null;
        singleKeyTranslatorController = null;
        qrCodeInstructionsEditController = null;
        confirmResetIntroController = null;
        confirmResetMailController = null;
        confirmResetQRCodeInstructionsController = null;
    }

    private void loadData() {
        enabledEl.select(ENABLED_KEYS[0], contactTracingModule.isEnabled());
        retentionPeriodEl.setValue(String.valueOf(contactTracingModule.getRetentionPeriod()));
        defaultDurationEl.setValue(String.valueOf(contactTracingModule.getDefaultDuration()));
        allowAnonymousRegistrationEl.select(ENABLED_KEYS[0], contactTracingModule.isAnonymousRegistrationForRegisteredUsersAllowed());

        startTimeEL.select(contactTracingModule.getAttendanceStartTimeState().name(), true);
        endTimeEl.select(contactTracingModule.getAttendanceEndTimeState().name(), true);
        nickNameEl.select(contactTracingModule.getNickNameState().name(), true);
        firstNameEl.select(contactTracingModule.getFirstNameState().name(), true);
        lastNameEl.select(contactTracingModule.getLastNameState().name(), true);
        streetEL.select(contactTracingModule.getStreetState().name(), true);
        extraAddressLineEl.select(contactTracingModule.getExtraAddressLineState().name(), true);
        cityEl.select(contactTracingModule.getCityState().name(), true);
        zipCodeEl.select(contactTracingModule.getZipCodeState().name(), true);
        emailEl.select(contactTracingModule.getEmailState().name(), true);
        institutionalEmailEl.select(contactTracingModule.getInstitutionalEMailState().name(), true);
        genericEmailEl.select(contactTracingModule.getGenericEmailState().name(), true);
        privatePhoneEl.select(contactTracingModule.getPrivatePhoneState().name(), true);
        mobilePhoneEl.select(contactTracingModule.getMobilePhoneState().name(), true);
        officePhoneEl.select(contactTracingModule.getOfficePhoneState().name(), true);

        registrationIntroEl.setValue(StringHelper.truncateText(StringHelper.xssScan(translate(CONTACT_TRACING_REGISTRATION_INTRO_KEY, new String[]{String.valueOf(contactTracingModule.getRetentionPeriod())}))));
        registrationConfirmationMailEl.setValue(StringHelper.truncateText(StringHelper.xssScan(translate(CONTACT_TRACING_CONFIRMATION_MAIL_KEY, new String[]{String.valueOf(contactTracingModule.getRetentionPeriod())}))));
        qrCodeInstructionsEl.setValue(StringHelper.truncateText(StringHelper.xssScan(contactTracingModule.getQrCodeInstructions())));
    }

    private void initNumericTextElement(TextElement textEl, String unit) {
        textEl.setDisplaySize(3);
        textEl.setMaxLength(3);
        textEl.setElementCssClass("form-inline");
        textEl.setTextAddOn(unit);
    }

    private void initVelocityContainers() {
        if (contactTracingModule.isEnabled()) {
            retentionPeriodEl.setVisible(true);
            allowAnonymousRegistrationEl.setVisible(true);
            defaultDurationEl.setVisible(true);
            questionnaireConfig.setVisible(true);
            messagesConfig.setVisible(true);
        } else {
            retentionPeriodEl.setVisible(false);
            allowAnonymousRegistrationEl.setVisible(false);
            defaultDurationEl.setVisible(false);
            questionnaireConfig.setVisible(false);
            messagesConfig.setVisible(false);
        }
    }

    private AttributeState getState(SingleSelection selectionElement) {
        try {
            return AttributeState.valueOf(selectionElement.getSelectedKey());
        } catch (Exception e) {
            return AttributeState.mandatory;
        }
    }

    private boolean validatePeriod(UserRequest ureq, TextElement el, int lowest, int highest, String unit) {
        el.clearError();
        boolean allOk = validateFormItem(ureq, el);
        if(el.isEnabled() && el.isVisible()) {
            String val = el.getValue();
            if (StringHelper.containsNonWhitespace(val)) {
                try {
                    int number = Integer.parseInt(val);
                    if (number < lowest || number > highest) {
                        el.setErrorKey("contact.tracing.form.error.wrong.period", String.valueOf(lowest), String.valueOf(highest), unit);
                        allOk = false;
                    }
                } catch (NumberFormatException e) {
                    el.setErrorKey("contact.tracing.form.error.wrong.period", String.valueOf(lowest), String.valueOf(highest), unit);
                    allOk = false;
                }
            } else if (el.isMandatory()) {
                el.setErrorKey("form.mandatory.hover");
                allOk = false;
            }
        }
        return allOk;
    }

    private void resetI18nKeys(String i18nKey) {
        // save new values
        Map<Locale, Locale> allOverlays = i18nModule.getOverlayLocales();
        for (String locale : i18nModule.getEnabledLanguageKeys()) {
            I18nItem item = i18nManager.getI18nItem(ContactTracingAdminController.class.getPackage().getName(), i18nKey, allOverlays.get(Locale.forLanguageTag(locale)));
            i18nManager.saveOrUpdateI18nItem(item, null);
        }
    }
}
