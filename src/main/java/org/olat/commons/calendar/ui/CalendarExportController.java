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

import java.util.Locale;

import org.olat.commons.calendar.CalendarManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;


public class CalendarExportController extends DefaultController {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(CalendarManager.class);

	private Translator translator;
	private VelocityContainer colorVC;

	public CalendarExportController(Locale locale, WindowControl wControl, String icalFeedLink) {
		super(wControl);
		translator = Util.createPackageTranslator(CalendarManager.class, locale);
		
		colorVC = new VelocityContainer("calEdit", VELOCITY_ROOT + "/calIcalFeed.html", translator, this);
		colorVC.contextPut("icalFeedLink", icalFeedLink);

		setInitialComponent(colorVC);
	}
	
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	protected void doDispose() {
		// nothing to dispose
	}
}
