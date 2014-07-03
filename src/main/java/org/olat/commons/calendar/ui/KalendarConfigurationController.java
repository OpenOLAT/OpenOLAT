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

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.ICalTokenGenerator;
import org.olat.commons.calendar.model.KalendarConfig;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.events.KalendarGUIAddEvent;
import org.olat.commons.calendar.ui.events.KalendarGUIImportEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.Util;
import org.olat.course.run.calendar.CourseCalendarSubscription;

public class KalendarConfigurationController extends BasicController {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(CalendarManager.class);

	private static final Object CMD_ADD = "add";
	private static final Object CMD_IMPORT = "import";
	private static final Object CMD_TOGGLE_DISPLAY = "tglvis";
	private static final Object CMD_CHOOSE_COLOR = "cc";
	private static final Object CMD_ICAL_FEED = "if";
	private static final Object CMD_ICAL_REGENERATE = "rf";
	private static final Object CMD_ICAL_REMOVE_FEED = "rmif";
	private static final Object CMD_UNSUBSCRIBE = "unsub";
	private static final String PARAM_ID = "id";

	private VelocityContainer configVC;
	private List<KalendarRenderWrapper> calendars;
	private CalendarColorChooserController colorChooser;
	private KalendarRenderWrapper lastCalendarWrapper;
	private CloseableModalController cmc;
	private String currentCalendarID;
	private CalendarExportController exportController;
	private DialogBoxController confirmRemoveDialog;
	private DialogBoxController confirmRegenerateDialog;
	
	private List<String> subscriptionIds;

