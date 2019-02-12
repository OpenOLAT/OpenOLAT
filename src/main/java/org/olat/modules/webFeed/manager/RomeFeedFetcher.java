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

import java.io.FileNotFoundException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.webFeed.Enclosure;
import org.olat.modules.webFeed.ExternalFeedFetcher;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.model.EnclosureImpl;
import org.olat.modules.webFeed.model.ItemImpl;
import org.springframework.stereotype.Service;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.ParsingFeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

/**
 * This implementation of an ExternalFeedFetcher uses the library Rome to fetch
 * feeds form an external web site.<br>
 *
 * Initial date: 12.05.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RomeFeedFetcher implements ExternalFeedFetcher {

	private static final OLog log = Tracing.createLoggerFor(RomeFeedFetcher.class);

	private final SyndFeedInput syndFeedInput;

	public RomeFeedFetcher() {
		this(new SyndFeedInput());
	}

	public RomeFeedFetcher(SyndFeedInput syndFeedInput) {
		this.syndFeedInput = syndFeedInput;
	}

	@Override
	public Feed fetchFeed(Feed feed) {
		SyndFeed syndFeed = fetchSyndFeed(feed.getExternalFeedUrl());
		return justifyFeed(feed, syndFeed);
	}

	/**
	 * Takes the values from the SyndFeed and pass them to the Feed.
	 * @param feed
	 * @param syndFeed
	 * @return the justified feed or null
	 */
	protected Feed justifyFeed(Feed feed, SyndFeed syndFeed) {
		if (feed == null) return null;

		String imageUrl = null;
		if (syndFeed == null) {
			// keep the old image
			imageUrl = feed.getExternalImageURL();
		} else if (syndFeed.getImage() != null) {
			// take the new image
			imageUrl = syndFeed.getImage().getUrl();
		}

		feed.setExternalImageURL(imageUrl);

		return feed;
	}

	@Override
	public List<Item> fetchItems(Feed feed) {
		SyndFeed syndFeed = fetchSyndFeed(feed.getExternalFeedUrl());
		if (syndFeed == null) {
			return new ArrayList<>();
		}

		return syndFeed.getEntries().stream()
				.map(entry -> convertEntry(feed, entry))
				.collect(Collectors.toList());
	}

	/**
	 * Fetches the SyndFeed of an URL.
	 * @param feedURL
	 * @return
	 */
	protected SyndFeed fetchSyndFeed(String feedURL) {
		SyndFeed syndFeed = null;

		try(Reader xmlReader = new XmlReader(new URL(feedURL))) {
			syndFeed = syndFeedInput.build(xmlReader);
			log.info("Read external feed: " + feedURL);
		} catch (Exception e) {
			log.warn("Cannot read external feed: : " + feedURL);
		}

		return syndFeed;
	}


	/**
	 * Converts a <code>SyndEntry</code> into an <code>Item</code>
	 *
	 * @param entry
	 * @return
	 */
	protected Item convertEntry(Feed feed, SyndEntry entry) {
		Item item = new ItemImpl(feed);

		item.setAuthor(entry.getAuthor());
		item.setExternalLink(entry.getLink());
		item.setGuid(entry.getUri());
		item.setLastModified(entry.getUpdatedDate());
		item.setPublishDate(entry.getPublishedDate());
		item.setTitle(entry.getTitle());

		if (entry.getDescription() != null) {
			item.setDescription(entry.getDescription().getValue());
		}

		List<SyndContent> contents = entry.getContents();
		item.setContent(joinContents(contents));

		List<SyndEnclosure> enclosures = entry.getEnclosures();
		item.setEnclosure(convertEnclosures(enclosures));

		return item;
	}

	/**
	 * Converts a List of <code>SyndEnclosures</code> into an <code>Enclosure</code>.
	 * Only one media file is supported. If the List has more than one entry, the
	 * first entry is taken.
	 * SyndEnclosures without an URL are not converted, because it is necessary to
	 * fetch the enclosure.
	 *
	 * @param enclosures
	 * @return the enclosure or null
	 */
	protected Enclosure convertEnclosures(List<SyndEnclosure> enclosures) {
		if (enclosures == null || enclosures.isEmpty()) return null;

		SyndEnclosure syndEnclosure = enclosures.get(0);
		Enclosure enclosure = null;

		if (StringHelper.containsNonWhitespace(syndEnclosure.getUrl())) {
			enclosure = new EnclosureImpl();
			enclosure.setExternalUrl(syndEnclosure.getUrl());
			enclosure.setLength(syndEnclosure.getLength());
			enclosure.setType(syndEnclosure.getType());
		}

		return enclosure;
	}

	/**
	 * Joins the values of all SyndContent by a html p.
	 *
	 * @param contents
	 * @return the joined values or null
	 */
	protected String joinContents(List<SyndContent> contents) {
		if (contents == null || contents.isEmpty()) return null;

		return contents.stream()
				 .map(SyndContent::getValue)
				 .collect(Collectors.joining("<p />"));
	}

	@Override
	public ValidatedURL validateFeedUrl(String url, boolean enclosuresExpected) {
		SyndFeedInput input = new SyndFeedInput();

		boolean modifiedProtocol = false;
		try {
			if (url != null) {
				url = url.trim();
			}
			if (url.startsWith("feed") || url.startsWith("itpc")) {
				// accept feed(s) urls like generated in safari browser
				url = "http" + url.substring(4);
				modifiedProtocol = true;
			}
			URL realUrl = new URL(url);
			SyndFeed feed = input.build(new XmlReader(realUrl));
			if (!feed.getEntries().isEmpty()) {
				if (enclosuresExpected) {
					SyndEntry entry = feed.getEntries().get(0);
					if (entry.getEnclosures().isEmpty()) {
						return new ValidatedURL(url, null, ValidatedURL.State.NO_ENCLOSURE);
					}
				}
				return new ValidatedURL(url, null, ValidatedURL.State.VALID);
			}
			// The feed was read successfully
			return new ValidatedURL(url, feed.getTitle(), ValidatedURL.State.VALID);
		} catch (ParsingFeedException e) {
			if (modifiedProtocol) {
				// fallback for SWITCHcast itpc -> http -> https
				url = "https" + url.substring(4);
				return validateFeedUrl(url, enclosuresExpected);
			}
			String message = String.format("Validation of the feed url %s failed. %s: %s ", url, e.getClass(), e.getMessage());
			log.debug(message);
			return new ValidatedURL(url, null, ValidatedURL.State.NOT_FOUND);
		} catch (FileNotFoundException e) {
			String message = String.format("Validation of the feed url %s failed. %s: %s ", url, e.getClass(), e.getMessage());
			log.debug(message);
			return new ValidatedURL(url, null, ValidatedURL.State.NOT_FOUND);
		} catch (Exception e) {
			String message = String.format("Validation of the feed url %s failed. %s: %s ", url, e.getClass(), e.getMessage());
			log.debug(message);
		}
		return new ValidatedURL(url, null, ValidatedURL.State.MALFORMED);
	}

}
