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
*/
package org.olat.core.commons.fullWebApp.popup;

import org.olat.core.commons.fullWebApp.BaseFullWebappPopupBrowserWindow;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindowController;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindowControllerCreator;

/**
 * Description:<br>
 * This class is configured in _spring/extconfig.xml to be the popup window
 * layout creator.<br>
 * Hence it provides the default popup window layout which is used by olat.core
 * controllers creating popup windows.<br>
 * The OLAT Webapplication may have different popup window layouts. These are
 * defined in an olat layout factory.<br>
 * 
 * @see {@link BaseFullWebappPopupLayoutFactory}
 * 
 * <P>
 * Initial Date: 26.07.2007 <br>
 * @author patrickb
 */
public class BaseFullWebappPopupLayoutCreator implements
		PopupBrowserWindowControllerCreator {

	/**
	 * If the contentController provided is not an instance of
	 * BaseFullWebappPopupLayout - it is wrapped into the default olat popup
	 * layout.<br>
	 * Otherwise the content controller is already wrapped with a Olat Popup
	 * Layout.
	 * 
	 * @see org.olat.core.gui.control.generic.popup.PopupBrowserWindowControllerCreator#createNewPopupBrowserController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.core.gui.control.creator.ControllerCreator)
	 */
	@Override
	public PopupBrowserWindowController createNewPopupBrowserController(
			UserRequest lureq, ControllerCreator contentControllerCreator) {
		/*
		 * the lwControl comes from the new BaseChiefController which represents
		 * the new window.
		 */
		BaseFullWebappPopupLayout oplm;
		if (!(contentControllerCreator instanceof BaseFullWebappPopupLayout)) {
			// wrap non layouted popupbrowser window request into minimial
			// popupwindow
			// this is to wrap popup window creation calls from the olat.core
			// into the
			// the default popup window layout -> e.g. minimal layout == true
			oplm = BaseFullWebappPopupLayoutFactory
					.createAuthMinimalPopupLayout(lureq,
							contentControllerCreator);
		} else {
			// the real content controller creator is wrapped by a layouting
			// pop-up window content controller creator.
			// e.g. using the BaseFullWebappPopupLayoutFactory to wrap it up.
			// ControllerCreator layoutCtrlr =
			// BaseFullWebappPopupLayoutFactory.createAuthHeaderPopupLayout(ureq,
			// ctrlCreator);
			//
			oplm = (BaseFullWebappPopupLayout) contentControllerCreator;
		}

		return new BaseFullWebappPopupBrowserWindow(lureq, oplm.getFullWebappParts());
	}
	
	@Override
	public PopupBrowserWindowController createNewUnauthenticatedPopupWindowController(UserRequest lureq,
			ControllerCreator contentControllerCreator) {
		BaseFullWebappPopupLayout oplm;
		if (!(contentControllerCreator instanceof BaseFullWebappPopupLayout)) {
			oplm = BaseFullWebappPopupLayoutFactory.createMinimalPopupLayout(contentControllerCreator);
		} else {
			oplm = (BaseFullWebappPopupLayout) contentControllerCreator;
		}

		return new BaseFullWebappPopupBrowserWindow(lureq, oplm.getFullWebappParts());
	}
}
