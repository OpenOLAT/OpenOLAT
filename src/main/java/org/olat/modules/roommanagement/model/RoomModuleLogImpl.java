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
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.modules.roommanagement.Location;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.roommanagement.RoomModuleLog;
import org.olat.modules.roommanagement.RoomModuleLogAction;

/**
 * Initial date: 22 Apr 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com
 */
@Entity(name = "rmmodulelog")
@Table(name = "o_rm_module_log")
public class RoomModuleLogImpl implements Persistable, RoomModuleLog {

	private static final long serialVersionUID = 8273649182736491827L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, insertable = true, updatable = false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creationdate", nullable = false, insertable = true, updatable = false)
	private Date creationDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "r_action", nullable = false, insertable = true, updatable = false)
	private RoomModuleLogAction action;

	@Column(name = "r_before_status", nullable = true, insertable = true, updatable = false)
	private String beforeStatus;

	@Column(name = "r_before", nullable = true, insertable = true, updatable = false)
	private String before;

	@Column(name = "r_after_status", nullable = true, insertable = true, updatable = false)
	private String afterStatus;

	@Column(name = "r_after", nullable = true, insertable = true, updatable = false)
	private String after;

	@ManyToOne(targetEntity = IdentityImpl.class, fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "fk_doer", nullable = true, insertable = true, updatable = false)
	private Identity doer;

	@ManyToOne(targetEntity = LocationImpl.class, fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "fk_location", nullable = true, insertable = true, updatable = false)
	private Location location;

	@ManyToOne(targetEntity = RoomImpl.class, fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "fk_room", nullable = true, insertable = true, updatable = false)
	private Room room;

	@ManyToOne(targetEntity = RoomBookingImpl.class, fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "fk_booking", nullable = true, insertable = true, updatable = false)
	private RoomBooking booking;

	@ManyToOne(targetEntity = LectureBlockImpl.class, fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "fk_lecture_block", nullable = true, insertable = true, updatable = false)
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
	public RoomModuleLogAction getAction() {
		return action;
	}

	public void setAction(RoomModuleLogAction action) {
		this.action = action;
	}

	@Override
	public String getBeforeStatus() {
		return beforeStatus;
	}

	public void setBeforeStatus(String beforeStatus) {
		this.beforeStatus = beforeStatus;
	}

	@Override
	public String getBefore() {
		return before;
	}

	public void setBefore(String before) {
		this.before = before;
	}

	@Override
	public String getAfterStatus() {
		return afterStatus;
	}

	public void setAfterStatus(String afterStatus) {
		this.afterStatus = afterStatus;
	}

	@Override
	public String getAfter() {
		return after;
	}

	public void setAfter(String after) {
		this.after = after;
	}

	@Override
	public IdentityRef getDoer() {
		return doer;
	}

	public void setDoer(Identity doer) {
		this.doer = doer;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	@Override
	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	@Override
	public RoomBooking getBooking() {
		return booking;
	}

	public void setBooking(RoomBooking booking) {
		this.booking = booking;
	}

	@Override
	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}

	public void setLectureBlock(LectureBlock lectureBlock) {
		this.lectureBlock = lectureBlock;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public int hashCode() {
		return key == null ? 6482917 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof RoomModuleLogImpl log) {
			return key != null && key.equals(log.key);
		}
		return false;
	}
}
