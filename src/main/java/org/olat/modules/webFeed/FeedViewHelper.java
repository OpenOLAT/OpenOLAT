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
package org.olat.modules.webFeed;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.modules.webFeed.dispatching.Path;
import org.olat.modules.webFeed.managers.FeedManager;
import org.olat.modules.webFeed.models.Enclosure;
import org.olat.modules.webFeed.models.Feed;
import org.olat.modules.webFeed.models.Item;
import org.olat.modules.webFeed.models.ItemPublishDateComparator;
import org.olat.modules.webFeed.portfolio.LiveBlogArtefactHandler;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResourceManager;

/**
 * The object provides helper methods for feed views. This is required since the
 * feed urls are user dependent.
 * 
 * <P>
 * Initial Date: Mar 11, 2009 <br>
 * 
 * @author gwassmann
 */
public class FeedViewHelper {
	
	private static final OLog log = Tracing.createLoggerFor(FeedViewHelper.class);
	
	// display 5 items per default
	private int itemsPerPage = 5;
	private Feed feed;
	private String feedAuthor;
	private Identity identity;
	private Translator translator;
	private Locale locale;
	private String baseUri, feedUrl, nodeId;
	private Long courseId;
	private static final String MEDIA_DIR = Path.MEDIA_DIR;
	// Per default show the first page
	private int page = 0;
	private List<Item> cachedItems;
	//
	private FeedManager feedManager = FeedManager.getInstance();

	/**
	 * Use this constructor for localized content (like e.g. date formats)
	 * 
	 * @param feed
	 * @param identity
	 * @param feedAuthor The full name's of the author
	 * @param locale
	 */
	public FeedViewHelper(Feed feed, Identity identity, String feedAuthor, Translator translator, Long courseId, String nodeId, FeedSecurityCallback callback) {
		this.feed = feed;
		this.identity = identity;
		this.feedAuthor = feedAuthor;
		this.translator = translator;
		this.locale = translator.getLocale();
		this.courseId = courseId;
		this.nodeId = nodeId;
		this.cachedItems = feed.getFilteredItems(callback, identity);
		this.setURIs();
	}

	/**
	 * Use this constructor if no internationalization properties are required
	 * 
	 * @param feed
	 * @param identityKey
	 */
	FeedViewHelper(Feed feed, Identity identity, Long courseId, String nodeId) {
		this.feed = feed;
		this.identity = identity;
		
		this.courseId = courseId;
		this.nodeId = nodeId;
		this.setURIs();
	}

	public String getFeedAuthor() {
		return feedAuthor;
	}

	/**
	 * Set the base uri of an internal feed. <br>
	 * E.g http://my.olat.org/olat/feed/ident/[IDKEY]/token/[TOKEN]/id/[ORESID]
	 */
	public void setURIs() {
		// Set feed base URI for internal feeds
		if (feed.isInternal()) {
			baseUri = FeedManager.getInstance().getFeedBaseUri(feed, identity, courseId, nodeId);
			feedUrl = baseUri + "/" + FeedManager.RSS_FEED_NAME;
		} else if (feed.isExternal()) {
			// The base uri is needed for dispatching the picture
			baseUri = FeedManager.getInstance().getFeedBaseUri(feed, identity, courseId, nodeId);
			feedUrl = feed.getExternalFeedUrl();
		} else {
			// feed is undefined
			// The base uri is needed for dispatching the picture
			baseUri = FeedManager.getInstance().getFeedBaseUri(feed, identity, courseId, nodeId);
			feedUrl = null;
			feed.setExternalImageURL(null);
		}
	}

	/**
	 * @return The iTunes subscription url
	 */
	public String getITunesUrl(String protocol) {
		String iTunesfeed = null;
		if (StringHelper.containsNonWhitespace(feedUrl)) {
			try {
				URL url = new URL(feedUrl);
				if (!StringHelper.containsNonWhitespace(protocol)) {
					protocol = "itpc";
				}
				iTunesfeed = protocol + "://" + url.getHost() + url.getPath();
			} catch (MalformedURLException e) {
				log.warn("Malformed podcast URL: " + feedUrl, e);
			}
		}
		return iTunesfeed;
	}

	/**
	 * @return The Yahoo! subscription url
	 */
	public String getYahooUrl() {
		return "http://add.my.yahoo.com/rss?url=" + feedUrl;
	}

	/**
	 * @return The Google subscription url
	 */
	public String getGoogleUrl() {
		return "http://fusion.google.com/add?feedurl=" + feedUrl;
	}

