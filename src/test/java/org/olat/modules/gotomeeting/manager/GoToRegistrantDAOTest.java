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

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.modules.gotomeeting.GoToMeeting;
import org.olat.modules.gotomeeting.GoToOrganizer;
import org.olat.modules.gotomeeting.GoToRegistrant;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToRegistrantDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GoToMeetingDAO meetingDao;
	@Autowired
	private GoToOrganizerDAO organizerDao;
	@Autowired
	private GoToRegistrantDAO registrantDao;
	
	@Test
	public void createRegistrant() {
		String token = UUID.randomUUID().toString();
		Identity trainee = JunitTestHelper.createAndPersistIdentityAsRndUser("trainee-2");
		
		GoToOrganizer organizer = organizerDao
				.createOrganizer(null, token, token, token, token, null, null, null, null, 10l, null);
		Assert.assertNotNull(organizer);
		
		Date start = new Date();
		Date end = new Date();
		String trainingKey = Long.toString(CodeHelper.getForeverUniqueID());
		GoToMeeting training = meetingDao
				.createTraining("New training", null, "Very interessant", trainingKey, start, end, organizer, null, null, null);
		dbInstance.commit();
		
		//create registrant
		String registrantKey = Long.toString(CodeHelper.getForeverUniqueID());
		String joinUrl = "http://openolat.com/join/" + registrantKey;
		String confirmUrl = "http://openolat.com/confirm/" + registrantKey;
		GoToRegistrant registrant = registrantDao.createRegistrant(training, trainee, registrantKey, joinUrl, confirmUrl);
		Assert.assertNotNull(registrant);
		dbInstance.commit();
		
		//load
		GoToRegistrant reloadRegistrant = registrantDao.getRegistrant(training, trainee);
		Assert.assertNotNull(reloadRegistrant);
		Assert.assertEquals(registrant, reloadRegistrant);
	}
	
	@Test
	public void getRegistrants() {
		String token = UUID.randomUUID().toString();
		Identity trainee = JunitTestHelper.createAndPersistIdentityAsRndUser("trainee-3");
		
		GoToOrganizer organizer = organizerDao
				.createOrganizer(null, token, token, token, token, null, null, null, null, 10l, null);
		Assert.assertNotNull(organizer);
		
		Date start = new Date();
		Date end = new Date();
		String trainingKey = Long.toString(CodeHelper.getForeverUniqueID());
		GoToMeeting training = meetingDao
				.createTraining("New training", null, "Very interessant", trainingKey, start, end, organizer, null, null, null);
		dbInstance.commit();
		
		//create registrant
		String registrantKey = Long.toString(CodeHelper.getForeverUniqueID());
		String joinUrl = "http://openolat.com/join/" + registrantKey;
		String confirmUrl = "http://openolat.com/confirm/" + registrantKey;
		GoToRegistrant registrant = registrantDao.createRegistrant(training, trainee, registrantKey, joinUrl, confirmUrl);
		Assert.assertNotNull(registrant);
		dbInstance.commit();
		
		//load
		List<GoToRegistrant> reloadRegistrants = registrantDao.getRegistrants(trainee, null, null, null);
		Assert.assertNotNull(reloadRegistrants);
		Assert.assertEquals(1, reloadRegistrants.size());
		Assert.assertEquals(registrant, reloadRegistrants.get(0));
	}
}
