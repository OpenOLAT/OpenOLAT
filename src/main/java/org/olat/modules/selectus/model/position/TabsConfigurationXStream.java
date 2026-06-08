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
package org.olat.modules.selectus.model.position;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.xml.XStreamHelper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 17 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TabsConfigurationXStream {
	
	private static final Logger log = Tracing.createLoggerFor(TabsConfigurationXStream.class);
	
	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	
	static {
		Class<?>[] types = new Class[] {
				TabsConfiguration.class, TabsConfiguration.Tab.class, TabConfiguration.class,
				Map.class, HashMap.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));
		
		xstream.aliasPackage("com.frentix.recruiting", "org.olat.modules.selectus");
	}
	
	public static String toXml(TabsConfiguration obj) {
		try {
			return xstream.toXML(obj);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	public static TabsConfiguration fromXml(String xml) {
		try {
			if(xml.indexOf("\"enum-map\"") >= 0) {
				// EnumMap are not supported anymore, backwards compatibility with selectus
				xml = xml.replace("\"enum-map\"", "\"map\"");
			}
			return (TabsConfiguration)xstream.fromXML(xml);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}
