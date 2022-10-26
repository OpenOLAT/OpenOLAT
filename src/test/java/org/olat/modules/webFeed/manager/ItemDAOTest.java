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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.webFeed.Enclosure;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.model.EnclosureImpl;
import org.olat.modules.webFeed.model.ItemImpl;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 05.05.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ItemDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private FeedDAO feedDao;
	@Autowired
	private ItemDAO itemDao;
	
	@Test
	public void createItem() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();
		
		Item item = itemDao.createItem(feed);
		dbInstance.commitAndCloseSession();

		//check values
		assertThat(item.getKey()).isNotNull();
		assertThat(item.getCreationDate()).isNotNull();
		assertThat(item.getLastModified()).isNotNull();
		assertThat(item.getFeed()).isEqualTo(feed);
	}
	
	@Test
	public void createItem_Null() {
		Item item = itemDao.createItem(null);
		dbInstance.commitAndCloseSession();

		//check values
		assertThat(item).isNull();
	}
	
	@Test
	public void createItem_Feed_Item() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();
		
		Item tempItem = new ItemImpl(feed);
		
		Item item = itemDao.createItem(feed, tempItem);
		dbInstance.commitAndCloseSession();

		//check values
		assertThat(item.getKey()).isNotNull();
		assertThat(item.getCreationDate()).isNotNull();
		assertThat(item.getLastModified()).isNotNull();
		assertThat(item.getFeed()).isEqualTo(feed);
	}
	
	@Test
	public void createItem_Feed_Item_keepDates() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();
		
		Item tempItem = new ItemImpl(feed);
		Date created = new GregorianCalendar(2000, 1, 1).getTime();
		tempItem.setCreationDate(created);
		Date modified = new GregorianCalendar(2000, 2, 2).getTime();
		tempItem.setLastModified(modified);
		
		Item item = itemDao.createItem(feed, tempItem);
		dbInstance.commitAndCloseSession();

		//check values
		assertThat(item.getCreationDate()).isCloseTo(created, 1000);
		assertThat(item.getLastModified()).isCloseTo(modified, 1000);
	}
	
	@Test
	public void createItem_Feed_Null() {
		Item tempItem = new ItemImpl(null);
		Item item = itemDao.createItem(null, tempItem);
		dbInstance.commitAndCloseSession();

		//check values
		assertThat(item).isNull();
	}
	
	@Test
	public void copyItem_Feed_Item() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();
		Item item = itemDao.createItem(feed);
		dbInstance.commitAndCloseSession();
		
		Item copy = itemDao.copyItem(feed, item);
		dbInstance.commitAndCloseSession();

		//check values
		assertThat(copy.getKey()).isNotEqualTo(item.getKey());
		assertThat(copy.getCreationDate()).isCloseTo(item.getCreationDate(), 1000);
		assertThat(copy.getLastModified()).isCloseTo(item.getLastModified(), 1000);
	}
	
	@Test
	public void copyItem_Feed_null() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();
		Item item = itemDao.createItem(feed);
		dbInstance.commitAndCloseSession();
		
		Item copy = itemDao.copyItem(null, item);
		dbInstance.commitAndCloseSession();

		//check values
		assertThat(copy).isNull();
	}
	
	@Test
	public void copyItem_Feed_Item_null() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();
		
		Item copy = itemDao.copyItem(feed, null);
		dbInstance.commitAndCloseSession();

		//check values
		assertThat(copy).isNull();
	}
	
	@Test
	public void copyItem_Feed_Item_noCreationDate() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();
		Item item = itemDao.createItem(feed);
		dbInstance.commitAndCloseSession();
		item.setCreationDate(null);
		
		Item copy = itemDao.copyItem(feed, item);
		dbInstance.commitAndCloseSession();

		//check values
		assertThat(copy).isNull();
	}
	
	@Test
	public void copyItem_Feed_Item_noLastModified() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();
		Item item = itemDao.createItem(feed);
		dbInstance.commitAndCloseSession();
		item.setLastModified(null);
		
		Item copy = itemDao.copyItem(feed, item);
		dbInstance.commitAndCloseSession();

		//check values
		assertThat(copy).isNull();
	}
	
	@Test
	public void loadItem() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();

		// create an item
		Item item = itemDao.createItem(feed);
		dbInstance.commitAndCloseSession();
		
		// reload the item from the database
		Item reloaded = itemDao.loadItem(item.getKey());

		//check values
		assertThat(reloaded.getKey()).isEqualTo(item.getKey());
		assertThat(reloaded.getFeed()).isEqualTo(feed);
	}
	
	@Test
	public void loadItem_notExisting() {	
		// load item for a non existing key
		Item item = itemDao.loadItem(-1L);

		// the item should be null
		assertThat(item).isNull();
	}
	
	@Test
	public void loadItemByGuid() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();

		// create an item
		String guid = "guid-123";
		Item tempItem = new ItemImpl(feed);
		tempItem.setGuid(guid);
		itemDao.createItem(feed, tempItem);
		dbInstance.commitAndCloseSession();
		
		// reload the item from the database
		Item item = itemDao.loadItemByGuid(feed.getKey(), guid);

		//check values
		assertThat(item).isNotNull();
	}
	
	@Test
	public void loadItemByGuid_guidTwoTimes() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		OLATResource resource2 = JunitTestHelper.createRandomResource();
		Feed feed2 = feedDao.createFeedForResourcable(resource2);
		dbInstance.commitAndCloseSession();

		// create an item
		String guid = "guid-123";
		Item tempItem = new ItemImpl(feed);
		tempItem.setGuid(guid);
		itemDao.createItem(feed, tempItem);
		Item tempItem2 = new ItemImpl(feed2);
		tempItem2.setGuid(guid);
		itemDao.createItem(feed2, tempItem2);
		dbInstance.commitAndCloseSession();
		
		// reload the item from the database
		Item item = itemDao.loadItemByGuid(feed.getKey(), guid);

		//check values
		assertThat(item).isNotNull();
	}
	
	@Test
	public void loadItemByGuid_notExisting() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();
		
		// load item for a non existing guid
		Item item = itemDao.loadItemByGuid(feed.getKey(), "guid-no");

		// the item should be null
		assertThat(item).isNull();
	}

	
	@Test
	public void loadItemByGuid_Feed_null() {	
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();

		// create an item
		String guid = "guid-123";
		Item tempItem = new ItemImpl(feed);
		tempItem.setGuid(guid);
		itemDao.createItem(feed, tempItem);
		dbInstance.commitAndCloseSession();
		
		// reload the item from the database
		Item item = itemDao.loadItemByGuid(null, guid);

		//check values
		assertThat(item).isNull();
	}
	
	@Test
	public void loadItemByGuid_without_feed() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();

		// create an item
		String guid = "guid-123467890";
		Item tempItem = new ItemImpl(feed);
		tempItem.setGuid(guid);
		itemDao.createItem(feed, tempItem);
		dbInstance.commitAndCloseSession();
		
		// reload the item from the database
		Item item = itemDao.loadItemByGuid(guid);

		//check values
		assertThat(item).isNotNull();
		
		//clean up
		itemDao.removeItem(item);
	}

	@Test
	public void loadItemByGuid_without_feed_multiple_guids() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();

		// create an item
		String guid = "guid-12345";
		Item tempItem = new ItemImpl(feed);
		tempItem.setGuid(guid);
		itemDao.createItem(feed, tempItem);
		dbInstance.commitAndCloseSession();
		tempItem = new ItemImpl(feed);
		tempItem.setGuid(guid);
		itemDao.createItem(feed, tempItem);
		dbInstance.commitAndCloseSession();
		
		// reload the item from the database
		Item item = itemDao.loadItemByGuid(guid);

		//check values
		assertThat(item).isNull();
	}
	
	@Test
	public void shouldLoadItemsOfAFeedUnfiltered() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();

		// create three items
		int numberOfItems = 3;
		for (int i = 0; i < numberOfItems; i++) {
			itemDao.createItem(feed);
		}
		dbInstance.commitAndCloseSession();
		
		List<Item> items = itemDao.loadItems(feed, null);

		// check if all three items of the feed are loaded
		assertThat(items.size()).isEqualTo(3);
	}
	
	@Test
	public void shouldLoadItemsOfAFeedFiltered() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		Item item1 = itemDao.createItem(feed);
		Item item2 = itemDao.createItem(feed);
		Item item3 = itemDao.createItem(feed);
		Item item4 = itemDao.createItem(feed);
		dbInstance.commitAndCloseSession();
		
		List<Item> items = itemDao.loadItems(feed, Arrays.asList(item1.getKey(), item3.getKey()));

		assertThat(items)
				.containsExactlyInAnyOrder(item1, item3)
				.doesNotContain(item2, item4);
	}
	
	@Test
	public void loadItemsGuid_Feed() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();

		// create three items
		int numberOfItems = 3;
		for (int i = 0; i < numberOfItems; i++) {
			itemDao.createItem(feed);
		}
		dbInstance.commitAndCloseSession();
		
		List<String> items = itemDao.loadItemsGuid(feed);

		// check if all three guids of the feed are loaded
		assertThat(items.size()).isEqualTo(3);
	}
	
	@Test
	public void loadItemsGuid_null() {
		List<String> items = itemDao.loadItemsGuid(null);

		assertThat(items).isNull();
	}
	
	@Test
	public void loadPublishedItems_Feed() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();

		// create three items
		int numberOfItems = 4;
		for (int i = 0; i < numberOfItems; i++) {
			Item item = new ItemImpl(feed);
		
			// Every 2nd Item has a publish date in the past
			if (i%2 == 0) {
				Date date = Date.from(LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
				item.setPublishDate(date);
			}
			itemDao.createItem(feed, item);
		}
		dbInstance.commitAndCloseSession();
		
		List<Item> items = itemDao.loadPublishedItems(feed);

		// check if two items of the feed are loaded
		assertThat(items.size()).isEqualTo(2);
	}

	
	@Test
	public void loadPublishedItems_null() {
		List<Item> items = itemDao.loadPublishedItems(null);

		assertThat(items).isNull();
	}
	
	@Test
	public void hasItems_Feed_true() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();

		// create three items
		int numberOfItems = 3;
		for (int i = 0; i < numberOfItems; i++) {
			itemDao.createItem(feed);
		}
		dbInstance.commitAndCloseSession();
		
		boolean hasItems = itemDao.hasItems(feed);

		assertThat(hasItems).isTrue();
	}
	
	@Test
	public void hasItems_Feed_false() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();
		
		boolean hasItems = itemDao.hasItems(feed);

		assertThat(hasItems).isFalse();
	}
	
	@Test
	public void hasItems_null() {
		boolean hasItems = itemDao.hasItems(null);

		assertThat(hasItems).isFalse();
	}
	
	@Test
	public void updateItem() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();
		
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("user-1234");
		
		Item item = itemDao.createItem(feed);
		dbInstance.commitAndCloseSession();
		
		// update the item
		item.setAuthor("author");
		item.setAuthorKey(1L);
		item.setContent("content");
		item.setDescription("description");
		item.setDraft(true);
		item.setExternalLink("https://example.com/");
		item.setGuid("guid-2");
		item.setHeight(3);
		item.setModifierKey(author.getKey());
		item.setPublishDate(new Date());
		item.setTitle("tile");
		item.setWidth(5);
		Enclosure enclosure = new EnclosureImpl();
		enclosure.setExternalUrl("http://exterla.url/abc.jpg");
		enclosure.setFileName("Filename.jpg");
		enclosure.setLength(9L);
		enclosure.setType("type");
		item.setEnclosure(enclosure);
		Item updated = itemDao.updateItem(item);

		//check values
		assertThat(updated.getAuthor()).isEqualTo(item.getAuthor());
		assertThat(updated.getAuthorKey()).isEqualTo(item.getAuthorKey());
		assertThat(updated.getContent()).isEqualTo(item.getContent());
		assertThat(updated.getDescription()).isEqualTo(item.getDescription());
		assertThat(updated.isDraft()).isEqualTo(item.isDraft());
		assertThat(updated.getExternalLink()).isEqualTo(item.getExternalLink());
		assertThat(updated.getGuid()).isEqualTo(item.getGuid());
		assertThat(updated.getHeight()).isEqualTo(item.getHeight());
		assertThat(updated.getModifierKey()).isEqualTo(item.getModifierKey());
		assertThat(updated.getPublishDate()).hasSameTimeAs(item.getPublishDate());
		assertThat(updated.getTitle()).isEqualTo(item.getTitle());
		assertThat(updated.getWidth()).isEqualTo(item.getWidth());
		assertThat(updated.getEnclosure().getExternalUrl()).isEqualTo(item.getEnclosure().getExternalUrl());
		assertThat(updated.getEnclosure().getFileName()).isEqualTo(item.getEnclosure().getFileName());
		assertThat(updated.getEnclosure().getLength()).isEqualTo(item.getEnclosure().getLength());
		assertThat(updated.getEnclosure().getType()).isEqualTo(item.getEnclosure().getType());
	}
	
	@Test
	public void updateItem_null() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();
		Item item = itemDao.createItem(null);
		dbInstance.commitAndCloseSession();

		Item updated = itemDao.updateItem(item);
		
		assertThat(updated).isNull();
	}
		
	
	@Test
	public void removeItem() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();
		
		// create three items
		Item item1 = itemDao.createItem(feed);
		Item item2 = itemDao.createItem(feed);
		Item item3 = itemDao.createItem(feed);
		dbInstance.commitAndCloseSession();
				
		// delete one item
		int numberOfDeletedItems = itemDao.removeItem(item2);
		dbInstance.commitAndCloseSession();
		
		// one item should be deleted
		assertThat(numberOfDeletedItems).isEqualTo(1);
		
		// check if one item is deleted and the two other items are still in
		// the database
		assertThat(itemDao.loadItem(item1.getKey())).isNotNull();
		assertThat(itemDao.loadItem(item2.getKey())).isNull();
		assertThat(itemDao.loadItem(item3.getKey())).isNotNull();
	}
	
	@Test
	public void removeItems_Feed() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();
		
		// create three items
		Item item1 = itemDao.createItem(feed);
		Item item2 = itemDao.createItem(feed);
		Item item3 = itemDao.createItem(feed);
		dbInstance.commitAndCloseSession();
				
		// delete all items of the feed
		int numberOfDeletedItems = itemDao.removeItems(feed);
		dbInstance.commitAndCloseSession();
		
		// three item should be deleted
		assertThat(numberOfDeletedItems).isEqualTo(3);
		
		// check if items are deleted
		assertThat(itemDao.loadItem(item1.getKey())).isNull();
		assertThat(itemDao.loadItem(item2.getKey())).isNull();
		assertThat(itemDao.loadItem(item3.getKey())).isNull();
		
		// the feed should still exist
		assertThat(feedDao.loadFeed(resource)).isNotNull();
	}
	
	@Test
	public void removeItems_Feed_isNull() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();
		
		// create three items
		Item item1 = itemDao.createItem(feed);
		Item item2 = itemDao.createItem(feed);
		Item item3 = itemDao.createItem(feed);
		dbInstance.commitAndCloseSession();
				
		// remove should be handled properly
		int numberOfDeletedItems = itemDao.removeItems(null);
		dbInstance.commitAndCloseSession();
		
		// no item should be deleted
		assertThat(numberOfDeletedItems).isEqualTo(0);
		
		// check if still all items are in the database
		assertThat(itemDao.loadItem(item1.getKey())).isNotNull();
		assertThat(itemDao.loadItem(item2.getKey())).isNotNull();
		assertThat(itemDao.loadItem(item3.getKey())).isNotNull();
	}
	
}
