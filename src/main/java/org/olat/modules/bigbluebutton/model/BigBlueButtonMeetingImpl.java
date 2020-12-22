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
package org.olat.modules.bigbluebutton.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingLayoutEnum;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsPublishingEnum;
import org.olat.modules.bigbluebutton.BigBlueButtonServer;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="bigbluebuttonmeeting")
@Table(name="o_bbb_meeting")
public class BigBlueButtonMeetingImpl implements Persistable, BigBlueButtonMeeting {

	private static final long serialVersionUID = -4319750358887231779L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="b_name", nullable=false, insertable=true, updatable=true)
	private String name;
	@Column(name="b_description", nullable=true, insertable=true, updatable=true)
	private String description;
	@Column(name="b_welcome", nullable=true, insertable=true, updatable=true)
	private String welcome;
	
	@Column(name="b_start_date", nullable=true, insertable=true, updatable=true)
	private Date startDate;
	@Column(name="b_leadtime", nullable=true, insertable=true, updatable=true)
	private long leadTime;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="b_start_with_leadtime", nullable=true, insertable=true, updatable=true)
	private Date startWithLeadTime;
	
	@Column(name="b_end_date", nullable=true, insertable=true, updatable=true)
	private Date endDate;
	@Column(name="b_followuptime", nullable=true, insertable=true, updatable=true)
	private long followupTime;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="b_end_with_followuptime", nullable=true, insertable=true, updatable=true)
	private Date endWithFollowupTime;
	
	@Column(name="b_permanent", nullable=false, insertable=true, updatable=true)
	private boolean permanent;
	
	@Column(name="b_guest", nullable=false, insertable=true, updatable=true)
	private boolean guest;
	@Column(name="b_identifier", nullable=true, insertable=true, updatable=true)
	private String identifier;
	@Column(name="b_read_identifier", nullable=true, insertable=true, updatable=true)
	private String readableIdentifier;
	@Column(name="b_password", nullable=true, insertable=true, updatable=true)
	private String password;

	@Column(name="b_layout", nullable=false, insertable=true, updatable=true)
	private String layout;

	@Column(name="b_meeting_id", nullable=false, insertable=true, updatable=false)
	private String meetingId;
	@Column(name="b_attendee_pw", nullable=false, insertable=true, updatable=false)
	private String attendeePassword;
	@Column(name="b_moderator_pw", nullable=false, insertable=true, updatable=false)
	private String moderatorPassword;
	
	@Column(name="b_main_presenter", nullable=true, insertable=true, updatable=true)
	private String mainPresenter;

	@Column(name="b_recordings_publishing", nullable=true, insertable=true, updatable=true)
	private String recordingsPublishing;
	@Column(name="b_record", nullable=true, insertable=true, updatable=true)
	private Boolean record;
	
	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_creator_id", nullable=true, insertable=true, updatable=false)
	private Identity creator;
	
	@ManyToOne(targetEntity=RepositoryEntry.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_entry_id", nullable=true, insertable=true, updatable=false)
	private RepositoryEntry entry;
	@Column(name="a_sub_ident", nullable=true, insertable=true, updatable=false)
	private String subIdent;

	@ManyToOne(targetEntity=BusinessGroupImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_group_id", nullable=true, insertable=true, updatable=false)
	private BusinessGroup businessGroup;
	
	@ManyToOne(targetEntity=BigBlueButtonMeetingTemplateImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_template_id", nullable=true, insertable=true, updatable=true)
	private BigBlueButtonMeetingTemplate template;
	
	@ManyToOne(targetEntity=BigBlueButtonServerImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_server_id", nullable=true, insertable=true, updatable=true)
	private BigBlueButtonServer server;

	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String getMeetingId() {
		return meetingId;
	}

	public void setMeetingId(String meetingId) {
		this.meetingId = meetingId;
	}

	@Override
	public String getAttendeePassword() {
		return attendeePassword;
	}

	public void setAttendeePassword(String attendeePassword) {
		this.attendeePassword = attendeePassword;
	}

	@Override
	public String getModeratorPassword() {
		return moderatorPassword;
	}

	public void setModeratorPassword(String moderatorPassword) {
		this.moderatorPassword = moderatorPassword;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String getWelcome() {
		return welcome;
	}

	@Override
	public void setWelcome(String welcome) {
		this.welcome = welcome;
	}
	
	

	public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}

	@Override
	public BigBlueButtonMeetingLayoutEnum getMeetingLayout() {
		return BigBlueButtonMeetingLayoutEnum.secureValueOf(layout);
	}

	@Override
	public void setMeetingLayout(BigBlueButtonMeetingLayoutEnum meetingLayout) {
		if(meetingLayout == null) {
			layout = BigBlueButtonMeetingLayoutEnum.standard.name();
		} else {
			layout = meetingLayout.name();
		}
	}

	@Override
	public boolean isPermanent() {
		return permanent;
	}

	@Override
	public void setPermanent(boolean permanent) {
		this.permanent = permanent;
	}

	@Override
	public boolean isGuest() {
		return guest;
	}

	@Override
	public void setGuest(boolean guest) {
		this.guest = guest;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getReadableIdentifier() {
		return readableIdentifier;
	}

	@Override
	public void setReadableIdentifier(String readableIdentifier) {
		this.readableIdentifier = readableIdentifier;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public Date getStartDate() {
		return startDate;
	}

	@Override
	public void setStartDate(Date start) {
		this.startDate = start;
	}

	@Override
	public long getLeadTime() {
		return leadTime;
	}

	@Override
	public void setLeadTime(long leadTime) {
		this.leadTime = leadTime;
	}

	@Override
	public Date getStartWithLeadTime() {
		return startWithLeadTime;
	}

	public void setStartWithLeadTime(Date startWithLeadTime) {
		this.startWithLeadTime = startWithLeadTime;
	}

	@Override
	public Date getEndDate() {
		return endDate;
	}

	@Override
	public void setEndDate(Date end) {
		this.endDate = end;
	}

	@Override
	public long getFollowupTime() {
		return followupTime;
	}

	@Override
	public void setFollowupTime(long followupTime) {
		this.followupTime = followupTime;
	}

	@Override
	public Date getEndWithFollowupTime() {
		return endWithFollowupTime;
	}

	public void setEndWithFollowupTime(Date endWithFollowupTime) {
		this.endWithFollowupTime = endWithFollowupTime;
	}

	public String getRecordingsPublishing() {
		return recordingsPublishing;
	}

	public void setRecordingsPublishing(String recordingsPublishing) {
		this.recordingsPublishing = recordingsPublishing;
	}
	
	@Override
	public BigBlueButtonRecordingsPublishingEnum getRecordingsPublishingEnum() {
		return BigBlueButtonRecordingsPublishingEnum.secureValueOf(recordingsPublishing);
	}

	@Override
	public void setRecordingsPublishingEnum(BigBlueButtonRecordingsPublishingEnum recordingsPublishing) {
		if(recordingsPublishing == null) {
			this.recordingsPublishing = BigBlueButtonRecordingsPublishingEnum.auto.name();
		} else {
			this.recordingsPublishing = recordingsPublishing.name();
		}
	}

	@Override
	public Boolean getRecord() {
		return record;
	}

	@Override
	public void setRecord(Boolean record) {
		this.record = record;
	}

	@Override
	public String getMainPresenter() {
		return mainPresenter;
	}

	@Override
	public void setMainPresenter(String mainPresenter) {
		this.mainPresenter = mainPresenter;
	}

	@Override
	public Identity getCreator() {
		return creator;
	}

	public void setCreator(Identity creator) {
		this.creator = creator;
	}

	@Override
	public RepositoryEntry getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}

	@Override
	public String getSubIdent() {
		return subIdent;
	}

	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
	}

	@Override
	public BusinessGroup getBusinessGroup() {
		return businessGroup;
	}

	public void setBusinessGroup(BusinessGroup businessGroup) {
		this.businessGroup = businessGroup;
	}
	
	@Override
	public BigBlueButtonMeetingTemplate getTemplate() {
		return template;
	}

	@Override
	public void setTemplate(BigBlueButtonMeetingTemplate template) {
		this.template = template;
	}

	@Override
	public BigBlueButtonServer getServer() {
		return server;
	}

	public void setServer(BigBlueButtonServer server) {
		this.server = server;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 964210765 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof BigBlueButtonMeetingImpl) {
			BigBlueButtonMeetingImpl meeting = (BigBlueButtonMeetingImpl)obj;
			return getKey() != null && getKey().equals(meeting.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
