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

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.ICourse;
import org.olat.course.assessment.UserCourseInformations;
import org.olat.restapi.repository.course.CoursesWebService;
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
	
	private static final OLog log = Tracing.createLoggerFor(UserCourseInformationsManagerTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserCourseInformationsManager userCourseInformationsManager;
	
	@Test
	public void createUpdateCourseInfos() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsUser("user-launch-2-" + UUID.randomUUID().toString());
		ICourse course = CoursesWebService.createEmptyCourse(user, "course-launch-dates", "course long name", null);
		dbInstance.commitAndCloseSession();
		
		userCourseInformationsManager.updateUserCourseInformations(course.getResourceableId(), user);
		dbInstance.commitAndCloseSession();
		
		UserCourseInformations infos = userCourseInformationsManager.getUserCourseInformations(course.getResourceableId(), user);
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
	public void updateSetLaunchDates_concurrent() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsUser("user-launch-1-" + UUID.randomUUID().toString());
		ICourse course = CoursesWebService.createEmptyCourse(user, "course-launch-dates", "course long name", null);
		dbInstance.commitAndCloseSession();

		int numOfThreads = 25;
		final CountDownLatch doneSignal = new CountDownLatch(numOfThreads);
		
		SetLaunchDatesThread[] threads = new SetLaunchDatesThread[numOfThreads];
		for(int i=numOfThreads; i-->0; ) {
			threads[i] = new SetLaunchDatesThread(user, course.getResourceableId(), doneSignal);
		}
		
		for(int i=numOfThreads; i-->0; ) {
			threads[i].start();
		}

		try {
			boolean interrupt = doneSignal.await(240, TimeUnit.SECONDS);
			Assert.assertTrue("Test takes too long (more than 10s)", interrupt);
		} catch (InterruptedException e) {
			Assert.fail("" + e.getMessage());
		}
		
		int countError = 0;
		for(int i=numOfThreads; i-->0; ) {
			countError += threads[i].getErrorCount();
		}
		Assert.assertEquals(0, countError);

		UserCourseInformations infos = userCourseInformationsManager.getUserCourseInformations(course.getResourceableId(), user);
		Assert.assertEquals(1250, infos.getVisit());
	}
	
	
	private class SetLaunchDatesThread extends Thread {
		
		private AtomicInteger errorCounter = new AtomicInteger();
		
		private final Identity user;
		private final Long courseResourceableId;
		private final CountDownLatch doneSignal;
		
		public SetLaunchDatesThread(Identity user, Long courseResourceableId, CountDownLatch doneSignal) {
			this.user = user;
			this.doneSignal = doneSignal;
			this.courseResourceableId = courseResourceableId;
		}
		
		public int getErrorCount() {
			return errorCounter.get();
		}
	
		@Override
		public void run() {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				log.error("", e);
			}
			
			UserCourseInformationsManager infoManager = CoreSpringFactory.getImpl(UserCourseInformationsManager.class);
			try {
				for(int i=0; i<50; i++) {
					infoManager.updateUserCourseInformations(courseResourceableId, user);
				}
			} catch (Exception e) {
				errorCounter.incrementAndGet();
				log.error("", e);
			} finally {
				doneSignal.countDown();
			}
		}
	}
}
