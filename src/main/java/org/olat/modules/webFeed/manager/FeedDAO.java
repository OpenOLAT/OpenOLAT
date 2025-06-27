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
package org.olat.modules.webFeed.manager;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.id.OLATResourceable;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.model.FeedImpl;
import org.olat.modules.webFeed.model.PublisherSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 02.05.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service("feedDao")
public class FeedDAO {
	
	@Autowired
	private DB dbInstance;
	
	public Feed createFeedForResourceable(OLATResourceable ores) {
		if (ores == null) return null;
		
		Feed feed = new FeedImpl(ores);
		feed.setCreationDate(new Date());
		feed.setLastModified(feed.getCreationDate());
		feed.setCanComment(true);
		feed.setCanRate(true);
		dbInstance.getCurrentEntityManager().persist(feed);
		return feed;
	}
	
	public Feed createFeed(Feed feed) {
		if (feed == null) return null;
		
		FeedImpl feedImpl = (FeedImpl) feed;
		if (feedImpl.getCreationDate() == null) {
			feedImpl.setCreationDate(new Date());
		}
		if (feedImpl.getLastModified() == null) {
			feedImpl.setLastModified(feedImpl.getCreationDate());	
		}
		dbInstance.getCurrentEntityManager().persist(feedImpl);
		return feed;
	}
	
	public Feed copyFeed(OLATResourceable source, OLATResourceable target) {
		if (source == null || target == null) return null;
		
		FeedImpl feed = (FeedImpl) loadFeed(source);
		dbInstance.getCurrentEntityManager().detach(feed);
		feed.setKey(null);
		feed.setResourceableId(target.getResourceableId());
		feed.setResourceableType(target.getResourceableTypeName());
		dbInstance.getCurrentEntityManager().persist(feed);
		return feed;
	}
	
	public Feed loadFeed(Long key) {
		if(key == null) return null;
		
		List<FeedImpl> feeds = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadFeedByKey", FeedImpl.class)
				.setParameter("key", key)
				.getResultList();
		return feeds == null || feeds.isEmpty() ? null : feeds.get(0);
	}
	
	public Feed loadFeed(OLATResourceable resourceable) {
		if (resourceable == null) return null;
		
		List<FeedImpl> feeds = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadFeedByRessourceable", FeedImpl.class)
				.setParameter("key", resourceable.getResourceableId())
				.setParameter("name", resourceable.getResourceableTypeName())
				.getResultList();
		return feeds.isEmpty() ? null : feeds.get(0);
	}
	
	public Feed updateFeed(Feed feed) {
		if (feed == null) return null;
		
		((FeedImpl)feed).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(feed);
	}
	
	public void removeFeedForResourceable(OLATResourceable resourceable) {
		if (resourceable == null) return;
		
		Feed feed = loadFeed(resourceable);
		if (feed != null) {
			dbInstance.getCurrentEntityManager().remove(feed);
		}
	}
	
	public List<PublisherSubscriber> getPublisherByType(IdentityRef identity, String publisherType, String data, Long resourceKey, List<GroupRoles> roles) {
		String q = """
			select pub, sub from notipublisher pub
			left join notisub as sub on (pub.key=sub.publisher.key and sub.identity.key=:identityKey)
			where pub.type=:type and pub.data=:data
			and pub.resName=:resName and pub.resId in (select courseOres.resId from olatresource courseOres
			  inner join references as ref on (ref.source.key=courseOres.key)
			  inner join repositoryentry as v on (courseOres.key=v.olatResource.key)
			  inner join v.groups as rel
			  inner join rel.group as reGroup
		      inner join reGroup.members as reMember
			  where ref.target.key=:resourceKey
			  and reMember.identity.key=:identityKey and reMember.role in (:roles)
			)""";
		
		List<String> rolesList = roles.stream()
				.map(GroupRoles::name).toList();
		
		List<Object[]> rawObjectList = dbInstance.getCurrentEntityManager()
				.createQuery(q.toString(), Object[].class)
				.setParameter("resName", "CourseModule")
				.setParameter("type", publisherType)
				.setParameter("data", data)
				.setParameter("resourceKey", resourceKey)
				.setParameter("identityKey", identity.getKey())
				.setParameter("roles", rolesList)
				.getResultList();
		return rawObjectList.stream().map(arr -> {
			Publisher publisher = (Publisher)arr[0];
			Subscriber subscriber = (Subscriber)arr[1];
			return new PublisherSubscriber(publisher, subscriber);
		}).toList();
	}
	
}
