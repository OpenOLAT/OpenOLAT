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
package org.olat.modules.webFeed.managers;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.fileresource.types.PodcastFileResource;
import org.olat.modules.webFeed.models.Feed;
import org.olat.modules.webFeed.models.Item;
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
public abstract class FeedManager extends BasicManager {

	protected static FeedManager INSTANCE;

	public static final String ITEMS_DIR = "items";
	public static final String FEED_FILE_NAME = "feed.xml";
	public static final String ITEM_FILE_NAME = "item.xml";
	public static final String MEDIA_DIR = "media";
	public static final String RSS_FEED_NAME = "feed.rss";
	public static final String RESOURCE_NAME = "feed";

	// Definition of different kinds of feeds. By convention, the kind is a single
	// noun designating the feed. (See also getFeedKind().)
	public static final String KIND_PODCAST = "podcast";
	public static final String KIND_BLOG = "blog";

	// public static final String KIND_PHOTOBLOG = "photoblog";
	// public static final String KIND_SCREENCAST = "screencast";

	/**
	 * Use this method instead of any constructor to get the singelton object.
	 * 
	 * @return INSTANCE
	 */
	public static final FeedManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Creates an OLAT podcast resource
	 * 
	 * @return The resource
	 */
	public abstract OLATResourceable createPodcastResource();

	/**
	 * Creates an OLAT blog resource
	 * 
	 * @return The resource
	 */
	public abstract OLATResourceable createBlogResource();

	/**
	 * Deletes a given feed.
	 * 
	 * @param feed
	 */
	public abstract void delete(OLATResourceable feed);

	/**
	 * Copies a given feed resourceable
	 * 
	 * @param feed
	 */
	public abstract boolean copy(OLATResource source, OLATResource target);

	/**
	 * Adds the given <code>Item</code> to the <code>Feed</code>.
	 * 
	 * @param item
	 * @param feed
	 */
	public abstract Feed addItem(Item item, FileElement file, Feed feed);

	/**
	 * Removes the given <code>Item</code> from the <code>Feed</code>. Its content
	 * will be deleted.
	 * 
	 * @param item
	 * @param feed
	 */
	public abstract Feed remove(Item item, Feed feed);

	/**
	 * @param modifiedItem
	 * @param feed
	 */
	public abstract Feed updateItem(Item modifiedItem, FileElement file, Feed feed);

	/**
	 * Update the feed source mode
	 * 
	 * @param external True: set to be an external feed; false: this is an
	 *          internal feed; null=undefined
	 * @param feed
	 * @return Feed the updated feed object
	 */
	public abstract Feed updateFeedMode(Boolean external, Feed feed);

	/**
	 * Update the feed metadata from the given feed object
	 * 
	 * @param feed
	 * @return
	 */
	public abstract Feed updateFeedMetadata(Feed feed);

	/**
	 * Load all items of the feed (from file system or the external feed)
	 * 
	 * @param feed
	 */
	public abstract List<Item> loadItems(final Feed feed);

	/**
	 * Get the item from the feed with the given GUID or NULL if no such item
	 * exists. Make sure you did load the feed before executing this!
	 * 
	 * @param feed
	 * @param GUID
	 * @return the Item or NULL
	 */
	public abstract Item getItem(Feed feed, String GUID);
	
	/**
	 * Returns the feed with the provided id or null if not found.
	 * 
	 * @param feed The feed to be re-read
	 * @return The newly read feed (without items)
	 */
	public abstract Feed getFeed(OLATResourceable feed);
	
	public abstract OlatRootFolderImpl getResourceContainer(OLATResourceable ores);

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
	public abstract String getFeedBaseUri(Feed feed, Identity identity, Long courseId, String nodeId);

	/**
	 * Creates the RSS feed resource.
	 * 
	 * @param feedId
	 * @param type The resource type name
	 * @param identityKey
	 * @return The RSS feed as a MediaResource
	 */
	public abstract MediaResource createFeedFile(OLATResourceable feed, Identity identity, Long courseId, String nodeId);

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
	 * @param feed
	 * @return The container of the item
	 */
	public abstract VFSContainer getItemContainer(Item item, Feed feed);

	/**
	 * Returns the media container of the item of feed
	 * 
	 * @param item
	 * @param feed
	 * @return The media container of the item
	 */
	public abstract VFSContainer getItemMediaContainer(Item item, Feed feed);

	/**
	 * Returns the File of the item's enclosure if it exists or null
	 * 
	 * @param item
	 * @param feed
	 * @return The enclosure media file
	 */
	public abstract File getItemEnclosureFile(Item item, Feed feed);

	/**
	 * Returns the container of the feed
	 * 
	 * @param feed
	 * @return The feed container
	 */
	public abstract VFSContainer getFeedContainer(OLATResourceable feed);
	
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
	public String getFeedKind(OLATResourceable ores) {
		String feedKind = null;
		String typeName = ores.getResourceableTypeName();
		if (PodcastFileResource.TYPE_NAME.equals(typeName)) {
			feedKind = KIND_PODCAST;
		} else if (BlogFileResource.TYPE_NAME.equals(typeName)) {
			feedKind = KIND_BLOG;
		} else if ("LiveBlog".equals(typeName)) {
			feedKind = KIND_BLOG;
		}
		return feedKind;
	}

	/**
	 * Set the image of the feed (update handled separately)
	 * 
	 * @param image
	 * @param feed
	 */
	public abstract void setImage(FileElement image, Feed feed);

	/**
	 * Delete the image of the feed
	 * 
	 * @param feed
	 */
	public abstract void deleteImage(Feed feed);

	/**
	 * Prepare the filesystem for a new item, create the item container and all
	 * necessary sub container, e.g. the media container
	 * 
	 * @param feed
	 * @param currentItem
	 * @return the container for the item
	 */
	public abstract VFSContainer createItemContainer(Feed feed, Item currentItem);

	public abstract Feed readFeedFile(VFSContainer root);
	
	public abstract Feed readFeedFile(Path feedPath);

	public abstract Item loadItem(VFSItem itemContainer);

}
