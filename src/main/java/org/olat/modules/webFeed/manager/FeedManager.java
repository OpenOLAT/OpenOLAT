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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or ied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.webFeed.manager;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.Item;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;

/**
 * The <code>FeedManager</code> singleton is responsible for dealing with feed
 * resources.
 *
 * <P>
 * Initial Date: Feb 11, 2009 <br>
 *
 * @author gwassmann
 */
public abstract class FeedManager {

	protected static FeedManager INSTANCE;

	public static final String MEDIA_DIR = "media";
	public static final String RSS_FEED_NAME = "feed.rss";
	public static final String RESOURCE_NAME = "feed";

	// Definition of different kinds of feeds. By convention, the kind is a single
	// noun designating the feed. (See also getFeedKind().)
	public static final String KIND_PODCAST = "podcast";
	public static final String KIND_BLOG = "blog";

	/**
	 * Use this method instead of any constructor to get the singelton object.
	 *
	 * @return INSTANCE
	 */
	public static final FeedManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Creates a blank OLAT podcast resource
	 *
	 * @return The resource
	 */
	public abstract OLATResourceable createPodcastResource();

	/**
	 * Check if a feed has items
	 *
	 * @param feed
	 * @return
	 */
	public abstract boolean hasItems(Feed feed);

	/**
	 * Creates a blank OLAT blog resource
	 *
	 * @return The resource
	 */
	public abstract OLATResourceable createBlogResource();

	/**
	 * Deletes a feed.
	 *
	 * @param feed
	 */
	public abstract void deleteFeed(OLATResourceable feed);

	/**
	 * Copies a given feed resourceable
	 *
	 * @param feed
	 */
	public abstract boolean copy(OLATResource source, OLATResource target);

	/**
	 * Enrich the feed with the properties in the RepositoryEntry.
	 *
	 * @param feed
	 * @param entry
	 * @param changedBy 
	 * @return the same Feed object with actualized attributes
	 */
	public abstract Feed enrichFeedByRepositoryEntry(Feed feed, RepositoryEntry entry, Identity changedBy);

	/**
	 * Update the feed with the properties in the RepositoryEntry and save it
	 * in the database.
	 *
	 * @param entry
	 * @param changedBy 
	 * @return a new updated Feed object
	 */
	public abstract Feed updateFeedWithRepositoryEntry(RepositoryEntry entry, Identity changedBy);

	/**
	 * Create the given Item and saves the appropriate file (podcast, video etc.)
	 * on the file system.
	 *
	 * @param feed the item will be added to this feed
	 * @param item the item to add
	 * @param file the file of the item
	 * @return
	 */
	public abstract Feed createItem(Feed feed, Item item, FileElement file);


	/**
	 * Removes the given Item from the feed and delete the item from the
	 * database. Additionally the content on the file system (podcast etc.)
	 * and the comments and the ratings of the item are deleted.
	 *
	 * @param item the item to remove
	 * @return the feed without the removed item
	 */
	public abstract Feed deleteItem(Item item);

	/**
	 * Update the Item in the database and save the file element in the file
	 * system.
	 *
	 * @param modifiedItem
	 * @param file
	 * @return the updated feed
	 */
	public abstract Item updateItem(Item modifiedItem, FileElement file);

	/**
	 * Update the feed source mode. Additionally it deleted all Items of the Feed
	 * if the mode of the Feed changes.
	 *
	 * @param external true: set to be an external feed; false: this is an
	 *          internal feed; null: undefined
	 * @param feed
	 * @return Feed the updated feed object
	 */
	public abstract Feed updateFeedMode(Boolean external, Feed feed);

	/**
	 * Update the feed from the given feed object
	 *
	 * @param feed
	 * @return
	 */
	public abstract Feed updateFeed(Feed feed);

	/**
	 * Update the external URL of the feed, delete all items of the feed and
	 * download the items of the new external feed URL.
	 *
	 * @param feed
	 * @param externalFeedUrl
	 * @return
	 */
	public abstract Feed updateExternalFeedUrl(Feed feed, String externalFeedUrl);

	/**
	 * Load the Item with the given key from the database or NULL if no such
	 * item exists.
	 *
	 * @param key the key of the Item
	 * @return the loaded Item or NULL
	 */
	public abstract Item loadItem(Long key);

	/**
	 * Load the Item with the given guid from the database or NULL if no such
	 * item exists or more then one items with the same guid exist.
	 *
	 * @param itemId
	 * @return
	 */
	public abstract Item loadItemByGuid(String itemId);

	/**
	 * Load all items of the feed (from file system or the external feed)
	 *
	 * @param feed
	 */
	public abstract List<Item> loadItems(Feed feed);

	/**
	 * Load the guid of all Items of the feed
	 *
	 * @param feed
	 */
	public abstract List<String> loadItemsGuid(Feed feed);

	/**
	 * Load all published Items
	 *
	 * @param feed
	 */
	public abstract List<Item> loadPublishedItems(Feed feed);

	/**
	 * Load all Items of a feed and filter them in relation to the identity rights.
	 *
	 * @param feed
	 * @param filteredItemIds only the items with this IDs are loaded. Null or an empty List does not filter by IDs.
	 * @param callback
	 * @param identity
	 * @return
	 */
	public abstract List<Item> loadFilteredAndSortedItems(Feed feed, List<Long> filteredItemIds, FeedSecurityCallback callback, Identity identity);

