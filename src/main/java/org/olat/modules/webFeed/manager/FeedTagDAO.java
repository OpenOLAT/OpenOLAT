/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.webFeed.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.FeedTag;
import org.olat.modules.webFeed.FeedTagSearchParams;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.model.FeedTagImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial date: Jun 19, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Component
public class FeedTagDAO {

	@Autowired
	private DB dbInstance;

	/**
	 * Create new FeedTag entry
	 * @param feed
	 * @param feedItem
	 * @param tag
	 * @return new FeedTag
	 */
	public FeedTag create(Feed feed, Item feedItem, Tag tag) {
		FeedTagImpl feedTag = new FeedTagImpl();
		feedTag.setCreationDate(new Date());
		feedTag.setFeed(feed);
		feedTag.setFeedItem(feedItem);
		feedTag.setTag(tag);
		dbInstance.getCurrentEntityManager().persist(feedTag);
		return feedTag;
	}

	/**
	 * delete given feedTag from DB
	 * @param feedTag
	 */
	public void delete(FeedTag feedTag) {
		QueryBuilder qb = new QueryBuilder();
		qb.append("delete from feedtag feedTag");
		qb.and().append("feedTag.key = :key");

		dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString())
				.setParameter("key", feedTag.getKey())
				.executeUpdate();
	}

	/**
	 * delete all tags corresponding to the feed
	 * @param feed
	 */
	public void delete(Feed feed) {
		QueryBuilder qb = new QueryBuilder();
		qb.append("delete from feedtag feedTag");
		qb.and().append("feedTag.feed.key = :feedKey");

		dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString())
				.setParameter("feedKey", feed.getKey())
				.executeUpdate();
	}

	/**
	 * delete all tags corresponding to the feedItem
	 * @param feedItem
	 */
	public void delete(Item feedItem) {
		QueryBuilder qb = new QueryBuilder();
		qb.append("delete from feedtag feedTag");
		qb.and().append("feedTag.feedItem.key = :feedItemKey");

		dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString())
				.setParameter("feedItemKey", feedItem.getKey())
				.executeUpdate();
	}

	/**
	 * load TagInfos for all tags inside a feed with count
	 * @param feed
	 * @param feedItem select all the tags this feedItem already has, can be null
	 * @return
	 */
	public List<TagInfo> loadFeedTagInfos(Feed feed, Item feedItem) {
		QueryBuilder qb = new QueryBuilder();
		qb.append("select new org.olat.core.commons.services.tag.model.TagInfoImpl(");
		qb.append(" tag.key");
		qb.append(" , min(tag.creationDate)");
		qb.append(" , min(tag.displayName)");
		qb.append(" , count(feedTag.feedItem.key)");
		if (feedItem != null) {
			qb.append(" , sum(case when (feedTag.feedItem.key = :feedItemKey) then 1 else 0 end) as selected");
		} else {
			qb.append(" , cast(0 as long) as selected");
		}
		qb.append(")");

		qb.append(" from feedtag feedTag");
		qb.append(" inner join feedTag.tag tag");
		qb.and().append("feedTag.feed.key = :feedKey");
		qb.groupBy().append("tag.key");

		TypedQuery<TagInfo> query = dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString(), TagInfo.class)
				.setParameter("feedKey", feed.getKey());

		if (feedItem != null) {
			query.setParameter("feedItemKey", feedItem.getKey());
		}

		return query.getResultList();
	}

	/**
	 * load TagInfos for given feedItemKeys
	 * @param feed
	 * @param feedItemKeys
	 * @return list of tagInfo object
	 */
	public List<TagInfo> loadFeedTagInfosForFeedItems(Feed feed, List<Long> feedItemKeys) {
		QueryBuilder qb = new QueryBuilder();
		qb.append("select new org.olat.core.commons.services.tag.model.TagInfoImpl(");
		qb.append(" tag.key");
		qb.append(" , min(tag.creationDate)");
		qb.append(" , min(tag.displayName)");
		qb.append(" , count(feedTag.feedItem.key)");
		qb.append(" , cast(0 as long) as selected");
		qb.append(")");

		qb.append(" from feedtag feedTag");
		qb.append(" inner join feedTag.tag tag");
		qb.and().append("feedTag.feed.key = :feedKey");
		qb.and().append("feedTag.feedItem.key in :feedItemKeys");
		qb.groupBy().append("tag.key");

		TypedQuery<TagInfo> query = dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString(), TagInfo.class)
				.setParameter("feedKey", feed.getKey())
				.setParameter("feedItemKeys", feedItemKeys);

		return query.getResultList();
	}


	/**
	 * load Tags based on searchParameters, such as feedKey and feedItemKey
	 * @param searchParams
	 * @return
	 */
	public List<FeedTag> loadTags(FeedTagSearchParams searchParams) {
		QueryBuilder qb = new QueryBuilder();
		qb.append("select feedTag from feedtag feedTag");
		qb.append(" inner join fetch feedTag.tag tag");
		qb.append(" left join fetch feedTag.feedItem feedItem");
		qb.append(" left join fetch feedTag.feed feed");

		if (searchParams.getFeedKey() != null) {
			qb.and().append("feedTag.feed.key = :feedKey");
		}
		if (searchParams.getFeedItemKeys() != null && !searchParams.getFeedItemKeys().isEmpty()) {
			qb.and().append("feedTag.feedItem.key in :feedItemKeys");
		}

		TypedQuery<FeedTag> query = dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString(), FeedTag.class);

		if (searchParams.getFeedKey() != null) {
			query.setParameter("feedKey", searchParams.getFeedKey());
		}
		if (searchParams.getFeedItemKeys() != null && !searchParams.getFeedItemKeys().isEmpty()) {
			query.setParameter("feedItemKeys", searchParams.getFeedItemKeys());
		}

		return query.getResultList();
	}
}
