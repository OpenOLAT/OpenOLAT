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

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.model.jpa.EvaluationFormSessionImpl;
import org.olat.modules.portfolio.PageBody;
import org.olat.repository.RepositoryEntry;
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
public class EvaluationFormSessionDAO {
	
	@Autowired
	private DB dbInstance;
	
	public EvaluationFormSession createSession(EvaluationFormParticipation participation) {
		EvaluationFormSessionImpl session = new EvaluationFormSessionImpl();
		session.setCreationDate(new Date());
		session.setLastModified(session.getCreationDate());
		session.setParticipation(participation);
		session.setSurvey(participation.getSurvey());
		session.setEvaluationFormSessionStatus(EvaluationFormSessionStatus.inProgress);
		dbInstance.getCurrentEntityManager().persist(session);
		return session;
	}
	
	public EvaluationFormSession createSessionForPortfolio(Identity identity, PageBody body, RepositoryEntry formEntry) {
		EvaluationFormSessionImpl session = createSession(identity, formEntry);
		session.setPageBody(body);
		dbInstance.getCurrentEntityManager().persist(session);
		return session;
	}

	private EvaluationFormSessionImpl createSession(Identity identity, RepositoryEntry formEntry) {
		EvaluationFormSessionImpl session = new EvaluationFormSessionImpl();
		session.setCreationDate(new Date());
		session.setLastModified(session.getCreationDate());
		session.setIdentity(identity);
		session.setFormEntry(formEntry);
		session.setEvaluationFormSessionStatus(EvaluationFormSessionStatus.inProgress);
		return session;
	}

	EvaluationFormSession loadSessionByParticipation(EvaluationFormParticipation participation) {
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
	
	boolean hasSessions(EvaluationFormSurvey survey) {
		if (survey == null) return false;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select session.key from evaluationformsession as session");
		sb.append(" where session.survey.key=:surveyKey");
		
		List<Long> sessions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("surveyKey", survey.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return sessions == null || sessions.isEmpty() || sessions.get(0) == null ? false : true;
	}
	
	public boolean hasSessionForPortfolioEvaluation(PageBody anchor) {
		StringBuilder sb = new StringBuilder();
		sb.append("select session.key from evaluationformsession as session")
		  .append(" where session.pageBody.key=:bodyKey");
		List<Long> sessions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("bodyKey", anchor.getKey())
				.getResultList();
		return sessions == null || sessions.isEmpty() || sessions.get(0) == null ? false : sessions.get(0).longValue() > 0;
	}
	
	public EvaluationFormSession getSessionForPortfolioEvaluation(IdentityRef identity, PageBody anchor) {
		StringBuilder sb = new StringBuilder();
		sb.append("select session from evaluationformsession as session")
		  .append(" where session.identity.key=:identityKey and session.pageBody.key=:bodyKey");
		List<EvaluationFormSession> sessions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), EvaluationFormSession.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("bodyKey", anchor.getKey())
				.getResultList();
		return sessions == null || sessions.isEmpty() ? null : sessions.get(0);
	}
	
	public EvaluationFormSession changeStatusOfSessionForPortfolioEvaluation(IdentityRef identity, PageBody anchor, EvaluationFormSessionStatus status) {
		EvaluationFormSession session = getSessionForPortfolioEvaluation(identity, anchor);
		return changeStatus(session, status);
	}
	
	public EvaluationFormSession changeStatus(EvaluationFormSession session, EvaluationFormSessionStatus newStatus) {
		if(session != null) {
			if(newStatus == EvaluationFormSessionStatus.done && session.getEvaluationFormSessionStatus() != EvaluationFormSessionStatus.done) {
				((EvaluationFormSessionImpl)session).setSubmissionDate(new Date());
				if(session.getFirstSubmissionDate() == null) {
					((EvaluationFormSessionImpl)session).setFirstSubmissionDate(session.getSubmissionDate());
				}
			}
			session.setEvaluationFormSessionStatus(newStatus);
			dbInstance.getCurrentEntityManager().merge(session);
		}
		return session;
	}
	
	public int deleteSessionForPortfolioEvaluation(PageBody anchor) {
		//delete responses
		int rows = 0;
		StringBuilder responseQ = new StringBuilder();
		responseQ.append("delete from evaluationformresponse as response where response.session.key in (")
                 .append(" select session.key from evaluationformsession session where session.pageBody.key=:bodyKey")
		         .append(")");
		rows += dbInstance.getCurrentEntityManager()
				.createQuery(responseQ.toString())
				.setParameter("bodyKey", anchor.getKey())
				.executeUpdate();
		
		// delete sessions
		String sessionQ = "delete from evaluationformsession session where session.pageBody.key=:bodyKey";
		rows += dbInstance.getCurrentEntityManager()
				.createQuery(sessionQ)
				.setParameter("bodyKey", anchor.getKey())
				.executeUpdate();
		return rows;
	}
	
	public boolean isInUse(RepositoryEntryRef formEntry) {
		String sb = "select session.key from evaluationformsession as session where session.formEntry.key=:formEntryKey";
		List<Long> sessions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("formEntryKey", formEntry.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return sessions == null || sessions.isEmpty() || sessions.get(0) == null ? false : true;
	}

}
