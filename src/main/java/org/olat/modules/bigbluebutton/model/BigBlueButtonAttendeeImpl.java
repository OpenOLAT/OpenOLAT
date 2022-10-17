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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.bigbluebutton.BigBlueButtonAttendee;
import org.olat.modules.bigbluebutton.BigBlueButtonAttendeeRoles;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;

/**
 * 
 * Initial date: 6 août 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="bigbluebuttonattendee")
@Table(name="o_bbb_attendee")
@NamedQuery(name="hasAttendeeByIdentity", query="select attendee.key from bigbluebuttonattendee as attendee where attendee.identity.key=:identityKey and attendee.meeting.key=:meetingKey")
@NamedQuery(name="hasAttendeeByPseudo", query="select attendee.key from bigbluebuttonattendee as attendee where attendee.meeting.key=:meetingKey and lower(attendee.pseudo)=:pseudo")
public class BigBlueButtonAttendeeImpl implements Persistable, BigBlueButtonAttendee {

	private static final long serialVersionUID = -3157941332717778067L;

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

	@Column(name="b_role", nullable=false, insertable=true, updatable=true)
	private String role;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="b_join_date", nullable=false, insertable=true, updatable=true)
	private Date joinDate;
	@Column(name="b_pseudo", nullable=true, insertable=true, updatable=false)
	private String pseudo;
	
	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_identity_id", nullable=true, insertable=true, updatable=false)
	private Identity identity;
	@ManyToOne(targetEntity=BigBlueButtonMeetingImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_meeting_id", nullable=true, insertable=true, updatable=false)
	private BigBlueButtonMeeting meeting;
	
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
	public BigBlueButtonAttendeeRoles getRolesEnum() {
		return BigBlueButtonAttendeeRoles.valueSecure(getRole());
	}

	@Override
	public Date getJoinDate() {
		return joinDate;
	}

	public void setJoinDate(Date joinDate) {
		this.joinDate = joinDate;
	}

	@Override
	public String getPseudo() {
		return pseudo;
	}

	public void setPseudo(String pseudo) {
		this.pseudo = pseudo;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public BigBlueButtonMeeting getMeeting() {
		return meeting;
	}

	public void setMeeting(BigBlueButtonMeeting meeting) {
		this.meeting = meeting;
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
		if(obj instanceof BigBlueButtonAttendeeImpl) {
			BigBlueButtonAttendeeImpl attendee = (BigBlueButtonAttendeeImpl)obj;
			return getKey() != null && getKey().equals(attendee.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
