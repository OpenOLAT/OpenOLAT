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
package org.olat.modules.forms.manager;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.EvaluationFormSurveyRef;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.jpa.EvaluationFormSessionImpl;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
class EvaluationFormSessionDAO {
	
	@Autowired
	private DB dbInstance;
	
	EvaluationFormSession createSession(EvaluationFormParticipation participation) {
		EvaluationFormSessionImpl session = new EvaluationFormSessionImpl();
		session.setCreationDate(new Date());
		session.setLastModified(session.getCreationDate());
		session.setParticipation(participation);
		session.setSurvey(participation.getSurvey());
		session.setEvaluationFormSessionStatus(EvaluationFormSessionStatus.inProgress);
		dbInstance.getCurrentEntityManager().persist(session);
		return session;
	}

	EvaluationFormSession loadSessionByKey(EvaluationFormSessionRef sessionRef) {
		if (sessionRef == null) return null;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select session from evaluationformsession as session");
		sb.append(" where session.key=:sessionKey");
		
		List<EvaluationFormSession> sessions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), EvaluationFormSession.class)
				.setParameter("sessionKey", sessionRef.getKey())
				.getResultList();
		
		return sessions.isEmpty()? null: sessions.get(0);
	}
	
	Long loadSessionsCount(SessionFilter filter) {

		StringBuilder sb = new StringBuilder();
		sb.append("select count(session) from evaluationformsession as session");
		sb.append(" where session.key in (").append(filter.getSelectKeys()).append(")");
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager().
				createQuery(sb.toString(), Long.class);
		filter.addParameters(query);
		
		return query.getResultList().get(0);
	}
	
	List<EvaluationFormSession> loadSessionsFiltered(SessionFilter filter, int firstResult, int maxResults,
			SortKey... orderBy) {

		StringBuilder sb = new StringBuilder();
		sb.append("select session");
		sb.append("  from evaluationformsession as session");
		if (filter.fetchSurveys()) {
			sb.append("       join fetch session.survey as survey");
		}
		if (filter.fetchExecutor()) {
			sb.append("    left join fetch session.participation as participation");
			sb.append("    left join fetch participation.executor as executor");
			sb.append("    left join fetch executor.user as user");
		}
		sb.append(" where session.key in (").append(filter.getSelectKeys()).append(")");
		
		appendOrderBy(sb, orderBy);

		TypedQuery<EvaluationFormSession> query = dbInstance.getCurrentEntityManager().
				createQuery(sb.toString(), EvaluationFormSession.class);
		filter.addParameters(query);
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		return query.getResultList();
	}
	
	private void appendOrderBy(StringBuilder sb, SortKey... orderBy) {
		if(orderBy != null && orderBy.length > 0 && orderBy[0] != null) {
			String sortKey = orderBy[0].getKey();
			boolean asc = orderBy[0].isAsc();
			sb.append(" order by ");
			sb.append(sortKey);
			appendAsc(sb, asc);
		} else {
			sb.append(" order by session.key asc ");
		}
	}
	
	private final StringBuilder appendAsc(StringBuilder sb, boolean asc) {
		if(asc) {
			sb.append(" asc");
		} else {
			sb.append(" desc");
		}
		return sb;
	}
	
	EvaluationFormSession loadSessionByParticipation(EvaluationFormParticipationRef participation) {
		if (participation == null) return null;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select session from evaluationformsession as session");
		sb.append(" where session.participation.key=:participationKey");
		
		List<EvaluationFormSession> sessions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), EvaluationFormSession.class)
				.setParameter("participationKey", participation.getKey())
				.getResultList();
		return sessions == null || sessions.isEmpty() ? null : sessions.get(0);
	}

	EvaluationFormSession updateSession(EvaluationFormSession session, String email, String firstname,
			String lastname, String age, String gender, String orgUnit, String studySubject) {
		if (session instanceof EvaluationFormSessionImpl) {
			EvaluationFormSessionImpl sessionImpl = (EvaluationFormSessionImpl) session;
			sessionImpl.setEmail(email);
			sessionImpl.setFirstname(firstname);
			sessionImpl.setLastname(lastname);
			sessionImpl.setAge(age);
			sessionImpl.setGender(gender);
			sessionImpl.setOrgUnit(orgUnit);
			sessionImpl.setStudySubject(studySubject);
			return update(sessionImpl);
		}
		return session;
	}
	
	EvaluationFormSession makeAnonymous(EvaluationFormSession session) {
		if (session instanceof EvaluationFormSessionImpl) {
			EvaluationFormSessionImpl sessionImpl = (EvaluationFormSessionImpl) session;
			sessionImpl.setParticipation(null);
			return update(sessionImpl);
		}
		return session;
	}
	
	private EvaluationFormSessionImpl update(EvaluationFormSessionImpl sessionImpl) {
		sessionImpl.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(sessionImpl);
	}
	
	boolean hasSessions(EvaluationFormSurveyRef surveyRef) {
		if (surveyRef == null) return false;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select session.key from evaluationformsession as session");
		sb.append(" where session.survey.key=:surveyKey");
		
		List<Long> sessions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("surveyKey", surveyRef.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return sessions == null || sessions.isEmpty() || sessions.get(0) == null ? false : true;
	}
	
	boolean hasSessions(RepositoryEntryRef formEntry) {
		if (formEntry == null) return false;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select session.key from evaluationformsession as session");
		sb.append(" where session.survey.formEntry.key=:formKey");
		
		List<Long> sessions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("formKey", formEntry.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return sessions == null || sessions.isEmpty() || sessions.get(0) == null ? false : true;
	}
	
	EvaluationFormSession changeStatus(EvaluationFormSession session, EvaluationFormSessionStatus newStatus) {
		if (session instanceof EvaluationFormSessionImpl) {
			EvaluationFormSessionImpl sessionImpl = (EvaluationFormSessionImpl) session;
			EvaluationFormSessionStatus oldStatus = sessionImpl.getEvaluationFormSessionStatus();
			sessionImpl.setEvaluationFormSessionStatus(newStatus);
			if (newStatus == EvaluationFormSessionStatus.done && oldStatus != EvaluationFormSessionStatus.done) {
				sessionImpl.setSubmissionDate(new Date());
				if (session.getFirstSubmissionDate() == null) {
					sessionImpl.setFirstSubmissionDate(sessionImpl.getSubmissionDate());
				}
			}
			session = dbInstance.getCurrentEntityManager().merge(sessionImpl);
		}
		return session;
	}
	
	void deleteSessions(EvaluationFormSurveyRef surveyRef) {
		if (surveyRef == null) return;
		
		StringBuilder sb = new StringBuilder();
		sb.append("delete from evaluationformsession session");
		sb.append(" where session.survey.key=:surveyKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("surveyKey", surveyRef.getKey())
				.executeUpdate();
	}

	void deleteSessions(List<? extends EvaluationFormParticipationRef> participationRefs) {
		if (participationRefs == null || participationRefs.isEmpty()) return;
		
		StringBuilder sb = new StringBuilder();
		sb.append("delete from evaluationformsession session");
		sb.append(" where session.participation.key in (:participationKeys)");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("participationKeys", getParticipationsKeys(participationRefs))
				.executeUpdate();
	}
	
	private List<Long> getParticipationsKeys(List<? extends EvaluationFormParticipationRef> participationRefs) {
		return participationRefs.stream().map(EvaluationFormParticipationRef::getKey).collect(Collectors.toList());
	}

}
