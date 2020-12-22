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
package org.olat.modules.appointments.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.Topic;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingImpl;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.model.TeamsMeetingImpl;

/**
 * 
 * Initial date: 11 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="appointment")
@Table(name="o_ap_appointment")
public class AppointmentImpl implements Persistable, Appointment {

	private static final long serialVersionUID = -8654763565657776253L;
	
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

	@Enumerated(EnumType.STRING)
	@Column(name="a_status", nullable=false, insertable=true, updatable=true)
	private Status status;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="a_status_mod_date", nullable=false, insertable=true, updatable=true)
	private Date statusModified;
	@Column(name="a_start", nullable=true, insertable=true, updatable=true)
	private Date start;
	@Column(name="a_end", nullable=true, insertable=true, updatable=true)
	private Date end;
	@Column(name="a_location", nullable=true, insertable=true, updatable=true)
	private String location;
	@Column(name="a_details", nullable=true, insertable=true, updatable=true)
	private String details;
	@Column(name="a_max_participations", nullable=true, insertable=true, updatable=true)
	private Integer maxParticipations;
	
	@ManyToOne(targetEntity=TopicImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_topic_id", nullable=false, insertable=true, updatable=false)
	private Topic topic;
	@OneToOne(targetEntity=BigBlueButtonMeetingImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_meeting_id", nullable=true, insertable=true, updatable=true)
	private BigBlueButtonMeeting bbbMeeting;
	@OneToOne(targetEntity=TeamsMeetingImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_teams_id", nullable=true, insertable=true, updatable=true)
	private TeamsMeeting teamsMeeting;

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
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public Date getStatusModified() {
		return statusModified;
	}

	public void setStatusModified(Date statusModified) {
		this.statusModified = statusModified;
	}

	@Override
	public Date getStart() {
		return start;
	}

	@Override
	public void setStart(Date start) {
		this.start = start;
	}

	@Override
	public Date getEnd() {
		return end;
	}

	@Override
	public void setEnd(Date end) {
		this.end = end;
	}

	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public String getDetails() {
		return details;
	}

	@Override
	public void setDetails(String details) {
		this.details = details;
	}

	@Override
	public Integer getMaxParticipations() {
		return maxParticipations;
	}

	@Override
	public void setMaxParticipations(Integer maxParticipations) {
		this.maxParticipations = maxParticipations;
	}

	@Override
	public Topic getTopic() {
		return topic;
	}

	public void setTopic(Topic topic) {
		this.topic = topic;
	}

	@Override
	public BigBlueButtonMeeting getBBBMeeting() {
		return bbbMeeting;
	}

	public void setBbbMeeting(BigBlueButtonMeeting bbbMeeting) {
		this.bbbMeeting = bbbMeeting;
	}

	@Override
	public TeamsMeeting getTeamsMeeting() {
		return teamsMeeting;
	}

	public void setTeamsMeeting(TeamsMeeting teamsMeeting) {
		this.teamsMeeting = teamsMeeting;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AppointmentImpl other = (AppointmentImpl) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

}
