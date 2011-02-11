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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.core.commons.services.clipboard.impl;

import org.olat.core.commons.services.clipboard.ClipboardEntry;
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
import org.olat.core.gui.control.floatingresizabledialog.FloatingResizableDialogController;

/**
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class ClipboardTrayController extends BasicController {

	private final ClipboardServiceImpl cbs;
	private Link openDetail;
	private VelocityContainer mainVc;
	private Controller trayContentC;
	private Panel expandedP;
	private ClipboardEntry latestCB;
	private Component detail;
	private FloatingResizableDialogController frdC;

	/**
	 * @param ureq
	 * @param wControl
	 */
	ClipboardTrayController(UserRequest ureq, WindowControl wControl, ClipboardServiceImpl cbs) {
		super(ureq, wControl);
		this.cbs = cbs;
		expandedP = new Panel("trayExpanded");

		mainVc = createVelocityContainer("tray");
		ComponentUtil.registerForValidateEvents(mainVc, this);
		openDetail = LinkFactory.createLink("opendetail", mainVc, this);
		mainVc.put("expanded", expandedP);
		putInitialPanel(mainVc);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (event == ComponentUtil.VALIDATE_EVENT) {
			// before rendering, check if we can still or newly offer the paste
			// button. this is the case when the clipboard contains an entry which
			// matches the desired dataflavor
			ClipboardEntry cbe = cbs.getClipboardEntry();
			if (cbe == null) {
				expandedP.setContent(null);
				if (trayContentC != null) trayContentC.dispose();
			} else {
				if (cbe != latestCB) {
					latestCB = cbe;
					trayContentC = cbe.createTrayUI().createController(ureq, getWindowControl());
					frdC = new FloatingResizableDialogController(ureq, getWindowControl(), trayContentC.getInitialComponent(), "clipboard tray");
					frdC.addControllerListener(this);
					detail = frdC.getInitialComponent();
					expandedP.setContent(detail);
				}
			}
		} else if (source == openDetail) {
			if (detail == null) {
				getWindowControl().setInfo("clipboard is empty");
			} else {
				expandedP.setContent(detail);
			}
		}
	}

	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == frdC) {
			if (event == Event.DONE_EVENT) {
				// close button was clicked -> hide
				expandedP.setContent(null);
			}
		}

	}

	@Override
	protected void doDispose() {
		if (trayContentC != null) trayContentC.dispose();
		if (frdC != null) frdC.dispose();
	}

}
