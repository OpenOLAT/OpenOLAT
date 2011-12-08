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
* <p>
*/
package org.olat.core.commons.services.commentAndRating.impl;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.type.Type;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingLoggingAction;
import org.olat.core.commons.services.commentAndRating.UserRatingsManager;
import org.olat.core.commons.services.commentAndRating.model.OLATResourceableRating;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;

/**
 * Description:<br>
 * This implementation of the user rating manager is database based.
 * 
 * <P>
 * Initial Date:  01.12.2009 <br>
 * @author gnaegi
 */
public class UserRatingsManagerImpl extends UserRatingsManager {

	/**
	 * Spring constructor. Use the getInstance method instead of calling this
	 * constructor directly.
	 */
	public UserRatingsManagerImpl() {
		// nothing to do
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.UserRatingsManager#createRatingsManager(org.olat.core.id.OLATResourceable, java.lang.String)
	 */
	@Override
	protected UserRatingsManager createRatingsManager(OLATResourceable ores, String subpath) {
		UserRatingsManager manager = new UserRatingsManagerImpl();
		manager.init(ores, subpath);
		return manager;
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.UserRatingsManager#calculateRatingAverage()
	 */
	@Override
	public Float calculateRatingAverage() {
		DBQuery query;
		if (getOLATResourceableSubPath() == null) {
			// special query when sub path is null
			query = DBFactory
					.getInstance()
					.createQuery(
							"select avg(rating) from UserRatingImpl where resName=:resname AND resId=:resId AND resSubPath is NULL");
		} else {
			query = DBFactory
					.getInstance()
					.createQuery(
							"select avg(rating) from UserRatingImpl where resName=:resname AND resId=:resId AND resSubPath=:resSubPath");
			query.setString("resSubPath", getOLATResourceableSubPath());
		}
		query.setString("resname", getOLATResourceable()
				.getResourceableTypeName());
		query.setLong("resId", getOLATResourceable().getResourceableId());
		query.setCacheable(true);
		//
		List results = query.list();
		Double average = (Double) query.list().get(0);
		// When no ratings are found, a null value is returned!
		if (average == null) return Float.valueOf(0);
		else return average.floatValue();			
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.UserRatingsManager#countRatings()
	 */
	@Override
	public Long countRatings() {
		DBQuery query;
		if (getOLATResourceableSubPath() == null) {
			// special query when sub path is null
			query = DBFactory
					.getInstance()
					.createQuery(
							"select count(*) from UserRatingImpl where resName=:resname AND resId=:resId AND resSubPath is NULL");
		} else {
			query = DBFactory
					.getInstance()
					.createQuery(
							"select count(*) from UserRatingImpl where resName=:resname AND resId=:resId AND resSubPath=:resSubPath");
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
	 * @see org.olat.core.commons.services.commentAndRating.UserRatingsManager#createRating(org.olat.core.id.Identity, int)
	 */
	@Override
	public UserRating createRating(Identity creator, int ratingValue) {
		UserRating rating = new UserRatingImpl(getOLATResourceable(),
				getOLATResourceableSubPath(), creator, Integer.valueOf(ratingValue));
		DBFactory.getInstance().saveObject(rating);
		// do logging
		ThreadLocalUserActivityLogger.log(CommentAndRatingLoggingAction.RATING_CREATED, getClass(),
				CoreLoggingResourceable.wrap(getOLATResourceable(), OlatResourceableType.feedItem));
		return rating;
	}

	/**
	 * 
	 * @see org.olat.core.commons.services.commentAndRating.UserRatingsManager#getRating(org.olat.core.id.Identity)
	 */
	@Override
	public UserRating getRating(Identity identity) {
		DBQuery query;
		if (getOLATResourceableSubPath() == null) {
			// special query when sub path is null
			query = DBFactory
					.getInstance()
					.createQuery(
							"select userRating from UserRatingImpl as userRating where creator=:creator AND resName=:resname AND resId=:resId AND resSubPath is NULL ");
		} else {
			query = DBFactory
					.getInstance()
					.createQuery(
							"select userRating from UserRatingImpl as userRating where creator=:creator AND resName=:resname AND resId=:resId AND resSubPath=:resSubPath");
			query.setString("resSubPath", getOLATResourceableSubPath());
		}
		query.setString("resname", getOLATResourceable()
				.getResourceableTypeName());
		query.setLong("resId", getOLATResourceable().getResourceableId());
		query.setEntity("creator", identity);
		query.setCacheable(true);
		//
		List<UserRating> results = query.list();
		if (results.size() == 0) return null;		
		return results.get(0);
	}
	
	/**
	 * @see org.olat.core.commons.services.commentAndRating.UserRatingsManager#deleteRating(org.olat.core.commons.services.commentAndRating.model.UserRating)
	 */
	public int deleteRating (UserRating rating) {
		if (!isRatingOfResource(rating)) {
			throw new AssertException(
					"This user rating manager is initialized for another resource than the given comment.");
		}
		// First reload parent from cache to prevent stale object or cache issues
		rating = reloadRating(rating);
		if (rating == null) {
			// Original rating has been deleted in the meantime. Don't delete it again.
			return 0;
		}
		// Delete this rating and finish
		DB db = DBFactory.getInstance();
		db.deleteObject(rating);
		return 1;

	}


	/**
	 * @see org.olat.core.commons.services.commentAndRating.UserRatingsManager#deleteAllRatings()
	 */
	@Override
	public int deleteAllRatings() {
		DB db = DBFactory.getInstance();
		String query;
		Object[] values;
		Type[] types;
		// special query when sub path is null
		if (getOLATResourceableSubPath() == null) {
			query = "from UserRatingImpl where resName=? AND resId=? AND resSubPath is NULL";
			values = new Object[] { getOLATResourceable().getResourceableTypeName(),  getOLATResourceable().getResourceableId() };
			types = new Type[] {Hibernate.STRING, Hibernate.LONG};
		} else {
			query = "from UserRatingImpl where resName=? AND resId=? AND resSubPath=?";
			values = new Object[] { getOLATResourceable().getResourceableTypeName(),  getOLATResourceable().getResourceableId(), getOLATResourceableSubPath() };
			types = new Type[] {Hibernate.STRING, Hibernate.LONG, Hibernate.STRING};
		}
		return db.delete(query, values, types);
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.UserRatingsManager#deleteAllRatingsIgnoringSubPath()
	 */
	@Override
	public int deleteAllRatingsIgnoringSubPath() {
		// Don't limit to subpath. Ignore if null or not, just delete on the resource
		String query = "from UserRatingImpl where resName=? AND resId=?";
		Object[] values = new Object[] { getOLATResourceable().getResourceableTypeName(),  getOLATResourceable().getResourceableId() };
		Type[] types = new Type[] {Hibernate.STRING, Hibernate.LONG};
		DB db = DBFactory.getInstance();
		return db.delete(query, values, types);		
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.UserRatingsManager#reloadRating(org.olat.core.commons.services.commentAndRating.model.UserRating)
	 */
	@Override
	public UserRating reloadRating(UserRating rating) {
		try {
			DB db = DBFactory.getInstance();
			return (UserRating) db.loadObject(rating);			
		} catch (Exception e) {
			// Huh, most likely the given object does not exist anymore on the
			// db, probably deleted by someone else
			logWarn("Tried to reload a user rating but got an exception. Probably deleted in the meantime", e);
			return null;
		}
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.UserRatingsManager#updateRating(org.olat.core.commons.services.commentAndRating.model.UserRating, int)
	 */
	@Override
	public UserRating updateRating(UserRating rating, int newRatingValue) {
		if (!isRatingOfResource(rating)) {
			throw new AssertException(
					"This user rating manager is initialized for another resource than the given comment.");
		}
		// First reload parent from cache to prevent stale object or cache issues
		rating = reloadRating(rating);
		if (rating == null) {
			// Original rating has been deleted in the meantime. Don't update it
			return null;
		}
		// Update DB entry
		rating.setRating(newRatingValue);
		DB db = DBFactory.getInstance();
		db.updateObject(rating);
		// do logging
		ThreadLocalUserActivityLogger.log(CommentAndRatingLoggingAction.RATING_UPDATED, getClass(),
				CoreLoggingResourceable.wrap(getOLATResourceable(), OlatResourceableType.feedItem));
		return rating;
	}

	/**
	 * Helper method to check if the given commerating has the same resource and
	 * path as configured for this manager
	 * 
	 * @param originalRating
	 * @return
	 */
	private boolean isRatingOfResource(UserRating originalRating) {
		if (this.getOLATResourceable().getResourceableId().equals(
				originalRating.getResId())
				&& this.getOLATResourceable().getResourceableTypeName().equals(
						originalRating.getResName())) {
			// check on resource subpath: can be null
			if (this.getOLATResourceableSubPath() == null) {
				return (originalRating.getResSubPath() == null);
			} else {
				return this.getOLATResourceableSubPath().equals(
						originalRating.getResSubPath());
			}
		}
		return false;
	}

	@Override
	//fxdiff
	public List<OLATResourceableRating> getMostRatedResourceables(int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select new ").append(OLATResourceableRating.class.getName()).append("(")
			.append(" rating.resName, rating.resId, rating.resSubPath, avg(rating.rating))")
			.append(" from ").append(UserRatingImpl.class.getName()).append(" as rating ")
			.append(" where rating.resName=:resName and rating.resId=:resId")
			.append(" group by rating.resName, rating.resId, rating.resSubPath")
			.append(" order by avg(rating.rating) desc");

		DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
		query.setString("resName", getOLATResourceable().getResourceableTypeName());
		query.setLong("resId", getOLATResourceable().getResourceableId());
		
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}

		List<OLATResourceableRating> mostRated = query.list();
		return mostRated;
	}
	
	

}
