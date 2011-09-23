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

package org.olat.core.gui.control;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.info.WindowControlInfo;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.logging.AssertException;

/**
 * Description:<br>
 * Initial Date:  Aug 10, 2005 <br>
 * @author Felix Jost
 */
public class LocalWindowControl implements WindowControl {
	private final WindowControl origWControl;
	private int localHeight = 0;
	//private final Controller controller;
	private final WindowControlInfoImpl wci;
	
	LocalWindowControl(WindowControl origWControl, DefaultController defaultcontroller) {
		this.origWControl = origWControl;
		wci = new WindowControlInfoImpl(defaultcontroller, (origWControl == null? null: origWControl.getWindowControlInfo()));
	}

	/**
	 * @see org.olat.core.gui.control.WindowControl#pop()
	 */
	public void pop() {
		if (localHeight == 0) throw new AssertException("cannot pop below surface...");
		origWControl.pop();
		localHeight--;
	}

	/**
	 * @see org.olat.core.gui.control.WindowControl#pushAsModalDialog(java.lang.String, org.olat.core.gui.components.Component)
	 */
	public void pushAsModalDialog(Component comp) {
		origWControl.pushAsModalDialog(comp);
		localHeight++;
	}

	/**
	 * @see org.olat.core.gui.control.WindowControl#pushToMainArea(org.olat.core.gui.components.Component)
	 */
	public void pushToMainArea(Component comp) {
		origWControl.pushToMainArea(comp);
		localHeight++;
	}

	/**
	 * @see org.olat.core.gui.control.WindowControl#setError(java.lang.String)
	 */
	public void setError(String string) {
		origWControl.setError(string);
	}

	/**
	 * @see org.olat.core.gui.control.WindowControl#setInfo(java.lang.String)
	 */
	public void setInfo(String string) {
		origWControl.setInfo(string);
	}

	/**
	 * @see org.olat.core.gui.control.WindowControl#setWarning(java.lang.String)
	 */
	public void setWarning(String string) {
		origWControl.setWarning(string);
	}
	
	
	public void makeFlat() {
		for (int i = 0; i < localHeight; i++) {
			origWControl.pop();
		}
		localHeight = 0;
	}

	public WindowControlInfo getWindowControlInfo() {
		return wci;
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.WindowControl#getBusinessControl()
	 */
	public BusinessControl getBusinessControl() {
		return origWControl.getBusinessControl();
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.WindowControl#getWindowBackOffice()
	 */
	public WindowBackOffice getWindowBackOffice() {
		return origWControl.getWindowBackOffice();
	}
}
