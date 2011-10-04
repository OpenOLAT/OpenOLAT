package com.frentix.olat.vc.provider.vitero;

import de.bps.course.nodes.vc.DefaultVCConfiguration;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for ViteroBookingConfiguration
 * 
 * <P>
 * Initial Date:  26 sept. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroBookingConfiguration extends DefaultVCConfiguration {

	private static final long serialVersionUID = 7658813481281328834L;
	
	private int bookingId;
	

	public int getBookingId() {
		return bookingId;
	}

	public void setBookingId(int bookingId) {
		this.bookingId = bookingId;
	}
	
	

	@Override
	public boolean isUseMeetingDates() {
		return true;
	}

	@Override
	public boolean isConfigValid() {
		return true;
	}

	
	
}
