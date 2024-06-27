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
package org.olat.modules.webFeed.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.model.TagImpl;
import org.olat.core.id.Persistable;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.FeedTag;
import org.olat.modules.webFeed.Item;

/**
 * Initial date: Jun 19, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Entity(name = "feedtag")
@Table(name = "o_feed_tag")
public class FeedTagImpl implements FeedTag, Persistable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, insertable = true, updatable = false)
	private Long key;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creationdate", nullable = false, insertable = true, updatable = false)
	private Date creationDate;

	@ManyToOne(targetEntity = FeedImpl.class, fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "fk_feed", nullable = false, insertable = true, updatable = false)
	private Feed feed;
	@ManyToOne(targetEntity = ItemImpl.class, fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "fk_feed_item", nullable = false, insertable = true, updatable = false)
	private Item feedItem;
	@ManyToOne(targetEntity = TagImpl.class, fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "fk_tag", nullable = true, insertable = true, updatable = false)
	private Tag tag;


	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

	@Override
	public Item getFeedItem() {
		return feedItem;
	}

	public void setFeedItem(Item feedItem) {
		this.feedItem = feedItem;
	}

	@Override
	public Tag getTag() {
		return tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}


	@Override
	public int hashCode() {
		return key == null ? 236520 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof FeedTagImpl feedTag) {
			return key != null && key.equals(feedTag.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
