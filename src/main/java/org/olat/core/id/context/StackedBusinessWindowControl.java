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
* Initial code contributed and copyrighted by<br>
* JGS goodsolutions GmbH, http://www.goodsolutions.ch
* <p>
*/
package org.olat.core.id.context;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.info.WindowControlInfo;

/**
 * Description:<br>
 * 
 * <P>
 * Initial Date:  14.06.2006 <br>
 *
 * @author Felix Jost
 */
public class StackedBusinessWindowControl implements WindowControl {
	private final WindowControl origWControl;
	private final BusinessControl businessControl;

	StackedBusinessWindowControl(WindowControl origWControl, BusinessControl businessControl) {
		this.origWControl = origWControl;
		this.businessControl = businessControl;
		
	}

	@Override
	public BusinessControl getBusinessControl() {
		// inject the new business control here
		return businessControl;
	}

	@Override
	public WindowControlInfo getWindowControlInfo() {
		return origWControl.getWindowControlInfo();
	}

	@Override
	public void makeFlat() {
		origWControl.makeFlat();
	}

	@Override
	public void pop() {
		origWControl.pop();
	}

	@Override
	public void pushAsModalDialog(Component comp) {
		origWControl.pushAsModalDialog(comp);
	}

	@Override
	public boolean removeModalDialog(Component comp) {
		return origWControl.removeModalDialog(comp);
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
	}

	@Override
	public void pushFullScreen(Controller ctrl, String bodyClass) {
		origWControl.pushFullScreen(ctrl, bodyClass);
	}

	@Override
	public void pushToMainArea(Component comp) {
		origWControl.pushToMainArea(comp);
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
	public WindowBackOffice getWindowBackOffice() {
		return origWControl.getWindowBackOffice();
	}
}
