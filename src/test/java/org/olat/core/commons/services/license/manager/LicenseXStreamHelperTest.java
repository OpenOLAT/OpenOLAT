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
package org.olat.core.commons.services.license.manager;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.model.LicenseImpl;
import org.olat.core.commons.services.license.model.LicenseTypeImpl;
import org.olat.core.commons.services.license.model.ResourceLicenseImpl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;

/**
 * 
 * Initial date: 16.03.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LicenseXStreamHelperTest {
	
	@Test
	public void shouldConvertToXmlAndBack() {
		String licensor = "licensor";
		String freetext = "freetext";
		String licenseTypeName = "licenseTypeName";
		LicenseTypeImpl licenseType = new LicenseTypeImpl();
		licenseType.setName(licenseTypeName);
		LicenseImpl license = new LicenseImpl();
		license.setLicensor(licensor);
		license.setFreetext(freetext);
		license.setLicenseType(licenseType);
		
		LicenseXStreamHelper licenseXStreamHelper = new LicenseXStreamHelper();
		String licenseAsXml = licenseXStreamHelper.toXml(license);
		System.out.println(licenseAsXml);
		License licenseFromXml = licenseXStreamHelper.licenseFromXml(licenseAsXml);
		
		assertThat(licenseFromXml.getLicensor()).isEqualTo(licensor);
		assertThat(licenseFromXml.getFreetext()).isEqualTo(freetext);
		assertThat(licenseFromXml.getLicenseType().getName()).isEqualTo(licenseTypeName);
	}
	
	@Test
	public void shouldConvertResourceLicenseToXmlAndBack() {
		String licensor = "licenseOR";
		String freetext = "free";
		String licenseTypeName = "strictlicense";
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Course", Long.valueOf(32));
		
		LicenseTypeImpl licenseType = new LicenseTypeImpl();
		licenseType.setName(licenseTypeName);
		ResourceLicenseImpl license = new ResourceLicenseImpl();
		license.setLicensor(licensor);
		license.setFreetext(freetext);
		license.setLicenseType(licenseType);
		license.setOLATResourceable(ores);
		
		LicenseXStreamHelper licenseXStreamHelper = new LicenseXStreamHelper();
		String licenseAsXml = licenseXStreamHelper.toXml(license);
		assertThat(licenseAsXml).isNotNull();
		License licenseFromXml = licenseXStreamHelper.licenseFromXml(licenseAsXml);
		
		assertThat(licenseFromXml.getLicensor()).isEqualTo(licensor);
		assertThat(licenseFromXml.getFreetext()).isEqualTo(freetext);
		assertThat(licenseFromXml.getLicenseType().getName()).isEqualTo(licenseTypeName);
	}

}
