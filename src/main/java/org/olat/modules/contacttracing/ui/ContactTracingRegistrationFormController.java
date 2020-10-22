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
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingManager;
import org.olat.modules.contacttracing.ContactTracingModule;
import org.olat.modules.contacttracing.ContactTracingModule.AttributeState;
import org.olat.modules.contacttracing.ContactTracingRegistration;
import org.olat.user.ProfileFormController;
import org.olat.user.UserPropertiesConfig;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 15.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingRegistrationFormController extends FormBasicController {

    private static final String usageIdentifier = ProfileFormController.class.getCanonicalName();
    private static final String[] ON_KEYS = new String[] {"on"};

    private final User user;
    private final ContactTracingLocation location;

    private DateChooser startTimeEl;
    private DateChooser endTimeEl;

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

    @Autowired
    private ContactTracingManager contactTracingManager;
    @Autowired
    private ContactTracingModule contactTracingModule;
    @Autowired
    private UserPropertiesConfig userPropertiesConfig;

    public ContactTracingRegistrationFormController(UserRequest ureq, WindowControl wControl, ContactTracingLocation location) {
        super(ureq, wControl, LAYOUT_VERTICAL);
        setTranslator(userPropertiesConfig.getTranslator(getTranslator()));
        setTranslator(Util.createPackageTranslator(UserPropertyHandler.class, getLocale(), getTranslator()));

        this.location = location;
        this.user = ureq.getUserSession().isAuthenticated() && !ureq.getUserSession().getRoles().isGuestOnly() ? getIdentity().getUser() : null;

        initForm(ureq);
        loadData();
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        // List containing all form items
        userPropertyHandlerFormItemMap = new HashMap<>();

        // Time recording
        FormLayoutContainer timeRecording = FormLayoutContainer.createDefaultFormLayout("timeRecording", getTranslator());
        timeRecording.setRootForm(mainForm);
        timeRecording.setFormTitle(translate("contact.tracing"));
        timeRecording.setFormDescription(translate("contact.tracing.registration.intro", new String[]{String.valueOf(contactTracingModule.getRetentionPeriod())}));
        formLayout.add(timeRecording);

        // Start and end time
        startTimeEl = uifactory.addDateChooser("contact.tracing.start.time", null, timeRecording);
        startTimeEl.setDateChooserTimeEnabled(true);
        startTimeEl.setUserObject(contactTracingModule.getAttendanceStartTimeState());

        endTimeEl = uifactory.addDateChooser("contact.tracing.end.time", null, timeRecording);
        endTimeEl.setDateChooserTimeEnabled(true);
        endTimeEl.setUserObject(contactTracingModule.getAttendanceEndTimeState());

        // User identification
        FormLayoutContainer userIdentification = FormLayoutContainer.createDefaultFormLayout("userIdentification", getTranslator());
        userIdentification.setRootForm(mainForm);
        userIdentification.setFormTitle(translate("contact.tracing.registration.user.identification"));
        formLayout.add(userIdentification);

        UserPropertyHandler nickNameHandler = userPropertiesConfig.getPropertyHandler(UserConstants.NICKNAME);
        nickNameEl = nickNameHandler.addFormItem(getLocale(), user, usageIdentifier, false, userIdentification);
        nickNameEl.setUserObject(contactTracingModule.getNickNameState());
        userPropertyHandlerFormItemMap.put(nickNameHandler, nickNameEl);

        UserPropertyHandler firstNameHandler = userPropertiesConfig.getPropertyHandler(UserConstants.FIRSTNAME);
        firstNameEl = firstNameHandler.addFormItem(getLocale(), user, usageIdentifier, false, userIdentification);
        firstNameEl.setUserObject(contactTracingModule.getFirstNameState());
        userPropertyHandlerFormItemMap.put(firstNameHandler, firstNameEl);

        UserPropertyHandler lastNameHandler = userPropertiesConfig.getPropertyHandler(UserConstants.LASTNAME);
        lastNameEl = lastNameHandler.addFormItem(getLocale(), user, usageIdentifier, false, userIdentification);
        lastNameEl.setUserObject(contactTracingModule.getLastNameState());
        userPropertyHandlerFormItemMap.put(lastNameHandler, lastNameEl);

        UserPropertyHandler streetHandler = userPropertiesConfig.getPropertyHandler(UserConstants.STREET);
        streetEl = streetHandler.addFormItem(getLocale(), user, usageIdentifier, false, userIdentification);
        streetEl.setUserObject(contactTracingModule.getStreetState());
        userPropertyHandlerFormItemMap.put(streetHandler, streetEl);

        UserPropertyHandler extraLineHandler = userPropertiesConfig.getPropertyHandler(UserConstants.EXTENDEDADDRESS);
        extraLineEl = extraLineHandler.addFormItem(getLocale(), user, usageIdentifier, false, userIdentification);
        extraLineEl.setUserObject(contactTracingModule.getExtraAddressLineState());
        userPropertyHandlerFormItemMap.put(extraLineHandler, extraLineEl);

        UserPropertyHandler zipCodeHandler = userPropertiesConfig.getPropertyHandler(UserConstants.ZIPCODE);
        zipCodeEl = zipCodeHandler.addFormItem(getLocale(), user, usageIdentifier, false, userIdentification);
        zipCodeEl.setUserObject(contactTracingModule.getZipCodeState());
        userPropertyHandlerFormItemMap.put(zipCodeHandler, zipCodeEl);

        UserPropertyHandler cityHandler = userPropertiesConfig.getPropertyHandler(UserConstants.CITY);
        cityEl = cityHandler.addFormItem(getLocale(), user, usageIdentifier, false, userIdentification);
        cityEl.setUserObject(contactTracingModule.getCityState());
        userPropertyHandlerFormItemMap.put(cityHandler, cityEl);

        // Contact information
        FormLayoutContainer contactInformation = FormLayoutContainer.createDefaultFormLayout("contactInformation", getTranslator());
        contactInformation.setRootForm(mainForm);
        contactInformation.setFormTitle(translate("contact.tracing.registration.contact.information"));
        formLayout.add(contactInformation);

        UserPropertyHandler emailHandler = userPropertiesConfig.getPropertyHandler(UserConstants.EMAIL);
        emailEl = emailHandler.addFormItem(getLocale(), user, usageIdentifier, false, contactInformation);
        emailEl.setUserObject(contactTracingModule.getEmailState());
        userPropertyHandlerFormItemMap.put(emailHandler, emailEl);

        UserPropertyHandler institutionalEmailHandler = userPropertiesConfig.getPropertyHandler(UserConstants.INSTITUTIONALEMAIL);
        institutionalEmailEl = institutionalEmailHandler.addFormItem(getLocale(), user, usageIdentifier, false, contactInformation);
        institutionalEmailEl.setUserObject(contactTracingModule.getInstitutionalEMailState());
        userPropertyHandlerFormItemMap.put(institutionalEmailHandler, institutionalEmailEl);

        UserPropertyHandler genericEmailHandler = userPropertiesConfig.getPropertyHandler("genericEmailProperty1");
        genericEmailEl = genericEmailHandler.addFormItem(getLocale(), user, usageIdentifier, false, contactInformation);
        genericEmailEl.setUserObject(contactTracingModule.getGenericEmailState());
        userPropertyHandlerFormItemMap.put(genericEmailHandler, genericEmailEl);

        UserPropertyHandler mobilePhoneHandler = userPropertiesConfig.getPropertyHandler(UserConstants.TELMOBILE);
        mobilePhoneEl = mobilePhoneHandler.addFormItem(getLocale(), user, usageIdentifier, false, contactInformation);
        mobilePhoneEl.setUserObject(contactTracingModule.getMobilePhoneState());
        userPropertyHandlerFormItemMap.put(mobilePhoneHandler, mobilePhoneEl);

        UserPropertyHandler privatePhoneHandler = userPropertiesConfig.getPropertyHandler(UserConstants.TELPRIVATE);
        privatePhoneEl = privatePhoneHandler.addFormItem(getLocale(), user, usageIdentifier, false, contactInformation);
        privatePhoneEl.setUserObject(contactTracingModule.getPrivatePhoneState());
        userPropertyHandlerFormItemMap.put(privatePhoneHandler, privatePhoneEl);

        UserPropertyHandler officePhoneHandler = userPropertiesConfig.getPropertyHandler(UserConstants.TELOFFICE);
        officePhoneEl = officePhoneHandler.addFormItem(getLocale(), user, usageIdentifier, false, contactInformation);
        officePhoneEl.setUserObject(contactTracingModule.getOfficePhoneState());
        userPropertyHandlerFormItemMap.put(officePhoneHandler, officePhoneEl);

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

        startTimeEl.setDate(startDate);
        checkEnabled(startTimeEl);
        checkMandatory(startTimeEl);
        checkVisible(startTimeEl);

        endTimeEl.setDate(DateUtils.addMinutes(startDate, contactTracingModule.getDefaultDuration()));
        checkEnabled(endTimeEl);
        checkMandatory(endTimeEl);
        checkVisible(endTimeEl);

        for (FormItem formItem : userPropertyHandlerFormItemMap.values()) {
            checkEnabled(formItem);
            checkVisible(formItem);
            checkMandatory(formItem);
        }

        saveUserPropertiesToProfileEl.setVisible(user != null);
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

        // Disable email element for authenticated users
        if (formItem == emailEl && user != null) {
            formItem.setEnabled(false);
        }

        // Enable all elements which are empty
        if (formItem instanceof TextElement) {
            if (!StringHelper.containsNonWhitespace(((TextElement) formItem).getValue())) {
                formItem.setEnabled(true);
            }
        }

        // Enable all fields for unregistered users
        if (user == null) {
            formItem.setEnabled(true);
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

    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        boolean allOk = super.validateFormLogic(ureq);

        // Check every form item
        userPropertyHandlerFormItemMap.values().forEach(this::validateFormItem);

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
            userPropertyHandlerFormItemMap.forEach((userPropertyHandler, formItem) -> userPropertyHandler.updateUserFromFormItem(user, formItem));

            // TODO Why not available after reboot?
        }

        // Create new entry
        Date deletionDate = DateUtils.addDays(new Date(), contactTracingModule.getRetentionPeriod());
        ContactTracingRegistration entry = contactTracingManager.createRegistration(location, startTimeEl.getDate(), deletionDate);

        // Set information
        entry.setEndDate(endTimeEl.getDate());
        entry.setNickName(getValue(nickNameEl));
        entry.setFirstName(getValue(firstNameEl));
        entry.setLastName(getValue(lastNameEl));
        entry.setStreet(getValue(streetEl));
        entry.setExtraAddressLine(getValue(extraLineEl));
        entry.setZipCode(getValue(zipCodeEl));
        entry.setCity(getValue(cityEl));
        entry.setEmail(getValue(emailEl));
        entry.setInstitutionalEmail(getValue(institutionalEmailEl));
        entry.setGenericEmail(getValue(genericEmailEl));
        entry.setPrivatePhone(getValue(privatePhoneEl));
        entry.setMobilePhone(getValue(mobilePhoneEl));
        entry.setOfficePhone(getValue(officePhoneEl));

        // Update entry
        contactTracingManager.persistRegistration(entry);

        // Send mail
        // TODO Send mail

        fireEvent(ureq, Event.DONE_EVENT);
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
