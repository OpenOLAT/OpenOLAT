/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.certificate.manager;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.certificate.model.CertificateConfig;
import org.olat.course.certificate.model.CertificateWorkUnit;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 5 août 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificateWorkUnitXStream {

	private static final Logger log = Tracing.createLoggerFor(CertificateWorkUnitXStream.class);
	
	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] { CertificateWorkUnit.class, CertificateConfig.class };
		xstream.ignoreUnknownElements();
		xstream.addPermission(new ExplicitTypePermission(types));
		xstream.alias("certificateWork", CertificateWorkUnit.class);
	}
	
	private CertificateWorkUnitXStream() {
		//
	}
	
	static String toXml(CertificateWorkUnit obj) {
		if (obj == null) return null;
		
		return xstream.toXML(obj);
	}
	
	static CertificateWorkUnit fromXml(String xml) {
		if(StringHelper.containsNonWhitespace(xml)) {
			try {
				Object obj = xstream.fromXML(xml);
				return (CertificateWorkUnit)obj;
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return null;
	}
}
