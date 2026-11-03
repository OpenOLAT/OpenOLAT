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
package org.olat.course.disclaimer;

import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfigLockedException;
import org.olat.course.disclaimer.model.CourseDisclaimerError;
import org.olat.course.disclaimer.model.CourseDisclaimerSettings;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/* 
 * Date: 24 Mar 2020<br>
 * @author Alexander Boeckle
 */
public interface CourseDisclaimerManager {
	/**
	 * Revokes all consents related to disclaimer of a repository entry
	 * 
	 * @param repositoryEntryRef
	 */
	public void revokeAllConsents(RepositoryEntryRef repositoryEntryRef);
	
	/**
	 * Deletes all consents related to a disclaimer of repository entry
	 * 
	 * @param repositoryEntryRef
	 */
	public void removeAllConsents(RepositoryEntryRef repositoryEntryRef);
	
	/**
	 * Accepts a disclaimer for repository entry
	 * 
	 * @param repositoryEntry
	 * @param identity
	 * @param disc1Accepted
	 * @param disc2Accepted
	 */
	public void acceptDisclaimer(RepositoryEntry repositoryEntry, Identity identity, Roles roles, boolean disc1Accepted, boolean disc2Accepted);
	
	/**
	 * Deletes all disclaimer consent database entries related to a repository entry and a list of users
	 * 
	 * @param repositoryEntryRef
	 * @param identityKeys
	 */
	public void removeConsents(RepositoryEntryRef repositoryEntryRef, List<Long> identityKeys);
	
	/**
	 * Revokes all disclaimer consents related to a repository entry and a list of users
	 * 
	 * @param repositoryEntryRef
	 * @param identityKeys
	 */
	public void revokeConsents(RepositoryEntryRef repositoryEntryRef, List<Long> identityKeys);
	
	/**
	 * Returns all consents related to a repository entry
	 * 
	 * @param repositoryEntryRef
	 * @return List<CourseDisclaimerConsent>
	 */
	public List<CourseDisclaimerConsent> getConsents(RepositoryEntryRef repositoryEntryRef);
	
	
	/**
	 * Returns the consent for a specific identity related to a repository entry or NULL if not available
	 * 
	 * @param repositoryEntryRef
	 * @param identity
	 * @return CourseDisclaimerConsent
	 */
	public CourseDisclaimerConsent getConsent(RepositoryEntryRef repositoryEntryRef, Identity identity);

	/**
	 * Returns whether access to a repository entry is granted or not to a user
	 * 
	 * @param repositoryEntry
	 * @param identityRef
	 * @return boolean
	 */
	public boolean isAccessGranted(RepositoryEntry repositoryEntry, IdentityRef identityRef, Roles roles);
	
	/**
	 * Returns whether any disclaimer has been accepted yet 
	 * 
	 * @param repositoryEntryRef
	 * @return boolean
	 */
	public boolean hasAnyConsent(RepositoryEntryRef repositoryEntryRef);
	
	/**
	 * Returns the amount of consents to a repository entry
	 * 
	 * @param repositoryEntryRef
	 * @return Long
	 */
	public Long countConsents(RepositoryEntryRef repositoryEntryRef);
	
	/**
	 * Returns whether any entry in the database for this repository entry is existing
	 * 
	 * @param repositoryEntryRef
	 * @return boolean
	 */
	public boolean hasAnyEntry(RepositoryEntryRef repositoryEntryRef);

	/**
	 * Removes all consents from the database related to an identity 
	 * 
	 * @param identityRef
	 */
	public void removeAllConsents(IdentityRef identityRef);
	
	/**
	 * Read the disclaimer configuration of the course.
	 *
	 * @param courseEntry The repository entry of the course
	 * @return The settings of both disclaimers
	 */
	public CourseDisclaimerSettings getSettings(RepositoryEntry courseEntry);
	
	/**
	 * Read the disclaimer configuration of the course.
	 *
	 * @param course The loaded course
	 * @return The settings of both disclaimers
	 */
	public CourseDisclaimerSettings getSettings(ICourse course);
	
	/**
	 * Validate the settings with the same rules as the course settings form:
	 * for an enabled disclaimer, the title (max. 255 characters), the terms
	 * (max. 30'000 characters after the XSS filter) and the first label
	 * (max. 255 characters) are mandatory, the second label is optional
	 * (max. 255 characters). Removing the
	 * consents is only allowed if both disclaimers are disabled.
	 *
	 * @param settings The settings to validate
	 * @param consentHandling What happens with the existing consents
	 * @return A list of errors, empty if the settings are valid
	 */
	public List<CourseDisclaimerError> validate(CourseDisclaimerSettings settings, ConsentHandling consentHandling);
	
	/**
	 * Save the disclaimer configuration of the course and apply the consent handling.
	 * As in the course settings form, the texts of a disabled disclaimer are not
	 * changed. The terms are filtered with the XSS filter. The settings must be
	 * validated first.
	 *
	 * @param courseEntry The repository entry of the course
	 * @param settings The new settings
	 * @param consentHandling What happens with the existing consents
	 * @param doer The identity which makes the change
	 * @return The saved settings
	 * @throws CourseConfigLockedException If the course is opened in the editor
	 */
	public CourseDisclaimerSettings updateSettings(RepositoryEntry courseEntry, CourseDisclaimerSettings settings,
			ConsentHandling consentHandling, Identity doer) throws CourseConfigLockedException;
}
