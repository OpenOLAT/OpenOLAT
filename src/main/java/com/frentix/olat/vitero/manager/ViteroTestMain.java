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
package com.frentix.olat.vitero.manager;

import com.frentix.olat.vitero.ViteroModule;
import com.frentix.olat.vitero.manager.stubs.BookingServiceStub.Bookingtype;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  6 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroTestMain {
	
	private final ViteroModule module;
	private final ViteroManager manager;
	
	public ViteroTestMain() {
		module = new ViteroModule(null);
		module.setAdminLogin("admin");
		module.setAdminPassword("007");
		module.setProtocol("http");
		module.setBaseUrl("192.168.1.54");
		module.setPort(8080);
		module.setContextPath("vitero");
		
		manager = new ViteroManager();
		manager.setViteroModule(module);
	}
	
	public static final void main(String[] args) {
		ViteroTestMain main = new ViteroTestMain();
		
		//booking by id
		//new ViteroTestMain().testGetBookingById(3);
		
		//licence
		main.testGetLicense();
	}
	
	public void testGetBookingById(int id) {
		try {
			Bookingtype type = manager.getBookingById(id);
			System.out.println(type.getBooking().getBookingid());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testGetLicense() {
		
		try {
			manager.getLicencedRoomSizes();
			System.out.println("Licence");
		} catch (VmsNotAvailableException e) {
			e.printStackTrace();
		}
	}
	

}
