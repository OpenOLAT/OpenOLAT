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

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.bigbluebutton.BigBlueButtonAttendee;
import org.olat.modules.bigbluebutton.BigBlueButtonAttendeeRoles;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.model.BigBlueButtonAttendeeImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 6 août 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class BigBlueButtonAttendeeDAO {
	
	@Autowired
	private DB dbInstance;
	
	public BigBlueButtonAttendee createAttendee(Identity identity, String pseudo, BigBlueButtonAttendeeRoles roles,
			Date joinDate, BigBlueButtonMeeting meeting) {
		BigBlueButtonAttendeeImpl attendee = new BigBlueButtonAttendeeImpl();
		attendee.setCreationDate(new Date());
		attendee.setLastModified(attendee.getCreationDate());
		if(roles != null) {
			attendee.setRole(roles.name());
		}
		attendee.setJoinDate(joinDate);
		attendee.setPseudo(pseudo);
		if(roles == BigBlueButtonAttendeeRoles.moderator || roles == BigBlueButtonAttendeeRoles.viewer) {
			attendee.setIdentity(identity);
		}
		attendee.setMeeting(meeting);
		dbInstance.getCurrentEntityManager().persist(attendee);
		return attendee;
	}
	
	public boolean hasAttendee(IdentityRef identity, BigBlueButtonMeeting meeting) {
		if(identity == null) return false;

		List<Long> attendeeKeys = dbInstance.getCurrentEntityManager()
			.createNamedQuery("hasAttendeeByIdentity", Long.class)
			.setParameter("identityKey", identity.getKey())
			.setParameter("meetingKey", meeting.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return attendeeKeys != null && !attendeeKeys.isEmpty()
				&& attendeeKeys.get(0) != null && attendeeKeys.get(0).longValue() > 0;
	}
	
	public boolean hasAttendee(String pseudo, BigBlueButtonMeeting meeting) {
		if(!StringHelper.containsNonWhitespace(pseudo)) return false;

		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select attendee.key from bigbluebuttonattendee as attendee")
		  .append(" where attendee.meeting.key=:meetingKey and ").lowerEqual("attendee.pseudo").append(":pseudo");
		
		List<Long> attendeeKeys = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("pseudo", pseudo.toLowerCase())
			.setParameter("meetingKey", meeting.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return attendeeKeys != null && !attendeeKeys.isEmpty()
				&& attendeeKeys.get(0) != null && attendeeKeys.get(0).longValue() > 0;
	}
	
	public BigBlueButtonAttendee getAttendee(IdentityRef identity, BigBlueButtonMeeting meeting) {
		StringBuilder sb = new StringBuilder();
		sb.append("select attendee from bigbluebuttonattendee as attendee")
		  .append(" inner join fetch attendee.identity as ident")
		  .append(" where ident.key=:identityKey and attendee.meeting.key=:meetingKey");
		
		List<BigBlueButtonAttendee> attendees = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), BigBlueButtonAttendee.class)
			.setParameter("identityKey", identity.getKey())
			.setParameter("meetingKey", meeting.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return attendees == null || attendees.isEmpty() ? null : attendees.get(0);
	}
	
	public int deleteAttendee(BigBlueButtonMeeting meeting) {
		String query = "delete from bigbluebuttonattendee as attendee where attendee.meeting.key=:meetingKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("meetingKey", meeting.getKey())
				.executeUpdate();
	}
	
	public int deleteAttendee(IdentityRef identity) {
		String query = "delete from bigbluebuttonattendee as attendee where attendee.identity.key=:identityKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}
}
