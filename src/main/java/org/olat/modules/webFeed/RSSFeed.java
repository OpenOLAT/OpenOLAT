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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.services.notifications.PersonalRSSServlet;
import org.olat.core.id.Identity;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.webFeed.manager.FeedManager;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEnclosureImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.feed.synd.SyndImage;
import com.rometools.rome.feed.synd.SyndImageImpl;

/**
 * Creates a podcast feed (syndication feed) from a podcast resource.
 * 
 * <P>
 * Initial Date: Feb 25, 2009 <br>
 * 
 * @author Gregor Wassmann
 */
public class RSSFeed extends SyndFeedImpl {

	private static final long serialVersionUID = 4010701266801565756L;

	/**
	 * Constructor. The identityKey is needed to generate personal URLs for the
	 * corresponding user.
	 */
	public RSSFeed(Feed feed, Identity identity, Long courseId, String nodeId) {
		super();

		// This helper object is required for generating the appropriate URLs for
		// the given user (identity)
		FeedViewHelper helper = new FeedViewHelper(feed, identity, courseId, nodeId);

		setFeedType("rss_2.0");
		setEncoding(PersonalRSSServlet.DEFAULT_ENCODING);
		setTitle(feed.getTitle());
		// According to the rss specification, the feed channel description is not
		// (explicitly) allowed to contain html tags.
		String strippedDescription = FilterFactory.getHtmlTagsFilter().filter(feed.getDescription());
		strippedDescription = strippedDescription == null? "": strippedDescription;
		strippedDescription = strippedDescription.replaceAll("&nbsp;", " ");
		setDescription(strippedDescription);
		setLink(helper.getJumpInLink(feed, null));

		setPublishedDate(feed.getLastModified());
		// The image
		if (feed.getImageName() != null) {
			SyndImage image = new SyndImageImpl();
			image.setDescription(feed.getDescription());
			image.setTitle(feed.getTitle());
			image.setLink(getLink());
			image.setUrl(helper.getImageUrl(feed));
			setImage(image);
		}

		List<SyndEntry> episodes = new ArrayList<>();
		List<Item> publishedItems = FeedManager.getInstance().loadPublishedItems(feed);
		for (Item item : publishedItems) {
			SyndEntry entry = new SyndEntryImpl();
			entry.setTitle(item.getTitle());

			SyndContent itemDescription = new SyndContentImpl();
			itemDescription.setType("text/plain");
			itemDescription.setValue(helper.getItemDescriptionForBrowser(item));
			entry.setDescription(itemDescription);
			
			// Link will also be converted to the rss guid tag. Except if there's an
			// enclosure, then the enclosure url is used.
			// Use jump-in link far all entries. This will be overriden if the item
			// has an enclosure.
			entry.setLink(helper.getJumpInLink(item.getFeed(), item));
			entry.setPublishedDate(item.getPublishDate());
			entry.setUpdatedDate(item.getLastModified());

			// The enclosure is the media (audio or video) file of the episode
			Enclosure media = item.getEnclosure();
			if (media != null) {
				SyndEnclosure enclosure = new SyndEnclosureImpl();
				enclosure.setUrl(helper.getMediaUrl(item));
				enclosure.setType(media.getType());
				enclosure.setLength(media.getLength());
				// Also set the item link to point to the enclosure
				entry.setLink(helper.getMediaUrl(item));
				List<SyndEnclosure> enclosures = new ArrayList<>();
				enclosures.add(enclosure);
				entry.setEnclosures(enclosures);
			}

			episodes.add(entry);
		}
		setEntries(episodes);
	}
}
