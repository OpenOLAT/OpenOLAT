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

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.config.CourseConfig;
import org.olat.course.disclaimer.CourseDisclaimerConsent;
import org.olat.course.disclaimer.CourseDisclaimerManager;
import org.olat.course.disclaimer.model.CourseDisclaimerConsentImpl;
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
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		removeAllConsents(identity);
		log.info(Tracing.M_AUDIT, "Course related consents deleted for identity=" + identity.getKey());
	}
}
