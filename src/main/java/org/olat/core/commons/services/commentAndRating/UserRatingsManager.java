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

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.commentAndRating.model.OLATResourceableRating;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.manager.BasicManager;

/**
 * Description:<br>
 * The user ratings manager provides methods to rate any given OLATResourceable
 * object. To use this manager, make sure a proper implementation for the bean
 * 'org.olat.core.commons.services.commentAndRating.UserRatingsManager' is
 * defined in the spring configuration.
 * 
 * <P>
 * Initial Date: 23.11.2009 <br>
 * 
 * @author gnaegi
 */
public abstract class UserRatingsManager extends BasicManager {
	protected OLATResourceable ores;
	protected String subpath;
	
	/**
	 * Use only inside commentAndRating package! Otherwise use CommentsAndRatingServiceFactory
	 * Factory method to create a ratings manager for the given
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
	public static final UserRatingsManager getInstance(OLATResourceable ores,
			String subpath) {
		UserRatingsManager ratingServiceFactory = (UserRatingsManager) CoreSpringFactory
				.getBean(UserRatingsManager.class);
		return ratingServiceFactory.createRatingsManager(ores, subpath);
	}

	/**
	 * Factory method to create a new instance of the ratings service for the
	 * given resource
	 * 
	 * @param ores
	 * @param subpath
	 * @return
	 */
	protected abstract UserRatingsManager createRatingsManager(
			OLATResourceable ores, String subpath);

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
	 * @return The number of ratings for the configured resource. 0 if no
	 *         ratings are available.
	 */
	public abstract Long countRatings();

	/**
	 * 
	 * @return all ratings 
	 */
	public abstract  List<UserRating> getAllRatings();
	
	/**
	 * @return The average of ratings for the configured resource. 0 if no
	 *         ratings are available.
	 */
	public abstract Float calculateRatingAverage();

	/**
	 * Create a new rating for the configured resource
	 * 
	 * @param creator
	 *            The user who is rating
	 * @param ratingValue
	 *            The rating
	 * @return
	 */
	public abstract UserRating createRating(Identity creator, int ratingValue);

	/**
	 * Get the rating for the configured user
	 * 
	 * @param identity
	 * @return The users rating or NULL
	 */
	public abstract UserRating getRating(Identity identity);

	/**
	 * Reload the given user rating with the most recent version from the
	 * database
	 * 
	 * @return the reloaded user rating or NULL if the rating does not exist
	 *         anymore
	 */
	public abstract UserRating reloadRating(UserRating rating);

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
	public abstract UserRating updateRating(UserRating rating,
			int newRatingValue);

	/**
	 * Delete a rating
	 * 
	 * @param rating
	 * @param int number of deleted ratings
	 */
	public abstract int deleteRating(UserRating rating);

	/**
	 * Delete all ratings for the configured resource and sub path
	 * 
	 * @return the number of deleted comments
	 */
	public abstract int deleteAllRatings();

	/**
	 * Delete all ratingsfor the configured resource while ignoring the sub
	 * path. Use this to delete all ratings e.g. from a blog for all blog posts
	 * in one query
	 * 
	 * @return
	 */
	public abstract int deleteAllRatingsIgnoringSubPath();
	
	/**
	 * Return the most rated resources
	 * @param limit The maximum number of resources returned
	 * @return
	 */
	//fxdiff
	public abstract List<OLATResourceableRating> getMostRatedResourceables(int maxResults);

}
