/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.TemporalType;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.feedback.ApplicationFeedbackImpl;

/**
 * 
 * Initial date: 27 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ApplicationFeedbackDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ApplicationFeedback createFeedback(Identity identity, Application app,
			Date deadline, ApplicationsFeedbackConfiguration configuration) {
		ApplicationFeedbackImpl feedback = new ApplicationFeedbackImpl();
		feedback.setCreationDate(new Date());
		feedback.setLastModified(feedback.getCreationDate());
		feedback.setReferenceStatus(ReferenceStatus.notSent);
		feedback.setDeadline(deadline);
		feedback.setIdentity(identity);
		feedback.setApplication(app);
		feedback.setConfiguration(configuration);
		dbInstance.getCurrentEntityManager().persist(feedback);
		return feedback;
	}
	
	public ApplicationFeedback updateFeedback(ApplicationFeedback feedback) {
		feedback.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(feedback);
	}
	
	public void deleteFeedback(ApplicationFeedback feedback) {
		dbInstance.getCurrentEntityManager().remove(feedback);
	}
	
	public int deleteApplication(Application application) {
		String q = "delete from rappfeedback as feedback where feedback.application.key=:applicationKey";
		return dbInstance.getCurrentEntityManager().createQuery(q)
			.setParameter("applicationKey", application.getKey())
			.executeUpdate();
	}
	
	public boolean hasFeedback(IdentityRef identity, ApplicationRef app,
			ApplicationsFeedbackConfiguration configuration) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select feedback.key from rappfeedback as feedback")
		  .append(" where feedback.identity.key=:identityKey")
		  .append(" and feedback.application.key=:applicationKey")
		  .append(" and feedback.configuration.key=:configurationKey");
		
		List<Long> feedbacks = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("applicationKey", app.getKey())
				.setParameter("configurationKey", configuration.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return feedbacks != null && !feedbacks.isEmpty()
				&& feedbacks.get(0) != null && feedbacks.get(0) > 0;
	}
	
	public boolean hasFeedbackOpen(IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select feedback.key from rappfeedback as feedback")
		  .append(" where feedback.identity.key=:identityKey");
		
		List<Long> feedbacks = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		return feedbacks != null && !feedbacks.isEmpty()
				&& feedbacks.get(0) != null && feedbacks.get(0) > 0;
	}
	
	public boolean hasFeedbackOpen(ApplicationRef application) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select feedback.key from rappfeedback as feedback")
		  .append(" where feedback.application.key=:applicationKey");
		
		List<Long> feedbacks = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("applicationKey", application.getKey())
				.getResultList();
		return feedbacks != null && !feedbacks.isEmpty()
				&& feedbacks.get(0) != null && feedbacks.get(0) > 0;
	}
	
	public ApplicationFeedback loadByKey(ApplicationFeedback feedback) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select feedback from rappfeedback as feedback")
		  .append(" inner join fetch feedback.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" inner join fetch feedback.application as app")
		  .append(" inner join fetch feedback.configuration as config")
		  .append(" where feedback.key=:feedbackKey");
		
		List<ApplicationFeedback> feedbacks = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApplicationFeedback.class)
				.setParameter("feedbackKey", feedback.getKey())
				.getResultList();
		return feedbacks != null && !feedbacks.isEmpty() ? feedbacks.get(0) : null;
	}
	
	public List<ApplicationFeedback> searchApplicationFeedback(ReferenceStatus status, Date limitDeadline) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select feedback from rappfeedback as feedback")
		  .append(" inner join fetch feedback.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" inner join fetch feedback.configuration as config")
		  .append(" where feedback.status=:status")
		  .append(" and (feedback.deadline<:limitDeadline or ")
		  .append("  (feedback.deadline is null and config.deadline<:limitDeadline)")
		  .append(" )");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApplicationFeedback.class)
				.setParameter("status", status.name())
				.setParameter("limitDeadline", limitDeadline, TemporalType.TIMESTAMP)
				.getResultList();
	}
	
	public List<ApplicationFeedback> loadByApplication(ApplicationRef application) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select feedback from rappfeedback as feedback")
		  .append(" inner join fetch feedback.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" inner join fetch feedback.configuration as config")
		  .append(" where feedback.application.key=:applicationKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApplicationFeedback.class)
				.setParameter("applicationKey", application.getKey())
				.getResultList();
	}
	
	public List<ApplicationFeedback> loadByApplications(List<? extends ApplicationRef> applications) {
		if(applications == null || applications.isEmpty()) {
			return new ArrayList<>();
		}
		
		List<Long> applicationKeys = applications.stream()
				.map(ApplicationRef::getKey)
				.collect(Collectors.toList());
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select feedback from rappfeedback as feedback")
		  .append(" inner join fetch feedback.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" inner join fetch feedback.configuration as config")
		  .append(" where feedback.application.key in (:applicationKeys)");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApplicationFeedback.class)
				.setParameter("applicationKeys", applicationKeys)
				.getResultList();
	}
	
	public List<ApplicationFeedback> loadByPosition(PositionRef position) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select feedback from rappfeedback as feedback")
		  .append(" inner join fetch feedback.application as app")
		  .append(" inner join fetch feedback.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" inner join fetch feedback.configuration as config")
		  .append(" where app.position.key=:positionKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApplicationFeedback.class)
				.setParameter("positionKey", position.getKey())
				.getResultList();
	}
	
	public List<ApplicationFeedback> loadByMember(IdentityRef member) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select feedback from rappfeedback as feedback")
		  .append(" inner join fetch feedback.application as app")
		  .append(" inner join fetch app.position as pos")
		  .append(" left join fetch pos.organisation as org")
		  .append(" inner join fetch feedback.configuration as config")
		  .append(" where feedback.identity.key=:memberKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApplicationFeedback.class)
				.setParameter("memberKey", member.getKey())
				.getResultList();
	}
	
	public List<ApplicationFeedback> getFeedbacks(ApplicationRef app) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select feedback from rappfeedback as feedback")
		  .append(" inner join feedback.identity as ident")
		  .append(" inner join ident.user as identUser")
		  .append(" where feedback.application.key=:applicationKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApplicationFeedback.class)
				.setParameter("applicationKey", app.getKey())
				.getResultList();
	}

}
