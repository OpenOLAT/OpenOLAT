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

import java.io.Serial;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;

/**
 * Preview callout shown when clicking a booking in the room scheduling calendar.
 *
 * Initial date: 7 Aug 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomSchedulingBookingCalloutController extends BasicController {

	private Link openInCoursePlannerLink;
	private String openInCoursePlannerUrl;

	public RoomSchedulingBookingCalloutController(UserRequest ureq, WindowControl wControl, RoomBooking booking) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("room_scheduling_booking_callout");

		Room room = booking.getRoom();
		if (room != null) {
			String roomRef = StringHelper.containsNonWhitespace(room.getExternalRef())
					? room.getExternalRef() : room.getDescription();
			String roomDesc = room.getDescription();
			if (StringHelper.containsNonWhitespace(roomDesc) && roomDesc.equals(roomRef)) {
				roomDesc = null;
			}
			mainVC.contextPut("roomRef", roomRef);
			mainVC.contextPut("roomDescription", roomDesc);
		}

		LectureBlock lb = booking.getLectureBlock();
		if (lb != null) {
			mainVC.contextPut("eventTitle", lb.getTitle());
			mainVC.contextPut("eventExternalRef", lb.getExternalRef());

			openInCoursePlannerUrl = BusinessControlFactory.getInstance()
					.getRelativeURLFromBusinessPathString(RoomUIHelper.getEventsBusinessPath(lb));
			openInCoursePlannerLink = LinkFactory.createButton("room.scheduling.details.open.in.course.planner", mainVC, this);
			openInCoursePlannerLink.setIconLeftCSS("o_icon o_icon-fw o_icon_external_link");
			openInCoursePlannerLink.setNewWindow(true, true);
		}

		if (booking.getStartDate() != null && booking.getEndDate() != null) {
			Formatter formatter = Formatter.getInstance(getLocale());
			mainVC.contextPut("date", formatter.formatDateWithDay(booking.getStartDate()));
			mainVC.contextPut("timeRange", formatter.formatTimeShort(booking.getStartDate())
					+ " - " + formatter.formatTimeShort(booking.getEndDate()));
		}

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == openInCoursePlannerLink) {
			fireEvent(ureq, new OpenInCoursePlannerEvent(openInCoursePlannerUrl));
		}
	}

	public static class OpenInCoursePlannerEvent extends Event {

		@Serial
		private static final long serialVersionUID = -7731308373583903056L;

		private static final String CMD = "openInCoursePlanner";

		private final String url;

		public OpenInCoursePlannerEvent(String url) {
			super(CMD);
			this.url = url;
		}

		public String getUrl() {
			return url;
		}
	}
}
