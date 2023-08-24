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
package org.olat.modules.openbadges.model;

import java.io.Serial;
import java.util.Date;

import org.olat.core.id.Persistable;
import org.olat.modules.openbadges.BadgeEntryConfiguration;
import org.olat.repository.RepositoryEntry;

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

/**
 * Initial date: 2023-06-13<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Entity(name="badgeentryconfig")
@Table(name="o_badge_entry_config")
public class BadgeEntryConfigurationImpl implements Persistable, BadgeEntryConfiguration {

	@Serial
	private static final long serialVersionUID = 7675483181673754048L;

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

	@Column(name = "b_award_enabled", nullable = false, insertable = true, updatable = true)
	private boolean awardEnabled;

	@Column(name = "b_owner_can_award", nullable = false, insertable = true, updatable = true)
	private boolean ownerCanAward;

	@Column(name = "b_coach_can_award", nullable = false, insertable = true, updatable = true)
	private boolean coachCanAward;

	@ManyToOne(targetEntity = RepositoryEntry.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_entry", nullable = false, insertable = true, updatable = false, unique = true)
	private RepositoryEntry entry;

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
	public boolean isAwardEnabled() {
		return awardEnabled;
	}

	@Override
	public void setAwardEnabled(boolean awardEnabled) {
		this.awardEnabled = awardEnabled;
	}

	@Override
	public boolean isOwnerCanAward() {
		return ownerCanAward;
	}

	@Override
	public void setOwnerCanAward(boolean ownerCanAward) {
		this.ownerCanAward = ownerCanAward;
	}

	@Override
	public boolean isCoachCanAward() {
		return coachCanAward;
	}

	@Override
	public void setCoachCanAward(boolean coachCanAward) {
		this.coachCanAward = coachCanAward;
	}

	@Override
	public RepositoryEntry getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof BadgeEntryConfigurationImpl badgeEntryConfig) {
			return getKey() != null && getKey().equals(badgeEntryConfig.getKey());
		}
		return false;
	}
}
