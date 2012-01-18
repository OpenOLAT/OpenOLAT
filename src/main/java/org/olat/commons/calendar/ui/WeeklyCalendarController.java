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
import org.olat.commons.calendar.GotoDateEvent;
import org.olat.commons.calendar.ImportCalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarComparator;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.components.WeeklyCalendarComponent;
import org.olat.commons.calendar.ui.events.KalendarGUIAddEvent;
import org.olat.commons.calendar.ui.events.KalendarGUIEditEvent;
import org.olat.commons.calendar.ui.events.KalendarGUIImportEvent;
import org.olat.commons.calendar.ui.events.KalendarModifiedEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.util.ComponentUtil;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.notifications.ContextualSubscriptionController;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.SubscriptionContext;
import org.olat.core.util.resource.OresHelper;
import org.olat.util.logging.activity.LoggingResourceable;

import de.bps.olat.util.notifications.SubscriptionProvider;
import de.bps.olat.util.notifications.SubscriptionProviderImpl;

public class WeeklyCalendarController extends BasicController implements Activateable2, CalendarController, GenericEventListener {

	private static final String CMD_PREVIOUS_WEEK = "pw";
	private static final String CMD_NEXT_WEEK = "nw";

	public static final String CALLER_HOME = "home";
	public static final String CALLER_PROFILE = "profile";
	public static final String CALLER_COLLAB = "collab";
	public static final String CALLER_COURSE = "course";
	
	private Panel mainPanel;
	private VelocityContainer vcMain;
	private List<KalendarRenderWrapper> calendarWrappers;
	private List<KalendarRenderWrapper> importedCalendarWrappers;
	private WeeklyCalendarComponent weeklyCalendar;
	private KalendarConfigurationController calendarConfig;
	private ImportedCalendarConfigurationController importedCalendarConfig;
	private KalendarEntryDetailsController editController;
	private SearchAllCalendarsController searchController;
	private ImportCalendarController importCalendarController;
	private CalendarSubscription calendarSubscription;
	private Controller subscriptionController;
	private String caller;
	private boolean dirty = false;
	private Link thisWeekLink;
	private Link searchLink;
	private Link subscribeButton;
	private Link unsubscribeButton;

	private CloseableModalController cmc;
	private GotoDateCalendarsForm gotoDateForm;
	private SubscriptionContext subsContext;
	private ContextualSubscriptionController csc;
	
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
	
