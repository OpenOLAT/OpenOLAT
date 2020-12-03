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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingAdminInfos;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingsSearchParameters;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonMeetingQueriesTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BigBlueButtonMeetingDAO bigBlueButtonMeetingDao;
	@Autowired
	private BigBlueButtonMeetingQueries bigBlueButtonMeetingQueries;
	
	@Test
	public void countMeetings() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("bigbluebutton-queries-1");
		BigBlueButtonMeeting meeting1 = bigBlueButtonMeetingDao.createAndPersistMeeting("Count-Meeting - 1", null, null, null, creator);
		BigBlueButtonMeeting meeting2 = bigBlueButtonMeetingDao.createAndPersistMeeting("Count-Meeting - 2", null, null, null, creator);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(meeting1);
		Assert.assertNotNull(meeting2);
		
		BigBlueButtonMeetingsSearchParameters params = new BigBlueButtonMeetingsSearchParameters();
		long numOfMeetings = bigBlueButtonMeetingQueries.count(params);
		Assert.assertTrue(numOfMeetings >= 2);
	}
	
	@Test
	public void searchMeetings() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("bigbluebutton-queries-1");
		String prefix = UUID.randomUUID().toString();
		BigBlueButtonMeeting meeting1 = bigBlueButtonMeetingDao.createAndPersistMeeting(prefix + " Search-Meeting - 1", null, null, null, creator);
		BigBlueButtonMeeting meeting2 = bigBlueButtonMeetingDao.createAndPersistMeeting(prefix + " Search-Meeting - 2", null, null, null, creator);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(meeting1);
		Assert.assertNotNull(meeting2);
		
		BigBlueButtonMeetingsSearchParameters params = new BigBlueButtonMeetingsSearchParameters();
		params.setSearchString(prefix);
		List<BigBlueButtonMeetingAdminInfos> meetings = bigBlueButtonMeetingQueries.search(params, 0, 2);
		assertThat(meetings)
			.isNotNull()
			.extracting(meetingAdmin -> meetingAdmin.getMeeting())
			.containsExactlyInAnyOrder(meeting1, meeting2);
	}
	
	@Test
	public void searchMeetingsCheckOrderBy() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("bigbluebutton-queries-3");
		BigBlueButtonMeeting meeting = bigBlueButtonMeetingDao.createAndPersistMeeting("Search-Meeting - 3", null, null, null, creator);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(meeting);
		
		BigBlueButtonMeetingsSearchParameters params = new BigBlueButtonMeetingsSearchParameters();
		for(BigBlueButtonMeetingsSearchParameters.OrderBy orderBy: BigBlueButtonMeetingsSearchParameters.OrderBy.values()) {
			params.setOrder(orderBy);
			params.setOrderAsc(true);
			List<BigBlueButtonMeetingAdminInfos> meetingsAsc = bigBlueButtonMeetingQueries.search(params, 0, 32);
			Assert.assertNotNull(meetingsAsc);
			
			params.setOrderAsc(false);
			List<BigBlueButtonMeetingAdminInfos> meetingsDesc = bigBlueButtonMeetingQueries.search(params, 0, 32);
			Assert.assertNotNull(meetingsDesc);
		}
	}
}
