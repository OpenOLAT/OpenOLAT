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
* <p>
*/

package org.olat.commons.calendar.ui;

import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.ImportCalendarManager;
import org.olat.commons.calendar.model.KalendarConfig;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.events.KalendarGUIAddEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.Util;



public class ImportedCalendarConfigurationController extends BasicController {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(CalendarManager.class);

	private static final Object CMD_ADD = "add";
	private static final Object CMD_TOGGLE_DISPLAY = "tglvis";
	private static final Object CMD_CHOOSE_COLOR = "cc";
	private static final Object CMD_REMOVE_CALENDAR = "rm";
	private static final String PARAM_ID = "id";

	private VelocityContainer configVC;
	private List<KalendarRenderWrapper> importedCalendarWrappers;
	private CalendarColorChooserController colorChooser;
	private KalendarRenderWrapper lastCalendarWrapper;
	private CloseableModalController cmc;
	private DialogBoxController confirmRemoveDialog;
	private String currentCalendarID;
	private Link manageCalendarsButton;
	private ManageCalendarsController manageCalendarsController;

	public ImportedCalendarConfigurationController(UserRequest ureq, WindowControl wControl,
			List<KalendarRenderWrapper> importedCalendarWrappers, boolean insideManager) {
		super(ureq, wControl);
		this.importedCalendarWrappers = importedCalendarWrappers;
		setTranslator(Util.createPackageTranslator(CalendarManager.class, ureq.getLocale()));
		
		configVC = new VelocityContainer("calEdit", VELOCITY_ROOT + "/importedCalConfig.html", getTranslator(), this);
		configVC.contextPut("calendars", importedCalendarWrappers);
		configVC.contextPut("insideManager", insideManager);
		manageCalendarsButton = LinkFactory.createButton("cal.managecalendars", configVC, this);

		putInitialPanel(configVC);
	}

	public void setCalendars(List<KalendarRenderWrapper> calendars) {
		this.importedCalendarWrappers = calendars;
		configVC.contextPut("calendars", calendars);
	}
	
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == configVC) {
			String command = event.getCommand();
			if (command.equals(CMD_ADD)) {
				// add new event to calendar
				String calendarID = ureq.getParameter(PARAM_ID);
				fireEvent(ureq, new KalendarGUIAddEvent(calendarID, new Date()));
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
				colorChooser = new CalendarColorChooserController(ureq, getWindowControl(), lastCalendarWrapper, lastCalendarWrapper.getKalendarConfig().getCss());
				listenTo(colorChooser);
				removeAsListenerAndDispose(cmc);
				cmc = new CloseableModalController(getWindowControl(), translate("close"), colorChooser.getInitialComponent());
				cmc.activate();
				listenTo(cmc);
			} else if (command.equals(CMD_REMOVE_CALENDAR)) {
				currentCalendarID = ureq.getParameter(PARAM_ID);
				confirmRemoveDialog = activateOkCancelDialog(ureq, translate("cal.import.remove.title"), translate("cal.import.remove.confirmation_message"), confirmRemoveDialog);
			}
		} else if (source == manageCalendarsButton){
			removeAsListenerAndDispose(manageCalendarsController);
			importedCalendarWrappers = ImportCalendarManager.getImportedCalendarsForIdentity(ureq);
			manageCalendarsController = new ManageCalendarsController(ureq, getWindowControl(), importedCalendarWrappers);
			listenTo(manageCalendarsController);
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), this.translate("close"), manageCalendarsController.getInitialComponent());
			cmc.activate();
			listenTo(cmc);
		} 
	}

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
				// remove the imported calendar
				ImportCalendarManager.deleteCalendar(currentCalendarID, ureq);
		
				// update the calendar list
				importedCalendarWrappers = ImportCalendarManager.getImportedCalendarsForIdentity(ureq);
				configVC.contextPut("calendars", importedCalendarWrappers);
				
				// show the information that the calendar has been deleted
				showInfo("cal.import.remove.info");
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if (source == cmc) {
			importedCalendarWrappers = ImportCalendarManager.getImportedCalendarsForIdentity(ureq);
			configVC.setDirty(true);
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
	
	private KalendarRenderWrapper findKalendarRenderWrapper(String calendarID) {
		for (KalendarRenderWrapper calendarWrapper : importedCalendarWrappers) {
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