	public KalendarConfigurationController(List<KalendarRenderWrapper> calendars, UserRequest ureq, WindowControl wControl, boolean insideManager) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CalendarManager.class, ureq.getLocale()));
		
		configVC = new VelocityContainer("calEdit", VELOCITY_ROOT + "/calConfig.html", getTranslator(), this);
		setCalendars(ureq, calendars);
		configVC.contextPut("insideManager", insideManager);
		configVC.contextPut("identity", ureq.getIdentity());
		configVC.contextPut("removeFromPersonalCalendar", Boolean.TRUE);
		putInitialPanel(configVC);
	}
	
	public void setEnableRemoveFromPersonalCalendar(boolean enable) {
		configVC.contextPut("removeFromPersonalCalendar", new Boolean(enable));
	}

	public void setCalendars(UserRequest ureq, List<KalendarRenderWrapper> calendars) {
		subscriptionIds = CourseCalendarSubscription.getSubscribedCourseCalendarIDs(ureq.getUserSession().getGuiPreferences());
		setCalendars(calendars);
	}
	
	public void setCalendars(List<KalendarRenderWrapper> calendars) {
		this.calendars = calendars;
		for (KalendarRenderWrapper calendar: calendars) {
			calendar.setSubscribed(subscriptionIds.contains(calendar.getKalendar().getCalendarID()));
		}

		configVC.contextPut("calendars", calendars);
	}
	
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == configVC) {
			String command = event.getCommand();
			if (command.equals(CMD_ADD)) {
				// add new event to calendar
				String calendarID = ureq.getParameter(PARAM_ID);
				fireEvent(ureq, new KalendarGUIAddEvent(calendarID, new Date()));
			} else if (command.equals(CMD_IMPORT)) {
				// add new event to calendar
				String calendarID = ureq.getParameter(PARAM_ID);
				fireEvent(ureq, new KalendarGUIImportEvent(calendarID));
			} else if (command.equals(CMD_TOGGLE_DISPLAY)) {
				String calendarID = ureq.getParameter(PARAM_ID);
				KalendarRenderWrapper calendarWrapper = findKalendarRenderWrapper(calendarID);
				KalendarConfig config = calendarWrapper.getKalendarConfig();
				config.setVis(!config.isVis());
				CalendarManagerFactory.getInstance().getCalendarManager().saveKalendarConfigForIdentity(
						config, calendarWrapper.getKalendar(), ureq);
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if (command.equals(CMD_CHOOSE_COLOR)) {
				String calendarID = ureq.getParameter(PARAM_ID);
				lastCalendarWrapper = findKalendarRenderWrapper(calendarID);
				removeAsListenerAndDispose(colorChooser);
				colorChooser = new CalendarColorChooserController(ureq, getWindowControl(), lastCalendarWrapper.getKalendarConfig().getCss());
				listenTo(colorChooser);
				removeAsListenerAndDispose(cmc);
				cmc = new CloseableModalController(getWindowControl(), translate("close"),  colorChooser.getInitialComponent(), false, translate("cal.color.title"));
				listenTo(cmc);
				cmc.activate();
			} else if (command.equals(CMD_ICAL_FEED)) {
				String calendarID = ureq.getParameter(PARAM_ID);
				KalendarRenderWrapper calendarWrapper = findKalendarRenderWrapper(calendarID);
				String calFeedLink = ICalTokenGenerator.getIcalFeedLink(calendarWrapper.getKalendar().getType(), calendarID, ureq.getIdentity());
				exportController = new CalendarExportController(getLocale(), getWindowControl(), calFeedLink);
				listenTo(exportController);
				removeAsListenerAndDispose(cmc);
				cmc = new CloseableModalController(getWindowControl(), translate("close"), exportController.getInitialComponent());
				cmc.activate();
				listenTo(cmc);
			} else if (command.equals(CMD_ICAL_REGENERATE)) {
				currentCalendarID = ureq.getParameter(PARAM_ID);
				confirmRegenerateDialog = activateOkCancelDialog(ureq, translate("cal.icalfeed.regenerate.title"), translate("cal.icalfeed.regenerate.warning"), confirmRegenerateDialog);
			} else if (command.equals(CMD_ICAL_REMOVE_FEED)) {
				currentCalendarID = ureq.getParameter(PARAM_ID);
				confirmRemoveDialog = activateOkCancelDialog(ureq, translate("cal.icalfeed.remove.title"), translate("cal.icalfeed.remove.confirmation_message"), confirmRemoveDialog);
			} else if (command.equals(CMD_UNSUBSCRIBE)) {
				currentCalendarID = ureq.getParameter(PARAM_ID);
				KalendarRenderWrapper calendarWrapper = findKalendarRenderWrapper(currentCalendarID);
				CalendarSubscription subscription = new CourseCalendarSubscription(calendarWrapper.getKalendar(), ureq.getUserSession().getGuiPreferences());
				subscription.unsubscribe();
				
				for (Iterator<KalendarRenderWrapper> it=calendars.iterator(); it.hasNext(); ) {
					KalendarRenderWrapper calendar = it.next();
					if (calendarWrapper.getKalendar().getCalendarID().equals(calendar.getKalendar().getCalendarID())) {
						it.remove();
					}
				}
				configVC.contextPut("calendars", calendars);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == colorChooser) {
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				String choosenColor = colorChooser.getChoosenColor();
				KalendarConfig config = lastCalendarWrapper.getKalendarConfig();
				config.setCss(choosenColor);
				CalendarManagerFactory.getInstance().getCalendarManager().saveKalendarConfigForIdentity(
						config, lastCalendarWrapper.getKalendar(), ureq);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if (source == confirmRemoveDialog ) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				KalendarRenderWrapper calendarWrapper = findKalendarRenderWrapper(currentCalendarID);
				ICalTokenGenerator.destroyIcalAuthToken(calendarWrapper.getKalendar().getType(), currentCalendarID, ureq.getIdentity());							
				showInfo("cal.icalfeed.remove.info");
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if (source == confirmRegenerateDialog) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				KalendarRenderWrapper calendarWrapper = findKalendarRenderWrapper(currentCalendarID);
				ICalTokenGenerator.regenerateIcalAuthToken(calendarWrapper.getKalendar().getType(), currentCalendarID, ureq.getIdentity());			
				String calFeedLink = ICalTokenGenerator.getIcalFeedLink(calendarWrapper.getKalendar().getType(), currentCalendarID, ureq.getIdentity());
				exportController = new CalendarExportController(getLocale(), getWindowControl(), calFeedLink);
				listenTo(exportController);
				removeAsListenerAndDispose(cmc);
				cmc = new CloseableModalController(getWindowControl(), translate("close"), exportController.getInitialComponent());
				cmc.activate();
				listenTo(cmc);		
			}
		}
		configVC.setDirty(true);
	}
	
	private KalendarRenderWrapper findKalendarRenderWrapper(String calendarID) {
		for (KalendarRenderWrapper calendarWrapper : calendars) {
			if (calendarWrapper.getKalendar().getCalendarID().equals(calendarID))
				return calendarWrapper;
		}
		return null;
	}
	

	protected void doDispose() {
		// controllers disposed by BasicController
		cmc = null;
		colorChooser = null;
	}

}
