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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarRecurEvent;
import org.olat.commons.calendar.ui.ConfirmUpdateController;
import org.olat.commons.calendar.ui.components.FullCalendarElement;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.events.CalendarGUIMoveEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIResizeEvent;
import org.olat.commons.calendar.ui.events.CalendarGUISelectEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIUpdateEvent;
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
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjAppointmentInfo;
import org.olat.modules.project.ProjAppointmentRef;
import org.olat.modules.project.ProjAppointmentSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjAppointmentDeleteConfirmationController.Cascade;
import org.olat.modules.project.ui.event.DeleteAppointmentEvent;
import org.olat.modules.project.ui.event.EditAppointmentEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Feb 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjCalendarAllController extends FormBasicController implements Activateable2 {
	
	private FormLink appintmentCreateLink;
	private FullCalendarElement calendarEl;
	
	private CloseableModalController cmc;
	private CloseableCalloutWindowController calloutCtr;
	private ProjAppointmentEditController appointmentEditCtrl;
	private ProjAppointmentPreviewController appointmentPreviewCtrl;
	private ConfirmUpdateController appointmentChangeAllCtr;
	private ProjAppointmentDeleteConfirmationController deleteConfirmationCtrl;
	
	private final ProjProject project;
	private final ProjProjectSecurityCallback secCallback;
	private String appointmentReadWriteKalendarId;
	private String appointmentReadOnlyKalendarId;
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private CalendarManager calendarManager;
	
	
	protected ProjCalendarAllController(UserRequest ureq, WindowControl wControl, ProjProject project,
			ProjProjectSecurityCallback secCallback) {
		super(ureq, wControl, "calendar_all");
		setTranslator(Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator()));
		this.project = project;
		this.secCallback = secCallback;
		
		initForm(ureq);
		loadModel();
	}
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		appintmentCreateLink = uifactory.addFormLink("appointment.create", formLayout, Link.BUTTON);
		appintmentCreateLink.setIconLeftCSS("o_icon o_icon_add");
		appintmentCreateLink.setVisible(secCallback.canCreateAppointments());
		
		calendarEl = new FullCalendarElement(ureq, "calendar", List.of(), getTranslator());
		formLayout.add("calendar", calendarEl);
	}

	private void loadModel() {
		ProjAppointmentSearchParams appointmentSearchParams = new ProjAppointmentSearchParams();
		appointmentSearchParams.setProject(project);
		appointmentSearchParams.setStatus(List.of(ProjectStatus.active));
		List<ProjAppointmentInfo> appointmentInfos = projectService.getAppointmentInfos(appointmentSearchParams);
		List<ProjAppointment> appointmentReadWrite = new ArrayList<>();
		List<ProjAppointment> appointmentReadOnly = new ArrayList<>();
		for (ProjAppointmentInfo appointmentInfo : appointmentInfos) {
			ProjAppointment appointment = appointmentInfo.getAppointment();
			boolean participant = appointmentInfo.getMembers().contains(getIdentity());
			if (secCallback.canEditAppointment(appointment, participant)) {
				appointmentReadWrite.add(appointment);
			} else {
				appointmentReadOnly.add(appointment);
			}
		}
		
		Kalendar appointmentReadWriteKalendar = projectService.toKalendar(appointmentReadWrite);
		appointmentReadWriteKalendarId = appointmentReadWriteKalendar.getCalendarID();
		KalendarRenderWrapper appointmentReadWriteWrapper = new KalendarRenderWrapper(appointmentReadWriteKalendar,
				translate("appointment.calendar.name"), "project.appointments.rw" + project.getKey());
		appointmentReadWriteWrapper.setPrivateEventsVisible(true);
		appointmentReadWriteWrapper.setCssClass(ProjectUIFactory.COLOR_APPOINTMENT);
		appointmentReadWriteWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_WRITE);
		
		Kalendar appointmentReadOnlyKalendar = projectService.toKalendar(appointmentReadOnly);
		appointmentReadOnlyKalendarId = appointmentReadOnlyKalendar.getCalendarID();
		KalendarRenderWrapper appointmentReadOnlyWrapper = new KalendarRenderWrapper(appointmentReadOnlyKalendar,
				translate("appointment.calendar.name"), "project.appointments.ro" + project.getKey());
		appointmentReadOnlyWrapper.setPrivateEventsVisible(true);
		appointmentReadOnlyWrapper.setCssClass(ProjectUIFactory.COLOR_APPOINTMENT);
		appointmentReadOnlyWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
		
		calendarEl.setCalendars(List.of(appointmentReadWriteWrapper, appointmentReadOnlyWrapper));
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries != null && !entries.isEmpty()) {
			ContextEntry entry = entries.get(0);
			String type = entry.getOLATResourceable().getResourceableTypeName();
			if (ProjectBCFactory.TYPE_APPOINTMENT.equals(type)) {
				Long key = entry.getOLATResourceable().getResourceableId();
				KalendarRenderWrapper calendar = calendarEl.getCalendar(appointmentReadWriteKalendarId);
				if (calendar != null) {
					String calendarId = key.toString();
					Optional<KalendarEvent> event = calendar.getKalendar().getEvents().stream().filter(e -> e.getID().equals(calendarId)).findFirst();
					if (event.isPresent()) {
						calendarEl.setFocusDate(event.get().getBegin());
						return;
					}
				}
				calendar = calendarEl.getCalendar(appointmentReadOnlyKalendarId);
				if (calendar != null) {
					String calendarId = key.toString();
					Optional<KalendarEvent> event = calendar.getKalendar().getEvents().stream().filter(e -> e.getID().equals(calendarId)).findFirst();
					if (event.isPresent()) {
						calendarEl.setFocusDate(event.get().getBegin());
						return;
					}
				}
			}
		} 
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (appointmentEditCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadModel();
			} else if (event == Event.CANCELLED_EVENT && appointmentEditCtrl.isFirstEdit()) {
				projectService.deleteAppointmentPermanent(appointmentEditCtrl.getAppointment());
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == appointmentPreviewCtrl) {
			if (event instanceof EditAppointmentEvent aeEvant) {
				calloutCtr.deactivate();
				cleanUp();
				
				doEditAppointment(ureq, aeEvant.getAppointment());
			} else if (event instanceof DeleteAppointmentEvent daEvent) {
				calloutCtr.deactivate();
				cleanUp();
				
				doConfirmDeleteAppointment(ureq, daEvent.getAppointment(), daEvent.getKalendarEvent());
			} else if (event == Event.DONE_EVENT) {
				calloutCtr.deactivate();
				cleanUp();
			}
		} else if (source == appointmentChangeAllCtr) {
			if (event instanceof CalendarGUIUpdateEvent) {
				doMoveRecurringAppointment((CalendarGUIUpdateEvent) event, appointmentChangeAllCtr.getKalendarEvent(),
						appointmentChangeAllCtr.getDayDelta(), appointmentChangeAllCtr.getMinuteDelta(),
						appointmentChangeAllCtr.getChangeBegin());
			}
			cmc.deactivate();
			cleanUp();
		} else if (deleteConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				Object userObject = deleteConfirmationCtrl.getUserObject();
				if (userObject instanceof KalendarEvent kalendarEvent)
				doDeleteAppointment(kalendarEvent, deleteConfirmationCtrl.getCascade());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			loadModel();
			cleanUp();
		} else if(calloutCtr == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(appointmentChangeAllCtr);
		removeAsListenerAndDispose(deleteConfirmationCtrl);
		removeAsListenerAndDispose(appointmentPreviewCtrl);
		removeAsListenerAndDispose(appointmentEditCtrl);
		removeAsListenerAndDispose(calloutCtr);
		removeAsListenerAndDispose(cmc);
		appointmentChangeAllCtr = null;
		deleteConfirmationCtrl = null;
		appointmentPreviewCtrl = null;
		appointmentEditCtrl = null;
		calloutCtr = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == appintmentCreateLink) {
			doCreateAppointment(ureq);
		} else if (source == calendarEl) {
			if (event instanceof CalendarGUISelectEvent) {
				CalendarGUISelectEvent selectEvent = (CalendarGUISelectEvent)event;
				if (selectEvent.getKalendarEvent() != null) {
					if (appointmentReadWriteKalendarId.equals(selectEvent.getKalendarRenderWrapper().getKalendar().getCalendarID())) {
						doOpenAppointmentCallout(ureq, selectEvent.getKalendarEvent(), selectEvent.getTargetDomId());
					}
				}
			} else if (event instanceof CalendarGUIMoveEvent) {
				CalendarGUIMoveEvent moveEvent = (CalendarGUIMoveEvent)event;
				if (appointmentReadWriteKalendarId.equals(moveEvent.getKalendarRenderWrapper().getKalendar().getCalendarID())) {
					doMoveAppointment(ureq, moveEvent.getKalendarEvent(), moveEvent.getDayDelta(),
							moveEvent.getMinuteDelta(), moveEvent.getAllDay(), true);
				}
			} else if (event instanceof CalendarGUIResizeEvent) {
				CalendarGUIResizeEvent resizeEvent = (CalendarGUIResizeEvent)event;
				if (appointmentReadWriteKalendarId.equals(resizeEvent.getKalendarRenderWrapper().getKalendar().getCalendarID())) {
					doMoveAppointment(ureq, resizeEvent.getKalendarEvent(), 0l, resizeEvent.getMinuteDelta(),
							resizeEvent.getAllDay(), false);
					
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
		
		ProjAppointment appointment = projectService.createAppointment(getIdentity(), project);
		appointmentEditCtrl = new ProjAppointmentEditController(ureq, getWindowControl(), appointment, Set.of(getIdentity()), true, false);
		listenTo(appointmentEditCtrl);
		
		String title = translate("appointment.edit");
		cmc = new CloseableModalController(getWindowControl(), "close", appointmentEditCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditAppointment(UserRequest ureq, ProjAppointmentRef appointment) {
		if (guardModalController(appointmentEditCtrl)) return;
		
		ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
		searchParams.setAppointments(List.of(appointment));
		List<ProjAppointmentInfo> appointmentInfos = projectService.getAppointmentInfos(searchParams);
		if (appointmentInfos.isEmpty()) {
			return;
		}
		ProjAppointmentInfo appointmentInfo = appointmentInfos.get(0);
		
		appointmentEditCtrl = new ProjAppointmentEditController(ureq, getWindowControl(),
				appointmentInfo.getAppointment(), appointmentInfo.getMembers(), false, false);
		listenTo(appointmentEditCtrl);
		
		String title = translate("appointment.edit");
		cmc = new CloseableModalController(getWindowControl(), "close", appointmentEditCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
		
	}
	
	private void doOpenAppointmentCallout(UserRequest ureq, KalendarEvent kalendarEvent, String targetDomId) {
		if(calloutCtr != null && appointmentEditCtrl != null) return;
		
		removeAsListenerAndDispose(calloutCtr);
		removeAsListenerAndDispose(appointmentEditCtrl);
		
		ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
		searchParams.setIdentifiers(List.of(kalendarEvent.getExternalId()));
		List<ProjAppointmentInfo> appointmentInfos = projectService.getAppointmentInfos(searchParams);
		if (appointmentInfos.isEmpty()) {
			return;
		}
		ProjAppointmentInfo appointmentInfo = appointmentInfos.get(0);
		
		// Start and end date from the calendar event because of recurring events.
		appointmentPreviewCtrl = new ProjAppointmentPreviewController(ureq, getWindowControl(), secCallback,
				appointmentInfo.getAppointment(), appointmentInfo.getMembers(), kalendarEvent);
		listenTo(appointmentPreviewCtrl);
		
		calloutCtr = new CloseableCalloutWindowController(ureq, getWindowControl(),
				appointmentPreviewCtrl.getInitialComponent(), targetDomId, null, true, "");
		listenTo(calloutCtr);
		calloutCtr.activate();
	}
	
	private void doMoveAppointment(UserRequest ureq, KalendarEvent kalendarEvent, Long days, Long minutes,
			Boolean allDay, boolean changeStartDate) {
		if (kalendarEvent instanceof KalendarRecurEvent recurEvent
				&& !StringHelper.containsNonWhitespace(kalendarEvent.getRecurrenceID())) {
			appointmentChangeAllCtr = new ConfirmUpdateController(ureq, getWindowControl(), recurEvent, days, minutes,
					allDay, changeStartDate);
			listenTo(appointmentChangeAllCtr);

			String title = translate("cal.edit.update");
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					appointmentChangeAllCtr.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		} else if (kalendarEvent != null) {
			projectService.moveAppointment(getIdentity(), kalendarEvent.getExternalId(), days, minutes, changeStartDate);
			loadModel();
		} else {
			loadModel();
		}
	}
	
	private void doMoveRecurringAppointment(CalendarGUIUpdateEvent calEvent, KalendarEvent kalendarEvent, Long days,
			Long minutes, boolean moveStartDate) {
		switch(calEvent.getCascade()) {
			case all: {
				projectService.moveAppointment(getIdentity(), kalendarEvent.getExternalId(), days, minutes, moveStartDate);
				break;
			}
			case once: {
				if (kalendarEvent instanceof KalendarRecurEvent recurEvent) {
					KalendarEvent occurenceEvent = calendarManager.createKalendarEventRecurringOccurence(recurEvent);
					projectService.createMovedAppointmentOcurrence(getIdentity(), kalendarEvent.getExternalId(),
							occurenceEvent.getRecurrenceID(), occurenceEvent.getBegin(), occurenceEvent.getEnd(), days,
							minutes, moveStartDate);
				}
				break;
			}
		}
		loadModel();
	}
	
	private void doConfirmDeleteAppointment(UserRequest ureq, ProjAppointmentRef appointmentRef, KalendarEvent kalendarEvent) {
		if (guardModalController(deleteConfirmationCtrl)) return;
		
		ProjAppointment appointment = projectService.getAppointment(appointmentRef);
		if (appointment == null || ProjectStatus.deleted == appointment.getArtefact().getStatus()) {
			return;
		}
		
		String message = translate("appointment.delete.confirmation.message", appointment.getSubject());
		boolean recurring = StringHelper.containsNonWhitespace(appointment.getRecurrenceRule());
		deleteConfirmationCtrl = new ProjAppointmentDeleteConfirmationController(ureq, getWindowControl(), message, recurring);
		deleteConfirmationCtrl.setUserObject(kalendarEvent);
		listenTo(deleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", deleteConfirmationCtrl.getInitialComponent(),
				true, translate("appointment.delete"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDeleteAppointment(KalendarEvent kalendarEvent, Cascade cascade) {
		switch(cascade) {
			case all: 
				projectService.deleteAppointmentSoftly(getIdentity(), kalendarEvent.getExternalId(), kalendarEvent.getOccurenceDate());
				break;
			case single: {
				projectService.addAppointmentExclusion(getIdentity(), kalendarEvent.getExternalId(), kalendarEvent.getBegin(), true);
				break;
			}
			case future: {
				projectService.addAppointmentExclusion(getIdentity(), kalendarEvent.getExternalId(), kalendarEvent.getBegin(), false);
				break;
			}
		}
		loadModel();
	}

}
