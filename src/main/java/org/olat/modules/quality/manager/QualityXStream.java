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
package org.olat.modules.quality.manager;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.model.ToDoTaskImpl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 19 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityXStream {
	
	private static final XStream xstream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		Class<?>[] types = new Class[] {
				ToDoTaskImpl.class,
				ToDoStatus.class,
				ToDoPriority.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));
		
		xstream.alias("ToDoTask", ToDoTaskImpl.class);
		xstream.omitField(ToDoTaskImpl.class, "baseGroup");
		xstream.omitField(ToDoTaskImpl.class, "assigneeRightsEnum");
	}
	
	public static String toXml(Object obj) {
		return xstream.toXML(obj);
	}
	
	@SuppressWarnings("unchecked")
	public static <U> U fromXml(String xml, @SuppressWarnings("unused") Class<U> cl) {
		Object obj = xstream.fromXML(xml);
		return (U)obj;
	}
	
	private static final XStream rolesXStream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {};
		rolesXStream.addPermission(new ExplicitTypePermission(types));
		rolesXStream.alias("Roles", List.class);
		rolesXStream.alias("Roles", ArrayList.class);
		rolesXStream.alias("Role", String.class);
	}
	
	public static String rolesToXml(Collection<String> roles) {
		return roles != null && !roles.isEmpty()
				? rolesXStream.toXML(new ArrayList<>(roles))
				: null;
	}
	
	private static final XStream tagsXStream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {};
		tagsXStream.addPermission(new ExplicitTypePermission(types));
		tagsXStream.alias("Tags", List.class);
		tagsXStream.alias("Tags", ArrayList.class);
		tagsXStream.alias("Tag", String.class);
	}
	
	public static String tagsToXml(Collection<String> roles) {
		return roles != null && !roles.isEmpty()
				? tagsXStream.toXML(new ArrayList<>(roles))
				: null;
	}
	
	@SuppressWarnings("unchecked")
	public static List<String> tagsFromXml(String xml) {
		return StringHelper.containsNonWhitespace(xml)
				? (ArrayList<String>) tagsXStream.fromXML(xml)
				: List.of();
	}
	
}
