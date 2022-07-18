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
package org.olat.modules.invitation.manager;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.invitation.InvitationAdditionalInfos;
import org.olat.modules.invitation.model.InvitationAdditionalInfosImpl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 18 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationAdditionalInfosXStream {
	
	private static final Logger log = Tracing.createLoggerFor(InvitationAdditionalInfosXStream.class);
	
	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {
				Map.class, HashMap.class, InvitationAdditionalInfos.class, InvitationAdditionalInfosImpl.class };
		xstream.addPermission(new ExplicitTypePermission(types));
	}
	
	public static String toXml(InvitationAdditionalInfos obj) {
		if (obj == null) return null;
		return xstream.toXML(obj);
	}
	
	public static InvitationAdditionalInfos fromXml(String xml) {
		if(StringHelper.containsNonWhitespace(xml)) {
			try {
				Object obj = xstream.fromXML(xml);
				return (InvitationAdditionalInfos)obj;
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return null;
	}
}
