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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.project.ui;

import java.util.Date;
import java.util.stream.Collectors;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.commons.services.tag.ui.TagUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjAppointmentInfo;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.model.ProjFormattedDateRange;
import org.olat.modules.project.ui.event.AppointmentDeleteEvent;
import org.olat.modules.project.ui.event.AppointmentEditEvent;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 Feb 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjAppointmentPreviewController extends BasicController {
	
	private VelocityContainer mainVC;
	private Link editLink;
	private Link deleteLink;
	
	private final ProjArtefactReferencesController referencesCtrl;
	
	private final ProjAppointment appointment;
	private final KalendarEvent kalendarEvent;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private CalendarManager calendarManager;
	
	public ProjAppointmentPreviewController(UserRequest ureq, WindowControl wControl,
			ProjProjectSecurityCallback secCallback, ProjAppointmentInfo info,
			KalendarEvent kalendarEvent) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator()));
		this.appointment = info.getAppointment();
		this.kalendarEvent = kalendarEvent;
		
		mainVC = createVelocityContainer("appointment_preview");
		putInitialPanel(mainVC);
		
		ProjFormattedDateRange formatRange = ProjectUIFactory.formatRange(getTranslator(), kalendarEvent.getBegin(),
				kalendarEvent.getEnd());
		mainVC.contextPut("date", formatRange.getDate());
		mainVC.contextPut("date2", formatRange.getDate2());
		mainVC.contextPut("time", formatRange.getTime());
		
		mainVC.contextPut("subject", appointment.getSubject());
		mainVC.contextPut("description", appointment.getDescription());
		mainVC.contextPut("location", appointment.getLocation());
		
		String userNames = info.getMembers().stream()
				.map(member -> userManager.getUserDisplayName(member))
				.collect(Collectors.joining(" / "));
		mainVC.contextPut("members", userNames);
		
		
		mainVC.contextPut("formattedTags", TagUIFactory.getFormattedTags(getLocale(), info.getTags()));
		
		String recurrenceRule = CalendarUtils.getRecurrence(appointment.getRecurrenceRule());
		if (StringHelper.containsNonWhitespace(recurrenceRule)) {
			String recurrence = switch (recurrenceRule) {
			case "DAILY" -> translate("cal.form.recurrence.daily");
			case "WEEKLY" -> translate("cal.form.recurrence.weekly");
			case "MONTHLY" -> translate("cal.form.recurrence.monthly");
			case "YEARLY" -> translate("cal.form.recurrence.yearly");
			case KalendarEvent.WORKDAILY -> translate("cal.form.recurrence.monthly");
			case KalendarEvent.BIWEEKLY -> translate("cal.form.recurrence.biweekly");
			default -> null;
			};
			mainVC.contextPut("recurrence", recurrence);
			
			Date recurrenceEnd = calendarManager.getRecurrenceEndDate(appointment.getRecurrenceRule());
			if (recurrenceEnd != null) {
				String formatRecurrenceEnd = Formatter.getInstance(getLocale()).formatDateAndTimeLong(recurrenceEnd);
				formatRecurrenceEnd = translate("until.date", formatRecurrenceEnd);
				mainVC.contextPut("recurrenceEnd", formatRecurrenceEnd);
			}
		}
		
		referencesCtrl = new ProjArtefactReferencesController(ureq, wControl, appointment.getArtefact(), false, true, false);
		listenTo(referencesCtrl);
		mainVC.put("references", referencesCtrl.getInitialComponent());
		
		if (secCallback.canEditAppointment(appointment)) {
			editLink = LinkFactory.createButton("edit", mainVC, this);
		}
		if (secCallback.canDeleteAppointment(appointment, getIdentity())) {
			deleteLink = LinkFactory.createButton("delete", mainVC, this);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == editLink) {
			fireEvent(ureq, new AppointmentEditEvent(appointment, kalendarEvent));
		} else if (source == deleteLink) {
			fireEvent(ureq, new AppointmentDeleteEvent(appointment, kalendarEvent));
		}

	}

}
