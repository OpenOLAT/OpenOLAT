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
package org.olat.modules.roommanagement.model;

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
import org.olat.basesecurity.model.OrganisationImpl;
import org.olat.core.id.Organisation;
import org.olat.modules.roommanagement.Location;
import org.olat.modules.roommanagement.LocationToOrganisation;

/**
 * Initial date: 22 Apr 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com
 */
@Entity(name = "rmlocationtoorganisation")
@Table(name = "o_rm_location_to_org")
public class LocationToOrganisationImpl implements Persistable, LocationToOrganisation {

	private static final long serialVersionUID = -2914758134609235812L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, insertable = true, updatable = false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creationdate", nullable = false, insertable = true, updatable = false)
	private Date creationDate;

	@ManyToOne(targetEntity = LocationImpl.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_location", nullable = false, insertable = true, updatable = false)
	private Location location;

	@ManyToOne(targetEntity = OrganisationImpl.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_organisation", nullable = false, insertable = true, updatable = false)
	private Organisation organisation;

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
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	@Override
	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public int hashCode() {
		return key == null ? 4918273 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof LocationToOrganisationImpl lto) {
			return key != null && key.equals(lto.key);
		}
		return false;
	}
}
