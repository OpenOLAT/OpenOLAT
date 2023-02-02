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
package org.olat.modules.video.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.video.VideoTaskSession;
import org.olat.modules.video.model.VideoTaskSessionImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class VideoTaskSessionDAO {
	
	@Autowired
	private DB dbInstance;
	
	
	public VideoTaskSession createAndPersistTaskSession(RepositoryEntry videoEntry,
			RepositoryEntry repositoryEntry, String subIdent,
			AssessmentEntry assessmentEntry, Identity identity, String anonymousIdentifier,
			long attempt, boolean authorMode) {
		
		VideoTaskSessionImpl testSession = new VideoTaskSessionImpl();
		Date now = new Date();
		testSession.setCreationDate(now);
		testSession.setLastModified(now);
		testSession.setVideoEntry(videoEntry);
		testSession.setRepositoryEntry(repositoryEntry);
		testSession.setSubIdent(subIdent);
		testSession.setAssessmentEntry(assessmentEntry);
		testSession.setAuthorMode(authorMode);
		testSession.setIdentity(identity);
		testSession.setAttempt(attempt);
		testSession.setAnonymousIdentifier(anonymousIdentifier);
		testSession.setCancelled(false);
		dbInstance.getCurrentEntityManager().persist(testSession);
		return testSession;
	}
	
	public long getLastAttempt(RepositoryEntryRef entry, String subIdent,
			IdentityRef identity, String anonymousIdentifier) {
		StringBuilder sb = new StringBuilder();
		sb.append("select max(session.attempt) from videotasksession session")
		  .append(" where session.repositoryEntry.key=:courseEntryKey");

		if(subIdent != null) {
			sb.append(" and session.subIdent=:courseSubIdent");
		} else {
			sb.append(" and session.subIdent is null");
		}
		
		if(anonymousIdentifier != null) {
			sb.append(" and session.anonymousIdentifier=:anonymousIdentifier");
		} else {
			sb.append(" and session.anonymousIdentifier is null");
		}
		if(identity != null) {
			sb.append(" and session.identity.key=:identityKey");
		}
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("courseEntryKey", entry.getKey());
		if(subIdent != null) {
			query.setParameter("courseSubIdent", subIdent);
		}
		if(anonymousIdentifier != null) {
			query.setParameter("anonymousIdentifier", anonymousIdentifier);
		}
		if(identity != null) {
			query.setParameter("identityKey", identity.getKey());
		}
		
		List<Long> lastAttempt = query.getResultList();
		return lastAttempt == null || lastAttempt.isEmpty() || lastAttempt.get(0) == null ? 0l : lastAttempt.get(0).longValue();
		
	}
	
	public VideoTaskSession loadByKey(Long testSessionKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("select session from videotasksession session")
		  .append(" where session.key=:sessionKey");
		List<VideoTaskSession> sessions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VideoTaskSession.class)
				.setParameter("sessionKey", testSessionKey)
				.getResultList();
		return sessions == null || sessions.isEmpty() ? null : sessions.get(0);
	}
	
	public VideoTaskSession getLastTaskSession(RepositoryEntryRef videoEntry, RepositoryEntryRef entry, String subIdent,
			IdentityRef identity, String anonymousIdentifier, boolean authorMode) {
			
		StringBuilder sb = new StringBuilder();
		sb.append("select session from videotasksession session ")
		  .append("where session.videoEntry.key=:videoEntryKey and session.authorMode=:authorMode");
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
		
		if(anonymousIdentifier != null) {
			sb.append(" and session.anonymousIdentifier=:anonymousIdentifier");
		} else {
			sb.append(" and session.anonymousIdentifier is null");
		}
		if(identity != null) {
			sb.append(" and session.identity.key=:identityKey");
		}
		sb.append(" order by session.creationDate desc");
		
		TypedQuery<VideoTaskSession> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VideoTaskSession.class)
				.setParameter("videoEntryKey", videoEntry.getKey())
				.setParameter("authorMode", authorMode);
		if(entry != null) {
			query.setParameter("courseEntryKey", entry.getKey());
		}
		if(subIdent != null) {
			query.setParameter("courseSubIdent", subIdent);
		}
		if(anonymousIdentifier != null) {
			query.setParameter("anonymousIdentifier", anonymousIdentifier);
		}
		if(identity != null) {
			query.setParameter("identityKey", identity.getKey());
		}
		
		List<VideoTaskSession> lastSessions = query.setMaxResults(1).getResultList();
		return lastSessions == null || lastSessions.isEmpty() ? null : lastSessions.get(0);
	}
	
	public List<VideoTaskSession> getTaskSessions(RepositoryEntryRef entry, String subIdent,
			List<? extends IdentityRef> identities, String anonymousIdentifier) {
			
		QueryBuilder sb = new QueryBuilder();
		sb.append("select session from videotasksession session")
		  .append(" inner join fetch session.assessmentEntry as aEntry")
		  .append(" left join fetch session.identity as ident")
		  .append(" left join fetch ident.user as identUser")
		  .where().append(" session.repositoryEntry.key=:courseEntryKey");

		if(subIdent != null) {
			sb.and().append("session.subIdent=:courseSubIdent");
		} else {
			sb.and().append("session.subIdent is null");
		}
		
		if(anonymousIdentifier != null) {
			sb.and().append("session.anonymousIdentifier=:anonymousIdentifier");
		} else {
			sb.and().append("session.anonymousIdentifier is null");
		}
		if(identities != null && !identities.isEmpty()) {
			sb.and().append("ident.key in (:identitiesKeys)");
		}
		sb.append(" order by ident.key, session.creationDate desc");
		
		TypedQuery<VideoTaskSession> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VideoTaskSession.class)
				.setParameter("courseEntryKey", entry.getKey());
		if(subIdent != null) {
			query.setParameter("courseSubIdent", subIdent);
		}
		if(anonymousIdentifier != null) {
			query.setParameter("anonymousIdentifier", anonymousIdentifier);
		}
		if(identities != null && !identities.isEmpty()) {
			List<Long> identitiesKeys = identities.stream()
					.map(IdentityRef::getKey).toList();
			query.setParameter("identitiesKeys", identitiesKeys);
		}
		return query.getResultList();
	}
	
	public long countTaskSessions(RepositoryEntryRef entry, String subIdent) {	
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(distinct ident.key) from videotasksession session")
		  .append(" inner join session.identity ident")
		  .where().append(" session.repositoryEntry.key=:courseEntryKey");

		if(subIdent != null) {
			sb.and().append("session.subIdent=:courseSubIdent");
		} else {
			sb.and().append("session.subIdent is null");
		}
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("courseEntryKey", entry.getKey());
		if(subIdent != null) {
			query.setParameter("courseSubIdent", subIdent);
		}
		List<Long> count = query.getResultList();
		return count == null || count.isEmpty() || count.get(0) == null ? 0l : count.get(0).longValue();
	}
	
	public VideoTaskSession update(VideoTaskSession testSession) {
		((VideoTaskSessionImpl)testSession).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(testSession);
	}
	
	public int deleteTaskSessions(RepositoryEntryRef entry, String subIdent) {	
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from videotasksession session")
		  .where().append(" session.repositoryEntry.key=:courseEntryKey");
		if(subIdent != null) {
			sb.and().append("session.subIdent=:courseSubIdent");
		} else {
			sb.and().append("session.subIdent is null");
		}
		
		Query query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("courseEntryKey", entry.getKey());
		if(subIdent != null) {
			query.setParameter("courseSubIdent", subIdent);
		}
		return query.executeUpdate();
	}
	
	public int deleteTaskSessions(List<VideoTaskSession> taskSessions) {	
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from videotasksession session")
		  .where().append(" session.key in (:taskSessionsKeys)");

		List<Long> taskSessionsKeys = taskSessions.stream()
				.map(VideoTaskSession::getKey)
				.toList();
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("taskSessionsKeys", taskSessionsKeys)
				.executeUpdate();
	}
}
