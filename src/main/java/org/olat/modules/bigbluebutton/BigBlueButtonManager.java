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
package org.olat.modules.bigbluebutton;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.UserSession;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.group.BusinessGroup;
import org.olat.modules.bigbluebutton.manager.BigBlueButtonUriBuilder;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.bigbluebutton.model.BigBlueButtonRecordingWithReference;
import org.olat.modules.bigbluebutton.model.BigBlueButtonServerInfos;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.w3c.dom.Document;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface BigBlueButtonManager {
	
	public BigBlueButtonServer createServer(String url, String recordingUrl, String sharedSecret);
	
	public BigBlueButtonServer updateServer(BigBlueButtonServer server);
	
	public boolean hasServer(String url);
	
	public List<BigBlueButtonServer> getServers();
	
	/**
	 * @param key The primary key of the server
	 * @return A server or null if not found
	 */
	public BigBlueButtonServer getServer(Long key);
	
	public List<BigBlueButtonServerInfos> getServersInfos();
	
	public List<BigBlueButtonServerInfos> filterServersInfos(List<BigBlueButtonServerInfos> infos);
	
	/**
	 * @return The active recordings handler
	 */
	public BigBlueButtonRecordingsHandler getRecordingsHandler();
	
	/**
	 * @return The list of available recordings handlers
	 */
	public List<BigBlueButtonRecordingsHandler> getRecordingsHandlers();
	
	
	public void deleteServer(BigBlueButtonServer server, BigBlueButtonErrors errors);
	
	
	/**
	 * Create and persist a meeting in OpenOlat. The method will generate
	 * an unique meeting identifier and passwords for attendees and moderators.
	 * 
	 * @param name The name of the meeting
	 * @param entry The repository entry (optional but this or group)
	 * @param subIdent The sub-identifier (optional)
	 * @param businessGroup The business group (optional but this or entry)
	 * @param creator Who creates the meeting
	 * @return A meeting with some default values
	 */
	public BigBlueButtonMeeting createAndPersistMeeting(String name, RepositoryEntry entry, String subIdent,
			BusinessGroup businessGroup, Identity creator);
	
	/**
	 * Is there a server available.
	 * 
	 * @param template The selected template
	 * @param start Start date
	 * @param leadTime Lead time
	 * @param end End date
	 * @param followupTime Follow-up time
	 * @return true if the meeting can be reserved
	 */
	public boolean isSlotAvailable(BigBlueButtonMeeting meeting, BigBlueButtonMeetingTemplate template, Date start, long leadTime, Date end, long followupTime);

	public BigBlueButtonMeeting getMeeting(BigBlueButtonMeeting meeting);
	
	public VFSContainer getSlidesContainer(BigBlueButtonMeeting meeting);
	
	/**
	 * The method will create a meeting and uploaded the slides to the
	 * BigBlueButton server but it will only doing it during the leading
	 * time.
	 * 
	 * @param meetingKey The meeting primary key
	 * @return true if slides were effectively uploaded
	 */
	public boolean preloadSlides(Long meetingKey);
	
	/**
	 * Return the first meeting which matches the specified identifier
	 * as the meeting's identifier or readable identifier.
	 * 
	 * @param identifier The identifier
	 * @return A meeting
	 */
	public BigBlueButtonMeeting getMeeting(String identifier);
	
	public BigBlueButtonMeeting updateMeeting(BigBlueButtonMeeting meeting);
	
	public boolean deleteMeeting(BigBlueButtonMeeting meeting, BigBlueButtonErrors errors);
	
	public BigBlueButtonMeetingTemplate createAndPersistTemplate(String name);
	
	public boolean isIdentifierInUse(String identifier, BigBlueButtonMeeting reference);

	public List<BigBlueButtonMeetingTemplate> getTemplates();
	
	public List<BigBlueButtonMeetingTemplate> getTemplates(List<BigBlueButtonTemplatePermissions> permissions);

	/**
	 * Calculate the permissions of the specified identity for the repository entry or business group.
	 * 
	 * @param entry The repository entry (optional)
	 * @param businessGroup The business group (optional)
	 * @param identity The identity
	 * @param userRoles The roles of the identity
	 * @return
	 */
	public List<BigBlueButtonTemplatePermissions> calculatePermissions(RepositoryEntry entry, BusinessGroup businessGroup, Identity identity, Roles userRoles);
	
	public BigBlueButtonMeetingTemplate updateTemplate(BigBlueButtonMeetingTemplate template);
	
	public void deleteTemplate(BigBlueButtonMeetingTemplate template);
	
	public boolean isTemplateInUse(BigBlueButtonMeetingTemplate template);
	
	public List<BigBlueButtonMeeting> getMeetings(RepositoryEntryRef entry, String subIdent, BusinessGroup businessGroup, boolean guestOnly);
	
	/**
	 * Return the list of upcoming meetings, without any permanent one.
	 * 
	 * @param entry The course / resource entry
	 * @param subIdent The sub identifier
	 * @return
	 */
	public List<BigBlueButtonMeeting> getUpcomingsMeetings(RepositoryEntryRef entry, String subIdent, int maxResults);
	
	public List<BigBlueButtonMeeting> getAllMeetings();
	
	public String join(BigBlueButtonMeeting meeting, Identity identity, String pseudo, BigBlueButtonAttendeeRoles role,
			Boolean isRunning, BigBlueButtonErrors errors);
	
	public boolean isMeetingRunning(BigBlueButtonMeeting meeting);
	
	/**
	 * Synchronizes the recordings of the storage in to the database
	 *
	 * @param endFrom filter the meeting by the end of the meeting 
	 * @param endTo filter the meeting by the end of the meeting 
	 * @param syncPermanent sync permanent meetings
	 */
	public void syncReferences(Date endFrom, Date endTo, boolean syncPermanent);
	
	public List<BigBlueButtonRecordingWithReference> getRecordingAndReferences(BigBlueButtonMeeting meeting, BigBlueButtonErrors errors);

	/**
	 * Get the recordings of the meetings from the database. The recordings are not
	 * synchronized between the recording storage and the database in advance.
	 *
	 * @param meetings
	 * @return
	 */
	public List<BigBlueButtonRecordingReference> getRecordingReferences(Collection<BigBlueButtonMeeting> meetings);
	
	public BigBlueButtonRecordingReference updateRecordingReference(BigBlueButtonRecordingReference reference);
	
	public String getRecordingUrl(UserSession usess, BigBlueButtonRecording record);
	
	public void deleteRecording(BigBlueButtonRecording record, BigBlueButtonMeeting meeting, BigBlueButtonErrors errors);
	
	
	public BigBlueButtonAttendee getAttendee(Identity identity, BigBlueButtonMeeting meeting);
	
	/**
	 * Factory method to create an URI builder which can calculate
	 * the checksum for BigBlueButton.
	 * 
	 * @param server The server
	 * @return An URI builder
	 */
	public BigBlueButtonUriBuilder getUriBuilder(BigBlueButtonServer server);
	
	public Document sendRequest(BigBlueButtonUriBuilder builder, BigBlueButtonErrors errors);
	
	public boolean checkConnection(String url, String sharedSecret, BigBlueButtonErrors errors);

	
	

}
