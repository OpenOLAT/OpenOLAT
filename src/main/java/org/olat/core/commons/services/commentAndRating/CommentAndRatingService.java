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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.core.commons.services.commentAndRating;

import java.util.List;

import org.olat.core.commons.services.commentAndRating.model.OLATResourceableRating;
import org.olat.core.commons.services.commentAndRating.model.UserComment;
import org.olat.core.commons.services.commentAndRating.model.UserCommentsCount;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;

/**
 * Description:<br>
 * The comment and rating service offers GUI elements to comment and rate
 * OLATResourceable objects. The objects can be specified even more precise by
 * providing an optional sub path
 * </pre>
 * <P>
 * Initial Date: 24.11.2009 <br>
 * 
 * @author gnaegi
 */
public interface CommentAndRatingService {
	
	/**
	 * @return The number of ratings for the configured resource. 0 if no
	 *         ratings are available.
	 */
	public Long countRatings(OLATResourceable ores, String resSubPath);

	/**
	 * 
	 * @return all ratings 
	 */
	public  List<UserRating> getAllRatings(OLATResourceable ores, String resSubPath);
	
	/**
	 * @return The average of ratings for the configured resource. 0 if no
	 *         ratings are available.
	 */
	public Float calculateRatingAverage(OLATResourceable ores, String resSubPath);

	/**
	 * Create a new rating for the configured resource
	 * 
	 * @param creator
	 *            The user who is rating
	 * @param ratingValue
	 *            The rating
	 * @return
	 */
	public UserRating createRating(Identity creator, OLATResourceable ores, String resSubPath, int ratingValue);

	/**
	 * Get the rating for the configured user
	 * 
	 * @param identity
	 * @return The users rating or NULL
	 */
	public UserRating getRating(Identity identity, OLATResourceable ores, String resSubPath);
	
	/**
	 * Get the rating for the user and resource
	 * @param identity
	 * @param ores
	 * @param resSubPath
	 * @return The value of the rating of null if not rated
	 */
	public Integer getRatingValue(Identity identity, OLATResourceable ores, String resSubPath);
	

	/**
	 * Reload the given user rating with the most recent version from the
	 * database
	 * 
	 * @return the reloaded user rating or NULL if the rating does not exist
	 *         anymore
	 */
	public UserRating reloadRating(UserRating rating);

	/**
	 * Update a rating. This will first reload the comment object and then
	 * update this new object to reduce stale object issues. Make sure you
	 * replace your object in your datamodel with the returned user comment
	 * object.
	 * 
	 * @param rating
	 *            The rating which should be updated
	 * @param rewRatingValue
	 *            The updated rating value
	 * @return the updated rating object. Might be a different object than the
	 *         rating given as attribute or NULL if the rating has been deleted
	 *         in the meantime and could not be updated at all.
	 */
	public UserRating updateRating(UserRating rating, int newRatingValue);
	
	/**
	 * Return the most rated resources
	 * @param limit The maximum number of resources returned
	 * @return
	 */
	public List<OLATResourceableRating> getMostRatedResourceables(OLATResourceable ores, int maxResults);

	
	
	public long countComments(OLATResourceable ores, String resSubPath);
	
	/**
	 * @return The number of comments for the configured resource and its sub path. 0 if no
	 *         comments are available.
	 */
	public List<UserCommentsCount> countCommentsWithSubPath(OLATResourceable ores, String resSubPath);

	/**
	 * Get a list of user comments for the configured resource
	 * 
	 */
	public List<UserComment> getComments(OLATResourceable ores, String resSubPath);

	/**
	 * Create a new comment for the configured resource
	 * 
	 * @param creator
	 *            The author of the comment
	 * @param comment
	 *            The commentText
	 * @return
	 */
	public UserComment createComment(Identity creator, OLATResourceable ores, String resSubPath, String commentText);

	/**
	 * Reload the given user comment with the most recent version from the
	 * database
	 * 
	 * @return the reloaded user comment or NULL if the comment does not exist
	 *         anymore
	 */
	public UserComment reloadComment(UserComment comment);

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
	public UserComment replyTo(UserComment originalComment, Identity creator, String replyCommentText);

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
	public UserComment updateComment(UserComment comment, String newCommentText);

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
	public int deleteComment(UserComment comment, boolean deleteReplies);

	/**
	 * Delete all comments and ratings for this resource and subpath
	 * configuration. See also the deleteAllIgnoringSubPath() method.
	 * 
	 * @param int number of deleted comments and ratings
	 */
	public int deleteAll(OLATResourceable ores, String oresSubPath);

	/**
	 * Delete all comments and ratings for this resource while ignoring the
	 * resource sub path. Use this when you delete the resource and not an element
	 * within the resource.
	 * 
	 * @param int number of deleted comments and ratings
	 */
	public int deleteAllIgnoringSubPath(OLATResourceable ores);

}
