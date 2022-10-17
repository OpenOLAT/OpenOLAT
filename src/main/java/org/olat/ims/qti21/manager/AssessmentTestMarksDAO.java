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

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.ims.qti21.AssessmentTestMarks;
import org.olat.ims.qti21.model.jpa.AssessmentTestMarksImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 03.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentTestMarksDAO {

	@Autowired
	private DB dbInstance;
	
	public AssessmentTestMarks createAndPersistTestMarks(RepositoryEntry testEntry,
			RepositoryEntry repositoryEntry, String subIdent, Identity identity, String marks) {
		
		AssessmentTestMarksImpl testSession = new AssessmentTestMarksImpl();
		Date now = new Date();
		testSession.setCreationDate(now);
		testSession.setLastModified(now);
		testSession.setTestEntry(testEntry);
		testSession.setRepositoryEntry(repositoryEntry);
		testSession.setSubIdent(subIdent);
		testSession.setIdentity(identity);
		testSession.setMarks(marks);
		dbInstance.getCurrentEntityManager().persist(testSession);
		return testSession;
	}
	
	public AssessmentTestMarks loadTestMarks(RepositoryEntryRef testEntry,
			RepositoryEntryRef entry, String subIdent, IdentityRef identity) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select marks from qtiassessmentmarks marks ")
		  .append("where marks.testEntry.key=:testEntryKey and marks.identity.key=:identityKey");
		if(entry != null) {
			sb.append(" and marks.repositoryEntry.key=:courseEntryKey");
		} else {
			sb.append(" and marks.repositoryEntry.key is null");
		}
		
		if(subIdent != null) {
			sb.append(" and marks.subIdent=:courseSubIdent");
		} else {
			sb.append(" and marks.subIdent is null");
		}
		
		TypedQuery<AssessmentTestMarks> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentTestMarks.class)
				.setParameter("testEntryKey", testEntry.getKey())
				.setParameter("identityKey", identity.getKey());
		if(entry != null) {
			query.setParameter("courseEntryKey", entry.getKey());
		}
		if(subIdent != null) {
			query.setParameter("courseSubIdent", subIdent);
		}
		
		List<AssessmentTestMarks> marks = query.setMaxResults(1).getResultList();
		return marks == null || marks.isEmpty() ? null : marks.get(0);
	}
	
	public AssessmentTestMarks merge(AssessmentTestMarks marks) {
		((AssessmentTestMarksImpl)marks).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(marks);
	}
}
