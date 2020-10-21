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
package org.olat.modules.contacttracing;

import org.olat.NewControllerFactory;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * Initial date: 12.10.2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */

@Service
public class ContactTracingModule extends AbstractSpringModule implements ConfigOnOff {

    private static final String PROP_ENABLED =              "contact.tracing.enabled";
    private static final String PROP_RETENTION_PERIOD =     "contact.tracing.retention.period";
    private static final String PROP_DEFAULT_DURATION =     "contact.tracing.duration.default";

    private static final String PROP_ATTENDANCE_START =     "contact.tracing.attendance.start";
    private static final String PROP_ATTENDANCE_END =       "contact.tracing.attendance.end";
    private static final String PROP_NICK_NAME =            "contact.tracing.nickname";
    private static final String PROP_LAST_NAME =            "contact.tracing.lastname";
    private static final String PROP_FIRSTNAME =            "contact.tracing.firstname";
    private static final String PROP_STREET =               "contact.tracing.street";
    private static final String PROP_EXTRA_LINE =           "contact.tracing.extra.address.line";
    private static final String PROP_ZIP =                  "contact.tracing.zip.code";
    private static final String PROP_CITY =                 "contact.tracing.city";
    private static final String PROP_EMAIL =                "contact.tracing.email";
    private static final String PROP_INSTITUTIONAL_EMAIL =  "contact.tracing.institutional.email";
    private static final String PROP_GENERIC_EMAIL =        "contact.tracing.generic.email";
    private static final String PROP_PRIVATE_PHONE =        "contact.tracing.private.phone";
    private static final String PROP_MOBILE_PHONE =         "contact.tracing.mobile.phone";
    private static final String PROP_OFFICE_PHONE =         "contact.tracing.office.phone";

    private static final String PROP_QR_INSTRUCTIONS =      "contact.tracing.qr.instructions";

    @Value("${contact.tracing.enabled}")
    private boolean enabled;
    @Value("${contact.tracing.retention.period}")
    private int retentionPeriod;
    @Value("${contact.tracing.duration.default}")
    private int defaultDuration;

    @Value("${contact.tracing.attendance.start}")
    private AttributeState attendanceStartTime;
    @Value("${contact.tracing.attendance.end}")
    private AttributeState attendanceEndTime;
    @Value("${contact.tracing.nickname}")
    private AttributeState nickName;
    @Value("${contact.tracing.firstname}")
    private AttributeState firstName;
    @Value("${contact.tracing.lastname}")
    private AttributeState lastName;
    @Value("${contact.tracing.street}")
    private AttributeState street;
    @Value("${contact.tracing.extra.address.line}")
    private AttributeState extraAddressLine;
    @Value("${contact.tracing.zip.code}")
    private AttributeState zipCode;
    @Value("${contact.tracing.city}")
    private AttributeState city;
    @Value("${contact.tracing.email}")
    private AttributeState email;
    @Value("${contact.tracing.institutional.email}")
    private AttributeState institutionalEMail;
    @Value("${contact.tracing.generic.email}")
    private AttributeState genericEmail;
    @Value("${contact.tracing.private.phone}")
    private AttributeState privatePhone;
    @Value("${contact.tracing.mobile.phone}")
    private AttributeState mobilePhone;
    @Value("${contact.tracing.office.phone}")
    private AttributeState officePhone;

    @Value("${contact.tracing.qr.instructions}")
    private String qrCodeInstructions;

    @Autowired
    public ContactTracingModule(CoordinatorManager coordinatorManager) {
        super(coordinatorManager);
    }

    @Override
    public void init() {
        initProperties();
    }

    private void initProperties() {
        enabled = getBooleanPropertyValue(PROP_ENABLED) || enabled;
        retentionPeriod = getIntPropertyValue(PROP_RETENTION_PERIOD, retentionPeriod);
        defaultDuration = getIntPropertyValue(PROP_DEFAULT_DURATION, defaultDuration);

        attendanceStartTime = getAttributeState(PROP_ATTENDANCE_START, attendanceStartTime);
        attendanceEndTime = getAttributeState(PROP_ATTENDANCE_END, attendanceEndTime);
        nickName = getAttributeState(PROP_NICK_NAME, nickName);
        firstName = getAttributeState(PROP_FIRSTNAME, firstName);
        lastName = getAttributeState(PROP_LAST_NAME, lastName);
        street = getAttributeState(PROP_STREET, street);
        extraAddressLine = getAttributeState(PROP_EXTRA_LINE, extraAddressLine);
        zipCode = getAttributeState(PROP_ZIP, zipCode);
        city = getAttributeState(PROP_CITY, city);
        email = getAttributeState(PROP_EMAIL, email);
        institutionalEMail = getAttributeState(PROP_INSTITUTIONAL_EMAIL, institutionalEMail);
        genericEmail = getAttributeState(PROP_GENERIC_EMAIL, genericEmail);
        privatePhone = getAttributeState(PROP_PRIVATE_PHONE, privatePhone);
        mobilePhone = getAttributeState(PROP_MOBILE_PHONE, mobilePhone);
        officePhone = getAttributeState(PROP_OFFICE_PHONE, officePhone);

        qrCodeInstructions = getStringPropertyValue(PROP_QR_INSTRUCTIONS, qrCodeInstructions);
    }

