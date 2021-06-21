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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.image.ImageService;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSItemMetaFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.webFeed.Enclosure;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.model.EnclosureImpl;
import org.olat.modules.webFeed.model.FeedImpl;
import org.olat.modules.webFeed.model.ItemImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;

/**
 * This class helps to store data like images and videos in the file systems
 * and handles the storage of Feeds and Items as XML as well.
 *
 * The structure of the files of a feed is:
 * resource
 *   feed
 *   __feed.xml
 *   __/items
 *   ____/item
 *   ______item.xml
 *   ______/media
 *   ________image.jpg
 *   ____/item
 *   ______...
 *
 * Initial date: 22.05.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class FeedFileStorge {

	private static final Logger log = Tracing.createLoggerFor(FeedFileStorge.class);

	private static final String MEDIA_DIR = "media";
	private static final String ITEMS_DIR = "items";
	public static final String FEED_FILE_NAME = "feed.xml";
	public static final String ITEM_FILE_NAME = "item.xml";

	// same as in repository metadata image upload
	private static final int PICTUREWIDTH = 570;

	private FileResourceManager fileResourceManager;
	private final XStream xstream;

	@Autowired
	private ImageService imageHelper;

	public FeedFileStorge() {
		fileResourceManager = FileResourceManager.getInstance();
		xstream = XStreamHelper.createXStreamInstance();
		XStreamHelper.allowDefaultPackage(xstream);
		xstream.alias("feed", FeedImpl.class);
		xstream.aliasField("type", FeedImpl.class, "resourceableType");
		xstream.omitField(FeedImpl.class, "id");
		xstream.omitField(FeedImpl.class, "itemIds");
		xstream.omitField(FeedImpl.class, "key");
		xstream.omitField(FeedImpl.class, "wrappers");
		xstream.alias("item", ItemImpl.class);
		xstream.omitField(ItemImpl.class, "key");
		xstream.omitField(ItemImpl.class, "feed");
		xstream.alias("enclosure", Enclosure.class, EnclosureImpl.class);
		xstream.ignoreUnknownElements();
	}

	/**
	 * Get the resource (root) container of the feed.
	 *
	 * @param ores
	 * @return
	 */
	public LocalFolderImpl getResourceContainer(OLATResourceable ores) {
		return fileResourceManager.getFileResourceRootImpl(ores);
	}
	
	public VFSContainer getOrCreateResourceMediaContainer(OLATResourceable ores) {
		VFSContainer mediaContainer = null;

		if (ores != null) {
			VFSContainer resourceDir = getResourceContainer(ores);
			mediaContainer = (VFSContainer) resourceDir.resolve(MEDIA_DIR);
			if (mediaContainer == null) {
				mediaContainer = resourceDir.createChildContainer(MEDIA_DIR);
			}
		}

		return mediaContainer;
	}

	/**
	 * Get the top most folder of a feed.
	 * The container is created if it does not exist.
	 *
	 * @param ores
	 * @return the container or null
	 */
	public VFSContainer getOrCreateFeedContainer(OLATResourceable ores) {
		VFSContainer feedContainer = null;

		if (ores != null) {
			VFSContainer resourceDir = getResourceContainer(ores);
			String feedContainerName = FeedManager.getInstance().getFeedKind(ores);
			feedContainer = (VFSContainer) resourceDir.resolve(feedContainerName);
			if (feedContainer == null) {
				feedContainer = resourceDir.createChildContainer(feedContainerName);
			}
		}

		return feedContainer;
	}

	/**
	 * Get the media container of a feed.
	 * The container is created if it does not exist.
	 *
	 * @param ores
	 * @return the container or null
	 */
	public VFSContainer getOrCreateFeedMediaContainer(OLATResourceable ores) {
		VFSContainer mediaContainer = null;

		if (ores != null) {
			VFSContainer feedContainer = getOrCreateFeedContainer(ores);
			mediaContainer = (VFSContainer) feedContainer.resolve(MEDIA_DIR);
			if (mediaContainer == null) {
				mediaContainer = feedContainer.createChildContainer(MEDIA_DIR);
			}
		}

		return mediaContainer;
	}

	/**
	 * Get the items container of a feed.
	 * The container is created if it does not exist.
	 *
	 * @param ores
	 * @return the container or null
	 */
	public VFSContainer getOrCreateFeedItemsContainer(OLATResourceable ores) {
		VFSContainer itemsContainer = null;

		if (ores != null) {
			VFSContainer feedContainer = getOrCreateFeedContainer(ores);
			itemsContainer = (VFSContainer) feedContainer.resolve(ITEMS_DIR);
			if (itemsContainer == null) {
				itemsContainer = feedContainer.createChildContainer(ITEMS_DIR);
			}
		}

		return itemsContainer;
	}

	/**
	 * Get the container of an item.
	 * The container is created if it does not exist.
	 *
	 * @param ores
	 * @return the container or null
	 */
	public VFSContainer getOrCreateItemContainer(Item item) {
		VFSContainer itemContainer = null;

		if (item != null) {
			Feed feed = item.getFeed();
			String guid = item.getGuid();
			itemContainer = getOrCreateItemContainer(feed, guid);
		}

		return itemContainer;
	}

	/**
	 * Delete the container of the item.
	 *
	 * @param item
	 */
	public void deleteItemContainer(Item item) {
		VFSContainer itemContainer = getOrCreateItemContainer(item);
		if (itemContainer != null) {
			itemContainer.delete();
		}
	}


	/**
	 * Get the container for the guid of an item.
	 * The container is created if it does not exist.

	 * @param feed
	 * @param guid
	 * @return
	 */
	public VFSContainer getOrCreateItemContainer(Feed feed, String guid) {
		VFSContainer itemContainer = null;

		if (feed != null && StringHelper.containsNonWhitespace(guid)) {
			VFSContainer feedContainer = getOrCreateFeedItemsContainer(feed);
			itemContainer = (VFSContainer) feedContainer.resolve(guid);
			if (itemContainer == null) {
				itemContainer = feedContainer.createChildContainer(guid);
			}
		}

		return itemContainer;
	}

	/**
	 * Get the media container of an item.
	 * The container is created if it does not exist.
	 *
	 * @param ores
	 * @return the container or null
	 */
	public VFSContainer getOrCreateItemMediaContainer(Item item) {
		VFSContainer mediaContainer = null;

		if (item != null) {
			VFSContainer itemContainer = getOrCreateItemContainer(item);
			if (itemContainer != null) {
				mediaContainer = (VFSContainer) itemContainer.resolve(MEDIA_DIR);
				if (mediaContainer == null) {
					mediaContainer = itemContainer.createChildContainer(MEDIA_DIR);
				}
			}
		}

		return mediaContainer;
	}

	/**
	 * Save the feed as XML into the feed container.
	 *
	 * @param feed
	 */
	public void saveFeedAsXML(Feed feed) {
		VFSContainer feedContainer = getOrCreateFeedContainer(feed);
		if (feedContainer != null) {
			VFSLeaf leaf = (VFSLeaf) feedContainer.resolve(FEED_FILE_NAME);
			if (leaf == null) {
				leaf = feedContainer.createChildLeaf(FEED_FILE_NAME);
			}
			XStreamHelper.writeObject(xstream, leaf, feed);
		}
	}

	/**
	 * Load the XML file of the feed from the feed container and convert it to
	 * a feed.
	 *
	 * @param ores
	 * @return the feed or null
	 */
	public Feed loadFeedFromXML(OLATResourceable ores) {
		Feed feed = null;

		VFSContainer feedContainer = getOrCreateFeedContainer(ores);
		if (feedContainer != null) {
			VFSLeaf leaf = (VFSLeaf) feedContainer.resolve(FEED_FILE_NAME);
			if (leaf != null) {
				feed = (FeedImpl) XStreamHelper.readObject(xstream, leaf);
				shorteningFeedToLengthOfDbAttribues(feed);
			}
		} else {
			log.warn("Feed XML-File could not be found on file system. Feed container: " + feedContainer);
		}

		return feed;
	}

	private void shorteningFeedToLengthOfDbAttribues(Feed feed) {
		if (feed.getAuthor() != null && feed.getAuthor().length() > 255) {
			feed.setAuthor(feed.getAuthor().substring(0, 255));
		}
		if (feed.getTitle() != null && feed.getTitle().length() > 1024) {
			feed.setTitle(feed.getTitle().substring(0, 1024));
		}
		if (feed.getDescription() != null && feed.getDescription().length() > 4000) {
			feed.setDescription(feed.getDescription().substring(0, 4000));
		}
		if (feed.getImageName() != null && feed.getImageName().length() > 1024) {
			feed.setImageName(null);
		}
		if (feed.getExternalFeedUrl() != null && feed.getExternalFeedUrl().length() > 4000) {
			feed.setExternalFeedUrl(null);
		}
		if (feed.getExternalImageURL() != null && feed.getExternalImageURL().length() > 4000) {
			feed.setExternalImageURL(null);
		}
	}

	/**
	 * Load the XML file of the feed from a Path and convert it to
	 * a feed.
	 *
	 * @param feedDir the directory which contains the feed file
	 * @return the feed or null
	 */
	public Feed loadFeedFromXML(Path feedDir) {
		Feed feed = null;

		if (feedDir != null) {
			Path feedPath = feedDir.resolve(FeedFileStorge.FEED_FILE_NAME);
			try (InputStream in = Files.newInputStream(feedPath);
					BufferedInputStream bis = new BufferedInputStream(in, FileUtils.BSIZE)) {
				feed = (FeedImpl) XStreamHelper.readObject(xstream, bis);
			} catch (IOException e) {
				log.warn("Feed XML-File could not be found on file system. Feed path: " + feedPath, e);
			}
		}

		return feed;
	}

	/**
	 * Delete the XML file of the feed from the feed container
	 *
	 * @param feed
	 */
	public void deleteFeedXML(Feed feed) {
		VFSContainer feedContainer = getOrCreateFeedContainer(feed);
		if (feedContainer != null) {
			VFSLeaf leaf = (VFSLeaf) feedContainer.resolve(FEED_FILE_NAME);
			if (leaf != null) {
				leaf.delete();
			}
		}
	}

	/**
	 * Save the item as XML into the item container.
	 *
	 * @param item
	 */
	public void saveItemAsXML(Item item) {
		VFSContainer itemContainer = getOrCreateItemContainer(item);
		if (itemContainer != null) {
			VFSLeaf leaf = (VFSLeaf) itemContainer.resolve(ITEM_FILE_NAME);
			if (leaf == null) {
				leaf = itemContainer.createChildLeaf(ITEM_FILE_NAME);
			}
			XStreamHelper.writeObject(xstream, leaf, item);
		}
	}

	/**
	 * Load the XML file of the item from the item container and convert it to
	 * an item.
	 *
	 * @param feed
	 * @param guid
	 * @return
	 */
	Item loadItemFromXML(VFSContainer itemContainer) {
		Item item = null;

		if (itemContainer != null) {
			VFSLeaf leaf = (VFSLeaf) itemContainer.resolve(ITEM_FILE_NAME);
			if (leaf != null) {
				try {
					item = (ItemImpl) XStreamHelper.readObject(xstream, leaf);
				} catch (Exception e) {
					log.warn("Item XML-File could not be read. Item container: " + leaf);
				}
			}
		}

		return item;
	}

	/**
	 * Load the XML file of all items of a feed and convert them to items.
	 *
	 * @param ores
	 * @return
	 */
	public List<Item> loadItemsFromXML(OLATResourceable ores) {
		List<Item> items = new ArrayList<>();

		VFSContainer itemsContainer = getOrCreateFeedItemsContainer(ores);
		if (itemsContainer != null) {
			List<VFSItem>  itemContainers = itemsContainer.getItems(new VFSItemMetaFilter());
			if (itemContainers != null && !itemContainers.isEmpty()) {
				for (VFSItem itemContainer : itemContainers) {
					Item item = loadItemFromXML((VFSContainer) itemContainer);
					if (item != null) {
						shorteningItemToLengthOfDbAttributes(item);
						items.add(item);
					}
				}
			}
		}

		return items;
	}

	private void shorteningItemToLengthOfDbAttributes(Item item) {
		if (item.getAuthor() != null && item.getAuthor().length() > 255) {
			item.setAuthor(item.getAuthor().substring(0, 255));
		}
		if (item.getExternalLink() != null && item.getExternalLink().length() > 4000) {
			item.setExternalLink(null);
		}
		if (item.getTitle() != null && item.getTitle().length() > 1024) {
			item.setTitle(item.getTitle().substring(0, 1024));
		}
	}

	/**
	 * Delete the XML file of the item from the item container
	 *
	 * @param item
	 */
	public void deleteItemXML(Item item) {
		VFSContainer itemContainer = getOrCreateItemContainer(item);
		if (itemContainer != null) {
			VFSLeaf leaf = (VFSLeaf) itemContainer.resolve(ITEM_FILE_NAME);
			if (leaf != null) {
				leaf.delete();
			}
		}
	}

	/**
	 * Save the media element of the feed. If already a file is in the media
	 * container, that file is previously deleted. If the media is null, this
	 * method will do nothing. It does not delete the existing media.
	 *
	 * @param feed
	 * @param media
	 * @return the file name which is save for the file system
	 */
	public String saveFeedMedia(Feed feed, FileElement media) {
		String saveFileName = null;

		if (media != null) {
			VFSContainer feedMediaContainer = getOrCreateFeedMediaContainer(feed);
			if (feedMediaContainer != null) {
				deleteFeedMedia(feed);
				VFSLeaf imageLeaf = media.moveUploadFileTo(feedMediaContainer);
				// Resize to same dimension box as with repo meta image
				VFSLeaf tmpImage = feedMediaContainer.createChildLeaf(Long.toString(CodeHelper.getRAMUniqueID()));
				imageHelper.scaleImage(imageLeaf, tmpImage, PICTUREWIDTH, PICTUREWIDTH, false);
				imageLeaf.delete();
				imageLeaf = tmpImage;
				// Make file system save
				saveFileName = Formatter.makeStringFilesystemSave(media.getUploadFileName());
				imageLeaf.rename(saveFileName);
			}
		}

		return saveFileName;
	}

	/**
	 * Save a file as the media element of the feed. If already a file is in
	 * the media container, that file is previously deleted.
	 *
	 * @param feed
	 * @param media
	 * @param changedBy 
	 * @return the file name which is save for the file system
	 */
	public String saveFeedMedia(Feed feed, VFSLeaf media, Identity changedBy) {
		String saveFileName = null;

		VFSContainer feedMediaContainer = getOrCreateFeedMediaContainer(feed);
		if (feedMediaContainer != null) {
			deleteFeedMedia(feed);
			if (media != null) {
				VFSManager.copyContent(media, feedMediaContainer.createChildLeaf(media.getName()), true, changedBy);
				saveFileName = media.getName();
			}
		}

		return saveFileName;
	}

	/**
	 * Load the the media element of the feed.
	 *
	 * @param feed
	 * @return the media alement or null
	 */
	public VFSLeaf loadFeedMedia(Feed feed) {
		VFSLeaf mediaFile = null;

		if (feed != null) {
			String feedImage = feed.getImageName();
			if (feedImage != null) {
				mediaFile = (VFSLeaf) getOrCreateFeedMediaContainer(feed).resolve(feedImage);
			}
		}

		return mediaFile;
	}

	/**
	 * Delete the the media of the feed.
	 *
	 * @param feed
	 */
	public void deleteFeedMedia(Feed feed) {
		VFSContainer feedMediaContainer = getOrCreateFeedMediaContainer(feed);
		if (feedMediaContainer != null) {
			for (VFSItem fileItem : feedMediaContainer.getItems(new VFSSystemItemFilter())) {
				fileItem.delete();
			}
		}
	}

	/**
	 * Save a file (video/audio/image) to the media container of the item.
	 * <p>
	 * If the media is null, this method will do nothing. It does not delete the
	 * existing media files.
	 *
	 * @param item
	 * @param media
	 * @return the file name which is save for the file system
	 */
	public String saveItemMedia(Item item, FileElement media) {
		String saveFileName = null;

		if (media != null) {
			VFSContainer itemMediaContainer = getOrCreateItemMediaContainer(item);
			if (itemMediaContainer != null) {
				media.moveUploadFileTo(itemMediaContainer);
				saveFileName = media.getUploadFileName();
			}
		}

		return saveFileName;
	}

	/**
	 * Load the media file of the item.
	 *
	 * @param item
	 * @return
	 */
	public File loadItemMedia(Item item) {
		File file = null;

		Enclosure enclosure = item.getEnclosure();
		VFSContainer mediaDir = getOrCreateItemMediaContainer(item);
		if (mediaDir != null && enclosure != null) {
			VFSLeaf mediaFile = (VFSLeaf) mediaDir.resolve(enclosure.getFileName());
			if (mediaFile instanceof LocalFileImpl) {
				file = ((LocalFileImpl) mediaFile).getBasefile();
			}
		}

		return file;
	}

	/**
	 * Delete a file from the media container of an item.
	 *
	 * @param item
	 * @param fileName
	 */
	public void deleteItemMedia(Item item, String fileName) {
		if (fileName != null) {
			VFSContainer itemContainer = getOrCreateItemMediaContainer(item);
			if (itemContainer != null) {
				VFSLeaf leaf = (VFSLeaf) itemContainer.resolve(fileName);
				if (leaf != null) {
					leaf.delete();
				}
			}
		}
	}

}
