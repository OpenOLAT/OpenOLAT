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

package org.olat.commons.info;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.olat.commons.info.manager.InfoMessageManager;
import org.olat.commons.info.model.InfoMessage;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Unit tests for the InfoMessageManager. It's integration, it need
 * the DB to test the schema, the queries...
 * 
 * <P>
 * Initial Date:  26 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoManagerTest extends OlatTestCase {
	
	private static Identity id1;
	
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private InfoMessageManager infoMessageManager;

	/**
	 * Set up a course with learn group and group area
	 * @see org.olat.test.OlatJerseyTestCase#setUp()
	 */
	@Before
	public void setUp() throws Exception {
		if(id1 == null) {
			id1 = JunitTestHelper.createAndPersistIdentityAsUser("info-msg-1");
			JunitTestHelper.createAndPersistIdentityAsUser("info-msg-2");
		}
	}
	
	@Test
	public void testManagers() {
		assertNotNull(infoMessageManager);
	}
	
	@Test
	public void testSaveInfoMessage() {
		final InfoOLATResourceable ores = new InfoOLATResourceable(5l, "InfoTests");
		final String subPath = UUID.randomUUID().toString();
		final String businessPath = "[test:la]";
		InfoMessage msg = infoMessageManager.createInfoMessage(ores, subPath, businessPath, id1);
		assertNotNull(msg);
		
		msg.setTitle("Title");
		msg.setMessage("Message");
		infoMessageManager.saveInfoMessage(msg);
		dbInstance.commitAndCloseSession();
		
		assertNotNull(msg.getKey());
		InfoMessage retrievedMsg = infoMessageManager.loadInfoMessageByKey(msg.getKey());
		assertNotNull(retrievedMsg);
		assertEquals("Title", retrievedMsg.getTitle());
		assertEquals("Message", retrievedMsg.getMessage());
		assertEquals(subPath, retrievedMsg.getResSubPath());
		assertEquals(businessPath, retrievedMsg.getBusinessPath());
		assertNotNull(retrievedMsg.getOLATResourceable());
		assertEquals(ores.getResourceableId(), retrievedMsg.getResId());
		assertEquals(ores.getResourceableTypeName(), retrievedMsg.getResName());
		assertNotNull(retrievedMsg.getAuthor());
		assertEquals(id1.getKey(), retrievedMsg.getAuthor().getKey());
	}
	
	@Test
	public void testLoadByResource() {
		final String resName = UUID.randomUUID().toString();
		final InfoOLATResourceable ores = new InfoOLATResourceable(5l, resName);
		InfoMessage msg = infoMessageManager.createInfoMessage(ores, null, null, id1);
		assertNotNull(msg);
		infoMessageManager.saveInfoMessage(msg);
		dbInstance.commitAndCloseSession();
		
		List<InfoMessage> retrievedMsg = infoMessageManager.loadInfoMessageByResource(ores, null, null, null, null, 0, 0);
		assertNotNull(retrievedMsg);
		assertEquals(1, retrievedMsg.size());
		assertEquals(msg.getKey(), retrievedMsg.get(0).getKey());
	}
	
	@Test
	public void testLoadByResource2() {
		final String resName = UUID.randomUUID().toString();
		final String subPath = UUID.randomUUID().toString();
		final String businessPath = UUID.randomUUID().toString();
		final InfoOLATResourceable ores = new InfoOLATResourceable(5l, resName);
		InfoMessage msg = infoMessageManager.createInfoMessage(ores, subPath, businessPath, id1);
		assertNotNull(msg);
		infoMessageManager.saveInfoMessage(msg);
		dbInstance.commitAndCloseSession();
		
		List<InfoMessage> retrievedMsg = infoMessageManager.loadInfoMessageByResource(ores, subPath, businessPath, null, null, 0, 0);
		assertNotNull(retrievedMsg);
		assertEquals(1, retrievedMsg.size());
		assertEquals(msg.getKey(), retrievedMsg.get(0).getKey());
	}
	
	@Test
	public void testDelete() {
		//create some messages
		final String resName = UUID.randomUUID().toString();
		final String subPath = UUID.randomUUID().toString();
		final String businessPath = UUID.randomUUID().toString();
		final InfoOLATResourceable ores = new InfoOLATResourceable(6l, resName);
		
		InfoMessage msg1 = infoMessageManager.createInfoMessage(ores, subPath, businessPath, id1);
		assertNotNull(msg1);
		infoMessageManager.saveInfoMessage(msg1);
		
		InfoMessage msg2 = infoMessageManager.createInfoMessage(ores, subPath, businessPath, id1);
		assertNotNull(msg2);
		infoMessageManager.saveInfoMessage(msg2);
		
		InfoMessage msg3 = infoMessageManager.createInfoMessage(ores, subPath, businessPath, id1);
		assertNotNull(msg3);
		infoMessageManager.saveInfoMessage(msg3);

		dbInstance.commitAndCloseSession();
		
		//delete a message
		infoMessageManager.deleteInfoMessage(msg2);
		dbInstance.commitAndCloseSession();
		
		//make the tests
		InfoMessage deletedInfo = infoMessageManager.loadInfoMessageByKey(msg2.getKey());
		assertNull(deletedInfo);
		
		List<InfoMessage> infos = infoMessageManager.loadInfoMessageByResource(ores, subPath, businessPath, null, null, 0, -1);
		assertNotNull(infos);
		assertEquals(2, infos.size());
	}
	
	@Test
	public void testCount() {
		final String resName = UUID.randomUUID().toString();
		final String subPath = UUID.randomUUID().toString();
		final String businessPath = UUID.randomUUID().toString();
		final InfoOLATResourceable ores = new InfoOLATResourceable(7l, resName);

		InfoMessage msg1 = infoMessageManager.createInfoMessage(ores, subPath, businessPath, id1);
		assertNotNull(msg1);
		infoMessageManager.saveInfoMessage(msg1);
		Calendar cal = Calendar.getInstance();
		cal.setTime(msg1.getCreationDate());
		cal.add(Calendar.SECOND, -1);
		Date after = cal.getTime();
		
		InfoMessage msg2 = infoMessageManager.createInfoMessage(ores, subPath, businessPath, id1);
		assertNotNull(msg2);
		infoMessageManager.saveInfoMessage(msg2);
		
		InfoMessage msg3 = infoMessageManager.createInfoMessage(ores, subPath, businessPath, id1);
		assertNotNull(msg3);
		infoMessageManager.saveInfoMessage(msg3);
		
		cal.setTime(msg3.getCreationDate());
		cal.add(Calendar.SECOND, +1);
		Date before = cal.getTime();
		
		dbInstance.commitAndCloseSession();
		
		int count1 = infoMessageManager.countInfoMessageByResource(ores, null, null, null, null);
		assertEquals(3, count1);
		
		int count2 = infoMessageManager.countInfoMessageByResource(ores, subPath, null, null, null);
		assertEquals(3, count2);
		
		int count3 = infoMessageManager.countInfoMessageByResource(ores, subPath, businessPath, null, null);
		assertEquals(3, count3);
		
		int count4 = infoMessageManager.countInfoMessageByResource(ores, subPath, businessPath, after, null);
		assertEquals(3, count4);
		
		int count5 = infoMessageManager.countInfoMessageByResource(ores, subPath, businessPath, after, before);
		assertEquals(3, count5);
	}
	
	private class InfoOLATResourceable implements OLATResourceable {
		private final Long resId;
		private final String resName;
		
		public InfoOLATResourceable(Long resId, String resName) {
			this.resId = resId;
			this.resName = resName;
		}

		@Override
		public String getResourceableTypeName() {
			return resName;
		}

		@Override
		public Long getResourceableId() {
			return resId;
		}
	}
}
