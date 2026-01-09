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
package org.olat.core.commons.services.notifications.manager;

import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 26 juin 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class PublisherDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PublisherDAO publisherDao;
	@Autowired
	private NotificationsManager notificationsManager;
	
	@Test
	public void getPublisherByType() {
		String identifier = UUID.randomUUID().toString();
		String publisherType = "TYPE-" + identifier;
		SubscriptionContext context = new SubscriptionContext("PS", Long.valueOf(312), identifier);
		PublisherData publisherData = new PublisherData(publisherType, "data-field-getPublisherByType-1", "[JUNIT:0]");
		
		Publisher publisher = notificationsManager.getOrCreatePublisher(context, publisherData);
		dbInstance.commitAndCloseSession();
		
		List<Publisher> publishers = publisherDao.getPublisherByType(publisherType);
		Assertions.assertThat(publishers)
			.hasSize(1)
			.containsExactly(publisher);
	}
	
	@Test
	public void getPublishers() {
		String identifier = UUID.randomUUID().toString();
		String publisherType = "TYPE-" + identifier;
		SubscriptionContext context = new SubscriptionContext("PS", Long.valueOf(313), identifier);
		PublisherData publisherData = new PublisherData(publisherType, "data-field-getPublishers-1", "[JUNIT:0]");
		
		Publisher publisher = notificationsManager.getOrCreatePublisher(context, publisherData);
		dbInstance.commitAndCloseSession();
		
		List<Publisher> publishers = publisherDao.getPublishers(context);
		Assertions.assertThat(publishers)
			.hasSize(1)
			.containsExactly(publisher);
	}
	
	@Test
	public void countPublishers() {
		String identifier = UUID.randomUUID().toString();
		String publisherType = "TYPE-" + identifier;
		SubscriptionContext context = new SubscriptionContext("PS", Long.valueOf(314), identifier);
		PublisherData publisherData = new PublisherData(publisherType, "data-field-countPublishers-1", "[JUNIT:0]");
		
		Publisher publisher = notificationsManager.getOrCreatePublisher(context, publisherData);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(publisher);
		
		long publishers = publisherDao.countPublishers(context);
		Assert.assertEquals(1l, publishers);
	}
}
