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
import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.commentAndRating.UserRatingsDelegate;
import org.olat.core.commons.services.commentAndRating.model.OLATResourceableRating;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.commons.services.commentAndRating.model.UserRatingImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
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

	@Autowired
	private DB dbInstance;
	
	private List<UserRatingsDelegate> delegates = new ArrayList<>();
	
	public void addDegelate(UserRatingsDelegate delegate) {
		delegates.add(delegate);
	}
	
	public UserRating createRating(Identity creator, OLATResourceable ores, String resSubPath, int ratingValue) {
		UserRatingImpl rating = new UserRatingImpl();
		rating.setCreator(creator);
		rating.setResName(ores.getResourceableTypeName());
		rating.setResId(ores.getResourceableId());
		rating.setResSubPath(resSubPath);
		rating.setCreationDate(new Date());
		rating.setLastModified(new Date());
		rating.setRating(ratingValue);
		updateDelegateRatings(rating);
		dbInstance.getCurrentEntityManager().persist(rating);
		return rating;
	}
	
	public List<UserRating> getAllRatings(OLATResourceable ores, String resSubPath){
		TypedQuery<UserRating> query;
		if (resSubPath == null) {
			// special query when sub path is null
			String sb = "select rating from userrating as rating where resName=:resname AND resId=:resId AND resSubPath is NULL";
			query = dbInstance.getCurrentEntityManager().createQuery(sb, UserRating.class);
		} else {
			String sb = "select rating from userrating as rating where resName=:resname AND resId=:resId AND resSubPath=:resSubPath";
			query = dbInstance.getCurrentEntityManager().createQuery(sb, UserRating.class)
					.setParameter("resSubPath", resSubPath);
		}
		return query.setParameter("resname", ores.getResourceableTypeName())
		     .setParameter("resId", ores.getResourceableId())
		     .setHint("org.hibernate.cacheable", Boolean.TRUE)
		     .getResultList();
	}

	public List<UserRating> getAllRatings(IdentityRef identity) {
		String sb = "select rating from userrating as rating where rating.creator.key=:identityKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb, UserRating.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public float getRatingAverage(OLATResourceable ores, String resSubPath) {
		StringBuilder sb = new StringBuilder();
		sb.append("select avg(rating) from userrating where resName=:resname and resId=:resId and resSubPath");
		if (resSubPath == null) {
			sb.append(" is null");
		} else {
			sb.append("=:resSubPath");	
		}
		
		TypedQuery<Number> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Number.class)
		     .setParameter("resname", ores.getResourceableTypeName())
		     .setParameter("resId", ores.getResourceableId());
		// When no ratings are found, a null value is returned!
		if(resSubPath != null) {
			query.setParameter("resSubPath", resSubPath);
		}
		
		Number average = query.getSingleResult();
		return average == null ? 0f : average.floatValue();	
	}
	
	public List<OLATResourceableRating> getMostRatedResourceables(OLATResourceable ores, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select new ").append(OLATResourceableRating.class.getName()).append("(")
			.append(" rating.resName, rating.resId, rating.resSubPath, avg(rating.rating))")
			.append(" from userrating as rating ")
			.append(" where rating.resName=:resName and rating.resId=:resId")
			.append(" group by rating.resName, rating.resId, rating.resSubPath")
			.append(" order by avg(rating.rating) desc");

		TypedQuery<OLATResourceableRating> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), OLATResourceableRating.class)
				.setParameter("resName", ores.getResourceableTypeName())
				.setParameter("resId", ores.getResourceableId());
		
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}

		return query.getResultList();
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
	
	public Integer getRatingValue(Identity identity, OLATResourceable ores, String resSubPath) {
		StringBuilder sb = new StringBuilder();
		sb.append("select r.rating from userrating as r where r.creator.key=:creatorKey and r.resName=:resname and r.resId=:resId");
		TypedQuery<Integer> query;
		if (resSubPath == null) {
			sb.append(" and r.resSubPath is NULL");
			query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Integer.class);
		} else {
			sb.append(" and resSubPath=:resSubPath");
			query = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), Integer.class)
					.setParameter("resSubPath", resSubPath);
		}
		List<Integer> results = query
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
		updateDelegateRatings(rating);
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
		updateDelegateRatings(rating);
		rating = dbInstance.getCurrentEntityManager().merge(rating);
		return rating;
	}

	public UserRating reloadRating(UserRating rating) {
		String query = "select r from userrating as r where r.key=:ratingKey";
		List<UserRating> ratings = dbInstance.getCurrentEntityManager()
				.createQuery(query, UserRating.class)
				.setParameter("ratingKey", rating.getKey())
				.getResultList();
		return ratings != null && !ratings.isEmpty() ? ratings.get(0) : null;
	}
	
	private void updateDelegateRatings(UserRating rating) {
		if(delegates == null || delegates.isEmpty()) return;

		OLATResourceable ores = OresHelper.createOLATResourceableInstance(rating.getResName(), rating.getResId());
		for(UserRatingsDelegate delegate:delegates) {
			if(delegate.accept(ores, rating.getResSubPath())) {
				StringBuilder sb = new StringBuilder();
				sb.append("select count(rating.key), sum(rating.rating) from userrating as rating")
				  .append(" where rating.resName=:resname and rating.resId=:resId");
				if(rating.getResSubPath() == null) {
					sb.append(" and rating.resSubPath is null");
				} else {
					sb.append(" and rating.resSubPath=:resSubPath");
				}
				if(rating.getKey() != null) {
				  sb.append(" and rating.key!=:ratingKey");
				}
	
				TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), Object[].class)
				     .setParameter("resname", rating.getResName())
				     .setParameter("resId", rating.getResId());
				if(rating.getKey() != null) {
					query.setParameter("ratingKey", rating.getKey());
				}
				if(rating.getResSubPath() != null) {
					query.setParameter("resSubPath", rating.getResSubPath());
				}
		
				Object[] stats = query.getSingleResult();
				if(stats == null || stats[0] == null || stats[1] == null) {
					Integer rate = rating.getRating();
					delegate.update(ores, rating.getResSubPath(), rate.doubleValue(), 1l);
				} else {
					long numOfRatings = ((Number)stats[0]).longValue();
					long sumOfRatings = ((Number)stats[1]).longValue();
					double rate = (sumOfRatings + rating.getRating().intValue()) / (numOfRatings + 1.0d);
					delegate.update(ores, rating.getResSubPath(), rate, numOfRatings + 1);
				}
			}
		}
	}

	public int deleteRatings(IdentityRef identity) {
		String query = "select r from userrating as r where r.creator.key=:creatorKey";
		List<UserRating> ratings = dbInstance.getCurrentEntityManager()
				.createQuery(query, UserRating.class)
				.setParameter("creatorKey", identity.getKey())
				.getResultList();

		for(UserRating rating:ratings) {
			dbInstance.getCurrentEntityManager().remove(rating);
			dbInstance.commit();
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(rating.getResName(), rating.getResId());
			recalculateDelegateRatings(ores, rating.getResSubPath());
		}
		return ratings.size();
	}
	
	private void recalculateDelegateRatings(OLATResourceable ores, String resSubPath) {
		if(delegates == null || delegates.isEmpty()) return;

		for(UserRatingsDelegate delegate:delegates) {
			if(delegate.accept(ores, resSubPath)) {
				StringBuilder sb = new StringBuilder();
				sb.append("select count(rating.key), sum(rating.rating) from userrating as rating")
				  .append(" where rating.resName=:resname and rating.resId=:resId");
				if(resSubPath == null) {
					sb.append(" and rating.resSubPath is null");
				} else {
					sb.append(" and rating.resSubPath=:resSubPath");
				}
	
				TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), Object[].class)
				     .setParameter("resname", ores.getResourceableTypeName())
				     .setParameter("resId", ores.getResourceableId());

				if(resSubPath != null) {
					query.setParameter("resSubPath", resSubPath);
				}
		
				Object[] stats = query.getSingleResult();
				if(stats == null || stats[0] == null || stats[1] == null) {
					delegate.update(ores, resSubPath, 0.0d, 0l);
				} else {
					long numOfRatings = ((Number)stats[0]).longValue();
					long sumOfRatings = ((Number)stats[1]).longValue();
					double rate = sumOfRatings / (double)numOfRatings;
					delegate.update(ores, resSubPath, rate, numOfRatings);
				}
			}
		}
	}

	public int deleteAllRatings(OLATResourceable ores, String resSubPath) {
		// special query when sub path is null
		if (resSubPath == null) {
			String sb = "delete from userrating where resName=:resName and resId=:resId and resSubPath is null";
			return dbInstance.getCurrentEntityManager().createQuery(sb.toString())
					.setParameter("resName", ores.getResourceableTypeName())
					.setParameter("resId", ores.getResourceableId())
					.executeUpdate();
		} else {
			String sb = "delete from userrating where resName=:resName and resId=:resId and resSubPath=:resSubPath";
			return dbInstance.getCurrentEntityManager().createQuery(sb.toString())
					.setParameter("resName", ores.getResourceableTypeName())
					.setParameter("resId", ores.getResourceableId())
					.setParameter("resSubPath",  resSubPath)
					.executeUpdate();
		}
	}

	public int deleteAllRatingsIgnoringSubPath(OLATResourceable ores) {
		String sb = "delete from userrating where resName=:resName and resId=:resId";
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString())
				.setParameter("resName", ores.getResourceableTypeName())
				.setParameter("resId", ores.getResourceableId())
				.executeUpdate();	
	}
}
