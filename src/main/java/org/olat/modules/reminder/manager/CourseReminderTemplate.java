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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailTemplate;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.user.UserManager;

public class CourseReminderTemplate extends MailTemplate {

	private static final String FIRST_NAME = "firstName";
	private static final String LAST_NAME = "lastName";
	private static final String FULL_NAME = "fullName";
	private static final String EMAIL = "email";
	private static final String USERNAME = "username";
	private static final String AFFECTED_USER_FIRST_NAME = "firstNameAffectedUser";
	private static final String AFFECTED_USER_LAST_NAME = "lastNameAffectedUser";
	private static final String AFFECTED_USER_USERNAME = "usernameAffectedUser";
	private static final String AFFECTED_USER_EMAIL = "emailAffectedUser";
	private static final String COURSE_URL = "courseUrl";
	private static final String COURSE_NAME = "courseName";
	private static final String COURSE_DESCRIPTION = "courseDescription";
	private static final String COURSE_REFERENCE = "courseReference";
	private static final String COURSE_TEASER = "courseTeaser";
	private static final String COURSE_OBJECTIVES = "courseObjectives";
	private static final String COURSE_REQUIREMENTS = "courseRequirements";
	private static final String COURSE_CERTIFICATION = "courseCertification";
	private static final String COURSE_AUTHORS = "courseAuthors";
	private static final String COURSE_MAIN_LANGUAGE = "courseMainLang";
	private static final String COURSE_EXPENDITURE_WORK = "courseExpOfWork";
	private static final String COURSE_EXECUTION_PERIOD_START = "courseExecPeriodStart";
	private static final String COURSE_EXECUTION_PERIOD_END = "courseExecPeriodEnd";
	private static final String COURSE_LOCATION = "courseLocation";

	private static final List<String> BODY_VARIABLE_NAMES = List.of(
			USERNAME, EMAIL, FIRST_NAME, LAST_NAME, FULL_NAME, AFFECTED_USER_USERNAME, AFFECTED_USER_EMAIL,
			AFFECTED_USER_FIRST_NAME, AFFECTED_USER_LAST_NAME, COURSE_NAME, COURSE_REFERENCE, COURSE_URL,
			COURSE_EXECUTION_PERIOD_START, COURSE_EXECUTION_PERIOD_END, COURSE_LOCATION, COURSE_TEASER,
			COURSE_DESCRIPTION, COURSE_OBJECTIVES, COURSE_REQUIREMENTS, COURSE_CERTIFICATION, COURSE_AUTHORS,
			COURSE_MAIN_LANGUAGE, COURSE_EXPENDITURE_WORK);
	private static final List<String> SUBJECT_VARIABLE_NAMES = List.of(
			COURSE_NAME, COURSE_EXECUTION_PERIOD_START, COURSE_EXECUTION_PERIOD_END, COURSE_LOCATION, COURSE_AUTHORS);
	private static final Collection<String> ALL_VARIABLE_NAMES = Stream
			.concat(BODY_VARIABLE_NAMES.stream(), SUBJECT_VARIABLE_NAMES.stream()).collect(Collectors.toSet());

	private final String url;
	private final RepositoryEntry entry;
	private Locale locale;
	private final RepositoryEntryLifecycleDAO lifecycleDAO;
	private Identity toRecipient;

	public CourseReminderTemplate(String subjectTemplate, String bodyTemplate, String url, RepositoryEntry entry,
			Locale locale, RepositoryEntryLifecycleDAO lifecycleDAO) {
		super(subjectTemplate, bodyTemplate, null);
		this.url = url;
		this.entry = entry;
		this.locale = locale;
		this.lifecycleDAO = lifecycleDAO;
	}

	public static List<String> bodyVariableNames() {
		return BODY_VARIABLE_NAMES;
	}

	public static List<String> subjectVariableNames() {
		return SUBJECT_VARIABLE_NAMES;
	}

	@Override
	public Collection<String> getVariableNames() {
		return ALL_VARIABLE_NAMES;
	}

	public String getUrl() {
		return url;
	}

