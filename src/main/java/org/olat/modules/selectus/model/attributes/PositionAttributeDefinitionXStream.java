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
package org.olat.modules.selectus.model.attributes;

import org.olat.core.util.xml.XStreamHelper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 17 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionAttributeDefinitionXStream {
	
	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	
	static {
		Class<?>[] types = new Class[] {
			SeparatorConfiguration.class, SelectConfiguration.class, SelectConfiguration.Option.class,
			SelectConfiguration.Order.class, SelectConfiguration.Display.class,
			StaticTextConfiguration.class, StaticTextConfiguration.TextDisplay.class,
			TextConfiguration.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));

		xstream.aliasPackage("com.frentix.recruiting", "org.olat.modules.selectus");
	}
	
	public static String toXml(Object obj) {
		return xstream.toXML(obj);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T fromXml(String xml, @SuppressWarnings("unused") Class<T> cl) {
		return (T)xstream.fromXML(xml);
	}
}
