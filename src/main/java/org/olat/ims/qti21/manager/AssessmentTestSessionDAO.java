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
package org.olat.ims.qti21.manager;

import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.model.jpa.AssessmentTestSessionImpl;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentTestSessionDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QTI21Storage storage;
	

	public AssessmentTestSession createAndPersistTestSession(RepositoryEntry testEntry,
			RepositoryEntry repositoryEntry, String subIdent,
			AssessmentEntry assessmentEntry, Identity identity,
			boolean authorMode) {
		
		AssessmentTestSessionImpl testSession = new AssessmentTestSessionImpl();
		Date now = new Date();
		testSession.setCreationDate(now);
		testSession.setLastModified(now);
		testSession.setTestEntry(testEntry);
		testSession.setRepositoryEntry(repositoryEntry);
		testSession.setSubIdent(subIdent);
		testSession.setAssessmentEntry(assessmentEntry);
		testSession.setAuthorMode(authorMode);
		testSession.setExploded(false);
		testSession.setIdentity(identity);
		testSession.setStorage(storage.getRelativeDir());
		dbInstance.getCurrentEntityManager().persist(testSession);
		return testSession;
	}
	
	public AssessmentTestSession getLastTestSession(RepositoryEntryRef testEntry,
			RepositoryEntryRef entry, String subIdent, IdentityRef identity) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select session from qtiassessmenttestsession session ")
		  .append("where session.testEntry.key=:testEntryKey and session.identity.key=:identityKey");
		if(entry != null) {
			sb.append(" and session.repositoryEntry.key=:courseEntryKey");
		} else {
			sb.append(" and session.repositoryEntry.key is null");
		}
		
		if(subIdent != null) {
			sb.append(" and session.subIdent=:courseSubIdent");
		} else {
			sb.append(" and session.subIdent is null");
		}
		sb.append(" order by session.creationDate desc");
		
		TypedQuery<AssessmentTestSession> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentTestSession.class)
				.setParameter("testEntryKey", testEntry.getKey())
				.setParameter("identityKey", identity.getKey());
		if(entry != null) {
			query.setParameter("courseEntryKey", entry.getKey());
		}
		if(subIdent != null) {
			query.setParameter("courseSubIdent", subIdent);
		}
		
		List<AssessmentTestSession> lastSessions = query.setMaxResults(1).getResultList();
		return lastSessions == null || lastSessions.isEmpty() ? null : lastSessions.get(0);
	}
	
	public AssessmentTestSession update(AssessmentTestSession testSession) {
		((AssessmentTestSessionImpl)testSession).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(testSession);
	}
	
	public List<AssessmentTestSession> getUserTestSessions(RepositoryEntryRef courseEntry, String courseSubIdent, IdentityRef identity) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadTestSessionsByUserAndRepositoryEntryAndSubIdent", AssessmentTestSession.class)
				.setParameter("repositoryEntryKey", courseEntry.getKey())
				.setParameter("identityKey", identity.getKey())
				.setParameter("subIdent", courseSubIdent)
				.getResultList();
	}
	
	
	public void deleteUserTestSessions(RepositoryEntryRef testEntry) {
		String q = "select session from qtiassessmenttestsession session where session.testEntry.key=:testEntryKey";
		List<AssessmentTestSession> sessions = dbInstance.getCurrentEntityManager()
				.createQuery(q, AssessmentTestSession.class)
				.setParameter("testEntryKey", testEntry.getKey())
				.getResultList();
		for(AssessmentTestSession session:sessions) {
			dbInstance.getCurrentEntityManager().remove(session);
		}
		
	}
}
