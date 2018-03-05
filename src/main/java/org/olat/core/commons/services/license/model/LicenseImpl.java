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
package org.olat.core.commons.services.license.model;

import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseType;

/**
 * 
 * Initial date: 02.03.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LicenseImpl implements License {
	
	private LicenseType licenseType;
	private String licensor;
	private String freetext;
	
	@Override
	public LicenseType getLicenseType() {
		return licenseType;
	}
	
	@Override
	public void setLicenseType(LicenseType licenseType) {
		this.licenseType = licenseType;
	}
	
	@Override
	public String getLicensor() {
		return licensor;
	}
	
	@Override
	public void setLicensor(String licensor) {
		this.licensor = licensor;
	}
	
	@Override
	public String getFreetext() {
		return freetext;
	}
	
	@Override
	public void setFreetext(String freetext) {
		this.freetext = freetext;
	}
	
}
