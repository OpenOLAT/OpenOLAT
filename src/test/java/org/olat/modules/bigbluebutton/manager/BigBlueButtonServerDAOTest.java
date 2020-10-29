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
package org.olat.modules.bigbluebutton.manager;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.bigbluebutton.BigBlueButtonServer;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonServerDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BigBlueButtonServerDAO bigBlueButtonServerDao;
	
	@Test
	public void createServer() {
		String url = "https://" + UUID.randomUUID().toString() + "/bigbluebutton";
		String recordingUrl = "https://" + UUID.randomUUID().toString() + "/bigbluebutton/recordings";
		String sharedSecret = UUID.randomUUID().toString();
		
		BigBlueButtonServer server = bigBlueButtonServerDao.createServer(url, recordingUrl, sharedSecret);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(server);
		Assert.assertEquals(url, server.getUrl());
		Assert.assertEquals(recordingUrl, server.getRecordingUrl());
		Assert.assertEquals(sharedSecret, server.getSharedSecret());
	}
	
	@Test
	public void getServers() {
		String url = "https://" + UUID.randomUUID().toString() + "/bigbluebutton";
		String recordingUrl = "https://" + UUID.randomUUID().toString() + "/bigbluebutton/recordings";
		String sharedSecret = UUID.randomUUID().toString();
		
		BigBlueButtonServer server = bigBlueButtonServerDao.createServer(url, recordingUrl, sharedSecret);
		dbInstance.commitAndCloseSession();
		
		List<BigBlueButtonServer> servers = bigBlueButtonServerDao.getServers();
		
		Assert.assertNotNull(servers);
		Assert.assertTrue(servers.contains(server));
	}
	
	@Test
	public void getServerByKey() {
		String name = UUID.randomUUID().toString();
		String url = "https://" + name + "/bigbluebutton";
		String recordingUrl = "https://" + name + "/bigbluebutton/recordings";
		String sharedSecret = name;
		
		BigBlueButtonServer server = bigBlueButtonServerDao.createServer(url, recordingUrl, sharedSecret);
		dbInstance.commitAndCloseSession();
		
		BigBlueButtonServer loadedServer = bigBlueButtonServerDao.getServer(server.getKey());
		Assert.assertNotNull(loadedServer);
		Assert.assertEquals(server, loadedServer);
	}
	
	@Test
	public void getNotExistantServerByKey() {
		BigBlueButtonServer loadedServer = bigBlueButtonServerDao.getServer(25474989l);
		Assert.assertNull(loadedServer);
	}
}
