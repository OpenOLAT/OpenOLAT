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
package org.olat.core.commons.services.commentAndRating.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingLoggingAction;
import org.olat.core.commons.services.commentAndRating.UserCommentsDelegate;
import org.olat.core.commons.services.commentAndRating.model.UserComment;
import org.olat.core.commons.services.commentAndRating.model.UserCommentImpl;
import org.olat.core.commons.services.commentAndRating.model.UserCommentsCount;
import org.olat.core.commons.services.commentAndRating.model.UserCommentsCountImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 13.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("userCommentsDAO")
public class UserCommentsDAO {

	@Autowired
	private DB dbInstance;
	
	private List<UserCommentsDelegate> delegates = new ArrayList<>();
	
	public void addDelegate(UserCommentsDelegate delegate) {
		delegates.add(delegate);
	}
	

	public UserComment createComment(Identity creator, OLATResourceable ores, String resSubPath, String commentText) {
		UserComment comment = new UserCommentImpl(ores, resSubPath, creator, commentText);
		dbInstance.getCurrentEntityManager().persist(comment);
		updateDelegateRatings(ores, resSubPath, true);
		
		// do Logging
		ThreadLocalUserActivityLogger.log(CommentAndRatingLoggingAction.COMMENT_CREATED, getClass(),
				CoreLoggingResourceable.wrap(ores, OlatResourceableType.feedItem));
		return comment;
	}

	public UserComment reloadComment(UserComment comment) {
		String q = "select comment from usercomment as comment where comment.key=:commentKey";
		List<UserComment> comments = dbInstance.getCurrentEntityManager()
		 		.createQuery(q, UserComment.class)
		 		.setParameter("commentKey", comment.getKey())
		 		.getResultList();
		return comments.isEmpty() ? null : comments.get(0);
	}
	
	public UserComment replyTo(UserComment originalComment, Identity creator, String replyCommentText) {
		// First reload parent from cache to prevent stale object or cache issues
		originalComment = reloadComment(originalComment);
		if (originalComment == null) {
			// Original comment has been deleted in the meantime. Don't create a reply
			return null;
		}
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(originalComment.getResName(), originalComment.getResId());
		UserCommentImpl reply = new UserCommentImpl(ores, originalComment.getResSubPath(), creator, replyCommentText);
		reply.setParent(originalComment);
		dbInstance.getCurrentEntityManager().persist(reply);
		updateDelegateRatings(ores, originalComment.getResSubPath(), true);
		return reply;
	}

	public List<UserComment> getComments(OLATResourceable ores, String resSubPath) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select comment from usercomment as comment")
		  .append(" left join fetch comment.creator as creatorIdent")
		  .append(" left join fetch creatorIdent.user as creatorUser")
		  .append(" where resName=:resname and resId=:resId");
		if (resSubPath == null) {
			sb.append(" and resSubPath is null");
		} else {
			sb.append(" and resSubPath=:resSubPath");
		}

