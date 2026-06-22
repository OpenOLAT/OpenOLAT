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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
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

		String description = room.getDescription();
		if (StringHelper.containsNonWhitespace(description) && !description.equals(reference)) {
			formLayout.contextPut("description", description);
		}

		formLayout.contextPut("statusName", room.getStatus().name());
		formLayout.contextPut("statusLabel", translate("building.status." + room.getStatus().name()));

		// Action links
		calendarLink = uifactory.addFormLink("room.detail.view.calendar", formLayout, Link.BUTTON);
		calendarLink.setIconLeftCSS("o_icon o_icon-fw o_icon_calendar");

		if (room.getStatus() != RoomStatus.deleted) {
			editLink = uifactory.addFormLink("edit", formLayout, Link.BUTTON);
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
				formLayout.contextPut("nextEvent", RoomUIHelper.formatNextEvent(next, getLocale()));
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

		// Card
		String cardId = RoomUIHelper.forgeRoomCard(formLayout, room, velocity_root, getTranslator());
		formLayout.contextPut("roomCardId", cardId);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == editLink) {
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if (source == calendarLink) {
			fireEvent(ureq, new Event("viewCalendar"));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