	public RepositoryEntry getEntry() {
		return entry;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void setToRecipient(Identity toRecipient) {
		this.toRecipient = toRecipient;
	}

	@Override
	public void putVariablesInMailContext(Identity recipient) {
		UserManager userManager = CoreSpringFactory.getImpl(UserManager.class);
		BaseSecurityManager securityManager = CoreSpringFactory.getImpl(BaseSecurityManager.class);

		if (recipient != null) {
			User user = recipient.getUser();
			putVariablesInMailContext(FIRST_NAME, StringHelper.escapeHtml(user.getFirstName()));
			putVariablesInMailContext(LAST_NAME, StringHelper.escapeHtml(user.getLastName()));
			putVariablesInMailContext(FULL_NAME, StringHelper.escapeHtml(userManager.getUserDisplayName(recipient)));
			
			String email = StringHelper.escapeHtml(userManager.getUserDisplayEmail(user, locale));
			putVariablesInMailContext("mail", email);
			putVariablesInMailContext(EMAIL, email);
			String loginName = securityManager.findAuthenticationName(recipient);
			if (!StringHelper.containsNonWhitespace(loginName)) {
				loginName = recipient.getName();
			}
			putVariablesInMailContext(USERNAME, loginName);
			
			if (toRecipient == null) {
				toRecipient = recipient;
			}
		}
		
		if (toRecipient != null) {
			User toUser = toRecipient.getUser();
			putVariablesInMailContext(AFFECTED_USER_FIRST_NAME, StringHelper.escapeHtml(toUser.getFirstName()));
			putVariablesInMailContext(AFFECTED_USER_LAST_NAME, StringHelper.escapeHtml(toUser.getLastName()));
			// Keep for backwards compatibility
			putVariablesInMailContext("recipientFirstName", StringHelper.escapeHtml(toUser.getFirstName()));
			putVariablesInMailContext("recipientLastName", StringHelper.escapeHtml(toUser.getLastName()));
			
			String loginNameAffectedUser = securityManager.findAuthenticationName(toRecipient);
			if (!StringHelper.containsNonWhitespace(loginNameAffectedUser)) {
				loginNameAffectedUser = toRecipient.getName();
			}
			putVariablesInMailContext(AFFECTED_USER_USERNAME, loginNameAffectedUser);
			
			String emailAffectedUser = StringHelper.escapeHtml(userManager.getUserDisplayEmail(toUser, locale));
			putVariablesInMailContext(AFFECTED_USER_EMAIL, emailAffectedUser);
		}
		
		// Put variables from greater context
		if (entry != null) {
			Formatter formatter = Formatter.getInstance(locale);
			RepositoryEntryLifecycle entryLifecycle = lifecycleDAO.loadByEntry(entry);

			putVariablesInMailContext(COURSE_URL, url);
			putVariablesInMailContext(COURSE_NAME, entry.getDisplayname());
			putVariablesInMailContext(COURSE_DESCRIPTION, entry.getDescription());
			putVariablesInMailContext(COURSE_REFERENCE, entry.getExternalRef());
			putVariablesInMailContext(COURSE_TEASER, entry.getTeaser());
			putVariablesInMailContext(COURSE_OBJECTIVES, entry.getObjectives());
			putVariablesInMailContext(COURSE_REQUIREMENTS, entry.getRequirements());
			putVariablesInMailContext(COURSE_CERTIFICATION, entry.getCredits());
			putVariablesInMailContext(COURSE_AUTHORS, entry.getAuthors());
			putVariablesInMailContext(COURSE_MAIN_LANGUAGE, entry.getMainLanguage());
			putVariablesInMailContext(COURSE_EXPENDITURE_WORK, entry.getExpenditureOfWork());
			if (entryLifecycle != null) {
				putVariablesInMailContext(COURSE_EXECUTION_PERIOD_START, formatter.formatDate(entryLifecycle.getValidFrom()));
				putVariablesInMailContext(COURSE_EXECUTION_PERIOD_END, formatter.formatDate(entryLifecycle.getValidTo()));
			}
			putVariablesInMailContext(COURSE_LOCATION, entry.getLocation());
		}
	}
}