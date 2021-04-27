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
package org.olat.course.assessment.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.UserCourseInformations;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 03.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserCourseInformationsManagerTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(UserCourseInformationsManagerTest.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private UserCourseInformationsManager userCourseInformationsManager;
	
	@Test
	public void createUpdateCourseInfos_create() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("user-launch-1-");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(user);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commitAndCloseSession();
		
		OLATResource courseResource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
		userCourseInformationsManager.updateUserCourseInformations(courseResource, user);
		dbInstance.commitAndCloseSession();
		
		UserCourseInformations infos = userCourseInformationsManager.getUserCourseInformations(courseResource, user);
		Assert.assertNotNull(infos);
		Assert.assertNotNull(infos.getIdentity());
		Assert.assertNotNull(infos.getResource());
		Assert.assertNotNull(infos.getInitialLaunch());
		Assert.assertNotNull(infos.getRecentLaunch());

		Assert.assertEquals(1, infos.getVisit());
		Assert.assertEquals(infos.getIdentity(), user);
		Assert.assertEquals(course.getResourceableId(), infos.getResource().getResourceableId());
		Assert.assertEquals(course.getResourceableTypeName(), infos.getResource().getResourceableTypeName());
	}
	
	@Test
	public void createUpdateCourseInfos_updateToo() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("user-launch-1-");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(user);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commitAndCloseSession();

		OLATResource courseResource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
		userCourseInformationsManager.updateUserCourseInformations(courseResource, user);
		dbInstance.commitAndCloseSession();
		
		userCourseInformationsManager.updateUserCourseInformations(courseResource, user);
		dbInstance.commitAndCloseSession();
		
		UserCourseInformations infos = userCourseInformationsManager.getUserCourseInformations(courseResource, user);
		Assert.assertNotNull(infos);
		Assert.assertNotNull(infos.getIdentity());
		Assert.assertEquals(2, infos.getVisit());
	}
	
	/**
	 * Check the low level update statement, it's the critical part of the
	 * method which update the user course informations.
	 */
	@Test
	public void createUpdateCourseInfos_updateToo_implementationDetails() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("user-launch-1-");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(user);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commitAndCloseSession();

		OLATResource courseResource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
		userCourseInformationsManager.updateUserCourseInformations(courseResource, user);
		dbInstance.commitAndCloseSession();
		
		int updated1 = ((UserCourseInformationsManagerImpl)userCourseInformationsManager).lowLevelUpdate(courseResource, user);
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(1, updated1);
		
		int updated2 = ((UserCourseInformationsManagerImpl)userCourseInformationsManager).lowLevelUpdate(courseResource, user);
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(1, updated2);
		
		UserCourseInformations infos = userCourseInformationsManager.getUserCourseInformations(courseResource, user);
		Assert.assertNotNull(infos);
		Assert.assertNotNull(infos.getIdentity());
		Assert.assertEquals(3, infos.getVisit());
	}
	
	@Test
	public void getRecentLaunchDate() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("user-launch-7-");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(user);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commitAndCloseSession();
		
		OLATResource courseResource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
		userCourseInformationsManager.updateUserCourseInformations(courseResource, user);
		dbInstance.commitAndCloseSession();
		
		Date launchDate = userCourseInformationsManager.getRecentLaunchDate(courseResource, user);
		Assert.assertNotNull(launchDate);
	}
	
	@Test
	public void getInitialLaunchDate_ressource() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsUser("user-launch");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(user);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commitAndCloseSession();
		
		OLATResource courseResource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
		userCourseInformationsManager.updateUserCourseInformations(courseResource, user);
		dbInstance.commitAndCloseSession();
		
		Date launchDate = userCourseInformationsManager.getInitialLaunchDate(courseResource, user);
		Assert.assertNotNull(launchDate);
	}
	
	@Test
	public void getInitialLaunchDate_repositoryEntry() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("user-launch");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(user);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commitAndCloseSession();
		
		OLATResource courseResource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
		courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		userCourseInformationsManager.updateUserCourseInformations(courseResource, user);
		dbInstance.commitAndCloseSession();
		
		Date launchDate = userCourseInformationsManager.getInitialLaunchDate(courseEntry, user);
		Assert.assertNotNull(launchDate);
	}
	
	@Test
	public void getInitialParticiantLaunchDate_businessGroup() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("init-launch-1-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("init-launch-2-");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(id1);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commitAndCloseSession();
		
		OLATResource courseResource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
		courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		userCourseInformationsManager.updateUserCourseInformations(courseResource, id1);
		userCourseInformationsManager.updateUserCourseInformations(courseResource, id2);
		dbInstance.commit();
		
		//add user to a group
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "initial launch", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, courseEntry);
	    businessGroupRelationDao.addRole(id1, group, GroupRoles.participant.name());
	    businessGroupRelationDao.addRole(id2, group, GroupRoles.participant.name());
	    dbInstance.commitAndCloseSession();
		
		Date launchDate = userCourseInformationsManager.getInitialParticipantLaunchDate(courseEntry, group);
		Assert.assertNotNull(launchDate);
	}
	
	@Test
	public void getInitialLaunchDates() {
		Identity user1 = JunitTestHelper.createAndPersistIdentityAsUser("user-launch-3-" + UUID.randomUUID().toString());
		Identity user2 = JunitTestHelper.createAndPersistIdentityAsUser("user-launch-4-" + UUID.randomUUID().toString());
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(user1);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commitAndCloseSession();
		
		OLATResource courseResource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
		userCourseInformationsManager.updateUserCourseInformations(courseResource, user1);
		userCourseInformationsManager.updateUserCourseInformations(courseResource, user2);
		dbInstance.commitAndCloseSession();
		
		List<Identity> users = new ArrayList<>();
		users.add(user1);
		users.add(user2);

		Map<Long,Date> launchDates = userCourseInformationsManager.getInitialLaunchDates(courseResource, users);
		Assert.assertNotNull(launchDates);
		Assert.assertEquals(2, launchDates.size());
		Assert.assertTrue(launchDates.containsKey(user1.getKey()));
		Assert.assertNotNull(launchDates.get(user1.getKey()));
		Assert.assertTrue(launchDates.containsKey(user2.getKey()));
		Assert.assertNotNull(launchDates.get(user2.getKey()));
	}
	
	@Test
	public void getInitialLaunchDates_noIdentites() {
		Identity user1 = JunitTestHelper.createAndPersistIdentityAsRndUser("user-launch-7-");
		Identity user2 = JunitTestHelper.createAndPersistIdentityAsRndUser("user-launch-8-");
		Identity user3 = JunitTestHelper.createAndPersistIdentityAsRndUser("user-launch-9-");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(user1);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commitAndCloseSession();
		
		OLATResource courseResource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
		userCourseInformationsManager.updateUserCourseInformations(courseResource, user1);
		userCourseInformationsManager.updateUserCourseInformations(courseResource, user2);
		userCourseInformationsManager.updateUserCourseInformations(courseResource, user3);
		dbInstance.commitAndCloseSession();
		
		//get all launch dates
		Map<Long,Date> launchDates = userCourseInformationsManager.getInitialLaunchDates(course.getResourceableId());
		Assert.assertNotNull(launchDates);
		Assert.assertEquals(3, launchDates.size());
		Assert.assertTrue(launchDates.containsKey(user1.getKey()));
		Assert.assertNotNull(launchDates.get(user1.getKey()));
		Assert.assertTrue(launchDates.containsKey(user2.getKey()));
		Assert.assertNotNull(launchDates.get(user2.getKey()));
		Assert.assertTrue(launchDates.containsKey(user3.getKey()));
		Assert.assertNotNull(launchDates.get(user3.getKey()));
	}
	
	/**
	 * This test is to analyze a red screen
	 */
	@Test
	public void updateInitialLaunchDates_loop() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsUser("user-launch-5-" + UUID.randomUUID().toString());
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(user);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commitAndCloseSession();
		
		for(int i=0; i<10; i++) {
			OLATResource courseResource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
			userCourseInformationsManager.updateUserCourseInformations(courseResource, user);
		}
		dbInstance.commitAndCloseSession();
		
		List<Identity> users = Collections.singletonList(user);
		OLATResource courseResource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
		Map<Long,Date> launchDates = userCourseInformationsManager.getInitialLaunchDates(courseResource, users);
		Assert.assertNotNull(launchDates);
		Assert.assertEquals(1, launchDates.size());
		Assert.assertTrue(launchDates.containsKey(user.getKey()));
		Assert.assertNotNull(launchDates.get(user.getKey()));
	}
	
	/**
	 * This test is to analyze a red screen
	 */
	@Test
	public void updateInitialLaunchDates_concurrent() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsUser("user-launch-concurrent-6-" + UUID.randomUUID().toString());
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(user);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commitAndCloseSession();
		
		final int numThreads = 20;
		
		CountDownLatch latch = new CountDownLatch(numThreads);
		UpdateThread[] threads = new UpdateThread[numThreads];
		for(int i=0; i<threads.length;i++) {
			OLATResource courseResource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
			threads[i] = new UpdateThread(user, courseResource, userCourseInformationsManager, latch, dbInstance);
		}

		for(int i=0; i<threads.length;i++) {
			threads[i].start();
		}
		
		try {
			latch.await(120, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Assert.fail("Takes too long (more than 120sec)");
		}
		
		int countErrors = 0;
		for(int i=0; i<threads.length;i++) {
			countErrors += threads[i].getErrors();
		}
		
		Assert.assertEquals(0, countErrors);
	}
	
	/* Needed to generate a lot of datas
	@Test
	public void testHeavyLoads() {
		List<Identity> loadIdentities = CoreSpringFactory.getImpl(BaseSecurity.class)
				.getVisibleIdentitiesByPowerSearch(null, null, false, null, null, null, null, null, 1000, 20000);

		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters();
		params.setRoles(new Roles(true, false, false, false, false, false, false));
		params.setResourceTypes(Collections.singletonList("CourseModule"));
		List<RepositoryEntry> loadOres = RepositoryManager.getInstance().genericANDQueryWithRolesRestriction(params, 0, -1, false);

		for(Identity identity:loadIdentities) {
			double r = Math.random() * loadOres.size();
			int pos = (int)Math.round(r) - 1;
			if(pos < 40) {
				pos = 40;
			}
			List<RepositoryEntry> subEntries = loadOres.subList(pos - 30, pos);
			for(RepositoryEntry entry:subEntries) {
				OLATResource resource = entry.getOlatResource();
				userCourseInformationsManager.updateUserCourseInformations(resource.getResourceableId(), identity, true);
			}
			dbInstance.commitAndCloseSession();
		}
	}
	*/
	
	private static class UpdateThread extends Thread {
		
		private final DB db;
		private final CountDownLatch latch;
		private final UserCourseInformationsManager uciManager;
		
		private final OLATResource courseResource;
		private final Identity user;
		
		private int errors = 0;
		
		public UpdateThread(Identity user, OLATResource courseResource,
				UserCourseInformationsManager uciManager, CountDownLatch latch, DB db) {
			this.user = user;
			this.courseResource = courseResource;
			this.uciManager = uciManager;
			this.latch = latch;
			this.db = db;
		}

		public int getErrors() {
			return errors;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(10);
				for(int i=0; i<25;i++) {
					uciManager.updateUserCourseInformations(courseResource, user);
					uciManager.getUserCourseInformations(courseResource, user);
					uciManager.updateUserCourseInformations(courseResource, user);
					db.commitAndCloseSession();
				}
			} catch (Exception e) {
				log.error("", e);
				errors++;
			} finally {
				latch.countDown();
			}
		}
	}
}
