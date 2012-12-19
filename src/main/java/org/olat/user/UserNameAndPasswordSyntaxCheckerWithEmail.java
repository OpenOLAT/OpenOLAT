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
package org.olat.user;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <h3>Description:</h3>
 * This login name and password syntax checker provides methods to check if a
 * user name and a password is syntactically correct. Inherit from this class
 * and configure your own checker in the spring user manager configuration to
 * allow different user names that the default (e.g. if you want to use email
 * addresses as login names)
 * <p>
 * Initial Date: 5.06.2008 <br>
 * 
 * @author Roman Haag, frentix GmbH, http://www.frentix.com
 */
public class UserNameAndPasswordSyntaxCheckerWithEmail extends UserNameAndPasswordSyntaxChecker {

	/**
	 * Validates an OLAT password on a syntactical level. A password is valid when
	 * it matches the following requrements:<br>
	 * <ol>
	 * <li />Only numbers (0-9), letters (a-z, A-Z) and the following special
	 * characters (!"#$%&'()*+,-./:;<=>?@[\]^_`{|}~) are allowed
	 * <li />Size is between 4 and 128 characters
	 * <li />At least one number and at least one letter must be used
	 * </ol>
	 * 
	 * @param password The passwort to validate
	 * @return true if it is valid, false otherwhise
	 */
	@Override
	public boolean syntaxCheckOlatPassword(String password) {
		Pattern p = Pattern.compile("^\\p{Graph}{4,128}$");
		Matcher m = p.matcher(password);

		if (m.matches()) {
			if (!password.matches(".*['\"]+.*")) {
				if (password.matches(".*\\p{Alpha}+.*")) {
					if (password.matches(".*\\p{Digit}+.*")) return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks for a valid login name. The method does only check if the name is
	 * syntactically correct. It does not check if the name is valid
	 * 
	 * @param login
	 * @return True if syntax is ok.
	 */
	@Override
	public boolean syntaxCheckOlatLogin(String login) {
		// Allow only alphanumeric login names 
		// @ is allowed also (will be replaced for Jabber)		
		return login.matches("[0-9a-z\\.\\-_]{4,64}") || login.matches("[0-9a-z\\.\\-_@]{6,64}");
	}

}
