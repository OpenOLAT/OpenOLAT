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
package com.frentix.olat.course.nodes.vitero;

import java.io.Serializable;
import java.util.List;

import com.frentix.olat.vitero.model.ViteroBooking;


/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for ViteroBookingConfiguration
 * 
 * <P>
 * Initial Date:  26 sept. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroBookingConfiguration implements Serializable {

	private static final long serialVersionUID = 7658813481281328834L;
	
	private int bookingId;
	private List<ViteroBooking> meetingDates;
	private boolean createMeetingImmediately;
	

	public int getBookingId() {
		return bookingId;
	}

	public void setBookingId(int bookingId) {
		this.bookingId = bookingId;
	}

	public List<ViteroBooking> getMeetingDates() {
		return meetingDates;
	}

	public void setMeetingDates(List<ViteroBooking> meetingDates) {
		this.meetingDates = meetingDates;
	}

	public boolean isCreateMeetingImmediately() {
		return createMeetingImmediately;
	}

	public void setCreateMeetingImmediately(boolean createMeetingImmediately) {
		this.createMeetingImmediately = createMeetingImmediately;
	}
	
	public boolean isUseMeetingDates() {
		return true;
	}
}
