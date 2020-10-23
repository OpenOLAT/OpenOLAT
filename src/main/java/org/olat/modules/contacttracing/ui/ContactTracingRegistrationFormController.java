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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.TranslatorHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingManager;
import org.olat.modules.contacttracing.ContactTracingModule;
import org.olat.modules.contacttracing.ContactTracingModule.AttributeState;
import org.olat.modules.contacttracing.ContactTracingRegistration;
import org.olat.user.ProfileFormController;
import org.olat.user.UserManager;
import org.olat.user.UserPropertiesConfig;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 15.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingRegistrationFormController extends FormBasicController {

    private static final Logger log = Tracing.createLoggerFor(ContactTracingRegistrationFormController.class);
    private static final String usageIdentifier = ProfileFormController.class.getCanonicalName();
    private static final String[] ON_KEYS = new String[] {"on"};

    private final User user;
    private final ContactTracingLocation location;
    private final boolean anonymous;

    private ContactTracingRegistration registration;

    private DateChooser startDateEl;
    private DateChooser endDateEl;

    private FormItem nickNameEl;
    private FormItem firstNameEl;
    private FormItem lastNameEl;

    private FormItem streetEl;
    private FormItem extraLineEl;
    private FormItem zipCodeEl;
    private FormItem cityEl;

    private FormItem emailEl;
    private FormItem institutionalEmailEl;
    private FormItem genericEmailEl;
    private FormItem privatePhoneEl;
    private FormItem officePhoneEl;
    private FormItem mobilePhoneEl;

    private MultipleSelectionElement saveUserPropertiesToProfileEl;

    private Map<UserPropertyHandler, FormItem> userPropertyHandlerFormItemMap;
    private Map<UserPropertyHandler, Boolean> updateablePropertyMap;

    @Autowired
    private ContactTracingManager contactTracingManager;
    @Autowired
    private ContactTracingModule contactTracingModule;
    @Autowired
    private UserPropertiesConfig userPropertiesConfig;
    @Autowired
    private MailManager mailManager;
    @Autowired
    private UserManager userManager;
    @Autowired
    private BaseSecurity baseSecurity;

    public ContactTracingRegistrationFormController(UserRequest ureq, WindowControl wControl, ContactTracingLocation location) {
        this(ureq, wControl, location, false);
    }

    public ContactTracingRegistrationFormController(UserRequest ureq, WindowControl wControl, ContactTracingLocation location, boolean anonymous) {
        super(ureq, wControl, LAYOUT_VERTICAL);
        setTranslator(userPropertiesConfig.getTranslator(getTranslator()));
        setTranslator(Util.createPackageTranslator(UserPropertyHandler.class, getLocale(), getTranslator()));

        UserSession usess = ureq.getUserSession();
        this.user = usess.isAuthenticated() && !usess.getRoles().isGuestOnly() ? baseSecurity.loadIdentityByKey(getIdentity().getKey()).getUser() : null;
        this.location = location;
        this.anonymous = anonymous && contactTracingModule.isAnonymousRegistrationForRegisteredUsersAllowed();

        initForm(ureq);
        loadData();
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        // List containing all form items
        userPropertyHandlerFormItemMap = new HashMap<>();
        updateablePropertyMap = new HashMap<>();

        // Time recording
        FormLayoutContainer timeRecording = FormLayoutContainer.createDefaultFormLayout("timeRecording", getTranslator());
        timeRecording.setRootForm(mainForm);
        timeRecording.setFormTitle(translate("contact.tracing"));
        timeRecording.setFormDescription(translate("contact.tracing.registration.intro", new String[]{
                String.valueOf(contactTracingModule.getRetentionPeriod()),
                ContactTracingHelper.getLocationsDetails(getTranslator(), location)}));
        formLayout.add(timeRecording);

        // Start and end time
        startDateEl = uifactory.addDateChooser("contact.tracing.start.time", null, timeRecording);
        startDateEl.setDateChooserTimeEnabled(true);
        startDateEl.setUserObject(contactTracingModule.getAttendanceStartTimeState());

        endDateEl = uifactory.addDateChooser("contact.tracing.end.time", null, timeRecording);
        endDateEl.setDateChooserTimeEnabled(true);
        endDateEl.setUserObject(contactTracingModule.getAttendanceEndTimeState());

        // User identification
        FormLayoutContainer userIdentification = FormLayoutContainer.createDefaultFormLayout("userIdentification", getTranslator());
        userIdentification.setRootForm(mainForm);
        userIdentification.setFormTitle(translate("contact.tracing.registration.user.identification"));
        formLayout.add(userIdentification);

        UserPropertyHandler nickNameHandler = userPropertiesConfig.getPropertyHandler(UserConstants.NICKNAME);
        nickNameEl = nickNameHandler.addFormItem(getLocale(), user, usageIdentifier, false, userIdentification);
        nickNameEl.setUserObject(contactTracingModule.getNickNameState());
        userPropertyHandlerFormItemMap.put(nickNameHandler, nickNameEl);
        updateablePropertyMap.put(nickNameHandler, nickNameEl.isEnabled());

        UserPropertyHandler firstNameHandler = userPropertiesConfig.getPropertyHandler(UserConstants.FIRSTNAME);
        firstNameEl = firstNameHandler.addFormItem(getLocale(), user, usageIdentifier, false, userIdentification);
        firstNameEl.setUserObject(contactTracingModule.getFirstNameState());
        userPropertyHandlerFormItemMap.put(firstNameHandler, firstNameEl);
        updateablePropertyMap.put(firstNameHandler, firstNameEl.isEnabled());

        UserPropertyHandler lastNameHandler = userPropertiesConfig.getPropertyHandler(UserConstants.LASTNAME);
        lastNameEl = lastNameHandler.addFormItem(getLocale(), user, usageIdentifier, false, userIdentification);
        lastNameEl.setUserObject(contactTracingModule.getLastNameState());
        userPropertyHandlerFormItemMap.put(lastNameHandler, lastNameEl);
        updateablePropertyMap.put(lastNameHandler, lastNameEl.isEnabled());

        UserPropertyHandler streetHandler = userPropertiesConfig.getPropertyHandler(UserConstants.STREET);
        streetEl = streetHandler.addFormItem(getLocale(), user, usageIdentifier, false, userIdentification);
        streetEl.setUserObject(contactTracingModule.getStreetState());
        userPropertyHandlerFormItemMap.put(streetHandler, streetEl);
        updateablePropertyMap.put(streetHandler, streetEl.isEnabled());

        UserPropertyHandler extraLineHandler = userPropertiesConfig.getPropertyHandler(UserConstants.EXTENDEDADDRESS);
        extraLineEl = extraLineHandler.addFormItem(getLocale(), user, usageIdentifier, false, userIdentification);
        extraLineEl.setUserObject(contactTracingModule.getExtraAddressLineState());
        userPropertyHandlerFormItemMap.put(extraLineHandler, extraLineEl);
        updateablePropertyMap.put(extraLineHandler, extraLineEl.isEnabled());

        UserPropertyHandler zipCodeHandler = userPropertiesConfig.getPropertyHandler(UserConstants.ZIPCODE);
        zipCodeEl = zipCodeHandler.addFormItem(getLocale(), user, usageIdentifier, false, userIdentification);
        zipCodeEl.setUserObject(contactTracingModule.getZipCodeState());
        userPropertyHandlerFormItemMap.put(zipCodeHandler, zipCodeEl);
        updateablePropertyMap.put(zipCodeHandler, zipCodeEl.isEnabled());

        UserPropertyHandler cityHandler = userPropertiesConfig.getPropertyHandler(UserConstants.CITY);
        cityEl = cityHandler.addFormItem(getLocale(), user, usageIdentifier, false, userIdentification);
        cityEl.setUserObject(contactTracingModule.getCityState());
        userPropertyHandlerFormItemMap.put(cityHandler, cityEl);
        updateablePropertyMap.put(cityHandler, cityEl.isEnabled());

        // Contact information
        FormLayoutContainer contactInformation = FormLayoutContainer.createDefaultFormLayout("contactInformation", getTranslator());
        contactInformation.setRootForm(mainForm);
        contactInformation.setFormTitle(translate("contact.tracing.registration.contact.information"));
        formLayout.add(contactInformation);

        UserPropertyHandler emailHandler = userPropertiesConfig.getPropertyHandler(UserConstants.EMAIL);
        emailEl = emailHandler.addFormItem(getLocale(), user, usageIdentifier, false, contactInformation);
        emailEl.setUserObject(contactTracingModule.getEmailState());
        userPropertyHandlerFormItemMap.put(emailHandler, emailEl);
        updateablePropertyMap.put(emailHandler, emailEl.isEnabled());

        UserPropertyHandler institutionalEmailHandler = userPropertiesConfig.getPropertyHandler(UserConstants.INSTITUTIONALEMAIL);
        institutionalEmailEl = institutionalEmailHandler.addFormItem(getLocale(), user, usageIdentifier, false, contactInformation);
        institutionalEmailEl.setUserObject(contactTracingModule.getInstitutionalEMailState());
        userPropertyHandlerFormItemMap.put(institutionalEmailHandler, institutionalEmailEl);
        updateablePropertyMap.put(institutionalEmailHandler, institutionalEmailEl.isEnabled());

        UserPropertyHandler genericEmailHandler = userPropertiesConfig.getPropertyHandler("genericEmailProperty1");
        genericEmailEl = genericEmailHandler.addFormItem(getLocale(), user, usageIdentifier, false, contactInformation);
        genericEmailEl.setUserObject(contactTracingModule.getGenericEmailState());
        userPropertyHandlerFormItemMap.put(genericEmailHandler, genericEmailEl);
        updateablePropertyMap.put(genericEmailHandler, genericEmailEl.isEnabled());

        UserPropertyHandler mobilePhoneHandler = userPropertiesConfig.getPropertyHandler(UserConstants.TELMOBILE);
        mobilePhoneEl = mobilePhoneHandler.addFormItem(getLocale(), user, usageIdentifier, false, contactInformation);
        mobilePhoneEl.setUserObject(contactTracingModule.getMobilePhoneState());
        userPropertyHandlerFormItemMap.put(mobilePhoneHandler, mobilePhoneEl);
        updateablePropertyMap.put(mobilePhoneHandler, mobilePhoneEl.isEnabled());

        UserPropertyHandler privatePhoneHandler = userPropertiesConfig.getPropertyHandler(UserConstants.TELPRIVATE);
        privatePhoneEl = privatePhoneHandler.addFormItem(getLocale(), user, usageIdentifier, false, contactInformation);
        privatePhoneEl.setUserObject(contactTracingModule.getPrivatePhoneState());
        userPropertyHandlerFormItemMap.put(privatePhoneHandler, privatePhoneEl);
        updateablePropertyMap.put(privatePhoneHandler, privatePhoneEl.isEnabled());

        UserPropertyHandler officePhoneHandler = userPropertiesConfig.getPropertyHandler(UserConstants.TELOFFICE);
        officePhoneEl = officePhoneHandler.addFormItem(getLocale(), user, usageIdentifier, false, contactInformation);
        officePhoneEl.setUserObject(contactTracingModule.getOfficePhoneState());
        userPropertyHandlerFormItemMap.put(officePhoneHandler, officePhoneEl);
        updateablePropertyMap.put(officePhoneHandler, officePhoneEl.isEnabled());

        saveUserPropertiesToProfileEl = uifactory.addCheckboxesHorizontal("contact.tracing.registration.save.to.profile.label", contactInformation, ON_KEYS, TranslatorHelper.translateAll(getTranslator(), new String[] {"contact.tracing.registration.save.to.profile"}));

        // Form buttons
        FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
        buttonLayout.setRootForm(mainForm);
        contactInformation.add(buttonLayout);

        uifactory.addFormCancelButton("contact.tracing.registration.cancel", buttonLayout, ureq, getWindowControl());
        uifactory.addFormSubmitButton("contact.tracing.registration.submit", buttonLayout);
    }

    private void loadData() {
        Date startDate = new Date();

        startDateEl.setDate(startDate);
        checkEnabled(startDateEl);
        checkMandatory(startDateEl);
        checkVisible(startDateEl);

        endDateEl.setDate(DateUtils.addMinutes(startDate, contactTracingModule.getDefaultDuration()));
        checkEnabled(endDateEl);
        checkMandatory(endDateEl);
        checkVisible(endDateEl);

        for (FormItem formItem : userPropertyHandlerFormItemMap.values()) {
            checkEnabled(formItem);
            checkVisible(formItem);
            checkMandatory(formItem);
            checkAnonymous(formItem);
        }

        saveUserPropertiesToProfileEl.setVisible(user != null && !anonymous);
    }

    private void checkVisible(FormItem formItem) {
        if (formItem.getUserObject() instanceof AttributeState) {
            AttributeState state = (AttributeState) formItem.getUserObject();
            formItem.setVisible(state != AttributeState.disabled);
        }

        if (formItem == nickNameEl && user == null) {
            formItem.setVisible(false);
        }
    }

    private void checkEnabled(FormItem formItem) {
        if (formItem.getUserObject() instanceof AttributeState) {
            AttributeState state = (AttributeState) formItem.getUserObject();
            // Form items are created with the userPropertyHandler
            // If a form is already disabled, it should stay disabled
            formItem.setEnabled(formItem.isEnabled() && state != AttributeState.disabled && state != AttributeState.automatic);
        }

        // Disable email element if prefilled
        if (formItem == emailEl) {
            if (StringHelper.containsNonWhitespace(((TextElement) emailEl).getValue())) {
                formItem.setEnabled(true);
                return;
            } else {
                formItem.setEnabled(true);
                return;
            }
        }

        // Enable all text elements which are empty
        if (formItem instanceof TextElement) {
            if (!StringHelper.containsNonWhitespace(((TextElement) formItem).getValue()) || user == null) {
                formItem.setEnabled(true);
            }
        }
    }

    private void checkMandatory(FormItem formItem) {
        if (formItem.getUserObject() instanceof AttributeState) {
            AttributeState state = (AttributeState) formItem.getUserObject();
            formItem.setMandatory(state == AttributeState.mandatory);

            if (formItem.isMandatory()) {
                if (formItem instanceof TextElement) {
                    ((TextElement) formItem).setNotEmptyCheck("contact.tracing.required");
                } else if (formItem instanceof DateChooser) {
                    ((DateChooser) formItem).setNotEmptyCheck("contact.tracing.required");
                }
            }
        }
    }

    private void checkAnonymous(FormItem formItem) {
        if (anonymous) {
            if (formItem == nickNameEl) {
                formItem.setVisible(false);
            }

            if (formItem instanceof TextElement) {
                formItem.setEnabled(true);
                ((TextElement) formItem).setValue(null);
            }
        }
    }

    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        boolean allOk = super.validateFormLogic(ureq);

        // Check every form item
        for (FormItem item : userPropertyHandlerFormItemMap.values()) {
            allOk &= validateFormItem(item);
        }

        // Validate date choosers
        Date startDate = startDateEl.getDate();
        Date endDate = endDateEl.getDate();
        Date currentDate = new Date();

        if (startDate != null) {
            if (startDate.after(currentDate)) {
                allOk = false;
                startDateEl.setErrorKey("contact.tracing.registration.date.start.error", null);
            } else {
                startDateEl.clearError();
            }
        } else if (startDateEl.isMandatory()){
            startDateEl.setErrorKey("contact.tracing.required", null);
        }

        if (endDate != null) {
            if (startDate != null && endDate.before(startDate)) {
                allOk = false;
                endDateEl.setErrorKey("contact.tracing.registration.date.end.before.start.error", null);
            } else if (endDate.before(currentDate)) {
                allOk = false;
                endDateEl.setErrorKey("contact.tracing.registration.date.end.error", null);
            } else {
                endDateEl.clearError();
            }
        } else if (endDateEl.isMandatory()){
            endDateEl.setErrorKey("contact.tracing.required", null);
        }

        return allOk;
    }

    @Override
    protected boolean validateFormItem(FormItem item) {
        if (item.getUserObject() instanceof AttributeState) {
            AttributeState state = (AttributeState) item.getUserObject();
            if (state != AttributeState.mandatory) {
                item.clearError();
            }
        }

        return super.validateFormItem(item);
    }

    @Override
    protected void formOK(UserRequest ureq) {
        // Save user information if selected
        if (user != null && saveUserPropertiesToProfileEl.isSelected(0 )) {
            Identity updateIdentity = baseSecurity.loadIdentityByKey(getIdentity().getKey());

            userPropertyHandlerFormItemMap.forEach((userPropertyHandler, formItem) -> {
                // If user is allowed to update this property, save content to user profile
                // This check is necessary in case of disabled user properties combined with mandatory fields in the contact tracing form
                if (updateablePropertyMap.get(userPropertyHandler)) {
                    userPropertyHandler.updateUserFromFormItem(updateIdentity.getUser(), formItem);
                }
            });
            userManager.updateUserFromIdentity(updateIdentity);
        }

        // Create new entry
        Date deletionDate = DateUtils.addDays(new Date(), contactTracingModule.getRetentionPeriod());
        ContactTracingRegistration registration = contactTracingManager.createRegistration(location, startDateEl.getDate(), deletionDate);

        // Set information
        registration.setEndDate(endDateEl.getDate());
        registration.setNickName(getValue(nickNameEl));
        registration.setFirstName(getValue(firstNameEl));
        registration.setLastName(getValue(lastNameEl));
        registration.setStreet(getValue(streetEl));
        registration.setExtraAddressLine(getValue(extraLineEl));
        registration.setZipCode(getValue(zipCodeEl));
        registration.setCity(getValue(cityEl));
        registration.setEmail(getValue(emailEl));
        registration.setInstitutionalEmail(getValue(institutionalEmailEl));
        registration.setGenericEmail(getValue(genericEmailEl));
        registration.setPrivatePhone(getValue(privatePhoneEl));
        registration.setMobilePhone(getValue(mobilePhoneEl));
        registration.setOfficePhone(getValue(officePhoneEl));

        // Update entry
        this.registration = contactTracingManager.persistRegistration(registration);

        // Send mail
        sendMail();

        fireEvent(ureq, Event.DONE_EVENT);
    }

    private void sendMail() {
        String subject = ContactTracingHelper.getMailSubject(getTranslator(), location);
        String body = ContactTracingHelper.getMailBody(getTranslator(), contactTracingModule.getRetentionPeriod(), location, firstNameEl, lastNameEl);
        String decoratedBody = mailManager.decorateMailBody(body, getLocale());
        String recipientAddress = ContactTracingHelper.getMailAddress(emailEl, institutionalEmailEl, genericEmailEl);
        Address from;
        Address[] to;

        try {
            from = new InternetAddress(WebappHelper.getMailConfig("mailSupport"));
            to = new Address[] {new InternetAddress(((recipientAddress)))};
        } catch (AddressException e) {
            log.error("Could not send registration notification message, bad mail address", e);
            return;
        }

        MailerResult result = new MailerResult();
        MimeMessage msg = mailManager.createMimeMessage(from, to, null, null, subject, decoratedBody, null, result);
        mailManager.sendMessage(msg, result);
        if (!result.isSuccessful()) {
            log.error("Could not send registration notification message: Location[" + location.getKey() + "], Recipient[" + recipientAddress + "]");
        }
    }

    public ContactTracingRegistration getRegistration() {
        return registration;
    }

    @Override
    protected void formCancelled(UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    private String getValue(FormItem formItem) {
        if (formItem instanceof TextElement) {
            return ((TextElement) formItem).getValue();
        }

        return null;
    }

    @Override
    protected void doDispose() {

    }
}
