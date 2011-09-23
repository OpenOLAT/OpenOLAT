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
* <p>
*/ 

package org.olat.course.config.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;


/**
 * Description: <br>
 * TODO: patrick Class Description for CourseChatSettingsForm
 * Initial Date: Jun 16, 2005 <br>
 * @author patrick
 */
public class CourseChatSettingsForm extends FormBasicController {

	private SelectionElement isOn;
	private boolean chatEnabled;

	/**
	 * @param name
	 * @param chatEnabled
	 */
	public CourseChatSettingsForm(UserRequest ureq, WindowControl wControl, boolean chatEnabled) {
		super(ureq, wControl);
		this.chatEnabled = chatEnabled;
		initForm (ureq);
	}

	/**
	 * @return if chat is enabled
	 */
	public boolean chatIsEnabled() {
		return isOn.isSelected(0);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		isOn = uifactory.addCheckboxesVertical("isOn", "chkbx.chat.onoff", formLayout, new String[] {"xx"}, new String[] {""}, null, 1);
		isOn.select("xx", chatEnabled);
		
		uifactory.addFormSubmitButton("save", "save", formLayout);
	}

	@Override
	protected void doDispose() {
		//
	}

}