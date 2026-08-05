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
package org.olat.modules.lecture.ui;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.roommanagement.model.RoomBookingImpl;
import org.olat.test.OlatTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Initial date: 5 August 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AddRoomsControllerTest extends OlatTestCase {

	private static Date date(int hour, int minute) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	@Test
	public void computeAdjacentSlots() {
		RoomBookingImpl morningSession = new RoomBookingImpl();
		morningSession.setStartDate(date(8, 0));
		morningSession.setEndDate(date(10, 0));

		RoomBookingImpl afternoonSession = new RoomBookingImpl();
		afternoonSession.setStartDate(date(14, 0));
		afternoonSession.setEndDate(date(16, 0));

		RoomBookingImpl eveningSession = new RoomBookingImpl();
		eveningSession.setStartDate(date(18, 0));
		eveningSession.setEndDate(date(20, 0));

		List<RoomBooking> bookings = Arrays.asList(eveningSession, morningSession, afternoonSession);

		AddRoomsRow row1 = new AddRoomsRow(null, null, false, 0);
		AddRoomsController.computeAdjacentSlots(row1, date(8, 0), date(10, 0), bookings, morningSession);

		Assert.assertEquals(date(6, 0), row1.getEarlierSlotFrom());
		Assert.assertEquals(date(8, 0), row1.getEarlierSlotTo());
		Assert.assertEquals(date(10, 0), row1.getLaterSlotFrom());
		Assert.assertEquals(date(12, 0), row1.getLaterSlotTo());

		AddRoomsRow row2 = new AddRoomsRow(null, null, false, 0);
		AddRoomsController.computeAdjacentSlots(row2, date(13, 0), date(17, 0), bookings, afternoonSession);

		Assert.assertEquals(date(10, 0), row2.getEarlierSlotFrom());
		Assert.assertEquals(date(14, 0), row2.getEarlierSlotTo());
		Assert.assertNull(row2.getLaterSlotFrom());
		Assert.assertNull(row2.getLaterSlotTo());

		AddRoomsRow row3 = new AddRoomsRow(null, null, false, 0);
		AddRoomsController.computeAdjacentSlots(row3, date(18, 0), date(23, 0), bookings, eveningSession);

		Assert.assertEquals(date(3, 0), row3.getEarlierSlotFrom());
		Assert.assertEquals(date(8, 0), row3.getEarlierSlotTo());
		Assert.assertNull(row3.getLaterSlotFrom());
		Assert.assertNull(row3.getLaterSlotTo());
	}
}