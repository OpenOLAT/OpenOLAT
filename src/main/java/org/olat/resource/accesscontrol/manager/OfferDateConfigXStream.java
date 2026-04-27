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
package org.olat.resource.accesscontrol.manager;

import org.olat.core.util.xml.XStreamHelper;
import org.olat.resource.accesscontrol.OfferDateConfig;
import org.olat.resource.accesscontrol.OfferDateRef;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * XStream helper for serializing {@link OfferDateConfig} to/from XML stored in the database.
 *
 * Initial date: 24.04.2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class OfferDateConfigXStream {

	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] { OfferDateConfig.class, OfferDateRef.class };
		xstream.addPermission(new ExplicitTypePermission(types));
		xstream.alias("dateConfig", OfferDateConfig.class);
	}

	public static String toXml(OfferDateConfig config) {
		if (config == null) {
			return null;
		}
		return xstream.toXML(config);
	}

	public static OfferDateConfig fromXml(String xml) {
		if (xml == null || xml.isBlank()) {
			return null;
		}
		return (OfferDateConfig) xstream.fromXML(xml);
	}

}
