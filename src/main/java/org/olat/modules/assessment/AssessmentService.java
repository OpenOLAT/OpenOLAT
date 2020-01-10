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
package org.olat.modules.assessment;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 22.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface AssessmentService {
	
	/**
	 * 
	 * @param assessedIdentity
	 * @param entry The repository entry where the assessment happens (the course if the test is in a course or
	 * 		same as the reference entry if the test is launched as a standalone test).
	 * @param subIdent An additional reference for the course element
	 * @param entryRoot 
	 * @param referenceEntry The test repository entry 
	 * @return
	 */
	public AssessmentEntry getOrCreateAssessmentEntry(Identity assessedIdentity, String anonymousIdentifier,
			RepositoryEntry entry, String subIdent, Boolean entryRoot, RepositoryEntry referenceEntry);
	
	/**
	 * 
	 * @param assessedIdentity
	 * @param entry
	 * @param subIdent
	 * @return
	 */
	public AssessmentEntry loadAssessmentEntry(Identity assessedIdentity, RepositoryEntry entry, String subIdent);
	
	/**
	 * 
	 * @param assessedIdentity
	 * @param entry
	 * @param subIdent
	 * @param referenceEntry
	 * @return
	 */
	public AssessmentEntry loadAssessmentEntry(Identity assessedIdentity, RepositoryEntryRef entry, String subIdent, RepositoryEntryRef referenceEntry);
	
	public AssessmentEntry updateAssessmentEntry(AssessmentEntry entry);
	
	
	public List<AssessmentEntry> loadAssessmentEntriesBySubIdent(RepositoryEntry entry, String subIdent);
	
	public List<AssessmentEntry> loadAssessmentEntriesBySubIdentWithStatus(RepositoryEntry entry, String subIdent, AssessmentEntryStatus status, boolean excludeZeroScore);
	
	public List<AssessmentEntry> loadAssessmentEntriesByAssessedIdentity(Identity assessedIdentity, RepositoryEntry entry);
	
	public List<AssessmentEntry> loadRootAssessmentEntriesByAssessedIdentity(Identity assessedIdentity, Collection<Long> entryKeys);
	
	public List<AssessmentEntry> loadAssessmentEntries(BusinessGroup assessedGroup, RepositoryEntry entry, String subIdent);

	public List<AssessmentEntryCompletion> loadAvgCompletionsByIdentities(RepositoryEntry entry, Collection<Long> identityKeys);
	
	public List<AssessmentEntryCompletion> loadAvgCompletionsByCurriculumElements(Identity assessedIdentity, Collection<Long> curEleKeys);
	
	public List<AssessmentEntryCompletion> loadAvgCompletionsByIdentities(CurriculumElement curriculumElement, List<Long> identityKeys);
	
	public List<AssessmentEntry> getRootEntriesWithStartOverSubEntries(Date start);

	public void setLastVisit(AssessmentEntry nodeAssessment, Date lastVisit);

	/**
	 * Update the status for a user, create the assessment entries if it doesn't exist.
	 * exist.
	 * @param entryRoot
	 * @param status
	 * @param group
	 * @return
	 */
	public AssessmentEntry updateAssessmentEntry(Identity assessedIdentity, RepositoryEntry entry, String subIdent,
			Boolean entryRoot, RepositoryEntry referenceEntry, AssessmentEntryStatus status);
	
	/**
	 * Update the status for a whole group of users, create the assessment entries if they don't
	 * exist.
	 * @param group
	 * @param entryRoot 
	 * @param status
	 * @return
	 */
	public List<AssessmentEntry> updateAssessmentEntries(BusinessGroup group, RepositoryEntry entry, String subIdent,
			Boolean entryRoot, RepositoryEntry referenceEntry, AssessmentEntryStatus status);

}
