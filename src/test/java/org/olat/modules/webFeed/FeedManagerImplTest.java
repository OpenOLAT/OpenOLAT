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
package org.olat.modules.webFeed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.webFeed.managers.FeedManager;
import org.olat.modules.webFeed.models.Feed;
import org.olat.modules.webFeed.models.Item;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * JUnit tests for <code>FeedManager</code> methods.
 * 
 * <P>
 * Initial Date: Feb 16, 2009 <br>
 * 
 * @author Gregor Wassmann
 */
public class FeedManagerImplTest extends OlatTestCase {
	@Autowired
	private FeedManager feedManager;
	private Feed feed;
	private static final String PODCAST_TITLE = "My Test Feed";

	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before
	public void setup() {
		// Create a feed that can be read, updated or deleted.
		OLATResourceable podcastResource = feedManager.createPodcastResource();
		feed = feedManager.getFeed(podcastResource);
		feed.setTitle(PODCAST_TITLE);
		feedManager.updateFeedMetadata(feed);

		// Add an episode
		// A feed can only be edited when it is an internal feed (meaning that
		// it is made within OLAT). Obviously, external feeds cannot be changed.
		Item item = new Item();
		item.setTitle("My Test Item");
		feed = feedManager.updateFeedMode(Boolean.FALSE, feed);
		feed = feedManager.addItem(item, null, feed);
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@After
	public void tearDown() {
		 if (feed != null) feedManager.delete(feed);
	}
	
	@Test
	public void should_service_present() {
		Assert.assertNotNull(feedManager);
		Assert.assertNotNull(feed);
	}

	/**
	 * Test method create
	 */
	@Test
	public void testCreatePodcast() {
		OLATResourceable podcastResource = feedManager.createPodcastResource();
		Feed newPodcast = feedManager.getFeed(podcastResource);
		assertNotNull(newPodcast);
		assertNotNull(newPodcast.getId());

		// Has a feed folder been created?
		VFSContainer podcastContainer = feedManager.getFeedContainer(newPodcast);
		assertNotNull(podcastContainer);
		feedManager.delete(newPodcast);
	}

	/**
	 * Test method read
	 */
	@Test public void testReadPodcast() {
		Feed readPodcast = feedManager.getFeed(feed);
		assertNotNull(readPodcast);
		assertNotNull(readPodcast.getId());
		assertEquals(PODCAST_TITLE, readPodcast.getTitle());
	}

	/**
	 * Test method update
	 */
	@Test public void testUpdate() {
		feed.setTitle("The title changed");
		feedManager.updateFeedMetadata(feed);
		// re-read for assertion
		Feed readPodcast = feedManager.getFeed(feed);
		assertEquals(feed.getTitle(), readPodcast.getTitle());
	}

	/**
	 * Test method delete
	 */
	@Test public void testDelete() {
		// int initialCount = feedManager.podcastCount();
		feedManager.delete(feed);
		// int newCount = feedManager.podcastCount();
		// assertEquals(initialCount - 1, newCount);
		// assertNull(feed);
	}

	/**
	 * Test method add
	 */
	@Test public void testAdd() {
		Item newEpisode = new Item();
		newEpisode.setGuid(CodeHelper.getGlobalForeverUniqueID()); 
		newEpisode.setTitle("This is my new Item");
		// Count episodes before
		int initialCount = feed.getItems().size();
		feedManager.addItem(newEpisode, null, feed);
		// re-read feed and count episodes
		feed = feedManager.getFeed(feed);
		int newCount = feed.getItems().size();
		// Compare
		assertEquals(initialCount + 1, newCount);
	}

	/**
	 * Test method remove
	 */
	@Test public void testRemove() {
		Item item = feed.getItems().get(0);
		// Count episodes before
		int initialCount = feed.getItems().size();
		feedManager.remove(item, feed);
		// re-read feed and count episodes after adding a new one
		feed = feedManager.getFeed(feed);
		int newCount = feed.getItems().size();
		// Compare
		assertEquals(initialCount - 1, newCount);
	}
}
