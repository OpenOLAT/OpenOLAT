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
package org.olat.modules.teams.manager;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.model.TeamsMeetingsSearchParameters;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsMeetingQueriesTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TeamsMeetingDAO teamsMeetingDao;
	@Autowired
	private TeamsMeetingQueries teamsMeetingQueries;
	
	@Test
	public void countMeetings() {
		String name = "Search-Meeting - 1";
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-1");
		TeamsMeeting meeting1 = teamsMeetingDao.createMeeting(name, new Date(), new Date(),
				null, null, null, creator);
		TeamsMeeting meeting2 = teamsMeetingDao.createMeeting(name, new Date(), new Date(),
				null, null, null, creator);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(meeting1);
		Assert.assertNotNull(meeting2);
		
		TeamsMeetingsSearchParameters params = new TeamsMeetingsSearchParameters();
		long numOfMeetings = teamsMeetingQueries.count(params);
		Assert.assertTrue(numOfMeetings >= 2);
	}

}
