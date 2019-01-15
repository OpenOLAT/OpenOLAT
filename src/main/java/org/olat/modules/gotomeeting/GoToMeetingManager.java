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
package org.olat.modules.gotomeeting;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.modules.gotomeeting.model.GoToError;
import org.olat.modules.gotomeeting.model.GoToOrganizerG2T;
import org.olat.modules.gotomeeting.model.GoToRecordingsG2T;
import org.olat.modules.gotomeeting.model.GoToType;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 22.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface GoToMeetingManager {
	
	public boolean createOrUpdateOrganizer(GoToOrganizerG2T organizer);
	
	public boolean createOrUpdateOrganizer(String name, String username, String password, Identity owner, GoToError error);
	
	public boolean refreshToken(GoToOrganizer organizer);
	
	public void updateOrganizer(GoToOrganizer organizer, String name);
	
	/**
	 * @return The list of all organizers on the system.
	 */
	public List<GoToOrganizer> getOrganizers();

	/**
	 * 
	 * @return the list of system-wide organizers 
	 */
	public List<GoToOrganizer> getSystemOrganizers();
	
	/**
	 * Return the organizers with the specific account and for the specified organizer.
	 * 
	 * @param accountKey The account
	 * @param organizerKey The organizer
	 * @return A list with only one mapping of an organizer
	 */
	public List<GoToOrganizer> getOrganizers(String accountKey, String organizerKey);
	
	/**
	 * Return the list of system wide organizers and the one which
	 * the user has created (if any).
	 * @param identity
	 * @return A lsit of organizers
	 */
	public List<GoToOrganizer> getOrganizersFor(Identity identity);
	
	/**
	 * 
	 * @param organizer
	 * @return
	 */
	public boolean removeOrganizer(GoToOrganizer organizer);
	
	/**
	 * Check if the organizer is available between the 2 specified dates.
	 * 
	 * @param organizer
	 * @param start
	 * @param end
	 * @return
	 */
	public boolean checkOrganizerAvailability(GoToOrganizer organizer, Date start, Date end);
	
	
	public GoToMeeting scheduleTraining(GoToOrganizer organizer, String name, String externalId, String description, Date start, Date end,
			RepositoryEntry resourceOwner, String subIdentifier, BusinessGroup businessGroup, GoToError error);
	
	/**
	 * Update name / description and the first session start and end date.
	 * @param meeting
	 * @param name
	 * @param description
	 * @param start
	 * @param end
	 * @return
	 */
	public GoToMeeting updateTraining(GoToMeeting meeting, String name, String description, Date start, Date end, GoToError error);
	
	/**
	 * Admin. method to retrieve all meetings / training / webinars booked through OpenOLAT.
	 * 
	 * @return
	 */
	public List<GoToMeeting> getAllMeetings();
	
	public List<GoToMeeting> getMeetings(GoToType type, RepositoryEntryRef entry, String subIdent, BusinessGroupRef businessGroup);
	
	/**
	 * Reload the meeting from the database AND from the
	 * GoTo server.
	 * 
	 * @param meeting
	 * @return
	 */
	public GoToMeeting getMeeting(GoToMeeting meeting, GoToError error);
	
	/**
	 * Load from the OpenOLAT system only.
	 * @param meetingKey
	 * @return
	 */
	public GoToMeeting getMeetingByKey(Long meetingKey);
	
	/**
	 * Load from the OpenOLAT system only. 
	 * @param externalId
	 * @return
	 */
	public GoToMeeting getMeetingByExternalId(String externalId);
	
	/**
	 * Register or update the registrant of a specified user
	 * 
	 * @param meeting
	 * @param trainee
	 * @return
	 */
	public GoToRegistrant registerTraining(GoToMeeting meeting, Identity trainee, GoToError error);
	
	public GoToRegistrant getRegistrant(GoToMeeting meeting, IdentityRef trainee);
	
	public String startTraining(GoToMeeting meeting, GoToError error);
	
	public List<GoToRecordingsG2T> getRecordings(GoToMeeting meeting, GoToError error);
	
	
	/**
	 * Return the list of registrants for a specified user.
	 * 
	 * @param identity
	 * @param entry
	 * @param subIdent
	 * @param businessGroup
	 * @return
	 */
	public List<GoToRegistrant> getRegistrants(IdentityRef identity, RepositoryEntryRef entry, String subIdent, BusinessGroupRef businessGroup);
	

	public boolean delete(GoToMeeting meeting);
	
	public void deleteAll(RepositoryEntryRef entry, String subIdent, BusinessGroupRef businessGroup);
	
	

}
