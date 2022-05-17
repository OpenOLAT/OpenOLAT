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

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.model.ParentPartItemRefs;
import org.olat.ims.qti21.model.jpa.AssessmentItemSessionImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 02.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentItemSessionDAO {
	
	@Autowired
	private DB dbInstance;
	
	public AssessmentItemSession createAndPersistAssessmentItemSession(AssessmentTestSession assessmentTestSession, ParentPartItemRefs parentParts,
			String assessmentItemIdentifier, String externalRefIdentifier) {
		AssessmentItemSessionImpl itemSession = new AssessmentItemSessionImpl();
		Date now = new Date();
		itemSession.setCreationDate(now);
		itemSession.setLastModified(now);
		itemSession.setAssessmentItemIdentifier(assessmentItemIdentifier);
		itemSession.setExternalRefIdentifier(externalRefIdentifier);
		itemSession.setAssessmentTestSession(assessmentTestSession);
		if(parentParts != null) {
			itemSession.setSectionIdentifier(parentParts.getSectionIdentifier());
			itemSession.setTestPartIdentifier(parentParts.getTestPartIdentifier());
		}
		dbInstance.getCurrentEntityManager().persist(itemSession);
		return itemSession;
	}
	
	public AssessmentItemSession loadByKey(Long key) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select itemSession from qtiassessmentitemsession itemSession")
		  .append(" where itemSession.key=:assessmentItemKey");
		
		List<AssessmentItemSession> itemSessions = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), AssessmentItemSession.class)
			.setParameter("assessmentItemKey", key)
			.getResultList();
		return itemSessions == null || itemSessions.isEmpty() ? null : itemSessions.get(0);
	}
	
	public AssessmentItemSession getAssessmentItemSession(AssessmentTestSession assessmentTestSession, String assessmentItemIdentifier) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select itemSession from qtiassessmentitemsession itemSession")
		  .append(" where itemSession.assessmentItemIdentifier=:assessmentItemIdentifier")
		  .append(" and itemSession.assessmentTestSession.key=:assessmentTestSessionKey");
		
		List<AssessmentItemSession> itemSessions = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), AssessmentItemSession.class)
			.setParameter("assessmentItemIdentifier", assessmentItemIdentifier)
			.setParameter("assessmentTestSessionKey", assessmentTestSession.getKey())
			.getResultList();
		return itemSessions == null || itemSessions.isEmpty() ? null : itemSessions.get(0);
	}
	
	public List<AssessmentItemSession> getAssessmentItemSessions(AssessmentTestSession assessmentTestSession) {
		StringBuilder sb = new StringBuilder();
		sb.append("select itemSession from qtiassessmentitemsession itemSession")
		  .append(" where itemSession.assessmentTestSession.key=:assessmentTestSessionKey");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), AssessmentItemSession.class)
			.setParameter("assessmentTestSessionKey", assessmentTestSession.getKey())
			.getResultList();
	}
	
	public List<AssessmentItemSession> getAssessmentItemSessions(RepositoryEntryRef entry, String subIdent, RepositoryEntry testEntry, String itemRef) {
		StringBuilder sb = new StringBuilder();
		sb.append("select itemSession from qtiassessmentitemsession itemSession")
		  .append(" inner join itemSession.assessmentTestSession testSession")
		  .append(" where testSession.testEntry.key=:testEntryKey");
		if(entry != null) {
			sb.append(" and testSession.repositoryEntry.key=:courseEntryKey");
		} else {
			sb.append(" and testSession.repositoryEntry.key=:testEntryKey");
		}
		
		if(subIdent != null) {
			sb.append(" and testSession.subIdent=:subIdent");
		} else {
			sb.append(" and testSession.subIdent is null");
		}
		
		if(itemRef != null) {
			sb.append(" and itemSession.assessmentItemIdentifier=:itemRef");
		}
		
		TypedQuery<AssessmentItemSession> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), AssessmentItemSession.class)
			.setParameter("testEntryKey", testEntry.getKey());
		if(entry != null) {
			query.setParameter("courseEntryKey", entry.getKey());
		}
		if(subIdent != null) {
			query.setParameter("subIdent", subIdent);
		}
		if(itemRef != null) {
			query.setParameter("itemRef", itemRef);
		}
		return query.getResultList();
	}
	
	public int setAssessmentItemSessionReviewFlag(RepositoryEntryRef entry, String subIdent, RepositoryEntry testEntry, String itemRef, boolean toReview) {
		StringBuilder sb = new StringBuilder();
		sb.append("update qtiassessmentitemsession item set item.toReview=:review where item.assessmentTestSession.id in")
		  .append(" (select testSession.id from qtiassessmenttestsession testSession")
		  .append(" where testSession.testEntry.key=:testEntryKey");
		if(entry != null) {
			sb.append(" and testSession.repositoryEntry.key=:courseEntryKey");
		} else {
			sb.append(" and testSession.repositoryEntry.key=:testEntryKey");
		}
		
		if(subIdent != null) {
			sb.append(" and testSession.subIdent=:subIdent");
		} else {
			sb.append(" and testSession.subIdent is null");
		}
		sb.append(" ) and item.assessmentItemIdentifier=:itemRef");

		Query query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString())
			.setParameter("testEntryKey", testEntry.getKey())
			.setParameter("itemRef", itemRef)
			.setParameter("review", Boolean.valueOf(toReview));
		if(entry != null) {
			query.setParameter("courseEntryKey", entry.getKey());
		}
		if(subIdent != null) {
			query.setParameter("subIdent", subIdent);
		}
		return query.executeUpdate();
	}
	
	public AssessmentItemSession merge(AssessmentItemSession itemSession) {
		return dbInstance.getCurrentEntityManager().merge(itemSession);
	}
	
	public double getProcentCorrectAtFirstAttempt(AssessmentTestSession assessmentTestSession) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(itemSession.key) as total,")
		  .append(" sum(case when itemSession.passed=true and itemSession.attempts=1 then 1 else 0 end) as correct")
		  .append(" from qtiassessmentitemsession itemSession")
		  .append(" where itemSession.assessmentTestSession.key=:assessmentTestSessionKey")
		  .append(" group by itemSession.assessmentTestSession.key");
		
		List<Object[]> stats = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("assessmentTestSessionKey", assessmentTestSession.getKey())
				.getResultList();
		
		double val = 0.0d;
		if(stats.size() == 1) {
			Object[] counters = stats.get(0);
			long total = PersistenceHelper.extractPrimitiveLong(counters, 0);
			long correct = PersistenceHelper.extractPrimitiveLong(counters, 1);
			if(correct > 0l) {
				val = correct / (double)total;
			}
		}
		return val;
	}
}
