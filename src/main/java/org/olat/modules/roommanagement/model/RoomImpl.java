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
import org.olat.modules.roommanagement.Location;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomStatus;

/**
 * Initial date: 22 Apr 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com
 */
@Entity(name = "rmroom")
@Table(name = "o_rm_room")
public class RoomImpl implements Persistable, Room {

	private static final long serialVersionUID = -6104831937302174583L;

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

	@Column(name = "r_status", nullable = false, insertable = true, updatable = true)
	private String status;

	@Column(name = "r_name", nullable = false, insertable = true, updatable = true)
	private String name;

	@Column(name = "r_ext_id", nullable = true, insertable = true, updatable = true)
	private String externalId;

	@Column(name = "r_ext_ref", nullable = true, insertable = true, updatable = true)
	private String externalRef;

	@Column(name = "r_description", nullable = true, insertable = true, updatable = true)
	private String description;

	@Column(name = "r_seats", nullable = true, insertable = true, updatable = true)
	private Integer seats;

	@Column(name = "r_admin_info", nullable = true, insertable = true, updatable = true)
	private String adminInfo;

	@ManyToOne(targetEntity = LocationImpl.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_location", nullable = false, insertable = true, updatable = false)
	private Location location;

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
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getExternalId() {
		return externalId;
	}

	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public String getExternalRef() {
		return externalRef;
	}

	@Override
	public void setExternalRef(String externalRef) {
		this.externalRef = externalRef;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public Integer getSeats() {
		return seats;
	}

	@Override
	public void setSeats(Integer seats) {
		this.seats = seats;
	}

	@Override
	public String getAdminInfo() {
		return adminInfo;
	}

	@Override
	public void setAdminInfo(String adminInfo) {
		this.adminInfo = adminInfo;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public void setLocation(Location location) {
		this.location = location;
	}

	@Override
	public RoomStatus getStatus() {
		return status == null ? null : RoomStatus.valueOf(status);
	}

	@Override
	public void setStatus(RoomStatus status) {
		this.status = status == null ? null : status.name();
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public int hashCode() {
		return key == null ? 5219384 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof RoomImpl room) {
			return key != null && key.equals(room.key);
		}
		return false;
	}
}
