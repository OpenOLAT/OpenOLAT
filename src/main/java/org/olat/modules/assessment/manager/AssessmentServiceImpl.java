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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentEntryCompletion;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
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
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentEntryDAO assessmentEntryDao;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	
	public AssessmentEntry createAssessmentEntry(Identity assessedIdentity, String anonymousIdentifier,
			RepositoryEntry entry, String subIdent, RepositoryEntry referenceEntry,
			Float score, Boolean passed, Date lastUserModified, Date lastCoachModified) {
		return assessmentEntryDao.createAssessmentEntry(assessedIdentity, anonymousIdentifier, entry, subIdent, null,
				referenceEntry, score, passed, lastUserModified, lastCoachModified);
	}

	@Override
	public AssessmentEntry getOrCreateAssessmentEntry(Identity assessedIdentity, String anonymousIdentifier,
			RepositoryEntry entry, String subIdent, Boolean entryRoot, RepositoryEntry referenceEntry) {
		
		AssessmentEntry assessmentEntry = assessmentEntryDao.loadAssessmentEntry(assessedIdentity, anonymousIdentifier, entry, subIdent);
		if(assessmentEntry == null) {
			assessmentEntry = assessmentEntryDao.createAssessmentEntry(assessedIdentity, anonymousIdentifier, entry, subIdent, entryRoot, referenceEntry);
			dbInstance.commit();
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
	public List<AssessmentEntryCompletion> loadEntryRootCompletions(Identity assessedIdentity, Collection<Long> entryKeys) {
		return assessmentEntryDao.loadEntryRootCompletions(assessedIdentity, entryKeys);
	}

	@Override
	public void setLastVisit(AssessmentEntry nodeAssessment, Date lastVisit) {
		assessmentEntryDao.setLastVisit(nodeAssessment, lastVisit);
	}

	@Override
	public AssessmentEntry updateAssessmentEntry(Identity assessedIdentity, RepositoryEntry entry, String subIdent,
			Boolean entryRoot, RepositoryEntry referenceEntry, AssessmentEntryStatus status) {
		AssessmentEntry assessmentEntry = getOrCreateAssessmentEntry(assessedIdentity, null, entry, subIdent, entryRoot, referenceEntry);
		assessmentEntry.setAssessmentStatus(status);
		return assessmentEntryDao.updateAssessmentEntry(assessmentEntry);
	}

	@Override
	public List<AssessmentEntry> updateAssessmentEntries(BusinessGroup group, RepositoryEntry entry, String subIdent,
			Boolean entryRoot, RepositoryEntry referenceEntry, AssessmentEntryStatus status) {
		List<AssessmentEntry> assessmentEntries = new ArrayList<>();
		List<Identity> groupParticipants = businessGroupRelationDao.getMembers(group, GroupRoles.participant.name());
		for(Identity groupParticipant:groupParticipants) {
			AssessmentEntry assessmentEntry = getOrCreateAssessmentEntry(groupParticipant, null, entry, subIdent, entryRoot, referenceEntry);
			assessmentEntry.setAssessmentStatus(status);
			assessmentEntry = assessmentEntryDao.updateAssessmentEntry(assessmentEntry);
			assessmentEntries.add(assessmentEntry);
		}
		
		return assessmentEntries;
	}

	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		assessmentEntryDao.deleteEntryForIdentity(identity);
	}
}
