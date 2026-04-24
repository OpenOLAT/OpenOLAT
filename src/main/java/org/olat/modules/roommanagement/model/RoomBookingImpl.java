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
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;

/**
 * Initial date: 22 Apr 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com
 */
@Entity(name = "rmroombooking")
@Table(name = "o_rm_room_booking")
public class RoomBookingImpl implements Persistable, RoomBooking {

	private static final long serialVersionUID = 1736491827364918237L;

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

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "r_start_date", nullable = false, insertable = true, updatable = true)
	private Date startDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "r_end_date", nullable = false, insertable = true, updatable = true)
	private Date endDate;

	@Column(name = "r_buffer_before", nullable = false, insertable = true, updatable = true)
	private int bufferBefore = 0;

	@Column(name = "r_buffer_after", nullable = false, insertable = true, updatable = true)
	private int bufferAfter = 0;

	@ManyToOne(targetEntity = RoomImpl.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_room", nullable = false, insertable = true, updatable = false)
	private Room room;

	@ManyToOne(targetEntity = LectureBlockImpl.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_lecture_block", nullable = false, insertable = true, updatable = false)
	private LectureBlock lectureBlock;

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
	public Date getStartDate() {
		return startDate;
	}

	@Override
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Override
	public Date getEndDate() {
		return endDate;
	}

	@Override
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Override
	public int getBufferBefore() {
		return bufferBefore;
	}

	@Override
	public void setBufferBefore(int bufferBefore) {
		this.bufferBefore = bufferBefore;
	}

	@Override
	public int getBufferAfter() {
		return bufferAfter;
	}

	@Override
	public void setBufferAfter(int bufferAfter) {
		this.bufferAfter = bufferAfter;
	}

	@Override
	public Room getRoom() {
		return room;
	}

	@Override
	public void setRoom(Room room) {
		this.room = room;
	}

	@Override
	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}

	@Override
	public void setLectureBlock(LectureBlock lectureBlock) {
		this.lectureBlock = lectureBlock;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public int hashCode() {
		return key == null ? 3847291 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof RoomBookingImpl booking) {
			return key != null && key.equals(booking.key);
		}
		return false;
	}
}
