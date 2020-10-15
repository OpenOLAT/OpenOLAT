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

    private static final String PROP_ENABLED =              "contacttracing.enabled";
    private static final String PROP_RETENTION_PERIOD =     "contacttracing.retention.period";

    private static final String PROP_ATTENDANCE_START =     "contacttracing.attendance.start";
    private static final String PROP_ATTENDANCE_END =       "contacttracing.attendance.end";
    private static final String PROP_NICK_NAME =            "contacttracing.nickname";
    private static final String PROP_LAST_NAME =            "contacttracing.lastname";
    private static final String PROP_FIRSTNAME =            "contacttracing.firstname";
    private static final String PROP_STREET =               "contacttracing.street";
    private static final String PROP_EXTRA_LINE =           "contacttracing.extra.address.line";
    private static final String PROP_ZIP =                  "contacttracing.zip.code";
    private static final String PROP_CITY =                 "contacttracing.city";
    private static final String PROP_EMAIL =                "contacttracing.email";
    private static final String PROP_INSTITUTIONAL_EMAIL =  "contacttracing.institutional.email";
    private static final String PROP_GENERIC_EMAIL =        "contacttracing.generic.email";
    private static final String PROP_PRIVATE_PHONE =        "contacttracing.private.phone";
    private static final String PROP_MOBILE_PHONE =         "contacttracing.mobile.phone";
    private static final String PROP_OFFICE_PHONE =         "contacttracing.office.phone";

    private static final String PROP_QR_INSTRUCTIONS =      "contacttracing.qr.instructions";

    @Value("${contacttracing.enabled}")
    private boolean enabled;
    @Value("${contacttracing.retention.period}")
    private int retentionPeriod;

    @Value("${contacttracing.attendance.start}")
    private AttributeState attendanceStartTime;
    @Value("${contacttracing.attendance.end}")
    private AttributeState attendanceEndTime;
    @Value("${contacttracing.nickname}")
    private AttributeState nickName;
    @Value("${contacttracing.firstname}")
    private AttributeState firstName;
    @Value("${contacttracing.lastname}")
    private AttributeState lastName;
    @Value("${contacttracing.street}")
    private AttributeState street;
    @Value("${contacttracing.extra.address.line}")
    private AttributeState extraAddressLine;
    @Value("${contacttracing.zip.code}")
    private AttributeState zipCode;
    @Value("${contacttracing.city}")
    private AttributeState city;
    @Value("${contacttracing.email}")
    private AttributeState email;
    @Value("${contacttracing.institutional.email}")
    private AttributeState institutionalEMail;
    @Value("${contacttracing.generic.email}")
    private AttributeState genericEmail;
    @Value("${contacttracing.private.phone}")
    private AttributeState privatePhone;
    @Value("${contacttracing.mobile.phone}")
    private AttributeState mobilePhone;
    @Value("${contacttracing.office.phone}")
    private AttributeState officePhone;

    @Value("${contacttracing.qr.instructions}")
    private String qrCodeInstructions;

    @Autowired
    public ContactTracingModule(CoordinatorManager coordinatorManager) {
        super(coordinatorManager);
    }

    @Override
    public void init() {
        enabled = getBooleanPropertyValue(PROP_ENABLED) || enabled;
        retentionPeriod = getIntPropertyValue(PROP_RETENTION_PERIOD, retentionPeriod);

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
        init();
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

    public AttributeState getAttendanceStartTime() {
        return attendanceStartTime;
    }

    public void setAttendanceStartTime(AttributeState attendanceStartTime) {
        this.attendanceStartTime = attendanceStartTime;
        setStringProperty(PROP_ATTENDANCE_START, attendanceStartTime.name(), true);
    }

    public AttributeState getAttendanceEndTime() {
        return attendanceEndTime;
    }

    public void setAttendanceEndTime(AttributeState attendanceEndTime) {
        this.attendanceEndTime = attendanceEndTime;
        setStringProperty(PROP_ATTENDANCE_END, attendanceEndTime.name(), true);
    }

    public AttributeState getNickName() {
        return nickName;
    }

    public void setNickName(AttributeState nickName) {
        this.nickName = nickName;
        setStringProperty(PROP_NICK_NAME, nickName.name(), true);
    }

    public AttributeState getFirstName() {
        return firstName;
    }

    public void setFirstName(AttributeState firstName) {
        this.firstName = firstName;
        setStringProperty(PROP_FIRSTNAME, firstName.name(), true);
    }

    public AttributeState getLastName() {
        return lastName;
    }

    public void setLastName(AttributeState lastName) {
        this.lastName = lastName;
        setStringProperty(PROP_LAST_NAME, lastName.name(), true);
    }

    public AttributeState getStreet() {
        return street;
    }

    public void setStreet(AttributeState street) {
        this.street = street;
        setStringProperty(PROP_STREET, street.name(), true);
    }

    public AttributeState getExtraAddressLine() {
        return extraAddressLine;
    }

    public void setExtraAddressLine(AttributeState extraAddressLine) {
        this.extraAddressLine = extraAddressLine;
        setStringProperty(PROP_EXTRA_LINE, extraAddressLine.name(), true);
    }

    public AttributeState getZipCode() {
        return zipCode;
    }

    public void setZipCode(AttributeState zipCode) {
        this.zipCode = zipCode;
        setStringProperty(PROP_ZIP, zipCode.name(), true);
    }

    public AttributeState getCity() {
        return city;
    }

    public void setCity(AttributeState city) {
        this.city = city;
        setStringProperty(PROP_CITY, city.name(), true);
    }

    public AttributeState getEmail() {
        return email;
    }

    public void setEmail(AttributeState email) {
        this.email = email;
        setStringProperty(PROP_EMAIL, email.name(), true);
    }

    public AttributeState getInstitutionalEMail() {
        return institutionalEMail;
    }

    public void setInstitutionalEMail(AttributeState institutionalEMail) {
        this.institutionalEMail = institutionalEMail;
        setStringProperty(PROP_INSTITUTIONAL_EMAIL, institutionalEMail.name(), true);
    }

    public AttributeState getGenericEmail() {
        return genericEmail;
    }

    public void setGenericEmail(AttributeState genericEmail) {
        this.genericEmail = genericEmail;
        setStringProperty(PROP_GENERIC_EMAIL, genericEmail.name(), true);
    }

    public AttributeState getPrivatePhone() {
        return privatePhone;
    }

    public void setPrivatePhone(AttributeState privatePhone) {
        this.privatePhone = privatePhone;
        setStringProperty(PROP_PRIVATE_PHONE, privatePhone.name(), true);
    }

    public AttributeState getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(AttributeState mobilePhone) {
        this.mobilePhone = mobilePhone;
        setStringProperty(PROP_MOBILE_PHONE, mobilePhone.name(), true);
    }

    public AttributeState getOfficePhone() {
        return officePhone;
    }

    public void setOfficePhone(AttributeState officePhone) {
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
