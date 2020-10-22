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

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Persistable;

/**
 * Initial date: 13.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public interface ContactTracingRegistration extends Persistable, CreateInfo {

    public Date getDeletionDate();

    public void setDeletionDate(Date deletionDate);

    public ContactTracingLocation getLocation();

    public void setLocation(ContactTracingLocation location);

    public Date getStartDate();

    public void setStartDate(Date startDate);

    public Date getEndDate();

    public void setEndDate(Date endDate);

    public String getNickName();

    public void setNickName(String nickName);

    public String getFirstName();

    public void setFirstName(String firstName);

    public String getLastName();

    public void setLastName(String lastName);

    public String getStreet();

    public void setStreet(String street);

    public String getExtraAddressLine();

    public void setExtraAddressLine(String extraAddressLine);

    public String getZipCode();

    public void setZipCode(String zipCode);

    public String getCity();

    public void setCity(String city);

    public String getEmail();

    public void setEmail(String email);

    public String getInstitutionalEmail();

    public void setInstitutionalEmail(String institutionalEmail);

    public String getGenericEmail();

    public void setGenericEmail(String genericEmail);

    public String getPrivatePhone();

    public void setPrivatePhone(String privatePhone);

    public String getMobilePhone();

    public void setMobilePhone(String mobilePhone);

    public String getOfficePhone();

    public void setOfficePhone(String officePhone);
}
