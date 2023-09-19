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

	private static final Collection<String> BODY_VARIABLE_NAMES =
			List.of(FIRST_NAME, LAST_NAME, FULL_NAME, EMAIL, USERNAME, COURSE_URL, COURSE_NAME, COURSE_DESCRIPTION, COURSE_REFERENCE,
					COURSE_TEASER, COURSE_OBJECTIVES, COURSE_REQUIREMENTS, COURSE_CERTIFICATION, COURSE_AUTHORS, COURSE_MAIN_LANGUAGE,
					COURSE_EXPENDITURE_WORK, COURSE_EXECUTION_PERIOD_START, COURSE_EXECUTION_PERIOD_END, COURSE_LOCATION);
	private static final Collection<String> SUBJECT_VARIABLE_NAMES =
			List.of(COURSE_NAME, COURSE_EXECUTION_PERIOD_START, COURSE_EXECUTION_PERIOD_END, COURSE_AUTHORS, COURSE_LOCATION);

	private final String url;
	private final RepositoryEntry entry;
	private final Locale locale;
	private final RepositoryEntryLifecycleDAO lifecycleDAO;

	public CourseReminderTemplate(String subjectTemplate, String bodyTemplate, String url, RepositoryEntry entry,
								  Locale locale, RepositoryEntryLifecycleDAO lifecycleDAO) {
		super(subjectTemplate, bodyTemplate, null);
		this.url = url;
		this.entry = entry;
		this.locale = locale;
		this.lifecycleDAO = lifecycleDAO;
	}

	public static Collection<String> bodyVariableNames() {
		return BODY_VARIABLE_NAMES;
	}

	public static Collection<String> subjectVariableNames() {
		return SUBJECT_VARIABLE_NAMES;
	}

	@Override
	public void putVariablesInMailContext(VelocityContext vContext, Identity recipient) {
		UserManager userManager = CoreSpringFactory.getImpl(UserManager.class);
		BaseSecurityManager securityManager = CoreSpringFactory.getImpl(BaseSecurityManager.class);

		User user = recipient.getUser();
		String firstName = StringHelper.escapeHtml(user.getFirstName());
		vContext.put(FIRST_NAME.toLowerCase(), firstName);
		vContext.put(FIRST_NAME, firstName);
		String lastName = StringHelper.escapeHtml(user.getLastName());
		vContext.put(LAST_NAME.toLowerCase(), lastName);
		vContext.put(LAST_NAME, lastName);
		String fullName = StringHelper.escapeHtml(userManager.getUserDisplayName(recipient));
		vContext.put(FULL_NAME.toLowerCase(), fullName);
		vContext.put(FULL_NAME, fullName);
		String email = StringHelper.escapeHtml(userManager.getUserDisplayEmail(user, locale));
		vContext.put("mail", email);
		vContext.put(EMAIL, email);
		String loginName = securityManager.findAuthenticationName(recipient);
		if (!StringHelper.containsNonWhitespace(loginName)) {
			loginName = recipient.getName();
		}
		vContext.put(USERNAME, loginName);
		// Put variables from greater context
		if (entry != null) {
			Formatter formatter = Formatter.getInstance(locale);
			RepositoryEntryLifecycle entryLifecycle = lifecycleDAO.loadByEntry(entry);

			vContext.put(COURSE_URL, url);
			vContext.put(COURSE_URL.toLowerCase(), url);
			vContext.put(COURSE_NAME, entry.getDisplayname());
			vContext.put(COURSE_NAME.toLowerCase(), entry.getDisplayname());
			vContext.put(COURSE_DESCRIPTION, entry.getDescription());
			vContext.put(COURSE_DESCRIPTION.toLowerCase(), entry.getDescription());
			vContext.put(COURSE_REFERENCE, entry.getExternalRef());
			vContext.put(COURSE_REFERENCE.toLowerCase(), entry.getExternalRef());
			vContext.put(COURSE_TEASER, entry.getTeaser());
			vContext.put(COURSE_TEASER.toLowerCase(), entry.getTeaser());
			vContext.put(COURSE_OBJECTIVES, entry.getObjectives());
			vContext.put(COURSE_OBJECTIVES.toLowerCase(), entry.getObjectives());
			vContext.put(COURSE_REQUIREMENTS, entry.getRequirements());
			vContext.put(COURSE_REQUIREMENTS.toLowerCase(), entry.getRequirements());
			vContext.put(COURSE_CERTIFICATION, entry.getCredits());
			vContext.put(COURSE_CERTIFICATION.toLowerCase(), entry.getCredits());
			vContext.put(COURSE_AUTHORS, entry.getAuthors());
			vContext.put(COURSE_AUTHORS.toLowerCase(), entry.getAuthors());
			vContext.put(COURSE_MAIN_LANGUAGE, entry.getMainLanguage());
			vContext.put(COURSE_MAIN_LANGUAGE.toLowerCase(), entry.getMainLanguage());
			vContext.put(COURSE_EXPENDITURE_WORK, entry.getExpenditureOfWork());
			vContext.put(COURSE_EXPENDITURE_WORK.toLowerCase(), entry.getExpenditureOfWork());
			if (entryLifecycle != null) {
				vContext.put(COURSE_EXECUTION_PERIOD_START, formatter.formatDateAndTime(entryLifecycle.getValidFrom()));
				vContext.put(COURSE_EXECUTION_PERIOD_START.toLowerCase(), formatter.formatDateAndTime(entryLifecycle.getValidFrom()));
				vContext.put(COURSE_EXECUTION_PERIOD_END, formatter.formatDateAndTime(entryLifecycle.getValidTo()));
				vContext.put(COURSE_EXECUTION_PERIOD_END.toLowerCase(), formatter.formatDateAndTime(entryLifecycle.getValidTo()));
			}
			vContext.put(COURSE_LOCATION, entry.getLocation());
			vContext.put(COURSE_LOCATION.toLowerCase(), entry.getLocation());
		}
	}
}