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
package org.olat.modules.assessment.manager;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.PersistenceException;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.id.Identity;
import org.olat.core.logging.DBRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentEntryCompletion;
import org.olat.modules.assessment.AssessmentEntryScoring;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentServiceImpl implements AssessmentService, UserDataDeletable {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentServiceImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentEntryDAO assessmentEntryDao;

	@Override
	public AssessmentEntry getOrCreateAssessmentEntry(Identity assessedIdentity, String anonymousIdentifier,
			RepositoryEntry entry, String subIdent, Boolean entryRoot, RepositoryEntry referenceEntry) {
		AssessmentEntry assessmentEntry = assessmentEntryDao.loadAssessmentEntry(assessedIdentity, anonymousIdentifier, entry, subIdent);
		if(assessmentEntry == null) {
			try {
				dbInstance.commit();
				assessmentEntry = assessmentEntryDao.createAssessmentEntry(assessedIdentity, anonymousIdentifier, entry, subIdent, entryRoot, referenceEntry);
				dbInstance.commit();
			} catch(PersistenceException | DBRuntimeException e) {
				if(PersistenceHelper.isConstraintViolationException(e)) {
					log.warn("", e);
					dbInstance.rollback();
					assessmentEntry = assessmentEntryDao.loadAssessmentEntry(assessedIdentity, anonymousIdentifier, entry, subIdent);
				} else {
					log.error("", e);
				}
			}
		}
		return assessmentEntry;
	}

	@Override
	public AssessmentEntry loadAssessmentEntry(Identity assessedIdentity, RepositoryEntry entry, String subIdent) {
		if(assessedIdentity == null || entry == null) return null;
		return assessmentEntryDao.loadAssessmentEntry(assessedIdentity, null, entry, subIdent);
	}

	@Override
	public AssessmentEntry loadAssessmentEntry(Identity assessedIdentity, RepositoryEntryRef entry, String subIdent, RepositoryEntryRef referenceEntry) {
		if(assessedIdentity == null || entry == null || referenceEntry == null) return null;
		return assessmentEntryDao.loadAssessmentEntry(assessedIdentity, entry, subIdent, referenceEntry);
	}

	@Override
	public AssessmentEntry updateAssessmentEntry(AssessmentEntry entry) {
		return assessmentEntryDao.updateAssessmentEntry(entry);
	}
	
	@Override
	public void resetAllRootPassed(RepositoryEntry entry) {
		assessmentEntryDao.resetAllRootPassed(entry);
		dbInstance.commit();
	}
	
	@Override
	public void resetAllOverridenRootPassed(RepositoryEntry entry) {
		assessmentEntryDao.resetAllOverridenRootPassed(entry);
		dbInstance.commit();
	}

	@Override
	public List<AssessmentEntry> loadAssessmentEntriesBySubIdent(RepositoryEntry entry, String subIdent) {
		return assessmentEntryDao.loadAssessmentEntryBySubIdent(entry, subIdent);
	}
	
	@Override
	public List<AssessmentEntry> loadAssessmentEntriesBySubIdentWithStatus(RepositoryEntry entry, String subIdent,
			AssessmentEntryStatus status, boolean excludeZeroScore) {
		return assessmentEntryDao.loadAssessmentEntryBySubIdentWithStatus(entry, subIdent, status, excludeZeroScore);
	}

	@Override
	public List<AssessmentEntry> loadAssessmentEntriesByAssessedIdentity(Identity assessedIdentity, RepositoryEntry entry) {
		return assessmentEntryDao.loadAssessmentEntriesByAssessedIdentity(assessedIdentity, entry);
	}

	@Override
	public List<AssessmentEntry> loadAssessmentEntries(BusinessGroup assessedGroup, RepositoryEntry entry, String subIdent) {
		return assessmentEntryDao.loadAssessmentEntryByGroup(assessedGroup.getBaseGroup(), entry, subIdent);
	}

	@Override
	public List<AssessmentEntryScoring> loadRootAssessmentEntriesByAssessedIdentity(Identity assessedIdentity, Collection<Long> entryKeys) {
		return assessmentEntryDao.loadRootAssessmentEntriesByAssessedIdentity(assessedIdentity, entryKeys);
	}
	
	@Override
	public List<AssessmentEntryCompletion> loadAvgCompletionsByIdentities(RepositoryEntry entry, Collection<Long> identityKeys) {
		return assessmentEntryDao.loadAvgCompletionsByIdentities(entry, identityKeys);
	}

	@Override
	public List<AssessmentEntryCompletion> loadAvgCompletionsByCurriculumElements(Identity assessedIdentity,
			Collection<Long> curEleKeys) {
		return assessmentEntryDao.loadAvgCompletionsByCurriculumElements(assessedIdentity, curEleKeys);
	}

	@Override
	public List<AssessmentEntryCompletion> loadAvgCompletionsByIdentities(CurriculumElement curriculumElement,
			List<Long> identityKeys) {
		return assessmentEntryDao.loadAvgCompletionsByIdentities(curriculumElement, identityKeys);
	}
	
	@Override
	public boolean hasAssessmentEntry(IdentityRef assessedIdentity, RepositoryEntryRef entry) {
		return assessmentEntryDao.hasAssessmentEntry(assessedIdentity, entry);
	}
	
	@Override
	public boolean hasGrades(RepositoryEntryRef remositoryEntry, String subIdent) {
		return assessmentEntryDao.hasGrades(remositoryEntry, subIdent);
	}
	
	@Override
	public Long getGradeCount(RepositoryEntryRef remositoryEntry, String subIdent) {
		return assessmentEntryDao.getGradeCount(remositoryEntry, subIdent);
	}

	@Override
	public List<AssessmentEntry> getRootEntriesWithStartOverSubEntries(Date start) {
		return assessmentEntryDao.getRootEntriesWithStartOverSubEntries(start);
	}

	@Override
	public void setLastVisit(AssessmentEntry nodeAssessment, Date lastVisit) {
		assessmentEntryDao.setLastVisit(nodeAssessment, lastVisit);
	}

	@Override
	public List<Long> getIdentityKeys(RepositoryEntry entry, String subIdent, Collection<AssessmentObligation> obligations) {
		return assessmentEntryDao.loadIdentityKeys(entry, subIdent, obligations);
	}

	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		assessmentEntryDao.deleteEntryForIdentity(identity);
	}

}
