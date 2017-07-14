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
	private RepositoryManager repostoryManager;
	@Mock
	OLATResource resourceDummy;
	@Mock
	Feed feedDummy;
	@Mock
	Item itemDummy;
	@Mock
	Syncer syncerDummy;
	
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
		ReflectionTestUtils.setField(sut, "repositoryManager", repostoryManager);
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
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(feedDummy);
		when(feedDAOMock.createFeed(any(Feed.class))).thenReturn(feedDummy);	
		when(feedDummy.isExternal()).thenReturn(false);
		
		sut.importFeedFromXML(resourceDummy, true);
		
		verify(feedDAOMock).createFeed(any(Feed.class));
	}
	
	@Test
	public void importShouldSaveItemsToDatabase() {
		List<Item> items = java.util.Arrays.asList(itemDummy, itemDummy, itemDummy);

		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedDummy);		
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(feedDummy);
		when(feedFileStorageMock.loadItemsFromXML(resourceDummy)).thenReturn(items);
		
		sut.importFeedFromXML(resourceDummy, true);
		
		verify(itemDAOMock, times(3)).createItem(any(Feed.class), any(Item.class));
	}
	
	@Test
	public void importShouldNotCreateItemsIfNoXmlFilesArePresent() {
		List<Item> emtpyList = new ArrayList<>();

		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedDummy);		
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(feedDummy);
		when(feedFileStorageMock.loadItemsFromXML(resourceDummy)).thenReturn(emtpyList);
		
		sut.importFeedFromXML(resourceDummy, true);

		verifyZeroInteractions(itemDAOMock);
	}
	
	@Test
	public void importShoulDeleteAuthorKey() {
		List<Item> items = java.util.Arrays.asList(itemDummy);

		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedDummy);		
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(feedDummy);
		when(feedFileStorageMock.loadItemsFromXML(resourceDummy)).thenReturn(items);
		when(itemDAOMock.loadItemByGuid(any(Long.class), any(String.class))).thenReturn(itemDummy);
		
		sut.importFeedFromXML(resourceDummy, true);
		
		verify(itemDummy).setAuthorKey(null);
	}
	
	@Test
	public void importShoulDeleteModifierKey() {
		List<Item> items = java.util.Arrays.asList(itemDummy);

		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedDummy);		
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(feedDummy);
		when(feedFileStorageMock.loadItemsFromXML(resourceDummy)).thenReturn(items);
		when(itemDAOMock.loadItemByGuid(any(Long.class), any(String.class))).thenReturn(itemDummy);
		
		sut.importFeedFromXML(resourceDummy, true);
		
		verify(itemDummy).setModifierKey(null);
	}
	
	@Test
	public void importShoulDeleteFeedXmlFile() {
		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedDummy);		
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(feedDummy);

		sut.importFeedFromXML(resourceDummy, true);
		
		verify(feedFileStorageMock).deleteFeedXML(feedDummy);
	}
	
	@Test
	public void importShoulDeleteItemsXmlFiles() {
		List<Item> items = java.util.Arrays.asList(itemDummy, itemDummy, itemDummy);

		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedDummy);		
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(feedDummy);
		when(feedFileStorageMock.loadItemsFromXML(resourceDummy)).thenReturn(items);
		
		sut.importFeedFromXML(resourceDummy, true);

		verify(feedFileStorageMock, times(3)).deleteItemXML(itemDummy);
	}
	
	@Test
	public void importShoulLoadItemsOfExternalFeeds() {
		List<Item> items = java.util.Arrays.asList(itemDummy);

		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedDummy);		
		when(feedFileStorageMock.loadFeedFromXML(any(OLATResource.class))).thenReturn(feedDummy);
		when(feedFileStorageMock.loadItemsFromXML(resourceDummy)).thenReturn(items);
		when(feedDummy.isExternal()).thenReturn(true);
		
		sut.importFeedFromXML(resourceDummy, true);

		verify(feedFetcherMock).fetchFeed(feedDummy);
		verify(feedFetcherMock).fetchItems(feedDummy);
	}
	
	@Test
	public void exportShouldWriteFeedToXmlFile() {
		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedDummy);	

		sut.getFeedArchive(resourceDummy);
		
		verify(feedFileStorageMock).saveFeedAsXML(feedDummy);
	}
	
	@Test
	public void exportShouldWriteItemsToXmlFiles() {
		List<Item> threeItems = java.util.Arrays.asList(itemDummy, itemDummy, itemDummy);
		
		when(itemDAOMock.loadItems(any(Feed.class))).thenReturn(threeItems);
		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedDummy);

		sut.getFeedArchive(resourceDummy);
		
		verify(feedFileStorageMock, times(3)).saveItemAsXML(itemDummy);
	}
	
	@Test
	public void exportShoulDeleteFeedXmlFile() {
		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedDummy);	

		sut.getFeedArchive(resourceDummy);
		
		verify(feedFileStorageMock).deleteFeedXML(feedDummy);
	}
	
	@Test
	public void exportShoulDeleteItemsXmlFiles() {
		List<Item> threeItems = java.util.Arrays.asList(itemDummy, itemDummy, itemDummy);

		when(itemDAOMock.loadItems(any(Feed.class))).thenReturn(threeItems);
		when(feedDAOMock.loadFeed(any(OLATResource.class))).thenReturn(feedDummy);
		
		sut.getFeedArchive(resourceDummy);

		verify(feedFileStorageMock, times(3)).deleteItemXML(itemDummy);
	}
	
	@Test
	public void enrichFeedFromrepositoryEntryShouldTransferAtributes() {
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
}
