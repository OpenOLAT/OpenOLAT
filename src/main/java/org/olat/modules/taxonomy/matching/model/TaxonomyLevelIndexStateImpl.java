/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
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
package org.olat.modules.taxonomy.matching.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.model.TaxonomyLevelImpl;

/**
 * Initial date: 2026-06-19<br>
 * @author uhensler, https://www.frentix.com
 */
@Entity(name = "ctaxonomylevelindexstate")
@Table(name = "o_tax_level_index_state")
public class TaxonomyLevelIndexStateImpl implements TaxonomyLevelIndexState {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, insertable = true, updatable = false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creationdate", nullable = false, insertable = true, updatable = false)
	private Date creationDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "lastmodified", nullable = false, insertable = true, updatable = true)
	private Date lastModified;

	@Enumerated(EnumType.STRING)
	@Column(name = "t_status", nullable = false, insertable = true, updatable = true)
	private IndexStatus status;

	@Column(name = "t_attempt_count", nullable = false, insertable = true, updatable = true)
	private int attemptCount;

	@Column(name = "t_last_error", nullable = true, insertable = true, updatable = true)
	private String lastError;

	@Column(name = "t_indexed_model_id", nullable = true, insertable = true, updatable = true)
	private String indexedModelId;

	@Column(name = "t_indexed_model_version", nullable = true, insertable = true, updatable = true)
	private String indexedModelVersion;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "t_last_index_date", nullable = true, insertable = true, updatable = true)
	private Date lastIndexDate;

	@ManyToOne(targetEntity = TaxonomyLevelImpl.class)
	@JoinColumn(name = "fk_level", nullable = false, insertable = true, updatable = false)
	private TaxonomyLevel level;

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
	public TaxonomyLevel getLevel() {
		return level;
	}

	public void setLevel(TaxonomyLevel level) {
		this.level = level;
	}

	@Override
	public IndexStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(IndexStatus status) {
		this.status = status;
	}

	@Override
	public int getAttemptCount() {
		return attemptCount;
	}

	@Override
	public void setAttemptCount(int attemptCount) {
		this.attemptCount = attemptCount;
	}

	@Override
	public String getLastError() {
		return lastError;
	}

	@Override
	public void setLastError(String lastError) {
		this.lastError = lastError;
	}

	@Override
	public String getIndexedModelId() {
		return indexedModelId;
	}

	@Override
	public void setIndexedModelId(String indexedModelId) {
		this.indexedModelId = indexedModelId;
	}

	@Override
	public String getIndexedModelVersion() {
		return indexedModelVersion;
	}

	@Override
	public void setIndexedModelVersion(String indexedModelVersion) {
		this.indexedModelVersion = indexedModelVersion;
	}

	@Override
	public Date getLastIndexDate() {
		return lastIndexDate;
	}

	@Override
	public void setLastIndexDate(Date lastIndexDate) {
		this.lastIndexDate = lastIndexDate;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof TaxonomyLevelIndexStateImpl other) {
			return key != null && key.equals(other.key);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return key == null ? super.hashCode() : key.hashCode();
	}
}
