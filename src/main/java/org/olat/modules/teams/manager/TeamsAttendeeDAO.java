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
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.teams.TeamsAttendee;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsUser;
import org.olat.modules.teams.model.TeamsAttendeeImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 15 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TeamsAttendeeDAO {
	
	@Autowired
	private DB dbInstance;
	
	public TeamsAttendee createAttendee(Identity identity, TeamsUser user, String role, Date joinDate, TeamsMeeting meeting) {
		TeamsAttendeeImpl attendee = new TeamsAttendeeImpl();
		attendee.setCreationDate(new Date());
		attendee.setLastModified(attendee.getCreationDate());
		attendee.setRole(role);
		attendee.setJoinDate(joinDate);
		attendee.setIdentity(identity);
		attendee.setTeamsUser(user);
		attendee.setMeeting(meeting);
		dbInstance.getCurrentEntityManager().persist(attendee);
		return attendee;
	}
	
	public boolean hasAttendee(IdentityRef identity, TeamsMeeting meeting) {
		if(identity == null) return false;

		List<Long> attendeeKeys = dbInstance.getCurrentEntityManager()
			.createNamedQuery("hasTeamsAttendeeByIdentity", Long.class)
			.setParameter("identityKey", identity.getKey())
			.setParameter("meetingKey", meeting.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return attendeeKeys != null && !attendeeKeys.isEmpty()
				&& attendeeKeys.get(0) != null && attendeeKeys.get(0).longValue() > 0;
	}

}
