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
package org.olat.course.nodes.practice.manager;

import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 13 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PracticeAssessmentTestSessionDAO {
	
	@Autowired
	private DB dbInstance;
	
	public long countCompletedTestSessions(RepositoryEntryRef testEntry,
			RepositoryEntryRef entry, String subIdent, IdentityRef identity) {
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(session.key) from qtiassessmenttestsession session")
		  .and().append(" session.testEntry.key=:testEntryKey and session.identity.key=:identityKey")
		  .and().append(" session.repositoryEntry.key=:courseEntryKey and session.subIdent=:subIdent")
		  .and().append(" session.cancelled=false and session.exploded=false")
		  .and().append(" (session.finishTime is not null or session.terminationTime is not null)")
		  .and().append(" session.passed=true");
		
		List<Long> counts = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("testEntryKey", testEntry.getKey())
				.setParameter("identityKey", identity.getKey())
				.setParameter("courseEntryKey", entry.getKey())
				.setParameter("subIdent", subIdent)
				.getResultList();
		return counts == null || counts.isEmpty() || counts.get(0) == null ? 0 : counts.get(0).longValue();
	}
	
	public List<AssessmentEntry> loadAssessmentEntries(RepositoryEntryRef entry, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select data from assessmententry data ")
		   .append(" inner join fetch data.identity ident")
		   .append(" inner join fetch ident.user identuser")
		   .and().append(" data.repositoryEntry.key=:repositoryEntryKey and data.subIdent=:subIdent")
		   .and().append(" data.score is not null")
		   .and().append(" data.share=true")
		   .append(" order by data.score");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntry.class)
				.setParameter("repositoryEntryKey", entry.getKey())
				.setParameter("subIdent", subIdent)
				.getResultList();
	}

}