	private ILoggingAction calLoggingAction;

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
		this(ureq, wControl, calendarWrappers, new ArrayList<KalendarRenderWrapper>(), caller, null, eventAlwaysVisible);
	}

	/**
	 * Used for Home 
	 * @param ureq
	 * @param wControl
	 * @param calendarWrappers
	 * @param importedCalendarWrappers
	 * @param caller
	 * @param eventAlwaysVisible  When true, the 'isVis()' check is disabled and events will be displayed always.
	 */
	public WeeklyCalendarController(UserRequest ureq, WindowControl wControl, List<KalendarRenderWrapper> calendarWrappers, List<KalendarRenderWrapper> importedCalendarWrappers, String caller, boolean eventAlwaysVisible) {
		this(ureq, wControl, calendarWrappers, importedCalendarWrappers, caller, null, eventAlwaysVisible );
	}

	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param calendarWrappers
	 * @param caller
	 * @param calendarSubscription
	 * @param eventAlwaysVisible  When true, the 'isVis()' check is disabled and events will be displayed always.
	 */
	public WeeklyCalendarController(UserRequest ureq, WindowControl wControl, List<KalendarRenderWrapper> calendarWrappers, String caller, CalendarSubscription calendarSubscription, boolean eventAlwaysVisible) {
		this(ureq, wControl, calendarWrappers, new ArrayList<KalendarRenderWrapper>(), caller, calendarSubscription, eventAlwaysVisible);
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
	public WeeklyCalendarController(UserRequest ureq, WindowControl wControl, List<KalendarRenderWrapper> calendarWrappers, List<KalendarRenderWrapper> importedCalendarWrappers, String caller, CalendarSubscription calendarSubscription, boolean eventAlwaysVisible) {
		super(ureq,wControl);
		
		setBasePackage(CalendarManager.class);
		
		this.calendarWrappers = calendarWrappers;
		this.importedCalendarWrappers = importedCalendarWrappers;
		this.calendarSubscription = calendarSubscription;
		this.caller = caller;
		
		// fxdiff OLAT-6399
		boolean isGuest = ureq.getUserSession().getRoles().isGuestOnly();

		// main panel
		mainPanel = new Panel("mainPanel");
		
		// main velocity controller
		vcMain = createVelocityContainer("indexWeekly");
		thisWeekLink = LinkFactory.createLink("cal.thisweek", vcMain, this);
		gotoDateForm = new GotoDateCalendarsForm(ureq, wControl, getTranslator());
		listenTo(gotoDateForm); 
		vcMain.put("cal.gotodate", gotoDateForm.getInitialComponent());
		searchLink = LinkFactory.createLink("cal.search.button", vcMain, this);
		subscribeButton = LinkFactory.createButtonXSmall("cal.subscribe", vcMain, this);
		unsubscribeButton = LinkFactory.createButtonXSmall("cal.unsubscribe", vcMain, this);
		
		vcMain.contextPut("caller", caller);
		mainPanel.setContent(vcMain);
		
		Collections.sort(calendarWrappers, KalendarComparator.getInstance());
		Collections.sort(importedCalendarWrappers, KalendarComparator.getInstance());
		
		List<KalendarRenderWrapper> allCalendarWrappers = new ArrayList<KalendarRenderWrapper>(calendarWrappers);
		allCalendarWrappers.addAll(importedCalendarWrappers);
		weeklyCalendar = new WeeklyCalendarComponent("weeklyCalendar", allCalendarWrappers, 7, getTranslator(), eventAlwaysVisible);
		weeklyCalendar.addListener(this);

	  // subscription, see OLAT-3861
		if (!isGuest) {
			SubscriptionProvider provider = new SubscriptionProviderImpl(caller, calendarWrappers.get(0));
			subsContext = provider.getSubscriptionContext();
			// if sc is null, then no subscription is desired
			if (subsContext != null) {
				csc = provider.getContextualSubscriptionController(ureq, getWindowControl());
				vcMain.put("calsubscription", csc.getInitialComponent());
			}
		}
		
		ComponentUtil.registerForValidateEvents(vcMain, this);

		vcMain.put("calendar", weeklyCalendar);

		// calendarConfiguration component
		calendarConfig = new KalendarConfigurationController(calendarWrappers, ureq, getWindowControl(), eventAlwaysVisible, true);
		listenTo(calendarConfig);
		
		vcMain.put("calendarConfig", calendarConfig.getInitialComponent());
		importedCalendarConfig = new ImportedCalendarConfigurationController(importedCalendarWrappers, ureq, getWindowControl(), false);
		importedCalendarConfig.addControllerListener(this);
		vcMain.put("importedCalendarConfig", importedCalendarConfig.getInitialComponent());

		// calendar subscription
		if (calendarSubscription == null || isGuest) {
			vcMain.contextPut("hasSubscription", Boolean.FALSE);
		} else {
			vcMain.contextPut("hasSubscription", Boolean.TRUE);
			vcMain.contextPut("isSubscribed", new Boolean(calendarSubscription.isSubscribed()));
		}
		setWeekYearInVelocityPage(vcMain, weeklyCalendar);

		this.putInitialPanel(mainPanel);
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), OresHelper.lookupType(CalendarManager.class));
	}
	
	public void setEnableRemoveFromPersonalCalendar(boolean enable) {
		calendarConfig.setEnableRemoveFromPersonalCalendar(enable);
	}
	
	public int getFocusYear() {
		return weeklyCalendar.getYear();
	}
	
	public int getFocusWeekOfYear() {
		return weeklyCalendar.getWeekOfYear();
	}

	public void setFocus(Date date) {
		Calendar focus = CalendarUtils.createCalendarInstance(getLocale());
		focus.setTime(date);
		weeklyCalendar.setFocus(focus.get(Calendar.YEAR), focus.get(Calendar.WEEK_OF_YEAR));
		setWeekYearInVelocityPage(vcMain, weeklyCalendar);
	}
	
	public void setFocus(int year, int weekOfYear) {
		weeklyCalendar.setFocus(year, weekOfYear);
	}

	private void setWeekYearInVelocityPage(VelocityContainer vc, WeeklyCalendarComponent weeklyCalendar) {
		vc.contextPut("week", weeklyCalendar.getWeekOfYear());
		vc.contextPut("year", weeklyCalendar.getYear());
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

	public void setCalendars(List calendars) {
		setCalendars(calendars, new ArrayList());
	}

	public void setCalendars(List calendars, List importedCalendars) {
		this.calendarWrappers = calendars;
		Collections.sort(calendarWrappers, KalendarComparator.getInstance());
		weeklyCalendar.setKalendars(calendarWrappers);
		calendarConfig.setCalendars(calendarWrappers);

		this.importedCalendarWrappers = importedCalendars;
		Collections.sort(calendarWrappers, KalendarComparator.getInstance());
		Collections.sort(importedCalendarWrappers, KalendarComparator.getInstance());
		
		ArrayList allCalendarWrappers = new ArrayList(calendarWrappers);
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

	public void event(UserRequest ureq, Component source, Event event) {
		if (event == ComponentUtil.VALIDATE_EVENT && dirty) {
			dirty = false;
			fireEvent(ureq, new KalendarModifiedEvent());
		} else if (event == ComponentUtil.VALIDATE_EVENT && weeklyCalendar.isDirty() && modifiedCalenderDirty  ) {
			KalendarRenderWrapper kalendarRenderWrapper = weeklyCalendar.getKalendarRenderWrapper(modifiedCalendarId);
			kalendarRenderWrapper.reloadKalendar();	
		}else if (source == vcMain) {
			if (event.getCommand().equals(CMD_PREVIOUS_WEEK)) {
				weeklyCalendar.previousWeek();
			} else if (event.getCommand().equals(CMD_NEXT_WEEK)) {
				weeklyCalendar.nextWeek();
			}
			setWeekYearInVelocityPage(vcMain, weeklyCalendar);
		} else if (source == thisWeekLink){
			Calendar cal = CalendarUtils.createCalendarInstance(ureq.getLocale());
			weeklyCalendar.setFocus(cal.get(Calendar.YEAR), cal.get(Calendar.WEEK_OF_YEAR));
		} else if (source == searchLink) {
			
			ArrayList allCalendarWrappers = new ArrayList(calendarWrappers);
			allCalendarWrappers.addAll(importedCalendarWrappers);
			
			removeAsListenerAndDispose(searchController);
			searchController = new SearchAllCalendarsController(ureq, getWindowControl(), allCalendarWrappers);
			listenTo(searchController);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent());
			listenTo(cmc);
			
			cmc.activate();
		} else if (source == subscribeButton || source == unsubscribeButton) {
			removeAsListenerAndDispose(subscriptionController);
			if(calendarSubscription.isSubscribed() == (source == unsubscribeButton)) {
				subscriptionController = calendarSubscription.triggerSubscribeAction();
			}
			if (subscriptionController != null) {
				// activate subscription controller
				listenTo(subscriptionController);
				mainPanel.setContent(subscriptionController.getInitialComponent());
			} else {
				vcMain.contextPut("isSubscribed", new Boolean(calendarSubscription.isSubscribed()));
				CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new KalendarModifiedEvent(), OresHelper.lookupType(CalendarManager.class));
			}
		} else if (source == weeklyCalendar) {
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
			} 
		} 
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
			weeklyCalendar.setDirty(true);
			// do logging if affectedCal not null
			if(affectedCal!=null) {
			  ThreadLocalUserActivityLogger.log(getCalLoggingAction(), getClass(), LoggingResourceable.wrap(ureq.getIdentity()), LoggingResourceable.wrap(affectedCal));			
			}
		} else if (source == importCalendarController) {
			cmc.deactivate();
		} else if(source == cmc && event == CloseableModalController.CLOSE_MODAL_EVENT){
			//DO NOT DEACTIVATE AS ALREADY CLOSED BY CloseableModalController INTERNALLY
			weeklyCalendar.setDirty(true);
		} else if (source == calendarConfig || source == importedCalendarConfig) {
			if (event instanceof KalendarGUIAddEvent) {
				pushAddEventController((KalendarGUIAddEvent)event, ureq);
			} else if (event instanceof KalendarGUIImportEvent) {
				pushImportEventController((KalendarGUIImportEvent)event, ureq);
			} else if (event == Event.CHANGED_EVENT) {
				importedCalendarWrappers = ImportCalendarManager.getImportedCalendarsForIdentity(ureq);
				importedCalendarConfig.setCalendars(importedCalendarWrappers);
				this.setCalendars(calendarWrappers, importedCalendarWrappers);
				weeklyCalendar.setDirty(true);
				vcMain.setDirty(true);
			}
		} else if (source == searchController) {
			if (event instanceof GotoDateEvent) {
				Date gotoDate = ((GotoDateEvent)event).getGotoDate();
			  weeklyCalendar.setDate(gotoDate);
			  setWeekYearInVelocityPage(vcMain, weeklyCalendar);
			}
			cmc.deactivate();
		} else if (source == subscriptionController) {
			// nothing to do here
			mainPanel.setContent(vcMain);
			vcMain.contextPut("isSubscribed", new Boolean(calendarSubscription.isSubscribed()));
		} else if (source == gotoDateForm) {
			weeklyCalendar.setDate(gotoDateForm.getGotoDate());
			setWeekYearInVelocityPage(vcMain, weeklyCalendar);
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
				weeklyCalendar.setDirty(true);
				vcMain.setDirty(true);
			}
		} else if (source == deleteSequenceYesNoController) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				KalendarEvent kalendarEvent = (KalendarEvent)deleteSequenceYesNoController.getUserObject();
				affectedCal = kalendarEvent.getCalendar();
				CalendarManagerFactory.getInstance().getCalendarManager().removeEventFrom(affectedCal, kalendarEvent);
				deleteSequenceYesNoController.dispose();
				weeklyCalendar.setDirty(true);
				vcMain.setDirty(true);
			}
		} 
		if (weeklyCalendar.isDirty()) {
			if (subsContext != null) {
				// group or course calendar -> prepared subscription context is the right one
				NotificationsManager.getInstance().markPublisherNews(subsContext, ureq.getIdentity());
			} else if(this.caller.equals(CALLER_HOME) && affectedCal != null) {
				// one can add/edit/remove dates of group and course calendars from the home calendar view -> choose right subscription context
				KalendarRenderWrapper calWrapper = null;
				for( Object calWrapperObj : calendarWrappers) {
					calWrapper = (KalendarRenderWrapper) calWrapperObj;
					if(affectedCal == calWrapper.getKalendar()) break;
				}
				if(calWrapper != null) {
					SubscriptionProvider provider = new SubscriptionProviderImpl(calWrapper);
					SubscriptionContext tmpSubsContext = provider.getSubscriptionContext();
					NotificationsManager.getInstance().markPublisherNews(tmpSubsContext, ureq.getIdentity());
				}
			}
		}
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String dateEntry = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(dateEntry.startsWith("date")) {
			Date gotoDate = BusinessControlFactory.getInstance().getDateFromContextEntry(entries.get(0));
			if(gotoDate != null) {
				weeklyCalendar.setDate(gotoDate);
				setWeekYearInVelocityPage(vcMain, weeklyCalendar);
			}
		}
	}

	/**
	 * @param ureq
	 * @param kalendarEvent
	 * @param kalendarWrapper
	 */
	private void pushEditEventController(UserRequest ureq, KalendarEvent kalendarEvent, KalendarRenderWrapper kalendarWrapper) {
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
			editController = new KalendarEntryDetailsController(ureq, kalendarEvent, kalendarWrapper,	calendarWrappers, false, caller, getWindowControl());
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
		KalendarRenderWrapper calendarWrapper = weeklyCalendar.getKalendarRenderWrapper(addEvent.getCalendarID());
		// create new KalendarEvent
		KalendarEvent newEvent = new KalendarEvent(CodeHelper.getGlobalForeverUniqueID(), "", addEvent.getStartDate(), (1000 * 60 * 60 * 1));
		if (calendarWrapper.getKalendar().getType().equals(CalendarManager.TYPE_COURSE) ||
				calendarWrapper.getKalendar().getType().equals(CalendarManager.TYPE_GROUP))
			newEvent.setClassification(KalendarEvent.CLASS_PUBLIC);
		
		newEvent.setAllDayEvent(addEvent.isAllDayEvent());
		String lastName  = ureq.getIdentity().getUser().getProperty(UserConstants.LASTNAME, getLocale());
		String firstName = ureq.getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, getLocale()); 
		newEvent.setCreatedBy(firstName + " " + lastName);
		newEvent.setCreated(new Date().getTime());
		ArrayList<KalendarRenderWrapper> allCalendarWrappers = new ArrayList<KalendarRenderWrapper>(calendarWrappers);
		allCalendarWrappers.addAll(importedCalendarWrappers);
		
		removeAsListenerAndDispose(editController);
		editController = new KalendarEntryDetailsController(
				ureq, newEvent, calendarWrapper, allCalendarWrappers, true, caller, getWindowControl()
		);
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
				weeklyCalendar.setDirty(true);
			}
		}
	}

}