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

import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 31.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("userRatingsDAO")
public class UserRatingsDAO {
	
	private static final OLog log = Tracing.createLoggerFor(UserRatingsDAO.class);
	
	@Autowired
	private DB dbInstance;
	
	public UserRating createRating(Identity creator, OLATResourceable ores, String resSubPath, int ratingValue) {
		UserRatingImpl rating = new UserRatingImpl();
		rating.setCreator(creator);
		rating.setResName(ores.getResourceableTypeName());
		rating.setResId(ores.getResourceableId());
		rating.setResSubPath(resSubPath);
		rating.setCreationDate(new Date());
		rating.setLastModified(new Date());
		rating.setRating(ratingValue);
		dbInstance.getCurrentEntityManager().persist(rating);
		return rating;
	}
	
	public float getRatingAverage(OLATResourceable ores, String resSubPath) {
		TypedQuery<Number> query;
		if (resSubPath == null) {
			// special query when sub path is null
			query = dbInstance.getCurrentEntityManager()
					.createQuery("select avg(rating) from userrating where resName=:resname AND resId=:resId AND resSubPath is NULL", Number.class);
		} else {
			query = dbInstance.getCurrentEntityManager()
					.createQuery("select avg(rating) from userrating where resName=:resname AND resId=:resId AND resSubPath=:resSubPath", Number.class)
					.setParameter("resSubPath", resSubPath);
		}
		Number average = query
		     .setParameter("resname", ores.getResourceableTypeName())
		     .setParameter("resId", ores.getResourceableId())
		     .getSingleResult();
		// When no ratings are found, a null value is returned!
		if (average== null) {
			return 0.0f;
		}
		return average.floatValue();	
	}
	
	public int countRatings(OLATResourceable ores, String resSubPath) {
		TypedQuery<Number> query;
		if (resSubPath == null) {
			// special query when sub path is null
			query = dbInstance.getCurrentEntityManager()
					.createQuery("select count(*) from userrating where resName=:resname AND resId=:resId AND resSubPath is NULL", Number.class);
		} else {
			query = dbInstance.getCurrentEntityManager()
					.createQuery("select count(*) from userrating where resName=:resname AND resId=:resId AND resSubPath=:resSubPath", Number.class)
					.setParameter("resSubPath", resSubPath);
		}
		Number count = query.setParameter("resname", ores.getResourceableTypeName())
		     .setParameter("resId", ores.getResourceableId())
		     .setHint("org.hibernate.cacheable", Boolean.TRUE)
		     .getSingleResult();
		return count.intValue();
	}
	
	public UserRatingImpl getRating(Identity identity, OLATResourceable ores, String resSubPath) {
		StringBuilder sb = new StringBuilder();
		sb.append("select r from userrating as r where r.creator.key=:creatorKey and r.resName=:resname and r.resId=:resId");
		TypedQuery<UserRatingImpl> query;
		if (resSubPath == null) {
			sb.append(" and r.resSubPath is NULL");
			query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), UserRatingImpl.class);
		} else {
			sb.append(" and resSubPath=:resSubPath");
			query = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), UserRatingImpl.class)
					.setParameter("resSubPath", resSubPath);
		}
		List<UserRatingImpl> results = query
			 .setParameter("resname", ores.getResourceableTypeName())
		     .setParameter("resId", ores.getResourceableId())
		     .setParameter("creatorKey", identity.getKey())
		     .setHint("org.hibernate.cacheable", Boolean.TRUE)
		     .getResultList();
		if (results.size() == 0) return null;		
		return results.get(0);
	}
	
	public UserRating updateRating(Identity identity, OLATResourceable ores, String resSubPath, int newRatingValue) {
		UserRatingImpl rating = getRating(identity, ores, resSubPath);
		if (rating == null) {
			return createRating(identity, ores, resSubPath, newRatingValue);
		}
		// Update DB entry
		rating.setRating(newRatingValue);
		rating.setLastModified(new Date());
		rating = dbInstance.getCurrentEntityManager().merge(rating);
		return rating;
	}

	public UserRating updateRating(UserRating rating, int newRatingValue) {
		// First reload parent from cache to prevent stale object or cache issues
		rating = reloadRating(rating);
		if (rating == null) {
			// Original rating has been deleted in the meantime. Don't update it
			return null;
		}
		// Update DB entry
		rating.setRating(newRatingValue);
		rating.setLastModified(new Date());
		rating = dbInstance.getCurrentEntityManager().merge(rating);
		return rating;
	}

	public UserRating reloadRating(UserRating rating) {
		try {
			return (UserRating)dbInstance.loadObject(rating);		
		} catch (Exception e) {
			// Huh, most likely the given object does not exist anymore on the
			// db, probably deleted by someone else
			log.warn("Tried to reload a user rating but got an exception. Probably deleted in the meantime", e);
			return null;
		}
	}
}
