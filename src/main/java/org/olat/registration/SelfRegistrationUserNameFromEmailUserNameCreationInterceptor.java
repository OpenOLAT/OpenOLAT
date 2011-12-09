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

package org.olat.registration;

import java.util.Map;

import org.olat.core.util.StringHelper;

/**
 * 
 * Description:<br>
 * An interceptor to preset the username while the registration process.
 * This one restrict the registration to one single domain and extract from
 * the email address the username
 * 
 * <P>
 * Initial Date:  5 mars 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class SelfRegistrationUserNameFromEmailUserNameCreationInterceptor extends AbstractUserNameCreationInterceptor {

	private String emailDomain;

	public String getEmailDomain() {
		return emailDomain;
	}

	public void setEmailDomain(String emailDomain) {
		this.emailDomain = emailDomain;
	}

	@Override
	public String getUsernameFor(Map<String, String> userAttributes) {
		String email = userAttributes.get("email");
		if(StringHelper.containsNonWhitespace(email)) {
			if(email.endsWith("@" + emailDomain)) {
				String proposedUsername = email.substring(0, email.indexOf('@'));
				if(isMakeUniqueProposal()) {
					return makeUniqueProposal(proposedUsername);
				}
				return proposedUsername;
			}
		}
		return null;
	}
}
