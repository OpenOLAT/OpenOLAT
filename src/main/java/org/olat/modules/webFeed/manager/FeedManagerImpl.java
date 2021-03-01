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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.logging.log4j.Logger;
import org.olat.admin.quota.QuotaConstants;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.media.CleanupAfterDeliveryFileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.fileresource.types.FeedFileResource;
import org.olat.fileresource.types.PodcastFileResource;
import org.olat.modules.webFeed.Enclosure;
import org.olat.modules.webFeed.ExternalFeedFetcher;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.RSSFeed;
import org.olat.modules.webFeed.SyndFeedMediaResource;
import org.olat.modules.webFeed.dispatching.FeedMediaDispatcher;
import org.olat.modules.webFeed.model.EnclosureImpl;
import org.olat.modules.webFeed.model.FeedImpl;
import org.olat.modules.webFeed.model.ItemPublishDateComparator;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.rometools.rome.feed.synd.SyndFeed;

/**
 * This is the actual feed manager implementation. It handles all operations on
 * the various feeds and items.
 *
 * <P>
 * Initial Date: Feb 17, 2009 <br>
 *
 * @author Gregor Wassmann
 */
public class FeedManagerImpl extends FeedManager {

	private static final Logger log = Tracing.createLoggerFor(FeedManagerImpl.class);

	// 10 minutes
	private static final int EXTERNAL_FEED_ACTUALIZATION_MILLIS = 10*60*1000;

	private RepositoryManager repositoryManager;
	private Coordinator coordinator;
	private OLATResourceManager resourceManager;
	private FileResourceManager fileResourceManager;

	@Autowired
	private DB dbInstance;
	@Autowired
	private FeedDAO feedDAO;
	@Autowired
	private ItemDAO itemDAO;
	@Autowired
	private QuotaManager quotaManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private FeedFileStorge feedFileStorage;
	@Autowired
	private ExternalFeedFetcher externalFeedFetcher;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	/**
	 * spring only
	 */
	protected FeedManagerImpl(OLATResourceManager resourceManager, FileResourceManager fileResourceManager,
			CoordinatorManager coordinatorManager) {

		this.resourceManager = resourceManager;
		this.fileResourceManager = fileResourceManager;
		INSTANCE = this;
		this.coordinator = coordinatorManager.getCoordinator();
	}

	FeedManagerImpl() {

	}

	public void setRepositoryManager(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
	}

	@Override
	public OLATResourceable createPodcastResource() {
		FeedFileResource podcastResource = new PodcastFileResource();
		return createFeedResource(podcastResource);
	}

	@Override
	public OLATResourceable createBlogResource() {
		FeedFileResource blogResource = new BlogFileResource();
		return createFeedResource(blogResource);
	}

	/**
	 * This method creates an OLATResource in the database and
	 * initializes the container on the file system.
	 *
	 * @param feedResource
	 * @return The feed resourcable
	 */
	private OLATResourceable createFeedResource(FeedFileResource feedResource) {
		// save the resource in the database
		OLATResource ores = resourceManager.createOLATResourceInstance(feedResource);
		resourceManager.saveOLATResource(ores);

		// create a feed and save it in the database
		feedDAO.createFeedForResourcable(feedResource);

		// Create a resource folder for storing the images
		feedFileStorage.getOrCreateFeedContainer(feedResource);

		return feedResource;
	}

	/**
	 * Load the Feed from the database.
	 *
	 * Additionally this method triggers the actualization of the external feed
	 * and his items. The download starts is the last modified time is
	 */
	@Override
	public Feed loadFeed(OLATResourceable ores) {
		Feed feed = feedDAO.loadFeed(ores);

		// Update the external feed and the items
		if (feed != null && feed.isExternal() && StringHelper.containsNonWhitespace(feed.getExternalFeedUrl())) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(feed.getLastModified());
			long lastModifiedMillis = cal.getTimeInMillis();
			Date nextUpdateDate = new Date(lastModifiedMillis + EXTERNAL_FEED_ACTUALIZATION_MILLIS);
			Date now = new Date();
			if (now.after(nextUpdateDate) || loadItems(feed).isEmpty()) {
				// time to update or first load after creation of the feed
				saveExternalFeedIAndtems(feed);
			}

			feed.setLastModified(new Date());
			feedDAO.updateFeed(feed);
		}

