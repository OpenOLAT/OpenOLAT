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
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.TaskRevision;

/**
 * 
 * Initial date: 28 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="gtataskrevision")
@Table(name="o_gta_task_revision")
public class TaskRevisionImpl implements TaskRevision, CreateInfo, Persistable, ModifiedInfo {

	private static final long serialVersionUID = 3507151705355830383L;

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
	
	@Column(name="g_status", nullable=false, insertable=true, updatable=true)
	private String status;
	@Column(name="g_rev_loop", nullable=false, insertable=true, updatable=true)
	private int revisionLoop;
	@Column(name="g_rev_comment", nullable=true, insertable=true, updatable=true)
	private String comment;
	@Column(name="g_rev_comment_lastmodified", nullable=true, insertable=true, updatable=true)
	private Date commentLastModified;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_date", nullable=true, insertable=true, updatable=false)
	private Date date;
	
	@ManyToOne(targetEntity=TaskImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_task", nullable=false, insertable=true, updatable=false)
	private Task task;
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_comment_author", nullable=true, insertable=true, updatable=false)
	private Identity commentAuthor;
	
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
	
	public void setCreationDate(Date date) {
		creationDate = date;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date date) {
		lastModified = date;
	}

	@Override
	@Transient
	public TaskProcess getTaskStatus() {
		if(StringHelper.containsNonWhitespace(status)) {
			return TaskProcess.valueOf(status);
		}
		return null;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public int getRevisionLoop() {
		return revisionLoop;
	}

	public void setRevisionLoop(int revisionLoop) {
		this.revisionLoop = revisionLoop;
	}

	@Override
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getCommentLastModified() {
		return commentLastModified;
	}

	public void setCommentLastModified(Date commentLastModified) {
		this.commentLastModified = commentLastModified;
	}

	public Identity getCommentAuthor() {
		return commentAuthor;
	}

	public void setCommentAuthor(Identity commentAuthor) {
		this.commentAuthor = commentAuthor;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 2368720 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof TaskRevisionImpl) {
			TaskRevisionImpl rev = (TaskRevisionImpl)obj;
			return getKey() != null && getKey().equals(rev.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
