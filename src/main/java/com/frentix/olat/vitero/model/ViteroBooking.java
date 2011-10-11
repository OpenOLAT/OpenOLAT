/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package com.frentix.olat.vitero.model;

import java.util.Date;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  6 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroBooking {
	
	private int bookingId;
	private int groupId;
	private Date start;
	private int startBuffer;
	private Date end;
	private int endBuffer;
	private int roomSize;
	private boolean autoSignIn;
	private String timeZoneId;
	
	public ViteroBooking() {
		// nothing to do
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

	public int getRoomSize() {
		return roomSize;
	}

	public void setRoomSize(int roomSize) {
		this.roomSize = roomSize;
	}

	public int getStartBuffer() {
		return startBuffer;
	}

	public void setStartBuffer(int startBuffer) {
		this.startBuffer = startBuffer;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}
	
	public int getEndBuffer() {
		return endBuffer;
	}

	public void setEndBuffer(int endBuffer) {
		this.endBuffer = endBuffer;
	}

	public Date getEnd() {
		return end;
	}
	
	public void setEnd(Date end) {
		this.end = end;
	}

	public String getTimeZoneId() {
		return timeZoneId;
	}

	public void setTimeZoneId(String timeZoneId) {
		this.timeZoneId = timeZoneId;
	}

	public boolean isAutoSignIn() {
		return autoSignIn;
	}

	public void setAutoSignIn(boolean autoSignIn) {
		this.autoSignIn = autoSignIn;
	}
}