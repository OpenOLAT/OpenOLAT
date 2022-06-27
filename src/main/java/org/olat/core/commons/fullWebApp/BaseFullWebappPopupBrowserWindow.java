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
package org.olat.core.commons.fullWebApp;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindowController;
import org.olat.core.util.UserSession;

/**
 * @author patrickb
 * 
 */
public class BaseFullWebappPopupBrowserWindow extends BaseFullWebappController implements PopupBrowserWindowController {

	/**
	 * @param ureq
	 * @param ouisc_wControl
	 * @param baseFullWebappControllerParts
	 */
	public BaseFullWebappPopupBrowserWindow(UserRequest ureq,
			BaseFullWebappControllerParts baseFullWebappControllerParts) {
		super(ureq, baseFullWebappControllerParts);
		// apply custom css if available
		if (contentCtrl instanceof MainLayoutController) {
			MainLayoutController mainLayoutCtr = (MainLayoutController) contentCtrl;
			addCurrentCustomCSSToView(mainLayoutCtr.getCustomCSS());
		}
		if(contentCtrl != null) {
			UserSession usess = ureq.getUserSession();
			if(usess != null && usess.getLastHistoryPoint() != null) {
				String path = usess.getLastHistoryPoint().getBusinessPath();
				setStartBusinessPath(path);
			}
		}
	}

	@Override
	public void setForPrint(boolean forPrint) {
		super.setForPrint(forPrint);
	}

	@Override
	public void open(UserRequest ureq) {
		ureq.getDispatchResult().setResultingWindow(getWindowControl().getWindowBackOffice().getWindow());
	}

	@Override
	public WindowControl getPopupWindowControl() {
		return getWindowControl();
	}

}
