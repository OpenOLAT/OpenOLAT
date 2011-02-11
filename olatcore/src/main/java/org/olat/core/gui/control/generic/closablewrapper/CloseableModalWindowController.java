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
package org.olat.core.gui.control.generic.closablewrapper;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.winmgr.JSCommand;

/**
 * <h3>Description:</h3> Modal dialog based on Ext.Window <h3>Events thrown by
 * this controller:</h3>
 * <ul>
 * <li>CLOSE_WINDOW_EVENT</li>
 * </ul>
 * <p>
 * Initial Date: 31.10.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 * 
 */
public class CloseableModalWindowController extends BasicController {
	/**
	 * Comment for <code>CLOSE_WINDOW_EVENT</code>
	 */
	public static final Event CLOSE_WINDOW_EVENT = new Event("CLOSE_WINDOW_EVENT");
	
	private final String modalId;

	private Link backIcon;
	private VelocityContainer mainVC;

	public CloseableModalWindowController(UserRequest ureq, WindowControl wControl, String title, Component modalContent, String id) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("modalwindow");
		if (title != null) mainVC.contextPut("title", title);
		mainVC.put("content", modalContent);
		modalId = "o_"+id;
		mainVC.contextPut("panelName", modalId);		
		putInitialPanel(mainVC);
	}

	/**
	 * set the initial size of modal window. if changed by user once, this won't
	 * be read until cookies have expired or got deleted!
	 * 
	 * @param width in pixels
	 * @param height in pixels
	 */
	public void setInitialWindowSize(int width, int height) {
		mainVC.contextPut("width", width);
		mainVC.contextPut("height", height);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == backIcon) {
			fireEvent(ureq, CLOSE_WINDOW_EVENT);
			deactivate();
		} else if ("close".equals(event.getCommand())) {
			fireEvent(ureq, CLOSE_WINDOW_EVENT);
			deactivate();
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#getInitialComponent()
	 */
	public Component getInitialComponent() {
		throw new RuntimeException("please use activate() instead");
	}

	/**
	 * 
	 */
	public void activate() {
		getWindowControl().pushAsModalDialog(super.getInitialComponent());
	}

	/**
	 * deactivates the modal controller. please do use this method here instead of
	 * getWindowControl().pop() !
	 * 
	 */
	public void deactivate() {
		getWindowControl().pop();

		StringBuilder sb = new StringBuilder();
		sb.append("if( Ext.getCmp('").append(modalId).append("') != null ) {").append("  Ext.getCmp('").append(modalId).append("').close();")
				.append("};");

		getWindowControl().getWindowBackOffice().sendCommandTo(new JSCommand(sb.toString()));
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}
}