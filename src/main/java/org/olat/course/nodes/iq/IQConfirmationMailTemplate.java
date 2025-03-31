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
package org.olat.course.nodes.iq;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailTemplate;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 20 déc. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IQConfirmationMailTemplate extends MailTemplate {
	
	private static final String FIRST_NAME = "firstName";
	private static final String LAST_NAME = "lastName";
	private static final String FULL_NAME = "fullName";
	private static final String EMAIL = "email";
	private static final String USERNAME = "username";
	private static final String COURSE_URL = "courseUrl";
	private static final String COURSE_NAME = "courseName";
	private static final String COURSE_DESCRIPTION = "courseDescription";
	private static final String COURSE_ELEMENT_NAME = "courseElementName";
	private static final String ATTEMPT = "attempt";
	private static final String SCORE = "score";
	private static final String MAX_SCORE = "maxScore";
	private static final String GRADING = "grading";
	private static final String PASSED = "passed";
	
	private static final Collection<String> VARIABLE_NAMES =
			List.of(FIRST_NAME, LAST_NAME, FULL_NAME, EMAIL, USERNAME,
					COURSE_URL, COURSE_NAME, COURSE_DESCRIPTION, COURSE_ELEMENT_NAME,
					ATTEMPT, SCORE, MAX_SCORE, GRADING, PASSED);
	
	private final String url;
	private final Locale locale;
	private final Translator translator;
	private final RepositoryEntry entry;
	private final Identity assessedIdentity;
	private final IQTESTCourseNode courseNode;
	private final AssessmentConfig assessmentConfig;
	private final UserCourseEnvironment assessedUserCourseEnv;
	
	public IQConfirmationMailTemplate(String subjectTemplate, String bodyTemplate, String url,
			RepositoryEntry entry, IQTESTCourseNode courseNode, UserCourseEnvironment assessedUserCourseEnv,
			AssessmentConfig assessmentConfig, Translator translator, Locale locale) {
		super(subjectTemplate, bodyTemplate, null);
		this.url = url;
		this.entry = entry;
		this.locale = locale;
		this.translator = translator;
		this.courseNode = courseNode;
		this.assessmentConfig = assessmentConfig;
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
	}
	
	public static final Collection<String> variableNames() {
		return VARIABLE_NAMES;
	}
	
	@Override
	public void putVariablesInMailContext(Identity recipient) {
		if(assessedIdentity != null) {
			UserManager userManager = CoreSpringFactory.getImpl(UserManager.class);
			BaseSecurityManager securityManager = CoreSpringFactory.getImpl(BaseSecurityManager.class);
			
			User user = assessedIdentity.getUser();
			String firstName = StringHelper.escapeHtml(user.getFirstName());
			String lastName = StringHelper.escapeHtml( user.getLastName());
			putVariablesInMailContext(FIRST_NAME, firstName);
			putVariablesInMailContext(LAST_NAME, lastName);
			putVariablesInMailContext(FIRST_NAME, firstName);
			putVariablesInMailContext(FIRST_NAME, firstName);
			String fullName = StringHelper.escapeHtml(userManager.getUserDisplayName(assessedIdentity));
			putVariablesInMailContext(FULL_NAME, fullName);
			String email = StringHelper.escapeHtml(userManager.getUserDisplayEmail(user, locale));
			putVariablesInMailContext(EMAIL, email);
			String loginName = securityManager.findAuthenticationName(recipient);
			putVariablesInMailContext(USERNAME, loginName);
		}
		
		if(entry != null) {
			putVariablesInMailContext(COURSE_URL, url);
			putVariablesInMailContext(COURSE_NAME, entry.getDisplayname());
			putVariablesInMailContext(COURSE_DESCRIPTION, entry.getDescription());
		}
		
		if(courseNode != null) {
			putVariablesInMailContext(COURSE_ELEMENT_NAME, courseNode.getShortName());

			CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
			AssessmentEvaluation assessmentEval = courseAssessmentService.getAssessmentEvaluation(courseNode, assessedUserCourseEnv);
			boolean passed = assessmentEval.getPassed() != null && assessmentEval.getPassed().booleanValue();
			boolean resultsVisible = assessmentEval.getUserVisible() != null && assessmentEval.getUserVisible().booleanValue();
			
			if(assessmentConfig.hasAttempts() && assessmentEval.getAttempts() != null) {
				putVariablesInMailContext(ATTEMPT, assessmentEval.getAttempts().toString());
			} else {
				putVariablesInMailContext(ATTEMPT, "-");
			}
			
			Float maxScore = assessmentConfig.getMaxScore();
			if(maxScore != null) {
				putVariablesInMailContext(MAX_SCORE, AssessmentHelper.getRoundedScore(maxScore));	
			} else {
				putVariablesInMailContext(MAX_SCORE, "");
			}

			// Set defaults value
			putVariablesInMailContext(SCORE, "");
			putVariablesInMailContext(PASSED, "");
			putVariablesInMailContext(GRADING, "");
			
			if(resultsVisible) {
				if(assessmentEval.getScore() != null) {
					putVariablesInMailContext(SCORE, AssessmentHelper.getRoundedScore(assessmentEval.getScore()));
				}

				if(assessmentEval.getPassed() != null) {
					String i18nPassed = passed ? "passed.yes" : "passed.no"; 
					putVariablesInMailContext(PASSED, translator.translate(i18nPassed));
				}
				
				if(assessmentConfig.hasGrade() && assessmentEval.getGrade() != null) {
					String grade = GradeUIFactory.translatePerformanceClass(translator,
							assessmentEval.getPerformanceClassIdent(), assessmentEval.getGrade(), assessmentEval.getGradeSystemIdent());
					putVariablesInMailContext(GRADING, grade);
				}
			}
		}
	}
}
