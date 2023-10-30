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
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarRecurEvent;
import org.olat.commons.calendar.ui.ConfirmUpdateController;
import org.olat.commons.calendar.ui.events.CalendarGUIUpdateEvent;
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
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjAppointmentInfo;
import org.olat.modules.project.ProjAppointmentSearchParams;
import org.olat.modules.project.ProjArtefactInfoParams;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjMilestoneSearchParams;
import org.olat.modules.project.ProjMilestoneStatus;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Feb 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjCalendarWidgetController extends FormBasicController {
	
	private static final String CMD_EDIT = "edit";
	
	private FormLink titleLink;
	private FormLink appointmentCreateLink;
	private FormLink milestoneCreateLink;
	private FormLink showAllLink;
	
	private CloseableModalController cmc;
	private ProjAppointmentEditController appointmentEditCtrl;
	private ProjMilestoneEditController milestoneEditCtrl;
	private ConfirmUpdateController appointmentEditAllCtr;
	
	private final ProjectBCFactory bcFactory;
	private final ProjProject project;
	private final ProjProjectSecurityCallback secCallback;
	private final Formatter formatter;
	private int counter = 0;
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private CalendarManager calendarManager;

	public ProjCalendarWidgetController(UserRequest ureq, WindowControl wControl, ProjectBCFactory bcFactory,
			ProjProject project, ProjProjectSecurityCallback secCallback) {
		super(ureq, wControl, "calendar_widget");
		this.bcFactory = bcFactory;
		this.project = project;
		this.secCallback = secCallback;
		this.formatter = Formatter.getInstance(getLocale());
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		titleLink = uifactory.addFormLink("calendar.widget.title", formLayout);
		titleLink.setIconRightCSS("o_icon o_icon_start");
		titleLink.setElementCssClass("o_link_plain");
		
		String url = bcFactory.getCalendarUrl(project);
		titleLink.setUrl(url);
		
		if (secCallback.canCreateAppointments() && secCallback.canCreateMilestones()) {
			DropdownItem createDropdown = uifactory.addDropdownMenu("create.dropdown", null, null, formLayout, getTranslator());
			createDropdown.setCarretIconCSS("o_icon o_icon_lg o_icon_add");
			createDropdown.setAriaLabel(translate("calendar.widget.commands.open"));
			createDropdown.setOrientation(DropdownOrientation.right);
			createDropdown.setButton(false);
			createDropdown.setGhost(true);
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
			appointmentCreateLink.setTitle(translate("appointment.create"));
			appointmentCreateLink.setGhost(true);
			appointmentCreateLink.setVisible(secCallback.canCreateAppointments());
		} else if (secCallback.canCreateMilestones()) {
			milestoneCreateLink = uifactory.addFormLink("milestone.create", "", null, formLayout, Link.BUTTON + Link.NONTRANSLATED);
			milestoneCreateLink.setIconLeftCSS("o_icon o_icon_add");
			milestoneCreateLink.setTitle(translate("milestone.create"));
			milestoneCreateLink.setGhost(true);
			milestoneCreateLink.setVisible(secCallback.canCreateMilestones());
		}
		
		showAllLink = uifactory.addFormLink("calendar.show.all", "", null, formLayout, Link.LINK + Link.NONTRANSLATED);
		showAllLink.setUrl(url);
	}

	public void reload() {
		loadModel();
	}
	
	private void loadModel() {
		int count = 0;
		
		ProjMilestoneSearchParams milestoneSearchParams = new ProjMilestoneSearchParams();
		milestoneSearchParams.setProject(project);
		milestoneSearchParams.setStatus(List.of(ProjectStatus.active));
		List<ProjMilestone> milestones = projectService.getMilestones(milestoneSearchParams);
		count += milestones.size();
		
		milestones.removeIf(milestone -> milestone.getDueDate() == null);
		if (!milestones.isEmpty()) {
			milestones.sort((m1, m2) -> m1.getDueDate().compareTo(m2.getDueDate()));
			List<CalendarWidgetRow> milestoneRows = new ArrayList<>(1);
			Date now = DateUtils.addDays(new Date(), -1);
			boolean inFutureFound = false;
			for (ProjMilestone milestone : milestones) {
				if (milestone.getStatus() == ProjMilestoneStatus.open && milestone.getDueDate().before(now)) {
					milestoneRows.add(new CalendarWidgetRow(
							ProjectUIFactory.getDisplayName(getTranslator(), milestone),
							"<i class=\"o_icon o_icon-fw o_icon_calendar\"> </i> " + formatter.formatDate(milestone.getDueDate()),
							true
							, ProjectUIFactory.COLOR_MILESTONE));
				} else if (!inFutureFound && milestone.getDueDate().after(now)) {
					milestoneRows.add(new CalendarWidgetRow(
							ProjectUIFactory.getDisplayName(getTranslator(), milestone),
							"<i class=\"o_icon o_icon-fw o_icon_calendar\"> </i> " + formatter.formatDate(milestone.getDueDate()),
							false
							, ProjectUIFactory.COLOR_MILESTONE));
					inFutureFound = true;
				}
			}
			if (!milestoneRows.isEmpty()) {
				flc.contextPut("milestoneRows", milestoneRows);
			}
		}
		
		// Load the appointments without date to number of events
		ProjAppointmentSearchParams appointmentSearchParams = new ProjAppointmentSearchParams();
		appointmentSearchParams.setProject(project);
		appointmentSearchParams.setStatus(List.of(ProjectStatus.active));
		appointmentSearchParams.setDatesNull(Boolean.TRUE);
		count += projectService.getAppointments(appointmentSearchParams).size();
		
		// Load the appointments with date to display them
		appointmentSearchParams.setDatesNull(Boolean.FALSE);
		List<ProjAppointment> appointments = projectService.getAppointments(appointmentSearchParams);
		Map<String, ProjAppointment> appointmentIdentToAppointment = appointments.stream()
				.collect(Collectors.toMap(ProjAppointment::getIdentifier, Function.identity()));
		
		Kalendar kalendar = projectService.getAppointmentsKalendar(appointments);
		List<KalendarEvent> appointmentEvents = calendarManager.getEvents(kalendar,
				DateUtils.addYears(new Date(), -30), DateUtils.addYears(new Date(), 10), true);
		count += appointmentEvents.size();
		
		Date todayStart = DateUtils.setTime(new Date(), 0, 0, 0);
		Date todayNow = new Date();
		Date todayEnd = DateUtils.setTime(new Date(), 23, 59, 59);
		appointmentEvents.removeIf(event -> todayNow.after(event.isAllDayEvent()? DateUtils.setTime(event.getEnd(), 23, 59, 59): event.getEnd()));
		appointmentEvents.sort(Comparator.comparing(KalendarEvent::getBegin)
				.thenComparing(Comparator.comparing(KalendarEvent::getEnd)));
		
		List<CalendarWidgetRow> todayRows = new ArrayList<>();
		List<CalendarWidgetRow> nextRows = new ArrayList<>(5);
		for (KalendarEvent event : appointmentEvents) {
			Date eventBegin = event.isAllDayEvent()? DateUtils.setTime(event.getBegin(), 0, 0, 1): event.getBegin();
			Date eventEnd = event.isAllDayEvent()? DateUtils.setTime(event.getEnd(), 23, 59, 59): event.getEnd();
			if (DateUtils.isOverlapping(eventBegin, eventEnd, todayNow, todayEnd)) {
				if (event.isAllDayEvent()) {
					CalendarWidgetRow row = new CalendarWidgetRow(
							ProjectUIFactory.getDisplayName(getTranslator(), event), 
							"<i class=\"o_icon o_icon-fw o_icon_time\"> </i> " + getTranslator().translate("all.day"),
							false,
							getEventColorCss(event));
					forgeDisplayNameLink(row, event, appointmentIdentToAppointment.get(event.getExternalId()));
					todayRows.add(row);
				} else if (event.getBegin().before(todayStart)) {
					CalendarWidgetRow row = new CalendarWidgetRow(
							ProjectUIFactory.getDisplayName(getTranslator(), event), 
							"<i class=\"o_icon o_icon-fw o_icon_time\"> </i> " + formatter.formatTimeShort(todayStart),
							false,
							getEventColorCss(event));
					forgeDisplayNameLink(row, event, appointmentIdentToAppointment.get(event.getExternalId()));
					todayRows.add(row);
				} else {
					CalendarWidgetRow row = new CalendarWidgetRow(
							ProjectUIFactory.getDisplayName(getTranslator(), event), 
							"<i class=\"o_icon o_icon-fw o_icon_time\"> </i> " + formatter.formatTimeShort(event.getBegin()),
							false,
							getEventColorCss(event));
					forgeDisplayNameLink(row, event, appointmentIdentToAppointment.get(event.getExternalId()));
					todayRows.add(row);
				}
			} else if(nextRows.size() < 5 && todayEnd.before(event.getBegin())) {
				String dueName = "<i class=\"o_icon o_icon-fw o_icon_calendar\"> </i> " + formatter.formatDate(event.getBegin());
				if (!event.isAllDayEvent()) {
					dueName += " <i class=\"o_icon o_icon-fw o_icon_time\"> </i> " + formatter.formatTimeShort(event.getBegin());
				}
				CalendarWidgetRow row = new CalendarWidgetRow(
						ProjectUIFactory.getDisplayName(getTranslator(), event),
						dueName,
						false,
						getEventColorCss(event));
				forgeDisplayNameLink(row, event, appointmentIdentToAppointment.get(event.getExternalId()));
				nextRows.add(row);
			}
		}
		flc.contextPut("todayRows", todayRows);
		flc.contextPut("nextRows", nextRows);
		
		showAllLink.setI18nKey(translate("calendar.show.all.count", String.valueOf(count)));
		showAllLink.setVisible(count > 0);
	}

	private String getEventColorCss(KalendarEvent event) {
		return StringHelper.containsNonWhitespace(event.getColor())? "o_cal_" + event.getColor(): ProjectUIFactory.COLOR_APPOINTMENT;
	}
	
	private void forgeDisplayNameLink(CalendarWidgetRow row, KalendarEvent event, ProjAppointment appointment) {
		if (secCallback.canEditAppointment(appointment)) {
			FormLink link = uifactory.addFormLink("app_edit_" + counter++, CMD_EDIT, "", null, flc, Link.LINK + Link.NONTRANSLATED);
			link.setI18nKey(StringHelper.escapeHtml(ProjectUIFactory.getDisplayName(getTranslator(), event)));
			link.setUserObject(event);
			link.setUserObject(row);
			row.setDisplayNameItem(link);
			row.setAppointment(appointment);
			row.setKalendarEvent(event);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (appointmentEditCtrl == source) {
			if (event == Event.DONE_EVENT) {
				reload();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (milestoneEditCtrl == source) {
			if (event == Event.DONE_EVENT) {
				reload();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == appointmentEditAllCtr) {
			if (event instanceof CalendarGUIUpdateEvent calEvent) {
				KalendarRecurEvent kalendarEvent = appointmentEditAllCtr.getKalendarEvent();
				org.olat.commons.calendar.ui.events.CalendarGUIUpdateEvent.Cascade cascade = calEvent.getCascade();
				cmc.deactivate();
				cleanUp();
				doEditRecurringAppointment(ureq, kalendarEvent, cascade);
			} else {
				cmc.deactivate();
				cleanUp();
			}
		} else if(cmc == source) {
			reload();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(appointmentEditAllCtr);
		removeAsListenerAndDispose(appointmentEditCtrl);
		removeAsListenerAndDispose(milestoneEditCtrl);
		removeAsListenerAndDispose(cmc);
		appointmentEditAllCtr = null;
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
		} else if (source instanceof FormLink link) {
			if (CMD_EDIT.equals(link.getCmd())) {
				if (link.getUserObject() instanceof CalendarWidgetRow row) {
					doEditAppointment(ureq, row.getAppointment(), row.getKalendarEvent());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doCreateAppointment(UserRequest ureq) {
		if (guardModalController(appointmentEditCtrl)) return;
		
		appointmentEditCtrl = new ProjAppointmentEditController(ureq, getWindowControl(), bcFactory, project,
				Set.of(getIdentity()), false, new Date());
		listenTo(appointmentEditCtrl);
		
		String title = translate("appointment.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), appointmentEditCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doCreateMilestone(UserRequest ureq) {
		if (guardModalController(milestoneEditCtrl)) return;
		
		milestoneEditCtrl = new ProjMilestoneEditController(ureq, getWindowControl(), bcFactory, project);
		listenTo(milestoneEditCtrl);
		
		String title = translate("milestone.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), milestoneEditCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditAppointment(UserRequest ureq, ProjAppointment appointment, KalendarEvent kalendarEvent) {
		if (kalendarEvent instanceof KalendarRecurEvent recurEvent
				&& !StringHelper.containsNonWhitespace(kalendarEvent.getRecurrenceID())) {
			appointmentEditAllCtr = new ConfirmUpdateController(ureq, getWindowControl(), recurEvent);
			listenTo(appointmentEditAllCtr);

			String title = translate("appointment.edit");
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					appointmentEditAllCtr.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		} else {
			doEditAppointment(ureq, appointment.getIdentifier());
		}
	}
	
	private void doEditRecurringAppointment(UserRequest ureq, KalendarRecurEvent kalendarRecurEvent,
		org.olat.commons.calendar.ui.events.CalendarGUIUpdateEvent.Cascade cascade) {
		switch(cascade) {
			case all: {
				doEditAppointment(ureq, kalendarRecurEvent.getExternalId());
				break;
			}
			case once: {
				KalendarEvent occurenceEvent = calendarManager.createKalendarEventRecurringOccurence(kalendarRecurEvent);
				ProjAppointment appointment = projectService.createAppointmentOcurrence(getIdentity(), bcFactory,
						kalendarRecurEvent.getExternalId(), occurenceEvent.getRecurrenceID(), occurenceEvent.getBegin(),
						occurenceEvent.getEnd());
				doEditAppointment(ureq, appointment.getIdentifier());
				break;
			}
		}
	}
	
	private void doEditAppointment(UserRequest ureq, String externalId) {
		if (guardModalController(appointmentEditCtrl)) return;
		
		ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
		searchParams.setIdentifiers(List.of(externalId));
		searchParams.setStatus(List.of(ProjectStatus.active));
		List<ProjAppointmentInfo> appointmentInfos = projectService.getAppointmentInfos(searchParams, ProjArtefactInfoParams.MEMBERS);
		if (appointmentInfos.isEmpty()) {
			return;
		}
		ProjAppointmentInfo appointmentInfo = appointmentInfos.get(0);
		
		appointmentEditCtrl = new ProjAppointmentEditController(ureq, getWindowControl(), bcFactory,
				appointmentInfo.getAppointment(), appointmentInfo.getMembers(), false);
		listenTo(appointmentEditCtrl);

		String title = translate("appointment.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), appointmentEditCtrl.getInitialComponent(),
				true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	public static final class CalendarWidgetRow {
		
		private final String displayName;
		private final String dueName;
		private final boolean warning;
		private final String colorCssClass;
		private KalendarEvent kalendarEvent;
		private ProjAppointment appointment;
		private FormItem displayNameItem;
		
		public CalendarWidgetRow(String displayName, String dueName, boolean warning, String colorCssClass) {
			this.displayName = displayName;
			this.dueName = dueName;
			this.warning = warning;
			this.colorCssClass = colorCssClass;
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getDueName() {
			return dueName;
		}

		public boolean isWarning() {
			return warning;
		}

		public String getColorCssClass() {
			return colorCssClass;
		}

		public KalendarEvent getKalendarEvent() {
			return kalendarEvent;
		}

		public void setKalendarEvent(KalendarEvent kalendarEvent) {
			this.kalendarEvent = kalendarEvent;
		}

		public ProjAppointment getAppointment() {
			return appointment;
		}

		public void setAppointment(ProjAppointment appointment) {
			this.appointment = appointment;
		}

		public String getDisplayNameItemName() {
			return displayNameItem != null? displayNameItem.getComponent().getComponentName(): null;
		}

		public FormItem getDisplayNameItem() {
			return displayNameItem;
		}

		public void setDisplayNameItem(FormItem displayNameItem) {
			this.displayNameItem = displayNameItem;
		}
		
	}

}
