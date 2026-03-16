/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceComment;
import org.olat.modules.selectus.model.references.ReferenceCommentImpl;

/**
 * 
 * Initial date: 27 oct. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ReferenceCommentDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ReferenceComment createComment(Reference reference, String comment) {
		ReferenceCommentImpl c = new ReferenceCommentImpl();
		c.setCreationDate(new Date());
		c.setComment(comment);
		c.setReference(reference);
		dbInstance.getCurrentEntityManager().persist(c);
		return c;
	}
	
	public List<ReferenceComment> getComments(Reference reference) {
		StringBuilder sb = new StringBuilder();
		sb.append("select comment from rreferencecomment comment ")
		  .append(" inner join fetch comment.reference ref")
		  .append(" where ref.key=:referenceKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ReferenceComment.class)
				.setParameter("referenceKey", reference.getKey())
				.getResultList();
	}
	
	public List<Long> getReferencesWithComments(Position position) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct ref.key from rreferencecomment comment ")
		  .append(" inner join comment.reference ref")
		  .append(" inner join ref.application app")
		  .append(" where app.position.key=:positionKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("positionKey", position.getKey())
				.getResultList();
	}
	
	public int deleteComments(Reference reference) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from rreferencecomment comment")
		  .append(" where comment.reference.key=:referenceKey");
		
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString())
			.setParameter("referenceKey", reference.getKey())
			.executeUpdate();
	}
}
