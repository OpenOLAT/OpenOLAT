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
package org.olat.modules.teams.model;

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
import javax.persistence.Transient;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.manager.MicrosoftGraphDAO;
import org.olat.repository.RepositoryEntry;

import com.microsoft.graph.models.generated.LobbyBypassScope;
import com.microsoft.graph.models.generated.OnlineMeetingPresenters;

/**
 * 
 * Initial date: 20 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="teamsmeeting")
@Table(name="o_teams_meeting")
public class TeamsMeetingImpl implements Persistable, TeamsMeeting {

	private static final long serialVersionUID = 1561074519446860579L;

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
	
	@Column(name="t_subject", nullable=false, insertable=true, updatable=true)
	private String subject;
	@Column(name="t_description", nullable=true, insertable=true, updatable=true)
	private String description;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="t_start_date", nullable=true, insertable=true, updatable=true)
	private Date startDate;
	@Column(name="t_leadtime", nullable=true, insertable=true, updatable=true)
	private long leadTime;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="t_start_with_leadtime", nullable=true, insertable=true, updatable=true)
	private Date startWithLeadTime;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="t_end_date", nullable=true, insertable=true, updatable=true)
	private Date endDate;
	@Column(name="t_followuptime", nullable=true, insertable=true, updatable=true)
	private long followupTime;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="t_end_with_followuptime", nullable=true, insertable=true, updatable=true)
	private Date endWithFollowupTime;
	
	@Column(name="t_permanent", nullable=false, insertable=true, updatable=true)
	private boolean permanent;
	
	@Column(name="t_main_presenter", nullable=true, insertable=true, updatable=true)
	private String mainPresenter;

	@Column(name="t_allowed_presenters", nullable=true, insertable=true, updatable=true)
	private String allowedPresenters;
	@Column(name="t_access_level", nullable=true, insertable=true, updatable=true)
	private String accessLevel;
	@Column(name="t_lobby_bypass_scope", nullable=true, insertable=true, updatable=true)
	private String lobbyBypassScope;
	@Column(name="t_entry_exit_announcement", nullable=true, insertable=true, updatable=true)
	private boolean entryExitAnnouncement;
	@Column(name="t_join_information", nullable=true, insertable=true, updatable=true)
	private String joinInformation;

	@Column(name="t_guest", nullable=false, insertable=true, updatable=true)
	private boolean guest;
	@Column(name="t_identifier", nullable=true, insertable=true, updatable=true)
	private String identifier;
	@Column(name="t_read_identifier", nullable=true, insertable=true, updatable=true)
	private String readableIdentifier;
	
	@Column(name="t_online_meeting_id", nullable=true, insertable=true, updatable=true)
	private String onlineMeetingId;
	@Column(name="t_online_meeting_join_url", nullable=true, insertable=true, updatable=true)
	private String onlineMeetingJoinUrl;
	
	@ManyToOne(targetEntity=RepositoryEntry.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_entry_id", nullable=true, insertable=true, updatable=false)
	private RepositoryEntry entry;
	@Column(name="a_sub_ident", nullable=true, insertable=true, updatable=false)
	private String subIdent;

	@ManyToOne(targetEntity=BusinessGroupImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_group_id", nullable=true, insertable=true, updatable=false)
	private BusinessGroup businessGroup;
	
	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_creator_id", nullable=true, insertable=true, updatable=false)
	private Identity creator;
	
	
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
	public String getSubject() {
		return subject;
	}

	@Override
	public void setSubject(String subject) {
		this.subject = subject;
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
	public Date getStartDate() {
		return startDate;
	}

	@Override
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
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
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
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
	public String getJoinInformation() {
		return joinInformation;
	}

	@Override
	public void setJoinInformation(String text) {
		this.joinInformation = text;
	}

	@Override
	public String getOnlineMeetingId() {
		return onlineMeetingId;
	}

	public void setOnlineMeetingId(String onlineMeetingId) {
		this.onlineMeetingId = onlineMeetingId;
	}

	@Override
	public String getOnlineMeetingJoinUrl() {
		return onlineMeetingJoinUrl;
	}

	public void setOnlineMeetingJoinUrl(String onlineMeetingJoinUrl) {
		this.onlineMeetingJoinUrl = onlineMeetingJoinUrl;
	}

	@Override
	public String getAllowedPresenters() {
		return allowedPresenters;
	}
	
	@Override
	@Transient
	public OnlineMeetingPresenters getAllowedPresentersEnum() {
		if(StringHelper.containsNonWhitespace(allowedPresenters)) {
			return MicrosoftGraphDAO.toOnlineMeetingPresenters(allowedPresenters);
		}
		return null;
	}

	@Override
	public void setAllowedPresenters(String allowedPresenters) {
		this.allowedPresenters = allowedPresenters;
	}

	@Override
	public String getAccessLevel() {
		return accessLevel;
	}

	@Override
	public void setAccessLevel(String accessLevel) {
		this.accessLevel = accessLevel;
	}

	@Override
	public String getLobbyBypassScope() {
		return lobbyBypassScope;
	}

	@Override
	public void setLobbyBypassScope(String lobbyBypassScope) {
		this.lobbyBypassScope = lobbyBypassScope;
	}

	@Override
	@Transient
	public LobbyBypassScope getLobbyBypassScopeEnum() {
		if(StringHelper.containsNonWhitespace(lobbyBypassScope)) {
			return MicrosoftGraphDAO.toLobbyBypassScope(lobbyBypassScope);
		}
		return null;
	}

	@Override
	public boolean isEntryExitAnnouncement() {
		return entryExitAnnouncement;
	}

	@Override
	public void setEntryExitAnnouncement(boolean entryExitAnnouncement) {
		this.entryExitAnnouncement = entryExitAnnouncement;
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
	public Identity getCreator() {
		return creator;
	}
	
	public void setCreator(Identity identity) {
		this.creator = identity;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? -8754546 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof TeamsMeetingImpl) {
			TeamsMeetingImpl meeting = (TeamsMeetingImpl)obj;
			return getKey() != null && getKey().equals(meeting.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("teamsMeeting[key=").append(getKey() == null ? "NULL" : getKey().toString())
		  .append(";subject=").append(getSubject() == null ? "" : getSubject())
		  .append("]");
		return super.toString();
	}
	
	
}
