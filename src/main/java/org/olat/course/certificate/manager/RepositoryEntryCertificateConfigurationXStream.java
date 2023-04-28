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
package org.olat.course.certificate.manager;

import java.io.InputStream;
import java.io.OutputStream;

import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;
import org.olat.course.certificate.model.CertificateTemplateImpl;
import org.olat.course.certificate.model.RepositoryEntryCertificateConfigurationImpl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 28 avr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryCertificateConfigurationXStream {
	
	private static final XStream configurationXStream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {
				RepositoryEntryCertificateConfiguration.class, RepositoryEntryCertificateConfigurationImpl.class,
				CertificateTemplate.class, CertificateTemplateImpl.class
			};
		configurationXStream.addPermission(new ExplicitTypePermission(types));
		configurationXStream.alias("certificateConfiguration", RepositoryEntryCertificateConfigurationImpl.class);
		configurationXStream.alias("certificateTemplate", CertificateTemplateImpl.class);
		configurationXStream.omitField(RepositoryEntryCertificateConfigurationImpl.class, "entry");
	}
	
	private RepositoryEntryCertificateConfigurationXStream() {
		//
	}
	
	public static RepositoryEntryCertificateConfiguration toRules(String configurationXml) {
		return (RepositoryEntryCertificateConfiguration)configurationXStream.fromXML(configurationXml);
	}
	
	public static RepositoryEntryCertificateConfiguration toRules(InputStream in) {
		return (RepositoryEntryCertificateConfiguration)configurationXStream.fromXML(in);
	}
	
	public static String toXML(RepositoryEntryCertificateConfiguration configuration) {
		return configurationXStream.toXML(configuration);
	}
	
	public static void toXML(RepositoryEntryCertificateConfiguration configuration, OutputStream out) {
		configurationXStream.toXML(configuration, out);
	}
	
	public static RepositoryEntryCertificateConfiguration fromXML(InputStream in) {
		return (RepositoryEntryCertificateConfiguration)configurationXStream.fromXML(in);
	}
}