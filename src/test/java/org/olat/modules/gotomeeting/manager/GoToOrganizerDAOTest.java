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
package org.olat.modules.gotomeeting.manager;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.gotomeeting.GoToOrganizer;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToOrganizerDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GoToOrganizerDAO organizerDao;
	
	@Test
	public void createOrganizer_withoutOwner() {
		String username = UUID.randomUUID().toString();
		String accessToken = UUID.randomUUID().toString();
		String refreshToken = UUID.randomUUID().toString();
		String organizerKey = UUID.randomUUID().toString();
		
		GoToOrganizer organizer = organizerDao
				.createOrganizer("Our account", username, accessToken, refreshToken, organizerKey, null, null, null, null, 10l, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(organizer);
		Assert.assertNotNull(organizer.getKey());
		Assert.assertNotNull(organizer.getCreationDate());
		Assert.assertNotNull(organizer.getLastModified());
		Assert.assertEquals(username, organizer.getUsername());
		
		GoToOrganizer reloadedOrganizer = organizerDao.loadOrganizerByKey(organizer.getKey());
		Assert.assertNotNull(reloadedOrganizer);
		Assert.assertEquals(organizer.getKey(), reloadedOrganizer.getKey());
		Assert.assertNotNull(organizer.getCreationDate());
		Assert.assertNotNull(organizer.getLastModified());
		Assert.assertEquals("Our account", reloadedOrganizer.getName());
		Assert.assertEquals(username, reloadedOrganizer.getUsername());
		Assert.assertEquals(accessToken, reloadedOrganizer.getAccessToken());
		Assert.assertEquals(refreshToken, reloadedOrganizer.getRefreshToken());
		Assert.assertEquals(organizerKey, reloadedOrganizer.getOrganizerKey());
		Assert.assertNotNull(reloadedOrganizer.getRenewDate());
		Assert.assertNotNull(reloadedOrganizer.getRenewRefreshDate());
	}
	
	@Test
	public void createOrganizer_withOwner() {
		Identity org = JunitTestHelper.createAndPersistIdentityAsRndUser("organizer-1");

		String username = UUID.randomUUID().toString();
		String accessToken = UUID.randomUUID().toString();
		String refreshToken = UUID.randomUUID().toString();
		String organizerKey = UUID.randomUUID().toString();
		
		GoToOrganizer organizer = organizerDao
				.createOrganizer(null, username, accessToken, refreshToken, organizerKey, "Albrecht", "Durer", "albert@openolat.com", "account-key", 1200l, org);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(organizer);

		GoToOrganizer reloadedOrganizer = organizerDao.loadOrganizerByKey(organizer.getKey());
		Assert.assertNotNull(reloadedOrganizer);
		Assert.assertEquals(organizer.getKey(), reloadedOrganizer.getKey());
		Assert.assertNotNull(organizer.getCreationDate());
		Assert.assertNotNull(organizer.getLastModified());
		Assert.assertEquals(username, reloadedOrganizer.getUsername());
		Assert.assertEquals(accessToken, reloadedOrganizer.getAccessToken());
		Assert.assertEquals(refreshToken, reloadedOrganizer.getRefreshToken());
		Assert.assertEquals(organizerKey, reloadedOrganizer.getOrganizerKey());
		Assert.assertEquals(org, reloadedOrganizer.getOwner());
		Assert.assertNotNull(reloadedOrganizer.getRenewDate());
		Assert.assertNotNull(reloadedOrganizer.getRenewRefreshDate());
	}
	
	@Test
	public void getOrganizers() {
		String username = UUID.randomUUID().toString();
		String accessToken = UUID.randomUUID().toString();
		String refreshToken = UUID.randomUUID().toString();
		String organizerKey = UUID.randomUUID().toString();
		
		GoToOrganizer organizer = organizerDao
				.createOrganizer(null, username, accessToken, refreshToken, organizerKey, "Martin", "Schongauer", null, null, 10l, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(organizer); 

		//load by user name
		List<GoToOrganizer> organizers = organizerDao.getOrganizers();
		Assert.assertNotNull(organizers);
		Assert.assertFalse(organizers.isEmpty());
		Assert.assertTrue(organizers.contains(organizer));
	}
	
	@Test
	public void getOrganizersFor() {
		Identity org = JunitTestHelper.createAndPersistIdentityAsRndUser("organizer-2");
		Identity trainee = JunitTestHelper.createAndPersistIdentityAsRndUser("trainee-1");
		
		String username = UUID.randomUUID().toString();
		String accessToken = UUID.randomUUID().toString();
		String refreshToken = UUID.randomUUID().toString();
		String organizerKey = UUID.randomUUID().toString();
		
		GoToOrganizer myOrganizer = organizerDao
				.createOrganizer(null, username, accessToken, refreshToken, organizerKey, "Martin", "Schongauer", null, null, 10l, org);
		GoToOrganizer systemOrganizer = organizerDao
				.createOrganizer(null, username + "_w", accessToken + "_w", refreshToken + "_w", organizerKey + "_w", "System", "Wide", null, null, 10l, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(myOrganizer); 

		//load available organizers
		List<GoToOrganizer> organizers = organizerDao.getOrganizersFor(org);
		Assert.assertNotNull(organizers);
		Assert.assertFalse(organizers.isEmpty());
		Assert.assertTrue(organizers.contains(myOrganizer));
		Assert.assertTrue(organizers.contains(systemOrganizer));
		
		// trainee has only system wide organizers
		List<GoToOrganizer> traineeOrganizers = organizerDao.getOrganizersFor(trainee);
		Assert.assertNotNull(traineeOrganizers);
		Assert.assertFalse(traineeOrganizers.isEmpty());
		Assert.assertFalse(traineeOrganizers.contains(myOrganizer));
		Assert.assertTrue(traineeOrganizers.contains(systemOrganizer));
	}
	
	@Test
	public void getOrganizers_accountKey() {
		String username = UUID.randomUUID().toString();
		String accessToken = UUID.randomUUID().toString();
		String refreshToken = UUID.randomUUID().toString();
		String organizerKey = UUID.randomUUID().toString();
		String accountKey = UUID.randomUUID().toString();
		
		GoToOrganizer organizer = organizerDao
				.createOrganizer(null, username, accessToken, refreshToken, organizerKey, "Martin", "Schongauer", null, accountKey, 10l, null);

		//load organizers
		List<GoToOrganizer> organizers = organizerDao.getOrganizers(accountKey, organizerKey);
		Assert.assertNotNull(organizers);
		Assert.assertEquals(1, organizers.size());
		Assert.assertTrue(organizers.contains(organizer));
		// check organizer
		GoToOrganizer reloadedOrganizer = organizers.get(0);
		Assert.assertNotNull(reloadedOrganizer);
		
		Assert.assertNotNull(reloadedOrganizer);
		Assert.assertEquals(organizer.getKey(), reloadedOrganizer.getKey());
		Assert.assertNotNull(organizer.getCreationDate());
		Assert.assertNotNull(organizer.getLastModified());
		Assert.assertEquals(username, reloadedOrganizer.getUsername());
		Assert.assertEquals(accessToken, reloadedOrganizer.getAccessToken());
		Assert.assertEquals(refreshToken, reloadedOrganizer.getRefreshToken());
		Assert.assertEquals(organizerKey, reloadedOrganizer.getOrganizerKey());
		Assert.assertNotNull(reloadedOrganizer.getRenewDate());
		Assert.assertNotNull(reloadedOrganizer.getRenewRefreshDate());
	}
}
