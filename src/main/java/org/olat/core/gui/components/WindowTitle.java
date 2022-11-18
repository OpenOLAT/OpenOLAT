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
package org.olat.core.gui.components;

/**
 * 
 * Simple wrapper object for being able to change the title in the Window class
 * without loosing the reference to the string in the velocity container
 * 
 * Initial date: 17 Nov 2022<br>
 * 
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class WindowTitle {
	private String value;

	WindowTitle() {
		// empty by default
	}
	
	public void setValue(String value) {
		this.value = value;	
	}
	
	public String getValue() {
		return this.value;
	}
}