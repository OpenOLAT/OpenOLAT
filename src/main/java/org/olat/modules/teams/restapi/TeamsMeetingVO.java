/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.teams.restapi;

import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.core.util.StringHelper;
import org.olat.modules.teams.TeamsMeeting;

import com.microsoft.graph.models.OnlineMeetingPresenters;


/**
 * 
 * Initial date: 14 janv. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "teamsServerVO")
public class TeamsMeetingVO {
	
	private Long key;
	private String subject;
	private String description;
	private String mainPresenter;
	
	private Date startDate;
	private long leadTime;
	private Date endDate;
	private long followupTime;
	private Boolean permanent;
	
	private Boolean guest;
	private String readableIdentifier;
	private String allowedPresenters;
	
	public TeamsMeetingVO() {
		//
	}

	public static final TeamsMeetingVO valueOf(TeamsMeeting meeting) {
		TeamsMeetingVO vo = new TeamsMeetingVO();
		vo.setKey(meeting.getKey());
		vo.setSubject(meeting.getSubject());
		vo.setDescription(meeting.getDescription());
		vo.setMainPresenter(meeting.getMainPresenter());
		
		vo.setStartDate(meeting.getStartDate());
		vo.setLeadTime(meeting.getLeadTime());
		vo.setEndDate(meeting.getEndDate());
		vo.setFollowupTime(meeting.getFollowupTime());
		vo.setPermanent(meeting.isPermanent());
		
		vo.setGuest(meeting.isGuest());
		vo.setReadableIdentifier(meeting.getReadableIdentifier());
		vo.setAllowedPresenters(meeting.getAllowedPresenters());
		return vo;
	}
	
	public static final void transfer(TeamsMeetingVO meetingVo, TeamsMeeting meeting) {
		if(StringHelper.containsNonWhitespace(meetingVo.getSubject())) {
			meeting.setSubject(meetingVo.getSubject());
		}
		if(meetingVo.getDescription() != null) {
			meeting.setDescription(meetingVo.getDescription());
		}
		if(meetingVo.getMainPresenter() != null) {
			meeting.setMainPresenter(meetingVo.getMainPresenter());
		}
		
		if(meetingVo.getStartDate() != null) {
			meeting.setStartDate(meetingVo.getStartDate());
		}
		meeting.setLeadTime(meetingVo.getLeadTime());
		if(meetingVo.getEndDate() != null) {
			meeting.setEndDate(meetingVo.getEndDate());
		}
		meeting.setFollowupTime(meetingVo.getFollowupTime());
		if(meetingVo.getPermanent() != null) {
			meeting.setPermanent(meetingVo.getPermanent().booleanValue());
		}
		
		if(meetingVo.getReadableIdentifier() != null) {
			meeting.setReadableIdentifier(meetingVo.getReadableIdentifier());
		}
		if(meetingVo.getGuest() != null) {
			meeting.setGuest(meetingVo.getGuest().booleanValue());
		}
		
		if(StringHelper.containsNonWhitespace(meetingVo.getAllowedPresenters())) {
			String presented = meetingVo.getAllowedPresenters();
			for(OnlineMeetingPresenters allowed:OnlineMeetingPresenters.values()) {
				if(allowed.name().equalsIgnoreCase(presented)) {
					meeting.setAllowedPresenters(allowed.name());
				}
			}
		}
	}
	
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public long getLeadTime() {
		return leadTime;
	}

	public void setLeadTime(long leadTime) {
		this.leadTime = leadTime;
	}

	public long getFollowupTime() {
		return followupTime;
	}

	public void setFollowupTime(long followupTime) {
		this.followupTime = followupTime;
	}

	public Boolean getPermanent() {
		return permanent;
	}

	public void setPermanent(Boolean permanent) {
		this.permanent = permanent;
	}

	public String getMainPresenter() {
		return mainPresenter;
	}

	public void setMainPresenter(String mainPresenter) {
		this.mainPresenter = mainPresenter;
	}

	public String getReadableIdentifier() {
		return readableIdentifier;
	}

	public void setReadableIdentifier(String readableIdentifier) {
		this.readableIdentifier = readableIdentifier;
	}

	public Boolean getGuest() {
		return guest;
	}

	public void setGuest(Boolean guest) {
		this.guest = guest;
	}

	public String getAllowedPresenters() {
		return allowedPresenters;
	}

	public void setAllowedPresenters(String allowedPresenters) {
		this.allowedPresenters = allowedPresenters;
	}

	@Override
	public String toString() {
		return "TeamsMeetingVO[key=" + key + ":subject=" + subject + "]";
	}
	
	@Override
	public int hashCode() {
		return key == null ? 32948 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof TeamsMeetingVO meeting) {
			return key != null && key.equals(meeting.key);
		}
		return false;
	}
}
