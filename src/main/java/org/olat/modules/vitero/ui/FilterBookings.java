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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.vitero.ui;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.olat.modules.vitero.model.ViteroBooking;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  11 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class FilterBookings {
	
	public static void filterMyFutureBookings(final List<ViteroBooking> bookings, final List<ViteroBooking> signedInBookings) {
		//only the bookings in the future
		Date now = new Date();
		for(Iterator<ViteroBooking> it=bookings.iterator(); it.hasNext(); ) {
			ViteroBooking booking = it.next();
			Date end = booking.getEnd();
			if(end.before(now)) {
				it.remove();
			} else if(!booking.isAutoSignIn()) {
				boolean in = false;
				for(ViteroBooking signedInBooking:signedInBookings) {
					if(signedInBooking.getBookingId() == booking.getBookingId()) {
						in = true;//already in
					}
				}
				if(!in) {
					it.remove();
				}
			}
		}
	}

}
