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
package org.olat.user.manager.lifecycle;

import java.util.Collection;
import java.util.Date;
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
import org.olat.user.UserLifecycleManager;
import org.olat.user.UserManager;

public class LifecycleMailTemplate extends MailTemplate {
	
	private static final String FIRST_NAME = "firstName";
	private static final String LAST_NAME = "lastName";
	private static final String FULL_NAME = "fullName";
	private static final String EMAIL = "email";
	private static final String USER_NAME = "userName";
	private static final String REACTION_TIME = "reactionTime";
	private static final Collection<String> VARIABLE_NAMES =
			List.of(FIRST_NAME, LAST_NAME, FULL_NAME, EMAIL, USER_NAME, REACTION_TIME);
	
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
			UserLifecycleManager userLifecycleManager = CoreSpringFactory.getImpl(UserLifecycleManager.class);
			
			User user = recipient.getUser();
			
			String firstname = StringHelper.escapeHtml(user.getProperty(UserConstants.FIRSTNAME, null));
			vContext.put("firstname", firstname);
			vContext.put(FIRST_NAME, firstname);
			String lastname = StringHelper.escapeHtml(user.getProperty(UserConstants.LASTNAME, null));
			vContext.put("lastname", lastname);
			vContext.put(LAST_NAME, lastname);
			String fullName = StringHelper.escapeHtml(userManager.getUserDisplayName(recipient));
			vContext.put("fullname", fullName);
			vContext.put(FULL_NAME, fullName); 
			String userDisplayEmail =StringHelper.escapeHtml( userManager.getUserDisplayEmail(user, locale));
			vContext.put("mail", userDisplayEmail);
			vContext.put(EMAIL, userDisplayEmail);
			String loginName = securityManager.findAuthenticationName(recipient);
			if(!StringHelper.containsNonWhitespace(loginName)) {
				loginName = recipient.getName();
			}
			vContext.put("username", loginName);
			vContext.put(USER_NAME, loginName);

			String reactionTime = "";
			// getDaysUntilDeactivation covers expiration and deactivation
			if (vContext.get("type").equals("before expiration") || vContext.get("type").equals("before deactivation")) {
				reactionTime = String.valueOf(userLifecycleManager.getDaysUntilDeactivation(recipient, new Date()));
			} else if (vContext.get("type").equals("before deletion")) {
				reactionTime = String.valueOf(userLifecycleManager.getDaysUntilDeletion(recipient, new Date()));
			}

			vContext.put("reactiontime", reactionTime);
			vContext.put(REACTION_TIME, reactionTime);
		}
	}
}