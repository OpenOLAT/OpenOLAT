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
package org.olat.core.gui.components.util;

/**
 * 
 * Initial date: 18 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface SelectionValuesSupplier {
	
	public String getValue(String key);
	
	public String[] keys();
	
	/**
	 * Returns a array of all values. The method creates a new array every time it is invoked.
	 *
	 * @return
	 */
	public String[] values();
	
	/**
	 * Returns a array of all descriptions. The method creates a new array every time it is invoked.
	 *
	 * @return
	 */
	public default String[] descriptions() {
		return null;
	}
	
	/**
	 * Returns a array of all icons. The method creates a new array every time it is invoked.
	 *
	 * @return
	 */
	public default String[] icons() {
		return null;
	}
	
	/**
	 * Returns a array of all custom css classes. The method creates a new array every time it is invoked.
	 *
	 * @return
	 */
	public default String[] cssClasses() {
		return null;
	}
	
	/**
	 * Returns a array of all enabled states. The method creates a new array every time it is invoked.
	 *
	 * @return
	 */
	public default Boolean[] enabledStates() {
		return null;
	}

}
