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

import java.io.File;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.ImportCalendarManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;

/**
 * Description:<BR>
 * <P>
 * Initial Date:  July 8, 2008
 *
 * @author Udit Sajjanhar
 */
public class CalendarFileUploadController extends FormBasicController {

	private FileElement uploadEl;
	
	CalendarFileUploadController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CalendarManager.class,  ureq.getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uploadEl = uifactory.addFileElement("upload", "cal.import.form.prompt", formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("cal.import.form.submit", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		uploadEl.clearError();
		if(uploadEl.getUploadFile() == null) {
			 uploadEl.setErrorKey("form.legende.mandatory", null);
			 allOk &= false;
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		File uploadedFile = uploadEl.getUploadFile();

		// store the uploaded file by a temporary name
		CalendarManager calManager = CalendarManagerFactory.getInstance().getCalendarManager();
		String calID = ImportCalendarManager.getTempCalendarIDForUpload(ureq);
		File tmpFile = calManager.getCalendarFile(CalendarManager.TYPE_USER, calID);
		if(FileUtils.copyFileToFile(uploadedFile, tmpFile, false)) {
			// try to parse the tmp file
			Object calendar = calManager.readCalendar(CalendarManager.TYPE_USER, calID);
			if (calendar != null) { 
				//the uploaded calendar file is ok.
				fireEvent(ureq, Event.DONE_EVENT);
			} else {
				getWindowControl().setError(translate("cal.import.form.format.error"));
			}
		} else {
			getWindowControl().setError(translate("cal.import.form.format.error"));
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}	

	@Override
	protected void doDispose() {
		// do nothing here yet
	}
}
