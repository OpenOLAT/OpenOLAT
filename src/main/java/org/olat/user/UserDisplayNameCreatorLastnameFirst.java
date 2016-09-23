/**
 * <a href=“http://www.openolat.org“>
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
 * 03.02.2012 by frentix GmbH, http://www.frentix.com
 * <p>
**/
package org.olat.user;


/**
 * <h3>Description:</h3> This bean implements an alternative method to display
 * the user name by showing first the lastname and then the firstname separated
 * by a comma.
 * <pre>
 * lastname, firstname
 * </pre>
 * <p>
 * Inherit from this bean and inject it to the usermanager to modify this
 * behavior
 * <p>
 * Initial Date: 02.03.2012 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class UserDisplayNameCreatorLastnameFirst extends UserDisplayNameCreator{

	/**
	 * Returns the users displayable name, e.g. "Firstname Lastname"
	 * 
	 * @param first The first name
	 * @param last The last name
	 * @return
	 */
	@Override
	protected String getDisplayName(String first, String last) {
		// expect null values to make it robust against NPE and remove whitespace
		String combined = "";
		if (last != null) {
			combined = last;
			if (first != null) {
				combined = combined + ", ";
			}
		}
		if (first != null) {
			combined = combined + first;
		}
		combined = combined.trim();
		if (combined.length() == 0) combined = "unknown user";
		return combined;
	}
}
