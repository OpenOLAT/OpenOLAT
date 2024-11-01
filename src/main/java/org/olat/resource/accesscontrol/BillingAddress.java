/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.resource.accesscontrol;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Organisation;

/**
 * 
 * Initial date: 30 Oct 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public interface BillingAddress extends ModifiedInfo, CreateInfo{

	Long getKey();

	String getIdentifier();

	void setIdentifier(String identifier);

	String getNameLine1();

	void setNameLine1(String nameLine1);

	String getNameLine2();

	void setNameLine2(String nameLine2);

	String getAddressLine1();

	void setAddressLine1(String addressLine1);

	String getAddressLine2();

	void setAddressLine2(String addressLine2);

	String getAddressLine3();

	void setAddressLine3(String addressLine3);

	String getAddressLine4();

	void setAddressLine4(String addressLine4);

	String getPoBox();

	void setPoBox(String poBox);

	String getRegion();

	void setRegion(String region);
	
	public String getZip();

	public void setZip(String zip);

	String getCity();

	void setCity(String city);

	String getCountry();

	void setCountry(String country);

	boolean isEnabled();

	void setEnabled(boolean enabled);

	Organisation getOrganisation();

	Identity getIdentity();

}
