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
package org.olat.modules.roommanagement.ui;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.util.StringHelper;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.model.RoomRefImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 8 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomDetailsController extends FormBasicController {

	private FormLink editLink;
	private FormLink calendarLink;
	private FormLink appleMapsLink;
	private FormLink googleMapsLink;
	private String appleMapsUrl;
	private String googleMapsUrl;
	private final Room room;

	@Autowired
	private RoomManagementService roomManagementService;

	public RoomDetailsController(UserRequest ureq, WindowControl wControl, Room room, Form mainForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "room_details_view", mainForm);
		this.room = room;
		initForm(ureq);
	}

	public Room getRoom() {
		return room;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String reference = room.getExternalRef();
		if (StringHelper.containsNonWhitespace(reference)) {
			formLayout.contextPut("reference", reference);
		}

		formLayout.contextPut("statusName", room.getStatus().name());
		formLayout.contextPut("statusLabel", translate("building.status." + room.getStatus().name()));

		// Action links
		calendarLink = uifactory.addFormLink("room.detail.view.calendar", formLayout, Link.BUTTON);
		calendarLink.setIconLeftCSS("o_icon o_icon-fw o_icon_calendar");

		if (room.getStatus() != RoomStatus.deleted) {
			editLink = uifactory.addFormLink("room.detail.edit", formLayout, Link.BUTTON);
			editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		}

		// Seats
		formLayout.contextPut("seats", room.getSeats());

		// Building info
		Building building = room.getBuilding();
		if (building != null) {
			String buildingRef = building.getExternalRef();
			if (StringHelper.containsNonWhitespace(buildingRef)) {
				formLayout.contextPut("buildingRef", buildingRef);
			}
			String buildingDesc = building.getDescription();
			if (StringHelper.containsNonWhitespace(buildingDesc)
					&& !buildingDesc.equals(buildingRef)) {
				formLayout.contextPut("buildingDesc", buildingDesc);
			}
			formLayout.contextPut("buildingStatusName", building.getStatus().name());
			formLayout.contextPut("buildingStatusLabel", translate("building.status." + building.getStatus().name()));
			if (StringHelper.containsNonWhitespace(building.getColorCss())) {
				formLayout.contextPut("buildingColorCss", building.getColorCss());
			}
		}

		// Next booking
		List<RoomBooking> futureBookings = roomManagementService.getBookingsForRoom(
				new RoomRefImpl(room.getKey()), new Date(), null);
		if (!futureBookings.isEmpty()) {
			RoomBooking next = futureBookings.get(0);
			if (next.getStartDate() != null && next.getEndDate() != null) {
				formLayout.contextPut("nextEvent", formatNextEvent(next));
			}
		}

		// Occupancy rate (current month)
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date startOfMonth = cal.getTime();
		cal.add(Calendar.MONTH, 1);
		Date endOfMonth = cal.getTime();

		List<RoomBooking> monthBookings = roomManagementService.getBookingsForRoom(
				new RoomRefImpl(room.getKey()), startOfMonth, endOfMonth);
		if (!monthBookings.isEmpty()) {
			long bookedMinutes = 0;
			for (RoomBooking booking : monthBookings) {
				if (booking.getStartDate() != null && booking.getEndDate() != null) {
					long durationMs = booking.getEndDate().getTime() - booking.getStartDate().getTime();
					bookedMinutes += durationMs / 60000L;
				}
			}
			int availableMinutes = 21 * 9 * 60;
			int pct = (int) Math.round(bookedMinutes * 100.0 / availableMinutes);
			formLayout.contextPut("occupancyRate", String.valueOf(pct));
		}

		// Additional info (room info)
		if (StringHelper.containsNonWhitespace(room.getRoomInfo())) {
			formLayout.contextPut("additionalInfo", StringHelper.xssScan(room.getRoomInfo()));
		}

		// Administrative info
		if (StringHelper.containsNonWhitespace(room.getAdminInfo())) {
			formLayout.contextPut("adminInfo", StringHelper.xssScan(room.getAdminInfo()));
		}

		// Visiting card map sub-layout (reuse building_detail_map.html)
		FormLayoutContainer mapCont = FormLayoutContainer.createCustomFormLayout(
				"roomVisitingCardMap", getTranslator(), velocity_root + "/building_detail_map.html");
		formLayout.add(mapCont);

		if (building != null) {
			if (StringHelper.containsNonWhitespace(building.getColorCss())) {
				mapCont.contextPut("colorCss", building.getColorCss());
			}
			if (building.getGeoLatitude() != null && building.getGeoLongitude() != null) {
				mapCont.contextPut("geoLat", building.getGeoLatitude());
				mapCont.contextPut("geoLon", building.getGeoLongitude());
				String leafletCssUri = StaticMediaDispatcher.getStaticURI("js/leaflet/leaflet.css");
				JSAndCSSComponent leafletLoader = new JSAndCSSComponent("leafletLoader",
						new String[] { "js/leaflet/leaflet.min.js" },
						new String[] { leafletCssUri });
				mapCont.put("leafletLoader", leafletLoader);
			}

			if (StringHelper.containsNonWhitespace(building.getAddress())) {
				formLayout.contextPut("address", building.getAddress());
				String query = URLEncoder.encode(building.getAddress(), StandardCharsets.UTF_8);
				appleMapsUrl = "https://maps.apple.com/?q=" + query;
				googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=" + query;

				appleMapsLink = uifactory.addFormLink("room.detail.apple.maps", "room.detail.apple.maps",
						"building.apple.maps", null, mapCont, Link.BUTTON);
				appleMapsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_arrow_up_right_from_square");
				appleMapsLink.setUrl(appleMapsUrl);
				appleMapsLink.setNewWindow(true, true, false);

				googleMapsLink = uifactory.addFormLink("room.detail.google.maps", "room.detail.google.maps",
						"building.google.maps", null, mapCont, Link.BUTTON);
				googleMapsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_arrow_up_right_from_square");
				googleMapsLink.setUrl(googleMapsUrl);
				googleMapsLink.setNewWindow(true, true, false);
			}

			if (StringHelper.containsNonWhitespace(building.getInfoUrl())) {
				formLayout.contextPut("infoUrl", building.getInfoUrl());
			}
		}
	}

	private String formatNextEvent(RoomBooking booking) {
		Date startDate = booking.getStartDate();
		Date endDate = booking.getEndDate();

		String dayOfWeek = new SimpleDateFormat("EEE", getLocale()).format(startDate);
		String date = DateFormat.getDateInstance(DateFormat.SHORT, getLocale()).format(startDate);
		String time = DateFormat.getTimeInstance(DateFormat.SHORT, getLocale()).format(startDate);

		long durationMinutes = (endDate.getTime() - startDate.getTime()) / 60000L;
		String duration = formatDuration(durationMinutes);

		return dayOfWeek + " " + date + ", " + time + " " + duration;
	}

	private String formatDuration(long minutes) {
		long hours = minutes / 60;
		long mins = minutes % 60;
		if (hours > 0 && mins > 0) {
			return hours + "h " + mins + "m";
		} else if (hours > 0) {
			return hours + "h";
		} else {
			return mins + "m";
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == editLink) {
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if (source == calendarLink) {
			fireEvent(ureq, new Event("viewCalendar"));
		} else if (source == appleMapsLink) {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(appleMapsUrl));
		} else if (source == googleMapsLink) {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(googleMapsUrl));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