	/**
	 * @return The feed url
	 */
	public String getFeedUrl() {
		return feedUrl;
	}

	/**
	 * @param item
	 * @return The media url of the item
	 */
	public String getMediaUrl(Item item) {
		// Reload item to prevent displaying of stale content
		feed = feedManager.getFeed(feed);
		item = feedManager.getItem(feed, item.getGuid());
		if(item == null) {
			return null;
		}

		String file = null;
		Enclosure enclosure = item.getEnclosure();
		if (enclosure != null) {
			if (feed.isExternal()) {
				file = item.getEnclosure().getExternalUrl();
			} else if (feed.isInternal()) {
				file = this.baseUri + "/" + item.getGuid() + "/" + MEDIA_DIR + "/" + enclosure.getFileName();
			}
		}
		return file;
	}

	/**
	 * @return The feed image url
	 */
	public String getImageUrl() {
		String imageUrl = null;
		if (feed.getImageName() != null) {
			imageUrl = baseUri + "/" + MEDIA_DIR + "/" + feed.getImageName();
		} else if (feed.getExternalImageURL() != null) {
			// If there's no custom image and the feed contains an image, use it!
			imageUrl = feed.getExternalImageURL();
		}
		return imageUrl;
	}

	/**
	 * @param enclosure
	 * @return The media type (audio or video)
	 */
	public String getMediaType(Enclosure enclosure) {
		String mediaType = null;
		if (enclosure != null) {
			// type is like 'video/mpeg' or 'audio/mpeg'
			String type = enclosure.getType();
			if (type != null) {
				type = type.split("/")[0];
				if ("audio".equals(type) || "video".equals(type)) {
					mediaType = type;
				}
			}
		}
		return mediaType;
	}

	/**
	 * @param item
	 * @return The formatted last modified date string of the item
	 */
	public String getLastModified(Item item) {
		// Reload item to prevent displaying of stale content
		feed = feedManager.getFeed(feed);
		item = feedManager.getItem(feed, item.getGuid());

		String lastModified = null;
		Date date = item == null ? null : item.getLastModified();
		if (date != null) {
			lastModified = DateFormat.getDateInstance(DateFormat.MEDIUM, this.locale).format(date);
		}
		return lastModified;
	}

	/**
	 * @param item
	 * @return The formatted last modified date string of the item
	 */
	private String getPublishDate(Item item) {
		// Reload item to prevent displaying of stale content
		feed = feedManager.getFeed(feed);
		item = feedManager.getItem(feed, item.getGuid());
		if(item == null) {
			return "";
		}

		String publishDate = null;
		Date date = item.getPublishDate();
		if (date != null) {
			publishDate = DateFormat.getDateInstance(DateFormat.MEDIUM, this.locale).format(date);
		}
		return publishDate;
	}

	/**
	 * @param item
	 * @return Information about publication date and author
	 */
	private String getPublishInfo(Item item) {
		// Reload item to prevent displaying of stale content
		feed = feedManager.getFeed(feed);
		item = feedManager.getItem(feed, item.getGuid());
		if(item == null) {
			return "";
		}

		String info = null;
		String date = getPublishDate(item);
		String author = StringHelper.escapeHtml(item.getAuthor());
		if (author != null) {
			if (date != null) {
				info = translator.translate("feed.published.by.on", new String[] { author, date });
			} else {
				info = translator.translate("feed.published.by", new String[] { author });
			}
		} else {
			if (date != null) {
				info = translator.translate("feed.published.on", new String[] { date });
			} else {
				// no publication info available
			}
		}
		return info;
	}

	/**
	 * @param item
	 * @return Information about the item. Is it draft, scheduled or published?
	 */
	public String getInfo(Item item) {
		// Reload item to prevent displaying of stale content
		feed = feedManager.getFeed(feed);
		item = feedManager.getItem(feed, item.getGuid());

		String info = null;
		if(item == null) {
			//oops deleted
			info = "";
		} else if (item.isDraft()) {
			info = translator.translate("feed.item.draft");
		} else if (item.isScheduled()) {
			info = translator.translate("feed.item.scheduled.for", new String[] { getPublishDate(item) });
		} else if (item.isPublished()) {
			info = getPublishInfo(item);
		}
		return info;
	}
	
	public boolean isModified(Item item) {
		// Reload item to prevent displaying of stale content
		feed = feedManager.getFeed(feed);
		item = feedManager.getItem(feed, item.getGuid());
		return item != null && item.getModifierKey() > 0 && StringHelper.containsNonWhitespace(item.getModifier());
	}
	
