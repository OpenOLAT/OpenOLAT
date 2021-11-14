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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.course.db;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.course.ICourse;

/**
 * Initial Date:  7 avr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class CustomDBMainController extends MainLayoutBasicController {
	
	public static final String CUSTOM_DB = "custom_db";

	private final CustomDBController dbController;


	public CustomDBMainController(UserRequest ureq, WindowControl windowControl, ICourse course, boolean readOnly) {
		super(ureq, windowControl);

		dbController = new CustomDBController(ureq, getWindowControl(), course.getResourceableId(), readOnly);
		listenTo(dbController);
		
		LayoutMain3ColsController columnLayoutCtr
			= new LayoutMain3ColsController(ureq, getWindowControl(), null, dbController.getInitialComponent(), "cdb-" + course.getResourceableId());
		listenTo(columnLayoutCtr);
		putInitialPanel(columnLayoutCtr.getInitialComponent());
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
