/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.ai.essay;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;

/**
 *
 * Backing row for the author "Generate with AI" drafts drawer. Each job
 * runs as a {@code LongRunnable} on the task executor, produces a set of
 * {@link EssayItemDraft} records, and transitions through
 * {@link State#PENDING} &rarr; {@link State#RUNNING} &rarr;
 * {@link State#DONE} / {@link State#FAILED} / {@link State#CANCELLED}.
 * <p>
 * Progress and error payloads are stored as JSON so the drawer UI can
 * render per-chunk progress without a second round-trip.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Entity(name = "essaygenerationjob")
@Table(name = "o_essay_generation_job")
public class EssayGenerationJob implements CreateInfo, ModifiedInfo, Persistable {

	private static final long serialVersionUID = 1L;

	public enum State {
		PENDING,
		RUNNING,
		DONE,
		FAILED,
		CANCELLED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "a_id", nullable = false, unique = true, insertable = true, updatable = false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "a_creationdate", nullable = false, insertable = true, updatable = false)
	private Date creationDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "a_lastmodified", nullable = false, insertable = true, updatable = true)
	private Date lastModified;

	@ManyToOne(targetEntity = IdentityImpl.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "a_created_by_fk", nullable = false, insertable = true, updatable = false)
	private Identity createdBy;

	@Enumerated(EnumType.STRING)
	@Column(name = "a_state", nullable = false, insertable = true, updatable = true)
	private State state = State.PENDING;

	@Lob
	@Column(name = "a_progress_json", nullable = true, insertable = true, updatable = true)
	private String progressJson;

	@Lob
	@Column(name = "a_error_json", nullable = true, insertable = true, updatable = true)
	private String errorJson;

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

	public Identity getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Identity createdBy) {
		this.createdBy = createdBy;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public String getProgressJson() {
		return progressJson;
	}

	public void setProgressJson(String progressJson) {
		this.progressJson = progressJson;
	}

	public String getErrorJson() {
		return errorJson;
	}

	public void setErrorJson(String errorJson) {
		this.errorJson = errorJson;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public int hashCode() {
		return key == null ? -214_008_331 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof EssayGenerationJob job) {
			return key != null && key.equals(job.key);
		}
		return false;
	}
}