	/**
	 * @param item
	 * @return Information about the item. Is it draft, scheduled or published?
	 */
	public String getModifierInfo(Item item) {
		// Reload item to prevent displaying of stale content
		feed = feedManager.getFeed(feed);
		item = feedManager.getItem(feed, item.getGuid());
		if(item == null) {
			return "";
		}

		if (isModified(item)) {
			String date = getLastModified(item);
			String modifier = item.getModifier();
			return translator.translate("feed.modified.by.on", new String[]{ modifier, date});
		}
		return null;
	}

	/**
	 * @return The formatted last modified date string of the feed
	 */
	public String getLastModified() {
		String lastModified = null;
		Date date = feed.getLastModified();
		if (date != null) {
			lastModified = DateFormat.getDateInstance(DateFormat.MEDIUM, this.locale).format(date);
		}
		return lastModified;
	}
	
	public String getWidth(Item item) {
		// Reload item to prevent displaying of stale content
		feed = feedManager.getFeed(feed);
		item = feedManager.getItem(feed, item.getGuid());
		int width = item == null ? 0 : item.getWidth();
		if(width > 0 && width < 2000) {
			return Integer.toString(width);
		}
		return "400";
	}
	
	public String getHeight(Item item) {
		// Reload item to prevent displaying of stale content
		feed = feedManager.getFeed(feed);
		item = feedManager.getItem(feed, item.getGuid());

		int height = item == null ? 0 : item.getHeight();
		if(height > 0 && height < 2000) {
			return Integer.toString(height);
		}
		return "300";
	}

	/**
	 * @param item the target item for the jumpInLink null if not want to refer to a specific post
	 * @return The jump in link
	 */
	public String getJumpInLink(Item item) {
		String jumpInLink = null;
		RepositoryManager resMgr = RepositoryManager.getInstance();
		if (courseId != null && nodeId != null) {
			OLATResourceable oresCourse = OLATResourceManager.getInstance().findResourceable(courseId, CourseModule.getCourseTypeName());
			OLATResourceable oresNode = OresHelper.createOLATResourceableInstance("CourseNode", Long.valueOf(nodeId));
			RepositoryEntry repositoryEntry = resMgr.lookupRepositoryEntry(oresCourse, false);
			List<ContextEntry> ces = new ArrayList<ContextEntry>();
			ces.add(BusinessControlFactory.getInstance().createContextEntry(repositoryEntry));
			ces.add(BusinessControlFactory.getInstance().createContextEntry(oresNode));
			jumpInLink = BusinessControlFactory.getInstance().getAsURIString(ces, false);
		} else {
			RepositoryEntry repositoryEntry = resMgr.lookupRepositoryEntry(feed, false);
			if (repositoryEntry != null){
				ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(repositoryEntry);
				jumpInLink = BusinessControlFactory.getInstance().getAsURIString(Collections.singletonList(ce), false);
			} else {
				// its a liveblog-feed
				final BusinessControlFactory bCF = BusinessControlFactory.getInstance();
				String feedBP = LiveBlogArtefactHandler.LIVEBLOG + feed.getResourceableId() + "]";
				final List<ContextEntry> ceList = bCF.createCEListFromString(feedBP);
				jumpInLink = bCF.getAsURIString(ceList, true);
			}
		}
		if(item != null && jumpInLink != null){
			jumpInLink += "/item=" + item.getGuid() +"/0";
		}
		return jumpInLink;
	}

	/**
	 * @param item
	 * @return The item description with media file paths that are dispatchable by
	 *         the FeedMediaDispatcher
	 */
	public String getItemDescriptionForBrowser(Item item) {
		// Reload item to prevent displaying of stale content
		feed = feedManager.getFeed(feed);
		item = feedManager.getItem(feed, item.getGuid());
		if(item == null) {
			return "";
		}
		
		String itemDescription = item.getDescription();
		if (itemDescription != null) {
			if (feed.isExternal()) {
				// Apply xss filter for security reasons. Only necessary for external
				// feeds (e.g. to not let them execute JS code in our OLAT environment)
				Filter xssFilter = FilterFactory.getXSSFilter(itemDescription.length() + 1);
				itemDescription = xssFilter.filter(itemDescription);
			} else {
				// Add relative media base to media elements to display internal media
				// files
				String basePath = baseUri + "/" + item.getGuid();
				Filter mediaUrlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(basePath);
				itemDescription = mediaUrlFilter.filter(itemDescription);
			}
		}
		itemDescription = Formatter.formatLatexFormulas(itemDescription);
		return itemDescription;
	}
	