	/**
	 * Returns the feed with the provided id or null if not found.
	 *
	 * @param feed The feed to be re-read
	 * @return The newly read feed
	 */
	public abstract Feed loadFeed(OLATResourceable feed);

	/**
	 * Returns the feed from the XML file inside the directory or null if not
	 * found.
	 *
	 * @param feedDir the directory which contains the feed file
	 * @return the feed or null
	 */
	public abstract Feed loadFeedFromXML(Path feedDir);

	/**
	 * In the early days all information about a feed where stored in XML files.
	 * This method migrates that old feeds from the XML files to the database.
	 * It first checks it the feed has to be migrated. If it has to, the XML
	 * files are read, the feed and the items are saved in the database and at
	 * the end the XML files are deleted. If the Feed is imported from an other
	 * system the identity keys should be deleted because they do not
	 * correspondent with the keys in the actual system.
	 *
	 * @param ores
	 * @param removeIdentityKeys
	 *            If true, the identity keys of the author and the modifier are
	 *            set to null.
	 */
	public abstract void importFeedFromXML(OLATResource ores, boolean removeIdentityKeys);

	/**
	 * Returns the media file of the item
	 *
	 * @param id
	 * @param resourceTypeName
	 * @param itemId
	 * @param fileName
	 * @return The media resource (audio or video file of the feed item)
	 */
	public abstract MediaResource createItemMediaFile(OLATResourceable feed, String itemId, String fileName);

	/**
	 * Returns the media file of the feed
	 *
	 * @param id
	 * @param resourceTypeName
	 * @param fileName
	 * @return The media file of the feed
	 */
	public abstract VFSLeaf createFeedMediaFile(OLATResourceable feed, String fileName, Size thumbnailSize);

	/**
	 * Returns the base URI of the feed including user identity key and token if
	 * necessary.
	 *
	 * @param feed
	 * @param idKey
	 * @return The base URI of the (RSS) feed
	 */
	public abstract String getFeedBaseUri(Feed feed, Identity identity, Roles roles, Long courseId, String nodeId);

	/**
	 * Creates the RSS feed resource.
	 *
	 * @param feedId
	 * @param type The resource type name
	 * @param identityKey
	 * @return The RSS feed as a MediaResource
	 */
	public abstract MediaResource createFeedFile(OLATResourceable feed, Identity identity, Roles roles, Long courseId, String nodeId);

	/**
	 * Creates and returns a zip-file media resource of the given feed resource
	 *
	 * @param resource
	 * @return A zip-file media resource
	 */
	public abstract MediaResource getFeedArchiveMediaResource(OLATResourceable resource);

	/**
	 * Create and returns a zip-file as VFSLeaf of the given feed resourue
	 *
	 * @param ores the resource
	 * @return The VFSLeaf
	 */
	public abstract VFSLeaf getFeedArchive(OLATResourceable ores);

	/**
	 * Returns the container of the item which belongs to the feed
	 *
	 * @param item
	 * @return The container of the item
	 */
	public abstract VFSContainer getItemContainer(Item item);

	/**
	 * Save the item in an XML file in the item container.
	 *
	 * @param item
	 */
	public abstract void saveItemAsXML(Item item);

	/**
	 * Delete the item XML file from the item container.
	 *
	 * @param item
	 */
	public abstract void deleteItemXML(Item item);

	/**
	 * Returns the File of the item's enclosure if it exists or null
	 *
	 * @param item
	 * @return The enclosure media file
	 */
	public abstract File loadItemEnclosureFile(Item item);


	public abstract Quota getQuota(OLATResourceable feed);

	/**
	 * Validates a feed url.
	 *
	 * @param url
	 * @return valid url (rss, atom etc.)
	 */
	public abstract ValidatedURL validateFeedUrl(String url, String type);

	/**
	 * Releases a lock
	 *
	 * @param lock The lock to be released
	 */
	public abstract void releaseLock(LockResult lock);

	/**
	 * Acquires the lock on the specified feed
	 *
	 * @param feed The feed to be locked
	 * @param identity The person who is locking the resource
	 * @return The lock result
	 */
	public abstract LockResult acquireLock(OLATResourceable feed, Identity identity);

	/**
	 * Acquires the lock of an item
	 *
	 * @param feed The item's feed
	 * @param item The item to be locked
	 * @param identity The person who is locking the resource
	 * @return The lock result
	 */
	public abstract LockResult acquireLock(OLATResourceable feed, Item item, Identity identity);

	/**
	 * @param feed
	 * @return True if the feed is locked
	 */
	public boolean isLocked(OLATResourceable feed) {
		return CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(feed, null);
	}

	/**
	 * There are different kinds of web feeds, e.g. podcasts, blogs etc. This
	 * method returns the kind of a resourceType. In contrast to the resource type
	 * name, the kind is a single noun designating the feed. It might be used to
	 * get a comprehensible expression for folder or file names.
	 *
	 * @param ores
	 * @return The kind of the resource type
	 */
	public abstract String getFeedKind(OLATResourceable ores);

	/**
	 * Replace the image of the feed.
	 * If the image is null, the existing image is kept.
	 *
	 * @param feed
	 * @param image
	 * @return
	 */
	public abstract Feed replaceFeedImage(Feed feed, FileElement image);

	/**
	 * Delete the feed image.
	 *
	 * @param feed
	 * @return
	 */
	public abstract Feed deleteFeedImage(Feed feed);

}
