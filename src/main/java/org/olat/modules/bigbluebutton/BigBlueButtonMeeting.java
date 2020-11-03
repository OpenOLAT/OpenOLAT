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

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface BigBlueButtonMeeting extends ModifiedInfo, CreateInfo {
	
	public Long getKey();
	
	/**
	 * The meeting ID used when creating a BBB Meeting via API. In the BBB recording
	 * metadata this is referred as "externalId".
	 * 
	 * @return
	 */
	public String getMeetingId();
	
	public String getAttendeePassword();

	public String getModeratorPassword();
	
	/**
	 * The name of the meeting used in OpenOlat and BBB
	 * 
	 * @return
	 */
	public String getName();
	
	public void setName(String name);
	
	/**
	 * The description of the meeting is displayed in OpenOlat before joining
	 * 
	 * @return
	 */
	public String getDescription();
	
	public void setDescription(String description);
	
	/**
	 * The welcome message is displayed in BBB after joining the meeting. 
	 * 
	 * @return
	 */
	public String getWelcome();
	
	public void setWelcome(String welcome);
	
	/**
	 * The layout pre sets arrangements of cams and presentations
	 * @return
	 */
	public BigBlueButtonMeetingLayoutEnum getMeetingLayout();
	
	public void setMeetingLayout(BigBlueButtonMeetingLayoutEnum layout);
	
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
	 * Allow joining of external users via URL and name input field. 
	 * @return
	 */
	public boolean isGuest();
	
	public void setGuest(boolean guest);

	/**
	 * The identifier is used in the OpenOlat BBB dispatcher to not expose the real
	 * meeting ID to the user, e.g. when accessed by external guests. BBB does not now
	 * this ID, it is OO internal only. 
	 * 
	 * @return
	 */
	public String getIdentifier();

	/**
	 * The readable identifier is similar to the identifier but can be modified by
	 * users in the edit for to create a human readable URL when sending the meeting
	 * join url via email to participants. BBB does not now this ID, it is OO
	 * internal only.
	 * 
	 * @return
	 */
	public String getReadableIdentifier();

	public void setReadableIdentifier(String readableIdentifier);
	
	
	/**
	 * If not a permanent meeting, the meetings starts at this date. Participants
	 * and guests can join at this time.
	 * 
	 * @return
	 */
	public Date getStartDate();
	
	public void setStartDate(Date start);

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
	 * The calculated date when the BBB meeting can be opened technically
	 * @return
	 */
	public Date getStartWithLeadTime();


	/**
	 * If not a permanent meeting, the meetings ends at this date. 
	 * 
	 * @return
	 */
	public Date getEndDate();
	
	public void setEndDate(Date end);

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
	 * Flag to auto or manual publish the recordings
	 * 
	 * @return
	 */
	public BigBlueButtonRecordingsPublishingEnum getRecordingsPublishingEnum();

	public void setRecordingsPublishingEnum(BigBlueButtonRecordingsPublishingEnum publishing);
	
	public Boolean getRecord();

	public void setRecord(Boolean record);
	
	/**
	 * The plain text name of the presenter or main organizer. By default the name
	 * of the creator but can be change to anything. Metadata just for display
	 * purposes.
	 * 
	 * @return
	 */
	public String getMainPresenter();

	public void setMainPresenter(String name);

	/**
	 * The identity who create the meeting
	 * 
	 * @return
	 */
	public Identity getCreator();
	
	public BigBlueButtonMeetingTemplate getTemplate();

	public void setTemplate(BigBlueButtonMeetingTemplate template);
	
	/**
	 * The group if it is a group meeting. Can be null for non-group meetings. 
	 * @return
	 */
	public BusinessGroup getBusinessGroup();

	/**
	 * The course repository entry in which the meetings was created. Can be null,
	 * e.g. when it is a group meeting.
	 * 
	 * @return
	 */
	public RepositoryEntry getEntry();

	/**
	 * The sub identifier for the repository entry, normally the course node ID. Can
	 * be null.
	 * 
	 * @return
	 */
	public String getSubIdent();

	/**
	 * The server on which the meeting runs. Can be null if the meeting has not yet
	 * been created on the BBB server. Permanent meetings will always run on the
	 * same BBB server.
	 * 
	 * @return
	 */
	public BigBlueButtonServer getServer();
	
	public void setServer(BigBlueButtonServer server);
	
}
