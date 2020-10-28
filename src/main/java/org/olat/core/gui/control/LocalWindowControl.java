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

package org.olat.core.gui.control;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.info.WindowControlInfo;
import org.olat.core.id.context.BusinessControl;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * Description:<br>
 * Initial Date:  Aug 10, 2005 <br>
 * @author Felix Jost
 */
public class LocalWindowControl implements WindowControl {
	private static final Logger log = Tracing.createLoggerFor(LocalWindowControl.class);
	private final WindowControl origWControl;
	private int localHeight = 0;
	private final WindowControlInfoImpl wci;
	
	LocalWindowControl(WindowControl origWControl, DefaultController defaultcontroller) {
		this.origWControl = origWControl;
		wci = new WindowControlInfoImpl(defaultcontroller, (origWControl == null? null: origWControl.getWindowControlInfo()));
	}

	@Override
	public void pop() {
		if (localHeight > 0) {
			origWControl.pop();
			localHeight--;
		} else {
			log.warn("Cannot pop below surface...");
		}
	}

	@Override
	public void pushAsModalDialog(Component comp) {
		origWControl.pushAsModalDialog(comp);
		localHeight++;
	}

	@Override
	public boolean removeModalDialog(Component comp) {
		boolean removed = origWControl.removeModalDialog(comp);
		if(removed) {
			localHeight--;
		}
		return removed;
	}

	@Override
	public void pushAsTopModalDialog(Component comp) {
		origWControl.pushAsTopModalDialog(comp);
	}

	@Override
	public boolean removeTopModalDialog(Component comp) {
		return origWControl.removeTopModalDialog(comp);
	}

	@Override
	public void pushAsCallout(Component comp, String targetId, CalloutSettings settings) {
		origWControl.pushAsCallout(comp, targetId, settings);
		localHeight++;
	}

	@Override
	public void pushFullScreen(Controller ctrl, String bodyClass) {
		origWControl.pushFullScreen(ctrl, bodyClass);
		localHeight++;
	}

	@Override
	public void pushToMainArea(Component comp) {
		origWControl.pushToMainArea(comp);
		localHeight++;
	}

	@Override
	public void setError(String string) {
		origWControl.setError(string);
	}

	@Override
	public void setInfo(String string) {
		origWControl.setInfo(string);
	}

	@Override
	public void setWarning(String string) {
		origWControl.setWarning(string);
	}

	@Override
	public void makeFlat() {
		for (int i = 0; i < localHeight; i++) {
			origWControl.pop();
		}
		localHeight = 0;
	}

	@Override
	public WindowControlInfo getWindowControlInfo() {
		return wci;
	}

	@Override
	public BusinessControl getBusinessControl() {
		return origWControl.getBusinessControl();
	}

	@Override
	public WindowBackOffice getWindowBackOffice() {
		return origWControl.getWindowBackOffice();
	}
}
