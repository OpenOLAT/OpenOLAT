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

package org.olat.core.gui.control.generic.tabbable;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLATRuntimeException;

/**
 * Description:<br>
 * TODO: patrick Class Description for ActivateableTabbableDefaultController
 * <P>
 * Initial Date: Aug 8, 2005 <br>
 * 
 * @author patrick
 */
public abstract class ActivateableTabbableDefaultController extends TabbableDefaultController implements Activateable {

	public ActivateableTabbableDefaultController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
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

	/**
	 * @see org.olat.core.gui.control.generic.dtabs.Activateable#activate(org.olat.core.gui.UserRequest,
	 *      java.lang.String)
	 */
	public void activate(UserRequest ureq, String viewIdentifier) {
		// viewIdentifier contains key of tab to be activated
		TabbedPane myTabbedPane = getTabbedPane();
		Translator translator = getTranslator();
		String[] paneKeys = getPaneKeys();

		if (myTabbedPane == null) { throw new OLATRuntimeException("tabs not yet added!", new IllegalStateException()); }
		boolean foundKey = false;
		if (paneKeys.length > 0) {
			int i = 0;
			while (!foundKey && i<paneKeys.length) {
				foundKey = viewIdentifier.equals(paneKeys[i]);
				i++;
			}
		}
		if (foundKey) {
			// it is a tab which we know
			myTabbedPane.setSelectedPane(translator.translate(viewIdentifier));
		} else {
			// it may be a tab of our children
			ActivateableTabbableDefaultController[] children = getChildren();
			for (int j = 0; j < children.length; j++) {
				children[j].activate(ureq, viewIdentifier);
			}
		}
		// if no activation happened, at least the first tab is selected.
	}

}
