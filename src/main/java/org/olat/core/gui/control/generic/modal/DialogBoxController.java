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
package org.olat.core.gui.control.generic.modal;

/**

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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;

/**
 * <h3>Description:</h3>
 * The dialog box controller creates a modal dialog box that blocks the user
 * interface until the user clicked on any of the buttons. In most cases
 * developers will use the DialogBoxControllerFactory and not use the generic
 * constructor here.
 * <p>
 * Note that this controller will activate the modal panel itself and also
 * remove the modal panel when the dialog is finished.
 * <h3>Events thrown by this controller:</h3>
 * <ul>
 * <li>ButtonClickedEvent: when user clicks a button provided in the
 * constructor</li>
 * <li>Event.CANCELLED_EVENT: when user clicks the close icon in the window bar</li>
 * </ul>
 * <p>
 * Initial Date: 26.11.2007<br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class DialogBoxController extends BasicController {
	
	private static final String LINK_PREFIX = "link_";
	
	private VelocityContainer dialogBoxVC;
	private Link closeLink;
	private Object userObject = null;

	DialogBoxController(UserRequest ureq, WindowControl control, String title, String text, List<String> buttonLabels) {
		super(ureq, control);
		dialogBoxVC = createVelocityContainer("dialogbox");
		// add optional title to velocity
		if (StringHelper.containsNonWhitespace(title)) {
			dialogBoxVC.contextPut("title", title);
		}
		// add content to velocity
		dialogBoxVC.contextPut("text", text);
		// add optional buttons to velocity
		List<String> buttons = new ArrayList<>();
		if (buttonLabels != null) {
			for (int i = 0; i < buttonLabels.size(); i++) {
				String buttonText = buttonLabels.get(i);
				String linkName = LINK_PREFIX + i;
				Link link = LinkFactory.createButton(linkName, dialogBoxVC, this);
				link.setCustomDisplayText(buttonText);
				// Within a dialog all 'you will loose form data' messages should be
				// suppressed. this is obvious to a user and leads to impossible
				// workflows. See OLAT-4257
				link.setSuppressDirtyFormWarning(true);
				buttons.add(linkName);
			}
		}
		dialogBoxVC.contextPut("buttons", buttons);
		
		// configuration default values:
		setCloseWindowEnabled(true);
		// activate modal dialog now
		putInitialPanel(dialogBoxVC);
	}

	public void setCloseWindowEnabled(boolean closeWindowEnabled) {
		// add optional close icon
		if (closeWindowEnabled){
			String title = translate("close.dialog");
			closeLink = LinkFactory.createIconClose(title, dialogBoxVC, this);
			// Within a dialog all 'you will loose form data' messages should be
			// suppressed. this is obvious to a user and leads to impossible
			// workflows. See OLAT-4257
			closeLink.setSuppressDirtyFormWarning(true);
			// a11y: Set focus to close link and thus to the dialog itself
			closeLink.setFocus(true); 
			closeLink.setTitle(title);
			dialogBoxVC.contextPut("closeIcon", Boolean.TRUE);
		}	else {
			dialogBoxVC.contextPut("closeIcon", Boolean.FALSE);			
		}
	}
	
	/**
	 * Set an optional context help link for this form. If you use a custom
	 * template this will have no effect
	 * 
	 * @param url The page in OpenOlat-docs 
	 */
	public void setContextHelp(String url) {
		if (url == null) {
			dialogBoxVC.contextRemove("off_chelp_url");
		} else {
			dialogBoxVC.contextPut("off_chelp_url", url);
		}
	}

	public void setCssClass(String cssClass) {
		if (StringHelper.containsNonWhitespace(cssClass)) 
			dialogBoxVC.contextPut("cssClass", cssClass);
		else
			dialogBoxVC.contextPut("cssClass", "");
	}
	
	/**
	 * Format the button with the index as a primary button.
	 *
	 * @param index
	 */
	public void setPrimary(int index) {
		Component component = dialogBoxVC.getComponent(LINK_PREFIX + index);
		if (component instanceof Link) {
			Link link = (Link) component;
			link.setPrimary(true);
		}
	}
	
	/**
	 * attach a object to the dialog which you later retrieve.
	 * @param userObject
	 */
	public void setUserObject(Object userObject){
		this.userObject = userObject;
	}
	
	/**
	 * retrieve attached user object.
	 * @return null if no user object was previously set
	 */
	public Object getUserObject(){
		return this.userObject;
	}

	public void activate() {
		getWindowControl().pushAsModalDialog(getInitialComponent());
	}
	
	/**
	 * only needed if you want to remove the dialog without having the user
	 * clicking one of the buttons or the close icon!
	 */
	public void deactivate() {
		getWindowControl().pop();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// in any case pop dialog from modal stack
		deactivate();
		if (source == closeLink) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else {		
			// all events come from link components. detect which one it was
			String sourceName = ((Link) source).getComponentName();
			String linkId = sourceName.substring(sourceName.indexOf("_") + 1);
			int pos = Integer.parseInt(linkId);
			fireEvent(ureq, new ButtonClickedEvent(pos));
		}
	}
}