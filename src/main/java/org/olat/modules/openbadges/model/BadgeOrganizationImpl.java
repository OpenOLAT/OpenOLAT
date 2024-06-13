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
import org.olat.core.logging.Tracing;
import org.olat.modules.openbadges.BadgeOrganization;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.logging.log4j.Logger;

/**
 * Initial date: 2024-06-12<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Entity(name="badgeorganization")
@Table(name="o_badge_organization")
public class BadgeOrganizationImpl implements Persistable, BadgeOrganization {

	private static final Logger log = Tracing.createLoggerFor(BadgeOrganizationImpl.class);

	@Serial
	private static final long serialVersionUID = -7083246592591519456L;

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
	@Column(name = "b_type", nullable = false, insertable = true, updatable = false)
	private BadgeOrganizationType type;

	@Column(name = "b_organization_key", nullable = false, insertable = true, updatable = true)
	private String organizationKey;

	@Column(name = "b_organization_value", nullable = false, insertable = true, updatable = true)
	private String organizationValue;

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
	public BadgeOrganizationType getType() {
		return type;
	}

	public void setType(BadgeOrganizationType type) {
		this.type = type;
	}

	@Override
	public String getOrganizationKey() {
		return organizationKey;
	}

	@Override
	public void setOrganizationKey(String organizationKey) {
		this.organizationKey = organizationKey;
	}

	@Override
	public String getOrganizationValue() {
		return organizationValue;
	}

	@Override
	public void setOrganizationValue(String organizationValue) {
		this.organizationValue = organizationValue;
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
		if (obj instanceof BadgeOrganizationImpl badgeOrganization) {
			return getKey() != null && getKey().equals(badgeOrganization.getKey());
		}
		return false;
	}
}
