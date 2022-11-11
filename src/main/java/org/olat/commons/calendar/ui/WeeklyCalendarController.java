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

import org.apache.commons.lang.RandomStringUtils;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarNotificationManager;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.CalendarUserConfiguration;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarComparator;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarRecurEvent;
import org.olat.commons.calendar.ui.components.FullCalendarElement;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.events.CalendarGUIAddEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIEditEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIFormEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIImportEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIModifiedEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIMoveEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIPrintEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIRemoveEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIResizeEvent;
import org.olat.commons.calendar.ui.events.CalendarGUISelectEvent;
import org.olat.commons.calendar.ui.events.CalendarGUISettingEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIUpdateEvent;
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
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

public class WeeklyCalendarController extends FormBasicController implements Activateable2, CalendarController, GenericEventListener {
	
	private FullCalendarElement calendarEl;

	private CalendarAggregatedURLController feedUrlCtrl;
	private CalendarEntryDetailsController editController;
	private CalendarPersonalConfigurationController configurationCtrl;

	private CloseableModalController cmc;
	private SubscriptionContext subsContext;
	private ContextualSubscriptionController csc;

	private ConfirmUpdateController updateCtr;
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
	private DialogBoxController deleteSingleYesNoController;
	private DialogBoxController deleteSequenceYesNoController;
	
	private final String caller;
	private boolean dirty = false;
	private final boolean allowImport;
	private final OLATResourceable callerOres;

	private List<KalendarRenderWrapper> calendarWrappers;
	
	private CalendarPrintMapper printMapper;
	private final String printUrl;
	
