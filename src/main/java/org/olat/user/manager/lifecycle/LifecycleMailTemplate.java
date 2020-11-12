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
package org.olat.user.manager.lifecycle;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.velocity.VelocityContext;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailTemplate;
import org.olat.user.UserManager;

public class LifecycleMailTemplate extends MailTemplate {
	
	private static final String FIRST_NAME = "firstName";
	private static final String LAST_NAME = "lastName";
	private static final String FULL_NAME = "fullName";
	private static final String EMAIL = "email";
	private static final String USER_NAME = "userName";
	private static final Collection<String> VARIABLE_NAMES =
			List.of(FIRST_NAME, LAST_NAME, FULL_NAME, EMAIL, USER_NAME);
	
	private final Locale locale;
	
	public LifecycleMailTemplate(String subject, String body, Locale locale) {
		super(subject, body, null);
		this.locale = locale;
	}
	
	public static final Collection<String> variableNames() {
		return VARIABLE_NAMES;
	}

	@Override
	public void putVariablesInMailContext(VelocityContext vContext, Identity recipient) {
		if(recipient != null) {
			UserManager userManager = CoreSpringFactory.getImpl(UserManager.class);
			BaseSecurityManager securityManager = CoreSpringFactory.getImpl(BaseSecurityManager.class);
			
			User user = recipient.getUser();
			
			vContext.put("firstname", user.getProperty(UserConstants.FIRSTNAME, null));
			vContext.put(FIRST_NAME, user.getProperty(UserConstants.FIRSTNAME, null));
			vContext.put("lastname", user.getProperty(UserConstants.LASTNAME, null));
			vContext.put(LAST_NAME, user.getProperty(UserConstants.LASTNAME, null));
			String fullName = userManager.getUserDisplayName(recipient);
			vContext.put("fullname", fullName);
			vContext.put(FULL_NAME, fullName); 
			vContext.put("mail", userManager.getUserDisplayEmail(user, locale));
			vContext.put(EMAIL, userManager.getUserDisplayEmail(user, locale));
			String loginName = securityManager.findAuthenticationName(recipient);
			if(!StringHelper.containsNonWhitespace(loginName)) {
				loginName = recipient.getName();
			}
			vContext.put("username", loginName);
			vContext.put(USER_NAME, loginName);
		}
	}
}