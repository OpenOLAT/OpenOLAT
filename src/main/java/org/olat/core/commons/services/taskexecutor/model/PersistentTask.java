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
package org.olat.core.commons.services.taskexecutor.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.commons.services.taskexecutor.Task;
import org.olat.core.commons.services.taskexecutor.TaskStatus;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;

/**
 * 
 * Initial date: 02.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="extask")
@Table(name="o_ex_task")
@NamedQuery(name="loadTaskByKey", query="select task from extask where task.key=:taskKey")
@NamedQuery(name="loadTaskByResource", query="select task from extask task where task.resource.key=:resourceKey")
@NamedQuery(name="taskToDos", query="select task.key from extask task where (task.statusStr='newTask' or (task.statusStr='inWork' and task.executorNode=:executorNode and task.executorBootId!=:executorBootId)) and (task.scheduledDate is null or task.scheduledDate <=:currentDate)")
public class PersistentTask implements Task, Persistable {
	
	private static final long serialVersionUID = 800884851125711998L;

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
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="e_name", nullable=false, insertable=true, updatable=false)
	private String name;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="e_scheduled", nullable=true, insertable=true, updatable=true)
	private Date scheduledDate;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_identity_id", nullable=true, insertable=true, updatable=false)
	private Identity creator;
	
	@ManyToOne(targetEntity=OLATResourceImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_resource_id", nullable=true, updatable=false)
	private OLATResource resource;
	
	@Column(name="e_ressubpath", nullable=true, insertable=true, updatable=false)
	private String resSubPath;
	
	@Column(name="e_status", nullable=false, insertable=true, updatable=true)
	private String statusStr;
	
	@Column(name="e_status_before_edit", nullable=true, insertable=true, updatable=true)
	private String statusBeforeEditStr;
	
	@Column(name="e_executor_node", nullable=true, insertable=true, updatable=true)
	private String executorNode;
	
	@Column(name="e_executor_boot_id", nullable=true, insertable=true, updatable=true)
	private String executorBootId;
	
	@Column(name="e_progress", nullable=true, insertable=true, updatable=true)
	private Double progress;
	@Column(name="e_checkpoint", nullable=true, insertable=true, updatable=true)
	private String checkpoint;
	
	@Column(name="e_task", nullable=false, insertable=true, updatable=true)
	private String task;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Date getScheduledDate() {
		return scheduledDate;
	}

	public void setScheduledDate(Date scheduledDate) {
		this.scheduledDate = scheduledDate;
	}

	@Override
	@Transient
	public TaskStatus getStatus() {
		return statusStr == null ? null : TaskStatus.valueOf(statusStr);
	}
	
	public void setStatus(TaskStatus status) {
		statusStr = (status == null ? null : status.name());
	}

	public String getStatusStr() {
		return statusStr;
	}

	public void setStatusStr(String statusStr) {
		this.statusStr = statusStr;
	}

	public String getStatusBeforeEditStr() {
		return statusBeforeEditStr;
	}

	public void setStatusBeforeEditStr(String statusBeforeEditStr) {
		this.statusBeforeEditStr = statusBeforeEditStr;
	}

	@Override
	public Identity getCreator() {
		return creator;
	}

	public void setCreator(Identity creator) {
		this.creator = creator;
	}

	public OLATResource getResource() {
		return resource;
	}

	public void setResource(OLATResource resource) {
		this.resource = resource;
	}

	public String getResSubPath() {
		return resSubPath;
	}

	public void setResSubPath(String resSubPath) {
		this.resSubPath = resSubPath;
	}

	public String getExecutorNode() {
		return executorNode;
	}

	public void setExecutorNode(String executorNode) {
		this.executorNode = executorNode;
	}

	public String getExecutorBootId() {
		return executorBootId;
	}

	public void setExecutorBootId(String executorBootId) {
		this.executorBootId = executorBootId;
	}
	
	@Override
	public Double getProgress() {
		return progress;
	}

	public void setProgress(Double progress) {
		this.progress = progress;
	}

	@Override
	public String getCheckpoint() {
		return checkpoint;
	}

	public void setCheckpoint(String checkpoint) {
		this.checkpoint = checkpoint;
	}

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}
	
	@Override
	public int hashCode() {
		return key == null ? -3987254 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof PersistentTask) {
			PersistentTask q = (PersistentTask)obj;
			return key != null && key.equals(q.key);
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
		sb.append("extask[key=").append(this.key)
			.append("]").append(super.toString());
		return sb.toString();
	}
}
