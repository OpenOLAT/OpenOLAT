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
package org.olat.modules.roommanagement.restapi;

import java.util.Date;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.modules.roommanagement.Building;

/**
 * Initial date: 18 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "buildingVO")
public class BuildingVO {

	private Long key;
	private Date createdDate;
	private Date lastModified;
	private String name;
	private String status;
	private String externalId;
	private String externalRef;
	private String description;
	private String address;
	private String infoUrl;
	private Double geoLat;
	private Double geoLon;
	private Long[] organisationKeys;

	public BuildingVO() {
		//
	}

	public static BuildingVO valueOf(Building building, List<Organisation> organisations, Roles roles) {
		BuildingVO vo = new BuildingVO();
		vo.setKey(building.getKey());
		vo.setCreatedDate(building.getCreationDate());
		vo.setLastModified(building.getLastModified());
		vo.setName(building.getDescription());
		if (building.getStatus() != null) {
			vo.setStatus(building.getStatus().name());
		}
		if (roles != null && (roles.isSystemAdmin() || roles.isAdministrator())) {
			vo.setExternalId(building.getExternalId());
		}
		vo.setExternalRef(building.getExternalRef());
		vo.setDescription(building.getInfo());
		vo.setAddress(building.getAddress());
		vo.setInfoUrl(building.getInfoUrl());
		if (building.getGeoLatitude() != null) {
			vo.setGeoLat(building.getGeoLatitude().doubleValue());
		}
		if (building.getGeoLongitude() != null) {
			vo.setGeoLon(building.getGeoLongitude().doubleValue());
		}
		if (organisations != null) {
			vo.setOrganisationKeys(organisations.stream().map(Organisation::getKey).toArray(Long[]::new));
		}
		return vo;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getExternalRef() {
		return externalRef;
	}

	public void setExternalRef(String externalRef) {
		this.externalRef = externalRef;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getInfoUrl() {
		return infoUrl;
	}

	public void setInfoUrl(String infoUrl) {
		this.infoUrl = infoUrl;
	}

	public Double getGeoLat() {
		return geoLat;
	}

	public void setGeoLat(Double geoLat) {
		this.geoLat = geoLat;
	}

	public Double getGeoLon() {
		return geoLon;
	}

	public void setGeoLon(Double geoLon) {
		this.geoLon = geoLon;
	}

	public Long[] getOrganisationKeys() {
		return organisationKeys;
	}

	public void setOrganisationKeys(Long[] organisationKeys) {
		this.organisationKeys = organisationKeys;
	}
}
