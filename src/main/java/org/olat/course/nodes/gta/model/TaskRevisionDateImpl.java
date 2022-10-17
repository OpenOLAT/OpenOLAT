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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.TaskRevisionDate;

/**
 * 
 * Initial date: 7 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="gtataskrevisiondate")
@Table(name="o_gta_task_revision_date")
public class TaskRevisionDateImpl implements CreateInfo, Persistable, TaskRevisionDate {

	private static final long serialVersionUID = -1476103422622666128L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@Column(name="g_status", nullable=false, insertable=true, updatable=true)
	private String status;
	@Column(name="g_rev_loop", nullable=false, insertable=true, updatable=true)
	private int revisionLoop;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_date", nullable=true, insertable=true, updatable=false)
	private Date date;
	
	@ManyToOne(targetEntity=TaskImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_task", nullable=false, insertable=true, updatable=false)
	private Task task;

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	@Override
	@Transient
	public TaskProcess getTaskStatus() {
		if(StringHelper.containsNonWhitespace(status)) {
			return TaskProcess.valueOf(status);
		}
		return null;
	}
	
	public void setTaskStatus(TaskProcess taskStatus) {
		if(taskStatus == null) {
			status = null;
		} else {
			status = taskStatus.name();
		}
	}

	@Override
	public int getRevisionLoop() {
		return revisionLoop;
	}

	public void setRevisionLoop(int revisionLoop) {
		this.revisionLoop = revisionLoop;
	}

	@Override
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public int hashCode() {
		return key == null ? 848502 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof TaskRevisionDateImpl) {
			TaskRevisionDateImpl rev = (TaskRevisionDateImpl)obj;
			return key != null && key.equals(rev.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
