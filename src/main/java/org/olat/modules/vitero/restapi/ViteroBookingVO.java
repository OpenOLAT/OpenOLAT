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
package org.olat.modules.vitero.restapi;

import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.modules.vitero.model.ViteroBooking;

/**
 * 
 * Initial date: 06.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "viteroBookingVO")
public class ViteroBookingVO {
	
	private int bookingId = -1;
	private int groupId;
	private String groupName;
	private String eventName;
	private String externalId;
	private Date start;
	private int startBuffer;
	private Date end;
	private int endBuffer;
	private int roomSize;
	private boolean inspire;
	private boolean autoSignIn;
	private String timeZoneId;
	
	public ViteroBookingVO() {
		//
	}
	
	public ViteroBookingVO(ViteroBooking booking) {
		bookingId = booking.getBookingId();
		groupId = booking.getGroupId();
		groupName = booking.getGroupName();
		eventName = booking.getEventName();
		externalId = booking.getExternalId();
		start = booking.getStart();
		startBuffer = booking.getStartBuffer();
		end = booking.getEnd();
		endBuffer = booking.getEndBuffer();
		roomSize = booking.getRoomSize();
		autoSignIn = booking.isAutoSignIn();
		inspire = booking.isInspire();
		timeZoneId = booking.getTimeZoneId();
	}
	
	public int getBookingId() {
		return bookingId;
	}
	
	public void setBookingId(int bookingId) {
		this.bookingId = bookingId;
	}
	
	public int getGroupId() {
		return groupId;
	}
	
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	
	public String getGroupName() {
		return groupName;
	}
	
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public Date getStart() {
		return start;
	}
	
	public void setStart(Date start) {
		this.start = start;
	}
	
	public int getStartBuffer() {
		return startBuffer;
	}
	
	public void setStartBuffer(int startBuffer) {
		this.startBuffer = startBuffer;
	}
	
	public Date getEnd() {
		return end;
	}
	
	public void setEnd(Date end) {
		this.end = end;
	}
	
	public int getEndBuffer() {
		return endBuffer;
	}
	
	public void setEndBuffer(int endBuffer) {
		this.endBuffer = endBuffer;
	}
	
	public int getRoomSize() {
		return roomSize;
	}
	
	public void setRoomSize(int roomSize) {
		this.roomSize = roomSize;
	}
	
	public boolean isAutoSignIn() {
		return autoSignIn;
	}
	
	public void setAutoSignIn(boolean autoSignIn) {
		this.autoSignIn = autoSignIn;
	}
	
	public boolean isInspire() {
		return inspire;
	}

	public void setInspire(boolean inspire) {
		this.inspire = inspire;
	}

	public String getTimeZoneId() {
		return timeZoneId;
	}
	
	public void setTimeZoneId(String timeZoneId) {
		this.timeZoneId = timeZoneId;
	}
}
