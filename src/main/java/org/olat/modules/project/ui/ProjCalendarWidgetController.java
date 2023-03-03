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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.DateUtils;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjAppointmentSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.model.ProjFormattedDateRange;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Feb 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjCalendarWidgetController extends FormBasicController {
	
	private FormLink titleLink;
	private FormLink createLink;
	private FormLink showAllLink;
	
	private CloseableModalController cmc;
	private ProjAppointmentEditController appointmentEditCtrl;
	
	private final ProjProject project;
	private final ProjProjectSecurityCallback secCallback;
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private CalendarManager calendarManager;

	protected ProjCalendarWidgetController(UserRequest ureq, WindowControl wControl, ProjProject project,
			ProjProjectSecurityCallback secCallback) {
		super(ureq, wControl, "calendar_widget");
		this.project = project;
		this.secCallback = secCallback;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		titleLink = uifactory.addFormLink("calendar.widget.title", formLayout);
		titleLink.setIconRightCSS("o_icon o_icon_start");
		titleLink.setElementCssClass("o_link_plain");
		
		String url = ProjectBCFactory.getCalendarUrl(project);
		titleLink.setUrl(url);
		
		createLink = uifactory.addFormLink("appointment.create", "", null, formLayout, Link.LINK + Link.NONTRANSLATED);
		createLink.setIconLeftCSS("o_icon o_icon_add");
		createLink.setElementCssClass("o_link_plain");
		createLink.setTitle(translate("appointment.create"));
		createLink.setVisible(secCallback.canCreateAppointments());
		
		showAllLink = uifactory.addFormLink("calendar.show.all", formLayout);
	}

	public void reload() {
		loadModel();
	}
	
	private void loadModel() {
		ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
		searchParams.setProject(project);
		searchParams.setStatus(List.of(ProjectStatus.active));
		List<ProjAppointment> appointments = projectService.getAppointments(searchParams);
		Kalendar kalendar = projectService.toKalendar(appointments);
		List<KalendarEvent> appointmentEvents = calendarManager.getEvents(kalendar,
				DateUtils.setTime(new Date(), 0, 0, 0), DateUtils.addDays(new Date(), 8), true);
		appointmentEvents.sort(Comparator.comparing(KalendarEvent::getBegin)
				.thenComparing(Comparator.comparing(KalendarEvent::getEnd)));
		
		Date todayStart = DateUtils.setTime(new Date(), 0, 0, 0);
		Date todayEnd = DateUtils.setTime(new Date(), 23, 59, 59);
		List<CalendarWidgetRow> todayRows = new ArrayList<>();
		List<CalendarWidgetRow> nextRows = new ArrayList<>();
		for (KalendarEvent event : appointmentEvents) {
			if (DateUtils.isOverlapping(event.getBegin(), event.getEnd(), todayStart, todayEnd)) {
				todayRows.add(createRow(event));
			} else if(nextRows.size() < 5 && todayEnd.before(event.getBegin())) {
				nextRows.add(createRow(event));
			}
		}
		flc.contextPut("todayRows", todayRows);
		flc.contextPut("nextRows", nextRows);
	}

	private CalendarWidgetRow createRow(KalendarEvent event) {
		ProjFormattedDateRange formatRange = ProjectUIFactory.formatRange(getTranslator(), event.getBegin(), event.getEnd());
		return new CalendarWidgetRow(formatRange.getDate(), formatRange.getDate2(), formatRange.getTime(), event.getSubject());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (appointmentEditCtrl == source) {
			if (event == Event.DONE_EVENT) {
				reload();
			} else if (event == Event.CANCELLED_EVENT && appointmentEditCtrl.isFirstEdit()) {
				projectService.deleteAppointmentPermanent(appointmentEditCtrl.getAppointment());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			reload();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(appointmentEditCtrl);
		removeAsListenerAndDispose(cmc);
		appointmentEditCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == titleLink || source == showAllLink) {
			fireEvent(ureq, ProjProjectDashboardController.SHOW_ALL);
		} else if (source == createLink) {
			doCreateAppointment(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doCreateAppointment(UserRequest ureq) {
		if (guardModalController(appointmentEditCtrl)) return;
		
		ProjAppointment appointment = projectService.createAppointment(getIdentity(), project);
		appointmentEditCtrl = new ProjAppointmentEditController(ureq, getWindowControl(), appointment, Set.of(getIdentity()), true, false);
		listenTo(appointmentEditCtrl);
		
		String title = translate("appointment.edit");
		cmc = new CloseableModalController(getWindowControl(), "close", appointmentEditCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	public static final class CalendarWidgetRow {
		
		private final String date;
		private final String date2;
		private final String time;
		private final String subject;
		
		public CalendarWidgetRow(String date, String date2, String time, String subject) {
			this.date = date;
			this.date2 = date2;
			this.time = time;
			this.subject = subject;
		}
		
		public String getDate() {
			return date;
		}
		
		public String getDate2() {
			return date2;
		}
		
		public String getTime() {
			return time;
		}
		
		public String getSubject() {
			return subject;
		}
		
	}

}
