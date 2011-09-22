/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.core.commons.services.commentAndRating;

import java.util.List;

import org.olat.core.commons.services.commentAndRating.model.UserComment;
import org.olat.core.commons.services.commentAndRating.model.UserCommentsCount;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.manager.BasicManager;

/**
 * Description:<br>
 * The user comment manager provides methods to comment any given
 * OLATResourceable object. To use this manager, make sure a proper
 * implementation for the bean
 * 'org.olat.core.commons.services.commentAndRating.UserCommentsManager' is
 * defined in the spring configuration.
 * 
 * <P>
 * Initial Date: 23.11.2009 <br>
 * 
 * @author gnaegi
 */
public abstract class UserCommentsManager extends BasicManager {
	protected OLATResourceable ores;
	protected String subpath;
	protected static UserCommentsManager instance;
	

	/**
	 * Use only inside commentAndRating package! Otherwise use CommentsAndRatingServiceFactory
	 * Factory method to create a comment manager for the given
	 * OLATResourceable. Note that this manager is statefully initialized with
	 * the OLATResourceable.
	 * 
	 * @param ores
	 *            The olat resourceable that provides a typename and a type id
	 * @param subpath
	 *            an optional string that defines any subpath. Use this when
	 *            your ores is not specific enough
	 * 
	 * @return
	 */
	public static final UserCommentsManager getInstance(OLATResourceable ores, String subpath) {
		return instance.createCommentManager(ores, subpath);
	}

	/**
	 * Factory method to create a new instance of the comment service for the given resource
	 * @param ores
	 * @param subpath
	 * @return
	 */
	protected abstract UserCommentsManager createCommentManager(OLATResourceable ores, String subpath);
	
	/**
	 * Helper method to set the olat resource for this manager
	 * 
	 * @param ores
	 * @param subpath
	 */
	public void init(OLATResourceable ores, String subpath) {
		this.ores = ores;
		this.subpath = subpath;
	}

	/**
	 * Access method to the OLATResourceable for implementations of this manager
	 * 
	 * @return
	 */
	protected final OLATResourceable getOLATResourceable() {
		return ores;
	}

	/**
	 * Access method to the sub path object for implementations of this manager
	 * 
	 * @return
	 */
	protected final String getOLATResourceableSubPath() {
		return subpath;
	}

	/**
	 * @return The number of comments for the configured resource. 0 if no
	 *         comments are available.
	 */
	public abstract Long countComments();
	
	/**
	 * @return The number of comments for the configured resource and its sub path. 0 if no
	 *         comments are available.
	 */
	public abstract List<UserCommentsCount> countCommentsWithSubPath();

	/**
	 * Get a list of user comments for the configured resource
	 * 
	 */
	public abstract List<UserComment> getComments();

	/**
	 * Create a new comment for the configured resource
	 * 
	 * @param creator
	 *            The author of the comment
	 * @param comment
	 *            The commentText
	 * @return
	 */
	public abstract UserComment createComment(Identity creator,
			String commentText);

	/**
	 * Reload the given user comment with the most recent version from the
	 * database
	 * 
	 * @return the reloaded user comment or NULL if the comment does not exist
	 *         anymore
	 */
	public abstract UserComment reloadComment(UserComment comment);

	/**
	 * Reply to an existing comment
	 * 
	 * @param originalComment
	 *            The comment to which the user replied
	 * @param creator
	 *            The author of the reply
	 * @param replyCommentText
	 *            The reply text
	 * @return The reply or NULL if the given original comment has been deleted
	 *         in the meantime
	 */
	public abstract UserComment replyTo(UserComment originalComment,
			Identity creator, String replyCommentText);

	/**
	 * Update a comment. This will first reload the comment object and then
	 * update this new object to reduce stale object issues. Make sure you
	 * replace your object in your data model with the returned user comment
	 * object.
	 * 
	 * @param comment
	 *            The comment which should be updated
	 * @param newCommentText
	 *            The updated comment text
	 * @return the updated comment object. Might be a different object than the
	 *         comment riven as attribute or NULL if the comment has been
	 *         deleted in the meantime and could not be updated at all.
	 */
	public abstract UserComment updateComment(UserComment comment, String newCommentText);

	/**
	 * Delete a comment
	 * 
	 * @param comment
	 * @param deleteReplies
	 *            true: cascade delete comment, also any existing replies;
	 *            false: don't delete replies, unlink them so they appear as new
	 *            comments
	 * @param int number of deleted comments (including replies)
	 */
	public abstract int deleteComment(UserComment comment,
			boolean deleteReplies);

	/**
	 * Delete all comments and replies for the configured resource and sub path
	 * 
	 * @return the number of deleted comments
	 */
	public abstract int deleteAllComments();

	/**
	 * Delete all comments and replies for the configured resource while ignoring
	 * the sub path. Use this to delete all comments e.g. from a blog for all blog
	 * posts in one query
	 * 
	 * @return
	 */
	public abstract int deleteAllCommentsIgnoringSubPath();

}
