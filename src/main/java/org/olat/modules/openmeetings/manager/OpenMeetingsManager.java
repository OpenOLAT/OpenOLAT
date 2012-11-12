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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openmeetings.manager;

import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.group.BusinessGroup;
import org.olat.modules.openmeetings.model.OpenMeetingsRecording;
import org.olat.modules.openmeetings.model.OpenMeetingsRoom;
import org.olat.modules.openmeetings.model.OpenMeetingsUser;

/**
 * 
 * Initial date: 06.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface OpenMeetingsManager {
	
	public String getOpenOLATExternalType();
	
	
	public String getURL(Identity identity, long roomId, String securedHash, Locale language);
	
	public String setUserToRoom(Identity identity, long roomId, boolean moderator)
	throws OpenMeetingsException;
	
	public String setGuestUserToRoom(String firstName, String lastName, long roomId, boolean moderator)
	throws OpenMeetingsException;
	
	public Long getRoomId(BusinessGroup group, OLATResourceable ores, String subIdentifier);
	
	public List<OpenMeetingsRoom> getOpenOLATRooms();

	public OpenMeetingsRoom getRoom(BusinessGroup group, OLATResourceable ores, String subIdentifier)
	throws OpenMeetingsException;
	
	public OpenMeetingsRoom addRoom(BusinessGroup group, OLATResourceable ores, String subIdentifier, OpenMeetingsRoom room);
	
	public OpenMeetingsRoom updateRoom(BusinessGroup group, OLATResourceable ores, String subIdentifier, OpenMeetingsRoom room);
	
	/**
	 * Open the room
	 * @param roomId
	 * @throws OpenMeetingsException
	 */
	public void openRoom(long roomId) throws OpenMeetingsException;
	
	/**
	 * Close the room
	 * @param roomId
	 * @throws OpenMeetingsException
	 */
	public void closeRoom(long roomId) throws OpenMeetingsException;
	
	public boolean deleteRoom(OpenMeetingsRoom room);
	
	public List<OpenMeetingsRecording> getRecordings(long roomId)
	throws OpenMeetingsException;
	
	/**
	 * Forge the recording URL
	 * @param filename
	 * @param roomId
	 * @param sid
	 * @return
	 */
	public String getRecordingURL(OpenMeetingsRecording recording, String sid);
	
	
	public List<OpenMeetingsUser> getUsersOf(OpenMeetingsRoom room);
	
	/**
	 * Kick all users from a room
	 * @param room
	 * @return
	 */
	public boolean removeUsersFromRoom(OpenMeetingsRoom room);
	
	/**
	 * Kick user of its meetings
	 * @param publicSID
	 * @return
	 */
	public boolean removeUser(String publicSID);
	
	
	public boolean checkConnection(String url, String login, String password)
		throws OpenMeetingsException;
	
	public void deleteAll(BusinessGroup group, OLATResourceable ores, String subIdentifier)
		throws OpenMeetingsException;

}
