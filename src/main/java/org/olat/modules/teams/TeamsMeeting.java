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
package org.olat.modules.teams;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;

import com.microsoft.graph.models.LobbyBypassScope;
import com.microsoft.graph.models.OnlineMeetingPresenters;

/**
 * 
 * Initial date: 20 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface TeamsMeeting extends ModifiedInfo, CreateInfo {
	
	public Long getKey();
	
	public String getSubject();

	public void setSubject(String subject);
	
	public String getDescription();
	
	public void setDescription(String description);
	
	public String getMainPresenter();
	
	public void setMainPresenter(String mainPresenter);
	
	public Date getStartDate();
	
	public void setStartDate(Date startDate);
	
	/**
	 * The minutes a meeting can be joined by the coaches prior to the start time,
	 * e.g. for preparation of slides. Is not taken into account room quota
	 * calculation.
	 * 
	 * @return
	 */
	public long getLeadTime();
	
	public void setLeadTime(long leadTime);

	/**
	 * The calculated date when the Teams meeting can be opened technically
	 * @return
	 */
	public Date getStartWithLeadTime();
	
	public Date getEndDate();
	
	public void setEndDate(Date startDate);
	
	/**
	 * The minutes a meeting can be over time. Is not taken into account room quota
	 * calculation.
	 * 
	 * @return
	 */
	public long getFollowupTime();
	
	public void setFollowupTime(long followupTime);

	/**
	 * The calculated latest date when the BBB meeting is closed automatically
	 * 
	 * @return
	 */
	public Date getEndWithFollowupTime();
	
	/**
	 * Permanent meetings have no start and end date. E.g. for groups who want to
	 * make spontaneous meetings without scheduling meetings in the first place.
	 * Note that permanent can quickly consume the available meeting quota as the
	 * count as current active meetings.
	 * 
	 * @return
	 */
	public boolean isPermanent();
	
	public void setPermanent(boolean permanent);
	
	/**
	 * Allow joining of external users via URL.
	 * @return
	 */
	public boolean isGuest();
	
	public void setGuest(boolean guest);

	/**
	 * The identifier is used in the OpenOlat dispatcher to not expose the real
	 * meeting ID to the user, e.g. when accessed by external guests. Teams does not now
	 * this ID, it is OO internal only. 
	 * 
	 * @return
	 */
	public String getIdentifier();

	/**
	 * The readable identifier is similar to the identifier but can be modified by
	 * users in the edit for to create a human readable URL when sending the meeting
	 * join URL via email to participants. Teams does not now this ID, it is OpenOlat
	 * internal only.
	 * 
	 * @return
	 */
	public String getReadableIdentifier();

	public void setReadableIdentifier(String readableIdentifier);
	
	public String getJoinInformation();
	
	public void setJoinInformation(String text);
	
	public boolean isParticipantsCanOpen();

	public void setParticipantsCanOpen(boolean participantsCanOpen);
	
	public String getOnlineMeetingId();
	
	public String getOnlineMeetingJoinUrl();
	
	public String getAllowedPresenters();
	
	public void setAllowedPresenters(String allowedPresenters);
	
	public OnlineMeetingPresenters getAllowedPresentersEnum();
	
	public String getAccessLevel();
	
	public void setAccessLevel(String accessLevel);
	
	public String getLobbyBypassScope();
	
	public void setLobbyBypassScope(String scope);

	public LobbyBypassScope getLobbyBypassScopeEnum();
	
	public boolean isEntryExitAnnouncement();
	
	public void setEntryExitAnnouncement(boolean entryExitAnnouncement);
	
	public RepositoryEntry getEntry();

	public String getSubIdent();

	public BusinessGroup getBusinessGroup();
	
	public Identity getCreator();

}
