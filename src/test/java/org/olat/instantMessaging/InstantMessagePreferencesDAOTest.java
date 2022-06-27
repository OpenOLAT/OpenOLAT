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

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.instantMessaging.manager.InstantMessagePreferencesDAO;
import org.olat.instantMessaging.model.ImPreferencesImpl;
import org.olat.instantMessaging.model.Presence;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InstantMessagePreferencesDAOTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(InstantMessagePreferencesDAOTest.class);
	private static int NUM_OF_THREADS = 20;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private InstantMessagePreferencesDAO imDao;
	
	@Test
	public void testCreatePreferences() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-prefs-1-");
		ImPreferencesImpl prefs = imDao.createPreferences(id, Presence.available.name(), true);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(prefs);
		Assert.assertNotNull(prefs.getKey());
		Assert.assertNotNull(prefs.getCreationDate());
		Assert.assertEquals(id, prefs.getIdentity());
		Assert.assertEquals(Presence.available.name(), prefs.getRosterDefaultStatus());
		Assert.assertTrue(prefs.isVisibleToOthers());
	}
	
	@Test
	public void testLoadPreferences() {
		//create a message
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-prefs-2-");
		ImPreferencesImpl prefs = imDao.createPreferences(id, Presence.unavailable.name(), true);
		Assert.assertNotNull(prefs);
		dbInstance.commitAndCloseSession();
		
		//load the message
		ImPreferencesImpl reloadedPrefs = imDao.getPreferences(id);
		Assert.assertNotNull(reloadedPrefs);
		Assert.assertEquals(prefs.getKey(), reloadedPrefs.getKey());
		Assert.assertEquals(id, reloadedPrefs.getIdentity());
		Assert.assertEquals(Presence.unavailable.name(), prefs.getRosterDefaultStatus());
		Assert.assertTrue(prefs.isVisibleToOthers());
	}

	@Test
	public void testUpdatePreferences_visibility() {
		//create a message
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-prefs-3-");
		ImPreferencesImpl prefs = imDao.createPreferences(id, Presence.unavailable.name(), true);
		Assert.assertNotNull(prefs);
		dbInstance.commitAndCloseSession();
		
		//update visibility
		imDao.updatePreferences(id, false);
		dbInstance.commitAndCloseSession();
		
		//check the visibility
		ImPreferencesImpl reloadedPrefs = imDao.getPreferences(id);
		Assert.assertNotNull(reloadedPrefs);
		Assert.assertEquals(prefs.getKey(), reloadedPrefs.getKey());
		Assert.assertEquals(id, reloadedPrefs.getIdentity());
		Assert.assertFalse(reloadedPrefs.isVisibleToOthers());
	}
	
	@Test
	public void testUpdatePreferences_status() {
		//create a message
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-prefs-4-");
		ImPreferencesImpl prefs = imDao.createPreferences(id, Presence.dnd.name(), true);
		Assert.assertNotNull(prefs);
		dbInstance.commitAndCloseSession();
		
		//update visibility
		imDao.updatePreferences(id, Presence.unavailable.name());
		dbInstance.commitAndCloseSession();
		
		//check the visibility
		ImPreferencesImpl reloadedPrefs = imDao.getPreferences(id);
		Assert.assertNotNull(reloadedPrefs);
		Assert.assertEquals(prefs.getKey(), reloadedPrefs.getKey());
		Assert.assertEquals(id, reloadedPrefs.getIdentity());
		Assert.assertEquals(Presence.unavailable.name(), reloadedPrefs.getRosterDefaultStatus());
	}
	
	/**
	 * Paranoia test to make sure that the preferences are not duplicated.
	 */
	@Test
	public void testUpdateTwicePreferences_status() {
		//create a message
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-prefs-6-");
		ImPreferencesImpl prefs = imDao.createPreferences(id, Presence.dnd.name(), true);
		Assert.assertNotNull(prefs);
		dbInstance.commitAndCloseSession();
		
		//1. update visibility
		imDao.updatePreferences(id, Presence.unavailable.name());
		dbInstance.commitAndCloseSession();
		//2. update visibility
		imDao.updatePreferences(id, Presence.unavailable.name());
		dbInstance.commitAndCloseSession();

		//check the number of preferences
		List<ImPreferencesImpl> msgs = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadIMPreferencesByIdentity", ImPreferencesImpl.class)
				.setParameter("identityKey", id.getKey())
				.getResultList();
		Assert.assertEquals(1, msgs.size());
	}
	
	@Test
	public void testUpdatePreferences_concurrent() {
		//create a message
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-prefs-4-");
		ImPreferencesImpl prefs = imDao.createPreferences(id, Presence.dnd.name(), true);
		Assert.assertNotNull(prefs);
		dbInstance.commitAndCloseSession();
		
		final CountDownLatch finishCount = new CountDownLatch(NUM_OF_THREADS);
		
		UpdateThread[] threads = new UpdateThread[NUM_OF_THREADS];
		for(int i=0; i<NUM_OF_THREADS; i++) {
			threads[i] = new UpdateThread(id, finishCount, i, dbInstance, imDao);
		}
		
		for(int i=0; i<NUM_OF_THREADS; i++) {
			threads[i].start();
		}
		
		try {
			finishCount.await(20, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Assert.fail("Takes too long (mote than 20sec)");
		}
		
		boolean allOk = true;
		for(int i=0; i<NUM_OF_THREADS; i++) {
			allOk &= threads[i].isOk();
		}
		Assert.assertTrue(allOk);
	}
	
	private static class UpdateThread extends Thread {
		
		private boolean ok = true;
		private final Identity id;
		private final CountDownLatch finishCount;
		
		private final DB dbInstance;
		private final InstantMessagePreferencesDAO imDao;
		
		public UpdateThread(Identity id, CountDownLatch finishCount, int num, 
				DB dbInstance, InstantMessagePreferencesDAO imDao) {
			super("Update im preferences - " + num);
			this.id = id;
			this.finishCount = finishCount;
			this.dbInstance = dbInstance;
			this.imDao = imDao;
		}
		
		public boolean isOk() {
			return ok;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(10);
				
				for(int i=0; i<NUM_OF_THREADS; i++) {
					double r = Math.random() * 5.0d;
					if(r < 1.0d) {
						imDao.updatePreferences(id, true);
					} else if(r < 2.0d) {
						imDao.updatePreferences(id, false);
					} else if(r < 3.0d) {
						imDao.updatePreferences(id, Presence.available.name());
					} else if(r < 4.0d) {
						imDao.updatePreferences(id, Presence.dnd.name());
					} else {
						imDao.updatePreferences(id, Presence.unavailable.name());
					}
					dbInstance.commitAndCloseSession();
				}
			} catch (Exception e) {
				log.error("", e);
				ok = false;
			} finally {
				finishCount.countDown();
			}
		}
	}
}