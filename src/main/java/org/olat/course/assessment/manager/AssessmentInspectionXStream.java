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
package org.olat.course.assessment.manager;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.assessment.AssessmentInspection;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.model.AssessmentInspectionConfigurationImpl;
import org.olat.course.assessment.model.AssessmentInspectionImpl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 5 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionXStream {
	
	private static final XStream xstream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		Class<?>[] types = new Class[] {
				AssessmentInspection.class, AssessmentInspectionImpl.class,
				AssessmentInspectionConfiguration.class, AssessmentInspectionConfigurationImpl.class,
				Identity.class, IdentityImpl.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));
		
		xstream.alias("inspection", AssessmentInspectionImpl.class);
		xstream.alias("identity", IdentityImpl.class);
		xstream.omitField(IdentityImpl.class, "user");
		xstream.omitField(AssessmentInspectionImpl.class, "configuration");
	}
	
	private AssessmentInspectionXStream() {
		//
	}
	
	public static String toXml(Object obj) {
		return xstream.toXML(obj);
	}
	
	@SuppressWarnings("unchecked")
	public static <U> U fromXml(String xml, @SuppressWarnings("unused") Class<U> cl) {
		Object obj = xstream.fromXML(xml);
		return (U)obj;
	}
	
	

}
