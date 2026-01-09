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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.TemporalType;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherChannel;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
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
public class SubscriberDAO {
	
	@Autowired
	private DB dbInstance;
	
	public long countSubscribers(Publisher publisher) {
		String query = """
				select count(sub.key) from notisub as sub
				where sub.publisher.key = :publisherKey""";
		
		List<Long> count = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("publisherKey", publisher.getKey())
				.getResultList();
		return count != null && !count.isEmpty() && count.get(0) != null ? count.get(0).longValue() : 0l;
	}
	
	public List<Subscriber> getSubscriber(IdentityRef identity, Publisher publisher) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("subscribersByPublisherAndIdentity", Subscriber.class)
				.setParameter("publisherKey", publisher.getKey())
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public List<Subscriber> getSubscribers(IdentityRef identity, List<Publisher> publishers) {
		if(publishers == null || publishers.isEmpty()) return new ArrayList<>();
		
		String query = """
				select sub from notisub as sub
				inner join sub.publisher as pub
				where sub.identity.key=:identityKey and sub.publisher.key in (:publishersKeys)""";
		
		List<Long> publishersKeys = publishers.stream()
				.map(Publisher::getKey)
				.toList();
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Subscriber.class)
				.setParameter("publishersKeys", publishersKeys)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public boolean hasSubscribers(IdentityRef identity, List<Publisher> publishers) {
		if(publishers == null || publishers.isEmpty()) return false;
		
		String query = """
				select sub.key from notisub as sub
				inner join sub.publisher as pub
				where sub.identity.key=:identityKey and sub.publisher.key in (:publishersKeys)
				and sub.enabled=true""";
		
		List<Long> publishersKeys = publishers.stream()
				.map(Publisher::getKey)
				.toList();
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("publishersKeys", publishersKeys)
				.setParameter("identityKey", identity.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null && keys.get(0).longValue() > 0;
	}
	
	public List<Subscriber> getSubscribers(PublisherData data, PublisherChannel channel) {
		String query = """
				select sub from notisub sub
				inner join fetch sub.identity as ident
				inner join fetch ident.user as identUser
				inner join fetch sub.publisher as pub
				where pub.type=:publisherType and pub.data=:publisherData
				and pub.channelType=:channel""";

		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Subscriber.class)
				.setParameter("publisherType", data.getType())
				.setParameter("publisherData", data.getData())
				.setParameter("channel", channel)
				.getResultList();
	}

	public Subscriber getSubscriberOfRootPublisher(IdentityRef identity, SubscriptionContext subscriptionContext) {
		QueryBuilder q = new QueryBuilder(256);		
		q.append("select sub from notisub as sub ")
		 .append(" inner join sub.publisher as pub ")
		 .where().append(" sub.identity.key=:anIdentityKey and pub.resName=:resName and pub.resId=:resId");
		if(StringHelper.containsNonWhitespace(subscriptionContext.getSubidentifier())) {
			q.and().append(" pub.subidentifier=:subidentifier");
		} else {
			q.and().append(" (pub.subidentifier=:subidentifier or pub.subidentifier is null)");
		}
		q.and().append(" pub.rootPublisher is null");
	
		List<Subscriber> subscribers = dbInstance.getCurrentEntityManager()
				.createQuery(q.toString(), Subscriber.class)
				.setParameter("anIdentityKey", identity.getKey())
				.setParameter("resName", subscriptionContext.getResName())
				.setParameter("resId", subscriptionContext.getResId())
				.setParameter("subidentifier", subscriptionContext.getSubidentifier())
				.getResultList();
		return subscribers.isEmpty() ? null : subscribers.get(0);
	}
	
	public Subscriber getSubscriber(IdentityRef identity, SubscriptionContext subscriptionContext, PublisherData data) {
		QueryBuilder q = new QueryBuilder();		
		q.append("select sub from notisub as sub ")
		 .append(" inner join sub.publisher as pub ")
		 .where().append(" sub.identity.key=:anIdentityKey and pub.resName=:resName and pub.resId=:resId");
		if(StringHelper.containsNonWhitespace(subscriptionContext.getSubidentifier())) {
			q.and().append("pub.subidentifier=:subidentifier");
		} else {
			q.and().append("(pub.subidentifier=:subidentifier or pub.subidentifier is null)");
		}
		q.and().append("pub.type=:publisherType and pub.data=:publisherData");

		List<Subscriber> subscribers = dbInstance.getCurrentEntityManager()
				.createQuery(q.toString(), Subscriber.class)
				.setParameter("anIdentityKey", identity.getKey())
				.setParameter("resName", subscriptionContext.getResName())
				.setParameter("resId", subscriptionContext.getResId())
				.setParameter("subidentifier", subscriptionContext.getSubidentifier())
				.setParameter("publisherType", data.getType())
				.setParameter("publisherData", data.getData())
				.getResultList();
		return subscribers.isEmpty() ? null : subscribers.get(0);
	}
	
	/**
	 * Load subscriber which are enabled, with publisher with news older than last email check,
	 * publisher which are enabled, with news older than a month
	 * 
	 * @param date Today
	 * @param defaultCompareDate Default comparison date if interval is not defined for the user
	 * @param maxCompareDate Don't search publisher with news older than this date
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	public List<Subscriber> getIdentityWithNews(Date date, Date defaultCompareDate, Date maxCompareDate, int firstResult, int maxResults) {
		String query = """
				select sub from notisub as sub
				inner join fetch sub.publisher as pub
				inner join fetch sub.identity as ident
				inner join fetch ident.user as identUser
				left join notimail mail on (mail.identity.key=sub.identity.key)
				where sub.enabled is true and pub.state=0 and ident.status<100 
				and (identUser.preferences.notificationInterval is null or identUser.preferences.notificationInterval<>'never')
				and sub.latestEmailed<=pub.latestNewsDate
				and pub.latestNewsDate>=:maxCompareDate
				and (mail.lastMail is null or (mail.nextMail is null and mail.lastMail<:defaultCompareDate) or mail.nextMail<=:date)
				and ((mail.nextMail is null and pub.latestNewsDate>=:defaultCompareDate) or pub.latestNewsDate>=mail.lastMail)
				order by ident.key, sub.key""";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Subscriber.class)
				.setParameter("date", date, TemporalType.TIMESTAMP)
				.setParameter("maxCompareDate", maxCompareDate, TemporalType.TIMESTAMP)
				.setParameter("defaultCompareDate", defaultCompareDate, TemporalType.TIMESTAMP)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
}
