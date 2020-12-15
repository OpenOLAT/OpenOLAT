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
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.teams.TeamsAttendee;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsUser;

/**
 * 
 * Initial date: 15 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="teamsattendee")
@Table(name="o_teams_attendee")
@NamedQuery(name="hasTeamsAttendeeByIdentity", query="select attendee.key from teamsattendee as attendee where attendee.identity.key=:identityKey and attendee.meeting.key=:meetingKey")
public class TeamsAttendeeImpl implements Persistable, TeamsAttendee {

	private static final long serialVersionUID = 9052810711360628756L;

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

	@Column(name="t_role", nullable=false, insertable=true, updatable=true)
	private String role;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="t_join_date", nullable=false, insertable=true, updatable=true)
	private Date joinDate;
	
	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_identity_id", nullable=true, insertable=true, updatable=false)
	private Identity identity;
	@ManyToOne(targetEntity=TeamsUserImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_teams_user_id", nullable=true, insertable=true, updatable=false)
	private TeamsUser teamsUser;
	@ManyToOne(targetEntity=TeamsMeetingImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_meeting_id", nullable=false, insertable=true, updatable=false)
	private TeamsMeeting meeting;
	
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

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public Date getJoinDate() {
		return joinDate;
	}

	public void setJoinDate(Date joinDate) {
		this.joinDate = joinDate;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public TeamsUser getTeamsUser() {
		return teamsUser;
	}

	public void setTeamsUser(TeamsUser teamsUser) {
		this.teamsUser = teamsUser;
	}

	@Override
	public TeamsMeeting getMeeting() {
		return meeting;
	}

	public void setMeeting(TeamsMeeting meeting) {
		this.meeting = meeting;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 46542123 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof TeamsAttendeeImpl) {
			TeamsAttendeeImpl attendee = (TeamsAttendeeImpl)obj;
			return getKey() != null && getKey().equals(attendee.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

}
