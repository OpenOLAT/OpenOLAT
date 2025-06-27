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

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.commentAndRating.manager.UserCommentsDAO;
import org.olat.core.commons.services.commentAndRating.model.UserComment;
import org.olat.core.id.Identity;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.manager.FeedNotificationsPushHandler.Notification;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 juin 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class FeedNotificationsPushHandlerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ItemDAO itemDao;
	@Autowired
	private FeedDAO feedDao;
	@Autowired
	private UserCommentsDAO userCommentsDao;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private FeedNotificationsPushHandler notificationsHandler;
	
	@Test
	public void loadIdentityToNotify() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("feed-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("feed-2");
		OLATResource ores = resourceManager.createOLATResourceInstance(new BlogFileResource());
		resourceManager.saveOLATResource(ores);
		
		OLATResource resource = JunitTestHelper.createRandomResource();
		Feed feed = feedDao.createFeedForResourceable(resource);
		Item item = itemDao.createItem(feed);
		dbInstance.commitAndCloseSession();
		
		UserComment comment = userCommentsDao.createComment(id1, ores, item.getKey().toString(), "Hello unit testers");
		UserComment replyComment = userCommentsDao.replyTo(comment, id2, "Hello unit notifier");
		dbInstance.commitAndCloseSession();
		
		List<Notification> notifications = notificationsHandler.collectIdentitiesToNotify(null, feed, item, replyComment);
		Assertions.assertThat(notifications)
			.hasSize(1)
			.map(Notification::identity)
			.containsExactly(id1);
	}

}
