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
package org.olat.modules.reminder.manager;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.velocity.VelocityContext;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailTemplate;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;

public class CourseReminderTemplate extends MailTemplate {
		
		private static final String FIRST_NAME = "firstName";
		private static final String LAST_NAME = "lastName";
		private static final String FULL_NAME = "fullName";
		private static final String EMAIL = "email";
		private static final String USERNAME = "username";
		private static final String COURSE_URL = "courseUrl";
		private static final String COURSE_NAME = "courseName";
		private static final String COURSE_DESCRIPTION = "courseDescription";
		private static final Collection<String> VARIABLE_NAMES =
				List.of(FIRST_NAME, LAST_NAME, FULL_NAME, EMAIL, USERNAME, COURSE_URL, COURSE_NAME, COURSE_DESCRIPTION);
		
		private final String url;
		private final RepositoryEntry entry;
		private final Locale locale;
		
		public CourseReminderTemplate(String subjectTemplate, String bodyTemplate, String url, RepositoryEntry entry, Locale locale) {
			super(subjectTemplate, bodyTemplate, null);
			this.url = url;
			this.entry = entry;
			this.locale = locale;
		}
		
		public static final Collection<String> variableNames() {
			return VARIABLE_NAMES;
		}

		@Override
		public void putVariablesInMailContext(VelocityContext vContext, Identity recipient) {
			UserManager userManager = CoreSpringFactory.getImpl(UserManager.class);
			BaseSecurityManager securityManager = CoreSpringFactory.getImpl(BaseSecurityManager.class);
			
			User user = recipient.getUser();
			vContext.put("firstname", user.getFirstName());
			vContext.put(FIRST_NAME, user.getFirstName());
			vContext.put("lastname", user.getLastName());
			vContext.put(LAST_NAME, user.getLastName());
			String fullName = userManager.getUserDisplayName(recipient);
			vContext.put("fullname", fullName);
			vContext.put(FULL_NAME, fullName); 
			String email = userManager.getUserDisplayEmail(user, locale);
			vContext.put("mail", email);
			vContext.put(EMAIL, email);
			String loginName = securityManager.findAuthenticationName(recipient);
			if(!StringHelper.containsNonWhitespace(loginName)) {
				loginName = recipient.getName();
			}
			vContext.put(USERNAME, loginName);
			// Put variables from greater context
			if(entry != null) {
				vContext.put(COURSE_URL, url);
				vContext.put("courseurl", url);
				vContext.put(COURSE_NAME, entry.getDisplayname());
				vContext.put("coursename", entry.getDisplayname());
				vContext.put(COURSE_DESCRIPTION, entry.getDescription());
				vContext.put("coursedescription", entry.getDescription());
			}
		}
	}