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
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
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
import org.olat.ims.cp.ContentPackage;

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
public class CPPageEditController extends BasicController {

	private Link closeLink;
	private CPMDFlexiForm mdCtr; // MetadataController
	private ContentPackage cp;
	private CPPage currentPage;

	protected CPPageEditController(UserRequest ureq, WindowControl control, CPPage page, ContentPackage cp) {
		super(ureq, control);
		this.cp = cp;
		currentPage = page;
		mdCtr = new CPMDFlexiForm(ureq, getWindowControl(), currentPage);
		listenTo(mdCtr);
		putInitialPanel(mdCtr.getInitialComponent());
	}

	/**
	 * Returns true, if the currentPage to Edit is a new one (not yet added to the
	 * manifest)
	 * 
	 * @return
	 */
	public boolean isNewPage() {
		return currentPage.getIdentifier().equals("-1");
	}

	protected void newPageAdded(String newNodeID) {
		this.currentPage.setIdentifier(newNodeID);
	}

	/**
	 * returns the CPPage, which is edited
	 * 
	 * @return
	 */
	public CPPage getCurrentPage() {
		return currentPage;
	}

	@Override
	protected void doDispose() {
	// TODO Auto-generated method stub
	}

	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == closeLink) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
	}

	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == mdCtr) {
			currentPage = mdCtr.getPage();
			fireEvent(ureq, event);
		}

	}

}
