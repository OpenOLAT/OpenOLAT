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
package org.olat.modules.selectus.ui.app_wizard;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 16.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Referee {

	private final String title;
	private final String firstName;
	private final String lastName;
	private final String institution;
	private final String email;
	private final String fullName;
	
	public Referee(String title, String firstName, String lastName, String fullName, String institution, String email) {
		this.title = title;
		this.firstName = firstName;
		this.lastName = lastName;
		this.institution = institution;
		this.email = email;
		this.fullName = fullName;
	}
	
	public String getFullName() {
		return fullName;
	}

	public String getTitle() {
		return title;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getInstitution() {
		return institution;
	}

	public String getEmail() {
		return email;
	}
	
	public boolean isComplete() {
		return StringHelper.containsNonWhitespace(firstName)
				&& StringHelper.containsNonWhitespace(lastName)
				&& StringHelper.containsNonWhitespace(institution)
				&& StringHelper.containsNonWhitespace(email);
	}
}
