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
package org.olat.core.gui.components.date;

import java.util.Date;

import org.olat.core.gui.components.velocity.VelocityContainer;

/**
 * Description:<br>
 * Use this factory to create a simple date component that can display a date as
 * a little calendar. Optionally the year can be enabled or disabled.
 * <p>
 * The component does not fire any events not can the date be changed using this
 * component. Use the flexi form for this purpose. This is a display-only
 * component.
 * 
 * <P>
 * Initial Date: 01.12.2009 <br>
 * 
 * @author gnaegi
 */
public class DateComponentFactory {

	/**
	 * Create a date view that displays the year, month and day.
	 * 
	 * @param name
	 *            the name of the component
	 * @param date
	 *            the date to be displayed
	 * @param container
	 *            the container or NULL if you add it yourself to your container
	 * @return the date component
	 */
	public static DateComponent createDateComponentWithYear(String name,
			Date date, VelocityContainer container) {
		DateComponent comp = new DateComponent(name, date, true);
		if (container != null) {
			container.put(name, comp);
		}
		return comp;
	}
	
	public static DateElement createDateElementWithYear(String name,
			Date date) {
		DateComponent cmp = createDateComponentWithYear(name, date, null);
		return new DateElement(name, cmp);
	}

	/**
	 * Create a date view that displays only the month and the day and no year.
	 * 
	 * @param name
	 *            the name of the component
	 * @param date
	 *            the date to be displayed
	 * @param container
	 *            the container or NULL if you add it yourself to your container
	 * @return the date component
	 */
	public static DateComponent createDateComponentWithoutYear(String name,
			Date date, VelocityContainer container) {
		DateComponent comp = new DateComponent(name, date, false);
		if (container != null) {
			container.put(name, comp);
		}
		return comp;
	}

}
