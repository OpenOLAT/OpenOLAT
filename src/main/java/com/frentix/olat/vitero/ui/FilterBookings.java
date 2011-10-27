package com.frentix.olat.vitero.ui;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.frentix.olat.vitero.model.ViteroBooking;

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
