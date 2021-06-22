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

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.model.LicenseImpl;
import org.olat.core.commons.services.license.model.LicenseTypeImpl;
import org.olat.core.commons.services.license.model.ResourceLicenseImpl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.springframework.stereotype.Component;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 16.03.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
class LicenseXStreamHelper {
	
	private static final Logger log = Tracing.createLoggerFor(LicenseXStreamHelper.class);
	
	private static final XStream licenseXStream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		Class<?>[] types = new Class[] {
				License.class, LicenseImpl.class, ResourceLicenseImpl.class,
				LicenseType.class, LicenseTypeImpl.class,
		};
		licenseXStream.addPermission(new ExplicitTypePermission(types));
		
		licenseXStream.alias("license", LicenseImpl.class);
		licenseXStream.alias("license", ResourceLicenseImpl.class);
		licenseXStream.alias("licenseType", LicenseTypeImpl.class);
		licenseXStream.ignoreUnknownElements();
		licenseXStream.omitField(LicenseImpl.class, "creationDate");
		licenseXStream.omitField(LicenseImpl.class, "lastModified");
		licenseXStream.omitField(ResourceLicenseImpl.class, "creationDate");
		licenseXStream.omitField(ResourceLicenseImpl.class, "lastModified");
		licenseXStream.omitField(LicenseTypeImpl.class, "creationDate");
		licenseXStream.omitField(LicenseTypeImpl.class, "lastModified");
	}
	
	String toXml(License license) {
		if (license == null) return null;

		return licenseXStream.toXML(license);
	}
	
	License licenseFromXml(String xml) {
		License license = null;
		if(StringHelper.containsNonWhitespace(xml)) {
			try {
				Object obj = licenseXStream.fromXML(xml);
				if(obj instanceof License) {
					license = (License) obj;
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return license;
	}
	
}
