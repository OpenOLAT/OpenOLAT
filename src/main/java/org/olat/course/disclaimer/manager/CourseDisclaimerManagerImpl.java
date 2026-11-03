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
package org.olat.course.disclaimer.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigLockedException;
import org.olat.course.disclaimer.ConsentHandling;
import org.olat.course.disclaimer.CourseDisclaimerConsent;
import org.olat.course.disclaimer.CourseDisclaimerManager;
import org.olat.course.disclaimer.model.CourseDisclaimerConsentImpl;
import org.olat.course.disclaimer.model.CourseDisclaimerError;
import org.olat.course.disclaimer.model.CourseDisclaimerSettings;
import org.olat.course.disclaimer.model.CourseDisclaimerSettings.Disclaimer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/* 
 * Date: 17 Mar 2020<br>
 * @author Alexander Boeckle
 */
@Service
public class CourseDisclaimerManagerImpl implements CourseDisclaimerManager, UserDataDeletable {
	
	private static final Logger log = Tracing.createLoggerFor(CourseDisclaimerConsentImpl.class);
	
	private static final int TITLE_MAX_LENGTH = 255;
	private static final int TERMS_MAX_LENGTH = 30000;
	private static final int LABEL_MAX_LENGTH = 255;
	
	@Autowired
	private CourseDisclaimerDAO courseDisclaimerDAO;
	
	@Autowired
	private DB dbInstance;

	@Override
	public void revokeAllConsents(RepositoryEntryRef repositoryEntryRef) {
		courseDisclaimerDAO.revokeAllConsents(repositoryEntryRef);
	}
	
	@Override
	public void removeAllConsents(RepositoryEntryRef repositoryEntryRef) {
		courseDisclaimerDAO.removeAllConsents(repositoryEntryRef);
	}

	@Override
	public List<CourseDisclaimerConsent> getConsents(RepositoryEntryRef repositoryEntryRef) {
		return courseDisclaimerDAO.getConsents(repositoryEntryRef);
	}
	
	@Override
	public CourseDisclaimerConsent getConsent(RepositoryEntryRef repositoryEntryRef, Identity identity) {
		return courseDisclaimerDAO.getCourseDisclaimerConsent(repositoryEntryRef, identity);
	}


	@Override
	public void acceptDisclaimer(RepositoryEntry repositoryEntry, Identity identitiy, Roles roles, boolean disc1Accepted, boolean disc2Accepted) {
		if (roles.isGuestOnly()) {
			return;
		}

		CourseDisclaimerConsent consent = courseDisclaimerDAO.getCourseDisclaimerConsent(repositoryEntry, identitiy);

		if (consent != null) {
			consent.setDisc1(disc1Accepted);
			consent.setDisc2(disc2Accepted);
			dbInstance.getCurrentEntityManager().merge(consent);
		} else {
			consent = new CourseDisclaimerConsentImpl();
			consent.setDisc1(disc1Accepted);
			consent.setDisc2(disc2Accepted);
			consent.setIdentity(identitiy);
			consent.setRepositoryEntry(repositoryEntry);
			dbInstance.getCurrentEntityManager().persist(consent);
		}
	}

	@Override
	public boolean isAccessGranted(RepositoryEntry repositoryEntry, IdentityRef identitiyRef, Roles roles) {
		CourseConfig courseConfig = CourseFactory.loadCourse(repositoryEntry.getOlatResource().getResourceableId()).getCourseConfig();
		boolean accessGranted = true;
		
		if (courseConfig.isDisclaimerEnabled()) {
			CourseDisclaimerConsent consent = courseDisclaimerDAO.getCourseDisclaimerConsent(repositoryEntry, identitiyRef);

			if (courseConfig.isDisclaimerEnabled(1)) {
				if (consent != null) {
					accessGranted &= consent.isDisc1Accepted();
				} else {
					accessGranted &= false;
				}
			}
			if (courseConfig.isDisclaimerEnabled(2)) {
				if (consent != null) {
					accessGranted &= consent.isDisc2Accepted();
				} else {
					accessGranted &= false;
				}
			}
			if (roles.isGuestOnly()) {
				accessGranted &= false;
			}
		}

		return accessGranted;
	}

	@Override
	public void removeConsents(RepositoryEntryRef repositoryEntryRef, List<Long> identityKeys) {
		courseDisclaimerDAO.removeConsents(repositoryEntryRef, identityKeys);
	}

	@Override
	public void revokeConsents(RepositoryEntryRef repositoryEntryRef, List<Long> identityKeys) {
		courseDisclaimerDAO.revokeConsents(repositoryEntryRef, identityKeys);
	}
	
