/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.review.ReviewElementDefinition;
import org.olat.modules.selectus.model.review.ReviewResponse;
import org.olat.modules.selectus.model.review.ReviewResponseImpl;

/**
 * 
 * Initial date: 3 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ReviewResponseDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ReviewResponse create(ApplicationLight application, ReviewElementDefinition element, Identity reviewer,
			String stringValue, Integer integerValue) {
		ReviewResponseImpl response = new ReviewResponseImpl();
		response.setCreationDate(new Date());
		response.setLastModified(response.getCreationDate());
		response.setStringValue(stringValue);
		response.setIntegerValue(integerValue);
		response.setReviewer(reviewer);
		response.setApplication(application);
		response.setElement(element);
		dbInstance.getCurrentEntityManager().persist(response);
		return response;
	}
	
	public ReviewResponse merge(ReviewResponse response) {
		((ReviewResponseImpl)response).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(response);
	}
	
	public void delete(ReviewResponse response) {
		dbInstance.getCurrentEntityManager().remove(response);
	}
	
	public int delete(ReviewElementDefinition element) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from rreviewresponse where element.key=:elementKey");
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString())
			.setParameter("elementKey", element.getKey())
			.executeUpdate();
	}
	
	public int delete(Application app) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from rreviewresponse where application.key=:applicationKey");
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString())
			.setParameter("applicationKey", app.getKey())
			.executeUpdate();
	}
	
	public List<ReviewResponse> getResponses(ApplicationRef application) {
		StringBuilder sb = new StringBuilder();
		sb.append("select response from rreviewresponse as response")
		  .append(" inner join fetch response.reviewer as ident")
		  .append(" inner join fetch ident.user as reviewer")
		  .append(" inner join fetch response.application as app")
		  .append(" inner join fetch response.element as el")
		  .append(" where response.application.key=:appKey");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), ReviewResponse.class)
				.setParameter("appKey", application.getKey())
				.getResultList();
	}
	
	public List<ReviewResponse> getResponses(PositionRef position, int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select response from rreviewresponse as response")
		  .append(" inner join fetch response.reviewer as ident")
		  .append(" inner join fetch ident.user as reviewer")
		  .append(" inner join fetch response.application as app")
		  .append(" inner join fetch response.element as el")
		  .append(" where app.positionKey=:posKey")
		  .append(" order by app.key, el.key, response.key asc");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), ReviewResponse.class)
				.setParameter("posKey", position.getKey())
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	public List<Identity> getReviewers(PositionRef position) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select reviewer from ").append(IdentityImpl.class.getName()).append(" as reviewer")
		  .append(" inner join fetch reviewer.user as reviewerUser")
		  .append(" where exists (select response.key from rreviewresponse as response")
		  .append("   inner join response.application as app")
		  .append("   where app.positionKey=:posKey and response.reviewer.key=reviewer.key")
		  .append(" )");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Identity.class)
				.setParameter("posKey", position.getKey())
				.getResultList();
	}
	
	public List<ReviewResponse> getResponses(ApplicationRef application, IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select response from rreviewresponse as response")
		  .append(" inner join fetch response.reviewer as ident")
		  .append(" inner join fetch response.application as app")
		  .append(" inner join fetch response.element as el")
		  .append(" where app.key=:appKey and ident.key=:identityKey");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), ReviewResponse.class)
				.setParameter("appKey", application.getKey())
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public ReviewResponse getResponse(ApplicationRef application, ReviewElementDefinition element, IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select response from rreviewresponse as response")
		  .append(" inner join fetch response.reviewer as ident")
		  .append(" inner join fetch response.application as app")
		  .append(" inner join fetch response.element as el")
		  .append(" where response.application.key=:appKey and response.reviewer.key=:identityKey")
		  .append(" and response.element.key=:elementKey");
		List<ReviewResponse> responses = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ReviewResponse.class)
				.setParameter("appKey", application.getKey())
				.setParameter("identityKey", identity.getKey())
				.setParameter("elementKey", element.getKey())
				.getResultList();
		return responses == null || responses.isEmpty() ? null : responses.get(0);
	}
	
	public Set<Long> getApplicationReviewed(Position position, IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select app.key from rreviewresponse as response")
		  .append(" inner join response.application as app")
		  .append(" where app.positionKey=:positionKey and response.reviewer.key=:identityKey")
		  .append(" and (response.integerValue is not null or length(response.stringValue) > 0)");
		List<Long> appKeys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("positionKey", position.getKey())
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		return new HashSet<>(appKeys);
	}
	
	public Map<Long, AtomicInteger> getNumberOfReviews(Position position, List<Long> reviewerKeys) {
		if(reviewerKeys == null || reviewerKeys.isEmpty()) {
			return new HashMap<>();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select app.key, response.reviewer.key from rreviewresponse as response")
		  .append(" inner join response.application as app")
		  .append(" where app.positionKey=:positionKey and response.reviewer.key in (:identityKeys)");
		List<Object[]> responses = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("positionKey", position.getKey())
				.setParameter("identityKeys", reviewerKeys)
				.getResultList();
		

		Map<Long,AtomicInteger> numOfReviews = new HashMap<>();
		Set<AppReviewerPair> duplicates = new HashSet<>();
		for(Object[] response:responses) {
			Long appKey = (Long)response[0];
			Long reviewerKey = (Long)response[1];
			AppReviewerPair pair = new AppReviewerPair(appKey, reviewerKey);
			if(!duplicates.contains(pair)) {
				if(numOfReviews.containsKey(appKey)) {
					numOfReviews.get(appKey).incrementAndGet();
				} else {
					numOfReviews.put(appKey, new AtomicInteger(1));
				}
				duplicates.add(pair);
			}
		}
		
		return numOfReviews;
	}
	
	private static final class AppReviewerPair {
		
		private final Long appKey;
		private final Long reviewerKey;
		
		public AppReviewerPair(Long appKey, Long reviewerKey) {
			this.appKey = appKey;
			this.reviewerKey = reviewerKey;
		}

		@Override
		public int hashCode() {
			return appKey.hashCode() + reviewerKey.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == this) {
				return true;
			}
			if(obj instanceof AppReviewerPair) {
				AppReviewerPair pair = (AppReviewerPair)obj;
				return appKey.equals(pair.appKey) && reviewerKey.equals(pair.reviewerKey);
			}
			return false;
		}
	}
}
