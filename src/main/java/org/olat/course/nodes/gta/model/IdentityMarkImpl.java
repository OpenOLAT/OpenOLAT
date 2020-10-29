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
package org.olat.course.nodes.gta.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.course.nodes.gta.IdentityMark;
import org.olat.course.nodes.gta.TaskList;

/**
 * 
 * Initial date: 02.10.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="gtaMark")
@Table(name="o_gta_mark")
@NamedQueries({
	@NamedQuery(name="loadByMarker", query=
			  "select mark"
			+ "  from gtaMark mark"
			+ " where mark.taskList.key=:taskListKey"
			+ "   and mark.marker.key=:markerKey"),
	@NamedQuery(name="loadByMarkerAndParticipant", query=
			  "select mark"
			+ "  from gtaMark mark"
			+ " where mark.taskList.key=:taskListKey"
			+ "   and mark.marker.key=:markerKey"
			+ "   and mark.participant.key=:participantKey"),
	@NamedQuery(name="deleteByMarker", query =
			  "delete from gtaMark mark"
			+ " where mark.taskList.key=:taskListKey"
			+ "   and mark.marker.key=:markerKey"
			+ "   and mark.participant.key=:participantKey"),
	@NamedQuery(name="deleteByTaskList", query =
			  "delete from gtaMark mark"
			+ " where mark.taskList.key=:taskListKey"),
	@NamedQuery(name="deleteByTaskKeys", query =
			  "delete from gtaMark mark"
			+ " where mark.key in (:taskKeys)")
})
public class IdentityMarkImpl implements IdentityMark, Persistable {

	private static final long serialVersionUID = 5984891452227836907L;
	
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
	
	@ManyToOne(targetEntity=TaskListImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_tasklist_id", nullable=false, insertable=true, updatable=false)
	private TaskList taskList;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_marker_identity_id", nullable=true, insertable=true, updatable=false)
	private Identity marker;
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_participant_identity_id", nullable=true, insertable=true, updatable=false)
	private Identity participant;
	
	@Override
	public Long getKey() {
		return key;
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
	public TaskList getTaskList() {
		return taskList;
	}
	
	@Override
	public void setTaskList(TaskList taskList) {
		this.taskList = taskList;
	}
	
	@Override
	public Identity getMarker() {
		return marker;
	}
	
	@Override
	public void setMarker(Identity marker) {
		this.marker = marker;
	}
	
	@Override
	public Identity getParticipant() {
		return participant;
	}
	
	@Override
	public void setParticipant(Identity participant) {
		this.participant = participant;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
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
		if(this == obj) {
			return true;
		} else if(obj instanceof IdentityMarkImpl) {
			IdentityMarkImpl other = (IdentityMarkImpl)obj;
			return getKey() != null && getKey().equals(other.getKey());
		}
		return false;
	}
	
}
