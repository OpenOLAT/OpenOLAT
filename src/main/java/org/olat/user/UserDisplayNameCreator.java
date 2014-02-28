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

import org.olat.basesecurity.IdentityNames;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;

/**
 * <h3>Description:</h3>
 * This bean implements a method to print out the users display name. The default implementation prints 
 * firsname lastname. 
 * <pre>
 * firstname lastname
 * </pre>
 * <p>
 * Inherit from this bean and inject it to the usermanager to modify this behavior
 * <p>
 * Initial Date: 02.03.2012 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class UserDisplayNameCreator {

	/**
	 * Returns the users displayable name, e.g. "Firstname Lastname"
	 * 
	 * @param user
	 * @return
	 */
	public String getUserDisplayName(User user) {
		if (user == null) return "unknown user";
		// use first and lastname for display purpose
		String first = user.getProperty(UserConstants.FIRSTNAME, null);
		String last = user.getProperty(UserConstants.LASTNAME, null);
		return getDisplayName(first, last);
	}

	public String getUserDisplayName(IdentityNames identity) {
		return getDisplayName(identity.getFirstName(), identity.getLastName());
	}
	
	protected String getDisplayName(String firstName, String lastName) {
		// expect null values to make it robust agains NPE and remove whitespace
		String combined = (firstName == null? "" : firstName) + " " + (lastName == null? "" : lastName);
		combined = combined.trim();
		if (combined.length() == 0) combined = "unknown user";
		return combined;
	}
}
