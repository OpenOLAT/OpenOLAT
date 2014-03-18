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

package org.olat.ims.cp.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Description:<br>
 * This controller controls the workflows regarding the edit-process of a
 * cp-page
 * 
 * 
 * <P>
 * Initial Date: 25.07.2008 <br>
 * 
 * @author sergio
 */
public class CPMetadataEditController extends BasicController {

	private Link closeLink;
	private CPMDFlexiForm mdCtr; // MetadataController
	private CPPage page;

	protected CPMetadataEditController(UserRequest ureq, WindowControl control, CPPage page) {
		super(ureq, control);
		this.page = page;
		mdCtr = new CPMDFlexiForm(ureq, getWindowControl(), page);
		listenTo(mdCtr);
		putInitialPanel(mdCtr.getInitialComponent());
	}

	protected void newPageAdded(String newNodeID) {
		this.page.setIdentifier(newNodeID);
	}

	/**
	 * returns the CPPage, which is edited
	 * 
	 * @return
	 */
	public CPPage getCurrentPage() {
		return page;
	}

	@Override
	protected void doDispose() {
	// nothing to do 
	}

	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == closeLink) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
	}

	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == mdCtr) {
			page = mdCtr.getPage();
			fireEvent(ureq, event);
		}

	}

}
