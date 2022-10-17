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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 4 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */

@Entity(name="gradingtimerecordappender")
@Table(name="o_grad_time_record")
public class GradingTimeRecordAppender implements Persistable {

	private static final long serialVersionUID = -1271603573444106919L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="g_time", nullable=false, insertable=false, updatable=true)
	private long time;
	
	@Temporal(TemporalType.DATE)
	@Column(name="g_date_record", nullable=false, insertable=true, updatable=false)
	private Date dateOfRecord;
	
	@Column(name="fk_grader", nullable=false, insertable=false, updatable=false)
	private Long graderKey;
	@Column(name="fk_assignment", nullable=true, insertable=false, updatable=false)
	private Long assignmentKey;
	
	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public Long getGraderKey() {
		return graderKey;
	}

	public void setGraderKey(Long graderKey) {
		this.graderKey = graderKey;
	}

	public Long getAssignmentKey() {
		return assignmentKey;
	}

	public void setAssignmentKey(Long assignmentKey) {
		this.assignmentKey = assignmentKey;
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
		if(obj instanceof GradingTimeRecordAppender) {
			GradingTimeRecordAppender timesheet = (GradingTimeRecordAppender)obj;
			return getKey() != null && getKey().equals(timesheet.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
