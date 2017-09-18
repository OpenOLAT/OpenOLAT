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
	private static final Long ITEM_KEY = 6L;
	private static Identity IGNORE_NEWS_FOR_NOBODY = null;
	private static boolean SEND_NO_EVENTS = false;

	@Mock
	private OLATResource resourceDummy;
	@Mock
	private Feed feedMock;
	@Mock
	private Item itemMock;
	@Mock
	private FileElement fileElementDummy;
	@Mock
	private Syncer syncerDummy;

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
	public void injectNonSpringManagers() {
		MockitoAnnotations.initMocks(this);
		when(coordinaterManagerMock.getCoordinator()).thenReturn(coordinaterMock);
		when(coordinaterMock.getSyncer()).thenReturn(syncerDummy);
		sut = new FeedManagerImpl(resourceManagerMock, fileResourceManagerMock, coordinaterManagerMock);
		feedDAOMock = mock(FeedDAO.class);
		ReflectionTestUtils.setField(sut, "feedDAO", feedDAOMock);
		ReflectionTestUtils.setField(sut, "itemDAO", itemDAOMock);
		ReflectionTestUtils.setField(sut, "feedFileStorage", feedFileStorageMock);
		ReflectionTestUtils.setField(sut, "externalFeedFetcher", feedFetcherMock);
		ReflectionTestUtils.setField(sut, "repositoryManager", repositoryManager);
		ReflectionTestUtils.setField(sut, "notificationsManager", notificationsManagerMock);

		when(feedMock.getKey()).thenReturn(FEED_KEY);
		when(feedMock.getResourceableTypeName()).thenReturn(RESOURCEABLE_TYPE_NAME);
		when(feedMock.getResourceableId()).thenReturn(RESOURCABLE_ID);
		when(feedMock.isInternal()).thenReturn(true);

		when(itemMock.getKey()).thenReturn(ITEM_KEY);
		when(itemMock.getFeed()).thenReturn(feedMock);
	}

	@Test
	public void shouldSaveItemWhenItemCreated() {
		when(feedDAOMock.loadFeed(FEED_KEY)).thenReturn(feedMock);

		sut.createItem(feedMock, itemMock, fileElementDummy);

		verify(itemDAOMock).createItem(feedMock, itemMock);
	}

	@Test
	public void shouldOnlyAllowToCreateItemIfFeedIsInternal() {
		when(feedDAOMock.loadFeed(FEED_KEY)).thenReturn(feedMock);
		when(feedMock.isInternal()).thenReturn(false);

		Feed feedOfCreatedItem = sut.createItem(null, itemMock, fileElementDummy);

		assertThat(feedOfCreatedItem).isNull();
		verify(itemDAOMock, never()).createItem(feedMock, itemMock);
	}

	@Test
	public void shouldSaveItemWhenItemUpdated() {
		when(itemDAOMock.loadItem(ITEM_KEY)).thenReturn(itemMock);
		when(itemDAOMock.updateItem(itemMock)).thenReturn(itemMock);

		sut.updateItem(itemMock, fileElementDummy);

		verify(itemDAOMock).updateItem(itemMock);
	}

	@Test
	public void shouldNotSaveItemIfItemToUpdateDoesNotExists() {
		when(itemDAOMock.loadItem(ITEM_KEY)).thenReturn(null);
		when(itemDAOMock.updateItem(itemMock)).thenReturn(itemMock);

		sut.updateItem(itemMock, fileElementDummy);

		verify(itemDAOMock, never()).updateItem(itemMock);
	}

	@Test
	public void shouldMarkPublisherNewsWhenItemCreated() {
		when(feedDAOMock.loadFeed(FEED_KEY)).thenReturn(feedMock);
		when(feedDAOMock.updateFeed(feedMock)).thenReturn(feedMock);

		sut.createItem(feedMock, itemMock, fileElementDummy);

		verify(notificationsManagerMock).markPublisherNews(
				RESOURCEABLE_TYPE_NAME,
				RESOURCABLE_ID.toString(),
				IGNORE_NEWS_FOR_NOBODY,
				SEND_NO_EVENTS);
	}

	@Test
	public void shouldMarkPublisherNewsWhenItemUpdated() {
		when(itemDAOMock.loadItem(ITEM_KEY)).thenReturn(itemMock);
		when(itemDAOMock.updateItem(itemMock)).thenReturn(itemMock);

		sut.updateItem(itemMock, fileElementDummy);

		verify(notificationsManagerMock).markPublisherNews(
				RESOURCEABLE_TYPE_NAME,
				RESOURCABLE_ID.toString(),
				IGNORE_NEWS_FOR_NOBODY,
				SEND_NO_EVENTS);
	}

	@Test
	public void importShouldNothingDoIfNoXmlFileIsPresent() {
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(null);

		sut.importFeedFromXML(any(OLATResource.class), true);

		verifyZeroInteractions(feedDAOMock);
		verifyZeroInteractions(itemDAOMock);
	}

	@Test
	public void importShouldSaveFeedToDatabase() {
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(feedMock);
		when(feedDAOMock.createFeed(any(Feed.class))).thenReturn(feedMock);
		when(feedMock.isExternal()).thenReturn(false);

		sut.importFeedFromXML(resourceDummy, true);

		verify(feedDAOMock).createFeed(any(Feed.class));
	}

	@Test
	public void importShouldSaveItemsToDatabase() {
		List<Item> items = java.util.Arrays.asList(itemMock, itemMock, itemMock);

		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedMock);
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(feedMock);
		when(feedFileStorageMock.loadItemsFromXML(resourceDummy)).thenReturn(items);

		sut.importFeedFromXML(resourceDummy, true);

		verify(itemDAOMock, times(3)).createItem(any(Feed.class), any(Item.class));
	}

	@Test
	public void importShouldNotCreateItemsIfNoXmlFilesArePresent() {
		List<Item> emtpyList = new ArrayList<>();

		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedMock);
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(feedMock);
		when(feedFileStorageMock.loadItemsFromXML(resourceDummy)).thenReturn(emtpyList);

		sut.importFeedFromXML(resourceDummy, true);

		verifyZeroInteractions(itemDAOMock);
	}

	@Test
	public void importShoulDeleteAuthorKey() {
		List<Item> items = java.util.Arrays.asList(itemMock);

		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedMock);
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(feedMock);
		when(feedFileStorageMock.loadItemsFromXML(resourceDummy)).thenReturn(items);
		when(itemDAOMock.loadItemByGuid(any(Long.class), any(String.class))).thenReturn(itemMock);

		sut.importFeedFromXML(resourceDummy, true);

		verify(itemMock).setAuthorKey(null);
	}

	@Test
	public void importShoulDeleteModifierKey() {
		List<Item> items = java.util.Arrays.asList(itemMock);

		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedMock);
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(feedMock);
		when(feedFileStorageMock.loadItemsFromXML(resourceDummy)).thenReturn(items);
		when(itemDAOMock.loadItemByGuid(any(Long.class), any(String.class))).thenReturn(itemMock);

		sut.importFeedFromXML(resourceDummy, true);

		verify(itemMock).setModifierKey(null);
	}

	@Test
	public void importShoulDeleteFeedXmlFile() {
		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedMock);
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(feedMock);

		sut.importFeedFromXML(resourceDummy, true);

		verify(feedFileStorageMock).deleteFeedXML(feedMock);
	}

	@Test
	public void importShoulDeleteItemsXmlFiles() {
		List<Item> items = java.util.Arrays.asList(itemMock, itemMock, itemMock);

		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedMock);
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(feedMock);
		when(feedFileStorageMock.loadItemsFromXML(resourceDummy)).thenReturn(items);

		sut.importFeedFromXML(resourceDummy, true);

		verify(feedFileStorageMock, times(3)).deleteItemXML(itemMock);
	}

	@Test
	public void importShoulLoadItemsOfExternalFeeds() {
		List<Item> items = java.util.Arrays.asList(itemMock);

		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedMock);
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(feedMock);
		when(feedFileStorageMock.loadItemsFromXML(resourceDummy)).thenReturn(items);
		when(feedMock.isExternal()).thenReturn(true);

		sut.importFeedFromXML(resourceDummy, true);

		verify(feedFetcherMock).fetchFeed(feedMock);
		verify(feedFetcherMock).fetchItems(feedMock);
	}

	@Test
	public void exportShouldWriteFeedToXmlFile() {
		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedMock);

		sut.getFeedArchive(resourceDummy);

		verify(feedFileStorageMock).saveFeedAsXML(feedMock);
	}

	@Test
	public void exportShouldWriteItemsToXmlFiles() {
		List<Item> threeItems = java.util.Arrays.asList(itemMock, itemMock, itemMock);

		when(itemDAOMock.loadItems(any(Feed.class))).thenReturn(threeItems);
		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedMock);

		sut.getFeedArchive(resourceDummy);

		verify(feedFileStorageMock, times(3)).saveItemAsXML(itemMock);
	}

	@Test
	public void exportShoulDeleteFeedXmlFile() {
		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedMock);

		sut.getFeedArchive(resourceDummy);

		verify(feedFileStorageMock).deleteFeedXML(feedMock);
	}

	@Test
	public void exportShoulDeleteItemsXmlFiles() {
		List<Item> threeItems = java.util.Arrays.asList(itemMock, itemMock, itemMock);

		when(itemDAOMock.loadItems(any(Feed.class))).thenReturn(threeItems);
		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedMock);

		sut.getFeedArchive(resourceDummy);

		verify(feedFileStorageMock, times(3)).deleteItemXML(itemMock);
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

		Feed enrichedFeed = sut.enrichFeedByRepositoryEntry(feed, entry);

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

		Feed enrichedFeed = sut.enrichFeedByRepositoryEntry(feed, null);

		assertThat(enrichedFeed).isEqualTo(feed);
		assertThat(enrichedFeed.getTitle()).isEqualTo(title);
		assertThat(enrichedFeed.getDescription()).isEqualTo(description);
		assertThat(enrichedFeed.getAuthor()).isEqualTo(authors);
	}

	@Test
	public void enrichFeedFromRepositoryEntryShouldReturnNullIfFeedIsNull() {
		RepositoryEntry entry = new RepositoryEntry();

		Feed enrichedFeed = sut.enrichFeedByRepositoryEntry(null, entry);

		assertThat(enrichedFeed).isNull();
	}
}
