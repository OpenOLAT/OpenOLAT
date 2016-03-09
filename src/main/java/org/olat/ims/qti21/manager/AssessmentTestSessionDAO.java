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
import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
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
	

	private final DateFormat formater = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	
	@Autowired
	private DB dbInstance;
	
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
		testSession.setStorage(createSessionStorage(testSession));
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
	
	/**
	 * Create a folder for a session in bcroot.
	 * 
	 * 
	 * @param session
	 * @return
	 */
	public File getSessionStorage(AssessmentTestSession session) {
		OlatRootFolderImpl rootContainer = getQtiSerializationPath();
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
	protected String createSessionStorage(AssessmentTestSessionImpl session) {
		File rootDir = getQtiSerializationPath().getBasefile();
		
		String datePart;
		synchronized(formater) {
			datePart = formater.format(session.getCreationDate());
		}
		String userPart = session.getIdentity().getKey() + "_" + datePart;

		File storage = rootDir;
		if(session.getRepositoryEntry() != null
				&& !session.getRepositoryEntry().equals(session.getTestEntry())) {
			storage = new File(storage, session.getRepositoryEntry().getKey().toString());
			if(StringHelper.containsNonWhitespace(session.getSubIdent())) {
				storage = new File(storage, session.getSubIdent());
			}
			userPart += "-" + session.getTestEntry().getKey().toString();
		} else {
			storage = new File(storage, session.getTestEntry().getKey().toString());
		}
		
		storage = new File(storage, userPart);
		storage.mkdirs();
		
		Path relativePath = rootDir.toPath().relativize(storage.toPath());
		String relativePathString = relativePath.toString();
		return relativePathString;
	}
	
    private OlatRootFolderImpl getQtiSerializationPath() {
    	return new OlatRootFolderImpl("/qtiassessment/", null);
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
	
	public int deleteUserTestSessions(RepositoryEntryRef testEntry) {
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
		return itemSessions + sessions + responses;
	}
}
