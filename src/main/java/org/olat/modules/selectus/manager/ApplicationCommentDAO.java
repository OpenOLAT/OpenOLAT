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

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationComment;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.review.ApplicationCommentImpl;

/**
 * 
 * Initial date: 13 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ApplicationCommentDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ApplicationComment createAndPersistComment(String comment, Identity author, Application application,
			Identity reviewer, ApplicationComment parentComment) {
		ApplicationCommentImpl appComment = new ApplicationCommentImpl();
		appComment.setCreationDate(new Date());
		appComment.setLastModified(appComment.getCreationDate());
		appComment.setComment(comment);
		appComment.setApplication(application);
		appComment.setAuthor(author);
		appComment.setReviewer(reviewer);
		appComment.setDeleted(false);
		if(parentComment != null) {
			appComment.setParentComment(parentComment);
		}
		dbInstance.getCurrentEntityManager().persist(appComment);
		return appComment;
	}
	
	public ApplicationComment update(ApplicationComment comment) {
		comment.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(comment);
	}
	
	public ApplicationCommentImpl markAsDelete(ApplicationComment comment) {
		ApplicationCommentImpl reloadedComment = dbInstance.getCurrentEntityManager()
			.getReference(ApplicationCommentImpl.class, comment.getKey());
		reloadedComment.setComment(null);
		reloadedComment.setDeleted(true);
		return dbInstance.getCurrentEntityManager().merge(reloadedComment);
	}
	
	public void delete(ApplicationComment comment) {
		ApplicationComment reloadedComment = dbInstance.getCurrentEntityManager()
			.getReference(ApplicationCommentImpl.class, comment.getKey());
		dbInstance.getCurrentEntityManager().remove(reloadedComment);
	}
	
	public boolean hasReply(ApplicationComment comment) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select comment.key from rapplicationcomment as comment")
		  .append(" where comment.parentComment.key=:commentKey");
		List<Long> childKeys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("commentKey", comment.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return childKeys != null && !childKeys.isEmpty() && childKeys.get(0) != null;
	}
	
	public List<ApplicationComment> getApplicationComments(ApplicationRef application) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select comment from rapplicationcomment as comment")
		  .append(" inner join fetch comment.author as author")
		  .append(" left join fetch comment.reviewer as reviewer")
		  .append(" left join fetch comment.parentComment as parent")
		  .append(" where comment.application.key=:applicationKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApplicationComment.class)
				.setParameter("applicationKey", application.getKey())
				.getResultList();
	}
	
	public List<ApplicationComment> getApplicationComments(ApplicationRef application, IdentityRef reviewer) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select comment from rapplicationcomment as comment")
		  .append(" inner join fetch comment.author as author")
		  .append(" left join fetch comment.reviewer as reviewer")
		  .append(" left join fetch comment.parentComment as parent")
		  .append(" where comment.application.key=:applicationKey and reviewer.key=:reviewerKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApplicationComment.class)
				.setParameter("applicationKey", application.getKey())
				.setParameter("reviewerKey", reviewer.getKey())
				.getResultList();
	}
	
	public List<Identity> getReviewers(PositionRef position) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select reviewer from ").append(IdentityImpl.class.getName()).append(" as reviewer")
		  .append(" inner join fetch reviewer.user as reviewerUser")
		  .append(" where exists (select comment.key from rapplicationcomment as comment")
		  .append("   inner join comment.application as commentApplication")
		  .append("   where comment.reviewer.key=reviewer.key and commentApplication.position.key=:positionKey")
		  .append(" )");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("positionKey", position.getKey())
				.getResultList();
	}
	
	public List<Identity> getAuthors(PositionRef position) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select author from ").append(IdentityImpl.class.getName()).append(" as author")
		  .append(" inner join fetch author.user as authorUser")
		  .append(" where exists (select comment.key from rapplicationcomment as comment")
		  .append("   inner join comment.application as commentApplication")
		  .append("   where comment.author.key=author.key and commentApplication.position.key=:positionKey")
		  .append(" )");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("positionKey", position.getKey())
				.getResultList();
	}
	
	public List<ApplicationComment> getAllComments(PositionRef position, int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select comment from rapplicationcomment as comment")
		  .append(" inner join fetch comment.application as application")
		  .append(" inner join fetch comment.author as author")
		  .append(" left join fetch comment.reviewer as reviewer")
		  .append(" left join fetch comment.parentComment as parent")
		  .append(" where application.position.key=:positionKey")
		  .append(" order by application.key, reviewer.key, comment.key");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApplicationComment.class)
				.setParameter("positionKey", position.getKey())
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
}
