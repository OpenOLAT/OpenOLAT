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
package org.olat.resource.accesscontrol;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ACReservationDAOTest extends OlatTestCase  {
	
	private final Logger log = Tracing.createLoggerFor(ACReservationDAOTest.class);
	
	@Autowired
	private Scheduler scheduler;
	@Autowired
	private DB dbInstance;
	@Autowired
	private ACReservationDAO acReservationDao;

	@Before
	public void interruptReservationJob() {	
		try {
			scheduler.pauseJob(new JobKey("acReservationCleanupJobDetail", Scheduler.DEFAULT_GROUP));
		} catch (SchedulerException e) {
			log.error("Cannot intterupt the reservation job.", e);
		}
	}
	
	@Test
	public void should_service_present() {
		Assert.assertNotNull(acReservationDao);
	}
	
	@Test
	public void testCreateReservation() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("reserv-1-" + UUID.randomUUID().toString());
		OLATResource resource = JunitTestHelper.createRandomResource();
		dbInstance.commitAndCloseSession();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, 1);
		ResourceReservation reservation = acReservationDao.createReservation(id, "test", cal.getTime(), resource);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reservation);
		Assert.assertNotNull(reservation.getKey());
		Assert.assertNotNull(reservation.getCreationDate());
		Assert.assertNotNull(reservation.getLastModified());
		Assert.assertEquals("test", reservation.getType());
		Assert.assertNotNull(reservation.getExpirationDate());
		Assert.assertEquals(id, reservation.getIdentity());
		Assert.assertEquals(resource, reservation.getResource());
	}
	
	@Test
	public void testLoadReservation() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("reserv-1-" + UUID.randomUUID().toString());
		OLATResource resource = JunitTestHelper.createRandomResource();
		dbInstance.commitAndCloseSession();

		ResourceReservation reservation = acReservationDao.createReservation(id, "test", null, resource);
		dbInstance.commitAndCloseSession();
		
		//check by load
		ResourceReservation loadedReservation = acReservationDao.loadReservation(id, resource);
		
		Assert.assertNotNull(loadedReservation);
		Assert.assertNotNull(loadedReservation.getKey());
		Assert.assertNotNull(loadedReservation.getCreationDate());
		Assert.assertNotNull(loadedReservation.getLastModified());
		Assert.assertEquals(id, loadedReservation.getIdentity());
		Assert.assertEquals(resource, loadedReservation.getResource());
		Assert.assertEquals(reservation, loadedReservation);
	}
	
	@Test
	public void testLoadOldReservation() throws Exception {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("reserv-1-" + UUID.randomUUID().toString());
		OLATResource resource = JunitTestHelper.createRandomResource();
		dbInstance.commitAndCloseSession();

		ResourceReservation reservation = acReservationDao.createReservation(id, "test", null, resource);
		dbInstance.commitAndCloseSession();
		
		Thread.sleep(5000);
		
		//check by load
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, -2);
		Date date = cal.getTime();
		List<ResourceReservation> oldReservations = acReservationDao.loadReservationOlderThan(date);
		Assert.assertNotNull(oldReservations);
		Assert.assertFalse(oldReservations.isEmpty());
		Assert.assertTrue(oldReservations.contains(reservation));
	}
	
	@Test
	public void testLoadExpiredReservation() throws Exception {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("reserv-1-" + UUID.randomUUID().toString());
		OLATResource resource = JunitTestHelper.createRandomResource();
		dbInstance.commitAndCloseSession();

		ResourceReservation reservation1 = acReservationDao.createReservation(id, "test", null, resource);
		dbInstance.commitAndCloseSession();
		
		Calendar cal2 = Calendar.getInstance();
		cal2.add(Calendar.SECOND, +2);
		ResourceReservation reservation2 = acReservationDao.createReservation(id, "test", cal2.getTime(), resource);
		dbInstance.commitAndCloseSession();
		
		Calendar cal3 = Calendar.getInstance();
		cal3.add(Calendar.SECOND, +10);
		ResourceReservation reservation3 = acReservationDao.createReservation(id, "test", cal3.getTime(), resource);
		dbInstance.commitAndCloseSession();
		
		Thread.sleep(5000);
		
		//check by load
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, -2);
		Date date = cal.getTime();
		List<ResourceReservation> oldReservations = acReservationDao.loadExpiredReservation(date);
		Assert.assertNotNull("Old reservations cannot be null", oldReservations);
		Assert.assertFalse(oldReservations.isEmpty());
		Assert.assertTrue(oldReservations.contains(reservation1));
		Assert.assertTrue(oldReservations.contains(reservation2));
		Assert.assertFalse(oldReservations.contains(reservation3));
	}
	
	
	@Test
	public void testCountReservation() {
		//create 3 identities and 3 reservations
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("reserv-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("reserv-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("reserv-3-" + UUID.randomUUID().toString());
		OLATResource resource = JunitTestHelper.createRandomResource();
		dbInstance.commitAndCloseSession();

		ResourceReservation reservation1 = acReservationDao.createReservation(id1, "test", null, resource);
		Assert.assertNotNull(reservation1);
		ResourceReservation reservation2 = acReservationDao.createReservation(id2, "test", null, resource);
		Assert.assertNotNull(reservation2);
		ResourceReservation reservation3 = acReservationDao.createReservation(id3, "test", null, resource);
		Assert.assertNotNull(reservation3);
		dbInstance.commitAndCloseSession();
		
		//count reservations
		int count = acReservationDao.countReservations(resource);
		Assert.assertEquals(3, count);
	}
	
	@Test
	public void testDeleteReservation() {
		//create 3 identities and 3 reservations
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("reserv-4-" + UUID.randomUUID().toString());
		OLATResource resource = JunitTestHelper.createRandomResource();
		ResourceReservation reservation = acReservationDao.createReservation(id, "test", null, resource);
		dbInstance.commitAndCloseSession();
		
		//count reservations
		int count = acReservationDao.countReservations(resource);
		Assert.assertEquals(1, count);
		
		//delete reservation
		acReservationDao.deleteReservation(reservation);
		dbInstance.commitAndCloseSession();
		
		//recount
		int countAfter = acReservationDao.countReservations(resource);
		Assert.assertEquals(0, countAfter);
		ResourceReservation deletedReservation = acReservationDao.loadReservation(reservation.getKey());
		Assert.assertNull(deletedReservation);
	}
	
	@Test
	public void testDeleteReservation_entityNotFound() {
		//create 3 identities and 3 reservations
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("reserv-5-" + UUID.randomUUID().toString());
		OLATResource resource = JunitTestHelper.createRandomResource();
		ResourceReservation reservation = acReservationDao.createReservation(id, "test", null, resource);
		dbInstance.commitAndCloseSession();
		
		//count reservations
		int count = acReservationDao.countReservations(resource);
		Assert.assertEquals(1, count);
		
		//delete reservation
		int rowDeleted = acReservationDao.deleteReservation(reservation);
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(1, rowDeleted);
		
		//try a second delete
		int rowDeleted2 = acReservationDao.deleteReservation(reservation);
		Assert.assertEquals(0, rowDeleted2);

		int count2 = acReservationDao.countReservations(resource);
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(0, count2);
	}
	
	/**
	 * check that one and only one reservation is deleted.
	 */
	@Test
	public void testDeleteReservation_paranoiaCheck() {
		//create 3 identities and 3 reservations
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("reserv-6-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("reserv-8-" + UUID.randomUUID().toString());
		OLATResource resource1 = JunitTestHelper.createRandomResource();
		OLATResource resource2 = JunitTestHelper.createRandomResource();
		ResourceReservation reservation1_1 = acReservationDao.createReservation(id1, "test", null, resource1);
		ResourceReservation reservation1_2 = acReservationDao.createReservation(id1, "test", null, resource2);
		ResourceReservation reservation2_1 = acReservationDao.createReservation(id2, "test", null, resource1);
		ResourceReservation reservation2_2 = acReservationDao.createReservation(id2, "test", null, resource2);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reservation1_1);
		Assert.assertNotNull(reservation1_2);
		Assert.assertNotNull(reservation2_1);
		Assert.assertNotNull(reservation2_2);
		
		//count reservations
		int count1 = acReservationDao.countReservations(resource1);
		Assert.assertEquals(2, count1);
		int count2 = acReservationDao.countReservations(resource2);
		Assert.assertEquals(2, count2);
		
		//delete reservation identity 1 on resource 2
		int rowDeleted = acReservationDao.deleteReservation(reservation1_2);
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(1, rowDeleted);

		int checkCount1 = acReservationDao.countReservations(resource1);
		Assert.assertEquals(2, checkCount1);
		int checkCount2 = acReservationDao.countReservations(resource2);
		Assert.assertEquals(1, checkCount2);
	}

	@Test
	public void testDeleteReservations() {
		//create 3 identities and 3 reservations
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("reserv-7-" + UUID.randomUUID().toString());
		OLATResource resource1 = JunitTestHelper.createRandomResource();
		OLATResource resource2 = JunitTestHelper.createRandomResource();
		ResourceReservation reservation1_1 = acReservationDao.createReservation(id, "test delete 1", null, resource1);
		ResourceReservation reservation1_2 = acReservationDao.createReservation(id, "test delete 2", null, resource1);
		ResourceReservation reservation2_1 = acReservationDao.createReservation(id, "test delete 3", null, resource2);
		dbInstance.commitAndCloseSession();

		acReservationDao.deleteReservations(resource1);
		dbInstance.commitAndCloseSession();

		//check the 2 reservations are deleted
		ResourceReservation deletedReservation1_1 = acReservationDao.loadReservation(reservation1_1.getKey());
		Assert.assertNull(deletedReservation1_1);
		ResourceReservation deletedReservation1_2 = acReservationDao.loadReservation(reservation1_2.getKey());
		Assert.assertNull(deletedReservation1_2);
		
		//check the third reservation on resource 2 survive
		ResourceReservation deletedReservation2_1 = acReservationDao.loadReservation(reservation2_1.getKey());
		Assert.assertNotNull(deletedReservation2_1);
		Assert.assertEquals(reservation2_1, deletedReservation2_1);
	}
}
