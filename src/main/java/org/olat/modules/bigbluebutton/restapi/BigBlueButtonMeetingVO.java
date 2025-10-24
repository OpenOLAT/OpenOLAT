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
package org.olat.modules.bigbluebutton.restapi;

import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;

/**
 * 
 * Initial date: 22 oct. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "bigBlueButtonMeetingVO")
public class BigBlueButtonMeetingVO {
	
	private Long key;
	private String name;
	private Date startDate;
	private long leadTime;
	private Date endDate;
	public long followupTime;
	private Long templateKey;
	
	public BigBlueButtonMeetingVO() {
		//
	}
	
	public static final BigBlueButtonMeetingVO valueOf(BigBlueButtonMeeting meeting) {
		BigBlueButtonMeetingVO vo = new BigBlueButtonMeetingVO();
		vo.setKey(meeting.getKey());
		vo.setName(meeting.getName());
		vo.setStartDate(meeting.getStartDate());
		vo.setLeadTime(meeting.getLeadTime());
		vo.setEndDate(meeting.getEndDate());
		vo.setFollowupTime(meeting.getFollowupTime());
		if(meeting.getTemplate() != null) {
			vo.setTemplateKey(meeting.getTemplate().getKey());
		}
		return vo;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Long getTemplateKey() {
		return templateKey;
	}

	public void setTemplateKey(Long templateKey) {
		this.templateKey = templateKey;
	}

	@Override
	public String toString() {
		return "BigBlueButtonMeetingVO[key=" + key + ":name=" + name + "]";
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
		if(obj instanceof BigBlueButtonMeetingVO template) {
			return key != null && key.equals(template.key);
		}
		return false;
	}
}
