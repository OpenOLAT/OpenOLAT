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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.core.id.Roles;
import org.olat.modules.roommanagement.Room;

/**
 * Initial date: 19 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "roomVO")
public class RoomVO {

	private Long key;
	private Date createdDate;
	private Date lastModified;
	private String name;
	private String status;
	private String externalId;
	private String externalRef;
	private String description;
	private Integer seats;
	private String adminInfo;
	private Long buildingKey;

	public RoomVO() {
		//
	}

	public static RoomVO valueOf(Room room, Roles roles) {
		RoomVO vo = new RoomVO();
		vo.setKey(room.getKey());
		vo.setCreatedDate(room.getCreationDate());
		vo.setLastModified(room.getLastModified());
		vo.setName(room.getDescription());
		if (room.getStatus() != null) {
			vo.setStatus(room.getStatus().name());
		}
		boolean admin = roles != null && (roles.isSystemAdmin() || roles.isAdministrator());
		if (admin) {
			vo.setExternalId(room.getExternalId());
			vo.setAdminInfo(room.getAdminInfo());
		}
		vo.setExternalRef(room.getExternalRef());
		vo.setDescription(room.getRoomInfo());
		vo.setSeats(room.getSeats());
		if (room.getBuilding() != null) {
			vo.setBuildingKey(room.getBuilding().getKey());
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

	public Integer getSeats() {
		return seats;
	}

	public void setSeats(Integer seats) {
		this.seats = seats;
	}

	public String getAdminInfo() {
		return adminInfo;
	}

	public void setAdminInfo(String adminInfo) {
		this.adminInfo = adminInfo;
	}

	public Long getBuildingKey() {
		return buildingKey;
	}

	public void setBuildingKey(Long buildingKey) {
		this.buildingKey = buildingKey;
	}
}