	@Override
	public boolean hasAnyConsent(RepositoryEntryRef repositoryEntryRef) {
		List<CourseDisclaimerConsent> consents = getConsents(repositoryEntryRef);
		
		if (consents.isEmpty()) {
			return false;
		} else {
			for (CourseDisclaimerConsent courseDisclaimerConsent : consents) {
				if (courseDisclaimerConsent.getConsentDate() != null) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override 
	public Long countConsents(RepositoryEntryRef repositoryEntryRef) {
		return courseDisclaimerDAO.countConsents(repositoryEntryRef);
	}
	
	@Override
	public boolean hasAnyEntry(RepositoryEntryRef repositoryEntryRef) {
		return !getConsents(repositoryEntryRef).isEmpty();
	}
	
	@Override
	public void removeAllConsents(IdentityRef identityRef) {
		courseDisclaimerDAO.removeAllConsents(identityRef);
	}

	@Override
	public CourseDisclaimerSettings getSettings(RepositoryEntry courseEntry) {
		ICourse course = CourseFactory.loadCourse(courseEntry);
		return getSettings(course);
	}

	@Override
	public CourseDisclaimerSettings getSettings(ICourse course) {
		CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
		return new CourseDisclaimerSettings(getDisclaimer(courseConfig, 1), getDisclaimer(courseConfig, 2));
	}

	private Disclaimer getDisclaimer(CourseConfig courseConfig, int disclaimer) {
		return new Disclaimer(courseConfig.isDisclaimerEnabled(disclaimer),
				courseConfig.getDisclaimerTitel(disclaimer), courseConfig.getDisclaimerTerms(disclaimer),
				courseConfig.getDisclaimerLabel(disclaimer, 1), courseConfig.getDisclaimerLabel(disclaimer, 2));
	}
	
	@Override
	public List<CourseDisclaimerError> validate(CourseDisclaimerSettings settings, ConsentHandling consentHandling) {
		List<CourseDisclaimerError> errors = new ArrayList<>();
		validate(1, settings.getTermsOfUse(), errors);
		validate(2, settings.getDataProtection(), errors);
		if(consentHandling == ConsentHandling.REMOVE && settings.isAnyEnabled()) {
			errors.add(new CourseDisclaimerError(0, "consents", CourseDisclaimerError.Type.REMOVE_NOT_ALLOWED, -1));
		}
		return errors;
	}
	
	private void validate(int disclaimerNumber, Disclaimer disclaimer, List<CourseDisclaimerError> errors) {
		if(disclaimer != null && disclaimer.isEnabled()) {
			validateText(disclaimerNumber, "title", disclaimer.getTitle(), true, TITLE_MAX_LENGTH, errors);
			// Same as the form: the length of the filtered terms is checked
			validateText(disclaimerNumber, "terms", filterTerms(disclaimer.getTerms()), true, TERMS_MAX_LENGTH, errors);
			validateText(disclaimerNumber, "label1", disclaimer.getLabel1(), true, LABEL_MAX_LENGTH, errors);
			validateText(disclaimerNumber, "label2", disclaimer.getLabel2(), false, LABEL_MAX_LENGTH, errors);
		}
	}
	
	private void validateText(int disclaimer, String field, String value, boolean mandatory, int maxLength, List<CourseDisclaimerError> errors) {
		if(!StringHelper.containsNonWhitespace(value)) {
			if(mandatory) {
				errors.add(new CourseDisclaimerError(disclaimer, field, CourseDisclaimerError.Type.MANDATORY, -1));
			}
		} else if(value.length() > maxLength) {
			errors.add(new CourseDisclaimerError(disclaimer, field, CourseDisclaimerError.Type.TOO_LONG, maxLength));
		}
	}
	
	@Override
	public CourseDisclaimerSettings updateSettings(RepositoryEntry courseEntry, CourseDisclaimerSettings settings,
			ConsentHandling consentHandling, Identity doer)
	throws CourseConfigLockedException {
		Long courseResId = courseEntry.getOlatResource().getResourceableId();
		if(CourseFactory.isCourseEditSessionOpen(courseResId)) {
			throw new CourseConfigLockedException(courseResId);
		}
	
		ICourse course;
		try {
			course = CourseFactory.openCourseEditSession(courseResId);
		} catch (AssertException e) {
			// opened in the meantime
			throw new CourseConfigLockedException(courseResId);
		}
	
		try {
			CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
			updateDisclaimer(courseConfig, 1, settings.getTermsOfUse());
			updateDisclaimer(courseConfig, 2, settings.getDataProtection());
			CourseFactory.setCourseConfig(courseResId, courseConfig);
			CourseFactory.saveCourse(courseResId);
		} finally {
			CourseFactory.closeCourseEditSession(courseResId, true);
		}
	
		if(consentHandling == ConsentHandling.REVOKE) {
			revokeAllConsents(courseEntry);
		} else if(consentHandling == ConsentHandling.REMOVE) {
			removeAllConsents(courseEntry);
		}
	
		log.info(Tracing.M_AUDIT, "Course disclaimer updated for course={} by identity={} (terms of use enabled={}, data protection enabled={}, consents={})",
				courseEntry.getKey(), (doer == null ? null : doer.getKey()),
				isEnabled(settings.getTermsOfUse()), isEnabled(settings.getDataProtection()), consentHandling);
		return getSettings(courseEntry);
	}
	
	private boolean isEnabled(Disclaimer disclaimer) {
		return disclaimer != null && disclaimer.isEnabled();
	}
	
	/**
	 * Same behavior as the course settings form: the texts of a disabled
	 * disclaimer are not changed.
	 */
	private void updateDisclaimer(CourseConfig courseConfig, int disclaimerNumber, Disclaimer disclaimer) {
		if(disclaimer == null) {
			return;
		}
	
		courseConfig.setDisclaimerEnabled(disclaimerNumber, disclaimer.isEnabled());
		if(disclaimer.isEnabled()) {
			String terms = filterTerms(disclaimer.getTerms());
			courseConfig.setDisclaimerTitle(disclaimerNumber, valueOrEmpty(disclaimer.getTitle()));
			courseConfig.setDisclaimerTerms(disclaimerNumber, valueOrEmpty(terms));
			courseConfig.setDisclaimerLabel(disclaimerNumber, 1, valueOrEmpty(disclaimer.getLabel1()));
			courseConfig.setDisclaimerLabel(disclaimerNumber, 2, valueOrEmpty(disclaimer.getLabel2()));
		}
	}
	
	/**
	 * The XSS filter is stateful, use a new instance for every call.
	 */
	private String filterTerms(String terms) {
		return FilterFactory.getXSSFilter().filter(terms);
	}
	
	private String valueOrEmpty(String value) {
		return value == null ? "" : value;
	}
	
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		removeAllConsents(identity);
		log.info(Tracing.M_AUDIT, "Course related consents deleted for identity=" + identity.getKey());
	}
}
