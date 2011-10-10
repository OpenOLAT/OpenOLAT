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
package com.frentix.olat.vitero.manager;

import java.util.Date;

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
		module = new ViteroModule();
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
		Bookingtype type = manager.getBookingById(id);
		System.out.println(type.getBooking().getBookingid());
	}
	
	public void testGetLicense() {
		
		manager.getLicence(new Date(), new Date());
		System.out.println("Licence");
	}
	

}