    private AttributeState getAttributeState(String property, AttributeState fallBack) {
        String propertyObj = getStringPropertyValue(property, false);
        if (propertyObj != null) {
            try {
                return AttributeState.valueOf(propertyObj);
            } catch (Exception ignored) {
                return AttributeState.mandatory;
            }
        }

        return fallBack;
    }

    @Override
    protected void initFromChangedProperties() {
        initProperties();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        setBooleanProperty(PROP_ENABLED, enabled, true);
    }

    public int getRetentionPeriod() {
        return retentionPeriod;
    }

    public void setRetentionPeriod(int retentionPeriod) {
        this.retentionPeriod = retentionPeriod;
        setIntProperty(PROP_RETENTION_PERIOD, retentionPeriod, true);
    }

    public int getDefaultDuration() {
        return defaultDuration;
    }

    public void setDefaultDuration(int defaultDuration) {
        this.defaultDuration = defaultDuration;
        setIntProperty(PROP_DEFAULT_DURATION, defaultDuration, true);
    }

    public AttributeState getAttendanceStartTimeState() {
        return attendanceStartTime;
    }

    public void setAttendanceStartTimeState(AttributeState attendanceStartTime) {
        this.attendanceStartTime = attendanceStartTime;
        setStringProperty(PROP_ATTENDANCE_START, attendanceStartTime.name(), true);
    }

    public AttributeState getAttendanceEndTimeState() {
        return attendanceEndTime;
    }

    public void setAttendanceEndTimeState(AttributeState attendanceEndTime) {
        this.attendanceEndTime = attendanceEndTime;
        setStringProperty(PROP_ATTENDANCE_END, attendanceEndTime.name(), true);
    }

    public AttributeState getNickNameState() {
        return nickName;
    }

    public void setNickNameState(AttributeState nickName) {
        this.nickName = nickName;
        setStringProperty(PROP_NICK_NAME, nickName.name(), true);
    }

    public AttributeState getFirstNameState() {
        return firstName;
    }

    public void setFirstNameState(AttributeState firstName) {
        this.firstName = firstName;
        setStringProperty(PROP_FIRSTNAME, firstName.name(), true);
    }

    public AttributeState getLastNameState() {
        return lastName;
    }

    public void setLastNameState(AttributeState lastName) {
        this.lastName = lastName;
        setStringProperty(PROP_LAST_NAME, lastName.name(), true);
    }

    public AttributeState getStreetState() {
        return street;
    }

    public void setStreetState(AttributeState street) {
        this.street = street;
        setStringProperty(PROP_STREET, street.name(), true);
    }

    public AttributeState getExtraAddressLineState() {
        return extraAddressLine;
    }

    public void setExtraAddressLineState(AttributeState extraAddressLine) {
        this.extraAddressLine = extraAddressLine;
        setStringProperty(PROP_EXTRA_LINE, extraAddressLine.name(), true);
    }

    public AttributeState getZipCodeState() {
        return zipCode;
    }

    public void setZipCodeState(AttributeState zipCode) {
        this.zipCode = zipCode;
        setStringProperty(PROP_ZIP, zipCode.name(), true);
    }

    public AttributeState getCityState() {
        return city;
    }

    public void setCityState(AttributeState city) {
        this.city = city;
        setStringProperty(PROP_CITY, city.name(), true);
    }

    public AttributeState getEmailState() {
        return email;
    }

    public void setEmailState(AttributeState email) {
        this.email = email;
        setStringProperty(PROP_EMAIL, email.name(), true);
    }

    public AttributeState getInstitutionalEMailState() {
        return institutionalEMail;
    }

    public void setInstitutionalEMailState(AttributeState institutionalEMail) {
        this.institutionalEMail = institutionalEMail;
        setStringProperty(PROP_INSTITUTIONAL_EMAIL, institutionalEMail.name(), true);
    }

    public AttributeState getGenericEmailState() {
        return genericEmail;
    }

    public void setGenericEmailState(AttributeState genericEmail) {
        this.genericEmail = genericEmail;
        setStringProperty(PROP_GENERIC_EMAIL, genericEmail.name(), true);
    }

    public AttributeState getPrivatePhoneState() {
        return privatePhone;
    }

    public void setPrivatePhoneState(AttributeState privatePhone) {
        this.privatePhone = privatePhone;
        setStringProperty(PROP_PRIVATE_PHONE, privatePhone.name(), true);
    }

    public AttributeState getMobilePhoneState() {
        return mobilePhone;
    }

    public void setMobilePhoneState(AttributeState mobilePhone) {
        this.mobilePhone = mobilePhone;
        setStringProperty(PROP_MOBILE_PHONE, mobilePhone.name(), true);
    }

    public AttributeState getOfficePhoneState() {
        return officePhone;
    }

    public void setOfficePhoneState(AttributeState officePhone) {
        this.officePhone = officePhone;
        setStringProperty(PROP_OFFICE_PHONE, officePhone.name(), true);
    }

    public String getQrCodeInstructions() {
        return qrCodeInstructions;
    }

    public void setQrCodeInstructions(String qrCodeInstructions) {
        this.qrCodeInstructions = qrCodeInstructions;
        setStringProperty(PROP_QR_INSTRUCTIONS, qrCodeInstructions, true);
    }

    public enum AttributeState {
        // lower case, because it's saved in olat.properties and config files
        disabled,
        mandatory,
        optional,
        automatic
    }
}
