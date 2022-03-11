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
package org.olat.instantMessaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.instantMessaging.manager.InstantMessageDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InstantMessageDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private InstantMessageDAO imDao;
	
	@Test
	public void testCreateMessage() {
		OLATResourceable chatResources = OresHelper.createOLATResourceableInstance("unit-im-dao-1", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-dao-1");
		String from = userManager.getUserDisplayName(id);
		InstantMessage msg = imDao.createMessage(id, from, false, "Hello world", null, null,
				chatResources, null, null, InstantMessageTypeEnum.text);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getKey());
		Assert.assertNotNull(msg.getCreationDate());
		Assert.assertEquals("Hello world", msg.getBody());
		
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void testLoadMessageById() {
		//create a message
		OLATResourceable chatResources = OresHelper.createOLATResourceableInstance("unit-im-dao-2", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-dao-2");
		String from = userManager.getUserDisplayName(id);
		InstantMessage msg = imDao.createMessage(id, from, false, "Hello load by id", null, null,
				chatResources, null, null, InstantMessageTypeEnum.text);
		Assert.assertNotNull(msg);
		dbInstance.commitAndCloseSession();
		
		//load the message
		InstantMessage reloadedMsg = imDao.loadMessageById(msg.getKey());
		Assert.assertNotNull(reloadedMsg);
		Assert.assertEquals(msg.getKey(), reloadedMsg.getKey());
		Assert.assertEquals("Hello load by id", reloadedMsg.getBody());
	}
	
	@Test
	public void testLoadMessageByResource() {
		//create a message
		OLATResourceable chatResources = OresHelper.createOLATResourceableInstance("unit-im-dao-3", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-dao-3");
		String from = userManager.getUserDisplayName(id);
		InstantMessage msg = imDao.createMessage(id, from, false, "Hello load by resource", null, null,
				chatResources, null, null, InstantMessageTypeEnum.text);
		Assert.assertNotNull(msg);
		dbInstance.commitAndCloseSession();
		
		//load the message
		List<InstantMessage> messageList = imDao.getMessages(chatResources, null, null, null, 0, -1);
		Assert.assertNotNull(messageList);
		Assert.assertEquals(1, messageList.size());
		Assert.assertEquals(msg.getKey(), messageList.get(0).getKey());
		Assert.assertEquals("Hello load by resource", messageList.get(0).getBody());
	}
	
	@Test
	public void testLoadMessageByChannel() {
		//create a message
		OLATResourceable chatResources = OresHelper.createOLATResourceableInstance("unit-im-dao-4", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-dao-4");
		String from = userManager.getUserDisplayName(id);
		InstantMessage msgNoChannel = imDao.createMessage(id, from, false, "Hello load by resource", null, null,
				chatResources, null, null, InstantMessageTypeEnum.text);
		InstantMessage msgWithChannel = imDao.createMessage(id, from, false, "Hello load by resource", null, null,
				chatResources, "sub-path", "channel", InstantMessageTypeEnum.text);
		Assert.assertNotNull(msgNoChannel);
		dbInstance.commitAndCloseSession();
		
		//load the message
		List<InstantMessage> messageList = imDao.getMessages(chatResources, "sub-path", "channel", null, 0, -1);
		assertThat(messageList)
			.containsExactly(msgWithChannel);
	}
}
