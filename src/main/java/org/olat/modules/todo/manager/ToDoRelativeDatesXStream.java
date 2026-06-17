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
package org.olat.modules.todo.manager;

import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.todo.ToDoRelativeDates;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 *
 * Initial date: 7 May 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class ToDoRelativeDatesXStream {

	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] { ToDoRelativeDates.class };
		xstream.addPermission(new ExplicitTypePermission(types));
		xstream.alias("relativeDates", ToDoRelativeDates.class);
	}

	public static String toXml(ToDoRelativeDates relativeDates) {
		if (relativeDates == null) {
			return null;
		}
		return xstream.toXML(relativeDates);
	}

	public static ToDoRelativeDates fromXml(String xml) {
		if (xml == null || xml.isBlank()) {
			return null;
		}
		return (ToDoRelativeDates) xstream.fromXML(xml);
	}

}
