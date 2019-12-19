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

/**
 * Description:<br>
 * Security callback for user comments and ratings
 * 
 * <P>
 * Initial Date: 24.11.2009 <br>
 * 
 * @author gnaegi
 */
public interface CommentAndRatingSecurityCallback {

	/**
	 * @return true: user is allowed to view comments; false: user is not
	 *         allowed to view comments.
	 */
	public boolean canViewComments();

	/**
	 * @return true: user can create new comments; false: no create
	 *         functionality
	 */
	public boolean canCreateComments();

	/**
	 * @param comment
	 *            The comment to be replied
	 * @return true: user can reply to existing comments; false: no reply
	 *         functionality
	 */
	public boolean canReplyToComment(UserComment comment);

	/**
	 * @param comment
	 *            The comment to edit
	 * @param allComments
	 *            list of all comments to check if this comment has any replies
	 * @return true: user can edit this comment; false: user can not edit this
	 *         comment
	 * 
	 */
	public boolean canUpdateComment(UserComment comment, List<UserComment> allComments);

	/**
	 * @param comment
	 *            The comment to delete
	 * @return true: user can delete this commetn; false: user can not delete
	 *         this comment
	 */
	public boolean canDeleteComment(UserComment comment);

	/**
	 * @return true: user is allowed to see community rating; false: user is not
	 *         allowed to see community rating;
	 */
	public boolean canViewRatingAverage();

	/**
	 * @return true: user is allowed to see how each single user rated the
	 *         resource
	 */
	public boolean canViewOtherUsersRatings();

	/**
	 * @return true: user can rate this resource; false: user can not rate this
	 *         resource
	 */
	public boolean canRate();
	

}
