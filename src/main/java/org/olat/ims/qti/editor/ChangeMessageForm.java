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

package org.olat.ims.qti.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;


/**
 * Initial Date: Jan 17, 2006 <br>
 * 
 * @author patrick
 */
public class ChangeMessageForm extends FormBasicController {

	private TextElement userMsg;
	private SelectionElement chkbx;

	/**
	 * @param name
	 */
	public ChangeMessageForm(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm (ureq);
	}

	public boolean hasInformLearners() {
		return chkbx.isSelected(0);
	}

	public String getUserMsg() {
		return userMsg != null ? userMsg.getValue() : null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled (UserRequest ureq) {
		fireEvent (ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("form.chngmsg.infolabel");
		setFormDescription("form.chngmsg.info");
		
		userMsg = uifactory.addTextAreaElement("userMsg", "form.chngmsg.usermsg", -1, 7, 80, true, false, "", formLayout);
		chkbx = uifactory.addCheckboxesVertical("learnerYes", "form.chngmsg.informlearners", formLayout, new String[]{"xx"}, new String[]{null}, 1);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected void doDispose() {
		//
	}

}
