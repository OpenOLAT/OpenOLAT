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

package org.olat.course.nodes.ta;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.mail.MailHelper;
import org.olat.course.nodes.TACourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * Initial Date:  30.08.2004
 *
 * @author Mike Stock
 */

public class DropboxForm extends FormBasicController {

	private ModuleConfiguration config;
	private SelectionElement enablemail;
	private TextElement confirmation;
	
	
	/**
	 * Dropbox configuration form.
	 * @param name
	 * @param config
	 * @param ureq
	 */
	public DropboxForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration config) {
		super(ureq, wControl);
		this.config = config;
		initForm(ureq);
	}

	/**
	 * @return mailEnabled field value
	 */
	public boolean mailEnabled() { return enablemail.isSelected(0); }
	
	/**
	 * @return confirmation field value
	 */
	public String getConfirmation() { return confirmation.getValue().trim(); }

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enablemail) {
			confirmation.setMandatory (enablemail.isSelected(0));
			validateFormLogic(ureq);
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		if (confirmation.isMandatory()) {
			if (confirmation.getValue().trim().length()==0) {
				confirmation.setExampleKey("conf.stdtext.example", null);
				confirmation.setErrorKey("error.nomailbody", null);
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("fieldset.dropbox.title");
		setFormContextHelp("Other#_bb_themenvergabe");

		String sConfirmation = (String)config.get(TACourseNode.CONF_DROPBOX_CONFIRMATION);
		if (sConfirmation == null || sConfirmation.length() == 0) {
			// grab standard text
			sConfirmation = translate("conf.stdtext");
			config.set(TACourseNode.CONF_DROPBOX_CONFIRMATION, sConfirmation);
		}
		
		confirmation = uifactory.addTextAreaElement("confirmation", "form.dropbox.confirmation", 2500, 4, 40, true, false, sConfirmation != null ? sConfirmation : "", formLayout);
		MailHelper.setVariableNamesAsHelp(confirmation, DropboxController.variableNames(), getLocale());
		
		Boolean enableMail = (Boolean)config.get(TACourseNode.CONF_DROPBOX_ENABLEMAIL);
		if(enableMail != null) {
			confirmation.setMandatory(enableMail.booleanValue());
		}
		enablemail = uifactory.addCheckboxesHorizontal("enablemail", "form.dropbox.enablemail", formLayout, new String[]{"xx"}, new String[]{null});
		enablemail.select("xx", enableMail != null ? enableMail.booleanValue() : true);
		enablemail.addActionListener(FormEvent.ONCLICK);
	
		uifactory.addFormSubmitButton("submit", formLayout);
	}
}
