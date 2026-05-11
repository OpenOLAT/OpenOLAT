/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.roommanagement.manager;

import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.roommanagement.Location;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.roommanagement.model.LocationImpl;
import org.olat.modules.roommanagement.model.RoomBookingImpl;
import org.olat.modules.roommanagement.model.RoomImpl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * Initial date: 4 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomManagementXStream {

	private static final XStream xstream = XStreamHelper.createXStreamInstanceForDBObjects();

	static {
		Class<?>[] types = new Class[] {
				Location.class, LocationImpl.class,
				Room.class, RoomImpl.class,
				RoomBooking.class, RoomBookingImpl.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));

		xstream.alias("Location", LocationImpl.class);
		xstream.alias("Room", RoomImpl.class);
		xstream.alias("RoomBooking", RoomBookingImpl.class);

		// Omit FK association fields to avoid lazy-load issues during serialization
		xstream.omitField(RoomImpl.class, "location");
		xstream.omitField(RoomBookingImpl.class, "room");
		xstream.omitField(RoomBookingImpl.class, "lectureBlock");
	}

	public static String toXml(Object obj) {
		if (obj == null) return null;
		return xstream.toXML(obj);
	}
}
