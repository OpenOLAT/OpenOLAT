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
import org.olat.core.commons.services.clipboard.ClipboardEntryCreator;
import org.olat.core.commons.services.clipboard.impl.ClipboardServiceImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.util.ComponentUtil;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class PasteFromClipboardController extends BasicController {

	private final ClipboardServiceImpl cbs;
	private Link pasteFrom;
	private VelocityContainer mainVc;
	private final Class[] acceptedFlavorInterfaces;

	/**
	 * @param ureq
	 * @param wControl
	 */
	PasteFromClipboardController(UserRequest ureq, WindowControl wControl, ClipboardServiceImpl cbs, Class[] acceptedFlavorInterfaces) {
		super(ureq, wControl);
		this.cbs = cbs;
		this.acceptedFlavorInterfaces = acceptedFlavorInterfaces;
		mainVc = createVelocityContainer("from");
		ComponentUtil.registerForValidateEvents(mainVc, this);
		pasteFrom = LinkFactory.createButton("pasteFrom", mainVc, this);
		putInitialPanel(mainVc);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (event == ComponentUtil.VALIDATE_EVENT) {
			// before rendering, check if we can still or newly offer the paste
			// button.
			// this is the case when the clipboard contains an entry which matches
			// the desired dataflavor
			ClipboardEntry cbe = cbs.getClipboardEntry();
			if (cbe == null) {
				// or do a setVisible?
				pasteFrom.setEnabled(false);
			} else {
				// there is an entry in the clipboard, check if we can make use of it.
				
				// TODO: Improve this handling
				boolean accepted = false;
				for (int i = 0; !accepted && i < acceptedFlavorInterfaces.length; i++) {
					Class aclass = acceptedFlavorInterfaces[i];
					if (aclass.equals(cbe.getClass())) {
						pasteFrom.setEnabled(true);
						accepted = true;
					}
			  }
			}
		} else if (source == pasteFrom) {
			fireEvent(ureq, new ClipboardEvent(cbs.getClipboardEntry()));
		}
	}
	
	@Override
	protected void doDispose() {
		// nothing to do
	}


}
