package com.frentix.olat.vc.provider.vitero;

import com.frentix.olat.vc.provider.vitero.stubs.BookingServiceStub.Bookingtype;

public class ViteroTestMain {
	
	private final ViteroBookingProvider provider;
	
	public ViteroTestMain() {
		provider = new ViteroBookingProvider();
		provider.setAdminLogin("admin");
		provider.setAdminPassword("007");
		provider.setProtocol("http");
		provider.setBaseUrl("192.168.1.54");
		provider.setPort(8080);
		provider.setContextPath("vitero");
	}
	
	public static final void main(String[] args) {
		new ViteroTestMain().testGetBookingById(3);
	}
	
	public void testGetBookingById(int id) {
		Bookingtype type = provider.getBookingById(id);
		System.out.println(type.getBooking().getBookingid());
	}
	

}
