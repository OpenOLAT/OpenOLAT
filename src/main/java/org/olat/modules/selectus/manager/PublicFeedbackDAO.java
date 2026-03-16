/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.PublicFeedback;
import org.olat.modules.selectus.model.feedback.PublicFeedbackImpl;

/**
 * 
 * Initial date: 30 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PublicFeedbackDAO {
	
	@Autowired
	private DB dbInstance;
	
	public PublicFeedback createFeedback(String firstName, String lastName, String email,
			String externalId, String externalRef, Application application) {
		PublicFeedbackImpl feedback = new PublicFeedbackImpl();
		feedback.setCreationDate(new Date());
		feedback.setLastModified(feedback.getCreationDate());
		feedback.setFirstName(firstName);
		feedback.setLastName(lastName);
		feedback.setEmail(email);
		feedback.setExternalId(externalId);
		feedback.setExternalRef(externalRef);
		feedback.setApplication(application);
		dbInstance.getCurrentEntityManager().persist(feedback);
		return feedback;
	}
	
	public PublicFeedback updatePublicFeedback(PublicFeedback feedback) {
		((PublicFeedbackImpl)feedback).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(feedback);
	}
	
	public void deletePublicFeedback(PublicFeedback feedback) {
		PublicFeedback reloadedFeedback = dbInstance.getCurrentEntityManager().getReference(PublicFeedbackImpl.class, feedback.getKey());
		dbInstance.getCurrentEntityManager().remove(reloadedFeedback);
	}
	
	public int deleteApplication(Application application) {
		String q = "delete from rpublicfeedback as feedback where feedback.application.key=:applicationKey";
		return dbInstance.getCurrentEntityManager().createQuery(q)
			.setParameter("applicationKey", application.getKey())
			.executeUpdate();
	}
	
	public PublicFeedback getFeedbackBy(ApplicationRef application, String email, String externalId) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select feedback from rpublicfeedback as feedback")
		  .append(" where feedback.application.key=:applicationKey");
		if(StringHelper.containsNonWhitespace(email)) {
			sb.append(" and feedback.email=:email");
		} else if(StringHelper.containsNonWhitespace(externalId)) {
			sb.append(" and feedback.externalId=:externalId");
		}
		
		TypedQuery<PublicFeedback> feedbacksQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PublicFeedback.class)
				.setParameter("applicationKey", application.getKey());
		if(StringHelper.containsNonWhitespace(email)) {
			feedbacksQuery.setParameter("email", email);
		} else if(StringHelper.containsNonWhitespace(externalId)) {
			feedbacksQuery.setParameter("externalId", externalId);
		}
		List<PublicFeedback> feedbacks = feedbacksQuery.getResultList();
		return feedbacks == null || feedbacks.isEmpty() ? null : feedbacks.get(0);
	}
	
	public List<PublicFeedback> getFeedbacks(ApplicationRef application) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select feedback from rpublicfeedback as feedback")
		  .append(" where feedback.application.key=:applicationKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PublicFeedback.class)
				.setParameter("applicationKey", application.getKey())
				.getResultList();
	}

}
