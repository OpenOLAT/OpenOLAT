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
package org.olat.core.util.mail;

import org.apache.commons.validator.routines.EmailValidator;

/**
 * Description:<br>
 * This validator checks if a given email address is valid against the RFC
 * specs. 
 * <P>
 * Initial Date: 12.11.2009 <br>
 * 
 * @author gnaegi
 */
public class EmailAddressValidator {
	private static final EmailValidator validator = EmailValidator.getInstance();
	/**
	 * Check if the given mail address is valid according to RFC specifications.
	 * @param mailAddress
	 * @return true: valid email address; false: not a valid email address
	 */
	public static boolean isValidEmailAddress(String mailAddress) {
		// Validate an email address
		return validator.isValid(mailAddress);
	}
}
