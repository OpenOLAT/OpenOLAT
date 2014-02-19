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
import java.io.IOException;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.ImportCalendarManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;

import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;

/**
 * Description:<BR>
 * <P>
 * Initial Date:  July 8, 2008
 *
 * @author Udit Sajjanhar
 */
public class CalendarFileUploadController extends BasicController {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(CalendarManager.class);

	private VelocityContainer calFileUploadVC;
	private Translator translator;
	private static final String COMMAND_PROCESS_UPLOAD = "pul";
	private static final long fileUploadLimit = 1024;
	private Link cancelButton;
	
	
	CalendarFileUploadController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		translator = Util.createPackageTranslator(CalendarManager.class,  ureq.getLocale());
		calFileUploadVC = new VelocityContainer("calmanage", VELOCITY_ROOT + "/calFileUpload.html", translator, this);
		cancelButton = LinkFactory.createButton("cancel", calFileUploadVC, this);
		putInitialPanel(calFileUploadVC);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == cancelButton) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}	else if (source == calFileUploadVC) { // those must be module links
			if (event.getCommand().equals(COMMAND_PROCESS_UPLOAD)) { 
				// process calendar file upload
				processCalendarFileUpload(ureq);
			}
		}
	}	
	
	private void processCalendarFileUpload(UserRequest ureq) {
		// upload the file
		try {
			// don't worry about NullPointerExceptions.
			// we'll catch exceptions if any operation fails.
			MultipartParser mpp = new MultipartParser(ureq.getHttpReq(), (int) fileUploadLimit * 1024);
			mpp.setEncoding("UTF-8");
			Part part;
			boolean fileWritten = false;
			while ((part = mpp.readNextPart()) != null) {
				if (part.isFile() && !fileWritten) {
					FilePart fPart = (FilePart) part;
					String type = fPart.getContentType();
					// get file contents
					logWarn(type + fPart.getFileName(), null);
					if (fPart != null && fPart.getFileName() != null && type.startsWith("text") && (type.toLowerCase().endsWith("calendar"))) {
						
						// store the uploaded file by a temporary name
						CalendarManager calManager = CalendarManagerFactory.getInstance().getCalendarManager();
						String calID = ImportCalendarManager.getTempCalendarIDForUpload(ureq);
						File tmpFile = calManager.getCalendarFile(CalendarManager.TYPE_USER, calID);
						fPart.writeTo(tmpFile);
						
						// try to parse the tmp file
						Object calendar = calManager.readCalendar(CalendarManager.TYPE_USER, calID);
						if (calendar != null) { 
							fileWritten = true;
						}
						
						//the uploaded calendar file is ok.
						fireEvent(ureq, Event.DONE_EVENT);
					}
				} else if (part.isParam()) {
					ParamPart pPart = (ParamPart) part;
					if (pPart.getName().equals("cancel")) {
						// action cancelled
						fireEvent(ureq, Event.CANCELLED_EVENT);
					}
				}
			}

			if (!fileWritten) {
				getWindowControl().setError(translator.translate("cal.import.form.format.error"));
			}

		} catch (IOException ioe) {
			// exceeded UL limit
			logWarn("IOException in CalendarFileUploadController: ", ioe);
			String slimitKB = String.valueOf(fileUploadLimit);
			String supportAddr = WebappHelper.getMailConfig("mailQuota");//->{0} für e-mail support e-mail adresse
			getWindowControl().setError(translator.translate("cal.import.form.limit.error", new String[] { slimitKB, supportAddr }));
			return;
		} catch (OLATRuntimeException e) {
			logWarn("Imported Calendar file not correct. Parsing failed.", e);
			getWindowControl().setError(translator.translate("cal.import.parsing.failed"));
			return;
		}catch (Exception e) {
			logWarn("Exception in CalendarFileUploadController: ", e);
			getWindowControl().setError(translator.translate("cal.import.form.failed"));
			return;
		}
	}

	@Override
	protected void doDispose() {
		// do nothing here yet
	}
}
