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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
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

	@Autowired
	private DB dbInstance;
	@Autowired
	private UserCourseInformationsManager userCourseInformationsManager;
	
	@Test
	public void createUpdateCourseInfos() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsUser("user-launch-1-" + UUID.randomUUID().toString());
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
	public void getInitialLaunchDate() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsUser("user-launch-2-" + UUID.randomUUID().toString());
		ICourse course = CoursesWebService.createEmptyCourse(user, "course-launch-dates", "course long name", null);
		dbInstance.commitAndCloseSession();
		
		userCourseInformationsManager.updateUserCourseInformations(course.getResourceableId(), user);
		dbInstance.commitAndCloseSession();
		
		Date launchDate = userCourseInformationsManager.getInitialLaunchDate(course.getResourceableId(), user);
		Assert.assertNotNull(launchDate);
	}
	
	@Test
	public void getInitialLaunchDates() {
		Identity user1 = JunitTestHelper.createAndPersistIdentityAsUser("user-launch-3-" + UUID.randomUUID().toString());
		Identity user2 = JunitTestHelper.createAndPersistIdentityAsUser("user-launch-4-" + UUID.randomUUID().toString());
		ICourse course = CoursesWebService.createEmptyCourse(user1, "course-launch-dates", "course long name", null);
		dbInstance.commitAndCloseSession();
		
		userCourseInformationsManager.updateUserCourseInformations(course.getResourceableId(), user1);
		userCourseInformationsManager.updateUserCourseInformations(course.getResourceableId(), user2);
		dbInstance.commitAndCloseSession();
		
		List<Identity> users = new ArrayList<Identity>();
		users.add(user1);
		users.add(user2);

		Map<Long,Date> launchDates = userCourseInformationsManager.getInitialLaunchDates(course.getResourceableId(), users);
		Assert.assertNotNull(launchDates);
		Assert.assertEquals(2, launchDates.size());
		Assert.assertTrue(launchDates.containsKey(user1.getKey()));
		Assert.assertNotNull(launchDates.get(user1.getKey()));
		Assert.assertTrue(launchDates.containsKey(user2.getKey()));
		Assert.assertNotNull(launchDates.get(user2.getKey()));
	}
}
