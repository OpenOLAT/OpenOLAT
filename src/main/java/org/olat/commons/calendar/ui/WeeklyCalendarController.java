/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.commons.calendar.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.ImportCalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarComparator;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.notification.CalendarNotificationManager;
import org.olat.commons.calendar.ui.components.FullCalendarElement;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.events.KalendarGUIAddEvent;
import org.olat.commons.calendar.ui.events.KalendarGUIEditEvent;
import org.olat.commons.calendar.ui.events.KalendarGUIImportEvent;
import org.olat.commons.calendar.ui.events.KalendarGUIMoveEvent;
import org.olat.commons.calendar.ui.events.KalendarGUIPrintEvent;
import org.olat.commons.calendar.ui.events.KalendarGUISelectEvent;
import org.olat.commons.calendar.ui.events.KalendarModifiedEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.ComponentUtil;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.util.logging.activity.LoggingResourceable;

public class WeeklyCalendarController extends FormBasicController implements Activateable2, CalendarController, GenericEventListener {

	public static final String CALLER_HOME = "home";
	public static final String CALLER_PROFILE = "profile";
	public static final String CALLER_COLLAB = "collab";
	public static final String CALLER_COURSE = "course";
	
	private List<KalendarRenderWrapper> calendarWrappers;
	private List<KalendarRenderWrapper> importedCalendarWrappers;
	private FullCalendarElement weeklyCalendar;
	private CalendarConfigurationController calendarConfig;
	private ImportedCalendarConfigurationController importedCalendarConfig;
	private CalendarEntryDetailsController editController;
	private ImportCalendarController importCalendarController;
	private String caller;
	private boolean dirty = false;

	private CloseableModalController cmc;
	private SubscriptionContext subsContext;
	private ContextualSubscriptionController csc;
	
	private CalendarPrintController printCtrl;
	private CalendarDetailsController eventDetailsCtr;
	private CloseableCalloutWindowController eventCalloutCtr;
	
	/**
	 * three options:
	 * 1. edit sequence
	 * 2. delete single date
	 * 3. delete whole sequence
	 */
	private DialogBoxController dbcSequence;
	private DialogBoxController deleteSingleYesNoController, deleteSequenceYesNoController;
	private String modifiedCalendarId;
	private boolean modifiedCalenderDirty = false;
	private final boolean eventAlwaysVisible;
	
	private CalendarPrintMapper printMapper;
	private String printUrl;
	
	private ILoggingAction calLoggingAction;
	
	private final CalendarNotificationManager calendarNotificationsManager;

	/**
	 * Display week view of calendar. Add the calendars to be displayed via
	 * addKalendarWrapper(KalendarRenderWrapper kalendarWrapper) method.
	 * @param ureq
	 * @param wControl
	 * @param calendarWrappers
	 * @param caller
	 * @param eventAlwaysVisible  When true, the 'isVis()' check is disabled and events will be displayed always.
	 */
	public WeeklyCalendarController(UserRequest ureq, WindowControl wControl, List<KalendarRenderWrapper> calendarWrappers, String caller, boolean eventAlwaysVisible) {
		this(ureq, wControl, calendarWrappers, new ArrayList<KalendarRenderWrapper>(), caller, eventAlwaysVisible);
	}
	
