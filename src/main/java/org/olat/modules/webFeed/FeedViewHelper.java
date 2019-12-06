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

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.nodes.feed.blog.BlogToolController;
import org.olat.modules.webFeed.dispatching.Path;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.modules.webFeed.model.ItemPublishDateComparator;
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
	
	private static final Logger log = Tracing.createLoggerFor(FeedViewHelper.class);
	
	// display 5 items per default
	private int itemsPerPage = 5;
	private Roles roles;
	private Identity identity;
	private Translator translator;
	private Locale locale;
	private String baseUri;
	private String feedUrl;
	private String nodeId;
	private Long courseId;
	private static final String MEDIA_DIR = Path.MEDIA_DIR;
	// Per default show the first page
	private int page = 0;

	private FeedManager feedManager = FeedManager.getInstance();

	/**
	 * Use this constructor for localized content (like e.g. date formats)
	 * @param identity
	 * @param feed
	 * @param locale
	 */
	public FeedViewHelper(Feed feed, Identity identity, Roles roles, Translator translator, Long courseId, String nodeId) {
		this.identity = identity;
		this.translator = translator;
		this.locale = translator.getLocale();
		this.courseId = courseId;
		this.nodeId = nodeId;
		this.roles = roles;
		this.setURIs(feed);
	}

	/**
	 * Use this constructor if no internationalization properties are required
	 * 
	 * @param feed
	 * @param identityKey
	 */
	FeedViewHelper(Feed feed, Identity identity, Roles roles, Long courseId, String nodeId) {
		this.identity = identity;
		this.roles = roles;
		this.courseId = courseId;
		this.nodeId = nodeId;
		this.setURIs(feed);
	}

	/**
	 * Set the base uri of an internal feed. <br>
	 * E.g http://my.olat.org/olat/feed/ident/[IDKEY]/token/[TOKEN]/id/[ORESID]
	 * @param feed 
	 */
	public void setURIs(Feed feed) {
		baseUri = FeedManager.getInstance().getFeedBaseUri(feed, identity, roles, courseId, nodeId);
		// Set feed base URI for internal feeds
		if (feed.isInternal()) {
			feedUrl = baseUri + "/" + FeedManager.RSS_FEED_NAME;
		} else if (feed.isExternal()) {
			// The base uri is needed for dispatching the picture
			feedUrl = feed.getExternalFeedUrl();
		} else {
			// feed is undefined
			// The base uri is needed for dispatching the picture
			feedUrl = null;
		}
	}

	/**
	 * @return The iTunes subscription url
	 */
	public String getPodcastAppUrl() {
		String iTunesfeed = null;
		if (StringHelper.containsNonWhitespace(feedUrl)) {
			try {
				URL url = new URL(feedUrl);
				iTunesfeed = "podcast://" + url.getHost() + url.getPath();
				if (iTunesfeed.endsWith("/" + FeedManager.RSS_FEED_NAME)) {
					iTunesfeed = iTunesfeed.replace("/" + FeedManager.RSS_FEED_NAME, "/feed.xml");
				}
			} catch (MalformedURLException e) {
				log.warn("Malformed podcast URL: {}", feedUrl, e);
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
	 * Format the lastModified of the feed.
	 * @param feed
	 * @return
	 */
	public String getLastModified(Feed feed) {
		String lastModified = null;
		
		Date date = feed.getLastModified();
		if (date != null) {
			lastModified = DateFormat.getDateInstance(DateFormat.MEDIUM, this.locale).format(date);
		}
		
		return lastModified;
	}

	/**
	 * Get the concatenated URL of the Image of an internal feed or the URL of
	 * the Image of an external Feed.
	 * 
	 * @param feed
	 * @return
	 */
	public String getImageUrl(Feed feed) {
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
	 * The feed description with dispatchable media file paths
	 * @param feed
	 * @return
	 */
	public String getFeedDescriptionForBrowser(Feed feed) {
		Filter mediaUrlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(baseUri);
		return mediaUrlFilter.filter(feed.getDescription());
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
	 * Information about the item mode. 
	 * 
	 * @param item
	 * @return Is it draft, scheduled or published?
	 */
	public String getInfo(Item item) {
		String info = null;
		
		if (item == null) {
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

	/**
	 * Get information about publication date and author.
	 * 
	 * @param item
	 * @return
	 */
	private String getPublishInfo(Item item) {
		String publishInfo = "";
		
		if (item != null) {
			String date = getPublishDate(item);
			String author = StringHelper.escapeHtml(item.getAuthor());
			if (author != null) {
				if (date != null) {
					publishInfo = translator.translate("feed.published.by.on", new String[] { author, date });
				} else {
					publishInfo = translator.translate("feed.published.by", new String[] { author });
				}
			} else if (date != null) {
				publishInfo = translator.translate("feed.published.on", new String[] { date });
			}
		}

		return publishInfo;
	}

	/**
	 * Get the formatted last modified date string of the item.
	 * 
	 * @param item
	 * @return
	 */
	private String getPublishDate(Item item) {
		String publishDate = "";
		
		if (item != null) {
			Date date = item.getPublishDate();
			if (date != null) {
				publishDate = DateFormat.getDateInstance(DateFormat.MEDIUM, this.locale).format(date);
			}
		}
		
		return publishDate;
	}

	/**
	 * Get Information about the modifier.
	 * 
	 * @param item
	 * @return 
	 */
	public String getModifierInfo(Item item) {
		String modifierInfo = "";
	
		if (isModified(item)) {
			String date = getLastModified(item);
			String modifier = item.getModifier();
			modifierInfo = translator.translate("feed.modified.by.on", new String[]{ modifier, date});
		}
		
		return modifierInfo;
	}

	/**
	 * Check if the Item was modified at least once.
	 * 
	 * @param item
	 * @return
	 */
	public boolean isModified(Item item) {
		boolean isModified = false;
		
		if (item != null) {
			isModified = item.getModifierKey() != null && StringHelper.containsNonWhitespace(item.getModifier());
		}
		
		return isModified;
	}

	/**
	 * Get the formatted last modified date of the Item.
	 * 
	 * @param item
	 * @return
	 */
	public String getLastModified(Item item) {
		String lastModified = null;
		
		if (item != null) {
			Date date = item.getLastModified();
			if (date != null) {
				lastModified = DateFormat.getDateInstance(DateFormat.MEDIUM, this.locale).format(date);
			}
		}
		
		return lastModified;
	}

	/**
	 * Get the item description with media file paths that are dispatchable by
	 * a FeedMediaDispatcher.
	 *         
	 * @param item
	 * @return 
	 */
	public String getItemDescriptionForBrowser(Item item) {
		String itemDescription = "";
		
		if (item != null) {
			String description = item.getDescription();
			if (description != null) {
				if (item.getFeed().isExternal()) {
					// Apply xss filter for security reasons. Only necessary for external
					// feeds (e.g. to not let them execute JS code in our OLAT environment)
					Filter xssFilter = FilterFactory.getXSSFilter();
					itemDescription = xssFilter.filter(description);
				} else {
					// Add relative media base to media elements to display internal media
					// files
					String basePath = baseUri + "/" + item.getGuid();
					Filter mediaUrlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(basePath);
					itemDescription = mediaUrlFilter.filter(description);
				}
			}
			itemDescription = Formatter.formatLatexFormulas(itemDescription);
		}

		return itemDescription;
	}

	/**
	 * The item content with media file paths that are dispatchable by
	 * a FeedMediaDispatcher.
	 *         
	 * @param item
	 * @return
	 */
	public String getItemContentForBrowser(Item item) {
		String itemContent = "";
		
		if (item != null) {
			String content = item.getContent();
			if (content != null) {
				if (item.getFeed().isExternal()) {
					// Apply xss filter for security reasons. Only necessary for external
					// feeds (e.g. to not let them execute JS code in our OLAT environment)
					Filter xssFilter = FilterFactory.getXSSFilter();
					itemContent = xssFilter.filter(content);
				} else {
					// Add relative media base to media elements to display internal media
					// files
					String basePath = baseUri + "/" + item.getGuid();
					Filter mediaUrlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(basePath);
					itemContent = mediaUrlFilter.filter(content);
				}
			}
		}
		
		return itemContent;
	}

	/**
	 * Get the width of the Item. The width is limited to a range of 0-2000.
	 * 
	 * @param item
	 * @return
	 */
	public String getWidth(Item item) {
		String widthString = "400";
		
		if (item != null) {
			Integer width = item.getWidth();
			if (width != null && width > 0 && width < 2000) {
				widthString = Integer.toString(width);
			}
		}
		
		return widthString;
	}
	
	/**
	 * Get the height of the Item. The height is limited to a range of 0-2000.
	 * 
	 * @param item
	 * @return
	 */
	public String getHeight(Item item) {
		String heightString = "400";
		
		if (item != null) {
			Integer height = item.getHeight();
			if (height != null && height > 0 && height < 2000) {
				heightString = Integer.toString(height);
			}
		}
		
		return heightString;
	}

	/**
	 * @param item
	 * @return The media url of the item
	 */
	public String getMediaUrl(Item item) {
		// Reload item to prevent displaying of stale content
		item = feedManager.loadItem(item.getKey());
		if(item == null) {
			return null;
		}
	
		String file = null;
		Enclosure enclosure = item.getEnclosure();
		if (enclosure != null) {
			if (item.getFeed().isExternal()) {
				file = item.getEnclosure().getExternalUrl();
			} else if (item.getFeed().isInternal()) {
				file = this.baseUri + "/" + item.getGuid() + "/" + MEDIA_DIR + "/" + enclosure.getFileName();
			}
		}
		return file;
	}
	
	/**
	 * 
	 * @param feed
	 *            the target feed for the jumpInLink
	 * @param item
	 *            the target item for the jumpInLink or null if not want to
	 *            refer to a specific item
	 * @return the jump in link
	 */
	public String getJumpInLink(Feed feed, Item item) {
		String jumpInLink = null;
		RepositoryManager resMgr = RepositoryManager.getInstance();
		if (courseId != null && nodeId != null) {
			
			List<ContextEntry> ces = new ArrayList<>();
			OLATResourceable oresCourse = OLATResourceManager.getInstance().findResourceable(courseId, CourseModule.getCourseTypeName());
			RepositoryEntry repositoryEntry = resMgr.lookupRepositoryEntry(oresCourse, false);
			ces.add(BusinessControlFactory.getInstance().createContextEntry(repositoryEntry));
			if (BlogToolController.SUBSCRIPTION_SUBIDENTIFIER.equals(nodeId)) {
				OLATResourceable oresTool = OresHelper
						.createOLATResourceableInstance(BlogToolController.SUBSCRIPTION_SUBIDENTIFIER, Long.valueOf(0));
				ces.add(BusinessControlFactory.getInstance().createContextEntry(oresTool));
			} else {
				OLATResourceable oresNode = OresHelper.createOLATResourceableInstance("CourseNode", Long.valueOf(nodeId));
				ces.add(BusinessControlFactory.getInstance().createContextEntry(oresNode));
			}
			jumpInLink = BusinessControlFactory.getInstance().getAsURIString(ces, false);
		} else {
			RepositoryEntry repositoryEntry = resMgr.lookupRepositoryEntry(feed, false);
			if (repositoryEntry != null){
				ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(repositoryEntry);
				jumpInLink = BusinessControlFactory.getInstance().getAsURIString(Collections.singletonList(ce), false);
			} else {
				// its a liveblog-helperFeed
				final BusinessControlFactory bCF = BusinessControlFactory.getInstance();
				String feedBP = LiveBlogArtefactHandler.LIVEBLOG + feed.getResourceableId() + "]";
				final List<ContextEntry> ceList = bCF.createCEListFromString(feedBP);
				jumpInLink = bCF.getAsURIString(ceList, true);
			}
		}
		if(item != null && jumpInLink != null){
			jumpInLink += "/item=" + item.getKey() +"/0";
		}
		return jumpInLink;
	}

	

	

	/* Used for paging */

	public void setItemsPerPage(int itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}

	/**
	 * Show older items, meaning go to the next page.
	 */
	public void olderItems() {
		page++;
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
	 * Go to the start page
	 * 
	 */
	public void startpage() {
		page = 0;
	}

	/**
	 * Check if there are newer items to display
	 * 
	 * @return
	 */
	public boolean hasNewerItems() {
		return page > 0;
	}

	/**
	 * Check if there are older Items to display.
	 * 
	 * @param items
	 * @return
	 */
	public boolean hasOlderItems(List<Item> items) {
		return items.size() > itemsPerPage * (page + 1);
	}

	/**
	 * Get all displayed items inside the paged list of items.
	 * 
	 * @param items the already sorted items
	 * @return
	 */
	public List<Item> getItemsOnPage(List<Item> items) {
		List<Item> itemsOnPage = new ArrayList<>(itemsPerPage);
		
		final int start = page * itemsPerPage;
		final int end = Math.min(items.size(), start + itemsPerPage);
		for (int i = start; i < end; i++) {
			itemsOnPage.add(items.get(i));
		}
		Collections.sort(itemsOnPage, new ItemPublishDateComparator());
		
		return itemsOnPage;
	}

	/**
	 * Check if the current user is the author of this helperFeed item
	 * @param item
	 * @return
	 */
	public boolean isAuthor(Item item) {
		return item != null && item.getAuthorKey() != null && item.getAuthorKey().equals(identity.getKey());
	}
}