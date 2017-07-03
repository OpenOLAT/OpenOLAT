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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.olat.core.id.OLATResourceable;
import org.olat.modules.webFeed.Enclosure;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.model.FeedImpl;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndImage;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;

/**
 * 
 * Initial date: 12.05.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class RomeFeedFetcherTest {

	private RomeFeedFetcher sut;
	
	@Mock
	Feed feedMock;
	@Mock
	OLATResourceable oresMock;
	@Mock
	private SyndFeedInput syndFeedInputMock;
	@Mock 
	private SyndFeed syndFeedMock;
	@Mock 
	private SyndImage syndImageMock;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private SyndEntry syndEntryMock;
	@Mock(name="a")
	private SyndEnclosure syndEnclosureMock;
	@Mock
	private SyndContent syndContentMock;
	
	@Before
	public void setUp() {
		sut = new RomeFeedFetcher(syndFeedInputMock);
	}
	
	@Test
	public void fetchFeedShouldReturnFeedIfExternalFeedIsNotAvaible() throws IllegalArgumentException, FeedException {
		Feed feed = sut.fetchFeed(feedMock);
		
		assertThat(feed).isEqualTo(feedMock);
	}
	
	@Test
	public void fetchItemsShouldReturnEmptyListIfExternalFeedIsNotAvaible() throws IllegalArgumentException, FeedException {
		List<Item> items = sut.fetchItems(feedMock);
		
		assertThat(items).isEmpty();
	}
	
	@Test
	public void justifiyFeed() {
		String urlOld = "http://example.com/image1.jpg";
		String urlNew = "http://example.com/image2.jpg";
		Feed feed = new FeedImpl(oresMock);
		feed.setExternalImageURL(urlOld);

		when(syndImageMock.getUrl()).thenReturn(urlNew);
		when(syndFeedMock.getImage()).thenReturn(syndImageMock);
		
		Feed justifiedFeed = sut.justifyFeed(feed, syndFeedMock);
		
		assertThat(justifiedFeed.getExternalImageURL()).isEqualTo(urlNew);
	}
	
	@Test
	public void justifiyFeed_externalImageIsNull() {
		String urlOld = "http://example.com/image1.jpg";
		Feed feed = new FeedImpl(oresMock);
		feed.setExternalImageURL(urlOld);
		
		when(syndFeedMock.getImage()).thenReturn(null);
		
		Feed justifiedFeed = sut.justifyFeed(feed, syndFeedMock);
		
		assertThat(justifiedFeed.getExternalImageURL()).isNull();
	}
	
	@Test
	public void justifiyFeed_externalImageUrlIsNull() {
		String urlOld = "http://example.com/image1.jpg";
		String urlNew = null;
		Feed feed = new FeedImpl(oresMock);
		feed.setExternalImageURL(urlOld);
		
		when(syndFeedMock.getImage()).thenReturn(syndImageMock);
		when(syndImageMock.getUrl()).thenReturn(urlNew);
		
		Feed justifiedFeed = sut.justifyFeed(feed, syndFeedMock);
		
		assertThat(justifiedFeed.getExternalImageURL()).isNull();
	}
	
	@Test
	public void justifiyFeed_Feed_null() {
		String urlOld = "http://example.com/image1.jpg";
		Feed feed = new FeedImpl(oresMock);
		feed.setExternalImageURL(urlOld);
		
		Feed justifiedFeed = sut.justifyFeed(feed, null);
		
		assertThat(justifiedFeed.getExternalImageURL()).isEqualTo(urlOld);
	}
	
	@Test
	public void justifiyFeed_null_Feed() {
		Feed justifiedFeed = sut.justifyFeed(null, syndFeedMock);
		
		assertThat(justifiedFeed).isNull();
	}

	@Test
	public void convertEntry() {
		// prepare the mock
		String author = "author";
		when(syndEntryMock.getAuthor()).thenReturn(author);
		String description = "description";
		when(syndEntryMock.getDescription().getValue()).thenReturn(description);
		String link = "link";
		when(syndEntryMock.getLink()).thenReturn(link);
		String title = "title";
		when(syndEntryMock.getTitle()).thenReturn(title);
		String uri = "uri";
		when(syndEntryMock.getUri()).thenReturn(uri);
		
		Date publishedDate = new Date();
		when(syndEntryMock.getPublishedDate()).thenReturn(publishedDate);
		Date updatedDate = new Date();
		when(syndEntryMock.getUpdatedDate()).thenReturn(updatedDate);
		
		List<SyndContent> contents = Arrays.asList(syndContentMock);
		when(syndEntryMock.getContents()).thenReturn(contents);
		
		when(syndEnclosureMock.getUrl()).thenReturn("URL");
		List<SyndEnclosure> enclosures = Arrays.asList(syndEnclosureMock);
		when(syndEntryMock.getEnclosures()).thenReturn(enclosures);
		
		// call the method
		Item item = sut.convertEntry(feedMock, syndEntryMock);
		
		// test
		assertThat(item.getFeed()).isEqualTo(feedMock);
		assertThat(item.getAuthor()).isEqualTo(author);
		assertThat(item.getContent()).isNotNull();
		assertThat(item.getDescription()).isEqualTo(description);
		assertThat(item.getEnclosure()).isNotNull();
		assertThat(item.getExternalLink()).isEqualTo(link);
		assertThat(item.getTitle()).isEqualTo(title);
		assertThat(item.getGuid()).isEqualTo(uri);
		assertThat(item.getLastModified()).isEqualTo(updatedDate);
		assertThat(item.getPublishDate()).isEqualTo(publishedDate);
	}
	
	@Test
	public void convertEntry_Description_null() {
		when(syndEntryMock.getDescription()).thenReturn(null);
		when(syndEntryMock.getContents()).thenReturn(null);

		Item item = sut.convertEntry(feedMock, syndEntryMock);

		assertThat(item.getDescription()).isNull();
	}
	
	@Test
	public void convertEnclosures() {
		Long length = 1l;
		when(syndEnclosureMock.getLength()).thenReturn(length);
		String type = "type";
		when(syndEnclosureMock.getType()).thenReturn(type);
		String url = "url";
		when(syndEnclosureMock.getUrl()).thenReturn(url);
		List<SyndEnclosure> enclosures = Arrays.asList(syndEnclosureMock);
		
		Enclosure enclosure = sut.convertEnclosures(enclosures);
		
		assertThat(enclosure.getExternalUrl()).isEqualTo(url);
		assertThat(enclosure.getLength()).isEqualTo(length);
		assertThat(enclosure.getType()).isEqualTo(type);
	}
	
	@Test
	public void convertEnclosures_null() {
		Enclosure enclosure = sut.convertEnclosures(null);
		
		assertThat(enclosure).isNull();
	}
	
	@Test
	public void convertEnclosures_emptyList() {
		List<SyndEnclosure> enclosures = new ArrayList<>();
		
		Enclosure enclosure = sut.convertEnclosures(enclosures);
		
		assertThat(enclosure).isNull();
	}
	
	@Test
	public void convertEnclosures_Url_null() {
		when(syndEnclosureMock.getUrl()).thenReturn(null);
		List<SyndEnclosure> enclosures = Arrays.asList(syndEnclosureMock);
		
		Enclosure enclosure = sut.convertEnclosures(enclosures);
		
		assertThat(enclosure).isNull();
	}
	
	@Test
	public void convertEnclosures_multipleEntries() {
		String url = "url";
		when(syndEnclosureMock.getUrl()).thenReturn(url);
		List<SyndEnclosure> enclosures = 
				Arrays.asList(syndEnclosureMock, syndEnclosureMock);
		
		sut.convertEnclosures(enclosures);
		
		// verify that getType is invoked only once / only from one list entry
		verify(syndEnclosureMock).getType();
	}
	
	@Test
	public void joinContents() {
		String value = "value";
		when(syndContentMock.getValue()).thenReturn(value);
		List<SyndContent> contents = Arrays.asList(syndContentMock);
		
		String content = sut.joinContents(contents);
		
		assertThat(content).isEqualTo(value);
	}
	
	@Test
	public void joinContents_null() {
		String content = sut.joinContents(null);
		
		assertThat(content).isNull();
	}
	
	@Test
	public void joinContents_emptyList() {
		List<SyndContent> contents = new ArrayList<>();
		
		String content = sut.joinContents(contents);
		
		assertThat(content).isNull();
	}
	
	@Test
	public void joinContents_multipleEntries() {
		String value = "value";
		when(syndContentMock.getValue()).thenReturn(value);
		List<SyndContent> contents = 
				Arrays.asList(syndContentMock, syndContentMock, syndContentMock);
		
		String content = sut.joinContents(contents);
		
		assertThat(content).isEqualTo("value<p />value<p />value");
	}
	
}
