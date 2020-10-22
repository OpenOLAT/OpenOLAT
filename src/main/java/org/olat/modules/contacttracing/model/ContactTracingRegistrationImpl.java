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
package org.olat.modules.contacttracing.model;

import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingRegistration;

/**
 * Initial date: 13.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@Entity(name = "contactTracingRegistration")
@Table(name = "o_ct_registration")
public class ContactTracingRegistrationImpl implements ContactTracingRegistration {

    private static final long serialVersionUID = 5722478172355407350L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
    private Long key;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="creationdate", nullable=false, insertable=true, updatable=false)
    private Date creationDate;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="l_deletion_date", nullable=false, insertable=true, updatable=false)
    private Date deletionDate;

    @ManyToOne(targetEntity = ContactTracingLocationImpl.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="fk_location", nullable=false, insertable=true, updatable=false)
    private ContactTracingLocation location;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="l_start_date", nullable=false, insertable=true, updatable=false)
    private Date startDate;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="l_end_date", nullable=true, insertable=true, updatable=true)
    private Date endDate;
    @Column(name = "l_nick_name", nullable = true, insertable = true, updatable = true)
    private String nickName;
    @Column(name = "l_first_name", nullable = true, insertable = true, updatable = true)
    private String firstName;
    @Column(name = "l_last_name", nullable = true, insertable = true, updatable = true)
    private String lastName;
    @Column(name = "l_street", nullable = true, insertable = true, updatable = true)
    private String street;
    @Column(name = "l_extra_line", nullable = true, insertable = true, updatable = true)
    private String extraAddressLine;
    @Column(name = "l_zip_code", nullable = true, insertable = true, updatable = true)
    private String zipCode;
    @Column(name = "l_city", nullable = true, insertable = true, updatable = true)
    private String city;
    @Column(name = "l_email", nullable = true, insertable = true, updatable = true)
    private String email;
    @Column(name = "l_institutional_email", nullable = true, insertable = true, updatable = true)
    private String institutionalEmail;
    @Column(name = "l_generic_email", nullable = true, insertable = true, updatable = true)
    private String genericEmail;
    @Column(name = "l_private_phone", nullable = true, insertable = true, updatable = true)
    private String privatePhone;
    @Column(name = "l_mobile_phone", nullable = true, insertable = true, updatable = true)
    private String mobilePhone;
    @Column(name = "l_office_phone", nullable = true, insertable = true, updatable = true)
    private String officePhone;

    @Override
    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public ContactTracingLocation getLocation() {
        return location;
    }

    @Override
    public void setLocation(ContactTracingLocation location) {
        this.location = location;
    }

    @Override
    public Date getDeletionDate() {
        return deletionDate;
    }

    @Override
    public void setDeletionDate(Date deletionDate) {
        this.deletionDate = deletionDate;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    @Override
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }

    @Override
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public String getNickName() {
        return nickName;
    }

    @Override
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String getStreet() {
        return street;
    }

    @Override
    public void setStreet(String street) {
        this.street = street;
    }

    @Override
    public String getExtraAddressLine() {
        return extraAddressLine;
    }

    @Override
    public void setExtraAddressLine(String extraAddressLine) {
        this.extraAddressLine = extraAddressLine;
    }

    @Override
    public String getZipCode() {
        return zipCode;
    }

    @Override
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    @Override
    public String getCity() {
        return city;
    }

    @Override
    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getGenericEmail() {
        return genericEmail;
    }

    @Override
    public void setGenericEmail(String genericEmail) {
        this.genericEmail = genericEmail;
    }

    @Override
    public String getInstitutionalEmail() {
        return institutionalEmail;
    }

    @Override
    public void setInstitutionalEmail(String institutionalEmail) {
        this.institutionalEmail = institutionalEmail;
    }

    @Override
    public String getPrivatePhone() {
        return privatePhone;
    }

    @Override
    public void setPrivatePhone(String privatePhone) {
        this.privatePhone = privatePhone;
    }

    @Override
    public String getMobilePhone() {
        return mobilePhone;
    }

    @Override
    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    @Override
    public String getOfficePhone() {
        return officePhone;
    }

    @Override
    public void setOfficePhone(String officePhone) {
        this.officePhone = officePhone;
    }

    @Override
    public boolean equalsByPersistableKey(Persistable persistable) {
        return equals(persistable);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (object instanceof ContactTracingRegistration) {
            return getKey() != null && getKey().equals(((ContactTracingRegistration)object).getKey());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