	/**
	 * @param item
	 * @return The item content with media file paths that are dispatchable by
	 *         the FeedMediaDispatcher
	 */
	public String getItemContentForBrowser(Item item) {
		// Reload item to prevent displaying of stale content
		feed = feedManager.getFeed(feed);
		item = feedManager.getItem(feed, item.getGuid());
		if(item == null) {
			return "";
		}
		
		String itemContent = item.getContent();
		if (itemContent != null) {
			if (feed.isExternal()) {
				// Apply xss filter for security reasons. Only necessary for external
				// feeds (e.g. to not let them execute JS code in our OLAT environment)
				Filter xssFilter = FilterFactory.getXSSFilter(itemContent.length() + 1);
				itemContent = xssFilter.filter(itemContent);
			} else {
				// Add relative media base to media elements to display internal media
				// files
				String basePath = baseUri + "/" + item.getGuid();
				Filter mediaUrlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(basePath);
				itemContent = mediaUrlFilter.filter(itemContent);
			}
		}
		return itemContent;
	}

	/**
	 * @return The feed description with dispatchable media file paths
	 */
	public String getFeedDescriptionForBrowser() {
		Filter mediaUrlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(baseUri);
		return mediaUrlFilter.filter(feed.getDescription());
	}

	/* Used for paging */

	public void setItemsPerPage(int itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}

	/**
	 * Show older items, meaning go to the next page.
	 */
	public void olderItems() {
		if (hasOlderItems()) {
			page++;
		}
	}

	/**
	 * @return True there are newer items to display
	 */
	public boolean hasOlderItems() {
		return cachedItems.size() > itemsPerPage * (page + 1);
	}

	/**
	 * Show newer items, meaning go to the previous page.
	 */
	public void newerItems() {
		page--;
		if (page < 0) {
			page = 0;
		}
	}

	/**
	 * Go to the startpage
	 */
	public void startpage() {
		page = 0;
	}

	/**
	 * @return True if there are newer items to display
	 */
	public boolean hasNewerItems() {
		return page > 0;
	}

	/**
	 * @param callback
	 * @return The items count of all displayed (accessible) items
	 */
	public int itemsCount(FeedSecurityCallback callback) {
		if (cachedItems == null) {
			cachedItems = feed.getFilteredItems(callback, identity);
		}
		return cachedItems.size();
	}

	/**
	 * @return The items to be displayed on the current page
	 */
	public List<Item> getItems(FeedSecurityCallback callback) {
		List<Item> itemsOnPage = new ArrayList<Item>(itemsPerPage);
		if (cachedItems == null) {
			cachedItems = feed.getFilteredItems(callback, identity);
		}
		final int start = page * itemsPerPage;
		final int end = Math.min(cachedItems.size(), start + itemsPerPage);
		for (int i = start; i < end; i++) {
			itemsOnPage.add(cachedItems.get(i));
		}
		return itemsOnPage;
	}

	/**
	 * @param selectedItems
	 */
	public void setSelectedItems(List<Item> selectedItems) {
		this.cachedItems = selectedItems;
		// go to the first page
		page = 0;
	}

	/**
	 * Removes the item from the current selection of items
	 * 
	 * @param item The item to remove
	 */
	public void removeItem(Item item) {
		cachedItems.remove(item);
	}

	/**
	 * Adds the item to the current selection of items.
	 * 
	 * @param item The item to add
	 */
	public void addItem(Item item) {
		if (!cachedItems.contains(item)) {
			cachedItems.add(item);
		}
		Collections.sort(cachedItems, new ItemPublishDateComparator());
	}

	/**
	 * Update the given item in the current selection of items. The code will
	 * replace the item with the same GUID in the current selection of items.
	 * 
	 * @param item The item to update
	 */
	public void updateItem(Item item) {
		if (cachedItems.contains(item)) {
			// Remove old version first. Not necessarily the same on object level
			// since item overrides the equal method
			cachedItems.remove(item);
		}
		addItem(item);
	}

	/**
	 * Resets the item selection to all accessible items of the feed
	 * 
	 * @param callback
	 */
	public void resetItems(FeedSecurityCallback callback) {
		feed = feedManager.getFeed(feed);
		cachedItems = feed.getFilteredItems(callback, identity);
	}

	/**
	 * Check if the current user is the author of this feed item
	 * @param item
	 * @return
	 */
	public boolean isAuthor(Item item) {
		if (item != null) {
			if (item.getAuthorKey() == identity.getKey().longValue()) {
				return true;
			}
		}
		return false;
	}

}