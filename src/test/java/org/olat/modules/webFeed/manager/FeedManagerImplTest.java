/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
* <p>
*/
package org.olat.modules.webFeed.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.id.Identity;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.Syncer;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.webFeed.ExternalFeedFetcher;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.model.FeedImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * Initial date: 12.05.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FeedManagerImplTest {

	private static final Long FEED_KEY = 4L;
	private static final String RESOURCEABLE_TYPE_NAME = "resurcable type name";
	private static final Long RESOURCABLE_ID = 5L;
	private static final String EXTERNAL_URL_OLD = "oldExternalURL";
	private static final String EXTERNAL_URL_NEW = "newExternalURL";
	private static final Long ITEM_KEY = 6L;
	private static Identity IGNORE_NEWS_FOR_NOBODY = null;
	private static boolean SEND_NO_EVENTS = false;

	@Mock
	private OLATResource resourceDummy;
	@Mock
	private Feed internatFeedMock;
	private Feed externalFeed;
	@Mock
	private Item internalItemMock;
	@Mock
	private FileElement fileElementDummy;
	@Mock
	private Syncer syncerDummy;
	
	@Mock
	private DB dbInstanceMock;
	@Mock
	private FeedDAO feedDAOMock;
	@Mock
	private ItemDAO itemDAOMock;
	@Mock
	private FeedFileStorge feedFileStorageMock;
	@Mock
	private ExternalFeedFetcher  feedFetcherMock;
	@Mock
	private OLATResourceManager resourceManagerMock;
	@Mock
	private FileResourceManager fileResourceManagerMock;
	@Mock
	private CoordinatorManager coordinaterManagerMock;
	@Mock
	private Coordinator coordinaterMock;
	@Mock
	private RepositoryManager repositoryManager;
	@Mock
	private NotificationsManager notificationsManagerMock;

	private FeedManagerImpl sut;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(coordinaterManagerMock.getCoordinator()).thenReturn(coordinaterMock);
		when(coordinaterMock.getSyncer()).thenReturn(syncerDummy);
		sut = new FeedManagerImpl(resourceManagerMock, fileResourceManagerMock, coordinaterManagerMock);
		feedDAOMock = mock(FeedDAO.class);
		
		ReflectionTestUtils.setField(sut, "dbInstance", dbInstanceMock);
		ReflectionTestUtils.setField(sut, "feedDAO", feedDAOMock);
		ReflectionTestUtils.setField(sut, "itemDAO", itemDAOMock);
		ReflectionTestUtils.setField(sut, "feedFileStorage", feedFileStorageMock);
		ReflectionTestUtils.setField(sut, "externalFeedFetcher", feedFetcherMock);
		ReflectionTestUtils.setField(sut, "repositoryManager", repositoryManager);
		ReflectionTestUtils.setField(sut, "notificationsManager", notificationsManagerMock);

		when(internatFeedMock.getKey()).thenReturn(FEED_KEY);
		when(internatFeedMock.getResourceableTypeName()).thenReturn(RESOURCEABLE_TYPE_NAME);
		when(internatFeedMock.getResourceableId()).thenReturn(RESOURCABLE_ID);
		when(internatFeedMock.isInternal()).thenReturn(true);

		when(internalItemMock.getKey()).thenReturn(ITEM_KEY);
		when(internalItemMock.getFeed()).thenReturn(internatFeedMock);

		externalFeed = new FeedImpl(resourceDummy);
		externalFeed.setKey(FEED_KEY);
		externalFeed.setExternal(true);
		externalFeed.setExternalFeedUrl(EXTERNAL_URL_OLD);
	}

	@Test
	public void shouldUpdateFeedWhenExternalUrlChanged() {
		when(feedDAOMock.loadFeed(externalFeed)).thenReturn(externalFeed);
		when(feedDAOMock.updateFeed(externalFeed)).thenReturn(externalFeed);

		Feed updatedFeed = sut.updateExternalFeedUrl(externalFeed, EXTERNAL_URL_NEW);

		assertThat(updatedFeed.getExternalFeedUrl()).isEqualTo(EXTERNAL_URL_NEW);
		assertThat(updatedFeed.getLastModified()).isNotNull();
		verify(feedDAOMock).updateFeed(externalFeed);
	}

	@Test
	public void shouldNotUpdateFeedWhenExternalUrlIsTheSame() {
		when(feedDAOMock.loadFeed(externalFeed)).thenReturn(externalFeed);
		when(feedDAOMock.updateFeed(externalFeed)).thenReturn(externalFeed);

		Feed updatedFeed = sut.updateExternalFeedUrl(externalFeed, EXTERNAL_URL_OLD);

		assertThat(updatedFeed.getExternalFeedUrl()).isEqualTo(EXTERNAL_URL_OLD);
		assertThat(updatedFeed.getLastModified()).isNotNull();
		verify(feedDAOMock).updateFeed(externalFeed);
	}

	@Test
	public void shouldDeleteAllItemsIfExternalUrlChanged() {
		when(feedDAOMock.loadFeed(externalFeed)).thenReturn(externalFeed);
		when(feedDAOMock.updateFeed(externalFeed)).thenReturn(externalFeed);

		sut.updateExternalFeedUrl(externalFeed, EXTERNAL_URL_NEW);

		verify(itemDAOMock).removeItems(externalFeed);
	}

	@Test
	public void shouldNotDeleteItemsWhenExternaUrlIsTheSame() {
		when(feedDAOMock.loadFeed(externalFeed)).thenReturn(externalFeed);
		when(feedDAOMock.updateFeed(externalFeed)).thenReturn(externalFeed);

		sut.updateExternalFeedUrl(externalFeed, EXTERNAL_URL_OLD);

		verify(itemDAOMock, never()).removeItems(externalFeed);
	}

	@Test
	public void shouldFetchItemsWhenExternalUrlChanged() {
		when(feedDAOMock.loadFeed(externalFeed)).thenReturn(externalFeed);
		when(feedDAOMock.updateFeed(externalFeed)).thenReturn(externalFeed);

		sut.updateExternalFeedUrl(externalFeed, EXTERNAL_URL_NEW);

		verify(feedFetcherMock).fetchFeed(externalFeed);
		verify(feedFetcherMock).fetchItems(externalFeed);
	}

	@Test
	public void shouldSaveItemWhenItemCreated() {
		when(feedDAOMock.loadFeed(FEED_KEY)).thenReturn(internatFeedMock);

		sut.createItem(internatFeedMock, internalItemMock, fileElementDummy);

		verify(itemDAOMock).createItem(internatFeedMock, internalItemMock);
	}

	@Test
	public void shouldOnlyAllowToCreateItemIfFeedIsInternal() {
		when(feedDAOMock.loadFeed(FEED_KEY)).thenReturn(internatFeedMock);
		when(internatFeedMock.isInternal()).thenReturn(false);

		Feed feedOfCreatedItem = sut.createItem(null, internalItemMock, fileElementDummy);

		assertThat(feedOfCreatedItem).isNull();
		verify(itemDAOMock, never()).createItem(internatFeedMock, internalItemMock);
	}

	@Test
	public void shouldSaveItemWhenItemUpdated() {
		when(itemDAOMock.loadItem(ITEM_KEY)).thenReturn(internalItemMock);
		when(itemDAOMock.updateItem(internalItemMock)).thenReturn(internalItemMock);

		sut.updateItem(internalItemMock, fileElementDummy);

		verify(itemDAOMock).updateItem(internalItemMock);
	}

	@Test
	public void shouldNotSaveItemIfItemToUpdateDoesNotExists() {
		when(itemDAOMock.loadItem(ITEM_KEY)).thenReturn(null);
		when(itemDAOMock.updateItem(internalItemMock)).thenReturn(internalItemMock);

		sut.updateItem(internalItemMock, fileElementDummy);

		verify(itemDAOMock, never()).updateItem(internalItemMock);
	}

	@Test
	public void shouldMarkPublisherNewsWhenItemCreated() {
		when(feedDAOMock.loadFeed(FEED_KEY)).thenReturn(internatFeedMock);
		when(feedDAOMock.updateFeed(internatFeedMock)).thenReturn(internatFeedMock);

		sut.createItem(internatFeedMock, internalItemMock, fileElementDummy);

		verify(notificationsManagerMock).markPublisherNews(
				RESOURCEABLE_TYPE_NAME,
				RESOURCABLE_ID.toString(),
				IGNORE_NEWS_FOR_NOBODY,
				SEND_NO_EVENTS);
	}

	@Test
	public void shouldMarkPublisherNewsWhenItemUpdated() {
		when(itemDAOMock.loadItem(ITEM_KEY)).thenReturn(internalItemMock);
		when(itemDAOMock.updateItem(internalItemMock)).thenReturn(internalItemMock);

		sut.updateItem(internalItemMock, fileElementDummy);

		verify(notificationsManagerMock).markPublisherNews(
				RESOURCEABLE_TYPE_NAME,
				RESOURCABLE_ID.toString(),
				IGNORE_NEWS_FOR_NOBODY,
				SEND_NO_EVENTS);
	}

	@Test
	public void importShouldSaveFeedToDatabase() {
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(internatFeedMock);
		when(feedDAOMock.createFeed(any(Feed.class))).thenReturn(internatFeedMock);
		when(internatFeedMock.isExternal()).thenReturn(false);

		sut.importFeedFromXML(resourceDummy, true);

		verify(feedDAOMock).createFeed(any(Feed.class));
	}

	@Test
	public void importShouldSaveItemsToDatabase() {
		List<Item> items = java.util.Arrays.asList(internalItemMock, internalItemMock, internalItemMock);

		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(internatFeedMock);
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(internatFeedMock);
		when(feedFileStorageMock.loadItemsFromXML(resourceDummy)).thenReturn(items);

		sut.importFeedFromXML(resourceDummy, true);

		verify(itemDAOMock, times(3)).createItem(any(Feed.class), any(Item.class));
	}

	@Test
	public void importShouldNotCreateItemsIfNoXmlFilesArePresent() {
		List<Item> emptyList = new ArrayList<>();

		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(internatFeedMock);
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(internatFeedMock);
		when(feedFileStorageMock.loadItemsFromXML(resourceDummy)).thenReturn(emptyList);

		sut.importFeedFromXML(resourceDummy, true);

		verifyZeroInteractions(itemDAOMock);
	}

	@Test
	public void importShoulDeleteAuthorKey() {
		List<Item> items = java.util.Arrays.asList(internalItemMock);

		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(internatFeedMock);
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(internatFeedMock);
		when(feedFileStorageMock.loadItemsFromXML(resourceDummy)).thenReturn(items);
		when(itemDAOMock.loadItemByGuid(any(Long.class), any(String.class))).thenReturn(internalItemMock);

		sut.importFeedFromXML(resourceDummy, true);

		verify(internalItemMock).setAuthorKey(null);
	}

	@Test
	public void importShoulDeleteModifierKey() {
		List<Item> items = java.util.Arrays.asList(internalItemMock);

		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(internatFeedMock);
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(internatFeedMock);
		when(feedFileStorageMock.loadItemsFromXML(resourceDummy)).thenReturn(items);
		when(itemDAOMock.loadItemByGuid(any(Long.class), any(String.class))).thenReturn(internalItemMock);

		sut.importFeedFromXML(resourceDummy, true);

		verify(internalItemMock).setModifierKey(null);
	}

	@Test
	public void importShoulDeleteFeedXmlFile() {
		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(internatFeedMock);
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(internatFeedMock);

		sut.importFeedFromXML(resourceDummy, true);

		verify(feedFileStorageMock).deleteFeedXML(internatFeedMock);
	}

	@Test
	public void importShoulDeleteItemsXmlFiles() {
		List<Item> items = java.util.Arrays.asList(internalItemMock, internalItemMock, internalItemMock);

		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(internatFeedMock);
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(internatFeedMock);
		when(feedFileStorageMock.loadItemsFromXML(resourceDummy)).thenReturn(items);

		sut.importFeedFromXML(resourceDummy, true);

		verify(feedFileStorageMock, times(3)).deleteItemXML(internalItemMock);
	}

	@Test
	public void importShoulLoadItemsOfExternalFeeds() {
		List<Item> items = java.util.Arrays.asList(internalItemMock);

		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(internatFeedMock);
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(internatFeedMock);
		when(feedFileStorageMock.loadItemsFromXML(resourceDummy)).thenReturn(items);
		when(internatFeedMock.isExternal()).thenReturn(true);

		sut.importFeedFromXML(resourceDummy, true);

		verify(feedFetcherMock).fetchFeed(internatFeedMock);
		verify(feedFetcherMock).fetchItems(internatFeedMock);
	}

	@Test
	public void exportShouldWriteFeedToXmlFile() {
		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(internatFeedMock);

		sut.getFeedArchive(resourceDummy);

		verify(feedFileStorageMock).saveFeedAsXML(internatFeedMock);
	}

	@Test
	public void exportShouldWriteItemsToXmlFiles() {
		List<Item> threeItems = java.util.Arrays.asList(internalItemMock, internalItemMock, internalItemMock);

		when(itemDAOMock.loadItems(any(Feed.class), isNull())).thenReturn(threeItems);
		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(internatFeedMock);

		sut.getFeedArchive(resourceDummy);

		verify(feedFileStorageMock, times(3)).saveItemAsXML(internalItemMock);
	}

	@Test
	public void exportShoulDeleteFeedXmlFile() {
		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(internatFeedMock);

		sut.getFeedArchive(resourceDummy);

		verify(feedFileStorageMock).deleteFeedXML(internatFeedMock);
	}

	@Test
	public void exportShoulDeleteItemsXmlFiles() {
		List<Item> threeItems = java.util.Arrays.asList(internalItemMock, internalItemMock, internalItemMock);

		when(itemDAOMock.loadItems(any(Feed.class), isNull())).thenReturn(threeItems);
		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(internatFeedMock);

		sut.getFeedArchive(resourceDummy);

		verify(feedFileStorageMock, times(3)).deleteItemXML(internalItemMock);
	}


	@Test
	public void enrichFeedFromRepositoryEntryShouldTransferAtributes() {
		Feed feed = new FeedImpl(resourceDummy);
		RepositoryEntry entry = new RepositoryEntry();
		String title = "Title";
		entry.setDisplayname(title);
		String description = "Description";
		entry.setDescription(description);
		String authors = "Author";
		entry.setAuthors(authors);

		Feed enrichedFeed = sut.enrichFeedByRepositoryEntry(feed, entry, null);

		assertThat(enrichedFeed.getTitle()).isEqualTo(title);
		assertThat(enrichedFeed.getDescription()).isEqualTo(description);
		assertThat(enrichedFeed.getAuthor()).isEqualTo(authors);
	}


	@Test
	public void enrichFeedFromRepositoryEntryShouldReturnUnchangedFeedIfRepositoryIsNull() {
		Feed feed = new FeedImpl(resourceDummy);
		String title = "Title";
		feed.setTitle(title);
		String description = "Description";
		feed.setDescription(description);
		String authors = "Author";
		feed.setAuthor(authors);

		Feed enrichedFeed = sut.enrichFeedByRepositoryEntry(feed, null, null);

		assertThat(enrichedFeed).isEqualTo(feed);
		assertThat(enrichedFeed.getTitle()).isEqualTo(title);
		assertThat(enrichedFeed.getDescription()).isEqualTo(description);
		assertThat(enrichedFeed.getAuthor()).isEqualTo(authors);
	}

	@Test
	public void enrichFeedFromRepositoryEntryShouldReturnNullIfFeedIsNull() {
		RepositoryEntry entry = new RepositoryEntry();

		Feed enrichedFeed = sut.enrichFeedByRepositoryEntry(null, entry, null);

		assertThat(enrichedFeed).isNull();
	}

}
