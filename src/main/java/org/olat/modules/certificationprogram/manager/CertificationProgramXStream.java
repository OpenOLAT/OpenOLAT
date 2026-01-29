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
package org.olat.modules.certificationprogram.manager;

import java.util.List;

import org.olat.basesecurity.model.OrganisationImpl;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.model.CertificateTemplateImpl;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramToOrganisation;
import org.olat.modules.certificationprogram.model.CertificationProgramImpl;
import org.olat.modules.certificationprogram.model.CertificationProgramMailConfigurationImpl;
import org.olat.modules.certificationprogram.model.CertificationProgramToOrganisationImpl;
import org.olat.modules.certificationprogram.ui.component.DurationType;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.model.CreditPointSystemImpl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 28 janv. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramXStream {
	
	private static final XStream xstream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		Class<?>[] types = new Class[] {
				CertificationProgram.class, CertificationProgramImpl.class,
				CertificationProgramMailConfigurationImpl.class, CertificationProgramMailConfigurationImpl.class,
				CreditPointSystem.class, CreditPointSystemImpl.class,
				CertificateTemplate.class, CertificateTemplateImpl.class,
				CertificationProgramToOrganisation.class, CertificationProgramToOrganisationImpl.class,
				Organisation.class, OrganisationImpl.class,
				DurationType.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));
		
		xstream.alias("CertificationProgram", CertificationProgramImpl.class);
		xstream.alias("CertificationProgramMailConfiguration", CertificationProgramMailConfigurationImpl.class);
		xstream.alias("CreditPointSystem", CreditPointSystemImpl.class);
		xstream.alias("CertificationProgramToOrganisation", CertificationProgramToOrganisationImpl.class);
		xstream.alias("Organisation", OrganisationImpl.class);
		
		xstream.omitField(CertificationProgramImpl.class, "group");
		xstream.omitField(CertificationProgramImpl.class, "resource");
		xstream.omitField(CertificationProgramImpl.class, "organisations");
		xstream.omitField(CertificationProgramMailConfigurationImpl.class, "certificationProgram");

		xstream.omitField(CreditPointSystemImpl.class, "organisations");
		
		xstream.omitField(OrganisationImpl.class, "group");
		xstream.omitField(OrganisationImpl.class, "root");
		xstream.omitField(OrganisationImpl.class, "parent");
		xstream.omitField(OrganisationImpl.class, "type");
		xstream.omitField(OrganisationImpl.class, "children");
		xstream.omitField(OrganisationImpl.class, "location");
		xstream.omitField(OrganisationImpl.class, "description");
		xstream.omitField(OrganisationImpl.class, "cssClass");
	}
	
	public static String toXml(Object obj) {
		return xstream.toXML(obj);
	}
	
	public static String toXml(List<Long> obj) {
		return xstream.toXML(obj);
	}
	
	@SuppressWarnings("unchecked")
	public static <U> U fromXml(String xml, @SuppressWarnings("unused") Class<U> cl) {
		if (StringHelper.containsNonWhitespace(xml)) {
			Object obj = xstream.fromXML(xml);
			return (U)obj;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static List<Long> fromXmlToListLong(String xml) {
		if (StringHelper.containsNonWhitespace(xml)) {
			Object obj = xstream.fromXML(xml);
			return (List<Long>)obj;
		}
		return null;
	}
}
