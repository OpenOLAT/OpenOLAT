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
import org.olat.ims.qti21.UserTestSession;
import org.olat.ims.qti21.model.jpa.UserTestSessionImpl;
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
public class SessionDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QTI21Storage storage;
	

	public UserTestSession createTestSession(RepositoryEntry testEntry,
			RepositoryEntry courseEntry, String courseSubIdent, Identity identity) {
		
		UserTestSessionImpl testSession = new UserTestSessionImpl();
		Date now = new Date();
		testSession.setCreationDate(now);
		testSession.setLastModified(now);
		testSession.setTestEntry(testEntry);
		testSession.setCourseEntry(courseEntry);
		testSession.setCourseSubIdent(courseSubIdent);
		testSession.setAuthorMode(false);
		testSession.setExploded(false);
		testSession.setIdentity(identity);
		testSession.setStorage(storage.getRelativeDir());
		dbInstance.getCurrentEntityManager().persist(testSession);
		return testSession;
	}
	
	public UserTestSession getLastTestSession(RepositoryEntryRef testEntry,
			RepositoryEntryRef courseEntry, String courseSubIdent, IdentityRef identity) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select session from qtiassessmentsession session ")
		  .append("where session.testEntry.key=:testEntryKey and session.identity.key=:identityKey");
		if(courseEntry != null) {
			sb.append(" and session.courseEntry.key=:courseEntryKey");
		} else {
			sb.append(" and session.courseEntry.key is null");
		}
		
		if(courseSubIdent != null) {
			sb.append(" and session.courseSubIdent=:courseSubIdent");
		} else {
			sb.append(" and session.courseSubIdent is null");
		}
		sb.append(" order by session.creationDate desc");
		
		TypedQuery<UserTestSession> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserTestSession.class)
				.setParameter("testEntryKey", testEntry.getKey())
				.setParameter("identityKey", identity.getKey());
		if(courseEntry != null) {
			query.setParameter("courseEntryKey", courseEntry.getKey());
		}
		if(courseSubIdent != null) {
			query.setParameter("courseSubIdent", courseSubIdent);
		}
		
		List<UserTestSession> lastSessions = query.setMaxResults(1).getResultList();
		return lastSessions == null || lastSessions.isEmpty() ? null : lastSessions.get(0);
	}
	
	public UserTestSession update(UserTestSession testSession) {
		((UserTestSessionImpl)testSession).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(testSession);
	}
	
	public List<UserTestSession> getUserTestSessions(RepositoryEntryRef courseEntry, String courseSubIdent, IdentityRef identity) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadTestSessionsByUserAndCourse", UserTestSession.class)
				.setParameter("courseEntryKey", courseEntry.getKey())
				.setParameter("identityKey", identity.getKey())
				.setParameter("courseSubIdent", courseSubIdent)
				.getResultList();
	}

}
