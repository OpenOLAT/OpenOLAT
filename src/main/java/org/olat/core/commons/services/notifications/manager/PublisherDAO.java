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
package org.olat.core.commons.services.notifications.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.logging.AssertException;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23 juin 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class PublisherDAO {
	
	@Autowired
	private DB dbInstance;
	
	public Publisher updatePublisher(Publisher publisher) {
		return dbInstance.getCurrentEntityManager().merge(publisher);
	}
	
	public int updatePublishers(String resName, List<Long> resId, Date lastNews) {
		if(resId == null || resId.isEmpty()) return 0;
		
		String query = """
				update notipublisher pub set pub.latestNewsDate=:date
				where pub.resName=:resName and pub.resId in (:resId) and pub.latestNewsDate<:date""";
		return dbInstance.getCurrentEntityManager().createQuery(query)
			.setParameter("resName", resName)
			.setParameter("resId", resId)
			.setParameter("date", lastNews, TemporalType.TIMESTAMP)
			.executeUpdate();
	}
	
	public List<Publisher> getPublisherByType(String type) {
		String query = """
				select pub from notipublisher pub
				left join fetch pub.parentPublisher as parentPublisher
				where pub.type=:type""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Publisher.class)
				.setParameter("type", type)
				.getResultList();
	}
	
	public List<Publisher> getPublishers(SubscriptionContext subsContext) {
		QueryBuilder q = new QueryBuilder();
		q.append("select pub from notipublisher pub ")
		 .where().append(" pub.resName=:resName and pub.resId=:resId");
		if(StringHelper.containsNonWhitespace(subsContext.getSubidentifier())) {
			q.and().append(" pub.subidentifier=:subidentifier");
		} else {
			q.and().append(" (pub.subidentifier='' or pub.subidentifier is null)");
		}
		
		TypedQuery<Publisher> query = dbInstance.getCurrentEntityManager()
				.createQuery(q.toString(), Publisher.class)
				.setParameter("resName", subsContext.getResName())
				.setParameter("resId", subsContext.getResId());
		if(StringHelper.containsNonWhitespace(subsContext.getSubidentifier())) {
			query.setParameter("subidentifier", subsContext.getSubidentifier());
		}
		return query.getResultList();
	}
	
	public long countPublishers(SubscriptionContext subsContext) {
		QueryBuilder q = new QueryBuilder();
		q.append("select count(pub.key) from notipublisher pub ")
		 .where().append(" pub.resName=:resName and pub.resId=:resId");
		if(StringHelper.containsNonWhitespace(subsContext.getSubidentifier())) {
			q.and().append(" pub.subidentifier=:subidentifier");
		} else {
			q.and().append(" (pub.subidentifier='' or pub.subidentifier is null)");
		}
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(q.toString(), Long.class)
				.setParameter("resName", subsContext.getResName())
				.setParameter("resId", subsContext.getResId());
		if(StringHelper.containsNonWhitespace(subsContext.getSubidentifier())) {
			query.setParameter("subidentifier", subsContext.getSubidentifier());
		}
		List<Long> count = query.getResultList();
		return count != null && !count.isEmpty() && count.get(0) != null ? count.get(0).longValue() : 0l;
	}
	
	public List<Publisher> getPublishers(String publisherType, String data) {
		String q = """
				select pub from notipublisher pub
				left join fetch pub.parentPublisher as parent
				where pub.type=:publisherType and pub.data=:data""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, Publisher.class)
				.setParameter("publisherType", publisherType)
				.setParameter("data", data)
				.getResultList();
	}
	
	/**
	 * @param resName
	 * @param resId
	 * @return a list of publishers belonging to the resource
	 */
	public List<Publisher> getPublishersByResNameAndId(String resName, Long resId) {
		String q = "select pub from notipublisher pub where pub.resName=:resName and pub.resId= :resId";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, Publisher.class)
				.setParameter("resName", resName)
				.setParameter("resId", resId)
				.getResultList();
	}
	
	public Publisher getRootPublisher(SubscriptionContext subsContext) {
		QueryBuilder q = new QueryBuilder();
		q.append("select pub from notipublisher pub ")
		 .where().append("pub.parentPublisher is null and pub.resName=:resName and pub.resId=:resId");
		if(StringHelper.containsNonWhitespace(subsContext.getSubidentifier())) {
			q.and().append(" pub.subidentifier=:subidentifier");
		} else {
			q.and().append(" (pub.subidentifier='' or pub.subidentifier is null)");
		}

		TypedQuery<Publisher> query = dbInstance.getCurrentEntityManager()
				.createQuery(q.toString(), Publisher.class)
				.setParameter("resName", subsContext.getResName())
				.setParameter("resId", subsContext.getResId());

		if(StringHelper.containsNonWhitespace(subsContext.getSubidentifier())) {
			query.setParameter("subidentifier", subsContext.getSubidentifier());
		}
		
		List<Publisher> res = query.getResultList();
		if (res.isEmpty()) return null;
		if (res.size() != 1) throw new AssertException("only one subscriber per person and publisher!!");
		return res.get(0);
	}
	
	public int deletePublishersAndSubscribers(List<Publisher> pubs) {
		if(pubs == null || pubs.isEmpty()) return 0;
		
		String q1 = "delete from notisub sub where sub.publisher in (:publishers)";
		Query query1 = dbInstance.getCurrentEntityManager().createQuery(q1);
		query1.setParameter("publishers", pubs);
		int rows = query1.executeUpdate();
		
		String q2 = "delete from notipublisher pub where pub in (:publishers)";
		Query query2 = dbInstance.getCurrentEntityManager().createQuery(q2);
		query2.setParameter("publishers", pubs);
		rows += query2.executeUpdate();
		return rows;
	}
	
	public void deletePublisher(Publisher publisher) {
		dbInstance.getCurrentEntityManager().remove(publisher);
	}
}
