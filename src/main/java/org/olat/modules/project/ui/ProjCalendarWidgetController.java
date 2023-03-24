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
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
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
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjMilestoneSearchParams;
import org.olat.modules.project.ProjMilestoneStatus;
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
	private FormLink appointmentCreateLink;
	private FormLink milestoneCreateLink;
	private FormLink showAllLink;
	
	private CloseableModalController cmc;
	private ProjAppointmentEditController appointmentEditCtrl;
	private ProjMilestoneEditController milestoneEditCtrl;
	
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
		
		if (secCallback.canCreateAppointments() && secCallback.canCreateMilestones()) {
			DropdownItem createDropdown = uifactory.addDropdownMenu("create.dropdown", null, null, formLayout, getTranslator());
			createDropdown.setCarretIconCSS("o_icon o_icon_lg o_icon_add");
			createDropdown.setOrientation(DropdownOrientation.right);
			createDropdown.setButton(false);
			createDropdown.setElementCssClass("o_link_plain");
			createDropdown.setEmbbeded(true);
			
			appointmentCreateLink = uifactory.addFormLink("appointment.create", "appointment.create", null, formLayout, Link.LINK);
			appointmentCreateLink.setIconLeftCSS("o_icon o_icon_fw o_icon_add");
			appointmentCreateLink.setVisible(secCallback.canCreateAppointments());
			createDropdown.addElement(appointmentCreateLink);
			
			milestoneCreateLink = uifactory.addFormLink("milestone.create", "milestone.create", null, formLayout, Link.LINK);
			milestoneCreateLink.setIconLeftCSS("o_icon o_icon_fw o_icon_add");
			milestoneCreateLink.setVisible(secCallback.canCreateAppointments());
			createDropdown.addElement(milestoneCreateLink);
			
		} else if (secCallback.canCreateAppointments()) {
			appointmentCreateLink = uifactory.addFormLink("appointment.create", "", null, formLayout, Link.BUTTON + Link.NONTRANSLATED);
			appointmentCreateLink.setIconLeftCSS("o_icon o_icon_add");
			appointmentCreateLink.setElementCssClass("o_link_plain");
			appointmentCreateLink.setTitle(translate("appointment.create"));
			appointmentCreateLink.setGhost(true);
			appointmentCreateLink.setVisible(secCallback.canCreateAppointments());
		} else if (secCallback.canCreateMilestones()) {
			milestoneCreateLink = uifactory.addFormLink("milestone.create", "", null, formLayout, Link.BUTTON + Link.NONTRANSLATED);
			milestoneCreateLink.setIconLeftCSS("o_icon o_icon_add");
			milestoneCreateLink.setElementCssClass("o_link_plain");
			milestoneCreateLink.setTitle(translate("milestone.create"));
			milestoneCreateLink.setGhost(true);
			milestoneCreateLink.setVisible(secCallback.canCreateMilestones());
		}
		
		showAllLink = uifactory.addFormLink("calendar.show.all", formLayout);
	}

	public void reload() {
		loadModel();
	}
	
	private void loadModel() {
		ProjMilestoneSearchParams milestoneSearchParams = new ProjMilestoneSearchParams();
		milestoneSearchParams.setProject(project);
		milestoneSearchParams.setStatus(List.of(ProjectStatus.active));
		List<ProjMilestone> milestones = projectService.getMilestones(milestoneSearchParams);
		if (!milestones.isEmpty()) {
			milestones.sort((m1, m2) -> m1.getDueDate().compareTo(m2.getDueDate()));
			List<MilestoneRow> milestoneRows = new ArrayList<>(1);
			Date now = DateUtils.addDays(new Date(), -1);
			boolean inFutureFound = false;
			for (ProjMilestone milestone : milestones) {
				if (milestone.getStatus() == ProjMilestoneStatus.open && milestone.getDueDate().before(now)) {
					milestoneRows.add(new MilestoneRow(ProjectUIFactory.getDisplayName(getTranslator(), milestone), milestone.getDueDate(), true));
				} else if (!inFutureFound && milestone.getDueDate().after(now)) {
					milestoneRows.add(new MilestoneRow(ProjectUIFactory.getDisplayName(getTranslator(), milestone), milestone.getDueDate(), false));
					inFutureFound = true;
				}
			}
			if (!milestoneRows.isEmpty()) {
				flc.contextPut("milestoneRows", milestoneRows);
			}
		}
		
		
		ProjAppointmentSearchParams appointmentSearchParams = new ProjAppointmentSearchParams();
		appointmentSearchParams.setProject(project);
		appointmentSearchParams.setStatus(List.of(ProjectStatus.active));
		List<ProjAppointment> appointments = projectService.getAppointments(appointmentSearchParams);
		Kalendar kalendar = projectService.getAppointmentsKalendar(appointments);
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
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if (event == Event.CANCELLED_EVENT && appointmentEditCtrl.isFirstEdit()) {
				projectService.deleteAppointmentPermanent(appointmentEditCtrl.getAppointment());
			}
			cmc.deactivate();
			cleanUp();
		} else if (milestoneEditCtrl == source) {
			if (event == Event.DONE_EVENT) {
				reload();
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if (event == Event.CANCELLED_EVENT && milestoneEditCtrl.isFirstEdit()) {
				projectService.deleteMilestonePermanent(milestoneEditCtrl.getMilestone());
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
		removeAsListenerAndDispose(milestoneEditCtrl);
		removeAsListenerAndDispose(cmc);
		appointmentEditCtrl = null;
		milestoneEditCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == titleLink || source == showAllLink) {
			fireEvent(ureq, ProjProjectDashboardController.SHOW_ALL);
		} else if (source == appointmentCreateLink) {
			doCreateAppointment(ureq);
		} else if (source == milestoneCreateLink) {
			doCreateMilestone(ureq);
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

	private void doCreateMilestone(UserRequest ureq) {
		if (guardModalController(milestoneEditCtrl)) return;
		
		ProjMilestone milestone = projectService.createMilestone(getIdentity(), project);
		milestoneEditCtrl = new ProjMilestoneEditController(ureq, getWindowControl(), milestone, true);
		listenTo(milestoneEditCtrl);
		
		String title = translate("milestone.edit");
		cmc = new CloseableModalController(getWindowControl(), "close", milestoneEditCtrl.getInitialComponent(), true, title, true);
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
	
	public static final class MilestoneRow {
		
		private final String displayName;
		private final Date dueDate;
		private final boolean warning;
		
		public MilestoneRow(String displayName, Date dueDate, boolean warning) {
			this.displayName = displayName;
			this.dueDate = dueDate;
			this.warning = warning;
		}

		public String getDisplayName() {
			return displayName;
		}

		public Date getDueDate() {
			return dueDate;
		}

		public boolean isWarning() {
			return warning;
		}
		
	}

}
