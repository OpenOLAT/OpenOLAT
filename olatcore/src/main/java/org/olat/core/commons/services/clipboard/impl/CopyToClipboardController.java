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
package org.olat.core.commons.services.clipboard.impl;

import org.olat.core.commons.services.clipboard.ClipboardEntry;
import org.olat.core.commons.services.clipboard.ClipboardEntryCreator;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * @author Felix Jost, http://www.goodsolutions.ch
 *
 */
public class CopyToClipboardController extends BasicController {

	private final ClipboardServiceImpl cbs;
	private final ClipboardEntryCreator cbCreator;
	private Link copyTo;

	/**
	 * @param ureq
	 * @param wControl
	 */
	CopyToClipboardController(UserRequest ureq, WindowControl wControl, ClipboardServiceImpl cbs, ClipboardEntryCreator cbCreator) {
		super(ureq, wControl);
		this.cbs = cbs;
		this.cbCreator = cbCreator;
		VelocityContainer mainVc = createVelocityContainer("to");
		
		copyTo = LinkFactory.createButton("copyTo", mainVc, this);
		putInitialPanel(mainVc);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == copyTo) {
			ClipboardEntry cbe = cbCreator.createClipboardEntry();
			cbs.setClipboardEntry(cbe);
		}
	}
	
	@Override
	protected void doDispose() {
		// nothing to do
	}


}
