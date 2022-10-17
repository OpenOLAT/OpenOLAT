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
package org.olat.modules.grading.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.modules.grading.GraderToIdentity;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingTimeRecord;

/**
 * 
 * Initial date: 4 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */

@Entity(name="gradingtimerecord")
@Table(name="o_grad_time_record")
public class GradingTimeRecordImpl implements GradingTimeRecord, Persistable {

	private static final long serialVersionUID = -1271603573444106919L;

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
	
	@Column(name="g_time", nullable=false, insertable=true, updatable=false)
	private long time;
	@Column(name="g_metadata_time", nullable=false, insertable=true, updatable=true)
	private long metadataTime;
	
	@Temporal(TemporalType.DATE)
	@Column(name="g_date_record", nullable=false, insertable=true, updatable=false)
	private Date dateOfRecord;
	
	@ManyToOne(targetEntity=GraderToIdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_grader", nullable=false, insertable=true, updatable=false)
	private GraderToIdentity grader;
	@ManyToOne(targetEntity=GradingAssignmentImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_assignment", nullable=true, insertable=true, updatable=true)
	private GradingAssignment assignment;
	
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
	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	@Override
	public long getMetadataTime() {
		return metadataTime;
	}

	@Override
	public void setMetadataTime(long metadataTime) {
		this.metadataTime = metadataTime;
	}

	@Override
	public Date getDateOfRecord() {
		return dateOfRecord;
	}

	public void setDateOfRecord(Date dateOfRecord) {
		this.dateOfRecord = dateOfRecord;
	}

	@Override
	public GraderToIdentity getGrader() {
		return grader;
	}

	public void setGrader(GraderToIdentity grader) {
		this.grader = grader;
	}

	@Override
	public GradingAssignment getAssignment() {
		return assignment;
	}

	public void setAssignment(GradingAssignment assignment) {
		this.assignment = assignment;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 862567 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof GradingTimeRecordImpl) {
			GradingTimeRecordImpl timesheet = (GradingTimeRecordImpl)obj;
			return getKey() != null && getKey().equals(timesheet.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
