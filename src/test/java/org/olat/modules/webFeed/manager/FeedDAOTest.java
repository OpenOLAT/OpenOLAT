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

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.model.FeedImpl;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.05.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FeedDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private FeedDAO feedDao;
	
	@Test
	public void createFeed_ores() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		
		Feed feed = feedDao.createFeedForResourcable(resource);
		Assert.assertNotNull(feed);
		dbInstance.commitAndCloseSession();

		//check values
		Assert.assertNotNull(feed.getKey());
		Assert.assertNotNull(feed.getCreationDate());
		Assert.assertNotNull(feed.getLastModified());
		Assert.assertEquals(resource.getResourceableId(), feed.getResourceableId());
		Assert.assertEquals(resource.getResourceableTypeName(), feed.getResourceableTypeName());
	}
	
	@Test
	public void createFeed_feed() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed tempFeed = new FeedImpl(resource);
		
		Feed feed = feedDao.createFeed(tempFeed);
		Assert.assertNotNull(feed);
		dbInstance.commitAndCloseSession();

		//check values
		Assert.assertNotNull(feed.getKey());
		Assert.assertNotNull(feed.getCreationDate());
		Assert.assertNotNull(feed.getLastModified());
		Assert.assertEquals(resource.getResourceableId(), feed.getResourceableId());
		Assert.assertEquals(resource.getResourceableTypeName(), feed.getResourceableTypeName());
	}
	
	@Test
	public void createFeed_feed_keepDates() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed tempFeed = new FeedImpl(resource);
		Date created = new GregorianCalendar(2000, 1, 1).getTime();
		tempFeed.setCreationDate(created);
		Date modified = new GregorianCalendar(2000, 2, 2).getTime();
		tempFeed.setLastModified(modified);
		
		Feed feed = feedDao.createFeed(tempFeed);
		dbInstance.commitAndCloseSession();

		//check values
		assertThat(feed.getCreationDate()).isCloseTo(created, 1000);
		assertThat(feed.getLastModified()).isCloseTo(modified, 1000);
	}
	
	@Test
	public void createFeed_null() {
		Feed feed = feedDao.createFeed(null);
		dbInstance.commitAndCloseSession();

		Assert.assertNull(feed);
	}
	
	@Test
	public void copyFeed() {
		OLATResource source = JunitTestHelper.createRandomResource();
		OLATResource target = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(source);
		dbInstance.commitAndCloseSession();
		
		Feed copy = feedDao.copyFeed(feed, target);
		dbInstance.commitAndCloseSession();

		assertThat(copy.getKey()).isNotEqualTo(feed.getKey());
		assertThat(copy.getResourceableId()).isNotEqualTo(feed.getResourceableId());
		assertThat(copy.getCreationDate()).isCloseTo(feed.getCreationDate(), 1000);
		assertThat(copy.getLastModified()).isCloseTo(feed.getLastModified(), 1000);
	}
	
	@Test
	public void copyFeed_Source_null() {
		OLATResource target = JunitTestHelper.createRandomResource();
		
		Feed copy = feedDao.copyFeed(null, target);
		dbInstance.commitAndCloseSession();

		assertThat(copy).isNull();
	}
	
	@Test
	public void copyFeed_Target_null() {
		OLATResource source = JunitTestHelper.createRandomResource();
		
		Feed copy = feedDao.copyFeed(source, null);
		dbInstance.commitAndCloseSession();

		assertThat(copy).isNull();
	}
	
	@Test
	public void loadFeed_Long() {
		OLATResource resource = JunitTestHelper.createRandomResource();

		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();
		
		Feed reloaded = feedDao.loadFeed(feed.getKey());

		//check values
		Assert.assertEquals(feed.getKey(), reloaded.getKey());
		Assert.assertEquals(resource.getResourceableId(), reloaded.getResourceableId());
		Assert.assertEquals(resource.getResourceableTypeName(), reloaded.getResourceableTypeName());
		// It's ok when the date is about the same in the database.
		Assert.assertTrue("Dates aren't close enough to each other!",
				(feed.getCreationDate().getTime() - reloaded.getCreationDate().getTime()) < 1000);
	}
	
	@Test
	public void loadFeed_notExisting() {	
		// load feed for a non existing key
		Feed feed = feedDao.loadFeed(-1L);

		Assert.assertNull(feed);
	}
	
	@Test
	public void loadFeed_Resourceable() {
		OLATResource resource = JunitTestHelper.createRandomResource();

		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();
		
		Feed reloaded = feedDao.loadFeed(resource);

		//check values
		Assert.assertEquals(feed.getKey(), reloaded.getKey());
		Assert.assertEquals(resource.getResourceableId(), reloaded.getResourceableId());
		Assert.assertEquals(resource.getResourceableTypeName(), reloaded.getResourceableTypeName());
	}
	
	@Test
	public void updateFeed() {
		OLATResource resource = JunitTestHelper.createRandomResource();

		// create and save a feed
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();
		
		// change the values of the feed
		String initialAuthor = "Author";
		feed.setAuthor(initialAuthor);
		String description = "My Feed";
		feed.setDescription(description);
		Boolean external = true;
		feed.setExternal(external);
		String externalFeedUrl = "http://feed.xml";
		feed.setExternalFeedUrl(externalFeedUrl);
		String externalImageURL = "https://www.example.com/image.png";
		feed.setExternalImageURL(externalImageURL);
		String name = "My Image";
		feed.setImageName(name);
		String title = "Display";
		feed.setTitle(title);
		
		// update the feed in the database and reload it
		feedDao.updateFeed(feed);
		dbInstance.commitAndCloseSession();
		Feed reloaded = feedDao.loadFeed(feed.getKey());

		//check values
		Assert.assertEquals(feed.getKey(), reloaded.getKey());
		Assert.assertEquals(feed.getAuthor(), reloaded.getAuthor());
		Assert.assertEquals(feed.getDescription(), reloaded.getDescription());
		Assert.assertEquals(feed.isExternal(), reloaded.isExternal());
		Assert.assertEquals(feed.isInternal(), reloaded.isInternal());
		Assert.assertEquals(feed.isUndefined(), reloaded.isUndefined());
		Assert.assertEquals(feed.getExternalFeedUrl(), reloaded.getExternalFeedUrl());
		Assert.assertEquals(feed.getExternalImageURL(), reloaded.getExternalImageURL());
		Assert.assertEquals(feed.getAuthor(), reloaded.getAuthor());
		Assert.assertEquals(feed.getImageName(), reloaded.getImageName());
		Assert.assertEquals(feed.getTitle(), reloaded.getTitle());
		Assert.assertEquals(resource.getResourceableId(), reloaded.getResourceableId());
		Assert.assertEquals(resource.getResourceableTypeName(), reloaded.getResourceableTypeName());
	}
	
	@Test
	public void updateFeed_null() {
		OLATResource resource = JunitTestHelper.createRandomResource();

		// create and save a feed
		feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();
		
		// update null
		Feed updated = feedDao.updateFeed(null);

		//check values
		Assert.assertNull(updated);
	}
	
	@Test
	public void removeFeed() {
		// store 3 feeds
		OLATResource resource1 = JunitTestHelper.createRandomResource();
		OLATResource resource2 = JunitTestHelper.createRandomResource();
		OLATResource resource3 = JunitTestHelper.createRandomResource();
		
		Feed feed1 = feedDao.createFeedForResourcable(resource1);
		Feed feed2 = feedDao.createFeedForResourcable(resource2);
		Feed feed3 = feedDao.createFeedForResourcable(resource3);
		dbInstance.commitAndCloseSession();
		
		// delete 1 feed
		feedDao.removeFeedForResourceable(feed2);
		dbInstance.commitAndCloseSession();
		
		// check if one feed is deleted and two feeds are still in the database
		Feed reloaded1 = feedDao.loadFeed(feed1.getKey());
		Assert.assertNotNull(reloaded1);
		Feed reloaded2 = feedDao.loadFeed(feed2.getKey());
		Assert.assertNull(reloaded2);
		Feed reloaded3 = feedDao.loadFeed(feed3.getKey());
		Assert.assertNotNull(reloaded3);
	}
	
	@Test
	public void removeFeed_null() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourcable(resource);
		dbInstance.commitAndCloseSession();
		
		feedDao.removeFeedForResourceable(null);
		dbInstance.commitAndCloseSession();
		
		Feed reloaded1 = feedDao.loadFeed(feed.getKey());
		Assert.assertNotNull(reloaded1);
	}

}