	/**
	 * Display week view of calendar. Add the calendars to be displayed via
	 * addKalendarWrapper(KalendarRenderWrapper kalendarWrapper) method.
	 * @param ureq
	 * @param wControl
	 * @param calendarWrappers
	 * @param importedCalendarWrappers
	 * @param caller
	 * @param calendarSubscription
	 * @param eventAlwaysVisible  When true, the 'isVis()' check is disabled and events will be displayed always.
	 */
	public WeeklyCalendarController(UserRequest ureq, WindowControl wControl, List<KalendarRenderWrapper> calendarWrappers, List<KalendarRenderWrapper> importedCalendarWrappers,
			String caller, boolean eventAlwaysVisible) {
		super(ureq,wControl, "indexWeekly");
		setTranslator(Util.createPackageTranslator(CalendarManager.class, ureq.getLocale(), getTranslator()));

		calendarNotificationsManager = CoreSpringFactory.getImpl(CalendarNotificationManager.class);
		
		this.eventAlwaysVisible = eventAlwaysVisible;
		this.calendarWrappers = calendarWrappers;
		this.importedCalendarWrappers = importedCalendarWrappers;
		this.caller = caller;
		
		String themeBaseUri = wControl.getWindowBackOffice().getWindow().getGuiTheme().getBaseURI();
		printMapper = new CalendarPrintMapper(themeBaseUri, getTranslator());
		printUrl = registerMapper(ureq, printMapper);

		initForm(ureq);
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), OresHelper.lookupType(CalendarManager.class));
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean isGuest = ureq.getUserSession().getRoles().isGuestOnly();

		Collections.sort(calendarWrappers, KalendarComparator.getInstance());
		Collections.sort(importedCalendarWrappers, KalendarComparator.getInstance());
		
		List<KalendarRenderWrapper> allCalendarWrappers = new ArrayList<KalendarRenderWrapper>(calendarWrappers);
		allCalendarWrappers.addAll(importedCalendarWrappers);
		weeklyCalendar = new FullCalendarElement(ureq, "weeklyCalendar", allCalendarWrappers, getTranslator(), eventAlwaysVisible);
		formLayout.add("calendar", weeklyCalendar);
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("caller", caller);
			
			// calendarConfiguration component
			calendarConfig = new CalendarConfigurationController(calendarWrappers, ureq, getWindowControl(), eventAlwaysVisible);
			listenTo(calendarConfig);
			layoutCont.put("calendarConfig", calendarConfig.getInitialComponent());
			
			//imported calendar list
			importedCalendarConfig = new ImportedCalendarConfigurationController(ureq, getWindowControl(),importedCalendarWrappers, false);
			listenTo(importedCalendarConfig);
			layoutCont.put("importedCalendarConfig", importedCalendarConfig.getInitialComponent());
			
			if (!isGuest && !calendarWrappers.isEmpty()) {
				subsContext = calendarNotificationsManager.getSubscriptionContext(calendarWrappers.get(0));
				// if sc is null, then no subscription is desired
				if (subsContext != null) {
					csc = getContextualSubscriptionController(ureq, calendarWrappers.get(0), subsContext);
					layoutCont.put("calsubscription", csc.getInitialComponent());
				}
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private ContextualSubscriptionController getContextualSubscriptionController(UserRequest ureq, KalendarRenderWrapper kalendarRenderWrapper, SubscriptionContext context) {
		String businessPath = getWindowControl().getBusinessControl().getAsString();
		if ((caller.equals(CalendarController.CALLER_COURSE) || caller.equals(CalendarManager.TYPE_COURSE))) {
			Long courseId = kalendarRenderWrapper.getLinkProvider().getControler().getCourseId();
			
			PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(CalendarManager.class), String.valueOf(courseId), businessPath);
			return new ContextualSubscriptionController(ureq, getWindowControl(), context, pdata);
		}
		if ((caller.equals(CalendarController.CALLER_COLLAB) || caller.equals(CalendarManager.TYPE_GROUP))) {
			BusinessGroup businessGroup = calendarNotificationsManager.getBusinessGroup(kalendarRenderWrapper);
			if(businessGroup != null) {
				PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(CalendarManager.class), String.valueOf(businessGroup.getResourceableId()), businessPath);
				return new ContextualSubscriptionController(ureq, getWindowControl(), context, pdata);
			}
		}
		return null;
	}
	
	public Date getFocus() {
		return weeklyCalendar.getFocusDate();
	}

	@Override
	public void setFocus(Date date) {
		Calendar focus = CalendarUtils.createCalendarInstance(getLocale());
		focus.setTime(date);
		weeklyCalendar.setFocusDate(focus.getTime());
	}
	
	@Override
	public void setFocusOnEvent(String eventId) {
		if  (eventId.length() > 0) {
			for(KalendarRenderWrapper wrapper:calendarWrappers) {
				KalendarEvent event = wrapper.getKalendar().getEvent(eventId);
				if(event != null) {
					setFocus(event.getBegin());
					break;
				}
			}
		}
	}

	public void setCalendars(List<KalendarRenderWrapper> calendars) {
		setCalendars(calendars, new ArrayList<KalendarRenderWrapper>());
	}

	public void setCalendars(List<KalendarRenderWrapper> calendars, List<KalendarRenderWrapper> importedCalendars) {
		this.calendarWrappers = calendars;
		this.importedCalendarWrappers = importedCalendars;
		Collections.sort(calendarWrappers, KalendarComparator.getInstance());
		Collections.sort(importedCalendarWrappers, KalendarComparator.getInstance());
		
		List<KalendarRenderWrapper> allCalendarWrappers = new ArrayList<KalendarRenderWrapper>(calendarWrappers);
		allCalendarWrappers.addAll(importedCalendarWrappers);
		weeklyCalendar.setKalendars(allCalendarWrappers);
		
		calendarConfig.setCalendars(calendarWrappers);
		importedCalendarConfig.setCalendars(importedCalendarWrappers);
	}
	
	public void setDirty() {
		dirty = true;
	}
	
	private ILoggingAction getCalLoggingAction() {
		return calLoggingAction;
	}

	private void setCalLoggingAction(ILoggingAction calLoggingAction) {
		this.calLoggingAction = calLoggingAction;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == weeklyCalendar) {
			if (event instanceof KalendarGUIEditEvent) {
				KalendarGUIEditEvent guiEvent = (KalendarGUIEditEvent) event;
				KalendarEvent kalendarEvent = guiEvent.getKalendarEvent();
				if (kalendarEvent == null) {
					// event already deleted
					getWindowControl().setError(translate("cal.error.eventDeleted"));
					return;
				}
				String recurrence = kalendarEvent.getRecurrenceRule();
				boolean isImported = false;
				KalendarRenderWrapper kalendarRenderWrapper = guiEvent.getKalendarRenderWrapper();
				if (kalendarRenderWrapper!=null) {
					isImported = kalendarRenderWrapper.isImported();
				}
				if( !isImported && recurrence != null && !recurrence.equals("") ) {
					List<String> btnLabels = new ArrayList<String>();
					btnLabels.add(translate("cal.edit.dialog.sequence"));
					btnLabels.add(translate("cal.edit.dialog.delete.single"));
					btnLabels.add(translate("cal.edit.dialog.delete.sequence"));
					if (dbcSequence != null) dbcSequence.dispose();
					dbcSequence = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), translate("cal.edit.dialog.title"), translate("cal.edit.dialog.text"), btnLabels);
					dbcSequence.addControllerListener(this);
					dbcSequence.setUserObject(guiEvent);
					dbcSequence.activate();
					return;
				}
				KalendarRenderWrapper kalendarWrapper = guiEvent.getKalendarRenderWrapper();
				pushEditEventController(ureq, kalendarEvent, kalendarWrapper);
			} else if (event instanceof KalendarGUIAddEvent) {
				pushAddEventController((KalendarGUIAddEvent)event, ureq);
			} else if (event instanceof KalendarGUISelectEvent) {
				KalendarGUISelectEvent selectEvent = (KalendarGUISelectEvent)event;
				if(selectEvent.getKalendarEvent() != null) {
					doOpenEventCallout(ureq, selectEvent.getKalendarEvent(), selectEvent.getKalendarRenderWrapper(), selectEvent.getTargetDomId());
				}
			} else if (event instanceof KalendarGUIMoveEvent) {
				KalendarGUIMoveEvent moveEvent = (KalendarGUIMoveEvent)event;
				doMove(moveEvent.getKalendarEvent(), moveEvent.getDayDelta(),
						moveEvent.getMinuteDelta(), moveEvent.getAllDay());
			}
		} else if (event instanceof KalendarGUIPrintEvent) {
			KalendarGUIPrintEvent printEvent = (KalendarGUIPrintEvent)event;
			if(printEvent.getFrom() != null && printEvent.getTo() != null) {
				doPrint(printEvent.getFrom(), printEvent.getTo());
			} else if(printEvent.getTargetDomId() != null) {
				doPrintEventCallout(ureq, printEvent.getTargetDomId());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	public void event(UserRequest ureq, Component source, Event event) {
		if (event == ComponentUtil.VALIDATE_EVENT && dirty) {
			dirty = false;
			fireEvent(ureq, new KalendarModifiedEvent());
		} else if (event == ComponentUtil.VALIDATE_EVENT && weeklyCalendar.getComponent().isDirty() && modifiedCalenderDirty  ) {
			KalendarRenderWrapper kalendarRenderWrapper = weeklyCalendar.getKalendarRenderWrapper(modifiedCalendarId);
			kalendarRenderWrapper.reloadKalendar();	
		}
		super.event(ureq, source, event);
	}

	public void event(UserRequest ureq, Controller source, Event event) {
		Kalendar affectedCal = null;
		if (dirty) {
			dirty = false;
			fireEvent(ureq, new KalendarModifiedEvent());
		}
		if (source == editController) {
			affectedCal = editController.getKalendarEvent().getCalendar();
			cmc.deactivate();
			weeklyCalendar.getComponent().setDirty(true);
			// do logging if affectedCal not null
			if(affectedCal!=null) {
			  ThreadLocalUserActivityLogger.log(getCalLoggingAction(), getClass(), LoggingResourceable.wrap(ureq.getIdentity()), LoggingResourceable.wrap(affectedCal));			
			}
			cleanUp();
		} else if (source == eventDetailsCtr) {
			if(event instanceof KalendarGUIEditEvent) {
				eventCalloutCtr.deactivate();

				KalendarGUIEditEvent editEvent = (KalendarGUIEditEvent)event;
				pushEditEventController(ureq, editEvent.getKalendarEvent(), editEvent.getKalendarRenderWrapper());
			}
		} else if(source == printCtrl) {
			if (event instanceof KalendarGUIPrintEvent) {
				KalendarGUIPrintEvent printEvent = (KalendarGUIPrintEvent)event;
				if(printEvent.getFrom() != null && printEvent.getTo() != null) {
					doPrint(printEvent.getFrom(), printEvent.getTo());
				}
			}
			eventCalloutCtr.deactivate();
		} else if (source == importCalendarController) {
			cmc.deactivate();
		} else if(source == cmc) {
			weeklyCalendar.getComponent().setDirty(true);
			cleanUp();
		} else if (source == calendarConfig || source == importedCalendarConfig) {
			if (event instanceof KalendarGUIAddEvent) {
				pushAddEventController((KalendarGUIAddEvent)event, ureq);
			} else if (event instanceof KalendarGUIImportEvent) {
				pushImportEventController((KalendarGUIImportEvent)event, ureq);
			} else if (event == Event.CHANGED_EVENT) {
				importedCalendarWrappers = ImportCalendarManager.getImportedCalendarsForIdentity(ureq);
				importedCalendarConfig.setCalendars(importedCalendarWrappers);
				setCalendars(calendarWrappers, importedCalendarWrappers);
				weeklyCalendar.getComponent().setDirty(true);
			}
		} else if (source == dbcSequence) {
			if(event != Event.CANCELLED_EVENT) {
				int pos = DialogBoxUIFactory.getButtonPos(event);
				KalendarGUIEditEvent guiEvent = (KalendarGUIEditEvent)dbcSequence.getUserObject();
				KalendarRenderWrapper kalendarWrapper = guiEvent.getKalendarRenderWrapper();
				KalendarEvent kalendarEvent = guiEvent.getKalendarEvent();
				if(pos == 0) { // edit the sequence
					// load the parent event of this sequence
					KalendarEvent parentEvent = kalendarWrapper.getKalendar().getEvent(kalendarEvent.getID());
					pushEditEventController(ureq, parentEvent, kalendarWrapper);
				} else if(pos == 1) { // delete a single event of the sequence
					deleteSingleYesNoController = activateYesNoDialog(ureq, null, translate("cal.delete.dialogtext"), deleteSingleYesNoController);
					deleteSingleYesNoController.setUserObject(kalendarEvent);
				} else if(pos == 2) { // delete the whole sequence
					deleteSequenceYesNoController = activateYesNoDialog(ureq, null, translate("cal.delete.dialogtext.sequence"), deleteSequenceYesNoController);
					deleteSequenceYesNoController.setUserObject(kalendarEvent);
				}
			}
			dbcSequence.dispose();
		} else if (source == deleteSingleYesNoController) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				KalendarEvent kalendarEvent = (KalendarEvent)deleteSingleYesNoController.getUserObject();
				affectedCal = kalendarEvent.getCalendar();
				KalendarEvent kEvent = affectedCal.getEvent(kalendarEvent.getID());
				kEvent.addRecurrenceExc(kalendarEvent.getBegin());
				CalendarManagerFactory.getInstance().getCalendarManager().updateEventFrom(affectedCal, kEvent);
				deleteSingleYesNoController.dispose();
				weeklyCalendar.getComponent().setDirty(true);
			}
		} else if (source == deleteSequenceYesNoController) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				KalendarEvent kalendarEvent = (KalendarEvent)deleteSequenceYesNoController.getUserObject();
				affectedCal = kalendarEvent.getCalendar();
				CalendarManagerFactory.getInstance().getCalendarManager().removeEventFrom(affectedCal, kalendarEvent);
				deleteSequenceYesNoController.dispose();
				weeklyCalendar.getComponent().setDirty(true);
			}
		} 
		if (weeklyCalendar.getComponent().isDirty()) {
			if (subsContext != null) {
				// group or course calendar -> prepared subscription context is the right one
				NotificationsManager.getInstance().markPublisherNews(subsContext, ureq.getIdentity(), true);
			} else if(caller.equals(CALLER_HOME) && affectedCal != null) {
				// one can add/edit/remove dates of group and course calendars from the home calendar view -> choose right subscription context
				for( KalendarRenderWrapper calWrapper : calendarWrappers) {
					if(affectedCal == calWrapper.getKalendar()) {
						SubscriptionContext tmpSubsContext = calendarNotificationsManager.getSubscriptionContext(calWrapper);
						NotificationsManager.getInstance().markPublisherNews(tmpSubsContext, ureq.getIdentity(), true);
					}
				}
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editController);
		removeAsListenerAndDispose(cmc);
		editController = null;
		cmc = null;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String dateEntry = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(dateEntry.startsWith("date")) {
			Date gotoDate = BusinessControlFactory.getInstance().getDateFromContextEntry(entries.get(0));
			if(gotoDate != null) {
				weeklyCalendar.setFocusDate(gotoDate);
			}
		}
	}
	
	private void doPrint(Date from, Date to) {
		StringBuilder sb = new StringBuilder();
		sb.append("window.open('" + printUrl + "/print.html', '_print','height=800,left=100,top=100,width=800,toolbar=no,titlebar=0,status=0,menubar=yes,location= no,scrollbars=1');");
		printMapper.setFrom(from);
		printMapper.setTo(to);
		printMapper.setCalendarWrappers(calendarWrappers);
		printMapper.setImportedCalendarWrappers(importedCalendarWrappers);
		getWindowControl().getWindowBackOffice().sendCommandTo(new JSCommand(sb.toString()));
	}
	
	private void doPrintEventCallout(UserRequest ureq, String targetDomId) {
		removeAsListenerAndDispose(eventCalloutCtr);
		removeAsListenerAndDispose(printCtrl);
		
		printCtrl = new CalendarPrintController(ureq, getWindowControl());
		listenTo(printCtrl);
		
		Component eventCmp = printCtrl.getInitialComponent();
		eventCalloutCtr = new CloseableCalloutWindowController(ureq, getWindowControl(), eventCmp, targetDomId,
				translate("print"), true, "o_cal_event_callout");
		listenTo(eventCalloutCtr);
		eventCalloutCtr.activate();
	}
	
	private void doOpenEventCallout(UserRequest ureq, KalendarEvent calEvent, KalendarRenderWrapper calWrapper, String targetDomId) {
		removeAsListenerAndDispose(eventCalloutCtr);
		removeAsListenerAndDispose(eventDetailsCtr);
		
		eventDetailsCtr = new CalendarDetailsController(ureq, getWindowControl(), calEvent, calWrapper);
		listenTo(eventDetailsCtr);
		
		Component eventCmp = eventDetailsCtr.getInitialComponent();
		eventCalloutCtr = new CloseableCalloutWindowController(ureq, getWindowControl(), eventCmp, targetDomId,
				"Event", true, "o_cal_event_callout");
		listenTo(eventCalloutCtr);
		eventCalloutCtr.activate();
	}
	
	private void doMove(KalendarEvent calEvent, Long dayDelta, Long minuteDelta, Boolean allDay) {
		Kalendar cal = calEvent.getCalendar();
		calEvent.setBegin(doMove(calEvent.getBegin(), dayDelta, minuteDelta));
		calEvent.setEnd(doMove(calEvent.getEnd(), dayDelta, minuteDelta));
		
		if(allDay != null && calEvent.isAllDayEvent() != allDay.booleanValue()) {
			calEvent.setAllDayEvent(allDay.booleanValue());
		}

		CalendarManagerFactory.getInstance().getCalendarManager().updateEventFrom(cal, calEvent);
		weeklyCalendar.getComponent().setDirty(true);
	}
	
	private Date doMove(Date date, Long dayDelta, Long minuteDelta) {
		if(date == null) {
			return date;
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if(dayDelta != null) {
			cal.add(Calendar.DATE, dayDelta.intValue());
		}
		if(minuteDelta != null) {
			cal.add(Calendar.MINUTE, minuteDelta.intValue());
		}
		return cal.getTime();
	}

	/**
	 * @param ureq
	 * @param kalendarEvent
	 * @param kalendarWrapper
	 */
	private void pushEditEventController(UserRequest ureq, KalendarEvent kalendarEvent, KalendarRenderWrapper kalendarWrapper) {
		if(editController != null) return;
		
		removeAsListenerAndDispose(editController);
		
		boolean canEdit = false;
		for (Iterator<KalendarRenderWrapper> iter = calendarWrappers.iterator(); iter.hasNext();) {
			KalendarRenderWrapper wrapper = iter.next();
			if (wrapper.getAccess() == KalendarRenderWrapper.ACCESS_READ_WRITE
					&& kalendarWrapper.getKalendar().getCalendarID().equals(wrapper.getKalendar().getCalendarID())) {
				canEdit = true;
			}
		}
		
		if(canEdit) {
			editController = new CalendarEntryDetailsController(ureq, kalendarEvent, kalendarWrapper,	calendarWrappers, false, caller, getWindowControl());
			listenTo(editController);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), editController.getInitialComponent());
			listenTo(cmc);
			
			cmc.activate();
			
			// set logging action
			setCalLoggingAction(CalendarLoggingAction.CALENDAR_ENTRY_MODIFIED);
		} else {
			showError("cal.error.readonly");
		}
	}

	private void pushAddEventController(KalendarGUIAddEvent addEvent, UserRequest ureq) {
		if(editController != null || ureq.getUserSession().getRoles().isGuestOnly()) {
			return;
		}
		
		KalendarRenderWrapper calendarWrapper = weeklyCalendar.getKalendarRenderWrapper(addEvent.getCalendarID());
		// create new KalendarEvent
		Date begin = addEvent.getStartDate();
		
		KalendarEvent newEvent;
		if(addEvent.getEndDate() == null) {
			newEvent = new KalendarEvent(CodeHelper.getGlobalForeverUniqueID(), "", begin, (1000 * 60 * 60 * 1));
		} else {
			newEvent = new KalendarEvent(CodeHelper.getGlobalForeverUniqueID(), "", begin, addEvent.getEndDate());
		}

		if (calendarWrapper != null &&
				(calendarWrapper.getKalendar().getType().equals(CalendarManager.TYPE_COURSE) ||
				 calendarWrapper.getKalendar().getType().equals(CalendarManager.TYPE_GROUP))) {
			newEvent.setClassification(KalendarEvent.CLASS_PUBLIC);
		}
		
		newEvent.setAllDayEvent(addEvent.isAllDayEvent());
		String lastName  = ureq.getIdentity().getUser().getProperty(UserConstants.LASTNAME, getLocale());
		String firstName = ureq.getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, getLocale()); 
		newEvent.setCreatedBy(firstName + " " + lastName);
		newEvent.setCreated(new Date().getTime());
		ArrayList<KalendarRenderWrapper> allCalendarWrappers = new ArrayList<KalendarRenderWrapper>(calendarWrappers);
		allCalendarWrappers.addAll(importedCalendarWrappers);
		
		removeAsListenerAndDispose(editController);
		editController = new CalendarEntryDetailsController(ureq, newEvent, calendarWrapper, allCalendarWrappers, true, caller, getWindowControl());
		listenTo(editController);
		
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editController.getInitialComponent());
		listenTo(cmc);
		
		cmc.activate();
		
		// set logging action
		setCalLoggingAction(CalendarLoggingAction.CALENDAR_ENTRY_CREATED);
	}
	
	private void pushImportEventController(KalendarGUIImportEvent importEvent, UserRequest ureq) {
		KalendarRenderWrapper calendarWrapper = weeklyCalendar.getKalendarRenderWrapper(importEvent.getCalendarID());

		removeAsListenerAndDispose(importCalendarController);

		importCalendarController = new ImportCalendarController(ureq, getWindowControl(), calendarWrapper);
		listenTo(importCalendarController);
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), importCalendarController.getInitialComponent());
		cmc.activate();
		listenTo(cmc);
	}
	
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, OresHelper.lookupType(CalendarManager.class));
	}
	
	public void event(Event event) {
		if (event instanceof KalendarModifiedEvent) {
			KalendarModifiedEvent kalendarModifiedEvent = (KalendarModifiedEvent)event;
			if (kalendarModifiedEvent.getType()!=null
					&& kalendarModifiedEvent.getCalendarId()!=null
					&& weeklyCalendar.getKalendarRenderWrapper(kalendarModifiedEvent.getCalendarId())!=null
					&& kalendarModifiedEvent.getType().equals(weeklyCalendar.getKalendarRenderWrapper(kalendarModifiedEvent.getCalendarId()).getKalendar().getType())					
					&& kalendarModifiedEvent.getCalendarId().equals(weeklyCalendar.getKalendarRenderWrapper(kalendarModifiedEvent.getCalendarId()).getKalendar().getCalendarID())) {
				// the event is for my calendar => reload it				
				
				//keeping a reference to the dirty calendar as reloading here raises an nested do in sync error. Using the component validation event to reload
				modifiedCalendarId  = kalendarModifiedEvent.getCalendarId();
				modifiedCalenderDirty = true;
				weeklyCalendar.getComponent().setDirty(true);
			}
		}
	}
}