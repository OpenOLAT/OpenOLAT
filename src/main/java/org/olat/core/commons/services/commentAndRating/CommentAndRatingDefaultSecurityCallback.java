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
package org.olat.core.commons.services.commentAndRating;

import java.util.List;

import org.olat.core.commons.services.commentAndRating.model.UserComment;
import org.olat.core.id.Identity;

/**
 * Description:<br>
 * A simple default implementation that allows normal users to crate new
 * comments, edit and deletes their own comments when there are not replies, and
 * allow moderators to do everything everytime. Dito for user ratings.
 * <p>
 * Guests are allowed to view but not create any data.
 * <P>
 * Initial Date: 24.11.2009 <br>
 * 
 * @author gnaegi
 */
public class CommentAndRatingDefaultSecurityCallback implements
		CommentAndRatingSecurityCallback {
	private Identity identity;
	private boolean isModerator;
	private boolean isAnonymous;

	/**
	 * Constructor to create a default security callback
	 * 
	 * @param identity
	 *            The current identity
	 * @param isModerator
	 *            true: this user has administrative capabilities; false: this
	 *            is a normal user
	 * @param isAnonymous
	 *            true: user is unknown, e.g. a guest; false: user is known
	 */
	public CommentAndRatingDefaultSecurityCallback(Identity identity,
			boolean isModerator, boolean isAnonymous) {
		this.identity = identity;
		this.isModerator = isModerator;
		this.isAnonymous = isAnonymous;
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback#canCreateComments()
	 */
	@Override
	public boolean canCreateComments() {
		if (isAnonymous) 
			return false;
		else 
			return true;
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback#canViewComments()
	 */
	@Override
	public boolean canViewComments() {
		// Even anonymous users can view comments
		return true;
	}


	/**
	 * @see org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback#canReplyToComment(org.olat.core.commons.services.commentAndRating.model.UserComment)
	 */
	@Override
	public boolean canReplyToComment(UserComment comment) {
		if (isAnonymous) 
			return false;
		if (comment == null)
			return false;
		// Don't allow reply to own comments
		if (comment.getCreator().equalsByPersistableKey(identity)) {
			return false;
		}
		return true;
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback#canDeleteComment(org.olat.core.commons.services.commentAndRating.model.UserComment)
	 */
	@Override
	public boolean canDeleteComment(UserComment comment) {
		if (comment == null)
			return false;
		// Anonymous user rule
		if (isAnonymous) 
			return false;
		// Moderator rule
		if (isModerator)
			return true;
		// Normal user rule: is allowed when there are no replies
		if (!comment.getCreator().equalsByPersistableKey(identity)) {
			return false;
		}
		return true;
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback#canUpdateComment(org.olat.core.commons.services.commentAndRating.model.UserComment, java.util.List)
	 */
	@Override
	public boolean canUpdateComment(UserComment comment, List<UserComment> allComments) {
		if (comment == null)
			return false;
		// Anonymous user rule
		if (isAnonymous) 
			return false;
		// Moderator rule
		if (isModerator)
			return true;
		// Normal user rule: is allowed when there are no replies
		if (!comment.getCreator().equalsByPersistableKey(identity)) {
			return false;
		}
		for (UserComment replyComment : allComments) {
			if (replyComment.getParent() != null && replyComment.getParent().getKey().equals(comment.getKey())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback#canRate()
	 */
	@Override
	public boolean canRate() {
		if (isAnonymous) 
			return false;
		else
			return true;
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback#canViewOtherUsersRatings()
	 */
	@Override
	public boolean canViewOtherUsersRatings() {
		// Moderator rule
		if (isModerator)
			return true;
		// Everybody else (users and anonymous users)
		else 
			return false;
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback#canViewRatingAverage()
	 */
	@Override
	public boolean canViewRatingAverage() {
		// Even anonymous users can view the rating average
		return true;
	}

}
