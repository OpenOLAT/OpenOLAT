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

import java.math.BigDecimal;
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
import org.olat.modules.roommanagement.Location;
import org.olat.modules.roommanagement.RoomStatus;

/**
 * Initial date: 22 Apr 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com
 */
@Entity(name = "rmlocation")
@Table(name = "o_rm_location")
public class LocationImpl implements Persistable, Location {

	private static final long serialVersionUID = 8372647183921047231L;

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

	@Column(name = "r_address", nullable = true, insertable = true, updatable = true)
	private String address;

	@Column(name = "r_info_url", nullable = true, insertable = true, updatable = true)
	private String infoUrl;

	@Column(name = "r_geo_lat", nullable = true, insertable = true, updatable = true)
	private BigDecimal geoLatitude;

	@Column(name = "r_geo_lon", nullable = true, insertable = true, updatable = true)
	private BigDecimal geoLongitude;

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
	public String getAddress() {
		return address;
	}

	@Override
	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public String getInfoUrl() {
		return infoUrl;
	}

	@Override
	public void setInfoUrl(String infoUrl) {
		this.infoUrl = infoUrl;
	}

	@Override
	public BigDecimal getGeoLatitude() {
		return geoLatitude;
	}

	@Override
	public void setGeoLatitude(BigDecimal geoLatitude) {
		this.geoLatitude = geoLatitude;
	}

	@Override
	public BigDecimal getGeoLongitude() {
		return geoLongitude;
	}

	@Override
	public void setGeoLongitude(BigDecimal geoLongitude) {
		this.geoLongitude = geoLongitude;
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
		return key == null ? 2867342 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof LocationImpl location) {
			return key != null && key.equals(location.key);
		}
		return false;
	}
}
