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

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.fileresource.types.FeedFileResource;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.model.FeedImpl;
import org.olat.modules.webFeed.model.ItemImpl;
import org.olat.test.OlatTestCase;
import org.olat.test.VFSJavaIOFile;

/**
 * Test the FeedFileStorage.
 *
 * saveItemMedia
 *
 *
 * Initial date: 22.05.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FeedFileStorgeTest extends OlatTestCase {

	FeedFileStorge sut;

	private FileResourceManager fileResourceManager;

	@Before
	public void setup() {
		fileResourceManager = FileResourceManager.getInstance();
		sut = new FeedFileStorge();
	}

	@Test
	public void getOrCreateFeedContainer_create() {
		FeedFileResource ores = new BlogFileResource();

		VFSContainer feedContainer = sut.getOrCreateFeedContainer(ores);

		assertThat(feedContainer).isNotNull();

		fileResourceManager.deleteFileResource(ores);
	}

	@Test
	public void getOrCreateFeedContainer_get() {
		FeedFileResource ores = new BlogFileResource();

		sut.getOrCreateFeedContainer(ores);
		VFSContainer feedContainer = sut.getOrCreateFeedContainer(ores);

		assertThat(feedContainer).isNotNull();

		fileResourceManager.deleteFileResource(ores);
	}

	@Test
	public void getOrCreateFeedContainer_null() {
		VFSContainer feedContainer = sut.getOrCreateFeedContainer(null);

		assertThat(feedContainer).isNull();
	}

	@Test
	public void getOrCreateFeedMediaContainer_create() {
		FeedFileResource ores = new BlogFileResource();

		VFSContainer feedMediaContainer = sut.getOrCreateFeedMediaContainer(ores);

		assertThat(feedMediaContainer).isNotNull();

		fileResourceManager.deleteFileResource(ores);
	}

	@Test
	public void getOrCreateFeedMediaContainer_get() {
		FeedFileResource ores = new BlogFileResource();

		sut.getOrCreateFeedMediaContainer(ores);
		VFSContainer feedMediaContainer = sut.getOrCreateFeedMediaContainer(ores);

		assertThat(feedMediaContainer).isNotNull();

		fileResourceManager.deleteFileResource(ores);
	}

	@Test
	public void getOrCreateFeedMediaContainer_null() {
		VFSContainer feedMediaContainer = sut.getOrCreateFeedMediaContainer(null);

		assertThat(feedMediaContainer).isNull();
	}

	@Test
	public void getOrCreateFeedItemsContainer_create() {
		FeedFileResource ores = new BlogFileResource();

		VFSContainer feedItemsContainer = sut.getOrCreateFeedItemsContainer(ores);

		assertThat(feedItemsContainer).isNotNull();

		fileResourceManager.deleteFileResource(ores);
	}

	@Test
	public void getOrCreateFeedItemsContainer_get() {
		FeedFileResource ores = new BlogFileResource();

		sut.getOrCreateFeedItemsContainer(ores);
		VFSContainer feedItemsContainer = sut.getOrCreateFeedItemsContainer(ores);

		assertThat(feedItemsContainer).isNotNull();

		fileResourceManager.deleteFileResource(ores);
	}

	@Test
	public void getOrCreateFeedItemsContainer_null() {
		VFSContainer feedItemsContainer = sut.getOrCreateFeedItemsContainer(null);

		assertThat(feedItemsContainer).isNull();
	}

	@Test
	public void getOrCreateItemContainer_create() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		Item item = new ItemImpl(feed);
		item.setGuid("guid");

		VFSContainer itemContainer = sut.getOrCreateItemContainer(item);

		assertThat(itemContainer).isNotNull();

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void getOrCreateItemContainer_get() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		Item item = new ItemImpl(feed);
		item.setGuid("guid");

		sut.getOrCreateItemContainer(item);
		VFSContainer itemContainer = sut.getOrCreateItemContainer(item);

		assertThat(itemContainer).isNotNull();

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void getOrCreateItemContainer_Item_null() {
		VFSContainer itemContainer = sut.getOrCreateItemContainer(null);

		assertThat(itemContainer).isNull();
	}

	@Test
	public void getOrCreateItemContainer_Feed_null() {
		Item item = new ItemImpl(null);
		item.setGuid("guid");

		VFSContainer itemContainer = sut.getOrCreateItemContainer(item);

		assertThat(itemContainer).isNull();
	}

	@Test
	public void getOrCreateItemContainer_Guid_empty() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		Item item = new ItemImpl(feed);
		item.setGuid("");

		VFSContainer itemContainer = sut.getOrCreateItemContainer(item);

		assertThat(itemContainer).isNull();

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void getOrCreateItemContainer_guid_create() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		String guid = "guid 123";

		VFSContainer itemContainer = sut.getOrCreateItemContainer(feed, guid);

		assertThat(itemContainer).isNotNull();

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void getOrCreateItemContainer_guid_get() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		String guid = "guid 123";

		sut.getOrCreateItemContainer(feed, guid);
		VFSContainer itemContainer = sut.getOrCreateItemContainer(feed, guid);

		assertThat(itemContainer).isNotNull();

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void getOrCreateItemContainer_guid_Feed_null() {
		String guid = "guid 123";

		VFSContainer itemContainer = sut.getOrCreateItemContainer(null, guid);

		assertThat(itemContainer).isNull();
	}

	@Test
	public void getOrCreateItemContainer_guid_Guid_empty() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);

		VFSContainer itemContainer = sut.getOrCreateItemContainer(feed, null);

		assertThat(itemContainer).isNull();

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void getOrCreateItemMediaContainer_create() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		Item item = new ItemImpl(feed);
		item.setGuid("guid");

		VFSContainer itemMediaContainer = sut.getOrCreateItemMediaContainer(item);

		assertThat(itemMediaContainer).isNotNull();

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void getOrCreateItemMediaContainer_get() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		Item item = new ItemImpl(feed);
		item.setGuid("guid");

		sut.getOrCreateItemMediaContainer(item);
		VFSContainer itemMediaContainer = sut.getOrCreateItemMediaContainer(item);

		assertThat(itemMediaContainer).isNotNull();

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void getOrCreateItemMediaContainer_Item_null() {
		VFSContainer itemMediaContainer = sut.getOrCreateItemMediaContainer(null);

		assertThat(itemMediaContainer).isNull();
	}

	@Test
	public void getOrCreateItemMediaContainer_Feed_null() {
		Item item = new ItemImpl(null);
		item.setGuid("guid");

		VFSContainer itemMediaContainer = sut.getOrCreateItemMediaContainer(item);

		assertThat(itemMediaContainer).isNull();
	}

	@Test
	public void getOrCreateItemMediaContainer_Guid_empty() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		Item item = new ItemImpl(feed);
		item.setGuid("");

		VFSContainer itemMediaContainer = sut.getOrCreateItemMediaContainer(item);

		assertThat(itemMediaContainer).isNull();

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void deleteItemContainer() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		Item item1 = new ItemImpl(feed);
		item1.setGuid("guid 1");
		sut.getOrCreateItemContainer(item1);
		Item item2= new ItemImpl(feed);
		item2.setGuid("guid 2");
		sut.getOrCreateItemContainer(item2);

		sut.deleteItemContainer(item1);

		// check if there is only one item container left
		assertThat(sut.getOrCreateFeedItemsContainer(feed).getItems().size()).isEqualTo(1);

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void deleteItemContainer_not_existing() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		Item item1 = new ItemImpl(feed);
		item1.setGuid("guid 1");
		Item item2= new ItemImpl(feed);
		item2.setGuid("guid 2");
		sut.getOrCreateItemContainer(item2);

		sut.deleteItemContainer(item1);

		// check if there is only one item container left
		assertThat(sut.getOrCreateFeedItemsContainer(feed).getItems().size()).isEqualTo(1);

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void deleteItemContainer_null() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		Item item1 = new ItemImpl(feed);
		item1.setGuid("guid 1");
		sut.getOrCreateItemContainer(item1);

		sut.deleteItemContainer(null);

		// check if there is one item container left
		assertThat(sut.getOrCreateFeedItemsContainer(feed).getItems().size()).isEqualTo(1);

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void saveFeedAsXML_new() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		feed.setAuthor("initialAuthor");

		sut.saveFeedAsXML(feed);

		// check if there is one file in the feed container
		assertThat(sut.getOrCreateFeedContainer(feed).getItems().size()).isEqualTo(1);

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void saveFeedAsXML_overwrite() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		feed.setAuthor("initialAuthor");
		sut.saveFeedAsXML(feed);

		feed.setAuthor("secondAuthor");
		sut.saveFeedAsXML(feed);

		// check if there is one file in the feed container
		assertThat(sut.getOrCreateFeedContainer(feed).getItems().size()).isEqualTo(1);

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void saveFeedAsXML_null() {
		sut.saveFeedAsXML(null);

		// no exception
	}

	@Test
	public void loadFeedFromXML_Ores() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		String autor = "autor";
		feed.setAuthor(autor);
		sut.saveFeedAsXML(feed);

		Feed reloaded = sut.loadFeedFromXML(resource);

		assertThat(reloaded).isNotNull();
		assertThat(reloaded.getAuthor()).isEqualTo(autor);

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void loadFeedFromXML_Ores_not_existing() {
		BlogFileResource resource = new BlogFileResource();

		Feed reloaded = sut.loadFeedFromXML(resource);

		assertThat(reloaded).isNull();

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void loadFeedFromXML_shortening_values() {
		StringBuilder sb = new StringBuilder(5000);
		for (int i = 0; i < 5000; i++){
		   sb.append("A");
		}
		String stringWith5000Chars =  sb.toString();
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		feed.setAuthor(stringWith5000Chars);
		feed.setDescription(stringWith5000Chars);
		feed.setTitle(stringWith5000Chars);
		feed.setExternalFeedUrl(stringWith5000Chars);
		feed.setExternalImageURL(stringWith5000Chars);
		sut.saveFeedAsXML(feed);

		Feed reloaded = sut.loadFeedFromXML(resource);

		assertThat(reloaded).isNotNull();
		assertThat(reloaded.getAuthor()).hasSize(255);
		assertThat(reloaded.getTitle()).hasSize(1024);
		assertThat(reloaded.getDescription()).hasSize(4000);
		assertThat(reloaded.getExternalFeedUrl()).isNull();
		assertThat(reloaded.getExternalImageURL()).isNull();
		assertThat(reloaded.getImageName()).isNull();

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void loadFeedFromXML_Path() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		String autor = "autor";
		feed.setAuthor(autor);
		sut.saveFeedAsXML(feed);
		Path feedDir = fileResourceManager.getFileResource(resource).toPath();

		Feed reloaded = FeedManager.getInstance().loadFeedFromXML(feedDir);

		assertThat(reloaded).isNotNull();
		assertThat(reloaded.getAuthor()).isEqualTo(autor);

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void loadFeedFromXML_Path_not_existing() {
		Path feedDir = Paths.get("abc");

		Feed reloaded = FeedManager.getInstance().loadFeedFromXML(feedDir);

		assertThat(reloaded).isNull();
	}
	
	@Test
	public void loadFeedFromXMLFile() throws URISyntaxException {
		URL feedUrl = FeedFileStorgeTest.class.getResource("feed.xml");
		VFSLeaf feedLeaf = new VFSJavaIOFile(feedUrl.toURI());

		Feed loadedFeed = (Feed)FeedFileStorge.fromXML(feedLeaf);

		assertThat(loadedFeed).isNotNull();
		assertThat(loadedFeed.getAuthor()).isEqualTo("jeremy");
		assertThat(loadedFeed.getTitle()).isEqualTo("XStream feed");
		assertThat(loadedFeed.getResourceableTypeName()).isEqualTo("FileResource.BLOG");
	}
	
	@Test
	public void loadExternalFeedFromXMLFile() throws URISyntaxException {
		URL feedUrl = FeedFileStorgeTest.class.getResource("feed_external.xml");
		VFSLeaf feedLeaf = new VFSJavaIOFile(feedUrl.toURI());

		Feed loadedFeed = (Feed)FeedFileStorge.fromXML(feedLeaf);

		assertThat(loadedFeed).isNotNull();
		assertThat(loadedFeed.getTitle()).isEqualTo("Blog OpenOlat");
		assertThat(loadedFeed.isExternal()).isTrue();
		assertThat(loadedFeed.getExternalFeedUrl()).isEqualTo("https://blog.openolat.ch/?feed=rss2");
		assertThat(loadedFeed.getResourceableTypeName()).isEqualTo("FileResource.BLOG");
	}

	@Test
	public void deleteFeedXML() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		feed.setAuthor("initialAuthor");
		sut.saveFeedAsXML(feed);

		sut.deleteFeedXML(feed);

		// check if there is no file in the feed container
		assertThat(sut.getOrCreateFeedContainer(feed).getItems(new VFSSystemItemFilter()).size()).isEqualTo(0);

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void deleteFeedXML_not_existing() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		feed.setAuthor("initialAuthor");

		sut.deleteFeedXML(feed);

		// check if there is no file in the feed container
		assertThat(sut.getOrCreateFeedContainer(feed).getItems().size()).isEqualTo(0);

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void deleteFeedXMLnull() {
		sut.deleteFeedXML(null);

		// no exception
	}

	@Test
	public void saveItemAsXML_new() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		Item item = new ItemImpl(feed);
		item.setGuid("123");
		item.setAuthor("autor");

		sut.saveItemAsXML(item);

		// check if there is one file in the item container
		assertThat(sut.getOrCreateItemContainer(item).getItems().size()).isEqualTo(1);

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void saveItemAsXML_overwrite() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		Item item = new ItemImpl(feed);
		item.setGuid("123");
		item.setAuthor("autor");
		sut.saveItemAsXML(item);

		item.setAuthor("secondAuthor");
		sut.saveItemAsXML(item);

		// check if there is one file in the item container
		assertThat(sut.getOrCreateItemContainer(item).getItems().size()).isEqualTo(1);

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void saveItemAsXML_null() {
		sut.saveItemAsXML(null);

		// no exception
	}

	@Test
	public void loadItemFromXML() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		Item item = new ItemImpl(feed);
		String guid = "guid öüä";
		item.setGuid(guid);
		String autor = "autor";
		item.setAuthor(autor);
		sut.saveItemAsXML(item);
		VFSContainer itemContainer = sut.getOrCreateItemContainer(item);

		Item reloaded = sut.loadItemFromXML(itemContainer);

		assertThat(reloaded).isNotNull();
		assertThat(reloaded.getAuthor()).isEqualTo(autor);

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void loadItemFromXML_not_existing() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		Item item = new ItemImpl(feed);
		String guid = "guid öüä";
		item.setGuid(guid);
		String autor = "autor";
		item.setAuthor(autor);
		VFSContainer itemContainer = sut.getOrCreateItemContainer(item);

		Item reloaded = sut.loadItemFromXML(itemContainer);

		assertThat(reloaded).isNull();

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void loadItemFromXML_null() {
		Item reloaded = sut.loadItemFromXML(null);

		assertThat(reloaded).isNull();
	}

	@Test
	public void loadItemsFromXML() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		Item item1 = new ItemImpl(feed);
		String guid1 = "guid 1";
		item1.setGuid(guid1);
		sut.saveItemAsXML(item1);
		Item item2 = new ItemImpl(feed);
		String guid2 = "guid 2";
		item2.setGuid(guid2);
		sut.saveItemAsXML(item2);
		Item item3 = new ItemImpl(feed);
		String guid3 = "guid 3";
		item3.setGuid(guid3);
		sut.saveItemAsXML(item3);

		List<Item> items = sut.loadItemsFromXML(feed);

		assertThat(items.size()).isEqualTo(3);

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void loadItemsFromXML_missing_XML() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		Item item1 = new ItemImpl(feed);
		String guid1 = "guid 1";
		item1.setGuid(guid1);
		sut.saveItemAsXML(item1);
		Item item2 = new ItemImpl(feed);
		String guid2 = "guid 2";
		item2.setGuid(guid2);
		sut.saveItemAsXML(item2);
		sut.deleteItemXML(item1);

		List<Item> items = sut.loadItemsFromXML(feed);

		assertThat(items.size()).isEqualTo(1);

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void loadItemsFromXML_empty() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);

		List<Item> items = sut.loadItemsFromXML(feed);

		assertThat(items.size()).isEqualTo(0);

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void loadItemsFromXML_null() {
		List<Item> items = sut.loadItemsFromXML(null);

		assertThat(items.size()).isEqualTo(0);
	}

	@Test
	public void loadItemsFromXML_shortening() {
		StringBuffer sb = new StringBuffer(5000);
		for (int i = 0; i < 5000; i++){
		   sb.append("A");
		}
		String stringWith5000Chars =  sb.toString();
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		Item item = new ItemImpl(feed);
		String guid1 = "guid 1";
		item.setGuid(guid1);
		item.setAuthor(stringWith5000Chars);
		item.setExternalLink(stringWith5000Chars);
		item.setTitle(stringWith5000Chars);
		sut.saveItemAsXML(item);

		List<Item> items = sut.loadItemsFromXML(feed);
		Item reloadedItem = items.get(0);

		assertThat(reloadedItem.getAuthor()).hasSize(255);
		assertThat(reloadedItem.getExternalLink()).isNull();
		assertThat(reloadedItem.getTitle()).hasSize(1024);

		fileResourceManager.deleteFileResource(resource);
	}
	
	@Test
	public void loadItemFromXMLFile() throws URISyntaxException {
		URL itemUrl = FeedFileStorgeTest.class.getResource("item_with_enclosure.xml");
		VFSLeaf itemLeaf = new VFSJavaIOFile(itemUrl.toURI());

		Item loadedItem = (Item)FeedFileStorge.fromXML(itemLeaf);

		assertThat(loadedItem).isNotNull();
		assertThat(loadedItem.getTitle()).isEqualTo("Test XStream");
		assertThat(loadedItem.getEnclosure().getFileName()).isEqualTo("demo_movie.mp4");
	}

	@Test
	public void deleteItemXML() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		Item item = new ItemImpl(feed);
		item.setGuid("123");
		item.setAuthor("autor");
		sut.saveItemAsXML(item);

		sut.deleteItemXML(item);

		// check if there is no file in the item container
		assertThat(sut.getOrCreateItemContainer(item).getItems(new VFSSystemItemFilter()).size()).isEqualTo(0);

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void deleteItemXML_not_existing() {
		BlogFileResource resource = new BlogFileResource();
		Feed feed = new FeedImpl(resource);
		Item item = new ItemImpl(feed);
		item.setGuid("123");
		item.setAuthor("autor");

		sut.deleteItemXML(item);

		// check if there is no file in the item container
		assertThat(sut.getOrCreateItemContainer(item).getItems().size()).isEqualTo(0);

		fileResourceManager.deleteFileResource(resource);
	}

	@Test
	public void deleteItemXMLnull() {
		sut.deleteItemXML(null);

		// no exception
	}

}
