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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.course;

import org.olat.ControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResourceManager;

/**
 * Description:<br>
 * TODO: patrickb Class Description for DisposedCourseRestartController
 * 
 * <P>
 * Initial Date: 19.04.2008 <br>
 * 
 * @author patrickb
 */
class DisposedCourseRestartController extends BasicController {

	private VelocityContainer initialContent;
	private Link restartLink;
	private RepositoryEntry courseRepositoryEntry;
	private Panel panel;

	public DisposedCourseRestartController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseRepositoryEntry) {
		super(ureq, wControl);
		initialContent = createVelocityContainer("disposedcourserestart");
		restartLink = LinkFactory.createButton("course.disposed.command.restart", initialContent, this);
		this.courseRepositoryEntry = courseRepositoryEntry;
		panel = putInitialPanel(initialContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// TODO Auto-generated method stub

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	@SuppressWarnings("unused")
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == restartLink) {
			DTabs dts = (DTabs) getWindowControl().getWindowBackOffice().getWindow().getAttribute("DTabs");
			OLATResourceable ores = OLATResourceManager.getInstance().findResourceable(
					courseRepositoryEntry.getOlatResource().getResourceableId(), courseRepositoryEntry.getOlatResource().getResourceableTypeName());
			if(ores==null) {
				//course was deleted!				
				MessageController msgController = MessageUIFactory.createInfoMessage(ureq, this.getWindowControl(), translate("course.deleted.title"), translate("course.deleted.text"));
				panel.setContent(msgController.getInitialComponent());				
				return;
			}
			DTab dt = dts.getDTab(ores);
			// remove and dispose "old course run"
			dts.removeDTab(dt);//disposes also dt and controllers
			/*
			 * create new tab with "refreshed course run" and activate the course
			 */
			dt = dts.createDTab(ores, courseRepositoryEntry.getDisplayname());
			if (dt == null) return; // full tabs -> warning already set by
															// dts.create...
			Controller launchController = ControllerFactory.createLaunchController(ores, null, ureq, dt.getWindowControl(), true);
			dt.setController(launchController);
			dts.addDTab(dt);
			dts.activate(ureq, dt, null);
			/*
			 * last but not least dispose myself - to clean up.
			 */
			dispose();
		}
	}

}
