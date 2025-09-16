/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.webFeed.manager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.modules.webFeed.Enclosure;
import org.olat.modules.webFeed.ExternalFeedFetcher;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.model.EnclosureImpl;
import org.olat.modules.webFeed.model.ItemImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.ParsingFeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

/**
 * This implementation of an ExternalFeedFetcher uses the library Rome to fetch
 * feeds form an external web site.<br>
 *
 * Initial date: 12.05.2017<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class RomeFeedFetcher implements ExternalFeedFetcher {

	private static final Logger log = Tracing.createLoggerFor(RomeFeedFetcher.class);
	
	@Autowired
	private HttpClientService httpClientService;

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
				.toList();
	}

	/**
	 * Fetches the SyndFeed of an URL.
	 * @param feedURL
	 * @return
	 * @throws IOException 
	 * @throws FeedException 
	 * @throws IllegalArgumentException 
	 */
	protected SyndFeed fetchSyndFeed(String feedURL) {
		try {
			SyndFeed syndFeed = getSyndFeed(feedURL);
			log.info("Read external feed: {}", feedURL);
			return syndFeed;
		} catch (Exception e) {
			log.debug(e);
		}
		
		return null;
	}
	
	private SyndFeed getSyndFeed(String feedURL) throws IOException, IllegalArgumentException, FeedException {
		try(CloseableHttpClient client = httpClientService.createHttpClient();
				CloseableHttpResponse response = client.execute(new HttpGet(feedURL))) {
			int statusCode = response.getStatusLine().getStatusCode();
			log.debug("Status code of: {} {}", feedURL, statusCode);
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					try (XmlReader xmlReader = new XmlReader(entity.getContent())) {
						return syndFeedInput.build(xmlReader);
					}
				}
			}
		}
		
		log.warn("Cannot read external feed: : {}", feedURL);
		return null;
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
		try {
			SyndFeed feed = getSyndFeed(url);
			if (feed == null) {
				return new ValidatedURL(url, null, ValidatedURL.State.NOT_FOUND);
			}
			if (enclosuresExpected && !feed.getEntries().isEmpty()) {
				SyndEntry entry = feed.getEntries().get(0);
				if (entry.getEnclosures().isEmpty()) {
					return new ValidatedURL(url, null, ValidatedURL.State.NO_ENCLOSURE);
				}
			}
			// The feed was read successfully
			return new ValidatedURL(url, feed.getTitle(), ValidatedURL.State.VALID);
		} catch (ParsingFeedException e) {
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
