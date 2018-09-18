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

import java.util.List;

import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.util.StringHelper;
import org.olat.modules.vitero.model.ViteroBooking;
import org.olat.properties.Property;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  10 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroBookingDataModel implements TableDataModel<ViteroBooking> {
	
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
			case name: return booking.getGroupName();
			case begin: return booking.getStart();
			case end: return booking.getEnd();
			case roomSize: {
				int roomSize = booking.getRoomSize();
				if(roomSize > 0) {
					return Integer.toString(roomSize);
				}
				return "-";
			}
			case resource: {
				Property property = booking.getProperty();
				if(property.getGrp() != null) {
					return property.getGrp().getName();
				} else if(StringHelper.containsNonWhitespace(booking.getResourceName())) {
					return booking.getResourceName();
				} else if(StringHelper.containsNonWhitespace(property.getResourceTypeName())) {
					return property.getResourceTypeName() + "(" + property.getResourceTypeId() + ")";
				}
				return "";
			}
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
			case group: {
				if(signedInBookings != null) {
					for(ViteroBooking signedInBooking: signedInBookings) {
						if(booking.getBookingId() == signedInBooking.getBookingId()) {
							return Boolean.TRUE;
						}
					}
				}
				return Boolean.FALSE;
			}
			default: return "";
		}
	}

	@Override
	public void setObjects(List<ViteroBooking> objects) {
		this.bookings = objects;
	}

	@Override
	public Object createCopyWithEmptyList() {
		return new ViteroBookingDataModel();
	}

	public enum Column {
		name,
		begin,
		end,
		group,
		roomSize,
		resource,
		open,
		sign,
	}
}