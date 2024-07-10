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

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.TagService;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.FeedTag;
import org.olat.modules.webFeed.FeedTagSearchParams;
import org.olat.modules.webFeed.Item;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Jun 29, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class FeedTagDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private TagService tagService;
	@Autowired
	private FeedTagDAO feedTagDAO;
	@Autowired
	private ItemDAO itemDAO;
	@Autowired
	private FeedDAO feedDAO;

	@Test
	public void shouldCreateFeedTag() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDAO.createFeedForResourceable(resource);
		dbInstance.commitAndCloseSession();

		Item feedItem = itemDAO.createItem(feed);
		Tag tag = tagService.getOrCreateTag(random());
		dbInstance.commitAndCloseSession();

		FeedTag feedTag = feedTagDAO.create(feed, feedItem, tag);
		dbInstance.commitAndCloseSession();

		assertThat(feedTag).isNotNull();
		assertThat(feedTag.getCreationDate()).isNotNull();
		assertThat(feedTag.getFeed()).isEqualTo(feed);
		assertThat(feedTag.getFeedItem()).isEqualTo(feedItem);
		assertThat(feedTag.getTag()).isEqualTo(tag);
	}

	@Test
	public void shouldDeleteFeedTag() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDAO.createFeedForResourceable(resource);
		dbInstance.commitAndCloseSession();

		Item feedItem = itemDAO.createItem(feed);
		Tag tag = tagService.getOrCreateTag(random());
		dbInstance.commitAndCloseSession();

		FeedTag feedTag = feedTagDAO.create(feed, feedItem, tag);
		dbInstance.commitAndCloseSession();

		feedTagDAO.delete(feedTag);
		dbInstance.commitAndCloseSession();

		FeedTagSearchParams params = new FeedTagSearchParams();
		params.setFeedKey(feedTag.getFeed().getKey());
		List<FeedTag> tags = feedTagDAO.loadTags(params);

		assertThat(tags).isEmpty();
	}

	@Test
	public void shouldDeleteByFeed() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed1 = feedDAO.createFeedForResourceable(resource);
		Feed feed2 = feedDAO.createFeedForResourceable(resource);
		dbInstance.commitAndCloseSession();

		Item feedItem1 = itemDAO.createItem(feed1);
		Item feedItem2 = itemDAO.createItem(feed2);
		Tag tag1 = tagService.getOrCreateTag(random());
		Tag tag2 = tagService.getOrCreateTag(random());
		dbInstance.commitAndCloseSession();

		feedTagDAO.create(feed1, feedItem1, tag1);
		feedTagDAO.create(feed1, feedItem1, tag2);
		FeedTag feedTag21 = feedTagDAO.create(feed2, feedItem2, tag1);
		FeedTag feedTag22 = feedTagDAO.create(feed2, feedItem2, tag2);
		dbInstance.commitAndCloseSession();

		feedTagDAO.delete(feed1);
		dbInstance.commitAndCloseSession();

		FeedTagSearchParams params = new FeedTagSearchParams();
		params.setFeedKey(feed2.getKey());
		List<FeedTag> tags = feedTagDAO.loadTags(params);

		assertThat(tags).containsExactlyInAnyOrder(feedTag21, feedTag22);
	}

	@Test
	public void shouldDeleteByFeedItem() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDAO.createFeedForResourceable(resource);
		dbInstance.commitAndCloseSession();

		Item feedItem1 = itemDAO.createItem(feed);
		Item feedItem2 = itemDAO.createItem(feed);
		Tag tag1 = tagService.getOrCreateTag(random());
		Tag tag2 = tagService.getOrCreateTag(random());
		dbInstance.commitAndCloseSession();

		feedTagDAO.create(feed, feedItem1, tag1);
		feedTagDAO.create(feed, feedItem1, tag2);
		FeedTag feedTag21 = feedTagDAO.create(feed, feedItem2, tag1);
		FeedTag feedTag22 = feedTagDAO.create(feed, feedItem2, tag2);
		dbInstance.commitAndCloseSession();

		feedTagDAO.delete(feedItem1);
		dbInstance.commitAndCloseSession();

		FeedTagSearchParams params = new FeedTagSearchParams();
		params.setFeedKey(feed.getKey());
		List<FeedTag> tags = feedTagDAO.loadTags(params);

		assertThat(tags).containsExactlyInAnyOrder(feedTag21, feedTag22);
	}

	@Test
	public void shouldLoadFeedTagInfos() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDAO.createFeedForResourceable(resource);
		dbInstance.commitAndCloseSession();

		Item feedItem1 = itemDAO.createItem(feed);
		Item feedItem2 = itemDAO.createItem(feed);
		Item feedItem3 = itemDAO.createItem(feed);
		Tag tag1 = tagService.getOrCreateTag(random());
		Tag tag2 = tagService.getOrCreateTag(random());
		Tag tag3 = tagService.getOrCreateTag(random());
		Tag tag4 = tagService.getOrCreateTag(random());
		dbInstance.commitAndCloseSession();

		feedTagDAO.create(feed, feedItem1, tag1);
		feedTagDAO.create(feed, feedItem1, tag2);
		feedTagDAO.create(feed, feedItem1, tag3);
		feedTagDAO.create(feed, feedItem2, tag1);
		feedTagDAO.create(feed, feedItem2, tag2);
		feedTagDAO.create(feed, feedItem3, tag1);
		feedTagDAO.create(feed, feedItem3, tag4);
		dbInstance.commitAndCloseSession();

		// Tags with feedItem selection
		Map<Long, TagInfo> keyToTag = feedTagDAO.loadFeedTagInfos(feed, feedItem2).stream()
				.collect(Collectors.toMap(TagInfo::getKey, Function.identity()));
		// all tagInfos, not necessarily selected
		assertThat(keyToTag).hasSize(4);
		assertThat(keyToTag.get(tag1.getKey()).getCount()).isEqualTo(3);
		assertThat(keyToTag.get(tag2.getKey()).getCount()).isEqualTo(2);
		assertThat(keyToTag.get(tag3.getKey()).getCount()).isEqualTo(1);
		assertThat(keyToTag.get(tag1.getKey()).isSelected()).isTrue();
		assertThat(keyToTag.get(tag2.getKey()).isSelected()).isTrue();
		assertThat(keyToTag.get(tag3.getKey()).isSelected()).isFalse();

		// Tags without feedItem
		keyToTag = feedTagDAO.loadFeedTagInfos(feed, null).stream()
				.collect(Collectors.toMap(TagInfo::getKey, Function.identity()));
		// all tagInfos, not necessarily selected
		assertThat(keyToTag).hasSize(4);
		assertThat(keyToTag.get(tag1.getKey()).getCount()).isEqualTo(3);
		assertThat(keyToTag.get(tag2.getKey()).getCount()).isEqualTo(2);
		assertThat(keyToTag.get(tag3.getKey()).getCount()).isEqualTo(1);
		assertThat(keyToTag.get(tag4.getKey()).getCount()).isEqualTo(1);
		assertThat(keyToTag.get(tag1.getKey()).isSelected()).isFalse();
		assertThat(keyToTag.get(tag2.getKey()).isSelected()).isFalse();
		assertThat(keyToTag.get(tag3.getKey()).isSelected()).isFalse();
		assertThat(keyToTag.get(tag4.getKey()).isSelected()).isFalse();
	}

	@Test
	public void shouldLoadFeedTagInfosForFeedItems() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDAO.createFeedForResourceable(resource);
		dbInstance.commitAndCloseSession();

		Item feedItem1 = itemDAO.createItem(feed);
		Item feedItem2 = itemDAO.createItem(feed);
		Item feedItem3 = itemDAO.createItem(feed);
		Tag tag1 = tagService.getOrCreateTag(random());
		Tag tag2 = tagService.getOrCreateTag(random());
		Tag tag3 = tagService.getOrCreateTag(random());
		dbInstance.commitAndCloseSession();

		feedTagDAO.create(feed, feedItem1, tag1);
		feedTagDAO.create(feed, feedItem1, tag2);
		feedTagDAO.create(feed, feedItem2, tag1);
		feedTagDAO.create(feed, feedItem2, tag3);
		feedTagDAO.create(feed, feedItem3, tag1);
		feedTagDAO.create(feed, feedItem3, tag2);
		dbInstance.commitAndCloseSession();

		// Prepare feed item keys
		List<Long> feedItemKeys = List.of(feedItem1.getKey(), feedItem3.getKey());

		// Load the TagInfos based on the feed items
		List<TagInfo> tagInfos = feedTagDAO.loadFeedTagInfosForFeedItems(feed, feedItemKeys);

		// Verify the results
		Map<Long, TagInfo> keyToTag = tagInfos.stream()
				.collect(Collectors.toMap(TagInfo::getKey, Function.identity()));

		assertThat(keyToTag).hasSize(2);
		// tag1 is associated with feedItem1 and feedItem3
		assertThat(keyToTag.get(tag1.getKey()).getCount()).isEqualTo(2);
		// tag2 is associated with feedItem1 and feedItem3
		assertThat(keyToTag.get(tag2.getKey()).getCount()).isEqualTo(2);
		// tag3 is not associated with feedItem1 or feedItem3
		assertThat(keyToTag.get(tag3.getKey())).isNull();
	}

	@Test
	public void shouldLoadTags_filterByFeed() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDAO.createFeedForResourceable(resource);
		dbInstance.commitAndCloseSession();

		Item feedItem = itemDAO.createItem(feed);
		Tag tag = tagService.getOrCreateTag(random());
		FeedTag feedTag = feedTagDAO.create(feed, feedItem, tag);
		dbInstance.commitAndCloseSession();

		FeedTagSearchParams params = new FeedTagSearchParams();
		params.setFeedKey(feed.getKey());
		List<FeedTag> feedTags = feedTagDAO.loadTags(params);

		assertThat(feedTags).containsExactlyInAnyOrder(feedTag);
	}

	@Test
	public void shouldLoadTags_filterByFeedItems() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDAO.createFeedForResourceable(resource);
		dbInstance.commitAndCloseSession();

		// Create three feed items for the feed
		Item feedItem1 = itemDAO.createItem(feed);
		Item feedItem2 = itemDAO.createItem(feed);
		Item feedItem3 = itemDAO.createItem(feed);

		// Create two tags
		Tag tag1 = tagService.getOrCreateTag(random());
		Tag tag2 = tagService.getOrCreateTag(random());
		dbInstance.commitAndCloseSession();

		// Associate feed items with tags
		FeedTag feedTag11 = feedTagDAO.create(feed, feedItem1, tag1);
		FeedTag feedTag12 = feedTagDAO.create(feed, feedItem1, tag2);
		FeedTag feedTag21 = feedTagDAO.create(feed, feedItem2, tag1);
		FeedTag feedTag22 = feedTagDAO.create(feed, feedItem2, tag2);
		FeedTag feedTag31 = feedTagDAO.create(feed, feedItem3, tag1);
		FeedTag feedTag32 = feedTagDAO.create(feed, feedItem3, tag2);
		dbInstance.commitAndCloseSession();

		// Set up search parameters to filter by specific feed items
		FeedTagSearchParams params = new FeedTagSearchParams();
		params.setFeedItemKeys(List.of(feedItem1.getKey(), feedItem3.getKey()));

		// Load the tags based on the search parameters
		List<FeedTag> feedTags = feedTagDAO.loadTags(params);

		// Verify that the correct tags are returned
		// Also ensure tags for feedItem2 are not included
		assertThat(feedTags)
				.containsExactlyInAnyOrder(feedTag11, feedTag12, feedTag31, feedTag32)
				.doesNotContain(feedTag21, feedTag22);
	}
}