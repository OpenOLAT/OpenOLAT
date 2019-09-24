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

package org.olat.core.gui.control.generic.tabbable;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;

/**
 * Initial Date: Aug 8, 2005 <br>
 * 
 * @author patrick
 */
public abstract class ActivateableTabbableDefaultController extends TabbableDefaultController implements Activateable2 {

	public ActivateableTabbableDefaultController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
	}

	public ActivateableTabbableDefaultController(UserRequest ureq, WindowControl wControl, Translator fallBackTranslator) {
		super(ureq, wControl, fallBackTranslator);
	}

	/**
	 * @return translation keys for the panes
	 */
	public abstract String[] getPaneKeys();

	/**
	 * @return tabbed pane
	 */
	public abstract TabbedPane getTabbedPane();

	/**
	 * a tabbed pane can be a composition of general to more specific tabs
	 * 
	 * @return
	 */
	protected ActivateableTabbableDefaultController[] getChildren() {
		return new ActivateableTabbableDefaultController[] {};
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		//viewIdentifier contains key of tab to be activated
		TabbedPane myTabbedPane = getTabbedPane();
		if (myTabbedPane == null) return;


		String[] paneKeys = getPaneKeys();
		String tabKey = entries.get(0).getOLATResourceable().getResourceableTypeName();
		boolean foundKey = false;
		if (paneKeys.length > 0) {
			int i = 0;
			while (!foundKey && i<paneKeys.length) {
				foundKey = tabKey.equals(paneKeys[i]);
				i++;
			}
		}
		if (foundKey) {
			// it is a tab which we know
			myTabbedPane.setSelectedPane(ureq, translate(tabKey));
		} else {
			// it may be a tab of our children
			ActivateableTabbableDefaultController[] children = getChildren();
			for (int j = 0; j < children.length; j++) {
				children[j].activate(ureq, entries, state);
			}
		}
		// if no activation happened, at least the first tab is selected.
	}
}