		return feed;
	}

	private void saveExternalFeedIAndtems(Feed feed) {
		saveExternalItems(feed);
		saveExternalFeed(feed);
		notificationsManager.markPublisherNews(feed.getResourceableTypeName(),
				feed.getResourceableId().toString(), null, false);
	}

	@Override
	public boolean hasItems(Feed feed) {
		return itemDAO.hasItems(feed);
	}

	@Override
	public Feed updateFeed(Feed feed) {
		return feedDAO.updateFeed(feed);
	}

	@Override
	public Feed updateFeedMode(Boolean external, Feed feed) {
		if (feed == null) return null;

		// first, reload actual version of the feed
		Feed reloaded = feedDAO.loadFeed(feed);
		if (reloaded == null) return null;

		// delete all items if the mode changes
		if (external == null
				|| feed.isUndefined()
				|| external.booleanValue() != feed.getExternal().booleanValue()) {
			itemDAO.removeItems(feed);
			reloaded.setExternalImageURL(null);
		}

		reloaded.setExternal(external);
		return updateFeed(reloaded);
	}

	@Override
	public Feed updateExternalFeedUrl(Feed feed, String externalFeedUrl) {
		Feed reloaded = feedDAO.loadFeed(feed);
		if (reloaded == null) return null;
		if (!feed.isExternal()) return feed;

		if (!StringHelper.isSame(reloaded.getExternalFeedUrl(), externalFeedUrl)) {
			itemDAO.removeItems(feed);
		}
		if (StringHelper.containsNonWhitespace(externalFeedUrl)) {
			reloaded.setExternalFeedUrl(externalFeedUrl);
			saveExternalFeedIAndtems(feed);
		} else {
			reloaded.setExternal(null);
			reloaded.setExternalFeedUrl(null);
		}
		reloaded.setLastModified(new Date());
		return feedDAO.updateFeed(reloaded);
	}

	@Override
	public Feed replaceFeedImage(Feed feed, FileElement image) {
		String saveFileName = null;

		if (image != null) {
			saveFileName = feedFileStorage.saveFeedMedia(feed, image);
			feed = feedDAO.loadFeed(feed.getKey());
			if (feed != null) {
				feed.setImageName(saveFileName);
				feed = feedDAO.updateFeed(feed);
			}
		}

		return feed;
	}

	@Override
	public Feed deleteFeedImage(Feed feed) {
		String saveFileName = null;

		feedFileStorage.deleteFeedMedia(feed);
		Feed reloadedFeed = feedDAO.loadFeed(feed.getKey());
		if (reloadedFeed != null) {
			reloadedFeed.setImageName(saveFileName);
			reloadedFeed = feedDAO.updateFeed(reloadedFeed);
		}

		return reloadedFeed;
	}

	@Override
	public void deleteFeed(OLATResourceable ores) {
		// delete the container on the file system
		fileResourceManager.deleteFileResource(ores);

		// delete comments and ratings
		CommentAndRatingService commentAndRatingService = CoreSpringFactory.getImpl(CommentAndRatingService.class);
		commentAndRatingService.deleteAllIgnoringSubPath(ores);

		// delete the feed and all items from the database
		Feed feed = feedDAO.loadFeed(ores);
		itemDAO.removeItems(feed);
		feedDAO.removeFeedForResourceable(ores);
	}

	@Override
	public Feed createItem(Feed feed, Item item, FileElement file) {
		if (feed == null || item == null || !feed.isInternal()) return null;

		// Set the current date as published date.
		if (item.getPublishDate() == null) {
			item.setPublishDate(new Date());
		}

		// Save the Enclosure
		Enclosure enclosure = replaceEnclosure(item, file);
		item.setEnclosure(enclosure);

		// Save the Item
		itemDAO.createItem(feed, item);

		// Set the modification date of the feed
		Feed reloadedFeed = feedDAO.loadFeed(feed.getKey());
		reloadedFeed.setLastModified(new Date());
		reloadedFeed = feedDAO.updateFeed(reloadedFeed);

		markPublisherNews(reloadedFeed);

		return reloadedFeed;
	}


	@Override
	public Item loadItem(Long key) {
		return itemDAO.loadItem(key);
	}

	@Override
	public Item loadItemByGuid(String guid) {
		return itemDAO.loadItemByGuid(guid);
	}

	@Override
	public List<Item> loadItems(Feed feed) {
		return itemDAO.loadItems(feed, null);
	}

	@Override
	public List<String> loadItemsGuid(Feed feed) {
		return itemDAO.loadItemsGuid(feed);
	}

	@Override
	public List<Item> loadPublishedItems(Feed feed) {
		return itemDAO.loadPublishedItems(feed);
	}

	@Override
	public List<Item> loadFilteredAndSortedItems(Feed feed, List<Long> filteredItemIds, FeedSecurityCallback callback, Identity identity) {
		List<Item> items = itemDAO.loadItems(feed, filteredItemIds);
		List<Item> filteredItems = new ArrayList<>();
		if (feed.isExternal()) {
			// show all items
			filteredItems = items;
		} else {
			for (Item item : items) {
				if (item.isPublished()) {
					// everybody can see published items
					filteredItems.add(item);
				} else if (item.isScheduled() && callback.mayEditItems()) {
					// scheduled items can be seen by everybody who can edit items
					// (moderators)
					filteredItems.add(item);
				} else if (identity.getKey().equals(item.getAuthorKey())) {
					// scheduled items and drafts of oneself are shown
					filteredItems.add(item);
				} else if (item.isDraft()) {
					if(callback.mayViewAllDrafts() || identity.getKey().equals(item.getModifierKey())) {
						filteredItems.add(item);
					}
				}
			}
		}
		Collections.sort(filteredItems, new ItemPublishDateComparator());
		return filteredItems;
	}

	@Override
	public Item updateItem(Item item, FileElement file) {
		if (item == null) return null;

		Item updatedItem = itemDAO.loadItem(item.getKey());

		if (updatedItem != null) {
			Enclosure enclosure = replaceEnclosure(item, file);
			item.setEnclosure(enclosure);
			updatedItem = itemDAO.updateItem(item);
			markPublisherNews(updatedItem.getFeed());
		}

		return updatedItem;
	}

	/**
	 * Save or delete the media file to the file system and get the appropriate
	 * Enclosure.
	 *
	 * @param item
	 * @param file
	 * @return
	 */
	private Enclosure replaceEnclosure(Item item, FileElement file) {
		Enclosure enclosure = item.getEnclosure();

		if (file != null) {
			if (file.isUploadSuccess()) {
				if (enclosure != null && enclosure.getFileName() != null) {
					feedFileStorage.deleteItemMedia(item, enclosure.getFileName());
				}
				// New uploaded file
				enclosure = new EnclosureImpl();
				enclosure.setLength(file.getUploadSize());
				enclosure.setType(file.getUploadMimeType());
				String saveFileName = feedFileStorage.saveItemMedia(item, file);
				enclosure.setFileName(saveFileName);
			} else if (file.getInitialFile() == null) {
				// If no or deleted initial file, delete the media file
				if (enclosure != null && enclosure.getFileName() != null) {
					feedFileStorage.deleteItemMedia(item, enclosure.getFileName());
				}
				enclosure = null;
			}
		}

		return enclosure;
	}


	@Override
	public Feed deleteItem(Item item) {
		Feed feed = item.getFeed();

		// delete the item from the database
		itemDAO.removeItem(item);

		// delete the item container from the file system
		feedFileStorage.deleteItemContainer(item);

		// delete comments and ratings
		CommentAndRatingService commentAndRatingService = CoreSpringFactory
				.getImpl(CommentAndRatingService.class);
		commentAndRatingService.deleteAll(feed, item.getGuid());

		// reload the Feed
		Feed reloadedFeed = feedDAO.loadFeed(feed.getKey());

		// If the last item has been removed, set the feed to undefined.
		// The user can then newly decide whether to add items manually
		// or from an external source.
		if (!hasItems(reloadedFeed)) {
			// set undefined
			reloadedFeed.setExternal(null);
		}
		reloadedFeed.setLastModified(new Date());
		reloadedFeed = feedDAO.updateFeed(reloadedFeed);

		return reloadedFeed;
	}

	/**
	 * Fetch the external feed and store it in the database.
	 *
	 * @param feed
	 */
	private void saveExternalFeed(Feed feed) {
		feed = externalFeedFetcher.fetchFeed(feed);

		if (feed != null) {
			feed.setLastModified(new Date());
			feedDAO.updateFeed(feed);
		}
	}

	/**
	 * Fetch all items of the external feed and store them in the database.
	 *
	 * @param feed
	 */
	private void saveExternalItems(Feed feed) {
		List<Item> externalItems = externalFeedFetcher.fetchItems(feed);

		for (Item externalItem : externalItems) {
			Item reloaded = itemDAO.loadItemByGuid(feed.getKey(), externalItem.getGuid());
			if (reloaded == null) {
				Date now = new Date();
				externalItem.setCreationDate(now);
				// Init the last modified
				if (externalItem.getLastModified() == null) {
					externalItem.setLastModified(now);
				}
				// published date should never be null because it triggers notifications
				if (externalItem.getPublishDate() == null) {
					externalItem.setPublishDate(now);
				}
				
				if(dbInstance.isMySQL()) {
					mysqlCleanUp(externalItem);
				}
				itemDAO.createItem(feed, externalItem);
			} else {
				// Do not overwrite initial values
				if (externalItem.getLastModified() != null) {
					reloaded.setLastModified(externalItem.getLastModified());
				}
				if (externalItem.getPublishDate() != null) {
					reloaded.setPublishDate(externalItem.getPublishDate());
				}
				reloaded.setAuthor(externalItem.getAuthor());
				reloaded.setExternalLink(externalItem.getExternalLink());
				reloaded.setTitle(externalItem.getTitle());
				reloaded.setDescription(externalItem.getDescription());
				reloaded.setContent(externalItem.getContent());
				reloaded.setEnclosure(externalItem.getEnclosure());
				
				if(dbInstance.isMySQL()) {
					mysqlCleanUp(reloaded);
				}
				itemDAO.updateItem(reloaded);
			}
		}
	}
	
	private void mysqlCleanUp(Item item) {
		item.setTitle(PersistenceHelper.convert(item.getTitle()));
		item.setContent(PersistenceHelper.convert(item.getContent()));
		item.setDescription(PersistenceHelper.convert(item.getDescription()));
	}

	private void markPublisherNews(Feed feed) {
		if (feed == null) return;

		notificationsManager.markPublisherNews(
				feed.getResourceableTypeName(),
				feed.getResourceableId().toString(),
				null,
				false);
	}

	@Override
	public Feed updateFeedWithRepositoryEntry(RepositoryEntry entry, Identity changedBy) {
		Feed feed = loadFeed(entry.getOlatResource());
		feed = enrichFeedByRepositoryEntry(feed, entry, changedBy);
		feed = updateFeed(feed);
		return feed;
	}

	@Override
	public Feed enrichFeedByRepositoryEntry(Feed feed, RepositoryEntry entry, Identity changedBy) {
		if (feed == null) return null;
		if (entry == null) return feed;

		// copy the metadata
		feed.setTitle(entry.getDisplayname());
		feed.setDescription(entry.getDescription());

		// Some old feeds have an author but it is not in the RespositoryEntry.
		// Keep the author of the feed in this case.
		if (entry.getAuthors() != null) {
			feed.setAuthor(entry.getAuthors());
		}

		// copy the image
		VFSLeaf image = repositoryManager.getImage(entry);
		String imageName = feedFileStorage.saveFeedMedia(feed, image, changedBy);
		feed.setImageName(imageName);

		return feed;
	}

	/**
	 * A unique key for the item of the feed. Can be used e.g. for locking and
	 * caching.
	 *
	 * @param string
	 * @param string2
	 * @return A unique key for the item of the feed
	 */
	private String itemKey(String string, String string2) {
		final StringBuilder key = new StringBuilder(128);
		key.append("feed").append(string2);
		key.append("_item_").append(string);
		return key.toString();
	}

	/**
	 * A unique key for the item of the feed. Can be used e.g. for locking and
	 * caching. (Protected for performance reasons)
	 *
	 * @param item
	 * @param feed
	 * @return A unique key for the item of the feed
	 */
	protected String itemKey(Item item, OLATResourceable feed) {
		return itemKey(item.getGuid(), feed.getResourceableId().toString());
	}

	@Override
	public VFSContainer getItemContainer(Item item) {
		return feedFileStorage.getOrCreateItemContainer(item);
	}

	@Override
	public void saveItemAsXML(Item item) {
		feedFileStorage.saveItemAsXML(item);
	}

	@Override
	public void deleteItemXML(Item item) {
		feedFileStorage.deleteItemXML(item);
	}

	@Override
	public File loadItemEnclosureFile(Item item) {
		return feedFileStorage.loadItemMedia(item);
	}

	@Override
	public MediaResource createItemMediaFile(OLATResourceable feed, String itemId, String fileName) {
		VFSMediaResource mediaResource = null;
		// Brute force method for fast delivery
		try {
			VFSItem item = feedFileStorage.getOrCreateFeedItemsContainer(feed);
			item = item.resolve(itemId);
			item = item.resolve(MEDIA_DIR);
			item = item.resolve(fileName);
			if (item instanceof VFSLeaf) {
				mediaResource = new VFSMediaResource((VFSLeaf) item);
			}
		} catch (NullPointerException e) {
			log.debug("Media resource could not be created from file: " + fileName);
		}
		return mediaResource;
	}

	@Override
	public VFSLeaf createFeedMediaFile(OLATResourceable feed, String fileName, Size thumbnailSize) {
		VFSLeaf mediaResource = null;
		// Brute force method for fast delivery
		try {
			VFSItem item =feedFileStorage.getOrCreateFeedMediaContainer(feed);
			item = item.resolve(fileName);
			if (item  == null) {
				item = feedFileStorage.getOrCreateResourceMediaContainer(feed);
				item = item.resolve(fileName);
			}
			if (thumbnailSize != null && thumbnailSize.getHeight() > 0 && thumbnailSize.getWidth() > 0
					&& item instanceof VFSLeaf && item.canMeta() == VFSConstants.YES) {
				item = vfsRepositoryService.getThumbnail((VFSLeaf)item, thumbnailSize.getWidth(), thumbnailSize.getHeight(), false);
			}
			if (item instanceof VFSLeaf) {
				mediaResource = (VFSLeaf) item;
			}
		} catch (NullPointerException e) {
			log.debug("Media resource could not be created from file: " + fileName);
		}
		return mediaResource;
	}

	/**
	 * Returns a podcast base URI of the type<br>
	 * http://myolat.org/olat/[podcast|blog]/[IDKEY/TOKEN]/ORESID
	 * 
	 * @param feed
	 * @param identityKey
	 * @return The feed base uri for the given user (identity)
	 */
	@Override
	public String getFeedBaseUri(Feed feed, Identity identity, Roles roles, Long courseId, String nodeId) {
		boolean isCourseNode = courseId != null && nodeId != null;

		final String slash = "/";
		StringBuilder uri = new StringBuilder(256);
		uri.append(Settings.getServerContextPathURI());
		uri.append(slash);
		uri.append(FeedMediaDispatcher.getURIPrefix(feed.getResourceableTypeName()));
		uri.append(slash);

		if (isCourseNode) {
			uri.append(org.olat.modules.webFeed.dispatching.Path.COURSE_NODE_INDICATOR);
			uri.append(slash);
		}

		if (identity != null) {
			// The identity can be null for guests
			String idKey = identity.getKey().toString();
			Authentication authentication = securityManager.findAuthenticationByAuthusername(idKey, FeedMediaDispatcher.TOKEN_PROVIDER, BaseSecurity.DEFAULT_ISSUER);
			if (authentication == null) {
				// Create an authentication
				String token = RandomStringUtils.randomAlphanumeric(6);
				authentication = securityManager.createAndPersistAuthentication(identity, FeedMediaDispatcher.TOKEN_PROVIDER, BaseSecurity.DEFAULT_ISSUER, idKey, token, null);
			}
			// If the repository entry allows guest access it is public, thus not
			// private.
			RepositoryEntry entry;
			if(courseId != null) {//check the course
				OLATResourceable courseOres = OresHelper.createOLATResourceableInstance("CourseModule", courseId);
				entry = repositoryManager.lookupRepositoryEntry(courseOres, false);
			} else {
				entry = repositoryManager.lookupRepositoryEntry(feed, false);
			}

			boolean isPrivate = true;
			if (entry != null && entry.isGuests()) {
				isPrivate = false;
			}
			if (isPrivate && !roles.isGuestOnly()) {
				// identity key
				uri.append(idKey);
				uri.append(slash);
				// token
				uri.append(authentication.getCredential());
				uri.append(slash);
			}
		}

		if (isCourseNode) {
			uri.append(courseId);
			uri.append(slash);
			uri.append(nodeId);
			uri.append(slash);
		}
		// feed id
		uri.append(feed.getResourceableId());
		// Append base uri delimiter. (Used to identify the root path for caching)
		uri.append("/_");
		return uri.toString();
	}

	@Override
	public MediaResource createFeedFile(OLATResourceable ores, Identity identity, Roles roles, Long courseId, String nodeId) {
		MediaResource media = null;
		Feed feed = loadFeed(ores);

		if (feed != null) {
			SyndFeed rssFeed = new RSSFeed(feed, identity, roles, courseId, nodeId);
			media = new SyndFeedMediaResource(rssFeed);
		}
		return media;
	}

	@Override
	public ValidatedURL validateFeedUrl(String url, String type) {
		boolean enclosuresExpected = type != null && type.indexOf("BLOG") >= 0? false: true;
		return externalFeedFetcher.validateFeedUrl(url, enclosuresExpected);
	}

	@Override
	public boolean copy(OLATResource sourceResource, OLATResource targetResource) {
		// copy the folders and files
		File sourceFileroot = fileResourceManager.getFileResourceRootImpl(sourceResource).getBasefile();
		File sourceFeedRoot = new File(sourceFileroot, getFeedKind(sourceResource));
		File targetFileroot = fileResourceManager.getFileResourceRootImpl(targetResource).getBasefile();
		FileUtils.copyFileToDir(sourceFeedRoot, targetFileroot, "add file resource");

		// load the feed and the items from the database
		Feed sourceFeed = feedDAO.loadFeed(sourceResource);
		List<Item> items = itemDAO.loadItems(sourceFeed, Collections.emptyList());

		// copy the feed in the database
		Feed targetFeed = feedDAO.copyFeed(sourceResource, targetResource);

		// copy the items in the database
		for (Item item : items) {
			itemDAO.copyItem(targetFeed, item);
		}

		return true;
	}

	@Override
	public MediaResource getFeedArchiveMediaResource(OLATResourceable resource) {
		VFSLeaf zip = getFeedArchive(resource);
		if(zip instanceof LocalFileImpl) {
			return new CleanupAfterDeliveryFileMediaResource(((LocalFileImpl) zip).getBasefile());
		}
		return new VFSMediaResource(zip);
	}

	@Override
	public VFSLeaf getFeedArchive(OLATResourceable resource) {
		VFSContainer rootContainer = feedFileStorage.getResourceContainer(resource);
		VFSContainer feedContainer = feedFileStorage.getOrCreateFeedContainer(resource);

		// Load the feed from database an store it to the XML file.
		Feed feed = feedDAO.loadFeed(resource);
		feed.setModelVersion(FeedImpl.CURRENT_MODEL_VERSION);
		feedFileStorage.saveFeedAsXML(feed);

		// Load the items from the database, make it export safe and store them
		// to XML files.
		List<Item> items = loadItems(feed);
		for (Item item : items) {
			if (feed.isInternal()) {
				// Because in internal feed only the author key is stored and this
				// key won't be the same in an other installation. We need a
				// fallback in this case.
				if (!item.isAuthorFallbackSet()) {
					String author = item.getAuthor();
					if (StringHelper.containsNonWhitespace(author)) {
						item.setAuthor(author);
					}
				}
			}
			feedFileStorage.saveItemAsXML(item);
		}

		// synchronize all zip processes for this feed
		// o_clusterOK by:fg
		VFSLeaf zip = coordinator.getSyncer().doInSync(resource, () -> {
			// Delete the old archive and recreate it from scratch
			String zipFileName = getFeedKind(resource) + ".zip";
			VFSItem oldArchive = rootContainer.resolve(zipFileName);
			if (oldArchive != null) {
				oldArchive.delete();
			}
			ZipUtil.zip(feedContainer.getItems(), rootContainer.createChildLeaf(zipFileName), new VFSSystemItemFilter(), false);
			return (VFSLeaf) rootContainer.resolve(zipFileName);
		});

		// delete the XML files again. They are only needed for the export.
		for (Item item : items) {
			feedFileStorage.deleteItemXML(item);
		}
		feedFileStorage.deleteFeedXML(feed);

		return zip;
	}

	@Override
	public Feed loadFeedFromXML(Path feedDir) {
		return feedFileStorage.loadFeedFromXML(feedDir);
	}

	@Override
	public void importFeedFromXML(OLATResource ores, boolean removeIdentityKeys) {
		Feed feedFromXml = feedFileStorage.loadFeedFromXML(ores);

		// Check if the feed already exits or create it. The feed exists
		// possibly, if a previous migration from an XML feed was not
		// successful.
		Feed feed = feedDAO.loadFeed(ores);
		if (feed == null && feedFromXml != null) {
			feedFromXml.setResourceableId(ores.getResourceableId());
			// Use the display name instead of the username
			if (!removeIdentityKeys && feedFromXml.getAuthor() != null) {
				String authorName = UserManager.getInstance().getUserDisplayName(feedFromXml.getAuthor());
				if (authorName != null) {
					feedFromXml.setAuthor(authorName);
				}
			}
			feed = feedDAO.createFeed(feedFromXml);
			log.info("Feed imported " + "(" + ores.getResourceableTypeName() + "): " + ores.getResourceableId());
		}

		if (feed != null) {
			List<Item> itemsFromXml = feedFileStorage.loadItemsFromXML(ores);
			
			// clean up for MySQL
			if(dbInstance.isMySQL()) {
				for (Item itemFromXml : itemsFromXml) {
					mysqlCleanUp(itemFromXml);
				}
			}
			
			itemsFromXml = fixFeedVersionIssues(feedFromXml, itemsFromXml);
			for (Item itemFromXml : itemsFromXml) {
				// Check if the item already exits or create it.
				Item item = itemDAO.loadItemByGuid(feed.getKey(), itemFromXml.getGuid());
				if (item == null) {
					if (removeIdentityKeys) {
						itemFromXml.setAuthorKey(null);
						itemFromXml.setModifierKey(null);
					} else {
						// Check if the identity exists
						if (itemFromXml.getAuthorKey() != null
								&& securityManager.loadIdentityShortByKey(itemFromXml.getAuthorKey()) == null) {
							itemFromXml.setAuthorKey(null);
						}
						if (itemFromXml.getModifierKey() != null
								&& securityManager.loadIdentityShortByKey(itemFromXml.getModifierKey()) == null) {
							itemFromXml.setModifierKey(null);
						}
					}
					itemDAO.createItem(feed, itemFromXml);
					log.info("Item imported: " + itemFromXml.getGuid());
				}
				feedFileStorage.deleteItemXML(itemFromXml);
			}

			if (feed.isExternal()) {
				saveExternalItems(feed);
				saveExternalFeed(feed);
			}
		}

		feedFileStorage.deleteFeedXML(feed);
	}

	/**
	 * Method that checks the current feed data model version and applies
	 * necessary fixes to the model. Since feeds can be exported and imported
	 * this fixes must apply on the fly and can't be implemented with the system
	 * upgrade mechanism.
	 *
	 * @param feed
	 * @return the fixed items
	 */
	private List<Item> fixFeedVersionIssues(Feed feed, List<Item> items) {
		if (feed != null) {
			if (feed.getModelVersion() < 2
					&& feed.isInternal()
					&& PodcastFileResource.TYPE_NAME.equals(feed.getResourceableTypeName())) {
				// In model 1 the podcast episode items were set as drafts
				// which resulted in invisible episodes. They have to be
				// set to published. (OLAT-5767)
				for (Item episode : items) {
					// Mark episode as published
					episode.setDraft(false);
				}
			}
		}
		return items;
	}

	@Override
	public void releaseLock(LockResult lock) {
		if (lock != null) {
			coordinator.getLocker().releaseLock(lock);
		}
	}

	@Override
	public LockResult acquireLock(OLATResourceable feed, Identity identity) {
		return coordinator.getLocker().acquireLock(feed, identity, null, null);
	}

	@Override
	public LockResult acquireLock(OLATResourceable feed, Item item, Identity identity) {
		String key = itemKey(item, feed);
		if (key.length() >= OresHelper.ORES_TYPE_LENGTH) {
			key = Encoder.md5hash(key);
		}
		OLATResourceable itemResource = OresHelper.createOLATResourceableType(key);
		return coordinator.getLocker().acquireLock(itemResource, identity, key, null);
	}

	@Override
	public Quota getQuota(OLATResourceable feed) {
		LocalFolderImpl container = feedFileStorage.getResourceContainer(feed);

		Quota quota = quotaManager.getCustomQuota(container.getRelPath());
		if (quota == null) {
			Quota defQuota = quotaManager.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_FEEDS);
			quota = quotaManager.createQuota(container.getRelPath(), defQuota.getQuotaKB(), defQuota.getUlLimitKB());
		}
		return quota;
	}

	@Override
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

}
