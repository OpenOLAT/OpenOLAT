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
package com.frentix.olat.vitero.ui;

import java.util.List;

import org.olat.core.gui.components.table.TableDataModel;

import com.frentix.olat.vitero.model.ViteroBooking;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  10 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroBookingDataModel implements TableDataModel {
	
	private List<ViteroBooking> bookings;
	private List<ViteroBooking> signedInBookings;
	
	
	public ViteroBookingDataModel() {
		//
	}
	
	public ViteroBookingDataModel(List<ViteroBooking> bookings) {
		this.bookings = bookings;
	}
	
	public ViteroBookingDataModel(List<ViteroBooking> bookings, List<ViteroBooking> signedInBookings) {
		this.bookings = bookings;
		this.signedInBookings = signedInBookings;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}
	
	@Override
	public int getRowCount() {
		return bookings == null ? 0 : bookings.size();
	}
	
	@Override
	public ViteroBooking getObject(int row) {
		return bookings.get(row);
	}

	@Override
	public Object getValueAt(int row, int col) {
		ViteroBooking booking = getObject(row);
		switch(Column.values()[col]) {
			case begin: return booking.getStart();
			case end: return booking.getEnd();
			case sign: {
				boolean auto = booking.isAutoSignIn();
				if(auto) {
					if(signedInBookings != null) {
						for(ViteroBooking signedInBooking: signedInBookings) {
							if(booking.getBookingId() == signedInBooking.getBookingId()) {
								return Sign.signout;
							}
						}
					}
					return Sign.signin;
				}
				return Sign.no;
			}
			default: return "";
		}
	}

	@Override
	public void setObjects(List objects) {
		this.bookings = objects;
	}

	@Override
	public Object createCopyWithEmptyList() {
		return new ViteroBookingDataModel();
	}
	
	public enum Sign {
		signin,
		signout,
		no,
	}

	public enum Column {
		begin,
		end,
		open,
		sign,
	}
}