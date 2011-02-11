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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.commons.calendar.ui;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.UserRequest;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.ImportCalendarManager;

import java.util.List;
import java.util.Locale;

/**
 * Description:<BR>
 * Manager for:
 * 1. export settings of OLAT calendars.
 * 2. import of external calendars
 * 3. settings of imported calendars
 * <P>
 * Initial Date:  June 28, 2008
 *
 * @author Udit Sajjanhar
 */
public class ManageCalendarsController extends BasicController {


	private VelocityContainer manageVC;
	private ImportedCalendarConfigurationController importedCalendarConfig;
	private CalendarFileUploadController calFileUpload;
	private CalendarImportByUrlController calImportByUrl;
	private Link importTypeFileButton;
	private Link importTypeUrlButton;
	private CalendarImportNameForm nameForm;
	private Panel panel;
	private String importUrl;

	

	ManageCalendarsController(UserRequest ureq, Locale locale, WindowControl wControl, List importedCalendarWrappers) {
		super(ureq, wControl);
		
		setBasePackage(CalendarManager.class);

		manageVC = createVelocityContainer("manageCalendars");
			
		// Import calendar functionalities
		importedCalendarConfig = new ImportedCalendarConfigurationController(importedCalendarWrappers, ureq, getWindowControl(), true);
		importedCalendarConfig.addControllerListener(this);
		manageVC.put("importedCalendarConfig", importedCalendarConfig.getInitialComponent());
		manageVC.contextPut("importedCalendarWrappers", importedCalendarWrappers);
		
		
		calFileUpload = new CalendarFileUploadController(ureq, locale, wControl);
		listenTo(calFileUpload);

		calImportByUrl = new CalendarImportByUrlController(ureq, wControl);
		listenTo(calImportByUrl);
		
		panel = new Panel("panel");		
		manageVC.put("fileupload", panel);
		manageVC.contextPut("choose", 1);
		
		importTypeFileButton = LinkFactory.createButton("cal.import.type.file", manageVC, this);
		importTypeUrlButton = LinkFactory.createButton("cal.import.type.url", manageVC, this);
	  putInitialPanel(manageVC);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == importTypeFileButton){	
			manageVC.contextPut("choose", 0);
			panel.setContent(calFileUpload.getInitialComponent());
		} else if (source == importTypeUrlButton){	
			manageVC.contextPut("choose", 0);
			panel.setContent(calImportByUrl.getInitialComponent());
		}else {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}	
	
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == nameForm) {
			if (event == Event.DONE_EVENT) {
				ImportCalendarManager.persistCalendar(nameForm.getCalendarName(), ureq, importUrl);
				importUrl = null; // reset importUrl
				// inform the user of successful import
				showInfo("cal.import.success");
				
				// reset the panel to import an another calendar
				manageVC.contextPut("choose", 1);
		
				// update the imported calendar list
				importedCalendarConfig.setCalendars(ImportCalendarManager.getImportedCalendarsForIdentity(ureq));
				
				manageVC.contextPut("importedCalendarWrappers",ImportCalendarManager.getImportedCalendarsForIdentity(ureq));
			} else if (event == Event.CANCELLED_EVENT) {
				// reset the panel to import an another calendar
				manageVC.contextPut("choose", 1);
				panel.setContent(calFileUpload.getInitialComponent());
			}
		} else 
		if (source == calFileUpload || source == calImportByUrl) {
			if (event == Event.DONE_EVENT) {
				// correct file has been uploaded. ask user the name of the calendar
				removeAsListenerAndDispose(nameForm);
				nameForm = new CalendarImportNameForm(ureq, getWindowControl());
				listenTo(nameForm);
				panel.setContent(nameForm.getInitialComponent());
				if (source == calImportByUrl) {
					// store import url for persistCalendar call
					importUrl = calImportByUrl.getImportUrl();
				}
			} else if (event == Event.CANCELLED_EVENT) {
				// reset the panel to import an another calendar
				manageVC.contextPut("choose", 1);
				if (source == calFileUpload) {
					panel.setContent(calFileUpload.getInitialComponent());
				} else {
					panel.setContent(calImportByUrl.getInitialComponent());
				}
			}
		} else if (source == importedCalendarConfig) {
			if (event == Event.CHANGED_EVENT) {
				manageVC.contextPut("importedCalendarWrappers",ImportCalendarManager.getImportedCalendarsForIdentity(ureq));
				manageVC.setDirty(true);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		}
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}

}
