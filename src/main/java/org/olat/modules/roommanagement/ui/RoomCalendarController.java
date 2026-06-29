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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.components.FullCalendarElement;
import org.olat.commons.calendar.ui.components.FullCalendarViews;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.commons.services.color.ColorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.modules.roommanagement.model.RoomRefImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 10 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomCalendarController extends FormBasicController {

	private final Room room;

	@Autowired
	private RoomManagementService roomManagementService;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private ColorService colorService;

	public RoomCalendarController(UserRequest ureq, WindowControl wControl, Room room) {
		super(ureq, wControl, "room_calendar");
		setTranslator(Util.createPackageTranslator(CalendarManager.class, ureq.getLocale(), getTranslator()));
		this.room = room;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String calId = "room." + room.getKey();
		Kalendar calendar = new Kalendar(calId, "Room");

		List<RoomBooking> bookings = roomManagementService.getBookingsForRoom(new RoomRefImpl(room.getKey()), null, null);
		Set<Long> bookingKeysWithWarnings = RoomUIHelper.computeBookingKeysWithWarnings(bookings, lectureService);
		for (RoomBooking booking : bookings) {
			if (booking.getStartDate() == null || booking.getEndDate() == null) continue;
			String subject = resolveSubject(booking);
			String eventId = CodeHelper.getGlobalForeverUniqueID();
			ZonedDateTime zStart = DateUtils.toZonedDateTime(booking.getStartDate(), calendarModule.getDefaultZoneId());
			ZonedDateTime zEnd = DateUtils.toZonedDateTime(booking.getEndDate(), calendarModule.getDefaultZoneId());
			KalendarEvent event = new KalendarEvent(eventId, null, subject, zStart, zEnd);
			if (bookingKeysWithWarnings.contains(booking.getKey())) {
				event.setComment("warning");
			}
			calendar.addEvent(event);
		}

		String displayName = StringHelper.containsNonWhitespace(room.getExternalRef())
				? room.getExternalRef() : room.getDescription();
		KalendarRenderWrapper wrapper = new KalendarRenderWrapper(calendar, displayName, calId);
		wrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
		wrapper.setPrivateEventsVisible(true);
		Building building = room.getBuilding();
		String colorCss = building != null && StringHelper.containsNonWhitespace(building.getColorCss())
				? building.getColorCss() : colorService.getDefaultColor();
		wrapper.setCssClass("o_rm_cal_pastel o_color_border " + colorCss);

		FullCalendarElement calendarEl = new FullCalendarElement(ureq, "roomCalendar", List.of(wrapper), getTranslator());
		calendarEl.setView(FullCalendarViews.timeGridWeek);
		calendarEl.setShowEventDuration(true);
		formLayout.add(calendarEl);
	}

	private String resolveSubject(RoomBooking booking) {
		if (booking.getLectureBlock() != null
				&& StringHelper.containsNonWhitespace(booking.getLectureBlock().getTitle())) {
			return booking.getLectureBlock().getTitle();
		}
		return StringHelper.containsNonWhitespace(room.getExternalRef())
				? room.getExternalRef() : room.getDescription();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// read-only calendar
	}
}
