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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.util.StringHelper;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.model.ItemImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 02.05.2017<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service("itemDao")
public class ItemDAO {

	@Autowired
	private DB dbInstance;

	/**
	 * Create a new Item and connect it with the feed.
	 * 
	 * @param feed
	 * @return the created item
	 */
	public Item createItem(Feed feed) {
		if (feed == null) return null;
		
		Item item = new ItemImpl(feed);
		item.setCreationDate(new Date());
		item.setLastModified(item.getCreationDate());
		dbInstance.getCurrentEntityManager().persist(item);
		return item;
	}
	
	/**
	 * Saves a new Item in the database. The Item has to have a Feed. Otherwise
	 * the Item is not saved an the method returns null.
	 * 
	 * @param feed
	 * @param item
	 * @return the created item or null
	 */
	public Item createItem(Feed feed, Item item) {
		if (feed == null) return null;
		
		ItemImpl itemImpl = (ItemImpl)item;
		itemImpl.setFeed(feed);
		if (itemImpl.getCreationDate() == null) {
			itemImpl.setCreationDate(new Date());
		}
		if (itemImpl.getLastModified() == null) {
			itemImpl.setLastModified(itemImpl.getCreationDate());
		}
		dbInstance.getCurrentEntityManager().persist(itemImpl);
		return itemImpl;
	}
	
	/**
	 * Copy an Item to an other (or the same) feed.<br>
	 * The attributes creationDate and lastModified are mandatory. If one of
	 * these is null, no new item is created and the method returns null.
	 * 
	 * @param feed
	 * @param item
	 * @return the created item or null
	 */
	public Item copyItem(Feed feed, Item item) {
		if (item == null) {
			return null;
		}
		
		Long key = item.getKey();
		Date creationDate = item.getCreationDate();
		Date lastModified = item.getLastModified();
		if (feed == null || key == null || creationDate == null || lastModified == null) {
			return null;
		}
		
		ItemImpl itemImpl = (ItemImpl) loadItem(item.getKey());
		dbInstance.getCurrentEntityManager().detach(itemImpl);
		itemImpl.setKey(null);
		itemImpl.setFeed(feed);
		dbInstance.getCurrentEntityManager().persist(itemImpl);
		return itemImpl;
	}

	/**
	 * Loads an item.
	 * 
	 * @param key the key of the item
	 * @return the loaded item
	 */
	public Item loadItem(Long key) {
		if (key == null) return null;
		
		return dbInstance.getCurrentEntityManager().find(ItemImpl.class, key);
	}

	/**
	 * Loads an item by GUID.
	 * The GUID is only unique inside a feed because when a external feed is
	 * stored two times the guid are stores two times as well.
	 * 
	 * @param feedKey the key of the feed
	 * @param guid the guid of the item
	 * @return the loaded item or null
	 */
	public Item loadItemByGuid(Long feedKey, String guid) {
		if (feedKey == null) return null;
		
		List<Item >items = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadItemByGuid", Item.class)
				.setParameter("feedKey", feedKey)
				.setParameter("guid", guid)
				.getResultList();
		
		return items != null && !items.isEmpty()? items.get(0): null;
	}
	
	/**
	 * Loads an item by GUID. This method is not safe because a GUID is only
	 * unique inside a feed. If more then one item have the same GUID, it return
	 * null.
	 * 
	 * @param guid
	 * @return
	 */
	public Item loadItemByGuid(String guid) {
		if (!StringHelper.containsNonWhitespace(guid)) return null;
		
		List<Item >items = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadItemByGuidWithoutFeed", Item.class)
				.setParameter("guid", guid)
				.getResultList();
		
		return items != null && items.size() == 1? items.get(0): null;
	}
	
	/**
	 * Loads all items of a feed.
	 * 
	 * @param feed
	 * @param filteredItemIds 
	 * @return the list of Items or null
	 */
	public List<Item> loadItems(Feed feed, List<Long> filteredItemIds) {
		if (feed == null) return null;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select data");
		sb.append("  from item data");
		sb.append(" where data.feed=:feed");
		if (filteredItemIds != null && !filteredItemIds.isEmpty()) {
			sb.append(" and data.key in (:filteredItemIds)");
		}
		
		 TypedQuery<Item> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Item.class)
				.setParameter("feed", feed);
		if (filteredItemIds != null && !filteredItemIds.isEmpty()) {
			query.setParameter("filteredItemIds", filteredItemIds);
		}
		return query.getResultList();
	}
	
	public List<Item> loadItemsByAuthor(IdentityRef author) {
		return dbInstance.getCurrentEntityManager()
			.createNamedQuery("loadItemsByAuthorWithFeed", Item.class)
			.setParameter("authorKey", author.getKey())
			.getResultList();
	}
	
	/**
	 * Loads all items of a feed.
	 * 
	 * @param feed
	 * @return the list of the guids or null
	 */
	public List<String> loadItemsGuid(Feed feed) {
		if (feed == null) return null;
		
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadItemsGuidByFeed", String.class)
				.setParameter("feed", feed)
				.getResultList();
	}

	/**
	 * Loads all published items of a feed.
	 * 
	 * @param feed
	 * @return the list of published Items or null
	 */
	public List<Item> loadPublishedItems(Feed feed) {
		if (feed == null) return null;
		
		// Load all items and filter in a second step instead of push the 
		// criteria to the query for the reason to use the same definition
		// of published everywhere.
		List<ItemImpl> items = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadItemsByFeed", ItemImpl.class)
				.setParameter("feed", feed)
				.getResultList()
				.stream()
				.filter((item) -> item.isPublished())
				.collect(Collectors.toList());
		return new ArrayList<>(items);
	}
	
	/**
	 * Check if the are items for a feed.
	 * 
	 * @param feed
	 * @return
	 */
	public boolean hasItems(Feed feed) {
		boolean hasItems = false;
		
		List<ItemImpl> items = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadItemsByFeed", ItemImpl.class)
				.setParameter("feed", feed)
				.setMaxResults(1)
				.getResultList();
		
		if (items != null && !items.isEmpty()) {
			hasItems = true;
		}
		
		return hasItems;
	}
	
	/**
	 * Update an item.
	 * 
	 * If it is an item of an internal feed, the last modified date is set.
	 * Items of external feeds get the last modified date from the external
	 * item.
	 * 
	 * @param item
	 * @return the updated item
	 */
	public Item updateItem(Item item) {
		if (item == null) return null;
		
		if (!item.getFeed().isExternal()) {
			((ItemImpl)item).setLastModified(new Date());
		}
		
		return dbInstance.getCurrentEntityManager().merge(item);
	}

	/**
	 * Deletes an item.
	 * 
	 * @param item
	 * @return the number of deleted items
	 */
	public int removeItem(Item item) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("removeItem").setParameter("key", item.getKey())
				.executeUpdate();
	}

	/**
	 * Deletes all Items of a Feed.
	 * 
	 * @param feed
	 */
	public int removeItems(Feed feed) {
		if (feed == null) return 0;
		
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("removeItemsForFeed").setParameter("feedKey", feed.getKey())
				.executeUpdate();
	}

}