	private ILoggingAction calLoggingAction;
	
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private CalendarNotificationManager calendarNotificationsManager;
	@Autowired
	private NotificationsManager notificationsManager;
	
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
	public WeeklyCalendarController(UserRequest ureq, WindowControl wControl, List<KalendarRenderWrapper> calendarWrappers,
			String caller, OLATResourceable callerOres, boolean allowImport) {
		super(ureq,wControl, "indexWeekly");
		setTranslator(Util.createPackageTranslator(CalendarManager.class, ureq.getLocale(), getTranslator()));

		this.caller = caller;
		this.allowImport = allowImport;
		this.calendarWrappers = calendarWrappers;
		this.callerOres = callerOres == null ? null : OresHelper.clone(callerOres);
		
		String themeBaseUri = wControl.getWindowBackOffice().getWindow().getGuiTheme().getBaseURI();
		printMapper = new CalendarPrintMapper(themeBaseUri, getTranslator());
		printUrl = registerMapper(ureq, printMapper);

		initForm(ureq);

		getWindowControl().getWindowBackOffice().addCycleListener(this);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), OresHelper.lookupType(CalendarManager.class));
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean isGuest = ureq.getUserSession().getRoles().isGuestOnly();

		Collections.sort(calendarWrappers, KalendarComparator.getInstance());
		
		calendarEl = new FullCalendarElement(ureq, "weeklyCalendar", calendarWrappers, getTranslator());
		formLayout.add("calendar", calendarEl);
		// configuration for all but curriculum calendars
		calendarEl.setConfigurationEnabled(!caller.equals(CalendarController.CALLER_CURRICULUM));
		// aggragted for all but curriculum calendars
		calendarEl.setAggregatedFeedEnabled(!caller.equals(CalendarController.CALLER_CURRICULUM));
		calendarEl.setAlwaysVisibleCalendars(getAlwaysVisibleKalendarRenderWrappers());

		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			if (!isGuest && !calendarWrappers.isEmpty()) {
				subsContext = calendarNotificationsManager.getSubscriptionContext(calendarWrappers.get(0));
				// if sc is null, then no subscription is desired
				if (subsContext != null) {
					csc = getContextualSubscriptionController(ureq, calendarWrappers.get(0), subsContext);
					if(csc != null) {
						layoutCont.put("calsubscription", csc.getInitialComponent());
					}
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
		if (caller.equals(CalendarController.CALLER_COURSE) || caller.equals(CalendarController.CALLER_CURRICULUM) ||caller.equals(CalendarManager.TYPE_COURSE)) {
			String courseId = kalendarRenderWrapper.getCalendarKey().getCalendarId();
			PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(CalendarManager.class), courseId, businessPath);
			return new ContextualSubscriptionController(ureq, getWindowControl(), context, pdata);
		}
		if (caller.equals(CalendarController.CALLER_COLLAB) || caller.equals(CalendarManager.TYPE_GROUP)) {
			BusinessGroup businessGroup = calendarNotificationsManager.getBusinessGroup(kalendarRenderWrapper);
			if(businessGroup != null) {
				PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(CalendarManager.class), String.valueOf(businessGroup.getResourceableId()), businessPath);
				return new ContextualSubscriptionController(ureq, getWindowControl(), context, pdata);
			}
		}
		return null;
	}
	
	public Date getFocus() {
		return calendarEl.getFocusDate();
	}

	@Override
	public void setFocus(Date date) {
		Calendar focus = CalendarUtils.createCalendarInstance(getLocale());
		focus.setTime(date);
		calendarEl.setFocusDate(focus.getTime());
	}
	
	@Override
	public void setFocusOnEvent(String eventId, String recurenceId) {
		if(StringHelper.containsNonWhitespace(eventId)) {
			for(KalendarRenderWrapper wrapper:calendarWrappers) {
				KalendarEvent event = wrapper.getKalendar().getEvent(eventId, recurenceId);
				if(event != null) {
					setFocus(event.getBegin());
					break;
				}
			}
		}
	}

	@Override
	public void setDifferentiateManagedEvent(boolean differentiate) {
		calendarEl.setDifferentiateManagedEvents(differentiate);
	}
	
	@Override
	public void setDifferentiateLiveStreams(boolean differentiate) {
		calendarEl.setDifferentiateLiveStreams(differentiate);
	}

	@Override
	public void setCalendars(List<KalendarRenderWrapper> calendars) {
		calendarWrappers = calendars;
		Collections.sort(calendarWrappers, KalendarComparator.getInstance());
		calendarEl.setCalendars(calendarWrappers);
	}
	
	@Override
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
		if (source == calendarEl) {
			if (event instanceof CalendarGUIEditEvent) {
				CalendarGUIEditEvent guiEvent = (CalendarGUIEditEvent) event;
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
					List<String> btnLabels = new ArrayList<>(3);
					btnLabels.add(translate("cal.edit.dialog.sequence"));
					btnLabels.add(translate("cal.edit.dialog.delete.single"));
					btnLabels.add(translate("cal.edit.dialog.delete.sequence"));
					if (dbcSequence != null) {
						dbcSequence.dispose();
					}
					dbcSequence = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), translate("cal.edit.dialog.title"), translate("cal.edit.dialog.text"), btnLabels);
					dbcSequence.addControllerListener(this);
					dbcSequence.setUserObject(guiEvent);
					dbcSequence.activate();
					return;
				}
				KalendarRenderWrapper kalendarWrapper = guiEvent.getKalendarRenderWrapper();
				pushEditEventController(ureq, kalendarEvent, kalendarWrapper);
			} else if (event instanceof CalendarGUIAddEvent) {
				pushAddEventController((CalendarGUIAddEvent)event, ureq);
			} else if (event instanceof CalendarGUISelectEvent) {
				CalendarGUISelectEvent selectEvent = (CalendarGUISelectEvent)event;
				if(selectEvent.getKalendarEvent() != null) {
					doOpenEventCallout(ureq, selectEvent.getKalendarEvent(), selectEvent.getKalendarRenderWrapper(), selectEvent.getTargetDomId());
				}
			} else if (event instanceof CalendarGUIMoveEvent) {
				CalendarGUIMoveEvent moveEvent = (CalendarGUIMoveEvent)event;
				doMove(ureq, moveEvent.getKalendarEvent(), moveEvent.getDayDelta(),
						moveEvent.getMinuteDelta(), moveEvent.getAllDay());
			} else if (event instanceof CalendarGUIResizeEvent) {
				CalendarGUIResizeEvent resizeEvent = (CalendarGUIResizeEvent)event;
				doResize(ureq, resizeEvent.getKalendarEvent(),
						resizeEvent.getMinuteDelta(), resizeEvent.getAllDay());
			}  else if (event instanceof CalendarGUIFormEvent) {
				String cmd = event.getCommand();
				if(CalendarGUIFormEvent.CONFIGURE.equals(cmd)) {
					doConfigure(ureq);
				} else if(CalendarGUIFormEvent.AGGREGATED_FEED.equals(cmd)) {
					CalendarGUIFormEvent guiEvent = (CalendarGUIFormEvent)event;
					doOpenAggregatedFeedUrl(ureq, guiEvent.getTargetDomId());
				}
			} else if (event instanceof CalendarGUIPrintEvent) {
				CalendarGUIPrintEvent printEvent = (CalendarGUIPrintEvent)event;
				if(printEvent.getFrom() != null && printEvent.getTo() != null) {
					doPrint(printEvent.getFrom(), printEvent.getTo());
				} else if(printEvent.getTargetDomId() != null) {
					doPrintEventCallout(ureq, printEvent.getTargetDomId());
				}
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		Kalendar affectedCal = null;
		if (dirty) {
			dirty = false;
			fireEvent(ureq, new CalendarGUIModifiedEvent());
		}
		if (source == editController) {
			affectedCal = editController.getKalendarEvent().getCalendar();
			cmc.deactivate();
			calendarEl.getComponent().setDirty(true);
			// do logging if affectedCal not null
			if(affectedCal!=null) {
			  ThreadLocalUserActivityLogger.log(getCalLoggingAction(), getClass(), LoggingResourceable.wrap(ureq.getIdentity()), LoggingResourceable.wrap(affectedCal));			
			}
			cleanUp();
		} else if (source == eventDetailsCtr) {
			if(event instanceof CalendarGUIEditEvent) {
				eventCalloutCtr.deactivate();
				cleanUp();

				CalendarGUIEditEvent editEvent = (CalendarGUIEditEvent)event;
				pushEditEventController(ureq, editEvent.getKalendarEvent(), editEvent.getKalendarRenderWrapper());
			} else if(event == Event.DONE_EVENT) {
				eventCalloutCtr.deactivate();
				cleanUp();
			}
		} else if(source == updateCtr) {
			if(event instanceof CalendarGUIUpdateEvent) {
				doUpdate((CalendarGUIUpdateEvent)event, updateCtr.getKalendarEvent(),
						updateCtr.getDayDelta(), updateCtr.getMinuteDelta(), updateCtr.getAllDay(), updateCtr.getChangeBegin());
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == printCtrl) {
			if (event instanceof CalendarGUIPrintEvent) {
				CalendarGUIPrintEvent printEvent = (CalendarGUIPrintEvent)event;
				if(printEvent.getFrom() != null && printEvent.getTo() != null) {
					doPrint(printEvent.getFrom(), printEvent.getTo());
				}
			}
			eventCalloutCtr.deactivate();
			cleanUp();
		} else if(source == cmc) {
			calendarEl.setCalendars(calendarWrappers);
			cleanUp();
		} else if (source == dbcSequence) {
			if(event != Event.CANCELLED_EVENT) {
				int pos = DialogBoxUIFactory.getButtonPos(event);
				CalendarGUIEditEvent guiEvent = (CalendarGUIEditEvent)dbcSequence.getUserObject();
				KalendarRenderWrapper kalendarWrapper = guiEvent.getKalendarRenderWrapper();
				KalendarEvent kalendarEvent = guiEvent.getKalendarEvent();
				if(pos == 0) { // edit the sequence
					// load the parent event of this sequence
					KalendarEvent parentEvent = kalendarWrapper.getKalendar().getEvent(kalendarEvent.getID(), kalendarEvent.getRecurrenceID());
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
				KalendarEvent kEvent = affectedCal.getEvent(kalendarEvent.getID(), kalendarEvent.getRecurrenceID());
				kEvent.addRecurrenceExc(kalendarEvent.getBegin());
				calendarManager.updateEventFrom(affectedCal, kEvent);
				deleteSingleYesNoController.dispose();
				calendarEl.getComponent().setDirty(true);
			}
		} else if (source == deleteSequenceYesNoController) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				KalendarEvent kalendarEvent = (KalendarEvent)deleteSequenceYesNoController.getUserObject();
				affectedCal = kalendarEvent.getCalendar();
				calendarManager.removeEventFrom(affectedCal, kalendarEvent);
				deleteSequenceYesNoController.dispose();
				calendarEl.getComponent().setDirty(true);
			}
		} else if (configurationCtrl == source) {
			if(event instanceof CalendarGUIImportEvent) {
				CalendarGUIImportEvent importEvent = (CalendarGUIImportEvent)event;
				calendarWrappers.add(importEvent.getCalendar());
				// reload only after closing the configuration
			} else if(event instanceof CalendarGUIRemoveEvent) {
				CalendarGUIRemoveEvent removeEvent = (CalendarGUIRemoveEvent)event;
				calendarWrappers.remove(removeEvent.getCalendar());
				// reload only after closing the configuration
			} else if(event instanceof CalendarGUISettingEvent) {
				// reload only after closing the configuration
			}
		} else if(eventCalloutCtr == source) {
			cleanUp();
		}
		
		if (calendarEl.getComponent().isDirty()) {
			if (subsContext != null) {
				// group or course calendar -> prepared subscription context is the right one
				notificationsManager.markPublisherNews(subsContext, ureq.getIdentity(), true);
			} else if(caller.equals(CALLER_HOME) && affectedCal != null) {
				// one can add/edit/remove dates of group and course calendars from the home calendar view -> choose right subscription context
				for( KalendarRenderWrapper calWrapper : calendarWrappers) {
					if(affectedCal == calWrapper.getKalendar()) {
						SubscriptionContext tmpSubsContext = calendarNotificationsManager.getSubscriptionContext(calWrapper);
						notificationsManager.markPublisherNews(tmpSubsContext, ureq.getIdentity(), true);
					}
				}
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(configurationCtrl);
		removeAsListenerAndDispose(eventCalloutCtr);
		removeAsListenerAndDispose(eventDetailsCtr);
		removeAsListenerAndDispose(editController);
		removeAsListenerAndDispose(updateCtr);
		removeAsListenerAndDispose(cmc);
		eventCalloutCtr = null;
		eventDetailsCtr = null;
		configurationCtrl = null;
		editController = null;
		updateCtr = null;
		cmc = null;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String dateEntry = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(dateEntry.startsWith("date")) {
			Date gotoDate = BusinessControlFactory.getInstance().getDateFromContextEntry(entries.get(0));
			if(gotoDate != null) {
				calendarEl.setFocusDate(gotoDate);
			}
		}
	}
	
	private void doOpenAggregatedFeedUrl(UserRequest ureq, String targetDomId) {
		String callerUrl = getCallerCalendarUrl();
		String aggregatedUrl = getAggregatedCalendarUrl(CalendarManager.TYPE_USER_AGGREGATED, getIdentity().getKey());
		feedUrlCtrl = new CalendarAggregatedURLController(ureq, getWindowControl(), callerUrl, aggregatedUrl);
		listenTo(feedUrlCtrl);
		
		eventCalloutCtr = new CloseableCalloutWindowController(ureq, getWindowControl(), feedUrlCtrl.getInitialComponent(), targetDomId,
				translate("print"), true, "o_cal_event_callout");
		listenTo(eventCalloutCtr);
		eventCalloutCtr.activate();
	}
	
	private List<KalendarRenderWrapper> getAlwaysVisibleKalendarRenderWrappers() {
		if(callerOres == null || calendarWrappers == null) return new ArrayList<>(1);
		
		List<KalendarRenderWrapper> alwaysVisible = new ArrayList<>();
		if(CalendarController.CALLER_CURRICULUM.equals(caller)) {
			alwaysVisible.addAll(calendarWrappers);
		} else {
			KalendarRenderWrapper callerKalendar = getCallerKalendarRenderWrapper();
			if(callerKalendar != null ) {
				alwaysVisible.add(callerKalendar);
			}
		}
		
		return alwaysVisible;
	}
	
	private KalendarRenderWrapper getCallerKalendarRenderWrapper() {
		if(callerOres == null || calendarWrappers == null) return null;
		
		String callerResId = callerOres.getResourceableId().toString();
		for(KalendarRenderWrapper calendarWrapper:calendarWrappers) {
			String calendarType = calendarWrapper.getKalendar().getType();
			String calendarId = calendarWrapper.getKalendar().getCalendarID();
			if(callerResId.equals(calendarId)) {
				if((CalendarController.CALLER_COLLAB.equals(caller) && CalendarManager.TYPE_GROUP.equals(calendarType))
						|| (CalendarController.CALLER_COURSE.equals(caller) && CalendarManager.TYPE_COURSE.equals(calendarType))) {
					return calendarWrapper;
				}
			} else if((CalendarController.CALLER_PROFILE.equals(caller) || CalendarController.CALLER_HOME.equals(caller))
				&& CalendarManager.TYPE_USER.equals(calendarType) && calendarId.equals(callerOres.getResourceableTypeName())) {
					return calendarWrapper;
			}
		}
		return null;
	}
	
	private String getCallerCalendarUrl() {
		if(callerOres == null) return null;
		
		String url = null;
		if(CalendarController.CALLER_COLLAB.equals(caller)) {
			url = getCallerCalendarUrl(CalendarManager.TYPE_GROUP, callerOres.getResourceableId().toString());
		} else if(CalendarController.CALLER_COURSE.equals(caller)) {
			url = getCallerCalendarUrl(CalendarManager.TYPE_COURSE, callerOres.getResourceableId().toString());
		} else if(CalendarController.CALLER_CURRICULUM.equals(caller)) {
			url = getAggregatedCalendarUrl(CalendarManager.TYPE_CURRICULUM_EL_AGGREGATED, callerOres.getResourceableId());
		}
		return url;
	}
	
	private String getCallerCalendarUrl(String type, String calendarId) {
		Kalendar callerKalendar = null;
		for(KalendarRenderWrapper calendarWrapper:calendarWrappers) {
			if(calendarWrapper.getKalendar().getType().equals(type) && calendarWrapper.getKalendar().getCalendarID().equals(calendarId)) {
				callerKalendar = calendarWrapper.getKalendar();
				break;
			}
		}
		
		if(callerKalendar != null) {
			CalendarUserConfiguration config = calendarManager.getCalendarUserConfiguration(getIdentity(), callerKalendar);
			if(config == null) {
				config = calendarManager.createCalendarConfig(getIdentity(), callerKalendar);
			} else if(!StringHelper.containsNonWhitespace(config.getToken())) {
				config.setToken(RandomStringUtils.randomAlphanumeric(6));
				config = calendarManager.saveCalendarConfig(config);
			}
			return Settings.getServerContextPathURI() + "/ical/" + type + "/" + config.getKey()
				+ "/" + config.getToken() + "/" + callerKalendar.getCalendarID() + ".ics";
		}
		return null;
	}
	
	private String getAggregatedCalendarUrl(String calendarType, Long calendarId) {
		List<CalendarUserConfiguration> configurations = calendarManager
				.getCalendarUserConfigurationsList(getIdentity(), calendarType, calendarId.toString());

		CalendarUserConfiguration config;
		if(configurations.isEmpty()) {
			config = calendarManager.createAggregatedCalendarConfig(calendarType, calendarId, getIdentity());
		} else if(StringHelper.containsNonWhitespace(configurations.get(0).getToken())) {
			config = configurations.get(0);
		} else {
			config = configurations.get(0);
			config.setToken(RandomStringUtils.randomAlphanumeric(6));
			config = calendarManager.saveCalendarConfig(config);
		}
		return Settings.getServerContextPathURI() + "/ical/" + calendarType + "/" + config.getKey() + "/" + config.getToken() + ".ics";
	}
	
	private void doConfigure(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(configurationCtrl);
		
		List<KalendarRenderWrapper> alwaysVisibleKalendars = this.getAlwaysVisibleKalendarRenderWrappers();
		List<KalendarRenderWrapper> allCalendars = new ArrayList<>(calendarWrappers);
		configurationCtrl = new CalendarPersonalConfigurationController(ureq, getWindowControl(),
				allCalendars, alwaysVisibleKalendars, allowImport);
		listenTo(configurationCtrl);
		
		String title = translate("cal.configuration.list");
		cmc = new CloseableModalController(getWindowControl(), "c", configurationCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doPrint(Date from, Date to) {
		StringBuilder sb = new StringBuilder();
		sb.append("window.open('" + printUrl + "/print.html', '_print','height=800,left=100,top=100,width=800,toolbar=no,titlebar=0,status=0,menubar=yes,location= no,scrollbars=1');");
		printMapper.setFrom(from);
		printMapper.setTo(to);
		printMapper.setCalendarWrappers(calendarWrappers);
		getWindowControl().getWindowBackOffice().sendCommandTo(new JSCommand(sb.toString()));
	}
	
	private void doPrintEventCallout(UserRequest ureq, String targetDomId) {
		if(eventCalloutCtr != null && printCtrl != null) return;
		
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
		if(eventCalloutCtr != null && eventDetailsCtr != null) return;
		
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
	
	private void doMove(UserRequest ureq, KalendarEvent calEvent, Long dayDelta, Long minuteDelta, Boolean allDay) {
		if(calEvent instanceof KalendarRecurEvent && !StringHelper.containsNonWhitespace(calEvent.getRecurrenceID())) {
			updateCtr = new ConfirmUpdateController(ureq, getWindowControl(), (KalendarRecurEvent)calEvent, dayDelta, minuteDelta, allDay, true);
			listenTo(updateCtr);
			
			String title = translate("cal.edit.update");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), updateCtr.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		} else if(calEvent != null) {
			Kalendar cal = calEvent.getCalendar();
			calEvent.setBegin(doMove(calEvent.getBegin(), dayDelta, minuteDelta));
			calEvent.setEnd(doMove(calEvent.getEnd(), dayDelta, minuteDelta));
			if(allDay != null && calEvent.isAllDayEvent() != allDay.booleanValue()) {
				calEvent.setAllDayEvent(allDay.booleanValue());
			}
			calendarManager.updateEventFrom(cal, calEvent);
			calendarEl.getComponent().setDirty(true);
		} else {
			showWarning("cal.error.eventDeleted");
			calendarEl.getComponent().setDirty(true);
		}
	}
	
	private void doResize(UserRequest ureq, KalendarEvent calEvent, Long minuteDelta, Boolean allDay) {
		if(calEvent instanceof KalendarRecurEvent && !StringHelper.containsNonWhitespace(calEvent.getRecurrenceID())) {
			updateCtr = new ConfirmUpdateController(ureq, getWindowControl(), (KalendarRecurEvent)calEvent, 0L, minuteDelta, allDay, false);
			listenTo(updateCtr);
			
			String title = translate("cal.edit.update");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), updateCtr.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		} else if(calEvent != null) {
			Kalendar cal = calEvent.getCalendar();
			calEvent.setEnd(doMove(calEvent.getEnd(), 0L, minuteDelta));
			if(allDay != null && calEvent.isAllDayEvent() != allDay.booleanValue()) {
				calEvent.setAllDayEvent(allDay.booleanValue());
			}
			calendarManager.updateEventFrom(cal, calEvent);
			calendarEl.getComponent().setDirty(true);
		} else {
			showWarning("cal.error.eventDeleted");
			calendarEl.getComponent().setDirty(true);
		}
	}
	
	private void doUpdate(CalendarGUIUpdateEvent event, KalendarEvent kalendarEvent, Long dayDelta, Long minuteDelta, Boolean allDay, boolean changeBegin) {
		switch(event.getCascade()) {
			case all: {
				if (changeBegin) {
					kalendarEvent.setBegin(doMove(kalendarEvent.getBegin(), dayDelta, minuteDelta));
				}
				kalendarEvent.setEnd(doMove(kalendarEvent.getEnd(), dayDelta, minuteDelta));
				if(allDay != null && kalendarEvent.isAllDayEvent() != allDay.booleanValue()) {
					kalendarEvent.setAllDayEvent(allDay.booleanValue());
				}
				calendarManager.updateEventFrom(kalendarEvent.getCalendar(), kalendarEvent);
				break;
			}
			case once: {
				if(kalendarEvent instanceof KalendarRecurEvent) {
					KalendarRecurEvent refEvent = (KalendarRecurEvent)kalendarEvent;
					kalendarEvent = calendarManager.createKalendarEventRecurringOccurence(refEvent);
					if (changeBegin) {
						kalendarEvent.setBegin(doMove(kalendarEvent.getBegin(), dayDelta, minuteDelta));
					}
					kalendarEvent.setEnd(doMove(kalendarEvent.getEnd(), dayDelta, minuteDelta));
					if(allDay != null && kalendarEvent.isAllDayEvent() != allDay.booleanValue()) {
						kalendarEvent.setAllDayEvent(allDay.booleanValue());
					}
					calendarManager.addEventTo(refEvent.getCalendar(), kalendarEvent);
				}
				break;
			}
		}
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
		if(guardModalController(editController)) return;
		
		removeAsListenerAndDispose(cmc);
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
	
	private void pushAddEventController(CalendarGUIAddEvent addEvent, UserRequest ureq) {
		if(guardModalController(editController) || ureq.getUserSession().getRoles().isGuestOnly()) {
			return;
		}
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(editController);
		
		KalendarRenderWrapper calendarWrapper = calendarEl.getCalendar(addEvent.getCalendarID());
		List<KalendarRenderWrapper> copyCalendarWrappers = new ArrayList<>(calendarWrappers);
		
		boolean isReadOnly = calendarWrapper == null || calendarWrapper.getAccess() == KalendarRenderWrapper.ACCESS_READ_ONLY;
		for(KalendarRenderWrapper copyCalendarWrapper:copyCalendarWrappers) {
			isReadOnly &= copyCalendarWrapper.getAccess() == KalendarRenderWrapper.ACCESS_READ_ONLY;
		}
		
		if(!isReadOnly) {
			// create new KalendarEvent
			KalendarEvent newEvent;
			Date begin = addEvent.getStartDate();
			String eventId = CodeHelper.getGlobalForeverUniqueID();
			if(addEvent.getEndDate() == null) {
				newEvent = new KalendarEvent(eventId, "", begin, (1000 * 60 * 60 * 1));
			} else {
				newEvent = new KalendarEvent(eventId, null, "", begin, addEvent.getEndDate());
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

			editController = new CalendarEntryDetailsController(ureq, newEvent, calendarWrapper, copyCalendarWrappers, true, caller, getWindowControl());
			listenTo(editController);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), editController.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
			
			// set logging action
			setCalLoggingAction(CalendarLoggingAction.CALENDAR_ENTRY_CREATED);
		}
	}
	
	@Override
	protected void doDispose() {
		getWindowControl().getWindowBackOffice().removeCycleListener(this);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, OresHelper.lookupType(CalendarManager.class));
        super.doDispose();
	}
	
	@Override
	public void event(Event event) {
		if (event instanceof CalendarGUIModifiedEvent) {
			CalendarGUIModifiedEvent kalendarModifiedEvent = (CalendarGUIModifiedEvent)event;
			if (kalendarModifiedEvent.getType()!=null
					&& kalendarModifiedEvent.getCalendarId()!=null
					&& calendarEl.getCalendar(kalendarModifiedEvent.getCalendarId())!=null
					&& kalendarModifiedEvent.getType().equals(calendarEl.getCalendar(kalendarModifiedEvent.getCalendarId()).getKalendar().getType())					
					&& kalendarModifiedEvent.getCalendarId().equals(calendarEl.getCalendar(kalendarModifiedEvent.getCalendarId()).getKalendar().getCalendarID())) {
				// the event is for my calendar => reload it				
				
				//keeping a reference to the dirty calendar as reloading here raises an nested do in sync error. Using the component validation event to reload
				calendarEl.getComponent().setDirty(true);
			}
		}
	}
}