		TypedQuery<UserComment> query = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), UserComment.class)
					.setParameter("resname", ores.getResourceableTypeName())
				    .setParameter("resId", ores.getResourceableId());
		if(resSubPath != null) {
			query.setParameter("resSubPath", resSubPath);
		}
		return query.getResultList();
	}
	
	public List<UserComment> getComments(IdentityRef identity) {
		String  query = "select comment from usercomment as comment where comment.creator.key=:identityKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, UserComment.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}

	public UserComment updateComment(UserComment comment, String newCommentText) {
		// First reload parent from cache to prevent stale object or cache issues
		comment = reloadComment(comment);
		if (comment == null) {
			// Original comment has been deleted in the meantime. Don't update it
			return null;
		}
		// Update DB entry
		comment.setComment(newCommentText);
		return dbInstance.getCurrentEntityManager().merge(comment);
	}


	public int deleteComment(UserComment comment, boolean deleteReplies) {
		int counter = 0;
		// First reload parent from cache to prevent stale object or cache issues
		comment = reloadComment(comment);
		if (comment == null) {
			// Original comment has been deleted in the meantime. Don't delete it again.
			return 0;
		}
		
		// First deal with all direct replies
		List<UserComment> replies = dbInstance.getCurrentEntityManager()
				.createQuery("select comment from usercomment as comment where parent=:parent", UserComment.class)
				.setParameter("parent", comment)
				.getResultList();
		if (deleteReplies) {
			// Since we have a many-to-one we first have to recursively delete
			// the replies to prevent foreign key constraints
			for (UserComment reply : replies) {
				counter += deleteComment(reply, true);
			}
		} else {
			// To not delete the replies we have to set the parent to the parent
			// of the original comment for each reply
			for (UserComment reply : replies) {
				reply.setParent(comment.getParent());
				dbInstance.getCurrentEntityManager().merge(reply);
			}
		}
		// Now delete this comment and finish
		dbInstance.getCurrentEntityManager().remove(comment);
		// do Logging
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(comment.getResName(), comment.getResId());
		updateDelegateRatings(ores, comment.getResSubPath(), true);
		ThreadLocalUserActivityLogger.log(CommentAndRatingLoggingAction.COMMENT_DELETED, getClass(),
				CoreLoggingResourceable.wrap(ores, OlatResourceableType.feedItem));
		return counter+1;
	}

	public List<UserCommentsCount> countCommentsWithSubPath(OLATResourceable ores, String resSubPath) {
		if (resSubPath != null) {
			UserCommentsCount count = new UserCommentsCountImpl(ores, resSubPath, countComments(ores, resSubPath));
			return Collections.singletonList(count);
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select comment.resSubPath, count(comment.key) from usercomment as comment ")
			.append(" where comment.resName=:resname AND comment.resId=:resId")
			.append(" group by comment.resSubPath");
		
		List<Object[]> counts = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("resname", ores.getResourceableTypeName())
				.setParameter("resId", ores.getResourceableId())
				.getResultList();
		
		Set<String> countMap = new HashSet<>();
		List<UserCommentsCount> countList = new ArrayList<>();
		for(Object[] count:counts) {
			Object subPath = count[0] == null ? "" : count[0];
			if(!countMap.contains(subPath)) {
				UserCommentsCount c = new UserCommentsCountImpl(ores, (String)count[0], (Long)count[1]);
				countList.add(c);
			}
		}
		return countList;
	}

	public long countComments(OLATResourceable ores, String resSubPath) {
		TypedQuery<Number> query;
		if (resSubPath == null) {
			// special query when sub path is null
			query = dbInstance.getCurrentEntityManager()
					.createQuery("select count(*) from usercomment where resName=:resname AND resId=:resId AND resSubPath is NULL", Number.class);
		} else {
			query = dbInstance.getCurrentEntityManager()
					.createQuery("select count(*) from usercomment where resName=:resname AND resId=:resId AND resSubPath=:resSubPath", Number.class)
					.setParameter("resSubPath", resSubPath);
		}
		return query.setParameter("resname", ores.getResourceableTypeName())
			.setParameter("resId", ores.getResourceableId())
			.getSingleResult().longValue();
	}
	
	public int deleteAllComments(IdentityRef identity) {
		Set<Long> commentWithReplyKeys = getCommentKeysWithReply(identity);
		
		String query = "select comment.key from usercomment comment where comment.creator.key=:creatorKey";
		List<Long> commentKeys = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("creatorKey", identity.getKey())
				.getResultList();

		int count = 0;
		for(Long commentKey:commentKeys) {
			UserComment comment = dbInstance.getCurrentEntityManager()
					.getReference(UserCommentImpl.class, commentKey);
			if(commentWithReplyKeys.contains(commentKey)) {
				comment.setComment("User has been deleted");
				dbInstance.getCurrentEntityManager().merge(comment);
			} else {
				dbInstance.getCurrentEntityManager().remove(comment);
			}
			
			if(count++ % 25 == 0) {
				dbInstance.commitAndCloseSession();
			} else {
				dbInstance.commit();
			}

			OLATResourceable ores = OresHelper.createOLATResourceableInstance(comment.getResName(), comment.getResId());
			updateDelegateRatings(ores, comment.getResSubPath(), false);
		}
		return commentKeys.size();
	}
	
	private Set<Long> getCommentKeysWithReply(IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select comment.key from usercomment as comment")
		  .append(" inner join usercomment as reply on (comment.key=reply.parent.key)")
		  .append(" where comment.creator.key=:creatorKey");
		List<Long> commentWithReplyKeyList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("creatorKey", identity.getKey())
				.getResultList();
		return new HashSet<>(commentWithReplyKeyList);
	}
	
	public int deleteAllComments(OLATResourceable ores, String resSubPath) {
		EntityManager em = dbInstance.getCurrentEntityManager();
		// special query when sub path is null
		List<UserCommentImpl> comments;
		if (resSubPath == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("select comment from usercomment comment")
			  .append(" where resName=:resName and resId=:resId and resSubPath is null")
			  .append(" order by creationDate desc");
			
			comments = em.createQuery(sb.toString(), UserCommentImpl.class)
				.setParameter("resName", ores.getResourceableTypeName())
				.setParameter("resId", ores.getResourceableId())
				.getResultList();
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("select comment from usercomment comment")
			  .append(" where resName=:resName and resId=:resId and resSubPath=:resSubPath")
			  .append(" order by creationDate desc");
			
			comments = em.createQuery(sb.toString(), UserCommentImpl.class)
					.setParameter("resName", ores.getResourceableTypeName())
					.setParameter("resId", ores.getResourceableId())
					.setParameter("resSubPath",  resSubPath)
					.getResultList();
		}
		
		if(comments != null && !comments.isEmpty()) {
			for(UserCommentImpl comment:comments) {
				em.remove(comment);
			}
		}
		updateDelegateRatings(ores, resSubPath, false);
		return comments == null ? 0 : comments.size();
	}

	/**
	 * Don't limit to subpath. Ignore if null or not, just delete on the resource
	 * @see org.olat.core.commons.services.commentAndRating.UserCommentsManager#deleteAllCommentsIgnoringSubPath()
	 */
	public int deleteAllCommentsIgnoringSubPath(OLATResourceable ores) {
		EntityManager em = dbInstance.getCurrentEntityManager();
		StringBuilder sb = new StringBuilder();
		sb.append("select comment from usercomment comment")
		  .append(" where resName=:resName and resId=:resId")
		  .append(" order by creationDate desc");

		List<UserCommentImpl> comments = em.createQuery(sb.toString(), UserCommentImpl.class)
			.setParameter("resName", ores.getResourceableTypeName())
			.setParameter("resId", ores.getResourceableId())
			.getResultList();
		for(UserCommentImpl comment:comments) {
			em.remove(comment);
		}
		updateDelegateRatings(ores, null, false);
		return comments.size();
	}
	
	private void updateDelegateRatings(OLATResourceable ores, String resSubPath, boolean newRating) {
		if(delegates == null || delegates.isEmpty()) return;

		for(UserCommentsDelegate delegate:delegates) {
			if(delegate.accept(ores, resSubPath)) {
				StringBuilder sb = new StringBuilder();
				sb.append("select count(comment.key) from usercomment as comment")
				  .append(" where comment.resName=:resname and comment.resId=:resId");
				if(resSubPath == null) {
					sb.append(" and comment.resSubPath is null");
				} else {
					sb.append(" and comment.resSubPath=:resSubPath");
				}
	
				TypedQuery<Number> query = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), Number.class)
				     .setParameter("resname", ores.getResourceableTypeName())
				     .setParameter("resId", ores.getResourceableId());
				if(resSubPath!= null) {
					query.setParameter("resSubPath", resSubPath);
				}
		
				Number count = query.getSingleResult();
				if(count == null) {
					int val = newRating ? 1 : 0;
					delegate.update(ores, resSubPath, val);
				} else {
					delegate.update(ores, resSubPath, count.intValue());
				}
			}
		}
	}
}
