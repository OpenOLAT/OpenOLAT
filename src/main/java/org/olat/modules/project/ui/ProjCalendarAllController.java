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
import java.util.Date;
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
import org.olat.commons.calendar.ui.events.CalendarGUIAddEvent;
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
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjAppointmentInfo;
import org.olat.modules.project.ProjAppointmentRef;
import org.olat.modules.project.ProjAppointmentSearchParams;
import org.olat.modules.project.ProjArtefactInfoParams;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjMilestoneInfo;
import org.olat.modules.project.ProjMilestoneRef;
import org.olat.modules.project.ProjMilestoneSearchParams;
import org.olat.modules.project.ProjMilestoneStatus;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectImageType;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjAppointmentDeleteConfirmationController.Cascade;
import org.olat.modules.project.ui.component.ProjAvatarComponent;
import org.olat.modules.project.ui.component.ProjAvatarComponent.Size;
import org.olat.modules.project.ui.event.AppointmentDeleteEvent;
import org.olat.modules.project.ui.event.AppointmentEditEvent;
import org.olat.modules.project.ui.event.MilestoneDeleteEvent;
import org.olat.modules.project.ui.event.MilestoneEditEvent;
import org.olat.modules.project.ui.event.MilestoneStatusEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Feb 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjCalendarAllController extends FormBasicController implements Activateable2 {
	
	private FormLink appointmentCreateLink;
	private FormLink milestoneCreateLink;
	private FullCalendarElement calendarEl;
	
	private CloseableModalController cmc;
	private CloseableCalloutWindowController calloutCtr;
	private ProjAppointmentEditController appointmentEditCtrl;
	private ProjAppointmentPreviewController appointmentPreviewCtrl;
	private ConfirmUpdateController appointmentEditAllCtr;
	private ConfirmUpdateController appointmentMoveAllCtr;
	private ProjAppointmentDeleteConfirmationController appointmentDeleteConfirmationCtrl;
	private ProjMilestoneEditController milestoneEditCtrl;
	private ProjMilestonePreviewController milestonePreviewCtrl;
	private ProjConfirmationController milestoneDeleteConfirmationCtrl;
	
	private final ProjProject project;
	private final ProjProjectSecurityCallback secCallback;
	private final String avatarUrl;
	private String appointmentReadWriteKalendarId;
	private String appointmentReadOnlyKalendarId;
	private String milestoneKalendarId;
	
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
		ProjProjectImageMapper projectImageMapper = new ProjProjectImageMapper(projectService);
		String projectMapperUrl = registerCacheableMapper(ureq, ProjProjectImageMapper.DEFAULT_ID, projectImageMapper,
				ProjProjectImageMapper.DEFAULT_EXPIRATION_TIME);
		this.avatarUrl = projectImageMapper.getImageUrl(projectMapperUrl, project, ProjProjectImageType.avatar);
		
		initForm(ureq);
		loadModel();
	}
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add("avatar", new ComponentWrapperElement(new ProjAvatarComponent("avatar", project, avatarUrl, Size.medium, false)));
		
		appointmentCreateLink = uifactory.addFormLink("appointment.create", formLayout, Link.BUTTON);
		appointmentCreateLink.setIconLeftCSS("o_icon o_icon_add");
		appointmentCreateLink.setVisible(secCallback.canCreateAppointments());
		
		milestoneCreateLink = uifactory.addFormLink("milestone.create", formLayout, Link.BUTTON);
		milestoneCreateLink.setIconLeftCSS("o_icon o_icon_add");
		milestoneCreateLink.setVisible(secCallback.canCreateMilestones());
		
		calendarEl = new FullCalendarElement(ureq, "calendar", List.of(), getTranslator());
		formLayout.add("calendar", calendarEl);
	}

	private void loadModel() {
		List<KalendarRenderWrapper> calendarWrappers = new ArrayList<>(3);
		
		// Appointments
		if (secCallback.canViewAppointments()) {
			ProjAppointmentSearchParams appointmentSearchParams = new ProjAppointmentSearchParams();
			appointmentSearchParams.setProject(project);
			appointmentSearchParams.setStatus(List.of(ProjectStatus.active));
			List<ProjAppointmentInfo> appointmentInfos = projectService.getAppointmentInfos(appointmentSearchParams, ProjArtefactInfoParams.MEMBERS);
			List<ProjAppointment> appointmentReadWrite = new ArrayList<>();
			List<ProjAppointment> appointmentReadOnly = new ArrayList<>();
			for (ProjAppointmentInfo appointmentInfo : appointmentInfos) {
				ProjAppointment appointment = appointmentInfo.getAppointment();
				if (secCallback.canEditAppointment(appointment)) {
					appointmentReadWrite.add(appointment);
				} else {
					appointmentReadOnly.add(appointment);
				}
			}
			
			Kalendar appointmentReadWriteKalendar = projectService.getAppointmentsKalendar(appointmentReadWrite);
			appointmentReadWriteKalendarId = appointmentReadWriteKalendar.getCalendarID();
			KalendarRenderWrapper appointmentReadWriteWrapper = new KalendarRenderWrapper(appointmentReadWriteKalendar,
					translate("appointment.calendar.name"), "project.appointments.rw" + project.getKey());
			appointmentReadWriteWrapper.setPrivateEventsVisible(true);
			appointmentReadWriteWrapper.setCssClass(ProjectUIFactory.COLOR_APPOINTMENT);
			appointmentReadWriteWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_WRITE);
			calendarWrappers.add(appointmentReadWriteWrapper);
			
			Kalendar appointmentReadOnlyKalendar = projectService.getAppointmentsKalendar(appointmentReadOnly);
			appointmentReadOnlyKalendarId = appointmentReadOnlyKalendar.getCalendarID();
			KalendarRenderWrapper appointmentReadOnlyWrapper = new KalendarRenderWrapper(appointmentReadOnlyKalendar,
					translate("appointment.calendar.name"), "project.appointments.ro" + project.getKey());
			appointmentReadOnlyWrapper.setPrivateEventsVisible(true);
			appointmentReadOnlyWrapper.setCssClass(ProjectUIFactory.COLOR_APPOINTMENT);
			appointmentReadOnlyWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
			calendarWrappers.add(appointmentReadOnlyWrapper);
		}
		
		// Milestones
		if (secCallback.canViewMilestones()) {
			ProjMilestoneSearchParams milestoneSearchParams = new ProjMilestoneSearchParams();
			milestoneSearchParams.setProject(project);
			milestoneSearchParams.setStatus(List.of(ProjectStatus.active));
			List<ProjMilestone> milestones = projectService.getMilestones(milestoneSearchParams);
			
			Kalendar milestoneKalendar = projectService.getMilestonesKalendar(milestones);
			milestoneKalendarId = milestoneKalendar.getCalendarID();
			KalendarRenderWrapper milestoneWrapper = new KalendarRenderWrapper(milestoneKalendar,
					translate("milestone.calendar.name"), "project.milestones." + project.getKey());
			milestoneWrapper.setPrivateEventsVisible(true);
			milestoneWrapper.setCssClass(ProjectUIFactory.COLOR_MILESTONE);
			int milestonesAccess = secCallback.canEditMilestones()
					? KalendarRenderWrapper.ACCESS_READ_WRITE
							: KalendarRenderWrapper.ACCESS_READ_ONLY;
			milestoneWrapper.setAccess(milestonesAccess);
			calendarWrappers.add(milestoneWrapper);
		}
		
		
		calendarEl.setCalendars(calendarWrappers);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries != null && !entries.isEmpty()) {
			ContextEntry entry = entries.get(0);
			String type = entry.getOLATResourceable().getResourceableTypeName();
			if (ProjectBCFactory.TYPE_APPOINTMENT.equals(type)) {
				if (secCallback.canViewAppointments()) {
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
			} else if (ProjectBCFactory.TYPE_MILESTONE.equals(type)) {
				if (secCallback.canViewMilestones()) {
					Long key = entry.getOLATResourceable().getResourceableId();
					KalendarRenderWrapper calendar = calendarEl.getCalendar(milestoneKalendarId);
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
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (appointmentEditCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == appointmentPreviewCtrl) {
			if (event instanceof AppointmentEditEvent aEvent) {
				calloutCtr.deactivate();
				cleanUp();
				
				doEditAppointment(ureq, aEvent.getKalendarEvent());
			} else if (event instanceof AppointmentDeleteEvent aEvent) {
				calloutCtr.deactivate();
				cleanUp();
				
				doConfirmDeleteAppointment(ureq, aEvent.getAppointment(), aEvent.getKalendarEvent());
			} else if (event == Event.DONE_EVENT) {
				calloutCtr.deactivate();
				cleanUp();
			}
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
		} else if (source == appointmentMoveAllCtr) {
			if (event instanceof CalendarGUIUpdateEvent calEvent) {
				doMoveRecurringAppointment(appointmentMoveAllCtr.getKalendarEvent(), calEvent.getCascade(),
						appointmentMoveAllCtr.getDayDelta(), appointmentMoveAllCtr.getMinuteDelta(),
						appointmentMoveAllCtr.getChangeBegin());
			}
			cmc.deactivate();
			cleanUp();
		} else if (appointmentDeleteConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				Object userObject = appointmentDeleteConfirmationCtrl.getUserObject();
				if (userObject instanceof KalendarEvent kalendarEvent)
				doDeleteAppointment(kalendarEvent, appointmentDeleteConfirmationCtrl.getCascade());
			}
			cmc.deactivate();
			cleanUp();
		} else if (milestoneEditCtrl == source) {
			if (event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == milestonePreviewCtrl) {
			if (event instanceof MilestoneEditEvent mEvent) {
				calloutCtr.deactivate();
				cleanUp();
				
				doEditMilestone(ureq, mEvent.getMilestone());
			} else if (event instanceof MilestoneStatusEvent mEvent) {
				calloutCtr.deactivate();
				cleanUp();
				
				doAcomplishMilestone(mEvent.getMilestone());
			} else if (event instanceof MilestoneDeleteEvent eEvent) {
				calloutCtr.deactivate();
				cleanUp();
				
				doConfirmDeleteMilestone(ureq, eEvent.getMilestone());
			} else if (event == Event.DONE_EVENT) {
				calloutCtr.deactivate();
				cleanUp();
			}
		} else if (milestoneDeleteConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				Object userObject = milestoneDeleteConfirmationCtrl.getUserObject();
				if (userObject instanceof ProjMilestoneRef milestone)
				doDeleteMilestone(milestone);
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
		removeAsListenerAndDispose(appointmentDeleteConfirmationCtrl);
		removeAsListenerAndDispose(appointmentPreviewCtrl);
		removeAsListenerAndDispose(appointmentEditAllCtr);
		removeAsListenerAndDispose(appointmentMoveAllCtr);
		removeAsListenerAndDispose(appointmentEditCtrl);
		removeAsListenerAndDispose(milestoneDeleteConfirmationCtrl);
		removeAsListenerAndDispose(milestonePreviewCtrl);
		removeAsListenerAndDispose(milestoneEditCtrl);
		removeAsListenerAndDispose(calloutCtr);
		removeAsListenerAndDispose(cmc);
		appointmentDeleteConfirmationCtrl = null;
		appointmentPreviewCtrl = null;
		appointmentEditAllCtr = null;
		appointmentMoveAllCtr = null;
		appointmentEditCtrl = null;
		milestoneDeleteConfirmationCtrl = null;
		milestonePreviewCtrl = null;
		milestoneEditCtrl = null;
		calloutCtr = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == appointmentCreateLink) {
			doCreateAppointment(ureq, new Date());
		} else if (source == milestoneCreateLink) {
			doCreateMilestone(ureq);
		} else if (source == calendarEl) {
			if (event instanceof CalendarGUIAddEvent caEvent) {
				doCreateAppointment(ureq, DateUtils.copyTime(caEvent.getStartDate(), new Date()));
			} else if (event instanceof CalendarGUISelectEvent) {
				CalendarGUISelectEvent selectEvent = (CalendarGUISelectEvent)event;
				if (selectEvent.getKalendarEvent() != null) {
					doOpenPreviewCallout(ureq, selectEvent.getKalendarEvent(), selectEvent.getTargetDomId());
				}
			} else if (event instanceof CalendarGUIMoveEvent) {
				CalendarGUIMoveEvent moveEvent = (CalendarGUIMoveEvent)event;
				doMove(ureq, moveEvent.getKalendarEvent(), moveEvent.getDayDelta(),
							moveEvent.getMinuteDelta(), moveEvent.getAllDay(), true);
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
	
	private void doOpenPreviewCallout(UserRequest ureq, KalendarEvent kalendarEvent, String targetDomId) {
		if (kalendarEvent.getCalendar() != null) {
			if (appointmentReadWriteKalendarId.equals(kalendarEvent.getCalendar().getCalendarID())) {
				doOpenPreviewAppointmentCallout(ureq, kalendarEvent, targetDomId);
			} else if (milestoneKalendarId.equals(kalendarEvent.getCalendar().getCalendarID())) {
				doOpenPreviewMilestoneCallout(ureq, kalendarEvent, targetDomId);
			}
		}
	}
	
	private void doOpenPreviewAppointmentCallout(UserRequest ureq, KalendarEvent kalendarEvent, String targetDomId) {
		if (calloutCtr != null && appointmentEditCtrl != null) return;
		
		removeAsListenerAndDispose(calloutCtr);
		removeAsListenerAndDispose(appointmentEditCtrl);
		
		ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
		searchParams.setIdentifiers(List.of(kalendarEvent.getExternalId()));
		searchParams.setStatus(List.of(ProjectStatus.active));
		List<ProjAppointmentInfo> appointmentInfos = projectService.getAppointmentInfos(searchParams,
				ProjArtefactInfoParams.of(true, false, false));
		if (appointmentInfos.isEmpty()) {
			return;
		}
		ProjAppointmentInfo appointmentInfo = appointmentInfos.get(0);
		
		appointmentPreviewCtrl = new ProjAppointmentPreviewController(ureq, getWindowControl(), secCallback,
				appointmentInfo, kalendarEvent);
		listenTo(appointmentPreviewCtrl);
		
		calloutCtr = new CloseableCalloutWindowController(ureq, getWindowControl(),
				appointmentPreviewCtrl.getInitialComponent(), targetDomId, null, true, "");
		listenTo(calloutCtr);
		calloutCtr.activate();
	}
	
	private void doOpenPreviewMilestoneCallout(UserRequest ureq, KalendarEvent kalendarEvent, String targetDomId) {
		if (calloutCtr != null && milestoneEditCtrl != null) return;
		
		removeAsListenerAndDispose(calloutCtr);
		removeAsListenerAndDispose(milestoneEditCtrl);
		
		ProjMilestoneSearchParams searchParams = new ProjMilestoneSearchParams();
		searchParams.setIdentifiers(List.of(kalendarEvent.getExternalId()));
		searchParams.setStatus(List.of(ProjectStatus.active));
		List<ProjMilestoneInfo> milestonesInfos = projectService.getMilestoneInfos(searchParams, ProjArtefactInfoParams.TAGS);
		if (milestonesInfos.isEmpty()) {
			return;
		}
		ProjMilestoneInfo milestoneInfo = milestonesInfos.get(0);
		
		milestonePreviewCtrl = new ProjMilestonePreviewController(ureq, getWindowControl(), secCallback,
				milestoneInfo);
		listenTo(milestonePreviewCtrl);
		
		calloutCtr = new CloseableCalloutWindowController(ureq, getWindowControl(),
				milestonePreviewCtrl.getInitialComponent(), targetDomId, null, true, "");
		listenTo(calloutCtr);
		calloutCtr.activate();
	}

	private void doCreateAppointment(UserRequest ureq, Date initialStartDate) {
		if (guardModalController(appointmentEditCtrl)) return;
		
		appointmentEditCtrl = new ProjAppointmentEditController(ureq, getWindowControl(), project, Set.of(getIdentity()), false, initialStartDate);
		listenTo(appointmentEditCtrl);
		
		String title = translate("appointment.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), appointmentEditCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doCreateMilestone(UserRequest ureq) {
		if (guardModalController(milestoneEditCtrl)) return;
		
		milestoneEditCtrl = new ProjMilestoneEditController(ureq, getWindowControl(), project);
		listenTo(milestoneEditCtrl);
		
		String title = translate("milestone.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), milestoneEditCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditAppointment(UserRequest ureq, KalendarEvent kalendarEvent) {
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
			doEditAppointment(ureq, kalendarEvent.getExternalId());
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
				ProjAppointment appointment = projectService.createAppointmentOcurrence(getIdentity(), kalendarRecurEvent.getExternalId(),
						occurenceEvent.getRecurrenceID(), occurenceEvent.getBegin(), occurenceEvent.getEnd());
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
		
		appointmentEditCtrl = new ProjAppointmentEditController(ureq, getWindowControl(), appointmentInfo.getAppointment(),
				appointmentInfo.getMembers(), false);
		listenTo(appointmentEditCtrl);

		String title = translate("appointment.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), appointmentEditCtrl.getInitialComponent(),
				true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doMove(UserRequest ureq, KalendarEvent kalendarEvent, Long days, Long minutes, Boolean allDay,
			boolean changeStartDate) {
		if (kalendarEvent.getCalendar() != null) {
			if (appointmentReadWriteKalendarId.equals(kalendarEvent.getCalendar().getCalendarID())) {
				doMoveAppointment(ureq, kalendarEvent, days, minutes, allDay, changeStartDate);
			} else if (milestoneKalendarId.equals(kalendarEvent.getCalendar().getCalendarID())) {
				doMoveMilestone(kalendarEvent, days);
			}
		}
	}
	
	private void doMoveAppointment(UserRequest ureq, KalendarEvent kalendarEvent, Long days, Long minutes,
			Boolean allDay, boolean changeStartDate) {
		if (kalendarEvent instanceof KalendarRecurEvent recurEvent
				&& !StringHelper.containsNonWhitespace(kalendarEvent.getRecurrenceID())) {
			appointmentMoveAllCtr = new ConfirmUpdateController(ureq, getWindowControl(), recurEvent, days, minutes,
					allDay, changeStartDate);
			listenTo(appointmentMoveAllCtr);

			String title = translate("cal.edit.update");
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					appointmentMoveAllCtr.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		} else if (kalendarEvent != null) {
			projectService.moveAppointment(getIdentity(), kalendarEvent.getExternalId(), days, minutes, changeStartDate);
			loadModel();
		} else {
			loadModel();
		}
	}
	
	private void doMoveRecurringAppointment(KalendarRecurEvent kalendarRecurEvent,
			org.olat.commons.calendar.ui.events.CalendarGUIUpdateEvent.Cascade cascade, Long days, Long minutes,
			boolean moveStartDate) {
		switch(cascade) {
			case all: {
				projectService.moveAppointment(getIdentity(), kalendarRecurEvent.getExternalId(), days, minutes, moveStartDate);
				break;
			}
			case once: {
				KalendarEvent occurenceEvent = calendarManager.createKalendarEventRecurringOccurence(kalendarRecurEvent);
				projectService.createMovedAppointmentOcurrence(getIdentity(), kalendarRecurEvent.getExternalId(),
						occurenceEvent.getRecurrenceID(), occurenceEvent.getBegin(), occurenceEvent.getEnd(), days,
						minutes, moveStartDate);
				break;
			}
		}
		loadModel();
	}
	
	private void doEditMilestone(UserRequest ureq, ProjMilestoneRef milestone) {
		if(guardModalController(milestoneEditCtrl)) return;
		
		ProjMilestoneSearchParams searchParams = new ProjMilestoneSearchParams();
		searchParams.setMilestones(List.of(milestone));
		searchParams.setStatus(List.of(ProjectStatus.active));
		List<ProjMilestone> milestones = projectService.getMilestones(searchParams);
		if (milestones.isEmpty()) {
			return;
		}
		
		milestoneEditCtrl = new ProjMilestoneEditController(ureq, getWindowControl(), milestones.get(0));
		listenTo(milestoneEditCtrl);

		String title = translate("milestone.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				milestoneEditCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAcomplishMilestone(ProjMilestoneRef milestone) {
		projectService.updateMilestoneStatus(getIdentity(), milestone, ProjMilestoneStatus.achieved);
	}
	
	private void doMoveMilestone(KalendarEvent kalendarEvent, Long days) {
		projectService.moveMilestone(getIdentity(), kalendarEvent.getExternalId(), days);
		loadModel();
	}
	
	private void doConfirmDeleteAppointment(UserRequest ureq, ProjAppointmentRef appointmentRef, KalendarEvent kalendarEvent) {
		if (guardModalController(appointmentDeleteConfirmationCtrl)) return;
		
		ProjAppointment appointment = projectService.getAppointment(appointmentRef);
		if (appointment == null || ProjectStatus.deleted == appointment.getArtefact().getStatus()) {
			return;
		}
		
		String message = translate("appointment.delete.confirmation.message", ProjectUIFactory.getDisplayName(getTranslator(), appointment));
		boolean recurring = StringHelper.containsNonWhitespace(appointment.getRecurrenceRule());
		appointmentDeleteConfirmationCtrl = new ProjAppointmentDeleteConfirmationController(ureq, getWindowControl(), message, recurring);
		appointmentDeleteConfirmationCtrl.setUserObject(kalendarEvent);
		listenTo(appointmentDeleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), appointmentDeleteConfirmationCtrl.getInitialComponent(),
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
	
	private void doConfirmDeleteMilestone(UserRequest ureq, ProjMilestoneRef milestoneRef) {
		if (guardModalController(milestoneDeleteConfirmationCtrl)) return;
		
		ProjMilestone milestone = projectService.getMilestone(milestoneRef);
		if (milestone == null || ProjectStatus.deleted == milestone.getArtefact().getStatus()) {
			return;
		}
		
		String message = translate("milestone.delete.confirmation.message", ProjectUIFactory.getDisplayName(getTranslator(), milestone));
		milestoneDeleteConfirmationCtrl = new ProjConfirmationController(ureq, getWindowControl(), message,
				"milestone.delete.confirmation.confirm", "milestone.delete.confirmation.button", true);
		milestoneDeleteConfirmationCtrl.setUserObject(milestone);
		listenTo(milestoneDeleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), milestoneDeleteConfirmationCtrl.getInitialComponent(),
				true, translate("milestone.delete"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDeleteMilestone(ProjMilestoneRef milestone) {
		projectService.deleteMilestoneSoftly(getIdentity(), milestone);
		loadModel();
	}
	
}
