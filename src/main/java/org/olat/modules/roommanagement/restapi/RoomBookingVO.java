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

import org.olat.modules.roommanagement.RoomBooking;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Initial date: 19 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Schema(description = "A room booking of a lecture block. On write, the room is resolved in this order of precedence: roomKey, else externalId, else externalRef.")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "roomBookingVO")
public class RoomBookingVO {

	private Long key;
	@Schema(description = "The primary key of the room. Takes precedence over externalId and externalRef.")
	private Long roomKey;
	@Schema(description = "The external ID of the room. Used if roomKey is not set. Takes precedence over externalRef.")
	private String externalId;
	@Schema(description = "The external reference of the room. Used if neither roomKey nor externalId is set.")
	private String externalRef;
	private Long lectureBlockKey;
	private Date startDate;
	private Date endDate;
	private Integer bufferBeforeMin;
	private Integer bufferAfterMin;

	public RoomBookingVO() {
		//
	}

	public static RoomBookingVO valueOf(RoomBooking booking) {
		RoomBookingVO vo = new RoomBookingVO();
		vo.setKey(booking.getKey());
		if (booking.getRoom() != null) {
			vo.setRoomKey(booking.getRoom().getKey());
			vo.setExternalId(booking.getRoom().getExternalId());
			vo.setExternalRef(booking.getRoom().getExternalRef());
		}
		if (booking.getLectureBlock() != null) {
			vo.setLectureBlockKey(booking.getLectureBlock().getKey());
		}
		vo.setStartDate(booking.getStartDate());
		vo.setEndDate(booking.getEndDate());
		vo.setBufferBeforeMin(booking.getBufferBefore());
		vo.setBufferAfterMin(booking.getBufferAfter());
		return vo;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public Long getRoomKey() {
		return roomKey;
	}

	public void setRoomKey(Long roomKey) {
		this.roomKey = roomKey;
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

	public Long getLectureBlockKey() {
		return lectureBlockKey;
	}

	public void setLectureBlockKey(Long lectureBlockKey) {
		this.lectureBlockKey = lectureBlockKey;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Integer getBufferBeforeMin() {
		return bufferBeforeMin;
	}

	public void setBufferBeforeMin(Integer bufferBeforeMin) {
		this.bufferBeforeMin = bufferBeforeMin;
	}

	public Integer getBufferAfterMin() {
		return bufferAfterMin;
	}

	public void setBufferAfterMin(Integer bufferAfterMin) {
		this.bufferAfterMin = bufferAfterMin;
	}
}
