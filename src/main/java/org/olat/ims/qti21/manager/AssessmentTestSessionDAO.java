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

import java.io.File;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSManager;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.ims.qti21.model.jpa.AssessmentTestSessionImpl;
import org.olat.ims.qti21.model.jpa.AssessmentTestSessionStatistics;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.vitero.model.GroupRole;
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
	
	private final DateFormat formater = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	
	@Autowired
	private DB dbInstance;
	
	public String formatDate(Date date) {
		synchronized(formater) {
			return formater.format(date);
		}
	}
	
	public AssessmentTestSession createAndPersistTestSession(RepositoryEntry testEntry,
			RepositoryEntry repositoryEntry, String subIdent,
			AssessmentEntry assessmentEntry, Identity identity, String anonymousIdentifier,
			Integer compensationExtraTime, boolean authorMode) {
		
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
		testSession.setCancelled(false);
		testSession.setIdentity(identity);
		testSession.setAnonymousIdentifier(anonymousIdentifier);
		testSession.setCompensationExtraTime(compensationExtraTime);
		testSession.setStorage(createSessionStorage(testSession));
		dbInstance.getCurrentEntityManager().persist(testSession);
		return testSession;
	}
	
	public AssessmentTestSession getLastTestSession(RepositoryEntryRef testEntry,
			RepositoryEntryRef entry, String subIdent, IdentityRef identity, String anonymousIdentifier, boolean authorMode) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select session from qtiassessmenttestsession session ")
		  .append("where session.testEntry.key=:testEntryKey and session.authorMode=:authorMode");
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
		sb.append(" and session.exploded=false and session.cancelled=false")
		  .append(" order by session.creationDate desc");
		
		TypedQuery<AssessmentTestSession> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentTestSession.class)
				.setParameter("testEntryKey", testEntry.getKey())
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
		
		List<AssessmentTestSession> lastSessions = query.setMaxResults(1).getResultList();
		return lastSessions == null || lastSessions.isEmpty() ? null : lastSessions.get(0);
	}
	
	public List<AssessmentTestSession> getTestSessions(RepositoryEntryRef testEntry,
			RepositoryEntryRef entry, String subIdent, IdentityRef identity) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select session from qtiassessmenttestsession session")
		  .append(" left join fetch session.assessmentEntry asEntry")
		  .append(" where session.testEntry.key=:testEntryKey and session.identity.key=:identityKey");
		if(entry != null) {
			sb.append(" and session.repositoryEntry.key=:courseEntryKey");
		} else {
			sb.append(" and session.repositoryEntry.key=:testEntryKey");
		}
		
		if(subIdent != null) {
			sb.append(" and session.subIdent=:courseSubIdent");
		} else {
			sb.append(" and session.subIdent is null");
		}
		
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
		
		return query.getResultList();
	}
	
	/**
	 * Create a folder for a session in bcroot.
	 * 
	 * 
	 * @param session
	 * @return
	 */
	public File getSessionStorage(AssessmentTestSession session) {
		LocalFolderImpl rootContainer = getQtiSerializationPath();
		File directory = new File(rootContainer.getBasefile(), session.getStorage());
		if(!directory.exists()) {
			directory.mkdirs();
		}
		return directory;
	}

	/**
	 * Create a folder for a session in bcroot/qtiassessment. The format
	 * is for tests in course:<br>
	 * bcroot/qtiassessment/{course repository primary key}/{course node}/{identity primary key}_{timestamp}_{test primary key}<br>
	 * and for standalone tests:<br>
	 * bcroot/qtiassessment/{test primary key}/{identity primary key}_{timestamp}<br>
	 * 
	 * @param session
	 * @return
	 */
	protected String createSessionStorage(AssessmentTestSession session) {
		File rootDir = getQtiSerializationPath().getBasefile();
		
		String datePart;
		synchronized(formater) {
			datePart = formater.format(session.getCreationDate());
		}
		String userPart;
		if(session.getIdentity() != null) {
			userPart = session.getIdentity().getKey() + "_" + datePart;
		} else {
			userPart = session.getAnonymousIdentifier();
		}

		File storage = rootDir;
		if(session.getRepositoryEntry() != null
				&& !session.getRepositoryEntry().equals(session.getTestEntry())) {
			storage = new File(storage, session.getRepositoryEntry().getKey().toString());
			if(StringHelper.containsNonWhitespace(session.getSubIdent())) {
				storage = new File(storage, session.getSubIdent());
			}
			userPart += "-" + session.getTestEntry().getKey().toString();
		} else if(session.getTestEntry() != null) {
			storage = new File(storage, session.getTestEntry().getKey().toString());
		} else {
			storage = new File(storage, "tmp");
		}
		
		storage = new File(storage, userPart);
		storage.mkdirs();
		
		Path relativePath = rootDir.toPath().relativize(storage.toPath());
		return relativePath.toString();
	}
	
    private LocalFolderImpl getQtiSerializationPath() {
    	return VFSManager.olatRootContainer("/qtiassessment/", null);
	}
    
	public AssessmentTestSession loadByKey(Long testSessionKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("select session from qtiassessmenttestsession session")
		  .append(" where session.key=:sessionKey");
		List<AssessmentTestSession> sessions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentTestSession.class)
				.setParameter("sessionKey", testSessionKey)
				.getResultList();
		return sessions == null || sessions.isEmpty() ? null : sessions.get(0);
	}
	
	/**
	 * Load the assessment test session and only fetch the user.
	 * 
	 * @param testSessionKey
	 * @return
	 */
	public AssessmentTestSession loadFullByKey(Long testSessionKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("select session from qtiassessmenttestsession session")
		  .append(" left join fetch session.identity ident")
		  .append(" left join fetch ident.user usr")
		  .append(" where session.key=:sessionKey");
		List<AssessmentTestSession> sessions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentTestSession.class)
				.setParameter("sessionKey", testSessionKey)
				.getResultList();
		return sessions == null || sessions.isEmpty() ? null : sessions.get(0);
	}
	
	public AssessmentTestSession update(AssessmentTestSession testSession) {
		((AssessmentTestSessionImpl)testSession).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(testSession);
	}
	
	public int extraTime(AssessmentTestSession testSession, int extraTime) {
		String q = "update qtiassessmenttestsessionextratime set extraTime=:extraTime where key=:sessionKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q)
				.setParameter("extraTime", extraTime)
				.setParameter("sessionKey", testSession.getKey())
				.executeUpdate();
	}
	
	public int compensationExtraTime(AssessmentTestSession testSession, int extraTime) {
		String q = "update qtiassessmenttestsessionextratime set compensationExtraTime=:extraTime where key=:sessionKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q)
				.setParameter("extraTime", extraTime)
				.setParameter("sessionKey", testSession.getKey())
				.executeUpdate();
	}
	
	
	/**
	 * Search test session without the author mode flag set to true.
	 * @param testEntry
	 * @return
	 */
	public boolean hasActiveTestSession(RepositoryEntryRef testEntry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select session.key from qtiassessmenttestsession session ")
		  .append("where session.testEntry.key=:testEntryKey and session.authorMode=false");
		
		List<Long> sessionKey = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("testEntryKey", testEntry.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return sessionKey != null && !sessionKey.isEmpty() && sessionKey.get(0) != null;
	}
	
	/**
	 * Return all assessment test session of a test with the author flag set to true.
	 * @param testEntry
	 * @return
	 */
	public List<AssessmentTestSession> getAuthorAssessmentTestSession(RepositoryEntryRef testEntry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select session from qtiassessmenttestsession session ")
		  .append("where session.testEntry.key=:testEntryKey and session.authorMode=true");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentTestSession.class)
				.setParameter("testEntryKey", testEntry.getKey())
				.getResultList();
	}
	
	/**
	 * The assessment test sessions of authenticated users (fetched in the query).
	 * Invalid sessions like exploded and cancelled are excluded.
	 * 
	 * @param courseEntry
	 * @param courseSubIdent
	 * @param testEntry
	 * @return A list of valid test sessions
	 */
	public List<AssessmentTestSession> getTestSessions(RepositoryEntryRef courseEntry, String courseSubIdent, RepositoryEntry testEntry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select session from qtiassessmenttestsession session")
		  .append(" left join fetch session.testEntry testEntry")
		  .append(" left join fetch testEntry.olatResource testResource")
		  .append(" inner join fetch session.identity assessedIdentity")
		  .append(" inner join fetch assessedIdentity.user assessedUser")
		  .append(" where session.repositoryEntry.key=:repositoryEntryKey and session.testEntry.key=:testEntryKey and ");
		if(StringHelper.containsNonWhitespace(courseSubIdent)) {
			sb.append("session.subIdent=:subIdent");
		} else {
			sb.append("session.subIdent is null");
		}
		sb.append(" and session.exploded=false and session.cancelled=false")
		  .append(" order by session.creationDate desc");
		
		TypedQuery<AssessmentTestSession> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentTestSession.class)
				.setParameter("repositoryEntryKey", courseEntry.getKey())
				.setParameter("testEntryKey", testEntry.getKey());
		if(StringHelper.containsNonWhitespace(courseSubIdent)) {
			query.setParameter("subIdent", courseSubIdent);
		}
		return query.getResultList();
	}
	
	/**
	 * A complete list of test sessions inclusive exploded and/or cancelled.
	 * 
	 * @param identity The assessed identity
	 * @return A list of test sessions
	 */
	protected List<AssessmentTestSession> getAllUserTestSessions(IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select session from qtiassessmenttestsession session")
		  .append(" left join fetch session.testEntry testEntry")
		  .append(" left join fetch testEntry.olatResource testResource")
		  .append(" where session.identity.key=:identityKey ");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentTestSession.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}

	public List<AssessmentTestSession> getUserTestSessions(RepositoryEntryRef courseEntry, String courseSubIdent,
			IdentityRef identity, boolean onlyValid) {
		StringBuilder sb = new StringBuilder();
		sb.append("select session from qtiassessmenttestsession session")
		  .append(" left join fetch session.testEntry testEntry")
		  .append(" left join fetch testEntry.olatResource testResource")
		  .append("  where session.repositoryEntry.key=:repositoryEntryKey and session.identity.key=:identityKey and ");
		if(StringHelper.containsNonWhitespace(courseSubIdent)) {
			sb.append("session.subIdent=:subIdent");
		} else {
			sb.append("session.subIdent is null");
		}
		if(onlyValid) {
			sb.append(" and session.exploded=false and session.cancelled=false");
		}
		sb.append(" order by session.creationDate desc");

		TypedQuery<AssessmentTestSession> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentTestSession.class)
				.setParameter("repositoryEntryKey", courseEntry.getKey())
				.setParameter("identityKey", identity.getKey());
		if(StringHelper.containsNonWhitespace(courseSubIdent)) {
			query.setParameter("subIdent", courseSubIdent);
		}
		return query.getResultList();
	}
	
	public List<AssessmentTestSessionStatistics> getUserTestSessionsStatistics(RepositoryEntryRef courseEntry, String courseSubIdent,
			IdentityRef identity, boolean onlyValid) {
		StringBuilder sb = new StringBuilder();
		sb.append("select session,")
		  .append(" (select count(itemSession.key) from qtiassessmentitemsession itemSession")
		  .append("   where itemSession.assessmentTestSession.key=session.key and itemSession.manualScore is not null")
		  .append(" ) as correctItems")
		  .append(" from qtiassessmenttestsession session")
		  .append(" left join fetch session.testEntry testEntry")
		  .append(" left join fetch testEntry.olatResource testResource")
		  .append("  where session.repositoryEntry.key=:repositoryEntryKey and session.identity.key=:identityKey");
		if(onlyValid) {
			sb.append(" and session.exploded=false and session.cancelled=false");
		}
		
		if(StringHelper.containsNonWhitespace(courseSubIdent)) {
			sb.append(" and session.subIdent=:subIdent");
		} else {
			sb.append(" and session.subIdent is null");
		}
		sb.append(" order by session.creationDate desc");
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("repositoryEntryKey", courseEntry.getKey())
				.setParameter("identityKey", identity.getKey());
		if(StringHelper.containsNonWhitespace(courseSubIdent)) {
			query.setParameter("subIdent", courseSubIdent);
		}
		List<Object[]> raws = query.getResultList();
		List<AssessmentTestSessionStatistics> stats = new ArrayList<>(raws.size());
		for(Object[] raw:raws) {
			AssessmentTestSession testSession = (AssessmentTestSession)raw[0];
			int numOfCorrectedItems = (raw[1] == null ? 0 : ((Number)raw[1]).intValue());
			stats.add(new AssessmentTestSessionStatistics(testSession, numOfCorrectedItems));
		}
		
		return stats;
	}
	
	/**
	 * Returns the last test session with a finish or termination time,
	 * last defined by the creation date and which is exploded or
	 * cancelled.
	 * 
	 * @param courseEntry The entry
	 * @param courseSubIdent The sub-identifier
	 * @param testEntry The test repository entry
	 * @param identity The assessed identity
	 * @return The last test session with a finish or termination time, not exploded, not cancelled
	 */
	public AssessmentTestSession getLastUserTestSession(RepositoryEntryRef courseEntry, String courseSubIdent, RepositoryEntry testEntry, IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select session from qtiassessmenttestsession session")
		  .append(" left join fetch session.testEntry testEntry")
		  .append(" left join fetch testEntry.olatResource testResource")
		  .append(" where session.repositoryEntry.key=:repositoryEntryKey and session.identity.key=:identityKey")
		  .append(" and session.testEntry.key=:testEntryKey and (session.finishTime is not null or session.terminationTime is not null)")
		  .append(" and session.exploded=false and session.cancelled=false");
		if(StringHelper.containsNonWhitespace(courseSubIdent)) {
			sb.append(" and session.subIdent=:subIdent");
		} else {
			sb.append(" and session.subIdent is null");
		}
		sb.append(" order by session.creationDate desc");
		
		TypedQuery<AssessmentTestSession> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentTestSession.class)
				.setParameter("repositoryEntryKey", courseEntry.getKey())
				.setParameter("testEntryKey", testEntry.getKey())
				.setParameter("identityKey", identity.getKey());
		if(StringHelper.containsNonWhitespace(courseSubIdent)) {
			query.setParameter("subIdent", courseSubIdent);
		}
		List<AssessmentTestSession> sessions = query
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return sessions.isEmpty() ? null : sessions.get(0);
	}
	
	public List<Identity> getRunningTestSessionIdentities(RepositoryEntryRef entry, String courseSubIdent,
			Map<String, String> userPropertiesSearch, boolean userPropertiesAsIntersectionSearch) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select assessedIdentity from qtiassessmenttestsession session")
		  .append(" inner join session.identity assessedIdentity")
		  .append(" inner join assessedIdentity.user assessedUser")
		  .where().append("session.repositoryEntry.key=:repositoryEntryKey and session.finishTime is null and session.terminationTime is null");
		if(StringHelper.containsNonWhitespace(courseSubIdent)) {
			sb.and().append("session.subIdent=:subIdent");
		} else {
			sb.and().append("session.subIdent is null");
		}
		
		if(userPropertiesSearch != null && !userPropertiesSearch.isEmpty()) {
			sb.and().append(" (");
			
			boolean append = false;
			for(Map.Entry<String,String> userProperty: userPropertiesSearch.entrySet()) {
				if(append) {
					sb.append(" and ", " or ", userPropertiesAsIntersectionSearch);
				} else {
					append = true;
				}
				String prop = userProperty.getKey();
				sb.appendFuzzyLike("assessedUser." + prop, "uprop" + prop);
			}
			sb.append(")");
		}
		
		TypedQuery<Identity> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("repositoryEntryKey", entry.getKey());
		if(StringHelper.containsNonWhitespace(courseSubIdent)) {
			query.setParameter("subIdent", courseSubIdent);
		}
		
		if(userPropertiesSearch != null && !userPropertiesSearch.isEmpty()) {
			for(Map.Entry<String, String> userProperty:userPropertiesSearch.entrySet()) {
				String fuzzyValue = PersistenceHelper.makeFuzzyQueryString(userProperty.getValue());
				query.setParameter("uprop" + userProperty.getKey(), fuzzyValue);
			}
		}
		
		return query.getResultList();
	}
	
	public List<Long> getRunningTestSessionIdentitiesKey(RepositoryEntryRef entry, String courseSubIdent, RepositoryEntry testEntry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select assessedIdentity.key from qtiassessmenttestsession session")
		  .append(" inner join session.identity assessedIdentity")
		  .append(" where session.repositoryEntry.key=:repositoryEntryKey and session.testEntry.key=:testEntryKey")
		  .append(" and session.finishTime is null and session.terminationTime is null");
		if(StringHelper.containsNonWhitespace(courseSubIdent)) {
			sb.append(" and session.subIdent=:subIdent");
		} else {
			sb.append(" and session.subIdent is null");
		}
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("repositoryEntryKey", entry.getKey())
				.setParameter("testEntryKey", testEntry.getKey());
		if(StringHelper.containsNonWhitespace(courseSubIdent)) {
			query.setParameter("subIdent", courseSubIdent);
		}
		return query.getResultList();
	}
	
	public List<AssessmentTestSession> getRunningTestSessions(RepositoryEntryRef entry, String courseSubIdent, RepositoryEntry testEntry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select session from qtiassessmenttestsession session")
		  .append(" left join fetch session.testEntry testEntry")
		  .append(" left join fetch testEntry.olatResource testResource")
		  .append(" inner join fetch session.identity assessedIdentity")
		  .append(" inner join fetch assessedIdentity.user assessedUser")
		  .append(" where session.repositoryEntry.key=:repositoryEntryKey and session.testEntry.key=:testEntryKey")
		  .append(" and session.finishTime is null and session.terminationTime is null");
		if(StringHelper.containsNonWhitespace(courseSubIdent)) {
			sb.append(" and session.subIdent=:subIdent");
		} else {
			sb.append(" and session.subIdent is null");
		}
		
		TypedQuery<AssessmentTestSession> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentTestSession.class)
				.setParameter("repositoryEntryKey", entry.getKey())
				.setParameter("testEntryKey", testEntry.getKey());
		if(StringHelper.containsNonWhitespace(courseSubIdent)) {
			query.setParameter("subIdent", courseSubIdent);
		}
		return query.getResultList();
	}
	
	public List<AssessmentTestSession> getRunningTestSessions(RepositoryEntryRef entry, List<String> courseSubIdents, List<? extends IdentityRef> identities) {
		List<Long> identityKeys = identities.stream()
				.map(IdentityRef::getKey)
				.collect(Collectors.toList());
		return getRunningTestSessionsByIdentityKeys(entry, courseSubIdents, identityKeys);
	}
	
	/**
	 * The query doesn't fetch the test entry, only the identities (no users).
	 * 
	 * @param entry The repository entry, the cours entry
	 * @param courseSubIdents A list of elements ids, null searches all
	 * @param identityKeys A list of assessed identities keys (mandatory)
	 * @return A list of assessment test sessions without finished or termination time,
	 * 		not exploded or cancelled
	 */
	public List<AssessmentTestSession> getRunningTestSessionsByIdentityKeys(RepositoryEntryRef entry, List<String> courseSubIdents, List<Long> identityKeys) {
		if(identityKeys == null || identityKeys.isEmpty()) return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select session from qtiassessmenttestsession session")
		  .append(" inner join fetch session.identity assessedIdentity")
		  .append(" where session.repositoryEntry.key=:repositoryEntryKey")
		  .append(" and session.finishTime is null and session.terminationTime is null")
		  .append(" and session.exploded=false and session.cancelled=false")
		  .append(" and assessedIdentity.key in (:identityKeys)");
		if(courseSubIdents != null && !courseSubIdents.isEmpty()) {
			sb.append(" and session.subIdent in (:subIdents)");
		}
		
		TypedQuery<AssessmentTestSession> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentTestSession.class)
				.setParameter("repositoryEntryKey", entry.getKey())
				.setParameter("identityKeys", identityKeys);		
		if(courseSubIdents != null && !courseSubIdents.isEmpty()) {
			query.setParameter("subIdents", courseSubIdents);
		}
		return query.getResultList();
	}
	
	/**
	 * 
	 * @param entry The repository entry (typically the course, or the test if not in a course) (mandatory)
	 * @param courseSubIdent An optional sub-identifier
	 * @param testEntry The test repository entry
	 * @param identities The list of assessed identities
	 * @return true if at least one of the identities has a running test session
	 */
	public boolean hasRunningTestSessions(RepositoryEntryRef entry, String courseSubIdent, RepositoryEntry testEntry, List<? extends IdentityRef> identities) {
		StringBuilder sb = new StringBuilder();
		sb.append("select session.key from qtiassessmenttestsession session")
		  .append(" left join session.testEntry testEntry")
		  .append(" left join testEntry.olatResource testResource")
		  .append(" where session.repositoryEntry.key=:repositoryEntryKey and session.testEntry.key=:testEntryKey")
		  .append(" and session.finishTime is null and session.terminationTime is null")
		  .append(" and session.exploded=false and session.cancelled=false");
		if(StringHelper.containsNonWhitespace(courseSubIdent)) {
			sb.append(" and session.subIdent=:subIdent");
		} else {
			sb.append(" and session.subIdent is null");
		}
		if(identities != null && !identities.isEmpty()) {
			sb.append(" and session.identity.key in (:identityKeys)");
		}
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("repositoryEntryKey", entry.getKey())
				.setParameter("testEntryKey", testEntry.getKey())
				.setFirstResult(0)
				.setMaxResults(1);
		if(StringHelper.containsNonWhitespace(courseSubIdent)) {
			query.setParameter("subIdent", courseSubIdent);
		}
		if(identities != null && !identities.isEmpty()) {
			List<Long> identityKeys = identities.stream()
					.map(IdentityRef::getKey)
					.collect(Collectors.toList());
			query.setParameter("identityKeys", identityKeys);
		}
		
		List<Long> found = query.getResultList();
		return found != null && !found.isEmpty() && found.get(0) != null && found.get(0) >= 0;
	}
	
	/**
	 * 
	 * @param entry The repository entry (typically the course, or the test if not in a course) (mandatory)
	 * @param courseSubIdent An optional sub-identifier
	 * @param testEntry The test repository entry
	 * @param identities The list of assessed identities
	 * @return true if at least one of the identities has a running test session
	 */
	public boolean hasRunningTestSessions(RepositoryEntryRef entry, List<String> courseSubIdents, List<? extends IdentityRef> identities) {
		StringBuilder sb = new StringBuilder();
		sb.append("select session.key from qtiassessmenttestsession session")
		  .append(" left join session.testEntry testEntry")
		  .append(" left join testEntry.olatResource testResource")
		  .append(" where session.repositoryEntry.key=:repositoryEntryKey")
		  .append(" and session.finishTime is null and session.terminationTime is null")
		  .append(" and session.exploded=false and session.cancelled=false")
		  .append(" and session.identity.key in (:identityKeys)");
		if(courseSubIdents != null && !courseSubIdents.isEmpty()) {
			sb.append(" and session.subIdent in (:subIdents)");
		}
		
		List<Long> identityKeys = identities.stream()
				.map(IdentityRef::getKey)
				.collect(Collectors.toList());
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setFirstResult(0)
				.setMaxResults(1)
				.setParameter("repositoryEntryKey", entry.getKey())
				.setParameter("identityKeys", identityKeys);		
		if(courseSubIdents != null && !courseSubIdents.isEmpty()) {
			query.setParameter("subIdents", courseSubIdents);
		}
		List<Long> found = query.getResultList();
		return found != null && !found.isEmpty() && found.get(0) != null && found.get(0) >= 0;
	}
	
	public int deleteTestSession(AssessmentTestSession testSession) {
		StringBuilder responseSb  = new StringBuilder();
		responseSb.append("delete from qtiassessmentresponse response where response.assessmentTestSession.key=:sessionKey");
		int responses = dbInstance.getCurrentEntityManager()
				.createQuery(responseSb.toString())
				.setParameter("sessionKey", testSession.getKey())
				.executeUpdate();
		
		StringBuilder itemSb  = new StringBuilder();
		itemSb.append("delete from qtiassessmentitemsession itemSession where itemSession.assessmentTestSession.key=:sessionKey");
		int itemSessions = dbInstance.getCurrentEntityManager()
				.createQuery(itemSb.toString())
				.setParameter("sessionKey", testSession.getKey())
				.executeUpdate();
		
		String q = "delete from qtiassessmenttestsession session where session.key=:sessionKey";
		int sessions = dbInstance.getCurrentEntityManager()
				.createQuery(q)
				.setParameter("sessionKey", testSession.getKey())
				.executeUpdate();
		return itemSessions + sessions + responses;
	}
	
	/**
	 * The method delete all bookmarks, responses, item sessions and test sessions
	 * of all users which had used the specificed test ressource.
	 * 
	 * @param testEntry
	 * @return
	 */
	public int deleteAllUserTestSessionsByTest(RepositoryEntryRef testEntry) {
		String marksSb = "delete from qtiassessmentmarks marks where marks.testEntry.key=:testEntryKey";
		int marks = dbInstance.getCurrentEntityManager()
				.createQuery(marksSb)
				.setParameter("testEntryKey", testEntry.getKey())
				.executeUpdate();
		
		StringBuilder responseSb  = new StringBuilder();
		responseSb.append("delete from qtiassessmentresponse response where")
		  .append("  response.assessmentItemSession.key in (")
		  .append("   select itemSession from qtiassessmentitemsession itemSession, qtiassessmenttestsession session ")
		  .append("   where itemSession.assessmentTestSession.key=session.key and session.testEntry.key=:testEntryKey")
		  .append(" )");
		int responses = dbInstance.getCurrentEntityManager()
				.createQuery(responseSb.toString())
				.setParameter("testEntryKey", testEntry.getKey())
				.executeUpdate();
		
		StringBuilder itemSb  = new StringBuilder();
		itemSb.append("delete from qtiassessmentitemsession itemSession")
		  .append(" where itemSession.assessmentTestSession.key in(")
		  .append("  select session.key from qtiassessmenttestsession session where session.testEntry.key=:testEntryKey")
		  .append(" )");
		int itemSessions = dbInstance.getCurrentEntityManager()
				.createQuery(itemSb.toString())
				.setParameter("testEntryKey", testEntry.getKey())
				.executeUpdate();
		
		String q = "delete from qtiassessmenttestsession session where session.testEntry.key=:testEntryKey";
		int sessions = dbInstance.getCurrentEntityManager()
				.createQuery(q)
				.setParameter("testEntryKey", testEntry.getKey())
				.executeUpdate();
		return marks + itemSessions + sessions + responses;
	}
	
	
	public int deleteAllUserTestSessionsByCourse(RepositoryEntryRef entry, String subIdent) {
		String marksSb = "delete from qtiassessmentmarks marks where marks.repositoryEntry.key=:entryKey and marks.subIdent=:subIdent";
		int marks = dbInstance.getCurrentEntityManager()
				.createQuery(marksSb)
				.setParameter("entryKey", entry.getKey())
				.setParameter("subIdent", subIdent)
				.executeUpdate();
		
		StringBuilder responseSb  = new StringBuilder();
		responseSb.append("delete from qtiassessmentresponse response where")
		  .append("  response.assessmentItemSession.key in (")
		  .append("   select itemSession from qtiassessmentitemsession itemSession, qtiassessmenttestsession session ")
		  .append("   where itemSession.assessmentTestSession.key=session.key and session.repositoryEntry.key=:entryKey and session.subIdent=:subIdent")
		  .append(" )");
		int responses = dbInstance.getCurrentEntityManager()
				.createQuery(responseSb.toString())
				.setParameter("entryKey", entry.getKey())
				.setParameter("subIdent", subIdent)
				.executeUpdate();
		
		StringBuilder itemSb  = new StringBuilder();
		itemSb.append("delete from qtiassessmentitemsession itemSession")
		  .append(" where itemSession.assessmentTestSession.key in(")
		  .append("  select session.key from qtiassessmenttestsession session where session.repositoryEntry.key=:entryKey and session.subIdent=:subIdent")
		  .append(" )");
		int itemSessions = dbInstance.getCurrentEntityManager()
				.createQuery(itemSb.toString())
				.setParameter("entryKey", entry.getKey())
				.setParameter("subIdent", subIdent)
				.executeUpdate();
		
		String q = "delete from qtiassessmenttestsession session where session.repositoryEntry.key=:entryKey and session.subIdent=:subIdent";
		int sessions = dbInstance.getCurrentEntityManager()
				.createQuery(q)
				.setParameter("entryKey", entry.getKey())
				.setParameter("subIdent", subIdent)
				.executeUpdate();
		return marks + itemSessions + sessions + responses;
	}
	
	
	public int deleteAllUserTestSessionsByCourse(RepositoryEntryRef entry) {
		String marksSb = "delete from qtiassessmentmarks marks where marks.repositoryEntry.key=:entryKey";
		int marks = dbInstance.getCurrentEntityManager()
				.createQuery(marksSb)
				.setParameter("entryKey", entry.getKey())
				.executeUpdate();
		
		StringBuilder responseSb  = new StringBuilder();
		responseSb.append("delete from qtiassessmentresponse response where")
		  .append("  response.assessmentItemSession.key in (")
		  .append("   select itemSession from qtiassessmentitemsession itemSession, qtiassessmenttestsession session ")
		  .append("   where itemSession.assessmentTestSession.key=session.key and session.repositoryEntry.key=:entryKey")
		  .append(" )");
		int responses = dbInstance.getCurrentEntityManager()
				.createQuery(responseSb.toString())
				.setParameter("entryKey", entry.getKey())
				.executeUpdate();
		
		StringBuilder itemSb  = new StringBuilder();
		itemSb.append("delete from qtiassessmentitemsession itemSession")
		  .append(" where itemSession.assessmentTestSession.key in(")
		  .append("  select session.key from qtiassessmenttestsession session where session.repositoryEntry.key=:entryKey")
		  .append(" )");
		int itemSessions = dbInstance.getCurrentEntityManager()
				.createQuery(itemSb.toString())
				.setParameter("entryKey", entry.getKey())
				.executeUpdate();
		
		String q = "delete from qtiassessmenttestsession session where session.repositoryEntry.key=:entryKey";
		int sessions = dbInstance.getCurrentEntityManager()
				.createQuery(q)
				.setParameter("entryKey", entry.getKey())
				.executeUpdate();
		return marks + itemSessions + sessions + responses;
	}
	
	/**
	 * The query only returns session with a valid finish time and which are not in
	 * author mode.
	 * 
	 * @param searchParams
	 * @return The returned list is order by user name and test session key
	 */
	public List<AssessmentTestSession> getTestSessionsOfResponse(QTI21StatisticSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select testSession from qtiassessmenttestsession testSession ")
		  .append(" inner join fetch testSession.assessmentEntry assessmentEntry")
		  .append(" left join assessmentEntry.identity as ident")
		  .append(" left join ident.user as usr");
		
		decorateTestSessionPermission(sb, searchParams);
		//need to be anonymized
		sb.append(" order by usr.lastName, usr.firstName, testSession.key");
		
		TypedQuery<AssessmentTestSession> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentTestSession.class);
		decorateTestSessionPermission(query, searchParams) ;
		return query.getResultList();
	}
	
	public List<AssessmentTestSession> getValidTestSessions(IdentityRef identity, RepositoryEntryRef courseEntry, String subIdent, Date from, Date to) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select session from qtiassessmenttestsession session ")
		  .append(" where session.repositoryEntry.key=:repositoryEntryKey and session.identity.key=:identityKey")
		  .append(" and session.exploded=false and session.cancelled=false")
		  .append(" and session.creationDate>=:from and session.creationDate<=:to");
		if(StringHelper.containsNonWhitespace(subIdent)) {
			sb.append(" and session.subIdent=:subIdent");
		} else {
			sb.append(" and session.subIdent is null");
		}
			
		TypedQuery<AssessmentTestSession> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentTestSession.class)
				.setParameter("repositoryEntryKey", courseEntry.getKey())
				.setParameter("identityKey", identity.getKey())
				.setParameter("from", from, TemporalType.TIMESTAMP)
				.setParameter("to", to, TemporalType.TIMESTAMP);
		if(StringHelper.containsNonWhitespace(subIdent)) {
			query.setParameter("subIdent", subIdent);
		}

		return query.getResultList();
	}
	
	/**
	 * Decorate a testSession query with the permissions of the specified search parameters.
	 * 
	 * @param sb
	 * @param searchParams
	 */
	private static final void decorateTestSessionPermission(QueryBuilder sb, QTI21StatisticSearchParams searchParams) {
	  	sb.append(" where testSession.testEntry.key=:testEntryKey")
	  	  .append(" and testSession.finishTime is not null and testSession.authorMode=false")
		  .append(" and testSession.exploded=false and testSession.cancelled=false");
		if(searchParams.getCourseEntry() != null || searchParams.getTestEntry() != null) {
			sb.append(" and testSession.repositoryEntry.key=:repoEntryKey");
		}
		if(StringHelper.containsNonWhitespace(searchParams.getNodeIdent())) {
			sb.append(" and testSession.subIdent=:subIdent");
		}
		sb.append(" and (");
		
		if(searchParams.getLimitToGroups() != null) {
			sb.append(" testSession.identity.key in (select membership.identity.key from  bgroupmember as membership, repoentrytogroup as rel")
			  .append("   where rel.entry.key=:repoEntryKey and rel.group.key=membership.group.key and rel.group.key in (:limitGroupKeys)");
			if(!searchParams.isViewNonMembers()) {
				sb.append(" and membership.role='").append(GroupRoles.participant.name()).append("'");
			}
			sb.append(" )");
		} else if(searchParams.getLimitToIdentities() != null && !searchParams.getLimitToIdentities().isEmpty()) {
			sb.append(" testSession.identity.key in (select membership.identity.key from  bgroupmember as membership, repoentrytogroup as rel")
			  .append("   where rel.entry.key=:repoEntryKey and rel.group.key=membership.group.key and membership.identity.key in (:limitIdentityKeys)")
			  .append("   and membership.role='").append(GroupRoles.participant.name()).append("'")
			  .append(" )");
		} else if (searchParams.isViewMembers() && searchParams.isViewNonMembers() && searchParams.isViewAnonymUsers()) {
			//no restrictions
			sb.append("1=1");
		} else if (searchParams.isViewMembers() || searchParams.isViewNonMembers() || searchParams.isViewAnonymUsers()) {
			boolean or = false;
			if (searchParams.isViewMembers()) {
				sb.append(" testSession.identity.key in ( select membership.identity.key from repoentrytogroup as rel, bgroupmember membership ")
				  .append("   where rel.entry.key=:repoEntryKey and rel.group.key=membership.group.key and membership.role='").append(GroupRole.participant).append("'")
				  .append(" )");
				or = true;
			}
			if (searchParams.isViewNonMembers()) {
				if (or) sb.append(" or ");
				sb.append(" testSession.identity.key not in (select membership.identity.key from repoentrytogroup as rel, bgroupmember as membership")
				  .append("   where rel.entry.key=:repoEntryKey and rel.group.key=membership.group.key and membership.role")
				      .in(GroupRoles.participant.name(), GroupRoles.coach.name(), CurriculumRoles.mastercoach.name(), GroupRoles.owner.name())
				.append(" )");
				or = true;
			}
			if (searchParams.isViewAnonymUsers()) {
				if (or) sb.append(" or ");
				sb.append(" testSession.anonymousIdentifier is not null");
			}
		}
		sb.append(")");
	}
	
	/**
	 * Decorate a testSession query with the permissions of the specified search parameters.
	 * 
	 * @param sb
	 * @param searchParams
	 */
	private static final void decorateTestSessionPermission(TypedQuery<?> query, QTI21StatisticSearchParams searchParams) {
		query.setParameter("testEntryKey", searchParams.getTestEntry().getKey());
		if(searchParams.getCourseEntry() != null) {
			query.setParameter("repoEntryKey", searchParams.getCourseEntry().getKey());
		} else {
			query.setParameter("repoEntryKey", searchParams.getTestEntry().getKey());
		}
		if(StringHelper.containsNonWhitespace(searchParams.getNodeIdent())) {
			query.setParameter("subIdent", searchParams.getNodeIdent());
		}
		if(searchParams.getLimitToGroups() != null && !searchParams.getLimitToGroups().isEmpty()) {
			List<Long> keys = searchParams.getLimitToGroups().stream()
					.map(Group::getKey).collect(Collectors.toList());
			query.setParameter("limitGroupKeys", keys);
		} else if(searchParams.getLimitToIdentities() != null && !searchParams.getLimitToIdentities().isEmpty()) {
			List<Long> keys = searchParams.getLimitToIdentities().stream()
					.map(Identity::getKey).collect(Collectors.toList());
			query.setParameter("limitIdentityKeys", keys);
		}
	}
}
