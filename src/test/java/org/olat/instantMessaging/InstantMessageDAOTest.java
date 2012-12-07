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

import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.instantMessaging.manager.InstantMessageDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
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
	private InstantMessageDAO imDao;
	
	@Test
	public void testCreateMessage() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsAdmin("im-1-" + UUID.randomUUID().toString());
		InstantMessage msg = imDao.createMessage(id, "Hello world");
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getKey());
		Assert.assertNotNull(msg.getCreationDate());
		Assert.assertEquals("Hello world", msg.getBody());
		
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void testLoadMessage() {
		//create a message
		Identity id = JunitTestHelper.createAndPersistIdentityAsAdmin("im-2-" + UUID.randomUUID().toString());
		InstantMessage msg = imDao.createMessage(id, "Hello load by id");
		Assert.assertNotNull(msg);
		dbInstance.commitAndCloseSession();
		
		//load the message
		InstantMessage reloadedMsg = imDao.loadMessageById(msg.getKey());
		Assert.assertNotNull(reloadedMsg);
		Assert.assertEquals(msg.getKey(), reloadedMsg.getKey());
		Assert.assertEquals("Hello load by id", reloadedMsg.getBody());
	}



}
