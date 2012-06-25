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
package org.olat.core.commons.services.commentAndRating.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingLoggingAction;
import org.olat.core.commons.services.commentAndRating.UserCommentsManager;
import org.olat.core.commons.services.commentAndRating.model.UserComment;
import org.olat.core.commons.services.commentAndRating.model.UserCommentsCount;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;

/**
 * Description:<br>
 * This implementation of the user comments manager is database based.
 * 
 * <P>
 * Initial Date: 23.11.2009 <br>
 * 
 * @author gnaegi
 */
public class UserCommentsManagerImpl extends UserCommentsManager {

	/**
	 * [spring]
	 */
	private UserCommentsManagerImpl() {
		instance = this;
	}
	

	/**
	 * @see org.olat.core.commons.services.commentAndRating.UserCommentsManager#createCommentManager(org.olat.core.id.OLATResourceable,
	 *      java.lang.String)
	 */
	@Override
	protected UserCommentsManager createCommentManager(OLATResourceable ores,
			String subpath) {
		UserCommentsManager manager = new UserCommentsManagerImpl();
		manager.init(ores, subpath);
		return manager;
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.UserCommentsManager#countComments()
	 */
	@Override
	public Long countComments() {
		DBQuery query;
		if (getOLATResourceableSubPath() == null) {
			// special query when sub path is null
			query = DBFactory
					.getInstance()
					.createQuery(
							"select count(*) from UserCommentImpl where resName=:resname AND resId=:resId AND resSubPath is NULL");
		} else {
			query = DBFactory
					.getInstance()
					.createQuery(
							"select count(*) from UserCommentImpl where resName=:resname AND resId=:resId AND resSubPath=:resSubPath");
			query.setString("resSubPath", getOLATResourceableSubPath());
		}
		query.setString("resname", getOLATResourceable()
				.getResourceableTypeName());
		query.setLong("resId", getOLATResourceable().getResourceableId());
		query.setCacheable(true);
		//
		Long count = (Long) query.list().get(0);
		return count;
	}
	
	/**
	 * @see org.olat.core.commons.services.commentAndRating.UserCommentsManager#countCommentsWithSubPath()
	 */
	@Override
	public List<UserCommentsCount> countCommentsWithSubPath() {
		if (getOLATResourceableSubPath() != null) {
			UserCommentsCount count = new UserCommentsCountImpl(getOLATResourceable(), getOLATResourceableSubPath(), countComments());
			return Collections.singletonList(count);
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select comment.resSubPath, count(comment.key) from ").append(UserCommentImpl.class.getName()).append(" as comment ")
			.append(" where comment.resName=:resname AND comment.resId=:resId")
			.append(" group by comment.resSubPath");
		
		DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
		query.setString("resname", getOLATResourceable().getResourceableTypeName());
		query.setLong("resId", getOLATResourceable().getResourceableId());
		
		Set<String> countMap = new HashSet<String>();
		List<Object[]> counts = query.list();
		List<UserCommentsCount> countList = new ArrayList<UserCommentsCount>();
		for(Object[] count:counts) {
			Object subPath = count[0] == null ? "" : count[0];
			if(!countMap.contains(subPath)) {
				UserCommentsCount c = new UserCommentsCountImpl(getOLATResourceable(), (String)count[0], (Long)count[1]);
				countList.add(c);
			}
		}
		return countList;
	}


	/**
	 * @see org.olat.core.commons.services.commentAndRating.UserCommentsManager#createComment(org.olat.core.id.Identity,
	 *      java.lang.String)
	 */
	@Override
	public UserComment createComment(Identity creator, String commentText) {
		UserComment comment = new UserCommentImpl(getOLATResourceable(),
				getOLATResourceableSubPath(), creator, commentText);
		DBFactory.getInstance().saveObject(comment);
		// do Logging
		ThreadLocalUserActivityLogger.log(CommentAndRatingLoggingAction.COMMENT_CREATED, getClass(),
				CoreLoggingResourceable.wrap(getOLATResourceable(), OlatResourceableType.feedItem));
		return comment;
	}
	
	/**
	 * @see org.olat.core.commons.services.commentAndRating.UserCommentsManager#reloadComment(org.olat.core.commons.services.commentAndRating.model.UserComment)
	 */
	public UserComment reloadComment(UserComment comment) {
		try {
			DB db = DBFactory.getInstance();
			return (UserComment) db.loadObject(comment);			
		} catch (Exception e) {
			// Huh, most likely the given object does not exist anymore on the
			// db, probably deleted by someone else
			logWarn("Tried to reload a user comment but got an exception. Probably deleted in the meantime", e);
			return null;
		}		
	}


	/**
	 * @see org.olat.core.commons.services.commentAndRating.UserCommentsManager#replyTo(org.olat.core.commons.services.commentAndRating.model.UserComment,
	 *      org.olat.core.id.Identity, java.lang.String)
	 */
	@Override
	public UserComment replyTo(UserComment originalComment, Identity creator,
			String replyCommentText) {
		if (!isCommentOfResource(originalComment)) {
			throw new AssertException(
					"This user comment manager is initialized for another resource than the given comment.");
		}
		// First reload parent from cache to prevent stale object or cache issues
		originalComment = reloadComment(originalComment);
		if (originalComment == null) {
			// Original comment has been deleted in the meantime. Don't create a reply
			return null;
		}
		UserCommentImpl reply = new UserCommentImpl(getOLATResourceable(),
				getOLATResourceableSubPath(), creator, replyCommentText);
		reply.setParent(originalComment);
		DBFactory.getInstance().saveObject(reply);
		return reply;
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.UserCommentsManager#getComments()
	 */
	@Override
	public List<UserComment> getComments() {
		DBQuery query;
		if (getOLATResourceableSubPath() == null) {
			// special query when sub path is null
			query = DBFactory
					.getInstance()
					.createQuery(
							"select comment from UserCommentImpl as comment where resName=:resname AND resId=:resId AND resSubPath is NULL");
		} else {
			query = DBFactory
					.getInstance()
					.createQuery(
							"select comment from UserCommentImpl as comment where resName=:resname AND resId=:resId AND resSubPath=:resSubPath");
			query.setString("resSubPath", getOLATResourceableSubPath());
		}
		query.setString("resname", getOLATResourceable()
				.getResourceableTypeName());
		query.setLong("resId", getOLATResourceable().getResourceableId());
		query.setCacheable(true);
		//
		List<UserComment> results = query.list();
		return results;
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.UserCommentsManager#updateComment(org.olat.core.commons.services.commentAndRating.model.UserComment)
	 */
	@Override
	public UserComment updateComment(UserComment comment, String newCommentText) {
		if (!isCommentOfResource(comment)) {
			throw new AssertException(
					"This user comment manager is initialized for another resource than the given comment.");
		}
		// First reload parent from cache to prevent stale object or cache issues
		comment = reloadComment(comment);
		if (comment == null) {
			// Original comment has been deleted in the meantime. Don't update it
			return null;
		}
		// Update DB entry
		comment.setComment(newCommentText);
		DB db = DBFactory.getInstance();
		db.updateObject(comment);
		return comment;
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.UserCommentsManager#deleteComment(org.olat.core.commons.services.commentAndRating.model.UserComment, boolean)
	 */
	@Override
	public int deleteComment(UserComment comment, boolean deleteReplies) {
		if (!isCommentOfResource(comment)) {
			throw new AssertException(
					"This user comment manager is initialized for another resource than the given comment.");
		}
		int counter = 0;
		// First reload parent from cache to prevent stale object or cache issues
		comment = reloadComment(comment);
		if (comment == null) {
			// Original comment has been deleted in the meantime. Don't delete it again.
			return 0;
		}
		DB db = DBFactory.getInstance();
		// First deal with all direct replies
		DBQuery query = db.createQuery("select comment from UserCommentImpl as comment where parent=:parent");
		query.setEntity("parent", comment);
		List<UserComment> replies = query.list();
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
				db.updateObject(reply);
			}
		}
		// Now delete this comment and finish
		db.deleteObject(comment);
		// do Logging
		ThreadLocalUserActivityLogger.log(CommentAndRatingLoggingAction.COMMENT_DELETED, getClass(),
				CoreLoggingResourceable.wrap(getOLATResourceable(), OlatResourceableType.feedItem));
		return counter+1;
	}
	
	/**
	 * @see org.olat.core.commons.services.commentAndRating.UserCommentsManager#deleteAllComments()
	 */
	public int deleteAllComments() {
		DB db = DBFactory.getInstance();
		String query;
		Object[] values;
		Type[] types;
		// special query when sub path is null
		if (getOLATResourceableSubPath() == null) {
			query = "from UserCommentImpl where resName=? AND resId=? AND resSubPath is NULL";
			values = new Object[] { getOLATResourceable().getResourceableTypeName(),  getOLATResourceable().getResourceableId() };
			types = new Type[] {StandardBasicTypes.STRING, StandardBasicTypes.LONG};
		} else {
			query = "from UserCommentImpl where resName=? AND resId=? AND resSubPath=?";
			values = new Object[] { getOLATResourceable().getResourceableTypeName(),  getOLATResourceable().getResourceableId(), getOLATResourceableSubPath() };
			types = new Type[] {StandardBasicTypes.STRING, StandardBasicTypes.LONG, StandardBasicTypes.STRING};
		}
		return db.delete(query, values, types);
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.UserCommentsManager#deleteAllCommentsIgnoringSubPath()
	 */
	public int deleteAllCommentsIgnoringSubPath() {
		// Don't limit to subpath. Ignore if null or not, just delete on the resource
		String query = "from UserCommentImpl where resName=? AND resId=?";
		Object[] values = new Object[] { getOLATResourceable().getResourceableTypeName(),  getOLATResourceable().getResourceableId() };
		Type[] types = new Type[] {StandardBasicTypes.STRING, StandardBasicTypes.LONG};
		DB db = DBFactory.getInstance();
		return db.delete(query, values, types);		
	}

	
	/**
	 * Helper method to check if the given comment has the same resource and
	 * path as configured for this manager
	 * 
	 * @param originalComment
	 * @return
	 */
	private boolean isCommentOfResource(UserComment originalComment) {
		if (this.getOLATResourceable().getResourceableId().equals(
				originalComment.getResId())
				&& this.getOLATResourceable().getResourceableTypeName().equals(
						originalComment.getResName())) {
			// check on resource subpath: can be null
			if (this.getOLATResourceableSubPath() == null) {
				return (originalComment.getResSubPath() == null);
			} else {
				return this.getOLATResourceableSubPath().equals(
						originalComment.getResSubPath());
			}
		}
		return false;
	}

}
